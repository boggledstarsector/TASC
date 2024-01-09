package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;

import java.util.ArrayList;
import java.util.Map;

public class BoggledTerraformingRequirement {
    public abstract static class TerraformingRequirement {
        private final String requirementId;
        private final boolean invert;

        public String getRequirementId() { return requirementId; }

        protected TerraformingRequirement(String requirementId, boolean invert) {
            this.requirementId = requirementId;
            this.invert = invert;
        }

        public void addTokenReplacements(Map<String, String> tokenReplacements) {}

        protected abstract boolean checkRequirementImpl(MarketAPI market);

        public final boolean checkRequirement(MarketAPI market) {
            boolean ret = checkRequirementImpl(market);
            if (invert) {
                ret = !ret;
            }
            return ret;
        }
    }

    public static class AlwaysTrue extends TerraformingRequirement {
        public AlwaysTrue(String requirementId, boolean invert) {
            super(requirementId, invert);
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            return true;
        }
    }

    public static class PlanetType extends TerraformingRequirement {
        String planetTypeId;

        public PlanetType(String requirementId, boolean invert, String planetTypeId) {
            super(requirementId, invert);
            this.planetTypeId = planetTypeId;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            return planetTypeId.equals(boggledTools.getPlanetType(market.getPlanetEntity()).getPlanetId());
        }
    }

    public static class FocusPlanetType extends PlanetType {
        public FocusPlanetType(String requirementId, boolean invert, String planetTypeId) {
            super(requirementId, invert, planetTypeId);
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            if (market.getPrimaryEntity().getOrbitFocus().getMarket() == null) {
                return false;
            }
            return super.checkRequirementImpl(market.getPrimaryEntity().getOrbitFocus().getMarket());
        }
    }

    public static class MarketHasCondition extends TerraformingRequirement {
        String conditionId;
        public MarketHasCondition(String requirementId, boolean invert, String conditionId) {
            super(requirementId, invert);
            this.conditionId = conditionId;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            return market.hasCondition(conditionId);
        }
    }

    public static class FocusMarketHasCondition extends MarketHasCondition {
        public FocusMarketHasCondition(String requirementId, boolean invert, String conditionId) {
            super(requirementId, invert, conditionId);
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            if (market.getPrimaryEntity().getOrbitFocus().getMarket() == null) {
                return false;
            }
            return super.checkRequirementImpl(market.getPrimaryEntity().getOrbitFocus().getMarket());
        }
    }

    public static class MarketHasIndustry extends TerraformingRequirement {
        String industryId;
        public MarketHasIndustry(String requirementId, boolean invert, String industryId) {
            super(requirementId, invert);
            this.industryId = industryId;
        }

        @Override
        public void addTokenReplacements(Map<String, String> tokenReplacements) {
            tokenReplacements.put("$industry", Global.getSettings().getIndustrySpec(industryId).getName());
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            Industry industry = market.getIndustry(industryId);
            return industry != null && industry.isFunctional() && market.hasIndustry(industryId);
        }
    }

    public static class MarketHasIndustryWithItem extends MarketHasIndustry {
        String itemId;
        public MarketHasIndustryWithItem(String requirementId, boolean invert, String industryId, String itemId) {
            super(requirementId, invert, industryId);
            this.itemId = itemId;
        }

        @Override
        public void addTokenReplacements(Map<String, String> tokenReplacements) {
            super.addTokenReplacements(tokenReplacements);
            tokenReplacements.put("$item", Global.getSettings().getSpecialItemSpec(itemId).getName());
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            if (!super.checkRequirementImpl(market)) {
                return false;
            }
            Industry industry = market.getIndustry(industryId);
            for (SpecialItemData specialItemData : industry.getVisibleInstalledItems()) {
                if (itemId.equals(specialItemData.getId())) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class PlanetWaterLevel extends TerraformingRequirement {
        int minWaterLevel;
        int maxWaterLevel;
        protected PlanetWaterLevel(String requirementId, boolean invert, int minWaterLevel, int maxWaterLevel) {
            super(requirementId, invert);
            this.minWaterLevel = minWaterLevel;
            this.maxWaterLevel = maxWaterLevel;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            PlanetAPI planet = market.getPlanetEntity();
            boggledTools.PlanetType planetType = boggledTools.getPlanetType(planet);
            int planetWaterLevel = planetType.getWaterLevel(market);
            return minWaterLevel <= planetWaterLevel && planetWaterLevel <= maxWaterLevel;
        }
    }

    public static class MarketHasWaterPresent extends PlanetWaterLevel {
        public MarketHasWaterPresent(String requirementId, boolean invert, int minWaterLevel, int maxWaterLevel) {
            super(requirementId, invert, minWaterLevel, maxWaterLevel);
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            return super.checkRequirementImpl(market) || boggledTools.hasIsmaraSling(market);
        }
    }

    public static class TerraformingPossibleOnMarket extends TerraformingRequirement {
        ArrayList<String> invalidatingConditions;
        public TerraformingPossibleOnMarket(String requirementId, boolean invert, ArrayList<String> invalidatingConditions) {
            super(requirementId, invert);
            this.invalidatingConditions = invalidatingConditions;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            for (String invalidatingCondition : invalidatingConditions) {
                if (market.hasCondition(invalidatingCondition)) {
                    return false;
                }
            }
            return boggledTools.getPlanetType(market.getPlanetEntity()).getTerraformingPossible();
        }
    }

    public static class MarketHasTags extends TerraformingRequirement {
        ArrayList<String> tags;
        public MarketHasTags(String requirementId, boolean invert, ArrayList<String> tags) {
            super(requirementId, invert);
            this.tags = tags;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            for (String tag : tags) {
                if (!market.hasTag(tag) && !market.getPrimaryEntity().hasTag(tag)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class MarketIsAtLeastSize extends TerraformingRequirement {
        int colonySize;
        public MarketIsAtLeastSize(String requirementId, boolean invert, int colonySize) {
            super(requirementId, invert);
            this.colonySize = colonySize;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            return market.getSize() >= colonySize;
        }
    }

    public static class MarketStorageContainsAtLeast extends TerraformingRequirement {
        String submarketId;
        String cargoId;
        int quantity;
        public MarketStorageContainsAtLeast(String requirementId, boolean invert, String submarketId, String cargoId, int quantity) {
            super(requirementId, invert);
            this.submarketId = submarketId;
            this.cargoId = cargoId;
            this.quantity = quantity;
        }

        @Override
        public void addTokenReplacements(Map<String, String> tokenReplacements) {
            for (SubmarketSpecAPI submarketSpec : Global.getSettings().getAllSubmarketSpecs()) {
                if (!submarketSpec.getId().equals(submarketId)) {
                    continue;
                }
                tokenReplacements.put("$submarket", Misc.lcFirst(submarketSpec.getName()));
                break;
            }
            tokenReplacements.put("$cargoName", Global.getSettings().getCommoditySpec(cargoId).getLowerCaseName());
            tokenReplacements.put("$cargoQuantity", Integer.toString(quantity));
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            return market.getSubmarket(submarketId).getCargo().getCommodityQuantity(cargoId) >= quantity;
        }
    }

    public static class PlayerHasStoryPointsAtLeast extends TerraformingRequirement {
        int quantity;
        public PlayerHasStoryPointsAtLeast(String requirementId, boolean invert, int quantity) {
            super(requirementId, invert);
            this.quantity = quantity;
        }

        @Override
        public void addTokenReplacements(Map<String, String> tokenReplacements) {
            tokenReplacements.put("$storyPointsQuantity", Integer.toString(quantity));
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            return Global.getSector().getPlayerStats().getStoryPoints() >= quantity;
        }
    }

    public static class WorldTypeSupportsResourceImprovement extends TerraformingRequirement {
        String resourceId;
        public WorldTypeSupportsResourceImprovement(String requirementId, boolean invert, String resourceId) {
            super(requirementId, invert);
            this.resourceId = resourceId;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            Pair<String, String> key = new Pair<>(boggledTools.getPlanetType(market.getPlanetEntity()).getPlanetId(), resourceId);
            String maxResource = boggledTools.getResourceLimits().get(key);

            if (maxResource == null || maxResource.isEmpty()) {
                return false;
            }

            if (market.hasCondition(maxResource)) {
                return false;
            }

            ArrayList<String> resourceProgression = boggledTools.getResourceProgressions().get(resourceId);

            boolean maxResourcePassed = false;
            for (String resource : resourceProgression) {
                if (resource.equals(maxResource)) {
                    maxResourcePassed = true;
                }
                if (market.hasCondition(resource) && maxResourcePassed) {
                    return false;
                }
            }

            return true;
        }
    }

    public static class IntegerFromTagSubstring extends TerraformingRequirement {
        String option;
        String tagSubstring;
        int maxValue;

        public IntegerFromTagSubstring(String requirementId, boolean invert, String option, String tagSubstring, int maxValue) {
            super(requirementId, invert);
            this.tagSubstring = tagSubstring;
            this.maxValue = maxValue;
            this.option = option;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            int testValue = 0;
            if (!option.isEmpty()) {
                testValue += boggledTools.getIntSetting(option);
            }
            for (String tag : market.getTags()) {
                if (tag.contains(tagSubstring)) {
                    testValue += Integer.parseInt(tag.substring(tagSubstring.length()));
                    break;
                }
            }
            return maxValue > testValue;
        }
    }

    public static class PlayerHasSkill extends TerraformingRequirement {
        String skill;
        public PlayerHasSkill(String requirementId, boolean invert, String skill) {
            super(requirementId, invert);
            this.skill = skill;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            return Global.getSector().getPlayerStats().getSkillLevel(skill) != 0;
        }
    }

    public static class SystemStarHasTags extends TerraformingRequirement {
        ArrayList<String> tags;
        public SystemStarHasTags(String requirementId, boolean invert, ArrayList<String> tags) {
            super(requirementId, invert);
            this.tags = tags;
        }

        private boolean starHasTag(PlanetAPI star, String tag) {
            return star != null && star.hasTag(tag);
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            StarSystemAPI system = market.getStarSystem();
            PlanetAPI primary = system.getStar();
            PlanetAPI secondary = system.getSecondary();
            PlanetAPI tertiary = system.getTertiary();
            for (String tag : tags) {
                boolean hasTag = starHasTag(primary, tag) || starHasTag(secondary, tag) || starHasTag(tertiary, tag);
                if (!hasTag) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class SystemStarType extends TerraformingRequirement {
        String starType;
        public SystemStarType(String requirementId, boolean invert, String starType) {
            super(requirementId, invert);
            this.starType = starType;
        }

        private boolean starEquals(PlanetAPI star) {
            return star != null && star.getTypeId().equals(starType);
        }
        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            StarSystemAPI system = market.getStarSystem();
            PlanetAPI primary = system.getStar();
            PlanetAPI secondary = system.getSecondary();
            PlanetAPI tertiary = system.getTertiary();

            if (starEquals(primary)) {
                return true;
            }
            else if (starEquals(secondary)) {
                return true;
            }
            return starEquals(tertiary);
        }
    }
}
