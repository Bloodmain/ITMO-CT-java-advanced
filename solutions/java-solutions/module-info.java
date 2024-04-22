/**
 * Java advanced solutions module. Module contains solutions for homeworks for
 * <a href=https://www.kgeorgiy.info/courses/java-advanced/homeworks.html>Java advanced</a> course at
 * <a href=https://itmo.ru>ITMO university</a>.
 *
 * @author Dunaev Kirill
 */
module info.kgeorgiy.ja.dunaev {
    requires info.kgeorgiy.java.advanced.walk;
    requires info.kgeorgiy.java.advanced.arrayset;
    requires info.kgeorgiy.java.advanced.student;
    requires info.kgeorgiy.java.advanced.implementor;
    requires info.kgeorgiy.java.advanced.iterative;
    requires info.kgeorgiy.java.advanced.mapper;
    requires info.kgeorgiy.java.advanced.crawler;
    requires java.compiler;

    exports info.kgeorgiy.ja.dunaev.walk;
    exports info.kgeorgiy.ja.dunaev.arrayset;
    exports info.kgeorgiy.ja.dunaev.student;
    exports info.kgeorgiy.ja.dunaev.implementor;
    exports info.kgeorgiy.ja.dunaev.iterative;
    exports info.kgeorgiy.ja.dunaev.crawler;
}
