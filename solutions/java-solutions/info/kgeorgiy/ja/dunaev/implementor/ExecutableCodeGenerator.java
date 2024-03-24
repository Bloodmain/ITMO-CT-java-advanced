package info.kgeorgiy.ja.dunaev.implementor;

import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to generate code of provided {@link Executable}.
 *
 * @author Dunaev Kirill
 */
public class ExecutableCodeGenerator {
    /**
     * Default constructor that does nothing.
     */
    public ExecutableCodeGenerator() {
    }

    /**
     * Generates code of an executable.
     *
     * @param executable    executable, whose code should be generated
     * @param isOverridable if the executable should be marked with {@code @Override} annotation
     * @param name          name of the executable
     * @param returnType    name of the returned type of this executable
     * @param content       content that will be places inside the executable
     * @return generated code
     */
    public static String generateCode(
            final Executable executable, boolean isOverridable,
            final String name, final String returnType, final String content
    ) {
        final String modifiers = Modifier.toString(makeNotAbstract(executable.getModifiers()));
        String header = Stream.of(modifiers, returnType, name)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));

        final String parameters = mapAndCommaJoin(
                executable.getParameters(),
                parameter -> parameter.getType().getCanonicalName() + " " + parameter.getName()
        );

        String exceptions = mapAndCommaJoin(executable.getExceptionTypes(), Class::getCanonicalName);
        if (!exceptions.isEmpty()) {
            exceptions = String.format(" throws %s", exceptions);
        }

        String override = "";
        if (isOverridable) {
            override = String.format("%s@Override%n", ClassCodeGenerator.TABULATION);
        }
        return String.format("%s%s%s(%s)%s {%n%s%s%n%s}%n",
                override,
                ClassCodeGenerator.TABULATION,
                header,
                parameters,
                exceptions,
                ClassCodeGenerator.TABULATION.repeat(2),
                content,
                ClassCodeGenerator.TABULATION
        );
    }

    /**
     * Makes provided {@link Modifier} non-abstract and non-transient.
     *
     * @param mod modifiers to change
     * @return changed modifier
     */
    private static int makeNotAbstract(int mod) {
        return mod & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT;
    }

    /**
     * Maps the given array and join it with comma.
     *
     * @param arr    an array to process
     * @param mapper map function
     * @param <T>    array element's type
     * @return joined string
     */
    public static <T> String mapAndCommaJoin(T[] arr, Function<T, String> mapper) {
        return Arrays.stream(arr)
                .map(mapper)
                .collect(Collectors.joining(", "));
    }
}
