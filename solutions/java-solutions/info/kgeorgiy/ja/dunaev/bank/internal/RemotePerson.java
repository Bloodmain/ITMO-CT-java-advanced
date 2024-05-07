package info.kgeorgiy.ja.dunaev.bank.internal;

import java.rmi.RemoteException;

/**
 * Remote implementation of the {@link Person}.
 *
 * @author Dunaev Kirill
 */
public class RemotePerson extends AbstractPerson {
    private final int port;

    /**
     * Creates a person with the given data. New account will be exported on the given port.
     *
     * @param firstName person's first name
     * @param lastName  person's last name
     * @param passport  person's passport
     * @param port      port, which new accounts exported on
     */
    public RemotePerson(String firstName, String lastName, String passport, int port) {
        super(firstName, lastName, passport);
        this.port = port;
    }

    @Override
    public Account createAccount(String subId) throws RemoteException {
        return computeExporting(accounts, subId, () -> createNewAccount(subId), port);
    }
}
