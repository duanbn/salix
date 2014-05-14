package com.salix.client;

import java.lang.reflect.Proxy;

import com.salix.client.connection.ConnectionPool;
import com.salix.client.connection.MyConnectionPool;

public class SalixFactoryBean {

	private ConnectionPool cpool;

	private String host;

	private int port;

	public Object create(String serviceName, String interfaceClass) throws ClassNotFoundException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> clazz = Class.forName(interfaceClass);
		RpcInvocationHandler rpcInvokeHandler = new RpcInvocationHandler(serviceName, this.cpool);
		return Proxy.newProxyInstance(cl, new Class<?>[] { clazz }, rpcInvokeHandler);
	}

	public void startup() {
		this.cpool = new MyConnectionPool(host, port);
		this.cpool.startup();
	}

	public void shutdown() {
		this.cpool.shutdown();
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
