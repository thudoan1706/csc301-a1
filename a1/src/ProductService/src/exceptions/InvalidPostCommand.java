package exceptions;

public class InvalidPostCommand extends RuntimeException {
    public InvalidPostCommand(String message) {
        super(message);
    }
}
