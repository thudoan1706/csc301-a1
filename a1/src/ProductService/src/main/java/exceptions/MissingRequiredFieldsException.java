package exceptions;

public class MissingRequiredFieldsException extends RuntimeException {
    public MissingRequiredFieldsException(String message) {
        super(message);
    }
}