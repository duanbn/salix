package com.salix.server;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class RpcProtocolDecoder implements ProtocolDecoder {

	private static final String SESS_ATTR_IOBUFFER = "sess_attr_iobuffer";

	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {

		IoBuffer pkg = (IoBuffer) session.getAttribute(SESS_ATTR_IOBUFFER);
		if (pkg == null) {
			int pkgLenth = in.getInt();
			pkg = IoBuffer.allocate(pkgLenth);
			pkg.put(in);
			if (pkg.hasRemaining()) {
				session.setAttribute(SESS_ATTR_IOBUFFER, pkg);
			}
		} else {
			pkg.put(in);
		}
		if (!pkg.hasRemaining()) {
			session.removeAttribute(SESS_ATTR_IOBUFFER);
			out.write(pkg.array());
		}
		
	}

	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
	}

	public void dispose(IoSession session) throws Exception {
	}
}
