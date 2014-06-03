package com.salix.core.ser.codec.impl;

import com.salix.core.ser.codec.*;
import com.salix.core.io.DataOutput;
import com.salix.core.io.DataInput;

/**
 * @author duanbn
 */
public class LongCodec implements Codec<Long>
{

    public void encode(DataOutput output, Long v, CodecConfig config)
    {
        output.writeByte(CodecType.TYPE_OLONG);

        if (v == null) {
            output.writeByte(CodecType.NULL);
        } else {
            output.writeByte(CodecType.NOT_NULL);
            output.writeLong(v);
        }
    }

    public Long decode(DataInput input, CodecConfig config)
    {
        byte isNull = input.readByte();
        if (isNull == CodecType.NULL) {
            return null;
        }
        return input.readLong();
    }

}
