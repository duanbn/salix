package com.salix.core.ser.codec.my;

import com.salix.core.ser.codec.*;
import com.salix.core.io.DataOutput;
import com.salix.core.io.DataInput;

/**
 * @author duanbn
 */
public class ByteCodec implements Codec<Byte>
{

    public void encode(DataOutput output, Byte v, CodecConfig config)
    {
        output.writeByte(CodecType.TYPE_OBYTE);

        if (v == null) {
            output.writeByte(CodecType.NULL);
        } else {
            output.writeByte(CodecType.NOT_NULL);
            output.writeByte(v);
        }
    }

    public Byte decode(DataInput input, CodecConfig config)
    {
        byte isNull = input.readByte();
        if (isNull == CodecType.NULL) {
            return null;
        }
        return input.readByte();
    }

}
