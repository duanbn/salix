package org.duanbn.salix.core.ser.codec.impl;

import org.duanbn.salix.core.io.DataInput;
import org.duanbn.salix.core.io.DataOutput;
import org.duanbn.salix.core.ser.codec.*;

/**
 * @author duanbn
 */
public class DoubleCodec implements Codec<Double>
{

    public void encode(DataOutput output, Double v, CodecConfig config)
    {
        output.writeByte(CodecType.TYPE_ODOUBLE);

        if (v == null) {
            output.writeByte(CodecType.NULL);
        } else {
            output.writeByte(CodecType.NOT_NULL);
            output.writeDouble(v);
        }
    }

    public Double decode(DataInput input, CodecConfig config)
    {
        byte isNull = input.readByte();
        if (isNull == CodecType.NULL) {
            return null;
        }
        return input.readDouble();
    }

}
