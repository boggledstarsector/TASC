# TASC Industries Market Size Scaling Analysis

This document details whether each TASC industry's construction cost and upkeep scale with the market size (colony size) or use fixed values.

## Summary

- **9 industries** use fixed construction cost and upkeep (do NOT scale with market size)
- **9 industries** scale with market size (use vanilla default behavior)
- **3 industries** have special/custom scaling behavior

---

## Industries With Fixed Cost and Upkeep (No Market Size Scaling)

These industries override the vanilla behavior and set fixed construction cost and upkeep values that do NOT change based on market size.

| Industry | Fixed Size Value | Code Reference |
|----------|------------------|----------------|
| **Boggled_Ismara's Sling** | Size 3 | `applyIncomeAndUpkeep(3)` |
| **Boggled_Planet_Cracker** | Size 3 | `applyIncomeAndUpkeep(3)` |
| **Boggled_Ouyang_Optimizer** | Size 3 | `applyIncomeAndUpkeep(3)` |
| **Boggled_GPA** | Size 3 | `applyIncomeAndUpkeep(3)` |
| **Boggled_Atmosphere_Processor** | Size 3 | `applyIncomeAndUpkeep(3)` |
| **Boggled_Perihelion_Project** | Size 3 | `applyIncomeAndUpkeep(3)` |
| **Boggled_Cryosanctum** | Size 6 | `applyIncomeAndUpkeep((float)size)` where `int size = 6;` |
| **Boggled_Remnant_Station** | Size 7 | `applyIncomeAndUpkeep(7)` |
| **Boggled_Kletka_Simulator** | Size 3 | `applyIncomeAndUpkeep(3)` with temperature multipliers |

### Design Notes for Fixed-Size Industries

**Boggled_Cryosanctum**: Hardcoded to size 6 regardless of actual market size, likely to match vanilla Cryosanctum balance.

**Terraforming Infrastructure** (Ismara's Sling, Perihelion Project, Planet Cracker, Ouyang Optimizer, GPA, Atmosphere Processor): All set to size 3, representing the terraforming project size rather than the colony size.

**Boggled_Kletka_Simulator**: Set to size 3, with additional temperature-based upkeep modifiers:
- Stations: 8x upkeep multiplier
- Very Cold: 0.25x upkeep multiplier
- Cold: 0.5x upkeep multiplier
- Hot: 2x upkeep multiplier
- Very Hot: 4x upkeep multiplier

**Boggled_Remnant_Station**: Set to size 7 to represent a full star fortress equivalent, with special demands (7 supplies, 4 domain artifacts if enabled).

---

## Industries That Scale With Market Size

These industries use the vanilla default `getBuildCost()` and `getUpkeep()` methods, which automatically scale construction cost and upkeep based on the market size.

| Industry | Notes |
|----------|-------|
| **Boggled_Hydroponics** | Standard scaling |
| **Boggled_Genelab** | Standard scaling |
| **Boggled_Mesozoic_Park** | Standard scaling |
| **Boggled_Limelight_Network** | Standard scaling |
| **Boggled_Harmonic_Damper** | Standard scaling |
| **Boggled_Stellar_Reflector_Array** | Standard scaling |
| **Boggled_Domed_Cities** | Standard scaling with multipliers: Sky Cities mode has 3x build cost and 6x upkeep multiplier applied on top of base |
| **Boggled_CHAMELEON** | Standard scaling |
| **Boggled_Domain_Archaeology** | Standard scaling |

### Special Scaling Behavior

**Boggled_Domed_Cities** has different modes with different cost multipliers:
- **Domed Cities/Seafloor Cities**: Standard scaling with market size
- **Sky Cities**: Standard scaling with market size, THEN applies:
  - 3x build cost multiplier (`SKY_CITIES_BUILD_COST_MULTIPLIER`)
  - 6x upkeep multiplier (`SKY_CITIES_UPKEEP_MULTIPLIER`)

---

## Special Cases (Custom Scaling Behavior)

### Boggled_Expand_Station

This industry has **unique cost scaling** that does NOT depend on market size:

- **Construction Cost**: Scales exponentially based on number of previous expansions
  - Formula: `base_cost * (2 ^ number_of_expansions)`
  - Configured by setting: `boggledStationProgressiveIncreaseInCostsToExpandStation`
  - If disabled: Uses vanilla default (scales with market size)
- **Upkeep**: Uses vanilla default (scales with market size)

This makes each subsequent expansion significantly more expensive, regardless of the station's market size.

### Boggled_Planetary_Agrav_Field

This industry uses custom scaling behavior for both construction cost and upkeep.

### Boggled_AI_Mining_Drones

This industry uses custom scaling behavior and is restricted to stations only.

---

## Methodology

This analysis was performed by examining the source code of each industry class in `src/boggled/campaign/econ/industries/` to identify:

1. Whether the industry overrides `getBuildCost()` - determines if construction cost scales
2. Whether the industry overrides `getBaseUpkeep()` or calls `applyIncomeAndUpkeep(size)` - determines if upkeep scales
3. The specific size value used (variable vs hardcoded)

### Vanilla Default Behavior

When an industry does NOT override these methods, Starsector's default behavior is:
- Construction cost scales with market size
- Upkeep scales with market size

### TASC Overrides

TASC industries that override this behavior use:
- `applyIncomeAndUpkeep(int size)` to set a fixed size for income/upkeep calculation
- Custom `getBuildCost()` to implement special cost formulas
- Custom `getBaseUpkeep()` to apply multipliers

---

## Generated

Generated on 2026-02-08 by analyzing TASC mod version 10.0.3
Updated on 2026-02-08 to fix categorization errors
