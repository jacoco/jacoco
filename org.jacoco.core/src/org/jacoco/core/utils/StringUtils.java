package org.jacoco.core.utils;

public class StringUtils {
    private StringUtils() {}

    public static String join(final Iterable<?> items, String separator) {
        if (separator == null) {
            separator = "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object item : items) {
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(item);
        }
        return sb.toString();
    }
}
