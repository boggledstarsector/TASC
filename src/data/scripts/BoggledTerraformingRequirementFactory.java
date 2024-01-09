package data.scripts;

import data.campaign.econ.boggledTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

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
}
