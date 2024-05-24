package info.kgeorgiy.ja.dunaev.i18n.test;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Tests for the analyzers' parsing method.
 *
 * @author Dunaev Kirill
 */
public class AnalyzerTests extends BaseTests {
    private static final Locale DEFAULT_LOCALE = Locale.US;
    private static final ResourceBundle DEFAULT_BUNDLE =
            ResourceBundle.getBundle("info.kgeorgiy.ja.dunaev.i18n.bundle.Report", DEFAULT_LOCALE);
    private static final String RESOURCES = "info/kgeorgiy/ja/dunaev/i18n/test/resources/small";

    private Map<String, Long> getData(String p, int n) throws IOException {
        String data = Files.readString(Path.of(RESOURCES, p + ".json"), StandardCharsets.UTF_8);

        Map<String, Long> res = new HashMap<>();

        var array = new JSONObject(data).getJSONArray("data");
        for (int i = 0; i < n; ++i) {
            var obj = array.getJSONObject(i);
            res.put(obj.getString("in"), obj.getLong("out"));
        }

        return res;
    }

    private <T> String generateRandom(int n, Function<T, String> format, Function<Integer, T> val) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < n; i++) {
            builder.append(format.apply(val.apply(RANDOM.nextInt())));
            builder.append(' ');
        }

        return builder.toString();
    }

    private String generateRandomDates(int n, Locale locale, int mode) {
        DateFormat format = DateFormat.getDateInstance(mode, locale);
        return generateRandom(n, format::format, x -> new Date(RANDOM.nextInt()));
    }

    private String generateRandomNumbers(int n, Locale locale) {
        NumberFormat format = NumberFormat.getInstance(locale);
        return generateRandom(n, format::format, Function.identity());
    }

    private String generateRandomCurrency(int n, Locale locale) {
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        return generateRandom(n, format::format, Function.identity());
    }

    private void testNumeric(Function<Locale, Analyzer> analyzerS, BiFunction<Integer, Locale, String> generator) {
        Locale[] locales = Locale.getAvailableLocales();
        Collections.shuffle(Arrays.asList(locales));

        for (Locale locale : Arrays.stream(locales).limit(100).toList()) {
            int n = RANDOM.nextInt(1000);

            Analyzer analyzer = analyzerS.apply(locale);
            analyzer.getLocalizedStats(generator.apply(n, locale));

            Assertions.assertEquals(n, analyzer.getTotal());
        }
    }

    @Test
    public void test_dates() {
        for (int mode : new int[]{DateFormat.SHORT, DateFormat.MEDIUM, DateFormat.LONG, DateFormat.FULL}) {
            testNumeric(l -> new DateAnalyzer(l, DEFAULT_LOCALE, DEFAULT_BUNDLE), (n, l) -> generateRandomDates(n, l, mode));
        }
    }

    @Test
    public void test_numbers() {
        testNumeric(l -> new NumbersAnalyzer(l, DEFAULT_LOCALE, DEFAULT_BUNDLE), this::generateRandomNumbers);
    }

    @Test
    public void test_currencies() {
        testNumeric(l -> new CurrencyAnalyzer(l, DEFAULT_LOCALE, DEFAULT_BUNDLE), this::generateRandomCurrency);
    }

    private void testText(Map<String, Long> input, Supplier<Analyzer> analyzerS) {
        for (var in : input.entrySet()) {
            Analyzer analyzer = analyzerS.get();
            analyzer.getLocalizedStats(in.getKey());
            Assertions.assertEquals(in.getValue(), analyzer.getTotal());
        }
    }

    @Test
    public void test_words() throws IOException {
        Map<String, Long> input = getData("words", 4);
        testText(input, () -> new WordsAnalyzer(Locale.forLanguageTag("ru-RU"), DEFAULT_LOCALE, DEFAULT_BUNDLE));
    }

    @Test
    public void test_sentence() throws IOException {
        Map<String, Long> input = getData("sentences", 4);
        testText(input, () -> new SentencesAnalyzer(Locale.forLanguageTag("ru-RU"), DEFAULT_LOCALE, DEFAULT_BUNDLE));
    }
}
