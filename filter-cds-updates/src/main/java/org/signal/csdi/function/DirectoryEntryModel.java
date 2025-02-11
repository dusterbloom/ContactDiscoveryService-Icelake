package org.signal.csdi.function;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.signal.cdsi.proto.DirectoryEntryProto;  // We'll need to generate this from cdsi.proto

public record DirectoryEntry(
    long e164,
    byte[] aci,
    byte[] pni,
    byte[] uak
) {
    public static DirectoryEntry parseFrom(byte[] data) throws InvalidProtocolBufferException {
        DirectoryEntryProto proto = DirectoryEntryProto.parseFrom(data);
        return new DirectoryEntry(
            proto.getE164(),
            proto.getAci().toByteArray(),
            proto.getPni().toByteArray(),
            proto.hasUak() ? proto.getUak().toByteArray() : null
        );
    }

    public byte[] toByteArray() {
        DirectoryEntryProto.Builder builder = DirectoryEntryProto.newBuilder()
            .setE164(e164)
            .setAci(ByteString.copyFrom(aci))
            .setPni(ByteString.copyFrom(pni));
            
        if (uak != null) {
            builder.setUak(ByteString.copyFrom(uak));
        }
        
        return builder.build().toByteArray();
    }
}