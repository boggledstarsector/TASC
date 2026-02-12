# TASC and Ashes of the Domain (AoTD) Research Integration

This document describes in detail how TASC integrates with Ashes of the Domain (AoTD) "Vaults of Knowledge" research system, including how the tech tree is loaded and how TASC checks research completion to control building and project availability.

## Overview

TASC features an optional but deep integration with AoTD's research system. When AoTD is enabled, TASC buildings and terraforming projects require completion of specific research projects before they can be constructed or started.

### Key Integration Points

1. **Research-locked buildings** - Industries require specific technologies before construction
2. **Research-locked terraforming projects** - Projects require research completion to progress
3. **Research-locked abilities** - Station construction abilities unlock via research
4. **Dynamic ability granting** - Script grants abilities when research is completed

## AoTD Tech Tree Loading

### Tech Tree CSV File

**File:** `data/campaign/aotd_tech_options.csv`

This CSV file defines TASC's research projects in AoTD's tech tree format. Each row represents one research project that can be completed in the Vaults of Knowledge interface.

**CSV Format:**
```csv
id,name,tier,timeToResearch,reqToResearchFirst,itemReqToResearch,iconId,rewards,columnNumber,rowNumber,isResearchedFromStart,otherReq,tags,modId
```

**Example Entries:**
```csv
tasc_domain_excavation,Domain Artifact Excavation,3,25,aotd_tech_streamlined_production,research_databank:1,none,BOGGLED_DOMAIN_ARCHAEOLOGY:industry,0,3,false,,,Terraforming & Station Construction
tasc_light_manipulation,Stellar Light Manipulation,3,40,tasc_domain_excavation,research_databank:3,none,BOGGLED_STELLAR_REFLECTOR_ARRAY:industry,1,0,false,,,Terraforming & Station Construction
tasc_atmosphere_manipulation,Atmosphere Manipulation,3,60,tasc_light_manipulation,research_databank:5,none,"Unlocks atmosphere modification terraforming projects:modifier
BOGGLED_ATMOSPHERE_PROCESSOR:industry",2,0,false,,,Terraforming & Station Construction
tasc_resource_manipulation,Resource Manipulation,3,90,tasc_atmosphere_manipulation,research_databank:7,none,"Unlocks resource improvement terraforming projects:modifier
BOGGLED_DOMED_CITIES:industry
BOGGLED_ISMARA_SLING:industry",3,0,false,,,Terraforming & Station Construction
tasc_planet_type_manipulation,Planet Type Manipulation,3,120,tasc_resource_manipulation,research_databank:10,none,"Unlocks planet type change terraforming projects:modifier",4,0,false,,,Terraforming & Station Construction
```

### How AoTD Loads the Tech Tree

AoTD's research system automatically loads CSV files from enabled mods during game initialization:

1. **CSV Discovery** - AoTD scans all enabled mods for CSV files matching specific patterns
2. **Tech Tree Merging** - CSV data from multiple mods is merged into a single unified tech tree
3. **UI Rendering** - The Vaults of Knowledge UI renders the combined tech tree visually

**Important:** TASC does **not** manually load this CSV. AoTD handles all loading and parsing automatically.

### Research Project Definitions

TASC defines the following research projects in AoTD's tech tree:

#### Terraforming Tech Line (Column 0)

| Research ID | Name | Tier | Prerequisites | Rewards |
|-------------|------|------|---------------|---------|
| `tasc_domain_excavation` | Domain Artifact Excavation | 3 | `aotd_tech_streamlined_production` | `BOGGLED_DOMAIN_ARCHAEOLOGY` industry |
| `tasc_light_manipulation` | Stellar Light Manipulation | 3 | `tasc_domain_excavation` | `BOGGLED_STELLAR_REFLECTOR_ARRAY` industry |
| `tasc_atmosphere_manipulation` | Atmosphere Manipulation | 3 | `tasc_light_manipulation` | Atmosphere modification terraforming projects, `BOGGLED_ATMOSPHERE_PROCESSOR` industry |
| `tasc_resource_manipulation` | Resource Manipulation | 3 | `tasc_atmosphere_manipulation` | Resource improvement projects, `BOGGLED_DOMED_CITIES`, `BOGGLED_ISMARA_SLING` industries |
| `tasc_planet_type_manipulation` | Planet Type Manipulation | 3 | `tasc_resource_manipulation` | Planet type change terraforming projects |

#### Genetics Tech Line (Column 6)

| Research ID | Name | Tier | Prerequisites | Rewards |
|-------------|------|------|---------------|---------|
| `tasc_genetic_manipulation` | Genetics | 3 | `tasc_domain_excavation` | `BOGGLED_GENELAB`, `BOGGLED_MESOZOIC_PARK` industries |

#### AI/Tech Tech Line (Column 4)

| Research ID | Name | Tier | Prerequisites | Rewards |
|-------------|------|------|---------------|---------|
| `tasc_limelight_network` | Virtual Reality | 3 | `tasc_domain_excavation` | `BOGGLED_LIMELIGHT_NETWORK` industry |
| `tasc_chameleon` | Advanced Cryptography | 3 | `tasc_limelight_network` | `BOGGLED_CHAMELEON` industry |
| `tasc_remnant_algorithms` | Artificial Simulation | 3 | `tasc_chameleon` | `BOGGLED_KLETKA_SIMULATOR` industry |

#### Station Tech Line (Column 2)

| Research ID | Name | Tier | Prerequisites | Rewards |
|-------------|------|------|---------------|---------|
| `tasc_station_restoration` | Station Restoration Equipment | 3 | `tasc_domain_excavation` | Colonize abandoned stations ability |
| `tasc_astropolis_construction` | Orbital Colonies | 3 | `tasc_station_restoration` | Astropolis station construction ability |
| `tasc_industrial_stations` | Orbital Industrial Complex | 3 | `tasc_astropolis_construction` | Mining and Siphon station construction abilities |

#### Advanced Station Tech (Column 3)

| Research ID | Name | Tier | Prerequisites | Rewards                            |
|-------------|------|------|---------------|------------------------------------|
| `tasc_remnant_station` | AI Nexus Re-Engineering | 3 | `tasc_remnant_algorithms` + `tasc_industrial_stations` | `BOGGLED_REMNANT_STATION` industry |

## TASC Research Checking System

### Core Research Check Method

**Location:** `src/boggled/campaign/econ/boggledTools.java:2390-2403`

```java
public static boolean isResearched(String key)
{
    // Pass this.getId() as key if this function is called from an industry
    if(Global.getSettings().getModManager().isModEnabled("aotd_vok"))
    {
        return AoTDMainResearchManager.getInstance().isResearchedForPlayer(key);
    }
    else
    {
        // TASC does not have built-in research functionality.
        // Always return true if the player is not using a mod that implements research.
        return true;
    }
}
```

**Key Behaviors:**
1. **Mod Detection** - Checks if AoTD (`aotd_vok`) is enabled
2. **Research Query** - If AoTD is enabled, queries `AoTDMainResearchManager` for research status
3. **Graceful Fallback** - If AoTD is not enabled, always returns `true` (buildings available without research)

### Research Project Constants

**Location:** `src/boggled/campaign/econ/boggledTools.java:191-195`

```java
public static class BoggledResearchProjects {
    public static final String resourceManipulation = "tasc_resource_manipulation";
    public static final String atmosphereManipulation = "tasc_atmosphere_manipulation";
    public static final String planetTypeManipulation = "tasc_planet_type_manipulation";
}
```

These constants map directly to research IDs defined in `aotd_tech_options.csv`.

## Building Construction Locking

### Industry Research Checks

Industries check research completion in their `isAvailableToBuild()` method:

**Pattern A: Single Research Prerequisite**

Example: `Boggled_Stellar_Reflector_Array.java`
```java
@Override
public boolean isAvailableToBuild()
{
    if(!boggledTools.isResearched("tasc_light_manipulation"))
    {
        return false;
    }

    if(!boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") ||
       !boggledTools.getBooleanSetting("boggledStellarReflectorArrayEnabled"))
    {
        return false;
    }

    return true;
}
```

**Pattern B: Multiple Research Prerequisites**

Example: `Boggled_Kletka_Simulator.java`
```java
@Override
public boolean isAvailableToBuild()
{
    if(!boggledTools.isResearched("tasc_remnant_algorithms"))
    {
        return false;
    }

    // Additional checks...
    return true;
}
```

### Industries Requiring Research

| Industry | Research Required |
|----------|-------------------|
| `Boggled_Domain_Archaeology` | `tasc_domain_excavation` |
| `Boggled_Stellar_Reflector_Array` | `tasc_light_manipulation` |
| `Boggled_Atmosphere_Processor` | `tasc_atmosphere_manipulation` |
| `Boggled_Domed_Cities` | `tasc_resource_manipulation` |
| `Boggled_Ismara_Sling` | `tasc_resource_manipulation` |
| `Boggled_Genelab` | `tasc_genetic_manipulation` |
| `Boggled_Mesozoic_Park` | `tasc_genetic_manipulation` |
| `Boggled_Limelight_Network` | `tasc_limelight_network` |
| `Boggled_CHAMELEON` | `tasc_chameleon` |
| `Boggled_Kletka_Simulator` | `tasc_remnant_algorithms` |
| `Boggled_Remnant_Station` | `tasc_remnant_station` |

### UI Visibility Control

Industries also implement `showWhenUnavailable()` to control visibility in the building list:

```java
@Override
public boolean showWhenUnavailable()
{
    if(!boggledTools.isResearched("tasc_light_manipulation"))
    {
        return false;  // Hide from building list entirely
    }

    return true;  // Show as unavailable if other requirements aren't met
}
```

**Behavior:**
- **Research not completed** → Building is **hidden** from the list
- **Research completed** → Building is **shown** (may be unavailable due to other requirements)

## Terraforming Project Research Locking

### Project Type Research Requirements

**Location:** `src/boggled/terraforming/BoggledBaseTerraformingProject.java:384-411`

Terraforming projects are organized into three categories, each requiring different research:

```java
public ArrayList<TerraformingRequirementObject> getProjectRequirements() {
    ArrayList<TerraformingRequirementObject> projects = new ArrayList<>();
    if (Global.getSettings().getModManager().isModEnabled(boggledTools.BoggledMods.atodVokModId)) {
        projects.add(getRequirementProjectIsResearched());
    }

    return projects;
}

public TerraformingRequirementObject getRequirementProjectIsResearched() {
    Boolean requirementMet = switch (projectType) {
        case PLANET_TYPE_CHANGE ->
                boggledTools.isResearched(boggledTools.BoggledResearchProjects.planetTypeManipulation);
        case RESOURCE_IMPROVEMENT ->
                boggledTools.isResearched(boggledTools.BoggledResearchProjects.resourceManipulation);
        case CONDITION_IMPROVEMENT ->
                boggledTools.isResearched(boggledTools.BoggledResearchProjects.atmosphereManipulation);
    };
    // ... tooltip generation
}
```

### Terraforming Project Research Mapping

| Project Type | Research Required | Examples |
|--------------|-------------------|----------|
| **PLANET_TYPE_CHANGE** | `tasc_planet_type_manipulation` | Barren → Terran, Toxic → Jungle, etc. |
| **RESOURCE_IMPROVEMENT** | `tasc_resource_manipulation` | Farmland improvement, Organics enhancement |
| **CONDITION_IMPROVEMENT** | `tasc_atmosphere_manipulation` | Remove atmosphere, Add water, etc. |

### Progress Stalling

**Location:** `src/boggled/terraforming/BoggledBaseTerraformingProject.java:82-106`

Terraforming projects check requirements every day:

```java
CampaignClockAPI clock = Global.getSector().getClock();
if (clock.getDay() != this.lastDayChecked) {
    // Avoid calling requirementsMet() every frame
    boolean requirementsMet = requirementsMet(getProjectRequirements());

    if (requirementsMet) {
        if(!this.requirementsWereMetLastTick)
        {
            this.resumeThisProject();  // Resume if stalled
            this.requirementsWereMetLastTick = true;
        }
        this.daysCompleted++;  // Progress continues
    } else {
        if(this.requirementsWereMetLastTick)
        {
            this.stallThisProject();  // Stall if requirements no longer met
            this.requirementsWereMetLastTick = false;
        }
        this.lastDayChecked = clock.getDay();
    }
}
```

**Behavior:**
- **Requirements met** → Project progresses 1 day per day
- **Requirements not met** → Project **stalls** (no progress, but not cancelled)
- **Requirements re-met** → Project automatically **resumes**

## Ability Unlocking System

### Every Frame Script for Abilities

**Location:** `src/boggled/scripts/BoggledAotDEveryFrameScript.java`

TASC uses an `EveryFrameScript` to dynamically grant abilities when research is completed:

```java
public class BoggledAotDEveryFrameScript implements EveryFrameScript {
    Map<List<String>, List<String>> researchAndAbilityIds;

    public BoggledAotDEveryFrameScript(Map<List<String>, List<String>> researchAndAbilityIds) {
        this.researchAndAbilityIds = researchAndAbilityIds;
    }

    @Override
    public void advance(float amount) {
        AoTDFactionResearchManager manager = AoTDMainResearchManager.getInstance().getManagerForPlayer();
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        CharacterDataAPI characterData = Global.getSector().getCharacterData();

        for (Map.Entry<List<String>, List<String>> entry : researchAndAbilityIds.entrySet()) {
            for (String researchId : entry.getKey()) {
                if (!manager.haveResearched(researchId)) {
                    continue;
                }

                for (String abilityId : entry.getValue()) {
                    if (!fleet.hasAbility(abilityId)) {
                        characterData.addAbility(abilityId);  // Grant ability!
                    }
                }
            }
        }
    }
}
```

**Key Features:**
- **Runs every frame** (`advance(float amount)`)
- **Checks research status** for each mapped research project
- **Grants abilities** when research is completed
- **Persistent checking** - grants ability even if research was completed previously

### Ability-Research Mapping

**Location:** `src/boggled/scripts/BoggledTascPlugin.java:269-280`

```java
private void addAotDEveryFrameScript() {
    if (aotdEnabled) {
        Map<List<String>, List<String>> researchAndAbilityIds = new LinkedHashMap<>();

        researchAndAbilityIds.put(
            Collections.singletonList("tasc_station_restoration"),
            Collections.singletonList("boggled_colonize_abandoned_station")
        );

        researchAndAbilityIds.put(
            Collections.singletonList("tasc_astropolis_construction"),
            Collections.singletonList("boggled_construct_astropolis_station")
        );

        researchAndAbilityIds.put(
            Collections.singletonList("tasc_industrial_stations"),
            asList("boggled_construct_mining_station", "boggled_construct_siphon_station")
        );

        Global.getSector().getPlayerFleet().addScript(new BoggledAotDEveryFrameScript(researchAndAbilityIds));
    }
}
```

### Station Ability Research Requirements

| Ability | Research Required | Unlocks |
|---------|-------------------|---------|
| `boggled_colonize_abandoned_station` | `tasc_station_restoration` | Colonize abandoned stations |
| `boggled_construct_astropolis_station` | `tasc_astropolis_construction` | Build Astropolis stations |
| `boggled_construct_mining_station` | `tasc_industrial_stations` | Build mining stations |
| `boggled_construct_siphon_station` | `tasc_industrial_stations` | Build siphon stations |

### Script Lifecycle

**Registration:** `BoggledTascPlugin.afterGameSave()` and `BoggledTascPlugin.onGameLoad()`
```java
addAotDEveryFrameScript();
```

**Cleanup:** `BoggledTascPlugin.beforeGameSave()`
```java
Global.getSector().getPlayerFleet().removeScriptsOfClass(BoggledAotDEveryFrameScript.class);
```

**Note:** The script is removed before saving and re-added after saving to prevent save file corruption.

## Mod Detection and Initialization

### AoTD Detection

**Location:** `src/boggled/scripts/BoggledTascPlugin.java:52`

```java
static boolean aotdEnabled = Global.getSettings().getModManager().isModEnabled("aotd_vok");
```

This static boolean is checked throughout the code to determine if AoTD integration should be active.

### Conditional Behavior

#### Station Construction (Without AoTD)
```java
public void applyStationConstructionAbilitiesPerSettingsFile() {
    if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.stationConstructionContentEnabled)
       && !aotdEnabled) {  // Only grant abilities if AoTD is NOT enabled
        if (!Global.getSector().getPlayerFleet().hasAbility("boggled_construct_astropolis_station")) {
            Global.getSector().getCharacterData().addAbility("boggled_construct_astropolis_station");
        }
    }
}
```

**Behavior:**
- **AoTD disabled** → Abilities granted automatically based on TASC settings
- **AoTD enabled** → Abilities locked behind research, granted by `BoggledAotDEveryFrameScript`

#### Settings Initialization
```java
public void onApplicationLoad() {
    loadSettingsFromJSON();

    if (aotdEnabled) {
        boggledTools.initialiseModIgnoreSettings();
    }
}
```

When AoTD is detected, TASC initializes special settings to handle the integration properly.

## Research Progression Flow

### Complete Tech Tree Progression

```
Tier 3 AoTD Tech: aotd_tech_streamlined_production
                    ↓
    tasc_domain_excavation (Domain Archaeology)
        ↓
        ├──→ tasc_light_manipulation (Stellar Reflector Array)
        │       ↓
        │   tasc_atmosphere_manipulation (Atmosphere Processor)
        │       ↓
        │   tasc_resource_manipulation (Domed Cities, Ismara's Sling)
        │       ↓
        │   tasc_planet_type_manipulation (Planet Type Changes)
        │
        ├──→ tasc_genetic_manipulation (Genelab, Mesozoic Park)
        │
        ├──→ tasc_limelight_network (Limelight Network)
        │       ↓
        │   tasc_chameleon (CHAMELEON)
        │       ↓
        │   tasc_remnant_algorithms (Kletka Simulator)
        │
        └──→ tasc_station_restoration (Colonize Abandoned Stations)
                ↓
            tasc_astropolis_construction (Astropolis Stations)
                ↓
            tasc_industrial_stations (Mining/Siphon Stations)
                ↓
            └──→ tasc_remnant_station (AI Station) [requires tasc_remnant_algorithms]
```

### Player Experience

1. **Game Start** - No TASC buildings or terraforming available
2. **Research `tasc_domain_excavation`** - Domain Archaeology building becomes available
3. **Research `tasc_light_manipulation`** - Stellar Reflector Array available
4. **Research `tasc_atmosphere_manipulation`** - Atmosphere Processor available, atmosphere modification projects unlocked
5. **Research `tasc_resource_manipulation`** - Domed Cities, Ismara's Sling available, resource improvement projects unlocked
6. **Research `tasc_planet_type_manipulation`** - Planet type change projects unlocked (ultimate goal)

## Technical Details

### AoTD Research Manager API

TASC uses AoTD's public API:

```java
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;

// Get player's research manager
AoTDFactionResearchManager manager = AoTDMainResearchManager.getInstance().getManagerForPlayer();

// Check if research is completed
boolean researched = manager.haveResearched("tasc_planet_type_manipulation");

// Alternative method
boolean researched = AoTDMainResearchManager.getInstance().isResearchedForPlayer("tasc_planet_type_manipulation");
```

### Research Rewards Format

The `rewards` column in `aotd_tech_options.csv` uses special prefixes:

| Prefix | Meaning | Example |
|--------|---------|---------|
| `industry:` | Unlocks industry | `BOGGLED_GENELAB:industry` |
| `modifier:` | Special modifier | `Unlocks planet type change terraforming projects:modifier` |

**Note:** The `industry:` prefix is automatically processed by AoTD to unlock the building.

### Mod Integration Compatibility

The CSV-based integration allows:
- **Multiple mods** to add research to the same tech tree
- **No code changes** needed for most mod compatibility
- **Visual organization** via column and row numbers

## Adding New Research-Locked Content

### Adding a New Research-Locked Industry

**Step 1:** Define research in `aotd_tech_options.csv`
```csv
tasc_my_new_research,My New Tech,3,60,tasc_atmosphere_manipulation,research_databank:5,none,BOGGLED_MY_INDUSTRY:industry,2,1,false,,,Terraforming & Station Construction
```

**Step 2:** Add research check to industry
```java
@Override
public boolean isAvailableToBuild()
{
    if(!boggledTools.isResearched("tasc_my_new_research"))
    {
        return false;
    }

    // Additional checks...
    return true;
}
```

### Adding New Terraforming Project Type

**Step 1:** Define research in `aotd_tech_options.csv` (if needed)

**Step 2:** Add research constant to `boggledTools.java`
```java
public static class BoggledResearchProjects {
    public static final String resourceManipulation = "tasc_resource_manipulation";
    public static final String atmosphereManipulation = "tasc_atmosphere_manipulation";
    public static final String planetTypeManipulation = "tasc_planet_type_manipulation";
    public static final String myNewProjectType = "tasc_my_new_project_type";
}
```

**Step 3:** Update project type switch in `BoggledBaseTerraformingProject.java`
```java
Boolean requirementMet = switch (projectType) {
    case PLANET_TYPE_CHANGE ->
            boggledTools.isResearched(boggledTools.BoggledResearchProjects.planetTypeManipulation);
    case RESOURCE_IMPROVEMENT ->
            boggledTools.isResearched(boggledTools.BoggledResearchProjects.resourceManipulation);
    case CONDITION_IMPROVEMENT ->
            boggledTools.isResearched(boggledTools.BoggledResearchProjects.atmosphereManipulation);
    case MY_NEW_PROJECT_TYPE ->
            boggledTools.isResearched(boggledTools.BoggledResearchProjects.myNewProjectType);
};
```

## Troubleshooting

### Common Issues

**Issue:** Buildings don't appear even after research is completed
- **Cause:** `showWhenUnavailable()` returns false when research not completed
- **Solution:** Ensure building list is refreshed (close and reopen colony screen)

**Issue:** Terraforming projects show as "stalled" after research is completed
- **Cause:** Project checks requirements once per day
- **Solution:** Wait one game day, or advance time via console

**Issue:** Abilities not granted after research completion
- **Cause:** `BoggledAotDEveryFrameScript` not running
- **Solution:** Save and reload the game (script is refreshed on load)

**Issue:** Research not showing in Vaults of Knowledge
- **Cause:** CSV file format error, or mod not loaded
- **Solution:** Check `starsector.log` for CSV parsing errors, verify AoTD is enabled

### Debug Logging

To check if research is being detected:
```java
boggledTools.writeMessageToLog("Research status for tasc_planet_type_manipulation: " +
    boggledTools.isResearched("tasc_planet_type_manipulation"));
```

## Related Code Locations

### Research System Entry Points
- `src/boggled/scripts/BoggledTascPlugin.java:52` - AoTD detection
- `src/boggled/scripts/BoggledTascPlugin.java:269-280` - Ability mapping initialization
- `src/boggled/campaign/econ/boggledTools.java:2390-2403` - Core research check method
- `src/boggled/scripts/BoggledAotDEveryFrameScript.java` - Dynamic ability granting

### Industry Research Checks
- `src/boggled/campaign/econ/industries/Boggled_Stellar_Reflector_Array.java:237`
- `src/boggled/campaign/econ/industries/Boggled_Atmosphere_Processor.java:43`
- `src/boggled/campaign/econ/industries/Boggled_Domed_Cities.java:187`
- `src/boggled/campaign/econ/industries/Boggled_Genelab.java:245`

### Terraforming Project Research Checks
- `src/boggled/terraforming/BoggledBaseTerraformingProject.java:384-441` - Research requirements
- `src/boggled/terraforming/BoggledBaseTerraformingProject.java:82-106` - Progress stalling logic

### Data Files
- `data/campaign/aotd_tech_options.csv` - Research project definitions

## Alternate Tech Tree (Complete Coverage)

TASC includes an alternate tech tree that provides **complete coverage** of all buildings and abilities, located in `data/campaign/custom_tech_trees/aotd_tech_options.csv`.

### Complete vs Default Tech Trees

**Default Tech Tree** (`data/campaign/aotd_tech_options.csv`):
- Covers 11 buildings
- Research progression focused on core terraforming and station features
- Recommended for first-time players

**Alternate Complete Tech Tree** (`data/campaign/custom_tech_trees/aotd_tech_options.csv`):
- Covers all 21 buildings and 4 abilities
- All content requires research
- More challenging progression
- Includes visual icons for all research projects
- See `custom_tech_trees/README.md` for swap instructions

### Using the Alternate Tech Tree

The alternate tech tree is provided as an option for players who prefer a more research-intensive progression. To use it:

1. Backup your default tech tree file
2. Copy the alternate file from `custom_tech_trees/` to `data/campaign/`
3. Replace the existing `aotd_tech_options.csv`

See `data/campaign/custom_tech_trees/README.md` for detailed instructions.

### Tech Tree Coverage

**Buildings in Complete Tree (21 total):**

*Terraforming Path (8 buildings):*
- Domain Archaeology, Stellar Reflector Array, Atmosphere Processor
- Harmonic Damper, Domed Cities, Ismara Sling
- Planetary Agrav Field, Perihelion Project
- Plus: Planet Type Manipulation terraforming projects

*Genetics Path (3 buildings):*
- Genelab, Mesozoic Park, Cloning, Cryosanctum

*Station Path (3 buildings):*
- Remnant Station, Hydroponics
- Plus 4 station construction abilities (Colonize, Astropolis, Mining, Siphon)

*Industry Path (4 buildings):*
- AI Mining Drones, Ouyang Optimizer
- Planet Cracker, Perihelion Project

*Security Path (3 buildings):*
- Limelight Network, CHAMELEON, Kletka Simulator

**Excluded from Tech Trees:**
- GPA (Galatian Particle Accelerator) - Disabled in code, not constructible
- Expand Station - Available without research, controlled by LunaLib settings

**Excluded from Tech Trees:**
- GPA (Galatian Particle Accelerator) - Disabled in code, not constructible

### Complete Tech Tree Structure

The complete tech tree is organized into **5 thematic paths** with logical progression:

#### Path 1: Terraforming & Climate Control (Column 0-6, Rows 0-1)
**Focus**: Environmental manipulation, hazard reduction, planet modification

**Research Progression:**
```
tasc_domain_excavation (Domain Archaeology)
    ↓
tasc_light_manipulation (Stellar Reflector Array)
    ↓
tasc_atmosphere_manipulation (Atmosphere Processor)
    ↓
tasc_geological_stabilization (Harmonic Damper)
    ↓
tasc_resource_manipulation (Domed Cities, Ismara Sling)
    ↓
tasc_planetary_agrav (Planetary Agrav Field)
    ↓
tasc_coronal_tap (Perihelion Project)

tasc_planet_type_manipulation (Planet Type Changes)
```

#### Path 2: Genetics & Biotechnology (Column 1-3, Rows 5-6)
**Focus**: Life sciences, genetic engineering, organ production, creature creation

**Research Progression:**
```
tasc_genetic_manipulation (Genelab, Mesozoic Park)
    ↓
tasc_cloning_technology (Cloning)
    ↓
tasc_cryopreservation (Cryosanctum)
```

#### Path 3: Station Construction & Expansion (Column 1-6, Rows 1-3)
**Focus**: Space stations, orbital infrastructure, station optimization

**Research Progression:**
```
tasc_station_restoration (Colonize Abandoned Station ability)
    ↓
tasc_astropolis_construction (Astropolis Station ability)
    ↓
tasc_hydroponics (Hydroponics)
    ↓
tasc_industrial_stations (Mining/Siphon Station abilities)
    ↓
tasc_automation_systems (AI Mining Drones)
tasc_gas_giant_harvesting (Ouyang Optimizer)
    ↓
tasc_planetary_cracking (Planet Cracker)
```

#### Path 4: Security & AI Systems (Column 1-6, Row 4)
**Focus**: Computing, encryption, AI systems, defense networks

**Research Progression:**
```
tasc_limelight_network (Limelight Network)
    ↓
tasc_chameleon (CHAMELEON)
    ↓
tasc_remnant_algorithms (Kletka Simulator)
    ↓
tasc_remnant_station (Remnant Station - requires both AI and Station tech)
```

### New Research Entries in Complete Tree

The complete tech tree adds **9 new research entries** for buildings not covered in the default tree:

1. **tasc_geological_stabilization** - Geological Stabilization
   - Unlocks: BOGGLED_HARMONIC_DAMPER
   - Prerequisites: tasc_atmosphere_manipulation
   - Cost: Tier 3, 70 days, 6 databank

2. **tasc_planetary_agrav** - Artificial Gravity Control
   - Unlocks: BOGGLED_PLANETARY_AGRAV_FIELD
   - Prerequisites: tasc_resource_manipulation
   - Cost: Tier 3, 100 days, 8 databank

3. **tasc_cloning_technology** - Advanced Cloning
   - Unlocks: BOGGLED_CLONING
   - Prerequisites: tasc_genetic_manipulation
   - Cost: Tier 3, 60 days, 5 databank

4. **tasc_cryopreservation** - Cryogenic Preservation
   - Unlocks: BOGGLED_CRYOSANCTUM
   - Prerequisites: tasc_cloning_technology
   - Cost: Tier 3, 80 days, 7 databank

5. **tasc_hydroponics** - Orbital Agriculture
   - Unlocks: BOGGLED_HYDROPONICS
   - Prerequisites: tasc_astropolis_construction
   - Cost: Tier 3, 70 days, 5 databank

6. **tasc_automation_systems** - Industrial Automation
   - Unlocks: BOGGLED_AI_MINING_DRONES
   - Prerequisites: tasc_industrial_stations
   - Cost: Tier 3, 100 days, 8 databank

7. **tasc_gas_giant_harvesting** - Gas Giant Optimization
   - Unlocks: BOGGLED_OUYANG_OPTIMIZER
   - Prerequisites: tasc_industrial_stations
   - Cost: Tier 3, 110 days, 9 databank

8. **tasc_planetary_cracking** - Planetary Cracking Technology
   - Unlocks: BOGGLED_PLANET_CRACKER
   - Prerequisites: tasc_automation_systems
   - Cost: Tier 3, 150 days, 15 databank (endgame)

9. **tasc_coronal_tap** - Coronal Tap Construction
    - Unlocks: BOGGLED_PERIHELION_PROJECT
    - Prerequisites: tasc_planetary_agrav
    - Cost: Tier 3, 140 days, 12 databank (endgame)

### Icon Assignments

The complete tech tree also adds icon IDs to all existing research entries:

| Research ID | Icon |
|-------------|------|
| tasc_domain_excavation | subicon_domain_archaeology.png |
| tasc_light_manipulation | subicon_stellar_mirror.png |
| tasc_atmosphere_manipulation | subicon_atmosphere_processor.png |
| tasc_geological_stabilization | subicon_harmonic_damper.png |
| tasc_resource_manipulation | subicon_domed_cities.png |
| tasc_planetary_agrav | subicon_planetary_agrav_field.png |
| tasc_coronal_tap | subicon_perihelion_project.png |
| tasc_genetic_manipulation | subicon_genelab_lobster.png |
| tasc_cloning_technology | subicon_cloning.png |
| tasc_cryopreservation | subicon_genelab_lobster.png |
| tasc_limelight_network | subicon_limelight.png |
| tasc_chameleon | subicon_chameleon.png |
| tasc_remnant_algorithms | subicon_kletka_simulator.png |
| tasc_station_restoration | subicon_expand_station.png |
| tasc_astropolis_construction | subicon_expand_station.png |
| tasc_hydroponics | subicon_hydroponics.png |
| tasc_station_expansion | subicon_expand_station.png |
| tasc_industrial_stations | subicon_mining_drones.png |
| tasc_automation_systems | subicon_mining_drones.png |
| tasc_gas_giant_harvesting | subicon_ouyang_optimizer.png |
| tasc_planetary_cracking | subicon_planet_cracker.png |
| tasc_remnant_station | subicon_kletka_simulator.png |

## Summary

TASC's AoTD integration provides a complete research progression system that:

1. **Loads research definitions** via CSV file that AoTD automatically merges into its tech tree
2. **Checks research completion** using AoTD's `AoTDMainResearchManager` API
3. **Locks building construction** behind research via `isAvailableToBuild()` checks
4. **Locks terraforming projects** by project type (planet type, resources, conditions)
5. **Dynamically grants abilities** when research is completed via EveryFrameScript
6. **Provides graceful fallback** when AoTD is not enabled (all features available)

This integration creates an immersive progression system where players must invest in Domain-era technology research to unlock advanced terraforming and station construction capabilities.
