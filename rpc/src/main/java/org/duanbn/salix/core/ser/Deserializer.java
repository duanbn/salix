package org.duanbn.salix.core.ser;

public interface Deserializer {

	Object deser(byte[] b, boolean isCompress) throws DeserializeException;

	<T> T deser(byte[] b, boolean isCompress, Class<T> T) throws DeserializeException;

	<T> T deser(byte[] b, Class<T> T) throws DeserializeException;

}
