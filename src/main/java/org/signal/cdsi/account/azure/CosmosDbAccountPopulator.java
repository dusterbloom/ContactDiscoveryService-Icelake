/*
 * Copyright 2022 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.signal.cdsi.account.azure;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosPagedIterable;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import org.signal.cdsi.account.AccountPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An AccountPopulator implementation for Azure using Cosmos DB.
 */
public class CosmosDbAccountPopulator implements AccountPopulator {

    private static final Logger logger = LoggerFactory.getLogger(CosmosDbAccountPopulator.class);

    // The container from which to read and update account documents.
    private final CosmosAsyncContainer container;

    public CosmosDbAccountPopulator(CosmosAsyncContainer container) {
        this.container = container;
    }

    /**
     * Example initialization method.
     * (Remove any incorrect @Override annotation if present.)
     */
    public void initialize() {
        // initialization code here if needed
    }

    /**
     * Reads an account document in a blocking manner.
     *
     * @param id  the document id
     * @param key the partition key (typically a string version of the e164 number)
     * @return the AccountDocument read from Cosmos DB
     */
    public AccountDocument readAccountDocument(String id, String key) {
        CosmosItemResponse<AccountDocument> response = container
                .readItem(id, new PartitionKey(key), new CosmosItemRequestOptions(), AccountDocument.class)
                .block();
        return response.getItem();
    }

    /**
     * Returns the total number of account documents in the container.
     * Uses the stream API because CosmosPagedIterable does not have a direct count() method.
     *
     * @return the count of documents
     */
    public long getTotalAccounts() {
        CosmosPagedIterable<AccountDocument> iterable = container.readAllItems(new PartitionKey(""), AccountDocument.class);
        return iterable.stream().count();
    }

    /**
     * Single definition of updateTotalAccountsMetric() (duplicate removed).
     *
     * @return the total number of accounts
     */
    public long updateTotalAccountsMetric() {
        return getTotalAccounts();
    }

    /**
     * Example method that demonstrates converting a primitive long to a String.
     *
     * @param value a long value
     * @return the string representation of the value
     */
    public String convertLongToString(long value) {
        return String.valueOf(value);
    }

    /**
     * Updates an account document (blocking upsert example).
     *
     * @param id     the document id
     * @param key    the partition key
     * @param newDoc the updated document
     */
    public void updateAccountDocument(String id, String key, AccountDocument newDoc) {
        container.upsertItem(newDoc, new PartitionKey(key), new CosmosItemRequestOptions()).block();
    }

    // ---------------------------------------------------------------------
    // Inner class representing an account document stored in Cosmos DB.
    // Adjust fields as needed.
    // ---------------------------------------------------------------------
    public static class AccountDocument {
        private String id;
        private long e164;
        // add other fields as necessary

        // Getters and setters
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public long getE164() {
            return e164;
        }
        public void setE164(long e164) {
            this.e164 = e164;
        }
        // Optionally, override toString(), equals(), and hashCode()
    }

    // ---------------------------------------------------------------------
    // AccountPopulator interface implementations.
    // Adjust these if your interface differs.
    // ---------------------------------------------------------------------
    @Override
    public boolean hasFinishedInitialAccountPopulation() {
        // For example purposes, return true (adjust as needed)
        return true;
    }

    @Override
    public boolean isHealthy() {
        // For example purposes, return true (adjust as needed)
        return true;
    }
}
