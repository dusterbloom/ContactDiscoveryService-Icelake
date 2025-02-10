/*
 * Copyright 2022 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.signal.cdsi.account.azure;

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * Configuration properties for connecting to Azure Cosmos DB used by CDSI.
 * 
 * These properties can be set in your application YAML file under the "cosmos" key.
 *
 * Example configuration in application.yml:
 *
 *   cosmos:
 *     cosmosDbEndpoint: "https://your-cosmos-account.documents.azure.com:443/"
 *     cosmosDbKey: "your-primary-key-here"
 *     cosmosDbDatabase: "your-database-name"
 *     cosmosDbContainer: "your-container-name"
 */
@ConfigurationProperties("cosmos")
public class AzureAccountTableConfiguration {

    /**
     * The endpoint URL for your Cosmos DB account.
     */
    private String cosmosDbEndpoint;

    /**
     * The primary key for your Cosmos DB account.
     */
    private String cosmosDbKey;

    /**
     * The Cosmos DB database name.
     */
    private String cosmosDbDatabase;

    /**
     * The Cosmos DB container name.
     */
    private String cosmosDbContainer;

    // Getters and setters

    public String getCosmosDbEndpoint() {
        return cosmosDbEndpoint;
    }

    public void setCosmosDbEndpoint(String cosmosDbEndpoint) {
        this.cosmosDbEndpoint = cosmosDbEndpoint;
    }

    public String getCosmosDbKey() {
        return cosmosDbKey;
    }

    public void setCosmosDbKey(String cosmosDbKey) {
        this.cosmosDbKey = cosmosDbKey;
    }

    public String getCosmosDbDatabase() {
        return cosmosDbDatabase;
    }

    public void setCosmosDbDatabase(String cosmosDbDatabase) {
        this.cosmosDbDatabase = cosmosDbDatabase;
    }

    public String getCosmosDbContainer() {
        return cosmosDbContainer;
    }

    public void setCosmosDbContainer(String cosmosDbContainer) {
        this.cosmosDbContainer = cosmosDbContainer;
    }
    
    /**
     * Constructs a Cosmos DB connection string.
     * 
     * @return the connection string formatted for Cosmos DB
     */
    public String getConnectionString() {
        return String.format("AccountEndpoint=%s;AccountKey=%s;", cosmosDbEndpoint, cosmosDbKey);
    }
}


// package org.signal.cdsi.account.azure;

// import io.micronaut.context.annotation.ConfigurationProperties;
// import io.micronaut.context.annotation.Context;
// import jakarta.validation.constraints.NotBlank;

// @Context
// @ConfigurationProperties("azure")
// public class AzureAccountTableConfiguration {
//    @NotBlank
//    private String cosmosDbEndpoint;
//    @NotBlank
//    private String cosmosDbKey;
//    @NotBlank 
//    private String cosmosDbDatabase;
//    @NotBlank
//    private String cosmosDbContainer;
//    @NotBlank
//    private String eventHubNamespace;
//    @NotBlank
//    private String eventHubName;
//    @NotBlank
//    private String eventHubKey;
//    @NotBlank
//    private String eventHubConsumerGroup;

//    public String getCosmosDbEndpoint() { return cosmosDbEndpoint; }
//    public void setCosmosDbEndpoint(String cosmosDbEndpoint) { this.cosmosDbEndpoint = cosmosDbEndpoint; }
   
//    public String getCosmosDbKey() { return cosmosDbKey; }
//    public void setCosmosDbKey(String cosmosDbKey) { this.cosmosDbKey = cosmosDbKey; }
   
//    public String getCosmosDbDatabase() { return cosmosDbDatabase; }
//    public void setCosmosDbDatabase(String cosmosDbDatabase) { this.cosmosDbDatabase = cosmosDbDatabase; }
   
//    public String getCosmosDbContainer() { return cosmosDbContainer; }
//    public void setCosmosDbContainer(String cosmosDbContainer) { this.cosmosDbContainer = cosmosDbContainer; }
   
//    public String getEventHubNamespace() { return eventHubNamespace; }
//    public void setEventHubNamespace(String eventHubNamespace) { this.eventHubNamespace = eventHubNamespace; }
   
//    public String getEventHubName() { return eventHubName; }
//    public void setEventHubName(String eventHubName) { this.eventHubName = eventHubName; }
   
//    public String getEventHubKey() { return eventHubKey; }
//    public void setEventHubKey(String eventHubKey) { this.eventHubKey = eventHubKey; }
   
//    public String getEventHubConsumerGroup() { return eventHubConsumerGroup; }
//    public void setEventHubConsumerGroup(String eventHubConsumerGroup) { this.eventHubConsumerGroup = eventHubConsumerGroup; }

//    public String getCosmosDbConnectionString() {
//        return String.format("AccountEndpoint=%s;AccountKey=%s;", cosmosDbEndpoint, cosmosDbKey);
//    }

//    public String getEventHubConnectionString() {
//        return String.format(
//            "Endpoint=sb://%s.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=%s",
//            eventHubNamespace, 
//            eventHubKey
//        );
//    }
// }