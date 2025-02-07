package org.signal.cdsi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.UUID;

public class Account {
    @JsonProperty
    String e164;

    @JsonProperty
    byte[] uuid;

    @JsonProperty
    boolean canonicallyDiscoverable;

    @JsonProperty
    byte[] pni;

    @JsonProperty
    byte[] uak;

    public Account() {} // Empty constructor for JSON

    public Account(String e164, byte[] uuid, boolean canonicallyDiscoverable, byte[] pni, byte[] uak) {
        this.e164 = e164;
        this.uuid = uuid;
        this.canonicallyDiscoverable = canonicallyDiscoverable;
        this.pni = pni;
        this.uak = uak;
    }

    public Account forceNotInCds() {
        return new Account(e164, uuid, false, pni, uak);
    }

    public String partitionKey() {
        return UUID.nameUUIDFromBytes(uuid).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return canonicallyDiscoverable == account.canonicallyDiscoverable &&
                e164.equals(account.e164) &&
                Arrays.equals(uuid, account.uuid) &&
                Arrays.equals(pni, account.pni) &&
                Arrays.equals(uak, account.uak);
    }
}