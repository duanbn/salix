package org.duanbn.salix.network.handler.impl;

import java.net.SocketAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import org.duanbn.salix.network.handler.ServerHandler;
import org.duanbn.salix.network.message.AckMessage;

public abstract class ServerOutBoundHandler implements ServerHandler {

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }

    /**
     * Calls {@link ChannelHandlerContext#bind(SocketAddress, ChannelPromise)}
     * to forward to the next {@link ChannelOutboundHandler} in the
     * {@link ChannelPipeline}. Sub-classes may override this method to change
     * behavior.
     */
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    /**
     * Calls
     * {@link ChannelHandlerContext#connect(SocketAddress, SocketAddress, ChannelPromise)}
     * to forward to the next {@link ChannelOutboundHandler} in the
     * {@link ChannelPipeline}. Sub-classes may override this method to change
     * behavior.
     */
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                        ChannelPromise promise)
            throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    /**
     * Calls {@link ChannelHandlerContext#disconnect(ChannelPromise)} to forward
     * to the next {@link ChannelOutboundHandler} in the {@link ChannelPipeline}
     * . Sub-classes may override this method to change behavior.
     */
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
    }

    /**
     * Calls {@link ChannelHandlerContext#close(ChannelPromise)} to forward to
     * the next {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close(promise);
    }

    /**
     * Calls {@link ChannelHandlerContext#close(ChannelPromise)} to forward to
     * the next {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    /**
     * Calls {@link ChannelHandlerContext#read()} to forward to the next
     * {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    public void read(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    /**
     * Calls {@link ChannelHandlerContext#write(Object)} to forward to the next
     * {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    public void write(ChannelHandlerContext ctx, AckMessage<?> ack, ChannelPromise promise) throws Exception {
        ctx.write(ack, promise);
    }

    /**
     * Calls {@link ChannelHandlerContext#flush()} to forward to the next
     * {@link ChannelOutboundHandler} in the {@link ChannelPipeline}.
     * Sub-classes may override this method to change behavior.
     */
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

}
