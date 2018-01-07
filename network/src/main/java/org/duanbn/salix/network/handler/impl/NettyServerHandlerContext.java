package org.duanbn.salix.network.handler.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.SocketAddress;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.duanbn.salix.network.handler.ServerHandler;
import org.duanbn.salix.network.message.AckMessage;
import org.duanbn.salix.network.message.AckMessage.AckCode;
import org.duanbn.salix.network.message.SendMessage;

public class NettyServerHandlerContext {

    private NameHandler head;
    private NameHandler tail;

    public NettyServerHandlerContext() {
        this.head = new NameHandler();
        this.tail = new NameHandler();

        this.head.next = this.tail;
        this.tail.preview = this.head;
    }

    public synchronized void addLastHandler(ServerHandler... handlers) {
        for (ServerHandler handler : handlers) {
            addLastHandler(null, handler);
        }
    }

    public void addLastHandler(String name, ServerHandler handler) {
        NameHandler current = new NameHandler(handler);
        current.name = name;

        current.next = tail;
        current.preview = tail.preview;
        tail.preview.next = current;
        tail.preview = current;
    }

    public synchronized void addFirstHandler(ServerHandler... handlers) {
        for (ServerHandler handler : handlers) {
            addFirstHandler(null, handler);
        }
    }

    public void addFirstHandler(String name, ServerHandler handler) {
        NameHandler current = new NameHandler(handler);
        current.name = name;

        current.preview = head;
        current.next = head.next;
        head.next.preview = current;
        head.next = current;
    }

    public synchronized void addBeforeHandler(String baseName, String name, ServerHandler handler) {
        NameHandler nameHandler = new NameHandler(handler);
        nameHandler.name = name;

        NameHandler current = this.head;
        do {
            if (current.name.equals(baseName)) {
                nameHandler.preview = current.preview;
                nameHandler.next = current;
                current.preview.next = nameHandler;
                current.preview = nameHandler;
                break;
            }
            current = current.next;
        } while (current.next != null);
    }

    public synchronized void addAfterHandler(String baseName, String name, ServerHandler handler) {
        NameHandler nameHandler = new NameHandler(handler);
        nameHandler.name = name;

        NameHandler current = this.head;
        do {
            if (current.name.equals(baseName)) {
                nameHandler.next = current.next;
                nameHandler.preview = current;
                current.next.preview = nameHandler;
                current.next = nameHandler;
                break;
            }
            current = current.next;
        } while (current.next != null);
    }

    public synchronized void removeHandler(String handlerName) {
        NameHandler current = this.head;
        do {
            if (current.name.equals(handlerName)) {
                current.preview.next = current.next;
                current.next.preview = current.preview;
                break;
            }
            current = current.next;
        } while (current.next != null);
    }

    public synchronized void removeHandler(ServerHandler handler) {
        NameHandler current = this.head;
        do {
            if (current.handler == handler) {
                current.preview.next = current.next;
                current.next.preview = current.preview;
                break;
            }
            current = current.next;
        } while (current.next != null);
    }

    private static class NameHandler {
        public NameHandler   preview;

        public NameHandler   next;

        public String        name;

        public ServerHandler handler;

        public Class<?>      sendMessageType;

        public NameHandler() {
            this(null);
        }

        public NameHandler(ServerHandler handler) {
            this(null, handler);
        }

        public NameHandler(String name, ServerHandler handler) {
            this.handler = handler;
            this.name = name;
            if (handler != null) {
                this.sendMessageType = _getGenricType(handler);
            }
        }

        private Class<?> _getGenricType(ServerHandler serverHandler) {
            Type genType = serverHandler.getClass().getGenericSuperclass();
            if (!(genType instanceof ParameterizedType)) {
                return Object.class;
            }
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            if (params[0] instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) params[0]).getRawType();
            }
            return (Class<?>) params[0];
        }
    }

    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise future) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerOutBoundHandler) {
                ((ServerOutBoundHandler) current.handler).bind(ctx, localAddress, future);
            }
            current = current.next;
        } while (current.next != null);
    }

    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                        ChannelPromise future)
            throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerOutBoundHandler) {
                ((ServerOutBoundHandler) current.handler).connect(ctx, remoteAddress, localAddress, future);
            }
            current = current.next;
        } while (current.next != null);
    }

    public void disconnect(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerOutBoundHandler) {
                ((ServerOutBoundHandler) current.handler).disconnect(ctx, future);
            }
            current = current.next;
        } while (current.next != null);
    }

    public void close(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerOutBoundHandler) {
                ((ServerOutBoundHandler) current.handler).close(ctx, future);
            }
            current = current.next;
        } while (current.next != null);
    }

    public void deregister(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerOutBoundHandler) {
                ((ServerOutBoundHandler) current.handler).deregister(ctx, future);
            }
            current = current.next;
        } while (current.next != null);
    }

    public void read(ChannelHandlerContext ctx) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerOutBoundHandler) {
                ((ServerOutBoundHandler) current.handler).read(ctx);
            }
            current = current.next;
        } while (current.next != null);
    }

    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerOutBoundHandler) {
                ((ServerOutBoundHandler) current.handler).write(ctx, (AckMessage<?>) msg, promise);
            }
            current = current.next;
        } while (current.next != null);
    }

    public void flush(ChannelHandlerContext ctx) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerOutBoundHandler) {
                ((ServerOutBoundHandler) current.handler).flush(ctx);
            }
            current = current.next;
        } while (current.next != null);
    }

    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerInBoundHandler) {
                ((ServerInBoundHandler<?>) current.handler).channelRegistered(ctx);
            }
            current = current.next;
        } while (current.next != null);
    }

    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerInBoundHandler) {
                ((ServerInBoundHandler<?>) current.handler).channelUnregistered(ctx);
            }
            current = current.next;
        } while (current.next != null);
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerInBoundHandler) {
                ((ServerInBoundHandler<?>) current.handler).channelActive(ctx);
            }
            current = current.next;
        } while (current.next != null);
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerInBoundHandler) {
                ((ServerInBoundHandler<?>) current.handler).channelInactive(ctx);
            }
            current = current.next;
        } while (current.next != null);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        NameHandler current = this.head;

        boolean isHandlerHandle = false;
        SendMessage sendMsg = (SendMessage<?>) msg;
        do {
            if (current.handler != null && current.handler instanceof ServerInBoundHandler) {
                final ServerInBoundHandler serverInboundHandler = (ServerInBoundHandler) current.handler;

                if (msg.getClass() == current.sendMessageType) {
                    AckMessage<?> ack = serverInboundHandler.channelRead(ctx, sendMsg);

                    if (ack == null) {
                        ack = new AckMessage<Void>();
                    }
                    ack.setRequestId(sendMsg.getRequestId());

                    ChannelFuture future = ctx.channel().writeAndFlush(ack);
                    future.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            serverInboundHandler.channelWriteAndFlushAfterRead(future);
                        }
                    });

                    isHandlerHandle = true;
                }
            }

            current = current.next;
        } while (current.next != null);

        // ack default message
        if (!isHandlerHandle) {
            AckMessage<?> ack = new AckMessage<String>(sendMsg.getRequestId(), AckCode.NO_HANDLER);
            ack.setMessage("no handler find");
            ctx.channel().writeAndFlush(ack);
        }

    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerInBoundHandler) {
                ((ServerInBoundHandler<?>) current.handler).channelReadComplete(ctx);
            }
            current = current.next;
        } while (current.next != null);
    }

    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerInBoundHandler) {
                ((ServerInBoundHandler<?>) current.handler).channelWritabilityChanged(ctx);
            }
            current = current.next;
        } while (current.next != null);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        NameHandler current = this.head;
        do {
            if (current.handler != null && current.handler instanceof ServerInBoundHandler) {
                ((ServerInBoundHandler<?>) current.handler).exceptionCaught(ctx, cause);
            }
            current = current.next;
        } while (current.next != null);
    }

}
