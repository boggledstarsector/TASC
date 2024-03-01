First up is the context object `RequirementContext` that is passed into a lot of things. Aside from the `contextName`, everything may be null. In general, grab what you need from the context and then check them all for null. Handle those cases appropriately.

## Terraforming projects
The `id` field is a file unique identifier. Other mods can override a project entirely by providing their own `terraforming_projects.csv` with an entry with the same `id` field. This is not the recommended way of modifying a project. Check `terraforming_project_mods.csv` and the `Terraforming Projects Mods` section.

`enable_settings` is a `|` separated list of mod specific setting IDs (`LunaLib`'s `LunaSettings.csv` or vanilla's `settings.json`). If the setting is `false`, then this project will not be available.

`project_type` is where the project should be displayed. `terraforming` projects are shown at the top of the terraforming UI, with `crafting` projects underneath them. `ability` and `industry` projects are used for abilities and industries respectively, and so aren't shown in the terraforming UI.

`tooltip` is the tooltip shown for the project. It's displayed on buttons in the terraforming UI and on the intel message when the project finishes.

`intel_complete_message` is the message shown when the project completes successfully.

`requirements` is the requirements for the project to be started. It's a JSON Array of JSON Objects with two fields, `requirement_id` is the ID of the requirement from `terraforming_requirements_OR.csv`, `and_then` is a JSON Array of JSON Objects of requirements to be checked only if this requirement is satisfied. This means you can have requirements that are only checked if previous requirements are met. One example is only checking the nearest planet's owner if the fleet is not in hyperspace.

`requirements_hidden` is the requirements for the project to be visible. Same format as `requirements`.

`base_project_duration` is the project's duration in days.

`dynamic_project_duration_modifiers` is a JSON Array of strings, with each string being the ID for a duration modifier from `duration_modifiers.csv`.

`requirements_stall` and `requirements_reset` are JSON Arrays of JSON Objects. `requirements_id` is the identifier for this requirement for modding purposes. `requirements` is the same format as the previous `requirements`. If any of the requirements are satisfied, then the project is stalled or reset respectively.

`project_complete_effects` is a JSON Array of strings, with each string being the ID of an effect from `project_effects.csv`. All the effects happen when the project is completed successfully.

`project_ongoing_effects` is the same format as `project_complete_effects`. All the effects happen while the project is ongoing. This is primarily used for industry projects.

`incomplete_message` is the text displayed when the project is not yet complete. This is displayed in a `project_type` dependent manner. `industry` projects display it below the industry description.

`incomplete_message_highlights` is a JSON Array of strings of the text to highlight.

`disrupted_message` and `disrupted_message_highlights` are the same as `incomplete_message` and `incomplete_message_highlights` but when the source industry is disrupted, e.g. the message when an atmosphere processor is disrupted.

## Industry options
The `id` field is a file unique identifier. Other mods can override an industry entirely by providing their own `industry_options.csv.csv` with an entry with the same `id` field. This is not the recommended way of modifying an industry. Check `industry_options_mods.csv` and the `Industry Options Mods` section.

`tooltip` is the text displayed in the effect source text, and the build or upgrade text.

`projects` is a `|` separated collection of projects the industry will use. Each entry should be one of the `id` fields from `terraforming_projects.csv`.

`building_finished_effects` is a JSON Array of strings, with each string being the ID of an effect from `project_effects.csv`. The effect is applied when the building is finished.

`improve_effects` is the same format as `building_finished_effects`. The effect is applied when the building is improved.

`ai_core_effects` is a JSON Array of JSON Objects with two fields, `ai_core_id` is the AI core ID that will apply these effects, and `projects` is a JSON Array of `Terraforming Project` IDs.

`base_pather_interest` is an integer of the Pather interest this industry generates.

`image_overrides` is a JSON Array of JSON Objects with 4 fields. `id` is a unique identifier for this image override used for modifying this entry. `requirements` is a list of requirements of the same format as `requirements` from section `Terraforming Projects`. `category` and `image_id` are the category and image ID entries from `settings.json`.

`pre_build_effects` is the same format as `building_finished_effects`. The effect is applied before the building is constructed.

## Industry option mods and terraforming project mods
`id` is a file unique identifier for this modifier.

`industry_id` and `project_id` are the `id` fields from `industry_options.csv` and `terraforming_projects.csv` that this modification will be applied to.

The rest of the fields are additions and removals from the corresponding field without `_added` and `_removed` from `industry_options.csv` and `terraforming_projects.csv`. Entries are added before being removed.

`requirements_added` is the same format as `requirements` in `terraforming_projects.csv` with an additional optional `parent_id` field in each entry. If `parent_id` is specified, the requirements will be added to the `and_then` collection of the specified ID.

`requirements_removed` is a JSON Array of requirement IDs to remove.

`requirements_stall_added` is a JSON Array of JSON Objects. `containing_id` is the `requirements_id` field of the requirement you want to add these requirements to. `requirements_added` is the same format as `requirements_added` above.

`requirements_stall_removed` is a JSON Array of JSON Objects. `containing_id` has the same meaning as with `requirements_stall_added` above. `requirements_removed` is the same format as `requirements_removed` above.

`requirements_reset_added` and `requirements_reset_removed` are the same formats as `requirements_stall_added` and `reuqirements_stall_removed`.

## Individual requirements
All requirements in `terraforming_requirement.csv` have 4 required fields. The `id` field is what is used to uniquely identify this requirement. Mods can overwrite a requirement by providing their own `terraforming_requirement.csv` file with an entry with the same `id` field.

`enable_settings` contains a `|` separated collection of setting IDs as specified in `LunaSettings.csv`. The setting must be a `Boolean` setting. If any of the settings are set to `false`, then the requirement will be skipped. If all of a requirement collections' requirements are disabled, the requirement is considered satisfied.

`requirement_type` contains either one of the entries in `Base Requirement Types` below, or a mod specified addition.

`invert` contains `true` or `false`. If `true`, the result of the check is inverted and a requirement check that returns true instead returns false, and vice versa.

`data` contains `requirement_type` specific data. Check `Base Requirement Types` for more info.

## Dynamic project duration modifiers
These modify the project duration based on some code. The `id` field is used to uniquely identify this modifier. Mods can overwrite a modifier by providing their own `duration_modifiers.csv` file with an entry with the same `id` field.

`enable_settings` contains a `|` separated collection of setting IDs as specified in `LunaSettings.csv`. The setting must be a `Boolean` setting. If any of the settings are set to `false`, then the modifier will be skipped.

`duration_modifier_type` contains either one of the entries in `Base Duration Modifier Types` below, or a mod specified addition.

`data` contains `duration_modifier_type` specific data. Check `Base Duration Modifier Types` for more info.

## Project effects
Same basic idea as requirements, specified in `project_effects.csv`. The `id` field is used to uniquely identify this effect. Mods can overwrite an effect by providing their own `project_effects.csv` file with an entry with the same `id` field.

`enable_settings` contains a `|` separated collection of setting IDs as specified in `LunaSettings.csv`. The setting must be a `Boolean` setting. If any of the settings are set to `false`, then the effect will be skipped.

`effect_type` contains either one of the entires in `Base Effect Types` below or a mode specified addition.

`data` contains `effect_type` specific data. Check `Base Effect Types` for more info.

## Requirements
Requirements are set up in 3 phases. Phase 1 is the individual requirements as documented above. They're defined in `terraforming_requirements.csv`. Phase 2 and 3 are grouping the requirements into OR and AND groups. Phase 2 is defined in `terraforming_requirements_OR.csv`, and phase 3 is defined wherever the requirements are used.

`terraforming_requirements_OR.csv` has 5 fields. The `id` field contains the unique ID that this requirement is identified by. It only needs to be unique to this file.

`tooltip` is the tooltip used whenever this requirement is queried for a tooltip.

`tooltip_highlights` is a JSON Array of substrings that will be highlighted.

`invert_all` contains `true` or `false`. If `true`, the result of the check is inverted and a requirement check that returns true instead returns false, and vice versa.

`requirements` is a `|` separated list of requirement IDs from `terraforming_requirement.csv`. If any of the requirements returns true, then this requirement is satisfied.

## Planet types
Planet types are specified in `planet_types.csv`. The `id` field is the planet type as used by Starsector.

`name` is a display name of the planet type.

`terraforming_type_id` is the type that is checked in requirements.

`terraforming_possible` is a boolean whether this planet type can be terraformed further.

`base_water_level` is an integer of the water level, should be `0`, `1`, or `2`.

`conditional_water_requirements` is a JSON Array of JSON Objects with 2 fields. `requirements` a JSON Object is the same format as `requirements` from `Terraforming Projects`. `water_level` is the water level of the planet if the condition is satisfied. 

## Planet resources
Resource progressions are in `resource_progression.csv`. `id` is the file unique ID for this resource. `resource_progression` is a JSON Array of the conditions that provide this resource. It should be sorted from the lowest quality to the highest quality.

Planet max resources are in `planet_max_resource.csv`. `id` is the `terraforming_type_id` from `planet_types.csv` that this info applies to.

`resources_max` is a JSON Array of JSON Objects. `resource_id` is the ID from `resource_progression.csv` that this resource is from. `resource_max` is an entry from the corresponding resource that this resource can't be higher than.

## Base Requirement Types
`AlwaysTrue` always returns true for its requirement check. To get an `AlwaysFalse` type effect, put `true` in the `invert` field.

`PlanetType` checks the targeted planet's planet type. `data` accepts a single value, one of the values in `terraforming_type_id` from `planet_types.csv`. Returns true if the planet's type matches the given value.

`FocusPlanetType` works the same as `PlanetType` but checks the planet the entity is orbiting.

`MarketHasCondition` checks the targeted market's conditions. `data` accepts a single value, one condition from the game (modded or otherwise). Returns true if the market has the given condition.

`FocusMarketHasCondition` works the same as `MarketHasCondition` but checks the market on the entity the market is orbiting.

`MarketHasIndustry` works the same as `MarketHasCondition` but checks industries instead. `data` accepts a single value, one industry from the game (modded or otherwise). Returns true if the market has the given industry, and the industry has finished being built.

`MarketHasIndustryWithItem` works the same as `MarketHasIndustry` but also checks if the industry has the given item. `data` is a JSON Object with two fields, `industry_id`, and `item_id`. Returns true if the industry is built and has the given item.

`MarketHasIndustryWithAICore` works the same as `MarketHasIndustryWithItem` but checks if the industry has the given AI core. JSON Object has `ai_core_id` instead of `item_id`. Returns true if the industry is built and has the given AI core.

`IndustryHasShortage` checks if the source industry has a shortage of the given commodities (modded or otherwise). Data is a JSON Array of the requested commodities. Returns true if the industry has a shortage of the given commodities.

`PlanetWaterLevel` checks the given planet's water level. The planet's water level is from the `base_water_level` and `conditional_water_requirements` fields in `planet_types.csv`. `data` is a JSON Object with 2 fields, `min_water_level` and `max_water_level`. Returns true if the planet's water level is greater than or equal to `min_water_level`, and less than or equal to `max_water_level`.

`MarketHasWaterPresent` is the same as `PlanetWaterLevel` but the JSON Object takes a third parameter, `water_industry_ids`. Returns true if `PlanetWaterLevel` would return true, or any of the markets in the starsystem belonging to the same faction have any of the given industries.

`TerraformingPossibleOnMarket` checks the `terraforming_possible` field from `planet_types.csv`, as well as a given list of invalidating conditions. `data` is a JSON Array of market conditions. Returns true if the `terraforming_possible` field is `true`, and if the market does not have any of the given conditions.

`MarketHasTags` checks the given list of tags. `data` is a JSON Array of tags. Returns true if the market has any of the given tags.

`MarketIsAtLeastSize` checks the market size. `data` is a single integer. Returns true if the market size is greater than or equal to the given value.

`MarketStorageContainsAtLeast` checks the provided market storage for the provided item/commodity. `data` is a JSON Object with a `submarket_id` field that specifies which market to check, `commodity_id` or `special_item_id` that specifies the commodity or special item to check for, and a `quantity` or `setting_id` field that specifies the quantity to check for. If `setting_id` is set, the `quantity` field is unused and the quantity is read from the `LunaLib` setting. `job_id` is the job ID for Crew Replacer. Returns true if the specified market storage has at least the specified number of specified items.

`FleetStorageContainsAtLeast` checks the fleet's storage for the provided item/commodity. `data` is the same as for `MarketStorageContainsAtLeast` but with two differences. `submarket_id` is not used and can be omitted, and if no `commodity_id` or `special_item_id` is specified, it will take credits. Returns true if the fleet storage has at least the specified number of specified items, or credits if no item is specified.

`FleetTooCloseToJumpPoint` checks the fleet's distance to a jump point. `data` is a single integer that is the distance to the jump point. Returns true if the fleet is greater than or equal to the specified distance. 2000 units is one map square.

`PlayerHasStoryPointsAtLeast` checks the player's story points. `data` is a single integer that is the required number of story points. Returns true if the player has at least the specified number of story points.

`WorldTypeSupportsResourceImprovement` checks the planet's max resource from `planet_max_resource.csv`. `data` is a single string of a resource ID from `resource_progression.csv`. Returns true if the given resource is less than the max resource, using the progression given in `resource_progression.csv`.

`IntegerFromMarketTagSubstring` checks the integer on the end of a tag on a market. `data` is a JSON Object with a `setting_id` value that specifies the setting to take an offset from. If it's empty, the offset is zero. `tag_substring` is the beginning of the tag that the number is taken from. `max_value` is the maximum value the tested value can be. Returns true if `max_value` is greater than the integer at the end of the tag plus the offset value.

`PlayerHasSkill` checks that the player has the specified skill. `data` is a string that contains the skill ID. Returns true if the player has the specified skill.

`SystemStarHasTags` checks all stars in a system for the given tags. A single star must have all the specified tags. `data` is a JSON Array of the tags the star must have. Returns true if any star in the system has all the requested tags.

`SystemStarType` checks if the system star is the given type. Uses vanilla star types. `data` is a string that contains the star type. Returns true if the system star is the same as the specified type.

`FleetInHyperspace` checks if the fleet is in hyperspace. `data` is unused. Returns true if the fleet is in hyperspace.

`SystemHasJumpPoints` checks if the given system has at least the specified number of jump points. `data` is a single integer specifying the number of jump points, or 1 if empty. Returns true if the number of jump points in the system is greater than or equal to the specified number.

`SystemHasPlanets` checks if the given system has at least the specified number of planets. `data` is a single integer specifying the number of planets, or 0 if empty. Returns true if the number of planets in the system is greater than or equal to the specified number.

`TargetPlanetOwnedBy` checks if the targeted planet is owned by the given factions. `data` is a JSON Array of faction IDs. Returns true if the planet's faction ID is contained in the provided list.

`TargetStationOwnedBy` works the same as `TargetPlanetOwnedBy` but targets stations.

`TargetPlanetGovernedByPlayer` checks if the targeted planet is governed by the player, ie the market is player owned but is not part of the player's faction.

`TargetPlanetWithinDistance` checks if the distance between the targeted planet and the fleet is less than or equal to the provided distance. `data` is a single integer of the distance the fleet has to be within.

`TargetStationWithinDistance` is the same as `TargetPlanetWithinDistance` but targets stations.

`TargetStationColonizable` checks if the targeted station is colonizable, ie it has both a market and has the `abandonded_station` condition. Returns true when both these conditions are true.

`TargetPlanetIsAtLeastSize` checks if the targeted planet's size is greater than or equal to the specified value. `data` is a single integer that specifies the minimum size. Returns true when the planet's radius is greater than or equal to the specified value.

`TargetPlanetOrbitFocusWithinDistance` checks if the object the targeted planet is orbiting is within the specified distance. `data` is a single integer that specifies the distance. Returns true when the targeted planet's orbit radius is less than the orbited object's radius plus the specified distance.

`TargetPlanetStarWithinDistance` and `TargetPlanetOrbitersWithinDistance` both work the same as `TargetPlanetOrbitFocusWithinDistance` except they target the system's star, and anything orbiting the targeted planet respectively.

`TargetPlanetMoonCountLessThan` checks that the targeted planet has less than the specified number of moons. `data` is a single integer that specifies the max number of moons. Returns true when the number of moons is less than the specified number of moons.

`TargetPlanetOrbitersTooClose` checks all other objects orbiting the same object as the targeted one for if their orbit comes too close to the targeted planet's orbit. `data` is a single integer that specifies the minimum distance between the orbits. Returns true when the difference between the two objects orbit radii is greater than the specified value.

`TargetPlanetStationCountLessThan` checks for stations with the specified tags that orbit the targeted planet. `data` is a JSON Object with three fields, `station_tags` which is a JSON Array of tags the station has to have to be counted, `setting_id` which is an optional setting that the count will come from, and `max_num` which is the maximum number of stations that can be placed around the planet. Returns true when the number of stations with the specified tags is less than the specified value.

`TargetSystemStationCountLessThan` is the same as `TargetPlanetStationCountLessThan` but checks for stations in the system.

`FleetInAsteroidBelt` and `FleetInAsteroidField` both check if the given fleet is inside an asteroid belt or asteroid field respectively. Returns true when inside the appropriate terran hazards.

`TargetPlanetStoryCritical` and `TargetStationStoryCritical` check if the targeted planet or station respectively is story critical. Returns true if the target planet or station respectively is story critical.

`BooleanSettingIsTrue` is for chaining a requirement on a setting. `data` is a JSON Object with three fields, `setting_id` is the `LunaLib` setting to check, `invert_setting` is an optional boolean to activate this on a `LunaLib` value of `false`, and `requirement_id` is the requirement to check if the value from `setting_id` is true. Returns true when the value from `setting_id` (after possible inversion) is false, otherwise returns the result of the specified requirement.

## Base Duration Modifier Types
`PlanetSize` modifies the project duration based on the size of the planet it's applied on.

`DurationSettingModifier` modifies the base project duration to be the value of the setting provided in `data`. The duration may still be modified further by other modifiers. `data` is a string containing the `LunaLib` setting ID to take the duration from. The `LunaLib` setting must be an `Int` type.

## Base Effect Types
`PlanetTypeChange` changes the planet type from whatever it is to the new type specified. The type is one of the planet types from `data/config/planets.json` (or a mod specific addition). `data` is a single string that contains the planet type id. 

`IndustrySwap` replaces one industry with another. If the industry that is to be removed is under construction, the new one is queued for construction at zero cost. If the industry that is to be removed is complete, it's just replaced with the new one. `data` is a JSON Object with two fields, `industry_to_remove` is a string that is the industry to remove, `industry to add` is a string that is the industry to add.

`MarketAddCondition` and `MarketRemoveCondition` adds or removes a condition respectively to the targeted market. `data` is a string that is the condition ID to add or remove respectively.

`MarketProgressResource` advances a resource along its progression according to `resource_progression.csv`. `data` is a JSON Object with two fields, `resource_id` is one of the `id` entries from `resource_progression.csv`, and `step` is how far it should advance along the progression and can be negative.

`FocusMarketAddCondition`, `FocusMarketRemoveCondition`, and `FocusMarketProgressResource` are the same as `MarketAddCondition`, `MarketRemoveCondition`, and `FocusMarketProgressResource` respectively, but target the orbit focus of the target market.

`FocusMarketAndSiphonStationProgressResource` is the same as `FocusMarketProgressResource` but also applies to Siphon Stations orbiting the same entity as this entity.

`SystemAddCoronalTap` adds a Coronal Tap to the targeted star.

`MarketRemoveIndustry` removes the specified industry from the target market. `data` is a single string of the industry ID to remove.

`RemoveItemFromSubmarket` removes the specified items from the specified submarket (Storage, etc.). `data` is a JSON Object with the following fields, `submarket_id` is the submarket to remove the items from (usually `storage`). `commodity_id` or `special_item_id` is the item to remove, use one or the other. `setting_id` or `quantity` specifies how much of the item are to be removed. If `setting_id` is specified, the quantity will be retrieved from the `LunaLib` setting of the same name.

`RemoveItemFromFleetStorage` works the same way as `RemoveItemFromSubmarket` but `commodity_id` or `special_item_id` can be omitted to remove credits instead.

`RemoveStoryPointsFromPlayer` removes story points from the player. `data` is a JSON Object with either `setting_id` or `quantity`. If `setting_id` is specified, the quantity will be retrieved from the `LunaLib` setting of the same name.

`AddItemToSubmarket` works the same way as `RemoveItemFromSubmarket`, but adds the item instead of removing it.

`AddStationToOrbit` adds a station to the orbit of the targeted planet. `data` is a JSON Object. `station_type` is the type of station to add from `boggled_astropolis`, `boggled_mining`, `boggled_siphon`, or a mod specific addition. `station_name` is what the station type will be called. `variants` is an optional JSON array of the variants from `custom_entities.json`. `num_stations_per_layer` is the number of stations that will be placed around a planet before they start being placed further out. `orbit_radius` is the distance from the planet that the station will orbit at. `station_construction_data` is a JSON Object of `station_type` specific data. All the base stations have `industries_to_queue` which is a JSON Array of industry IDs to queue for building. `boggled_mining` and `boggled_siphon` also have `resources_to_highlight` which is a JSON Array of resources to highlight in the tooltip.

`AddStationToAsteroids` adds a station to the nearest asteroid belt or asteroid field. It otherwise functions exactly like `AddStationToOrbit`

`ColonizeAbandonedStation` colonizes an abandoned station. If the station is an abandoned custom station, then it will be colonized using the `station_colonization_data` field. Otherwise, it will be colonized according to some hard coded data.

`EffectWithRequirement` combines effects with requirements. If the requirements are not satisfied, the effects will not trigger. `data` is a JSON Object. `requirements` is the same as `Terraforming Projects` `requirements` field. `effects` is a JSON Array of effects to apply if the requirement is satisfied. `display_requirement_tooltip_on_requirement_failure` is a boolean that controls if the requirement tooltip is displayed when the requirement is not satisfied. `display_effect_tooltip_on_requirement_failure` does the same but for the effect tooltip. Both of them are optional and are `false` if omitted.

`AdjustRelationsWith` adjusts relations between one faction, and one or more factions. `data` is a JSON Object. `faction_id_to_adjust_relations_to` is the faction ID of the target faction. `faction_ids_to_adjust_relations` is a JSON Array of factions that the target faction's relations are being adjusted. `new_relation_value` is the new value of the relations.

`AdjustRelationsWillAllExcept` is the same as `AdjustRelationsWith` except `faction_ids_to_not_adjust_relations`, instead of `faction_ids_to_adjust_relations`, is a JSON Array of factions that won't have their relations adjusted.

`TriggerMilitaryResponse` triggers a military response. `data` is a JSON Object. `response_fraction` is the fraction of military fleets assigned to the market that will respond. `response_duration` is the number of days they will spend responding.

`DecivilizeMarket` decivilizes a market as though it's just been saturation bombed. `data` is a JSON Object. `faction_ids_to_not_make_hostile` is a JSON Array of faction IDs that won't become hostile.

All effects that begin with `Modify` have `data` that is a JSON Object. `modifier_type` is a string that specifies how the modifier works, valid values are `market_size`, `flat`, `mult`, or `percent`. When `modifier_type` is `market_size`, `value` is a modifier to the market size. When `modifier_type` is `flat`, `mult`, or `percent`, value is a flat value, a multiplier, or a percentage multiplier applied to the thing being modified. `display_type` allows the tooltip to show in a different manner than what the modifier actually is. Current allowed combinations are `mult` for `modifier_type` and `percent` for `display_type`. 

`ModifyPatherInterest` modifies the industry's pather interest.
`ModifyColonyGrowthRate` modifies the colony's growth rate.
`ModifyColonyGroundDefense` modifies the colony's ground defense.
`ModifyColonyAccessibility` modifies the colony's accessibility.
`ModifyColonyStability` modifies the colony's stability.
`ModifyIndustryUpkeep` modifies the target industry's upkeep.
`ModifyIndustryIncome` modifies the target industry's upkeep.
`ModifyIndustrySupplyWithDeficit` modifies the target industry's supply, adjusted by the deficit of the source industry's demanded commodities. `data` takes the additional fields of `commodities_demanded` which is an optional JSON Array of commodity IDs to check for deficits, and `max_deficit` which is the maximum amount the commodities can be deficited from these demands. If you just want to modify the supply with no deficits, these can be omitted. 
`ModifyIndustryDemand` modifies the target industry's commodity demands.

`EffectToIndustry` applies an effect to another industry. The industry that this is applied to becomes the source industry, and the industry ID that the specified effect is applied to becomes the target industry. `data` is a JSON Object. `industry_id` is the industry that will become the target industry. `effect_id` is the effect that will be applied to the target industry.

`SuppressConditions` suppresses conditions. The conditions are still there, but they have no effects. `data` is a JSON Array of condition IDs to suppress.

`IndustryMonthlyItemProduction` allows an industry to produce items each month.

`IndustryMonthlyItemProductionChance` specifies the base chance of an item being produced each month. `data` is a JSON Array of JSON Objects. `commodity_priority` is the priority of checking if an item is produced. Lower priorities are checked first. If multiple items have the same priority, check order is unspecified. `commodity_id` is the commodity to be produced. `chance` is the base chance of the item to be produced, out of 100. `requirements` is the same format as `requirements` from `Terraforming Projects` and is the requirements that must be satisfied for the item to be produced regardless of chance.

`IndustryMonthlyItemProductionChanceModifier` are modifiers to the base production chance. If a commodity specified here has no base production chance, then it will never be produced. `data` is a JSON Array of JSON Objects. `commodity_id` is the commodity ID to modify. `chance_modifier` is the amount to modify the base chance by.

`StepTag` modifies the numeric value at the end of a tag on a market, if the tag doesn't exist, then the first time this effect is applied, the tag is added with the step as its value. `data` is a JSON Object. `tag` is the start of the tag. `step` is how much the value at the end of the tag should be changed.

`IndustryRemove` removes the specified industry from the market. Special items and AI cores will be moved to storage. `data` is a JSON Object. `industry_id` is the industry ID to remove.

`TagSubstringPowerModifyBuildCost` modifies the build cost of the source industry according to the integer at the end of the tag substring, or the default if the tag doesn't exist on the market. The modified build cost is `2 ** value * buildCost`. data` is a JSON Object. `tag` is the tag to check and extract the value from. `default` is the value to use if the tag doesn't exist.

`EliminatePatherInterest` makes this market not generate any pather interest.

`AddStellarReflectorsToOrbit` adds 3 stellar mirrors or stellar shades to orbit depending on market conditions.

All effects that begin with `CommodityDeficit` accept a JSON Object with a `commodities_demanded` field that is a JSON Array of commodity IDs. They check the collection of specified commodities then do type specific effects. They may have other fields in the JSON Object.

`CommodityDeficitToProduction` applies the deficit in the demanded commodities to the specified commodities. `commodities_deficited` is a JSON Array of commodity IDs that will be affected by the shortage in demanded commodities, and `max_deficit` is the optional maximum amount the commodity can be deficited. If `max_deficit` is omitted, the maximum production reduction is unlimited.

`CommodityDeficitModifierToUpkeep` is a combination of `CommodityDeficit` and `ModifyIndustryUpkeep`. It requires `commodities_demanded`, `modifier_type`, and `value`.

All effects that begin with `CommodityDemand` or `CommoditySupply` accept a JSON Object with a `commodity_id` field, which is a string of the commodity to supply or demand, and a `quantity`, which is an integer which determines how the commodity is supplied or demanded.

`CommodityDemandFlat` demands the same amount of commodity regardless of any other condition.

`CommodityDemandMarketSize` demands a variable amount of commodity depending on the market size. `quantity` is a modifier to the market size, e.g. a `quantity` of 1 means the demand will be market size + 1.

`CommodityDemandPlayerMarketSizeElseFlat` is `CommodityDemandMarketSize` if the market is player owned, otherwise it's `CommodityDemandFlat`. If the market is player owned, then `quantity` is treated as 0.

`CommoditySupplyFlat` and `CommoditySupplyMarketSize` are the same as `CommodityDemandFlat` and `CommodityDemandMarketSize` but supply the commodity instead of demanding it.

`AttachProjectToIndustry` attaches a terraforming project to an industry. This is so the industry can show the project progress, incomplete message, etc. from the colony management screen. `data` is a JSON Object. `industry_id` is the industry ID it should be attached to. The industry must implement the `BoggledIndustryInterface`. If the industry plugin is specified as `boggled.campaign.econ.industries.BoggledBaseIndustry`, `boggled.campaign.econ.industries.Boggled_Remnant_Station`, or `boggled.campaign.econ.industries.Boggled_Cryosanctum` then it does. Other mod specified industry plugins may work.

# Adding New Types
Create a new class that extends `BoggledTerraformingRequirement.TerraformingRequirement`, implement all required functions. Create a new class that implements `BoggledTerraformingRequirementFactory.TerraformingRequirementFactory` and implement `constructFromJSON`. Call `boggledTools.addTerraformingRequirementFactory` with what you want the requirement type to be called, and an instance of the new requirement factory.

Do the same thing for `BoggledTerraformingProjectEffect.TerraformingProjectEffect`, `BoggledTerraformingProjectEffectFactory.TerraformingProjectEffectFactory`, and `boggledTools.addTerraformingProjectEffectFactory`