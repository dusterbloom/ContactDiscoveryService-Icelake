package org.signal.cdsi.azure.config;

import com.azure.spring.cloud.core.properties.AzureProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import javax.validation.constraints.NotBlank;

@Configuration
@ConfigurationProperties(prefix = "azure")
public class AzureConfiguration {
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

    // Required configuration for Azure Functions
    private String functionAppName;
    private String resourceGroup;
    private String region = "northeurope";  // Default region

    // Getters
    public String getCosmosDbEndpoint() { return cosmosDbEndpoint; }
    public String getCosmosDbKey() { return cosmosDbKey; }
    public String getCosmosDbDatabase() { return cosmosDbDatabase; }
    public String getCosmosDbContainer() { return cosmosDbContainer; }
    public String getEventHubNamespace() { return eventHubNamespace; }
    public String getEventHubName() { return eventHubName; }
    public String getEventHubKey() { return eventHubKey; }
    public String getFunctionAppName() { return functionAppName; }
    public String getResourceGroup() { return resourceGroup; }
    public String getRegion() { return region; }

    // Setters
    public void setCosmosDbEndpoint(String cosmosDbEndpoint) { this.cosmosDbEndpoint = cosmosDbEndpoint; }
    public void setCosmosDbKey(String cosmosDbKey) { this.cosmosDbKey = cosmosDbKey; }
    public void setCosmosDbDatabase(String cosmosDbDatabase) { this.cosmosDbDatabase = cosmosDbDatabase; }
    public void setCosmosDbContainer(String cosmosDbContainer) { this.cosmosDbContainer = cosmosDbContainer; }
    public void setEventHubNamespace(String eventHubNamespace) { this.eventHubNamespace = eventHubNamespace; }
    public void setEventHubName(String eventHubName) { this.eventHubName = eventHubName; }
    public void setEventHubKey(String eventHubKey) { this.eventHubKey = eventHubKey; }
    public void setFunctionAppName(String functionAppName) { this.functionAppName = functionAppName; }
    public void setResourceGroup(String resourceGroup) { this.resourceGroup = resourceGroup; }
    public void setRegion(String region) { this.region = region; }

    // Helper methods to construct connection strings
    public String getCosmosDbConnectionString() {
        return String.format("AccountEndpoint=%s;AccountKey=%s;", cosmosDbEndpoint, cosmosDbKey);
    }

    public String getEventHubConnectionString() {
        return String.format("Endpoint=sb://%s.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=%s",
                eventHubNamespace, eventHubKey);
    }
}