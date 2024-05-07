package info.kgeorgiy.ja.dunaev.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Objects;

import info.kgeorgiy.ja.dunaev.bank.internal.Bank;
import info.kgeorgiy.ja.dunaev.bank.internal.RemoteBank;

/**
 * Simple bank server that export bank.
 *
 * @author Dunaev Kirill
 */
public class BankServer {
    private final static int DEFAULT_PORT = 8888;
    /**
     * Url, which the bank will be exported on
     */
    public final static String BANK_BIND_URL = "//localhost/bank";

    /**
     * CLI for server. Usage:
     * <pre> {@code BankServer [<port>]}</pre>
     *
     * @param args command line argument
     */
    public static void main(final String[] args) {
        Objects.requireNonNull(args);
        Arrays.stream(args).forEach(Objects::requireNonNull);
        int port = DEFAULT_PORT;

        try {
            if (args.length > 0) {
                int p = Integer.parseInt(args[0]);
                if (p < 1 || p > 65535) {
                    System.err.println("Invalid port number: " + p);
                    return;
                }
                port = p;
            }
        } catch (final NumberFormatException e) {
            System.err.printf("Invalid port number (%s): %s%n", args[0], e.getMessage());
            return;
        }

        final Bank bank = new RemoteBank(port);
        try {
            UnicastRemoteObject.exportObject(bank, port);
            Naming.rebind(BANK_BIND_URL, bank);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
        }
    }
}
