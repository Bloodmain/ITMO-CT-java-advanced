package info.kgeorgiy.ja.dunaev.walk;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class SHA1Hasher implements Hasher {
    private final MessageDigest digest;

    SHA1Hasher() throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance("sha-1");
    }

    @Override
    public void update(final byte[] data, final int size) {
        digest.update(data, 0, size);
    }

    @Override
    public String digest() {
        byte[] hash = digest.digest();
        return format(hash);
    }

    @Override
    public String errorHash() {
        return format(new byte[digest.getDigestLength()]);
    }

    @Override
    public void reset() {
        digest.reset();
    }

    private String format(final byte[] hash) {
        return HexFormat.of().formatHex(hash);
    }
}
