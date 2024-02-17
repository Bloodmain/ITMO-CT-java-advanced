package info.kgeorgiy.ja.dunaev.walk.exceptions;

public class WalkException extends Exception {
    public WalkException(String message) {
        super(message);
    }

    public WalkException(String message, Throwable cause) {
        super(message, cause);
    }
}
