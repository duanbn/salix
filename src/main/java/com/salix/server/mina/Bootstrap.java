package com.salix.server.mina;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.salix.server.RpcServiceContext;
import com.salix.server.mina.codec.RpcProtocolCodecFactory;

public class Bootstrap implements ApplicationContextAware, InitializingBean, DisposableBean {

	public static final Logger LOG = Logger.getLogger(Bootstrap.class);

	private String name;

	private int port;

	private String zkHost;

	private ApplicationContext springCtx;
	private RpcServiceContext rsc;

	public void afterPropertiesSet() throws Exception {
		IoAcceptor acceptor = new NioSocketAcceptor();

		// LoggingFilter loggingFilter = new LoggingFilter();
		// loggingFilter.setExceptionCaughtLogLevel(LogLevel.ERROR);
		// acceptor.getFilterChain().addLast("logger", loggingFilter);
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new RpcProtocolCodecFactory()));

		rsc = new RpcServiceContext(this.name, this.zkHost, this.springCtx);
		rsc.setListenPort(port);
		rsc.init();

		acceptor.setHandler(new DispatchHandler(rsc));

		acceptor.bind(new InetSocketAddress(this.port));

		LOG.info("startup done listen port " + this.port);
	}

	public void destroy() throws Exception {
		rsc.destroy();

		LOG.info("salix app " + this.name + " shutdown done listen port " + this.port);
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.springCtx = applicationContext;
	}

	private int _getRandomPort() {
		Random r = new Random();

		while (true) {
			int port = r.nextInt(65535);
			if (port < 10000) {
				continue;
			}

			try {
				new Socket("127.0.0.1", port);
			} catch (ConnectException e) {
				return port;
			} catch (IOException e) {
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		if (port == 0) {
			this.port = _getRandomPort();
		} else {
			this.port = port;
		}
	}

	public String getZkHost() {
		return zkHost;
	}

	public void setZkHost(String zkHost) {
		this.zkHost = zkHost;
	}
}
