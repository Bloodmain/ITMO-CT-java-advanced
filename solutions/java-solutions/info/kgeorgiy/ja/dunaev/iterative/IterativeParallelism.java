package info.kgeorgiy.ja.dunaev.iterative;

import info.kgeorgiy.java.advanced.iterative.ListIP;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to parallelize iterative processing of lists.
 * It splits the list into sequential blocks and processes them in parallel.
 * Each method has {@code threads} parameter which indicates the maximum number of these blocks.
 *
 * @author Dunaev Kirill
 */
public class IterativeParallelism implements ListIP {
    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return valueParallelTask(
                threads, values,
                v -> v.max(comparator).orElseThrow(),
                v -> v.max(comparator).orElse(null)
        );
    }

    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(final int threads, List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return valueParallelTask(
                threads, values,
                v -> v.allMatch(predicate),
                v -> v.reduce(true, Boolean::logicalAnd)
        );
    }

    @Override
    public <T> boolean any(final int threads, List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    @Override
    public <T> int count(final int threads, List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return valueParallelTask(
                threads, values,
                v -> (int) v.filter(predicate).count(),
                v -> v.reduce(Integer::sum).orElse(0)
        );
    }

    @Override
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return valueParallelTask(
                threads, values,
                v -> v.map(Object::toString).collect(Collectors.joining()),
                v -> v.collect(Collectors.joining())
        );
    }

    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return listParallelTask(threads, values, v -> v.filter(predicate));
    }

    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f) throws InterruptedException {
        return listParallelTask(threads, values, v -> v.map(f));
    }

    private <T, K extends T, R> R valueParallelTask(
            final int threads,
            final List<K> values,
            final Function<Stream<K>, ? extends R> function,
            final Function<Stream<R>, R> finisher
    ) throws InterruptedException {
        return parallelTask(
                threads, values, () -> null,
                (v, r, i) -> r.set(i, function.apply(v)),
                finisher
        );
    }

    private <T, R, K extends T> List<R> listParallelTask(
            final int threads,
            final List<K> values,
            final Function<Stream<K>, Stream<? extends R>> function
    ) throws InterruptedException {
        return parallelTask(
                threads, values, ArrayList::new,
                (v, r, i) -> function.apply(v).forEach(r.get(i)::add),
                v -> v.flatMap(List::stream).collect(Collectors.toCollection(ArrayList::new))
        );
    }

    private <T, R, K extends T> R parallelTask(
            final int threads,
            final List<K> values,
            final Supplier<R> defaultValue,
            final TriConsumer<Stream<K>, List<R>, Integer> processor,
            final Function<Stream<R>, R> finisher
    ) throws InterruptedException {

        final List<R> subListResult = new ArrayList<>();
        final List<Thread> threadList = new ArrayList<>();
        final int subListSize = (values.size() + threads - 1) / threads;

        for (int start = 0, i = 0; start < values.size(); ++i, start += subListSize) {
            final int subListIndex = i;
            final int subListStart = start;
            subListResult.add(defaultValue.get());

            threadList.add(new Thread(() ->
                    processor.accept(
                            values.subList(subListStart, Math.min(values.size(), subListStart + subListSize)).stream(),
                            subListResult,
                            subListIndex
                    )
            ));
            threadList.getLast().start();
        }
        for (final Thread th : threadList) {
            th.join();
        }

        return finisher.apply(subListResult.stream());
    }
}
