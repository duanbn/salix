package org.duanbn.salix.network.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.duanbn.salix.network.EndPoint;
import org.duanbn.salix.network.EndPointFactory;
import org.duanbn.salix.network.EndPointGroup;
import org.duanbn.salix.network.EndPointPromise;
import org.duanbn.salix.network.ServerEndPoint;
import org.duanbn.salix.network.exception.EndPointException;
import org.duanbn.salix.network.exception.EndPointInternalException;
import org.duanbn.salix.network.exception.TimeoutException;
import org.duanbn.salix.network.handler.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyEndPointFactory extends AbstractLifecycle implements EndPointFactory {

    private static final Logger                  LOG                 = LoggerFactory
            .getLogger(NettyEndPointFactory.class);

    private static volatile NettyEndPointFactory instance;

    private EndPointConfig                       config;
    private String                               localHost;

    private NioEventLoopGroup                    bossGroup;
    private NioEventLoopGroup                    workderGroup;

    private ServerBootstrap                      serverBootstrap;
    private Bootstrap                            bootstrap;

    private Map<String, ServerEndPoint>          serverHolder        = new ConcurrentHashMap<String, ServerEndPoint>();
    private Map<String, EndPoint>                endPointHolder      = new ConcurrentHashMap<String, EndPoint>();
    private List<EndPointGroup>                  endPointGroupHolder = new ArrayList<EndPointGroup>();

    private NettyEndPointFactory(EndPointConfig config) {
        if (config != null) {
            this.config = config;
        } else {
            this.config = new EndPointConfig();
        }

        try {
            LOG.info("starting get local host");
            this.localHost = InetAddress.getLocalHost().getHostAddress();
            LOG.info("get local host done is {}", this.localHost);
        } catch (UnknownHostException e) {
            throw new IllegalStateException("get local host failure", e);
        }
    }

    public static EndPointFactory getInstance() {
        return getInstance(null);
    }

    public static EndPointFactory getInstance(EndPointConfig config) {
        if (instance == null) {
            synchronized (NettyEndPointFactory.class) {
                if (instance == null) {
                    instance = new NettyEndPointFactory(config);
                }
            }
        }

        return instance;
    }

    public ServerBootstrap getServerBootstrap() {
        return this.serverBootstrap;
    }

    public Bootstrap getBootStrap() {
        return this.bootstrap;
    }

    @Override
    public synchronized EndPointFactory startup() {
        if (isRunning()) {
            LOG.info("is running now");
            return this;
        }
        if (isStartuping()) {
            LOG.info("is startuping now");
            return this;
        }

        startuping();

        this.bossGroup = new NioEventLoopGroup();
        this.workderGroup = new NioEventLoopGroup();

        // init ServerBootstrap
        this.serverBootstrap = new ServerBootstrap();
        this.serverBootstrap.group(bossGroup, workderGroup);
        this.serverBootstrap.channel(NioServerSocketChannel.class);
        this.serverBootstrap.option(ChannelOption.SO_BACKLOG, config.getBackLog())
                .option(ChannelOption.SO_KEEPALIVE, config.isSoKeepAlive())
                .option(ChannelOption.TCP_NODELAY, config.isTcpNodelay());
        LOG.info("init server bootstrap done");

        // init Bootstrap
        this.bootstrap = new Bootstrap();
        this.bootstrap.channel(NioSocketChannel.class);
        this.bootstrap.group(workderGroup);
        this.bootstrap.option(ChannelOption.TCP_NODELAY, this.config.isTcpNodelay())
                .option(ChannelOption.SO_KEEPALIVE, this.config.isSoKeepAlive())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.config.getConnectionTimeout())
                .option(ChannelOption.SO_SNDBUF, this.config.getSendBuf())
                .option(ChannelOption.SO_RCVBUF, this.config.getReceiveBuf());
        LOG.info("init boostrap done");

        running();

        return this;
    }

    @Override
    public synchronized void shutdown() {
        if (!isRunning()) {
            LOG.info("not running");
            return;
        }

        shutdowning();
        LOG.info("start shutdown");

        for (ServerEndPoint server : this.serverHolder.values()) {
            server.close();
        }
        this.serverHolder.clear();

        for (EndPoint client : this.endPointHolder.values()) {
            client.close();
        }
        this.endPointHolder.clear();

        this.bossGroup.shutdownGracefully();
        this.workderGroup.shutdownGracefully();

        super.shutdown();
        LOG.info("shutdown done");
    }

    @Override
    public EventLoopGroup getBossEventLoopGroup() {
        return this.bossGroup;
    }

    @Override
    public EventLoopGroup getWorkerEventLoopGroup() {
        return this.workderGroup;
    }

    @Override
    public String getLocalHost() {
        return this.localHost;
    }

    @Override
    public EndPointConfig getConfig() {
        return this.config;
    }

    @Override
    public ServerEndPoint createServerEndPoint(int port) throws EndPointException {
        return createServerEndPoint(port, new ServerHandler[0]);
    }

    @Override
    public ServerEndPoint createServerEndPoint(int port, ServerHandler... handlers) throws EndPointException {
        ServerEndPoint serverEndPoint = new NettyServerEndPoint(this, port);

        if (handlers.length > 0) {
            serverEndPoint.addLastHandler(handlers);
        }

        serverEndPoint.listen(this.config);

        this.serverHolder.put(serverEndPoint.getId(), serverEndPoint);

        return serverEndPoint;
    }

    @Override
    public EndPoint createEndPoint(String host, int port) {
        NettyEndPoint endPoint = new NettyEndPoint(this, host, port);

        EndPointPromise<Void> connectPromise = endPoint.connect();
        try {
            connectPromise.doWait(this.config.getConnectionTimeout());
        } catch (InterruptedException e) {
            throw new EndPointInternalException(e);
        } catch (TimeoutException e) {
            throw new EndPointInternalException(e);
        }

        this.endPointHolder.put(endPoint.getId(), endPoint);

        return endPoint;
    }

    @Override
    public EndPointGroup createEndPointGroup() {
        NettyEndPointGroup group = new NettyEndPointGroup(this);
        synchronized (this.endPointGroupHolder) {
            this.endPointGroupHolder.add(group);
        }
        return group;
    }

    @Override
    public EndPointGroup createEndPointGroup(List<InetSocketAddress> addresses) {
        EndPointGroup group = createEndPointGroup();
        group.add(addresses);
        return group;
    }

    @Override
    public EndPointGroup createEndPointGroup(InetSocketAddress... addresses) {
        return createEndPointGroup(Arrays.asList(addresses));
    }

    @Override
    public List<EndPointGroup> getGroups() {
        return this.endPointGroupHolder;
    }

}
