package info.kgeorgiy.ja.dunaev.walk;

public interface Hasher {
    void update(byte[] data, int size);

    int digest();

    void reset();
}
