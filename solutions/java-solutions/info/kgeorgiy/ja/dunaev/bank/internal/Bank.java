package info.kgeorgiy.ja.dunaev.bank.internal;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents the bank.
 *
 * @author Dunaev Kirill
 */
public interface Bank extends Remote, Serializable {
    /**
     * Find and get the person with specified type by passport.
     *
     * @param passport passport to find
     * @param type     type of the person
     * @return the found person or null if not found
     * @throws RemoteException if remote server error occurred
     */
    Person getPersonByPassport(String passport, PersonType type) throws RemoteException;

    /**
     * Creates a person. If the person with the specified passport exists, returns it.
     * Otherwise, creates a new person with the given data.
     *
     * @param firstName a first name of a person
     * @param lastName  a last name of a person
     * @param passport  a passport of a person
     * @param type      a type of person to create
     * @return the created person
     * @throws RemoteException if remote server error occurred
     */
    Person createPerson(String firstName, String lastName, String passport, PersonType type) throws RemoteException;

    /**
     * Makes a transaction from account with {@code id1} to an account with {@code id2}.
     *
     * @param id1    id of first account
     * @param id2    id of second account
     * @param amount the amount to send
     * @throws RemoteException  if remote server error occurred
     * @throws AccountException if one of the account can't be updated on this amount
     */
    void makeTransaction(String id1, String id2, long amount) throws RemoteException, AccountException;


    /**
     * Type of the persons.
     * <ul>
     * <li> {@code LOCAL} is for {@link LocalPerson}</li>
     * <li> {@code REMOTE} is for {@link RemotePerson}</li>
     * </ul>
     */
    enum PersonType {
        LOCAL, REMOTE
    }
}
