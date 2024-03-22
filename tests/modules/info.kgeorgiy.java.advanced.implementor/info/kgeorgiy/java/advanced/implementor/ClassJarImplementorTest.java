package info.kgeorgiy.java.advanced.implementor;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class ClassJarImplementorTest extends ClassImplementorTest {
    public ClassJarImplementorTest() {
    }

    @Test
    @Override
    public void test01_constructor() {
        assertConstructor(Impler.class, JarImpler.class);
    }

    @Override
    protected void implement(final Path root, final Impler implementor, final Class<?> clazz) throws ImplerException {
        super.implement(root, implementor, clazz);
        InterfaceJarImplementorTest.implementJar(root, implementor, clazz);
    }
}
