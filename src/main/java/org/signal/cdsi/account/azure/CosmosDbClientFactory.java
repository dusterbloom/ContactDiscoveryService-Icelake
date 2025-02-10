// src/main/java/org/signal/cdsi/account/azure/CosmosDbClientFactory.java 
package org.signal.cdsi.account.azure;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;

@Factory
public class CosmosDbClientFactory {
    @Singleton
    CosmosClient cosmosClient(AzureAccountTableConfiguration config) {
        return new CosmosClientBuilder()
            .endpoint(config.getCosmosEndpoint())
            .key(config.getCosmosKey())
            .buildClient();
    }
}