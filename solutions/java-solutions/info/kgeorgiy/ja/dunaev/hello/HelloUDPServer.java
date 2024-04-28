package info.kgeorgiy.ja.dunaev.hello;

import info.kgeorgiy.ja.dunaev.iterative.IterativeParallelism;
import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Server that uses UDP protocol to receive packets and to send them with the prefix.
 *
 * @author Dunaev Kirill
 */
public class HelloUDPServer implements HelloServer {
    private static final byte[] ANSWER_PREFIX = "Hello, ".getBytes(StandardCharsets.UTF_8);
    private final Logger logger = new StandardOutputLogger("UDP Server");

    private ExecutorService service;
    private DatagramSocket socket;
    private Semaphore semaphore;
    private Thread listener;
    private boolean started;

    /**
     * Creates server. The server should be started with {@link #start(int, int)}.
     */
    public HelloUDPServer() {
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the server has been already stared
     * @throws UncheckedIOException  if a {@link SocketException} is occurred
     */
    @Override
    public void start(int port, int threads) {
        if (started) {
            throw new IllegalStateException("Server is already started");
        }

        final int bufferSize;
        try {
            socket = new DatagramSocket(port);
            bufferSize = socket.getReceiveBufferSize();
        } catch (final SocketException e) {
            throw new UncheckedIOException(e);
        }

        semaphore = new Semaphore(threads);
        service = Executors.newFixedThreadPool(threads);
        started = true;

        listener = new Thread(() -> listen(bufferSize));
        listener.start();
    }

    private void listen(int bufferSize) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                semaphore.acquire();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted on semaphore, stopping listening", e);
                break;
            }

            try {
                final byte[] buffer = new byte[bufferSize];
                DatagramPacket packet = new DatagramPacket(buffer, ANSWER_PREFIX.length, bufferSize - ANSWER_PREFIX.length);
                socket.receive(packet);
                service.submit(() -> processQuery(packet));
            } catch (final Exception e) {
                semaphore.release();
                logger.error("Error receiving a package, stopping listening", e);
                break;
            }
        }
    }

    private void processQuery(DatagramPacket query) {
        try {
            byte[] buffer = query.getData();
            System.arraycopy(ANSWER_PREFIX, 0, buffer, 0, ANSWER_PREFIX.length);
            DatagramPacket answer = new DatagramPacket(buffer, ANSWER_PREFIX.length + query.getLength(), query.getSocketAddress());
            socket.send(answer);
        } catch (final IOException e) {
            logger.error("Error sending the answer, ignoring", e);
        } finally {
            semaphore.release();
        }
    }

    @Override
    public void close() {
        if (started) {
            socket.close();
            service.close();

            listener.interrupt();
            try {
                IterativeParallelism.joinThreads(List.of(listener));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            started = false;
        }
    }

    /**
     * CLI for server.
     * Usage: <pre>{@code HelloUDPServer <port> <threads>}</pre>
     * where {@code port} - port to listen, {@code thread} - number of query workers
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Objects.requireNonNull(args);
        Arrays.stream(args).forEach(Objects::requireNonNull);
        if (args.length != 2) {
            System.err.println("Usage: HelloUDPServer <port> <threads>");
            return;
        }
        try {
            int port = HelloUDPClient.inBoundsOrThrow("Invalid port: ", 1, HelloUDPClient.MAX_PORT, Integer.parseInt(args[0]));
            int threads = HelloUDPClient.inBoundsOrThrow("Expected positive threads: ", 1, Integer.MAX_VALUE, Integer.parseInt(args[1]));

            try (HelloServer server = new HelloUDPServer()) {
                server.start(port, threads);
            } catch (final UncheckedIOException ex) {
                System.err.println("An exception during server start: " + ex.getMessage());
            }
        } catch (final NumberFormatException e) {
            System.err.println("Bad integer arguments: " + e.getMessage());
        }
    }
}
