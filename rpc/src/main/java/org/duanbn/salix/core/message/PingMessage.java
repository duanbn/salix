package org.duanbn.salix.core.message;

public class PingMessage extends Message {

    private long time;

    private int elapse;
    
    public long getTime() {
        return time;
    }
    
    public void setTime(long time) {
        this.time = time;
    }
    
    public int getElapse() {
        return elapse;
    }
    
    public void setElapse(int elapse) {
        this.elapse = elapse;
    }
}
