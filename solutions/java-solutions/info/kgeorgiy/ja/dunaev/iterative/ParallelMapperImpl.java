package info.kgeorgiy.ja.dunaev.iterative;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Class to parallelize map function on list.
 * Runs the provided function on each element in parallel, using the thread pool.
 *
 * @author Dunaev Kirill
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threadPool;
    private final ConcurrentQueue<Task<?>> taskQueue;
    private volatile boolean isClosed = false;

    /**
     * Creates class with a thread pool, containing provided number of threads.
     *
     * @param threads numbers of thread in the thread pool
     * @throws IllegalStateException if the number of thread is not positive
     */
    public ParallelMapperImpl(final int threads) {
        IterativeParallelism.assertPositive(threads);

        taskQueue = new ConcurrentQueue<>();
        final Runnable worker = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    taskQueue.poll().run();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        threadPool = Stream.generate(() -> worker).limit(threads).map(Thread::new).toList();
        threadPool.forEach(Thread::start);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the mapper has been closed
     */
    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> f, final List<? extends T> args) throws InterruptedException {
        assertOpen();

        final TasksManager<R> tasksManager = new TasksManager<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            final int index = i;
            taskQueue.add(new Task<>(
                    tasksManager,
                    () -> f.apply(args.get(index)),
                    index
            ));
        }

        List<R> results = tasksManager.waitResults();
        assertOpen();   // if the mapper has been closed, then the results is in undefined state
        return results;
    }

    @Override
    public void close() {
        if (isClosed) {
            return;
        }

        isClosed = true;
        threadPool.forEach(Thread::interrupt);
        try {
            IterativeParallelism.joinThreads(threadPool);
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            taskQueue.forEach(Task::done);
        }
    }

    private void assertOpen() {
        if (isClosed) {
            throw new IllegalStateException("Mapper is closed");
        }
    }
}
