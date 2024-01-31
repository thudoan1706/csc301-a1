package exceptions;

/**
 * This exception is thrown to indicate that one or more required fields are missing
 * in the data provided. It extends the RuntimeException class.
 */
public class MissingRequiredFieldsException extends RuntimeException {
    public MissingRequiredFieldsException(String message) {
        super(message);
    }
}