package boggled.scripts;

import boggled.campaign.econ.boggledTools;
import boggled.campaign.econ.industries.BoggledCommonIndustry;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Pair;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BoggledTerraformingProjectEffectFactory {
    public interface TerraformingProjectEffectFactory {
        BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException;
    }

    public static class PlanetTypeChange implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            boggledTools.CheckPlanetTypeExists(id, data);
            return new BoggledTerraformingProjectEffect.PlanetTypeChange(id, enableSettings, data);
        }
    }

    public static class IndustrySwap implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String industryIdToRemove = jsonData.getString("industry_to_remove");
            String industryIdToAdd = jsonData.getString("industry_to_add");
            return new BoggledTerraformingProjectEffect.IndustrySwap(id, enableSettings, industryIdToRemove, industryIdToAdd);
        }
    }

    public static class MarketAddCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            boggledTools.CheckMarketConditionExists(id, data);
            return new BoggledTerraformingProjectEffect.MarketAddCondition(id, enableSettings, data);
        }
    }

    public static class MarketRemoveCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            boggledTools.CheckMarketConditionExists(id, data);
            return new BoggledTerraformingProjectEffect.MarketRemoveCondition(id, enableSettings, data);
        }
    }

    public static class MarketOptionalCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String option = jsonData.getString("option");
            String conditionId = jsonData.getString("condition_id");

            boggledTools.CheckMarketConditionExists(id, conditionId);

            if (boggledTools.getBooleanSetting(option)) {
                return new BoggledTerraformingProjectEffect.MarketAddCondition(id, enableSettings, conditionId);
            }
            return new BoggledTerraformingProjectEffect.MarketRemoveCondition(id, enableSettings, conditionId);
        }
    }

    public static class MarketProgressResource implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String resourceId = jsonData.getString("resource_id");
            int step = jsonData.getInt("step");

            boggledTools.CheckResourceExists(id, resourceId);

            return new BoggledTerraformingProjectEffect.MarketProgressResource(id, enableSettings, resourceId, step);
        }
    }

    public static class FocusMarketAddCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            boggledTools.CheckMarketConditionExists(id, data);
            return new BoggledTerraformingProjectEffect.FocusMarketAddCondition(id, enableSettings, data);
        }
    }

    public static class FocusMarketRemoveCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            boggledTools.CheckMarketConditionExists(id, data);
            return new BoggledTerraformingProjectEffect.FocusMarketRemoveCondition(id, enableSettings, data);
        }
    }

    public static class FocusMarketProgressResource implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String resourceId = jsonData.getString("resource_id");
            int step = jsonData.getInt("step");

            boggledTools.CheckResourceExists(id, resourceId);

            return new BoggledTerraformingProjectEffect.FocusMarketProgressResource(id, enableSettings, resourceId, step);
        }
    }

    public static class FocusMarketAndSiphonStationProgressResource implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String resourceId = jsonData.getString("resource_id");
            int step = jsonData.getInt("step");

            boggledTools.CheckResourceExists(id, resourceId);

            return new BoggledTerraformingProjectEffect.FocusMarketAndSiphonStationProgressResource(id, enableSettings, resourceId, step);
        }
    }

    public static class SystemAddCoronalTap implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            return new BoggledTerraformingProjectEffect.SystemAddCoronalTap(id, enableSettings);
        }
    }

    public static class MarketRemoveIndustry implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            return new BoggledTerraformingProjectEffect.MarketRemoveIndustry(id, enableSettings, data);
        }
    }

    public static class RemoveItemFromSubmarket implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String submarketId = jsonData.getString("submarket_id");

            BoggledTerraformingRequirement.ItemRequirement.ItemType itemType = null;
            String itemId = null;

            String commodityId = jsonData.optString("commodity_id");
            if (!commodityId.isEmpty()) {
                itemType = BoggledTerraformingRequirement.ItemRequirement.ItemType.RESOURCES;
                itemId = commodityId;
                boggledTools.CheckCommodityExists(id, commodityId);
            }

            String specialItemId = jsonData.optString("special_item_id");
            if (!specialItemId.isEmpty()) {
                itemType = BoggledTerraformingRequirement.ItemRequirement.ItemType.SPECIAL;
                itemId = specialItemId;
                boggledTools.CheckSpecialItemExists(id, specialItemId);
            }

            if (itemType == null) {
                itemType = BoggledTerraformingRequirement.ItemRequirement.ItemType.CREDITS;
            }

            String settingId = jsonData.optString("setting_id");

            int quantity = jsonData.getInt("quantity");

            boggledTools.CheckSubmarketExists(id, submarketId);

            return new BoggledTerraformingProjectEffect.RemoveItemFromSubmarket(id, enableSettings, submarketId, itemType, itemId, settingId, quantity);
        }
    }

    public static class RemoveStoryPointsFromPlayer implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            int quantity = jsonData.getInt("quantity");
            String settingId = jsonData.optString("setting_id");
            return new BoggledTerraformingProjectEffect.RemoveStoryPointsFromPlayer(id, enableSettings, quantity, settingId);
        }
    }

    public static class RemoveItemFromFleetStorage implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            BoggledTerraformingRequirement.ItemRequirement.ItemType itemType = null;
            String itemId = null;

            String commodityId = jsonData.optString("commodity_id");
            if (!commodityId.isEmpty()) {
                itemType = BoggledTerraformingRequirement.ItemRequirement.ItemType.RESOURCES;
                itemId = commodityId;
                boggledTools.CheckCommodityExists(id, commodityId);
            }

            String specialItemId = jsonData.optString("special_item_id");
            if (!specialItemId.isEmpty()) {
                itemType = BoggledTerraformingRequirement.ItemRequirement.ItemType.SPECIAL;
                itemId = specialItemId;
                boggledTools.CheckSpecialItemExists(id, specialItemId);
            }

            if (itemType == null) {
                itemType = BoggledTerraformingRequirement.ItemRequirement.ItemType.CREDITS;
            }

            String settingId = jsonData.optString("setting_id");

            int quantity = jsonData.getInt("quantity");

            return new BoggledTerraformingProjectEffect.RemoveItemFromFleetStorage(id, enableSettings, itemType, itemId, settingId, quantity);
        }
    }

    public static class AddItemToSubmarket implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String submarketId = jsonData.getString("submarket_id");
            String itemId = jsonData.getString("item_id");
            int quantity = jsonData.getInt("quantity");

            boggledTools.CheckSubmarketExists(id, submarketId);
            boggledTools.CheckItemExists(id, itemId);

            return new BoggledTerraformingProjectEffect.AddItemToSubmarket(id, enableSettings, submarketId, itemId, quantity);
        }
    }

    public static class AddStationToOrbit implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String stationType = jsonData.getString("station_type");
            String stationName = jsonData.getString("station_name");
            JSONArray variantsArray = jsonData.optJSONArray("variants");
            List<String> variants = new ArrayList<>();
            if (variantsArray != null) {
                for (int i = 0; i < variantsArray.length(); ++i) {
                    variants.add(variantsArray.getString(i));
                }
            }
            int numStationsPerLayer = jsonData.getInt("num_stations_per_layer");
            float orbitRadius = (float) jsonData.getDouble("orbit_radius");

            BoggledStationConstructionFactory.StationConstructionFactory factory = boggledTools.stationConstructionFactories.get(stationType);
            BoggledStationConstructors.StationConstructionData stationConstructionData = factory.constructFromJSON(id, jsonData.getJSONObject("station_construction_data").toString());

            return new BoggledTerraformingProjectEffect.AddStationToOrbit(id, enableSettings, stationType, stationName, variants, numStationsPerLayer, orbitRadius, stationConstructionData);
        }
    }

    public static class AddStationToAsteroids implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String stationType = jsonData.getString("station_type");
            String stationName = jsonData.getString("station_name");
            JSONArray variantsArray = jsonData.optJSONArray("variants");
            List<String> variants = new ArrayList<>();
            if (variantsArray != null) {
                for (int i = 0; i < variantsArray.length(); ++i) {
                    variants.add(variantsArray.getString(i));
                }
            }
            int numStationsPerLayer = jsonData.getInt("num_stations_per_layer");
            float orbitRadius = (float) jsonData.getDouble("orbit_radius");

            BoggledStationConstructionFactory.StationConstructionFactory factory = boggledTools.stationConstructionFactories.get(stationType);
            BoggledStationConstructors.StationConstructionData stationConstructionData = factory.constructFromJSON(id, jsonData.getJSONObject("station_construction_data").toString());

            return new BoggledTerraformingProjectEffect.AddStationToEntity(id, enableSettings, stationType, stationName, variants, numStationsPerLayer, orbitRadius, stationConstructionData);
        }
    }

    public static class ColonizeAbandonedStation implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            Logger log = Global.getLogger(this.getClass());
            JSONObject jsonData = new JSONObject(data);
            JSONObject stationColonizationDataObject = jsonData.optJSONObject("station_colonization_data");
            List<BoggledStationConstructors.StationConstructionData> stationConstructionData = new ArrayList<>();
            if (stationColonizationDataObject != null) {
                for (Iterator<String> it = stationColonizationDataObject.keys(); it.hasNext(); ) {
                    String key = it.next();
                    String stationColonizationDataString = stationColonizationDataObject.getJSONObject(key).toString();
                    BoggledStationConstructionFactory.StationConstructionFactory factory = boggledTools.stationConstructionFactories.get(key);
                    if (factory == null) {
                        log.error("ColonizeAbandonedStation " + id + " has invalid station construction factory " + key);
                        continue;
                    }
                    BoggledStationConstructors.StationConstructionData scd = factory.constructFromJSON(id, stationColonizationDataString);
                    if (scd != null) {
                        stationConstructionData.add(scd);
                    } else {
                        log.error("ColonizeAbandonedStation " + id + " has invalid station construction data " + key);
                    }
                }
            }
            BoggledStationConstructors.StationConstructionData defaultStationConstructionData = new BoggledStationConstructors.DefaultConstructionData("default_station", new ArrayList<String>());
            return new BoggledTerraformingProjectEffect.ColonizeAbandonedStation(id, enableSettings, defaultStationConstructionData, stationConstructionData);
        }
    }

    public static class EffectWithRequirement implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            Logger log = Global.getLogger(this.getClass());
            JSONObject jsonData = new JSONObject(data);
            JSONArray reqsArray = jsonData.getJSONArray("requirement_ids");
            JSONArray effectsArray = jsonData.getJSONArray("effects");

            BoggledProjectRequirementsAND reqs = boggledTools.requirementsFromRequirementsArray(reqsArray, "EffectWithRequirement", id, "requirements");
            List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> effects = new ArrayList<>();
            for (int i = 0; i < effectsArray.length(); ++i) {
                String effectString = effectsArray.getString(i);
                BoggledTerraformingProjectEffect.TerraformingProjectEffect effect = boggledTools.getProjectEffect(effectString);
                if (effect != null) {
                    effects.add(effect);
                } else {
                    log.info("EffectWithRequirement " + id + " has invalid effect " + effectString);
                }
            }
            boolean displayRequirementTooltipOnRequirementFailure = jsonData.optBoolean("display_requirement_tooltip_on_requirement_failure", false);
            boolean displayEffectTooltipOnRequirementFailure = jsonData.optBoolean("display_effect_tooltip_on_requirement_failure", false);
            return new BoggledTerraformingProjectEffect.EffectWithRequirement(id, enableSettings, reqs, effects, displayRequirementTooltipOnRequirementFailure, displayEffectTooltipOnRequirementFailure);
        }
    }

    public static class AdjustRelationsWith implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String factionIdToAdjustRelationsTo = jsonData.getString("faction_id_to_adjust_relations_to");
            JSONArray factionIdsToAdjustRelationsArray = jsonData.getJSONArray("faction_ids_to_adjust_relations");
            List<String> factionIdsToAdjustRelations = new ArrayList<>();
            for (int i = 0; i < factionIdsToAdjustRelationsArray.length(); ++i) {
                factionIdsToAdjustRelations.add(factionIdsToAdjustRelationsArray.getString(i));
            }
            float newRelationValue = (float) jsonData.getDouble("new_relation_value");
            return new BoggledTerraformingProjectEffect.AdjustRelationsWith(id, enableSettings, factionIdToAdjustRelationsTo, factionIdsToAdjustRelations, newRelationValue);
        }
    }

    public static class AdjustRelationsWithAllExcept implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String factionIdToAdjustRelationsTo = jsonData.getString("faction_id_to_adjust_relations_to");
            JSONArray factionIdsToNotAdjustRelationsArray = jsonData.getJSONArray("faction_ids_to_not_adjust_relations");
            List<String> factionIdsToNotAdjustRelations = new ArrayList<>();
            for (int i = 0; i < factionIdsToNotAdjustRelationsArray.length(); ++i) {
                factionIdsToNotAdjustRelations.add(factionIdsToNotAdjustRelationsArray.getString(i));
            }
            float newRelationValue = (float) jsonData.getDouble("new_relation_value");
            return new BoggledTerraformingProjectEffect.AdjustRelationsWithAllExcept(id, enableSettings, factionIdToAdjustRelationsTo, factionIdsToNotAdjustRelations, newRelationValue);
        }
    }

    public static class TriggerMilitaryResponse implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            float responseFraction = (float) jsonData.getDouble("response_fraction");
            float responseDuration = (float) jsonData.getDouble("response_duration");
            return new BoggledTerraformingProjectEffect.TriggerMilitaryResponse(id, enableSettings, responseFraction, responseDuration);
        }
    }

    public static class DecivilizeMarket implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray factionIdsToNotMakeHostileArray = jsonData.getJSONArray("faction_ids_to_not_make_hostile");
            List<String> factionIdsToNotMakeHostile = new ArrayList<>();
            for (int i = 0; i < factionIdsToNotMakeHostileArray.length(); ++i) {
                factionIdsToNotMakeHostile.add(factionIdsToNotMakeHostileArray.getString(i));
            }
            return new BoggledTerraformingProjectEffect.DecivilizeMarket(id, enableSettings, factionIdsToNotMakeHostile);
        }
    }

    public static class ModifyPatherInterest implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modifierType = jsonData.getString("modifier_type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledTerraformingProjectEffect.ModifyPatherInterest(id, enableSettings, modifierType, value);
        }
    }

    public static class ModifyColonyGrowthRate implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modifierType = jsonData.getString("modifier_type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledTerraformingProjectEffect.ModifyColonyGrowthRate(id, enableSettings, modifierType, value);
        }
    }

    public static class ModifyColonyGroundDefense implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modifierType = jsonData.getString("modifier_type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledTerraformingProjectEffect.ModifyColonyGroundDefense(id, enableSettings, modifierType, value);
        }
    }

    public static class ModifyColonyAccessibility implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modifierType = jsonData.getString("modifier_type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledTerraformingProjectEffect.ModifyColonyAccessibility(id, enableSettings, modifierType, value);
        }
    }

    public static class ModifyColonyStability implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modifierType = jsonData.getString("modifier_type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledTerraformingProjectEffect.ModifyColonyStability(id, enableSettings, modifierType, value);
        }
    }

    public static class ModifyIndustryUpkeep implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modifierType = jsonData.getString("modifier_type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledTerraformingProjectEffect.ModifyIndustryUpkeep(id, enableSettings, modifierType, value);
        }
    }

    public static class ModifyIndustryIncome implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modifierType = jsonData.getString("modifier_type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledTerraformingProjectEffect.ModifyIndustryIncome(id, enableSettings, modifierType, value);
        }
    }

    public static class ModifyIndustrySupplyWithDeficit implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray jsonCommoditiesDemanded = jsonData.getJSONArray("commodities_demanded");
            List<String> commoditiesDemanded = new ArrayList<>();
            for (int i = 0; i < jsonCommoditiesDemanded.length(); ++i) {
                commoditiesDemanded.add(jsonCommoditiesDemanded.getString(i));
            }
            int supplyBonus = jsonData.getInt("bonus");
            return new BoggledTerraformingProjectEffect.ModifyIndustrySupplyWithDeficit(id, enableSettings, commoditiesDemanded, "flat", supplyBonus);
        }
    }

    public static class ModifyIndustryDemand implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modifierType = jsonData.getString("modifier_type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledTerraformingProjectEffect.ModifyIndustryDemand(id, enableSettings, modifierType, value);
        }
    }

    public static class EffectToIndustry implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String industryId = jsonData.getString("industry_id");
            String effectId = jsonData.getString("effect_id");
            BoggledTerraformingProjectEffect.TerraformingProjectEffect effect = boggledTools.getProjectEffect(effectId);
            return new BoggledTerraformingProjectEffect.EffectToIndustry(id, enableSettings, industryId, effect);
        }
    }

    public static class SuppressConditions implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray conditionIdsArray = jsonData.getJSONArray("condition_ids");
            List<String> conditionIds = new ArrayList<>();
            for (int i = 0; i < conditionIdsArray.length(); ++i) {
                conditionIds.add(conditionIdsArray.getString(i));
            }
            return new BoggledTerraformingProjectEffect.SuppressConditions(id, enableSettings, conditionIds);
        }
    }

    public static class IndustryMonthlyItemProduction implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            return new BoggledTerraformingProjectEffect.IndustryMonthlyItemProduction(id, enableSettings);
        }
    }

    public static class IndustryMonthlyItemProductionChance implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);
            List<BoggledCommonIndustry.ProductionData> productionData = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int priority = jsonObject.getInt("commodity_priority");
                String commodityId = jsonObject.getString("commodity_id");
                int chance = jsonObject.getInt("chance");
                JSONArray requirementsArray = jsonObject.optJSONArray("requirement_ids");
                BoggledProjectRequirementsAND reqs = boggledTools.requirementsFromRequirementsArray(requirementsArray, "MonthlyItemProductionChance", id, "MonthlyItemProductionChance");

                productionData.add(new BoggledCommonIndustry.ProductionData(priority, commodityId, chance, reqs));
            }
            return new BoggledTerraformingProjectEffect.IndustryMonthlyItemProductionChance(id, enableSettings, productionData);
        }
    }

    public static class IndustryMonthlyItemProductionChanceModifier implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);
            List<Pair<String, Integer>> productionData = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String commodityId = jsonObject.getString("commodity_id");
                int chanceModifier = jsonObject.getInt("chance_modifier");

                productionData.add(new Pair<>(commodityId, chanceModifier));
            }
            return new BoggledTerraformingProjectEffect.IndustryMonthlyItemProductionChanceModifier(id, enableSettings, productionData);
        }
    }

    public static class StepTag implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String tag = jsonData.getString("tag");
            int step = jsonData.getInt("step");
            return new BoggledTerraformingProjectEffect.StepTag(id, enableSettings, tag, step);
        }
    }

    public static class IndustryRemove implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String industryIdToRemove = jsonData.getString("industry_id");
            return new BoggledTerraformingProjectEffect.IndustryRemove(id, enableSettings, industryIdToRemove);
        }
    }

    public static class TagSubstringPowerModifyBuildCost implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String tag = jsonData.getString("tag");
            int tagDefault = jsonData.getInt("default");
            return new BoggledTerraformingProjectEffect.TagSubstringPowerModifyBuildCost(id, enableSettings, tag, tagDefault);
        }
    }

    public static class EliminatePatherInterest implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            return new BoggledTerraformingProjectEffect.EliminatePatherInterest(id, enableSettings);
        }
    }

    public static class AddStellarReflectorsToOrbit implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            return new BoggledTerraformingProjectEffect.AddStellarReflectorsToOrbit(id, enableSettings);
        }
    }

    public static class CommodityDemandFlat implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String commodityId = jsonData.getString("commodity_id");
            int quantity = jsonData.getInt("quantity");
            return new BoggledTerraformingProjectEffect.CommodityDemandFlat(id, enableSettings, commodityId, quantity);
        }
    }

    public static class CommodityDemandMarketSize implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String commodityId = jsonData.getString("commodity_id");
            int quantity = jsonData.getInt("quantity");
            return new BoggledTerraformingProjectEffect.CommodityDemandMarketSize(id, enableSettings, commodityId, quantity);
        }
    }

    public static class CommodityDemandPlayerMarketSizeElseFlat implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String commodityId = jsonData.getString("commodity_id");
            int quantity = jsonData.getInt("quantity");
            return new BoggledTerraformingProjectEffect.CommodityDemandPlayerMarketSizeElseFlat(id, enableSettings, commodityId, quantity);
        }
    }

    public static class CommoditySupplyFlat implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String commodityId = jsonData.getString("commodity_id");
            int quantity = jsonData.getInt("quantity");
            return new BoggledTerraformingProjectEffect.CommoditySupplyFlat(id, enableSettings, commodityId, quantity);
        }
    }

    public static class CommoditySupplyMarketSize implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String commodityId = jsonData.getString("commodity_id");
            int quantity = jsonData.getInt("quantity");
            return new BoggledTerraformingProjectEffect.CommoditySupplyMarketSize(id, enableSettings, commodityId, quantity);
        }
    }

    public static class CommodityDeficitToInactive implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray commoditiesDemandedArray = jsonData.getJSONArray("commodities_demanded");
            List<String> commoditiesDemanded = new ArrayList<>();
            for (int i = 0; i < commoditiesDemandedArray.length(); ++i) {
                commoditiesDemanded.add(commoditiesDemandedArray.getString(i));
            }
            return new BoggledTerraformingProjectEffect.CommodityDeficitToInactive(id, enableSettings, commoditiesDemanded);
        }
    }

    public static class CommodityDeficitToProduction implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray commoditiesDemandedArray = jsonData.getJSONArray("commodities_demanded");
            List<String> commoditiesDemanded = new ArrayList<>();
            for (int i = 0; i < commoditiesDemandedArray.length(); ++i) {
                commoditiesDemanded.add(commoditiesDemandedArray.getString(i));
            }
            JSONArray commoditiesDeficitedArray = jsonData.getJSONArray("commodities_deficited");
            List<String> commoditiesDeficited = new ArrayList<>();
            for (int i = 0; i < commoditiesDeficitedArray.length(); ++i) {
                commoditiesDeficited.add(commoditiesDeficitedArray.getString(i));
            }
            return new BoggledTerraformingProjectEffect.CommodityDeficitToProduction(id, enableSettings, commoditiesDemanded, commoditiesDeficited);
        }
    }

    public static class CommodityDeficitModifierToUpkeep implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray commoditiesDemandedArray = jsonData.getJSONArray("commodities_demanded");
            List<String> commoditiesDemanded = new ArrayList<>();
            for (int i = 0; i < commoditiesDemandedArray.length(); ++i) {
                commoditiesDemanded.add(commoditiesDemandedArray.getString(i));
            }
            String modifierType = jsonData.getString("modifier_type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledTerraformingProjectEffect.CommodityDeficitModifierToUpkeep(id, enableSettings, commoditiesDemanded, modifierType, value);
        }
    }
}
