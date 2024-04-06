package info.kgeorgiy.ja.dunaev.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Class to parallelize map function on list.
 * Runs the provided function on each element in parallel, using the thread pool.
 *
 * @author Dunaev Kirill
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threadPool;
    private final ConcurrentQueue<Runnable> taskQueue;

    /**
     * Creates class with a thread pool, containing provided number of threads.
     *
     * @param threads numbers of thread in the thread pool
     */
    public ParallelMapperImpl(int threads) {
        taskQueue = new ConcurrentQueue<>();
        Runnable threadTask = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    taskQueue.poll().run();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        threadPool = Stream.generate(() -> new Thread(threadTask)).limit(threads).toList();
        threadPool.forEach(Thread::start);
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        WaitGroup waitGroup = new WaitGroup(args.size());
        RuntimeExceptionsManager exceptionsManager = new RuntimeExceptionsManager();

        final List<R> result = new ArrayList<>(Collections.nCopies(args.size(), null));
        IntStream.range(0, args.size())
                .forEach(i -> taskQueue.add(() -> {
                            try {
                                result.set(i, f.apply(args.get(i)));
                            } catch (final RuntimeException e) {
                                exceptionsManager.addException(e);
                            }
                            waitGroup.done();
                        }
                ));

        waitGroup.waitForZero();
        return exceptionsManager.ifNoExceptions(result);
    }

    @Override
    public void close() {
        threadPool.forEach(Thread::interrupt);
        try {
            IterativeParallelism.joinThreads(threadPool);
        } catch (final InterruptedException ignored) {
        }
    }
}
