package com.salix.core.ser.codec.impl;

import com.salix.core.ser.codec.*;
import com.salix.core.io.DataOutput;
import com.salix.core.io.DataInput;

/**
 * @author duanbn
 */
public class IntCodec implements Codec<Integer>
{

    public void encode(DataOutput output, Integer v, CodecConfig config)
    {
        output.writeByte(CodecType.TYPE_OINT);

        if (v == null) {
            output.writeByte(CodecType.NULL);
        } else {
            output.writeByte(CodecType.NOT_NULL);
            output.writeInt(v);
        }
    }

    public Integer decode(DataInput input, CodecConfig config)
    {
        byte isNull = input.readByte();
        if (isNull == CodecType.NULL) {
            return null;
        }
        return input.readInt();
    }

}
