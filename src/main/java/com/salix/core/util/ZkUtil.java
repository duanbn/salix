package com.salix.core.util;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper.States;

public class ZkUtil implements Watcher {

	private CountDownLatch connectedLatch = new CountDownLatch(1);
	private int sessionTimeout = 30000;

	public ZooKeeper getZooKeeper(String zkUrl) {
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
