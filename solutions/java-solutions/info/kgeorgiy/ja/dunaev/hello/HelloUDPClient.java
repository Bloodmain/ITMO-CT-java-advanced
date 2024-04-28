package info.kgeorgiy.ja.dunaev.hello;

import info.kgeorgiy.ja.dunaev.iterative.IterativeParallelism;
import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Client that uses UDP protocol to send datagrams to a server and to print received data.
 *
 * @author Dunaev Kirill
 */
public class HelloUDPClient implements HelloClient {
    /**
     * Max value that can represent a port
     */
    public static final int MAX_PORT = 65535;

    private static final int TIMEOUT = 250; // ms
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+", Pattern.UNICODE_CHARACTER_CLASS);
    private final Logger logger = new StandardOutputLogger("UDP Client");

    /**
     * Creates client. The client should be started with {@link #run(String, int, String, int, int)}.
     */
    public HelloUDPClient() {
    }

    /**
     * {@inheritDoc}
     *
     * @throws UncheckedIOException if an error occurred when getting address or creating socket
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try {
            InetAddress address = InetAddress.getByName(host);
            List<Thread> actions = IntStream.range(1, threads + 1)
                    .<Runnable>mapToObj(i -> () -> sendTask(prefix + i + "_", requests, port, address, i))
                    .map(Thread::new)
                    .toList();

            actions.forEach(Thread::start);
            IterativeParallelism.joinThreads(actions);
        } catch (final UnknownHostException e) {
            logger.error("Can't get address of the given host", e);
            throw new UncheckedIOException(e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting for threads", e);
        }
    }

    private void sendTask(String prefix, int requests, int port, InetAddress address, int thread) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT);

            for (int i = 1; i <= requests; i++) {
                byte[] message = (prefix + i).getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
                int[] originInts = new int[]{thread, i};
                while (true) {
                    if (sendAndReceive(socket, packet, originInts)) {
                        break;
                    }
                }
            }
        } catch (final SocketException e) {
            logger.error("Can't create socket", e);
            throw new UncheckedIOException(e);
        }
    }

    private boolean sendAndReceive(DatagramSocket socket, DatagramPacket packet, int[] originInts) {
        try {
            socket.send(packet);
            logger.info("Successfully send packet with message: \"%s\"".formatted(getMessage(packet)));
        } catch (final IOException e) {
            logger.error("Can't send packet", e);
            return false;
        }

        try {
            int bufferSize = socket.getReceiveBufferSize();
            DatagramPacket receive = new DatagramPacket(new byte[bufferSize], bufferSize);
            socket.receive(receive);

            String receivedMessage = getMessage(receive);
            if (validate(receivedMessage, originInts)) {
                logger.info("Successfully received packet with message: \"%s\"".formatted(receivedMessage));
                return true;
            } else {
                logger.warn("Received packet with invalid message: \"%s\"".formatted(receivedMessage));
                return false;
            }
        } catch (final IOException e) {
            logger.error("Can't receive packet", e);
            return false;
        }
    }

    private String getMessage(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
    }

    private boolean validate(String s, int[] originInts) {
        Matcher m = DIGIT_PATTERN.matcher(s);
        for (int x : originInts) {
            try {
                if (!m.find() || Integer.parseInt(m.group()) != x) {
                    return false;
                }
            } catch (final NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * CLI for client.
     * Usage: <pre>{@code HelloUDPClient <host> <port> <prefix> <threads> <requests>}</pre>
     * where {@code host} name or ip of the server, {@code port} - port, send requests to which,
     * {@code prefix} - prefix of all query, {@code thread} - number of query workers,
     * {@code requests} - number of requests in each thread
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Objects.requireNonNull(args);
        Arrays.stream(args).forEach(Objects::requireNonNull);
        if (args.length != 5) {
            System.err.println("Usage: HelloUDPClient <host> <port> <prefix> <threads> <requests>");
            return;
        }
        try {
            String host = args[0];
            int port = inBoundsOrThrow("Invalid port: ", 1, MAX_PORT, Integer.parseInt(args[1]));
            String prefix = args[2];
            int threads = inBoundsOrThrow("Expected positive threads: ", 1, Integer.MAX_VALUE, Integer.parseInt(args[3]));
            int requests = inBoundsOrThrow("Expected positive requests: ", 1, Integer.MAX_VALUE, Integer.parseInt(args[4]));

            try {
                HelloClient client = new HelloUDPClient();
                client.run(host, port, prefix, threads, requests);
            } catch (final UncheckedIOException ex) {
                System.err.println("An exception while running client: " + ex.getMessage());
            }
        } catch (final NumberFormatException e) {
            System.err.println("Bad integer arguments: " + e.getMessage());
        }
    }

    /**
     * Assert that the given value is in the given bounds.
     *
     * @param ctx   an error context
     * @param lower a lower bound
     * @param upper an upper bound
     * @param v     the value
     * @return the given value
     * @throws IllegalArgumentException if the value is not in bounds
     */
    public static int inBoundsOrThrow(String ctx, int lower, int upper, int v) {
        if (v < lower || v > upper) {
            throw new IllegalArgumentException(ctx + v);
        }
        return v;
    }
}
