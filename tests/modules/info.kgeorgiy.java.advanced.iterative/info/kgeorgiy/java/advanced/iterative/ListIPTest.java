package info.kgeorgiy.java.advanced.iterative;

import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

/**
 * Basic tests for hard version
 * of <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#homework-concurrent">Iterative parallelism</a> homework
 * for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class ListIPTest<T extends ListIP> extends ScalarIPTest<T> {
    public ListIPTest() {
    }

    @Test
    public void test51_join() throws InterruptedException {
        testS(
                (data, ignore) -> data.map(Object::toString).collect(Collectors.joining()),
                (i, t, d, v) -> i.join(t, d),
                UNIT
        );
    }

    @Test
    public void test52_filter() throws InterruptedException {
        testS(
                (data, predicate) -> data.filter(predicate).toList(),
                ListIP::filter,
                PREDICATES
        );
    }
}
