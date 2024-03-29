package info.kgeorgiy.ja.dunaev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class that disassembly the given {@code token}. Checks if the {@code token} can be implemented.
 *
 * @author Dunaev Kirill
 * @see Implementor
 */
public class ClassDisassembler {
    /**
     * Base tests for checks if the {@link ClassDisassembler#token} can be implemented.
     */
    private static final Map<Predicate<Class<?>>, String> BASE_IMPLEMENTABILITY_TESTS = Map.of(
            Class::isPrimitive, "Can't implement primitive type",
            Class::isArray, "Can't implement array type",
            Class::isSealed, "Can't implement sealed token",
            token -> token.isAssignableFrom(Record.class), "Can't implement record",
            token -> token.isAssignableFrom(Enum.class), "Can't implement enum",
            token -> Modifier.isFinal(token.getModifiers()), "Can't implement final typeToken",
            token -> Modifier.isPrivate(token.getModifiers()), "Can't implement private typeToken"
    );

    /**
     * The {@code token} to implement.
     */
    private final Class<?> token;

    /**
     * Any non-private constructor of the {@link ClassDisassembler#token}
     */
    private final ConstructorCodeGenerator nonPrivateCtor;

    /**
     * All methods of {@link ClassDisassembler#token} that should be implemented (abstract and not implemented already).
     */
    private final List<MethodCodeGenerator> methodsToImplement;

    /**
     * Constructor that collects all needed information about {@code token}.
     *
     * @param token {@code token} to be disassembled
     * @throws ImplerException if {@code token} can't be implemented
     */
    public ClassDisassembler(final Class<?> token) throws ImplerException {
        this.token = token;

        nonPrivateCtor = Arrays.stream(token.getDeclaredConstructors())
                .filter(ctor -> !Modifier.isPrivate(ctor.getModifiers()))
                .filter(ctor -> Arrays.stream(ctor.getParameterTypes()).noneMatch(type -> Modifier.isPrivate(type.getModifiers())))
                .findAny()
                .map(ConstructorCodeGenerator::new)
                .orElse(null);

        Stream<MethodCodeGenerator> stream = findMethodsToImplement(token.getMethods()); // including methods from interfaces
        for (Class<?> superclassToken = token; superclassToken != null; superclassToken = superclassToken.getSuperclass()) {
            stream = Stream.concat(stream, findMethodsToImplement(superclassToken.getDeclaredMethods()));
        }
        methodsToImplement = stream.distinct()
                .filter(method -> Modifier.isAbstract(method.method().getModifiers()))
                .toList();

        assertImplementable();
    }

    /**
     * Filters the methods leaving only those, who needs to be implemented.
     * If there are more than one method with same signature, then leaves the method with narrower return type
     * (as for {@link MethodCodeGenerator#BY_NARROWER_RETURN_TYPE_COMPARATOR})
     *
     * @param methods methods to filter
     * @return stream of wrapped by {@link MethodCodeGenerator} methods which needs to be implemented
     */
    private Stream<MethodCodeGenerator> findMethodsToImplement(Method[] methods) {
        return Arrays.stream(methods)
                .map(MethodCodeGenerator::new)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.collectingAndThen(
                                Collectors.minBy(MethodCodeGenerator.BY_NARROWER_RETURN_TYPE_COMPARATOR),
                                Optional::orElseThrow
                        )
                )).values().stream();
    }

    /**
     * Checks if the {@link ClassDisassembler#token} can be implemented.
     *
     * @throws ImplerException if the token can't be implemented.
     */
    private void assertImplementable() throws ImplerException {
        final Optional<String> failedMessage = BASE_IMPLEMENTABILITY_TESTS.entrySet().stream()
                .filter(entry -> entry.getKey().test(token))
                .findAny()
                .map(Map.Entry::getValue);

        if (failedMessage.isPresent()) {
            throw new ImplerException(failedMessage.get());
        }

        if (!token.isInterface() && nonPrivateCtor == null) {
            throw new ImplerException("Can't implement typeToken with only private ctors");
        }

        Stream<Class<?>> typesStream = Stream.concat(
                methodsToImplement.stream()
                        .flatMap(method -> Arrays.stream(method.method().getParameterTypes())),
                methodsToImplement.stream()
                        .map(method -> method.method().getReturnType())
        );

        if (typesStream.anyMatch(type -> Modifier.isPrivate(type.getModifiers()))) {
            throw new ImplerException("Can't implement typeToken with method that uses private types");
        }
    }

    /**
     * Generates code of the {@link ClassDisassembler#token} implementation.
     *
     * @return generated code of the {@link ClassDisassembler#token} implementation.
     */
    public String generateCode() {
        return ClassCodeGenerator.generateCode(token, nonPrivateCtor, methodsToImplement);
    }
}
