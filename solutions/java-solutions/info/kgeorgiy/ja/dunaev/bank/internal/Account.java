package info.kgeorgiy.ja.dunaev.bank.internal;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents the person's account.
 *
 * @author Dunaev Kirill
 */
public interface Account extends Remote, Serializable {
    /**
     * Returns the id of the account.
     *
     * @return the id of the account
     * @throws RemoteException if remote server error occurred
     */
    String getId() throws RemoteException;

    /**
     * Returns the amount of the account.
     *
     * @return the amount of the account
     * @throws RemoteException if remote server error occurred
     */
    long getAmount() throws RemoteException;

    /**
     * Updates the amount of the account with the given delta.
     *
     * @param delta update's delta
     * @throws AccountException if update action can't be performed
     * @throws RemoteException  if remote server error occurred
     */
    void updateAmount(long delta) throws RemoteException, AccountException;
}
