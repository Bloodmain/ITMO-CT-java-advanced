package info.kgeorgiy.ja.dunaev.bank.internal;

/**
 * Remote implementation of the {@link Account}.
 *
 * @author Dunaev Kirill
 */
public class RemoteAccount implements Account {
    private long amount;
    private final String id;
    private static final long DEFAULT_AMOUNT = 0;

    /**
     * Creates new account with {@value DEFAULT_AMOUNT} amount.
     *
     * @param id account's id
     */
    public RemoteAccount(String id) {
        this.id = id;
        this.amount = DEFAULT_AMOUNT;
    }

    /**
     * Copy constructor. Copies the id and the amount.
     *
     * @param account account to copy
     */
    public RemoteAccount(RemoteAccount account) {
        this.id = account.getId();
        this.amount = account.getAmount();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized long getAmount() {
        return amount;
    }

    @Override
    public synchronized void updateAmount(long delta) throws AccountException {
        try {
            long newAmount = Math.addExact(amount, delta);
            if (newAmount < 0) {
                throw new AccountException(
                        "Trying to update amount to negative value (amount: %d, delta: %d)".formatted(amount, delta)
                );
            }
            amount = newAmount;
        } catch (final ArithmeticException e) {
            throw new AccountException("Amount exceeded limits (amount: %d, delta: %d)".formatted(amount, delta), e);
        }
    }
}
