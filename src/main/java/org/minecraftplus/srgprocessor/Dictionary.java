package org.minecraftplus.srgprocessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class Dictionary {

    private final Map<Trigger, Action> rules = new LinkedHashMap<>();

    public Map<Trigger, Action> getRules() {
        return rules;
    }

    public Dictionary load(InputStream in) throws IOException {
        List<String> lines = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines()
                .map(Dictionary::stripComment)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());

        for (String line : lines) {
            String[] pts = line.split(" ");
            if (pts.length < 1)
                throw new IOException("Invalid dictionary rule line, not enough parts: " + line);

            Trigger trigger = new Trigger(pts[0]);
            Action action = new Action(pts[1]);
            rules.put(trigger, action);
        }

        return this;
    }

    private static String stripComment(String str) {
        int idx = str.indexOf('#');
        if (idx == 0)
            return "";
        if (idx != -1)
            str = str.substring(0, idx - 1);
        int end = str.length();
        while (end > 1 && str.charAt(end - 1) == ' ')
            end--;
        return end == 0 ? "" : str.substring(0, end);
    }

    public class Trigger  {

        Pattern pattern;
        Pattern filter;

        public Trigger(String line) throws IOException {
            String[] pts = line.split(":");
            if (pts.length < 1)
                throw new IOException("Invalid trigger line, not enough parts: " + line);

            this.withPattern(pts[0]);
            if (pts.length >= 2)
                this.withFilter(pts[1]);
        }

        public Trigger withPattern(String pattern) throws PatternSyntaxException {
            this.pattern = Pattern.compile(pattern);
            return this;
        }

        public Trigger withFilter(String filter) throws PatternSyntaxException {
            this.filter = Pattern.compile(filter);
            return this;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public Pattern getFilter() {
            return filter;
        }

        @Override
        public String toString() {
            return "Trigger{" +
                    "pattern=" + pattern +
                    ", filter=" + filter +
                    '}';
        }
    }

    public static class Action {

        Type type;
        String value;

        public Action(String line) throws IOException {
            String[] pts = line.split(":");
            if (pts.length < 1)
                throw new IOException("Invalid action line, not enough parts: " + line);

            try {
                this.type = Type.valueOf(pts[0]);
            } catch (IllegalArgumentException e) {
                // If regular string in dictionary, it mean replace
                this.type = Type.RENAME;
                this.value = line;
                return;
            }

            switch (type) {
                case RENAME:
                case PREFIX:
                case SUFFIX:
                    if (pts.length < 2)
                        throw new IOException("Invalid action line, no value for action: " + line);
                    this.value = pts[1];
                    break;
                case FIRST:
                case LAST:
                    break;
                default:
                    throw new IllegalStateException("Wait, that's illegal.");
            }
        }

        public Action withType(Type type) {
            this.type = type;
            return this;
        }

        public Action withValue(String value) {
            this.value = value;
            return this;
        }

        public Type getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public String act(Matcher matcher) {

            // Do action, return refactored
            switch (type) {
                case RENAME:
                    //System.out.println("RENAME V:" + value);
                    return matcher.replaceFirst(this.value);
                case PREFIX:
                    //System.out.println("PREFIX V:" + value + " G:" + matcher.group());
                    return this.value + matcher.group();
                case SUFFIX:
                    //System.out.println("SUFFIX G:" + matcher.group() + " V:" + value);
                    return matcher.group() +  this.value;
                case FIRST:
                    //System.out.println("FIRST S:" + Utils.splitCase(matcher.group())[0]);
                    return Utils.splitCase(matcher.group())[0];
                case LAST:
                    String[] words = Utils.splitCase(matcher.group());
                    //System.out.println("LAST S:" + words[words.length - 1]);
                    return words[words.length - 1];
                default:
                    throw new IllegalStateException("Wait, that's illegal.");
            }
        }

        @Override
        public String toString() {
            return "Action{" +
                    "type=" + type +
                    ", value='" + value + '\'' +
                    '}';
        }

        enum Type {
            RENAME, PREFIX, SUFFIX, FIRST, LAST
        }
    }
}
