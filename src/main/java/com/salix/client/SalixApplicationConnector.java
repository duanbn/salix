package com.salix.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.salix.beans.SalixServerAddressBean;
import com.salix.client.connection.ConnectionPool;
import com.salix.client.connection.SimpleConnectionPool;
import com.salix.constant.Const;
import com.salix.core.ser.Deserializer;
import com.salix.core.ser.MyDeserializer;
import com.salix.exception.NoAvailableServerException;

/**
 * one application connector.
 *
 * @author duanbn
 */
public class SalixApplicationConnector implements Watcher {

	public static final Logger LOG = Logger.getLogger(SalixApplicationConnector.class);

	/**
	 * connection application name.
	 */
	private String appName;
	/**
	 * connection service name.
	 */
	private List<String> serviceNames;
	/**
	 * available connection pool.
	 */
	private Map<String, ConnectionPool> connPools;

	private Deserializer deser;

	private Random r = new Random();

	private ZooKeeper zkClient;

	private Map<String, SalixApplicationConnector> appConnectorMap;

	public SalixApplicationConnector(String appName, ZooKeeper zkClient) {
		this.appName = appName;
		this.zkClient = zkClient;

		this.connPools = new HashMap<String, ConnectionPool>();

		this.deser = MyDeserializer.getInstance();
	}

	/**
	 * init.
	 */
	public void init() throws Exception {
		String zkAliveNodePath = _getZkAliveNodePath();

		// init connection pool
		List<String> aliveAddresses = this.zkClient.getChildren(zkAliveNodePath, this);
		ConnectionPool connPool = null;
		for (String aliveAddress : aliveAddresses) {
			connPool = _createConnectionPool(aliveAddress);

			this.connPools.put(aliveAddress, connPool);
		}

		// load rpc service name
		_loadService();
	}

	/**
	 * destroy.
	 */
	public void destroy() throws Exception {
		for (ConnectionPool cp : this.connPools.values()) {
			cp.shutdown();
		}
	}

	/**
	 * select a available connection pool.
	 */
	public ConnectionPool select() throws NoAvailableServerException {
		if (this.connPools.isEmpty()) {
			throw new NoAvailableServerException("get connection fail no available server");
		}

		Collection<ConnectionPool> cps = this.connPools.values();
		Iterator<ConnectionPool> cpIt = cps.iterator();
		if (cps.size() > 1) {
			int index = r.nextInt(cps.size());
			for (int i = 0; i < index; i++) {
				cpIt.next();
			}
		}
		return cpIt.next();
	}

	public void process(WatchedEvent event) {
		String zkAliveNodePath = _getZkAliveNodePath();
		String zkServiceNamesPath = _getZkServiceNamesPath();

		// update connection pool
		if (event.getPath().equals(zkAliveNodePath)) {
			synchronized (this.connPools) {
				List<String> aliveAddresses = null;
				try {
					aliveAddresses = this.zkClient.getChildren(zkAliveNodePath, this);
				} catch (Exception e) {
					LOG.warn(e);
				}

				Set<String> deadAddreses = new HashSet<String>(this.connPools.keySet());
				if (aliveAddresses != null) {
					for (String aliveAddress : aliveAddresses) {
						if (!this.connPools.containsKey(aliveAddress)) {
							ConnectionPool connPool = null;
							try {
								connPool = _createConnectionPool(aliveAddress);
								this.connPools.put(aliveAddress, connPool);
							} catch (Exception e) {
								LOG.warn("create connection failure, " + e.getMessage());
							}
						} else {
							deadAddreses.remove(aliveAddress);
						}
					}
				}

				// clean dead connection
				for (String deadAddress : deadAddreses) {
					this.connPools.get(deadAddress).shutdown();
					this.connPools.remove(deadAddress);
				}
			}
		}

		// update service names
		if (event.getPath().equals(zkServiceNamesPath)) {
			try {
				_loadService();
			} catch (Exception e) {
				LOG.warn(e);
			}
		}
	}

	/**
	 * load first service if there are mutilple same name service.
	 * 
	 * @throws Exception
	 */
	private void _loadService() throws Exception {
		String zkServiceNamesPath = _getZkServiceNamesPath();

		this.serviceNames = this.zkClient.getChildren(zkServiceNamesPath, this);

		if (serviceNames != null) {
			for (String serviceName : serviceNames) {
				if (!this.appConnectorMap.containsKey(serviceName))
					this.appConnectorMap.put(serviceName, this);
				else
					LOG.warn(serviceName + " has exist, ignore load");
			}
		}
	}

	private String _getZkAliveNodePath() {
		return Const.ZK_ROOT + "/" + appName + Const.ZK_LIVE_NODES;
	}

	private String _getZkServiceNamesPath() {
		return Const.ZK_ROOT + "/" + appName + Const.ZK_SERVICES;
	}

	private ConnectionPool _createConnectionPool(String aliveAddress) throws Exception {
		String zkAliveNodePath = _getZkAliveNodePath();

		byte[] zkNodeData = this.zkClient.getData(zkAliveNodePath + "/" + aliveAddress, false, null);

		SalixServerAddressBean serverAddressBean = this.deser.deser(zkNodeData, SalixServerAddressBean.class);

		// init connection pool
		ConnectionPool connPool = new SimpleConnectionPool(serverAddressBean.getIp(), serverAddressBean.getPort());
		connPool.startup();

		return connPool;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public List<String> getServiceNames() {
		return this.serviceNames;
	}

	public Map<String, SalixApplicationConnector> getAppConnectorMap() {
		return appConnectorMap;
	}

	public void setAppConnectorMap(Map<String, SalixApplicationConnector> appConnectorMap) {
		this.appConnectorMap = appConnectorMap;
	}
}
