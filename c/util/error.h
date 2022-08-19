// Copyright 2022 Signal Messenger, LLC
// SPDX-License-Identifier: AGPL-3.0-only

#ifndef _CDSI_ERROR_H
#define _CDSI_ERROR_H

#include <stddef.h>

typedef enum {
  // General purpose errors, use specific errors (or add them) if possible.
  err_SUCCESS,
  err_OOM,

  err_ENCLAVE__GENERAL__ = 100,  // general enclave functionality, not associated with an external function
  err_ENCLAVE__GENERAL__LOAD_MODULE_HOST_FILE_SYSTEM,
  err_ENCLAVE__GENERAL__MOUNT_ROOT,
  err_ENCLAVE__GENERAL__SIGNAL_RECORD_SIZE_INVALID,
  err_ENCLAVE__GENERAL__CLIENT_STATE,
  err_ENCLAVE__GENERAL__GET_EVIDENCE,
  err_ENCLAVE__GENERAL__EREPORT_SIZE,
  err_ENCLAVE__GENERAL__ENTROPY,
  err_ENCLAVE__GENERAL__INVALID_PRIVATE_KEY,
  err_ENCLAVE__GENERAL__REINIT,
  err_ENCLAVE__GENERAL__EREPORT_PB_NEW,
  err_ENCLAVE__GENERAL__EREPORT_PB_ENCODE,
  err_ENCLAVE__GENERAL__OE_ATTESTER_INITIALIZE,
  err_ENCLAVE__GENERAL__OE_SERIALIZE_CUSTOM_CLAIMS,
  err_ENCLAVE__GENERAL__BUFFER_TOO_SMALL,
  err_ENCLAVE__GENERAL__INVALID_LOAD_FACTOR,
  err_ENCLAVE__GENERAL__CLIENT_ADD_FAILED,
  err_ENCLAVE__GENERAL__CLIENT_GET_FAILED,
  err_ENCLAVE__GENERAL__CLIENT_REMOVE_FAILED,
  err_ENCLAVE__GENERAL__CLIENT_LOCK_FAILED,
  err_ENCLAVE__GENERAL__NOINIT,

  err_ENCLAVE__LOADPB__ = 200,  // enclave_load_pb
  err_ENCLAVE__LOADPB__REQUEST_PB_NEW,
  err_ENCLAVE__LOADPB__SECRET_TOO_LARGE,
  err_ENCLAVE__LOADPB__TUPLES_INVALID,
  err_ENCLAVE__LOADPB__REQUEST_PB_DECODE,

  err_ENCLAVE__NEWCLIENT__ = 300,  // enclave_new_client
  err_ENCLAVE__NEWCLIENT__EREPORT_TOO_LARGE,
  err_ENCLAVE__NEWCLIENT__LOCK_INIT_FAILED,

  err_ENCLAVE__RATELIMIT__ = 400,  // enclave_rate_limit
  err_ENCLAVE__RATELIMIT__REQUEST_PB_NEW,
  err_ENCLAVE__RATELIMIT__RESPONSE_PB_NEW,
  err_ENCLAVE__RATELIMIT__INVALID_TOKEN,
  err_ENCLAVE__RATELIMIT__INVALID_E164S,
  err_ENCLAVE__RATELIMIT__MERGE_FAILED,
  err_ENCLAVE__RATELIMIT__MERGE_NOT_SORTED,
  err_ENCLAVE__RATELIMIT__BAD_OUTPUT,
  err_ENCLAVE__RATELIMIT__NO_TOKEN,
  err_ENCLAVE__RATELIMIT__UNSUPPORTED_VERSION,
  err_ENCLAVE__RATELIMIT__REQUEST_PB_DECODE,
  err_ENCLAVE__RATELIMIT__RESPONSE_PB_ENCODE,
  err_ENCLAVE__RATELIMIT__REQUEST_PB_INVALID,
  err_ENCLAVE__RATELIMIT__INVALID_TOKEN_FORMAT,

  err_ENCLAVE__RUN__ = 500,  // enclave_run
  err_ENCLAVE__RUN__INVALID_CLIENT,
  err_ENCLAVE__RUN__REQUEST_PB_NEW,
  err_ENCLAVE__RUN__NO_TOKEN_ACK,
  err_ENCLAVE__RUN__INDEX_PAIRS,
  err_ENCLAVE__RUN__RESPONSE_PB_NEW,
  err_ENCLAVE__RUN__REQUEST_PB_DECODE,
  err_ENCLAVE__RUN__RESPONSE_PB_ENCODE,

  err_ENCLAVE__HANDSHAKE__ = 600,  // enclave_handshake
  err_ENCLAVE__HANDSHAKE__INVALID_STATE,
  err_ENCLAVE__HANDSHAKE__EXTRACT_PUBKEY_PB,
  err_ENCLAVE__HANDSHAKE__EXTRACT_PUBKEY_VERIFICATION,

  err_FIXEDSET__ = 700,
  err_FIXEDSET__RESIZE_INVALID,

  err_ORAM__ = 800,
  err_ORAM__PUT_FAILURE,
  err_ORAM__GET_FAILURE,
  err_ORAM__ACCESS_UNALLOCATED_BLOCK,
  err_ORAM__POSITION_MAP_NOT_FOUND,
  err_ORAM__STASH_NOT_FOUND,

  err_OHTABLE__ = 900,
  err_OHTABLE__PUT__FAILURE,
  err_OHTABLE__GET__FAILURE,
  err_OHTABLE__ROBIN_HOOD_UPSERT__RECORD_EMPTY,
  err_OHTABLE__TABLE_FULL,

  err_ENCLAVE__RETRYRESPONSE__ = 1000,
  err_ENCLAVE__RETRYRESPONSE__RESPONSE_PB_ENCODE,
  err_ENCLAVE__RETRYRESPONSE__RESPONSE_PB_NEW,

  err_JNISHIM__ = 1100,
  err_JNISHIM__CREATE_ENCLAVE,
  err_JNISHIM__INIT_ENCLAVE,
  err_JNISHIM__METHOD_CALL_FAILURE,

  err_QUEUE__ = 1200,
  err_QUEUE__CLOSED,

  err_SHARD__ = 1300,
  err_SHARD__DESTROYING,

  err_ENCLAVE__TABLE_STATISTICS__ = 1400,
  err_ENCLAVE__TABLE_STATISTICS__RESPONSE_PB_NEW,
  err_ENCLAVE__TABLE_STATISTICS__RESPONSE_PB_ENCODE,
  err_ENCLAVE__TABLE_STATISTICS__RESPONSE_PB_ALLOC_SHARDS,
  err_ENCLAVE__TABLE_STATISTICS__RESPONSE_PB_ALLOC_VALUES,

  err_TEST__ = 1000000,
  err_TEST__ASSERTION,

  err_HOST__ = 2000000,
  err_HOST__GENERAL__ = 2000100, // general host functionality, not associated with an external function
  err_HOST__GENERAL_OE_ERROR,

  err_HOST__LOADPB__ = 2000200, // enclave_load_pb
  err_HOST__LOADPB__REQUEST_PB,
  err_HOST__LOADPB__TOO_MANY_SAMPLES,
  err_HOST__LOADPB__ENCLAVE_CALL,

  err_HOST__NEWCLIENT__ = 2000300, // enclave_new_client
  err_HOST__NEWCLIENT__NOISE_HANDSHAKE_NEW,

  err_HOST__RATELIMIT__ = 2000400, // enclave_rate_limit
  err_HOST__RATELIMIT__REQUEST_PB,
  err_HOST__RATELIMIT__RESPONSE_ENCODE,
  err_HOST__RATELIMIT__ENCRYPT,
  err_HOST__RATELIMIT__DECRYPT,
  err_HOST__RATELIMIT__CALL_ENCLAVE,

  err_HOST__RUN__ = 2000500,
  err_HOST__RUN__DECRYPT,
  err_HOST__RUN__REQUEST_PB,
  err_HOST__RUN__RESPONSE_PB,
  err_HOST__RUN__ENCRYPT,
  err_HOST__RUN__CALL_ENCLAVE,

  err_HOST__HANDSHAKE__ = 2000600, // enclave_handshake
  err_HOST__HANDSHAKE__NEW,
  err_HOST__HANDSHAKE__START,
  err_HOST__HANDSHAKE__INVALID_STATE,
  err_HOST__HANDSHAKE__READ,
  err_HOST__HANDSHAKE__WRITE,
  err_HOST__HANDSHAKE__SPLIT,
  err_HOST__HANDSHAKE__CALL_ENCLAVE,
  err_HOST__HANDSHAKE__INITIALIZE_VERIFIER,
  err_HOST__HANDSHAKE__INVALID_REPORT,
  err_HOST__HANDSHAKE__INVALID_CLAIMS,

  err_HOST__TABLE_STATISTICS__ = 2000700,
  err_HOST__TABLE_STATISTICS__PB_NEW,
  err_HOST__TABLE_STATISTICS__PB_DECODE,

  // Noise error spaces, used by noiseutil/noise.h's noise_errort() function.
  err_NOISE__CIPHERSTATE__ENCRYPT__ = 3001000,
  err_NOISE__CIPHERSTATE__DECRYPT__ = 3001100,
  err_NOISE__HANDSHAKESTATE__NEW__ = 3001200,
  err_NOISE__HANDSHAKESTATE__READ__ = 3001300,
  err_NOISE__HANDSHAKESTATE__WRITE__ = 3001400,
  err_NOISE__HANDSHAKESTATE__SPLIT__ = 3001500,
  err_NOISE__HANDSHAKESTATE__START__ = 3001600,
  err_NOISE__DHSTATE__NEW__ = 3001700,
  err_NOISE__DHSTATE__GET_PUBLIC_KEY__ = 3001800,
  err_NOISE__DHSTATE__SET_KEYPAIR_PRIVATE__ = 3001900,
  err_NOISE__DHSTATE__SET_PUBLIC_KEY__ = 3002000,
} error_t;

#define RETURN_IF_ERROR(x) \
  do                       \
  {                        \
    error_t _x = (x);      \
    if (_x != err_SUCCESS) \
      return _x;           \
  } while (0)

#define GOTO_IF_ERROR(x, lbl) \
  do                          \
  {                           \
    error_t _x = (x);         \
    if (_x != err_SUCCESS)    \
      goto lbl;               \
  } while (0)

#define MALLOCZ_SIZE(v, s) ((NULL == (v = calloc(1, s))) ? err_OOM : err_SUCCESS)

// malloc's and zero's memory.
// Usage:
// foo_t* f;
// if (err_SUCCESS != MALLOCZ(f)) ...
#define MALLOCZ(v) MALLOCZ_SIZE(v, sizeof(*v))

// malloc's and zero's memory.
// Usage:
// foo_t* f;
// MALLOCZ_OR_RETURN_ERROR(f);  // will return err_OOM on failure.
#define MALLOCZ_OR_RETURN_ERROR(v) RETURN_IF_ERROR(MALLOCZ(v))

#define ASSERT_ERR(x, err) \
  RETURN_IF_ERROR((x) ? err_SUCCESS : err)

#endif  // _CDSI_ERROR_H
