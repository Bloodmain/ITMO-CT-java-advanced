package info.kgeorgiy.ja.dunaev.hello;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Server that uses UDP protocol and non-blocking operations to receive packets and to send them with specified formats.
 *
 * @author Dunaev Kirill
 */
public class HelloUDPNonblockingServer extends AbstractServer {
    private Selector selector;
    private SendQueue sendQueue;

    /**
     * Creates server. The server should be started with {@link #start(int, Map)}.
     */
    public HelloUDPNonblockingServer() {
    }

    @Override
    protected void setup(Map<Integer, String> ports) throws IOException {
        sendQueue = new SendQueue();
        listeners = Executors.newSingleThreadExecutor();
        selector = Selector.open();

        for (Map.Entry<Integer, String> port : ports.entrySet()) {
            DatagramChannel channel = DatagramChannel.open();
            try {
                channel.configureBlocking(false);
                channel.bind(new InetSocketAddress(port.getKey()));

                SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
                key.attach(new PortOptions(port.getValue(), channel.socket().getReceiveBufferSize()));
            } catch (final IOException e) {
                channel.close();
                throw e;
            }
        }

        listeners.submit(() -> listen(this::listenWork));
    }

    private void listenWork() throws IOException {
        selector.select();
        Utils.processKeys(selector, this::sendData, this::receiveData);
    }

    private void receiveData(SelectionKey key, DatagramChannel channel) {
        PortOptions options = (PortOptions) key.attachment();
        ByteBuffer buffer = ByteBuffer.allocate(options.bufferSize());

        try {
            SocketAddress addr = channel.receive(buffer);
            buffer.flip();
            handlersService.submit(() -> processQuery(addr, buffer, key, options.format()));
        } catch (final IOException e) {
            logger.error("Error receiving a packet, ignoring", e);
        }
    }

    private void sendData(SelectionKey key, DatagramChannel channel) {
        SendPacket packet = sendQueue.pollPacket(key);
        if (packet == null) {
            return;
        }

        try {
            channel.send(packet.buffer(), packet.addr());
        } catch (IOException e) {
            logger.error("Error sending the answer, ignoring", e);
        }
    }

    private void processQuery(SocketAddress addr, ByteBuffer buffer, SelectionKey key, String format) {
        byte[] res = replaceFormat(Utils.getMessage(buffer), format);
        buffer.clear();
        buffer.put(res);
        buffer.flip();

        sendQueue.addPacket(new SendPacket(buffer, addr), key);
        selector.wakeup();
    }

    @Override
    protected void closeResources() {
        Utils.closeSelector(selector, logger);
    }

    private record SendPacket(ByteBuffer buffer, SocketAddress addr) {
    }

    private record PortOptions(String format, int bufferSize) {
    }

    private static class SendQueue {
        private final Queue<SendPacket> packetsToSend = new ArrayDeque<>();

        private synchronized void addPacket(SendPacket packet, SelectionKey key) {
            packetsToSend.add(packet);
            sendInterest(key, true);
        }

        private synchronized SendPacket pollPacket(SelectionKey key) {
            SendPacket packet = packetsToSend.poll();
            if (packet == null) {
                sendInterest(key, false);
            }
            return packet;
        }

        private static void sendInterest(SelectionKey key, boolean interest) {
            if (interest) {
                key.interestOpsOr(SelectionKey.OP_WRITE);
            } else {
                key.interestOpsAnd(~SelectionKey.OP_WRITE);
            }
        }
    }

    /**
     * CLI for server.
     * Usage: <pre>{@code HelloUDPNonblockingServer <port> <threads>}</pre>
     * where {@code port} - port to listen, {@code thread} - number of query workers
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        serverMain(args, HelloUDPNonblockingServer::new);
    }
}
