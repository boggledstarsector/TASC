# AGENTS.md

This file provides guidance to AI coding tools when working with code in this repository.

## Project Overview

TASC (Terraforming and Station Construction) is a Starsector mod that adds planet terraforming and space station construction features. The mod is built for Starsector version 0.98a-RC7 and requires Java 17.

### Build System

This is a **Java project** for Starsector modding. The project uses:
- **IntelliJ IDEA** as the primary IDE (though any Java IDE with Gradle/Maven support could work)
- **JDK 17.0.12** (specific version required)
- No traditional build system files (gradle/maven) - compilation is handled by IntelliJ's project structure

## Player-facing documentation

Player-facing documentation for TASC's content and systems lives in [tascdocs](https://github.com/boggledstarsector/tascdocs), a separate website repository. The guides under `docs/` in this repository are for developers working on the mod. They do not replace the site.

Whenever any change is made in this repository, open a companion pull request against `boggledstarsector/tascdocs` that updates existing pages or adds new ones for that change. A TASC change is not finished while that companion pull request is missing. A translation that only changes display text does not need a tascdocs pull request.

Site pages are organized by topic under `app/docs/` and `app/components/` (buildings, station abilities, and terraforming projects). Follow `docs/creating-new-building-pages.md` in tascdocs when adding a building page.

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
- Setting ids, types, defaults, min, max, and tab live in `data/config/boggled/luna_settings_structure.csv`. Names and tooltips live in `strings.json` under `tasc_luna`. There is no `data/config/LunaSettings.csv`, because LunaLib would display that file's prose columns.

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
- `data/strings/strings.json` - Player-facing sentences, keyed by category
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

## Translating TASC

This section is for an agent producing a translation of TASC into any language. Edit display text only. Leave Java alone. A translation does not include a tascdocs change. The tascdocs rule above still applies to code and content changes.

Save every edited file as UTF-8. Do not let an editor rewrite a CSV into another encoding. Do not add or remove rows. Do not reorder columns. A `strings.json` that is not valid UTF-8 fails mod load with the parser's error.

`$token` and `$Token` stay spelled exactly as in the English string, including the capital letter when it is capitalized. Tokens contain no spaces. Word order may change around the tokens. `$playerName` in `rules.csv` stays as written. Substitution is a literal replace, so a `%` or an extra parenthesis in a translation is shown as text.

A missing key shows `[INVALID]` plus the id, and the log names the category and id the first time that key is used. Fill the key. Do not delete the key.

Highlighted values are the tokens. Do not add a second copy of a word for the highlighter to search for.

Singular and plural are separate keys. Translate both. Do not merge them. The same applies to pairs such as can / cannot.

The terraforming tab caption comes from `tasc_ui` key `terraforming_tab`. It must not contain these substrings, because Ashlib matches button text with `contains`: `income`, `colonies`, `doctrine & blueprints`, `custom production`, `research & production`.

`TascAshlibCommandTabListener.getButtonToBePlacedNear()` returns `"custom production"`. That is an Ashlib map key in Java. It is not a translatable string. With Ashlib loaded, a translated vanilla command row never gets past Ashlib's search for `"income"`, so TASC's tab is not created. That limit is in Ashlib's `CommandTabTracker`, not in a TASC string. Do not try to fix it by translating the anchor.

`tasc_abilities` key `vanilla_abandoned_siphon_station` is a search string. Colonize Abandoned Station looks for that text inside the station's full name, then gives a matching gas-giant station the siphon volatiles, a Mining industry, and the `siphon_entity_name` rename. Set the value to the name the base game shows for an abandoned siphon station. The shipped value `Abandoned Siphon Station` is the English fallback when a translation leaves the key unchanged. Keep the value non-empty, because an empty value matches no station.

Planet-type words the player sees are the `tasc_terraforming` keys `planet_type_<id>` (`planet_type_terran`, `planet_type_gas_giant`, and the rest). The ids in `planet_type_mapping.csv` and in code (`terran`, `barren`, `gas_giant`, and the rest) stay English.

Building names are the `name` column of `industries.csv` and the matching display strings. Translate the column. Leave the `id` column.

Files to edit, and the columns or keys that are display text:

- `data/strings/strings.json` — every value. Keys stay. Categories: `tasc_general`, `tasc_ui`, `tasc_abilities`, `tasc_terraforming`, `tasc_industries`, `tasc_intel`, `tasc_luna` (`name_<fieldID>`, `tooltip_<fieldID>`).
- `data/strings/descriptions.csv` — `text1`, `text2`, `text3`. Leave `id` and `type`.
- `data/strings/tips.json` — the tip strings. Leave the keys.
- `data/campaign/industries.csv` — `name`, `desc`. Leave `id`, tags, plugin, and the numeric columns.
- `data/campaign/abilities.csv` — `name`, `desc`. Leave `id` and `plugin`.
- `data/campaign/market_conditions.csv` — `name`, `desc`. Leave `id` and `script`.
- `data/campaign/commodities.csv` — `name`, `desc`. Leave `id`.
- `data/campaign/rules.csv` — the `text` column, and the label after the second colon in `options` (`40:SL_buildDomainRelay:Construct a Domain-tech comm relay` — only the part after the second colon). Leave `id`, `trigger`, `conditions`, `script`, and the option id (`SL_buildDomainRelay`).
- `data/campaign/aotd_tech_options.csv` and `data/campaign/custom_tech_trees/aotd_tech_options.csv` — the `name` column. Leave `id`, `rewards`, and the other columns. Both copies need the same names where the ids match.
- `mod_info.json` — `name` and `description` only. Leave `id` (`Terraforming & Station Construction`), `modPlugin`, dependency ids, and version fields.

Files and columns an agent must not edit for a translation:

- Any file under `src/`.
- `data/config/boggled/luna_settings_structure.csv` (field ids, types, defaults, min, max, tab). LunaLib auto-loads `data/config/LunaSettings.csv`. That path is not in this mod, so a translation must not create it.
- `data/config/settings.json`.
- `data/campaign/terraforming/planet_type_mapping.csv`, `domed_cities_suppressed_conditions.csv`, and `stellar_reflector_array_suppressed_conditions.csv`. Both sides of those tables are ids.
- Graphics paths, sound ids, plugin class names, and commodity or condition ids inside sentences. If a sentence names a commodity, the Java token already inserts that commodity's display name. Do not replace an id that the script compares.

Load a campaign, open the colony command row, open one station ability tooltip, open one terraforming requirement, and open LunaLib. Any `[INVALID]` line is a missed key. A crash on the command row with Ashlib and a translated base game is Ashlib's `"income"` lookup, not a bad TASC string.
