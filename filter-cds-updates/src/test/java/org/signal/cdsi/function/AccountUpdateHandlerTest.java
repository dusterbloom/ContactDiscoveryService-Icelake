package org.signal.cdsi.function;

import com.azure.messaging.eventhubs.EventData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.signal.cdsi.account.AccountUpdateValidator;
import org.signal.cdsi.enclave.DirectoryEntry;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountUpdateHandlerTest {

    @Mock
    private AccountUpdateValidator validator;
    
    private AccountUpdateHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new AccountUpdateHandler(validator);
    }

    @Test
    void shouldHandleValidUpdate() {
        // Arrange
        DirectoryEntry entry = DirectoryEntry.newBuilder()
                .setE164(1234567890L)
                .setAci(9876543210L)
                .setPni(1122334455L)
                .build();
        
        EventData event = new EventData(entry.toByteArray());
        when(validator.isValid(any())).thenReturn(true);

        // Act
        Optional<DirectoryEntry> result = handler.handleUpdate(event);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1234567890L, result.get().getE164());
        assertEquals(9876543210L, result.get().getAci());
        assertEquals(1122334455L, result.get().getPni());
        verify(validator).isValid(any());
    }

    @Test
    void shouldFilterInvalidUpdate() {
        // Arrange
        DirectoryEntry entry = DirectoryEntry.newBuilder()
                .setE164(1234567890L)
                .build();
        
        EventData event = new EventData(entry.toByteArray());
        when(validator.isValid(any())).thenReturn(false);

        // Act
        Optional<DirectoryEntry> result = handler.handleUpdate(event);

        // Assert
        assertTrue(result.isEmpty());
        verify(validator).isValid(any());
    }

    @Test
    void shouldHandleCorruptedEvent() {
        // Arrange
        EventData event = new EventData(new byte[]{1, 2, 3}); // Invalid protobuf data

        // Act
        Optional<DirectoryEntry> result = handler.handleUpdate(event);

        // Assert
        assertTrue(result.isEmpty());
        verify(validator, never()).isValid(any());
    }

    @Test
    void shouldHandleNullEvent() {
        // Act & Assert
        assertDoesNotThrow(() -> handler.handleUpdate(null));
        assertTrue(handler.handleUpdate(null).isEmpty());
    }

    @Test
    void shouldHandleValidatorException() {
        // Arrange
        DirectoryEntry entry = DirectoryEntry.newBuilder()
                .setE164(1234567890L)
                .build();
        
        EventData event = new EventData(entry.toByteArray());
        when(validator.isValid(any())).thenThrow(new RuntimeException("Test exception"));

        // Act
        Optional<DirectoryEntry> result = handler.handleUpdate(event);

        // Assert
        assertTrue(result.isEmpty());
    }
}