package info.kgeorgiy.ja.dunaev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Class to crawl websites. Provides list of downloaded sites, and all errors if ones.
 * Has graceful shutdown close and CLI.
 *
 * @author Dunaev Kirill
 */
public class WebCrawler implements AdvancedCrawler {
    private static final int DEFAULT_LIMITATIONS = 100;
    private static final int DEFAULT_DEPTH = 1;

    private final Downloader downloader;
    private final ExecutorService downloadService;
    private final ExecutorService extractService;
    private final Map<String, PerHostQueue> hostsQueues;
    private final int perHost;

    /**
     * Creates crawler with specified settings.
     *
     * @param downloader  downloader of websites
     * @param downloaders maximum number of pages downloading in parallel
     * @param extractors  maximum number of pages extracting links in parallel from which
     * @param perHost     maximum number of pages downloading in parallel from the same host
     * @throws IllegalArgumentException if non-positive limitations are provided
     * @throws NullPointerException     if downloader is null
     */
    public WebCrawler(final Downloader downloader, final int downloaders, final int extractors, final int perHost) {
        Objects.requireNonNull(downloader, "downloader must not be null");
        if (downloaders <= 0 || extractors <= 0 || perHost <= 0) {
            throw new IllegalArgumentException("Expected positive limitations");
        }

        this.downloader = downloader;
        downloadService = Executors.newFixedThreadPool(downloaders);
        extractService = Executors.newFixedThreadPool(extractors);
        hostsQueues = new ConcurrentHashMap<>();
        this.perHost = perHost;
    }

    private List<String> getLinks(
            final Map<String, IOException> errors,
            final Document doc,
            final String url
    ) {
        try {
            return doc.extractLinks();
        } catch (final IOException e) {
            errors.put(url, e);
        }
        return Collections.emptyList();
    }

    private Future<List<String>> downloadThenExtractLinks(
            final Set<String> downloaded,
            final Map<String, IOException> errors,
            final String url,
            final int depth
    ) {
        try {
            final Document doc = downloader.download(url);
            downloaded.add(url);
            if (depth > 1) {
                return extractService.submit(() -> getLinks(errors, doc, url));
            }
        } catch (final IOException e) {
            errors.put(url, e);
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    private class PerHostQueue {
        ArrayDeque<Task> queue = new ArrayDeque<>();
        private int counter = 0;

        private record Task(Callable<Future<List<String>>> task,
                            CompletableFuture<Future<Future<List<String>>>> resultFuture) {
        }

        // returns future that will be done when the task is picked up by a download service
        private synchronized Future<Future<Future<List<String>>>> submit(Callable<Future<List<String>>> task) {
            if (counter < perHost) {
                ++counter;
                return CompletableFuture.completedFuture(downloadService.submit(() -> {
                    try {
                        return task.call();
                    } finally {
                        done();
                    }
                }));
            } else {
                CompletableFuture<Future<Future<List<String>>>> future = new CompletableFuture<>();
                queue.add(new Task(task, future));
                return future;
            }
        }

        private synchronized void done() {
            --counter;
            Task next = queue.poll();
            if (next != null) {
                next.resultFuture.complete(submit(next.task).resultNow());
            }
        }
    }

    private Future<Future<Future<List<String>>>> signToDownloadQueue(
            final Set<String> downloaded,
            final Map<String, IOException> errors,
            String url,
            int i,
            final Predicate<String> allowURL,
            final Predicate<String> allowHost
    ) {
        final String host = sneakyCallable(() -> URLUtils.getHost(url));
        if (allowHost.test(host) && allowURL.test(url)) {
            return hostsQueues
                    .computeIfAbsent(host, k -> new PerHostQueue())
                    .submit(() -> downloadThenExtractLinks(downloaded, errors, url, i));
        } else {
            // More futures to the God of future *I am a Time Lord?*
            return CompletableFuture.completedFuture(CompletableFuture.completedFuture(
                    CompletableFuture.completedFuture(Collections.emptyList()))
            );
        }
    }

    private Result bfsDownload(
            final String url,
            final int depth,
            final Predicate<String> allowURL,
            final Predicate<String> allowHost
    ) {
        final Map<String, IOException> errors = new ConcurrentHashMap<>();
        final Set<String> downloaded = ConcurrentHashMap.newKeySet();

        Set<String> toProcess = Set.of(url);
        for (int layer = depth; layer > 0 && !toProcess.isEmpty(); --layer) {
            final int i = layer;

            final var downloadFutures = toProcess.stream()
                    .filter(u -> !downloaded.contains(u) && !errors.containsKey(u))
                    .map(u -> signToDownloadQueue(downloaded, errors, u, i, allowURL, allowHost))
                    .toList();

            toProcess = downloadFutures.stream()
                    .map(f -> sneakyCallable(f::get))  // This get waits for task to be picked up by a download service
                    .map(f -> sneakyCallable(f::get))  // Get downloaded documents
                    .map(f -> sneakyCallable(f::get))  // Get extracted links
                    .flatMap(List::stream)             // Concat all links
                    .collect(Collectors.toSet());
        }

        return new Result(downloaded.stream().toList(), errors);
    }

    private Result downloadSiteLimits(final String url, final int depth, final Predicate<String> allowURL, final Predicate<String> allowHost) {
        if (depth < 1) {
            throw new IllegalArgumentException("Depth should be >= 1");
        }

        return sneakyCallable(() -> bfsDownload(url, depth, allowURL, allowHost));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if depth {@code <1}
     */
    @Override
    public Result download(final String url, final int depth, final Set<String> excludes) {
        return downloadSiteLimits(
                url, depth,
                u -> excludes.stream().noneMatch(u::contains),
                h -> true
        );
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if depth {@code <1}
     */
    @Override
    public Result advancedDownload(final String url, final int depth, final List<String> hosts) {
        final Set<String> hostsSet = new HashSet<>();
        hostsSet.addAll(hosts);

        return downloadSiteLimits(
                url, depth,
                u -> true,
                hostsSet::contains
        );
    }

    private static <R> R sneakyCallable(final Callable<R> callable) {
        try {
            return callable.call();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        downloadService.close();
        extractService.close();
    }

    /**
     * CLI for the crawler. Crawls the website and prints downloaded sites and errors.
     * Usage: <pre>{@code WebCrawler url [depth [downloads [extractors [perHost]]]]}</pre>
     * Default values are {@value DEFAULT_DEPTH} for depth and {@value DEFAULT_LIMITATIONS} for other arguments.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        Objects.requireNonNull(args);
        Arrays.stream(args).forEach(Objects::requireNonNull);

        if (args.length < 1) {
            System.err.println("Expected at least 1 argument. Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]}");
            return;
        }

        final String url = args[0];
        try {
            final int depth = parseIntArgument(args, 1, DEFAULT_DEPTH);
            final int downloaders = parseIntArgument(args, 2, DEFAULT_LIMITATIONS);
            final int extractors = parseIntArgument(args, 3, DEFAULT_LIMITATIONS);
            final int perHost = parseIntArgument(args, 4, DEFAULT_LIMITATIONS);
            System.out.printf(
                    "Applied settings:%n\t* Url: \"%s\"%n\t* Depth: %d%n\t* Downloaders: %d%n\t* Extractors: %d%n\t* PerHost: %d%n",
                    url, depth, downloaders, extractors, perHost
            );

            try (final Crawler crawler =
                         new WebCrawler(
                                 new CachingDownloader(0, Path.of("cached-sites/")),
                                 downloaders, extractors, perHost
                         )
            ) {
                try {
                    print(crawler.download(url, depth));
                } catch (final RuntimeException e) {
                    System.err.println("Error while crawling: " + e.getMessage());
                }
            } catch (final IOException e) {
                System.err.println("Can't create temporary directory for downloader: " + e.getMessage());
            }
        } catch (final IllegalArgumentException e) {
            System.err.println("Bad limitations provided: " + e.getMessage());
        }
    }

    private static int parseIntArgument(final String[] args, final int index, final int defaultValue) {
        if (index >= args.length) {
            return defaultValue;
        }

        final int arg = Integer.parseInt(args[index]);
        if (arg <= 0) {
            throw new IllegalArgumentException("Expected positive integer for limitations, found %d".formatted(arg));
        }
        return arg;
    }

    private static void print(final Result result) {
        final List<String> downloaded = result.getDownloaded();
        final Map<String, IOException> errors = result.getErrors();
        if (downloaded.isEmpty()) {
            System.out.println("Nothing has been downloaded.");
        } else {
            System.out.println("Downloaded:");
            result.getDownloaded().forEach(u -> System.out.println("\t" + u));
        }

        if (errors.isEmpty()) {
            System.out.println("No errors.");
        } else {
            System.out.println("Errors:");
            result.getErrors().forEach((u, e) -> System.out.printf("\t %s: %s%n", u, e.getMessage()));
        }
    }
}
