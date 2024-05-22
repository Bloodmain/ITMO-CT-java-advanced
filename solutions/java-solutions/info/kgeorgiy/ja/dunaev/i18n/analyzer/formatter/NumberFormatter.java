package info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.stat.Stats;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Formatter that formats statistic of numbers to the given locale.
 *
 * @author Dunaev Kirill
 */
public class NumberFormatter extends AbstractFormatter {
    protected final String key;
    protected final Stats<?> stats;

    /**
     * Creates a formatter with the given output locale, bundle, key to find in the bundle and the statistics.
     *
     * @param locale the output locale
     * @param bundle the output bundle (should be agreed with the output locale)
     * @param key    a key to find in bundle
     * @param stats  the statistics
     */
    public NumberFormatter(Locale locale, ResourceBundle bundle, String key, Stats<?> stats) {
        super(bundle, locale);
        this.key = key;
        this.stats = stats;
    }

    protected String addKey(String r) {
        return key + r;
    }

    /**
     * Returns the formatted header of the statistics.
     *
     * @return the formatted header
     */
    public String header() {
        return format(
                get("FormatHeader"),
                get("Statistic"),
                get("StatisticPreposition"),
                get(addKey("DativePlural"))
        );
    }

    /**
     * Returns the formatted "total" statistic.
     *
     * @return the formatted "total" statistic
     */
    public String total() {
        long uniques = stats.getUniques();
        return formatTab(
                get("FormatWords"),
                get("Number"),
                get(addKey("GenitivePlural")),
                stats.getOccurrences(),
                uniques,
                get(uniques % 10 == 1 && uniques % 100 != 11 ? "UniqueOne" : "UniqueMany")
        );
    }

    protected String valueFormat() {
        return "FormatStat";
    }

    protected String formatValue(String cmp, Object value) {
        return formatTab(
                get(valueFormat()),
                get(cmp),
                get(addKey("Nominative")),
                value
        );
    }

    /**
     * Returns the formatted "minimum value" statistic.
     *
     * @return the formatted "minimum value" statistic
     */
    public String minValue() {
        return formatValue("MinNeuter", stats.getMinValue());
    }

    /**
     * Returns the formatted "maximum value" statistic.
     *
     * @return the formatted "maximum value" statistic
     */
    public String maxValue() {
        return formatValue("MaxNeuter", stats.getMaxValue());
    }

    /**
     * Returns the formatted "average value" statistic.
     *
     * @return the formatted "average value" statistic
     */
    public String average() {
        return formatTab(
                get("FormatNumberAverage"),
                get("AverageNeuter"),
                get(addKey("Nominative")),
                stats.getAverage()
        );
    }
}
