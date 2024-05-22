package info.kgeorgiy.ja.dunaev.i18n.test;

import info.kgeorgiy.ja.dunaev.i18n.TextStatistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Tests for the {@link TextStatistics} app.
 *
 * @author Dunaev Kirill
 */
public class AppTests extends BaseTests {
    private static final String RESOURCES = "info/kgeorgiy/ja/dunaev/i18n/test/resources/full";
    private static final List<String> LANGS = List.of("ru-RU", "en-US");

    private void testFileLang(String lang, String name, String inLocale) throws IOException {
        Path out = Files.createTempFile(null, null);

        TextStatistics.main(new String[]{inLocale, lang, Path.of(RESOURCES, name + ".in").toString(), out.toString()});

        String res = Files.readString(out, StandardCharsets.UTF_8);
        String expected = Files.readString(Path.of(RESOURCES, name + ".out." + lang), StandardCharsets.UTF_8);

        Assertions.assertEquals(expected, res);
    }

    private void testFile(String name, String inLocale) throws IOException {
        for (String lang : LANGS) {
            testFileLang(lang, name, inLocale);
        }
    }

    @Test
    public void test_kgeorgiy() throws IOException {
        testFile("kgeorgiy", "ru-RU");
    }

    @Test
    public void test_emptyFile() throws IOException {
        for (Locale locale : Locale.getAvailableLocales()) {
            testFile("empty", locale.toLanguageTag());
        }
    }

    @Test
    public void test_chinese() throws IOException {
        testFile("chinese", "zh");
    }

    @Test
    public void test_arabic() throws IOException {
        testFile("arabic", "ar");
    }

    @Test
    public void test_warAndPeace() throws IOException {
        testFile("warAndPeace", "ru-RU");
    }

    @Test
    public void test_history() throws IOException {
        testFile("history", "en-US");
    }
}
