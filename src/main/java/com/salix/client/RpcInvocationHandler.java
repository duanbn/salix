package com.salix.client;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.log4j.Logger;

import com.salix.client.connection.Connection;
import com.salix.core.message.RpcMessage;
import com.salix.exception.NoAvailableServerException;

/**
 * 客户端动态代理执行类.
 * 
 * @author duanbn
 * @since 1.0
 */
public class RpcInvocationHandler implements InvocationHandler {

	public static final Logger LOG = Logger.getLogger(RpcInvocationHandler.class);

	private String serviceName;
	private Map<String, SalixApplicationConnector> appConnectorMap;

	public RpcInvocationHandler(String serviceName, Map<String, SalixApplicationConnector> appConnectorMap) {
		this.serviceName = serviceName;
		this.appConnectorMap = appConnectorMap;
	}

	/**
	 * 执行调用方法.
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		// select Application Connector by service name.
		SalixApplicationConnector appConnector = this.appConnectorMap.get(serviceName);
		if (appConnector == null) {
			throw new RuntimeException("can not find service " + serviceName);
		}

		Connection conn = null;
		Object returnVal = null;
		try {
			RpcMessage msg = new RpcMessage();
			msg.setServiceName(serviceName);
			msg.setMethodName(method.getName());
			msg.setParamTypes(method.getParameterTypes());
			msg.setArgs(args);

			while (true) {
				try {
					// 从连接池中获取一个连接
					conn = appConnector.select().getConnection();
					conn.send(msg);
					returnVal = conn.receive().getBody();
					break;
				} catch (Exception e) {
					if (e instanceof IOException || e instanceof NoAvailableServerException) {
						try {
							Thread.sleep(500);
							conn = appConnector.select().getConnection();
						} catch (Exception ex) {
							if (e instanceof IOException || e instanceof NoAvailableServerException) {
								LOG.warn(e.getMessage() + " test reconnecting..");
							}
						}
					}
				}
			}

			if (returnVal != null) {
				if (returnVal instanceof Throwable) {
					throw (Throwable) returnVal;
				}
				return returnVal;
			}

			return null;
		} finally {
			if (conn != null)
				conn.close();
		}
	}
}
