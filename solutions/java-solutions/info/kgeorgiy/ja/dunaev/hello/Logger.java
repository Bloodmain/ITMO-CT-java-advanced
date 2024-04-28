package info.kgeorgiy.ja.dunaev.hello;

/**
 * Logger that should be used to log info/errors.
 *
 * @author Dunaev Kirill
 */
public interface Logger {
    /**
     * Logs an information.
     *
     * @param message an info to log
     */
    void info(String message);

    /**
     * Logs a warning.
     */
    void warn(String message);

    /**
     * Logs an error.
     *
     * @param ctx context of an error
     * @param e   the error to log
     */
    void error(String ctx, Exception e);
}
