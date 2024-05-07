package info.kgeorgiy.ja.dunaev.bank;

import info.kgeorgiy.ja.dunaev.bank.internal.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * Tests for {@link info.kgeorgiy.ja.dunaev.bank.internal} and {@link BankApp}.
 *
 * @author Dunaev Kirill
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class BankTests {
    private static final Random RANDOM = new Random(297562875629470103L);
    private static final String BANK_BIND_URL = "//localhost/bank";
    private static final int BANK_PORT = 1808;

    private static final int THREADS = 10;
    private static final int MAJOR_PER_THREAD = 1000;
    private static final int DELTA_BOUND = 10000;

    private Bank bank;

    private record PersonalInfo(String firstName, String lastName, String passport) {
    }

    private static final List<PersonalInfo> INFOS =
            List.of(
                    new PersonalInfo("Alisa", "Koton", "097"),

                    new PersonalInfo("Kirill", "Dunaev", "367205"),
                    new PersonalInfo("Alena", "Dubinina", "11122004"),
                    new PersonalInfo("Alexander", "Sergeev", "554434"),
                    new PersonalInfo("Maksim", "Nesmeshutkin", "000000"),
                    new PersonalInfo("Nikita", "Nebabin", "123456"),
                    new PersonalInfo("Georgiy", "Sitkin", "1"),
                    new PersonalInfo("Dmitriy", "Gedelized", "10023492429423423598243234234235234534")
            );

    private static final List<String> SUB_IDS =
            List.of("0", "982725", "10", "80175982375923759238572938572389", "000", "382482", "77720", "018743");

    private static final List<PersonalInfo> WEIRD_INFOS =
            List.of(
                    new PersonalInfo("Алёна", "Макаревич", "[&](){return \"123123\";}();"),
                    new PersonalInfo("Καληνύχτα", "行って来ます", "what?, string passport:D"),
                    new PersonalInfo("\uD83D\uDC69\uD83C\uDFFD\u200D\uD83E\uDDBD\n" +
                            "\uD83D\uDC69\uD83C\uDFFD\u200D\uD83E\uDDBD\u200D➡\uFE0F\n" +
                            "\uD83E\uDDD1\uD83C\uDFFD\u200D\uD83E\uDDBD\n",
                            "👨🏽‍🦯", " "),
                    new PersonalInfo("", "", ""),
                    new PersonalInfo("DROP TABLE User;", "System.exit(-1);", "return 1;")
            );

    private static final List<String> WEIRD_SUB_IDS =
            List.of("Hey, you!", "    ", "\n", "-10", "-00023", "0.23", "DROP TABlE Users;",
                    "\uD83E\uDD11\uD83E\uDD20\uD83D\uDE08\uD83D\uDC7F\uD83D\uDCA9☠\uFE0F",
                    "Руссы против...", "Ich heisße…", "اِسْتِقْلَالِيَّةٌ");

    private static void log(String message) {
        System.out.println(message);
    }

    @BeforeAll
    public static void setupRMI() {
        log("Trying to create RMI registry");
        try {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            log("RMI registry created");
        } catch (final RemoteException e) {
            log("Can't create RMI registry on the 'well-known' port, supposing it's already started");
        }
    }

    @BeforeEach
    public void setupBankServer() throws RemoteException, MalformedURLException, NotBoundException {
        log("Starting a bank server");
        final Bank bank = new RemoteBank(BANK_PORT);
        UnicastRemoteObject.exportObject(bank, BANK_PORT);
        Naming.rebind(BANK_BIND_URL, bank);
        log("Server started");

        log("Getting a remote bank");
        this.bank = (Bank) Naming.lookup(BankServer.BANK_BIND_URL);
        log("Remote bank found");
    }

    private Person createPerson(PersonalInfo info, Bank.PersonType type) throws RemoteException {
        return bank.createPerson(info.firstName, info.lastName, info.passport, type);
    }

    private Person createRemotePerson(PersonalInfo info) throws RemoteException {
        return createPerson(info, Bank.PersonType.REMOTE);
    }

    private void addDeltaFail(Account account, long was, long delta, String ctx) throws RemoteException {
        Assertions.assertThrows(AccountException.class, () -> account.updateAmount(delta), ctx);
        Assertions.assertEquals(was, account.getAmount(), "Account amount should not be updated after exception");
    }

    private long addDeltaOkay(Account account, long was, long delta) throws RemoteException {
        Assertions.assertDoesNotThrow(() -> account.updateAmount(delta));
        long amount = account.getAmount();
        Assertions.assertEquals(was + delta, amount,
                "Account amount (%d) doesn't match expected (%d)".formatted(amount, was + delta));

        return delta;
    }

    private long getRandomDelta() {
        return RANDOM.nextInt(DELTA_BOUND);
    }

    private long addRandomDelta(Account account, long was) throws RemoteException {
        long delta = getRandomDelta();
        return addDeltaOkay(account, was, delta);
    }

    @Test
    public void test01_defaultValues() throws RemoteException {
        Assertions.assertNull(bank.getPersonByPassport(INFOS.getFirst().passport, Bank.PersonType.REMOTE));
        Person person = createRemotePerson(INFOS.getFirst());
        String subId = SUB_IDS.getFirst();

        Assertions.assertNull(person.getAccount(subId));
        Assertions.assertEquals(0, person.createAccount(subId).getAmount(), "Default account's amount should be 0");
    }

    @Test
    public void test02_increasing() throws RemoteException {
        Person person = createRemotePerson(INFOS.getFirst());
        String subId = SUB_IDS.getFirst();

        long amount = 0;
        Account account = person.createAccount(subId);
        for (int i = 0; i < 100; ++i) {
            amount += addRandomDelta(account, amount);
        }

        Assertions.assertEquals(amount, person.createAccount(subId).getAmount(),
                "Created person account's amount doesn't match expected");
    }

    @Test
    public void test03_negativeException() throws RemoteException {
        Person person = createRemotePerson(INFOS.getFirst());
        String subId = SUB_IDS.getFirst();

        Account account = person.createAccount(subId);
        addDeltaFail(account, 0, -getRandomDelta() - 13, "Expected exception due to negative value count");
        addDeltaFail(account, 0, Long.MIN_VALUE, "Expected exception due to negative value count");
    }

    @Test
    public void test04_decreasing() throws RemoteException {
        Person person = createRemotePerson(INFOS.getFirst());
        String subId = SUB_IDS.getFirst();

        Account account = person.createAccount(subId);
        long amount = addRandomDelta(account, 0);

        while (true) {
            long delta = -RANDOM.nextInt(10);
            if (amount + delta >= 0) {
                amount += addDeltaOkay(account, amount, delta);
            } else {
                addDeltaFail(account, amount, delta, "Expected exception due to negative value count");
                break;
            }
        }
    }

    @Test
    public void test05_limits() throws RemoteException {
        Person person = createRemotePerson(INFOS.getFirst());
        String subId = SUB_IDS.getFirst();

        Account account = person.createAccount(subId);

        addDeltaOkay(account, 0, Long.MAX_VALUE);
        addDeltaFail(account, Long.MAX_VALUE, 1, "Expected exception due to exceeding long");
        addDeltaFail(account, Long.MAX_VALUE, Long.MAX_VALUE, "Expected exception due to exceeding long");
    }

    public <T> void concurrentTest(IntFunction<Callable<T>> threadAction) throws InterruptedException, ExecutionException {
        try (ExecutorService service = Executors.newFixedThreadPool(THREADS)) {
            log("Running on %d threads".formatted(THREADS));

            List<Callable<T>> callables = IntStream.range(0, THREADS).mapToObj(threadAction).toList();

            for (var f : service.invokeAll(callables)) {
                f.get();
            }
        }
    }

    @Test
    public void test06_concurrentUpdate() throws RemoteException, InterruptedException, ExecutionException {
        PersonalInfo info = INFOS.getFirst();
        String subId = SUB_IDS.getFirst();

        List<Integer> deltas = RANDOM.ints(THREADS * MAJOR_PER_THREAD, 0, DELTA_BOUND).boxed().toList();

        concurrentTest(i -> () -> {
            Person person = createRemotePerson(info);
            Account account = person.createAccount(subId);

            int localSum = 0;
            for (int j = 0; j < MAJOR_PER_THREAD; j++) {
                int delta = deltas.get(i * MAJOR_PER_THREAD + j);
                localSum += delta;
                Assertions.assertDoesNotThrow(() -> account.updateAmount(delta));
            }

            log("Thread %d is done summing %d".formatted(i, localSum));
            return null;
        });

        long amount = createRemotePerson(info).getAccount(subId).getAmount();
        int expectedAmount = deltas.stream().mapToInt(Integer::intValue).sum();

        Assertions.assertEquals(expectedAmount, amount, "Bad amount for account " + subId);
    }

    Map<String, Long> createAccounts(Person person, List<String> subIds) throws RemoteException {
        Map<String, Long> amounts = new HashMap<>();

        for (String subId : subIds) {
            Account acc = person.createAccount(subId);
            amounts.put(subId, addRandomDelta(acc, 0));
        }

        return amounts;
    }

    @Test
    public void test10_multipleAccounts() throws RemoteException {
        Person person = createRemotePerson(INFOS.getFirst());

        List<String> subIdsCopy = new ArrayList<>(SUB_IDS);
        Map<String, Long> amounts = createAccounts(person, subIdsCopy);

        Collections.shuffle(subIdsCopy);
        for (String subId : subIdsCopy) {
            Account acc = person.createAccount(subId);
            amounts.replace(subId, addRandomDelta(acc, amounts.get(subId)));
        }
    }

    private void testIds(PersonalInfo info, List<String> sub_ids) throws RemoteException {
        Person person = createRemotePerson(info);

        for (String subId : sub_ids) {
            Account acc = person.createAccount(subId);
            addRandomDelta(acc, 0);
            Assertions.assertEquals("%s:%s".formatted(info.passport, subId), person.getAccount(subId).getId());
        }
    }

    @Test
    public void test11_ids() throws RemoteException {
        for (PersonalInfo info : INFOS) {
            testIds(info, SUB_IDS);
        }
    }

    @Test
    public void test12_weirdAccounts() throws RemoteException {
        for (PersonalInfo info : WEIRD_INFOS) {
            testIds(info, WEIRD_SUB_IDS);
        }
    }

    @Test
    void test13_allAccounts() throws RemoteException {
        Person person = createRemotePerson(INFOS.getFirst());
        Map<String, Long> amounts = createAccounts(person, SUB_IDS);

        Map<String, RemoteAccount> allAccounts = person.getAllAccounts();
        for (var entry : allAccounts.entrySet()) {
            long amount = amounts.get(entry.getKey());
            long delta = addRandomDelta(entry.getValue(), amount);
            Assertions.assertEquals(amount + delta, person.getAccount(entry.getKey()).getAmount(),
                    "Account from getAllAccounts doesn't update the remote account's amount");
        }
    }

    @Test
    public void test14_concurrentAccounts() throws RemoteException, InterruptedException, ExecutionException {
        PersonalInfo info = INFOS.getFirst();

        List<String> sub_ids = RANDOM.ints(THREADS * MAJOR_PER_THREAD, 0, SUB_IDS.size())
                .mapToObj(SUB_IDS::get)
                .toList();
        List<Long> deltas = RANDOM.longs(THREADS * MAJOR_PER_THREAD, 0, DELTA_BOUND).boxed().toList();

        concurrentTest(i -> () -> {
            Person person = createRemotePerson(info);

            for (int j = 0; j < MAJOR_PER_THREAD; j++) {
                int now = i * MAJOR_PER_THREAD + j;
                Account account = person.createAccount(sub_ids.get(now));
                account.updateAmount(deltas.get(now));
            }

            return null;
        });

        Map<String, Long> expectedAmounts = new HashMap<>();
        for (int i = 0; i < sub_ids.size(); i++) {
            expectedAmounts.merge(sub_ids.get(i), deltas.get(i), Long::sum);
        }

        Person person = createRemotePerson(info);
        for (var entry : expectedAmounts.entrySet()) {
            Account account = person.getAccount(entry.getKey());
            Assertions.assertNotNull(account, "Account hasn't been created");
            Assertions.assertEquals(entry.getValue(), account.getAmount(), "Bad amount for account " + entry.getKey());
        }
    }

    private void assertEqualsPersons(Person p1, Person p2) throws RemoteException {
        Assertions.assertEquals(p1.getFirstName(), p2.getFirstName(), "First names doesn't match");
        Assertions.assertEquals(p1.getLastName(), p2.getLastName(), "Last names doesn't match");
        Assertions.assertEquals(p1.getPassport(), p2.getPassport(), "Passports doesn't match");

        Map<String, RemoteAccount> accounts1 = p1.getAllAccounts();
        Map<String, RemoteAccount> accounts2 = p2.getAllAccounts();

        Assertions.assertEquals(accounts1.size(), accounts2.size(), "Accounts' number doesn't match");
        for (var entry : accounts1.entrySet()) {
            Assertions.assertTrue(accounts2.containsKey(entry.getKey()), "Person2 doesn't have account " + entry.getValue());
        }
    }

    @Test
    public void test20_localSnapshot() throws RemoteException {
        PersonalInfo info = INFOS.getFirst();
        Person remotePerson = createRemotePerson(info);

        createAccounts(remotePerson, SUB_IDS);

        Person localPerson = bank.getPersonByPassport(info.passport, Bank.PersonType.LOCAL);

        Assertions.assertNotNull(localPerson, "Local person hasn't been created");
        assertEqualsPersons(remotePerson, localPerson);
    }

    @Test
    public void test21_localCreation() throws RemoteException {
        PersonalInfo info = INFOS.getFirst();

        Person localPerson = bank.createPerson(info.firstName, info.lastName, info.passport, Bank.PersonType.LOCAL);
        Assertions.assertNull(bank.getPersonByPassport(info.passport, Bank.PersonType.REMOTE));
        Assertions.assertNull(bank.getPersonByPassport(info.passport, Bank.PersonType.LOCAL));

        Assertions.assertEquals(localPerson.getFirstName(), info.firstName, "Bad first name for local person");
        Assertions.assertEquals(localPerson.getLastName(), info.lastName, "Bad last name for local person");
        Assertions.assertEquals(localPerson.getPassport(), info.passport, "Bad passport for local person");
        Assertions.assertEquals(localPerson.getAllAccounts().size(), 0, "Local created person should have 0 accounts");
    }

    private Map<String, Long> collectAccounts(Person person, List<String> subIds) throws RemoteException {
        Map<String, Long> expectedAmounts = new HashMap<>();
        for (String subId : subIds) {
            expectedAmounts.put(subId, person.getAccount(subId).getAmount());
        }
        return expectedAmounts;
    }

    private void testIndependence(Person target, Person changeable) throws RemoteException {
        Map<String, Long> expectedAmounts = collectAccounts(target, SUB_IDS);

        for (int i = 0; i < 10; ++i) {
            String id = SUB_IDS.get(RANDOM.nextInt(SUB_IDS.size()));
            Account account = changeable.getAccount(id);
            long amount = account.getAmount();
            long delta = addRandomDelta(account, amount);
            log("Account %s delta %d -> %d".formatted(id, amount, amount + delta));
        }

        for (String id : SUB_IDS) {
            long localAmount = target.getAccount(id).getAmount();
            log("Local amount %s real amount %d".formatted(id, localAmount));
            Assertions.assertEquals(expectedAmounts.get(id), localAmount, "Local amount should not be changed");
        }
    }

    @Test
    public void test22_localIndependence() throws RemoteException {
        PersonalInfo info = INFOS.getFirst();
        Person remotePerson = createRemotePerson(info);
        createAccounts(remotePerson, SUB_IDS);
        Person localPerson = bank.getPersonByPassport(info.passport, Bank.PersonType.LOCAL);

        testIndependence(localPerson, remotePerson);
        testIndependence(remotePerson, localPerson);
    }

    @Test
    public void test23_manySnapshots() throws RemoteException {
        final int actionCount = 1000;

        List<Person> remotes = new ArrayList<>();

        record Snapshot(Person person, Map<String, Long> expectedAmounts) {
        }

        List<Snapshot> snapshots = new ArrayList<>();

        for (PersonalInfo info : INFOS) {
            Person remote = createRemotePerson(info);
            remotes.add(remote);
            createAccounts(remote, SUB_IDS);
        }

        Map<String, Integer> snapshotsCount = new HashMap<>();
        Map<String, Integer> snapshotsChanges = new HashMap<>();

        for (int i = 0; i < actionCount; ++i) {
            int action = RANDOM.nextInt(3);
            int personId = RANDOM.nextInt(remotes.size());
            Person person = remotes.get(personId);

            switch (action) {
                case 0 -> {
                    String subId = SUB_IDS.get(RANDOM.nextInt(SUB_IDS.size()));
                    Account account = person.getAccount(subId);
                    addRandomDelta(account, account.getAmount());
                }
                case 1 -> {
                    snapshotsCount.merge(person.getPassport(), 1, Integer::sum);
                    snapshots.add(new Snapshot(bank.getPersonByPassport(person.getPassport(), Bank.PersonType.LOCAL),
                            collectAccounts(person, SUB_IDS)));
                }
                case 2 -> {
                    if (!snapshots.isEmpty()) {
                        snapshotsChanges.merge(person.getPassport(), 1, Integer::sum);
                        Snapshot snapshotToChange = snapshots.get(RANDOM.nextInt(snapshots.size()));
                        String subId = SUB_IDS.get(RANDOM.nextInt(SUB_IDS.size()));
                        Account account = snapshotToChange.person.getAccount(subId);
                        long delta = addRandomDelta(account, account.getAmount());

                        snapshotToChange.expectedAmounts.merge(subId, delta, Long::sum);
                    }
                }
            }
        }

        log("Created %d snapshots".formatted(snapshots.size()));
        for (var entry : snapshotsCount.entrySet()) {
            log("For person %s created %d snapshots with %d changes"
                    .formatted(entry.getKey(), entry.getValue(), snapshotsChanges.get(entry.getKey())));
        }

        for (Snapshot s : snapshots) {
            Map<String, RemoteAccount> accounts = s.person().getAllAccounts();
            Assertions.assertEquals(s.expectedAmounts.size(), accounts.size(),
                    "Snapshots accounts doesn't match expected");

            for (var entry : s.expectedAmounts.entrySet()) {
                Assertions.assertTrue(accounts.containsKey(entry.getKey()),
                        "Snapshot doesn't contain account" + entry.getKey());
                Assertions.assertEquals(entry.getValue(), accounts.get(entry.getKey()).getAmount(),
                        "Bad snapshot amount on account " + entry.getKey());
            }
        }
    }

    private void testInfo(Person person, PersonalInfo expectedInfo) throws RemoteException {
        Assertions.assertEquals(expectedInfo.firstName, person.getFirstName(), "Bad first name");
        Assertions.assertEquals(expectedInfo.lastName, person.getLastName(), "Bad last name");
        Assertions.assertEquals(expectedInfo.passport, person.getPassport(), "Bad passport");
    }

    private void createPersons(List<PersonalInfo> infos) throws RemoteException {
        for (PersonalInfo info : infos) {
            Person person = createRemotePerson(info);
            testInfo(person, info);
        }
    }

    private PersonalInfo getInfo(Person person) throws RemoteException {
        return new PersonalInfo(person.getFirstName(), person.getLastName(), person.getPassport());
    }

    @Test
    public void test30_weirdInfos() throws RemoteException {
        createPersons(WEIRD_INFOS);

        for (PersonalInfo info : WEIRD_INFOS) {
            Person person = bank.getPersonByPassport(info.passport, Bank.PersonType.REMOTE);
            Assertions.assertNotNull(person, "Person %s hasn't been created".formatted(info.passport));
            testInfo(person, info);
        }
    }

    private void assertNoTypedPerson(PersonalInfo info, Bank.PersonType type) throws RemoteException {
        Assertions.assertNull(bank.getPersonByPassport(info.passport, type), "This person should be created");
    }

    private void assertNoPerson(PersonalInfo info) throws RemoteException {
        assertNoTypedPerson(info, Bank.PersonType.REMOTE);
        assertNoTypedPerson(info, Bank.PersonType.LOCAL);
    }

    private void assertTypedPerson(PersonalInfo info, Bank.PersonType type) throws RemoteException {
        testInfo(bank.getPersonByPassport(info.passport, type), info);
    }

    private void assertPerson(PersonalInfo info) throws RemoteException {
        assertTypedPerson(info, Bank.PersonType.REMOTE);
        assertTypedPerson(info, Bank.PersonType.LOCAL);
    }

    @Test
    public void test31_differentTypes() throws RemoteException {
        List<PersonalInfo> infos = new ArrayList<>(INFOS);
        PersonalInfo unusedInfo = infos.removeLast();

        createPersons(infos);
        for (PersonalInfo info : infos) {
            assertPerson(info);
        }

        assertNoPerson(unusedInfo);
        testInfo(createPerson(unusedInfo, Bank.PersonType.LOCAL), unusedInfo);
        assertNoPerson(unusedInfo);

        testInfo(createPerson(unusedInfo, Bank.PersonType.REMOTE), unusedInfo);

        assertPerson(unusedInfo);
    }

    private String getRandomString() {
        byte[] bytes = new byte[3];
        RANDOM.nextBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Test
    public void test32_concurrentPersons() throws RemoteException, InterruptedException, ExecutionException {
        int perThread = MAJOR_PER_THREAD / 5;

        List<PersonalInfo> infos = new ArrayList<>();
        Map<String, Set<PersonalInfo>> passportPossibleInfos = new HashMap<>();
        Map<String, Integer> passportsCount = new HashMap<>();
        for (int i = 0; i < THREADS * perThread; ++i) {
            String passport = getRandomString();
            PersonalInfo info = new PersonalInfo(getRandomString(), getRandomString(), passport);
            infos.add(info);
            passportPossibleInfos.merge(passport, new HashSet<>(List.of(info)), (l1, l2) -> {
                l1.addAll(l2);
                return l1;
            });
            passportsCount.merge(passport, 1, Integer::sum);
        }

        log("Created %d persons".formatted(infos.size()));
        int collisions = 0;
        for (var entry : passportsCount.entrySet()) {
            collisions += entry.getValue() - 1;
        }
        log("With %d collisions".formatted(collisions));

        concurrentTest(i -> () -> {
            for (int j = 0; j < perThread; j++) {
                PersonalInfo info = infos.get(i * perThread + j);
                createRemotePerson(info);
                PersonalInfo corrupted = WEIRD_INFOS.getFirst();
                createPerson(corrupted, Bank.PersonType.LOCAL);
            }

            return null;
        });
        log("Creation is done");

        for (PersonalInfo info : infos) {
            Person person = bank.getPersonByPassport(info.passport, Bank.PersonType.REMOTE);
            PersonalInfo actualInfo = getInfo(person);
            Assertions.assertTrue(passportPossibleInfos.get(info.passport).contains(actualInfo), "Unexpected person's data");

            passportPossibleInfos.replace(info.passport, Set.of(actualInfo));
            assertPerson(actualInfo);
        }

        for (PersonalInfo info : WEIRD_INFOS) {
            assertNoPerson(info);
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.MethodName.class)
    public class AppTests {
        private final PrintStream standardOut = System.out;
        private final ByteArrayOutputStream outCaptor = new ByteArrayOutputStream();

        private final PrintStream standardErr = System.err;
        private final ByteArrayOutputStream errCaptor = new ByteArrayOutputStream();

        private void log(String message) {
            standardOut.println(message);
        }

        // Output capturing is commented because I don't know if this is allowed
        @BeforeEach
        public void setUpOutputCaptor() {
            log("Replacing output+error streams");
//            System.setOut(new PrintStream(outCaptor));
//            System.setErr(new PrintStream(errCaptor));
        }

        @AfterEach
        public void restoreOutput() {
//            System.setOut(standardOut);
//            System.setErr(standardErr);
            log("Output+error streams restored");
        }

        private void assertOutputContains(String expected) {
//            Assertions.assertTrue(outCaptor.toString().contains(expected), "Output doesn't contain \"%s\"".formatted(expected));
            outCaptor.reset();
        }

        private void assertNoErrors() {
//            Assertions.assertTrue(errCaptor.toString().isEmpty(), "Expected no records in error stream");
        }

        private void assertError() {
//            Assertions.assertFalse(errCaptor.toString().isEmpty(), "Expected something in error stream");
            errCaptor.reset();
        }

        private String[] createArgs(String... args) {
            return args;
        }

        private void assertAmount(PersonalInfo info, String subId, long expectedAmount) throws RemoteException {
            Person person = bank.getPersonByPassport(info.passport, Bank.PersonType.REMOTE);
            Assertions.assertNotNull(person, "Person not found");
            Account account = person.getAccount(subId);
            Assertions.assertNotNull(account, "Account not found");
            Assertions.assertEquals(expectedAmount, account.getAmount(), "Bad amount");
        }

        private void runAppOkay(PersonalInfo info, String subId, long was, long delta) throws RemoteException {
            BankApp.main(createArgs(info.firstName, info.lastName, info.passport, subId, String.valueOf(delta)));
            assertNoErrors();
            assertOutputContains(String.valueOf(was + delta));

            assertAmount(info, subId, was + delta);
        }

        private long runAppRandomDelta(PersonalInfo info, String subId, long was) throws RemoteException {
            long delta = getRandomDelta();
            runAppOkay(info, subId, was, delta);
            return was + delta;
        }

        private void runAppError(String... args) {
            BankApp.main(args);
            assertError();
        }

        private long stressTestPerson(PersonalInfo info, String subId, long was) throws RemoteException {
            for (int i = 0; i < 20; ++i) {
                long delta = getRandomDelta() - DELTA_BOUND / 2;
                if (was + delta >= 0) {
                    runAppOkay(info, subId, was, delta);
                    was += delta;
                } else {
                    runAppError(info.firstName, info.lastName, info.passport, subId, String.valueOf(delta));
                }
            }
            return was;
        }

        @Test
        public void test40_appOkay() throws RemoteException {
            PersonalInfo info = INFOS.getFirst();
            String subId = SUB_IDS.getFirst();

            runAppRandomDelta(info, subId, 0);
        }

        @Test
        public void test41_appInvalidArgs() {
            runAppError("", "", "", "3"); // too few args
            runAppError();
            runAppError("", "", "", "", "as"); // not numeric delta
            runAppError("", "", "", "", " "); // not numeric delta
        }

        @Test
        public void test42_appCheckData() throws RemoteException {
            PersonalInfo info = INFOS.getFirst();
            String subId = SUB_IDS.getFirst();

            long amount = runAppRandomDelta(info, subId, 0);
            amount = runAppRandomDelta(info, subId, amount);

            runAppError(info.firstName, "bad last name", info.passport, subId, "1");
            runAppRandomDelta(info, subId, amount);
        }

        @Test
        public void test43_appNegativeAmount() throws RemoteException {
            PersonalInfo info = INFOS.getFirst();
            String subId = SUB_IDS.getFirst();

            long amount = runAppRandomDelta(info, subId, 0);

            runAppOkay(info, subId, amount, -amount);
            runAppError(info.firstName, info.lastName, info.passport, subId, "-1");
        }

        @Test
        public void test45_manyPersons() throws RemoteException {
            int count = 500;

            HashMap<String, Long> amounts = new HashMap<>();
            for (int i = 0; i < count; ++i) {
                PersonalInfo info = INFOS.get(RANDOM.nextInt(INFOS.size()));
                String subId = SUB_IDS.get(RANDOM.nextInt(SUB_IDS.size()));

                String id = "%s:%s".formatted(info.passport, subId);
                amounts.put(id,
                        stressTestPerson(info, subId, amounts.getOrDefault(id, 0L)));

                if (i % 100 == 0) {
                    log("Stressed %d out of %d".formatted(i, count));
                }
            }
        }
    }
}
