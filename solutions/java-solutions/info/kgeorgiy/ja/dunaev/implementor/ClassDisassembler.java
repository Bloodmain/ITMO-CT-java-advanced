package info.kgeorgiy.ja.dunaev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassDisassembler {
    private static final Map<Predicate<Class<?>>, String> BASE_IMPLEMENTABILITY_TESTS = Map.of(
            Class::isPrimitive, "Can't implement primitive type",
            Class::isArray, "Can't implement array type",
            token -> token.isAssignableFrom(Enum.class), "Can't implement enum",
            token -> Modifier.isFinal(token.getModifiers()), "Can't implement final typeToken",
            token -> Modifier.isPrivate(token.getModifiers()), "Can't implement private typeToken"
    );

    private final Class<?> token;
    private final ConstructorCodeGenerator nonPrivateCtor;
    private final List<MethodCodeGenerator> methodsToImplement;

    public ClassDisassembler(final Class<?> token) throws ImplerException {
        this.token = token;

        nonPrivateCtor = Arrays.stream(token.getDeclaredConstructors())
                .filter(ctor -> !Modifier.isPrivate(ctor.getModifiers()))
                .findAny()
                .map(ConstructorCodeGenerator::new)
                .orElse(null);

        Stream<MethodCodeGenerator> stream = findMethodsToImplement(token.getMethods());
        for (Class<?> superclassToken = token; superclassToken != null; superclassToken = superclassToken.getSuperclass()) {
            stream = Stream.concat(stream, findMethodsToImplement(superclassToken.getDeclaredMethods()));
        }
        methodsToImplement = stream.distinct()
                .filter(method -> Modifier.isAbstract(method.method().getModifiers()))
                .toList();

        assertImplementable();
    }

    private Stream<MethodCodeGenerator> findMethodsToImplement(Method[] methods) {
        return Arrays.stream(methods)
                .map(MethodCodeGenerator::new);
    }

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

    public String generateCode() {
        return ClassCodeGenerator.generateCode(token, nonPrivateCtor, methodsToImplement);
    }
}
