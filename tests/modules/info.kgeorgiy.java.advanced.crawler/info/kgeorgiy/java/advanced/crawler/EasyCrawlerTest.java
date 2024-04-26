package info.kgeorgiy.java.advanced.crawler;

import info.kgeorgiy.java.advanced.base.BaseTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Full tests for easy version
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
        test("https://bars.itmo.ru", 1);
    }

    @Test
    public void test02_pageAndLinks() throws IOException {
        test("https://itmo.ru", 2);
        test("https://www.itmo.ru", 3);
    }

    @Test
    public void test03_invalid() throws IOException {
        test("https://itmo.ru/ru/educational-activity/voprosi_predlojeniya.htmvoprosy_i_predlozheniya.htmvoprosy_i_predlozheniya.htm", 1);
    }

    @Test
    public void test04_deep() throws IOException {
        for (int i = 1; i <= 5; i++) {
            test("http://www.kgeorgiy.info", i);
        }
    }

    @Test
    public void test05_noLimits() throws IOException {
        test(UNLIMITED, UNLIMITED, 10, 10);
    }

    @Test
    public void test06_limitDownloads() throws IOException {
        test(10, UNLIMITED, 100, 10);
    }

    @Test
    public void test07_limitExtractors() throws IOException {
        test(UNLIMITED, 10, 10, 100);
    }

    @Test
    public void test08_limitBoth() throws IOException {
        test(10, 10, 100, 100);
    }

    @Test
    public void test09_performance() throws IOException {
        checkTime(6500, test(UNLIMITED, UNLIMITED, 1000, 1000));
    }

    @Test
    public void test10_realTimePerformance() throws IOException {
        checkTime(5000, test(UNLIMITED, UNLIMITED, -10, 100));
    }

    protected static void checkTime(final double target, final long time) {
        System.err.println("Time: " + time);
        Assertions.assertTrue(time > 0.8 * target, "Too parallel: " + time);
        Assertions.assertTrue(time < 1.2 * target, "Not parallel: " + time);
    }

    private void test(final String url, final int depth) throws IOException {
        test(url, depth, UNLIMITED, UNLIMITED, UNLIMITED, 10, 10);
    }

    protected final long test(
            final int downloaders,
            final int extractors,
            final int downloadTimeout,
            final int extractTimeout
    ) throws IOException {
        return test("http://nerc.itmo.ru/subregions/index.html", 3, downloaders, extractors,
                UNLIMITED, downloadTimeout, extractTimeout);
    }

    protected final long test(
            final String url,
            final int depth,
            final int downloaders,
            final int extractors,
            final int perHost,
            final int downloadTimeout,
            final int extractTimeout
    ) throws IOException {
        return test(new CrawlerTask(url, depth, downloaders, extractors, perHost, downloadTimeout, extractTimeout));
    }

    protected long test(final CrawlerTask task) throws IOException {
        return task.test(
                Crawler.class,
                crawler -> crawler.download(task.url(), task.depth()),
                url -> true
        );
    }
}
