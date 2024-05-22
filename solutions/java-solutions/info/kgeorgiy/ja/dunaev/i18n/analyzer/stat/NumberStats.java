package info.kgeorgiy.ja.dunaev.i18n.analyzer.stat;

/**
 * Class to collect stats of numbers.
 *
 * @author Dunaev Kirill
 */
public class NumberStats extends Stats<Double> {
    /**
     * Create a number statistic with default comparator on {@link Double}.
     */
    public NumberStats() {
        super(Double::compareTo);
    }

    @Override
    protected void addValue(Double v) {
        sum += v;
    }
}
