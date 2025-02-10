package org.signal.cdsi.account.azure;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import org.signal.cdsi.account.AccountPopulator;
import org.signal.cdsi.enclave.DirectoryEntry;
import org.signal.cdsi.metrics.MetricsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class CosmosDbAccountPopulator implements AccountPopulator {
   private static final Logger logger = LoggerFactory.getLogger(CosmosDbAccountPopulator.class);
   private static final Duration METRICS_REFRESH_INTERVAL = Duration.ofMinutes(1);

   private final CosmosContainer container;
   private final MeterRegistry meterRegistry;
   private final AtomicLong totalAccounts;
   private final AtomicLong processedUpdates;
   private volatile boolean running;
   private volatile Instant lastUpdateTime;

   public CosmosDbAccountPopulator(
           final CosmosContainer container,
           final MeterRegistry meterRegistry) {
       this.container = container;
       this.meterRegistry = meterRegistry;
       this.totalAccounts = new AtomicLong(0);
       this.processedUpdates = new AtomicLong(0);
       this.lastUpdateTime = Instant.now();
       
       registerMetrics();
   }

   private void registerMetrics() {
       meterRegistry.gauge(MetricsUtil.name(getClass(), "accounts.total"), totalAccounts);
       meterRegistry.gauge(MetricsUtil.name(getClass(), "accounts.updates.processed"), processedUpdates);
       meterRegistry.gauge(MetricsUtil.name(getClass(), "accounts.last_update_age_seconds"), 
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
           final String e164 = String.valueOf(entry.e164());
           final AccountDocument document = buildAccountDocument(entry);

           container.upsertItem(document, new PartitionKey(e164), new CosmosItemRequestOptions())
                   .flatMap(response -> {
                       processedUpdates.incrementAndGet();
                       lastUpdateTime = Instant.now();
                       return updateTotalAccountsMetric();
                   })
                   .onErrorResume(error -> {
                       logger.error("Failed to update account document: {}", e164, error);
                       meterRegistry.counter(MetricsUtil.name(getClass(), "accounts.update.errors")).increment();
                       return Mono.empty();
                   })
                   .block();

       } catch (Exception e) {
           logger.error("Error processing account update", e);
           meterRegistry.counter(MetricsUtil.name(getClass(), "accounts.update.errors")).increment();
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
       document.setId(String.valueOf(entry.e164()));
       document.setE164(entry.e164());
       
       document.setAci(entry.aci());
       document.setPni(entry.pni());
       document.setUak(entry.uak());
       
       document.setLastUpdate(Instant.now());
       return document;
   }

   @Override
   public boolean hasFinishedInitialAccountPopulation() {
       return running;
   }

   @Override
   public boolean isHealthy() {
       return running;
   }

   private static class AccountDocument {
       private String id;
       private long e164;
       private byte[] aci;
       private byte[] pni;
       private byte[] uak;
       private Instant lastUpdate;

       public String getId() { return id; }
       public void setId(String id) { this.id = id; }
       
       public long getE164() { return e164; }
       public void setE164(long e164) { this.e164 = e164; }
       
       public byte[] getAci() { return aci; }
       public void setAci(byte[] aci) { this.aci = aci; }
       
       public byte[] getPni() { return pni; }
       public void setPni(byte[] pni) { this.pni = pni; }
       
       public byte[] getUak() { return uak; }
       public void setUak(byte[] uak) { this.uak = uak; }
       
       public Instant getLastUpdate() { return lastUpdate; }
       public void setLastUpdate(Instant lastUpdate) { this.lastUpdate = lastUpdate; }
   }
}