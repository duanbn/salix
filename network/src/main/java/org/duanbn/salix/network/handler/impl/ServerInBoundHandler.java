package org.duanbn.salix.network.handler.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import org.duanbn.salix.network.handler.ServerHandler;
import org.duanbn.salix.network.message.AckMessage;
import org.duanbn.salix.network.message.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServerInBoundHandler<T extends SendMessage<?>> implements ServerHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ServerInBoundHandler.class);

    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} was unregistered
     * from its {@link EventLoop}
     */
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} is now active
     */
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} was registered
     * is now inactive and reached its end of lifetime.
     */
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * Invoked when the current {@link Channel} has read a message from the
     * peer.
     */
    public AckMessage<?> channelRead(ChannelHandlerContext ctx, T message) throws Exception {
        AckMessage<?> ack = new AckMessage<Object>(message.getRequestId());
        return ack;
    }

    public void channelWriteAndFlushAfterRead(ChannelFuture future) throws Exception {
    }

    /**
     * Invoked when the last message read by the current read operation has been
     * consumed by {@link #channelRead(ChannelHandlerContext, Object)}. If
     * {@link ChannelOption#AUTO_READ} is off, no further attempt to read an
     * inbound data from the current {@link Channel} will be made until
     * {@link ChannelHandlerContext#read()} is called.
     */
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * Gets called if an user event was triggered.
     */
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    }

    /**
     * Gets called once the writable state of a {@link Channel} changed. You can
     * check the state with {@link Channel#isWritable()}.
     */
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    }

    /**
     * Gets called if a {@link Throwable} was thrown.
     */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("server internal error", cause);
    }

}
