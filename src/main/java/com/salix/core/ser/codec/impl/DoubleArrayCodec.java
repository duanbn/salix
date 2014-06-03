package com.salix.core.ser.codec.impl;

import com.salix.core.ser.codec.*;
import com.salix.core.io.DataOutput;
import com.salix.core.io.DataInput;

public class DoubleArrayCodec implements Codec<double[]>
{

    public void encode(DataOutput output, double[] v, CodecConfig config)
    {
        // write type
        output.writeByte(CodecType.TYPE_ARRAY_DOUBLE);

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
            output.writeDouble(v[i]);
        }
    }

    public double[] decode(DataInput input, CodecConfig config)
    {
        // read is null
        if (input.readByte() == CodecType.NULL) {
            return null;
        }

        // read length
        int length = input.readVInt();
        double[] array = new double[length];
        for (int i=0; i<length; i++) {
            array[i] = input.readDouble();
        }
        return array;
    }

}
