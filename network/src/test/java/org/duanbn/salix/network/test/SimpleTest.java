package org.duanbn.salix.network.test;

import java.net.InetSocketAddress;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import org.duanbn.salix.network.EndPoint;
import org.duanbn.salix.network.EndPointFactory;
import org.duanbn.salix.network.EndPointFuture;
import org.duanbn.salix.network.EndPointFuture.FutureListener;
import org.duanbn.salix.network.EndPointFuture.FutureResult;
import org.duanbn.salix.network.EndPointFutureGroup;
import org.duanbn.salix.network.EndPointGroup;
import org.duanbn.salix.network.exception.TimeoutException;
import org.duanbn.salix.network.handler.impl.ServerInBoundHandler;
import org.duanbn.salix.network.impl.EndPointConfig;
import org.duanbn.salix.network.impl.NettyEndPointFactory;
import org.duanbn.salix.network.message.AckMessage;
import org.duanbn.salix.network.message.SendMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTest {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleTest.class);

    public static class EchoHandler extends ServerInBoundHandler<SendMessage<String>> {
        @Override
        public AckMessage<String> channelRead(ChannelHandlerContext ctx, SendMessage<String> message) throws Exception {
            LOG.info("receive {} {}", message.getRequestId(), message.getData());

            AckMessage<String> ack = new AckMessage<String>(message.getRequestId());
            ack.setData((String) message.getData());

            return ack;
        }
    }

    public static class TimeoutHandler extends ServerInBoundHandler<TimeoutMessage> {
        @Override
        public AckMessage<String> channelRead(ChannelHandlerContext ctx, TimeoutMessage message) throws Exception {
            LOG.info("receive {} {}", message.getRequestId(), message.getData());

            AckMessage<String> ack = new AckMessage<String>();
            ack.setData(message.getData());

            Thread.sleep(1000);

            return ack;
        }
    }

    @Test
    public void testServer() throws Exception {
        remoteFactory.createServerEndPoint(9999, new EchoHandler(), new TimeoutHandler());
        remoteFactory.createServerEndPoint(9998, new EchoHandler());

        System.in.read();
    }

    @Test
    public void testClient() {
        EndPoint endPoint = remoteFactory.createEndPoint("127.0.0.1", 9999);

        try {
            //            LOG.info("{}", endPoint.syncSend(new SendMessage<String>("hello sync")).getData());
            LOG.info("{}", endPoint.syncSend(new UnknowMessage()).getMessage());
        } catch (TimeoutException e) {
            LOG.error(e.getMessage());
        }

        EndPointFuture<AckMessage<String>> remoteFuture = endPoint.asyncSend(new TimeoutMessage());
        remoteFuture.addListener(new FutureListener<AckMessage<String>>() {
            @Override
            public void optionComplete(FutureResult<AckMessage<String>> result) {
                AckMessage<String> value = result.getAckMessage();

                if (result.isSuccess()) {
                    LOG.info("{}", value.getData());
                } else {
                    LOG.error("", result.getCause());
                }
            }
        });

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
        }
    }

    @Test
    public void testGroup() throws Exception {
        EndPointGroup group = remoteFactory.createEndPointGroup(new InetSocketAddress("127.0.0.1", 9999),
                new InetSocketAddress("127.0.0.1", 9998));

        SendMessage<String> msg = new SendMessage<String>("hello");
        EndPointFutureGroup futureGroup = group.send(msg);
        List<AckMessage<String>> result = futureGroup.sync();
        for (AckMessage<String> v : result) {
            LOG.info("{}", v.getData());
        }
    }

    private static EndPointFactory remoteFactory;

    @BeforeClass
    public static void beforeClass() {
        EndPointConfig config = new EndPointConfig();
        //        config.setSendTimeout(500);

        remoteFactory = NettyEndPointFactory.getInstance(config);
        remoteFactory.startup();
    }

    @AfterClass
    public static void afterClass() {
        remoteFactory.shutdown();
    }

    public static class TimeoutMessage extends SendMessage<String> {
        private static final long serialVersionUID = -2625628975210985949L;

        public TimeoutMessage() {
            this.data = "timeout message";
        }
    }

    public static class UnknowMessage extends SendMessage<Void> {
        private static final long serialVersionUID = 8207110751751262743L;
    }
}
