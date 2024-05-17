package info.kgeorgiy.ja.dunaev.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.*;

/**
 * Server that uses UDP protocol to receive packets and to send them with specified formats.
 *
 * @author Dunaev Kirill
 */
public class HelloUDPServer extends AbstractServer {
    private final List<DatagramSocket> sockets = new ArrayList<>();

    /**
     * Creates server. The server should be started with {@link #start(int, Map)}.
     */
    public HelloUDPServer() {
    }

    @Override
    protected void setup(final Map<Integer, String> ports) throws IOException {
        listeners = Executors.newFixedThreadPool(ports.size());

        for (final Map.Entry<Integer, String> port : ports.entrySet()) {
            final DatagramSocket socket = new DatagramSocket(port.getKey());
            sockets.add(socket);

            final int bufferSize = socket.getReceiveBufferSize();
            listeners.execute(() -> listen(() -> listenWork(socket, port.getValue(), bufferSize)));
        }
    }

    private void listenWork(final DatagramSocket socket, final String format, final int bufferSize) throws IOException {
        final DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);
        socket.receive(packet);
        handlersService.submit(() -> processQuery(packet, socket, format));
    }

    private void processQuery(final DatagramPacket query, final DatagramSocket socket, final String format) {
        try {
            query.setData(replaceFormat(Utils.getMessage(query), format));
            socket.send(query);
        } catch (final IOException e) {
            logger.error("Error sending the answer, ignoring", e);
        }
    }

    @Override
    protected void closeResources() {
        sockets.forEach(DatagramSocket::close);
        sockets.clear();
    }

    /**
     * CLI for server.
     * Usage: <pre>{@code HelloUDPServer <port> <threads>}</pre>
     * where {@code port} - port to listen, {@code thread} - number of query workers
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        serverMain(args, HelloUDPServer::new);
    }
}
