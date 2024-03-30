package info.kgeorgiy.ja.dunaev.iterative;

/**
 * Represents an operation that accepts three input arguments and returns no result.
 * This is the three-arity specialization of {@link java.util.function.Consumer}.
 *
 * @param <T> type of the first argument
 * @param <U> type of the second argument
 * @param <K> type of the third argument
 * @author Dunaev Kirill
 * @see java.util.function.BiConsumer
 */
@FunctionalInterface
public interface TriConsumer<T, U, K> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param k the third input argument
     */
    void accept(T t, U u, K k);
}
