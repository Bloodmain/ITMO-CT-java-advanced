package info.kgeorgiy.ja.dunaev.i18n.analyzer;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter.SummaryFormatter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Summary analyzer.
 *
 * @author Dunaev Kirill
 */
public class SummaryAnalyzer {
    private final Locale inputLocale;
    private final Locale outputLocale;
    private final ResourceBundle outputBundle;
    private final String inputFileName;

    /**
     * Creates an analyzer with specified input and output locales and the name of an input file.
     *
     * @param inputLocale   the input locale
     * @param outputLocale  the output locale
     * @param outputBundle  the output resource bundle (should be agreed with the output locale)
     * @param inputFileName the name of the input file
     */
    public SummaryAnalyzer(Locale inputLocale, Locale outputLocale, ResourceBundle outputBundle, String inputFileName) {
        this.inputLocale = inputLocale;
        this.outputLocale = outputLocale;
        this.outputBundle = outputBundle;
        this.inputFileName = inputFileName;
    }

    /**
     * Returns the formatted, localized string of all statistics of sentence, words, numbers, etc. concatenated by line separator.
     *
     * @param text the text to process
     * @return the statistics
     */
    public String getLocalizedStats(String text) {
        List<Summary> analyzers = List.of(
                new Summary("Sentence", new SentencesAnalyzer(inputLocale, outputLocale, outputBundle)),
                new Summary("Word", new WordsAnalyzer(inputLocale, outputLocale, outputBundle)),
                new Summary("Number", new NumbersAnalyzer(inputLocale, outputLocale, outputBundle)),
                new Summary("Currency", new CurrencyAnalyzer(inputLocale, outputLocale, outputBundle)),
                new Summary("Date", new DateAnalyzer(inputLocale, outputLocale, outputBundle))
        );

        SummaryFormatter formatter = new SummaryFormatter(outputLocale, outputBundle, inputFileName);

        List<String> headers = new ArrayList<>(List.of(formatter.title(), formatter.header()));
        List<String> statistics = new ArrayList<>();
        for (Summary analyzer : analyzers) {
            statistics.addAll(analyzer.analyzer().getLocalizedStats(text));
            headers.add(formatter.totals(analyzer.summaryKey(), analyzer.analyzer()));
        }

        return Stream.concat(headers.stream(), statistics.stream())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private record Summary(String summaryKey, Analyzer analyzer) {
    }
}
