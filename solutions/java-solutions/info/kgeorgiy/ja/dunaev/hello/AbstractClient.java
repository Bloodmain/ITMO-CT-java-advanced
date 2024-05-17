package info.kgeorgiy.ja.dunaev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class representing UDP client.
 *
 * @author Dunaev Kirill
 */
public abstract class AbstractClient implements HelloClient {
    /**
     * Time (ms) for which message is waited
     */
    protected static final int TIMEOUT = 250;
    /**
     * Patter for digits (including unicode)
     */
    protected static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+", Pattern.UNICODE_CHARACTER_CLASS);
    /**
     * Logger for logging any errors.
     */
    protected final Logger logger = new StandardOutputLogger("UDP Client");

    /**
     * Tries to get {@link InetAddress} from the given host.
     *
     * @param host host to get
     * @return inet address of the host
     * @throws UncheckedIOException if the host is unknown
     */
    protected InetAddress getHost(final String host) {
        try {
            return InetAddress.getByName(host);
        } catch (final UnknownHostException e) {
            logger.error("Can't get address of the given host", e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Validate the message by comparing integers in the message with the given ones.
     *
     * @param s          a message to validate
     * @param originInts integers to compare with
     * @return {@code true} if the message is valid, {@code false} otherwise
     */
    protected static boolean validate(final String s, final int[] originInts) {
        final Matcher m = DIGIT_PATTERN.matcher(s);
        final List<MatchResult> results = m.results().toList();
        if (originInts.length != results.size()) {
            return false;
        }
        for (int i = 0; i < results.size(); i++) {
            try {
                if (Integer.parseInt(results.get(i).group()) != originInts[i]) {
                    return false;
                }
            } catch (final NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper for client CLI.
     *
     * @param args           command line arguments
     * @param clientSupplier the supplier of a client instance
     */
    protected static void mainClient(final String[] args, final Supplier<HelloClient> clientSupplier) {
        Objects.requireNonNull(args);
        Arrays.stream(args).forEach(Objects::requireNonNull);
        if (args.length != 5) {
            System.err.println("Usage: HelloUDPClient <host> <port> <prefix> <threads> <requests>");
            return;
        }
        try {
            final String host = args[0];
            final int port = Utils.inBoundsOrThrow("Invalid port: ", 1, Utils.MAX_PORT, Integer.parseInt(args[1]));
            final String prefix = args[2];
            final int threads = Utils.inBoundsOrThrow("Expected positive threads: ", 1, Integer.MAX_VALUE, Integer.parseInt(args[3]));
            final int requests = Utils.inBoundsOrThrow("Expected positive requests: ", 1, Integer.MAX_VALUE, Integer.parseInt(args[4]));

            try {
                clientSupplier.get().run(host, port, prefix, threads, requests);
            } catch (final UncheckedIOException ex) {
                System.err.println("An exception while running client: " + ex.getMessage());
            }
        } catch (final NumberFormatException e) {
            System.err.println("Bad integer arguments: " + e.getMessage());
        }
    }

    /**
     * Format the message like "[prefix][threadInd]_[reqInd]"
     *
     * @param prefix   the prefix
     * @param theadInd the thread index
     * @param reqInd   the request index
     * @return the formatted message
     */
    protected static byte[] formatMessage(final String prefix, final int theadInd, final int reqInd) {
        return "%s%d_%d".formatted(prefix, theadInd, reqInd).getBytes(Utils.CHARSET);
    }

    /**
     * Logs the successful receiving of the message
     *
     * @param message the received message
     */
    protected void logReceiveSuccessfully(final String message) {
        logger.info("Successfully received packet with message: \"%s\"".formatted(message));
    }

    /**
     * Logs the receiving of the invalid message
     *
     * @param message the received message
     */
    protected void logReceiveInvalid(final String message) {
        logger.warn("Received packet with invalid message: \"%s\"".formatted(message));
    }

    /**
     * Logs the successful send of the message
     *
     * @param message the send message
     */
    protected void logSendSuccessfully(final String message) {
        logger.info("Successfully send packet with message: \"%s\"".formatted(message));
    }
}
