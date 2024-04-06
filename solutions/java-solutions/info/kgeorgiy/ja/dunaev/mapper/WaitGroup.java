package info.kgeorgiy.ja.dunaev.mapper;

/**
 * Wait group that allows to wait until the provided counter reaches zero.
 *
 * @author Dunaev Kirill
 */
public class WaitGroup {
    private int counter;

    /**
     * Creates wait group with the given counter.
     *
     * @param counter the value to store
     * @throws IllegalStateException if the value <= 0
     */
    public WaitGroup(int counter) {
        this.counter = counter;
        assertPositive();
    }

    /**
     * Thread-safety decrement the counter. If it reaches zero, calls notifyAll.
     *
     * @throws IllegalStateException if the counter has already reached zero
     */
    public synchronized void done() {
        assertPositive();

        counter--;
        if (counter == 0) {
            notifyAll();
        }
    }

    /**
     * Waits for counter to reach zero.
     *
     * @throws InterruptedException if this thread is interrupted
     */
    public synchronized void waitForZero() throws InterruptedException {
        while (counter != 0) {
            wait();
        }
    }

    private void assertPositive() {
        if (counter <= 0) {
            throw new IllegalStateException("Using counter with non-positive value");
        }
    }
}
