package data.scripts;

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
import data.campaign.econ.boggledTools;
import data.campaign.econ.industries.BoggledCommonIndustry;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class BoggledTerraformingProjectEffect {
    public abstract static class TerraformingProjectEffect {
        abstract void applyProjectEffect(MarketAPI market);

        void addTokenReplacements(LinkedHashMap<String, String> tokenReplacements) {};
    }

    public static class PlanetTypeChangeProjectEffect extends TerraformingProjectEffect {
        private final String newPlanetType;

        public PlanetTypeChangeProjectEffect(String newPlanetType) {
            this.newPlanetType = newPlanetType;
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            market.getPlanetEntity().changeType(newPlanetType, null);
        }
    }

    public static class MarketAddConditionProjectEffect extends TerraformingProjectEffect {
        private final String condition;

        public MarketAddConditionProjectEffect(String condition) {
            this.condition = condition;
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            boggledTools.addCondition(market, condition);
        }
    }

    public static class MarketRemoveConditionProjectEffect extends TerraformingProjectEffect {
        String condition;

        public MarketRemoveConditionProjectEffect(String condition) {
            this.condition = condition;
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            boggledTools.removeCondition(market, condition);
        }
    }

    public static class MarketProgressResourceProjectEffect extends TerraformingProjectEffect {
        private final String resource;
        private final int step;

        public MarketProgressResourceProjectEffect(String resource, int step) {
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
        public void applyProjectEffect(MarketAPI market) {
            ArrayList<String> resourcesProgression = boggledTools.getResourceProgressions().get(resource);
            if (resourcesProgression == null || resourcesProgression.isEmpty()) {
                return;
            }

            incrementResourceWithDefault(market, boggledTools.getResourceProgressions().get(resource));
        }
    }

    public static class FocusMarketAddConditionProjectEffect extends MarketAddConditionProjectEffect {
        public FocusMarketAddConditionProjectEffect(String condition) {
            super(condition);
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            super.applyProjectEffect(BoggledCommonIndustry.getFocusMarketOrMarket(market));
        }
    }

    public static class FocusMarketRemoveConditionProjectEffect extends MarketRemoveConditionProjectEffect {
        public FocusMarketRemoveConditionProjectEffect(String condition) {
            super(condition);
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            super.applyProjectEffect(BoggledCommonIndustry.getFocusMarketOrMarket(market));
        }
    }

    public static class FocusMarketProgressResourceProjectEffect extends MarketProgressResourceProjectEffect {
        public FocusMarketProgressResourceProjectEffect(String resource, int step) {
            super(resource, step);
        }
        @Override
        public void applyProjectEffect(MarketAPI market) {
            super.applyProjectEffect(BoggledCommonIndustry.getFocusMarketOrMarket(market));
        }
    }

    public static class FocusMarketAndSiphonStationProgressResourceProjectEffect extends MarketProgressResourceProjectEffect {
        public FocusMarketAndSiphonStationProgressResourceProjectEffect(String resource, int step) {
            super(resource, step);
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            super.applyProjectEffect(BoggledCommonIndustry.getFocusMarketOrMarket(market));

            SectorEntityToken closestGasGiantToken = market.getPrimaryEntity();
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
                    super.applyProjectEffect(entity.getMarket());
                }
            }
        }
    }

    public static class SystemAddCoronalTap extends TerraformingProjectEffect {
        public SystemAddCoronalTap() {
        }
        @Override
        public void applyProjectEffect(MarketAPI market) {
            StarSystemAPI system = market.getStarSystem();
            SectorEntityToken tapToken = null;

            if (system.getType() == StarSystemGenerator.StarSystemType.TRINARY_2CLOSE) {
                tapToken = system.addCustomEntity("coronal_tap_" + market.getStarSystem().getName(), null, "coronal_tap", Global.getSector().getPlayerFaction().getId());

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

                    tapToken = system.addCustomEntity("coronal_tap_" + market.getStarSystem().getName(), null, "coronal_tap", Global.getSector().getPlayerFaction().getId());

                    tapToken.setCircularOrbitPointingDown(star, boggledTools.getAngleFromEntity(market.getPrimaryEntity(), star), orbitRadius, orbitDays);
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

    public static class MarketAddStellarReflectors extends TerraformingProjectEffect {
        public MarketAddStellarReflectors() {

        }

        @Override
        public void applyProjectEffect(MarketAPI market) {

        }
    }

    public static class RemoveItemFromMarketStorage extends TerraformingProjectEffect {
        String submarketId;
        String itemId;
        int quantity;
        public RemoveItemFromMarketStorage(String submarketId, String itemId, int quantity) {
            this.submarketId = submarketId;
            this.itemId = itemId;
            this.quantity = quantity;
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            market.getSubmarket(submarketId).getCargo().removeCommodity(itemId, quantity);
        }
    }

    public static class RemoveStoryPointsFromPlayer extends TerraformingProjectEffect {
        int quantity;
        public RemoveStoryPointsFromPlayer(int quantity) {
            this.quantity = quantity;
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            Global.getSector().getPlayerStats().spendStoryPoints(quantity, false, null, false, null);
        }
    }

    public static class AddItemToMarketStorage extends TerraformingProjectEffect {
        String submarketId;
        String itemId;
        int quantity;
        public AddItemToMarketStorage(String submarketId, String itemId, int quantity) {
            this.submarketId = submarketId;
            this.itemId = itemId;
            this.quantity = quantity;
        }

        @Override
        void addTokenReplacements(LinkedHashMap<String, String> tokenReplacements) {
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
        public void applyProjectEffect(MarketAPI market) {
            market.getSubmarket(submarketId).getCargo().addSpecial(new SpecialItemData(itemId, null), quantity);
        }
    }
}
