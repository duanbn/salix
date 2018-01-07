package org.duanbn.salix.network.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.duanbn.salix.network.EndPoint;
import org.duanbn.salix.network.EndPointFactory;
import org.duanbn.salix.network.EndPointFuture;
import org.duanbn.salix.network.EndPointFutureGroup;
import org.duanbn.salix.network.EndPointGroup;
import org.duanbn.salix.network.message.AckMessage;
import org.duanbn.salix.network.message.SendMessage;

public class NettyEndPointGroup extends AbstractLifecycle implements EndPointGroup {

    private Lock                  lock         = new ReentrantLock();
    private Map<String, EndPoint> avalilable   = new HashMap<String, EndPoint>();
    private Map<String, EndPoint> unavalilable = new HashMap<String, EndPoint>();

    private Random                random       = new Random();

    private EndPointFactory       remoteFactory;

    private String                id;

    public NettyEndPointGroup(EndPointFactory remoteFactory) {
        this.remoteFactory = remoteFactory;

        this.id = UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public <T> EndPointFutureGroup send(SendMessage<?> msg) {
        EndPointFutureGroup futureGroup = new EndPointFutureGroup();

        EndPointFuture<? extends AckMessage<T>> callFuture = null;
        for (EndPoint endpoint : avalilable.values()) {
            callFuture = endpoint.asyncSend(msg);
            futureGroup.add(callFuture);
        }

        return futureGroup;
    }

    @Override
    public void add(List<InetSocketAddress> address) {
        for (InetSocketAddress a : address) {
            add(a.getHostName(), a.getPort());
        }
    }

    @Override
    public void add(String host, int port) {
        EndPoint endPoint = this.remoteFactory.createEndPoint(host, port);
        add(endPoint);
    }

    @Override
    public void add(EndPoint endPoint) {
        if (endPoint.isRunning()) {
            setAvalilable(endPoint);
        } else {
            setUnavalilable(endPoint);
        }

        endPoint.setGroup(this);
    }

    @Override
    public EndPoint remove(String endPointId) {
        EndPoint endpoint = null;

        if (this.avalilable.containsKey(endPointId)) {
            endpoint = this.avalilable.remove(endPointId);
        }

        if (endPointId == null && this.unavalilable.containsKey(endPointId)) {
            endpoint = this.unavalilable.remove(endPointId);
        }

        endpoint.setGroup(null);

        return endpoint;
    }

    @Override
    public EndPoint get(String endPointId) {
        return this.avalilable.get(endPointId);
    }

    @Override
    public EndPoint choice() {
        String[] endPointIds = this.avalilable.keySet().toArray(new String[0]);

        String endPointId = endPointIds[random.nextInt(endPointIds.length)];

        return get(endPointId);
    }

    @Override
    public List<EndPoint> avalilableList() {
        List<EndPoint> result = new ArrayList<EndPoint>();
        result.addAll(this.avalilable.values());
        return result;
    }

    @Override
    public void closeAll() {
        for (EndPoint endpoint : avalilable.values()) {
            endpoint.close();

            setUnavalilable(endpoint);
        }
    }

    public void setAvalilable(EndPoint endpoint) {
        this.lock.lock();

        try {
            this.avalilable.put(endpoint.getId(), endpoint);
            this.unavalilable.remove(endpoint.getId());
        } finally {
            this.lock.unlock();
        }
    }

    public void setUnavalilable(EndPoint endpoint) {
        this.lock.lock();

        try {
            this.unavalilable.put(endpoint.getId(), endpoint);
            this.avalilable.remove(endpoint.getId());
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public EndPointFactory getRemoteFactory() {
        return this.remoteFactory;
    }
}
