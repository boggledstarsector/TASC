package boggled.scripts;

import boggled.campaign.econ.industries.BoggledCommonIndustry;
import boggled.campaign.econ.industries.BoggledIndustryInterface;
import boggled.scripts.PlayerCargoCalculations.bogglesDefaultCargo;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.FleetAdvanceScript;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import boggled.campaign.econ.boggledTools;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BoggledTerraformingProjectEffect {
    public static class EffectTooltipPara {
        public String prefix;
        public String suffix;
        public List<String> infix = new ArrayList<>();
        public List<String> highlights = new ArrayList<>();
        public List<Color> highlightColors = new ArrayList<>();

        public EffectTooltipPara() {
            this.prefix = "";
            this.suffix = "";
        }

        public EffectTooltipPara(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }
    }

    public abstract static class TerraformingProjectEffect {
        public enum DescriptionMode {
            TO_APPLY,
            APPLIED
        }

        public enum DescriptionSource {
            GENERIC,
            RIGHT_AFTER_DESCRIPTION_SECTION,
            POST_DEMAND_SECTION,
            AI_CORE_DESCRIPTION,
            IMPROVE_DESCRIPTION
        }

        String id;
        String[] enableSettings;
        protected TerraformingProjectEffect(String id, String[] enableSettings) {
            this.id = id;
            this.enableSettings = enableSettings;
        }

        public String getId() { return id; }
        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

        protected abstract void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource);
        protected abstract void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx);

        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {}

        public void addTokenReplacements(Map<String, String> tokenReplacements) {}

        public final void addEffectTooltipInfo(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            if (!isEnabled()) {
                return;
            }
            addTooltipInfoImpl(ctx, effectTypeToPara, effectSource, mode, source);
        }

        public final void applyProjectEffect(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            if (!isEnabled()) {
                unapplyProjectEffect(ctx);
                return;
            }
            applyProjectEffectImpl(ctx, effectSource);
        }

        public final void unapplyProjectEffect(BoggledTerraformingRequirement.RequirementContext ctx) {
            if (!isEnabled()) {
                return;
            }
            unapplyProjectEffectImpl(ctx);
        }
    }

    public static class PlanetTypeChange extends TerraformingProjectEffect {
        private final String newPlanetType;

        public PlanetTypeChange(String id, String[] enableSettings, String newPlanetType) {
            super(id, enableSettings);
            this.newPlanetType = newPlanetType;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            ctx.getPlanet().changeType(newPlanetType, null);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class IndustrySwap extends TerraformingProjectEffect {
        String industryIdToRemove;
        String industryIdToAdd;
        protected IndustrySwap(String id, String[] enableSettings, String industryIdToRemove, String industryIdToAdd) {
            super(id, enableSettings);
            this.industryIdToRemove = industryIdToRemove;
            this.industryIdToAdd = industryIdToAdd;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }

            Industry industryToRemove = market.getIndustry(industryIdToRemove);
            if (industryToRemove == null) {
                return;
            }
            boolean needsConstruction = !industryToRemove.isFunctional();
            market.removeIndustry(industryIdToRemove, MarketAPI.MarketInteractionMode.REMOTE, false);

            if (needsConstruction) {
                market.getConstructionQueue().addToEnd(industryIdToAdd, 0);
            } else {
                market.addIndustry(industryIdToAdd);
            }
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class MarketAddCondition extends TerraformingProjectEffect {
        private final String condition;

        public MarketAddCondition(String id, String[] enableSettings, String condition) {
            super(id, enableSettings);
            this.condition = condition;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }
            boggledTools.addCondition(market, condition);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }
            boggledTools.removeCondition(market, condition);
        }
    }

    public static class MarketAddConditionNoRemove extends TerraformingProjectEffect {
        private final String condition;

        protected MarketAddConditionNoRemove(String id, String[] enableSettings, String condition) {
            super(id, enableSettings);
            this.condition = condition;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }
            boggledTools.addCondition(market, condition);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class MarketRemoveCondition extends TerraformingProjectEffect {
        String condition;

        public MarketRemoveCondition(String id, String[] enableSettings, String condition) {
            super(id, enableSettings);
            this.condition = condition;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }
            boggledTools.removeCondition(market, condition);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }
            boggledTools.addCondition(market, condition);
        }
    }

    public static class MarketProgressResource extends TerraformingProjectEffect {
        private final String resource;
        private final int step;

        public MarketProgressResource(String id, String[] enableSettings, String resource, int step) {
            super(id, enableSettings);
            this.resource = resource;
            this.step = step;
        }

        int clamp(int val, int min, int max) {
            return Math.max(min, Math.min(max, val));
        }

        private void incrementResourceWithDefault(MarketAPI market, List<String> resourceProgression, int step) {
            // Step because OuyangOptimization goes volatiles_trace (0) to volatiles_abundant (2), etc
            int defaultResourceIdx = clamp(step, 0, resourceProgression.size() - 1);
            String defaultResource = resourceProgression.get(defaultResourceIdx);
            boolean resourceFound = false;
            for (int i = 0; i < resourceProgression.size() - 1; ++i) {
                if (market.hasCondition(resourceProgression.get(i))) {
                    boggledTools.removeCondition(market, resourceProgression.get(i));
                    int newConditionIdx = clamp(i + step, 0, resourceProgression.size() - 1);
                    boggledTools.addCondition(market, resourceProgression.get(newConditionIdx));
                    resourceFound = true;
                    break;
                }
            }

            if (!resourceFound && defaultResource != null && !defaultResource.isEmpty()) {
                boggledTools.addCondition(market, defaultResource);
            }
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            List<String> resourcesProgression = boggledTools.getResourceProgressions().get(resource);
            if (resourcesProgression == null || resourcesProgression.isEmpty()) {
                return;
            }

            incrementResourceWithDefault(market, boggledTools.getResourceProgressions().get(resource), step);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            MarketAPI market = ctx.getClosestMarket();
            List<String> resourcesProgression = boggledTools.getResourceProgressions().get(resource);
            if (resourcesProgression == null || resourcesProgression.isEmpty()) {
                return;
            }

            incrementResourceWithDefault(market, boggledTools.getResourceProgressions().get(resource), -step);
        }
    }

    public static class FocusMarketAddCondition extends MarketAddCondition {
        public FocusMarketAddCondition(String id, String[] enableSettings, String condition) {
            super(id, enableSettings, condition);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            super.applyProjectEffectImpl(ctx.getFocusContext(), effectSource);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            super.unapplyProjectEffectImpl(ctx.getFocusContext());
        }
    }

    public static class FocusMarketRemoveCondition extends MarketRemoveCondition {
        public FocusMarketRemoveCondition(String id, String[] enableSettings, String condition) {
            super(id, enableSettings, condition);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            super.applyProjectEffectImpl(ctx.getFocusContext(), effectSource);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            super.unapplyProjectEffectImpl(ctx.getFocusContext());
        }
    }

    public static class FocusMarketProgressResource extends MarketProgressResource {
        public FocusMarketProgressResource(String id, String[] enableSettings, String resource, int step) {
            super(id, enableSettings, resource, step);
        }
        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            super.applyProjectEffectImpl(ctx.getFocusContext(), effectSource);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            super.unapplyProjectEffectImpl(ctx.getFocusContext());
        }
    }

    public static class FocusMarketAndSiphonStationProgressResource extends MarketProgressResource {
        public FocusMarketAndSiphonStationProgressResource(String id, String[] enableSettings, String resource, int step) {
            super(id, enableSettings, resource, step);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            super.applyProjectEffectImpl(ctx.getFocusContext(), effectSource);

            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }
            SectorEntityToken closestGasGiantToken = market.getPrimaryEntity().getOrbitFocus();
            if (closestGasGiantToken == null) {
                return;
            }
            for (SectorEntityToken entity : closestGasGiantToken.getStarSystem().getAllEntities()) {
                /*
                Search through all entities in the system
                Just to find any siphon stations attached to the gas giant this station is orbiting
                Because gas giants can have both acropolis stations and siphon stations
                Should make this more flexible in the future, but for now, eh
                 */
                if (entity.hasTag(Tags.STATION)
                    && entity.getOrbitFocus() != null
                    && entity.getOrbitFocus().equals(closestGasGiantToken)
                    && entity.getMarket() != null
                    && (   entity.getCustomEntitySpec().getDefaultName().equals("Side Station")
                        || entity.getCustomEntitySpec().getDefaultName().equals("Siphon Station"))
                    && !entity.getId().equals("beholder_station"))
                {
                    super.applyProjectEffectImpl(ctx, effectSource);
                }
            }
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            super.unapplyProjectEffectImpl(ctx.getFocusContext());

            SectorEntityToken closestGasGiantToken = ctx.getPlanetMarket().getPrimaryEntity();
            if (closestGasGiantToken == null) {
                return;
            }
            for (SectorEntityToken entity : closestGasGiantToken.getStarSystem().getAllEntities()) {
                /*
                Search through all entities in the system
                Just to find any siphon stations attached to the gas giant this station is orbiting
                Because gas giants can have both acropolis stations and siphon stations
                Should make this more flexible in the future, but for now, eh
                 */
                if (entity.hasTag(Tags.STATION)
                    && entity.getOrbitFocus() != null
                    && entity.getOrbitFocus().equals(closestGasGiantToken)
                    && entity.getMarket() != null
                    && (   entity.getCustomEntitySpec().getDefaultName().equals("Side Station")
                        || entity.getCustomEntitySpec().getDefaultName().equals("Siphon Station"))
                    && !entity.getId().equals("beholder_station"))
                {
                    super.unapplyProjectEffectImpl(ctx);
                }
            }
        }
    }

    public static class SystemAddCoronalTap extends TerraformingProjectEffect {
        public SystemAddCoronalTap(String id, String[] enableSettings) {
            super(id, enableSettings);
        }
        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            StarSystemAPI starSystem = market.getStarSystem();
            SectorEntityToken tapToken = null;

            if (starSystem.getType() == StarSystemGenerator.StarSystemType.TRINARY_2CLOSE) {
                tapToken = starSystem.addCustomEntity("coronal_tap_" + starSystem.getName(), null, "coronal_tap", Global.getSector().getPlayerFaction().getId());

                float minDist = Float.MAX_VALUE;
                PlanetAPI closest = null;
                for (PlanetAPI star : tapToken.getContainingLocation().getPlanets()) {
                    if (!star.isNormalStar()) {
                        continue;
                    }

                    float dist = Misc.getDistance(tapToken.getLocation(), star.getLocation());
                    if (dist < minDist) {
                        minDist = dist;
                        closest = star;
                    }
                }

                if (closest != null) {
                    tapToken.setFacing(Misc.getAngleInDegrees(tapToken.getLocation(), closest.getLocation()) + 180.0f);
                }

            } else {
                WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<>();
                WeightedRandomPicker<PlanetAPI> fallback = new WeightedRandomPicker<>();

                for (PlanetAPI planet : starSystem.getPlanets()) {
                    if (!planet.isNormalStar()) {
                        continue;
                    }

                    if (planet.getTypeId().equals(StarTypes.BLUE_GIANT) || planet.getTypeId().equals(StarTypes.BLUE_SUPERGIANT)) {
                        picker.add(planet);
                    } else {
                        fallback.add(planet);
                    }
                }
                if (picker.isEmpty()) {
                    picker.addAll(fallback);
                }

                PlanetAPI star = picker.pick();
                if (star != null) {
                    CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec(Entities.CORONAL_TAP);

                    float orbitRadius = star.getRadius() + spec.getDefaultRadius() + 100f;
                    float orbitDays = orbitRadius / 20f;

                    tapToken = starSystem.addCustomEntity("coronal_tap_" + starSystem.getName(), null, "coronal_tap", Global.getSector().getPlayerFaction().getId());

                    tapToken.setCircularOrbitPointingDown(star, boggledTools.getAngleFromEntity(ctx.getClosestMarket().getPrimaryEntity(), star), orbitRadius, orbitDays);
                }
            }

            if (tapToken != null) {
                tapToken.addTag("BOGGLED_BUILT_BY_PERIHELION_PROJECT");
                tapToken.removeScriptsOfClass(FleetAdvanceScript.class);

                starSystem.addScript(new CoronalTapParticleScript(tapToken));
                starSystem.addTag(Tags.HAS_CORONAL_TAP);

                MemoryAPI memory = tapToken.getMemory();
                memory.set("$usable", true);
                memory.set("$defenderFleetDefeated", true);

                memory.unset("$hasDefenders");
                memory.unset("$defenderFleet");
            }
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class MarketRemoveIndustry extends TerraformingProjectEffect {
        String industryId;
        public MarketRemoveIndustry(String id, String[] enableSettings, String industryId) {
            super(id, enableSettings);
            this.industryId = industryId;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }
            market.removeIndustry(industryId, null, false);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static abstract class RemoveItemFromCargo extends TerraformingProjectEffect {
        BoggledTerraformingRequirement.StorageContainsAtLeast.ItemType itemType;
        String itemId;
        String settingId;
        int quantity;
        String jobId;
        public RemoveItemFromCargo(String id, String[] enableSettings, BoggledTerraformingRequirement.StorageContainsAtLeast.ItemType itemType, String itemId, String settingId, int quantity, String jobId) {
            super(id, enableSettings);
            this.itemType = itemType;
            this.itemId = itemId;
            this.settingId = settingId;
            this.quantity = quantity;
            this.jobId = jobId;
        }

        protected void removeItemFromCargo(CargoAPI cargo) {
            int quantityToRemove = quantity;
            if (!settingId.isEmpty()) {
                quantityToRemove = boggledTools.getIntSetting(settingId);
            }
            switch (itemType) {
                case CREDITS:
                    cargo.getCredits().subtract(quantityToRemove);
                    break;
                case RESOURCES:
                    if (!jobId.isEmpty()) {
                        bogglesDefaultCargo.active.removeCommodity(cargo, jobId, itemId, quantityToRemove);
                    } else {
                        cargo.removeItems(CargoAPI.CargoItemType.RESOURCES, itemId, quantityToRemove);
                    }
                    break;
                case SPECIAL:
                    cargo.removeItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(itemId, null), quantityToRemove);
                    break;
            }
        }

        @Override
        public void addTokenReplacements(Map<String, String> tokenReplacements) {
            switch (itemType) {
                case CREDITS:
                    tokenReplacements.put("$itemName", "credits");
                    tokenReplacements.put("$ItemName", "Credits");
                    break;
                case RESOURCES:
                    tokenReplacements.put("$itemName", Global.getSettings().getCommoditySpec(itemId).getLowerCaseName());
                    tokenReplacements.put("$ItemName", Global.getSettings().getCommoditySpec(itemId).getName());
                    break;
                case SPECIAL:
                    tokenReplacements.put("$itemName", Global.getSettings().getSpecialItemSpec(itemId).getName().toLowerCase());
                    tokenReplacements.put("$ItemName", Global.getSettings().getSpecialItemSpec(itemId).getName());
                    break;
            }

            int quantityToCheck = quantity;
            if (!settingId.isEmpty()) {
                quantityToCheck = boggledTools.getIntSetting(settingId);
            }
            tokenReplacements.put("$itemQuantity", String.format("%,d", quantityToCheck));
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            if (!effectTypeToPara.containsKey("ItemCost")) {
                effectTypeToPara.put("ItemCost", new EffectTooltipPara("Expends ", "."));
            }
            EffectTooltipPara para = effectTypeToPara.get("ItemCost");
            String itemString = "";
            switch (itemType) {
                case CREDITS:
                    itemString = "credits";
                    break;
                case RESOURCES:
                    itemString = Global.getSettings().getCommoditySpec(itemId).getLowerCaseName();
                    break;
                case SPECIAL:
                    itemString = Global.getSettings().getSpecialItemSpec(itemId).getName().toLowerCase();
                    break;
            }
            int quantityToCheck = quantity;
            if (!settingId.isEmpty()) {
                quantityToCheck = boggledTools.getIntSetting(settingId);
            }
            String quantityString = String.format("%,d", quantityToCheck);
            para.infix.add(quantityString + " " + itemString);
            para.highlights.add(quantityString);
            para.highlightColors.add(Misc.getHighlightColor());
        }
    }

    public static class RemoveItemFromSubmarket extends RemoveItemFromCargo {
        String submarketId;
        public RemoveItemFromSubmarket(String id, String[] enableSettings, String submarketId, BoggledTerraformingRequirement.StorageContainsAtLeast.ItemType itemType, String itemId, String settingId, int quantity, String jobId) {
            super(id, enableSettings, itemType, itemId, settingId, quantity, jobId);
            this.submarketId = submarketId;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }
            removeItemFromCargo(market.getSubmarket(submarketId).getCargo());
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class RemoveStoryPointsFromPlayer extends TerraformingProjectEffect {
        int quantity;
        String settingId;
        public RemoveStoryPointsFromPlayer(String id, String[] enableSettings, int quantity, String settingId) {
            super(id, enableSettings);
            this.quantity = quantity;
            this.settingId = settingId;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            int quantityToRemove = quantity;
            if (!settingId.isEmpty()) {
                quantityToRemove = boggledTools.getIntSetting(settingId);
            }
            Global.getSector().getPlayerStats().spendStoryPoints(quantityToRemove, false, null, false, null);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class RemoveItemFromFleetStorage extends RemoveItemFromCargo {
        public RemoveItemFromFleetStorage(String id, String[] enableSettings, BoggledTerraformingRequirement.StorageContainsAtLeast.ItemType itemType, String itemId, String settingId, int quantity, String jobId) {
            super(id, enableSettings, itemType, itemId, settingId, quantity, jobId);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            CampaignFleetAPI playerFleet = ctx.getFleet();
            if (playerFleet == null) {
                return;
            }
            removeItemFromCargo(playerFleet.getCargo());
//            bogglesDefaultCargo.active.removeCommodity("station_type", commodityId, quantity);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class AddItemToSubmarket extends TerraformingProjectEffect {
        BoggledTerraformingRequirement.StorageContainsAtLeast.ItemType itemType;
        String submarketId;
        String itemId;
        String settingId;
        int quantity;
        public AddItemToSubmarket(String id, String[] enableSettings, BoggledTerraformingRequirement.StorageContainsAtLeast.ItemType itemType, String submarketId, String itemId, String settingId, int quantity) {
            super(id, enableSettings);
            this.itemType = itemType;
            this.submarketId = submarketId;
            this.itemId = itemId;
            this.settingId = settingId;
            this.quantity = quantity;
        }

        @Override
        public void addTokenReplacements(Map<String, String> tokenReplacements) {
            switch (itemType) {
                case CREDITS:
                    tokenReplacements.put("$itemName", "credits");
                    tokenReplacements.put("$ItemName", "Credits");
                    break;
                case RESOURCES:
                    tokenReplacements.put("$itemName", Global.getSettings().getCommoditySpec(itemId).getLowerCaseName());
                    tokenReplacements.put("$ItemName", Global.getSettings().getCommoditySpec(itemId).getName());
                    break;
                case SPECIAL:
                    tokenReplacements.put("$itemName", Global.getSettings().getSpecialItemSpec(itemId).getName().toLowerCase());
                    tokenReplacements.put("$ItemName", Global.getSettings().getSpecialItemSpec(itemId).getName());
                    break;
            }


            for (SubmarketSpecAPI submarketSpec : Global.getSettings().getAllSubmarketSpecs()) {
                if (submarketSpec.getId().equals(submarketId)) {
                    tokenReplacements.put("$submarket", Misc.lcFirst(submarketSpec.getName()));
                }
            }

            int quantityToCheck = quantity;
            if (!settingId.isEmpty()) {
                quantityToCheck = boggledTools.getIntSetting(settingId);
            }
            tokenReplacements.put("$itemQuantity", String.format("%,d", quantityToCheck));
        }

        protected void addItemToCargo(CargoAPI cargo) {
            int quantityToAdd = quantity;
            if (!settingId.isEmpty()) {
                quantityToAdd = boggledTools.getIntSetting(settingId);
            }
            switch (itemType) {
                case CREDITS:
                    cargo.getCredits().add(quantityToAdd);
                    break;
                case RESOURCES:
                    cargo.addItems(CargoAPI.CargoItemType.RESOURCES, itemId, quantityToAdd);
                    break;
                case SPECIAL:
                    cargo.addItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(itemId, null), quantityToAdd);
                    break;
            }
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }
            addItemToCargo(market.getSubmarket(submarketId).getCargo());
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static abstract class AddStation extends TerraformingProjectEffect {
        String stationType;
        String stationName;
        List<String> variants;
        String variantOptionId;

        int numStationsPerLayer;
        float orbitRadius;

        BoggledStationConstructors.StationConstructionData stationConstructionData;

        public AddStation(String id, String[] enableSettings, String stationType, String stationName, List<String> variants, String variantOptionId, int numStationsPerLayer, float orbitRadius, BoggledStationConstructors.StationConstructionData stationConstructionData) {
            super(id, enableSettings);
            this.stationType = stationType;
            this.stationName = stationName;
            this.variants = variants;
            this.variantOptionId = variantOptionId;
            this.numStationsPerLayer = numStationsPerLayer;
            this.orbitRadius = orbitRadius;
            this.stationConstructionData = stationConstructionData;
        }

        @NotNull
        protected static List<String> getGreekAlphabetList() {
            List<String> greekAlphabetList = new ArrayList<>();
            greekAlphabetList.add("Alpha");
            greekAlphabetList.add("Beta");
            greekAlphabetList.add("Gamma");
            greekAlphabetList.add("Delta");
            greekAlphabetList.add("Epsilon");
            greekAlphabetList.add("Zeta");
            greekAlphabetList.add("Eta");
            greekAlphabetList.add("Theta");
            greekAlphabetList.add("Iota");
            greekAlphabetList.add("Kappa");
            greekAlphabetList.add("Lambda");
            greekAlphabetList.add("Mu");
            greekAlphabetList.add("Nu");
            greekAlphabetList.add("Xi");
            greekAlphabetList.add("Omicron");
            greekAlphabetList.add("Pi");
            greekAlphabetList.add("Rho");
            greekAlphabetList.add("Sigma");
            greekAlphabetList.add("Tau");
            greekAlphabetList.add("Upsilon");
            greekAlphabetList.add("Phi");
            greekAlphabetList.add("Chi");
            greekAlphabetList.add("Psi");
            greekAlphabetList.add("Omega");
            return greekAlphabetList;
        }

        protected String getVariant(int numStationsAlreadyPresent)
        {
            numStationsAlreadyPresent = Math.abs(numStationsAlreadyPresent);
            if (variants.isEmpty())
            {
                return "";
            }

            if (!variantOptionId.isEmpty())
            {
                int variantId = boggledTools.getIntSetting(variantOptionId);
                if (variantId != 0)
                {
                    // Variants is zero indexed but variantId is 1-3, not 0-2. Subtract 1 to account for this.
                    return "_" + variants.get((variantId - 1) % variants.size());
                }
            }

            return "_" + variants.get(numStationsAlreadyPresent % variants.size());
        }

        protected String getColonyNameString(int numStationsAlreadyPresent) {
            numStationsAlreadyPresent = Math.abs(numStationsAlreadyPresent);
            List<String> greekAlphabetList = getGreekAlphabetList();
            int letterNum = numStationsAlreadyPresent % greekAlphabetList.size();
            int suffixNum = numStationsAlreadyPresent / greekAlphabetList.size();
            String ret = greekAlphabetList.get(letterNum);
            if (suffixNum != 0) {
                ret = ret + "-" + suffixNum;
            }
            return ret;
        }

        protected int greekAlphabetIndexOf(List<String> greekAlphabet, String name) {
            for (int i = 0; i < greekAlphabet.size(); ++i) {
                String greekLetter = greekAlphabet.get(i);
                if (name.endsWith(greekLetter)) {
                    return i;
                }
            }
            return -1;
        }

        protected String getStationGreekLetterTag(SectorEntityToken station) {
            Collection<String> tags = station.getTags();
            for (String tag : tags) {
                if (tag.startsWith(boggledTools.BoggledTags.stationGreekLetterPrefix)) {
                    return tag;
                }
            }
            return null;
        }

        protected SectorEntityToken compareAndGetLatest(List<String> greekAlphabet, SectorEntityToken o1, SectorEntityToken o2) {
            if (o1 == null) {
                return o2;
            }
            if (o2 == null) {
                return o1;
            }
            String o1Tag = getStationGreekLetterTag(o1);
            String o2Tag = getStationGreekLetterTag(o2);
            if (o1Tag == null) {
                return o2;
            }
            if (o2Tag == null) {
                return o1;
            }

            int o1NumIndexStart = o1Tag.indexOf('-');
            int o2NumIndexStart = o2Tag.indexOf('-');
            if (o1NumIndexStart == -1 && o2NumIndexStart == -1) {
                // Neither has a number at the end, so we compare according to the index in the alphabet
                int o1GreekIndex = greekAlphabetIndexOf(greekAlphabet, o1Tag);
                int o2GreekIndex = greekAlphabetIndexOf(greekAlphabet, o2Tag);
                int comp = Integer.compare(o1GreekIndex, o2GreekIndex);
                if (comp < 0) {
                    return o2;
                }
                // If they return equal, something else is broken
                if (comp == 0) {
                    Global.getLogger(this.getClass()).error("Sector entity tokens " + o1.getName() + " and " + o2.getName() + " compared equal");
                }
                return o1;
            }
            if (o1NumIndexStart == -1) {
                // o2 has a number at the end, o1 doesn't, therefore o1 comes before o2
                return o2;
            }
            if (o2NumIndexStart == -1) {
                // o1 has a number at the end, o2 doesn't, therefore o1 comes after o2
                return o1;
            }
            // They both have a number at the end, so compare that
            int o1Num = Integer.parseInt(o1Tag.substring(o1NumIndexStart + 1));
            int o2Num = Integer.parseInt(o2Tag.substring(o2NumIndexStart + 1));
            int comp = Integer.compare(o1Num, o2Num);
            if (comp < 0) {
                return o2;
            } else if (comp > 0) {
                return o1;
            }
            // The numbers are the same, so compare based on the index in the alphabet
            int o1GreekIndex = greekAlphabetIndexOf(greekAlphabet, o1Tag.substring(0, o1NumIndexStart));
            int o2GreekIndex = greekAlphabetIndexOf(greekAlphabet, o2Tag.substring(0, o2NumIndexStart));
            comp = Integer.compare(o1GreekIndex, o2GreekIndex);
            if (comp < 0) {
                return o2;
            }
            // If they return equal, something else is broken
            if (comp == 0) {
                Global.getLogger(this.getClass()).error("Sector entity tokens " + o1.getName() + " and " + o2.getName() + " compared equal");
            }
            return o1;
        }
    }

    public static class AddStationToOrbit extends AddStation {
        public AddStationToOrbit(String id, String[] enableSettings, String stationType, String stationName, List<String> variants, String variantOptionId, int numStationsPerLayer, float orbitRadius, BoggledStationConstructors.StationConstructionData stationConstructionData) {
            super(id, enableSettings, stationType, stationName, variants, variantOptionId, numStationsPerLayer, orbitRadius, stationConstructionData);
        }

        @Override
        public void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            PlanetAPI targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return;
            }
            if (!effectTypeToPara.containsKey("StationConstructionTarget")) {
                effectTypeToPara.put("StationConstructionTarget", new EffectTooltipPara("Target host world: ", ""));
            }
            EffectTooltipPara para = effectTypeToPara.get("StationConstructionTarget");
            para.infix.add(targetPlanet.getName());
            para.highlights.add(targetPlanet.getName());
            para.highlightColors.add(Misc.getHighlightColor());

            stationConstructionData.addTooltipInfo(ctx, effectTypeToPara);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            PlanetAPI targetPlanet = ctx.getPlanet();
            StarSystemAPI starSystem = ctx.getStarSystem();
            if (targetPlanet == null || starSystem == null) {
                return;
            }

            String playerFactionId = Global.getSector().getPlayerFaction().getId();

            String customEntityStationTag = stationType + "_station";

            int numStations = boggledTools.numStationsInOrbit(targetPlanet, stationType);

            String variantLetter = getVariant(numStations);

            String id = stationType + numStations + "_" + UUID.randomUUID();

            String colonyNameString = getColonyNameString(numStations);
            SectorEntityToken newStation = starSystem.addCustomEntity(id, targetPlanet.getName() + " " + stationName + " " + colonyNameString, customEntityStationTag + variantLetter + "_small", playerFactionId);
            SectorEntityToken newStationLights = starSystem.addCustomEntity(id + "Lights", targetPlanet.getName() + " " + stationName + " " + colonyNameString + " Lights Overlay", customEntityStationTag + variantLetter + "_small_lights_overlay", playerFactionId);

            newStation.addTag(boggledTools.BoggledTags.stationNamePrefix + stationName);
            newStation.addTag(boggledTools.BoggledTags.stationGreekLetterPrefix + colonyNameString);

            float baseOrbitRadius = targetPlanet.getRadius() + this.orbitRadius;
            int orbitRadiusMultiplier = numStations / numStationsPerLayer;
            float orbitRadius = targetPlanet.getRadius() + this.orbitRadius + orbitRadiusMultiplier * (this.orbitRadius / 2);
            if (numStations == 0) {
                newStation.setCircularOrbitPointingDown(targetPlanet, boggledTools.randomOrbitalAngleFloat(), orbitRadius, orbitRadius / 10f);
            } else {
                final List<String> greekAlphabet = getGreekAlphabetList();
                List<SectorEntityToken> allEntitiesInSystem = starSystem.getEntitiesWithTag(stationType);
                SectorEntityToken latestStation = null;
                for (SectorEntityToken entity : allEntitiesInSystem) {
                    if (entity.equals(newStation)) {
                        continue;
                    }
                    if (entity.getOrbitFocus() == null) {
                        continue;
                    }
                    if (!entity.getOrbitFocus().equals(targetPlanet)) {
                        continue;
                    }
                    if (!entity.getCustomEntityType().contains(customEntityStationTag)) {
                        continue;
                    }
                    latestStation = compareAndGetLatest(greekAlphabet, latestStation, entity);
                }
                float step = 360f / numStationsPerLayer;
                if (numStations % numStationsPerLayer == 0) {
                    step += step / 2;
                }
                assert latestStation != null;
                newStation.setCircularOrbitPointingDown(targetPlanet, (latestStation.getCircularOrbitAngle() + step) % 360f, orbitRadius, baseOrbitRadius / 10f);
            }
            newStationLights.setOrbit(newStation.getOrbit().makeCopy());

            newStation.addScript(new BoggledUnderConstructionEveryFrameScript(ctx, newStation, stationConstructionData));
            Global.getSoundPlayer().playUISound("ui_boggled_station_start_building", 1.0f, 1.0f);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class AddStationToEntity extends AddStation {
        public AddStationToEntity(String id, String[] enableSettings, String stationType, String stationName, List<String> variants, String variantOptionId, int numStationsPerLayer, float orbitRadius, BoggledStationConstructors.StationConstructionData stationConstructionData) {
            super(id, enableSettings, stationType, stationName, variants, variantOptionId, numStationsPerLayer, orbitRadius, stationConstructionData);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            SectorEntityToken playerFleet = ctx.getFleet();
            StarSystemAPI starSystem = ctx.getStarSystem();
            if (playerFleet == null || starSystem == null) {
                return;
            }

            String playerFactionId = Global.getSector().getPlayerFaction().getId();

            String customEntityStationTag = stationType + "_station";

            int numStations = boggledTools.numStationsInSystem(starSystem, stationType);

            String variantLetter = getVariant(numStations);

            String id = stationType + numStations + "_" + UUID.randomUUID();

            SectorEntityToken newStation = starSystem.addCustomEntity(id, starSystem.getBaseName() + " " + stationName + " " + getColonyNameString(numStations), customEntityStationTag + variantLetter + "_small", playerFactionId);
            SectorEntityToken newStationLights = starSystem.addCustomEntity(id + "Lights", starSystem.getName() + " " + stationName + " " + getColonyNameString(numStations) + " Lights Overlay", customEntityStationTag + variantLetter + "_small_lights_overlay", playerFactionId);

            newStation.addTag(boggledTools.BoggledTags.stationNamePrefix + stationName);

            if (boggledTools.playerFleetInAsteroidBelt(playerFleet)) {
                SectorEntityToken focus = boggledTools.getFocusOfAsteroidBelt(playerFleet);
                float orbitRadius = Misc.getDistance(focus, playerFleet);
                float orbitAngle = Misc.getAngleInDegrees(focus.getLocation(), playerFleet.getLocation());
                newStation.setCircularOrbitPointingDown(focus, orbitAngle, orbitRadius, orbitRadius / 10f);
            } else if (boggledTools.playerFleetInAsteroidField(playerFleet)) {
                OrbitAPI orbit = boggledTools.getAsteroidFieldOrbit(playerFleet);
                if (orbit != null) {
                    float orbitRadius = Misc.getDistance(orbit.getFocus(), playerFleet);
                    float orbitAngle = Misc.getAngleInDegrees(orbit.getFocus().getLocation(), playerFleet.getLocation());
                    float orbitPeriod = orbit.getOrbitalPeriod();
                    newStation.setCircularOrbitWithSpin(orbit.getFocus(), orbitAngle, orbitRadius, orbitPeriod, 5f, 10f);
                } else {
                    SectorEntityToken focus = boggledTools.getAsteroidFieldEntity(playerFleet);
                    float orbitRadius = Misc.getDistance(focus, playerFleet);
                    float orbitAngle = Misc.getAngleInDegrees(focus.getLocation(), playerFleet.getLocation());
                    newStation.setCircularOrbitWithSpin(focus, orbitAngle, orbitRadius, 40f, 5f, 10f);
                }
            }
            newStationLights.setOrbit(newStation.getOrbit().makeCopy());

            newStation.addScript(new BoggledUnderConstructionEveryFrameScript(ctx, newStation, stationConstructionData));
            Global.getSoundPlayer().playUISound("ui_boggled_station_start_building", 1.0f, 1.0f);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            stationConstructionData.addTooltipInfo(ctx, effectTypeToPara);
        }
    }

    public static class ColonizeAbandonedStation extends TerraformingProjectEffect {
        BoggledStationConstructors.StationConstructionData defaultStationConstructionData;
        List<BoggledStationConstructors.StationConstructionData> stationConstructionData;
        public ColonizeAbandonedStation(String id, String[] enableSettings, BoggledStationConstructors.StationConstructionData defaultStationConstructionData, List<BoggledStationConstructors.StationConstructionData> stationConstructionData) {
            super(id, enableSettings);
            this.defaultStationConstructionData = defaultStationConstructionData;
            this.stationConstructionData = stationConstructionData;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            SectorEntityToken playerFleet = ctx.getFleet();
            SectorEntityToken targetStation = ctx.getStation();
            StarSystemAPI starSystem = ctx.getStarSystem();
            if (playerFleet == null || targetStation == null || starSystem == null) {
                return;
            }

            targetStation.setInteractionImage("illustrations", "orbital_construction");
            targetStation.getMemoryWithoutUpdate().set("$abandonedStation", false);
            targetStation.setFaction(playerFleet.getFaction().getId());

            String id = "NewMarketForStation" + "_" + UUID.randomUUID();

            MarketAPI market = null;
            for (BoggledStationConstructors.StationConstructionData scd : stationConstructionData) {
                if (targetStation.hasTag(scd.getStationType())) {
                    market = scd.createMarket(targetStation);
                }
            }

            if (market == null) {
                market = defaultStationConstructionData.createMarket(targetStation);
            }

            if (targetStation.hasTag(boggledTools.BoggledTags.astropolisStation) || targetStation.hasTag(boggledTools.BoggledTags.miningStation) || targetStation.hasTag(boggledTools.BoggledTags.siphonStation)) {
                SectorEntityToken newLightsOnColonize = starSystem.addCustomEntity("boggled_newLightsOnColonize", "New Lights Overlay From Colonizing Abandoned Station", targetStation.getCustomEntityType() + "_lights_overlay", playerFleet.getFaction().getId());
                newLightsOnColonize.setOrbit(targetStation.getOrbit().makeCopy());
            } else if(targetStation.getId().contains("new_maxios")) {
                market.addCondition(Conditions.ORE_MODERATE);
                market.getConstructionQueue().addToEnd(Industries.MINING, 0);
            } else if(targetStation.getId().contains("laicaille_habitat")) {
                market.addCondition(Conditions.ORE_ABUNDANT);
                market.getConstructionQueue().addToEnd(Industries.MINING, 0);
            } else if(targetStation.getId().contains("thule_pirate_station")) {
                market.addCondition(Conditions.VOLATILES_DIFFUSE);
                market.addCondition(Conditions.COLD);
                market.getConstructionQueue().addToEnd(Industries.MINING, 0);
            } else if(targetStation.getId().contains("port_tse")) {
                market.addCondition(Conditions.ORE_ABUNDANT);
                market.addCondition(Conditions.RARE_ORE_RICH);
                market.getConstructionQueue().addToEnd(Industries.MINING, 0);
            } else if(targetStation.getId().contains("arcadia_station")) {
                market.addCondition(Conditions.VOLATILES_ABUNDANT);
                market.getConstructionQueue().addToEnd(Industries.MINING, 0);
            } else if(targetStation.getId().contains("tigra_city")) {
                market.addCondition(Conditions.ORE_MODERATE);
                market.getConstructionQueue().addToEnd(Industries.MINING, 0);
            }
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            SectorEntityToken targetStation = ctx.getStation();
            if (targetStation == null) {
                return;
            }
            if (!effectTypeToPara.containsKey("StationColonizationTarget")) {
                effectTypeToPara.put("StationColonizationTarget", new EffectTooltipPara("Colonization target: ", ""));
            }
            EffectTooltipPara para = effectTypeToPara.get("StationColonizationTarget");
            para.infix.add(targetStation.getName());
            para.highlights.add(targetStation.getName());
            para.highlightColors.add(Misc.getHighlightColor());
        }
    }

    public static class EffectWithRequirement extends TerraformingProjectEffect {
        BoggledProjectRequirementsAND requirements;
        List<TerraformingProjectEffect> effects;
        boolean displayRequirementTooltipOnRequirementFailure;
        boolean displayEffectTooltipOnRequirementFailure;

        public EffectWithRequirement(String id, String[] enableSettings, BoggledProjectRequirementsAND requirements, List<TerraformingProjectEffect> effects, boolean displayRequirementTooltipOnRequirementFailure, boolean displayEffectTooltipOnRequirementFailure) {
            super(id, enableSettings);
            this.requirements = requirements;
            this.effects = effects;
            this.displayRequirementTooltipOnRequirementFailure = displayRequirementTooltipOnRequirementFailure;
            this.displayEffectTooltipOnRequirementFailure = displayEffectTooltipOnRequirementFailure;
        }

        private String getRequirementsString(BoggledTerraformingRequirement.RequirementContext ctx) {
            List<BoggledCommonIndustry.TooltipData> effectSuffixes = requirements.getTooltip(ctx, boggledTools.getTokenReplacements(ctx), displayRequirementTooltipOnRequirementFailure, false);
            List<String> requirementTooltipsString = new ArrayList<>();
            for (BoggledCommonIndustry.TooltipData effectSuffix : effectSuffixes) {
                requirementTooltipsString.add(effectSuffix.text);
            }
            return Misc.getAndJoined(requirementTooltipsString);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            if (!requirements.requirementsMet(ctx)) {
                unapplyProjectEffect(ctx);
                return;
            }
            String suffix = getRequirementsString(ctx);
            if (suffix.isEmpty()) {
                suffix = effectSource;
            }
            for (TerraformingProjectEffect effect : effects) {
                effect.applyProjectEffect(ctx, suffix);
            }
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            for (TerraformingProjectEffect effect : effects) {
                effect.unapplyProjectEffect(ctx);
            }
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            if (!requirements.requirementsMet(ctx)) {
                Map<String, String> tokenReplacements = boggledTools.getTokenReplacements(ctx);
                List<BoggledCommonIndustry.TooltipData> tooltips = requirements.getTooltip(ctx, tokenReplacements, displayRequirementTooltipOnRequirementFailure, false);
                EffectTooltipPara para = new EffectTooltipPara("", "");
                for (BoggledCommonIndustry.TooltipData tooltip : tooltips) {
                    if (!para.infix.isEmpty()) {
                        para.infix.add("\n");
                    }
                    para.infix.add(tooltip.text);
                    for (Color highlightColor : tooltip.highlightColors) {
                        para.highlightColors.add(Misc.getNegativeHighlightColor());
                    }
                    para.highlights.addAll(tooltip.highlights);
                }
                effectTypeToPara.put(id, para);

                if (!displayEffectTooltipOnRequirementFailure) {
                    return;
                }
            }
            String suffix = getRequirementsString(ctx);
            for (TerraformingProjectEffect effect : effects) {
                effect.addTooltipInfoImpl(ctx, effectTypeToPara, suffix, mode, source);
            }
        }
    }

    public static class AdjustRelationsWith extends TerraformingProjectEffect {
        String factionIdToAdjustRelationsTo;
        List<String> factionIdsToAdjustRelations;
        float newRelationValue;
        public AdjustRelationsWith(String id, String[] enableSettings, String factionIdToAdjustRelationsTo, List<String> factionIdsToAdjustRelations, float newRelationValue) {
            super(id, enableSettings);
            this.factionIdToAdjustRelationsTo = factionIdToAdjustRelationsTo;
            this.factionIdsToAdjustRelations = factionIdsToAdjustRelations;
            this.newRelationValue = newRelationValue;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            for (String factionId : factionIdsToAdjustRelations) {
                Global.getSector().getFaction(factionId).setRelationship(factionIdToAdjustRelationsTo, newRelationValue);
            }
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class AdjustRelationsWithAllExcept extends TerraformingProjectEffect {
        String factionIdToAdjustRelationsTo;
        List<String> factionIdsToNotAdjustRelations;
        float newRelationValue;
        public AdjustRelationsWithAllExcept(String id, String[] enableSettings, String factionIdToAdjustRelationsTo, List<String> factionIdsToNotAdjustRelations, float newRelationValue) {
            super(id, enableSettings);
            this.factionIdToAdjustRelationsTo = factionIdToAdjustRelationsTo;
            this.factionIdsToNotAdjustRelations = factionIdsToNotAdjustRelations;
            this.newRelationValue = newRelationValue;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getPlanetMarket();
            FactionAPI marketFaction = null;
            if (market != null) {
                marketFaction = market.getFaction();
            }
            for (FactionAPI faction : Global.getSector().getAllFactions()) {
                if (factionIdsToNotAdjustRelations.contains(faction.getId()) && (faction != marketFaction)) {
                    continue;
                }
                faction.setRelationship(factionIdToAdjustRelationsTo, newRelationValue);
            }
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class TriggerMilitaryResponse extends TerraformingProjectEffect {
        float responseFraction;
        float responseDuration;

        public TriggerMilitaryResponse(String id, String[] enableSettings, float responseFraction, float responseDuration) {
            super(id, enableSettings);
            this.responseFraction = responseFraction;
            this.responseDuration = responseDuration;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }

            if (!market.getFaction().getCustomBoolean(Factions.CUSTOM_NO_WAR_SIM)) {
                MilitaryResponseScript.MilitaryResponseParams params = new MilitaryResponseScript.MilitaryResponseParams(CampaignFleetAIAPI.ActionType.HOSTILE, "player_ground_raid_" + market.getId(), market.getFaction(), market.getPrimaryEntity(), responseFraction, responseDuration);
                market.getContainingLocation().addScript(new MilitaryResponseScript(params));
            }

            for (CampaignFleetAPI fleet : market.getContainingLocation().getFleets()) {
                if (fleet.getFaction() == market.getFaction()) {
                    Misc.setFlagWithReason(fleet.getMemoryWithoutUpdate(),  MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF, "raidAlarm", true, 1f);
                }
            }
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class DecivilizeMarket extends TerraformingProjectEffect {
        List<String> factionIdsToNotMakeHostile;
        public DecivilizeMarket(String id, String[] enableSettings, List<String> factionIdsToNotMakeHostile) {
            super(id, enableSettings);
            this.factionIdsToNotMakeHostile = factionIdsToNotMakeHostile;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }
            MemoryAPI mem = Global.getSector().getCharacterData().getMemoryWithoutUpdate();
            int atrocities = (int) mem.getFloat(MemFlags.PLAYER_ATROCITIES);
            atrocities++;
            mem.set(MemFlags.PLAYER_ATROCITIES, atrocities);

            List<FactionAPI> factionsToNotMakeHostile = new ArrayList<>();
            for (FactionAPI faction : Global.getSector().getAllFactions()) {
                if (factionIdsToNotMakeHostile.contains(faction.getId())) {
                    continue;
                }
                factionsToNotMakeHostile.add(faction);
            }

            // Added per Histidine's comments in the forum - see Page 148, comment #2210 in the TASC thread.
            // If you're reading this because it's not working properly for what you're trying to do, let me know!
            MarketCMD.TempData actionData = new MarketCMD.TempData();
            actionData.bombardType = MarketCMD.BombardType.SATURATION;
            actionData.willBecomeHostile = factionsToNotMakeHostile;

            ListenerUtil.reportSaturationBombardmentFinished(null, market, actionData);

            DecivTracker.decivilize(market, true);
            MarketCMD.addBombardVisual(market.getPrimaryEntity());
            MarketCMD.addBombardVisual(market.getPrimaryEntity());
            MarketCMD.addBombardVisual(market.getPrimaryEntity());

            // Copied from MarketCMD saturation bombing code.
            InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
            if (dialog != null && dialog.getPlugin() instanceof RuleBasedDialog) {
                if (dialog.getInteractionTarget() != null && dialog.getInteractionTarget().getMarket() != null) {
                    Global.getSector().setPaused(false);
                    dialog.getInteractionTarget().getMarket().getMemoryWithoutUpdate().advance(0.0001f);
                    Global.getSector().setPaused(true);
                }

                ((RuleBasedDialog) dialog.getPlugin()).updateMemory();
            }
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            MarketAPI targetMarket = ctx.getClosestMarket();
            if (targetMarket == null) {
                return;
            }
            SectorEntityToken target = targetMarket.getPrimaryEntity();
            if (target == null) {
                return;
            }
            EffectTooltipPara para = effectTypeToPara.get("DecivilizationTarget");
            if (para == null) {
                effectTypeToPara.put("DecivilizationTarget", new EffectTooltipPara("Target colony: ", ""));
                para = effectTypeToPara.get("DecivilizationTarget");
            }

            para.infix.add(target.getName());
            para.highlights.add(target.getName());
            para.highlightColors.add(Misc.getHighlightColor());
        }
    }

    public static class Modifier {
        private enum StatModType {
            FLAT,
            MULT,
            PERCENT,
            MARKET_SIZE
        }
        private final String id;
        private StatModType modifierType;
        private StatModType displayType;
        private final float value;

        private StatModType getModType(String modType) {
            switch (modType) {
                case "flat": return StatModType.FLAT;
                case "mult": return StatModType.MULT;
                case "percent": return StatModType.PERCENT;
                case "market_size": return StatModType.MARKET_SIZE;
            }
            throw new RuntimeException("Industry effect " + id + " has invalid mod type string " + modifierType);
        }

        public Modifier(String id, String modifierType, float value, String displayType) {
            this.modifierType = getModType(modifierType);
            this.displayType = getModType(displayType);
            this.id = id;
            this.value = value;
        }

        public MutableStat createModifier(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MutableStat mod = new MutableStat(0);
            switch (modifierType) {
                case MARKET_SIZE: mod.modifyFlat(id, ctx.getClosestMarket().getSize() + value, effectSource); break;
                case FLAT: mod.modifyFlat(id, value, effectSource); break;
                case MULT: mod.modifyMult(id, value, effectSource); break;
                case PERCENT: mod.modifyPercent(id, value, effectSource); break;
            }
            return mod;
        }

        public ModifierStrings getModifierStrings(BoggledTerraformingRequirement.RequirementContext ctx, Color positiveHighlightColor, Color negativeHighlightColor, TerraformingProjectEffect.DescriptionSource source, boolean invertIncreaseReduce, EffectStringVariants effect) {
            return new ModifierStrings(ctx, modifierType, displayType, value, positiveHighlightColor, negativeHighlightColor, source, invertIncreaseReduce, effect);
        }

        public static class EffectStringVariants {
            public String positive;
            public String negative;

            public EffectStringVariants(String positive, String negative) {
                this.positive = positive;
                this.negative = negative;
            }
        }

        public static class ModifierStrings {
            String increasesOrReduces;
            String IncreasesOrReduces;
            String increasedOrReduced;
            String IncreasedOrReduced;

            String bonusStringPrefix;
            String byOrTo;

            String bonusString;
            String highlightString;
            Color highlightColor;

            String suffix;

            Color positiveHighlightColor;
            Color negativeHighlightColor;

            String effect;

            private void setToIncrease(EffectStringVariants effect) {
                highlightColor = positiveHighlightColor;
                this.increasesOrReduces = "increases";
                this.IncreasesOrReduces = "Increases";
                this.increasedOrReduced = "increased";
                this.IncreasedOrReduced = "Increased";
                this.effect = effect.positive;
            }

            private void setToReduce(EffectStringVariants effect) {
                highlightColor = negativeHighlightColor;
                this.increasesOrReduces = "reduces";
                this.IncreasesOrReduces = "Reduces";
                this.increasedOrReduced = "reduced";
                this.IncreasedOrReduced = "Reduced";
                this.effect = effect.negative;
            }

            private String formatBonusString(float value, int numPlacesAfterDecimal) {
                if (value % 1 < 0.0001f) {
                    return String.format("%." + numPlacesAfterDecimal + "f", Math.abs(value));
                }
                return String.format("%.2f", Math.abs(value));
            }

            float modifyValueForDisplay(StatModType modType, StatModType displayType, float value) {
                if (modType == displayType) {
                    return value;
                }

                if (modType == StatModType.MULT && displayType == StatModType.PERCENT) {
                    value = -(1 - value) * 100;
                }
                return value;
            }

            private void setToIncrease(EffectStringVariants effect, boolean invertIncreaseReduce) {
                if (invertIncreaseReduce) {
                    setToReduce(effect);
                } else {
                    setToIncrease(effect);
                }
            }

            private void setToReduce(EffectStringVariants effect, boolean invertIncreaseReduce) {
                if (invertIncreaseReduce) {
                    setToIncrease(effect);
                } else {
                    setToReduce(effect);
                }
            }

            ModifierStrings(BoggledTerraformingRequirement.RequirementContext ctx, StatModType modType, StatModType displayType, float baseValue, Color positiveHighlightColor, Color negativeHighlightColor, TerraformingProjectEffect.DescriptionSource source, boolean invertIncreaseReduce, EffectStringVariants effect) {
                this.positiveHighlightColor = positiveHighlightColor;
                this.negativeHighlightColor = negativeHighlightColor;
                suffix = "";
                bonusStringPrefix = "";
                float value = modifyValueForDisplay(modType, displayType, baseValue);
                switch (displayType) {
                    case MARKET_SIZE:
                        suffix = "(based on colony size)";
                        value += ctx.getClosestMarket().getSize();
                    case FLAT: {
                        if (value < 0) {
                            setToReduce(effect, invertIncreaseReduce);
                            if (   source != TerraformingProjectEffect.DescriptionSource.AI_CORE_DESCRIPTION
                                && source != TerraformingProjectEffect.DescriptionSource.IMPROVE_DESCRIPTION) {
                                bonusStringPrefix = "-";
                            }
                            bonusString = formatBonusString(Math.abs(value), 0);
                        } else {
                            setToIncrease(effect, invertIncreaseReduce);
                            if (   source != TerraformingProjectEffect.DescriptionSource.AI_CORE_DESCRIPTION
                                && source != TerraformingProjectEffect.DescriptionSource.IMPROVE_DESCRIPTION) {
                                bonusStringPrefix = "+";
                            }
                            bonusString = formatBonusString(value, 0);
                        }
                        byOrTo = "by";
                        highlightString = bonusStringPrefix + bonusString;
                        break;
                    }
                    case MULT: {
                        if (value < 1) {
                            setToReduce(effect, invertIncreaseReduce);
                        } else {
                            setToIncrease(effect, invertIncreaseReduce);
                        }
                        byOrTo = "by";
                        highlightString = Strings.X + formatBonusString(value, 1);
                        bonusString = highlightString;
                        break;
                    }
                    case PERCENT: {
                        if (value < 0) {
                            setToReduce(effect, invertIncreaseReduce);
                        } else {
                            setToIncrease(effect, invertIncreaseReduce);
                        }
                        byOrTo = "by";
                        highlightString = formatBonusString(value, 0) + "%";
                        bonusString = highlightString + "%";
                        break;
                    }
                }
            }
        }
    }

    private static abstract class ModifierEffect extends TerraformingProjectEffect {
        Modifier mod;
        protected ModifierEffect(String id, String[] enableSettings, String modifierType, float value, String displayType) {
            super(id, enableSettings);
            mod = new Modifier(id, modifierType, value, displayType);
        }

        private String ucFirst(String str, boolean modify) {
            if (modify) {
                return Misc.ucFirst(str);
            }
            return str;
        }

        private String lcFirst(String str, boolean modify) {
            if (modify) {
                return Misc.lcFirst(str);
            }
            return str;
        }

        protected EffectTooltipPara createTooltipData(BoggledTerraformingRequirement.RequirementContext ctx, String effect, boolean modifyEffectCase, String effectSource, String suffix, DescriptionMode mode, DescriptionSource source, Color positiveHighlight, Color negativeHighlight) {
            Modifier.EffectStringVariants effectVariants = new Modifier.EffectStringVariants(effect, effect);
            return createTooltipData(ctx, effectVariants, modifyEffectCase, effectSource, suffix, mode, source, positiveHighlight, negativeHighlight, false);
        }

        protected EffectTooltipPara createTooltipData(BoggledTerraformingRequirement.RequirementContext ctx, String effect, boolean modifyEffectCase, String effectSource, String suffix, DescriptionMode mode, DescriptionSource source, Color positiveHighlight, Color negativeHighlight, boolean invertIncreaseReduce) {
            Modifier.EffectStringVariants effectVariants = new Modifier.EffectStringVariants(effect, effect);
            return createTooltipData(ctx, effectVariants, modifyEffectCase, effectSource, suffix, mode, source, positiveHighlight, negativeHighlight, invertIncreaseReduce);
        }

        protected EffectTooltipPara createTooltipData(BoggledTerraformingRequirement.RequirementContext ctx, Modifier.EffectStringVariants effect, boolean modifyEffectCase, String effectSource, String suffix, DescriptionMode mode, DescriptionSource source, Color positiveHighlight, Color negativeHighlight) {
            return createTooltipData(ctx, effect, modifyEffectCase, effectSource, suffix, mode, source, positiveHighlight, negativeHighlight, false);
        }

        protected EffectTooltipPara createTooltipData(BoggledTerraformingRequirement.RequirementContext ctx, Modifier.EffectStringVariants effect, boolean modifyEffectCase, String effectSource, String suffix, DescriptionMode mode, DescriptionSource source, Color positiveHighlight, Color negativeHighlight, boolean invertIncreaseReduce) {
            Modifier.ModifierStrings modStrings = mod.getModifierStrings(ctx, positiveHighlight, negativeHighlight, source, invertIncreaseReduce, effect);
            String text;
            if (source == DescriptionSource.POST_DEMAND_SECTION) {
                text = ucFirst(modStrings.effect, modifyEffectCase) + ": " + modStrings.bonusStringPrefix + modStrings.bonusString;
            } else {
                if (mode == DescriptionMode.APPLIED) {
                    text = ucFirst(modStrings.effect, modifyEffectCase) + " " + modStrings.increasedOrReduced + " " + modStrings.byOrTo + " " + modStrings.bonusString;
                } else {
                    text = modStrings.IncreasesOrReduces + " " + lcFirst(modStrings.effect, modifyEffectCase) + " " + modStrings.byOrTo + " " + modStrings.bonusString;
                }
            }

            if (!suffix.isEmpty()) {
                text += " " + suffix;
            }

            if (source != DescriptionSource.POST_DEMAND_SECTION) {
                text += ".";
            }


            EffectTooltipPara ret = new EffectTooltipPara(text, "");
            ret.highlights.add(modStrings.highlightString);
            ret.highlightColors.add(modStrings.highlightColor);
            return ret;
        }
    }

    public static class ModifyPatherInterest extends ModifierEffect {
        public ModifyPatherInterest(String id, String[] enableSettings, String modifierType, float value, String displayType) {
            super(id, enableSettings, modifierType, value, displayType);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BoggledIndustryInterface targetIndustryInterface = ctx.getTargetIndustryInterface();
            if (targetIndustryInterface == null) {
                return;
            }
            targetIndustryInterface.modifyPatherInterest(mod.createModifier(ctx, effectSource));
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BoggledIndustryInterface targetIndustryInterface = ctx.getTargetIndustryInterface();
            if (targetIndustryInterface == null) {
                return;
            }
            targetIndustryInterface.unmodifyPatherInterest(id);
        }
    }

    public static class ModifyColonyGrowthRate extends ModifierEffect {
        public ModifyColonyGrowthRate(String id, String[] enableSettings, String modifierType, float value, String displayType) {
            super(id, enableSettings, modifierType, value, displayType);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BoggledIndustryInterface targetIndustryInterface = ctx.getTargetIndustryInterface();
            if (targetIndustryInterface == null) {
                return;
            }
            targetIndustryInterface.modifyImmigration(mod.createModifier(ctx, effectSource));
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BoggledIndustryInterface targetIndustryInterface = ctx.getTargetIndustryInterface();
            if (targetIndustryInterface == null) {
                return;
            }
            targetIndustryInterface.unmodifyImmigration(id);
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            effectTypeToPara.put("ModifyColonyGrowthRate", createTooltipData(ctx, "population growth", true, effectSource, "", mode, source, Misc.getHighlightColor(), Misc.getNegativeHighlightColor()));
        }
    }

    public static class ModifyColonyGroundDefense extends ModifierEffect {
        public ModifyColonyGroundDefense(String id, String[] enableSettings, String modifierType, float value, String displayType) {
            super(id, enableSettings, modifierType, value, displayType);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getMarket().getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).applyMods(mod.createModifier(ctx, effectSource));
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getMarket().getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            String effect;
            if (source == DescriptionSource.POST_DEMAND_SECTION) {
                effect = "ground defense strength";
            } else {
                effect = "ground defenses";
            }
            effectTypeToPara.put("ModifyColonyGroundDefense", createTooltipData(ctx, effect, true, effectSource, "", mode, source, Misc.getHighlightColor(), Misc.getNegativeHighlightColor()));
        }
    }

    public static class ModifyColonyAccessibility extends ModifierEffect {
        public ModifyColonyAccessibility(String id, String[] enableSettings, String modifierType, float value, String displayType) {
            super(id, enableSettings, modifierType, value, displayType);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getMarket().getAccessibilityMod().applyMods(mod.createModifier(ctx, effectSource));
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getMarket().getAccessibilityMod().unmodify(id);
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            Modifier.EffectStringVariants effectVariants = new Modifier.EffectStringVariants("accessibility bonus", "accessibility penalty");
            effectTypeToPara.put("ModifyColonyAccessibility", createTooltipData(ctx, effectVariants, true, effectSource, "", mode, source, Misc.getHighlightColor(), Misc.getNegativeHighlightColor()));
        }
    }

    public static class ModifyColonyStability extends ModifierEffect {
        public ModifyColonyStability(String id, String[] enableSettings, String modifierType, float value, String displayType) {
            super(id, enableSettings, modifierType, value, displayType);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getMarket().getStability().applyMods(mod.createModifier(ctx, effectSource));
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getMarket().getStability().unmodify(id);
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            effectTypeToPara.put("ModifyColonyStability", createTooltipData(ctx, "stability", true, effectSource, "", mode, source, Misc.getHighlightColor(), Misc.getNegativeHighlightColor()));
        }
    }

    public static class ModifyIndustryUpkeep extends ModifierEffect {
        public ModifyIndustryUpkeep(String id, String[] enableSettings, String modifierType, float value, String displayType) {
            super(id, enableSettings, modifierType, value, displayType);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getUpkeep().applyMods(mod.createModifier(ctx, effectSource));
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getUpkeep().unmodify(id);
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            effectTypeToPara.put("ModifyIndustryUpkeep", createTooltipData(ctx, "upkeep cost", true, effectSource, "", mode, source, Misc.getNegativeHighlightColor(), Misc.getHighlightColor()));
        }
    }

    public static class ModifyIndustryIncome extends ModifierEffect {
        public ModifyIndustryIncome(String id, String[] enableSettings, String modifierType, float value, String displayType) {
            super(id, enableSettings, modifierType, value, displayType);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getIncome().applyMods(mod.createModifier(ctx, effectSource));
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getIncome().unmodify(id);
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            effectTypeToPara.put("ModifyIndustryIncome", createTooltipData(ctx, "income", true, effectSource, "", mode, source, Misc.getHighlightColor(), Misc.getNegativeHighlightColor()));
        }
    }

    public static class ModifyIndustryIncomeByAccessibility extends ModifierEffect {
        protected ModifyIndustryIncomeByAccessibility(String id, String[] enableSettings, String modifierType, float value, String displayType) {
            super(id, enableSettings, modifierType, value, displayType);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            MarketAPI market = ctx.getClosestMarket();
            if (targetIndustry == null || market == null) {
                return;
            }
            float accessMult = market.getAccessibilityMod().computeEffective(0.0f);
            targetIndustry.getIncome().modifyMult(id, accessMult, "Accessibility");
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getIncome().unmodify(id);
        }
    }

    public static class ModifyIndustrySupplyWithDeficit extends ModifierEffect {
        List<String> commoditiesDemanded;
        int maxDeficit;
        public ModifyIndustrySupplyWithDeficit(String id, String[] enableSettings, List<String> commoditiesDemanded, String modifierType, float value, int maxDeficit, String displayType) {
            super(id, enableSettings, modifierType, value, displayType);
            this.commoditiesDemanded = commoditiesDemanded;
            this.maxDeficit = maxDeficit;
        }

        private int getBonus(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource, BaseIndustry sourceIndustry) {
            Pair<String, Integer> deficit = sourceIndustry.getMaxDeficit(commoditiesDemanded.toArray(new String[0]));
            deficit.two = Math.min(deficit.two, maxDeficit);
            return Math.max(0, mod.createModifier(ctx, effectSource).getModifiedInt() - deficit.two);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BaseIndustry sourceIndustry = ctx.getSourceIndustry();
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (sourceIndustry == null || targetIndustry == null) {
                return;
            }

            if (!sourceIndustry.isFunctional()) {
                unapplyProjectEffectImpl(ctx);
            }

            int bonus = getBonus(ctx, effectSource, sourceIndustry);
            targetIndustry.getSupplyBonusFromOther().modifyFlat(id, bonus, effectSource);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getSupplyBonusFromOther().unmodify(id);
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            effectTypeToPara.put("ModifyIndustrySupplyWithDeficit", createTooltipData(ctx, "production", true, effectSource, "unit", mode, source, Misc.getHighlightColor(), Misc.getNegativeHighlightColor()));
        }
    }

    public static class ModifyIndustryDemand extends ModifierEffect {
        public ModifyIndustryDemand(String id, String[] enableSettings, String modifierType, float value, String displayType) {
            super(id, enableSettings, modifierType, -value, displayType);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            MutableStat modifier = mod.createModifier(ctx, effectSource);
            targetIndustry.getDemandReductionFromOther().applyMods(modifier);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getDemandReductionFromOther().unmodify(id);
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            effectTypeToPara.put("ModifyIndustryDemand", createTooltipData(ctx, "demand", true, effectSource, "unit", mode, source, Misc.getNegativeHighlightColor(), Misc.getHighlightColor(), true));
        }
    }

    public static class EffectToIndustry extends TerraformingProjectEffect {
        String industryId;
        TerraformingProjectEffect effect;
        public EffectToIndustry(String id, String[] enableSettings, String industryId, TerraformingProjectEffect effect) {
            super(id, enableSettings);
            this.industryId = industryId;
            this.effect = effect;
        }

        private BoggledTerraformingRequirement.RequirementContext getTargetIndustryCtx(BoggledTerraformingRequirement.RequirementContext ctx) {
            BaseIndustry sourceIndustry = ctx.getSourceIndustry();
            if (sourceIndustry == null) {
                return null;
            }
            BaseIndustry targetIndustry = (BaseIndustry) sourceIndustry.getMarket().getIndustry(industryId);
            if (targetIndustry == null) {
                return null;
            }
            return new BoggledTerraformingRequirement.RequirementContext(ctx, targetIndustry);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BoggledTerraformingRequirement.RequirementContext targetCtx = getTargetIndustryCtx(ctx);
            if (targetCtx == null) {
                return;
            }
            effect.applyProjectEffectImpl(targetCtx, effectSource);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BoggledTerraformingRequirement.RequirementContext targetCtx = getTargetIndustryCtx(ctx);
            if (targetCtx == null) {
                return;
            }
            effect.unapplyProjectEffectImpl(targetCtx);
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            BaseIndustry sourceIndustry = ctx.getSourceIndustry();
            BoggledTerraformingRequirement.RequirementContext targetCtx = getTargetIndustryCtx(ctx);
            if (sourceIndustry == null || targetCtx == null) {
                return;
            }
            effect.addTooltipInfoImpl(targetCtx, effectTypeToPara, effectSource, mode, source);
            for (EffectTooltipPara para : effectTypeToPara.values()) {
                para.prefix = sourceIndustry.getNameForModifier() + " " + Misc.lcFirst(para.prefix);
            }
        }
    }

    public static class SuppressConditions extends TerraformingProjectEffect {
        List<String> conditionIds;
        public SuppressConditions(String id, String[] enableSettings, List<String> conditionIds) {
            super(id, enableSettings);
            this.conditionIds = conditionIds;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            MarketAPI market = targetIndustry.getMarket();
            for (String conditionId : conditionIds) {
                if (conditionId.equals(Conditions.WATER_SURFACE) && market.hasCondition(conditionId)) {
                    // Suppress water surface without actually suppressing it
                    // Actually suppressing it causes aquaculture to produce no food
                    market.getHazard().modifyFlat(id, -0.25f, effectSource);
                } else {
                    market.suppressCondition(conditionId);
                }
            }
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            MarketAPI market = targetIndustry.getMarket();
            for (String conditionId : conditionIds) {
                if (conditionId.equals(Conditions.WATER_SURFACE)) {
                    market.getHazard().unmodify(id);
                } else {
                    market.unsuppressCondition(conditionId);
                }
            }
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }

            EffectTooltipPara ret = effectTypeToPara.get("SuppressConditions");
            if (ret == null) {
                ret = new EffectTooltipPara();
                effectTypeToPara.put("SuppressConditions", ret);
                if (mode == DescriptionMode.TO_APPLY) {
                    ret.prefix = "If operational, would counter the effects of:";
                } else {
                    ret.prefix = "Countering the effects of:";
                }
            }

            List<String> infix = new ArrayList<>();

            MarketAPI targetMarket = targetIndustry.getMarket();
            for (String conditionId : conditionIds) {
                if (targetMarket.hasCondition(conditionId)) {
                    String conditionName = Global.getSettings().getMarketConditionSpec(conditionId).getName();
                    infix.add("\n           " + conditionName);
                    ret.highlights.add(conditionName);
                    ret.highlightColors.add(Misc.getHighlightColor());
                }
            }
            if (ret.highlights.isEmpty()) {
                ret.suffix = "\n           (None)";
                ret.highlights.add("(None)");
                ret.highlightColors.add(Misc.getGrayColor());
            } else {
                ret.infix.addAll(infix);
                ret.suffix = "";
            }
        }
    }

    public static class IndustryMonthlyItemProduction extends TerraformingProjectEffect {
        protected IndustryMonthlyItemProduction(String id, String[] enableSettings) {
            super(id, enableSettings);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BoggledIndustryInterface industryInterface = ctx.getTargetIndustryInterface();
            if (industryInterface == null) {
                return;
            }
            industryInterface.setEnableMonthlyProduction(true);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BoggledIndustryInterface industryInterface = ctx.getTargetIndustryInterface();
            if (industryInterface == null) {
                return;
            }
            industryInterface.setEnableMonthlyProduction(false);
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            BoggledIndustryInterface targetIndustryInterface = ctx.getTargetIndustryInterface();
            if (targetIndustryInterface == null) {
                return;
            }
            List<BoggledCommonIndustry.ProductionData> data = targetIndustryInterface.getProductionData();

            String chanceOrChances = data.size() == 1 ? "Chance" : "Chances";

            EffectTooltipPara para = effectTypeToPara.get("IndustryMonthlyItemProductionChance");
            if (para == null) {
                effectTypeToPara.put("IndustryMonthlyItemProductionChance", new EffectTooltipPara(chanceOrChances + " of producing items (base): ", ""));
                para = effectTypeToPara.get("IndustryMonthlyItemProductionChance");
            }

            Pair<Integer, Integer> chance100 = new Pair<>(100, 100);
            Map<String, String> tokenReplacements = boggledTools.getTokenReplacements(ctx);
            for (BoggledCommonIndustry.ProductionData datum : data) {
                boolean requirementsMet = datum.requirements.requirementsMet(ctx);
                Pair<Integer, Integer> chance = targetIndustryInterface.getProductionChance(datum.commodityId);
                chance100.one = Math.min(100 - chance.one, chance100.one);
                chance100.two = Math.min(100 - chance.two, chance100.two);
                Color highlightColor = Misc.getHighlightColor();
                if (!requirementsMet) {
                    highlightColor = Misc.getNegativeHighlightColor();
                }
                String modifiedPercentString = chance.two + "%";
                String basePercentString = chance.one + "%";
                para.highlights.add(modifiedPercentString);
                para.highlights.add(basePercentString);
                para.highlightColors.add(highlightColor);
                para.highlightColors.add(highlightColor);

                modifiedPercentString += "%";
                basePercentString += "%";
                StringBuilder chanceString = new StringBuilder("\n    ").append(Global.getSettings().getCommoditySpec(datum.commodityId).getName()).append(": ").append(modifiedPercentString).append(" (").append(basePercentString).append(")");

                if (!requirementsMet) {
                    List<BoggledCommonIndustry.TooltipData> requirementTooltips = datum.requirements.getTooltip(ctx, tokenReplacements, true, false);
                    for (BoggledCommonIndustry.TooltipData tooltip : requirementTooltips) {
                        String tooltipText = Misc.lcFirst(tooltip.text);
                        chanceString.append(", ").append(tooltipText);
                        para.highlights.add(tooltipText);
                        para.highlightColors.add(Misc.getNegativeHighlightColor());
                    }
                }

                para.infix.add(chanceString.toString());
            }

            chance100.one = Math.max(chance100.one, 0);
            chance100.two = Math.max(chance100.two, 0);
            String modifiedChance100String = chance100.two + "%";
            String baseChance100String = chance100.one + "%";
            para.highlights.add(0, baseChance100String);
            para.highlights.add(0, modifiedChance100String);
            para.highlightColors.add(0, Misc.getHighlightColor());
            para.highlightColors.add(0, Misc.getHighlightColor());
            modifiedChance100String += "%";
            baseChance100String += "%";
            para.infix.add(0, "\n    Nothing: " + modifiedChance100String + " (" + baseChance100String + ")");
            if (boggledTools.getBooleanSetting("boggledKletkaSimulatorShowProductionRoll")) {
                int lastProductionRoll = targetIndustryInterface.getLastProductionRoll();
                para.highlights.add(Integer.toString(lastProductionRoll));
                para.highlightColors.add(Misc.getHighlightColor());
                para.infix.add("\nLast roll: " + lastProductionRoll + " (Roll must be less than the stated %% in order to receive that item)");
            }
        }
    }

    public static class IndustryMonthlyItemProductionChance extends TerraformingProjectEffect {
        protected List<BoggledCommonIndustry.ProductionData> data;
        public IndustryMonthlyItemProductionChance(String id, String[] enableSettings, List<BoggledCommonIndustry.ProductionData> data) {
            super(id, enableSettings);
            this.data = data;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BoggledIndustryInterface targetIndustryInterface = ctx.getTargetIndustryInterface();
            if (targetIndustryInterface == null) {
                return;
            }
            for (BoggledCommonIndustry.ProductionData datum : data) {
                targetIndustryInterface.addProductionData(datum);
            }
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BoggledIndustryInterface targetIndustryInterface = ctx.getTargetIndustryInterface();
            if (targetIndustryInterface == null) {
                return;
            }
            for (BoggledCommonIndustry.ProductionData datum : data) {
                targetIndustryInterface.removeProductionData(datum);
            }
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {

        }
    }

    public static class IndustryMonthlyItemProductionChanceModifier extends TerraformingProjectEffect {
        List<Pair<String, Integer>> data;
        public IndustryMonthlyItemProductionChanceModifier(String id, String[] enableSettings, List<Pair<String, Integer>> data) {
            super(id, enableSettings);
            this.data = data;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BoggledIndustryInterface targetIndustryInterface = ctx.getTargetIndustryInterface();
            if (targetIndustryInterface == null) {
                return;
            }
            for (Pair<String, Integer> datum : data) {
                targetIndustryInterface.modifyProductionChance(datum.one, id, datum.two);
            }
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BoggledIndustryInterface targetIndustryInterface = ctx.getTargetIndustryInterface();
            if (targetIndustryInterface == null) {
                return;
            }
            for (Pair<String, Integer> datum : data) {
                targetIndustryInterface.unmodifyProductionChance(datum.one, id);
            }
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            String chanceOrChances = data.size() == 1 ? "chance" : "chances";

            EffectTooltipPara para = effectTypeToPara.get("IndustryMonthlyItemProductionChance");
            if (para == null) {
                effectTypeToPara.put("IndustryMonthlyItemProductionChance", new EffectTooltipPara("Improves " + chanceOrChances + " of producing items: ", ""));
                para = effectTypeToPara.get("IndustryMonthlyItemProductionChance");
            }

            List<String> commodityAndChance = new ArrayList<>();
            for (Pair<String, Integer> datum : data) {
                String commodityName = Global.getSettings().getCommoditySpec(datum.one).getName();
                String chanceString = datum.two + "%";
                para.highlights.add(chanceString);
                para.highlightColors.add(Misc.getHighlightColor());
                commodityAndChance.add(commodityName + " by " + chanceString + "%");
            }
            String text = Misc.getAndJoined(commodityAndChance);
            para.infix.add(text);
        }
    }

    public static class StepTag extends TerraformingProjectEffect {
        String tag;
        int step;
        public StepTag(String id, String[] enableSettings, String tag, int step) {
            super(id, enableSettings);
            this.tag = tag;
            this.step = step;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }
            for (String tag : market.getTags()) {
                if (tag.startsWith(this.tag)) {
                    int tagValueOld = Integer.parseInt(tag.substring(this.tag.length()));
                    market.removeTag(tag);
                    market.addTag(this.tag + (tagValueOld + step));
                    return;
                }
            }
            market.addTag(this.tag + step);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class IndustryRemove extends TerraformingProjectEffect {
        String industryIdToRemove;
        public IndustryRemove(String id, String[] enableSettings, String industryIdToRemove) {
            super(id, enableSettings);
            this.industryIdToRemove = industryIdToRemove;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }

            Industry targetIndustry = market.getIndustry(industryIdToRemove);
            CargoAPI cargo = market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();
            if (targetIndustry != null && cargo != null) {
                String aiCoreId = targetIndustry.getAICoreId();
                if (aiCoreId != null) {
                    cargo.addCommodity(aiCoreId, 1);
                }

                SpecialItemData specialItem = targetIndustry.getSpecialItem();
                if (specialItem != null) {
                    cargo.addSpecial(specialItem, 1);
                }

                for (InstallableIndustryItemPlugin installableItem : targetIndustry.getInstallableItems()) {
                    if (installableItem.getCurrentlyInstalledItemData() != null) {
                        cargo.addSpecial(installableItem.getCurrentlyInstalledItemData(), 1);
                    }
                }
            }
            market.removeIndustry(industryIdToRemove, null, false);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class TagSubstringPowerModifyBuildCost extends TerraformingProjectEffect {
        MutableStat modifier;
        String tagSubstring;
        int tagDefault;
        public TagSubstringPowerModifyBuildCost(String id, String[] enableSettings, String tagSubstring, int tagDefault) {
            super(id, enableSettings);
            this.modifier = new MutableStat(0);
            this.tagSubstring = tagSubstring;
            this.tagDefault = tagDefault;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            BoggledIndustryInterface sourceInterface = ctx.getSourceIndustryInterface();
            if (market == null || sourceInterface == null) {
                return;
            }

            int tagCount = tagDefault;
            for (String tag : market.getTags()) {
                if (tag.startsWith(tagSubstring)) {
                    tagCount = Integer.parseInt(tag.substring(tagSubstring.length()));
                    break;
                }
            }
            modifier.modifyMult(id, (float) Math.pow(2, tagCount));
            sourceInterface.modifyBuildCost(modifier);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BoggledIndustryInterface sourceInterface = ctx.getSourceIndustryInterface();
            if (sourceInterface == null) {
                return;
            }
            sourceInterface.unmodifyBuildCost(id);
        }
    }

    public static class EliminatePatherInterest extends TerraformingProjectEffect {
        protected EliminatePatherInterest(String id, String[] enableSettings) {
            super(id, enableSettings);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            MarketAPI market = ctx.getClosestMarket();
            BoggledIndustryInterface sourceInterface = ctx.getSourceIndustryInterface();
            if (market == null || sourceInterface == null) {
                return;
            }
            float patherInterest = 0;
            if (market.getAdmin().getAICoreId() != null) {
                patherInterest += 10;
            }

            for (Industry industry : market.getIndustries()) {
                if (industry.isHidden()) {
                    continue;
                }
                patherInterest += industry.getPatherInterest();
            }

            MutableStat modifier = new MutableStat(0f);
            modifier.modifyFlat(id, -patherInterest, effectSource);
            sourceInterface.modifyPatherInterest(modifier);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BoggledIndustryInterface sourceInterface = ctx.getSourceIndustryInterface();
            if (sourceInterface == null) {
                return;
            }
            sourceInterface.unmodifyPatherInterest(id);
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara, String effectSource, DescriptionMode mode, DescriptionSource source) {
            MarketAPI market = ctx.getClosestMarket();
            if (market == null) {
                return;
            }
            EffectTooltipPara para = new EffectTooltipPara("Eliminates pather interest on ", ".");
            para.infix.add(market.getName());
            para.highlights.add(market.getName());
            para.highlightColors.add(Misc.getHighlightColor());
            effectTypeToPara.put("EliminatePatherInterest", para);
        }
    }

    public static abstract class CommodityDeficit extends TerraformingProjectEffect {
        List<String> commodityIds;
        public CommodityDeficit(String id, String[] enableSettings, List<String> commodityIds) {
            super(id, enableSettings);
            this.commodityIds = commodityIds;
        }
    }

    public static class CommodityDeficitToProduction extends CommodityDeficit {
        List<String> commoditiesDeficited;
        int maxDeficit;
        public CommodityDeficitToProduction(String id, String[] enableSettings, List<String> commodityIds, List<String> commoditiesDeficited, int maxDeficit) {
            super(id, enableSettings, commodityIds);
            this.commoditiesDeficited = commoditiesDeficited;
            this.maxDeficit = maxDeficit;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BaseIndustry sourceIndustry = ctx.getSourceIndustry();
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            BoggledIndustryInterface targetIndustryInterface = ctx.getTargetIndustryInterface();
            if (sourceIndustry == null || targetIndustry == null || targetIndustryInterface == null) {
                return;
            }
            Pair<String, Integer> deficit = sourceIndustry.getMaxDeficit(commodityIds.toArray(new String[0]));
            deficit.two = Math.min(deficit.two, maxDeficit);
            targetIndustryInterface.applyDeficitToProduction(id + "_DeficitToCommodity", deficit, commoditiesDeficited.toArray(new String[0]));
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
        }
    }

    public static class CommodityDeficitModifierToUpkeep extends CommodityDeficit {
        Modifier mod;
        public CommodityDeficitModifierToUpkeep(String id, String[] enableSettings, List<String> commodityIds, String modifierType, float value, String displayType) {
            super(id, enableSettings, commodityIds);
            this.mod = new Modifier(id, modifierType, value, displayType);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BaseIndustry sourceIndustry = ctx.getSourceIndustry();
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (sourceIndustry == null || targetIndustry == null) {
                return;
            }
            List<Pair<String, Integer>> deficits = sourceIndustry.getAllDeficit(commodityIds.toArray(new String[0]));
            if (deficits.isEmpty()) {
                unapplyProjectEffectImpl(ctx);
            } else {
                String deficitString = "Commodity shortage";
                if (deficits.size() == 1) {
                    deficitString = Misc.ucFirst(Global.getSettings().getCommoditySpec(deficits.get(0).one).getLowerCaseName()) + " shortage";
                }
                targetIndustry.getUpkeep().applyMods(mod.createModifier(ctx, deficitString));
            }
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (targetIndustry == null) {
                return;
            }
            targetIndustry.getUpkeep().unmodify(id);
        }
    }

    public static abstract class CommoditySupplyDemand extends TerraformingProjectEffect {
        String commodityId;
        int quantity;
        public CommoditySupplyDemand(String id, String[] enableSettings, String commodityId, int quantity) {
            super(id, enableSettings);
            this.commodityId = commodityId;
            this.quantity = quantity;
        }

        protected abstract int getQuantity(BoggledTerraformingRequirement.RequirementContext ctx);
    }

    public static abstract class CommodityDemand extends CommoditySupplyDemand {
        public CommodityDemand(String id, String[] enableSettings, String commodityId, int quantity) {
            super(id, enableSettings, commodityId, quantity);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BaseIndustry sourceIndustry = ctx.getSourceIndustry();
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            BoggledIndustryInterface targetIndustryInterface = ctx.getTargetIndustryInterface();
            if (sourceIndustry == null || targetIndustry == null || targetIndustryInterface == null) {
                return;
            }
            int quantityToDemand = getQuantity(ctx);
            targetIndustry.demand(id, commodityId, quantityToDemand, sourceIndustry.getNameForModifier());
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {

        }
    }

    public static class CommodityDemandFlat extends CommodityDemand {
        public CommodityDemandFlat(String id, String[] enableSettings, String commodityId, int quantity) {
            super(id, enableSettings, commodityId, quantity);
        }

        @Override
        protected int getQuantity(BoggledTerraformingRequirement.RequirementContext ctx) {
            return quantity;
        }
    }

    public static class CommodityDemandMarketSize extends CommodityDemand {
        public CommodityDemandMarketSize(String id, String[] enableSettings, String commodityId, int quantity) {
            super(id, enableSettings, commodityId, quantity);
        }

        @Override
        protected int getQuantity(BoggledTerraformingRequirement.RequirementContext ctx) {
            return Math.max(0, ctx.getSourceIndustry().getMarket().getSize() + quantity);
        }
    }

    public static class CommodityDemandPlayerMarketSizeElseFlat extends CommodityDemand {
        public CommodityDemandPlayerMarketSizeElseFlat(String id, String[] enableSettings, String commodityId, int quantity) {
            super(id, enableSettings, commodityId, quantity);
        }

        @Override
        protected int getQuantity(BoggledTerraformingRequirement.RequirementContext ctx) {
            if (ctx.getSourceIndustry().getMarket().isPlayerOwned()) {
                return Math.max(0, ctx.getSourceIndustry().getMarket().getSize());
            }
            return quantity;
        }
    }

    public static abstract class CommoditySupply extends CommoditySupplyDemand {
        public CommoditySupply(String id, String[] enableSettings, String commodityId, int quantity) {
            super(id, enableSettings, commodityId, quantity);
        }

        protected abstract String getNameForModifier(BaseIndustry sourceIndustry);

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BaseIndustry sourceIndustry = ctx.getSourceIndustry();
            BaseIndustry targetIndustry = ctx.getTargetIndustry();
            if (sourceIndustry == null || targetIndustry == null) {
                return;
            }
            int quantityToDemand = getQuantity(ctx);
            targetIndustry.supply(id, commodityId, quantityToDemand, getNameForModifier(sourceIndustry));
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
        }
    }

    public static class CommoditySupplyFlat extends CommoditySupply {
        public CommoditySupplyFlat(String id, String[] enableSettings, String commodityId, int quantity) {
            super(id, enableSettings, commodityId, quantity);
        }

        @Override
        protected String getNameForModifier(BaseIndustry sourceIndustry) {
            return sourceIndustry.getNameForModifier();
        }

        @Override
        protected int getQuantity(BoggledTerraformingRequirement.RequirementContext ctx) {
            return quantity;
        }
    }

    public static class CommoditySupplyMarketSize extends CommoditySupply {
        public CommoditySupplyMarketSize(String id, String[] enableSettings, String commodityId, int quantity) {
            super(id, enableSettings, commodityId, quantity);
        }

        @Override
        protected String getNameForModifier(BaseIndustry sourceIndustry) {
            return "Base value for colony size";
        }

        @Override
        protected int getQuantity(BoggledTerraformingRequirement.RequirementContext ctx) {
            return Math.max(0, ctx.getSourceIndustry().getMarket().getSize() + quantity);
        }
    }

    public static class AttachProjectToIndustry extends TerraformingProjectEffect {
        String industryId;
        protected AttachProjectToIndustry(String id, String[] enableSettings, String industryId) {
            super(id, enableSettings);
            this.industryId = industryId;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
            BoggledTerraformingProject.ProjectInstance projectInstance = ctx.getProjectInstance();
            MarketAPI market = ctx.getClosestMarket();
            if (market == null || projectInstance == null) {
                return;
            }

            BoggledIndustryInterface industry = (BoggledIndustryInterface) market.getIndustry(industryId);
            if (industry == null) {
                return;
            }

            industry.attachProject(projectInstance);
        }

        @Override
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            BoggledTerraformingProject.ProjectInstance projectInstance = ctx.getProjectInstance();
            MarketAPI market = ctx.getClosestMarket();
            if (market == null || projectInstance == null) {
                return;
            }

            BoggledIndustryInterface industry = (BoggledIndustryInterface) market.getIndustry(industryId);
            if (industry == null) {
                return;
            }

            industry.detachProject(projectInstance);
        }
    }
}
