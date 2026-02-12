# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TASC (Terraforming and Station Construction) is a Starsector mod that adds planet terraforming and space station construction features. The mod is built for Starsector version 0.98a-RC7 and requires Java 17.

### Build System

This is a **Java project** for Starsector modding. The project uses:
- **IntelliJ IDEA** as the primary IDE (though any Java IDE with Gradle/Maven support could work)
- **JDK 17.0.12** (specific version required)
- No traditional build system files (gradle/maven) - compilation is handled by IntelliJ's project structure

### Build Dependencies

The mod requires these dependencies (jars must be in the mods folder):
- **LunaLib** (required dependency, defined in mod_info.json)
- **IllustratedEntities** - For image/text handling
- **CrewReplacer** - For crew calculations (optional)
- **SecondInCommand** - For fleet features
- **Ashlib** - For command tab integration
- **AotD - Vaults of Knowledge** (AoTD) - For research tree integration (optional)

### Building and Testing

1. **Rebuild project**: In IntelliJ, select `Build -> Rebuild Project`
2. **Fix library references**: If build errors occur, go to `File -> Project Structure -> Libraries` and ensure references to dependency mod jar files are correct
3. **Test changes**:
   - The mod repo should be cloned to `C:\Program Files (x86)\Fractal Softworks\Starsector\mods\tasc`
   - Make code changes, rebuild the project in IntelliJ
   - Restart Starsector - the new build will be loaded automatically

## Code Architecture

### Core Plugin Structure

**BoggledTascPlugin** (`src/boggled/scripts/BoggledTascPlugin.java`) is the main entry point extending `BaseModPlugin`. It handles:
- Game load/save events (onGameLoad, beforeGameSave, afterGameSave)
- Ability and building registration based on settings
- Station and terraforming feature initialization
- Domain-tech content integration
- Listener registration (terraforming menu, core modification, Ashlib integration)

### Terraforming System

The terraforming system is built around a project-based architecture:

**Base Classes:**
- **BoggledBaseTerraformingProject** - Abstract base for all terraforming projects. Handles:
  - Project lifecycle (start, stall, resume, complete, cancel)
  - Progress tracking (days completed vs required days)
  - Requirement checking and notifications
  - Intel UI integration

**Project Types:**
1. **Planet Type Change** - Changes planet from one type to another (e.g., barren to terran)
2. **Resource Improvement** - Improves resource deposits (farmland, organics, volatiles)
3. **Condition Improvement** - Adds/removes planet conditions

**Key Classes:**
- **Terraforming_Controller** condition - Manages the active terraforming project on a market
- **Planet type classes** (PlanetTypeChangeTerran, PlanetTypeChangeJungle, etc.) - Implement specific planet type conversions
- **Condition modification classes** - Handle atmospheric and environmental changes

**Planet Type Mapping:**
- Located in `data/campaign/terraforming/planet_type_mapping.csv`
- Maps game planet type IDs to TASC planet type categories (barren, desert, frozen, gas_giant, jungle, star, terran, toxic, tundra, volcanic, water)
- This determines which terraforming operations are valid for each planet type

### Station Construction

Station abilities are managed through the `abilities` package:
- **Construct_Astropolis_Station** - Build orbital habitats
- **Construct_Mining_Station** - Build resource extraction stations
- **Construct_Siphon_Station** - Build gas siphoning stations
- **Colonize_Abandoned_Station** - Take control of abandoned stations

Station industries:
- **Boggled_Remnant_Station** - Remnant-type station industry
- **Boggled_Expand_Station** - Expand station size
- **Cramped_Quarters** condition - Applied to all stations for hazard/accessibility modifications

### Industries

Custom industries in `src/boggled/campaign/econ/industries/`:
- **Domain-tech buildings**: Boggled_Domain_Archaeology, Boggled_Genelab, Boggled_Cryosanctum
- **Terraforming infrastructure**: Boggled_Stellar_Reflector_Array, Boggled_Ismara_Sling, Boggled_Atmosphere_Processor, Boggled_Domed_Cities
- **Special projects**: Boggled_Mesozoic_Park, Boggled_Cloning, Boggled_CHAMELEON, etc.

### Utilities and Configuration

**boggledTools** (`src/boggled/campaign/econ/boggledTools.java`) - Central utility class containing:
- Constant definitions (BoggledConditions, BoggledIndustries, BoggledCommodities, BoggledTags, etc.)
- Settings management via LunaLib
- Market and planet helper functions
- Terraforming validation logic
- Water level calculations for terraforming
- **Research system integration** - Data-driven research checking via CSV loading

**Settings System:**
- Customization is now handled **exclusively via LunaLib**
- The `data/config/settings.json` file only contains graphics configuration and rule command packages
- LunaLib settings are loaded at runtime through `boggledTools.getBooleanSetting()` and similar methods

### UI Components

Located in `src/boggled/ui/`:
- **BoggledTerraformingCoreUI** - Main terraforming menu interface
- **BoggledTerraformingCoreUIAshlib** - Ashlib integration variant
- **BoggledCoreModificationListener** - Handles core item modification interactions
- **BoggledCoreModifierEveryFrameScript** - Manages UI state and menu opening

### Data Files Structure

- `data/campaign/terraforming/` - Terraforming configuration (planet type mappings, suppressed conditions)
- `data/campaign/aotd_tech_options.csv` - Default AoTD research tree definitions (11 buildings)
- `data/campaign/custom_tech_trees/aotd_tech_options.csv` - Alternate complete tech tree (21 buildings)
- `data/campaign/industries.csv` - Industry definitions
- `data/campaign/abilities.csv` - Ability definitions
- `data/campaign/market_conditions.csv` - Market condition definitions
- `data/campaign/commodities.csv` - Commodity definitions (e.g., domain_artifacts)
- `data/strings/descriptions.csv` - Text descriptions
- `data/config/` - Graphics and sounds configuration

## Important Integration Points

### AoTD (Vaults of Knowledge) Integration

TASC features a **data-driven research system** that integrates with AoTD's Vaults of Knowledge:

**How It Works:**
- Research definitions are loaded from CSV files (`data/campaign/aotd_tech_options.csv`)
- Buildings and abilities are locked behind research via `rewards` column in CSV
- **No hardcoded research checks** - all buildings use `boggledTools.isBuildingResearchComplete(this.getId())`
- The system gracefully degrades when AoTD is disabled (all features available)

**Key Components:**
- **CSV Loading**: `boggledTools.loadAotdTechOptionsCSV()` - Parses research definitions from CSV
- **Research Checking**: `boggledTools.isBuildingResearchComplete(industryId)` - Unified research check
- **Ability Granting**: `BoggledAotDEveryFrameScript` - Dynamically grants abilities when research completes
- **Research Maps**: `industryResearchMap`, `abilityResearchMap`, `researchNamesMap` - Store CSV data

**Terraforming Project Research:**
- Projects require research based on type:
  - Planet Type Changes: `tasc_planet_type_manipulation`
  - Resource Improvements: `tasc_resource_manipulation`
  - Condition Modifications: `tasc_atmosphere_manipulation`
- Checked via `boggledTools.isResearched(researchId)` in terraforming project requirements

**Tech Trees:**
- **Default Tree** (`data/campaign/aotd_tech_options.csv`): 11 buildings, core progression
- **Alternate Complete Tree** (`data/campaign/custom_tech_trees/aotd_tech_options.csv`): 21 buildings, comprehensive coverage
- See `docs/data-driven-research-system.md` for detailed implementation guide

### Unknown Skies (US) Planet Types

Unknown Skies adds custom planet types with "us_" prefix. These are handled:
- In `planet_type_mapping.csv` - mapped to TASC categories
- In `BoggledBaseTerraformingProject.isUnknownSkiesPlanetType()` - detection method
- Custom planet type change implementations in `src/boggled/terraforming/us/`

### Save/Load Behavior

The plugin carefully manages state:
- **beforeGameSave**: Removes abilities, clears tags, removes scripts
- **afterGameSave**: Re-adds abilities and buildings based on settings
- **onGameLoad**: Re-initializes listeners and scripts

This prevents save file corruption when settings change or mods are enabled/disabled.

## Common Patterns

### Adding a New Terraforming Project

1. Extend `BoggledBaseTerraformingProject` or `BoggledBaseTerraformingPlanetTypeChangeProject`
2. Implement required methods:
   - `getProjectName()` - Display name
   - `getProjectRequirements()` - ArrayList of TerraformingRequirementObject
   - `conditionsToAddUponCompletion()` - Conditions to add
   - `conditionsToRemoveUponCompletion()` - Conditions to remove
3. For planet type changes, extend `BoggledBaseTerraformingPlanetTypeChangeProject` and implement the target planet type logic
4. Register the project in the terraforming menu UI

### Adding a New Research-Locked Building

Using the data-driven research system:

1. **Add CSV entry** to `data/campaign/aotd_tech_options.csv`:
   ```csv
   tasc_my_research,My Research Name,3,60,tasc_prerequisite_research,research_databank:5,none,BOGGLED_MY_BUILDING:industry,2,0,false,,,Terraforming & Station Construction
   ```

2. **Building code** - Use unified research check:
   ```java
   @Override
   public boolean isAvailableToBuild()
   {
       if(!boggledTools.isBuildingResearchComplete(this.getId()))
       {
           return false;
       }
       // Other requirement checks...
       return true;
   }
   ```

3. **No Java code changes needed** for research definitions - all configured via CSV

See `docs/data-driven-research-system.md` for complete guide.

### Checking Terraforming Eligibility

Use the requirement pattern:
```java
public TerraformingRequirementObject getRequirementWorldTypeAllowsTerraforming() {
    String tascPlanetType = boggledTools.getTascPlanetType(market.getPlanetEntity());
    Boolean worldTypeAllowsTerraforming = boggledTools.tascPlanetTypeAllowsTerraforming(tascPlanetType);
    // ... create tooltip
    return new TerraformingRequirementObject("World type allows terraforming", worldTypeAllowsTerraforming, tooltip);
}
```

### Market and Planet Helpers

Key utilities in `boggledTools`:
- `getTascPlanetType(planetEntity)` - Get TASC planet category
- `marketIsStation(market)` - Check if market is a station
- `addCondition(market, conditionId)` - Safely add condition
- `getTerraformingControllerFromMarket(market)` - Get active project
- `getWaterLevelForMarket(market)` - Get water level for terraforming
- `isBuildingResearchComplete(industryId)` - Check if building's research is complete
- `getRequiredResearchForIndustry(industryId)` - Get research ID required for building
- `isResearched(researchId)` - Check if specific research project is complete

## Debugging

- The mod logs messages via `boggledTools.writeMessageToLog(message)`
- Check Starsector's starsector.log for errors
- Common issues: missing dependencies, incorrect library paths in IntelliJ, LunaLib settings conflicts

## File Naming Conventions

- Classes are prefixed with "Boggled_" for industries and conditions
- Scripts use lowercase "boggled" prefix (e.g., `boggledAddTerraformingController`)
- Package structure follows standard Java conventions under `src/boggled/`

## Additional Documentation

- **[Industry Upkeep Costs](docs/industry-upkeep-costs.md)** - Detailed documentation of which TASC industries use fixed vs market-scaled construction costs and upkeep
- **[CSV Data Loading](docs/csv-data-loading.md)** - In-depth guide to how TASC loads configuration data from CSV files (planet mappings, suppressed conditions, etc.)
- **[AoTD Research Integration](docs/aotd-research-integration.md)** - Complete guide to TASC's integration with Ashes of the Domain Vaults of Knowledge research system, including tech tree structure, research-locked buildings/projects, and ability unlocking
- **[Data-Driven Research System](docs/data-driven-research-system.md)** - Technical documentation of TASC's CSV-based research system, including how to add custom research, the parsing system, and building/ability integration
- **[Ability Research Implementation](docs/ability-research-implementation.md)** - Guide to implementing research-locked abilities using the data-driven system
