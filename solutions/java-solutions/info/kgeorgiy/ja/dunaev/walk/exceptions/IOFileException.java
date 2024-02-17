package info.kgeorgiy.ja.dunaev.walk.exceptions;

public class IOFileException extends WalkException{
    public IOFileException(String message) {
        super(message);
    }

    public IOFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
