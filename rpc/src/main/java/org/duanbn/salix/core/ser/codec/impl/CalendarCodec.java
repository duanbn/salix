package org.duanbn.salix.core.ser.codec.impl;

import java.util.Calendar;

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
public class CalendarCodec implements Codec<Calendar> {

	public void encode(DataOutput output, Calendar v, CodecConfig config) throws CodecException {
		output.writeByte(CodecType.TYPE_CALENDER);

		if (v == null) {
			output.writeByte(CodecType.NULL);
		} else {
			output.writeByte(CodecType.NOT_NULL);
			output.writeVLong(v.getTimeInMillis());
		}
	}

	public Calendar decode(DataInput input, CodecConfig config) throws CodecException {
		byte isNull = input.readByte();
		if (isNull == CodecType.NULL) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(input.readVLong());
		return cal;
	}

}
