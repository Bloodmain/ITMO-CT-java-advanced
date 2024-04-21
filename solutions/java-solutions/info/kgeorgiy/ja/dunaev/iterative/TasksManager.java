package info.kgeorgiy.ja.dunaev.iterative;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the {@link Task} and their results.
 * Stores tasks' results, occurred exceptions and the tasks counter, which indicates how many tasks are not done yet.
 *
 * @param <R> result type
 * @author Dunaev Kirill
 */
public class TasksManager<R> {
    private final List<R> results;
    private int counter;
    private RuntimeException ex;

    /**
     * Creates manager with the given number of tasks to manage.
     *
     * @param size number of tasks
     * @throws IllegalArgumentException if the provided size is negative
     */
    public TasksManager(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Task size must be non-negative");
        }

        results = new ArrayList<>(Collections.nCopies(size, null));
        counter = size;
    }

    /**
     * Saves the result of the task with the given index.
     *
     * @param index index of the task
     * @param value result
     */
    public synchronized void result(int index, R value) {
        results.set(index, value);
    }

    /**
     * Adds an exception. If it's a first exception added, then class will store it.
     * Otherwise, the exception is added suppressed to the stored one.
     *
     * @param e an exception to add
     */
    public synchronized void exception(RuntimeException e) {
        if (ex == null) {
            ex = e;
        } else {
            ex.addSuppressed(e);
        }
    }

    /**
     * Decrements the task counter. If it reaches zero, calls notify.
     *
     * @throws IllegalStateException if the counter has already reached zero
     */
    public synchronized void done() {
        if (counter <= 0) {
            throw new IllegalStateException("Done with non-positive counter");
        }

        --counter;
        if (counter == 0) {
            notify();
        }
    }

    /**
     * Waits until tasks counter reaches zero and returns the results or throws the stored exception.
     *
     * @return tasks' results
     * @throws InterruptedException if this thread is interrupted
     * @throws RuntimeException     if there is an exception stored
     */
    public synchronized List<R> waitResults() throws InterruptedException {
        while (counter > 0) {
            wait();
        }
        if (ex != null) {
            throw ex;
        }
        return results;
    }
}
