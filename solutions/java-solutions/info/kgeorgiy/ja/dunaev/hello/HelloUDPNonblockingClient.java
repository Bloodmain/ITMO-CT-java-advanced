package info.kgeorgiy.ja.dunaev.hello;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * Client that uses UDP protocol and non-blocking operations to a server and to print received data.
 *
 * @author Dunaev Kirill
 */
public class HelloUDPNonblockingClient extends AbstractClient {
    /**
     * Creates client. The client should be started with {@link #run(String, int, String, int, int)}.
     */
    public HelloUDPNonblockingClient() {
    }

    /**
     * {@inheritDoc}
     *
     * @throws UncheckedIOException if an error occurred when getting address or creating socket
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        InetAddress addr = getHost(host);
        Selector selector = openSelector();

        for (int i = 0; i < threads; i++) {
            DatagramChannel channel = openChannel(selector);
            try {
                channel.configureBlocking(false);
                SelectionKey key = channel.register(selector, SelectionKey.OP_WRITE);
                key.attach(new TaskContext(i + 1, 1,
                        channel.socket().getReceiveBufferSize(), prefix, requests)
                );
            } catch (final IOException e) {
                Utils.closeChannel(channel, logger);
                Utils.closeSelector(selector, logger);
                throw new UncheckedIOException(e);
            }
        }

        try {
            selectTask(selector, new InetSocketAddress(addr, port));
        } finally {
            Utils.closeSelector(selector, logger);
        }
    }

    private Selector openSelector() {
        try {
            return Selector.open();
        } catch (final IOException e) {
            logger.error("Cannot open selector", e);
            throw new UncheckedIOException(e);
        }
    }

    private DatagramChannel openChannel(Selector selector) {
        try {
            return DatagramChannel.open();
        } catch (final IOException e) {
            logger.error("Cannot open channel", e);
            Utils.closeSelector(selector, logger);
            throw new UncheckedIOException(e);
        }
    }

    private void selectTask(Selector selector, InetSocketAddress serverAddr) {
        while (selector.keys().stream().anyMatch(SelectionKey::isValid)) {
            try {
                if (selector.select(TIMEOUT) == 0) {
                    selector.keys().forEach(k -> k.interestOps(SelectionKey.OP_WRITE));
                }
                Utils.processKeys(selector, (k, c) -> sendData(k, c, serverAddr), this::receiveData);
            } catch (final IOException e) {
                logger.error("Error while selecting task", e);
                throw new UncheckedIOException(e);
            }
        }
    }

    private void sendData(SelectionKey key, DatagramChannel channel, InetSocketAddress serverAddr) {
        TaskContext ctx = (TaskContext) key.attachment();

        try {
            ctx.sendBuffer.flip();
            channel.send(ctx.sendBuffer, serverAddr);
        } catch (final IOException e) {
            logger.error("Can't send packet", e);
            return;
        }
        logSendSuccessfully(Utils.getMessage(ctx.sendBuffer));
        key.interestOps(SelectionKey.OP_READ);
    }

    private void receiveData(SelectionKey key, DatagramChannel channel) {
        TaskContext ctx = (TaskContext) key.attachment();

        try {
            ctx.receiveBuffer.clear();
            channel.receive(ctx.receiveBuffer);
            ctx.receiveBuffer.flip();
        } catch (final IOException e) {
            logger.error("Can't receive packet", e);
            return;
        }

        String message = Utils.getMessage(ctx.receiveBuffer);
        if (validate(Utils.getMessage(ctx.receiveBuffer), new int[]{ctx.threadIndex, ctx.requestIndex})) {
            logReceiveSuccessfully(message);
            if (!ctx.next()) {
                Utils.closeChannel(key.channel(), logger);
                key.cancel();
                return;
            }

            key.interestOps(SelectionKey.OP_WRITE);
        } else {
            logReceiveInvalid(message);
        }
    }

    private static class TaskContext {
        private final int requests;
        private final String prefix;
        private final int threadIndex;
        private int requestIndex;

        private final ByteBuffer receiveBuffer;
        private final ByteBuffer sendBuffer;

        private TaskContext(int threadIndex, int requestIndex, int bufferSize, String prefix, int requests) {
            this.threadIndex = threadIndex;
            this.requestIndex = requestIndex;
            this.requests = requests;
            this.prefix = prefix;

            receiveBuffer = ByteBuffer.allocate(bufferSize);
            sendBuffer = ByteBuffer.allocate(bufferSize);

            nextMessage();
        }

        private boolean next() {
            requestIndex++;
            if (requestIndex > requests) {
                return false;
            }
            nextMessage();
            return true;
        }

        private void nextMessage() {
            sendBuffer.clear();
            sendBuffer.put(formatMessage(prefix, threadIndex, requestIndex));
        }
    }

    /**
     * CLI for client.
     * Usage: <pre>{@code HelloUDPNonblockingClient <host> <port> <prefix> <threads> <requests>}</pre>
     * where {@code host} name or ip of the server, {@code port} - port, send requests to which,
     * {@code prefix} - prefix of all query, {@code thread} - number of query workers,
     * {@code requests} - number of requests in each thread
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        mainClient(args, HelloUDPNonblockingClient::new);
    }
}
