package com.salix.core.ser.codec.impl;

import java.sql.Timestamp;

import com.salix.core.io.DataInput;
import com.salix.core.io.DataOutput;
import com.salix.core.ser.codec.Codec;
import com.salix.core.ser.codec.CodecConfig;
import com.salix.core.ser.codec.CodecType;
import com.salix.exception.CodecException;

public class TimestampCodec implements Codec<Timestamp> {

	public void encode(DataOutput output, Timestamp v, CodecConfig config) throws CodecException {
		output.writeByte(CodecType.TYPE_TIMESTAMP);

		if (v == null) {
			output.writeByte(CodecType.NULL);
		} else {
			output.writeByte(CodecType.NOT_NULL);
			output.writeVLong(v.getTime());
		}
	}

	public Timestamp decode(DataInput input, CodecConfig config) throws CodecException {
		byte isNull = input.readByte();
		if (isNull == CodecType.NULL) {
			return null;
		}
		return new Timestamp(input.readVLong());
	}

}
