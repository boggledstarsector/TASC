First up is the context object `RequirementContext` that is passed into a lot of things. Aside from the `contextName`, everything may be null. In general, grab what you need from the context and then check them all for null. Handle those cases appropriately.

## Individual requirements

All requirements in `terraforming_requirement.csv` have 4 required fields. The `id` field is what is used to uniquely identify this requirement. Mods can overwrite a requirement by providing their own `terraforming_requirement.csv` file with an entry with the same `id` field.

`enable_settings` contains a `|` separated collection of setting IDs as specified in `LunaSettings.csv`. The setting must be a `Boolean` setting. If the setting is set to `false`, then the requirement will be skipped. If all of a requirement collections' requirements are disabled, the requirement is considered satisfied.

`requirement_type` contains either one of the entries in `Base Requirement Types` below, or a mod specified addition.

`invert` contains `true` or `false`. If `true`, the result of the check is inverted and a requirement check that returns true instead returns false, and vice versa.

`data` contains `requirement_type` specific data. Check `Base Requirement Types` for more info.

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

`MarketStorageContainsAtLeast` checks the provided market storage for the provided item/commodity. `data` is a JSON Object with a `submarket_id` field that specifies which market to check, `commodity_id` or `special_item_id` that specifies the commodity or special item to check for, and a `quantity` or `setting_id` field that specifies the quantity to check for. If `setting_id` is set, the `quantity` field is unused and the quantity is read from the `LunaLib` setting. Returns true if the specified market storage has at least the specified number of specified items.

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

`TargetPlanetOwnedBy` checks if the targeted planet is owned by the given factions. `data` is a JSON Array of faction IDs. Returns true if the planet's faction 