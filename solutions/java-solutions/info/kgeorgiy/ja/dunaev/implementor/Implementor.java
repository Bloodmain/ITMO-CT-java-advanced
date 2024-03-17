package info.kgeorgiy.ja.dunaev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Implementor implements Impler {
    private static final String JAVA_EXT = ".java";
    private static final String PACKAGES_SEPARATOR = ".";

    private static BufferedWriter openFileWrite(final Path path) throws IOException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (final IOException ignored) {
            }
        }
        return Files.newBufferedWriter(path);
    }

    private Path resolveTokenPath(final Path root, final Class<?> token) {
        String implName = token.getPackageName() + PACKAGES_SEPARATOR + token.getSimpleName() + ClassCodeGenerator.IMPL_SUFFIX;
        return root.resolve(implName.replace(PACKAGES_SEPARATOR, File.separator) + JAVA_EXT);
    }

    @Override
    public void implement(final Class<?> token, final Path root) throws ImplerException {
        ClassDisassembler classGenerator = new ClassDisassembler(token);

        Path implPath = resolveTokenPath(root, token);
        try (final BufferedWriter writer = openFileWrite(implPath)) {
            try {
                writer.write(classGenerator.generateCode());
            } catch (final IOException e) {
                throw new ImplerException("Exception while writing to a file", e);
            }
        } catch (final IOException e) {
            throw new ImplerException("Can't open file to write class", e);
        }
    }
}
