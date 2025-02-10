package org.signal.cdsi.account.azure;

import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@Property(name = "azure.eventhub.connection-string", value = "Endpoint=sb://test.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=test")
@Property(name = "azure.eventhub.name", value = "test-hub")
@Property(name = "azure.eventhub.consumer-group", value = "$Default")
class EventHubClientFactoryTest {

    @Inject
    EventHubClientFactory clientFactory;

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_EVENTHUB_CONNECTION_STRING", matches = ".*")
    void shouldCreateConsumerClient() {
        // Act
        EventHubConsumerClient client = clientFactory.eventHubConsumerClient();

        // Assert
        assertNotNull(client);
        assertEquals("test-hub", client.getEventHubName());
        assertEquals("$Default", client.getConsumerGroup());
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_EVENTHUB_CONNECTION_STRING", matches = ".*")
    void shouldCreateProducerClient() {
        // Act
        EventHubProducerClient client = clientFactory.eventHubProducerClient();

        // Assert
        assertNotNull(client);
        assertEquals("test-hub", client.getEventHubName());
    }

    @Test
    void shouldReuseClientsWhenRequestedMultipleTimes() {
        // Act
        EventHubConsumerClient consumer1 = clientFactory.eventHubConsumerClient();
        EventHubConsumerClient consumer2 = clientFactory.eventHubConsumerClient();
        
        EventHubProducerClient producer1 = clientFactory.eventHubProducerClient();
        EventHubProducerClient producer2 = clientFactory.eventHubProducerClient();

        // Assert
        assertSame(consumer1, consumer2, "Consumer clients should be singleton");
        assertSame(producer1, producer2, "Producer clients should be singleton");
    }
}