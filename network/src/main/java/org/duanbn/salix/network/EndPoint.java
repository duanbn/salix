package org.duanbn.salix.network;

import org.duanbn.salix.network.exception.TimeoutException;
import org.duanbn.salix.network.message.AckMessage;
import org.duanbn.salix.network.message.SendMessage;

public interface EndPoint extends Lifecycle {

    String getId();

    void setGroup(EndPointGroup group);

    EndPointGroup getGroup();

    EndPointFactory getFactory();

    EndPointPromise<Void> connect();

    void close();

    AckMessage<?> syncSend(SendMessage<?> msg) throws TimeoutException;

    <T extends AckMessage<?>> EndPointFuture<T> asyncSend(SendMessage<?> msg);

}
