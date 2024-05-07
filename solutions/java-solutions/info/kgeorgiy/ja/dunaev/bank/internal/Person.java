package info.kgeorgiy.ja.dunaev.bank.internal;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Represents the person.
 *
 * @author Dunaev Kirill
 */
public interface Person extends Remote, Serializable {
    /**
     * Returns the first name of the person.
     *
     * @return the first name of the person
     * @throws RemoteException if remote server error occurred
     */
    String getFirstName() throws RemoteException;

    /**
     * Returns the last name of the person.
     *
     * @return the last name of the person
     * @throws RemoteException if remote server error occurred
     */
    String getLastName() throws RemoteException;

    /**
     * Returns the passport of the person.
     *
     * @return the passport of the person
     * @throws RemoteException if remote server error occurred
     */
    String getPassport() throws RemoteException;

    /**
     * Returns the account with the given id or null if none.
     *
     * @param subId account's id
     * @return the found account
     * @throws RemoteException if remote server error occurred
     */
    Account getAccount(String subId) throws RemoteException;

    /**
     * Returns person's all accounts.
     *
     * @return person's all accounts
     * @throws RemoteException if remote server error occurred
     */
    Map<String, RemoteAccount> getAllAccounts() throws RemoteException;

    /**
     * Creates an account. If the account with the specified id exists, returns it.
     * Otherwise, creates a new account.
     *
     * @param subId account's id
     * @return the created account
     * @throws RemoteException if remote server error occurred
     */
    Account createAccount(String subId) throws RemoteException;
}
