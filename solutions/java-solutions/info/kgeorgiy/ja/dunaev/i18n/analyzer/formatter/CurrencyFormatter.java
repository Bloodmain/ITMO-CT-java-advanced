package info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.stat.Stats;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Formatter that formats statistic of currencies to the given locale.
 *
 * @author Dunaev Kirill
 */
public class CurrencyFormatter extends FeminineNumberFormatter {
    /**
     * Creates a formatter with the given output locale, bundle, key to find in the bundle and the statistics.
     *
     * @param locale the output locale
     * @param bundle the output bundle (should be agreed with the output locale)
     * @param key    a key to find in bundle
     * @param stats  the statistics
     */
    public CurrencyFormatter(Locale locale, ResourceBundle bundle, String key, Stats<?> stats) {
        super(locale, bundle, key, stats);
    }

    @Override
    protected String averageFormat() {
        return "FormatCurrencyAverage";
    }

    @Override
    protected String valueFormat() {
        return "FormatStatCurrency";
    }
}
