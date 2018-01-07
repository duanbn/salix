package org.duanbn.salix.core.ser.codec.impl;

import org.duanbn.salix.core.io.DataInput;
import org.duanbn.salix.core.io.DataOutput;
import org.duanbn.salix.core.ser.codec.*;

public class ODoubleArrayCodec implements Codec<Double[]>
{

    public void encode(DataOutput output, Double[] v, CodecConfig config)
    {
        // write type
        output.writeByte(CodecType.TYPE_ARRAY_ODOUBLE);

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
            output.writeDouble(v[i]);
        }
    }

    public Double[] decode(DataInput input, CodecConfig config)
    {
        // read is null
        if (input.readByte() == CodecType.NULL) {
            return null;
        }

        // read length
        int length = input.readVInt();
        Double[] array = new Double[length];
        for (int i=0; i<length; i++) {
            if (input.readByte() == CodecType.NOT_NULL) {
                array[i] = input.readDouble();
            }
        }
        return array;
    }

}
