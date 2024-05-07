package info.kgeorgiy.ja.dunaev.bank.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * Abstract implementation of {@link Person}.
 *
 * @author Dunaev Kirill
 */
public abstract class AbstractPerson implements Person {
    /**
     * Accounts storage.
     */
    protected final ConcurrentMap<String, RemoteAccount> accounts = new ConcurrentHashMap<>();

    /**
     * Person's first name.
     */
    protected final String firstName;
    /**
     * Person's last name.
     */
    protected final String lastName;
    /**
     * Person's passport.
     */
    protected final String passport;

    /**
     * Creates a person with the given data.
     *
     * @param firstName person's first name
     * @param lastName  person's last name
     * @param passport  person's passport
     */
    protected AbstractPerson(String firstName, String lastName, String passport) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passport = passport;
    }

    @Override
    public String getFirstName() throws RemoteException {
        return firstName;
    }

    @Override
    public String getLastName() throws RemoteException {
        return lastName;
    }

    @Override
    public String getPassport() throws RemoteException {
        return passport;
    }

    @Override
    public Account getAccount(String subId) throws RemoteException {
        return accounts.get(subId);
    }

    @Override
    public Map<String, RemoteAccount> getAllAccounts() throws RemoteException {
        return accounts;
    }

    /**
     * Creates new {@link RemoteAccount} instance.
     *
     * @param subId account's sub id.
     * @return created account
     */
    protected RemoteAccount createNewAccount(String subId) {
        return new RemoteAccount("%s:%s".formatted(passport, subId));
    }

    /**
     * Puts the new value in the map if key has no mapping yet. It exports the new value in the same compute,
     * so that this function is thread-safe: if it returned a value, that means it is already exported.
     *
     * @param map      the map to put a value
     * @param key      the key put mapping on
     * @param newValue supplier to get a new value if there is no mapping
     * @param port     port to export new objects
     * @param <K>      map's key type
     * @param <V>      map's value type
     * @return the current value in the map associated with the given key
     * @throws RemoteException if exporting an object threw an exception
     * @implNote the method uses UncheckedRemoteException in compute, catches it and rethrow it outside compute.
     * However, it is also possible to just make this function synchronized.
     */
    public static <K, V extends Remote> V computeExporting(
            ConcurrentMap<K, V> map,
            K key,
            Supplier<V> newValue,
            int port
    ) throws RemoteException {
        try {
            return map.compute(key, (k, v) -> {
                if (v == null) {
                    v = newValue.get();
                    try {
                        UnicastRemoteObject.exportObject(v, port);
                    } catch (final RemoteException e) {
                        throw new UncheckedRemoteException(e);
                    }
                }
                return v;
            });
        } catch (final UncheckedRemoteException e) {
            throw e.getCause();
        }
    }
}
