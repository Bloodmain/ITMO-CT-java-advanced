package info.kgeorgiy.ja.dunaev.bank.internal;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Remote implementation of {@link Bank}.
 *
 * @author Dunaev Kirill
 */
public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, RemotePerson> persons = new ConcurrentHashMap<>();

    /**
     * Creates the bank. New persons in this bank would be exported on the given port.
     *
     * @param port port to export persons
     */
    public RemoteBank(int port) {
        this.port = port;
    }

    @Override
    public Person getPersonByPassport(String passport, PersonType type) throws RemoteException {
        return switch (type) {
            case REMOTE -> getRemotePersonByPassport(passport);
            case LOCAL -> getLocalPersonByPassport(passport);
        };
    }

    private RemotePerson getRemotePersonByPassport(String passport) throws RemoteException {
        return persons.get(passport);
    }

    private LocalPerson getLocalPersonByPassport(String passport) throws RemoteException {
        Person remotePerson = getRemotePersonByPassport(passport);
        if (remotePerson == null) {
            return null;
        }
        return new LocalPerson(
                remotePerson.getFirstName(), remotePerson.getLastName(),
                remotePerson.getPassport(), remotePerson.getAllAccounts()
        );
    }

    @Override
    public Person createPerson(String firstName, String lastName, String passport, PersonType type) throws RemoteException {
        return switch (type) {
            case REMOTE -> AbstractPerson.computeExporting(
                    persons, passport,
                    () -> new RemotePerson(firstName, lastName, passport, port), port
            );

            case LOCAL -> {
                Person remotePerson = getRemotePersonByPassport(passport);
                yield new LocalPerson(
                        firstName, lastName, passport,
                        remotePerson == null ? Collections.emptyMap() : remotePerson.getAllAccounts()
                );
            }
        };
    }

    private Account getAccount(String id) throws RemoteException {
        String[] idSplit = id.split(":");
        return getRemotePersonByPassport(idSplit[0]).getAccount(idSplit[1]);
    }

    @Override
    public synchronized void makeTransaction(String id1, String id2, long amount) throws RemoteException, AccountException {
        Account acc1 = getAccount(id1);
        Account acc2 = getAccount(id2);

        acc1.updateAmount(-amount);
        acc2.updateAmount(amount);
    }
}
