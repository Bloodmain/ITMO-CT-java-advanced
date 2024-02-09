package info.kgeorgiy.java.advanced.walk;

import info.kgeorgiy.java.advanced.base.BaseTest;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 * Tests for easy version
 * of <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#homework-walk">Walk</a> homework
 * for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WalkTest extends BaseTest {
    protected final WalkUtil util = new WalkUtil();

    public WalkTest() {
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        WalkUtil.clear();
    }

    protected Path testDir() {
        return WalkUtil.path(testMethodName);
    }

    @Test
    public void test10_oneEmptyFile() throws IOException {
        testRandomFiles(1, 0);
    }

    @Test
    public void test20_smallRandomFiles() throws IOException {
        testRandomFiles(10, 100);
    }

    @Test
    public void test60_noInput() {
        final String[] args = new String[]{util.randomFileName(), util.randomFileName()};
        final Method method;
        final Class<?> cut = loadClass();
        try {
            method = cut.getMethod("main", String[].class);
        } catch (final NoSuchMethodException e) {
            throw new AssertionError("Cannot find method main(String[]) of " + cut, e);
        }
        try {
            method.invoke(null, (Object) args);
        } catch (final IllegalAccessException e) {
            throw new AssertionError("Cannot call main(String[]) of " + cut, e);
        } catch (final InvocationTargetException e) {
            throw new AssertionError("Error thrown", e.getCause());
        }
    }

    private void testRandomFiles(final int n, final int maxSize) throws IOException {
        test(randomFiles(n, maxSize));
    }

    private Map<String, byte[]> randomFiles(final int n, final int maxSize) throws IOException {
        return util.randomFiles(n, maxSize, testDir());
    }

    protected void test(final Map<String, byte[]> files) {
        check(files.keySet(), files);
    }

    protected void check(final Collection<String> inputs, final Map<String, byte[]> files) {
        check(inputs, files, testMethodName + ".in", testMethodName + ".out");
    }

    protected void check(
            final Collection<String> inputs,
            final Map<String, byte[]> files,
            final String inputName,
            final String outputName
    ) {
        util.check(inputs, files, inputName, outputName);
    }
}

