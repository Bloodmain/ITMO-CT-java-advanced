package info.kgeorgiy.ja.dunaev.i18n.analyzer.stat;

import java.util.Comparator;

/**
 * Class to collect statistics of text.
 *
 * @author Dunaev Kirill
 */
public class TextStats extends Stats<String> {
    private String maxLengthValue;
    private String minLengthValue;

    /**
     * Creates statistics with the provided comparator on strings.
     *
     * @param comparator the comparator
     */
    public TextStats(Comparator<? super String> comparator) {
        super(comparator);
    }

    @Override
    protected void addValue(String v) {
        sum += v.length();
    }

    @Override
    protected void processFirst(String v) {
        maxLengthValue = v;
        minLengthValue = v;
    }

    @Override
    protected void processNormal(String v) {
        long length = v.length();

        if (length > maxLengthValue.length()) {
            maxLengthValue = v;
        }

        if (length < minLengthValue.length()) {
            minLengthValue = v;
        }
    }

    /**
     * Returns the length of the maximum (by length) string.
     *
     * @return the maximum length
     */
    public long getMaxLength() {
        String str = getMaxLengthValue();
        return str == null ? 0 : str.length();
    }

    /**
     * Returns the length of the minimum (by length) string.
     *
     * @return the minimum length
     */
    public long getMinLength() {
        String str = getMinLengthValue();
        return str == null ? 0 : str.length();
    }

    /**
     * Returns the string with the maximum length.
     *
     * @return the string with maximum length
     */
    public String getMaxLengthValue() {
        return maxLengthValue;
    }

    /**
     * Returns the string with the minimum length.
     *
     * @return the string with minimum length
     */
    public String getMinLengthValue() {
        return minLengthValue;
    }
}
