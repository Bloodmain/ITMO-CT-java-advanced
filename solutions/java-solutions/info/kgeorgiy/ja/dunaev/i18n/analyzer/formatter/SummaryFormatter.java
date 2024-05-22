package info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.Analyzer;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Formatter that formats summary of all analyzers.
 *
 * @author Dunaev Kirill
 */
public class SummaryFormatter extends AbstractFormatter {
    private final String inputFileName;

    /**
     * Creates a formatter with the given output locale, bundle, key to find in the bundle and the statistics.
     *
     * @param locale        the output locale
     * @param bundle        the output bundle (should be agreed with the output locale)
     * @param inputFileName the name of the input file
     */
    public SummaryFormatter(Locale locale, ResourceBundle bundle, String inputFileName) {
        super(bundle, locale);
        this.inputFileName = inputFileName;
    }

    /**
     * Returns the formatted title of the summary.
     *
     * @return the formatted title
     */
    public String title() {
        return format(
                get("FormatFile"),
                get("AnalyzingFile"),
                inputFileName
        );
    }

    /**
     * Returns the formatted header of the summary.
     *
     * @return the formatted header
     */
    public String header() {
        return format(get("SummaryHeader"));
    }

    /**
     * Returns the formatted totals of the summary.
     *
     * @return the formatted totals
     */
    public String totals(String key, Analyzer analyzer) {
        return formatTab(
                get("FormatStat"),
                get("NumberNominativeCapital"),
                get(key + "GenitivePlural"),
                analyzer.getTotal()
        );
    }
}
