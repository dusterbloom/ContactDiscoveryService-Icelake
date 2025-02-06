# Signal Contact Discovery Service (CDSI) Integration Guide

## 📘 Overview

The Signal Contact Discovery Service (CDSI) provides a secure, privacy-preserving method for discovering which of a user's contacts are Signal users. This guide will walk you through the complete integration process.

## 🔒 Core Principles

1. **Absolute Privacy**: No raw contact lists are ever exposed
2. **Cryptographic Security**: Uses Intel SGX and Noise Protocol
3. **Oblivious Computation**: Prevents database inference
4. **Rate-Limited**: Protects against abuse

## 🚀 Integration Workflow

### 1. Prerequisites

#### Technical Requirements
- Secure key generation capabilities
- Noise Protocol client implementation
- HTTPS/TLS support
- Base64 encoding/decoding
- Cryptographically secure random number generation

#### Required Components
- User's E.164 phone number
- User Access Keys (UAKs)
- Secure key storage mechanism

### 2. Authentication Preparation

```typescript
interface UserCredentials {
  e164: string;        // User's phone number
  uak: Buffer;         // User Access Key
  identityKey: KeyPair; // Signal identity key
}

class ContactDiscoveryClient {
  private credentials: UserCredentials;
  private noiseProtocol: NoiseProtocol;

  constructor(credentials: UserCredentials) {
    this.credentials = credentials;
    this.noiseProtocol = new NoiseProtocol();
  }

  // Initialize secure communication channel
  async initializeHandshake(): Promise<HandshakeResponse> {
    // 1. Generate ephemeral keys
    const ephemeralKeys = this.noiseProtocol.generateEphemeralKeys();

    // 2. Perform Noise NK handshake
    const handshakeMessage = this.noiseProtocol.createHandshakeInitiation(
      this.credentials.identityKey,
      ephemeralKeys
    );

    // 3. Send handshake to CDSI
    const serverResponse = await this.sendHandshakeToServer(handshakeMessage);

    // 4. Complete handshake and establish secure channel
    this.noiseProtocol.completeHandshake(serverResponse);

    return serverResponse;
  }
}
```

### 3. Contact Discovery Process

```typescript
interface Contact {
  phoneNumber: string;
}

class SignalContactDiscovery {
  private cdsiClient: ContactDiscoveryClient;

  async discoverSignalContacts(localContacts: Contact[]): Promise<SignalContact[]> {
    // 1. Prepare contact data
    const e164s = localContacts.map(contact => contact.phoneNumber);
    const uaks = this.generateUserAccessKeys(e164s);

    try {
      // 2. Perform rate limit request
      const rateLimitToken = await this.cdsiClient.getRateLimitToken(
        e164s, 
        uaks
      );

      // 3. Perform contact discovery
      const discoveryResults = await this.cdsiClient.discoverContacts({
        e164s,
        uaks,
        token: rateLimitToken
      });

      // 4. Process and return matching contacts
      return this.processDiscoveryResults(discoveryResults);
    } catch (error) {
      // Handle rate limiting, network errors, etc.
      this.handleDiscoveryError(error);
    }
  }

  private generateUserAccessKeys(e164s: string[]): Buffer[] {
    // Implement secure UAK generation
    // This is a placeholder - actual implementation requires 
    // cryptographically secure methods
    return e164s.map(e164 => {
      // Generate a cryptographically secure UAK
      return this.cryptoService.generateUAK(e164);
    });
  }
}
```

### 4. Error Handling & Retry Strategy

```typescript
class ContactDiscoveryErrorHandler {
  private static MAX_RETRIES = 3;
  private static RETRY_BACKOFF = [1000, 5000, 10000]; // Exponential backoff

  static async handleDiscoveryError(
    error: DiscoveryError, 
    retryContext: RetryContext
  ): Promise<DiscoveryResult> {
    if (retryContext.attempts >= this.MAX_RETRIES) {
      throw new PermanentDiscoveryError(error);
    }

    switch (error.type) {
      case 'RATE_LIMITED':
        // Wait for specified retry-after duration
        await this.delay(error.retryAfter);
        break;
      
      case 'NETWORK_ERROR':
        // Exponential backoff
        await this.delay(
          this.RETRY_BACKOFF[retryContext.attempts]
        );
        break;
      
      case 'AUTHENTICATION_ERROR':
        // Re-establish handshake
        await this.reAuthenticate();
        break;
    }

    // Retry the discovery process
    return this.retryDiscovery(retryContext);
  }
}
```

## 🛡️ Security Best Practices

1. **Key Management**
   - Store identity keys securely
   - Rotate keys periodically
   - Never expose raw keys

2. **Contact List Handling**
   - Minimize contact list exposure
   - Hash/anonymize where possible
   - Use secure, temporary storage

3. **Rate Limiting Compliance**
   - Respect server-side rate limits
   - Implement exponential backoff
   - Cache and batch requests

## 🔍 Debugging & Monitoring

### Logging Recommendations
- Log error codes (never raw errors)
- Track request attempts
- Monitor rate limit status
- Implement telemetry without compromising user privacy

### Common Troubleshooting
- Verify network connectivity
- Check key generation process
- Validate E.164 phone number formatting
- Ensure proper error handling

## 🚧 Common Pitfalls

1. **Incorrect UAK Generation**
   - Ensure cryptographically secure generation
   - Use approved cryptographic libraries

2. **Exposing Raw Contact Lists**
   - Never send unencrypted contact lists
   - Implement local, secure processing

3. **Ignoring Rate Limits**
   - Implement robust retry mechanisms
   - Respect server-side constraints

## 📦 Recommended Libraries

### Noise Protocol
- `noise-protocol-rs`
- `noise-protocol-js`
- `noise-protocol-swift`

### Cryptography
- `libsignal`
- `tink`
- Platform-specific secure key management

## 🔬 Performance Optimization

- Batch contact discovery requests
- Implement intelligent caching
- Use background processing
- Minimize network calls

## 📋 Compliance Checklist

- [ ] Implement Noise Protocol handshake
- [ ] Secure key generation
- [ ] Rate limit handling
- [ ] Error management
- [ ] Privacy protection
- [ ] Secure storage of sensitive data

## 🆘 Support & Community

- **Signal Developer Documentation**: [Link to official docs]
- **Community Forums**: [Link to forums]
- **Issue Tracking**: [Link to GitHub issues]

## 📝 Version Compatibility

**Current CDSI Version**: v1.0.0
**Supported Platforms**: 
- iOS
- Android
- Web (with limitations)

---

### 🚀 Quick Start Example

```typescript
async function findSignalContacts() {
  const discovery = new SignalContactDiscovery(userCredentials);
  
  try {
    const signalContacts = await discovery.discoverSignalContacts(
      localContactList
    );
    
    // Use discovered Signal contacts
    updateSignalContactsList(signalContacts);
  } catch (error) {
    handleContactDiscoveryError(error);
  }
}
```

**Remember**: Always prioritize user privacy and follow Signal's security guidelines!