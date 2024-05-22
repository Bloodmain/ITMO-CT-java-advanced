package info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.stat.TextStats;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Formatter that formats statistic of text to the given locale.
 *
 * @author Dunaev Kirill
 */
public class TextFormatter extends NumberFormatter {
    private final TextStats stats;

    /**
     * Creates a formatter with the given output locale, bundle, key to find in the bundle and the statistics.
     *
     * @param locale the output locale
     * @param bundle the output bundle (should be agreed with the output locale)
     * @param key    a key to find in bundle
     * @param stats  the statistics
     */
    public TextFormatter(Locale locale, ResourceBundle bundle, String key, TextStats stats) {
        super(locale, bundle, key, stats);
        this.stats = stats;
    }

    private String formatLength(String cmp, Object length, Object value) {
        return formatTab(
                get("FormatLength"),
                get(cmp),
                get("Length"),
                get(addKey("Genitive")),
                length,
                value
        );
    }

    /**
     * Returns the formatted "minimum length" statistic.
     *
     * @return the formatted "minimum length" statistic
     */
    public String minLength() {
        return formatLength("MinFeminine", stats.getMinLength(), stats.getMinLengthValue());
    }

    /**
     * Returns the formatted "maximum length" statistic.
     *
     * @return the formatted "maximum length" statistic
     */
    public String maxLength() {
        return formatLength("MaxFeminine", stats.getMaxLength(), stats.getMaxLengthValue());
    }

    @Override
    public String average() {
        return formatTab(
                get("FormatAverage"),
                get("AverageFeminine"),
                get("Length"),
                get(addKey("Genitive")),
                stats.getAverage()
        );
    }

    @Override
    protected String valueFormat() {
        return "FormatStatQuoted";
    }
}
