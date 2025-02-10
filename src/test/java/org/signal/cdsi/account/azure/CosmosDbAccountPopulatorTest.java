package org.signal.cdsi.account.azure;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.signal.cdsi.enclave.DirectoryEntry;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CosmosDbAccountPopulatorTest {

    private CosmosClient cosmosClient;
    private CosmosContainer container;
    private MeterRegistry meterRegistry;
    private CosmosDbAccountPopulator populator;

    @BeforeEach
    void setUp() {
        cosmosClient = mock(CosmosClient.class);
        CosmosDatabase database = mock(CosmosDatabase.class);
        container = mock(CosmosContainer.class);
        meterRegistry = new SimpleMeterRegistry();

        when(cosmosClient.getDatabase(anyString())).thenReturn(database);
        when(database.getContainer(anyString())).thenReturn(container);
        when(container.upsertItem(any(), any(), any())).thenReturn(Mono.just(mock(CosmosItemResponse.class)));

        populator = new CosmosDbAccountPopulator(cosmosClient, "test-db", "test-container", meterRegistry);
        populator.start();
    }

    @Test
    void shouldStoreValidAccountUpdate() {
        // Arrange
        DirectoryEntry entry = DirectoryEntry.newBuilder()
                .setE164(1234567890L)
                .setAci(9876543210L)
                .setPni(1122334455L)
                .setUak(5544332211L)
                .build();

        // Act
        populator.loadAccounts(entry);

        // Assert
        ArgumentCaptor<AccountDocument> documentCaptor = ArgumentCaptor.forClass(AccountDocument.class);
        verify(container).upsertItem(documentCaptor.capture(), any(), any());

        AccountDocument storedDocument = documentCaptor.getValue();
        assertEquals("1234567890", storedDocument.getId());
        assertEquals(1234567890L, storedDocument.getE164());
        assertEquals(9876543210L, storedDocument.getAci());
        assertEquals(1122334455L, storedDocument.getPni());
        assertEquals(5544332211L, storedDocument.getUak());
        assertNotNull(storedDocument.getLastUpdate());
    }

    @Test
    void shouldHandlePartialAccountUpdate() {
        // Arrange
        DirectoryEntry entry = DirectoryEntry.newBuilder()
                .setE164(1234567890L)
                .setPni(1122334455L)  // Only E164 and PNI
                .build();

        // Act
        populator.loadAccounts(entry);

        // Assert
        ArgumentCaptor<AccountDocument> documentCaptor = ArgumentCaptor.forClass(AccountDocument.class);
        verify(container).upsertItem(documentCaptor.capture(), any(), any());

        AccountDocument storedDocument = documentCaptor.getValue();
        assertEquals("1234567890", storedDocument.getId());
        assertEquals(1234567890L, storedDocument.getE164());
        assertNull(storedDocument.getAci());  // Should be null since not in update
        assertEquals(1122334455L, storedDocument.getPni());
        assertNull(storedDocument.getUak());  // Should be null since not in update
    }

    @Test
    void shouldIncrementMetricsOnSuccess() {
        // Arrange
        DirectoryEntry entry = DirectoryEntry.newBuilder()
                .setE164(1234567890L)
                .setAci(9876543210L)
                .build();

        // Act
        populator.loadAccounts(entry);

        // Assert
        assertEquals(1, meterRegistry.counter("accounts.update.processed").count());
        assertEquals(0, meterRegistry.counter("accounts.update.errors").count());
    }

    @Test
    void shouldHandleCosmosDbError() {
        // Arrange
        when(container.upsertItem(any(), any(), any()))
                .thenReturn(Mono.error(new RuntimeException("Test error")));

        DirectoryEntry entry = DirectoryEntry.newBuilder()
                .setE164(1234567890L)
                .build();

        // Act
        populator.loadAccounts(entry);

        // Assert
        assertEquals(0, meterRegistry.counter("accounts.update.processed").count());
        assertEquals(1, meterRegistry.counter("accounts.update.errors").count());
    }

    @Test
    void shouldNotProcessUpdatesWhenStopped() {
        // Arrange
        DirectoryEntry entry = DirectoryEntry.newBuilder()
                .setE164(1234567890L)
                .build();

        // Act
        populator.stop();
        populator.loadAccounts(entry);

        // Assert
        verify(container, never()).upsertItem(any(), any(), any());
    }

    @Test
    void shouldMaintainLastUpdateTimestamp() {
        // Arrange
        DirectoryEntry entry = DirectoryEntry.newBuilder()
                .setE164(1234567890L)
                .build();

        // Act
        populator.loadAccounts(entry);

        // Assert
        double lastUpdateAge = meterRegistry.gauge("accounts.last_update_age_seconds", populator);
        assertNotNull(lastUpdateAge);
        assertTrue(lastUpdateAge >= 0);
    }
}