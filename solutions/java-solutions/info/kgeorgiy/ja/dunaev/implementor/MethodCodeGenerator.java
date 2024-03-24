package info.kgeorgiy.ja.dunaev.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * Record that generates code of the containing {@link Method}.
 *
 * @param method method, whose code should be generated
 * @author Dunaev Kirill
 * @see ExecutableCodeGenerator
 */
public record MethodCodeGenerator(Method method) {
    /**
     * Comparator that compare two methods by their return type.
     * One method is less than the other if it has narrower return type than the other.
     */
    public static final Comparator<MethodCodeGenerator> BY_NARROWER_RETURN_TYPE_COMPARATOR = (m1, m2) -> {
        if (m1.method.getReturnType().equals(m2.method.getReturnType())) {
            return 0;
        } else if (m1.hasNarrowerReturnTypeThan(m2)) {
            return -1;
        }
        return 1;
    };

    /**
     * Generates code of the {@link MethodCodeGenerator#method}.
     *
     * @return generated code
     */
    public String generateCode() {
        return ExecutableCodeGenerator.generateCode(
                method, true,
                method.getName(), method.getReturnType().getCanonicalName(),
                String.format("return %s;", getDefaultReturnValue(method.getReturnType()))
        );
    }

    /**
     * Gives a default value for the provided {@code token}. It is:
     * <ul>
     *     <li>{@code null} - for reference types</li>
     *     <li>{@code ""} - for {@code void}</li>
     *     <li>{@code false} - for {@code boolean}</li>
     * </ul>
     *
     * @param token {@code token}, whose default value is required
     * @return default value of the {@code token}
     */
    private static String getDefaultReturnValue(final Class<?> token) {
        if (token.isPrimitive()) {
            if (token == void.class) {
                return "";
            } else if (token == boolean.class) {
                return "false";
            }
            return "0";
        } else {
            return "null";
        }
    }

    /**
     * Checks whether this has narrower return type than the other.
     *
     * @param other other method to compare return types
     * @return {@code true} if this has narrower return type, {@code false} otherwise
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
