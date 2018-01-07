package org.duanbn.salix.network.impl;

import java.util.concurrent.atomic.AtomicReference;

import org.duanbn.salix.network.Lifecycle;

public abstract class AbstractLifecycle implements Lifecycle {

    private AtomicReference<Byte> state = new AtomicReference<Byte>();

    public AbstractLifecycle() {
        this.state.set((byte) 0);
    }

    @Override
    public byte getState() {
        return this.state.get().byteValue();
    }

    @Override
    public boolean isInit() {
        return this.state.get().byteValue() == INIT;
    }

    @Override
    public void startuping() {
        this.state.set(STARTING);
    }

    @Override
    public boolean isStartuping() {
        return this.state.get().byteValue() == STARTING;
    }

    @Override
    public void running() {
        synchronized (this) {
            this.state.set(RUNNING);
        }
    }

    @Override
    public boolean isRunning() {
        return this.state.get().byteValue() == RUNNING;
    }

    @Override
    public void shutdowning() {
        synchronized (this) {
            this.state.set(SHUTDOWNING);
        }
    }

    @Override
    public boolean isShutdowning() {
        return this.state.get().byteValue() == SHUTDOWNING;
    }

    @Override
    public void shutdown() {
        synchronized (this) {
            this.state.set(SHUTDOWN);
        }
    }

    @Override
    public boolean isShutdown() {
        return this.state.get().byteValue() == SHUTDOWN;
    }

}
