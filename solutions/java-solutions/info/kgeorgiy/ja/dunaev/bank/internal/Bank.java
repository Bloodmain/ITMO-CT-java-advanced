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
