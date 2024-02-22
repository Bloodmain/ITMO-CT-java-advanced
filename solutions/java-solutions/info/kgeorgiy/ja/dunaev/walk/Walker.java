package info.kgeorgiy.ja.dunaev.walk;

import info.kgeorgiy.ja.dunaev.walk.exceptions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;

public class Walker {
    private static final EnumSet<FileVisitOption> OPTIONS = EnumSet.noneOf(FileVisitOption.class);
    private static final String DEFAULT_HASH_ALGORITHM = "jenkins";

    private static final int BUFFER_SIZE = 4096;
    private static final byte[] BUFFER = new byte[BUFFER_SIZE];

    private static String calculateHashCode(final Path path, final Hasher hasher) {
        try (final InputStream fileStream = Files.newInputStream(path)) {
            hasher.reset();
            int count;
            while ((count = fileStream.read(BUFFER)) != -1) {
                hasher.update(BUFFER, count);
            }
            return hasher.digest();
        } catch (final IOException e) {
            return hasher.errorHash();
        }
    }

    private static BufferedWriter openFileWrite(final Path path) throws IOException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException ignored) {
            }
        }
        return Files.newBufferedWriter(path);
    }

    private static void write(final BufferedWriter writer, final String hash, final String pathname) throws IOException {
        writer.write(hash + " " + pathname);
        writer.newLine();
    }

    private static Hasher getHasher(final String algorithm) throws NoSuchAlgorithmException {
        return switch (algorithm) {
            case "jenkins" -> new JenkinsHasher();
            case "sha-1" -> new SHA1Hasher();
            default -> throw new NoSuchAlgorithmException(algorithm);
        };
    }

    public static void walk(final String[] args, final int depth) throws WalkException {
        if (args == null) {
            throw new BadArgumentException("Expected array with input and output filenames");
        }

        if (args.length < 2) {
            throw new BadArgumentException("Expected input and output filenames, provided: " + args.length + " arguments");
        }

        if (args[0] == null || args[1] == null) {
            throw new BadArgumentException("Expected non-null string arguments (input and output filenames)");
        }

        final Hasher hasher;
        try {
            if (args.length == 3) {
                if (args[2] == null) {
                    throw new BadArgumentException("Expected non-null hash algorithm name as the third argument");
                }
                hasher = getHasher(args[2]);
            } else {
                hasher = getHasher(DEFAULT_HASH_ALGORITHM);
            }
        } catch (final NoSuchAlgorithmException e) {
            throw new UnsupportedHashAlgorithmException("Unsupported hash algorithm: \"" + args[2] + "\"");
        }

        try (final BufferedReader input = Files.newBufferedReader(Paths.get(args[0]))) {
            try (final BufferedWriter output = openFileWrite(Paths.get(args[1]))) {
                final FileVisitor<Path> hashFileVisitor = new SimpleFileVisitor<>() {
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
                };

                String pathname;
                try {
                    while ((pathname = input.readLine()) != null) {
                        try {
                            try {
                                Files.walkFileTree(Paths.get(pathname), OPTIONS, depth, hashFileVisitor);
                            } catch (final InvalidPathException e) {
                                write(output, hasher.errorHash(), pathname);
                            }
                        } catch (final IOException e) {
                            throw new IOFileException("Can't write to output file: " + e.getMessage(), e);
                        }
                    }
                } catch (final IOException e) {
                    throw new IOFileException("Can't read from input file: " + e.getMessage(), e);
                }

            } catch (final InvalidPathException e) {
                throw new PathException("Invalid path to output file: " + e.getMessage(), e);
            } catch (final IOException e) {
                throw new IOFileException("Can't open output file: " + e.getMessage(), e);
            }

        } catch (final InvalidPathException e) {
            throw new PathException("Invalid path to input file: " + e.getMessage(), e);
        } catch (final IOException e) {
            throw new IOFileException("Can't open input file: " + e.getMessage(), e);
        }
    }
}
