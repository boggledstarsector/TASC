package data.scripts;

import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class BoggledTerraformingRequirementFactory {
    public interface TerraformingRequirementFactory {
        BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) throws JSONException;
    }

    public static class PlanetType implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
//            boggledTools.CheckPlanetTypeExists(data);
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
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            String[] industryAndItem = data.split(boggledTools.csvSubOptionSeparator);
            assert(industryAndItem.length == 2);
            boggledTools.CheckIndustryExists(industryAndItem[0]);
            boggledTools.CheckItemExists(industryAndItem[1]);
            return new BoggledTerraformingRequirement.MarketHasIndustryWithItem(requirementId, invert, industryAndItem[0], industryAndItem[1]);
        }
    }

    public static class MarketHasWaterPresent implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            String[] waterLevelStrings = data.split(boggledTools.csvSubOptionSeparator);
            assert(waterLevelStrings.length == 2);
            int[] waterLevels = new int[waterLevelStrings.length];
            for (int i = 0; i < waterLevels.length; ++i) {
                waterLevels[i] = Integer.parseInt(waterLevelStrings[i]);
            }
            return new BoggledTerraformingRequirement.MarketHasWaterPresent(requirementId, invert, waterLevels[0], waterLevels[1]);
        }
    }

    public static class TerraformingPossibleOnMarket implements TerraformingRequirementFactory {
        @Override
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            ArrayList<String> invalidatingConditions = new ArrayList<>(Arrays.asList(data.split(boggledTools.csvSubOptionSeparator)));

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
        public BoggledTerraformingRequirement.TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            String[] submarketIdAndcargoIdAndQuantityStrings = data.split(boggledTools.csvSubOptionSeparator);
            assert(submarketIdAndcargoIdAndQuantityStrings.length == 3);
            String submarketId = submarketIdAndcargoIdAndQuantityStrings[0];
            String commodityId = submarketIdAndcargoIdAndQuantityStrings[1];
            int quantity = Integer.parseInt(submarketIdAndcargoIdAndQuantityStrings[2]);

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
