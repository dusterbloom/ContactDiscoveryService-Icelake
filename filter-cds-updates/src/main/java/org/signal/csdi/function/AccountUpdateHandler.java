package org.signal.cdsi.account.azure.function;

import com.azure.messaging.eventhubs.EventData;
import org.signal.cdsi.account.AccountUpdateValidator;
import org.signal.cdsi.enclave.DirectoryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AccountUpdateHandler {
    private static final Logger logger = LoggerFactory.getLogger(AccountUpdateHandler.class);

    private final AccountUpdateValidator validator;

    public AccountUpdateHandler(AccountUpdateValidator validator) {
        this.validator = validator;
    }

    public Optional<DirectoryEntry> handleUpdate(EventData event) {
        try {
            DirectoryEntry entry = DirectoryEntry.parseFrom(event.getBody());

            if (!validator.isValid(entry)) {
                logger.debug("Skipping invalid directory entry");
                return Optional.empty();
            }

            return Optional.of(entry);
        } catch (Exception e) {
            logger.warn("Error processing account update", e);
            return Optional.empty();
        }
    }
}