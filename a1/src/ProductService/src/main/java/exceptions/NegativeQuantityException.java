package exceptions;

/**
 * This exception is thrown to indicate that a negative quantity value is not allowed.
 * It extends the RuntimeException class.
 */
public class NegativeQuantityException extends RuntimeException {
    public NegativeQuantityException(String message) {
        super(message);
    }
}
