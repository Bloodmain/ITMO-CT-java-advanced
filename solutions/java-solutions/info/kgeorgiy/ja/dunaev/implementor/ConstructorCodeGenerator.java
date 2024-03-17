package info.kgeorgiy.ja.dunaev.implementor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

public record ConstructorCodeGenerator(Constructor<?> ctor) {
    public String generateCode() {
        final String parameters = ExecutableCodeGenerator.mapAndCommaJoin(ctor.getParameters(), Parameter::getName);

        return ExecutableCodeGenerator.generateCode(
                ctor,
                false,
                ctor.getDeclaringClass().getSimpleName() + ClassCodeGenerator.IMPL_SUFFIX,
                "",
                String.format("super(%s);", parameters)
        );
    }
}
