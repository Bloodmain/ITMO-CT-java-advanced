package info.kgeorgiy.ja.dunaev.i18n.analyzer;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter.TextFormatter;

import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Analyzer of sentences in the text.
 *
 * @author Dunaev Kirill
 */
public class SentencesAnalyzer extends BreakIteratorAnalyzer {
    /**
     * Creates an analyzer with specified input and output locales.
     *
     * @param inputLocale  the input locale
     * @param outputLocale the output locale
     * @param outputBundle the output resource bundle (should be agreed with the output locale)
     */
    public SentencesAnalyzer(Locale inputLocale, Locale outputLocale, ResourceBundle outputBundle) {
        super(inputLocale);
        this.breakIterator = BreakIterator.getSentenceInstance(inputLocale);
        this.formatter = new TextFormatter(outputLocale, outputBundle, "Sentence", stats);
    }

    @Override
    public List<String> getLocalizedStats(String text) {
        return collectStats(text, t -> true);
    }
}
