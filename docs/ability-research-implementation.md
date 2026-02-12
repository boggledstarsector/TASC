# Ability Research Implementation Summary

## Overview

The data-driven research system has been extended to TASC abilities with **two layers of protection** for robust research gating.

## Two-Layer Protection System

### Layer 1: Primary Gating (Ability Addition)

**Location:** `BoggledAotDEveryFrameScript.java`

The script runs every frame and checks research status:
```java
if (manager.haveResearched(researchId)) {
    if (!fleet.hasAbility(abilityId)) {
        characterData.addAbility(abilityId);  // Literally adds to player's ability bar
    }
}
```

**What this does:**
- Abilities are **NOT in the player's ability bar** until research is completed
- Once research is done, abilities are automatically added
- This is the primary and most visible layer of protection

### Layer 2: Secondary Validation (Usage Prevention)

**Location:** Each ability file's `isUsable()` method

Each ability checks research before allowing activation:
```java
// Check if required research is completed (data-driven system)
String requiredResearch = boggledTools.getRequiredResearchForAbility("boggled_construct_astropolis_station");
if (requiredResearch != null && !boggledTools.isResearched(requiredResearch))
{
    return false;  // Ability appears grayed out/unusable
}
```

**What this does:**
- Even if an ability is somehow added without research, it can't be used
- Provides defense-in-depth against cheating, bugs, or edge cases
- Shows a tooltip message: "Requires the [Research Name] research to be completed."

## Why Two Layers?

### Primary Gating Alone (Insufficient)
- ❌ Doesn't protect against console commands adding abilities
- ❌ Doesn't protect against save editing
- ❌ Doesn't protect against bugs in the EveryFrameScript
- ❌ No feedback if someone tries to use an ability without research

### Two-Layer Approach (Robust)
- ✅ Primary: Abilities don't appear until research is done
- ✅ Secondary: Even if added, abilities are unusable without research
- ✅ Consistent with building pattern (buildings have similar checks)
- ✅ Clear user feedback via tooltips
- ✅ Defense-in-depth against edge cases

## Files Modified

### Core System (3 files)
1. **boggledTools.java** - Added ability research map and parsing
2. **BoggledAotDEveryFrameScript.java** - Primary gating (adds abilities)
3. **BoggledTascPlugin.java** - Simplified initialization

### CSV Data (1 file)
4. **aotd_tech_options.csv** - Added ability IDs to rewards column

### Ability Files (4 files) - Secondary Validation
5. **Construct_Astropolis_Station.java**
   - Added research check in `isUsable()`
   - Added tooltip message for research requirement
   - Gated by: `tasc_astropolis_construction`

6. **Construct_Mining_Station.java**
   - Added research check in `isUsable()`
   - Added tooltip message for research requirement
   - Gated by: `tasc_industrial_stations`

7. **Construct_Siphon_Station.java**
   - Added research check in `isUsable()`
   - Added tooltip message for research requirement
   - Gated by: `tasc_industrial_stations`

8. **Colonize_Abandoned_Station.java**
   - Added research check in `isUsable()`
   - Added tooltip message for research requirement
   - Gated by: `tasc_station_restoration`

## Testing Checklist

### Primary Gating (Ability Addition)
- [ ] New game with AoTD - abilities NOT in ability bar initially
- [ ] Complete `tasc_station_restoration` - "Colonize Abandoned Station" appears
- [ ] Complete `tasc_astropolis_construction` - "Construct Astropolis Station" appears
- [ ] Complete `tasc_industrial_stations` - Both Mining and Siphon abilities appear
- [ ] Save and reload - abilities persist

### Secondary Validation (Usage Prevention)
- [ ] Try to use ability without research - grayed out/unusable
- [ ] Hover over ability - shows "Requires [Research Name] research" message
- [ ] Console-add ability without research - still unusable
- [ ] Complete research - ability becomes usable immediately

### Edge Cases
- [ ] AoTD disabled - abilities available (no research checks)
- [ ] CSV deleted - abilities available (graceful degradation)
- [ ] Corrupted CSV - error logged, other abilities work
- [ ] Existing save with completed research - abilities auto-added

## CSV Format

### Single Ability
```csv
tasc_astropolis_construction,Orbital Colonies,3,60,tasc_station_restoration,research_databank:5,none,"Allow building Astropolis type stations:modifier
boggled_construct_astropolis_station:ability",2,2,false,,,Terraforming & Station Construction
```

### Multiple Abilities from One Research
```csv
tasc_industrial_stations,Orbital Industrial Complex,3,90,tasc_astropolis_construction,research_databank:7,none,"Allow building Mining and Siphon type stations:modifier
boggled_construct_mining_station:ability
boggled_construct_siphon_station:ability",3,2,false,,,Terraforming & Station Construction
```

## Code Patterns

### Checking Research in Ability
```java
@Override
public boolean isUsable()
{
    // Check if required research is completed (data-driven system)
    String requiredResearch = boggledTools.getRequiredResearchForAbility("boggled_construct_astropolis_station");
    if (requiredResearch != null && !boggledTools.isResearched(requiredResearch))
    {
        return false;
    }

    // ... rest of validation logic
}
```

### Showing Research Requirement in Tooltip
```java
@Override
public void createTooltip(TooltipMakerAPI tooltip, boolean expanded)
{
    Color highlight = Misc.getHighlightColor();
    Color bad = Misc.getNegativeHighlightColor();

    tooltip.addTitle("Ability Name");
    float pad = 10.0F;
    tooltip.addPara("Ability description...", pad, highlight, new String[]{...});

    // Check research requirement for tooltip
    String requiredResearch = boggledTools.getRequiredResearchForAbility("boggled_construct_astropolis_station");
    if (requiredResearch != null && !boggledTools.isResearched(requiredResearch))
    {
        String researchName = boggledTools.getResearchDisplayName(requiredResearch);
        tooltip.addPara("Requires the " + researchName + " research to be completed.", bad, pad);
    }

    // ... rest of tooltip
}
```

## Benefits

1. **Consistency** - Abilities use the same research system as buildings
2. **Robustness** - Two layers of protection against edge cases
3. **User-Friendly** - Clear feedback when research is required
4. **Modder-Friendly** - Add new ability unlocks via CSV only
5. **Backward Compatible** - Works with existing saves
6. **Graceful Degradation** - If CSV parsing fails, abilities remain available

## Comparison: Buildings vs Abilities

| Aspect | Buildings | Abilities |
|--------|-----------|-----------|
| **Primary Gating** | Hidden in build menu (not implemented) | Not in ability bar |
| **Secondary Validation** | `isAvailableToBuild()` check | `isUsable()` check |
| **User Feedback** | Tooltip when unavailable | Tooltip when unusable |
| **Data Source** | `aotd_tech_options.csv` | `aotd_tech_options.csv` |
| **Unlock Mechanism** | N/A (always visible but locked) | Auto-added to ability bar |

## Notes

- Abilities use lowercase IDs (e.g., `boggled_construct_astropolis_station`)
- Research is case-sensitive when checked with `isResearched()`
- The `:ability` suffix in CSV distinguishes from `:industry`
- Multiple abilities can be unlocked by one research project
- Once unlocked, abilities persist even if CSV is modified
- The system is opt-in: abilities without CSV entries are unaffected
