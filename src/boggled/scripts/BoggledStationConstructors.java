package boggled.scripts;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;

import java.util.List;
import java.util.Map;

public class BoggledStationConstructors {
    public static abstract class StationConstructionData {
        String stationType;

        List<String> industriesToQueue;

        public StationConstructionData(String stationType, List<String> industriesToQueue) {
            this.stationType = stationType;
            this.industriesToQueue = industriesToQueue;
        }

        protected MarketAPI createDefaultMarket(SectorEntityToken stationEntity, String hostName, String marketType) {
            MarketAPI market = Global.getFactory().createMarket(stationEntity.getId() + hostName + marketType, stationEntity.getName(), 3);

            market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
            market.setPrimaryEntity(stationEntity);

            market.setFactionId(Global.getSector().getPlayerFleet().getFaction().getId());
            market.setPlayerOwned(true);

            market.addCondition(Conditions.POPULATION_3);

            market.addCondition(boggledTools.BoggledConditions.spriteControllerConditionId);
            market.addCondition(boggledTools.BoggledConditions.crampedQuartersConditionId);

            //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
            //market_conditions.csv overwrites the vanilla no_atmosphere condition
            //the only change made is to hide the icon on markets where primary entity has station tag
            //This is done so refining and fuel production can slot the special items
            //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
            market.addCondition(Conditions.NO_ATMOSPHERE);
            market.suppressCondition(Conditions.NO_ATMOSPHERE);

            market.addIndustry(Industries.POPULATION);
            market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);
            for (String industryToQueue : industriesToQueue) {
                market.getConstructionQueue().addToEnd(industryToQueue, 0);
            }

            stationEntity.setMarket(market);

            Global.getSector().getEconomy().addMarket(market, true);

            // If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
            // Still bugged as of 0.95.1a
            Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);

            market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
            StoragePlugin storage = (StoragePlugin)market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin();
            storage.setPlayerPaidToUnlock(true);
            market.addSubmarket(Submarkets.LOCAL_RESOURCES);

            boggledTools.surveyAll(market);
            boggledTools.refreshSupplyAndDemand(market);

            return market;
        }

        public abstract void createMarket(SectorEntityToken stationEntity);

        public void addTooltipInfo(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> effectTypeToPara) {
            String durationString = String.format("%,d", ctx.getProject().getModifiedProjectDuration(ctx));
            BoggledTerraformingProjectEffect.EffectTooltipPara para = new BoggledTerraformingProjectEffect.EffectTooltipPara("Building a station here will take " + durationString + " days.", "");
            para.highlights.add(durationString);
            effectTypeToPara.put("StationConstructionModifiedBuildTime", para);
        }
    }

    public static class AstropolisConstructionData extends StationConstructionData {
        public AstropolisConstructionData(String stationType, List<String> industriesToQueue) {
            super(stationType, industriesToQueue);
        }

        @Override
        public void createMarket(SectorEntityToken stationEntity) {
            SectorEntityToken hostPlanet = stationEntity.getOrbitFocus();
            MarketAPI market = createDefaultMarket(stationEntity, hostPlanet.getName(), "AstropolisMarket");

            Global.getSoundPlayer().playUISound(boggledTools.BoggledSounds.stationConstructed, 1.0F, 1.0F);
        }
    }

    public static class MiningStationConstructionData extends StationConstructionData {
        private List<String> resourcesToHighlight;

        public MiningStationConstructionData(String stationType, List<String> industriesToQueue, List<String> resourcesToHighlight) {
            super(stationType, industriesToQueue);
            this.resourcesToHighlight = resourcesToHighlight;
        }

        @Override
        public void addTooltipInfo(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> effectTypeToPara) {
            super.addTooltipInfo(ctx, effectTypeToPara);

            StarSystemAPI starSystem = ctx.getStarSystem();
            if (starSystem == null) {
                return;
            }

            int numAsteroidBeltsInSystem = boggledTools.getNumAsteroidTerrainsInSystem(ctx.getFleet());
            String numAsteroidBeltsInSystemString = String.format("%,d", numAsteroidBeltsInSystem);
            String resourceString = boggledTools.getMiningStationResourceString(numAsteroidBeltsInSystem);
            BoggledTerraformingProjectEffect.EffectTooltipPara para = new BoggledTerraformingProjectEffect.EffectTooltipPara("There are " + numAsteroidBeltsInSystemString + " asteroid belts in the " + ctx.getStarSystem() + ". A mining station would have " + resourceString + " resources.", "");
            para.highlights.add(numAsteroidBeltsInSystemString);
            para.highlights.add(resourceString);
            effectTypeToPara.put("StationConditionsAndReason", para);
        }

        @Override
        public void createMarket(SectorEntityToken stationEntity) {
            StarSystemAPI system = stationEntity.getStarSystem();

            MarketAPI market = createDefaultMarket(stationEntity, system.getName(), "MiningStationMarket");

            if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.miningStationLinkToResourceBelts)) {
                int numAsteroidBeltsInSystem = boggledTools.getNumAsteroidTerrainsInSystem(stationEntity);
                String resourceLevel = boggledTools.getMiningStationResourceString(numAsteroidBeltsInSystem);
                market.addCondition("ore_" + resourceLevel);
                market.addCondition("rare_ore_" + resourceLevel);
            } else {
                String resourceLevel = "moderate";
                int staticAmountPerSettings = boggledTools.getIntSetting(boggledTools.BoggledSettings.miningStationStaticAmount);
                switch (staticAmountPerSettings) {
                    case 1:
                        resourceLevel = "sparse";
                        break;
                    case 2:
                        resourceLevel = "moderate";
                        break;
                    case 3:
                        resourceLevel = "abundant";
                        break;
                    case 4:
                        resourceLevel = "rich";
                        break;
                    case 5:
                        resourceLevel = "ultrarich";
                        break;
                }
                market.addCondition("ore_" + resourceLevel);
                market.addCondition("rare_ore_" + resourceLevel);
            }

            Global.getSoundPlayer().playUISound(boggledTools.BoggledSounds.stationConstructed, 1.0F, 1.0F);
        }
    }

    public static class SiphonStationConstructionData extends StationConstructionData {
        private List<String> resourcesToHighlight;

        public SiphonStationConstructionData(String stationType, List<String> industriesToQueue, List<String> resourcesToHighlight) {
            super(stationType,  industriesToQueue);
            this.resourcesToHighlight = resourcesToHighlight;
        }

        @Override
        public void addTooltipInfo(BoggledTerraformingRequirement.RequirementContext ctx, Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> effectTypeToPara) {
            super.addTooltipInfo(ctx, effectTypeToPara);

            PlanetAPI targetPlanet = ctx.getPlanet();
            if (targetPlanet == null) {
                return;
            }

            for (String resource : resourcesToHighlight) {
                String resourceName = null;
                for (MarketConditionAPI condition : targetPlanet.getMarket().getConditions()) {
                    if (condition.getId().contains(resource)) {
                        resourceName = condition.getName().toLowerCase().replace(" " + resource, "");
                    }
                }
                if (resourceName != null) {
                    BoggledTerraformingProjectEffect.EffectTooltipPara para = new BoggledTerraformingProjectEffect.EffectTooltipPara("A siphon station constructed here would have " + resourceName + " " + resource + ".", "");
                    para.highlights.add(resourceName);
                    effectTypeToPara.put("StationConditionsAndReason", para);
                }
            }
        }

        @Override
        public void createMarket(SectorEntityToken stationEntity) {
            SectorEntityToken hostPlanet = stationEntity.getOrbitFocus();
            MarketAPI market = createDefaultMarket(stationEntity, hostPlanet.getName(), "MiningStationMarket");

            if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.siphonStationLinkToGasGiant)) {
                if(hostPlanet.getMarket().hasCondition(Conditions.VOLATILES_TRACE)) {
                    market.addCondition(Conditions.VOLATILES_TRACE);
                } else if(hostPlanet.getMarket().hasCondition(Conditions.VOLATILES_DIFFUSE)) {
                    market.addCondition(Conditions.VOLATILES_DIFFUSE);
                } else if(hostPlanet.getMarket().hasCondition(Conditions.VOLATILES_ABUNDANT)) {
                    market.addCondition(Conditions.VOLATILES_ABUNDANT);
                } else if(hostPlanet.getMarket().hasCondition(Conditions.VOLATILES_PLENTIFUL)) {
                    market.addCondition(Conditions.VOLATILES_PLENTIFUL);
                } else { // Can a gas giant not have any volatiles at all?
                    market.addCondition(Conditions.VOLATILES_TRACE);
                }
            } else {
                String resourceLevel = "diffuse";
                int staticAmountPerSettings = boggledTools.getIntSetting(boggledTools.BoggledSettings.siphonStationStaticAmount);
                switch(staticAmountPerSettings) {
                    case 1:
                        resourceLevel = "trace";
                        break;
                    case 2:
                        resourceLevel = "diffuse";
                        break;
                    case 3:
                        resourceLevel = "abundant";
                        break;
                    case 4:
                        resourceLevel = "plentiful";
                        break;
                }
                market.addCondition("volatiles_" + resourceLevel);
            }

            Global.getSoundPlayer().playUISound(boggledTools.BoggledSounds.stationConstructed, 1.0F, 1.0F);
        }
    }
}
