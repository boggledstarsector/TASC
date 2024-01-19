package boggled.scripts;

import boggled.scripts.PlayerCargoCalculations.bogglesDefaultCargo;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketSpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.FleetAdvanceScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import boggled.campaign.econ.boggledTools;

import java.util.*;
import java.util.List;

public class BoggledTerraformingProjectEffect {
    public static class ProjectEffectWithRequirement {
        public TerraformingProjectEffect effect;
        public BoggledProjectRequirementsAND requirement;

        public ProjectEffectWithRequirement(TerraformingProjectEffect effect, BoggledProjectRequirementsAND requirement) {
            this.effect = effect;
            this.requirement = requirement;
        }
    }

    public static class EffectTooltipPara {
        public String prefix;
        public String suffix;
        public Set<String> infix = new LinkedHashSet<>();
        public List<String> highlights = new ArrayList<>();

        public EffectTooltipPara(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }
    }

    public abstract static class TerraformingProjectEffect {
        String id;
        String[] enableSettings;
        protected TerraformingProjectEffect(String id, String[] enableSettings) {
            this.id = id;
            this.enableSettings = enableSettings;
        }

        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

        protected abstract void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx);
        protected void unapplyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {}

        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara) {}

        public void addTokenReplacements(Map<String, String> tokenReplacements) {}

        public final void addEffectTooltipInfo(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara) {
            if (!isEnabled()) {
                return;
            }
            addTooltipInfoImpl(ctx, effectTypeToPara);
        }

        public final void applyProjectEffect(BoggledTerraformingRequirement.RequirementContext ctx) {
            if (!isEnabled()) {
                return;
            }
            applyProjectEffectImpl(ctx);
        }

        public final void unapplyProjectEffect(BoggledTerraformingRequirement.RequirementContext ctx) {
            if (!isEnabled()) {
                return;
            }
            unapplyProjectEffectImpl(ctx);
        }
    }

    public static class PlanetTypeChangeProjectEffect extends TerraformingProjectEffect {
        private final String newPlanetType;

        public PlanetTypeChangeProjectEffect(String id, String[] enableSettings, String newPlanetType) {
            super(id, enableSettings);
            this.newPlanetType = newPlanetType;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getPlanet().changeType(newPlanetType, null);
        }
    }

    public static class MarketAddConditionProjectEffect extends TerraformingProjectEffect {
        private final String condition;

        public MarketAddConditionProjectEffect(String id, String[] enableSettings, String condition) {
            super(id, enableSettings);
            this.condition = condition;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            boggledTools.addCondition(ctx.getMarket(), condition);
        }
    }

    public static class MarketRemoveConditionProjectEffect extends TerraformingProjectEffect {
        String condition;

        public MarketRemoveConditionProjectEffect(String id, String[] enableSettings, String condition) {
            super(id, enableSettings);
            this.condition = condition;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            boggledTools.removeCondition(ctx.getMarket(), condition);
        }
    }

    public static class MarketProgressResourceProjectEffect extends TerraformingProjectEffect {
        private final String resource;
        private final int step;

        public MarketProgressResourceProjectEffect(String id, String[] enableSettings, String resource, int step) {
            super(id, enableSettings);
            this.resource = resource;
            this.step = step;
        }

        private void incrementResourceWithDefault(MarketAPI market, ArrayList<String> resourceProgression) {
            // Step because OuyangOptimization goes volatiles_trace (0) to volatiles_abundant (2), etc
            String defaultResource = resourceProgression.get(Math.max(0, step - 1));
            boolean resourceFound = false;
            for (int i = 0; i < resourceProgression.size() - 1; ++i) {
                if (market.hasCondition(resourceProgression.get(i))) {
                    boggledTools.removeCondition(market, resourceProgression.get(i));
                    boggledTools.addCondition(market, resourceProgression.get(Math.min(i + step, resourceProgression.size() - 1)));
                    resourceFound = true;
                    break;
                }
            }

            if (!resourceFound && defaultResource != null && !defaultResource.isEmpty()) {
                boggledTools.addCondition(market, defaultResource);
            }
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ArrayList<String> resourcesProgression = boggledTools.getResourceProgressions().get(resource);
            if (resourcesProgression == null || resourcesProgression.isEmpty()) {
                return;
            }

            incrementResourceWithDefault(ctx.getMarket(), boggledTools.getResourceProgressions().get(resource));
        }
    }

    public static class FocusMarketAddConditionProjectEffect extends MarketAddConditionProjectEffect {
        public FocusMarketAddConditionProjectEffect(String id, String[] enableSettings, String condition) {
            super(id, enableSettings, condition);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            super.applyProjectEffectImpl(ctx.getFocusContext());
        }
    }

    public static class FocusMarketRemoveConditionProjectEffect extends MarketRemoveConditionProjectEffect {
        public FocusMarketRemoveConditionProjectEffect(String id, String[] enableSettings, String condition) {
            super(id, enableSettings, condition);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            super.applyProjectEffectImpl(ctx.getFocusContext());
        }
    }

    public static class FocusMarketProgressResourceProjectEffect extends MarketProgressResourceProjectEffect {
        public FocusMarketProgressResourceProjectEffect(String id, String[] enableSettings, String resource, int step) {
            super(id, enableSettings, resource, step);
        }
        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            super.applyProjectEffectImpl(ctx.getFocusContext());
        }
    }

    public static class FocusMarketAndSiphonStationProgressResourceProjectEffect extends MarketProgressResourceProjectEffect {
        public FocusMarketAndSiphonStationProgressResourceProjectEffect(String id, String[] enableSettings, String resource, int step) {
            super(id, enableSettings, resource, step);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            super.applyProjectEffectImpl(ctx.getFocusContext());

            SectorEntityToken closestGasGiantToken = ctx.getMarket().getPrimaryEntity();
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
                    && (entity.getCustomEntitySpec().getDefaultName().equals("Side Station")
                        || entity.getCustomEntitySpec().getDefaultName().equals("Siphon Station"))
                    && !entity.getId().equals("beholder_station"))
                {
                    super.applyProjectEffectImpl(ctx);
                }
            }
        }
    }

    public static class SystemAddCoronalTap extends TerraformingProjectEffect {
        public SystemAddCoronalTap(String id, String[] enableSettings) {
            super(id, enableSettings);
        }
        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            StarSystemAPI system = ctx.getMarket().getStarSystem();
            SectorEntityToken tapToken = null;

            if (system.getType() == StarSystemGenerator.StarSystemType.TRINARY_2CLOSE) {
                tapToken = system.addCustomEntity("coronal_tap_" + system.getName(), null, "coronal_tap", Global.getSector().getPlayerFaction().getId());

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

                for (PlanetAPI planet : system.getPlanets()) {
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

                    tapToken = system.addCustomEntity("coronal_tap_" + system.getName(), null, "coronal_tap", Global.getSector().getPlayerFaction().getId());

                    tapToken.setCircularOrbitPointingDown(star, boggledTools.getAngleFromEntity(ctx.getMarket().getPrimaryEntity(), star), orbitRadius, orbitDays);
                }
            }

            if (tapToken != null) {
                tapToken.addTag("BOGGLED_BUILT_BY_PERIHELION_PROJECT");
                tapToken.removeScriptsOfClass(FleetAdvanceScript.class);

                system.addScript(new CoronalTapParticleScript(tapToken));
                system.addTag(Tags.HAS_CORONAL_TAP);

                MemoryAPI memory = tapToken.getMemory();
                memory.set("$usable", true);
                memory.set("$defenderFleetDefeated", true);

                memory.unset("$hasDefenders");
                memory.unset("$defenderFleet");
            }
        }
    }

    public static class MarketRemoveIndustry extends TerraformingProjectEffect {
        String industryId;
        public MarketRemoveIndustry(String id, String[] enableSettings, String industryId) {
            super(id, enableSettings);
            this.industryId = industryId;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getMarket().removeIndustry(industryId, null, false);
        }
    }

    public static class RemoveCommodityFromSubmarket extends TerraformingProjectEffect {
        String submarketId;
        String itemId;
        int quantity;
        public RemoveCommodityFromSubmarket(String id, String[] enableSettings, String submarketId, String itemId, int quantity) {
            super(id, enableSettings);
            this.submarketId = submarketId;
            this.itemId = itemId;
            this.quantity = quantity;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getMarket().getSubmarket(submarketId).getCargo().removeCommodity(itemId, quantity);
        }
    }

    public static class RemoveStoryPointsFromPlayer extends TerraformingProjectEffect {
        int quantity;
        public RemoveStoryPointsFromPlayer(String id, String[] enableSettings, int quantity) {
            super(id, enableSettings);
            this.quantity = quantity;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            Global.getSector().getPlayerStats().spendStoryPoints(quantity, false, null, false, null);
        }
    }

    public static class RemoveCommodityFromFleetStorage extends TerraformingProjectEffect {
        String commodityId;
        int quantity;
        protected RemoveCommodityFromFleetStorage(String id, String[] enableSettings, String commodityId, int quantity) {
            super(id, enableSettings);
            this.commodityId = commodityId;
            this.quantity = quantity;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            bogglesDefaultCargo.active.removeCommodity("station_type", commodityId, quantity);
        }

        @Override
        public void addTokenReplacements(Map<String, String> tokenReplacements) {
            tokenReplacements.put("$commodityName", Global.getSettings().getCommoditySpec(commodityId).getLowerCaseName());
            tokenReplacements.put("$CommodityName", Global.getSettings().getCommoditySpec(commodityId).getName());
            tokenReplacements.put("$commodityQuantity", Integer.toString(quantity));
        }

        @Override
        public void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara) {
            if (!effectTypeToPara.containsKey("CommodityCost")) {
                effectTypeToPara.put("CommodityCost", new EffectTooltipPara("Expends ", "."));
            }
            EffectTooltipPara para = effectTypeToPara.get("CommodityCost");
            String commodityString = Global.getSettings().getCommoditySpec(commodityId).getLowerCaseName();
            String quantityString = String.format("%,d", quantity);
            para.infix.add(quantityString + " " + commodityString);
            para.highlights.add(quantityString);
        }
    }

    public static class RemoveCreditsFromFleet extends TerraformingProjectEffect {
        int quantity;
        protected RemoveCreditsFromFleet(String id, String[] enableSettings, int quantity) {
            super(id, enableSettings);
            this.quantity = quantity;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getFleet().getCargo().getCredits().subtract(quantity);
        }

        @Override
        public void addTokenReplacements(Map<String, String> tokenReplacements) {
            tokenReplacements.put("$creditsQuantity", Integer.toString(quantity));
        }

        @Override
        public void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara) {
            if (!effectTypeToPara.containsKey("CommodityCost")) {
                effectTypeToPara.put("CommodityCost", new EffectTooltipPara("Expends ", "."));
            }
            EffectTooltipPara para = effectTypeToPara.get("CommodityCost");
            String quantityString = String.format("%,d", quantity);
            para.infix.add(quantityString + " credits");
            para.highlights.add(quantityString);
        }
    }

    public static class AddItemToSubmarket extends TerraformingProjectEffect {
        String submarketId;
        String itemId;
        int quantity;
        public AddItemToSubmarket(String id, String[] enableSettings, String submarketId, String itemId, int quantity) {
            super(id, enableSettings);
            this.submarketId = submarketId;
            this.itemId = itemId;
            this.quantity = quantity;
        }

        @Override
        public void addTokenReplacements(Map<String, String> tokenReplacements) {
            super.addTokenReplacements(tokenReplacements);
            for (SubmarketSpecAPI submarketSpec : Global.getSettings().getAllSubmarketSpecs()) {
                if (submarketSpec.getId().equals(submarketId)) {
                    tokenReplacements.put("$submarket", Misc.lcFirst(submarketSpec.getName()));
                }
            }
            tokenReplacements.put("$craftedItem", Global.getSettings().getSpecialItemSpec(itemId).getName());
            tokenReplacements.put("$craftedItemQuantity", Integer.toString(quantity));
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getMarket().getSubmarket(submarketId).getCargo().addSpecial(new SpecialItemData(itemId, null), quantity);
        }
    }

    public static class AddStationToOrbit extends TerraformingProjectEffect {
        String stationType;

        public AddStationToOrbit(String id, String[] enableSettings, String stationType) {
            super(id, enableSettings);
            this.stationType = stationType;
        }

        @Override
        public void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara) {
            PlanetAPI targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return;
            }
            if (!effectTypeToPara.containsKey("StationConstructionTarget")) {
                effectTypeToPara.put("StationConstructionTarget", new EffectTooltipPara("Target host world: ", "."));
            }
            EffectTooltipPara para = effectTypeToPara.get("StationConstructionTarget");
            para.infix.add(targetPlanet.getName());
            para.highlights.add(targetPlanet.getName());
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
        }
    }
}
