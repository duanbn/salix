package org.duanbn.salix.core.ser.codec.impl;

import org.duanbn.salix.core.io.DataInput;
import org.duanbn.salix.core.io.DataOutput;
import org.duanbn.salix.core.ser.codec.*;

/**
 * @author duanbn
 */
public class StringCodec implements Codec<String>
{

    public void encode(DataOutput output, String v, CodecConfig config)
    {
        output.writeByte(CodecType.TYPE_STRING);

        if (v == null) {
            output.writeByte(CodecType.NULL);
        } else {
            output.writeByte(CodecType.NOT_NULL);
            output.writeGBK(v);
        }
    }

    public String decode(DataInput input, CodecConfig config)
    {
        byte isNull = input.readByte();
        if (isNull == CodecType.NULL) {
            return null;
        }
        return input.readGBK();
    }

}
