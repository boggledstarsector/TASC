# TASC Complete Tech Tree (Alternate)

## What This Is

This folder contains an **alternate research tech tree** for TASC that includes **ALL 21 buildings and 4 abilities** behind research projects, compared to the default tech tree which only includes the 11 default buildings.

If you enabled all TASC buildings, you can use this tech tree to gate those buildings behind research projects. You can also use it as a template for creating your own tech tree depending on which TASC features you enabled or disabled via LunaLib.

## How to Use the Complete Tech Tree

### Step 1: Backup the Default File

Before making changes, backup the default tech tree file:
1. Navigate to: `Starsector/mods/tasc/data/campaign/`
2. Copy `aotd_tech_options.csv`
3. Paste it in the same folder as `aotd_tech_options.csv.backup`

### Step 2: Swap the Files

1. Navigate to: `Starsector/mods/tasc/data/campaign/custom_tech_trees/`
2. Copy `aotd_tech_options.csv` from this folder
3. Navigate back to: `Starsector/mods/tasc/data/campaign/`
4. **REPLACE** the existing `aotd_tech_options.csv` with the one you copied

### Step 3: Start Playing

- **New Game**: All buildings will now require research to build
- **Existing Save**: Research requirements will update immediately (buildings already under construction remain unaffected)

## How to Revert to Default Tech Tree

If you want to switch back to the default tech tree:

1. Navigate to: `Starsector/mods/tasc/data/campaign/`
2. Delete `aotd_tech_options.csv`
3. Rename `aotd_tech_options.csv.backup` to `aotd_tech_options.csv`

## Save Compatibility

- **Switching to complete tree**: Safe for existing saves. Buildings that were available before may now require research.
- **Switching back to default**: Safe for existing saves. Research requirements will revert to default.

## Notes

- GPA (Galatian Particle Accelerator) remains disabled in both tech trees
- Expand Station is available without research and is controlled by LunaLib settings

## Tech Tree Structure

The complete tech tree is organized into 5 paths:

### 1. Terraforming & Climate Control (Column 0-1, Rows 0-1)
**Focus**: Environmental manipulation, hazard reduction, planet modification

**Buildings:**
- Domain Archaeology (entry point)
- Stellar Reflector Array (climate manipulation)
- Atmosphere Processor (atmosphere modification)
- Harmonic Damper (tectonic stabilization)
- Domed Cities (habitat creation)
- Ismara Sling (water creation)
- Planetary Agrav Field (gravity control)
- Perihelion Project (solar megastructure)
- Planet Type Manipulation (ultimate terraforming)

**Progression:** Basic climate → Atmosphere → Geology → Resources → Gravity → Megastructures → Planet Types

### 2. Genetics & Biotechnology (Column 1-3, Rows 5-6)
**Focus**: Life sciences, genetic engineering, organ production, creature creation

**Buildings:**
- Genelab + Mesozoic Park (entry: Genetic Manipulation)
- Cloning (Advanced Cloning)
- Cryosanctum (Cryogenic Preservation)

**Progression:** Basic genetics → Cloning → Advanced storage

### 3. Station Construction & Expansion (Column 1-5, Rows 1-3)
**Focus**: Space stations, orbital infrastructure, station optimization

**Abilities:**
- Colonize Abandoned Station (restoration)
- Construct Astropolis Station (orbital habitats)
- Construct Mining Station (resource extraction)
- Construct Siphon Station (gas giant mining)

**Buildings:**
- Hydroponics
- Remnant Station (requires both AI and Station tech)

**Progression:** Repair → Habitats → Agriculture → Industrial Stations → Specialized Stations

### 4. Industrial Automation & Optimization (Column 5-6, Rows 2-3)
**Focus**: Industry optimization, automation, resource extraction, megastructure construction

**Buildings:**
- AI Mining Drones (Industrial Automation)
- Ouyang Optimizer (Gas Giant Optimization)
- Planet Cracker (extreme extraction)

**Progression:** Automation → Gas Giant Optimization → Extreme Extraction

### 5. Security & AI Systems (Column 1-4, Row 4)
**Focus**: Computing, encryption, AI systems, defense networks

**Buildings:**
- Limelight Network (VR infrastructure)
- CHAMELEON (encryption/security)
- Kletka Simulator (AI simulation)
- Remnant Station (AI nexus - requires both AI and Station tech)

**Progression:** VR → Encryption → AI Simulation → AI Station
