package org.duanbn.salix.core.ser.codec.impl;


import java.sql.Date;

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
public class SqlDateCodec implements Codec<Date> {

	public void encode(DataOutput output, Date v, CodecConfig config) throws CodecException {
		output.writeByte(CodecType.TYPE_SQLDATE);

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
