package info.kgeorgiy.ja.dunaev.i18n.analyzer.stat;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to collect stats information.
 *
 * @param <T> state' subject
 * @author Dunaev Kirill
 */
public abstract class Stats<T> {
    private long occurrences = 0;
    protected double sum = 0;
    private final Set<T> uniques = new HashSet<>();

    private T minValue;
    private T maxValue;

    boolean first = true;

    private final Comparator<? super T> comparator;

    /**
     * Creates class with the given comparator for supporting min/max value.
     *
     * @param comparator the comparator
     */
    public Stats(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }

    protected abstract void addValue(T v);

    protected void processFirst(T v) {
    }

    protected void processNormal(T v) {
    }

    /**
     * Adds a new object to the stats.
     *
     * @param v an object to add
     */
    public void add(T v) {
        occurrences++;
        uniques.add(v);

        addValue(v);

        if (first) {
            minValue = v;
            maxValue = v;
            processFirst(v);
            first = false;
            return;
        }

        if (comparator.compare(minValue, v) > 0) {
            minValue = v;
        }

        if (comparator.compare(maxValue, v) < 0) {
            maxValue = v;
        }

        processNormal(v);
    }

    /**
     * Returns the number of all occurrences of all objects in the statistic.
     *
     * @return total occurrences
     */
    public long getOccurrences() {
        return occurrences;
    }

    /**
     * Returns the number of unique occurrences of objects in the statistic.
     *
     * @return unique occurrences
     */
    public long getUniques() {
        return uniques.size();
    }

    /**
     * Returns the minimum (by provided comparator) value of all objects added.
     *
     * @return the minimum value
     */
    public T getMinValue() {
        return minValue;
    }

    /**
     * Returns the maximum (by provided comparator) value of all objects added.
     *
     * @return the maximum value
     */
    public T getMaxValue() {
        return maxValue;
    }

    /**
     * Returns the average of all objects. The value of object itself should be specified by subclasses.
     *
     * @return the minimum value
     */
    public double getAverage() {
        return sum / occurrences;
    }
}
