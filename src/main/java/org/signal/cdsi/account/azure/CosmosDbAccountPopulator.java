package org.signal.cdsi.account.azure;

import jakarta.inject.Singleton;
import io.micronaut.scheduling.annotation.Scheduled;
import com.azure.cosmos.CosmosClient;
import org.signal.cdsi.account.AccountPopulator;
import org.signal.cdsi.enclave.DirectoryEntry;

@Singleton
public class CosmosDbAccountPopulator implements AccountPopulator {
   private final CosmosClient cosmosClient;
   
   public CosmosDbAccountPopulator(CosmosClient cosmosClient) {
       this.cosmosClient = cosmosClient;
   }
    // Replace ULong usage with NumberUtil methods
    private String formatE164(long number) {
        return NumberUtil.formatE164(number);
    }
    private static final Logger logger = LoggerFactory.getLogger(CosmosDbAccountPopulator.class);
    private static final Duration METRICS_REFRESH_INTERVAL = Duration.ofMinutes(1);

    private final CosmosContainer container;
    private final MeterRegistry meterRegistry;
    private final AtomicLong totalAccounts;
    private final AtomicLong processedUpdates;
    private volatile boolean running;
    private volatile Instant lastUpdateTime;

    public CosmosDbAccountPopulator(
            final CosmosClient cosmosClient,
            final String databaseName,
            final String containerName,
            final MeterRegistry meterRegistry) {
        
        this.container = cosmosClient.getDatabase(databaseName).getContainer(containerName);
        this.meterRegistry = meterRegistry;
        this.totalAccounts = new AtomicLong(0);
        this.processedUpdates = new AtomicLong(0);
        this.lastUpdateTime = Instant.now();
        
        registerMetrics();
    }

    private void registerMetrics() {
        meterRegistry.gauge("accounts.total", totalAccounts);
        meterRegistry.gauge("accounts.updates.processed", processedUpdates);
        meterRegistry.gauge("accounts.last_update_age_seconds", 
            this,
            populator -> Duration.between(populator.lastUpdateTime, Instant.now()).getSeconds());
    }

    @Override
    public void loadAccounts(final DirectoryEntry entry) {
        if (!running) {
            logger.warn("Account populator is not running; discarding update");
            return;
        }

        try {
            final String e164 = ULong.toString(entry.getE164());
            final AccountDocument document = buildAccountDocument(entry);

            container.upsertItem(document, new PartitionKey(e164), new CosmosItemRequestOptions())
                    .flatMap(response -> {
                        processedUpdates.incrementAndGet();
                        lastUpdateTime = Instant.now();
                        return updateTotalAccountsMetric();
                    })
                    .onErrorResume(error -> {
                        logger.error("Failed to update account document: {}", e164, error);
                        meterRegistry.counter("accounts.update.errors").increment();
                        return Mono.empty();
                    })
                    .block();  // Block since the interface is synchronous

        } catch (Exception e) {
            logger.error("Error processing account update", e);
            meterRegistry.counter("accounts.update.errors").increment();
        }
    }

    private Mono<Void> updateTotalAccountsMetric() {
        return container.readAllItems(new PartitionKey(""), AccountDocument.class)
                .count()
                .doOnNext(count -> totalAccounts.set(count))
                .then();
    }

    private AccountDocument buildAccountDocument(DirectoryEntry entry) {
        AccountDocument document = new AccountDocument();
        document.setId(ULong.toString(entry.getE164()));
        document.setE164(entry.getE164());
        
        if (entry.hasAci()) {
            document.setAci(entry.getAci());
        }
        
        if (entry.hasPni()) {
            document.setPni(entry.getPni());
        }
        
        if (entry.hasUak()) {
            document.setUak(entry.getUak());
        }
        
        document.setLastUpdate(Instant.now());
        return document;
    }

    @Override
    public void start() {
        logger.info("Starting CosmosDB account populator");
        running = true;

        // Verify connection and container access
        try {
            container.readItem("test", new PartitionKey("test"), Object.class)
                    .onErrorResume(error -> {
                        if (!error.getMessage().contains("NotFound")) {
                            logger.error("Failed to verify CosmosDB access", error);
                        }
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception e) {
            logger.error("Failed to start CosmosDB account populator", e);
            running = false;
            throw e;
        }
    }

    @Override
    public void stop() {
        logger.info("Stopping CosmosDB account populator");
        running = false;
    }

    private static class AccountDocument {
        private String id;
        private long e164;
        private Long aci;
        private Long pni;
        private Long uak;
        private Instant lastUpdate;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public long getE164() { return e164; }
        public void setE164(long e164) { this.e164 = e164; }
        
        public Long getAci() { return aci; }
        public void setAci(Long aci) { this.aci = aci; }
        
        public Long getPni() { return pni; }
        public void setPni(Long pni) { this.pni = pni; }
        
        public Long getUak() { return uak; }
        public void setUak(Long uak) { this.uak = uak; }
        
        public Instant getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(Instant lastUpdate) { this.lastUpdate = lastUpdate; }
    }
}