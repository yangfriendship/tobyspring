package springbook.user.exception;

public class SqlUpdateFailureException extends RuntimeException {

    public SqlUpdateFailureException() {
    }

    public SqlUpdateFailureException(String message) {
        super(message);
    }
}
