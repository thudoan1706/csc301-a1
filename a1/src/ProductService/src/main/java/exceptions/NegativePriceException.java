package exceptions;

/**
 * This exception is thrown to indicate that a negative price value is not allowed.
 * It extends the RuntimeException class.
 */
public class NegativePriceException extends RuntimeException {
    public NegativePriceException(String message) {
        super(message);
    }
}
