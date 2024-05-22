package info.kgeorgiy.ja.dunaev.i18n.test;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Base class for test that contains common methods.
 *
 * @author Dunaev Kirill
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class BaseTests {
    protected static final Random RANDOM = new Random(297562875629470103L);

    protected String getRandomString(int byteLength) {
        byte[] bytes = new byte[byteLength];
        RANDOM.nextBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
