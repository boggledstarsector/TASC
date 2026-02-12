# Data-Driven Research System for TASC

## Overview

TASC now uses a **data-driven research system** that reads from AoTD's `aotd_tech_options.csv` file to determine which buildings and abilities require research projects. This replaces the previous hardcoded approach where each building/ability explicitly checked for specific research IDs.

## Benefits

1. **Modders can add custom research** by extending the CSV file without modifying Java code
2. **Flexible configuration** - research requirements are defined in data files, not compiled code
3. **Unified behavior** - all buildings use the same research checking mechanism
4. **Graceful degradation** - if CSV parsing fails, buildings remain available
5. **Backward compatible** - works with existing saves and without AoTD enabled

## Implementation Details

### Core Components

#### 1. Data Structures (boggledTools.java)

Three static maps store the research mappings:

- **`industryResearchMap`**: Maps industry IDs to required research project IDs
  - Key: Industry ID (e.g., "boggled_stellar_reflector_array")
  - Value: Research project ID (e.g., "tasc_light_manipulation") or null if no research required

- **`abilityResearchMap`**: Maps ability IDs to required research project IDs
  - Key: Ability ID (e.g., "boggled_construct_astropolis_station")
  - Value: Research project ID (e.g., "tasc_astropolis_construction") or null if no research required

- **`researchNamesMap`**: Maps research project IDs to display names
  - Key: Research ID (e.g., "tasc_light_manipulation")
  - Value: Display name (e.g., "Stellar Light Manipulation")

#### 2. CSV Loading (boggledTools.loadAotdTechOptionsCSV())

Called during game load in `BoggledTascPlugin.onGameLoad()`, this method:
- Clears existing mappings
- Checks if AoTD is enabled (returns early if not)
- Reads `data/campaign/aotd_tech_options.csv`
- Parses each row to extract research ID, name, and rewards
- Builds the industry-to-research mapping by parsing the rewards column
- Logs the number of TASC industry mappings found

#### 3. Rewards Parsing (boggledTools.parseResearchRewards())

The rewards column can contain multiple entries separated by `\n`. The parser:
- Splits the rewards string by `\n`
- Checks each entry for the `:industry` or `:ability` suffix
- For industries: Verifies the item ID starts with `BOGGLED_` or `boggled_` (case-insensitive)
  - Normalizes industry IDs to lowercase
  - Adds the mapping to `industryResearchMap`
- For abilities: Verifies the item ID starts with `boggled_`
  - Normalizes ability IDs to lowercase
  - Adds the mapping to `abilityResearchMap`

#### 4. Main Utility Function (boggledTools.isBuildingResearchComplete())

The core function that checks if a building's research is complete:

```java
public static boolean isBuildingResearchComplete(String industryId)
```

Returns `true` if:
- AoTD is not enabled, OR
- Building has no research requirement in CSV, OR
- Required research is completed

Returns `false` if:
- AoTD is enabled AND
- Building has research requirement AND
- Research is not completed

#### 5. Accessor Methods

- **`getRequiredResearchForIndustry(String industryId)`**: Returns the research ID required for a building, or null if no research is required
- **`getRequiredResearchForAbility(String abilityId)`**: Returns the research ID required for an ability, or null if no research is required
- **`getAbilityResearchMap()`**: Returns the complete ability-to-research map (for use by BoggledAotDEveryFrameScript)
- **`getResearchDisplayName(String researchId)`**: Returns the display name for a research project

## Building Updates

All 21 TASC industries now use `isBuildingResearchComplete(this.getId())` instead of hardcoded research checks.

### Buildings with Existing Research (11 files)

1. Boggled_Stellar_Reflector_Array - `tasc_light_manipulation`
2. Boggled_Atmosphere_Processor - `tasc_atmosphere_manipulation`
3. Boggled_Domed_Cities - `tasc_resource_manipulation`
4. Boggled_Ismara_Sling - `tasc_resource_manipulation`
5. Boggled_Genelab - `tasc_genetic_manipulation`
6. Boggled_Mesozoic_Park - `tasc_genetic_manipulation`
7. Boggled_Limelight_Network - `tasc_limelight_network`
8. Boggled_CHAMELEON - `tasc_chameleon`
9. Boggled_Kletka_Simulator - `tasc_remnant_algorithms`
10. Boggled_Domain_Archaeology - `tasc_domain_excavation`
11. Boggled_Remnant_Station - `tasc_remnant_station`

### Abilities with Research Requirements (4 abilities)

TASC abilities also use the data-driven research system. Ability IDs are specified in the CSV rewards column using the `:ability` suffix.

1. **boggled_colonize_abandoned_station** - Unlocked by `tasc_station_restoration`
2. **boggled_construct_astropolis_station** - Unlocked by `tasc_astropolis_construction`
3. **boggled_construct_mining_station** - Unlocked by `tasc_industrial_stations`
4. **boggled_construct_siphon_station** - Unlocked by `tasc_industrial_stations`

### Buildings without Research Requirements (10 files)

These buildings now have research checks but no entries in CSV (yet):
1. Boggled_Planet_Cracker
2. Boggled_Ouyang_Optimizer
3. Boggled_Perihelion_Project
4. Boggled_Cryosanctum
5. Boggled_Hydroponics
6. Boggled_Harmonic_Damper
7. Boggled_Planetary_Agrav_Field
8. Boggled_AI_Mining_Drones
9. Boggled_Expand_Station (removed from research system)
10. Boggled_GPA (disabled, returns false)

## CSV File Format

The `data/campaign/aotd_tech_options.csv` file follows this format:

```csv
id,name,tier,timeToResearch,reqToResearchFirst,itemReqToResearch,iconId,rewards,columnNumber,rowNumber,isResearchedFromStart,otherReq,tags,modId
tasc_light_manipulation,Stellar Light Manipulation,3,40,tasc_domain_excavation,research_databank:3,none,BOGGLED_STELLAR_REFLECTOR_ARRAY:industry,1,0,false,,,Terraforming & Station Construction
```

### Key Columns

- **Column 1 (`id`)**: Research project ID (e.g., "tasc_light_manipulation")
- **Column 2 (`name`)**: Display name (e.g., "Stellar Light Manipulation")
- **Column 8 (`rewards`)**: Comma-separated list of rewards, can include:
  - `BOGGLED_XXX:industry` - Unlocks a TASC building
  - `boggled_xxx:ability` - Unlocks a TASC ability
  - `:modifier` - Text description
  - Multiple entries separated by `\n`

### Multi-line Rewards Example

**Buildings:**
```csv
"Unlocks atmosphere modification terraforming projects:modifier
BOGGLED_ATMOSPHERE_PROCESSOR:industry"
```

**Abilities:**
```csv
"Allow building Astropolis type stations. You can add this ability to your ability bar once this technology is researched.:modifier
boggled_construct_astropolis_station:ability"
```

**Multiple Abilities from One Research:**
```csv
"Allow building Mining and Siphon type stations. You can add this ability to your ability bar once this technology is researched.:modifier
boggled_construct_mining_station:ability
boggled_construct_siphon_station:ability"
```

## How Modders Add Custom Research

To add a custom research project that unlocks a TASC building or ability:

1. Open `data/campaign/aotd_tech_options.csv`
2. Add a new row with your research project details
3. In the `rewards` column, add:
   - `BOGGLED_YOUR_BUILDING:industry` for buildings
   - `boggled_your_ability:ability` for abilities
4. Save the file and restart the game

### Example: Building

```csv
tasc_advanced_terraforming,Advanced Terraforming,3,90,tasc_planet_type_manipulation,research_databank:10,none,BOGGLED_CUSTOM_BUILDING:industry,5,0,false,,,Terraforming & Station Construction
```

### Example: Ability

```csv
tasc_custom_station_tech,Custom Station Technology,3,60,tasc_astropolis_construction,research_databank:5,none,"Custom description text:modifier
boggled_custom_station_ability:ability",2,2,false,,,Terraforming & Station Construction
```

## Error Handling

The system includes robust error handling:

1. **CSV file missing**: Logs error, continues with empty map → all buildings available
2. **Malformed CSV**: Catches exceptions, skips problematic rows → buildings with valid mappings work
3. **Empty rewards column**: Skipped in parsing → no mappings for that research project
4. **Building ID not found in map**: Returns null → building available
5. **Research project doesn't exist**: `isResearched()` returns false → building locked (correct behavior)

### Graceful Degradation

At worst, buildings become available (same as AoTD disabled). No crashes or broken states.

## Testing Checklist

### Buildings WITH Research in CSV

- [ ] New game with AoTD enabled - buildings locked until research completed
- [ ] Existing save with research completed - buildings become buildable
- [ ] Existing save with research not completed - buildings remain locked
- [ ] Research completed during gameplay - buildings unlock immediately

### Buildings WITHOUT Research in CSV

- [ ] New game with AoTD enabled - buildings available (no research required)
- [ ] New game with AoTD disabled - all buildings available
- [ ] Existing save - buildings remain available

### Abilities WITH Research in CSV

- [ ] New game with AoTD enabled - abilities NOT added to player until research completed
- [ ] Existing save with research completed - abilities automatically added to player
- [ ] Existing save with research not completed - abilities remain locked
- [ ] Research completed during gameplay - abilities added immediately to player's ability bar
- [ ] Multiple abilities from same research - both abilities unlocked correctly

### System-Level Tests

- [ ] Custom CSV entry added (building) - parsed correctly and locks/unlocks building
- [ ] Custom CSV entry added (ability) - parsed correctly and locks/unlocks ability
- [ ] CSV file deleted/missing - graceful degradation (all buildings/abilities available)
- [ ] Malformed CSV row - logged error, other rows still work
- [ ] Building/ability not in CSV - automatically available
- [ ] Save and reload - research mappings persist correctly

## Migration Notes

### For Players

- **Existing saves**: Research mappings are rebuilt on load from CSV
- **No action required**: System is backward compatible
- **With AoTD disabled**: All buildings and abilities available (same as before)

### For Modders

- **No Java code changes needed**: Just add entries to CSV
- **Can extend existing research**: Add new buildings/abilities to existing research project's rewards
- **Can create new research**: Add new rows with custom research IDs
- **Buildings**: Use `BOGGLED_XXX:industry` format (case-insensitive)
- **Abilities**: Use `boggled_xxx:ability` format (lowercase preferred)

## Files Modified

### Core Changes (3 files)

1. **src/boggled/campaign/econ/boggledTools.java**
   - Added `industryResearchMap`, `abilityResearchMap`, and `researchNamesMap` static maps
   - Added `loadAotdTechOptionsCSV()` method
   - Added `parseResearchRewards()` method (handles both `:industry` and `:ability`)
   - Added `readAllLinesFromCSV()` method
   - Added `getRequiredResearchForIndustry()` method
   - Added `getRequiredResearchForAbility()` method
   - Added `getAbilityResearchMap()` method
   - Added `getResearchDisplayName()` method
   - Added `isBuildingResearchComplete()` method
   - Added imports for BufferedReader, InputStreamReader, InputStream

2. **src/boggled/scripts/BoggledTascPlugin.java**
   - Added CSV loading call in `onGameLoad()`
   - Simplified `addAotDEveryFrameScript()` - removed hardcoded mapping

3. **src/boggled/scripts/BoggledAotDEveryFrameScript.java**
   - Rewrote to use data-driven map from `boggledTools.getAbilityResearchMap()`
   - Removed constructor parameter (no hardcoded map)
   - Simplified loop logic (one research per ability)

### Building Updates (21 files)

All TASC industries updated to use `isBuildingResearchComplete(this.getId())`:

**Buildings WITH existing research (11 files)**:
3. Boggled_Stellar_Reflector_Array.java
4. Boggled_Atmosphere_Processor.java
5. Boggled_Domed_Cities.java
6. Boggled_Ismara_Sling.java
7. Boggled_Genelab.java
8. Boggled_Mesozoic_Park.java
9. Boggled_Limelight_Network.java
10. Boggled_CHAMELEON.java
11. Boggled_Kletka_Simulator.java
12. Boggled_Domain_Archaeology.java
13. Boggled_Remnant_Station.java

**Buildings WITHOUT existing research (10 files)**:
14. Boggled_Planet_Cracker.java
15. Boggled_Ouyang_Optimizer.java
16. Boggled_Perihelion_Project.java
17. Boggled_Cryosanctum.java
18. Boggled_Hydroponics.java
19. Boggled_Harmonic_Damper.java
20. Boggled_Planetary_Agrav_Field.java
21. Boggled_AI_Mining_Drones.java
22. Boggled_Expand_Station.java
23. Boggled_GPA.java (disabled)

## CSV Data Files

**Default Tech Tree:**
- **data/campaign/aotd_tech_options.csv** - Contains TASC research project definitions with building and ability unlocks (11 buildings, default coverage)

**Alternate Complete Tech Tree:**
- **data/campaign/custom_tech_trees/aotd_tech_options.csv** - Alternate tech tree with complete coverage of all 21 buildings and 4 abilities
- **data/campaign/custom_tech_trees/README.md** - Instructions for switching to the alternate tech tree

See the "Alternate Tech Tree" section in `aotd-research-integration.md` for details on the differences between default and complete trees.

## Notes

- Industry IDs are normalized to lowercase for consistency
- Ability IDs are normalized to lowercase for consistency
- Research IDs in CSV are case-sensitive when used with `isResearched()`
- The `rewards` column parsing supports multiple entries separated by `\n`
- Buildings/abilities not listed in any research project's rewards are automatically available
- All 21 TASC buildings now use the unified research checking system
- All 4 TASC station construction abilities now use the unified research checking system
- The system is opt-in: buildings/abilities without research entries remain available
- The `BoggledAotDEveryFrameScript` runs every frame and checks research status
- Abilities are added to `CharacterDataAPI` when research is completed
- Already-unlocked abilities persist even if CSV is later modified
