package info.kgeorgiy.ja.dunaev.bank.internal;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Local implementation of the {@link Person}.
 * The class copies accounts in the constructor, so it can be used as a local snapshot of a {@link RemotePerson}
 *
 * @author Dunaev Kirill
 */
public class LocalPerson extends AbstractPerson implements Serializable {
    /**
     * Creates a person with the given data. Copies provided accounts.
     *
     * @param firstName person's first name
     * @param lastName  person's last name
     * @param passport  person's passport
     * @param copyData  account to copy
     */
    public LocalPerson(String firstName, String lastName, String passport, Map<String, RemoteAccount> copyData) {
        super(firstName, lastName, passport);
        copyData.forEach((key, value) -> accounts.put(key, new RemoteAccount(value)));
    }

    @Override
    public Account createAccount(String subId) throws RemoteException {
        return accounts.computeIfAbsent(subId, i -> createNewAccount(subId));
    }
}
