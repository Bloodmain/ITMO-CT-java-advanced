package info.kgeorgiy.ja.dunaev.mapper;

/**
 * Counter that allows to wait until it reaches zero.
 *
 * @author Dunaev Kirill
 */
public class WaitCounter {
    private int counter;

    /**
     * Creates counter with given value.
     *
     * @param value the value to store
     * @throws IllegalStateException if the value <= 0
     */
    public WaitCounter(int value) {
        this.counter = value;
        assertPositive();
    }

    /**
     * Thread-safety decrement the counter. If it reaches zero, calls notifyAll.
     *
     * @throws IllegalStateException if the counter is used when it has already reached zero
     */
    public synchronized void decrement() {
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
