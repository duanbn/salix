package org.duanbn.salix.network.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.duanbn.salix.network.ServerEndPoint;
import org.duanbn.salix.network.exception.EndPointException;
import org.duanbn.salix.network.handler.ServerHandler;
import org.duanbn.salix.network.handler.impl.NettyInBoundHandler;
import org.duanbn.salix.network.handler.impl.NettyServerHandlerContext;
import org.duanbn.salix.network.message.HeartbeatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServerEndPoint extends AbstractLifecycle implements ServerEndPoint {

    private static final Logger       LOG = LoggerFactory.getLogger(NettyServerEndPoint.class);

    private String                    id;

    private int                       listenPort;

    private ServerBootstrap           serverBootstrap;

    private NettyServerHandlerContext serverHandlerContext;

    private NettyEndPointFactory      remoteFactory;

    public NettyServerEndPoint(NettyEndPointFactory remoteFactory, int port) {
        this.remoteFactory = remoteFactory;

        this.serverBootstrap = remoteFactory.getServerBootstrap();

        this.serverHandlerContext = new NettyServerHandlerContext();

        this.listenPort = port;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public ServerEndPoint addLastHandler(ServerHandler... handlers) {
        this.serverHandlerContext.addLastHandler(handlers);
        return this;
    }

    @Override
    public ServerEndPoint addLastHandler(String name, ServerHandler handler) {
        this.serverHandlerContext.addLastHandler(name, handler);
        return this;
    }

    @Override
    public ServerEndPoint addFirstHandler(ServerHandler... handler) {
        this.serverHandlerContext.addFirstHandler(handler);
        return this;
    }

    @Override
    public ServerEndPoint addFirstHandler(String name, ServerHandler handler) {
        this.serverHandlerContext.addFirstHandler(name, handler);
        return this;
    }

    @Override
    public ServerEndPoint addBeforeHandler(String baseName, String name, ServerHandler handler) {
        this.serverHandlerContext.addBeforeHandler(baseName, name, handler);
        return this;
    }

    @Override
    public ServerEndPoint addAfterHandler(String baseName, String name, ServerHandler handler) {
        this.serverHandlerContext.addAfterHandler(baseName, name, handler);
        return this;
    }

    @Override
    public ServerEndPoint removeHandler(String handlerName) {
        this.serverHandlerContext.removeHandler(handlerName);
        return this;
    }

    @Override
    public ServerEndPoint removeHandler(ServerHandler handler) {
        this.serverHandlerContext.removeHandler(handler);
        return this;
    }

    @Override
    public int getListenPort() {
        return this.listenPort;
    }

    @Override
    public void listen(EndPointConfig config) throws EndPointException {
        startuping();

        String localIp = this.remoteFactory.getLocalHost();
        this.id = new StringBuilder(localIp.replaceAll("\\.", "")).append(this.listenPort).toString();

        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ObjectDecoder decoder = new ObjectDecoder(ClassResolvers.cacheDisabled(null));
                ObjectEncoder encoder = new ObjectEncoder();

                ch.pipeline().addLast("logging", new LoggingHandler(LogLevel.DEBUG));

                // send a heartbeat message when there is no outbound for 30 seconds
                // close the connection where there is no inbound for 60 seconds
                ch.pipeline().addLast("idleHandler", new IdleStateHandler(60, 30, 0));

                NettyInBoundHandler inboundHandler = new NettyInBoundHandler(serverHandlerContext);
                //                NettyOutboundHandler outboundHandler = new NettyOutboundHandler(serverCoreHandler);
                ch.pipeline().addLast(decoder, new ChannelInboundHandlerAdapter() {
                    @Override
                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                        if (evt instanceof IdleStateEvent) {
                            Channel channel = ctx.channel();

                            IdleStateEvent e = (IdleStateEvent) evt;
                            if (e.state() == IdleState.READER_IDLE) {
                                channel.close();
                                LOG.info("{} heartbeat timeout closed", channel.remoteAddress());
                            } else if (e.state() == IdleState.WRITER_IDLE) {
                                channel.writeAndFlush(new HeartbeatMessage());
                            }
                        }
                    }
                }, inboundHandler, encoder);
            }
        });

        this.serverBootstrap.bind(this.listenPort).syncUninterruptibly();

        running();

        LOG.info("server({}) startup done listen on {}:{}", this.getId(), localIp, this.listenPort);
    }

    @Override
    public void close() {
    }

}
