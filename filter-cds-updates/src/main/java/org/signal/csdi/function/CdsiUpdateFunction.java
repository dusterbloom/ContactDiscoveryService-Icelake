package org.signal.cdsi.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CdsiUpdateFunction {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String KEY_ACCOUNT_UUID = "U";
    private static final String ATTR_ACCOUNT_E164 = "P";
    private static final String ATTR_PNI_UUID = "PNI";
    private static final String ATTR_CANONICALLY_DISCOVERABLE = "C";
    private static final String ATTR_UAK = "UAK";

    @FunctionName("ProcessCdsiUpdates")
    @EventHubOutput(
        name = "output",
        eventHubName = "%EventHubName%",
        connection = "EventHubConnectionString"
    )
    public String run(
        @CosmosDBTrigger(
            name = "input",
            databaseName = "accountDatabase",
            collectionName = "accountContainer",
            leaseCollectionName = "leases",
            createLeaseCollectionIfNotExists = true,
            connectionStringSetting = "CosmosDBConnection"
        )
        String[] documents,
        final ExecutionContext context
    ) {
        try {
            List<Account> updates = new ArrayList<>();
            
            for (String document : documents) {
                JsonNode change = OBJECT_MAPPER.readTree(document);
                
                // Get the previous and new data from the Cosmos DB change feed
                JsonNode oldImage = change.get("previousData");
                JsonNode newImage = change.get("data");
                
                List<Account> accountUpdates = processAccountUpdate(oldImage, newImage);
                updates.addAll(accountUpdates);
            }
            
            return OBJECT_MAPPER.writeValueAsString(updates);
        } catch (Exception e) {
            context.getLogger().severe("Error processing documents: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private List<Account> processAccountUpdate(JsonNode oldImage, JsonNode newImage) {
        List<Account> updates = new ArrayList<>();

        try {
            if (oldImage == null || oldImage.isEmpty()) {
                // This is an insert, respect "should-be-in-cds"
                Account newAccount = accountFromJson(newImage);
                if (newAccount != null) {
                    updates.add(newAccount);
                }
            } else if (newImage == null || newImage.isEmpty()) {
                // This is a delete - create a deletion entry
                Account oldAccount = accountFromJson(oldImage);
                if (oldAccount != null) {
                    updates.add(oldAccount.forceNotInCds());
                }
            } else {
                // This is an update - handle both old and new states
                Account oldAccount = accountFromJson(oldImage);
                Account newAccount = accountFromJson(newImage);
                
                if (oldAccount != null && newAccount != null) {
                    if (!oldAccount.e164().equals(newAccount.e164())) {
                        // Phone number changed - emit both records
                        updates.add(oldAccount.forceNotInCds());
                        updates.add(newAccount);
                    } else if (!oldAccount.equals(newAccount)) {
                        // Other changes - emit new record
                        updates.add(newAccount);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process account update", e);
        }

        return updates;
    }

    private Account accountFromJson(JsonNode item) {
        if (item == null || item.isEmpty()) {
            return null;
        }

        try {
            String e164String = item.get(ATTR_ACCOUNT_E164).asText();
            // Remove '+' prefix and parse to long
            long e164 = Long.parseLong(e164String.substring(1));
            
            UUID uuid = UUIDFromBytes(item.get(KEY_ACCOUNT_UUID).binaryValue());
            UUID pni = UUIDFromBytes(item.get(ATTR_PNI_UUID).binaryValue());
            boolean canonicallyDiscoverable = item.has(ATTR_CANONICALLY_DISCOVERABLE) && 
                                            item.get(ATTR_CANONICALLY_DISCOVERABLE).asBoolean();
            
            byte[] uak = null;
            if (item.has(ATTR_UAK) && !item.get(ATTR_UAK).isNull()) {
                uak = item.get(ATTR_UAK).binaryValue();
            }

            return new Account(e164, uuid, pni, uak, canonicallyDiscoverable);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse account from JSON", e);
        }
    }

    private UUID UUIDFromBytes(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            throw new IllegalArgumentException("Invalid UUID bytes");
        }
        
        long msb = 0;
        long lsb = 0;
        
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (bytes[i] & 0xff);
        }
        
        return new UUID(msb, lsb);
    }

    private record Account(
        long e164,
        UUID uuid,
        UUID pni,
        byte[] uak,
        boolean canonicallyDiscoverable
    ) {
        Account forceNotInCds() {
            return new Account(e164, uuid, pni, uak, false);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Account account = (Account) o;
            return e164 == account.e164 &&
                   canonicallyDiscoverable == account.canonicallyDiscoverable &&
                   uuid.equals(account.uuid) &&
                   pni.equals(account.pni) &&
                   java.util.Arrays.equals(uak, account.uak);
        }
    }
}