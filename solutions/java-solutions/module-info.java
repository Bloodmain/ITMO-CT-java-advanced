/**
 * Java advanced solutions module. Module contains solutions for homeworks for
 * <a href=https://www.kgeorgiy.info/courses/java-advanced/homeworks.html>Java advanced</a> course at
 * <a href=https://itmo.ru>ITMO university</a>.
 *
 * @author Dunaev Kirill
 */
module info.kgeorgiy.ja.dunaev {
    requires java.compiler;
    requires java.rmi;
    requires org.junit.jupiter.api;
    requires org.junit.platform.launcher;
    requires org.json;

    requires info.kgeorgiy.java.advanced.walk;
    requires info.kgeorgiy.java.advanced.arrayset;
    requires info.kgeorgiy.java.advanced.student;
    requires info.kgeorgiy.java.advanced.implementor;
    requires info.kgeorgiy.java.advanced.iterative;
    requires info.kgeorgiy.java.advanced.mapper;
    requires info.kgeorgiy.java.advanced.crawler;
    requires info.kgeorgiy.java.advanced.hello;

    exports info.kgeorgiy.ja.dunaev.walk;
    exports info.kgeorgiy.ja.dunaev.walk.exceptions;
    exports info.kgeorgiy.ja.dunaev.arrayset;
    exports info.kgeorgiy.ja.dunaev.student;
    exports info.kgeorgiy.ja.dunaev.implementor;
    exports info.kgeorgiy.ja.dunaev.iterative;
    exports info.kgeorgiy.ja.dunaev.crawler;
    exports info.kgeorgiy.ja.dunaev.hello;
    exports info.kgeorgiy.ja.dunaev.bank;
    exports info.kgeorgiy.ja.dunaev.bank.internal;
    exports info.kgeorgiy.ja.dunaev.i18n;

    opens info.kgeorgiy.ja.dunaev.bank to org.junit.platform.commons;
    opens info.kgeorgiy.ja.dunaev.i18n.test to org.junit.platform.commons;
}
