package info.kgeorgiy.ja.dunaev.walk;

public class JenkinsHasher implements Hasher {
    private int hash = 0;
    private static final int BYTE_MOD = 256;

    @Override
    public void update(byte[] data, int size) {
        for (int i = 0; i < size; ++i) {
            hash += (data[i] + BYTE_MOD) % BYTE_MOD;
            hash += hash << 10;
            hash ^= hash >>> 6;
        }
    }

    @Override
    public int digest() {
        hash += hash << 3;
        hash ^= hash >>> 11;
        hash += hash << 15;

        int old_hash = hash;
        reset();

        return old_hash;
    }

    @Override
    public void reset() {
        hash = 0;
    }
}
