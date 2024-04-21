package info.kgeorgiy.java.advanced.crawler;

import info.kgeorgiy.java.advanced.base.BaseTest;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Basic tests for easy version
 * of <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#crawler">Web Crawler</a> homework
 * for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class EasyCrawlerTest extends BaseTest {
    public static final int UNLIMITED = 100;

    public EasyCrawlerTest() {
    }

    @Test
    public void test01_singlePage() throws IOException {
        test("https://en.itmo.ru/en/page/50/Partnership.htm", 1);
    }

    @Test
    public void test02_pageAndLinks() throws IOException {
        test("https://itmo.ru", 2);
    }

    private static void test(final String url, final int depth) throws IOException {
        new CrawlerTask(url, depth, UNLIMITED, UNLIMITED, UNLIMITED, 10, 10).test(
                Crawler.class,
                crawler -> crawler.download(url, depth),
                u -> true
        );
    }
}
