package org.duanbn.salix.network.impl;

public class EndPointConfig {

    private boolean tcpNodelay        = false;

    private boolean soKeepAlive       = true;

    private int     connectionTimeout = 60 * 1000;

    private int     sendBuf           = 1024 * 128;

    private int     receiveBuf        = 1024 * 128;

    private int     backLog           = 1024 * 128;

    private int     reconnectTime     = 5 * 1000;

    private int     sendRetry         = -1;

    private int     sendTimeout       = 60 * 1000;

    public boolean isTcpNodelay() {
        return tcpNodelay;
    }

    public EndPointConfig setTcpNodelay(boolean tcpNodelay) {
        this.tcpNodelay = tcpNodelay;
        return this;
    }

    public boolean isSoKeepAlive() {
        return soKeepAlive;
    }

    public EndPointConfig setSoKeepAlive(boolean soKeepAlive) {
        this.soKeepAlive = soKeepAlive;
        return this;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public EndPointConfig setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public int getSendBuf() {
        return sendBuf;
    }

    public EndPointConfig setSendBuf(int sendBuf) {
        this.sendBuf = sendBuf;
        return this;
    }

    public int getReceiveBuf() {
        return receiveBuf;
    }

    public EndPointConfig setReceiveBuf(int receiveBuf) {
        this.receiveBuf = receiveBuf;
        return this;
    }

    public int getBackLog() {
        return backLog;
    }

    public void setBackLog(int backLog) {
        this.backLog = backLog;
    }

    public int getReconnectTime() {
        return reconnectTime;
    }

    public EndPointConfig setReconnectTime(int reconnectTime) {
        this.reconnectTime = reconnectTime;
        return this;
    }

    public int getSendRetry() {
        return sendRetry;
    }

    public void setSendRetry(int sendRetry) {
        this.sendRetry = sendRetry;
    }

    public int getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

}
