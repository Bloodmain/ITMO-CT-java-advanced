package info.kgeorgiy.java.advanced.implementor;

import info.kgeorgiy.java.advanced.implementor.basic.classes.AbstractClassWithInterface;
import info.kgeorgiy.java.advanced.implementor.basic.classes.standard.IIOException;
import info.kgeorgiy.java.advanced.implementor.basic.classes.standard.IIOImage;
import info.kgeorgiy.java.advanced.implementor.basic.classes.standard.RMIServerImpl;
import info.kgeorgiy.java.advanced.implementor.basic.classes.standard.RelationNotFoundException;

import org.junit.jupiter.api.Test;

/**
 * Basic tests for hard version
 * of <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#homework-implementor">Implementor</a> homework
 * for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class ClassImplementorTest extends InterfaceImplementorTest {
    public ClassImplementorTest() {
    }

    @Test
    public void test11_defaultConstructorClasses() {
        testOk(RelationNotFoundException.class, AbstractClassWithInterface.class);
    }

    @Test
    public void test12_noDefaultConstructorClasses() {
        testOk(IIOException.class);
    }

    @Test
    public void test13_ambiguousConstructorClasses() {
        testOk(IIOImage.class);
    }

    @Test
    public void test18_nonPublicAbstractMethod() {
        testOk(RMIServerImpl.class);
    }

    @Test
    public void test19_enum() {
        testFail(Enum.class);
    }
}
