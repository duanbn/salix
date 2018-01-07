package org.duanbn.salix.network.message;

public class AckMessage<T> extends RemoteMessage {

    private static final long serialVersionUID = 1918977244983978282L;

    protected String          requestId;

    protected AckCode         code;

    protected String          message;

    protected T               data;

    public AckMessage() {

    }

    public AckMessage(String requestId) {
        this(requestId, AckCode.OK);
    }

    public AckMessage(String requestId, AckCode code) {
        this.requestId = requestId;
        this.code = code;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public AckCode getCode() {
        return code;
    }

    public void setCode(AckCode code) {
        this.code = code;
    }

    public String getMessage() {
        if (this.message != null && !this.message.trim().equals("")) {
            return this.message;
        }

        return "no message";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static enum AckCode {

        OK(200, "success"),
        NO_HANDLER(405, "no handler find"),
        ERROR(500, "remote exception");

        private int    value;
        private String message;

        private AckCode(int value, String message) {
            this.value = value;
            this.message = message;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }

}
