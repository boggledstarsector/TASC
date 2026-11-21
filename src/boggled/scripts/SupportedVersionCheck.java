package boggled.scripts;

import boggled.campaign.econ.StringHelper;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModManagerAPI;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.SettingsAPI;

/**
 * Stops launch when this build is running on too old a game, or on too old an Ashlib.
 * Numbers are compared as dotted integers. A trailing letter and an -RC suffix are ignored,
 * and a missing trailing number counts as 0. An unreadable version is refused.
 */
public final class SupportedVersionCheck {
    static final int[] GAME_MINIMUM = {0, 98, 5};
    static final int[] ASHLIB_MINIMUM = {2, 2, 1};
    private static final String ASHLIB_ID = "ashlib";

    private SupportedVersionCheck() { }

    public static void check() {
        SettingsAPI settings = Global.getSettings();
        if (settings == null) {
            throw new RuntimeException(gameUnreadable());
        }
        String gameVersion = settings.getVersionString();
        if (!isReadable(gameVersion)) {
            throw new RuntimeException(gameUnreadable());
        }
        if (isOlderThan(gameVersion, GAME_MINIMUM)) {
            throw new RuntimeException(gameTooOld(gameVersion.trim()));
        }

        ModManagerAPI mods = settings.getModManager();
        if (mods == null) {
            throw new RuntimeException(modListUnreadable());
        }
        if (!mods.isModEnabled(ASHLIB_ID)) {
            return;
        }
        ModSpecAPI spec = mods.getModSpec(ASHLIB_ID);
        String ashlibVersion = spec == null ? null : spec.getVersion();
        if (!isReadable(ashlibVersion)) {
            throw new RuntimeException(ashlibUnreadable());
        }
        if (isOlderThan(ashlibVersion, ASHLIB_MINIMUM)) {
            throw new RuntimeException(ashlibTooOld(ashlibVersion.trim()));
        }
    }

    /** False when the version cannot be read. Call {@link #isReadable} first. */
    static boolean isOlderThan(String version, int[] minimum) {
        int[] parts = components(version);
        if (parts == null) {
            return false;
        }
        return compare(parts, minimum) < 0;
    }

    static boolean isReadable(String version) {
        return components(version) != null;
    }

    static int[] components(String raw) {
        if (raw == null) {
            return null;
        }
        String version = raw.trim();
        if (version.regionMatches(true, 0, "Starsector ", 0, "Starsector ".length())) {
            version = version.substring("Starsector ".length()).trim();
        }
        int length = version.length();
        int index = 0;
        int[] parts = new int[8];
        int count = 0;
        boolean expectDigit = true;
        while (index < length && count < parts.length) {
            if (!Character.isDigit(version.charAt(index))) {
                return null;
            }
            int value = 0;
            while (index < length && Character.isDigit(version.charAt(index))) {
                int digit = version.charAt(index) - '0';
                if (value > (Integer.MAX_VALUE - digit) / 10) {
                    return null;
                }
                value = value * 10 + digit;
                index++;
            }
            parts[count++] = value;
            expectDigit = false;
            if (index < length && version.charAt(index) == '.') {
                index++;
                expectDigit = true;
                continue;
            }
            break;
        }
        if (count == 0 || expectDigit) {
            return null;
        }
        if (index < length && Character.isLetter(version.charAt(index))) {
            index++;
        }
        if (index < length) {
            if (!version.regionMatches(true, index, "-RC", 0, 3)) {
                return null;
            }
            index += 3;
            while (index < length && Character.isDigit(version.charAt(index))) {
                index++;
            }
            if (index != length) {
                return null;
            }
        }
        int[] result = new int[count];
        System.arraycopy(parts, 0, result, 0, count);
        return result;
    }

    private static int compare(int[] left, int[] right) {
        int length = Math.max(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int a = i < left.length ? left[i] : 0;
            int b = i < right.length ? right[i] : 0;
            if (a != b) {
                return a < b ? -1 : 1;
            }
        }
        return 0;
    }

    private static String gameTooOld(String gameVersion) {
        return sentence("game_version_too_old", "gameVersion", gameVersion,
                "TASC requires Starsector 0.98.5a or newer. This game is " + gameVersion + ".");
    }

    private static String gameUnreadable() {
        return sentence("game_version_unreadable", null, null,
                "TASC requires Starsector 0.98.5a or newer. This game's version could not be read.");
    }

    private static String ashlibTooOld(String ashlibVersion) {
        return sentence("ashlib_version_too_old", "ashlibVersion", ashlibVersion,
                "TASC requires Ashlib 2.2.1 or newer when Ashlib is enabled. The enabled Ashlib is " + ashlibVersion + ".");
    }

    private static String ashlibUnreadable() {
        return sentence("ashlib_version_unreadable", null, null,
                "TASC requires Ashlib 2.2.1 or newer when Ashlib is enabled. The enabled Ashlib version could not be read.");
    }

    private static String modListUnreadable() {
        return sentence("mod_list_unreadable", null, null,
                "TASC could not read the list of enabled mods, so it could not check the Ashlib version.");
    }

    private static String sentence(String id, String token, String value, String fallback) {
        try {
            String message = token == null
                    ? StringHelper.getString(StringHelper.GENERAL, id)
                    : StringHelper.getString(StringHelper.GENERAL, id, StringHelper.tokens(token, value));
            if (message == null || message.isEmpty() || message.startsWith("[INVALID]")) {
                return fallback;
            }
            return message;
        } catch (Exception ex) {
            return fallback;
        }
    }
}
