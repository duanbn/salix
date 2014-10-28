package com.salix.server.processor;

import com.salix.core.message.Message;
import com.salix.server.RpcServiceContext;

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
