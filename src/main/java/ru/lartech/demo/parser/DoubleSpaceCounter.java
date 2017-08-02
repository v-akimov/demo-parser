package ru.lartech.demo.parser;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by z003cptz on 24.03.2016.
 */
public class DoubleSpaceCounter implements Function<String, Integer> {
    public static final DoubleSpaceCounter INSTANCE = new DoubleSpaceCounter();

    private static final Pattern REGEXP = Pattern.compile("([^ ]  [^ ])");

    @Override
    public Integer apply(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }

        int count = 0;
        Matcher matcher = REGEXP.matcher(s);
        while (matcher.find()) {
            count++;
        }

        return count;
    }
}
