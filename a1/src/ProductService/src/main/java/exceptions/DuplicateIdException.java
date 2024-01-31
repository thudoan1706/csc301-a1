package exceptions;

/**
 * The {@code DuplicateIdException} class is an exception that indicates an attempt
 * to create a new product with an ID that already exists in the system.
 */
public class DuplicateIdException extends RuntimeException {
    public DuplicateIdException(String message) {
        super(message);
    }
}
