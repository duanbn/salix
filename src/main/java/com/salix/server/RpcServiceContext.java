package com.salix.server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.context.ApplicationContext;

import com.salix.beans.SalixServerAddressBean;
import com.salix.beans.SalixServiceBean;
import com.salix.constant.Const;
import com.salix.core.message.RpcMessage;
import com.salix.core.message.RpcServiceMessage;
import com.salix.core.ser.Deserializer;
import com.salix.core.ser.MyDeserializer;
import com.salix.core.ser.MySerializer;
import com.salix.core.ser.Serializer;
import com.salix.core.util.ZkUtil;
import com.salix.server.processor.IProcessor;
import com.salix.server.processor.RpcProcessor;
import com.salix.server.processor.RpcServiceProcessor;

public class RpcServiceContext {

	public static final Logger LOG = Logger.getLogger(RpcServiceContext.class);

	public static final Map<Class<?>, IProcessor> processorConfig = new HashMap<Class<?>, IProcessor>(1);

	private ApplicationContext springCtx;
	private String appName;
	private ZooKeeper zkClient;

	private Serializer ser;

	private int listenPort;

	public RpcServiceContext(String appName, String zkHost, ApplicationContext springCtx) {
		this.appName = appName;
		this.springCtx = springCtx;

		this.zkClient = ZkUtil.getZooKeeper(zkHost);

		this.ser = MySerializer.getInstance();
	}

	public void init() {
		IProcessor rpcProcessor = new RpcProcessor();
		rpcProcessor.setSpringCtx(springCtx);
		processorConfig.put(RpcMessage.class, rpcProcessor);
		LOG.info("load RpcProcessor done");

		IProcessor rpcServiceProcessor = new RpcServiceProcessor(this);
		processorConfig.put(RpcServiceMessage.class, rpcServiceProcessor);
		LOG.info("load RpcServiceProcessor done");

		_initMeta();
	}

	private void _initMeta() {
		try {
			Stat stat = this.zkClient.exists(Const.ZK_ROOT, false);
			if (stat == null) {
				this.zkClient.create(Const.ZK_ROOT, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}

			String salixApplicationNode = _getApplicationNode();
			stat = this.zkClient.exists(salixApplicationNode, false);
			if (stat == null) {
				this.zkClient.create(salixApplicationNode, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}
			String salixAliveNode = _getSalixAliveNode();
			stat = this.zkClient.exists(salixAliveNode, false);
			if (stat == null) {
				this.zkClient.create(salixAliveNode, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			String salixServicesNode = _getSalixServicesNode();
			stat = this.zkClient.exists(salixServicesNode, false);
			if (stat == null) {
				this.zkClient
						.create(salixServicesNode, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}

			// put this server's connect info to zookeeper
			String localIp = _getRealIp();
			String salixAliveServerNode = salixAliveNode + "/" + localIp + ":" + this.listenPort;
			stat = this.zkClient.exists(salixAliveServerNode, false);
			if (stat != null) {
				this.zkClient.delete(salixAliveServerNode, 0);
			}
			byte[] zkNodeData = ser.ser(new SalixServerAddressBean(localIp, this.listenPort));
			this.zkClient.create(salixAliveServerNode, zkNodeData, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

			// put services of this server to zookeeper
			List<String> toBeRemove = this.zkClient.getChildren(salixServicesNode, false);
			// load salix service
			String[] beanNames = this.springCtx.getBeanDefinitionNames();
			for (String beanName : beanNames) {
				if (!beanName.startsWith("org.springframework.") && !beanName.equals(appName)) {
					String salixServiceNode = salixServicesNode + "/" + beanName;
					Stat zkNodeStat = this.zkClient.exists(salixServiceNode, false);

					if (zkNodeStat == null) {
						_createServiceNode(beanName);

						if (LOG.isDebugEnabled()) {
							LOG.debug("add rpc service " + beanName);
						}
					} else if ((System.currentTimeMillis() - zkNodeStat.getCtime()) > Const.REFRESH) {
						this.zkClient.delete(_getSalixServiceNode(beanName), 0);
						_createServiceNode(beanName);

						if (LOG.isDebugEnabled()) {
							LOG.debug("refresh rpc service " + beanName);
						}
					}
					toBeRemove.remove(beanName);
				}
			}
			// remove old service
			for (String serviceName : toBeRemove) {
				this.zkClient.delete(salixServicesNode + "/" + serviceName, 0);
			}
		} catch (Exception e) {
			throw new RuntimeException("初始化salix失败", e);
		}
	}

	private void _createServiceNode(String serviceName) throws Exception {
		Object salixService = this.springCtx.getBean(serviceName);
		SalixServiceBean salixServiceBean = new SalixServiceBean(serviceName, _getServiceMethodText(salixService));
		byte[] zkNodeData = ser.ser(salixServiceBean);
		this.zkClient.create(_getSalixServiceNode(serviceName), zkNodeData, ZooDefs.Ids.OPEN_ACL_UNSAFE,
				CreateMode.PERSISTENT);
	}

	private String _getSalixServiceNode(String serviceName) {
		return Const.ZK_ROOT + "/" + this.appName + Const.ZK_SERVICES + "/" + serviceName;
	}

	private String _getSalixServicesNode() {
		return Const.ZK_ROOT + "/" + this.appName + Const.ZK_SERVICES;
	}

	private String _getSalixAliveNode() {
		return Const.ZK_ROOT + "/" + this.appName + Const.ZK_LIVE_NODES;
	}

	private String _getApplicationNode() {
		return Const.ZK_ROOT + "/" + this.appName;
	}

	private List<String> _getServiceMethodText(Object serviceObj) {
		List<String> list = new ArrayList<String>();
		// TODO: to be coding.
		return list;
	}

	public String _getRealIp() {
		String localip = null;// 本地IP，如果没有配置外网IP则返回它
		String netip = null;// 外网IP

		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}

		InetAddress ip = null;
		boolean finded = false;// 是否找到外网IP
		while (netInterfaces.hasMoreElements() && !finded) {
			NetworkInterface ni = netInterfaces.nextElement();
			Enumeration<InetAddress> address = ni.getInetAddresses();
			while (address.hasMoreElements()) {
				ip = address.nextElement();
				if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 外网IP
					netip = ip.getHostAddress();
					finded = true;
					break;
				} else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 内网IP
					localip = ip.getHostAddress();
				}
			}
		}

		if (netip != null && !"".equals(netip)) {
			return netip;
		} else {
			return localip;
		}
	}

	public void destroy() {
		try {
			this.zkClient.close();
		} catch (InterruptedException e) {
		}
	}

	public IProcessor get(Class<?> msgClazz) {
		return processorConfig.get(msgClazz);
	}

	public int getListenPort() {
		return listenPort;
	}

	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}

}
