package info.kgeorgiy.ja.dunaev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.basic.classes.AbstractClassWithInterface;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Test {
    private static final SimpleFileVisitor<Path> DELETE = new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    public static void main(String[] args) {
        Implementor implementor = new Implementor();
        Path p = Path.of("/home/bloodmain/Projects/ITMO/java-advanced/solutions/java-solutions/test");
        try {
            Files.walkFileTree(p, DELETE);
        } catch (IOException e) {
            System.out.println("Can't delete: " + e.getMessage());
        }
        Class<?> token = A.B.class;

        try {
            implementor.implement(token, p);
        } catch (ImplerException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
