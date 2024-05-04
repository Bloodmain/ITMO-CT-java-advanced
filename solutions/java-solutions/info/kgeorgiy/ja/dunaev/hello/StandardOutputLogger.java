package info.kgeorgiy.ja.dunaev.hello;

/**
 * {@link Logger} that print all the data to standard output.
 *
 * @author Dunaev Kirill
 */
public class StandardOutputLogger implements Logger {
    private final String prefix;

    /**
     * Creates a logger. All the messages will contain the given string as a prefix.
     *
     * @param prefix a string to add to all messages
     */
    public StandardOutputLogger(String prefix) {
        this.prefix = prefix;
    }

    private String formatPrefix(String message) {
        return "%s | %s".formatted(prefix, message);
    }

    @Override
    public void info(String message) {
        System.out.println(formatPrefix(message));
    }

    @Override
    public void warn(String message) {
        System.err.println(formatPrefix(message));
    }

    @Override
    public void error(String ctx, Exception e) {
        System.err.println(formatPrefix(ctx + ": " + e.getMessage()));
    }
}
