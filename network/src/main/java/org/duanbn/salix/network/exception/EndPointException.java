package org.duanbn.salix.network.exception;

public class EndPointException extends Exception {

    private static final long serialVersionUID = -8014030155390735679L;

    protected String          requestId;

    public EndPointException(String requestId, String msg) {
        super(msg);
        this.requestId = requestId;
    }

    public EndPointException(String requestId, String msg, Throwable t) {
        super(msg, t);
        this.requestId = requestId;
    }

    public EndPointException(String requestId, Throwable t) {
        super(t);
        this.requestId = requestId;
    }

    public String getReqeustId() {
        return this.requestId;
    }

}
