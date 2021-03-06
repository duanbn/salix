package org.duanbn.salix.core.ser.codec.impl;

import java.lang.reflect.Array;

import org.duanbn.salix.core.io.DataInput;
import org.duanbn.salix.core.io.DataOutput;
import org.duanbn.salix.core.ser.codec.Codec;
import org.duanbn.salix.core.ser.codec.CodecConfig;
import org.duanbn.salix.core.ser.codec.CodecType;
import org.duanbn.salix.core.util.ReflectUtil;
import org.duanbn.salix.exception.CodecException;

/**
 * 对对象数组进行编码.
 *
 * @author duanbn
 */
public class ObjectArrayCodec implements Codec<Object[]>
{

    public void encode(DataOutput output, Object[] v, CodecConfig config) throws CodecException
    {
        try {
            // write type
            output.writeByte(CodecType.TYPE_ARRAY_OBJECT);

            // write is null
            if (v == null) {
                output.writeByte(CodecType.NULL);
                return;
            }
            output.writeByte(CodecType.NOT_NULL);

            // write array class name
            output.writeGBK(v.getClass().getComponentType().getName());
            // write length
            int length = Array.getLength(v);
            output.writeVInt(length);

            // write array value
            Object arrayValue = null;
            Codec codec = null;
            for (int i=0; i<length; i++) {
                arrayValue = Array.get(v, i);
                if (arrayValue == null) {
                    output.writeByte(CodecType.NULL);
                    continue;
                }
                output.writeByte(CodecType.NOT_NULL);
                codec = config.lookup(arrayValue);
                codec.encode(output, arrayValue, config);
            }
        } catch (Exception e) {
            throw new CodecException(e);
        }
    }

    public Object[] decode(DataInput input, CodecConfig config) throws CodecException
    {
        try {
            // read is null
            if (input.readByte() == CodecType.NULL) {
                return null;
            }

            // read array class name
            Class<?> arrayClass = ReflectUtil.getClass(input.readGBK());
            // read length
            int length = input.readVInt();

            // read array value
            Object array = Array.newInstance(arrayClass, length);
            Codec codec = null;
            for (int i=0; i<length; i++) {
                if (input.readByte() == CodecType.NOT_NULL) {
                    byte type = input.readByte();
                    codec = config.lookup(type);
                    Object arrayValue = codec.decode(input, config);
                    Array.set(array, i, arrayValue);
                }
            }
            return (Object[]) array;
        } catch (Exception e) {
            throw new CodecException(e);
        }
    }

}
