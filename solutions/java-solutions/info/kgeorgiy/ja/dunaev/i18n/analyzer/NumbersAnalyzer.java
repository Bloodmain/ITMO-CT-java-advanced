package info.kgeorgiy.ja.dunaev.i18n.analyzer;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter.NumberFormatter;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Analyzer for numbers.
 *
 * @author Dunaev Kirill
 */
public class NumbersAnalyzer extends NumericAnalyzer {
    protected NumberFormat numberFormat;

    /**
     * Creates an analyzer with specified input and output locales.
     *
     * @param inputLocale  the input locale
     * @param outputLocale the output locale
     * @param outputBundle the output resource bundle (should be agreed with the output locale)
     */
    public NumbersAnalyzer(Locale inputLocale, Locale outputLocale, ResourceBundle outputBundle) {
        this.formatter = new NumberFormatter(outputLocale, outputBundle, "Number", stats);
        this.numberFormat = NumberFormat.getNumberInstance(inputLocale);
    }

    @Override
    protected Number processPosition(String text, ParsePosition p) {
        return numberFormat.parse(text, p);
    }
}
