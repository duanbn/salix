package com.salix.core.ser.codec.impl;

import com.salix.core.ser.codec.*;
import com.salix.core.io.DataOutput;
import com.salix.core.io.DataInput;

/**
 * @author duanbn
 */
public class BooleanCodec implements Codec<Boolean>
{

    public void encode(DataOutput output, Boolean v, CodecConfig config)
    {
        output.writeByte(CodecType.TYPE_OBOOLEAN);

        if (v == null) {
            output.writeByte(CodecType.NULL);
        } else {
            output.writeByte(CodecType.NOT_NULL);
            output.writeBoolean(v);
        }
    }

    public Boolean decode(DataInput input, CodecConfig config)
    {
        byte isNull = input.readByte();
        if (isNull == CodecType.NULL) {
            return null;
        }
        return input.readBoolean();
    }

}
