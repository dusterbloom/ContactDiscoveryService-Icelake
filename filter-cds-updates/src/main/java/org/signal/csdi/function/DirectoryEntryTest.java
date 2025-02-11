package org.signal.csdi.function;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

class DirectoryEntryTest {

    @Test
    void testProtobufSerializationDeserialization() throws Exception {
        // Create test data
        long e164 = 14155551212L;
        byte[] aci = new byte[16];
        byte[] pni = new byte[16];
        byte[] uak = new byte[16];
        Arrays.fill(aci, (byte)1);
        Arrays.fill(pni, (byte)2);
        Arrays.fill(uak, (byte)3);

        // Create original entry
        DirectoryEntry original = new DirectoryEntry(e164, aci, pni, uak);

        // Serialize to bytes
        byte[] serialized = original.toByteArray();

        // Deserialize back
        DirectoryEntry deserialized = DirectoryEntry.parseFrom(serialized);

        // Verify
        assertEquals(original.e164(), deserialized.e164());
        assertArrayEquals(original.aci(), deserialized.aci());
        assertArrayEquals(original.pni(), deserialized.pni());
        assertArrayEquals(original.uak(), deserialized.uak());
    }

    @Test
    void testProtobufWithNullUak() throws Exception {
        // Create test data
        long e164 = 14155551212L;
        byte[] aci = new byte[16];
        byte[] pni = new byte[16];
        Arrays.fill(aci, (byte)1);
        Arrays.fill(pni, (byte)2);

        // Create original entry with null UAK
        DirectoryEntry original = new DirectoryEntry(e164, aci, pni, null);

        // Serialize to bytes
        byte[] serialized = original.toByteArray();

        // Deserialize back
        DirectoryEntry deserialized = DirectoryEntry.parseFrom(serialized);

        // Verify
        assertEquals(original.e164(), deserialized.e164());
        assertArrayEquals(original.aci(), deserialized.aci());
        assertArrayEquals(original.pni(), deserialized.pni());
        assertNull(deserialized.uak());
    }
}