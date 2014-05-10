package com.salix.core.ser.codec.my;

import com.salix.core.ser.codec.*;
import com.salix.core.io.DataOutput;
import com.salix.core.io.DataInput;
import com.salix.core.util.ReflectUtil;
import com.salix.core.ser.codec.exception.*;

import java.lang.reflect.*;

public class EnumArrayCodec implements Codec<Enum[]>
{

    public void encode(DataOutput output, Enum[] v, CodecConfig config) throws CodecException
    {
        // write type
        output.writeByte(CodecType.TYPE_ARRAY_ENUM);

        // write is null
        if (v == null) {
            output.writeByte(CodecType.NULL);
            return;
        }
        output.writeByte(CodecType.NOT_NULL);

        // write length
        int length = v.length;
        output.writeVInt(length);

        boolean isWriteClass = false;
        // write value
        for (int i=0; i<length; i++) {
            if (v[i] == null) {
                output.writeByte(CodecType.NULL);
                continue;
            }
            output.writeByte(CodecType.NOT_NULL);
            if (!isWriteClass) {
                output.writeGBK(v[i].getDeclaringClass().getName());
                isWriteClass = true;
            }
            output.writeGBK(v[i].name());
        }
    }

    public Enum[] decode(DataInput input, CodecConfig config) throws CodecException
    {
        // read is null
        if (input.readByte() == CodecType.NULL) {
            return null;
        }

        // read length
        int length = input.readVInt();

        boolean isReadClass = false;
        Object array = null;
        Class<Enum> oc = null;
        for (int i=0; i<length; i++) {
            if (input.readByte() == CodecType.NOT_NULL) {
                if (!isReadClass) {
                    oc = (Class<Enum>) ReflectUtil.getClass(input.readGBK());
                    array = Array.newInstance(oc, length);
                    isReadClass = true;
                }
                String name = input.readGBK();
                Array.set(array, i, Enum.valueOf(oc, name));
            }
        }
        return (Enum[])array;
    }

}
