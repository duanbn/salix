package org.duanbn.salix.network.exception;

public class EndPointInternalException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 3810337403318330586L;

    public EndPointInternalException(String msg) {
        super(msg);
    }

    public EndPointInternalException(String msg, Throwable t) {
        super(msg, t);
    }

    public EndPointInternalException(Throwable t) {
        super(t);
    }

}
