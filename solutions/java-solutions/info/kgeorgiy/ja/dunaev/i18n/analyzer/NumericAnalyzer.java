package info.kgeorgiy.ja.dunaev.i18n.analyzer;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter.NumberFormatter;
import info.kgeorgiy.ja.dunaev.i18n.analyzer.stat.NumberStats;
import info.kgeorgiy.ja.dunaev.i18n.analyzer.stat.Stats;

import java.text.ParsePosition;
import java.util.List;

/**
 * Analyzer for numbers that gives an opportunity to customize how to parse a text at the given position.
 *
 * @author Dunaev Kirill
 */
public abstract class NumericAnalyzer implements Analyzer {
    protected Stats<Double> stats;
    protected NumberFormatter formatter;

    protected NumericAnalyzer() {
        this.stats = new NumberStats();
    }

    @Override
    public List<String> getLocalizedStats(String text) {
        ParsePosition p = new ParsePosition(0);
        while (p.getIndex() < text.length()) {
            int old = p.getIndex();
            Number number = processPosition(text, p);
            if (old != p.getIndex()) {
                stats.add(number.doubleValue());
            } else {
                p.setIndex(p.getIndex() + 1);
            }
        }

        return localizeStats();
    }

    protected abstract Number processPosition(String text, ParsePosition p);

    private List<String> localizeStats() {
        return List.of(
                formatter.header(),
                formatter.total(),
                formatter.minValue(),
                formatter.maxValue(),
                formatter.average()
        );
    }

    @Override
    public long getTotal() {
        return stats.getOccurrences();
    }
}
