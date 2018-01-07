package org.duanbn.salix.core.ser.codec.impl;

import org.duanbn.salix.core.io.DataInput;
import org.duanbn.salix.core.io.DataOutput;
import org.duanbn.salix.core.ser.codec.*;

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
