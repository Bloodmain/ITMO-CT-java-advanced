package info.kgeorgiy.ja.dunaev.i18n.test;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.stat.NumberStats;
import info.kgeorgiy.ja.dunaev.i18n.analyzer.stat.Stats;
import info.kgeorgiy.ja.dunaev.i18n.analyzer.stat.TextStats;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

/**
 * Tests for classes that collect statistics.
 *
 * @author Dunaev Kirill
 */
public class StatsTests extends BaseTests{
    private <T> void checkStats(List<T> values, Stats<T> stats, ToDoubleFunction<T> map, Comparator<? super T> cmp) {
        Assertions.assertEquals(values.size(), stats.getOccurrences());
        Assertions.assertEquals(values.stream().distinct().count(), stats.getUniques());
        Assertions.assertEquals(values.stream().min(cmp).orElseThrow(), stats.getMinValue());
        Assertions.assertEquals(values.stream().max(cmp).orElseThrow(), stats.getMaxValue());
        Assertions.assertTrue(doubleEquals(values.stream().mapToDouble(map).sum() / values.size(), stats.getAverage()));
    }

    @Test
    public void test_defaultStats() {
        NumberStats stats = new NumberStats();

        Assertions.assertEquals(0, stats.getOccurrences());
        Assertions.assertEquals(0, stats.getUniques());
        Assertions.assertNull(stats.getMaxValue());
        Assertions.assertNull(stats.getMinValue());
    }

    private boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) < 1e-7;
    }

    @Test
    public void test_numberStats() {
        List<Double> values = RANDOM.doubles(10000).boxed().toList();
        NumberStats stats = new NumberStats();
        values.forEach(stats::add);
        checkStats(values, stats, Double::doubleValue, Double::compareTo);
    }

    private void testStrings(Comparator<? super String> comparator) {
        List<String> values = RANDOM.ints(100, 0, 100).mapToObj(this::getRandomString).toList();
        TextStats stats = new TextStats(comparator);
        values.forEach(stats::add);

        checkStats(values, stats, String::length, comparator);

        String minL = values.stream().min(Comparator.comparingLong(String::length)).orElseThrow();
        String maxL = values.stream().max(Comparator.comparingLong(String::length)).orElseThrow();
        Assertions.assertEquals(minL.length(), stats.getMinLength());
        Assertions.assertEquals(maxL.length(), stats.getMaxLength());
        Assertions.assertEquals(maxL, stats.getMaxLengthValue());
        Assertions.assertEquals(minL, stats.getMinLengthValue());
    }

    @Test
    public void test_stringStats() {
        testStrings(String::compareTo);
    }

    @Test
    public void test_stringStatsComparator() {
        testStrings(Comparator.comparing(String::length));
        testStrings(Comparator.comparing(s -> 0));
        testStrings(Collator.getInstance(Locale.CHINESE));
    }
}
