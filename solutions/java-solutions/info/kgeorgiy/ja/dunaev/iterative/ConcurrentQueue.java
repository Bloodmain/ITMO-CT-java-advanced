package info.kgeorgiy.ja.dunaev.iterative;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Thread-safety queue. Note: queue has limited by {@value ConcurrentQueue#CAPACITY} size.
 *
 * @param <T> type of the stored elements
 * @author Dunaev Kirill
 */
public class ConcurrentQueue<T> {
    private final Queue<T> storage = new ArrayDeque<>(CAPACITY);
    private static final int CAPACITY = 1 << 20;

    /**
     * Creates empty queue.
     */
    public ConcurrentQueue() {
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
        final T elem = storage.poll();
        notifyAll();
        return elem;
    }

    /**
     * Applies the given consumer to all elements in the queue.
     *
     * @param consumer the consumer to apply
     */
    public synchronized void forEach(Consumer<T> consumer) {
        storage.forEach(consumer);
    }
}
