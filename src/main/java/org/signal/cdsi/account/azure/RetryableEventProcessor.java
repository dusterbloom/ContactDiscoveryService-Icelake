package org.signal.cdsi.account.azure;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import io.micronaut.context.annotation.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

@Context
public class RetryableEventProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RetryableEventProcessor.class);
    
    private final RetryPolicyConfiguration retryConfig;

    public RetryableEventProcessor(RetryPolicyConfiguration retryConfig) {
        this.retryConfig = retryConfig;
    }

    public Consumer<EventContext> processEvent() {
        return eventContext -> {
            try {
                EventData event = eventContext.getEventData();
                // Process your event here
                eventContext.updateCheckpoint();
            } catch (Exception e) {
                logger.error("Error processing event", e);
                throw e; // Will trigger retry based on retry policy
            }
        };
    }

    public Consumer<ErrorContext> processError() {
        return errorContext -> {
            logger.error("Error occurred in EventProcessor: {}", errorContext.getThrowable().getMessage());
        };
    }
}