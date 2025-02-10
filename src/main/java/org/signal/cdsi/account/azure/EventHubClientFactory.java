package org.signal.cdsi.account.azure;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import com.azure.messaging.eventhubs.*;

@Factory
public class EventHubClientFactory {
    @Singleton
    EventHubConsumerClient eventHubConsumerClient(AzureAccountTableConfiguration config) {
        return new EventHubClientBuilder()
            .connectionString(config.getEventHubConnectionString())
            .consumerGroup(config.getEventHubConsumerGroup())
            .buildConsumerClient();
    }

    @Singleton
    EventHubProducerClient eventHubProducerClient(AzureAccountTableConfiguration config) {
        return new EventHubClientBuilder()
            .connectionString(config.getEventHubConnectionString())
            .buildProducerClient();
    }
}