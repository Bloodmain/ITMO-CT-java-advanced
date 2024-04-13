package info.kgeorgiy.ja.dunaev.iterative;

import java.util.function.Supplier;

/**
 * Manages one task: evaluates the result, transfers all information (result/exception) to the {@link TasksManager}.
 *
 * @param <R> result type
 * @author Dunaev Kirill
 */
public class Task<R> {
    private final TasksManager<R> manager;
    private final Supplier<R> resultSupplier;
    private final int resultIndex;

    /**
     * Creates task.
     *
     * @param manager        manager to process results and exceptions
     * @param resultSupplier supplier to evaluate
     * @param resultIndex    index of the result in the manager
     */
    public Task(TasksManager<R> manager, Supplier<R> resultSupplier, int resultIndex) {
        this.resultSupplier = resultSupplier;
        this.manager = manager;
        this.resultIndex = resultIndex;
    }

    /**
     * Evaluates the result using supplier. Sets a result or an exception to the {@link #manager}.
     * Also calls {@link #done()}.
     */
    public void run() {
        try {
            manager.result(resultIndex, resultSupplier.get());
        } catch (final RuntimeException e) {
            manager.exception(e);
        } finally {
            done();
        }
    }

    /**
     * Informs the manager that the task is done by calling {@link TasksManager#done()}.
     */
    public void done() {
        manager.done();
    }
}
