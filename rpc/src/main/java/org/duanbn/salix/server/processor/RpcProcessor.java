package org.duanbn.salix.server.processor;

import java.util.*;

import org.duanbn.salix.core.message.Message;
import org.duanbn.salix.core.message.RpcMessage;

import java.lang.reflect.Method;

public class RpcProcessor extends AbstractProcessor {

	private static final Map<String, Method> methodCache = new HashMap<String, Method>();

	public Message process(Message in) throws Throwable {

		RpcMessage msg = (RpcMessage) in;
		Message out = new Message();

		String serviceName = msg.getServiceName();
		String methodName = msg.getMethodName();

		Object[] args = msg.getArgs();

		Object service = this.springCtx.getBean(serviceName);

		try {
			Method m = methodCache.get(msg.getMethodInfo());
			if (m == null) {
				m = service.getClass().getMethod(methodName, msg.getParamTypes());
				methodCache.put(msg.getMethodInfo(), m);
			}

			Object returnVal = m.invoke(service, args);

			out.setBody(returnVal);
		} catch (Exception e) {
			throw e;
		}

		return out;
	}

}
