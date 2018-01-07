package org.duanbn.salix.network.exception;

public class TimeoutException extends Exception {

    private static final long serialVersionUID = -8014030155390735679L;

    private String            requestId;

    private Class<?>          sendMessageClass;

    public TimeoutException(String msg) {
        super(msg);
    }

    public TimeoutException(Throwable t) {
        super(t);
    }

    public TimeoutException(String msg, Throwable t) {
        super(msg, t);
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getReqeustId() {
        return this.requestId;
    }

    public Class<?> getSendMessageClass() {
        return sendMessageClass;
    }

    public void setSendMessageClass(Class<?> sendMessageClass) {
        this.sendMessageClass = sendMessageClass;
    }

}
