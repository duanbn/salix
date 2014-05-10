package com.salix.core.ser.codec.my;

import com.salix.core.ser.codec.*;
import com.salix.core.io.DataOutput;
import com.salix.core.io.DataInput;

public class OShortArrayCodec implements Codec<Short[]>
{

    public void encode(DataOutput output, Short[] v, CodecConfig config)
    {
        // write type
        output.writeByte(CodecType.TYPE_ARRAY_OSHORT);

        // write is null
        if (v == null) {
            output.writeByte(CodecType.NULL);
            return;
        }
        output.writeByte(CodecType.NOT_NULL);

        // write length
        int length = v.length;
        output.writeVInt(length);

        // write value
        for (int i=0; i<length; i++) {
            if (v[i] == null) {
                output.writeByte(CodecType.NULL);
                continue;
            }
            output.writeByte(CodecType.NOT_NULL);
            output.writeShort(v[i]);
        }
    }

    public Short[] decode(DataInput input, CodecConfig config)
    {
        // read is null
        if (input.readByte() == CodecType.NULL) {
            return null;
        }

        // read length
        int length = input.readVInt();
        Short[] array = new Short[length];
        for (int i=0; i<length; i++) {
            if (input.readByte() == CodecType.NOT_NULL) {
                array[i] = input.readShort();
            }
        }
        return array;
    }

}
