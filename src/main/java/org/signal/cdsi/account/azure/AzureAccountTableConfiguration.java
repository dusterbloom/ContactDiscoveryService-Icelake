package org.signal.cdsi.account.azure;

import io.micronaut.context.annotation.ConfigurationProperties;
import javax.validation.constraints.NotBlank;

@ConfigurationProperties("azure.account")
public class AzureAccountTableConfiguration {
    @NotBlank
    private String cosmosEndpoint;
    
    @NotBlank
    private String cosmosDbKey;
    
    @NotBlank
    private String cosmosDbDatabase;
    
    @NotBlank
    private String cosmosDbContainer;
    
    // Event Hub Configuration
    @NotBlank
    private String eventHubNamespace;
    
    @NotBlank
    private String eventHubName;
    
    @NotBlank
    private String eventHubKey;
    
    @NotBlank
    private String eventHubConsumerGroup;

    // Azure Function Configuration
    private String functionAppName;
    private String resourceGroup;
    private String region = "northeurope";  // Default region

    // Getters and Setters
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
    
    public String getFunctionAppName() { return functionAppName; }
    public void setFunctionAppName(String functionAppName) { this.functionAppName = functionAppName; }
    
    public String getResourceGroup() { return resourceGroup; }
    public void setResourceGroup(String resourceGroup) { this.resourceGroup = resourceGroup; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    // Helper methods for connection strings
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