package info.kgeorgiy.ja.dunaev.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;

public class Walker {
    private static final int BUFFER_SIZE = 4096;

    private static String calculateHashCode(final Path path, final Hasher hasher) {
        try (final InputStream fileStream = Files.newInputStream(path)) {
            hasher.reset();
            // :NOTE: reuse
            final byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = fileStream.read(buffer)) != -1) {
                hasher.update(buffer, count);
            }
            return hasher.digest();
        } catch (final IOException e) {
            return hasher.errorHash();
        }
    }

    private static BufferedWriter openFileWrite(final Path path) throws IOException {
        if (path.getParent() != null) {
            // :NOTE: fail on error?
            Files.createDirectories(path.getParent());
        }
        return Files.newBufferedWriter(path);
    }

    private static Path getpath(final String pathname) {
        try {
            return Paths.get(pathname);
        } catch (final InvalidPathException e) {
            return null;
        }
    }

    private static void write(final BufferedWriter writer, final String hash, final String pathname) throws IOException {
        writer.write(hash + " " + pathname);
        writer.newLine();
    }


    // :NOTE: Map
    private static Hasher getHasher(final String algorithm) {
        if (algorithm.equals("jenkins")) {
            return new JenkinsHasher();
        } else {
            try {
                return new MessageDigestHasher(algorithm);
            } catch (final NoSuchAlgorithmException e) {
                return null;
            }
        }
    }

    /* package-private */
    static void walk(final String[] args, final int depth) {
        // :NOTE: code reuse
        if (args == null) {
            System.err.println("Expected array with input and output filenames");
            return;
        }

        if (args.length < 2) {
            System.err.println("Expected input and output filenames, provided: " + args.length + " arguments");
            return;
        }

        if (args[0] == null || args[1] == null) {
            System.err.println("Expected non-null string arguments (input and output filenames)");
            return;
        }

        final Hasher hasher;
        if (args.length == 3) {
            if (args[2] == null) {
                System.err.println("Expected non-null hash algorithm name as the third argument");
                return;
            }
            hasher = getHasher(args[2]);
            if (hasher == null) {
                System.err.println("Unsupported hash algorithm: \"" + args[2] + "\"");
                return;
            }
        } else {
            hasher = getHasher("jenkins");
        }

        // :NOTE: charset
        try (
                final BufferedReader input = Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8);
                final BufferedWriter output = openFileWrite(Paths.get(args[1]))
        ) {
            String pathname;
            while ((pathname = input.readLine()) != null) {
                try {
                    try {
                        Path path = Paths.get(pathname);
                        // :NOTE: EnumSet.noneOf(FileVisitOption.class)
                        // :NOTE: Reuse
                        Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), depth, new SimpleFileVisitor<>() {
                            @Override
                            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                                    throws IOException {
                                final String hash = calculateHashCode(file, hasher);
                                write(output, hash, file.toString());
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed(final Path file, final IOException e)
                                    throws IOException {
                                write(output, hasher.errorHash(), file.toString());
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } catch (final InvalidPathException e1) {
                        write(output, hasher.errorHash(), pathname);
                    }
                } catch (final IOException e) {
                    System.err.println("Can't write to file: " + e.getMessage());
                }
            }
        } catch (final IOException | InvalidPathException e) {
            // :NOTE: More context
            System.err.println("Can't open input/output file: " + e.getMessage());
        }
    }
}
