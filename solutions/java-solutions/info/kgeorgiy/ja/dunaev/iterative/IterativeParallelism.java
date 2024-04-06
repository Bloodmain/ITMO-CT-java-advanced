package info.kgeorgiy.ja.dunaev.iterative;

import info.kgeorgiy.java.advanced.iterative.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Class to parallelize iterative processing of lists.
 * It splits the list into sequential blocks and processes them in parallel.
 * Each method has {@code threads} parameter which indicates the maximum number of these blocks.
 *
 * @author Dunaev Kirill
 */
public class IterativeParallelism implements AdvancedIP {
    private final ParallelMapper mapper;

    /**
     * Constructs class that will create threads itself.
     */
    public IterativeParallelism() {
        mapper = null;
    }

    /**
     * Constructs class that will parallelize list using given the mapper.
     * This class itself will not create new threads.
     *
     * @param mapper mapper that will be used
     */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T maximum(
            final int threads,
            final List<? extends T> values,
            final Comparator<? super T> comparator,
            final int step
    ) throws InterruptedException {
        Function<Stream<? extends T>, T> f = v -> v.max(comparator).orElse(null);
        return parallelTask(threads, step, values, f, f);
    }

    @Override
    public <T> T minimum(
            final int threads,
            final List<? extends T> values,
            final Comparator<? super T> comparator,
            final int step
    ) throws InterruptedException {
        return maximum(threads, values, comparator.reversed(), step);
    }

    @Override
    public <T> boolean all(
            final int threads,
            List<? extends T> values,
            final Predicate<? super T> predicate,
            final int step
    ) throws InterruptedException {
        return parallelTask(
                threads, step, values,
                v -> v.allMatch(predicate),
                v -> v.reduce(true, Boolean::logicalAnd)
        );
    }

    @Override
    public <T> boolean any(
            final int threads,
            List<? extends T> values,
            final Predicate<? super T> predicate,
            final int step
    ) throws InterruptedException {
        return !all(threads, values, predicate.negate(), step);
    }

    @Override
    public <T> int count(
            final int threads,
            List<? extends T> values,
            final Predicate<? super T> predicate,
            final int step
    ) throws InterruptedException {
        return parallelTask(
                threads, step, values,
                v -> (int) v.filter(predicate).count(),
                v -> v.reduce(Integer::sum).orElse(0)
        );
    }

    @Override
    public String join(
            final int threads,
            final List<?> values,
            final int step
    ) throws InterruptedException {
        Function<Stream<String>, String> joiner = v -> v.collect(Collectors.joining());
        return parallelTask(
                threads, step, values,
                v -> joiner.apply(v.map(Object::toString)),
                joiner
        );
    }

    @Override
    public <T> List<T> filter(
            final int threads,
            final List<? extends T> values,
            final Predicate<? super T> predicate,
            final int step
    ) throws InterruptedException {
        return listParallelTask(threads, step, values, v -> v.filter(predicate));
    }

    @Override
    public <T, U> List<U> map(
            final int threads,
            final List<? extends T> values,
            final Function<? super T, ? extends U> f,
            final int step
    ) throws InterruptedException {
        return listParallelTask(threads, step, values, v -> v.map(f));
    }

    @Override
    public <T> T reduce(
            int threads,
            List<T> values,
            T identity,
            BinaryOperator<T> operator,
            int step
    ) throws InterruptedException {
        return mapReduce(threads, values, Function.identity(), identity, operator, step);
    }

    @Override
    public <T, R> R mapReduce(
            int threads,
            List<T> values,
            Function<T, R> lift,
            R identity,
            BinaryOperator<R> operator,
            int step
    ) throws InterruptedException {
        Function<? super Stream<R>, R> reducer = v -> v.reduce(identity, operator);
        return parallelTask(
                threads, step, values,
                v -> reducer.apply(v.map(lift)),
                reducer
        );
    }

    private <T, R> List<R> listParallelTask(
            final int threads,
            final int step,
            final List<? extends T> values,
            final Function<? super Stream<? extends T>, Stream<? extends R>> function
    ) throws InterruptedException {
        return parallelTask(
                threads, step, values,
                v -> function.apply(v).collect(Collectors.<R, List<R>>toCollection(ArrayList::new)),
                v -> v.flatMap(List::stream).toList()
        );
    }

    private <T, R, K extends T> R parallelTask(
            final int threads,
            final int step,
            final List<K> values,
            final Function<? super Stream<K>, R> processor,
            final Function<? super Stream<R>, R> finisher
    ) throws InterruptedException {
        final int subListSize = (values.size() + threads - 1) / threads;
        List<Stream<K>> segments = new ArrayList<>();
        for (int start = 0; start < values.size(); start += subListSize) {
            segments.add(getNthIndices(start, Math.min(values.size(), start + subListSize), step)
                    .mapToObj(values::get)
            );
        }

        List<R> results = mapper != null ? mapper.map(processor, segments) : runInParallel(segments, processor);
        return finisher.apply(results.stream().filter(Objects::nonNull));
    }

    private static <T, R, K extends T> List<R> runInParallel(
            List<Stream<K>> segments,
            final Function<? super Stream<K>, R> processor
    ) throws InterruptedException {
        final List<R> subListResult = new ArrayList<>();
        final List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < segments.size(); ++i) {
            final int index = i;
            subListResult.add(null);
            threadList.add(new Thread(() -> subListResult.set(index, processor.apply(segments.get(index)))));
            threadList.getLast().start();
        }
        join(threadList);
        return subListResult;
    }

    /**
     * Joins the given threads. If thread is interrupted, non-joined threads are interrupted and joined again.
     * All of appeared {@link InterruptedException} are added suppressed to the first one.
     *
     * @param threadList thread to join
     * @throws InterruptedException if this thread is interrupted
     */
    public static void join(List<Thread> threadList) throws InterruptedException {
        int i = 0;
        try {
            for (; i < threadList.size(); ++i) {
                threadList.get(i).join();
            }
        } catch (final InterruptedException e) {
            List<Thread> toInterrupt = threadList.subList(i, threadList.size());
            toInterrupt.forEach(Thread::interrupt);
            toInterrupt.forEach(th -> joinSuppress(th, e));
            throw e;
        }
    }

    /**
     * Guaranteed to join thread. All of thrown {@link InterruptedException} are added suppressed to the given one.
     *
     * @param thread thread to join
     * @param e      exception, add new exceptions suppressed to which
     */
    public static void joinSuppress(Thread thread, Exception e) {
        while (true) {
            if (!thread.isAlive()) {
                break;
            }
            try {
                thread.join();
                break;
            } catch (final InterruptedException e2) {
                e.addSuppressed(e2);
            }
        }
    }

    /**
     * Returns each n-th elements of the int range [from; to).
     *
     * @param from start of the range (inclusive)
     * @param to   end of the range (exclusive)
     * @param n    step
     * @return each n-th elements of the range
     */
    public static IntStream getNthIndices(int from, int to, int n) {
        return IntStream.range(from, to)
                .filter(j -> j % n == 0);
    }
}

