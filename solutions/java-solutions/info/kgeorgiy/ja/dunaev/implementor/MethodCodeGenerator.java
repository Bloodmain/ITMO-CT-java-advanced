package info.kgeorgiy.ja.dunaev.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public record MethodCodeGenerator(Method method) {
    public String generateCode() {
        return ExecutableCodeGenerator.generateCode(
                method, true,
                method.getName(), method.getReturnType().getCanonicalName(),
                String.format("return %s;", getDefaultReturnValue(method.getReturnType()))
        );
    }

    private static String getDefaultReturnValue(final Class<?> token) {
        if (token.isPrimitive()) {
            return switch (token.getSimpleName()) {
                case "void" -> "";
                case "boolean" -> "false";
                default -> "0";
            };
        } else {
            return "null";
        }
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
