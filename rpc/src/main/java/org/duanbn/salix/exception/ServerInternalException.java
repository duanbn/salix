package org.duanbn.salix.exception;

public class ServerInternalException extends RuntimeException {

	public ServerInternalException(String message) {
        super(message);
    }

    public ServerInternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerInternalException(Throwable cause) {
        super(cause);
    }
	
}
