package exceptions;

public class ProductSerializationException extends RuntimeException {
    public ProductSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
