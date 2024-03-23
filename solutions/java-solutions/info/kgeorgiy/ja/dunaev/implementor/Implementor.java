package info.kgeorgiy.ja.dunaev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public class Implementor implements JarImpler {
    private static final String JAVA_EXT = ".java";
    private static final String CLASS_FILE_EXT = ".class";
    private static final String PACKAGES_SEPARATOR = ".";
    private static final String JAR_PACKAGES_SEPARATOR = "/";

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

    private static void createParents(final Path path) {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (final IOException ignored) {
            }
        }
    }

    private static String getImplName(final Class<?> token, final String separator, final String extension) {
        String implName = token.getPackageName() + PACKAGES_SEPARATOR + token.getSimpleName() + ClassCodeGenerator.IMPL_SUFFIX;
        return implName.replace(PACKAGES_SEPARATOR, separator) + extension;
    }

    private static Path resolveTokenPath(final Path root, final Class<?> token, String extension) {
        return root.resolve(getImplName(token, File.separator, extension));
    }

    @Override
    public void implement(final Class<?> token, final Path root) throws ImplerException {
        ClassDisassembler classDisassembler = new ClassDisassembler(token);

        Path path = resolveTokenPath(root, token, JAVA_EXT);
        createParents(path);
        try (final BufferedWriter writer = Files.newBufferedWriter(path)) {
            try {
                writer.write(classDisassembler.generateCode());
            } catch (final IOException e) {
                throw new ImplerException("Exception while writing to a file", e);
            }
        } catch (final IOException e) {
            throw new ImplerException("Can't open file to write class", e);
        }
    }

    private static String getClassPath(Class<?> token) throws ImplerException {
        try {
            CodeSource source = token.getProtectionDomain().getCodeSource();
            if (source == null) {
                return "";
            }
            return Path.of(source.getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new ImplerException("Can't get classpath", e);
        }
    }

    private static void compile(final Class<?> token, final Path root) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final String classpath = root + File.pathSeparator + getClassPath(token);

        final String[] args = Stream.of(
                resolveTokenPath(root, token, JAVA_EXT).toString(),
                "-cp", classpath,
                "-encoding", StandardCharsets.UTF_8.name()
        ).toArray(String[]::new);

        int exitCode = compiler.run(null, null, null, args);
        if (exitCode != 0) {
            throw new ImplerException("Compiler returned non-zero code: " + exitCode);
        }
    }

    @Override
    public void implementJar(final Class<?> token, final Path jarFile) throws ImplerException {
        Path tmpDir = Path.of("./temp");
        try {
            Files.createDirectory(tmpDir);
        } catch (final IOException e) {
            throw new ImplerException("Can't create temporary directory", e);
        }

        implement(token, tmpDir);
        compile(token, tmpDir);

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        createParents(jarFile);
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            try (InputStream classStream = Files.newInputStream(resolveTokenPath(tmpDir, token, CLASS_FILE_EXT))) {
                try {
                    JarEntry entry = new JarEntry(getImplName(token, JAR_PACKAGES_SEPARATOR, CLASS_FILE_EXT));
                    jarOutputStream.putNextEntry(entry);
                    classStream.transferTo(jarOutputStream);
                } catch (final IOException e) {
                    throw new ImplerException("Error while writing to jar file", e);
                }
            } catch (final IOException e) {
                throw new ImplerException("Can't open created .class file", e);
            }
        } catch (final IOException e) {
            throw new ImplerException("Can't open jar file to write", e);
        }

        try {
            Files.walkFileTree(tmpDir, DELETE);
        } catch (final IOException ignored) {
        }
    }

    public static void main(String[] args) {
        if (args == null) {
            System.err.println("Expected non-null array of arguments");
            return;
        }
        if (args.length != 1 && args.length != 3) {
            System.err.println("Expected <class name to implement> or -jar <class name> <jar file name>.jar");
            return;
        }
        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Expected non-null arguments");
            return;
        }

        try {
            if (args.length == 1) {
                Class<?> token = Class.forName(args[0]);
                new Implementor().implement(token, Path.of("."));
            } else {
                Class<?> token = Class.forName(args[1]);
                new Implementor().implementJar(token, Path.of(args[2]));
            }
        } catch (final ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
        } catch (final ImplerException e) {
            System.err.println("Error while implementing the class: " + e.getMessage());
        }
    }
}
