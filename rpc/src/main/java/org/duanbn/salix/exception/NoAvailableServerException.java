package org.duanbn.salix.exception;

/**
 * codec exception.
 *
 * @author duanbingnan
 */
public class NoAvailableServerException extends Exception {
    
    public NoAvailableServerException(String message) {
        super(message);
    }

    public NoAvailableServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoAvailableServerException(Throwable cause) {
        super(cause);
    }

}
