package org.duanbn.salix.network;

import org.duanbn.salix.network.exception.EndPointException;
import org.duanbn.salix.network.handler.ServerHandler;
import org.duanbn.salix.network.impl.EndPointConfig;

public interface ServerEndPoint extends Lifecycle {

    String getId();

    ServerEndPoint addLastHandler(ServerHandler... handler);

    ServerEndPoint addLastHandler(String name, ServerHandler handler);

    ServerEndPoint addFirstHandler(ServerHandler... handler);

    ServerEndPoint addFirstHandler(String name, ServerHandler handler);

    ServerEndPoint addBeforeHandler(String baseName, String name, ServerHandler handler);

    ServerEndPoint addAfterHandler(String baseName, String name, ServerHandler handler);

    ServerEndPoint removeHandler(String handlerName);

    ServerEndPoint removeHandler(ServerHandler hander);

    int getListenPort();

    void listen(EndPointConfig config) throws EndPointException;

    void close();

}
