package org.duanbn.salix.core.ser.codec.impl;

import org.duanbn.salix.core.io.DataInput;
import org.duanbn.salix.core.io.DataOutput;
import org.duanbn.salix.core.ser.codec.Codec;
import org.duanbn.salix.core.ser.codec.CodecConfig;
import org.duanbn.salix.core.ser.codec.CodecType;
import org.duanbn.salix.core.util.ReflectUtil;
import org.duanbn.salix.exception.CodecException;

public class EnumCodec implements Codec<Enum>
{

    public void encode(DataOutput output, Enum v, CodecConfig config) throws CodecException
    {

        output.writeByte(CodecType.TYPE_ENUM);

        if (v == null) {
            output.writeByte(CodecType.NULL);
        } else {
            output.writeByte(CodecType.NOT_NULL);
            output.writeGBK(v.getDeclaringClass().getName());
            output.writeGBK(v.name());
        }

    }

    public Enum decode(DataInput input, CodecConfig config) throws CodecException
    {
        byte isNull = input.readByte();
        if (isNull == CodecType.NULL) {
            return null;
        }
        
        Class<Enum> oc = (Class<Enum>) ReflectUtil.getClass(input.readGBK());
        String name = input.readGBK();
        return Enum.valueOf(oc, name);
    }

}
