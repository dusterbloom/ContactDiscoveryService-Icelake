/*
 * Copyright 2022 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.signal.cdsi.account.azure;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.ConsistencyLevel;
// Use Jakarta inject (Micronaut uses jakarta.inject now)
import jakarta.inject.Singleton;

/**
 * Factory for creating a CosmosAsyncClient using settings from AzureAccountTableConfiguration.
 */
@Singleton
public class CosmosDbClientFactory {

    private final AzureAccountTableConfiguration config;

    public CosmosDbClientFactory(AzureAccountTableConfiguration config) {
        this.config = config;
    }

    @Singleton
    public CosmosAsyncClient cosmosAsyncClient() {
        return new CosmosClientBuilder()
                .endpoint(config.getCosmosDbEndpoint())  // Make sure your configuration defines getCosmosDbEndpoint()
                .key(config.getCosmosDbKey())            // ...and getCosmosDbKey()
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildAsyncClient();
    }
}
