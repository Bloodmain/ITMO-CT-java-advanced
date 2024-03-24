package info.kgeorgiy.ja.dunaev.implementor;

import java.util.List;
import java.util.StringJoiner;

/**
 * Class to generate code of the {@code token} implementation.
 *
 * @author Dunaev Kirill
 * @see ClassDisassembler
 */
public class ClassCodeGenerator {
    /**
     * A suffix for implementation class' name.
     */
    public static final String IMPL_SUFFIX = "Impl";
    /**
     * A tabulation for formatting code.
     */
    public static final String TABULATION = " ".repeat(4);

    /**
     * Default constructor that does nothing.
     */
    public ClassCodeGenerator() {
    }

    /**
     * Generates {@code token}'s implementation code.
     * Caller should provide constructor and all methods that should be implemented.
     *
     * @param token   {@code token}, whose implementation's code should be generated
     * @param ctor    non-private constructor of the {@code token}
     * @param methods {@code token}'s methods that should be implemented
     * @return generated code
     */
    public static String generateCode(Class<?> token, ConstructorCodeGenerator ctor, List<MethodCodeGenerator> methods) {
        StringJoiner sj = new StringJoiner(System.lineSeparator());

        String packageName = token.getPackageName();
        if (!packageName.isEmpty()) {
            sj.add(String.format("package %s;%n", packageName));
        }

        sj.add(String.format(
                "public class %s %s %s {",
                token.getSimpleName() + IMPL_SUFFIX,
                token.isInterface() ? "implements" : "extends",
                token.getCanonicalName()
        ));

        if (ctor != null) {
            sj.add(ctor.generateCode());
        }
        methods.stream()
                .map(MethodCodeGenerator::generateCode)
                .forEach(sj::add);
        return sj + "}" + System.lineSeparator();
    }
}
