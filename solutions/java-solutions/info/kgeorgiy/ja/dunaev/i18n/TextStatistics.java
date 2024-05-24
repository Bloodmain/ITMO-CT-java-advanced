package info.kgeorgiy.ja.dunaev.i18n;

import info.kgeorgiy.ja.dunaev.i18n.analyzer.SummaryAnalyzer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;

/**
 * Class for CLI of text statistics.
 *
 * @author Dunaev Kirill
 */
public class TextStatistics {
    private static final String BUNDLE = "info.kgeorgiy.ja.dunaev.i18n.bundle.Report";
    private static final ResourceBundle DEFAULT_BUNDLE = ResourceBundle.getBundle(BUNDLE, Locale.US);
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * CLI for text statistic.
     * Usage: <pre>{@code TextStatistics <text locale> <report locale> <text filename> <report filename>}</pre>,
     * where {@code text locale} - locale of an input text, {@code report locale} - locale of output report,
     * {@code text filename} - path to the input text, {@code report filename} - path to the report file.
     *
     * @param args command line argument
     */
    public static void main(String[] args) {
        Objects.requireNonNull(args);
        Arrays.stream(args).forEach(Objects::requireNonNull);

        if (args.length != 4) {
            System.out.println(MessageFormat.format(DEFAULT_BUNDLE.getString("FormatUsage"), DEFAULT_BUNDLE.getString("Usage"), DEFAULT_BUNDLE.getString("InputLocale"), DEFAULT_BUNDLE.getString("OutputLocale"), DEFAULT_BUNDLE.getString("InputFile"), DEFAULT_BUNDLE.getString("OutputFile")));
            return;
        }

        final Locale inputLocale = Locale.forLanguageTag(args[0]);
        final Locale outputLocale = Locale.forLanguageTag(args[1]);

        try {
            final ResourceBundle outputBundle = ResourceBundle.getBundle(BUNDLE, outputLocale);
            try {
                String inputFileName = args[2];
                final String text = Files.readString(Path.of(inputFileName), DEFAULT_CHARSET);
                try {
                    final Path outputPath = Path.of(args[3]);
                    createParents(outputPath);
                    String output = collectStatistics(text, inputLocale, outputLocale, outputBundle, inputFileName);
                    Files.writeString(outputPath, output, DEFAULT_CHARSET);
                } catch (final IOException e) {
                    error(e, "OutputError", outputBundle);
                }
            } catch (final IOException e) {
                error(e, "InputError", outputBundle);
            }
        } catch (final MissingResourceException e) {
            error(e, "OutputBundleError", DEFAULT_BUNDLE);
        }
    }

    private static void error(Throwable e, String msgKey, ResourceBundle bundle) {
        System.err.println(MessageFormat.format(bundle.getString("FormatError"), bundle.getString(msgKey), e.getLocalizedMessage()));
    }

    private static void createParents(Path path) {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (final IOException ignored) {
            }
        }
    }

    private static String collectStatistics(String text, Locale inputLocale, Locale outputLocale, ResourceBundle outputBundle, String inputFileName) {

        SummaryAnalyzer analyzer = new SummaryAnalyzer(inputLocale, outputLocale, outputBundle, inputFileName);
        return analyzer.getLocalizedStats(text);
    }
}
