package info.kgeorgiy.ja.dunaev.i18n.analyzer;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter.CurrencyFormatter;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Analyzer for currencies.
 *
 * @author Dunaev Kirill
 */
public class CurrencyAnalyzer extends NumbersAnalyzer {
    /**
     * Creates an analyzer with specified input and output locales.
     *
     * @param inputLocale  the input locale
     * @param outputLocale the output locale
     * @param outputBundle the output resource bundle (should be agreed with the output locale)
     */
    public CurrencyAnalyzer(Locale inputLocale, Locale outputLocale, ResourceBundle outputBundle) {
        super(inputLocale, outputLocale, outputBundle);
        this.formatter = new CurrencyFormatter(outputLocale, outputBundle, "Currency", stats);
        this.numberFormat = NumberFormat.getCurrencyInstance(inputLocale);
    }
}
