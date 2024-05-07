package info.kgeorgiy.ja.dunaev.bank.internal;

/**
 * Account's update exception.
 *
 * @author Dunaev Kirill
 */
public class AccountException extends Exception {
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public AccountException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public AccountException(String message, Throwable cause) {
        super(message, cause);
    }
}
