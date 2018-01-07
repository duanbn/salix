package org.duanbn.salix.network;

import java.net.InetSocketAddress;
import java.util.List;

import io.netty.channel.EventLoopGroup;
import org.duanbn.salix.network.exception.EndPointException;
import org.duanbn.salix.network.handler.ServerHandler;
import org.duanbn.salix.network.impl.EndPointConfig;

public interface EndPointFactory extends Lifecycle {

    EndPointFactory startup();

    void shutdown();

    EventLoopGroup getBossEventLoopGroup();

    EventLoopGroup getWorkerEventLoopGroup();

    String getLocalHost();

    EndPointConfig getConfig();

    ServerEndPoint createServerEndPoint(int port) throws EndPointException;

    ServerEndPoint createServerEndPoint(int port, ServerHandler... handlers) throws EndPointException;

    EndPoint createEndPoint(String host, int port);

    EndPointGroup createEndPointGroup();

    EndPointGroup createEndPointGroup(List<InetSocketAddress> addresses);

    EndPointGroup createEndPointGroup(InetSocketAddress... addresses);

    List<EndPointGroup> getGroups();

}
