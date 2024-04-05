package info.kgeorgiy.ja.dunaev.iterative;

import info.kgeorgiy.java.advanced.iterative.AdvancedIP;

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
    @Override
    public <T> T maximum(final int threads,
                         final List<? extends T> values,
                         final Comparator<? super T> comparator,
                         final int step
    ) throws InterruptedException {
        return valueParallelTask(
                threads, step, values,
                v -> v.max(comparator).orElse(null),
                v -> v.max(comparator).orElse(null)
        );
    }

    @Override
    public <T> T minimum(final int threads,
                         final List<? extends T> values,
                         final Comparator<? super T> comparator,
                         final int step
    ) throws InterruptedException {
        return maximum(threads, values, comparator.reversed(), step);
    }

    @Override
    public <T> boolean all(final int threads,
                           List<? extends T> values,
                           final Predicate<? super T> predicate,
                           final int step
    ) throws InterruptedException {
        return valueParallelTask(
                threads, step, values,
                v -> v.allMatch(predicate),
                v -> v.reduce(true, Boolean::logicalAnd)
        );
    }

    @Override
    public <T> boolean any(final int threads,
                           List<? extends T> values,
                           final Predicate<? super T> predicate,
                           final int step
    ) throws InterruptedException {
        return !all(threads, values, predicate.negate(), step);
    }

    @Override
    public <T> int count(final int threads,
                         List<? extends T> values,
                         final Predicate<? super T> predicate,
                         final int step
    ) throws InterruptedException {
        return valueParallelTask(
                threads, step, values,
                v -> (int) v.filter(predicate).count(),
                v -> v.reduce(Integer::sum).orElse(0)
        );
    }

    @Override
    public String join(final int threads,
                       final List<?> values,
                       final int step
    ) throws InterruptedException {
        return valueParallelTask(
                threads, step, values,
                v -> v.map(Object::toString).collect(Collectors.joining()),
                v -> v.collect(Collectors.joining())
        );
    }

    @Override
    public <T> List<T> filter(final int threads,
                              final List<? extends T> values,
                              final Predicate<? super T> predicate,
                              final int step
    ) throws InterruptedException {
        return listParallelTask(threads, step, values, v -> v.filter(predicate));
    }

    @Override
    public <T, U> List<U> map(final int threads,
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
        Function<Stream<T>, T> reducer = getMonoidReducer(identity, operator);
        return valueParallelTask(threads, step, values, reducer, reducer);
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
        Function<Stream<R>, R> reducer = getMonoidReducer(identity, operator);
        return valueParallelTask(
                threads, step, values,
                v -> reducer.apply(v.map(lift)),
                reducer
        );
    }

    private <T> Function<Stream<T>, T> getMonoidReducer(T identity, BinaryOperator<T> operator) {
        return v -> v.reduce(identity, operator);
    }

    private <T, R, K extends T> R valueParallelTask(
            final int threads,
            final int step,
            final List<K> values,
            final Function<Stream<K>, ? extends R> function,
            final Function<Stream<R>, R> finisher
    ) throws InterruptedException {
        return parallelTask(
                threads, step, values, () -> null,
                (v, r, i) -> r.set(i, function.apply(v)),
                finisher
        );
    }

    private <T, R, K extends T> List<R> listParallelTask(
            final int threads,
            final int step,
            final List<K> values,
            final Function<Stream<K>, Stream<? extends R>> function
    ) throws InterruptedException {
        return this.<T, List<R>, K>parallelTask(
                threads, step, values, ArrayList::new,
                (v, r, i) -> function.apply(v).forEach(r.get(i)::add),
                v -> v.flatMap(List::stream).toList()
        );
    }

    private <T, R, K extends T> R parallelTask(
            final int threads,
            final int step,
            final List<K> values,
            final Supplier<R> defaultValue,
            final TriConsumer<Stream<K>, List<R>, Integer> processor,
            final Function<Stream<R>, R> finisher
    ) throws InterruptedException {
        final List<R> subListResult = new ArrayList<>();
        final List<Thread> threadList = new ArrayList<>();
        final int subListSize = (values.size() + threads - 1) / threads;

        for (int start = 0, i = 0; start < values.size(); ++i, start += subListSize) {
            startThread(values, subListResult, threadList,
                    defaultValue, processor,
                    start, i, Math.min(values.size(), start + subListSize), step);
        }
        join(threadList);
        return finisher.apply(subListResult.stream().filter(Objects::nonNull));
    }

    private <T, R, K extends T> void startThread(
            final List<K> values,
            final List<R> results,
            final List<Thread> threadList,
            final Supplier<R> defaultValue,
            final TriConsumer<Stream<K>, List<R>, Integer> processor,
            final int start, final int index, final int size, final int step
    ) {
        results.add(defaultValue.get());
        threadList.add(new Thread(() -> processor.accept(
                getNthIndices(start, size, step)
                        .mapToObj(values::get),
                results,
                index
        )));
        threadList.getLast().start();
    }

    private void join(List<Thread> threadList) throws InterruptedException {
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

    private void joinSuppress(Thread th, Exception e) {
        while (true) {
            if (!th.isAlive()) {
                break;
            }
            try {
                th.join();
                break;
            } catch (final InterruptedException e2) {
                e.addSuppressed(e2);
            }
        }
    }

    private IntStream getNthIndices(int from, int to, int n) {
        return IntStream.range(from, to)
                .filter(j -> j % n == 0);
    }
}

