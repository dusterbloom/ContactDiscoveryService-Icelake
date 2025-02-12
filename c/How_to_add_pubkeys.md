Okay, Sensei is on the case. Adding support for blockchain public keys to this CDSI service while maintaining security and minimizing changes requires a careful approach. Here's a breakdown of how I would tackle this, focusing on actionable steps and file references:

**I. Understanding the Existing System**

Before diving into modifications, it's crucial to fully grasp how the current phone number-based discovery works. This involves tracing the data flow from client request to enclave processing and back.  Pay close attention to data serialization/deserialization.

**II. Design Considerations**

1.  **Identifier Type:** You're introducing a new identifier type (public key) alongside E.164 phone numbers. How will you differentiate between them?  A simple enum or a dedicated field in the request might suffice.
2.  **Data Storage:**  The ORAM within the SGX enclave needs to store both phone numbers and public keys.  Consider the size implications of public keys (typically larger than phone numbers) and how this might affect ORAM performance.
3.  **Security Properties:**  Ensure that the addition of public keys doesn't weaken the existing security guarantees.  Both identifiers should be treated with the same level of confidentiality and integrity.  Pay close attention to side-channel resistance.
4.  **Minimal Changes:** Minimize modifications to existing code to reduce the risk of introducing bugs or breaking existing functionality.

**III. Actionable Steps**

Here's a detailed plan, referencing specific files:

1.  **Define a New Identifier Type:**

    *   **File:** `c/proto/cdsi.proto`

    Modify the `ClientRequest` message to include a field indicating the type of identifier being used. I'd suggest using a oneof.
    This is the most important step. Add a oneof that contains either e164s or public_keys.
    This would require changes to the proto file so that the generated classes are updated.

    ```protobuf
    message ClientRequest {
      // Existing fields...

      oneof identifier {
        bytes e164s = 2;
        bytes public_keys = 9; // New field for public keys
      }
    }
    ```

    Also, update the proto message for `EnclaveLoad` to align with the new identifier schema.

    ```protobuf
    message EnclaveLoad {
      // Existing fields...

      // Use a oneof to support either E164 or public key identifiers
      oneof identifier_aci_pni_uak_tuples {
        bytes e164_aci_pni_uak_tuples = 2;
        bytes public_key_aci_pni_uak_tuples = 10; // New field for public key tuples
      }
    }
    ```

    *   **File:** `src/main/proto/cdsi.proto`
        Make same changes to proto file for Java service

2.  **Update Data Structures:**

    *   **File:** `c/aci_pni_uak_helpers/aci_pni_uak_helpers.h`
        *Modify the `signal_user_record` structure to accommodate either an E.164 or a public key. Since public keys are larger, you'll likely need to increase the size of the identifier field.  Consider using a `union` or a dynamically sized array (with a size limit) to handle both types. If you go with a union, you need to add a type field to indicate which one is used.

    ```c
    typedef struct signal_user_record signal_user_record;
    struct signal_user_record
    {
        // Byte layout here must match the layout received from EnclaveLoad e164_aci_pni_uak_tuples.
        uint8_t identifier_type; // New field to indicate type (e.g., 0 for E.164, 1 for public key)
        union {
            uint64_t e164;
            uint8_t public_key[PUBLIC_KEY_SIZE]; // Define PUBLIC_KEY_SIZE appropriately
        } id;
        uint64_t aci[2];
        uint64_t pni[2];
        uint64_t uak[2];
    };
    ```

    *   **File:** `c/aci_pni_uak_helpers/aci_pni_uak_helpers.c`
        *Update the `create_e164_pni_aci_triples` function to handle the new `signal_user_record` structure.  You'll need to check the `identifier_type` and access the appropriate field (either `e164` or `public_key`).

3.  **Modify Data Loading and Processing:**

    *   **File:** `c/encalve/enc.c`
        *Modify `enclave_load_pb` to handle the new `EnclaveLoad` proto message. The logic will need to iterate through the `identifier_aci_pni_uak_tuples` oneof, checking which type of identifier is present and parsing the tuples accordingly.
        *Modify `enclave_run` to incorporate a new code path to handle the `public_keys` field in ClientRequest.
        *The function `create_e164_pni_aci_triples` needs to be modified to support both E164 and public key types.

4.  **Update Fixedset:**

    *   **File:** `c/fixedset/fixedset.h` and `c/fixedset/fixedset.c`

        The `fixedset` data structure will need to be updated to support the new `aci_uak_pair` structure with the identifier.
        This involves:
        *   Updating `fixedset_entry_t` to include the `identifier_type` and appropriate field for the identifier.
        *   Modifying `keq` to check the identifier type and compare the appropriate fields.
        *   Modifying `fixedset_new` and `fixedset_entry_size` to account for the new size of the fixedset entries.
        *   Modifying  `fixedset_find` to use the `identifier_type` and appropriate fields.
        *   Update `khash` to incorporate the public key into the hash calculation. This may involve using a different hashing algorithm suitable for larger keys.

5.  **Adapt the Java Code:**

    *   **Files:** (Multiple files in `src/main/java/org/signal/cdsi/`)
        *   Update the Java classes that handle protobuf messages to reflect the changes in `cdsi.proto`. This will likely involve regenerating the Java classes from the proto file.
        *   Modify the `EnclaveClient` class to handle the different types of identifiers.
        *   Adjust the `WebSocketHandler` to properly construct and parse the updated `ClientRequest` and `ClientResponse` messages.

6.  **Update Tests:**

    *   **Files:** (Multiple files in `c/testhost/tests/` and `src/test/java/org/signal/cdsi/`)
        *   Modify the test code to include tests for the new public key-based contact discovery.  This should involve creating test cases with valid and invalid public keys, and verifying that the service returns the correct results.  Ensure that these tests cover both the SGX enclave and the host-side Java code.

**IV. Security Considerations**

1.  **Hashing Public Keys:** When hashing public keys for use in ORAM or other data structures, use a cryptographically secure hash function (e.g., SHA-256) to prevent collisions and ensure even distribution.
2.  **Side-Channel Resistance:**  Carefully review all code that handles public keys to ensure that it is resistant to timing and memory access pattern side-channel attacks.  Use constant-time operations and avoid branches that depend on secret data.
3.  **Public Key Validation:**  Implement proper validation of public keys to ensure that they are valid and conform to the expected format.  Reject invalid keys to prevent potential attacks.
4.  **Denial of Service:** Adding support for public keys may increase the computational cost of contact discovery. Implement appropriate rate limiting and resource management to prevent denial-of-service attacks.

**V. Key Points**

*   **Prioritize Security:** Always prioritize security over performance or convenience. Carefully review all code changes to ensure that they do not weaken the existing security guarantees.
*   **Test Thoroughly:**  Write comprehensive unit and integration tests to verify that the new functionality works correctly and that existing functionality is not broken.
*   **Code Review:**  Have your code reviewed by experienced security engineers to identify potential vulnerabilities.
*   **Keep It Simple:**  Strive for simplicity and clarity in your code.  Complex code is more difficult to understand and more likely to contain bugs.

This is a challenging task, but by following these steps carefully, you can add support for public keys to the CDSI service while maintaining its security and privacy properties. Good luck, disciple!





## List of code modifications for CDSI to support phone numbers, public keys, and emails

### Task: Update Protobuf Definitions

File location: `c/proto/cdsi.proto`
Task description: Modify the `ClientRequest` and `EnclaveLoad` messages to use a generic `Identifier` type.

```protobuf
message ClientRequest {
  message Identifier {
    enum IdentifierType {
      E164 = 0;
      PUBLIC_KEY = 1;
      EMAIL = 2;
    }
    IdentifierType type = 1;
    bytes value = 2;  // The actual identifier value (E.164, public key, email hash, etc.)
  }
  repeated Identifier identifiers = 2;
  bytes aci_uak_pairs = 1;
  // ... other fields ...
}

message ClientResponse {
  message ContactInfo {
    ClientRequest.Identifier identifier = 1;
    bytes pni = 2;
    bytes aci = 3;
  }
  repeated ContactInfo contact_infos = 1;
  // ... other fields ...
}

message EnclaveLoad {
    message Tuple {
        string identifier_type = 1; // Type of identifier
        bytes identifier_value = 2;  // The actual identifier
        bytes aci = 3;
        bytes pni = 4;
        bytes uak = 5;
    }
    repeated Tuple tuples = 2;
}
```

File location: `src/main/proto/cdsi.proto`
Task description: Modify the `ClientRequest` and `EnclaveLoad` messages to use a generic `Identifier` type.

```protobuf
message ClientRequest {
  message Identifier {
    enum IdentifierType {
      E164 = 0;
      PUBLIC_KEY = 1;
      EMAIL = 2;
    }
    IdentifierType type = 1;
    bytes value = 2;  // The actual identifier value (E.164, public key, email hash, etc.)
  }
  repeated Identifier identifiers = 2;
  bytes aci_uak_pairs = 1;
  // ... other fields ...
}

message ClientResponse {
  message ContactInfo {
    ClientRequest.Identifier identifier = 1;
    bytes pni = 2;
    bytes aci = 3;
  }
  repeated ContactInfo contact_infos = 1;
  // ... other fields ...
}

message EnclaveLoad {
    message Tuple {
        string identifier_type = 1; // Type of identifier
        bytes identifier_value = 2;  // The actual identifier
        bytes aci = 3;
        bytes pni = 4;
        bytes uak = 5;
    }
    repeated Tuple tuples = 2;
}
```

### Task: Update Enclave Data Structures

File location: `c/aci_pni_uak_helpers/aci_pni_uak_helpers.h`
Task description: Modify the `signal_user_record` structure to accommodate different identifier types and sizes.

```c
#define MAX_IDENTIFIER_SIZE 64 // or a suitable size for your use case

typedef struct {
    uint8_t type; // e.g., 0 for E.164, 1 for public key, 2 for email
    uint8_t value[MAX_IDENTIFIER_SIZE];
} identifier_t;

struct signal_user_record
{
    identifier_t id;
    uint64_t aci[2];
    uint64_t pni[2];
    uint64_t uak[2];
};
```

### Task: Modify the `create_e164_pni_aci_triples` Function

File location: `c/aci_pni_uak_helpers/aci_pni_uak_helpers.c`
Task description: Update the `create_e164_pni_aci_triples` function to handle the new `signal_user_record` structure. You'll need to check the `identifier_type` and access the appropriate fields.

```c
error_t create_e164_pni_aci_triples(
    fixedset_t* index,
    size_t num_identifiers,
    ClientRequest_Identifier identifiers[num_identifiers], // Changed type
    signal_user_record ohtable_response[num_identifiers],
    e164_pni_aci_triple out_triples[num_identifiers])
{
    memset(out_triples, 0, num_identifiers * sizeof(*out_triples));
    for (uint64_t i = 0; i < (uint64_t) num_identifiers; ++i)
    {
        // copy locally
        signal_user_record r = ohtable_response[i];
        ClientRequest_Identifier identifier = identifiers[i];

        // We consider the record empty if EITHER:  e164 == UINT64_MAX  OR  r.aci is all zeros
        // We do this because we don&apos;t currently have a &quot;remove from ohtable&quot; function, so we
        // instead do a &quot;remove&quot; by overwriting the record for the e164 to all zeros.
        // The following line should be constant time, since it&apos;s bitwise.
        uint64_t got_and = 0;
        switch (r.id.type) {
            case E164:
                got_and = ZERO_OR_U64MAX(~r.id.e164) &amp; (ZERO_OR_U64MAX(r.aci[0]) | ZERO_OR_U64MAX(r.aci[1]));
                break;
            case PUBLIC_KEY:
                // Implement logic for public key
                break;
            case EMAIL:
                // Implement logic for email
                break;
            default:
                // Handle unknown identifier type
                break;
        }
        // munge uak[0]=i in case where e164 not found, so hash lookups are all different
        r.uak[0] = (r.uak[0] &amp; got_and) | (i &amp; ~got_and);

        // We want to search for an ACI/UAK pair, but they&apos;re not contiguously laid
        // out in the signal_user_record, so we need to make our own pair.
        aci_uak_pair pair = { .aci = { r.aci[0], r.aci[1] }, .uak = { r.uak[0], r.uak[1] } };

        // We store the UAK as all zeros in the OHTable if there isn&apos;t an associated UAK,
        // in which case we never want to return the ACI.  We do the lookup into the
        // map regardless, but then check to make sure the UAK is nonzero as well.
        bool add_aci = fixedset_get(index, &amp;pair) &amp; ((r.uak[0] | r.uak[1]) != 0);
        uint64_t aci_and = add_aci ? UINT64_MAX : 0;

        // This should give us:
        //   - Everything all zeros, if e164 was not found
        //   - ACI all zeros, if ACI/UAK pair was not found
        out_triples[i].e164 =   got_and &amp;           r.id.e164;
        out_triples[i].pni[0] = got_and &amp;           r.pni[0];
        out_triples[i].pni[1] = got_and &amp;           r.pni[1];
        out_triples[i].aci[0] = got_and &amp; aci_and &amp; r.aci[0];
        out_triples[i].aci[1] = got_and &amp; aci_and &amp; r.aci[1];
    }
    return err_SUCCESS;
}
```

### Task: Adapt Fixedset for New Identifier Types

File location: `c/fixedset/fixedset.h`
Task description: Update the `fixedset_entry_t` structure to accommodate the generic identifier.

```c
typedef struct fixedset_entry_t {
  uint32_t jump;
  identifier_t kv; // Changed to identifier_t
} fixedset_entry_t;
```

File location: `c/fixedset/fixedset.c`
Task description: Modify the `keq` and `khash` functions to handle different identifier types.

```c
static bool keq(fixedset_t* h, const unsigned char* a, const unsigned char* b, uint8_t type_a, uint8_t type_b) {
    //Add handling for the case where type_a != type_b.
    if (type_a != type_b) return false;

    unsigned char out = 0;
    if(type_a == 0) {
        for (size_t i = 0; i &lt; h-&gt;ksize; i++) {
          out |= a[i] ^ b[i];
        }
    } else if(type_a == 1) {
        for (size_t i = 0; i &lt; h-&gt;ksize; i++) {
          out |= a[i] ^ b[i];
        }
    } else {
        return false;
    }
    return out == 0;
}
```

```c
static size_t khash(fixedset_t* h, const unsigned char* k, uint8_t identifier_type) {
  size_t out = 0;
  // different hashing based on identifier type
  if (identifier_type == 0) {
    halfsiphash(k, h-&gt;ksize, h-&gt;halfsiphash_key, (unsigned char*) &amp;out, sizeof(out));
  } else if (identifier_type == 1){
    // Public Key Type
    // Implement a more robust hashing algorithm for public keys (e.g., SHA-256)
    // and truncate the result to fit within a size_t.
    // This is a placeholder; replace with your actual SHA-256 implementation
  }
  return out;
}
```

### Task: Modify Enclave Load and Run Functions

File location: `c/enclave/enc.c`
Task description: Update `enclave_load_pb` and `enclave_run` to handle the new `ClientRequest` and `EnclaveLoad` proto messages.

```c
int enclave_load_pb(
    size_t len,
    unsigned char *load_request_pb)
{
  // ...
  struct org_signal_cdsi_enclave_load_t *load_req = org_signal_cdsi_enclave_load_new(workspace, workspace_size);
  // ...

  if (load_req-&gt;clear_all)
  {
    // clear the table
    TEST_LOG(&quot;sharded_ohtable_clear&quot;);
    sharded_ohtable_clear(g_table);
  }

  for (size_t i = 0; i &lt; load_req-&gt;tuples.size; ++i) {
      struct org_signal_cdsi_enclave_load_tuple_t* tuple = load_req-&gt;tuples.items_p + i;
      // Extract identifier and associated data from tuple
      // Use tuple-&gt;identifier_type and tuple-&gt;identifier_value
      // ...
  }

  // ...
}

int enclave_run(
    uint64_t cli,
    uint32_t permits,
    size_t in_size,
    unsigned char *in,
    size_t out_size,
    unsigned char *out,
    size_t *actual_out_size)
{
  // ...
    for(size_t i = 0; i < req->identifiers.size; ++i) {
        struct org_signal_cdsi_client_request_identifier_t* identifier = req->identifiers.items_p + i;
        // Access identifier properties
        char* type = identifier->type;
        uint8_t* value = identifier->value.buf_p;
        size_t value_size = identifier->value.size;

        //Now what was e164s has to be handled inside the loop.
    }
  // ...
}
```

### Task: Update Java Code

File location: `src/main/java/org/signal/cdsi/EnclaveClient.java`
Task description: Modify the `EnclaveClient` class and related components to handle the different types of identifiers.

File location: `src/main/java/org/signal/cdsi/WebSocketHandler.java`
Task description: Adjust the `WebSocketHandler` to properly construct and parse the updated `ClientRequest` and `ClientResponse` messages.

The Java code will require significant changes to accommodate the new protobuf definitions and data structures. You'll need to regenerate the Java classes from the proto files and modify the code to handle the new generic identifier type.

### Task: Update Tests

File location: `c/testhost/tests/testhost.c` and `src/test/java/org/signal/cdsi/`
Task description: Modify the test code to include tests for the new public key-based contact discovery. This should involve creating test cases with valid and invalid public keys, and verifying that the service returns the correct results. Ensure that these tests cover both the SGX enclave and the host-side Java code.

These are the major areas that need to be addressed. Keep in mind:
*   You will encounter more files that need to be changed than what I have mentioned.
*   Code quality is important.
*   The test suite needs to be fully functional.
*   Security is more important than anything else.

