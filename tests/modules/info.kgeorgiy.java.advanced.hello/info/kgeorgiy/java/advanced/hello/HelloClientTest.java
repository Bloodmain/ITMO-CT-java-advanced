package info.kgeorgiy.java.advanced.hello;

import info.kgeorgiy.java.advanced.base.BaseTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.*;

/**
 * Basic tests for {@link HelloClient}.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class HelloClientTest extends BaseTest {
    private static int port = 28888;
    public static final String PREFIX = HelloClientTest.class.getName();

    public HelloClientTest() {
    }

    @Test
    public void test01_singleRequest() throws SocketException {
        test(1, 1);
    }

    @Test
    public void test03_singleWithFailures() throws SocketException {
        test(1, 0.1);
    }

    @Test
    public void test05_singleMultithreaded() throws SocketException {
        test(10, 1);
    }

    @SuppressWarnings("try")
    private static void test(final int threads, final double p) throws SocketException {
        final int port = HelloClientTest.port++;
        try (final DatagramSocket socket = new DatagramSocket(port)) {
            final ExecutorService executor = Executors.newFixedThreadPool(2);
            try {
                final CompletionService<int[]> completionService = new ExecutorCompletionService<>(executor);
                completionService.submit(Util.server(PREFIX, threads, p, socket));
                completionService.submit(() -> {
                    final HelloClient client = createCUT();
                    client.run("localhost", port, PREFIX, threads, 1);
                    return null;
                });
                for (int i = 0; i < 2; i++) {
                    try {
                        final int[] actual = completionService.take().get();
                        if (actual != null) {
                            for (int j = 0; j < actual.length; j++) {
                                Assertions.assertEquals(1, actual[j], "Invalid number of requests on thread " + j);
                            }
                        } else {
                            socket.close();
                        }
                    } catch (final ExecutionException e) {
                        final Throwable cause = e.getCause();
                        if (cause instanceof RuntimeException) {
                            throw (RuntimeException) cause;
                        } else if (cause instanceof Error) {
                            throw (Error) cause;
                        } else {
                            throw new AssertionError("Unexpected exception", e.getCause());
                        }
                    }
                }
            } finally {
                executor.shutdownNow();
            }
        } catch (final InterruptedException e) {
            throw new AssertionError("Test thread interrupted", e);
        }
    }
}
