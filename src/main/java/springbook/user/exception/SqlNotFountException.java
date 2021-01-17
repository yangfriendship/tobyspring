package springbook.user.exception;

public class SqlNotFountException extends RuntimeException {

    public SqlNotFountException() {
    }

    public SqlNotFountException(String message) {
        super(message + "에 해당하는 Sql을 찾을 수 없습니다.");
    }
}
