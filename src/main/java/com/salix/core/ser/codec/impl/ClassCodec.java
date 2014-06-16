package com.salix.core.ser.codec.impl;

import com.salix.core.ser.codec.*;
import com.salix.core.io.DataOutput;
import com.salix.core.io.DataInput;

/**
 * @author duanbn
 */
public class ClassCodec implements Codec<Class>
{

    public void encode(DataOutput output, Class v, CodecConfig config)
    {
        output.writeByte(CodecType.TYPE_CLASS);

        if (v == null) {
            output.writeByte(CodecType.NULL);
        } else {
            output.writeByte(CodecType.NOT_NULL);
            output.writeUTF8(v.getName());
        }
    }

    public Class decode(DataInput input, CodecConfig config)
    {
        byte isNull = input.readByte();
        if (isNull == CodecType.NULL) {
            return null;
        }
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            String className = input.readUTF8();
            if (Boolean.TYPE.getName().equals(className)) {
                return Boolean.TYPE;
            } else if (Byte.TYPE.getName().equals(className)) {
                return Byte.TYPE;
            } else if (Character.TYPE.getName().equals(className)) {
                return Character.TYPE;
            } else if (Short.TYPE.getName().equals(className)) {
                return Short.TYPE;
            } else if (Integer.TYPE.getName().equals(className)) {
                return Integer.TYPE;
            } else if (Long.TYPE.getName().equals(className)) {
                return Long.TYPE;
            } else if (Float.TYPE.getName().equals(className)) {
                return Float.TYPE;
            } else if (Double.TYPE.getName().equals(className)) {
                return Double.TYPE;
            }

            Class c = loader.loadClass(className);
            return c;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
