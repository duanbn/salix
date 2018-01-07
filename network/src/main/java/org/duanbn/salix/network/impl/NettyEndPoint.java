package org.duanbn.salix.network.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.commons.lang.StringUtils;
import org.duanbn.salix.network.EndPoint;
import org.duanbn.salix.network.EndPointFactory;
import org.duanbn.salix.network.EndPointFuture;
import org.duanbn.salix.network.EndPointGroup;
import org.duanbn.salix.network.EndPointPromise;
import org.duanbn.salix.network.exception.EndPointInternalException;
import org.duanbn.salix.network.exception.TimeoutException;
import org.duanbn.salix.network.message.AckMessage;
import org.duanbn.salix.network.message.HeartbeatMessage;
import org.duanbn.salix.network.message.SendMessage;
import org.duanbn.salix.network.util.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyEndPoint extends AbstractLifecycle implements EndPoint {

    private static final Logger                        LOG                = LoggerFactory
            .getLogger(NettyEndPoint.class);

    private String                                     id;

    private String                                     host;

    private int                                        port;

    private NettyEndPointFactory                       remoteFactory;
    private EndPointGroup                              belongGroup;
    private Bootstrap                                  bootstrap;
    private Channel                                    channel;
    private Map<String, EndPointFuture<AckMessage<?>>> remoteFutureHolder = new ConcurrentHashMap<String, EndPointFuture<AckMessage<?>>>();

    private EndPointPromise<Void>                      connectPromise;
    private ConnectChannelFutureListener               connectListener;

    public NettyEndPoint(NettyEndPointFactory factory, String host, int port) {
        this.id = host.replaceAll("\\.", "") + port;
        this.host = host;
        this.port = port;

        this.remoteFactory = factory;
        this.bootstrap = factory.getBootStrap();

        this.connectPromise = new EndPointPromise<Void>();
        this.connectListener = new ConnectChannelFutureListener(this);

    }

    @Override
    public EndPointGroup getGroup() {
        return this.belongGroup;
    }

    @Override
    public void setGroup(EndPointGroup group) {
        this.belongGroup = group;
    }

    @Override
    public EndPointPromise<Void> connect() {
        startuping();

        final NettyEndPoint endPoint = this;

        LOG.info("connecting to server {}", this);

        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ObjectDecoder decoder = new ObjectDecoder(ClassResolvers.cacheDisabled(null));
                ObjectEncoder encoder = new ObjectEncoder();
                EndPointCoreChannelHandler endPointCoreChannelHandler = new EndPointCoreChannelHandler(endPoint);
                ch.pipeline().addLast(decoder);
                ch.pipeline().addLast(encoder);
                ch.pipeline().addLast(endPointCoreChannelHandler);
            }
        });

        ChannelFuture cf = bootstrap.connect(this.host, this.port);
        cf.addListener(this.connectListener);

        return this.connectPromise;
    }

    @Override
    public void close() {
        if (isRunning()) {

            this.shutdowning();

            this.channel.close().syncUninterruptibly();

            if (this.belongGroup != null) {
                this.belongGroup.setUnavalilable(this);
            }

            this.shutdown();
        }
    }

    @Override
    public AckMessage<?> syncSend(final SendMessage<?> msg) throws TimeoutException {
        EndPointConfig remoteConfig = this.remoteFactory.getConfig();
        int sendRetry = remoteConfig.getSendRetry();

        if (sendRetry <= 0) {
            sendRetry = 1;
        }

        SendMessage<?> sendMsg = initSendMessage(msg);

        EndPointFuture<AckMessage<?>> remoteFuture = new EndPointFuture<AckMessage<?>>();
        while (sendRetry-- > 0) {
            asyncSend0(sendMsg, remoteFuture);

            try {
                if (remoteConfig.getSendTimeout() > 0) {
                    remoteFuture.await(remoteConfig.getSendTimeout());
                } else {
                    remoteFuture.await();
                }

                if (remoteFuture.isSuccess()) {
                    return (AckMessage<?>) remoteFuture.getMessage();
                } else {
                    throw new EndPointInternalException(sendMsg.getRequestId(), remoteFuture.getThrowable());
                }
            } catch (InterruptedException e) {
                LOG.warn("send message interupted {}", e.getMessage());
            } catch (TimeoutException e) {
                LOG.warn("send message timeout wait for {} remain retry {}", remoteConfig.getSendTimeout(), sendRetry);
            }
        }

        this.remoteFutureHolder.remove(remoteFuture.getRequestId());

        TimeoutException te = new TimeoutException("send message failure timeout requestId is " + sendMsg.getRequestId()
                + " message type is" + sendMsg.getClass());
        te.setRequestId(sendMsg.getRequestId());
        te.setSendMessageClass(sendMsg.getClass());
        throw te;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T extends AckMessage<?>> EndPointFuture<T> asyncSend(SendMessage<?> msg) {
        SendMessage<?> sendMsg = initSendMessage(msg);

        EndPointConfig remoteConfig = this.remoteFactory.getConfig();

        final String requestId = sendMsg.getRequestId();

        EndPointFuture remoteFuture = new EndPointFuture();

        if (remoteConfig.getSendTimeout() > 0) {
            Future timeoutFuture = this.remoteFactory.getWorkerEventLoopGroup().schedule(new Runnable() {
                @Override
                public void run() {
                    EndPointFuture remoteFuture = remoteFutureHolder.remove(requestId);
                    remoteFuture.setFailure(new TimeoutException(requestId + " send time out"));
                    remoteFuture.signal();
                }
            }, remoteConfig.getSendTimeout(), TimeUnit.MILLISECONDS);
            remoteFuture.setTimeoutFuture(timeoutFuture);
        }

        asyncSend0(sendMsg, remoteFuture);

        return remoteFuture;
    }

    private SendMessage<?> initSendMessage(SendMessage<?> sendMsg) {
        if (StringUtils.isBlank(sendMsg.getRequestId())) {
            // gen request id.
            String requestId = UUID.randomUUID().toString().replace("-", "");
            sendMsg.setRequestId(requestId);
        }

        return sendMsg;
    }

    private void asyncSend0(SendMessage<?> msg, EndPointFuture<AckMessage<?>> remoteFuture) {
        String requestId = msg.getRequestId();

        remoteFuture.setRequestId(requestId);
        if (!this.remoteFutureHolder.containsKey(requestId)) {
            this.remoteFutureHolder.put(requestId, remoteFuture);
        }

        this.channel.writeAndFlush(msg);
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public void setConnectPromise(EndPointPromise<Void> promise) {
        this.connectPromise = promise;
    }

    public EndPointPromise<Void> getConnectPromise() {
        return this.connectPromise;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public EndPointFactory getFactory() {
        return this.remoteFactory;
    }

    /**
     * Connect listener
     */
    public static class ConnectChannelFutureListener implements ChannelFutureListener {

        private static final Logger LOG = LoggerFactory.getLogger(ConnectChannelFutureListener.class);

        private NettyEndPoint       endPoint;

        public ConnectChannelFutureListener(NettyEndPoint endPoint) {
            this.endPoint = endPoint;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                Channel channel = future.channel();

                this.endPoint.setChannel(channel);

                this.endPoint.running();
                if (this.endPoint.getGroup() != null) {
                    this.endPoint.getGroup().setAvalilable(this.endPoint);
                }

                LOG.info("{} connect done", endPoint);

                this.endPoint.getConnectPromise().signalAll();
                // promise has signaled. so we need new another one
                this.endPoint.setConnectPromise(new EndPointPromise<Void>());
            } else {
                LOG.info("{} connect failure", endPoint);
                EndPointConfig config = this.endPoint.getFactory().getConfig();
                Sleep.doSleep(config.getReconnectTime());
                endPoint.connect();
            }
        }
    }

    /**
     * core channel handler
     * 
     * @author shanwei Dec 7, 2017 5:21:07 PM
     */
    public class EndPointCoreChannelHandler extends ChannelInboundHandlerAdapter {

        private EndPoint endPoint;

        public EndPointCoreChannelHandler(EndPoint endPoint) {
            this.endPoint = endPoint;
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if (this.endPoint.getGroup() != null) {
                this.endPoint.getGroup().setUnavalilable(this.endPoint);
            }

            if (this.endPoint.isRunning()) {
                LOG.info("{} disconnect when is running", this.endPoint);
                this.endPoint.connect();
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Channel channel = ctx.channel();

            if (msg instanceof AckMessage) {
                handleRemoteMessage((AckMessage<?>) msg);
            } else if (msg instanceof HeartbeatMessage) {
                channel.writeAndFlush(new HeartbeatMessage());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            Iterator<EndPointFuture<AckMessage<?>>> remoteFutureIt = remoteFutureHolder.values().iterator();
            EndPointFuture<AckMessage<?>> remoteFuture = null;
            while (remoteFutureIt.hasNext()) {
                remoteFuture = remoteFutureIt.next();

                if (remoteFuture.getTimeoutFuture() != null) {
                    remoteFuture.getTimeoutFuture().cancel(true);
                }

                remoteFuture.setFailure(cause);
                remoteFuture.signal();
                remoteFutureIt.remove();
            }
        }

        private void handleRemoteMessage(AckMessage<?> msg) {
            String requestId = msg.getRequestId();

            if (remoteFutureHolder.containsKey(requestId)) {

                EndPointFuture<AckMessage<?>> future = remoteFutureHolder.remove(requestId);

                if (future.getTimeoutFuture() != null) {
                    future.getTimeoutFuture().cancel(true);
                }

                future.setSuccess(msg);

                future.signal();
            }
        }

    }

    @Override
    public String toString() {
        return this.id + "::" + this.host + ":" + this.port;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NettyEndPoint other = (NettyEndPoint) obj;
        if (host == null) {
            if (other.host != null)
                return false;
        } else if (!host.equals(other.host))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (port != other.port)
            return false;
        return true;
    }
}
