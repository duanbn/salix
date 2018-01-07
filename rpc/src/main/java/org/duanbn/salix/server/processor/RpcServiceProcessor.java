package org.duanbn.salix.server.processor;

import org.duanbn.salix.core.message.Message;
import org.duanbn.salix.server.RpcServiceContext;

public class RpcServiceProcessor extends AbstractProcessor {

	private RpcServiceContext rsc;

	public RpcServiceProcessor(RpcServiceContext rsc) {
		this.rsc = rsc;
	}

	public Message process(Message in) throws Throwable {
        // FIXME: have not implements
		throw new UnsupportedOperationException();
	}

}
