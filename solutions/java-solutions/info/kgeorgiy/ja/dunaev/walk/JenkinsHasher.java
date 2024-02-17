package info.kgeorgiy.ja.dunaev.walk;

import java.util.HexFormat;

public class JenkinsHasher implements Hasher {
    private final String errorHash = format(0);
    private int hash = 0;

    @Override
    public void update(final byte[] data, final int size) {
        for (int i = 0; i < size; ++i) {
            hash += Byte.toUnsignedInt(data[i]);
            hash += hash << 10;
            hash ^= hash >>> 6;
        }
    }

    @Override
    public String digest() {
        hash += hash << 3;
        hash ^= hash >>> 11;
        hash += hash << 15;

        int old_hash = hash;
        reset();

        return format(old_hash);
    }

    @Override
    public String errorHash() {
        return errorHash;
    }


    @Override
    public void reset() {
        hash = 0;
    }

    private String format(final int hashcode) {
        return HexFormat.of().toHexDigits(hashcode);
    }
}
