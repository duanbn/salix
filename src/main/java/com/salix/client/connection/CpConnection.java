package com.salix.client.connection;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import com.salix.core.message.Message;

/**
 * 连接池中的连接器.
 * 
 * @author duanbn
 * @since 1.1
 */
public class CpConnection extends AbstractConnection {
	public static final Logger log = Logger.getLogger(CpConnection.class);

	/**
	 * 当前连接是否被激活
	 */
	private boolean active;

	private Semaphore se;

	public CpConnection(String host, int port) throws IOException {
		super(host, port);
	}

	/**
	 * 将此连接设置为活动. 表示此连接正在被使用
	 */
	public void setActive(Semaphore se) {
		this.active = true;
		this.se = se;
	}

	/**
	 * 判断此连接是否是活动的.
	 */
	public boolean isActive() {
		return this.active;
	}

	@Override
	public void send(Message message) throws IOException {
		if (!isActive()) {
			throw new IllegalStateException("连接处于非活动状态, 请重新从连接池中获取连接");
		}

		try {
			super.send(message);
		} finally {
			this.se.release();
		}
	}

	@Override
	public Message receive() throws IOException {
		if (!isActive()) {
			throw new IllegalStateException("连接处于非活动状态, 请重新从连接池中获取连接");
		}

		try {
			return super.receive();
		} finally {
			this.se.release();
		}
	}

	/**
	 * 逻辑关闭连接. 关闭连接并将状态设置为非活动.从连接池中获取此连接的时候 使用逻辑关闭
	 */
	public void close() {
		this.active = false;
		this.se.release();
	}

	/**
	 * 物理关闭此连接关联的通道. 当直接使用此连接的时候需物理关闭连接.
	 */
	public void closeChannel() {
		try {
			if (isOpen())
				this.channel.close();
		} catch (IOException e) {
			log.warn("关闭通道失败");
		} finally {
			this.se.release();
		}
	}

	@Override
	public String toString() {
		StringBuilder info = new StringBuilder();
		info.append("host=").append(super.getLocalAddress());
		info.append(", port=").append(super.getLocalPort());
		return info.toString();
	}

}
