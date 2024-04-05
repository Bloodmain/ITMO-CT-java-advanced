package info.kgeorgiy.java.advanced.iterative;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Full tests for easy version
 * of <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#homework-concurrent">Iterative parallelism</a> homework
 * for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class NewScalarIPTest<P extends NewScalarIP> extends ScalarIPTest<P> {
    protected final int[] steps = {1, 3, 5, 1000_000};

    public NewScalarIPTest() {
    }

    @Test
    public void test91_stepMaximum() throws InterruptedException {
        testStep(Collections::max, NewScalarIP::maximum, COMPARATORS);
    }

    @Test
    public void test92_stepMinimum() throws InterruptedException {
        testStep(Collections::min, NewScalarIP::minimum, COMPARATORS);
    }

    @Test
    public void test93_stepAll() throws InterruptedException {
        testStepS(Stream::allMatch, NewScalarIP::all, PREDICATES);
    }

    @Test
    public void test94_stepAny() throws InterruptedException {
        testStepS(Stream::anyMatch, NewScalarIP::any, PREDICATES);
    }

    @Test
    public void test95_stepCount() throws InterruptedException {
        testStepS((data, value) -> (int) data.filter(value).count(), NewScalarIP::count, PREDICATES);
    }

    protected final <T, U> void testStep(
            final BiFunction<List<Integer>, U, T> fExpected,
            final StepFunction<P, T, U> fActual,
            final List<Named<U>> cases
    ) throws InterruptedException {
        for (final int step : steps) {
            try {
                test(
                        (data, value) -> fExpected.apply(nth(data, step), value),
                        (instance, threads, data, value) -> fActual.apply(instance, threads, data, value, step),
                        cases
                );
            } catch (final AssertionError e) {
                throw new AssertionError("Step " + step + ": " + e.getMessage(), e);
            }
        }
    }

    protected final <T, U> void testStepS(
            final BiFunction<Stream<Integer>, U, T> fExpected,
            final StepFunction<P, T, U> fActual,
            final List<Named<U>> cases
    ) throws InterruptedException {
        testStep((data, value) -> fExpected.apply(data.stream(), value), fActual, cases);
    }

    protected static <T> List<T> nth(final List<T> items, final int step) {
        return IntStream.iterate(0, i -> i < items.size(), i -> i + step).mapToObj(items::get).toList();
    }

    public interface StepFunction<P, T, U> {
        T apply(P instance, int threads, List<Integer> data, U value, int step) throws InterruptedException;
    }
}
