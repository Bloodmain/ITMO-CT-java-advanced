package info.kgeorgiy.ja.dunaev.bank.internal;

import java.rmi.RemoteException;

/**
 * Runtime version of {@link java.rmi.RemoteException}.
 *
 * @author Dunaev Kirill
 */
public class UncheckedRemoteException extends RuntimeException {
    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause the cause
     */
    public UncheckedRemoteException(RemoteException cause) {
        super(cause);
    }

    /**
     * Returns the cause of this exception.
     *
     * @return the {@code RemoteException} which is the cause of this exception.
     */
    @Override
    public RemoteException getCause() {
        return (RemoteException) super.getCause();
    }
}
