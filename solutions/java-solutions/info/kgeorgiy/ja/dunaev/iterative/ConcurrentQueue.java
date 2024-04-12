package info.kgeorgiy.ja.dunaev.iterative;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Thread-safety queue. Note: queue has bounded size by {@value ConcurrentQueue#CAPACITY}.
 *
 * @param <T> type of the stored elements
 * @author Dunaev Kirill
 */
public class ConcurrentQueue<T> {
    private final Queue<T> storage;
    private static final int CAPACITY = 1 << 20;

    /**
     * Creates empty queue.
     */
    public ConcurrentQueue() {
        storage = new ArrayDeque<>(CAPACITY);
    }

    /**
     * Checks whether the queue is empty.
     * Warning: the method should not be used without the class locked, because its state can be changed
     * between the time when this method released lock and the time when programmer's code uses the result of it.
     *
     * @return {@code true} if the queue is empty, {@code false} otherwise
     */
    public synchronized boolean isEmpty() {
        return storage.isEmpty();
    }

    /**
     * Adds an element to the queue.
     * This is blocking operation: the method waits until there is an empty space in the queue.
     *
     * @param element an element to add
     * @throws InterruptedException if this thread is interrupted
     */
    public synchronized void add(T element) throws InterruptedException {
        while (storage.size() >= CAPACITY) {
            wait();
        }
        storage.add(element);
        notifyAll();
    }

    /**
     * Retrieves and removes the first element of the queue.
     * This is blocking operation: the method waits until the queue is not empty.
     *
     * @return the first element of the queue
     * @throws InterruptedException if this thread is interrupted
     */
    public synchronized T poll() throws InterruptedException {
        while (storage.isEmpty()) {
            wait();
        }
        T elem = storage.poll();
        notifyAll();
        return elem;
    }
}
