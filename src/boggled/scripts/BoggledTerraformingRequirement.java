package boggled.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketSpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import boggled.campaign.econ.boggledTools;
import boggled.campaign.econ.industries.BoggledCommonIndustry;
import boggled.campaign.econ.industries.BoggledIndustryInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BoggledTerraformingRequirement {
    public static class RequirementContext {
        private final String contextName;
        private final BaseIndustry sourceIndustry;
        private final BoggledIndustryInterface sourceIndustryInterface;
        private final BaseIndustry targetIndustry;
        private final BoggledIndustryInterface targetIndustryInterface;
        private MarketAPI closestMarket;
        private MarketAPI planetMarket;
        private MarketAPI stationMarket;
        private PlanetAPI planet;
        private SectorEntityToken station;
        private StarSystemAPI starSystem;
        private final CampaignFleetAPI fleet;
        private final BoggledTerraformingProject project;

        public RequirementContext(RequirementContext that, BoggledTerraformingProject project) {
            this.contextName = that.contextName;
            this.sourceIndustry = that.sourceIndustry;
            this.sourceIndustryInterface = that.sourceIndustryInterface;
            this.targetIndustry = that.targetIndustry;
            this.targetIndustryInterface = that.targetIndustryInterface;
            this.closestMarket = that.closestMarket;
            this.planetMarket = that.planetMarket;
            this.stationMarket = that.stationMarket;
            this.planet = that.planet;
            this.station = that.station;
            this.starSystem = that.starSystem;
            this.fleet = that.fleet;
            this.project = project;
        }

        public RequirementContext(BaseIndustry sourceIndustry, BoggledTerraformingProject project) {
            this.contextName = "Industry " + sourceIndustry.getCurrentName();
            this.sourceIndustry = sourceIndustry;
            this.sourceIndustryInterface = (sourceIndustry instanceof BoggledIndustryInterface) ? (BoggledIndustryInterface) sourceIndustry : null;
            this.targetIndustry = this.sourceIndustry;
            this.targetIndustryInterface = this.sourceIndustryInterface;
            this.closestMarket = sourceIndustry.getMarket();

            if (this.closestMarket.getPrimaryEntity().hasTag(Tags.STATION)) {
                this.station = this.closestMarket.getPrimaryEntity();
                this.stationMarket = this.closestMarket;
                this.planet = this.closestMarket.getPlanetEntity();
                if (this.planet != null) {
                    this.planetMarket = this.planet.getMarket();
                }
            } else {
                this.planet = this.closestMarket.getPlanetEntity();
                this.planetMarket = this.closestMarket;
            }
            this.starSystem = this.closestMarket.getStarSystem();
            this.fleet = Global.getSector().getPlayerFleet();
            this.project = project;
        }

        public RequirementContext(RequirementContext that, BaseIndustry targetIndustry) {
            this.contextName = that.contextName;
            this.sourceIndustry = that.sourceIndustry;
            this.sourceIndustryInterface = that.sourceIndustryInterface;
            this.targetIndustry = targetIndustry;
            this.targetIndustryInterface = (targetIndustry instanceof BoggledIndustryInterface) ? (BoggledIndustryInterface) targetIndustry : null;
            this.closestMarket = that.closestMarket;
            this.planetMarket = that.planetMarket;
            this.stationMarket = that.stationMarket;
            this.planet = that.planet;
            this.station = that.station;
            this.starSystem = that.starSystem;
            this.fleet = that.fleet;
            this.project = that.project;
        }

        public RequirementContext(MarketAPI market, BoggledTerraformingProject project) {
            this.sourceIndustry = null;
            this.sourceIndustryInterface = null;
            this.targetIndustry = null;
            this.targetIndustryInterface = null;
            this.fleet = Global.getSector().getPlayerFleet();

            this.closestMarket = market;
            if (market == null) {
                this.contextName = "Market null";
                this.planetMarket = null;
                this.stationMarket = null;
                this.planet = null;
                this.station = null;
            } else {
                this.contextName = "Market " + market.getName();

                if (this.closestMarket.hasTag("station") || this.closestMarket.getPrimaryEntity().hasTag("station")) {
                    this.planetMarket = null;
                    this.planet = null;
                    this.stationMarket = this.closestMarket;
                    this.station = this.stationMarket.getPrimaryEntity();
                } else {
                    this.planetMarket = this.closestMarket;
                    this.planet = this.planetMarket.getPlanetEntity();
                    this.stationMarket = null;
                    this.station = null;
                }
            }
            this.starSystem = this.fleet.getStarSystem();
            this.project = project;
        }

        public RequirementContext(CampaignFleetAPI fleet) {
            this.contextName = "Fleet " + fleet.getName();
            this.sourceIndustry = null;
            this.sourceIndustryInterface = null;
            this.targetIndustry = null;
            this.targetIndustryInterface = null;
            this.fleet = fleet;
            this.planet = boggledTools.getClosestPlanetToken(fleet);
            this.closestMarket = boggledTools.getClosestMarketToEntity(fleet);
            this.starSystem = this.fleet.getStarSystem();
            if (this.planet != null) {
                this.planetMarket = this.planet.getMarket();
            } else {
                this.planetMarket = null;
            }

            if (this.closestMarket != null && this.closestMarket.getPrimaryEntity().hasTag(Tags.STATION)) {
                this.stationMarket = this.closestMarket;
                this.station = this.closestMarket.getPrimaryEntity();
            } else {
                this.stationMarket = null;
                this.station = null;
            }
            this.project = null;
        }

        public RequirementContext getFocusContext() {
            return new RequirementContext(BoggledCommonIndustry.getFocusMarketOrMarket(this.getClosestMarket()), this.project);
        }

        public void updatePlanet() {
            this.planet = boggledTools.getClosestPlanetToken(fleet);
            this.station = boggledTools.getClosestStationInSystem(fleet);
            this.starSystem = fleet.getStarSystem();
            if (this.planet != null) {
                this.planetMarket = this.planet.getMarket();
            } else {
                this.planetMarket = null;
            }
            if (this.station != null) {
                this.stationMarket = this.station.getMarket();
            } else {
                this.stationMarket = null;
            }

            if (this.planetMarket != null) {
                this.closestMarket = this.planetMarket;
            }
            if (this.stationMarket != null) {
                this.closestMarket = this.stationMarket;
            }
            if (this.planetMarket != null && this.stationMarket != null) {
                float stationDistanceToFleet = Misc.getDistance(station, fleet);
                float planetDistanceToFleet = Misc.getDistance(planet, fleet);
                if (stationDistanceToFleet < planetDistanceToFleet) {
                    this.closestMarket = this.stationMarket;
                } else {
                    this.closestMarket = this.planetMarket;
                }
            }
        }

        public String getName() { return contextName; }
        public BaseIndustry getSourceIndustry() { return sourceIndustry; }
        public BoggledIndustryInterface getSourceIndustryInterface() { return sourceIndustryInterface; }
        public BaseIndustry getTargetIndustry() { return targetIndustry; }
        public BoggledIndustryInterface getTargetIndustryInterface() { return targetIndustryInterface; }
        public MarketAPI getClosestMarket() { return closestMarket; }
        public MarketAPI getPlanetMarket() { return planetMarket; }
        public MarketAPI getStationMarket() { return stationMarket; }
        public PlanetAPI getPlanet() { return planet; }
        public SectorEntityToken getStation() { return station; }
        public StarSystemAPI getStarSystem() { return starSystem; }
        public CampaignFleetAPI getFleet() { return fleet; }
        public BoggledTerraformingProject getProject() { return project; }
    }

    public abstract static class TerraformingRequirement {
        private final String id;
        private final boolean invert;

        private final String[] enableSettings;

        public String getId() { return id; }

        protected TerraformingRequirement(String id, String[] enableSettings, boolean invert) {
            this.id = id;
            this.invert = invert;
            this.enableSettings = enableSettings;
        }

        public boolean isEnabled() {
            return boggledTools.optionsAllowThis(enableSettings);
        }

        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {}

        protected abstract boolean checkRequirementImpl(RequirementContext ctx);

        public final boolean checkRequirement(RequirementContext ctx) {
            boolean ret = checkRequirementImpl(ctx);
            if (invert) {
                ret = !ret;
            }
            return ret;
        }
    }

    public static abstract class ItemRequirement extends TerraformingRequirement {
        public enum ItemType {
            CREDITS,
            RESOURCES,
            SPECIAL
        }
        ItemType itemType;
        String itemId;
        String settingId;
        int quantity;

        protected ItemRequirement(String id, String[] enableSettings, boolean invert, ItemType itemType, String itemId, String settingId, int quantity) {
            super(id, enableSettings, invert);
            this.itemType = itemType;
            this.itemId = itemId;
            this.settingId = settingId;
            this.quantity = quantity;
        }

        protected final boolean checkCargoHasItem(CargoAPI cargo) {
            int quantityToCheck = quantity;
            if (!settingId.isEmpty()) {
                quantityToCheck = boggledTools.getIntSetting(settingId);
            }
            switch (itemType) {
                case CREDITS: return cargo.getCredits().get() >= quantityToCheck;
                case RESOURCES: return cargo.getQuantity(CargoAPI.CargoItemType.RESOURCES, itemId) >= quantityToCheck;
                case SPECIAL: return cargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(itemId, null)) >= quantityToCheck;
            }
            return false;
        }

        protected void addTokenReplacements(CargoAPI cargo, RequirementContext ctx, Map<String, String> tokenReplacements) {
            switch (itemType) {
                case CREDITS:
                    tokenReplacements.put("$itemName", "credits");
                    tokenReplacements.put("$ItemName", "Credits");
                    tokenReplacements.put("$currentItemQuantity", String.format("%,d", (int) cargo.getCredits().get()));
                    break;
                case RESOURCES:
                    tokenReplacements.put("$itemName", Global.getSettings().getCommoditySpec(itemId).getName().toLowerCase());
                    tokenReplacements.put("$ItemName", Global.getSettings().getCommoditySpec(itemId).getName());
                    tokenReplacements.put("$currentItemQuantity", String.format("%,d", (int) cargo.getQuantity(CargoAPI.CargoItemType.RESOURCES, itemId)));
                    break;
                case SPECIAL:
                    tokenReplacements.put("$itemName", Global.getSettings().getSpecialItemSpec(itemId).getName().toLowerCase());
                    tokenReplacements.put("$ItemName", Global.getSettings().getSpecialItemSpec(itemId).getName().toLowerCase());
                    tokenReplacements.put("$currentItemQuantity", String.format("%,d", (int) cargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(itemId, null))));
                    break;
            }
            int quantityToCheck = quantity;
            if (!settingId.isEmpty()) {
                quantityToCheck = boggledTools.getIntSetting(settingId);
            }
            tokenReplacements.put("$itemQuantity", Integer.toString(quantityToCheck));
        }
    }

    public static class AlwaysTrue extends TerraformingRequirement {
        public AlwaysTrue(String id, String[] enableSettings, boolean invert) {
            super(id, enableSettings, invert);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            return true;
        }
    }

    public static class PlanetType extends TerraformingRequirement {
        String planetTypeId;

        public PlanetType(String id, String[] enableSettings, boolean invert, String planetTypeId) {
            super(id, enableSettings, invert);
            this.planetTypeId = planetTypeId;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            if (ctx.getPlanet() == null) {
                return;
            }
            tokenReplacements.put("$planetType", boggledTools.getPlanetType(ctx.getPlanet()).getPlanetTypeName());
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            if (ctx.getPlanet() == null) {
                return false;
            }
            return planetTypeId.equals(boggledTools.getPlanetType(ctx.getPlanet()).getPlanetId());
        }
    }

    public static class FocusPlanetType extends PlanetType {
        public FocusPlanetType(String id, String[] enableSettings, boolean invert, String planetTypeId) {
            super(id, enableSettings, invert, planetTypeId);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return false;
            }
            MarketAPI focusMarket = market.getPrimaryEntity().getOrbitFocus().getMarket();
            if (focusMarket == null) {
                return false;
            }
            return super.checkRequirementImpl(new RequirementContext(focusMarket, ctx.getProject()));
        }
    }

    public static class MarketHasCondition extends TerraformingRequirement {
        String conditionId;
        public MarketHasCondition(String id, String[] enableSettings, boolean invert, String conditionId) {
            super(id, enableSettings, invert);
            this.conditionId = conditionId;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return false;
            }
            return market.hasCondition(conditionId);
        }
    }

    public static class FocusMarketHasCondition extends MarketHasCondition {
        public FocusMarketHasCondition(String id, String[] enableSettings, boolean invert, String conditionId) {
            super(id, enableSettings, invert, conditionId);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return false;
            }
            MarketAPI focusMarket = market.getPrimaryEntity().getOrbitFocus().getMarket();
            if (focusMarket == null) {
                return false;
            }
            return super.checkRequirementImpl(new RequirementContext(focusMarket, ctx.getProject()));
        }
    }

    public static class MarketHasIndustry extends TerraformingRequirement {
        String industryId;
        public MarketHasIndustry(String id, String[] enableSettings, boolean invert, String industryId) {
            super(id, enableSettings, invert);
            this.industryId = industryId;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            String industryName = Global.getSettings().getIndustrySpec(industryId).getName();
            tokenReplacements.put("$industry", industryName.toLowerCase());
            tokenReplacements.put("$Industry", industryName);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return false;
            }
            Industry industry = market.getIndustry(industryId);
            return industry != null && industry.isFunctional() && market.hasIndustry(industryId);
        }
    }

    public static class MarketHasIndustryWithItem extends MarketHasIndustry {
        String itemId;
        public MarketHasIndustryWithItem(String id, String[] enableSettings, boolean invert, String industryId, String itemId) {
            super(id, enableSettings, invert, industryId);
            this.itemId = itemId;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            super.addTokenReplacements(ctx, tokenReplacements);
            String industryItem = Global.getSettings().getSpecialItemSpec(itemId).getName();
            tokenReplacements.put("$industryItem", industryItem.toLowerCase());
            tokenReplacements.put("$IndustryItem", industryItem);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            if (!super.checkRequirementImpl(ctx)) {
                return false;
            }
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
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

    public static class MarketHasIndustryWithAICore extends MarketHasIndustry {
        String aiCoreId;
        public MarketHasIndustryWithAICore(String id, String[] enableSettings, boolean invert, String industryId, String aiCoreId) {
            super(id, enableSettings, invert, industryId);
            this.aiCoreId = aiCoreId;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            super.addTokenReplacements(ctx, tokenReplacements);
            tokenReplacements.put("$aiCore", Global.getSettings().getCommoditySpec(aiCoreId).getName());
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            if (!super.checkRequirementImpl(ctx)) {
                return false;
            }
            MarketAPI market = ctx.getClosestMarket();
            Industry industry = market.getIndustry(industryId);
            return industry.getAICoreId() != null && industry.getAICoreId().equals(aiCoreId);
        }
    }

    public static class IndustryHasShortage extends TerraformingRequirement {
        List<String> commodityIds;
        protected IndustryHasShortage(String id, String[] enableSettings, boolean invert, List<String> commodityIds) {
            super(id, enableSettings, invert);
            this.commodityIds = commodityIds;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            BaseIndustry sourceIndustry = ctx.getSourceIndustry();;
            if (sourceIndustry == null) {
                return false;
            }
            return !sourceIndustry.getAllDeficit(commodityIds.toArray(new String[0])).isEmpty();
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            List<String> commodities = new ArrayList<>();
            for (String commodityId : commodityIds) {
                commodities.add(Global.getSettings().getCommoditySpec(commodityId).getName());
            }
            tokenReplacements.put("$commodityDeficit", Misc.getAndJoined(commodities));
        }
    }

    public static class PlanetWaterLevel extends TerraformingRequirement {
        int minWaterLevel;
        int maxWaterLevel;
        protected PlanetWaterLevel(String id, String[] enableSettings, boolean invert, int minWaterLevel, int maxWaterLevel) {
            super(id, enableSettings, invert);
            this.minWaterLevel = minWaterLevel;
            this.maxWaterLevel = maxWaterLevel;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            if (ctx.getPlanet() == null) {
                return false;
            }
            boggledTools.PlanetType planetType = boggledTools.getPlanetType(ctx.getPlanet());
            int planetWaterLevel = planetType.getWaterLevel(ctx);
            return minWaterLevel <= planetWaterLevel && planetWaterLevel <= maxWaterLevel;
        }
    }

    public static class MarketHasWaterPresent extends PlanetWaterLevel {
        public MarketHasWaterPresent(String id, String[] enableSettings, boolean invert, int minWaterLevel, int maxWaterLevel) {
            super(id, enableSettings, invert, minWaterLevel, maxWaterLevel);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return false;
            }
            return super.checkRequirementImpl(ctx) || boggledTools.hasIsmaraSling(market);
        }
    }

    public static class TerraformingPossibleOnMarket extends TerraformingRequirement {
        List<String> invalidatingConditions;
        public TerraformingPossibleOnMarket(String id, String[] enableSettings, boolean invert, List<String> invalidatingConditions) {
            super(id, enableSettings, invert);
            this.invalidatingConditions = invalidatingConditions;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return false;
            }
            for (String invalidatingCondition : invalidatingConditions) {
                if (market.hasCondition(invalidatingCondition)) {
                    return false;
                }
            }
            return boggledTools.getPlanetType(ctx.getPlanet()).getTerraformingPossible();
        }
    }

    public static class MarketHasTags extends TerraformingRequirement {
        List<String> tags;
        public MarketHasTags(String id, String[] enableSettings, boolean invert, List<String> tags) {
            super(id, enableSettings, invert);
            this.tags = tags;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return false;
            }
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
        public MarketIsAtLeastSize(String id, String[] enableSettings, boolean invert, int colonySize) {
            super(id, enableSettings, invert);
            this.colonySize = colonySize;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return false;
            }
            return market.getSize() >= colonySize;
        }
    }

    public static class MarketStorageContainsAtLeast extends ItemRequirement {
        String submarketId;
        public MarketStorageContainsAtLeast(String id, String[] enableSettings, boolean invert, String submarketId, ItemType itemType, String itemId, String settingId, int quantity) {
            super(id, enableSettings, invert, itemType, itemId, settingId, quantity);
            this.submarketId = submarketId;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }
            super.addTokenReplacements(market.getSubmarket(submarketId).getCargo(), ctx, tokenReplacements);
            for (SubmarketSpecAPI submarketSpec : Global.getSettings().getAllSubmarketSpecs()) {
                if (!submarketSpec.getId().equals(submarketId)) {
                    continue;
                }
                tokenReplacements.put("$submarket", Misc.lcFirst(submarketSpec.getName()));
                break;
            }
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return false;
            }
            return checkCargoHasItem(market.getSubmarket(submarketId).getCargo());
        }
    }

    public static class FleetStorageContainsAtLeast extends ItemRequirement {
        protected FleetStorageContainsAtLeast(String id, String[] enableSettings, boolean invert, ItemType itemType, String itemId, String settingId, int quantity) {
            super(id, enableSettings, invert, itemType, itemId, settingId, quantity);
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            CampaignFleetAPI playerFleet = ctx.getFleet();
            if (playerFleet == null) {
                return;
            }
            super.addTokenReplacements(playerFleet.getCargo(), ctx, tokenReplacements);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            CampaignFleetAPI playerFleet = ctx.getFleet();
            if (playerFleet == null) {
                return false;
            }
            return super.checkCargoHasItem(playerFleet.getCargo());
        }
    }

    public static class FleetTooCloseToJumpPoint extends TerraformingRequirement {
        float distance;

        protected FleetTooCloseToJumpPoint(String id, String[] enableSettings, boolean invert, float distance) {
            super(id, enableSettings, invert);
            this.distance = distance;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            StarSystemAPI starSystem = ctx.getStarSystem();
            if (starSystem == null) {
                return false;
            }
            for (Object object : starSystem.getEntities(JumpPointAPI.class)) {
                if (Misc.getDistance((JumpPointAPI) object, ctx.getFleet()) < distance) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class PlayerHasStoryPointsAtLeast extends TerraformingRequirement {
        int quantity;
        public PlayerHasStoryPointsAtLeast(String id, String[] enableSettings, boolean invert, int quantity) {
            super(id, enableSettings, invert);
            this.quantity = quantity;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            tokenReplacements.put("$storyPointsQuantity", Integer.toString(quantity));
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            return Global.getSector().getPlayerStats().getStoryPoints() >= quantity;
        }
    }

    public static class WorldTypeSupportsResourceImprovement extends TerraformingRequirement {
        String resourceId;
        public WorldTypeSupportsResourceImprovement(String id, String[] enableSettings, boolean invert, String resourceId) {
            super(id, enableSettings, invert);
            this.resourceId = resourceId;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            PlanetAPI planet = ctx.getPlanet();
            if (planet == null) {
                return false;
            }

            MarketAPI market = ctx.getPlanetMarket();
            if (market == null) {
                return false;
            }

            Pair<String, String> key = new Pair<>(boggledTools.getPlanetType(planet).getPlanetId(), resourceId);
            String maxResource = boggledTools.getResourceLimits().get(key);

            if (maxResource == null || maxResource.isEmpty()) {
                return false;
            }

            if (market.hasCondition(maxResource)) {
                return false;
            }

            List<String> resourceProgression = boggledTools.getResourceProgressions().get(resourceId);

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

        public IntegerFromTagSubstring(String id, String[] enableSettings, boolean invert, String option, String tagSubstring, int maxValue) {
            super(id, enableSettings, invert);
            this.tagSubstring = tagSubstring;
            this.maxValue = maxValue;
            this.option = option;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return false;
            }

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
        public PlayerHasSkill(String id, String[] enableSettings, boolean invert, String skill) {
            super(id, enableSettings, invert);
            this.skill = skill;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            return Global.getSector().getPlayerStats().getSkillLevel(skill) != 0;
        }
    }

    public static class SystemStarHasTags extends TerraformingRequirement {
        List<String> tags;
        public SystemStarHasTags(String id, String[] enableSettings, boolean invert, List<String> tags) {
            super(id, enableSettings, invert);
            this.tags = tags;
        }

        private boolean starHasTag(PlanetAPI star, String tag) {
            return star != null && star.hasTag(tag);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            if (ctx.getStarSystem() == null) {
                return false;
            }

            StarSystemAPI system = ctx.getStarSystem();
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
        public SystemStarType(String id, String[] enableSettings, boolean invert, String starType) {
            super(id, enableSettings, invert);
            this.starType = starType;
        }

        private boolean starEquals(PlanetAPI star) {
            return star != null && star.getTypeId().equals(starType);
        }
        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            if (ctx.getStarSystem() == null) {
                return false;
            }

            StarSystemAPI system = ctx.getStarSystem();
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

    public static class FleetInHyperspace extends TerraformingRequirement {
        protected FleetInHyperspace(String id, String[] enableSettings, boolean invert) {
            super(id, enableSettings, invert);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            return ctx.getFleet().isInHyperspace() || ctx.getFleet().isInHyperspaceTransition();
        }
    }

    public static class SystemHasJumpPoints extends TerraformingRequirement {
        int numJumpPoints;
        protected SystemHasJumpPoints(String id, String[] enableSettings, boolean invert, int numJumpPoints) {
            super(id, enableSettings, invert);
            this.numJumpPoints = numJumpPoints;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            if (ctx.getStarSystem() == null) {
                return false;
            }
            return ctx.getStarSystem().getJumpPoints().size() >= numJumpPoints;
        }
    }

    public static class SystemHasPlanets extends TerraformingRequirement {
        int numPlanets;
        protected SystemHasPlanets(String id, String[] enableSettings, boolean invert, int numPlanets) {
            super(id, enableSettings, invert);
            this.numPlanets = numPlanets;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            if (ctx.getStarSystem() == null) {
                return false;
            }
            int planetCount = -1;
            for (PlanetAPI planet : ctx.getStarSystem().getPlanets()) {
                if (!planet.isStar()) {
                    planetCount++;
                }
            }
            return planetCount >= numPlanets;
        }
    }

    public static class TargetPlanetOwnedBy extends TerraformingRequirement {
        List<String> factions;

        protected TargetPlanetOwnedBy(String id, String[] enableSettings, boolean invert, List<String> factions) {
            super(id, enableSettings, invert);
            this.factions = factions;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return false;
            }

            MarketAPI market = targetPlanet.getMarket();
            if (market == null) {
                return false;
            }

            if (!market.isPlayerOwned()) {
                return !factions.contains(market.getFactionId());
            }

            return false;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            PlanetAPI targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return;
            }
            tokenReplacements.put("$factionName", targetPlanet.getFaction().getDisplayNameWithArticle());
        }
    }

    public static class TargetStationOwnedBy extends TerraformingRequirement {
        List<String> factions;
        protected TargetStationOwnedBy(String id, String[] enableSettings, boolean invert, List<String> factions) {
            super(id, enableSettings, invert);
            this.factions = factions;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            SectorEntityToken targetStation = ctx.getStation();
            if (targetStation == null) {
                return false;
            }

            MarketAPI market = targetStation.getMarket();
            if (market == null) {
                return false;
            }

            if (!market.isPlayerOwned()) {
                return !factions.contains(market.getFactionId());
            }

            return false;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            SectorEntityToken targetStation = ctx.getStation();
            if (targetStation == null) {
                return;
            }
            tokenReplacements.put("$factionName", targetStation.getFaction().getDisplayNameWithArticle());
        }
    }

    public static class TargetPlanetGovernedByPlayer extends TerraformingRequirement {
        protected TargetPlanetGovernedByPlayer(String id, String[] enableSettings, boolean invert) {
            super(id, enableSettings, invert);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return false;
            }

            MarketAPI market = targetPlanet.getMarket();
            if (market == null) {
                return false;
            }

            if (!market.isPlayerOwned()) {
                return false;
            }

            if (market.getFaction() == ctx.getFleet().getFaction()) {
                return false;
            }

            return true;
        }
    }

    public static class TargetPlanetWithinDistance extends TerraformingRequirement {
        float distance;
        protected TargetPlanetWithinDistance(String id, String[] enableSettings, boolean invert, float distance) {
            super(id, enableSettings, invert);
            this.distance = distance;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return false;
            }
            return (Misc.getDistance(ctx.getFleet(), targetPlanet) - targetPlanet.getRadius()) < distance;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            SectorEntityToken playerFleet = ctx.getFleet();
            if (targetPlanet == null || playerFleet == null) {
                return;
            }
            float distanceInSu = (Misc.getDistance(playerFleet, targetPlanet) - targetPlanet.getRadius()) / 2000f;
            float requiredDistanceInSu = distance / 2000f;
            tokenReplacements.put("$planetName", targetPlanet.getName());
            tokenReplacements.put("$distanceInSu", String.format("%.2f", distanceInSu));
            tokenReplacements.put("$requiredDistanceInSu", String.format("%.2f", requiredDistanceInSu));
        }
    }

    public static class TargetStationWithinDistance extends TerraformingRequirement {
        float distance;
        protected TargetStationWithinDistance(String id, String[] enableSettings, boolean invert, float distance) {
            super(id, enableSettings, invert);
            this.distance = distance;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            SectorEntityToken targetStation = ctx.getStation();
            SectorEntityToken playerFleet = ctx.getFleet();
            if (targetStation == null || playerFleet == null) {
                return false;
            }
            return Misc.getDistance(ctx.getFleet(), targetStation) < distance;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            SectorEntityToken targetStation = ctx.getStation();
            SectorEntityToken playerFleet = ctx.getFleet();
            if (targetStation == null || playerFleet == null) {
                return;
            }
            float distanceInSu = Misc.getDistance(playerFleet, targetStation) / 2000f;
            float requiredDistanceInSu = distance / 2000f;
            tokenReplacements.put("$stationName", targetStation.getName());
            tokenReplacements.put("$distanceInSu", String.format("%.2f", distanceInSu));
            tokenReplacements.put("$requiredDistanceInSu", String.format("%.2f", requiredDistanceInSu));
        }
    }

    public static class TargetStationColonizable extends TerraformingRequirement {
        protected TargetStationColonizable(String id, String[] enableSettings, boolean invert) {
            super(id, enableSettings, invert);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            SectorEntityToken targetStation = ctx.getStation();
            if (targetStation == null) {
                return false;
            }
            return targetStation.getMarket() != null && targetStation.getMarket().hasCondition(Conditions.ABANDONED_STATION);
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            SectorEntityToken targetStation = ctx.getStation();
            if (targetStation == null) {
                return;
            }
            tokenReplacements.put("$stationName", targetStation.getName());
        }
    }

    public static class TargetPlanetIsAtLeastSize extends TerraformingRequirement {
        float size;
        protected TargetPlanetIsAtLeastSize(String id, String[] enableSettings, boolean invert, float size) {
            super(id, enableSettings, invert);
            this.size = size;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return false;
            }
            return targetPlanet.getRadius() >= size;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return;
            }
            tokenReplacements.put("$planetName", targetPlanet.getName());
        }
    }

    public static class TargetPlanetOrbitFocusWithinDistance extends TerraformingRequirement {
        float distance;
        protected TargetPlanetOrbitFocusWithinDistance(String id, String[] enableSettings, boolean invert, float distance) {
            super(id, enableSettings, invert);
            this.distance = distance;
        }

        protected boolean check(SectorEntityToken planet, SectorEntityToken block) {
            return planet.getCircularOrbitRadius() < (block.getRadius() + distance);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return false;
            }
            SectorEntityToken orbitFocus = targetPlanet.getOrbitFocus();
            if (orbitFocus == null) {
                return false;
            }
            return check(targetPlanet, orbitFocus);
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return;
            }
            tokenReplacements.put("$planetName", targetPlanet.getName());
            SectorEntityToken orbitFocus = targetPlanet.getOrbitFocus();
            if (orbitFocus == null) {
                return;
            }
            tokenReplacements.put("$focusPlanetName", orbitFocus.getName());
        }
    }

    public static class TargetPlanetStarWithinDistance extends TargetPlanetOrbitFocusWithinDistance {
        protected TargetPlanetStarWithinDistance(String id, String[] enableSettings, boolean invert, float distance) {
            super(id, enableSettings, invert, distance);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return false;
            }
            SectorEntityToken orbitFocus = targetPlanet.getOrbitFocus();
            if (orbitFocus == null) {
                return false;
            }
            if (!orbitFocus.isStar()) {
                return false;
            }
            return check(targetPlanet, orbitFocus);
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            PlanetAPI targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return;
            }
            tokenReplacements.put("$planetName", targetPlanet.getName());
            SectorEntityToken orbitFocus = targetPlanet.getOrbitFocus();
            if (orbitFocus == null) {
                return;
            }
            if (orbitFocus.isStar()) {
                tokenReplacements.put("$starName", orbitFocus.getName());
            }
        }
    }

    public static class TargetPlanetOrbitersWithinDistance extends TargetPlanetOrbitFocusWithinDistance {
        protected TargetPlanetOrbitersWithinDistance(String id, String[] enableSettings, boolean invert, float distance) {
            super(id, enableSettings, invert, distance);
        }

        PlanetAPI check(SectorEntityToken targetPlanet, StarSystemAPI starSystem) {
            for (PlanetAPI planet : starSystem.getPlanets()) {
                if (planet.getOrbitFocus() == null) {
                    continue;
                }
                if (planet.isStar()) {
                    continue;
                }
                if (!planet.getOrbitFocus().equals(targetPlanet)) {
                    continue;
                }
                if (planet.getRadius() == 0) {
                    continue;
                }
                if (check(targetPlanet, planet)) {
                    continue;
                }
                return planet;
            }
            return null;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return false;
            }
            StarSystemAPI starSystem = ctx.getStarSystem();
            if (starSystem == null) {
                return false;
            }
            return check(targetPlanet, starSystem) != null;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return;
            }
            tokenReplacements.put("$planetName", targetPlanet.getName());
            StarSystemAPI starSystem = ctx.getStarSystem();
            if (starSystem == null) {
                return;
            }
            PlanetAPI blockPlanet = check(targetPlanet, starSystem);
            if (blockPlanet == null) {
                return;
            }
            tokenReplacements.put("$focusPlanetName", blockPlanet.getName());
        }
    }

    public static class TargetPlanetMoonCountLessThan extends TerraformingRequirement {
        int maxMoons;
        protected TargetPlanetMoonCountLessThan(String id, String[] enableSettings, boolean invert, int maxMoons) {
            super(id, enableSettings, invert);
            this.maxMoons = maxMoons;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return false;
            }
            StarSystemAPI starSystem = ctx.getStarSystem();
            if (starSystem == null) {
                return false;
            }
            int moonCount = 0;
            for (PlanetAPI planet : starSystem.getPlanets()) {
                if (planet.getOrbitFocus() == null) {
                    continue;
                }
                if (planet.isStar()) {
                    continue;
                }
                if (!planet.getOrbitFocus().equals(targetPlanet)) {
                    continue;
                }
                if (planet.getRadius() == 0) {
                    continue;
                }
                moonCount++;
            }
            return moonCount < maxMoons;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return;
            }
            tokenReplacements.put("$planetName", targetPlanet.getName());
        }
    }

    public static class TargetPlanetOrbitersTooClose extends TerraformingRequirement {
        float distance;

        protected TargetPlanetOrbitersTooClose(String id, String[] enableSettings, boolean invert, float distance) {
            super(id, enableSettings, invert);
            this.distance = distance;
        }

        PlanetAPI check(SectorEntityToken targetPlanet, StarSystemAPI starSystem) {
            for (PlanetAPI planet : starSystem.getPlanets()) {
                if (planet.getOrbitFocus() == null) {
                    continue;
                }
                if (planet.isStar()) {
                    continue;
                }
                if (!planet.getOrbitFocus().equals(targetPlanet.getOrbitFocus())) {
                    continue;
                }
                float orbitRadiusDifference = Math.abs(planet.getCircularOrbitRadius() - targetPlanet.getCircularOrbitRadius());
                if (orbitRadiusDifference < distance) {
                    return planet;
                }
            }
            return null;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return false;
            }
            StarSystemAPI starSystem = ctx.getStarSystem();
            if (starSystem == null) {
                return false;
            }
            if (check(targetPlanet, starSystem) == null) {
                return false;
            }
            return true;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            SectorEntityToken targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return;
            }
            tokenReplacements.put("$planetName", targetPlanet.getName());
            StarSystemAPI starSystem = ctx.getStarSystem();
            if (starSystem == null) {
                return;
            }
            SectorEntityToken focusPlanet = check(targetPlanet, starSystem);
            if (focusPlanet != null) {
                tokenReplacements.put("$focusPlanetName", focusPlanet.getName());
            }
        }
    }

    public static class TargetPlanetStationCountLessThan extends TerraformingRequirement {
        List<String> stationTags;
        int maxNumStations;
        protected TargetPlanetStationCountLessThan(String id, String[] enableSettings, boolean invert, List<String> stationTags, int maxNumStations) {
            super(id, enableSettings, invert);
            this.stationTags = stationTags;
            this.maxNumStations = maxNumStations;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            PlanetAPI targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return false;
            }
            return boggledTools.numStationsInOrbit(targetPlanet, stationTags.toArray(new String[0])) < maxNumStations;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            PlanetAPI targetPlanet = ctx.getPlanet();
            if (targetPlanet != null) {
                tokenReplacements.put("$planetName", targetPlanet.getName());
                int numStations = boggledTools.numStationsInOrbit(targetPlanet, stationTags.toArray(new String[0]));
                tokenReplacements.put("$maxNumStations", String.format("%,d", maxNumStations));
                tokenReplacements.put("$numStations", String.format("%,d", numStations));
                tokenReplacements.put("$stationOrStations", numStations == 1 ? "station" : "stations");
            }
        }
    }

    public static class TargetSystemStationCountLessThan extends TerraformingRequirement {
        List<String> stationTags;
        int maxNumStations;
        protected TargetSystemStationCountLessThan(String id, String[] enableSettings, boolean invert, List<String> stationTags, int maxNumStations) {
            super(id, enableSettings, invert);
            this.stationTags = stationTags;
            this.maxNumStations = maxNumStations;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            StarSystemAPI starSystem = ctx.getStarSystem();
            if (starSystem == null) {
                return false;
            }
            return boggledTools.numStationsInSystem(starSystem, stationTags.toArray(new String[0])) < maxNumStations;
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            StarSystemAPI starSystem = ctx.getStarSystem();
            if (starSystem != null) {
                tokenReplacements.put("$systemName", starSystem.getName());
                int numStations = boggledTools.numStationsInSystem(starSystem, stationTags.toArray(new String[0]));
                tokenReplacements.put("$maxNumStations", String.format("%,d", maxNumStations));
                tokenReplacements.put("$numStations", String.format("%,d", numStations));
                tokenReplacements.put("$stationOrStations", numStations == 1 ? "station" : "stations");
            }
        }
    }

    public static class FleetInAsteroidBelt extends TerraformingRequirement {
        protected FleetInAsteroidBelt(String id, String[] enableSettings, boolean invert) {
            super(id, enableSettings, invert);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            CampaignFleetAPI playerFleet = ctx.getFleet();
            if (playerFleet == null) {
                return false;
            }
            return boggledTools.playerFleetInAsteroidBelt(playerFleet);
        }
    }

    public static class FleetInAsteroidField extends TerraformingRequirement {
        protected FleetInAsteroidField(String id, String[] enableSettings, boolean invert) {
            super(id, enableSettings, invert);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            CampaignFleetAPI playerFleet = ctx.getFleet();
            if (playerFleet == null) {
                return false;
            }
            return boggledTools.playerFleetInAsteroidField(playerFleet);
        }
    }

    public static class TargetPlanetStoryCritical extends TerraformingRequirement {
        protected TargetPlanetStoryCritical(String id, String[] enableSettings, boolean invert) {
            super(id, enableSettings, invert);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            PlanetAPI targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return false;
            }
            MarketAPI market = targetPlanet.getMarket();
            if (market == null) {
                return false;
            }
            return Misc.isStoryCritical(market);
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            PlanetAPI targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return;
            }
            MarketAPI market = targetPlanet.getMarket();
            if (market == null) {
                return;
            }
            tokenReplacements.put("$marketName", market.getName());
        }
    }

    public static class TargetStationStoryCritical extends TerraformingRequirement {
        protected TargetStationStoryCritical(String id, String[] enableSettings, boolean invert) {
            super(id, enableSettings, invert);
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            SectorEntityToken targetStation = ctx.getStation();
            if (targetStation == null) {
                return false;
            }
            MarketAPI market = targetStation.getMarket();
            if (market == null) {
                return false;
            }
            return Misc.isStoryCritical(market);
        }

        @Override
        public void addTokenReplacements(RequirementContext ctx, Map<String, String> tokenReplacements) {
            SectorEntityToken targetStation = ctx.getStation();
            if (targetStation == null) {
                return;
            }
            MarketAPI market = targetStation.getMarket();
            if (market == null) {
                return;
            }
            tokenReplacements.put("$marketName", market.getName());
        }
    }

    public static class BooleanSettingIsTrue extends TerraformingRequirement {
        String settingId;
        boolean invertSetting;
        TerraformingRequirement req;
        protected BooleanSettingIsTrue(String id, String[] enableSettings, boolean invert, String settingId, boolean invertSetting, TerraformingRequirement req) {
            super(id, enableSettings, invert);
            this.settingId = settingId;
            this.invertSetting = invertSetting;
            this.req = req;
        }

        @Override
        protected boolean checkRequirementImpl(RequirementContext ctx) {
            boolean settingValue = boggledTools.getBooleanSetting(settingId);
            if (invertSetting) {
                settingValue = !settingValue;
            }
            if (settingValue) {
                return req.checkRequirement(ctx);
            }
            return true;
        }
    }
}
