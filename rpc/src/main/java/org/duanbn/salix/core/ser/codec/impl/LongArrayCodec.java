package org.duanbn.salix.core.ser.codec.impl;

import org.duanbn.salix.core.io.DataInput;
import org.duanbn.salix.core.io.DataOutput;
import org.duanbn.salix.core.ser.codec.*;

public class LongArrayCodec implements Codec<long[]> {

	public void encode(DataOutput output, long[] v, CodecConfig config) {
		// write type
		output.writeByte(CodecType.TYPE_ARRAY_LONG);

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
			output.writeLong(v[i]);
		}
	}

	public long[] decode(DataInput input, CodecConfig config) {
		// read is null
		if (input.readByte() == CodecType.NULL) {
			return null;
		}

		// read length
		int length = input.readVInt();
		long[] array = new long[length];
		for (int i = 0; i < length; i++) {
			array[i] = input.readLong();
		}
		return array;
	}

}
