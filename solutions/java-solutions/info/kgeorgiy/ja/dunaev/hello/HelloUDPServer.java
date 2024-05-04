package info.kgeorgiy.ja.dunaev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;
import info.kgeorgiy.java.advanced.hello.NewHelloServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Server that uses UDP protocol to receive packets and to send them with the prefix.
 *
 * @author Dunaev Kirill
 */
public class HelloUDPServer implements NewHelloServer {
    private final Logger logger = new StandardOutputLogger("UDP Server");

    private ExecutorService service;
    private ExecutorService listeners;
    private List<DatagramSocket> sockets;

    private Semaphore semaphore;
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
    public void start(int threads, Map<Integer, String> ports) {
        if (started) {
            throw new IllegalStateException("Server is already started");
        }

        if (ports == null || ports.isEmpty()) {
            return;
        }

        try {
            sockets = new ArrayList<>();
            semaphore = new Semaphore(threads);
            service = Executors.newFixedThreadPool(threads);
            started = true;

            listeners = Executors.newFixedThreadPool(ports.size());
            for (Map.Entry<Integer, String> port : ports.entrySet()) {
                DatagramSocket socket = new DatagramSocket(port.getKey());
                sockets.add(socket);
                listeners.execute(() -> listen(socket, port.getValue()));
            }
        } catch (final SocketException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void listen(DatagramSocket socket, String format) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                semaphore.acquire();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted on semaphore, stopping listening", e);
                break;
            }

            try {
                int bufferSize = socket.getReceiveBufferSize();
                final byte[] buffer = new byte[bufferSize];
                DatagramPacket packet = new DatagramPacket(buffer, bufferSize);
                socket.receive(packet);
                service.submit(() -> processQuery(packet, socket, format));
            } catch (final Exception e) {
                semaphore.release();
                logger.error("Error receiving a package, stopping listening", e);
                break;
            }
        }
    }

    private void processQuery(DatagramPacket query, DatagramSocket socket, String format) {
        try {
            byte[] res = format.replaceAll("\\$", HelloUDPClient.getMessage(query)).getBytes(StandardCharsets.UTF_8);
            DatagramPacket answer = new DatagramPacket(res, res.length, query.getSocketAddress());
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
            sockets.forEach(DatagramSocket::close);
            service.close();
            listeners.close();
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
