package org.duanbn.salix.core.ser.codec.impl;

import org.duanbn.salix.core.io.DataInput;
import org.duanbn.salix.core.io.DataOutput;
import org.duanbn.salix.core.ser.codec.*;

/**
 * @author duanbn
 */
public class CharCodec implements Codec<Character>
{

    public void encode(DataOutput output, Character v, CodecConfig config)
    {
        output.writeByte(CodecType.TYPE_OCHAR);

        if (v == null) {
            output.writeByte(CodecType.NULL);
        } else {
            output.writeByte(CodecType.NOT_NULL);
            output.writeChar(v);
        }
    }

    public Character decode(DataInput input, CodecConfig config)
    {
        byte isNull = input.readByte();
        if (isNull == CodecType.NULL) {
            return null;
        }
        return input.readChar();
    }

}
