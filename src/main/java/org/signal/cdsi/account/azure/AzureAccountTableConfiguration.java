package org.signal.cdsi.account.azure;

import io.micronaut.context.annotation.ConfigurationProperties;
import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties("azure.account")
public class AzureAccountTableConfiguration {

    @NotBlank
    private String cosmosEndpoint;

    @NotBlank
    private String cosmosKey;

    @NotBlank
    private String cosmosDatabase;

    @NotBlank
    private String cosmosContainer;

    @NotBlank
    private String eventHubConnectionString;

    @NotBlank
    private String eventHubName;

    @NotBlank
    private String eventHubConsumerGroup;

    private String preferredRegions;

    // Getters and setters
    public String getCosmosEndpoint() {
        return cosmosEndpoint;
    }

    public void setCosmosEndpoint(String cosmosEndpoint) {
        this.cosmosEndpoint = cosmosEndpoint;
    }

    public String getCosmosKey() {
        return cosmosKey;
    }

    public void setCosmosKey(String cosmosKey) {
        this.cosmosKey = cosmosKey;
    }

    public String getCosmosDatabase() {
        return cosmosDatabase;
    }

    public void setCosmosDatabase(String cosmosDatabase) {
        this.cosmosDatabase = cosmosDatabase;
    }

    public String getCosmosContainer() {
        return cosmosContainer;
    }

    public void setCosmosContainer(String cosmosContainer) {
        this.cosmosContainer = cosmosContainer;
    }

    public String getEventHubConnectionString() {
        return eventHubConnectionString;
    }

    public void setEventHubConnectionString(String eventHubConnectionString) {
        this.eventHubConnectionString = eventHubConnectionString;
    }

    public String getEventHubName() {
        return eventHubName;
    }

    public void setEventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
    }

    public String getEventHubConsumerGroup() {
        return eventHubConsumerGroup;
    }

    public void setEventHubConsumerGroup(String eventHubConsumerGroup) {
        this.eventHubConsumerGroup = eventHubConsumerGroup;
    }

    public String getPreferredRegions() {
        return preferredRegions;
    }

    public void setPreferredRegions(String preferredRegions) {
        this.preferredRegions = preferredRegions;
    }
}