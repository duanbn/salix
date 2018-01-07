package org.duanbn.salix.network;

public interface Lifecycle {

    public static final byte INIT        = (byte) 0;
    public static final byte STARTING    = (byte) 1;
    public static final byte RUNNING     = (byte) 2;
    public static final byte SHUTDOWNING = (byte) 3;
    public static final byte SHUTDOWN    = (byte) 4;

    byte getState();

    boolean isInit();

    void startuping();

    boolean isStartuping();

    void running();

    boolean isRunning();

    void shutdowning();

    boolean isShutdowning();

    void shutdown();

    boolean isShutdown();

}
