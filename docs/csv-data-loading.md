# TASC CSV Data Loading System

This document describes in detail how TASC loads configuration data from CSV files, including planet type mappings, suppressed conditions, and other mod configuration.

## Overview

TASC uses Starsector's built-in `SettingsAPI` to load and parse CSV files during game initialization. The CSV files are converted to JSON format internally by Starsector's engine, then processed by TASC's initialization code to populate in-memory data structures.

## Data Loading Architecture

### 1. CSV File Locations

All TASC CSV data files are located in the `data/campaign/terraforming/` directory:

```
data/campaign/terraforming/
├── planet_type_mapping.csv                    # Maps game planet type IDs to TASC categories
├── domed_cities_suppressed_conditions.csv     # Conditions suppressed by Domed Cities
└── stellar_reflector_array_suppressed_conditions.csv  # Conditions suppressed by Stellar Reflector Array
```

### 2. Loading Process

The CSV data loading process occurs during game initialization in the following sequence:

#### Step 1: Plugin Initialization (`BoggledTascPlugin.onGameLoad`)

When a game is loaded, `BoggledTascPlugin.onGameLoad()` is called by Starsector's mod loading system.

**Location:** `src/boggled/scripts/BoggledTascPlugin.java`

```java
@Override
public void onGameLoad() {
    // Prevent duplicate loading
    if (lastGameLoad != thisGameLoad) {
        return;
    }

    try {
        SettingsAPI settings = Global.getSettings();

        // Load planet type mapping CSV
        JSONArray planetTypeMapping = settings.getMergedSpreadsheetDataForMod(
            "planet_type_id",
            "data/campaign/terraforming/planet_type_mapping.csv",
            boggledTools.BoggledMods.tascModId
        );

        // Load suppressed conditions CSVs
        JSONArray domedCitiesSuppressedConditions = settings.getMergedSpreadsheetDataForMod(
            "condition_id",
            "data/campaign/terraforming/domed_cities_suppressed_conditions.csv",
            boggledTools.BoggledMods.tascModId
        );

        JSONArray stellarReflectorArraySuppressedConditions = settings.getMergedSpreadsheetDataForMod(
            "condition_id",
            "data/campaign/terraforming/stellar_reflector_array_suppressed_conditions.csv",
            boggledTools.BoggledMods.tascModId
        );

        // Initialize boggledTools data structures
        boggledTools.initializeDomedCitiesSuppressedConditionsFromJSON(domedCitiesSuppressedConditions);
        boggledTools.initializeStellarReflectorArraySuppressedConditionsFromJSON(stellarReflectorArraySuppressedConditions);
        boggledTools.initializePlanetMappingsFromJSON(planetTypeMapping);

    } catch (IOException | JSONException ex) {
        boggledTools.writeMessageToLog(ex.getMessage());
    }
}
```

#### Step 2: Starsector CSV→JSON Conversion

Starsector's `SettingsAPI.getMergedSpreadsheetDataForMod()` method handles the CSV parsing:

1. **Reads the CSV file** from the specified path
2. **Parses CSV rows** into JSON objects
3. **Returns a JSONArray** where each array element is a JSONObject representing one CSV row
4. **Handles merging** if multiple mods define the same CSV file (enables mod compatibility)

The method signature:
```java
JSONArray getMergedSpreadsheetDataForMod(String keyColumn, String csvPath, String modId)
```

**Parameters:**
- `keyColumn`: The column name to use as the unique key (for merging purposes)
- `csvPath`: Relative path to the CSV file within the mod
- `modId`: The mod ID requesting the data (from `mod_info.json`)

#### Step 3: JSON Data Processing

The loaded JSON data is passed to initialization methods in `boggledTools` that populate in-memory data structures.

## CSV File Formats

### Planet Type Mapping CSV

**File:** `data/campaign/terraforming/planet_type_mapping.csv`

**Purpose:** Maps every planet type ID in the game (vanilla + modded) to TASC's simplified planet categories.

**Format:**
```csv
planet_type_id,tasc_planet_type
barren,barren
barren_castiron,barren
terran,terran
US_jungle,jungle
```

**Columns:**
- `planet_type_id`: The actual planet type ID used by Starsector (from planet specs)
- `tasc_planet_type`: TASC's internal category (barren, desert, frozen, gas_giant, jungle, star, terran, toxic, tundra, volcanic, water)

**Processing:**
```java
public static void initializePlanetMappingsFromJSON(@NotNull JSONArray planetsJson) {
    for (int i = 0; i < planetsJson.length(); ++i) {
        JSONObject row = planetsJson.getJSONObject(i);
        String planetTypeId = row.getString("planet_type_id");
        String tascPlanetType = row.getString("tasc_planet_type");

        // Validate data
        if(planetTypeId == null || planetTypeId.isBlank() ||
           tascPlanetType == null || tascPlanetType.isBlank() ||
           !validPlanetTypes.contains(tascPlanetType)) {
            throw new RuntimeException("Invalid planet mapping data");
        }

        // Populate forward mapping (planet_type_id → tasc_planet_type)
        planetTypeIdToTascPlanetTypeMapping.put(planetTypeId, tascPlanetType);

        // Populate reverse mapping (tasc_planet_type → set of planet_type_ids)
        if(tascPlanetTypeToAllPlanetTypeIdsMapping.containsKey(tascPlanetType)) {
            tascPlanetTypeToAllPlanetTypeIdsMapping.get(tascPlanetType).add(planetTypeId);
        } else {
            tascPlanetTypeToAllPlanetTypeIdsMapping.put(tascPlanetType, new HashSet<>() {{
                add(planetTypeId);
            }});
        }
    }
}
```

**Data Structures Populated:**
- `HashMap<String, String> planetTypeIdToTascPlanetTypeMapping` - Maps specific planet type to TASC category
- `HashMap<String, HashSet<String>> tascPlanetTypeToAllPlanetTypeIdsMapping` - Reverse mapping for queries

### Suppressed Conditions CSVs

**Files:**
- `data/campaign/terraforming/domed_cities_suppressed_conditions.csv`
- `data/campaign/terraforming/stellar_reflector_array_suppressed_conditions.csv`

**Purpose:** Lists planet conditions that should be suppressed (have their effects disabled) when specific buildings are present.

**Format:**
```csv
condition_id
no_atmosphere
thin_atmosphere
toxic_atmosphere
# very_hot  # Comments start with #
```

**Columns:**
- `condition_id`: The condition ID to suppress (lines starting with `#` are treated as comments)

**Processing:**
```java
public static HashSet<String> getStringListFromJson(JSONArray json, String key) {
    HashSet<String> stringSet = new HashSet<>();

    for (int i = 0; i < json.length(); ++i) {
        JSONObject row = json.getJSONObject(i);
        String condition_id = row.getString(key);

        // Skip empty strings
        if (condition_id != null && !condition_id.isEmpty()) {
            stringSet.add(condition_id);
        }
    }

    return stringSet;
}
```

**Usage Example:**
```java
// Domed Cities suppressed conditions
HashSet<String> conditions = getStringListFromJson(domedCitiesSuppressedConditionsJSON, "condition_id");
boggledTools.domedCitiesSuppressedConditions = new ArrayList<String>(conditions);
```

**Data Structures Populated:**
- `ArrayList<String> domedCitiesSuppressedConditions` - Conditions suppressed by Domed Cities
- `ArrayList<String> stellarReflectorArraySuppressedConditions` - Conditions suppressed by Stellar Reflector Array

## In-Memory Data Structures

All loaded CSV data is stored in static fields within `boggledTools`:

### Planet Type Mappings

**Location:** `src/boggled/campaign/econ/boggledTools.java`

```java
// Forward mapping: planet_type_id → tasc_planet_type
private static final HashMap<String, String> planetTypeIdToTascPlanetTypeMapping = new HashMap<>();

// Reverse mapping: tasc_planet_type → set of planet_type_ids
private static final HashMap<String, HashSet<String>> tascPlanetTypeToAllPlanetTypeIdsMapping = new HashMap<>();

// TASC planet type properties
private static final HashMap<String, PlanetWaterLevel> tascPlanetTypeToBaseWaterLevelMapping = new HashMap<>();
private static final HashMap<String, ArrayList<Integer>> tascPlanetTypeToResourceLevelMapping = new HashMap<>();
private static final HashMap<String, Boolean> tascPlanetTypeCanImproveFarmlandMapping = new HashMap<>();
private static final HashMap<String, Boolean> tascPlanetTypeAllowsForTerraforming = new HashMap<>();
private static final HashMap<String, Boolean> tascPlanetTypeAllowsForHumanHabitability = new HashMap<>();
```

### Suppressed Conditions

```java
public static ArrayList<String> domedCitiesSuppressedConditions = new ArrayList<>();
public static ArrayList<String> stellarReflectorArraySuppressedConditions = new ArrayList<>();
```

## Data Access Patterns

### Getting TASC Planet Type

```java
// From a PlanetAPI object
public static String getTascPlanetType(PlanetAPI planet) {
    if(planet == null || planet.getSpec() == null || planet.getSpec().getPlanetType() == null) {
        return TascPlanetTypes.unknownPlanetId;
    }

    String planetTypeId = planet.getSpec().getPlanetType();
    return planetTypeIdToTascPlanetTypeMapping.getOrDefault(planetTypeId, TascPlanetTypes.unknownPlanetId);
}

// From a planet type ID string
public static String getTascPlanetType(String planetTypeId) {
    return planetTypeIdToTascPlanetTypeMapping.getOrDefault(planetTypeId, TascPlanetTypes.unknownPlanetId);
}
```

### Checking Terraforming Eligibility

```java
public static boolean tascPlanetTypeAllowsTerraforming(String tascPlanetType) {
    return tascPlanetTypeAllowsForTerraforming.getOrDefault(tascPlanetType, false);
}
```

### Getting All Planet Types for a Category

```java
public static HashSet<String> getAllPlanetTypeIdsForTascPlanetType(String tascPlanetType) {
    return tascPlanetTypeToAllPlanetTypeIdsMapping.getOrDefault(tascPlanetType, new HashSet<>());
}
```

## Error Handling

### CSV Parsing Errors

If CSV data cannot be parsed, the initialization code throws a `RuntimeException`:

```java
try {
    // CSV loading
} catch (IOException | JSONException ex) {
    boggledTools.writeMessageToLog(ex.getMessage());
}
```

### Data Validation

The planet mapping initialization validates data:

```java
if(planetTypeId == null || planetTypeId.isBlank() ||
   tascPlanetType == null || tascPlanetType.isBlank() ||
   !validPlanetTypes.contains(tascPlanetType)) {
    throw new RuntimeException(
        "You have a blank cell in the planet mapping CSV file, " +
        "or you've specified an invalid TASC planet type. " +
        "Delete the file and replace it with the original."
    );
}
```

## Mod Compatibility

### CSV Merging

Starsector's `getMergedSpreadsheetDataForMod()` allows multiple mods to define the same CSV file. This enables:

1. **Unknown Skies integration** - US planet types can be mapped by adding rows to `planet_type_mapping.csv`
2. **Other planet mods** - Any mod adding custom planet types can be supported by adding mappings

**Example:** If a mod called "NewPlanets" adds planet type `new_planet_alien`, another mod (or a compatibility patch) can extend the planet mapping CSV:

```csv
planet_type_id,tasc_planet_type
new_planet_alien,terran  # Maps alien planets to TASC's terran category
```

### Adding New Mappings

To add support for new planet types:

1. **Create or extend** `planet_type_mapping.csv` in your mod
2. **Add rows** for each new planet type ID
3. **Specify** the appropriate TASC category
4. **TASC will automatically** load the merged data at game startup

## CSV File Best Practices

### Commenting

CSV files support comments using `#`:

```csv
condition_id
# Suppressed by Domed Cities
no_atmosphere
thin_atmosphere
# Uncomment to suppress very_hot
# very_hot
```

### Validation

- **No blank cells** in required columns
- **Valid TASC planet types** only (barren, desert, frozen, gas_giant, jungle, star, terran, toxic, tundra, volcanic, water)
- **Unique planet type IDs** in the `planet_type_id` column
- **Valid condition IDs** that exist in the game

### Save-Game Compatibility

**Important:** Changes to CSV files during a savegame may not take effect immediately:

- **Planet mappings** apply immediately when loaded
- **Suppressed conditions** persist once applied:
  - Removing conditions from the CSV mid-game will **not** unsuppress them
  - To remove suppressed conditions mid-game: deconstruct the building first, then edit the CSV

## Performance Considerations

### Loading Frequency

CSV data is loaded:
- **Once** when the game is first loaded (`onGameLoad`)
- **Not reloaded** during normal gameplay
- **Re-loaded** when loading a saved game

### Memory Usage

All CSV data is kept in memory as static HashMaps:
- **Planet type mappings:** ~146 entries × 2 data structures
- **Suppressed conditions:** ~10-20 conditions per building
- **Negligible memory footprint** compared to sprites and sounds

## Debugging CSV Loading

### Logging

TASC logs errors during CSV loading:

```java
boggledTools.writeMessageToLog(ex.getMessage());
```

Check `starsector.log` for:
- CSV file not found errors
- JSON parsing errors
- Data validation errors

### Common Issues

**Issue:** "Invalid TASC planet type"
- **Cause:** Typo in `tasc_planet_type` column
- **Fix:** Use valid planet type IDs only

**Issue:** "Blank cell in planet mapping CSV"
- **Cause:** Empty cell in required column
- **Fix:** Fill all cells or remove empty rows

**Issue:** New planet types not recognized
- **Cause:** CSV not loaded or merged correctly
- **Fix:** Verify CSV path and mod ID in `getMergedSpreadsheetDataForMod()` call

## Related Code Locations

### CSV Loading Entry Point
- `src/boggled/scripts/BoggledTascPlugin.java:238-263`

### Data Initialization Methods
- `src/boggled/campaign/econ/boggledTools.java:790-832`
  - `initializePlanetMappingsFromJSON()`
  - `initializeDomedCitiesSuppressedConditionsFromJSON()`
  - `initializeStellarReflectorArraySuppressedConditionsFromJSON()`
  - `getStringListFromJson()`

### Data Access Methods
- `src/boggled/campaign/econ/boggledTools.java:662-690`
  - `getTascPlanetType(PlanetAPI)`
  - `getTascPlanetType(String)`
  - `tascPlanetTypeAllowsTerraforming()`
  - `getAllPlanetTypeIdsForTascPlanetType()`

### Data Structure Definitions
- `src/boggled/campaign/econ/boggledTools.java:244-342`
  - HashMap declarations for all CSV-loaded data

## Summary

TASC's CSV loading system leverages Starsector's built-in `SettingsAPI` to:
1. **Parse CSV files** into JSON format automatically
2. **Merge data** from multiple mods for compatibility
3. **Populate in-memory HashMaps** for fast runtime access
4. **Validate data** during initialization to fail fast on errors
5. **Provide modding hooks** for extending planet type support without code changes

This system makes TASC highly extensible - modders can add support for new planet types by simply editing CSV files, without touching any Java code.
