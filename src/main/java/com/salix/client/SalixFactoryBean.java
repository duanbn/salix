package com.salix.client;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.salix.constant.Const;
import com.salix.core.util.ZkUtil;
import com.salix.exception.InterfaceNotFoundException;

/**
 * rpc service stub factory bean.
 *
 * @author duanbn
 */
public class SalixFactoryBean<T> implements FactoryBean<T>, InitializingBean, DisposableBean, Watcher {

	private static final Logger LOG = Logger.getLogger(SalixFactoryBean.class);

	private String serviceName;

	private String interfaceClass;

	private int proxyType = 1;

	private String zkUrl;

	private ZooKeeper zkClient;

	/**
	 * keep serviceName - appConnector mapping. key : service name, value :
	 * application connector.
	 */
	private Map<String, SalixApplicationConnector> appConnectorMap = new HashMap<String, SalixApplicationConnector>();

	@SuppressWarnings("unchecked")
	public T getObject() throws Exception {

		RpcInvocationHandler rpcInvokeHandler = new RpcInvocationHandler(serviceName, appConnectorMap);

		T instance = null;

		if (proxyType == 1) {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Class<?> clazz = Class.forName(interfaceClass);

			instance = (T) Proxy.newProxyInstance(cl, new Class<?>[] { clazz }, rpcInvokeHandler);
		} else if (proxyType == 2) {
			instance = (T) JavassistProxy.newProxyInstance(interfaceClass, rpcInvokeHandler);
		}

		return instance;
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
		this.zkClient = ZkUtil.getZooKeeper(this.zkUrl, 2 * 1000);

		List<String> appNames = this.zkClient.getChildren(Const.ZK_ROOT, this);

		for (String appName : appNames) {
			SalixApplicationConnector appConnector = new SalixApplicationConnector(appName, this.zkClient);
			appConnector.setAppConnectorMap(this.appConnectorMap);
			appConnector.init();
		}
	}

	public void destroy() throws Exception {
		for (SalixApplicationConnector appConnector : this.appConnectorMap.values()) {
			appConnector.destroy();
		}

		this.zkClient.close();
	}

	public void process(WatchedEvent event) {
		if (event.getPath().equals(Const.ZK_ROOT)) {
			try {
				List<String> appNames = this.zkClient.getChildren(Const.ZK_ROOT, false);

				for (String appName : appNames) {
					if (!this.appConnectorMap.containsKey(appName)) {
						SalixApplicationConnector appConnector = new SalixApplicationConnector(appName, this.zkClient);
						appConnector.setAppConnectorMap(this.appConnectorMap);
						appConnector.init();
					}
				}
			} catch (Exception e) {
				LOG.warn(e);
			}
		}
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public int getProxyType() {
		return proxyType;
	}

	public void setProxyType(int proxyType) {
		this.proxyType = proxyType;
	}

	public String getInterfaceClass() {
		return interfaceClass;
	}

	public void setInterfaceClass(String interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	public String getZkUrl() {
		return zkUrl;
	}

	public void setZkUrl(String zkUrl) {
		this.zkUrl = zkUrl;
	}
}
