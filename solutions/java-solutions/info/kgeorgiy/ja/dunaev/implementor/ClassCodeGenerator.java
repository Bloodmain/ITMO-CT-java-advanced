package info.kgeorgiy.ja.dunaev.implementor;

import java.util.List;
import java.util.StringJoiner;

public class ClassCodeGenerator {
    public static final String IMPL_SUFFIX = "Impl";

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
