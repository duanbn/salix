package com.salix.client;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.salix.client.connection.ConnectionPool;
import com.salix.client.connection.MyConnectionPool;
import com.salix.exception.InterfaceNotFoundException;

public class SalixFactoryBean<T> implements FactoryBean<T>, InitializingBean, DisposableBean {

	private String id;

	private ConnectionPool cpool;

	private String host;

	private int port;

	private String serviceName;

	private String interfaceClass;

	@SuppressWarnings("unchecked")
	public T getObject() throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<?> clazz = Class.forName(interfaceClass);
		RpcInvocationHandler rpcInvokeHandler = new RpcInvocationHandler(serviceName, this.cpool);
		return (T) Proxy.newProxyInstance(cl, new Class<?>[] { clazz }, rpcInvokeHandler);
	}

	public Class<?> getObjectType() {
		try {
			return Class.forName(interfaceClass);
		} catch (ClassNotFoundException e) {
			throw new InterfaceNotFoundException(this.interfaceClass);
		}
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		this.cpool = new MyConnectionPool(host, port);
		this.cpool.startup();
	}

	public void destroy() throws Exception {
		this.cpool.shutdown();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getInterfaceClass() {
		return interfaceClass;
	}

	public void setInterfaceClass(String interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

}
