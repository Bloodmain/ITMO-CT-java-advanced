package info.kgeorgiy.ja.dunaev.walk.exceptions;

public class BadArgumentException extends WalkException {
    public BadArgumentException(String message) {
        super(message);
    }

    public BadArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
