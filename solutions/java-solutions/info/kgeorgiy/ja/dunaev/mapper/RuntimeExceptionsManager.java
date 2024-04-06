package info.kgeorgiy.ja.dunaev.mapper;

/**
 * Manages runtime exceptions. Adds it suppressed to the first received one.
 *
 * @author Dunaev Kirill
 */
public class RuntimeExceptionsManager {
    private RuntimeException ex;

    /**
     * Creates manager with empty exception.
     */
    public RuntimeExceptionsManager() {
        ex = null;
    }

    /**
     * Adds an exception. If it's a first exception added, then class will store it.
     * Otherwise, the exception is added suppressed to the stored one.
     *
     * @param e an exception to add
     */
    public synchronized void addException(RuntimeException e) {
        if (ex == null) {
            ex = e;
        } else {
            ex.addSuppressed(e);
        }
    }

    /**
     * Returns the given element if no exception is stored. Throws the stored exception otherwise.
     *
     * @param element element to return
     * @param <R>     type of the element
     * @return the given element
     * @throws RuntimeException if the stored exception is not empty
     */
    public synchronized <R> R ifNoExceptions(R element) {
        if (ex != null) {
            throw ex;
        }
        return element;
    }
}
