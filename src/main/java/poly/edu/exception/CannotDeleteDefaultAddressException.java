package poly.edu.exception;

/**
 * Exception thrown when trying to delete the default address
 */
public class CannotDeleteDefaultAddressException extends RuntimeException {

    public CannotDeleteDefaultAddressException(String message) {
        super(message);
    }

    public CannotDeleteDefaultAddressException(String message, Throwable cause) {
        super(message, cause);
    }
}
