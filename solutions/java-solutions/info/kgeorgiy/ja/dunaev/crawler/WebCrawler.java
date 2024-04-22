package info.kgeorgiy.ja.dunaev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Class to crawl websites. Provides list of downloaded sites, and all errors if ones.
 * Has graceful shutdown close and CLI.
 *
 * @author Dunaev Kirill
 */
public class WebCrawler implements Crawler {
    private static final int DEFAULT_LIMITATIONS = 100;
    private static final int DEFAULT_DEPTH = 1;

    private final Downloader downloader;
    private final ExecutorService downloadService;
    private final ExecutorService extractService;
    private final Map<String, Semaphore> perHostSemaphores;
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
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        Objects.requireNonNull(downloader, "downloader must not be null");
        if (downloaders <= 0 || extractors <= 0 || perHost <= 0) {
            throw new IllegalArgumentException("Expected positive limitations");
        }

        this.downloader = downloader;
        downloadService = Executors.newFixedThreadPool(downloaders);
        extractService = Executors.newFixedThreadPool(extractors);
        perHostSemaphores = new ConcurrentHashMap<>();
        this.perHost = perHost;
    }

    private List<String> getLinks(
            Map<String, Optional<IOException>> downloaded,
            Document doc,
            String url
    ) {
        try {
            return doc.extractLinks();
        } catch (final IOException e) {
            downloaded.replace(url, Optional.of(e));
        }
        return Collections.emptyList();
    }

    private Future<List<String>> downloadThenExtractLinks(
            Map<String, Optional<IOException>> downloaded,
            String url,
            int depth
    ) throws InterruptedException {
        try {
            if (Objects.isNull(downloaded.putIfAbsent(url, Optional.empty()))) {
                String host = URLUtils.getHost(url);
                acquireHost(host);
                try {
                    Document doc = downloader.download(url);
                    if (depth > 1) {
                        return extractService.submit(() -> getLinks(downloaded, doc, url));
                    }
                } finally {
                    releaseHost(host);
                }
            }
        } catch (final IOException e) {
            downloaded.replace(url, Optional.of(e));
        }
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    private Map<String, Optional<IOException>> bfsDownload(
            String url,
            int depth
    ) throws InterruptedException {
        Map<String, Optional<IOException>> downloaded = new ConcurrentHashMap<>();

        Set<String> toProcess = Set.of(url);
        for (int layer = depth; layer > 0 && !toProcess.isEmpty(); --layer) {
            final int i = layer;

            var downloadFutures = downloadService.invokeAll(toProcess
                    .stream()
                    .<Callable<Future<List<String>>>>map(u -> () -> downloadThenExtractLinks(downloaded, u, i))
                    .toList()
            );

            toProcess = downloadFutures.stream()
                    .map(f -> sneakyCallable(f::get))    // Get downloaded documents
                    .map(f -> sneakyCallable(f::get))    // Get extracted links
                    .flatMap(List::stream)               // Concat all links
                    .collect(Collectors.toSet());
        }

        return downloaded;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if depth {@code <1}
     */
    @Override
    public Result download(String url, int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("Depth should be >= 1");
        }

        return makeResult(sneakyCallable(() -> bfsDownload(url, depth)));
    }

    private static Result makeResult(Map<String, Optional<IOException>> all) {
        List<String> downloaded = new ArrayList<>();
        Map<String, IOException> errors = new HashMap<>();
        all.forEach((u, err) -> {
            if (err.isEmpty()) {
                downloaded.add(u);
            } else {
                errors.put(u, err.get());
            }
        });

        return new Result(downloaded, errors);
    }

    private void acquireHost(String host) throws InterruptedException {
        perHostSemaphores.computeIfAbsent(host, h -> new Semaphore(perHost));
        perHostSemaphores.get(host).acquire();
    }

    private void releaseHost(String host) {
        perHostSemaphores.get(host).release();
    }

    private static <R> R sneakyCallable(Callable<R> callable) {
        try {
            return callable.call();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        gracefulShutdown(downloadService);
        gracefulShutdown(extractService);
    }

    private void gracefulShutdown(ExecutorService service) {
        service.shutdown();
        try {
            if (!service.awaitTermination(30, TimeUnit.SECONDS)) {
                forceShutdown(service);
            }
        } catch (InterruptedException e) {
            forceShutdown(service);
            Thread.currentThread().interrupt();
        }
    }

    private void forceShutdown(ExecutorService service) {
        service.shutdownNow();
    }

    /**
     * CLI for the crawler. Crawls the website and prints downloaded sites and errors.
     * Usage: <pre>{@code WebCrawler url [depth [downloads [extractors [perHost]]]]}</pre>
     * Default values are {@value DEFAULT_DEPTH} for depth and {@value DEFAULT_LIMITATIONS} for other arguments.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Objects.requireNonNull(args);
        Arrays.stream(args).forEach(Objects::requireNonNull);

        if (args.length < 1) {
            System.err.println("Expected at least 1 argument");
            System.exit(1);
        }

        String url = args[0];
        try {
            int depth = parseIntArgument(args, 1, DEFAULT_DEPTH);
            int downloaders = parseIntArgument(args, 2, DEFAULT_LIMITATIONS);
            int extractors = parseIntArgument(args, 3, DEFAULT_LIMITATIONS);
            int perHost = parseIntArgument(args, 4, DEFAULT_LIMITATIONS);
            System.out.printf(
                    "Applied settings:%n\t* Url: \"%s\"%n\t* Depth: %d%n\t* Downloaders: %d%n\t* Extractors: %d%n\t* PerHost: %d%n",
                    url, depth, downloaders, extractors, perHost
            );

            try (Crawler crawler =
                         new WebCrawler(
                                 new CachingDownloader(0, Path.of("cached-sites/")),
                                 downloaders, extractors, perHost
                         )
            ) {
                try {
                    print(crawler.download(url, depth));
                } catch (final RuntimeException e) {
                    System.err.println("Error while crawling: " + e.getMessage());
                    System.exit(2);
                }
            } catch (final IOException e) {
                System.err.println("Can't create temporary directory for downloader: " + e.getMessage());
                System.exit(1);
            }
        } catch (final IllegalArgumentException e) {
            System.err.println("Bad limitations provided: " + e.getMessage());
            System.exit(1);
        }
    }

    private static int parseIntArgument(String[] args, int index, int defaultValue) {
        if (index >= args.length) {
            return defaultValue;
        }

        int arg = Integer.parseInt(args[index]);
        if (arg <= 0) {
            throw new IllegalArgumentException(String.format("Expected positive integer for limitations, found %d", arg));
        }
        return arg;
    }

    private static void print(Result result) {
        List<String> downloaded = result.getDownloaded();
        Map<String, IOException> errors = result.getErrors();
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
