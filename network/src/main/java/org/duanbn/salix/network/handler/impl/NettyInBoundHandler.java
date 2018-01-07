package org.duanbn.salix.network.handler.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.duanbn.salix.network.message.HeartbeatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyInBoundHandler extends ChannelInboundHandlerAdapter {

    private Logger                    LOG = LoggerFactory.getLogger(NettyInBoundHandler.class);

    private NettyServerHandlerContext serverHandlerContext;

    public NettyInBoundHandler(NettyServerHandlerContext serverCoreHandler) {
        this.serverHandlerContext = serverCoreHandler;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.serverHandlerContext.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        this.serverHandlerContext.channelUnregistered(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        this.serverHandlerContext.channelReadComplete(ctx);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        this.serverHandlerContext.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("{} has connected", ctx.channel());
        serverHandlerContext.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("{} has disconnected", ctx.channel());
        serverHandlerContext.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HeartbeatMessage) {
            return;
        }

        serverHandlerContext.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("", cause);

        serverHandlerContext.exceptionCaught(ctx, cause);
    }

}
