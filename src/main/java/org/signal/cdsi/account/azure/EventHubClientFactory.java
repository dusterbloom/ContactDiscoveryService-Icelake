package org.signal.cdsi.account.azure;

import com.azure.messaging.eventhubs.*;
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
            .eventHubName(config.getEventHubName())
            .buildConsumerClient();
    }

    @Singleton
    EventHubProducerClient eventHubProducerClient() {
        return new EventHubClientBuilder()
            .connectionString(config.getEventHubConnectionString())
            .eventHubName(config.getEventHubName())
            .buildProducerClient();
    }
}