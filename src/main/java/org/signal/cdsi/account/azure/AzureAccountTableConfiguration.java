package org.signal.cdsi.account.azure;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import jakarta.validation.constraints.NotBlank;

@Context
@ConfigurationProperties("azure")
public class AzureAccountTableConfiguration {
   @NotBlank
   private String cosmosDbEndpoint;
   @NotBlank
   private String cosmosDbKey;
   @NotBlank 
   private String cosmosDbDatabase;
   @NotBlank
   private String cosmosDbContainer;
   @NotBlank
   private String eventHubNamespace;
   @NotBlank
   private String eventHubName;
   @NotBlank
   private String eventHubKey;
   @NotBlank
   private String eventHubConsumerGroup;

   public String getCosmosDbEndpoint() { return cosmosDbEndpoint; }
   public void setCosmosDbEndpoint(String cosmosDbEndpoint) { this.cosmosDbEndpoint = cosmosDbEndpoint; }
   
   public String getCosmosDbKey() { return cosmosDbKey; }
   public void setCosmosDbKey(String cosmosDbKey) { this.cosmosDbKey = cosmosDbKey; }
   
   public String getCosmosDbDatabase() { return cosmosDbDatabase; }
   public void setCosmosDbDatabase(String cosmosDbDatabase) { this.cosmosDbDatabase = cosmosDbDatabase; }
   
   public String getCosmosDbContainer() { return cosmosDbContainer; }
   public void setCosmosDbContainer(String cosmosDbContainer) { this.cosmosDbContainer = cosmosDbContainer; }
   
   public String getEventHubNamespace() { return eventHubNamespace; }
   public void setEventHubNamespace(String eventHubNamespace) { this.eventHubNamespace = eventHubNamespace; }
   
   public String getEventHubName() { return eventHubName; }
   public void setEventHubName(String eventHubName) { this.eventHubName = eventHubName; }
   
   public String getEventHubKey() { return eventHubKey; }
   public void setEventHubKey(String eventHubKey) { this.eventHubKey = eventHubKey; }
   
   public String getEventHubConsumerGroup() { return eventHubConsumerGroup; }
   public void setEventHubConsumerGroup(String eventHubConsumerGroup) { this.eventHubConsumerGroup = eventHubConsumerGroup; }

   public String getCosmosDbConnectionString() {
       return String.format("AccountEndpoint=%s;AccountKey=%s;", cosmosDbEndpoint, cosmosDbKey);
   }

   public String getEventHubConnectionString() {
       return String.format(
           "Endpoint=sb://%s.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=%s",
           eventHubNamespace, 
           eventHubKey
       );
   }
}