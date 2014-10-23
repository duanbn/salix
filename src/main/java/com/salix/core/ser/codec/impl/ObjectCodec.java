package com.salix.core.ser.codec.impl;

import java.lang.reflect.Field;

import com.salix.core.io.DataInput;
import com.salix.core.io.DataOutput;
import com.salix.core.ser.codec.Codec;
import com.salix.core.ser.codec.CodecConfig;
import com.salix.core.ser.codec.CodecType;
import com.salix.core.util.ReflectUtil;
import com.salix.exception.CodecException;

/**
 * 这个类只能序列化自定义对象，不能将基本类型当作对象来进行序列化，否则会发生错误. 基本类型的序列化使用相关的编码类.
 *
 * @author duanbn
 * @since 1.0
 */
public class ObjectCodec implements Codec<Object> {

	public void encode(DataOutput output, Object v, CodecConfig config) throws CodecException {
		try {
			output.writeByte(CodecType.TYPE_OBJECT); // write type

			if (v == null) {
				output.writeByte(CodecType.NULL); // write isnull
				return;
			}
			output.writeByte(CodecType.NOT_NULL); // write is not null

			Class<?> oc = v.getClass();
			output.writeUTF8(oc.getName()); // write classname

			Object fvalue = null;
			Codec codec = null;

			for (Field f : ReflectUtil.getFields(oc)) { // write field
				if (!f.isAccessible()) {
					f.setAccessible(true);
				}

				// 基本类型序列化
				if (f.getType() == Boolean.TYPE) {
					output.writeByte(CodecType.TYPE_OBOOLEAN);
					output.writeBoolean(f.getBoolean(v));
					continue;
				} else if (f.getType() == Byte.TYPE) {
					output.writeByte(CodecType.TYPE_OBYTE);
					output.writeByte(f.getByte(v));
					continue;
				} else if (f.getType() == Character.TYPE) {
					output.writeByte(CodecType.TYPE_OCHAR);
					output.writeChar(f.getChar(v));
					continue;
				} else if (f.getType() == Short.TYPE) {
					output.writeByte(CodecType.TYPE_OSHORT);
					output.writeShort(f.getShort(v));
					continue;
				} else if (f.getType() == Integer.TYPE) {
					output.writeByte(CodecType.TYPE_OINT);
					output.writeInt(f.getInt(v));
					continue;
				} else if (f.getType() == Long.TYPE) {
					output.writeByte(CodecType.TYPE_OLONG);
					output.writeLong(f.getLong(v));
					continue;
				} else if (f.getType() == Float.TYPE) {
					output.writeByte(CodecType.TYPE_OFLOAT);
					output.writeFloat(f.getFloat(v));
					continue;
				} else if (f.getType() == Double.TYPE) {
					output.writeByte(CodecType.TYPE_ODOUBLE);
					output.writeDouble(f.getDouble(v));
					continue;
				}

				fvalue = f.get(v);
				if (fvalue == null) { // write field isnull
					output.writeByte(CodecType.NULL);
					continue;
				}
				output.writeByte(CodecType.NOT_NULL);

				codec = config.lookup(fvalue);
				codec.encode(output, fvalue, config);
			}

		} catch (IllegalAccessException e) {
			throw new CodecException(e);
		}
	}

	public Object decode(DataInput input, CodecConfig config) throws CodecException {
		try {
			if (input.readByte() == CodecType.NULL) { // read isnull
				return null;
			}

			Class<?> oc = ReflectUtil.getClass(input.readUTF8()); // read
																	// classname
			Object instance = oc.newInstance();

			Object fvalue = null;
			Codec codec = null;
			for (Field f : ReflectUtil.getFields(oc)) { // read field
				if (!f.isAccessible()) {
					f.setAccessible(true);
				}

				byte type = input.readByte();

				// 基本类型反序列化
				if (type == CodecType.TYPE_OBOOLEAN) {
					f.setBoolean(instance, input.readBoolean());
					continue;
				} else if (type == CodecType.TYPE_OBYTE) {
					f.setByte(instance, input.readByte());
					continue;
				} else if (type == CodecType.TYPE_OCHAR) {
					f.setChar(instance, input.readChar());
					continue;
				} else if (type == CodecType.TYPE_OINT) {
					f.setInt(instance, input.readInt());
					continue;
				} else if (type == CodecType.TYPE_OSHORT) {
					f.setShort(instance, input.readShort());
					continue;
				} else if (type == CodecType.TYPE_OLONG) {
					f.setLong(instance, input.readLong());
					continue;
				} else if (type == CodecType.TYPE_OFLOAT) {
					f.setFloat(instance, input.readFloat());
					continue;
				} else if (type == CodecType.TYPE_ODOUBLE) {
					f.setDouble(instance, input.readDouble());
					continue;
				}

				if (type == CodecType.NULL) {
					f.set(instance, null);
					continue;
				}
				type = input.readByte();

				codec = config.lookup(type);
				fvalue = codec.decode(input, config);

				f.set(instance, fvalue);
			}
			return instance;
		} catch (Exception e) {
			throw new CodecException(e);
		}
	}

}
