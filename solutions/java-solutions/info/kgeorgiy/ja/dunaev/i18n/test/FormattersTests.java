package info.kgeorgiy.ja.dunaev.i18n.test;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter.CurrencyFormatter;
import info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter.DateFormatter;
import info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter.NumberFormatter;
import info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter.TextFormatter;
import info.kgeorgiy.ja.dunaev.i18n.analyzer.stat.NumberStats;
import info.kgeorgiy.ja.dunaev.i18n.analyzer.stat.Stats;
import info.kgeorgiy.ja.dunaev.i18n.analyzer.stat.TextStats;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Function;

import org.json.*;

/**
 * Tests for the formatters.
 *
 * @author Dunaev Kirill
 */
public class FormattersTests extends BaseTests {
    private static final String BUNDLE = "info.kgeorgiy.ja.dunaev.i18n.bundle.Report";
    private static final String RESOURCES = "info/kgeorgiy/ja/dunaev/i18n/test/resources";

    private NumberStats getNumberStats() {
        NumberStats stats = new NumberStats();
        RANDOM.doubles(1000).forEach(stats::add);
        return stats;
    }

    private TextStats getTextStats(Comparator<? super String> comparator) {
        TextStats stats = new TextStats(comparator);
        RANDOM.ints(1000, 0, 100).mapToObj(this::getRandomString).forEach(stats::add);
        return stats;
    }

    private JSONObject getData(String p) throws IOException {
        String data = Files.readString(Path.of(RESOURCES, p + ".json"), StandardCharsets.UTF_8);
        return new JSONObject(data);
    }

    @FunctionalInterface
    private interface FormatterCtor {
        NumberFormatter construct(Locale l, ResourceBundle r, NumberStats s);
    }

    @FunctionalInterface
    private interface FormatterStringCtor {
        TextFormatter construct(Locale l, ResourceBundle r, TextStats s);
    }

    private void checkData(NumberFormatter formatter, JSONObject data, Stats<?> stats, Format format, Locale locale) {
        NumberFormat occurFormatter = NumberFormat.getInstance(locale);
        Assertions.assertEquals(data.getString("header"), formatter.header());
        Assertions.assertEquals(data.getString("occurrences").formatted(
                        occurFormatter.format(stats.getOccurrences()), occurFormatter.format(stats.getUniques())
                ),
                formatter.total());
        Assertions.assertEquals(data.getString("minValue").formatted(format.format(stats.getMinValue())), formatter.minValue());
        Assertions.assertEquals(data.getString("maxValue").formatted(format.format(stats.getMaxValue())), formatter.maxValue());
        Assertions.assertEquals(data.getString("avg").formatted(format.format(stats.getAverage())), formatter.average());
    }

    private void testLang(
            String lang,
            String country,
            String key,
            FormatterCtor formatter,
            Function<Locale, Format> format,
            Function<Locale, NumberStats> stats
    ) throws IOException {
        JSONObject data = getData(key + lang);
        Locale locale = Locale.of(lang, country);
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, locale);

        var stat = stats.apply(locale);
        checkData(formatter.construct(locale, bundle, stat), data, stat, format.apply(locale), locale);
    }

    private void testData(String key, FormatterCtor formatter, Function<Locale, Format> format, Function<Locale, NumberStats> stats) throws IOException {
        testLang("RU", "RU", key, formatter, format, stats);
        testLang("EN", "US", key, formatter, format, stats);
    }

    private void testNumber(String key, FormatterCtor formatter, Function<Locale, Format> format) throws IOException {
        testData(key, formatter, format, l -> getNumberStats());
    }

    @Test
    public void test_numbers() throws IOException {
        testNumber("numbers", (l, b, s) -> new NumberFormatter(l, b, "Number", s), NumberFormat::getInstance);
    }

    @Test
    public void test_currencies() throws IOException {
        testNumber("currency", (l, b, s) -> new CurrencyFormatter(l, b, "Currency", s), NumberFormat::getCurrencyInstance);
    }

    @Test
    public void test_dates() throws IOException {
        testNumber("dates", (l, b, s) -> new DateFormatter(l, b, "Date", s), l -> DateFormat.getDateInstance(DateFormat.MEDIUM, l));
    }

    private void checkStringData(TextFormatter formatter, JSONObject data, TextStats stats, Locale locale) {
        NumberFormat occurFormatter = NumberFormat.getInstance(locale);
        Assertions.assertEquals(data.getString("header"), formatter.header());
        Assertions.assertEquals(data.getString("occurrences").formatted(
                        occurFormatter.format(stats.getOccurrences()), occurFormatter.format(stats.getUniques())
                ),
                formatter.total());
        Assertions.assertEquals(data.getString("minValue").formatted(stats.getMinValue()), formatter.minValue());
        Assertions.assertEquals(data.getString("maxValue").formatted(stats.getMaxValue()), formatter.maxValue());
        Assertions.assertEquals(data.getString("minLength").formatted(
                occurFormatter.format(stats.getMinLength()), stats.getMinLengthValue()), formatter.minLength());
        Assertions.assertEquals(data.getString("maxLength").formatted(
                occurFormatter.format(stats.getMaxLength()), stats.getMaxLengthValue()), formatter.maxLength());
        Assertions.assertEquals(data.getString("avg").formatted(occurFormatter.format(stats.getAverage())), formatter.average());
    }

    private void testStringLang(
            String lang,
            String country,
            String key,
            FormatterStringCtor formatter,
            Function<Locale, TextStats> stats
    ) throws IOException {
        JSONObject data = getData(key + lang);
        Locale locale = Locale.of(lang, country);
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, locale);

        var stat = stats.apply(locale);
        checkStringData(formatter.construct(locale, bundle, stat), data, stat, locale);
    }

    private void testStringData(String key, FormatterStringCtor formatter, Function<Locale, TextStats> stats) throws IOException {
        testStringLang("RU", "RU", key, formatter, stats);
        testStringLang("EN", "US", key, formatter, stats);
    }

    private void testString(String key, FormatterStringCtor formatter) throws IOException {
        testStringData(key, formatter, l -> getTextStats(Collator.getInstance(l)));
    }

    @Test
    public void test_words() throws IOException {
        testString("words", (l, b, s) -> new TextFormatter(l, b, "Word", s));
    }

    @Test
    public void test_sentences() throws IOException {
        testString("sentences", (l, b, s) -> new TextFormatter(l, b, "Sentence", s));
    }
}
