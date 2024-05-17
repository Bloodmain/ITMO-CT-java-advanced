package info.kgeorgiy.ja.dunaev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;
import info.kgeorgiy.java.advanced.hello.NewHelloServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Abstract class representing UDP server.
 *
 * @author Dunaev Kirill
 */
public abstract class AbstractServer implements NewHelloServer {
    /**
     * Logger for logging any errors.
     */
    protected final Logger logger = new StandardOutputLogger("UDP Server");

    /**
     * Handlers service to process queries' answers.
     */
    protected ExecutorService handlersService;
    /**
     * Thread pool for socket listeners.
     */
    protected ExecutorService listeners;

    /**
     * Represents the state of the server.
     */
    protected boolean started;

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the server has been already stared
     * @throws UncheckedIOException  if a {@link SocketException} is occurred
     */
    @Override
    public void start(final int threads, final Map<Integer, String> ports) {
        if (started) {
            throw new IllegalStateException("Server is already started");
        }

        if (ports.isEmpty()) {
            return;
        }

        try {
            started = true;
            handlersService = new ThreadPoolExecutor(
                    threads, threads, 0, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<>(10 * threads),
                    new ThreadPoolExecutor.DiscardPolicy()
            );
            setup(ports);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        if (started) {
            closeResources();
            if (handlersService != null) {
                handlersService.close();
            }
            if (listeners != null) {
                listeners.close();
            }

            started = false;
        }
    }

    /**
     * A listener, that will loop and invoke the provided action.
     * If exception is occurred - it's logged and the listener's loop stops.
     *
     * @param listenWork a work to do each iteration of the listen loop
     */
    protected void listen(final IORunnable listenWork) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                listenWork.run();
            } catch (final IOException e) {
                logger.error("Error receiving a package, stopping listening", e);
                break;
            }
        }
    }

    /**
     * Setups the server before starting.
     *
     * @param ports ports to listen
     * @throws IOException if I/O exception occurred
     */
    protected abstract void setup(Map<Integer, String> ports) throws IOException;

    /**
     * Close server's resources.
     */
    protected abstract void closeResources();

    /**
     * Represents an operation that does not return a result and may throw {@link IOException}.
     */
    @FunctionalInterface
    protected interface IORunnable {
        /**
         * Runs this operation.
         *
         * @throws IOException if I/O exception occurred
         */
        void run() throws IOException;
    }

    /**
     * Format the given format by replacing all "$" with the given message.
     *
     * @param message the message to insert
     * @param format  the format
     * @return bytes of the result's string in UTF_8 encoding
     */
    public static byte[] replaceFormat(final String message, final String format) {
        return format.replace("$", message).getBytes(Utils.CHARSET);
    }

    /**
     * Helper for server CLI.
     *
     * @param args           command line arguments
     * @param serverSupplier the supplier of a server instance
     */
    protected static void serverMain(final String[] args, final Supplier<HelloServer> serverSupplier) {
        Objects.requireNonNull(args);
        Arrays.stream(args).forEach(Objects::requireNonNull);
        if (args.length != 2) {
            System.err.println("Usage: HelloUDPServer <port> <threads>");
            return;
        }
        try {
            final int port = Utils.inBoundsOrThrow("Invalid port: ", 1, Utils.MAX_PORT, Integer.parseInt(args[0]));
            final int threads = Utils.inBoundsOrThrow("Expected positive threads: ", 1, Integer.MAX_VALUE, Integer.parseInt(args[1]));

            try (final HelloServer server = serverSupplier.get()) {
                server.start(port, threads);
            } catch (final UncheckedIOException ex) {
                System.err.println("An exception during server start: " + ex.getMessage());
            }
        } catch (final NumberFormatException e) {
            System.err.println("Bad integer arguments: " + e.getMessage());
        }
    }
}
