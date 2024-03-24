package info.kgeorgiy.ja.dunaev.implementor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

/**
 * Record that generates code of the containing {@link Constructor}.
 *
 * @param ctor constructor whose code should be generated
 * @author Dunaev Kirill
 * @see ExecutableCodeGenerator
 */
public record ConstructorCodeGenerator(Constructor<?> ctor) {
    /**
     * Generates code of the {@link ConstructorCodeGenerator#ctor}.
     *
     * @return generated code
     */
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
