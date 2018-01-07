package org.duanbn.salix.core.ser.codec.impl;

import java.sql.Timestamp;

import org.duanbn.salix.core.io.DataInput;
import org.duanbn.salix.core.io.DataOutput;
import org.duanbn.salix.core.ser.codec.Codec;
import org.duanbn.salix.core.ser.codec.CodecConfig;
import org.duanbn.salix.core.ser.codec.CodecType;
import org.duanbn.salix.exception.CodecException;

/**
 * 
 * @author duanbn
 * 
 */
public class TimestampArrayCodec implements Codec<Timestamp[]> {

	public void encode(DataOutput output, Timestamp[] v, CodecConfig config) throws CodecException {
		// write type
		output.writeByte(CodecType.TYPE_ARRAY_TIMESTAMP);

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

	public Timestamp[] decode(DataInput input, CodecConfig config) throws CodecException {
		// read is null
		if (input.readByte() == CodecType.NULL) {
			return null;
		}

		// read length
		int length = input.readVInt();
		Timestamp[] array = new Timestamp[length];
		for (int i = 0; i < length; i++) {
			array[i] = new Timestamp(input.readVLong());
		}
		return array;
	}

}
