package info.kgeorgiy.ja.dunaev.i18n.analyzer;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter.TextFormatter;

import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Analyzer of words in the text.
 *
 * @author Dunaev Kirill
 */
public class WordsAnalyzer extends BreakIteratorAnalyzer {
    /**
     * Creates an analyzer with specified input and output locales.
     *
     * @param inputLocale  the input locale
     * @param outputLocale the output locale
     * @param outputBundle the output resource bundle (should be agreed with the output locale)
     */
    public WordsAnalyzer(Locale inputLocale, Locale outputLocale, ResourceBundle outputBundle) {
        super(inputLocale);
        this.breakIterator = BreakIterator.getWordInstance(inputLocale);
        this.formatter = new TextFormatter(outputLocale, outputBundle, "Word", stats);
    }

    @Override
    public List<String> getLocalizedStats(String text) {
        return collectStats(text, t -> t.codePoints().anyMatch(Character::isLetter));
    }
}
