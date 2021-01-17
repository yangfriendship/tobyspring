package springbook.user.exception;

public class SqlNotFountException extends RuntimeException {

    public SqlNotFountException() {
    }

    public SqlNotFountException(String message) {
        super(message);
    }
}
