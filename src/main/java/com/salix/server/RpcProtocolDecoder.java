package com.salix.server;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class RpcProtocolDecoder implements ProtocolDecoder {

	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		int pkgLenth = in.getInt();

		IoBuffer pkg = IoBuffer.allocate(pkgLenth);
		while (pkg.hasRemaining()) {
			pkg.put(in);
		}

		out.write(pkg.array());
	}

	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
	}

	public void dispose(IoSession session) throws Exception {
	}
}
