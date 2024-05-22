package info.kgeorgiy.ja.dunaev.i18n.analyzer;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter.DateFormatter;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Stream;

/**
 * Analyzer for dates.
 *
 * @author Dunaev Kirill
 */
public class DateAnalyzer extends NumericAnalyzer {
    private final List<DateFormat> formats;

    /**
     * Creates an analyzer with specified input and output locales.
     *
     * @param inputLocale  the input locale
     * @param outputLocale the output locale
     * @param outputBundle the output resource bundle (should be agreed with the output locale)
     */
    public DateAnalyzer(Locale inputLocale, Locale outputLocale, ResourceBundle outputBundle) {
        this.formatter = new DateFormatter(outputLocale, outputBundle, "Date", stats);
        this.formats = Stream.of(DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG, DateFormat.FULL)
                .map(s -> DateFormat.getDateInstance(s, inputLocale))
                .toList();
    }

    @Override
    protected Number processPosition(String text, ParsePosition p) {
        for (DateFormat format : formats) {
            int old = p.getIndex();
            Date date = format.parse(text, p);
            if (old != p.getIndex()) {
                return date.getTime();
            }
        }
        return null;
    }
}
