package info.kgeorgiy.ja.dunaev.i18n.analyzer;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter.TextFormatter;
import info.kgeorgiy.ja.dunaev.i18n.analyzer.stat.TextStats;

import java.text.BreakIterator;
import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Analyzer that splits text using {@link BreakIterator}.
 *
 * @author Dunaev Kirill
 */
public abstract class BreakIteratorAnalyzer implements Analyzer {
    protected BreakIterator breakIterator;
    protected TextStats stats;
    protected TextFormatter formatter;

    protected BreakIteratorAnalyzer(Locale inputLocale) {
        this.stats = new TextStats(Collator.getInstance(inputLocale));
    }

    protected List<String> collectStats(String text, Predicate<String> checker) {
        breakIterator.setText(text);
        int begin = breakIterator.first();
        int end = breakIterator.next();

        while (end != BreakIterator.DONE) {
            String token = text.substring(begin, end).strip();
            if (checker.test(token)) {
                stats.add(token);
            }
            begin = end;
            end = breakIterator.next();
        }

        return localizeStats();
    }

    private List<String> localizeStats() {
        return List.of(
                formatter.header(),
                formatter.total(),
                formatter.minValue(),
                formatter.maxValue(),
                formatter.minLength(),
                formatter.maxLength(),
                formatter.average()
        );
    }

    @Override
    public long getTotal() {
        return stats.getOccurrences();
    }
}
