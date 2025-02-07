package org.signal.cdsi;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class FilterCdsUpdatesFunction {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @FunctionName("FilterCdsUpdates")
    @EventHubOutput(
        name = "output",
        eventHubName = "%EVENT_HUB_NAME%", 
        connection = "EventHubConnectionString"
    )
    public String run(
        @CosmosDBTrigger(
            name = "input",
            databaseName = "accountDatabase",
            collectionName = "accountContainer",
            connectionStringSetting = "CosmosDBConnection",
            leaseCollectionName = "leases",
            createLeaseCollectionIfNotExists = true
        )
        String[] documents,
        final ExecutionContext context
    ) {
        try {
            for (String document : documents) {
                JsonNode change = OBJECT_MAPPER.readTree(document);
                
                // Extract old and new images
                JsonNode oldImage = change.get("previousData");
                JsonNode newImage = change.get("data");
                
                if (oldImage == null || oldImage.isEmpty()) {
                    // Insert case
                    Account newAccount = OBJECT_MAPPER.treeToValue(newImage, Account.class);
                    return OBJECT_MAPPER.writeValueAsString(newAccount);
                } else if (newImage == null || newImage.isEmpty()) {
                    // Delete case
                    Account oldAccount = OBJECT_MAPPER.treeToValue(oldImage, Account.class);
                    return OBJECT_MAPPER.writeValueAsString(oldAccount.forceNotInCds());
                }
                
                // Update case
                Account oldAccount = OBJECT_MAPPER.treeToValue(oldImage, Account.class);
                Account newAccount = OBJECT_MAPPER.treeToValue(newImage, Account.class);
                
                if (!oldAccount.e164.equals(newAccount.e164)) {
                    // Phone number change - emit both records
                    return OBJECT_MAPPER.writeValueAsString(new Account[]{
                        oldAccount.forceNotInCds(),
                        newAccount
                    });
                }
                
                if (!oldAccount.equals(newAccount)) {
                    // Other changes - emit new record
                    return OBJECT_MAPPER.writeValueAsString(newAccount);
                }
            }
            
            return null; // No updates needed
            
        } catch (Exception e) {
            context.getLogger().severe("Error processing document: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}