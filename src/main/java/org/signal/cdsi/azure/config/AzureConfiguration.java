// 2. src/main/java/org/signal/cdsi/azure/config/AzureConfiguration.java
package org.signal.cdsi.azure.config;

import com.microsoft.azure.functions.annotation.CosmosDBOutput;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfiguration {
    private String cosmosDbConnection;
    private String eventHubConnection;
    private String databaseName;
    private String containerName;
    private String eventHubName;

    // Getters and setters
}