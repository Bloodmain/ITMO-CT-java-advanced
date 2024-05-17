package info.kgeorgiy.ja.dunaev.hello;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Client that uses UDP protocol to send datagrams to a server and to print received data.
 *
 * @author Dunaev Kirill
 */
public class HelloUDPClient extends AbstractClient {
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
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        final InetAddress address = getHost(host);
        try (final ExecutorService service = Executors.newFixedThreadPool(threads)) {
            IntStream.range(1, threads + 1)
                    .<Runnable>mapToObj(i -> () -> sendTask(prefix, requests, port, address, i))
                    .forEach(service::execute);
        }
    }

    private void sendTask(final String prefix, final int requests, final int port, final InetAddress address, final int thread) {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT);
            final int bufferSize = socket.getReceiveBufferSize();

            final DatagramPacket receivePacket = new DatagramPacket(new byte[bufferSize], bufferSize);
            final DatagramPacket sendPacket = new DatagramPacket(new byte[0], 0, address, port);

            for (int i = 1; i <= requests; i++) {
                sendPacket.setData(formatMessage(prefix, thread, i));
                final int[] originInts = new int[]{thread, i};
                while (true) {
                    if (sendAndReceive(socket, sendPacket, receivePacket, originInts)) {
                        break;
                    }
                }
            }
        } catch (final SocketException e) {
            logger.error("Can't create socket", e);
            throw new UncheckedIOException(e);
        }
    }

    private boolean sendAndReceive(final DatagramSocket socket, final DatagramPacket packet, final DatagramPacket receivePacket, final int[] originInts) {
        try {
            socket.send(packet);
            logSendSuccessfully(Utils.getMessage(packet));
        } catch (final IOException e) {
            logger.error("Can't send packet", e);
            return false;
        }

        try {
            socket.receive(receivePacket);
            final String receivedMessage = Utils.getMessage(receivePacket);
            if (validate(receivedMessage, originInts)) {
                logReceiveSuccessfully(receivedMessage);
                return true;
            } else {
                logReceiveInvalid(receivedMessage);
                return false;
            }
        } catch (final IOException e) {
            logger.error("Can't receive packet", e);
            return false;
        }
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
    public static void main(final String[] args) {
        mainClient(args, HelloUDPClient::new);
    }
}
