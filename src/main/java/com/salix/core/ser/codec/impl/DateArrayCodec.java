package com.salix.core.ser.codec.impl;

import java.util.Date;

import com.salix.core.io.DataInput;
import com.salix.core.io.DataOutput;
import com.salix.core.ser.codec.Codec;
import com.salix.core.ser.codec.CodecConfig;
import com.salix.core.ser.codec.CodecType;
import com.salix.exception.CodecException;

/**
 * 
 * @author duanbn
 *
 */
public class DateArrayCodec implements Codec<Date[]> {

	public void encode(DataOutput output, Date[] v, CodecConfig config) throws CodecException {
		// write type
		output.writeByte(CodecType.TYPE_ARRAY_DATE);

		// write is null
		if (v == null) {
			output.writeByte(CodecType.NULL);
			return;
		}
		output.writeByte(CodecType.NOT_NULL);

		// write length
		int length = v.length;
		output.writeVInt(length);

		// write value
		for (int i = 0; i < length; i++) {
			output.writeVLong(v[i].getTime());
		}
	}

	public Date[] decode(DataInput input, CodecConfig config) throws CodecException {
		// read is null
		if (input.readByte() == CodecType.NULL) {
			return null;
		}

		// read length
		int length = input.readVInt();
		Date[] array = new Date[length];
		for (int i = 0; i < length; i++) {
			array[i] = new Date(input.readVLong());
		}
		return array;
	}

}
