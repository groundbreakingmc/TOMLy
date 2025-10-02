package com.github.groundbreakingmc.tomly.utils;

import org.jetbrains.annotations.NotNull;

public final class NumberUtils {

    private NumberUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static @NotNull String stripUnderscores(@NotNull StringBuilder numberBuilder) {
        if (indexOf(numberBuilder) == -1) return numberBuilder.toString();
        return stripUnderscores(numberBuilder.toString());
    }

    public static @NotNull String stripUnderscores(@NotNull String input) {
        if (input.indexOf('_') == -1) return input;
        final char[] result = new char[input.length()];
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c != '_') result[i] = c;
        }
        return new String(result);
    }

    private static int indexOf(@NotNull StringBuilder sb) {
        for (int i = 0; i < sb.length(); i++) if (sb.charAt(i) == '_') return i;
        return -1;
    }
}
