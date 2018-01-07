package org.duanbn.salix.network;

import java.net.InetSocketAddress;
import java.util.List;

import org.duanbn.salix.network.message.SendMessage;

public interface EndPointGroup extends Lifecycle {

    String getId();

    <T> EndPointFutureGroup send(SendMessage<?> msg);

    void add(List<InetSocketAddress> address);

    void add(String ip, int port);

    void add(EndPoint endPoint);

    EndPoint remove(String endPointId);

    EndPoint get(String endPointId);

    EndPoint choice();

    List<EndPoint> avalilableList();

    void closeAll();

    void setAvalilable(EndPoint endpoint);

    void setUnavalilable(EndPoint endpoint);

    EndPointFactory getRemoteFactory();

}
