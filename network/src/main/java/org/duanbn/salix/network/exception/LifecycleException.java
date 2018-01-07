package org.duanbn.salix.network.exception;

import org.duanbn.salix.network.Lifecycle;

public class LifecycleException extends Exception {

    private static final long serialVersionUID = -3023911779650799616L;

    private byte              state;

    public LifecycleException(String msg) {
        super(msg);
    }

    public LifecycleException(String msg, Throwable t) {
        super(msg, t);
    }

    public LifecycleException(Throwable t) {
        super(t);
    }

    public void setState(byte state) {
        this.state = state;
    }

    public byte getState() {
        return this.state;
    }

    public boolean isStartuping() {
        return this.state == Lifecycle.STARTING;
    }

    public boolean isRunning() {
        return this.state == Lifecycle.RUNNING;
    }

    public boolean isShutdowning() {
        return this.state == Lifecycle.SHUTDOWNING;
    }

    public boolean isShutdown() {
        return this.state == Lifecycle.SHUTDOWN;
    }

}
