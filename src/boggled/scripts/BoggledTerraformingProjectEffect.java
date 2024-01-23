package boggled.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketSpecAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.FleetAdvanceScript;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import boggled.campaign.econ.boggledTools;
import org.jetbrains.annotations.NotNull;

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
            boggledTools.addCondition(ctx.getPlanetMarket(), condition);
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
            boggledTools.removeCondition(ctx.getPlanetMarket(), condition);
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

            incrementResourceWithDefault(ctx.getPlanetMarket(), boggledTools.getResourceProgressions().get(resource));
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
            StarSystemAPI starSystem = ctx.getPlanetMarket().getStarSystem();
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

                    tapToken.setCircularOrbitPointingDown(star, boggledTools.getAngleFromEntity(ctx.getPlanetMarket().getPrimaryEntity(), star), orbitRadius, orbitDays);
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
    }

    public static class MarketRemoveIndustry extends TerraformingProjectEffect {
        String industryId;
        public MarketRemoveIndustry(String id, String[] enableSettings, String industryId) {
            super(id, enableSettings);
            this.industryId = industryId;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            ctx.getPlanetMarket().removeIndustry(industryId, null, false);
        }
    }

    public static abstract class RemoveItemFromCargo extends TerraformingProjectEffect {
        CargoAPI.CargoItemType itemType;
        String itemId;
        int quantity;
        public RemoveItemFromCargo(String id, String[] enableSettings, CargoAPI.CargoItemType itemType, String itemId, int quantity) {
            super(id, enableSettings);
            this.itemType = itemType;
            this.itemId = itemId;
            this.quantity = quantity;
        }

        protected void removeItemFromCargo(CargoAPI cargo) {
            switch (itemType) {
                case RESOURCES:
                    cargo.removeItems(itemType, itemId, quantity);
                    break;
                case SPECIAL:
                    cargo.removeItems(itemType, new SpecialItemData(itemId, null), quantity);
                    break;
            }
        }

        @Override
        public void addTokenReplacements(Map<String, String> tokenReplacements) {
            switch (itemType) {
                case RESOURCES:
                    tokenReplacements.put("$itemName", Global.getSettings().getCommoditySpec(itemId).getLowerCaseName());
                    tokenReplacements.put("$ItemName", Global.getSettings().getCommoditySpec(itemId).getName());
                    break;
                case SPECIAL:
                    tokenReplacements.put("$itemName", Global.getSettings().getSpecialItemSpec(itemId).getName().toLowerCase());
                    tokenReplacements.put("$ItemName", Global.getSettings().getSpecialItemSpec(itemId).getName());
                    break;
            }

            tokenReplacements.put("$itemQuantity", String.format("%,d", quantity));
        }

        @Override
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara) {
            if (!effectTypeToPara.containsKey("ItemCost")) {
                effectTypeToPara.put("ItemCost", new EffectTooltipPara("Expends ", "."));
            }
            EffectTooltipPara para = effectTypeToPara.get("ItemCost");
            String itemString = "";
            switch (itemType) {
                case RESOURCES:
                    itemString = Global.getSettings().getCommoditySpec(itemId).getLowerCaseName();
                    break;
                case SPECIAL:
                    itemString = Global.getSettings().getSpecialItemSpec(itemId).getName().toLowerCase();
                    break;
            }
            String quantityString = String.format("%,d", quantity);
            para.infix.add(quantityString + " " + itemString);
            para.highlights.add(quantityString);
        }
    }

    public static class RemoveItemFromSubmarket extends RemoveItemFromCargo {
        String submarketId;
        public RemoveItemFromSubmarket(String id, String[] enableSettings, String submarketId, CargoAPI.CargoItemType itemType, String itemId, int quantity) {
            super(id, enableSettings, itemType, itemId, quantity);
            this.submarketId = submarketId;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            MarketAPI market = ctx.getPlanetMarket();
            if (market == null) {
                return;
            }
            removeItemFromCargo(market.getSubmarket(submarketId).getCargo());
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

    public static class RemoveItemFromFleetStorage extends RemoveItemFromCargo {
        protected RemoveItemFromFleetStorage(String id, String[] enableSettings, CargoAPI.CargoItemType itemType, String commodityId, int quantity) {
            super(id, enableSettings, itemType, commodityId, quantity);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            CampaignFleetAPI playerFleet = ctx.getFleet();
            if (playerFleet == null) {
                return;
            }
            removeItemFromCargo(playerFleet.getCargo());
//            bogglesDefaultCargo.active.removeCommodity("station_type", commodityId, quantity);
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
            ctx.getPlanetMarket().getSubmarket(submarketId).getCargo().addSpecial(new SpecialItemData(itemId, null), quantity);
        }
    }

    public static abstract class AddStation extends TerraformingProjectEffect {
        String stationType;
        String stationName;
        List<String> variants;

        int numStationsPerLayer;
        float orbitRadius;

        BoggledStationConstructors.StationConstructionData stationConstructionData;

        public AddStation(String id, String[] enableSettings, String stationType, String stationName, List<String> variants, int numStationsPerLayer, float orbitRadius, BoggledStationConstructors.StationConstructionData stationConstructionData) {
            super(id, enableSettings);
            this.stationType = stationType;
            this.stationName = stationName;
            this.variants = variants;
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

        protected String getVariant(int numStationsAlreadyPresent) {
            numStationsAlreadyPresent = Math.abs(numStationsAlreadyPresent);
            if (variants.isEmpty()) {
                return "";
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
                if (name.contains(greekLetter)) {
                    return i;
                }
            }
            return -1;
        }

        protected SectorEntityToken compareAndGetLatest(List<String> greekAlphabet, SectorEntityToken o1, SectorEntityToken o2) {
            if (o1 == null) {
                return o2;
            }
            if (o2 == null) {
                return o1;
            }
            int o1NumIndexStart = o1.getName().indexOf('-');
            int o2NumIndexStart = o2.getName().indexOf('-');
            if (o1NumIndexStart == -1 && o2NumIndexStart == -1) {
                // Neither has a number at the end, so we compare according to the index in the alphabet
                int o1GreekIndex = greekAlphabetIndexOf(greekAlphabet, o1.getName());//greekAlphabet.indexOf(o1.getName());
                int o2GreekIndex = greekAlphabetIndexOf(greekAlphabet, o2.getName());//greekAlphabet.indexOf(o2.getName());
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
            int o1Num = Integer.parseInt(o1.getName().substring(o1NumIndexStart + 1));
            int o2Num = Integer.parseInt(o2.getName().substring(o2NumIndexStart + 1));
            int comp = Integer.compare(o1Num, o2Num);
            if (comp < 0) {
                return o1;
            } else if (comp > 0) {
                return o2;
            }
            // The numbers are the same, so compare based on the index in the alphabet
            int o1GreekIndex = greekAlphabet.indexOf(o1.getName());
            int o2GreekIndex = greekAlphabet.indexOf(o2.getName());
            comp = Integer.compare(o1GreekIndex, o2GreekIndex);
            if (comp < 0) {
                return o1;
            }
            // If they return equal, something else is broken
            if (comp == 0) {
                Global.getLogger(this.getClass()).error("Sector entity tokens " + o1.getName() + " and " + o2.getName() + " compared equal");
            }
            return o2;
        }
    }

    public static class AddStationToOrbit extends AddStation {
        public AddStationToOrbit(String id, String[] enableSettings, String stationType, String stationName, List<String> variants, int numStationsPerLayer, float orbitRadius, BoggledStationConstructors.StationConstructionData stationConstructionData) {
            super(id, enableSettings, stationType, stationName, variants, numStationsPerLayer, orbitRadius, stationConstructionData);
        }

        @Override
        public void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara) {
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

            stationConstructionData.addTooltipInfo(ctx, effectTypeToPara);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
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

            SectorEntityToken newStation = starSystem.addCustomEntity(id, targetPlanet.getName() + " " + stationName + " " + getColonyNameString(numStations), customEntityStationTag + variantLetter + "_small", playerFactionId);
            SectorEntityToken newStationLights = starSystem.addCustomEntity(id + "Lights", targetPlanet.getName() + " " + stationName + " " + getColonyNameString(numStations) + " Lights Overlay", customEntityStationTag + variantLetter + "_small_lights_overlay", playerFactionId);

            newStation.addTag(boggledTools.BoggledTags.stationNamePrefix + stationName);

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
    }

    public static class AddStationToEntity extends AddStation {
        public AddStationToEntity(String id, String[] enableSettings, String stationType, String stationName, List<String> variants, int numStationsPerLayer, float orbitRadius, BoggledStationConstructors.StationConstructionData stationConstructionData) {
            super(id, enableSettings, stationType, stationName, variants, numStationsPerLayer, orbitRadius, stationConstructionData);
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
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
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara) {
            stationConstructionData.addTooltipInfo(ctx, effectTypeToPara);
        }
    }

    public static class ColonizeAbandonedStation extends TerraformingProjectEffect {
        BoggledStationConstructors.StationConstructionData defaultStationConstructionData;
        List<BoggledStationConstructors.StationConstructionData> stationConstructionData;
        protected ColonizeAbandonedStation(String id, String[] enableSettings, BoggledStationConstructors.StationConstructionData defaultStationConstructionData, List<BoggledStationConstructors.StationConstructionData> stationConstructionData) {
            super(id, enableSettings);
            this.defaultStationConstructionData = defaultStationConstructionData;
            this.stationConstructionData = stationConstructionData;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
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

            if (targetStation.hasTag("boggled_astropolis") || targetStation.hasTag("boggled_mining") || targetStation.hasTag("boggled_siphon")) {
                SectorEntityToken newLightsOnColonize = starSystem.addCustomEntity("boggled_newLightsOnColonize", "New Lights Overlay From Colonizing Abandoned Station", targetStation.getCustomEntityType() + "_lights_overlay", playerFleet.getFaction().getId());
                newLightsOnColonize.setOrbit(targetStation.getOrbit().makeCopy());
            } else if(targetStation.hasTag("boggled_gatekeeper_station")) {
                targetStation.setCustomDescriptionId("gatekeeper_station");

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
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara) {
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
        }
    }

    public static class EffectWithRequirement extends TerraformingProjectEffect {
        BoggledProjectRequirementsAND requirements;
        List<TerraformingProjectEffect> effects;

        protected EffectWithRequirement(String id, String[] enableSettings, BoggledProjectRequirementsAND requirements, List<TerraformingProjectEffect> effects) {
            super(id, enableSettings);
            this.requirements = requirements;
            this.effects = effects;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            if (!requirements.requirementsMet(ctx)) {
                return;
            }
            for (TerraformingProjectEffect effect : effects) {
                effect.applyProjectEffect(ctx);
            }
        }
    }

    public static class AdjustRelationsWith extends TerraformingProjectEffect {
        String factionIdToAdjustRelationsTo;
        List<String> factionIdsToAdjustRelations;
        float newRelationValue;
        protected AdjustRelationsWith(String id, String[] enableSettings, String factionIdToAdjustRelationsTo, List<String> factionIdsToAdjustRelations, float newRelationValue) {
            super(id, enableSettings);
            this.factionIdToAdjustRelationsTo = factionIdToAdjustRelationsTo;
            this.factionIdsToAdjustRelations = factionIdsToAdjustRelations;
            this.newRelationValue = newRelationValue;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            for (String factionId : factionIdsToAdjustRelations) {
                Global.getSector().getFaction(factionId).setRelationship(factionIdToAdjustRelationsTo, newRelationValue);
            }
        }
    }

    public static class AdjustRelationsWithAllExcept extends TerraformingProjectEffect {
        String factionIdToAdjustRelationsTo;
        List<String> factionIdsToNotAdjustRelations;
        float newRelationValue;
        protected AdjustRelationsWithAllExcept(String id, String[] enableSettings, String factionIdToAdjustRelationsTo, List<String> factionIdsToNotAdjustRelations, float newRelationValue) {
            super(id, enableSettings);
            this.factionIdToAdjustRelationsTo = factionIdToAdjustRelationsTo;
            this.factionIdsToNotAdjustRelations = factionIdsToNotAdjustRelations;
            this.newRelationValue = newRelationValue;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
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
    }

    public static class TriggerMilitaryResponse extends TerraformingProjectEffect {
        float responseFraction;
        float responseDuration;

        protected TriggerMilitaryResponse(String id, String[] enableSettings, float responseFraction, float responseDuration) {
            super(id, enableSettings);
            this.responseFraction = responseFraction;
            this.responseDuration = responseDuration;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
            MarketAPI market = ctx.getPlanetMarket();
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
    }

    public static class DecivilizeMarket extends TerraformingProjectEffect {
        List<String> factionIdsToNotMakeHostile;
        protected DecivilizeMarket(String id, String[] enableSettings, List<String> factionIdsToNotMakeHostile) {
            super(id, enableSettings);
            this.factionIdsToNotMakeHostile = factionIdsToNotMakeHostile;
        }

        @Override
        protected void applyProjectEffectImpl(BoggledTerraformingRequirement.RequirementContext ctx) {
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
        protected void addTooltipInfoImpl(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, EffectTooltipPara> effectTypeToPara) {
            SectorEntityToken targetStation = ctx.getStation();
            if (targetStation == null) {
                return;
            }
            if (!effectTypeToPara.containsKey("DecivilizationTarget")) {
                effectTypeToPara.put("DecivilizationTarget", new EffectTooltipPara("Target colony: ", ""));
            }
            EffectTooltipPara para = effectTypeToPara.get("DecivilizationTarget");
            para.infix.add(targetStation.getName());
            para.highlights.add(targetStation.getName());
        }
    }
}
