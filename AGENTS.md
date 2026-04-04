# CLAUDE.md

This file provides guidance to AI coding tools when working with code in this repository.

## Project Overview

TASC (Terraforming and Station Construction) is a Starsector mod that adds planet terraforming and space station construction features. The mod is built for Starsector version 0.98a-RC7 and requires Java 17.

### Build System

This is a **Java project** for Starsector modding. The project uses:
- **IntelliJ IDEA** as the primary IDE (though any Java IDE with Gradle/Maven support could work)
- **JDK 17.0.12** (specific version required)
- No traditional build system files (gradle/maven) - compilation is handled by IntelliJ's project structure

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

## Additional Documentation

- **[Changelog](CHANGELOG.md)** - Version history and release notes for TASC
- **[Industry Upkeep Costs](docs/industry-upkeep-costs.md)** - Detailed documentation of which TASC industries use fixed vs market-scaled construction costs and upkeep
- **[CSV Data Loading](docs/csv-data-loading.md)** - In-depth guide to how TASC loads configuration data from CSV files (planet mappings, suppressed conditions, etc.)
- **[AoTD Research Integration](docs/aotd-research-integration.md)** - Complete guide to TASC's integration with Ashes of the Domain Vaults of Knowledge research system, including tech tree structure, research-locked buildings/projects, and ability unlocking
- **[Data-Driven Research System](docs/data-driven-research-system.md)** - Technical documentation of TASC's CSV-based research system, including how to add custom research, the parsing system, and building/ability integration
- **[Ability Research Implementation](docs/ability-research-implementation.md)** - Guide to implementing research-locked abilities using the data-driven system
- **[Vanilla Game Lore and Data](docs/lore/)** - Extracted vanilla Starsector game data for reference and consistency

## Vanilla Game Reference Data

The `/docs/lore/` folder contains extracted vanilla Starsector game data for reference when writing descriptions, tooltips, and lore for TASC content:

- **[abilities.csv](docs/lore/abilities.csv)** - Vanilla ship and fleet ability definitions, including active abilities and system stats
- **[descriptions.csv](docs/lore/descriptions.csv)** - Full game descriptions for industries, items, commodities, and other entities
- **[game_mechanics.txt](docs/lore/game_mechanics.txt)** - Core game mechanics documentation and formulas
- **[industries.csv](docs/lore/industries.csv)** - Vanilla industry definitions with requirements, outputs, and modifiers
- **[market_conditions.csv](docs/lore/market_conditions.csv)** - All market/hazard condition definitions and their effects
- **[planets.json](docs/lore/planets.json)** - Planet type definitions and properties
- **[rules.csv](docs/lore/rules.csv)** - Rule command definitions for dialog scripts and triggered actions
- **[ship_names.json](docs/lore/ship_names.json)** - Vanilla ship naming conventions and faction-specific name patterns
- **[spacers_manual_combat.txt](docs/lore/spacers_manual_combat.txt)** - Combat mechanics from the in-game Spacers' Manual
- **[spacers_manual_other.txt](docs/lore/spacers_manual_other.txt)** - General gameplay guides from the Spacers' Manual
- **[spacers_manual_ui.txt](docs/lore/spacers_manual_ui.txt)** - Interface and controls documentation from the Spacers' Manual
- **[special_items.csv](docs/lore/special_items.csv)** - Special item definitions (industry-boosting artifacts, AI cores, etc.)
- **[strings.json](docs/lore/strings.json)** - General string constants and localization data
- **[tips.json](docs/lore/tips.json)** - Loading screen tips and gameplay hints
- **[tooltips.json](docs/lore/tooltips.json)** - Tooltip text definitions for UI elements

**Usage Guidelines:**
- Use these files to match vanilla game terminology, tone, and formatting
- Reference vanilla descriptions when writing new TASC content for consistency
- Understand existing game mechanics before implementing new features
- Maintain style consistency with vanilla tooltips and ability descriptions
