package exceptions;

/**
 * This exception is thrown to indicate that a product with a specific ID is not found.
 * It extends the RuntimeException class.
 */
public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(String message) {
        super(message);
    }
}
