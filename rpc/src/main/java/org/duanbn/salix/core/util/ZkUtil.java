package org.duanbn.salix.core.util;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;

public class ZkUtil {

	public static final int DEFAULT_SESSION_TIMEOUT = 30 * 1000;

	public static ZooKeeper getZooKeeper(String zkUrl) {
		return getZooKeeper(zkUrl, DEFAULT_SESSION_TIMEOUT);
	}

	public static ZooKeeper getZooKeeper(String zkUrl, int sessionTimeout) {
		ZkConnector zkConnector = new ZkConnector(zkUrl, sessionTimeout);
		return zkConnector.getZooKeeper();
	}

	private static class ZkConnector implements Watcher {
		private CountDownLatch connectedLatch = new CountDownLatch(1);

		private String zkUrl;
		private int sessionTimeout;

		public ZkConnector(String zkUrl, int sessionTimeout) {
			this.zkUrl = zkUrl;
			this.sessionTimeout = sessionTimeout;
		}

		public ZooKeeper getZooKeeper() {
			// 创建zookeeper连接
			try {
				ZooKeeper zk = new ZooKeeper(zkUrl, sessionTimeout, this);
				if (States.CONNECTING == zk.getState()) {
					connectedLatch.await();
				}

				return zk;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public void process(WatchedEvent event) {
			if (event.getState() == KeeperState.SyncConnected) {
				connectedLatch.countDown();
			}
		}
	}

}
