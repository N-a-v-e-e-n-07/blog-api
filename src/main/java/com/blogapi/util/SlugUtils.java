package com.blogapi.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public final class SlugUtils {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern MULTIPLE_DASHES = Pattern.compile("-{2,}");

    private SlugUtils() {}

    public static String toSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return MULTIPLE_DASHES.matcher(
                NON_LATIN.matcher(
                        WHITESPACE.matcher(normalized.toLowerCase(Locale.ENGLISH)).replaceAll("-")
                ).replaceAll("")
        ).replaceAll("-").trim();
    }

    public static String toUniqueSlug(String input) {
        return toSlug(input) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
