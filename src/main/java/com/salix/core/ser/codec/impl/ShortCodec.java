package com.salix.core.ser.codec.impl;

import com.salix.core.ser.codec.*;
import com.salix.core.io.DataOutput;
import com.salix.core.io.DataInput;

/**
 * @author duanbn
 */
public class ShortCodec implements Codec<Short>
{

    public void encode(DataOutput output, Short v, CodecConfig config)
    {
        output.writeByte(CodecType.TYPE_OSHORT);

        if (v == null) {
            output.writeByte(CodecType.NULL);
        } else {
            output.writeByte(CodecType.NOT_NULL);
            output.writeShort(v);
        }
    }

    public Short decode(DataInput input, CodecConfig config)
    {
        byte isNull = input.readByte();
        if (isNull == CodecType.NULL) {
            return null;
        }
        return input.readShort();
    }

}
