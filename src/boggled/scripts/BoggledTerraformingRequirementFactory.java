package boggled.scripts;

import boggled.campaign.econ.boggledTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoggledTerraformingRequirementFactory {
    public interface TerraformingRequirementFactory {
        BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException;
    }

    public static class AlwaysTrue implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.AlwaysTrue(requirementId, invert);
        }
    }

    public static class PlanetType implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            return new BoggledTerraformingRequirement.PlanetType(requirementId, invert, data);
        }
    }

    public static class FocusPlanetType implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            boggledTools.CheckPlanetTypeExists(data);
            return new BoggledTerraformingRequirement.FocusPlanetType(requirementId, invert, data);
        }
    }

    public static class MarketHasCondition implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            boggledTools.CheckMarketConditionExists(data);
            return new BoggledTerraformingRequirement.MarketHasCondition(requirementId, invert, data);
        }
    }

    public static class FocusMarketHasCondition implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            boggledTools.CheckMarketConditionExists(data);
            return new BoggledTerraformingRequirement.FocusMarketHasCondition(requirementId, invert, data);
        }
    }

    public static class MarketHasIndustry implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            boggledTools.CheckIndustryExists(data);
            return new BoggledTerraformingRequirement.MarketHasIndustry(requirementId, invert, data);
        }
    }

    public static class MarketHasIndustryWithItem implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            String industryId = jsonData.getString("industry_id");
            String itemId = jsonData.getString("item_id");

            boggledTools.CheckIndustryExists(industryId);
            boggledTools.CheckItemExists(itemId);

            return new BoggledTerraformingRequirement.MarketHasIndustryWithItem(requirementId, invert, industryId, itemId);
        }
    }

    public static class MarketHasIndustryWithAICore implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            String industryId = jsonData.getString("industry_id");
            String aiCoreId = jsonData.getString("ai_core_id");

            boggledTools.CheckIndustryExists(industryId);
            boggledTools.CheckCommodityExists(aiCoreId);

            return new BoggledTerraformingRequirement.MarketHasIndustryWithAICore(requirementId, invert, industryId, aiCoreId);
        }
    }

    public static class PlanetWaterLevel implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            int minWaterLevel = jsonData.getInt("min_water_level");
            int maxWaterLevel = jsonData.getInt("max_water_level");

            return new BoggledTerraformingRequirement.PlanetWaterLevel(requirementId, invert, minWaterLevel, maxWaterLevel);
        }
    }

    public static class MarketHasWaterPresent implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            int minWaterLevel = jsonData.getInt("min_water_level");
            int maxWaterLevel = jsonData.getInt("max_water_level");

            return new BoggledTerraformingRequirement.MarketHasWaterPresent(requirementId, invert, minWaterLevel, maxWaterLevel);
        }
    }

    public static class TerraformingPossibleOnMarket implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);

            ArrayList<String> invalidatingConditions = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); ++i) {
                invalidatingConditions.add(jsonArray.getString(i));
            }

            return new BoggledTerraformingRequirement.TerraformingPossibleOnMarket(requirementId, invert, invalidatingConditions);
        }
    }

    public static class MarketHasTags implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            ArrayList<String> tags = new ArrayList<>(Arrays.asList(data.split(boggledTools.csvOptionSeparator)));

            return new BoggledTerraformingRequirement.MarketHasTags(requirementId, invert, tags);
        }
    }

    public static class MarketIsAtLeastSize implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            int colonySize = Integer.parseInt(data);
            return new BoggledTerraformingRequirement.MarketIsAtLeastSize(requirementId, invert, colonySize);
        }
    }

    public static class MarketStorageContainsAtLeast implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            String submarketId = jsonData.getString("submarket_id");
            String commodityId = jsonData.getString("commodity_id");
            int quantity = jsonData.getInt("quantity");

            boggledTools.CheckSubmarketExists(submarketId);
            boggledTools.CheckCommodityExists(commodityId);

            return new BoggledTerraformingRequirement.MarketStorageContainsAtLeast(requirementId, invert, submarketId, commodityId, quantity);
        }
    }

    public static class FleetStorageContainsAtLeast implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String commodityId = jsonData.getString("commodity_id");
            int quantity = jsonData.getInt("quantity");
            return new BoggledTerraformingRequirement.FleetStorageContainsAtLeast(requirementId, invert, commodityId, quantity);
        }
    }

    public static class FleetContainsCreditsAtLeast implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            int quantity = jsonData.getInt("quantity");
            return new BoggledTerraformingRequirement.FleetContainsCreditsAtLeast(requirementId, invert, quantity);
        }
    }

    public static class FleetTooCloseToJumpPoint implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            float distance = Float.parseFloat(data);
            return new BoggledTerraformingRequirement.FleetTooCloseToJumpPoint(requirementId, invert, distance);
        }
    }

    public static class PlayerHasStoryPointsAtLeast implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledTerraformingRequirement.PlayerHasStoryPointsAtLeast(requirementId, invert, quantity);
        }
    }

    public static class WorldTypeSupportsResourceImprovement implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            boggledTools.CheckResourceExists(data);
            return new BoggledTerraformingRequirement.WorldTypeSupportsResourceImprovement(requirementId, invert, data);
        }
    }

    public static class IntegerFromTagSubstring implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            String option = jsonData.getString("option");
            String tagSubstring = jsonData.getString("tag_substring");
            int maxValue = jsonData.getInt("max_value");

            return new BoggledTerraformingRequirement.IntegerFromTagSubstring(requirementId, invert, option, tagSubstring, maxValue);
        }
    }

    public static class PlayerHasSkill implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            boggledTools.CheckSkillExists(data);
            return new BoggledTerraformingRequirement.PlayerHasSkill(requirementId, invert, data);
        }
    }

    public static class SystemStarHasTags implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            ArrayList<String> tags = new ArrayList<>(Arrays.asList(data.split(boggledTools.csvOptionSeparator)));

            return new BoggledTerraformingRequirement.SystemStarHasTags(requirementId, invert, tags);
        }
    }

    public static class SystemStarType implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            return new BoggledTerraformingRequirement.SystemStarType(requirementId, invert, data);
        }
    }

    public static class FleetInHyperspace implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.FleetInHyperspace(requirementId, invert);
        }
    }

    public static class SystemHasJumpPoints implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            int numJumpPoints = 1;
            if (!data.isEmpty()) {
                numJumpPoints = Integer.parseInt(data);
            }
            return new BoggledTerraformingRequirement.SystemHasJumpPoints(requirementId, invert, numJumpPoints);
        }
    }

    public static class SystemHasPlanets implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            int numPlanets = 0;
            if (!data.isEmpty()) {
                numPlanets = Integer.parseInt(data);
            }
            return new BoggledTerraformingRequirement.SystemHasPlanets(requirementId, invert, numPlanets);
        }
    }

    public static class TargetPlanetNotOwnedBy implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            JSONArray jsonData = new JSONArray(data);
            List<String> factions = new ArrayList<>();
            for (int i = 0; i < jsonData.length(); ++i) {
                factions.add(jsonData.getString(i));
            }
            return new BoggledTerraformingRequirement.TargetPlanetNotOwnedBy(requirementId, invert, factions);
        }
    }

    public static class TargetPlanetGovernedByPlayer implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetGovernedByPlayer(requirementId, invert);
        }
    }

    public static class TargetPlanetWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetWithinDistance(requirementId, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetIsAtLeastSize implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetIsAtLeastSize(requirementId, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetOrbitFocusWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetOrbitFocusWithinDistance(requirementId, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetStarWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetStarWithinDistance(requirementId, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetOrbitersWithinDistance implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetOrbitersWithinDistance(requirementId, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetMoonCountLessThan implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetMoonCountLessThan(requirementId, invert, Integer.parseInt(data));
        }
    }

    public static class TargetPlanetOrbitersTooClose implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.TargetPlanetOrbitersTooClose(requirementId, invert, Float.parseFloat(data));
        }
    }

    public static class TargetPlanetStationCountLessThan implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray stationTagsArray = jsonData.getJSONArray("station_tags");
            List<String> stationTags = new ArrayList<>();
            for (int i = 0; i < stationTagsArray.length(); ++i) {
                stationTags.add(stationTagsArray.getString(i));
            }
            int maxNum = jsonData.getInt("max_num");
            return new BoggledTerraformingRequirement.TargetPlanetStationCountLessThan(requirementId, invert, stationTags, maxNum);
        }
    }

    public static class TargetSystemStationCountLessThan implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray stationTagsArray = jsonData.getJSONArray("station_tags");
            List<String> stationTags = new ArrayList<>();
            for (int i = 0; i < stationTagsArray.length(); ++i) {
                stationTags.add(stationTagsArray.getString(i));
            }
            int maxNum = jsonData.getInt("max_num");
            return new BoggledTerraformingRequirement.TargetSystemStationCountLessThan(requirementId, invert, stationTags, maxNum);
        }
    }

    public static class FleetInAsteroidBelt implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.FleetInAsteroidBelt(requirementId, invert);
        }
    }

    public static class FleetInAsteroidField implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException {
            return new BoggledTerraformingRequirement.FleetInAsteroidField(requirementId, invert);
        }
    }
}
