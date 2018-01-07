package org.duanbn.salix.network.message;

public class SendMessage<T> extends RemoteMessage {

    private static final long serialVersionUID = 6749874243813208927L;

    protected String          requestId;

    protected T               data;

    public SendMessage() {
    }

    public SendMessage(T data) {
        this.data = data;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getDataByClass(Class<T> clazz) {
        return (T) data;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public SendMessage<T> clone() {
        SendMessage<T> value = new SendMessage<T>(this.data);
        value.setRequestId(this.requestId);
        return value;
    }

}
