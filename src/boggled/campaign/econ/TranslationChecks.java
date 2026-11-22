package boggled.campaign.econ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure checks for translation data. No Starsector types, so the checks can be called without the game.
 */
public final class TranslationChecks {
    // A $ that does not start a token ($name) or a number ($100).
    private static final Pattern UNCLOSED_DOLLAR = Pattern.compile("\\$(?![A-Za-z0-9])");
    // $the market, $a world: a determiner used as if it were part of the token.
    private static final Pattern WHITESPACE_TOKEN = Pattern.compile("\\$(?:the|a|an|this|that) [A-Za-z]");

    private TranslationChecks() { }

    public static List<String> tokenProblems(String value) {
        List<String> problems = new ArrayList<>();
        if (value == null || value.isEmpty()) {
            return problems;
        }
        Matcher unclosed = UNCLOSED_DOLLAR.matcher(value);
        while (unclosed.find()) {
            int at = unclosed.start();
            String snippet = value.substring(at, Math.min(value.length(), at + 16));
            problems.add("token is unclosed or contains whitespace near \"" + snippet + "\"");
        }
        Matcher spaced = WHITESPACE_TOKEN.matcher(value);
        while (spaced.find()) {
            problems.add("token contains whitespace near \"" + spaced.group() + "\"");
        }
        return problems;
    }

    public static List<String> missingIds(Collection<String> expected, Collection<String> present) {
        List<String> missing = new ArrayList<>();
        Set<String> have = present == null ? Collections.emptySet() : new HashSet<>(present);
        if (expected == null) {
            return missing;
        }
        for (String id : expected) {
            if (id != null && !id.isEmpty() && !have.contains(id)) {
                missing.add(id);
            }
        }
        return missing;
    }
}
