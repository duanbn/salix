package com.salix.core.ser.codec.my;

import com.salix.core.ser.codec.*;
import com.salix.core.io.DataOutput;
import com.salix.core.io.DataInput;

public class ShortArrayCodec implements Codec<short[]>
{

    public void encode(DataOutput output, short[] v, CodecConfig config)
    {
        // write type
        output.writeByte(CodecType.TYPE_ARRAY_SHORT);

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
            output.writeShort(v[i]);
        }
    }

    public short[] decode(DataInput input, CodecConfig config)
    {
        // read is null
        if (input.readByte() == CodecType.NULL) {
            return null;
        }

        // read length
        int length = input.readVInt();
        short[] array = new short[length];
        for (int i=0; i<length; i++) {
            array[i] = input.readShort();
        }
        return array;
    }

}
