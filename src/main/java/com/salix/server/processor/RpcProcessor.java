package com.salix.server.processor;

import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;

import com.salix.core.message.Message;
import com.salix.core.message.RpcMessage;

public class RpcProcessor implements IProcessor {

	private ApplicationContext springCtx;

	public Message process(Message in) throws Throwable {

		RpcMessage msg = (RpcMessage) in;
		Message out = new Message();

		String serviceName = msg.getServiceName();
		String methodName = msg.getMethodName();

		Object[] args = msg.getArgs();

		Object service = this.springCtx.getBean(serviceName);

		try {
			Method m = service.getClass().getMethod(methodName, msg.getParamTypes());
			Object returnVal = m.invoke(service, args);
			out.setBody(returnVal);
		} catch (Exception e) {
			throw e;
		}

		return out;
	}

	public void setSpringCtx(ApplicationContext springCtx) {
		this.springCtx = springCtx;
	}
}
