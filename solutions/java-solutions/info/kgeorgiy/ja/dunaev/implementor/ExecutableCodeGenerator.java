package info.kgeorgiy.ja.dunaev.implementor;

import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExecutableCodeGenerator {
    private static final String TABULATION = " ".repeat(4);

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
            override = String.format("%s@Override%n", TABULATION);
        }
        return String.format("%s%s%s(%s)%s {%n%s%s%n%s}%n",
                override,
                TABULATION,
                header,
                parameters,
                exceptions,
                TABULATION.repeat(2),
                content,
                TABULATION
        );
    }

    private static int makeNotAbstract(int mod) {
        return mod & ~Modifier.ABSTRACT & ~Modifier.TRANSIENT;
    }

    public static <T> String mapAndCommaJoin(T[] arr, Function<T, String> mapper) {
        return Arrays.stream(arr)
                .map(mapper)
                .collect(Collectors.joining(", "));
    }
}
