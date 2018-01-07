package org.duanbn.salix.client.connection;

import java.net.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.*;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

/**
 * 默认的连接池实现.
 * 
 * @author duanbn
 * @since 1.1
 * 
 * @see ConnectionPoolConfig
 */
public class SimpleConnectionPool extends AbstractLifecycle implements ConnectionPool {
	public static final Logger LOG = Logger.getLogger(SimpleConnectionPool.class);

	public static final int DEFAULT_MIN_CONNECT_NUM = 1;
	public static final int DEFAULT_MAX_CONNECT_NUM = 3;
	private int minConnectNum = DEFAULT_MIN_CONNECT_NUM;
	private int maxConnectNum = DEFAULT_MAX_CONNECT_NUM;

	private int cleanPeriod = 20 * 1000;

	private Map<String, CpConnection> pool;

	private Semaphore se;

	private String host;
	private int port;

	/**
	 * 清理多余连接，间隔1分钟执行.
	 */
	private CheckThread checker;

	public SimpleConnectionPool(String host, int port) {
		super();
		pool = new HashMap<String, CpConnection>();

		this.host = host;
		this.port = port;
	}

	@Override
	public void doInit() {
		se = new Semaphore(maxConnectNum, true);
	}

	@Override
	public void doStartup() {
		try {
			CpConnection conn = null;
			while (this.pool.size() < minConnectNum) {
				conn = createConnection();
				this.pool.put(conn.getLocalAddress(), conn);
			}
		} catch (IOException e) {
			throw new IllegalStateException("网络连接错误，初始化连接池失败:" + e.getMessage());
		}

		if (this.pool.size() != minConnectNum) {
			throw new IllegalStateException("网络连接错误，初始化连接池失败");
		}

		// 停止清理线程
		if (checker != null) {
			synchronized (checker) {
				checker.interrupt();
			}
		}

		// 启动清理线程.
		checker = new CheckThread();
		checker.start();
	}

	/**
	 * 创建连接.当由于网络或者服务器原因创建失败则重试3次，间隔1秒.
	 * 
	 * @return 新连接.
	 * @throws IOException
	 *             连接异常
	 */
	private CpConnection createConnection() throws IOException {
		int retry = 3;
		while (retry-- > 0) {
			try {
				CpConnection conn = new CpConnection(this.host, this.port);
				conn.setCp(this);
				LOG.info("create new connection " + conn.getLocalAddress() + " done server is " + this.getAddress());
				return conn;
			} catch (IOException e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					LOG.error(ex);
				}
			}
		}

		throw new IOException("create new connection to " + this.host + ":" + this.port + " failure");
	}

	public void removeConnection(String connAddress) {
		this.pool.remove(connAddress);
	}

	public boolean ensureServerAlive() {
		boolean isAlive = true;
		Socket s = null;
		try {
			s = new Socket(this.host, this.port);
		} catch (Exception e) {
			isAlive = false;
		} finally {
			try {
				if (s != null) {
					s.close();
				}
			} catch (Exception e) {
			}
		}
		return isAlive;
	}

	public void setMinConnect(int num) {
		this.minConnectNum = num;
	}

	public int getMinConnect() {
		return this.minConnectNum;
	}

	public void setMaxConnect(int num) {
		this.maxConnectNum = num;
	}

	public int getMaxConnect() {
		return this.maxConnectNum;
	}

	public int getActiveConnect() {
		return this.pool.size();
	}

	public Connection getConnection() throws IOException {
		if (isPause()) {
			throw new IllegalStateException("获取连接失败，连接池已经暂停");
		}
		if (isShutdown()) {
			throw new IllegalStateException("连接池已经关闭, address=" + this.host + ":" + this.port);
		}

		try {
			se.acquire();
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}

		synchronized (this.pool) {
			CpConnection conn = null;
			// 遍历连接池找到可用的连接.
			Iterator<CpConnection> connIt = this.pool.values().iterator();
			while (connIt.hasNext()) {
				CpConnection c = connIt.next();
				if (c.isOpen() && !c.isActive()) {
					c.setActive(se);
					return c;
				}
			}

			// 当连接池中都不可用时并且没有达到最大连接数则创建新的连接.
			if (this.pool.size() < maxConnectNum) {
				conn = createConnection();
				conn.setActive(se);
				this.pool.put(conn.getLocalAddress(), conn);
				return conn;
			}
		}

		throw new IOException("获取连接失败");
	}

	public String getAddress() {
		return this.host + ":" + this.port;
	}

	@Override
	public void doPause() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void doUnpause() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void doShutdown() {
		synchronized (this.pool) {
			// 关闭连接池的连接.
			for (CpConnection c : this.pool.values()) {
				c.closeChannel();
			}
		}

		// 停止清理线程
		if (checker != null) {
			synchronized (checker) {
				checker.interrupt();
			}
			checker = null;
		}
	}

	/**
	 * 清理多余连接的线程.
	 */
	private class CheckThread extends Thread {
		private boolean isRun;

		public CheckThread() {
			super("connectionpool-thread-checker");
			isRun = true;
		}

		public void run() {
			while (isRun && !this.isInterrupted()) {
				try {
					synchronized (pool) {
						Iterator<CpConnection> it = pool.values().iterator();
						CpConnection conn = null;
						while (it.hasNext() && pool.size() > minConnectNum) {
							conn = it.next();
							if (!conn.isActive()) {
								conn.closeChannel();
								it.remove();
							}
						}
					}

					Thread.sleep(cleanPeriod);
				} catch (Exception e) {
					isRun = false;
					if (LOG.isDebugEnabled()) {
						LOG.debug("退出清理连接线程");
					}
				}
			}
		}

	}
}
