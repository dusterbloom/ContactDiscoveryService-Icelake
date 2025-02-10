package org.signal.cdsi.account.azure;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Factory
public class CosmosDbClientFactory {

    @Value("${azure.cosmos.endpoint}")
    private String endpoint;

    @Value("${azure.cosmos.key}")
    private String key;

    @Value("${azure.cosmos.database}")
    private String database;

    @Value("${azure.cosmos.container}")
    private String container;

    @Value("${azure.cosmos.preferredRegions}")
    private String preferredRegions;

    @Singleton
    CosmosClient cosmosClient() {
        return new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key)
                .preferredRegions(List.of(preferredRegions.split(",")))
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildClient();
    }
}