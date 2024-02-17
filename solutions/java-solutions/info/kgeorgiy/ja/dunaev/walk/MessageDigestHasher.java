package info.kgeorgiy.ja.dunaev.walk;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestHasher implements Hasher {
    private final MessageDigest digest;

    MessageDigestHasher(String algorithm) throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance(algorithm);
    }

    @Override
    public void update(byte[] data, int size) {
        digest.update(data, 0, size);
    }

    @Override
    public String digest() {
        byte[] hash = digest.digest();
        return format(hash);
    }

    @Override
    public String errorHash() {
        return format(new byte[]{0});
    }

    @Override
    public void reset() {
        digest.reset();
    }

    private String format(byte[] hash) {
        return String.format("%0" + (digest.getDigestLength() << 1) + "x", new BigInteger(1, hash));
    }
}
