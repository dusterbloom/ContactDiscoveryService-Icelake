// CosmosDbClientFactory.java
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;

@Singleton
public class CosmosDbClientFactory {
    @Singleton
    CosmosClient cosmosClient(AzureAccountTableConfiguration config) {
        return new CosmosClientBuilder()
            .endpoint(config.getCosmosDbEndpoint())
            .key(config.getCosmosDbKey())
            .consistencyLevel(ConsistencyLevel.SESSION)
            .buildClient();
    }
}