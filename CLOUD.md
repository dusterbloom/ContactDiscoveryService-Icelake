# 🚀 Production Setup for CDSI on Azure


### Prerequisite: 🔧 Installing the Azure CLI (`az`) on Your Azure Machine

On a Ubuntu 22.04 machine, you can use the one-liner installer provided by Microsoft:

```bash
curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
```

---

#### For Other Operating Systems

- **Windows:** Use the MSI installer from the [Azure CLI installation page](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli-windows).
- **macOS:** Install via Homebrew with `brew update && brew install azure-cli`.

---

### Verification

After installation, verify that the CLI is installed correctly by running:

```bash
az --version
```

This command will display the installed version and confirm that `az` is available on your system as shown below.

```bash
user@dev-sgx-small:~$ az --version
azure-cli                         2.68.0

core                              2.68.0
telemetry                          1.1.0

Dependencies:
msal                              1.31.1
azure-mgmt-resource               23.1.1

Python location '/opt/az/bin/python3'
Extensions directory '/home/peppi/.azure/cliextensions'

Python (Linux) 3.12.8 (main, Jan  8 2025, 03:38:16) [GCC 13.3.0]

Legal docs and information: aka.ms/AzureCliLegal


Your CLI is up-to-date.
```




## 1. Provision Cosmos DB for the Directory Query Rate Limiter ✨

### What You Need
- A Cosmos DB account with a dedicated database and container to back the directory query rate limiter.

### Steps

- **Create a Cosmos DB Account:**  
  ```bash
  az cosmosdb create --name my-cdsi-cosmos --resource-group myResourceGroup --kind GlobalDocumentDB --locations regionName=EastUS
  ```

- **Create the Database and Container:**  
  ```bash
  az cosmosdb sql database create --account-name my-cdsi-cosmos --name cdsiDatabase --resource-group myResourceGroup
  az cosmosdb sql container create --account-name my-cdsi-cosmos --database-name cdsiDatabase --name rateLimiterContainer --partition-key-path "/partitionKey" --resource-group myResourceGroup
  ```

- **Configuration:**  
  Use the connection endpoint and key provided by Cosmos DB in your CDSI configuration (as specified in the repository’s configuration checklist).

---

## 2. Set Up Azure Cache for Redis for the Connection Rate Limiter 🔥

### What You Need
- A managed Redis instance to enforce connection rate limits.

### Steps

- **Create the Redis Instance:**  
  ```bash
  az redis create --name my-cdsi-redis --resource-group myResourceGroup --location EastUS --sku Basic --vm-size c0
  ```

- **Configuration:**  
  Retrieve the connection string from the Azure Portal or via CLI and add it to your CDSI configuration (e.g., under the `redis: uris` property).

---

## 3. Establish an Event-Driven Pipeline for Account Data Updates 📊

Since CDSI originally expects account updates via DynamoDB and Kinesis, the Azure-native approach is to use:

- **Account Data Store:**  
  Consider using Cosmos DB (or Azure Table Storage) to store account data.

- **Event Ingestion with Event Hubs:**  
  Use Azure Event Hubs to capture streaming account updates.

### Steps

- **Create an Event Hubs Namespace and Event Hub:**  
  ```bash
  az eventhubs namespace create --name myCdsiEHNamespace --resource-group myResourceGroup --location EastUS --sku Standard
  az eventhubs eventhub create --name cdsiUpdates --namespace-name myCdsiEHNamespace --resource-group myResourceGroup
  ```

- **Integration:**  
  In your CDSI configuration, set the `accountTable.streamName` property (or its Azure equivalent) to reference the Event Hub, allowing CDSI to pull account update messages.

---

## 4. Deploy an Azure Function to Replace AWS Lambda for Filtering Updates 💻

CDSI uses a Lambda (located in the `filter-cds-updates` directory) to filter and forward account updates. **Azure Functions** is a natural substitute.

### Steps

- **Create an Azure Functions App:**  
  ```bash
  az functionapp create --resource-group myResourceGroup --consumption-plan-location EastUS --runtime dotnet --functions-version 4 --name myCdsiFunctions --storage-account myStorageAccount
  ```

- **Develop & Deploy Your Function:**  
  Develop a function that uses an **Event Hub trigger** to replicate the filtering logic from your existing Lambda. Deploy your code using the Azure Functions Core Tools or integrate it into your CI/CD pipeline.

- **Configuration:**  
  Store required settings (like the Event Hub connection string and any shared secrets) in **Azure Key Vault** and reference them via your function app’s settings.

---

## Automation & Best Practices 🛠️

- **Infrastructure as Code (IaC):**  
  Use ARM/Bicep or Terraform to codify these resources, ensuring reproducibility and ease of scaling.

- **Secret Management:**  
  Leverage **Azure Key Vault** to securely store secrets such as the authentication shared secret and Cosmos DB keys.

- **Monitoring & Logging:**  
  Integrate **Azure Monitor** and **Application Insights** with your deployed services to track performance, errors, and usage patterns.

- **CI/CD Integration:**  
  Incorporate these IaC scripts and function deployments into your CI/CD pipeline (via Azure DevOps or GitHub Actions) to streamline updates and testing.

---

## Final Thoughts 🌟

By automating the provisioning of these services and securely managing configurations, you'll create a robust production environment on Azure for CDSI testing. This approach minimizes manual setup, ensures reproducibility, and adheres to modern engineering best practices.

For further details on CDSI configuration, refer to the [INTEGRATION_GUIDE.md](https://github.com/dusterbloom/ContactDiscoveryService-Icelake/blob/main/INTEGRATION_GUIDE.md) in the repository.

Feel free to reach out if you need more specifics or run into any challenges during setup! 🙌
