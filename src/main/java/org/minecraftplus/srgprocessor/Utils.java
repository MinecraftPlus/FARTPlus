package org.minecraftplus.srgprocessor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Utils {

    public static final Set<String> JAVA_KEYWORDS;
    public static final String PRIMITIVE_TYPES = "ZCBSIJFDV";
    public static final Pattern DESC = Pattern.compile("\\[*L(?<cls>[^;]+);|([" + PRIMITIVE_TYPES + "])");

    public static String[] splitCase(String input) {
        return input.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
    }

    static {
        JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
                "abstract",
                "continue",
                "for",
                "new",
                "switch",
                "assert",
                "default",
                "goto",
                "package",
                "synchronized",
                "boolean",
                "do",
                "if",
                "private",
                "this",
                "break",
                "double",
                "implements",
                "protected",
                "throw",
                "byte",
                "else",
                "import",
                "public",
                "throws",
                "case",
                "enum",
                "instanceof",
                "return",
                "transient",
                "catch",
                "extends",
                "int",
                "short",
                "try",
                "char",
                "final",
                "interface",
                "static",
                "void",
                "class",
                "finally",
                "long",
                "strictfp",
                "volatile",
                "const",
                "float",
                "native",
                "super",
                "while"));
    }

    public void fun(String Interface) {

    }
}