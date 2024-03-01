package info.kgeorgiy.java.advanced.base;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Test runners base class.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public final class BaseTester {
    private final long start = System.currentTimeMillis();
    private final Map<String, BiFunction<BaseTester, String, Class<?>>> tests = new LinkedHashMap<>();

    public BaseTester() {
    }

    @SuppressWarnings("unused")
    public static void depends(final Class<?> classes) {
    }

    public void run(final String[] args) {
        if (args.length != 2 && args.length != 3) {
            printUsage();
            return;
        }

        final String test = args[0];
        final String cut = args[1];
        if (!tests.containsKey(test)) {
            System.out.println("Unknown variant " + test);
            printUsage();
            return;
        }

        final Class<?> token = test(test, cut);

        System.out.println("============================");
        final long time = System.currentTimeMillis() - start;
        System.out.printf("OK %s for %s in %dms %n", test, cut, time);
        certify(token, test + (args.length > 2 ? args[2] : ""));
    }

    public Class<?> test(final String test, final String cut) {
        return tests.get(test).apply(this, cut);
    }

    private static Class<?> test(final String cut, final Class<?> test) {
        System.err.printf("Running %s for %s%n", test, cut);

        System.setProperty(BaseTest.CUT_PROPERTY, cut);

        final SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        final LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(test))
                .build();

        final TestExecutionListener timeListener = new TestExecutionListener() {
            private final PrintStream err = System.err;
            private long startTime;

            @Override
            public void executionStarted(final TestIdentifier test) {
                if (test.isTest()) {
                    startTime = System.currentTimeMillis();
                    err.println("=== Running " + test.getDisplayName());
                }
            }

            @Override
            public void executionFinished(final TestIdentifier test, final TestExecutionResult result) {
                if (test.isTest()) {
                    System.err.printf(
                            "--- %s finished in %dms%n",
                            test.getDisplayName(),
                            System.currentTimeMillis() - startTime
                    );
                }
            }
        };

        LauncherFactory.create().execute(request, summaryListener, timeListener);
        final TestExecutionSummary summary = summaryListener.getSummary();
        if (summary.getTestsFailedCount() == 0) {
            return test;
        }

        for (final TestExecutionSummary.Failure failure : summary.getFailures()) {
            final Throwable exception = failure.getException();
            System.err.println("Test " + failure.getTestIdentifier().getDisplayName() + " failed: " + exception.getMessage());
            //noinspection CallToPrintStackTrace
            exception.printStackTrace();
        }
        System.exit(1);
        throw new AssertionError("Exit");
    }

    private static void certify(final Class<?> token, final String salt) {
        try {
            final CG cg = (CG) Class.forName("info.kgeorgiy.java.advanced.base.CertificateGenerator").getDeclaredConstructor().newInstance();
            cg.certify(token, salt);
        } catch (final ClassNotFoundException e) {
            // Ignore
        } catch (final Exception e) {
            System.err.println("Error running certificate generator");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private void printUsage() {
        System.out.println("Usage:");
        for (final String name : tests.keySet()) {
            System.out.format(
                    "    java -cp . -p . -m %s %s Solution.class.name [salt]%n",
                    getClass().getPackage().getName(),
                    name
            );
        }
        System.exit(1);
    }

    public BaseTester add(final String name, final Class<?> testClass) {
        return add(name, (tester, cut) -> test(cut, testClass));
    }

    public BaseTester add(final String name, final BiFunction<BaseTester, String, Class<?>> test) {
        tests.put(name, test);
        return this;
    }
}
