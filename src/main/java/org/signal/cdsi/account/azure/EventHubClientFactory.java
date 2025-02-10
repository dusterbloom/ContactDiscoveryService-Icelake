import com.azure.messaging.eventhubs.*;
import com.azure.identity.DefaultAzureCredential;
import jakarta.inject.Singleton;

@Singleton
public class EventHubClientFactory {
    private final AzureAccountTableConfiguration config;

    public EventHubClientFactory(AzureAccountTableConfiguration config) {
        this.config = config;
    }

    @Singleton
    EventHubConsumerClient eventHubConsumerClient() {
        return new EventHubClientBuilder()
            .connectionString(config.getEventHubConnectionString())
            .consumerGroup(config.getEventHubConsumerGroup())
            .buildConsumerClient();
    }
}