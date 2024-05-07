package info.kgeorgiy.ja.dunaev.bank;

import info.kgeorgiy.ja.dunaev.bank.internal.Account;
import info.kgeorgiy.ja.dunaev.bank.internal.AccountException;
import info.kgeorgiy.ja.dunaev.bank.internal.Bank;
import info.kgeorgiy.ja.dunaev.bank.internal.Person;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Simple bank client that tries to get bank, person and account from remote, using provided arguments.
 *
 * @author Dunaev Kirill
 */
public class BankApp {
    /**
     * CLI for client. Usage:
     * <pre> {@code BankClient <first name> <last name> <passport> <account id> <amount delta>}</pre>
     *
     * @param args command line argument
     */
    public static void main(final String[] args) {
        Objects.requireNonNull(args);
        Arrays.stream(args).forEach(Objects::requireNonNull);

        if (args.length != 5) {
            System.err.println("Usage: BankClient <first name> <last name> <passport> <account id> <amount delta>");
            return;
        }

        final String firstName = args[0];
        final String lastName = args[1];
        final String passport = args[2];
        final String subId = args[3];
        final long delta;
        try {
            delta = Long.parseLong(args[4]);
        } catch (final NumberFormatException e) {
            System.err.println("Invalid amount delta: " + args[4]);
            return;
        }

        final Bank bank;
        try {
            bank = (Bank) Naming.lookup(BankServer.BANK_BIND_URL);
        } catch (final NotBoundException e) {
            System.err.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.err.println("Bank URL is invalid");
            return;
        } catch (RemoteException e) {
            System.err.println("Can't get bank from remote: " + e.getMessage());
            return;
        }

        Person person;
        try {
            person = bank.getPersonByPassport(passport, Bank.PersonType.REMOTE);
            if (person == null) {
                System.out.println("Creating person");
                person = bank.createPerson(firstName, lastName, passport, Bank.PersonType.REMOTE);
            } else {
                System.out.println("Person already exists, validating data");
                try {
                    assertEquals(person.getFirstName(), firstName, "first name");
                    assertEquals(person.getLastName(), lastName, "last name");
                } catch (final IllegalStateException e) {
                    System.err.println("Invalid person's data: " + e.getMessage());
                    return;
                }
                System.out.println("Data has been validated");
            }
        } catch (final RemoteException e) {
            System.err.println("Can't get person from remote: " + e.getMessage());
            return;
        }

        Account account;
        try {
            account = person.getAccount(subId);
            if (account == null) {
                System.out.println("Creating account");
                account = person.createAccount(subId);
            } else {
                System.out.println("Account already exists");
            }
        } catch (final RemoteException e) {
            System.err.println("Can't get account from remote: " + e.getMessage());
            return;
        }

        try {
            System.out.println("Account id: " + account.getId());
            System.out.println("Money: " + account.getAmount());
            System.out.printf("Adding money (%d)%n", delta);
            try {
                account.updateAmount(delta);
            } catch (AccountException e) {
                System.err.println("Can't update account's amount: " + e.getMessage());
                return;
            }
            System.out.println("Money now: " + account.getAmount());
        } catch (final RemoteException e) {
            System.err.println("Error while accessing account's amount: " + e.getMessage());
        }
    }

    private static void assertEquals(String a, String b, String ctx) {
        if (!a.equals(b)) {
            throw new IllegalStateException("Invalid %s: \"%s\" doesn't match against \"%s\"".formatted(ctx, a, b));
        }
    }
}
