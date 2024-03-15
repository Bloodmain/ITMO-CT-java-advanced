package info.kgeorgiy.java.advanced.implementor;

import info.kgeorgiy.java.advanced.implementor.basic.interfaces.InterfaceWithDefaultMethod;
import info.kgeorgiy.java.advanced.implementor.basic.interfaces.InterfaceWithStaticMethod;
import info.kgeorgiy.java.advanced.implementor.basic.interfaces.standard.Accessible;
import info.kgeorgiy.java.advanced.implementor.basic.interfaces.standard.Descriptor;
import info.kgeorgiy.java.advanced.implementor.basic.interfaces.standard.RandomAccess;

import org.junit.jupiter.api.Test;

/**
 * Basic tests for easy version
 * of <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#homework-implementor">Implementor</a> homework
 * for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class InterfaceImplementorTest extends BaseImplementorTest {
    public InterfaceImplementorTest() {
    }

    @Test
    public void test01_constructor() {
        assertConstructor(Impler.class);
    }

    @Test
    public void test02_methodlessInterfaces() {
        testOk(RandomAccess.class);
    }

    @Test
    public void test03_standardInterfaces() {
        testOk(Accessible.class);
    }

    @Test
    public void test04_extendedInterfaces() {
        testOk(Descriptor.class);
    }

    @Test
    public void test05_standardNonInterfaces() {
        testFail(void.class, String.class);
    }

    @Test
    public void test06_java8Interfaces() {
        testOk(InterfaceWithStaticMethod.class, InterfaceWithDefaultMethod.class);
    }
}
