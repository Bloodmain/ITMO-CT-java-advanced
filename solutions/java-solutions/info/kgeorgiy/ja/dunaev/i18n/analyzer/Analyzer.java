package info.kgeorgiy.ja.dunaev.i18n.analyzer;

import java.util.List;

/**
 * Represents an analyzer to analyze text and collect statistics.
 *
 * @author Dunaev Kirill
 */
public interface Analyzer {
    /**
     * Returns the formatted, localized list of all statistics.
     *
     * @param text the text to process
     * @return the statistics
     */
    List<String> getLocalizedStats(String text);

    /**
     * Returns the "total" statistic. For example, to use in {@link SummaryAnalyzer}.
     *
     * @return the "total" statistic
     */
    long getTotal();
}
