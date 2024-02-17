package info.kgeorgiy.ja.dunaev.walk.exceptions;

public class UnsupportedHashAlgorithmException extends WalkException {
    public UnsupportedHashAlgorithmException(String message) {
        super(message);
    }

    public UnsupportedHashAlgorithmException(String message, Throwable cause) {
        super(message, cause);
    }
}
