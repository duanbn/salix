package org.duanbn.salix.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.log4j.Logger;
import org.duanbn.salix.client.connection.*;
import org.duanbn.salix.core.message.RpcMessage;
import org.duanbn.salix.exception.NoAvailableServerException;

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

		Object returnVal = null;
		RpcMessage msg = new RpcMessage();
		msg.setServiceName(serviceName);
		msg.setMethodName(method.getName());
		msg.setParamTypes(method.getParameterTypes());
		msg.setArgs(args);

		ConnectionPool cp = null;
		Connection conn = null;
		int retry = 3;
		while (retry-- > 0) {
			try {
				cp = appConnector.select();
				conn = cp.getConnection();
				returnVal = conn.send(msg).getBody();
				conn.close();
				break;
			} catch (NoAvailableServerException e) {
				throw e;
			} catch (Exception e) {
				if (conn != null) {
					((CpConnection) conn).closeChannel();
				}

				if (!cp.ensureServerAlive())
					appConnector.deadServer(cp.getAddress());

				if (LOG.isDebugEnabled())
					LOG.debug(e.getMessage() + " test reconnecting..", e);
			}
		}

		if (returnVal != null) {
			if (returnVal instanceof Throwable) {
				throw (Throwable) returnVal;
			}
			return returnVal;
		}

		return null;
	}

}
