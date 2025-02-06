### 1. What Icelake (CDSi) Does

**Secure Contact Discovery Service:**  
Icelake (the CDSi component) is Signal’s implementation of a secure contact discovery service. In this system, a host‐side Java service (built with Micronaut) acts as a “front end” to an SGX enclave (implemented in C) that performs cryptographic operations and secure data processing. Key functions include:
  
- **Enclave Initialization and Attestation:** The enclave exposes functions such as `enclave_init` and `enclave_attest` so that it can be securely bootstrapped and remotely attested. This ensures that the code running inside SGX is genuine and that the keys (for example, used for encrypting handshake messages) are generated securely.
- **Client Handshake and Secure Channel Establishment:** Functions like `enclave_handshake` (and related routines) support a Noise Protocol handshake to derive session keys. These routines use the Noise library (with protocols such as “Noise_NK_25519_ChaChaPoly_SHA256”) to perform constant‐time operations and mitigate side‑channel attacks.
- **Rate Limiting and Request Processing:** The enclave implements functions such as `enclave_rate_limit` and `enclave_run` that both decrypt incoming client requests (which are bundled as Noise packets) and then apply rate‐limiting and query operations in a way that “obliviously” accesses data (using techniques such as ORAM and fixed‑set lookups). This protects against leaking information through memory access patterns.
- **ORAM and Data Structures for Oblivious Operations:** Within the C code you see several modules (for example, in `c/path_oram/`) that implement ORAM (Oblivious RAM) to store a “directory” or table of user records in a way that hides access patterns. This includes the use of fixed‑size “buckets” and a position map that may itself be implemented with a simple linear scan (when small) or a recursive ORAM structure (when larger).

---

### 2. Integration Points & How to Integrate Icelake

Based on the directory structure and build instructions (including the Maven and Dockerfiles), here are the integration points:

- **Java Host Service:**  
  The top‑level service is a Micronaut‑based Java application (located in `src/main/java/org/signal/cdsi/`). This service provides network endpoints (likely REST or WebSocket endpoints) that the mobile app or other clients can use. The service is configured via YAML files (for example, `application.yml` and `application-dev.yml`) so that it can run in both development and production modes.

- **Enclave Integration:**  
  The secure SGX enclave code is found in the `c/` directory. The build system (using Maven with an exec plugin and Makefiles) builds both the host-side artifacts and the enclave binary (and its “signed” version) for use in production. The enclave exposes its API through an EDL file (`cds.edl`), which defines the trusted calls (ECALLs) like:
  - **Administrative calls:** `enclave_init`, `enclave_stop_shards`, `enclave_load_pb`, etc.
  - **Client calls:** `enclave_new_client`, `enclave_handshake`, `enclave_rate_limit`, `enclave_run`, and `enclave_retry_response`.

- **External Dependencies:**  
  The README and configuration files indicate that CDSi (Icelake) requires configuration for:
  - **Authentication:** It uses Signal’s external service credential system. The property `authentication.sharedSecret` must be set (as a base64‑encoded 32‑byte secret) so that end users can be authenticated.
  - **Data Sources & Streams:** In production, CDSi pulls account data (from DynamoDB and a Kinesis stream, for example) and may integrate with other cloud services (Cosmos, Redis, etc.) for rate limiting and directory queries.

- **Deployment & Containerization:**  
  A Dockerfile is provided (see the Dockerfile in the top‑level directory) so that the entire service (including enclave support) can be containerized. This makes integration into a larger infrastructure (for example, in a cloud environment) straightforward.

---

### 3. Communication Model: Who Talks to Whom?

One of the key questions is whether the solution requires your own server to act as an intermediary with the Signal server or if the mobile app can talk directly to Icelake. Here’s what we found:

- **Direct Client Communication:**  
  The documentation (in the README) states that “end users communicate directly with CDSi” and that authentication is performed using Signal’s standard external service credentials. This design implies that a mobile app can be configured to connect directly to the CDSi host service without needing an additional intermediary server on your side.

- **Role of the Signal Server:**  
  The CDSi service is built to integrate with Signal’s ecosystem. Rather than requiring your own server to proxy all communications, the mobile app—once it is authenticated—can send contact discovery queries directly to the CDSi service. The CDSi host service (backed by the enclave) then processes the request securely and returns a response.

In other words, **the mobile app can talk directly to the CDSi service (Icelake) rather than requiring an additional server to interact with the Signal server.** (See also the configuration section in the README where it describes setting the shared secret for authentication.)

---

### 4. Summary

- **How Icelake Functions:**  
  Icelake (CDSi) is a secure contact discovery service that combines a Java host application with a trusted SGX enclave. The enclave implements functions for client authentication, Noise‑protocol handshakes, rate limiting, and secure data queries (using oblivious data structures such as ORAM).

- **Integration Points:**  
  The service is designed to be integrated as a standalone microservice. Its integration points include the network endpoints provided by the Micronaut service, configuration properties for authentication and external data sources, and the build/deployment artifacts (including Docker containers) for deploying the enclave‑backed service.

- **Client Interaction:**  
  The design indicates that end‑user mobile apps can communicate directly with CDSi after proper authentication (using the shared secret). There is no requirement that your own server act as a middleman between the mobile app and the Signal server.

---

### References

- Repository README and source files in the `signalapp-contactdiscoveryservice-icelake` project provide the primary details of configuration and functionality cite.
- The integration and deployment instructions (e.g. Micronaut configuration, Dockerfiles) further confirm that CDSi is designed for direct client interaction cite.
