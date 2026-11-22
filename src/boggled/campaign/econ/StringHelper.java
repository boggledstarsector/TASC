package boggled.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads data/strings/strings.json. Substitution is a literal replace of $token and $Token.
 * A missing key returns "[INVALID] " plus the id and is logged once.
 */
public final class StringHelper {
    public static final String GENERAL = "tasc_general";
    public static final String UI = "tasc_ui";
    public static final String ABILITIES = "tasc_abilities";
    public static final String TERRAFORMING = "tasc_terraforming";
    public static final String INDUSTRIES = "tasc_industries";
    public static final String INTEL = "tasc_intel";
    public static final String LUNA = "tasc_luna";

    private static final Set<String> loggedMissing = new HashSet<>();
    private static final Set<String> loggedUnknownTokens = new HashSet<>();
    private static final Pattern TOKEN = Pattern.compile("\\$[A-Za-z][A-Za-z0-9_]*");

    private StringHelper() { }

    public static String getString(String id) {
        return getString(GENERAL, id);
    }

    public static String getString(String category, String id) {
        if (id == null) {
            return "";
        }
        try {
            return Global.getSettings().getString(category, id);
        } catch (Exception ex) {
            String key = category + ":" + id;
            if (loggedMissing.add(key)) {
                Global.getLogger(StringHelper.class).warn("Missing string " + key, ex);
                boggledTools.writeMessageToLog("Missing string " + key);
            }
            return "[INVALID] " + id;
        }
    }

    public static String getString(String category, String id, Map<String, String> tokens) {
        return substituteTokens(getString(category, id), tokens);
    }

    public static Map<String, String> tokens(String... pairs) {
        Map<String, String> map = new LinkedHashMap<>();
        if (pairs == null) {
            return map;
        }
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1] == null ? "" : pairs[i + 1]);
        }
        return map;
    }

    /**
     * Replaces $name and $Name. Longer names are replaced first.
     * Unknown $tokens are left in place and logged once.
     */
    public static String substituteTokens(String text, Map<String, String> tokens) {
        if (text == null || tokens == null || tokens.isEmpty()) {
            return text;
        }
        List<String> names = new ArrayList<>();
        for (String raw : tokens.keySet()) {
            if (raw == null || raw.isEmpty()) {
                continue;
            }
            names.add(raw.startsWith("$") ? raw.substring(1) : raw);
        }
        names.sort((a, b) -> Integer.compare(b.length(), a.length()));
        String result = text;
        for (String name : names) {
            String value = tokens.get(name);
            if (value == null) {
                value = tokens.get("$" + name);
            }
            if (value == null) {
                value = "";
            }
            result = result.replace("$" + name, value);
            if (!name.isEmpty() && Character.isLowerCase(name.charAt(0))) {
                String capitalName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                result = result.replace("$" + capitalName, capitalize(value));
            }
        }
        Matcher matcher = TOKEN.matcher(result);
        while (matcher.find()) {
            String found = matcher.group();
            if (loggedUnknownTokens.add(found + "|" + text)) {
                boggledTools.writeMessageToLog("Unknown token " + found + " left in string: " + text);
            }
        }
        return result;
    }

    public static LabelAPI applyHighlight(LabelAPI label, java.awt.Color highlightColor, String... highlights) {
        if (label == null || highlights == null || highlights.length == 0) {
            return label;
        }
        List<String> present = new ArrayList<>();
        for (String highlight : highlights) {
            if (highlight != null && !highlight.isEmpty()) {
                present.add(highlight);
            }
        }
        if (present.isEmpty()) {
            return label;
        }
        label.setHighlight(present.toArray(new String[0]));
        if (highlightColor != null) {
            label.setHighlightColor(highlightColor);
        }
        return label;
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value == null ? "" : value;
        }
        return Misc.ucFirst(value);
    }
}
