package com.salix.core.ser.codec.impl;

import com.salix.core.ser.codec.*;
import com.salix.core.io.DataOutput;
import com.salix.core.io.DataInput;

/**
 * @author duanbn
 */
public class FloatCodec implements Codec<Float>
{

    public void encode(DataOutput output, Float v, CodecConfig config)
    {
        output.writeByte(CodecType.TYPE_OFLOAT);

        if (v == null) {
            output.writeByte(CodecType.NULL);
        } else {
            output.writeByte(CodecType.NOT_NULL);
            output.writeFloat(v);
        }
    }

    public Float decode(DataInput input, CodecConfig config)
    {
        byte isNull = input.readByte();
        if (isNull == CodecType.NULL) {
            return null;
        }
        return input.readFloat();
    }

}
