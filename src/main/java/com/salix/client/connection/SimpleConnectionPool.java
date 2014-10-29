package com.salix.client.connection;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
	public static final Logger log = Logger.getLogger(SimpleConnectionPool.class);

	public static final int DEFAULT_MIN_CONNECT_NUM = 1;
	public static final int DEFAULT_MAX_CONNECT_NUM = 20;
	private int minConnectNum = DEFAULT_MIN_CONNECT_NUM;
	private int maxConnectNum = DEFAULT_MAX_CONNECT_NUM;

	private int cleanPeriod = 20 * 1000;

	private List<CpConnection> pool;

	private static Semaphore se;

	private String host;
	private int port;

	/**
	 * 清理多余连接，间隔1分钟执行.
	 */
	private CheckThread checker;

	public SimpleConnectionPool(String host, int port) {
		pool = new LinkedList<CpConnection>();

		this.host = host;
		this.port = port;
	}

	@Override
	public void doInit() {
		se = new Semaphore(maxConnectNum, true);

		try {
			CpConnection conn = null;
			while (pool.size() < minConnectNum) {
				conn = createConnection();
				pool.add(conn);
			}
		} catch (IOException e) {
			throw new IllegalStateException("网络连接错误，初始化连接池失败");
		}

		if (pool.size() != minConnectNum) {
			throw new IllegalStateException("网络连接错误，初始化连接池失败");
		}
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
				if (log.isDebugEnabled())
					log.debug("connect to " + this.host + ":" + this.port + " done");
				return conn;
			} catch (IOException e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					log.error(ex);
				}
			}
		}

		throw new IOException("create connection failure");
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
		return pool.size();
	}

	public Connection getConnection() {
		if (isPause()) {
			throw new IllegalStateException("获取连接失败，连接池已经暂停");
		}
		if (isShutdown()) {
			throw new IllegalStateException("连接池已经关闭");
		}

		synchronized (pool) {
			try {
				se.acquire();
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1);
			}

			CpConnection conn = null;
			// 遍历连接池找到可用的连接.
			for (CpConnection c : pool) {
				if (c.isOpen() && !c.isActive()) {
					c.setActive(se);
					return c;
				}
			}

			// 当连接池中都不可用时并且没有达到最大连接数则创建新的连接.
			try {
				if (pool.size() < maxConnectNum) {
					conn = createConnection();
					conn.setActive(se);
					pool.add(conn);
					return conn;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		throw new RuntimeException("获取连接失败");
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
		// 关闭连接池的连接.
		for (CpConnection c : pool) {
			c.closeChannel();
		}

		// 停止清理线程
		checker.interrupt();
	}

	@Override
	public void doStartup() {
		// 启动清理线程.
		checker = new CheckThread();
		checker.start();
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
						Iterator<CpConnection> it = pool.iterator();
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
					if (log.isDebugEnabled()) {
						log.debug("退出清理连接线程");
					}
				}
			}
		}
	}
}
