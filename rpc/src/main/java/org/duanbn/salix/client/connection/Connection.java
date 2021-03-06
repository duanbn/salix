package org.duanbn.salix.client.connection;

import java.io.IOException;

import org.duanbn.salix.core.message.Message;

/**
 * 连接器接口.
 * 
 * @author duanbn
 * @since 1.1
 */
public interface Connection {

	/**
	 * ping. 如果返回-1则表示服务器无法连接.
	 * 
	 * @return 延迟时间.
	 */
	public int ping();

	/**
	 * 不管通道是否是阻塞的，此方法都将会阻塞直到一个信息被发送完毕.
	 * 
	 * @param message
	 *            发送的信息.
     * @return 接收到的消息
	 */
	public Message send(Message message) throws IOException;

	/**
	 * 逻辑关闭连接. 关闭连接并将状态设置为非活动.从连接池中获取此连接的时候 使用逻辑关闭
	 */
	public void close();

	public String getLocalAddress();

	public int getLocalPort();
}
