package org.duanbn.salix.core.ser;

import org.duanbn.salix.core.io.*;
import org.duanbn.salix.core.ser.codec.*;
import org.duanbn.salix.core.util.GzipCompressUtil;
import org.duanbn.salix.exception.CodecException;

/**
 * 反序列化工具类，解析一个二进制的字节数组转化成一个Java对象. 读取编码标志位确定转化为那个对象.
 * 
 * @author duanbn
 * @see CodecConfig
 */
public class MyDeserializer implements Deserializer {
	private static final ThreadLocal<DataInput> inputRef = new ThreadLocal<DataInput>();

	private static MyDeserializer instance;

	private CodecConfig config;

	private MyDeserializer() {
		this.config = CodecConfig.load();
	}

	public static MyDeserializer getInstance() {
		if (instance == null) {
			synchronized (MyDeserializer.class) {
				if (instance == null) {
					instance = new MyDeserializer();
				}
			}
		}

		return instance;
	}

	public Object deser(byte[] b, boolean isCompress) throws DeserializeException {
		if (b == null || b.length == 0) {
			throw new IllegalArgumentException("b=null");
		}

		try {
			// memory leak
			/*
			 * DataInput input = _getInput(); if (isCompress) {
			 * input.setDataBuffer(GzipCompressUtil.uncompress(b)); } else {
			 * input.setDataBuffer(b); }
			 */
			DataInput input = null;
			if (isCompress) {
				input = new ByteBufferInput(GzipCompressUtil.uncompress(b));
			} else {
				input = new ByteBufferInput(b);
			}

			byte type = input.readByte();
			Codec codec = config.lookup(type);

			return codec.decode(input, config);
		} catch (Exception e) {
			throw new DeserializeException(e);
		}
	}

	public <T> T deser(byte[] b, boolean isCompress, Class<T> T) throws DeserializeException {
		return (T) deser(b, isCompress);
	}

	public <T> T deser(byte[] b, Class<T> T) throws DeserializeException {
		return deser(b, false, T);
	}

	private DataInput _getInput() {
		if (inputRef.get() == null) {
			inputRef.set(new ByteBufferInput());
		}

		return inputRef.get();
	}
}
