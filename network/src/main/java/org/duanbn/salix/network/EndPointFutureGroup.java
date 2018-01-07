package org.duanbn.salix.network;

import java.util.ArrayList;
import java.util.List;

import org.duanbn.salix.network.message.AckMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndPointFutureGroup {

    private static final Logger     LOG        = LoggerFactory.getLogger(EndPointFutureGroup.class);

    private List<EndPointFuture<?>> futureList = new ArrayList<EndPointFuture<?>>();

    @SuppressWarnings("unchecked")
    public <T> List<AckMessage<T>> sync() {
        List<AckMessage<T>> ackList = new ArrayList<AckMessage<T>>();

        for (EndPointFuture<?> future : futureList) {
            try {
                future.await();
            } catch (InterruptedException e) {
                LOG.warn("future {} interrupted may be lose message", future.getRequestId());
            }
            ackList.add((AckMessage<T>) future.getMessage());
        }

        return ackList;
    }

    public EndPointFutureGroup add(EndPointFuture<?> value) {
        synchronized (futureList) {
            this.futureList.add(value);
        }
        return this;
    }

    public List<EndPointFuture<?>> getFutures() {
        return this.futureList;
    }

    public void clear() {
        this.futureList.clear();
    }
}
