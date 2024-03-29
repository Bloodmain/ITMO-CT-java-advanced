package info.kgeorgiy.ja.dunaev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * Implementation of {@link JarImpler} interface. Contains methods for implementing provided type {@code token}.
 * Creates either .java or .jar file.
 *
 * @author Dunaev Kirill
 */
public class Implementor implements JarImpler {
    /**
     * Extension for java files.
     */
    private static final String JAVA_EXT = ".java";
    /**
     * Extension for class files.
     */
    private static final String CLASS_FILE_EXT = ".class";
    /**
     * Separator for packages in java code.
     */
    private static final String PACKAGES_SEPARATOR = ".";
    /**
     * Separator for packages in jar files.
     */
    private static final String JAR_PACKAGES_SEPARATOR = "/";
    /**
     * Unicode format for mapping non-ascii characters.
     */
    private static final String UNICODE_FORMAT = "\\u%04x";
    /**
     * Unicode encoder, that checks whether the character can be represented as ASC-II
     */
    private static final CharsetEncoder ASCII_ENCODER = StandardCharsets.US_ASCII.newEncoder();

    /**
     * Default constructor.
     */
    public Implementor() {
    }

    /**
     * File visitor that deletes all files and all directories it visits.
     */
    private static final FileVisitor<Path> DELETE = new SimpleFileVisitor<>() {
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

    /**
     * Creates parents directories of a given path. Does not rethrow {@link IOException} if one occurs.
     *
     * @param path a path, whose parents directories are to be created
     */

    private static void createParents(final Path path) {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (final IOException ignored) {
            }
        }
    }

    /**
     * Makes name for implementation file with given extension.
     * The name consists of {@code token} {@link Class#getPackageName()}, {@link Class#getSimpleName()}, "Impl" suffix and the given extension.
     * Entries (except extension) are separated by the given separator.
     *
     * @param token     {@code token} of the class, whose name is needed
     * @param separator separator of entries in the name
     * @param extension extension of the file to add in the end of the name
     * @return generated implementation's name
     */
    private static String getImplName(final Class<?> token, final String separator, final String extension) {
        String implName = token.getPackageName() + PACKAGES_SEPARATOR + token.getSimpleName() + ClassCodeGenerator.IMPL_SUFFIX;
        return implName.replace(PACKAGES_SEPARATOR, separator) + extension;
    }

    /**
     * Resolves the given path with implementation name of the given {@code token}.
     * Generate implementation name with {@link Implementor#getImplName(Class, String, String)} with {@link File#separator}.
     *
     * @param root      the root of resolving
     * @param token     {@code token}, whose implementation name would be used for resolving
     * @param extension extension of the resulting implementation
     * @return resolved path
     */

    private static Path resolveTokenPath(final Path root, final Class<?> token, String extension) {
        return root.resolve(getImplName(token, File.separator, extension));
    }

    /**
     * Maps non-ascii characters of the string to its unicode representation.
     * I.e. maps to the \\u + hex representation of a char
     *
     * @param str string to encode
     * @return encoded string
     */
    private static String encode(final String str) {
        return str.chars()
                .mapToObj(ch -> ASCII_ENCODER.canEncode((char) ch) ?
                        String.valueOf((char) ch) :
                        String.format(UNICODE_FORMAT, ch))
                .collect(Collectors.joining());
    }

    @Override
    public void implement(final Class<?> token, final Path root) throws ImplerException {
        ClassDisassembler classDisassembler = new ClassDisassembler(token);

        Path path = resolveTokenPath(root, token, JAVA_EXT);
        createParents(path);
        try (final BufferedWriter writer = Files.newBufferedWriter(path)) {
            try {
                writer.write(encode(classDisassembler.generateCode()));
            } catch (final IOException e) {
                throw new ImplerException("Exception while writing to a file", e);
            }
        } catch (final IOException e) {
            throw new ImplerException("Can't open file to write class", e);
        }
    }

    /**
     * Gives classpath of the given {@code token}. If {@link ProtectionDomain#getCodeSource()} returns null, then the result is empty {@link String}.
     *
     * @param token {@code token}, whose classpath should be returned
     * @return classpath of the given token
     * @throws ImplerException if URISyntaxException is thrown while trying to get uri of source location
     */
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

    /**
     * Compiles the given {@code token}, whose code is located at {@code root}. Uses {@link StandardCharsets#UTF_8} encoding.
     *
     * @param token {@code token} to compile
     * @param root  directory, in which {@code token}'s source code is located
     * @throws ImplerException if no java compiler is found, or compiler return non-zero exit code
     */

    private static void compile(final Class<?> token, final Path root) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("No java compiler is available");
        }
        final String classpath = root + File.pathSeparator + getClassPath(token);

        final String[] args = new String[]{
                resolveTokenPath(root, token, JAVA_EXT).toString(),
                "-cp", classpath,
                "-encoding", StandardCharsets.UTF_8.name()
        };

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
                    jarOutputStream.closeEntry();
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

    /**
     * CLI for implementing the given {@code token}.
     * There are two ways of using:
     * <ul>
     *     <li>One command-line argument is provided - it is treated as a type-token to implement. Implemented file will be created in the current directory.</li>
     *     <li>Three arguments are provided - the first one should be "-jar". The second one is treated as a type-token to implement.
     *     The third one - as a target .jar file name.</li>
     * </ul>
     *
     * @param args command-line arguments to process
     */
    public static void main(String[] args) {
        if (args == null) {
            System.err.println("Expected non-null array of arguments");
            return;
        }
        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Expected non-null arguments");
            return;
        }
        if (!(args.length == 1 || (args.length == 3 && args[0].equals("-jar")))) {
            System.err.println("Expected <class name to implement> or -jar <class name> <jar file name>.jar");
            return;
        }

        try {
            final Implementor implementor = new Implementor();
            if (args.length == 1) {
                Class<?> token = Class.forName(args[0]);
                implementor.implement(token, Path.of("."));
            } else {
                Class<?> token = Class.forName(args[1]);
                implementor.implementJar(token, Path.of(args[2]));
            }
        } catch (final ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
        } catch (final ImplerException e) {
            System.err.println("Error while implementing the class: " + e.getMessage());
        }
    }
}
