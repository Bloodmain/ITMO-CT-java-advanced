package info.kgeorgiy.ja.dunaev.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

public record MethodCodeGenerator(Method method) {
    public static final Comparator<MethodCodeGenerator> BY_NARROWER_RETURN_TYPE_COMPARATOR = (m1, m2) -> {
        if (m1.method.getReturnType().equals(m2.method.getReturnType())) {
            return 0;
        } else if (m1.hasNarrowerReturnTypeThan(m2)) {
            return -1;
        }
        return 1;
    };

    private static final Map<Class<?>, String> UNIQUE_PRIMITIVE_DEFAULT_TYPES = Map.of(
            void.class, "",
            boolean.class, "false"
    );

    public String generateCode() {
        return ExecutableCodeGenerator.generateCode(
                method, true,
                method.getName(), method.getReturnType().getCanonicalName(),
                String.format("return %s;", getDefaultReturnValue(method.getReturnType()))
        );
    }

    private static String getDefaultReturnValue(final Class<?> token) {
        if (token.isPrimitive()) {
            return UNIQUE_PRIMITIVE_DEFAULT_TYPES.getOrDefault(token, "0");
        } else {
            return "null";
        }
    }

    /*
    Checks if other's return type can be assigned from this' return type.
    In other words, checks whether this has narrower return type than other
    */
    public boolean hasNarrowerReturnTypeThan(MethodCodeGenerator other) {
        return other.method.getReturnType().isAssignableFrom(method.getReturnType());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodCodeGenerator that) {
            return method.getName().equals(that.method.getName()) &&
                    Arrays.equals(method.getParameterTypes(), that.method.getParameterTypes());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method.getName(), Arrays.hashCode(method.getParameterTypes()));
    }
}
