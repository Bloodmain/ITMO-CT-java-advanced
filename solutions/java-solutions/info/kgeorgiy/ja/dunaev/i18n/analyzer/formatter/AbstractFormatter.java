package info.kgeorgiy.ja.dunaev.i18n.analyzer.formatter;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A abstract class for all formatters.
 *
 * @author Dunaev Kirill
 */
public abstract class AbstractFormatter {
    protected final ResourceBundle bundle;
    protected final Locale locale;

    protected AbstractFormatter(ResourceBundle bundle, Locale locale) {
        this.bundle = bundle;
        this.locale = locale;
    }

    protected String get(String key) {
        return bundle.getString(key);
    }

    protected String format(String pattern, Object... args) {
        MessageFormat format = new MessageFormat(pattern, locale);
        return format.format(args, new StringBuffer(), null).toString();
    }

    protected String formatTab(String pattern, Object... args) {
        return MessageFormat.format(bundle.getString("FormatTab"), format(pattern, args));
    }
}


