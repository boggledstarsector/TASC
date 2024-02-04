package boggled.scripts;

import boggled.campaign.econ.boggledTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class BoggledTerraformingRequirementFactory {
    public interface TerraformingRequirementFactory {
        BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException;
    }

    public static class AlwaysTrue implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.AlwaysTrue(id, enableSettings, invert);
        }
    }

    public static class PlanetType implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) {
            return new BoggledTerraformingRequirement.PlanetType(id, enableSettings, invert, data);
        }
    }

    public static class FocusPlanetType implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) {
            boggledTools.CheckPlanetTypeExists(id, data);
            return new BoggledTerraformingRequirement.FocusPlanetType(id, enableSettings, invert, data);
        }
    }

    public static class MarketHasCondition implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) {
            boggledTools.CheckMarketConditionExists(id, data);
            return new BoggledTerraformingRequirement.MarketHasCondition(id, enableSettings, invert, data);
        }
    }

    public static class FocusMarketHasCondition implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) {
            boggledTools.CheckMarketConditionExists(id, data);
            return new BoggledTerraformingRequirement.FocusMarketHasCondition(id, enableSettings, invert, data);
        }
    }

    public static class MarketHasIndustry implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) {
            boggledTools.CheckIndustryExists(id, data);
            return new BoggledTerraformingRequirement.MarketHasIndustry(id, enableSettings, invert, data);
        }
    }

    public static class MarketHasIndustryWithItem implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            String industryId = jsonData.getString("industry_id");
            String itemId = jsonData.getString("item_id");

            boggledTools.CheckIndustryExists(id, industryId);
            boggledTools.CheckItemExists(id, itemId);

            return new BoggledTerraformingRequirement.MarketHasIndustryWithItem(id, enableSettings, invert, industryId, itemId);
        }
    }

    public static class MarketHasIndustryWithAICore implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            String industryId = jsonData.getString("industry_id");
            String aiCoreId = jsonData.getString("ai_core_id");

            boggledTools.CheckIndustryExists(id, industryId);
            boggledTools.CheckCommodityExists(id, aiCoreId);

            return new BoggledTerraformingRequirement.MarketHasIndustryWithAICore(id, enableSettings, invert, industryId, aiCoreId);
        }
    }

    public static class IndustryHasShortage implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray commodityIdsArray = jsonData.getJSONArray("commodity_ids");
            List<String> commodityIds = boggledTools.stringListFromJSON(commodityIdsArray);
            for (String commodityId : commodityIds) {
                boggledTools.CheckCommodityExists("IndustryHasShortage", commodityId);
            }
            return new BoggledTerraformingRequirement.IndustryHasShortage(id, enableSettings, invert, commodityIds);
        }
    }

    public static class PlanetWaterLevel implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            int minWaterLevel = jsonData.getInt("min_water_level");
            int maxWaterLevel = jsonData.getInt("max_water_level");

            return new BoggledTerraformingRequirement.PlanetWaterLevel(id, enableSettings, invert, minWaterLevel, maxWaterLevel);
        }
    }

    public static class MarketHasWaterPresent implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            int minWaterLevel = jsonData.getInt("min_water_level");
            int maxWaterLevel = jsonData.getInt("max_water_level");

            List<String> waterIndustryIds = boggledTools.stringListFromJSON(jsonData.getJSONArray("water_industry_ids"));

            return new BoggledTerraformingRequirement.MarketHasWaterPresent(id, enableSettings, invert, minWaterLevel, maxWaterLevel, waterIndustryIds);
        }
    }

    public static class TerraformingPossibleOnMarket implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);

            List<String> invalidatingConditions = boggledTools.stringListFromJSON(jsonArray);

            return new BoggledTerraformingRequirement.TerraformingPossibleOnMarket(id, enableSettings, invert, invalidatingConditions);
        }
    }

    public static class MarketHasTags implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);
            List<String> tags = boggledTools.stringListFromJSON(jsonArray);

            return new BoggledTerraformingRequirement.MarketHasTags(id, enableSettings, invert, tags);
        }
    }

    public static class MarketIsAtLeastSize implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) {
            int colonySize = Integer.parseInt(data);
            return new BoggledTerraformingRequirement.MarketIsAtLeastSize(id, enableSettings, invert, colonySize);
        }
    }

    public static class MarketStorageContainsAtLeast implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
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

            return new BoggledTerraformingRequirement.MarketStorageContainsAtLeast(id, enableSettings, invert, submarketId, itemType, itemId, settingId, quantity);
        }
    }

    public static class FleetStorageContainsAtLeast implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
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

            return new BoggledTerraformingRequirement.FleetStorageContainsAtLeast(id, enableSettings, invert, itemType, itemId, settingId, quantity);
        }
    }

    public static class FleetTooCloseToJumpPoint implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            float distance = Float.parseFloat(data);
            return new BoggledTerraformingRequirement.FleetTooCloseToJumpPoint(id, enableSettings, invert, distance);
        }
    }

    public static class PlayerHasStoryPointsAtLeast implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledTerraformingRequirement.PlayerHasStoryPointsAtLeast(id, enableSettings, invert, quantity);
        }
    }

    public static class WorldTypeSupportsResourceImprovement implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) {
            boggledTools.CheckResourceExists(id, data);
            return new BoggledTerraformingRequirement.WorldTypeSupportsResourceImprovement(id, enableSettings, invert, data);
        }
    }

    public static class IntegerFromTagSubstring implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            String option = jsonData.getString("option");
            String tagSubstring = jsonData.getString("tag_substring");
            int maxValue = jsonData.getInt("max_value");

            return new BoggledTerraformingRequirement.IntegerFromTagSubstring(id, enableSettings, invert, option, tagSubstring, maxValue);
        }
    }

    public static class PlayerHasSkill implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) {
            boggledTools.CheckSkillExists(data);
            return new BoggledTerraformingRequirement.PlayerHasSkill(id, enableSettings, invert, data);
        }
    }

    public static class SystemStarHasTags implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);
            List<String> tags = boggledTools.stringListFromJSON(jsonArray);

            return new BoggledTerraformingRequirement.SystemStarHasTags(id, enableSettings, invert, tags);
        }
    }

    public static class SystemStarType implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) {
            return new BoggledTerraformingRequirement.SystemStarType(id, enableSettings, invert, data);
        }
    }

    public static class FleetInHyperspace implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.FleetInHyperspace(id, enableSettings, invert);
        }
    }

    public static class SystemHasJumpPoints implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            int numJumpPoints = 1;
            if (!data.isEmpty()) {
                numJumpPoints = Integer.parseInt(data);
            }
            return new BoggledTerraformingRequirement.SystemHasJumpPoints(id, enableSettings, invert, numJumpPoints);
        }
    }

    public static class SystemHasPlanets implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            int numPlanets = 0;
            if (!data.isEmpty()) {
                numPlanets = Integer.parseInt(data);
            }
            return new BoggledTerraformingRequirement.SystemHasPlanets(id, enableSettings, invert, numPlanets);
        }
    }

    public static class TargetPlanetOwnedBy implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);
            List<String> factions = boggledTools.stringListFromJSON(jsonArray);

            return new BoggledTerraformingRequirement.TargetPlanetOwnedBy(id, enableSettings, invert, factions);
        }
    }

    public static class TargetStationOwnedBy implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);
            List<String> factions = boggledTools.stringListFromJSON(jsonArray);

            return new BoggledTerraformingRequirement.TargetStationOwnedBy(id, enableSettings, invert, factions);
        }
    }

    public static class TargetPlanetGovernedByPlayer implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetGovernedByPlayer(id, enableSettings, invert);
        }
    }

    public static class TargetPlanetWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetWithinDistance(id, enableSettings, invert, Float.parseFloat(data));
        }
    }

    public static class TargetStationWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetStationWithinDistance(id, enableSettings, invert, Float.parseFloat(data));
        }
    }

    public static class TargetStationColonizable implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetStationColonizable(id, enableSettings, invert);
        }
    }

    public static class TargetPlanetIsAtLeastSize implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetIsAtLeastSize(id, enableSettings, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetOrbitFocusWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetOrbitFocusWithinDistance(id, enableSettings, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetStarWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetStarWithinDistance(id, enableSettings, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetOrbitersWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetOrbitersWithinDistance(id, enableSettings, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetMoonCountLessThan implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetMoonCountLessThan(id, enableSettings, invert, Integer.parseInt(data));
        }
    }

    public static class TargetPlanetOrbitersTooClose implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetOrbitersTooClose(id, enableSettings, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetStationCountLessThan implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray stationTagsArray = jsonData.getJSONArray("station_tags");
            List<String> stationTags = boggledTools.stringListFromJSON(stationTagsArray);
            String settingId = jsonData.getString("setting_id");
            int maxNum = jsonData.getInt("max_num");

            return new BoggledTerraformingRequirement.TargetPlanetStationCountLessThan(id, enableSettings, invert, stationTags, settingId, maxNum);
        }
    }

    public static class TargetSystemStationCountLessThan implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray stationTagsArray = jsonData.getJSONArray("station_tags");
            List<String> stationTags = boggledTools.stringListFromJSON(stationTagsArray);
            int maxNum = jsonData.getInt("max_num");

            return new BoggledTerraformingRequirement.TargetSystemStationCountLessThan(id, enableSettings, invert, stationTags, maxNum);
        }
    }

    public static class FleetInAsteroidBelt implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.FleetInAsteroidBelt(id, enableSettings, invert);
        }
    }

    public static class FleetInAsteroidField implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.FleetInAsteroidField(id, enableSettings, invert);
        }
    }

    public static class TargetPlanetStoryCritical implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetStoryCritical(id, enableSettings, invert);
        }
    }

    public static class TargetStationStoryCritical implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetStationStoryCritical(id, enableSettings, invert);
        }
    }

    public static class BooleanSettingIsTrue implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String id, String[] enableSettings, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String settingId = jsonData.getString("setting_id");
            boolean invertSetting = jsonData.optBoolean("invert_setting", false);
            String requirementId = jsonData.getString("requirement_id");
            BoggledTerraformingRequirement.TerraformingRequirement req = boggledTools.getTerraformingRequirements().get(requirementId);
            return new BoggledTerraformingRequirement.BooleanSettingIsTrue(id, enableSettings, invert, settingId, invertSetting, req);
        }
    }
}
