package poly.edu.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility class for generating URL-safe slugs from text.
 * Handles Vietnamese diacritics (e.g., "Áo Polo Nam Thoáng Khí" →
 * "ao-polo-nam-thoang-khi").
 */
public class SlugUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern MULTIDASH = Pattern.compile("-{2,}");

    /**
     * Vietnamese special character mapping.
     * Normalizer handles most diacritics, but đ/Đ needs manual replacement.
     */
    private static String replaceVietnameseChars(String text) {
        return text
                .replace("đ", "d")
                .replace("Đ", "D");
    }

    /**
     * Generate a URL-safe slug from the given text.
     *
     * Algorithm:
     * 1. Replace Vietnamese đ/Đ manually (Normalizer doesn't handle these)
     * 2. Use Unicode Normalizer to decompose diacritics (NFD)
     * 3. Remove diacritical marks (combining characters)
     * 4. Lowercase everything
     * 5. Replace whitespace with hyphens
     * 6. Remove non-alphanumeric characters (except hyphens)
     * 7. Collapse multiple consecutive hyphens
     * 8. Trim leading/trailing hyphens
     *
     * @param text the input text (e.g., product name in Vietnamese)
     * @return URL-safe slug (e.g., "ao-polo-nam-thoang-khi")
     */
    public static String toSlug(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        // Step 1: Replace Vietnamese đ/Đ
        String result = replaceVietnameseChars(text);

        // Step 2-3: Normalize and remove diacritics
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = result.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Step 4: Lowercase
        result = result.toLowerCase();

        // Step 5: Replace whitespace with hyphens
        result = WHITESPACE.matcher(result).replaceAll("-");

        // Step 6: Remove non-alphanumeric (keep hyphens)
        result = NONLATIN.matcher(result).replaceAll("");

        // Step 7: Collapse multiple hyphens
        result = MULTIDASH.matcher(result).replaceAll("-");

        // Step 8: Trim leading/trailing hyphens
        result = result.replaceAll("^-|-$", "");

        return result;
    }
}
