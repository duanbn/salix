package org.duanbn.salix.core.ser.codec.impl;

import java.sql.Timestamp;

import org.duanbn.salix.core.io.DataInput;
import org.duanbn.salix.core.io.DataOutput;
import org.duanbn.salix.core.ser.codec.Codec;
import org.duanbn.salix.core.ser.codec.CodecConfig;
import org.duanbn.salix.core.ser.codec.CodecType;
import org.duanbn.salix.exception.CodecException;

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
