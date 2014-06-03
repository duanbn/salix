package com.salix.core.ser.codec.impl;

import java.util.Calendar;

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
public class CalendarArrayCodec implements Codec<Calendar[]> {

	public void encode(DataOutput output, Calendar[] v, CodecConfig config) throws CodecException {
		// write type
		output.writeByte(CodecType.TYPE_ARRAY_CALENDER);

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
			output.writeVLong(v[i].getTimeInMillis());
		}
	}

	public Calendar[] decode(DataInput input, CodecConfig config) throws CodecException {
		// read is null
		if (input.readByte() == CodecType.NULL) {
			return null;
		}

		// read length
		int length = input.readVInt();
		Calendar[] array = new Calendar[length];
		Calendar cal = null;
		for (int i = 0; i < length; i++) {
			cal = Calendar.getInstance();
			cal.setTimeInMillis(input.readVLong());
			array[i] = cal;
		}
		return array;
	}

}
