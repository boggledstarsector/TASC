package boggled.campaign.econ;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Registers LunaLib settings from the structure table. Names and tooltips come from tasc_luna.
 * data/config/LunaSettings.csv is not shipped, because LunaLib would show that file's prose columns.
 */
public final class LunaSettingsRegistrar {
    private static final String PATH = "data/config/boggled/luna_settings_structure.csv";

    private LunaSettingsRegistrar() { }

    public static void register() {
        if (!Global.getSettings().getModManager().isModEnabled(boggledTools.BoggledMods.lunalibModId)) {
            return;
        }
        String modId = boggledTools.BoggledMods.tascModId;
        JSONArray rows;
        try {
            rows = Global.getSettings().getMergedSpreadsheetDataForMod("fieldID", PATH, modId);
        } catch (Exception ex) {
            boggledTools.writeMessageToLog("Failed to read LunaLib structure table: " + ex.getMessage());
            return;
        }
        if (rows == null) {
            boggledTools.writeMessageToLog("LunaLib structure table is missing: " + PATH);
            return;
        }

        Set<String> present = new HashSet<>();
        for (int i = 0; i < rows.length(); i++) {
            try {
                JSONObject row = rows.getJSONObject(i);
                String fieldId = row.optString("fieldID", "").trim();
                if (fieldId.isEmpty()) {
                    continue;
                }
                present.add(fieldId);
                registerRow(modId, row, fieldId);
            } catch (Exception ex) {
                boggledTools.writeMessageToLog("Failed to register a LunaLib setting: " + ex.getMessage());
            }
        }

        for (String missing : TranslationChecks.missingIds(Arrays.asList(LunaSettingIds.FIELD_IDS), present)) {
            boggledTools.writeMessageToLog("Missing LunaLib field id: " + missing);
        }
        LunaSettings.SettingsCreator.refresh(modId);
    }

    private static void registerRow(String modId, JSONObject row, String fieldId) {
        String type = row.optString("fieldType", "");
        String tab = row.optString("tab", "");
        String name = StringHelper.getString(StringHelper.LUNA, "name_" + fieldId);
        String tooltip = tooltipOrEmpty(fieldId);
        switch (type) {
            case "Header" -> LunaSettings.SettingsCreator.addHeader(modId, fieldId, name, tab);
            case "Boolean" -> LunaSettings.SettingsCreator.addBoolean(
                    modId, fieldId, name, tooltip, parseBoolean(row.optString("defaultValue", "False")), tab);
            case "Int" -> {
                int value = parseInt(row.optString("defaultValue", "0"), 0);
                int min = parseInt(row.optString("minValue", ""), Integer.MIN_VALUE);
                int max = parseInt(row.optString("maxValue", ""), Integer.MAX_VALUE);
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
                LunaSettings.SettingsCreator.addInt(modId, fieldId, name, tooltip, value, min, max, tab);
            }
            default -> boggledTools.writeMessageToLog("Unknown LunaLib field type " + type + " for " + fieldId);
        }
    }

    private static String tooltipOrEmpty(String fieldId) {
        String key = "tooltip_" + fieldId;
        String value = StringHelper.getString(StringHelper.LUNA, key);
        if (value.startsWith("[INVALID]")) {
            boggledTools.writeMessageToLog("Missing LunaLib tooltip " + key);
            return "";
        }
        return value;
    }

    private static boolean parseBoolean(String raw) {
        return raw != null && raw.trim().equalsIgnoreCase("true");
    }

    private static int parseInt(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            boggledTools.writeMessageToLog("Invalid LunaLib integer \"" + raw + "\"");
            return fallback;
        }
    }
}
