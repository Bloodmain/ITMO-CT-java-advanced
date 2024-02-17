package info.kgeorgiy.ja.dunaev.walk;

public class JenkinsHasher implements Hasher {
    private int hash = 0;

    @Override
    public void update(byte[] data, int size) {
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
        return format(0);
    }


    @Override
    public void reset() {
        hash = 0;
    }

    private String format(int hashcode) {
        return String.format("%08x", hashcode);
    }
}
