# 📱 CDSI: Contact Discovery Service on Icelake 🧊

## 🏗️ Project Overview

CDSI is a secure contact discovery service designed to help users find and connect with their contacts safely and efficiently. Think of it like a digital phonebook that protects your privacy!

## 📂 Project Structure

```
📁 Root Directory
│
├── 🌐 Java Service (Micronaut)
│   └── Handles server-side operations
│
└── 🔒 SGX C Code
    └── Secure computation magic happens here
```

## 🛠️ Getting Started: Building the Project

### Prerequisites
- 💻 Git
- ☕ Java Development Kit
- 🔧 Maven
- 🔐 OpenSSL 1.1.1



### Libffi7

```bash
wget http://mirrors.kernel.org/ubuntu/pool/main/libf/libffi/libffi7_3.3-4_amd64.deb
sudo dpkg -i libffi7_3.3-4_amd64.deb
sudo apt-get install -f
```
### LibSSL

```bash
wget http://security.ubuntu.com/ubuntu/pool/main/o/openssl/libssl1.1_1.1.1f-1ubuntu2.23_amd64.deb
 sudo dpkg -i libssl1.1_1.1.1f-1ubuntu2.23_amd64.deb
sudo apt-get install -f
```

### Azure Client + libllvm11

```bash
 sudo apt-get install az-dcap-client libllvm11
```

### OpenEnclave

```bash
sudo apt -y install open-enclave
```

### Maven

```bash
sudo apt install maven
```

### Java 17 OpenJDK

```bash
sudo apt update
sudo apt install openjdk-17-jdk

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

source ~/.bashrc  
```

### 🆘 OpenSSL Installation Troubleshoot
If you're on Ubuntu 22.04 and need OpenSSL 1.1.1:

```bash
# 📥 Download OpenSSL
wget https://www.openssl.org/source/openssl-1.1.1u.tar.gz

# 📦 Extract and install
tar xvzf openssl-1.1.1u.tar.gz
cd openssl-1.1.1u/
./config
make -j8
sudo make install -j8
sudo ldconfig

```



### Build Steps
```bash
# 🔗 Initialize submodules
git submodule init
git submodule update

# 🏗️ Build and verify
mvn verify
```



## 🚀 Running the Service

### 🧪 Development Mode
Perfect for testing and debugging:
```bash
./mvnw mn:run -Dmicronaut.environments=dev
```



## Production mode

### Build the enclave

```bash
./mvnw exec:exec@enclave-release [build success]
```






### 🌍 Production Configuration Checklist

#### 🔐 Security Setup
- 🔑 Authentication Secret
  ```yaml
  authentication:
    sharedSecret: <your-secret-here>
  ```

#### 💾 Data Sources
- 🗃️ Cosmos Database (Rate Limiting)
  ```yaml
  cosmos:
    database: your-database-name
    endpoint: https://your-cosmos-endpoint
  ```

- 🔴 Redis Cluster
  ```yaml
  redis:
    uris: redis://your-redis-server
  ```

- 💽 Account Data
  ```yaml
  accountTable:
    region: your-aws-region
    tableName: your-dynamodb-table
    streamName: your-kinesis-stream
  ```

#### 🏭 Enclave Configuration
```yaml
enclave:
  enclaveId: unique-enclave-id
  availableEpcMemory: 32000000
  loadFactor: 1.6
```

## 🆕 Creating Enclave Releases
```bash
# 🏷️ Generate new enclave version
./mvnw exec:exec@enclave-release
```

## 🤔 What Makes This Special?

- 🔒 **Secure Computation**: Uses SGX for ultra-private contact matching
- 🚦 **Rate Limiting**: Prevents abuse with Cosmos and Redis
- 🔄 **Dynamic Updates**: Syncs contact info from DynamoDB and Kinesis

## 📜 License

📍 AGPLv3 - Open Source, Privacy-Focused 
© 2022 Signal Messenger, LLC

## 🆘 Need Help?
- Check our documentation
- Explore the source code
- Join our community discussions