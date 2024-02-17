package info.kgeorgiy.ja.dunaev.walk;

public interface Hasher {
    void update(byte[] data, int size);

    String digest();
    String errorHash();

    void reset();
}
