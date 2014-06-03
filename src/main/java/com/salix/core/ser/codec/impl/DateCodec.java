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
public class DateCodec implements Codec<Date> {

	public void encode(DataOutput output, Date v, CodecConfig config) throws CodecException {
		output.writeByte(CodecType.TYPE_DATE);

		if (v == null) {
			output.writeByte(CodecType.NULL);
		} else {
			output.writeByte(CodecType.NOT_NULL);
			output.writeVLong(v.getTime());
		}
	}

	public Date decode(DataInput input, CodecConfig config) throws CodecException {
		byte isNull = input.readByte();
		if (isNull == CodecType.NULL) {
			return null;
		}
		return new Date(input.readVLong());
	}

}
