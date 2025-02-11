package org.signal.cdsi.account.azure;

import com.azure.messaging.eventhubs.*;
import com.azure.core.amqp.AmqpRetryOptions;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import java.time.Duration; 

@Factory
public class EventHubClientFactory {
    private final AzureAccountTableConfiguration config;
    private final RetryPolicyConfiguration retryConfig;

    public EventHubClientFactory(
            AzureAccountTableConfiguration config,
            RetryPolicyConfiguration retryConfig) {
        this.config = config;
        this.retryConfig = retryConfig;
    }

    @Singleton
    EventHubConsumerClient eventHubConsumerClient() {
        AmqpRetryOptions retryOptions = new AmqpRetryOptions()
                .setMaxRetries(retryConfig.getMaxRetries())
                .setDelay(retryConfig.getInitialRetryInterval())
                .setMaxDelay(retryConfig.getMaxRetryInterval())
                .setTryTimeout(Duration.ofSeconds(60));

        return new EventHubClientBuilder()
                .connectionString(config.getEventHubConnectionString())
                .consumerGroup(config.getEventHubConsumerGroup())
                .retry(retryOptions)
                .buildConsumerClient();
    }

    @Singleton
    EventHubProducerClient eventHubProducerClient() {
        AmqpRetryOptions retryOptions = new AmqpRetryOptions()
                .setMaxRetries(retryConfig.getMaxRetries())
                .setDelay(retryConfig.getInitialRetryInterval())
                .setMaxDelay(retryConfig.getMaxRetryInterval())
                .setTryTimeout(Duration.ofSeconds(60));

        return new EventHubClientBuilder()
            .connectionString(config.getEventHubConnectionString())
            .retry(retryOptions)
            .buildProducerClient();
    }
}