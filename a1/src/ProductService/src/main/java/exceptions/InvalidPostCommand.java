package exceptions;

/**
 * This exception is thrown to indicate that an invalid POST command was received.
 * It extends the RuntimeException class.
 */
public class InvalidPostCommand extends RuntimeException {
    public InvalidPostCommand(String message) {
        super(message);
    }
}
