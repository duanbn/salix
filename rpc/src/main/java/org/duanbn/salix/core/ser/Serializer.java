package org.duanbn.salix.core.ser;

public interface Serializer
{

    byte[] ser(Object v, boolean isCompress) throws SerializeException;

    byte[] ser(Object v) throws SerializeException;

}
