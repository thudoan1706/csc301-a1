package exceptions;

/**
 * This exception is thrown to indicate an error during the serialization of a product to JSON format.
 * It extends the RuntimeException class and includes the original cause of the exception.
 */
public class ProductSerializationException extends RuntimeException {
    public ProductSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
