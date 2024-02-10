package info.kgeorgiy.ja.dunaev.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

public class Walker {
    private static final int BUFFER_SIZE = 4096;
    private static final Hasher HASHER = new JenkinsHasher();
    private static final int ERROR_HASH = 0;

    private static int calculateHashCode(Path path) {
        try (InputStream fileStream = Files.newInputStream(path)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = fileStream.read(buffer)) != -1) {
                HASHER.update(buffer, count);
            }
            return HASHER.digest();
        } catch (IOException e) {
            HASHER.reset();
            return 0;
        }
    }

    private static BufferedWriter openFileWrite(Path path) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        return Files.newBufferedWriter(path, StandardCharsets.UTF_8);
    }

    private static Path getpath(String pathname) {
        try {
            return Paths.get(pathname);
        } catch (InvalidPathException e) {
            return null;
        }
    }

    private static void write(BufferedWriter writer, int hash, String pathname) throws IOException {
        writer.write(String.format("%08x %s", hash, pathname));
        writer.newLine();
    }

    public static void walk(String[] args, int depth) {
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

        try (
                BufferedReader input = Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8);
                BufferedWriter output = openFileWrite(Paths.get(args[1]))
        ) {
            String pathname;
            while ((pathname = input.readLine()) != null) {
                Path path = getpath(pathname);
                try {
                    if (path == null) {
                        write(output, ERROR_HASH, pathname);
                        continue;
                    }

                    Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), depth, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            int hash = calculateHashCode(file);
                            write(output, hash, file.toString());
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException e)
                                throws IOException {
                            write(output, ERROR_HASH, file.toString());
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    System.err.println("Can't write to file: " + e.getMessage());
                }
            }

        } catch (IOException | InvalidPathException e) {
            System.err.println("Can't open input/output file: " + e.getMessage());
        }
    }
}
