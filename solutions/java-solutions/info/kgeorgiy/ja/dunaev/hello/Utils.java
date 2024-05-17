package info.kgeorgiy.ja.dunaev.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.function.BiConsumer;

/**
 * Class for some util methods for UDP servers and clients.
 *
 * @author Dunaev Kirill
 */
public class Utils {
    /**
     * Max value that can represent a port
     */
    public static final int MAX_PORT = 65535;

    /**
     * UTF-8 charset
     */
    public static final Charset CHARSET = StandardCharsets.UTF_8;

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
    public static int inBoundsOrThrow(final String ctx, final int lower, final int upper, final int v) {
        if (v < lower || v > upper) {
            throw new IllegalArgumentException(ctx + v);
        }
        return v;
    }

    /**
     * Process the selector's keys. The {@link Selector#select()} method should have been already invoked.
     *
     * @param selector the selector
     * @param sendOp   an operation to do with the writable key
     * @param recOp    an operation to do with the readable key
     */
    public static void processKeys(
            Selector selector,
            BiConsumer<SelectionKey, DatagramChannel> sendOp,
            BiConsumer<SelectionKey, DatagramChannel> recOp
    ) {
        for (final Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
            final SelectionKey key = it.next();
            try {
                DatagramChannel channel = (DatagramChannel) key.channel();
                if (key.isWritable()) {
                    sendOp.accept(key, channel);
                }
                if (key.isReadable()) {
                    recOp.accept(key, channel);
                }
            } finally {
                it.remove();
            }
        }
    }

    /**
     * Gets the message which is contained in the buffer.
     *
     * @param buffer the buffer to process
     * @return the message
     */
    public static String getMessage(ByteBuffer buffer) {
        return new String(buffer.array(), buffer.arrayOffset(), buffer.limit(), CHARSET);
    }

    /**
     * Return message from the packet.
     *
     * @param packet the packet to get message from
     * @return the message
     */
    public static String getMessage(final DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength(), CHARSET);
    }

    /**
     * Closes the given selector and its channels, logging the error if one.
     *
     * @param selector the selector to close
     * @param logger   the logger
     */
    public static void closeSelector(Selector selector, Logger logger) {
        try {
            for (SelectionKey key : selector.keys()) {
                closeChannel(key.channel(), logger);
            }
            selector.close();
        } catch (final IOException e) {
            logger.error("Error closing selector", e);
        }
    }

    /**
     * Closes the given channel, logging the error if one.
     *
     * @param channel the channel to close
     * @param logger  the logger
     */
    public static void closeChannel(Channel channel, Logger logger) {
        try {
            channel.close();
        } catch (final IOException e) {
            logger.error("Error closing channel", e);
        }
    }
}
