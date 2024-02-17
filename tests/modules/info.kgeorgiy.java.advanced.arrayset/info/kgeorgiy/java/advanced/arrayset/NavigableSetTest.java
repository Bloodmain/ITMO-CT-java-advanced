package info.kgeorgiy.java.advanced.arrayset;

import net.java.quickcheck.collection.Pair;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.NavigableSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * Tests for hard version
 * of <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#homework-arrayset">ArraySet</a> homework
 * for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NavigableSetTest extends SortedSetTest {
    public NavigableSetTest() {
    }

    @Test
    public void test32_ceiling() {
        testGetN("ceiling(%s)", NavigableSet::ceiling);
    }

    @Test
    public void test34_floor() {
        testGetN("floor(%s)", NavigableSet::floor);
    }


    @Test
    public void test35_navigableTailSet() {
        testGetN("tailSet(%s, true)", (s, e) -> s.tailSet(e, true));
        testGetN("tailSet(%s, false)", (s, e) -> s.tailSet(e, false));
    }

    @Test
    public void test24_navigableSubSet() {
        testPairsN((pair, from, to) -> IntStream.range(0, 4).forEach(i -> pair.testGet(
                String.format("subSet(%d, %b, %d, %b)", from, i % 2 == 1, to, i / 2 == 1),
                set -> set.subSet(from, i % 2 == 1, to, i / 2 == 1)
        )));
    }

    @Test
    public void test26_descendingSet() {
        final List<Integer> data = List.of(10, 20, 30);
        final NavigableSet<Integer> s = set(data, Integer::compareTo);
        final NavigableSet<Integer> set = s.descendingSet();
        assertEquals("toArray()", List.of(30, 20, 10), toArray(set));
        assertEquals("size()", 3, set.size());
        assertEquals("first()", 30, set.first().intValue());
        assertEquals("last()", 10, set.last().intValue());
        assertEquals("descendingIterator().next()", 10, set.descendingIterator().next().intValue());

        testGet("floor(%s)", set::floor, descendingPairs(10, 10, 20, 20, 30, 30, null));
        testGet("lower(%s)", set::lower, descendingPairs(10, 20, 20, 30, 30, null, null));
        testGet("ceiling(%s)", set::ceiling, descendingPairs(null, 10, 10, 20, 20, 30, 30));
        testGet("higher(%s)", set::higher, descendingPairs(null, null, 10, 10, 20, 20, 30));

        testGet("headSet(%s).size()", i -> set.headSet(i).size(), descendingPairs(3, 2, 2, 1, 1, 0, 0));
        testGet("tailSet(%s).size()", i -> set.tailSet(i).size(), descendingPairs(0, 1, 1, 2, 2, 3, 3));

        assertEquals("descendingSet().toArray()", data, toArray(set.descendingSet()));
    }

    private static List<Pair<Integer, Integer>> descendingPairs(final Integer v5, final Integer v10, final Integer v15, final Integer v20, final Integer v25, final Integer v30, final Integer v35) {
        return List.of(
                pair(5, v5),
                pair(10, v10),
                pair(15, v15),
                pair(20, v20),
                pair(25, v25),
                pair(30, v30),
                pair(35, v35)
        );
    }

    private static <T> void testGet(final String format, final Function<T, T> method, final List<Pair<T, T>> pairs) {
        for (final Pair<T, T> pair : pairs) {
            assertEquals(String.format(format, pair.getFirst()), pair.getSecond(), method.apply(pair.getFirst()));
        }
    }

    private static <T> Pair<T, T> pair(final T arg, final T result) {
        return new Pair<>(arg, result);
    }

    private void testPairsN(final TestPair<Integer, NavigableSet<Integer>> testCase) {
        testPairs(testCase);
    }

    private <R> void testGetN(final String name, final BiFunction<NavigableSet<Integer>, Integer, R> method) {
        testGet(name, method);
    }
}
