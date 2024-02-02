package boggled.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import boggled.campaign.econ.boggledTools;
import boggled.scripts.PlayerCargoCalculations.bogglesDefaultCargo;
import boggled.scripts.PlayerCargoCalculations.booglesCrewReplacerCargo;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

public class BoggledTascPlugin extends BaseModPlugin {
    static {
        ResourceDepositsCondition.COMMODITY.put(Conditions.RUINS_SCATTERED, boggledTools.BoggledCommodities.domainArtifacts);
        ResourceDepositsCondition.COMMODITY.put(Conditions.RUINS_WIDESPREAD, boggledTools.BoggledCommodities.domainArtifacts);
        ResourceDepositsCondition.COMMODITY.put(Conditions.RUINS_EXTENSIVE, boggledTools.BoggledCommodities.domainArtifacts);
        ResourceDepositsCondition.COMMODITY.put(Conditions.RUINS_VAST, boggledTools.BoggledCommodities.domainArtifacts);

        ResourceDepositsCondition.MODIFIER.put(Conditions.RUINS_SCATTERED, -1);
        ResourceDepositsCondition.MODIFIER.put(Conditions.RUINS_WIDESPREAD, 0);
        ResourceDepositsCondition.MODIFIER.put(Conditions.RUINS_EXTENSIVE, 1);
        ResourceDepositsCondition.MODIFIER.put(Conditions.RUINS_VAST, 2);

        ResourceDepositsCondition.INDUSTRY.put(boggledTools.BoggledCommodities.domainArtifacts, boggledTools.BoggledIndustries.domainArchaeologyIndustryId);

        ResourceDepositsCondition.BASE_MODIFIER.put(boggledTools.BoggledCommodities.domainArtifacts, -2);
    }

    static int lastGameLoad = 0;
    static int thisGameLoad = 0;

    public void applyStationSettingsToAllStationsInSector() {
        if(boggledTools.getBooleanSetting("boggledApplyStationSettingsToAllStationsInSector")) {
            for (StarSystemAPI system : Global.getSector().getStarSystems()) {
                for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
                    SectorEntityToken primaryEntity = market.getPrimaryEntity();
                    if (primaryEntity != null && primaryEntity.hasTag(Tags.STATION)) {
                        //Cramped Quarters also controls global hazard and accessibility modifications
                        //even if Cramped Quarters itself is disabled
                        if (!market.hasCondition(boggledTools.BoggledConditions.crampedQuartersConditionId)) {
                            market.addCondition(boggledTools.BoggledConditions.crampedQuartersConditionId);
                        }

                        //Some special items require "no_atmosphere" condition on market to be installed
                        //Stations by default don't meet this condition because they don't have the "no_atmosphere" condition
                        //Combined with market_conditions.csv overwrite, this will give stations no_atmosphere while
                        //hiding all effects from the player and having no impact on the economy or hazard rating
                        if (!market.hasCondition(Conditions.NO_ATMOSPHERE)) {
                            market.addCondition(Conditions.NO_ATMOSPHERE);
                            market.suppressCondition(Conditions.NO_ATMOSPHERE);
                        }
                    }
                }
            }
        }
    }

    public void applyTerraformingAbilitiesPerSettingsFile() {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled)) {
            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_open_terraforming_control_panel")) {
                Global.getSector().getCharacterData().addAbility("boggled_open_terraforming_control_panel");
            }
        } else {
            Global.getSector().getCharacterData().removeAbility("boggled_open_terraforming_control_panel");
        }
    }

    public void applyStationConstructionAbilitiesPerSettingsFile() {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.stationConstructionContentEnabled)) {
            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_construct_astropolis_station")) {
                if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.astropolisEnabled)) {
                    Global.getSector().getCharacterData().addAbility("boggled_construct_astropolis_station");
                }
            } else {
                if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.astropolisEnabled)) {
                    Global.getSector().getCharacterData().removeAbility("boggled_construct_astropolis_station");
                }
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_construct_mining_station")) {
                if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.miningStationEnabled)) {
                    Global.getSector().getCharacterData().addAbility("boggled_construct_mining_station");
                }
            } else {
                if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.miningStationEnabled)) {
                    Global.getSector().getCharacterData().removeAbility("boggled_construct_mining_station");
                }
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_construct_siphon_station")) {
                if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.siphonStationEnabled)) {
                    Global.getSector().getCharacterData().addAbility("boggled_construct_siphon_station");
                }
            } else {
                if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.siphonStationEnabled)) {
                    Global.getSector().getCharacterData().removeAbility("boggled_construct_siphon_station");
                }
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_colonize_abandoned_station")) {
                if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.stationColonizationEnabled)) {
                    Global.getSector().getCharacterData().addAbility("boggled_colonize_abandoned_station");
                }
            } else {
                if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.stationColonizationEnabled)) {
                    Global.getSector().getCharacterData().removeAbility("boggled_colonize_abandoned_station");
                }
            }
        } else {
            Global.getSector().getCharacterData().removeAbility("boggled_construct_astropolis_station");
            Global.getSector().getCharacterData().removeAbility("boggled_construct_mining_station");
            Global.getSector().getCharacterData().removeAbility("boggled_construct_siphon_station");
            Global.getSector().getCharacterData().removeAbility("boggled_colonize_abandoned_station");
        }
    }

    public void applyDomainArchaeologySettings() {
        //Enable/disable Domain-tech content
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled)) {
            if(Global.getSector().getFaction(Factions.LUDDIC_CHURCH) != null && !Global.getSector().getFaction(Factions.LUDDIC_CHURCH).isIllegal(boggledTools.BoggledCommodities.domainArtifacts)) {
                Global.getSector().getFaction(Factions.LUDDIC_CHURCH).makeCommodityIllegal(boggledTools.BoggledCommodities.domainArtifacts);
            }

            if(Global.getSector().getFaction(Factions.LUDDIC_PATH) != null && !Global.getSector().getFaction(Factions.LUDDIC_PATH).isIllegal(boggledTools.BoggledCommodities.domainArtifacts)) {
                Global.getSector().getFaction(Factions.LUDDIC_PATH).makeCommodityIllegal(boggledTools.BoggledCommodities.domainArtifacts);
            }

            Global.getSettings().getCommoditySpec(boggledTools.BoggledCommodities.domainArtifacts).getTags().clear();

            if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.replaceAgreusTechMiningWithDomainArchaeology)) {
                SectorEntityToken agreusPlanet = boggledTools.getPlanetTokenForQuest("Arcadia", "agreus");
                if(agreusPlanet != null) {
                    MarketAPI agreusMarket = agreusPlanet.getMarket();
                    if(agreusMarket != null && agreusMarket.hasIndustry(Industries.TECHMINING) && !agreusMarket.hasIndustry(boggledTools.BoggledIndustries.domainArchaeologyIndustryId) && !agreusMarket.isPlayerOwned()) {
                        // See boggledAgreusTechMiningEveryFrameScript for solution to Agreus Everybody loves KoC Techmining/Domain Archaeology issue
                        if(!Global.getSettings().getModManager().isModEnabled("Everybody loves KoC")) {
                            agreusMarket.addIndustry(boggledTools.BoggledIndustries.domainArchaeologyIndustryId);
                            agreusMarket.removeIndustry(Industries.TECHMINING, null, false);
                        } else {
                            Global.getSector().addTransientScript(new boggledAgreusTechMiningEveryFrameScript());
                        }
                    }
                }
            }
        } else {
            Global.getSettings().getCommoditySpec(boggledTools.BoggledCommodities.domainArtifacts).getTags().add("nonecon");
        }
    }

    public void addDomainTechBuildingsToVanillaColonies() {
        // Check to avoid null pointer exception if player has modified/randomized sector
        if(Global.getSector() == null || Global.getSector().getStarSystem("Askonia") == null) {
            return;
        }

        if(!Global.getSector().getPlayerPerson().hasTag("boggledDomainTechBuildingPlacementFinished")) {
            // Add Genelab on Volturn
            // Add LLN on Fikenhild
            // Add GPA on Ancyra
            if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.addDomainTechBuildingsToVanillaColonies)) {
                SectorEntityToken volturnPlanet = boggledTools.getPlanetTokenForQuest("Askonia", "volturn");
                if(volturnPlanet != null) {
                    MarketAPI volturnMarket = volturnPlanet.getMarket();
                    if(volturnMarket != null && !volturnMarket.hasIndustry(boggledTools.BoggledIndustries.genelabIndustryId)) {
                        volturnMarket.addIndustry(boggledTools.BoggledIndustries.genelabIndustryId);
                    }
                }

                SectorEntityToken fikenhildPlanet = boggledTools.getPlanetTokenForQuest("Westernesse", "fikenhild");
                if(fikenhildPlanet != null) {
                    MarketAPI fikenhildMarket = fikenhildPlanet.getMarket();
                    if(fikenhildMarket != null && !fikenhildMarket.hasIndustry("BOGGLED_LIMELIGHT_NETWORK")) {
                        fikenhildMarket.addIndustry("BOGGLED_LIMELIGHT_NETWORK");
                    }
                }

                SectorEntityToken ancyraPlanet = boggledTools.getPlanetTokenForQuest("Galatia", "ancyra");
                if(ancyraPlanet != null) {
                    MarketAPI ancyraMarket = ancyraPlanet.getMarket();
                    if(ancyraMarket != null && !ancyraMarket.hasIndustry("BOGGLED_GPA")) {
                        ancyraMarket.addIndustry("BOGGLED_GPA");
                    }
                }
            }

            Global.getSector().getPlayerPerson().addTag("boggledDomainTechBuildingPlacementFinished");
        }
    }

    public void replaceCryosanctums() {
        // Replace all Cryosanctums
        if(!Global.getSector().getPlayerPerson().hasTag("boggledCryosanctumReplacementFinished") && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.cryosanctumReplaceEverywhere)) {
            for(StarSystemAPI system : Global.getSector().getStarSystems()) {
                for(MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
                    if(market != null && market.hasIndustry(Industries.CRYOSANCTUM) && !market.hasIndustry(boggledTools.BoggledIndustries.cryosanctumIndustryId)) {
                        market.removeIndustry(Industries.CRYOSANCTUM, null, false);
                        market.addIndustry(boggledTools.BoggledIndustries.cryosanctumIndustryId);
                    }
                }
            }

            Global.getSector().getPlayerPerson().addTag("boggledCryosanctumReplacementFinished");
        }
    }

    public void enablePlanetKiller() {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.planetKillerEnabled)) {
            // PK weapons are deployed via ability, not a ground raid.
            // I left the mostly finished code for ground raid deployment in here in case I want to enable it in a future update.
            // Global.getSector().getListenerManager().addListener(new boggledPlanetKillerGroundRaidObjectiveListener());

            Global.getSector().getCharacterData().addAbility("boggled_deploy_planet_killer");
        }
    }

    public static void loadSettingsFromJSON() {
        if (lastGameLoad != thisGameLoad) {
            return;
        }

        Logger log = Global.getLogger(BoggledTascPlugin.class);
        try {
            SettingsAPI settings = Global.getSettings();
            // Utility stuff first, planet types, max planet resources, resource progressions, etc
            JSONArray planetTypes = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/planet_types.csv", boggledTools.BoggledMods.tascModId);
            JSONArray resourceProgressions = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/resource_progression.csv", boggledTools.BoggledMods.tascModId);
            JSONArray resourceLimits = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/planet_max_resource.csv", boggledTools.BoggledMods.tascModId);

            // Terraforming requirement, requirements, and project duration modifiers next
            JSONArray terraformingRequirement = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/terraforming_requirement.csv", boggledTools.BoggledMods.tascModId);
            JSONArray terraformingRequirements = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/terraforming_requirements_OR.csv", boggledTools.BoggledMods.tascModId);
            JSONArray terraformingDurationModifiers = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/duration_modifiers.csv", boggledTools.BoggledMods.tascModId);
            JSONArray terraformingProjectEffects = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/project_effects.csv", boggledTools.BoggledMods.tascModId);

            // Projects and industries both require requirements and duration modifiers
            JSONArray terraformingProjects = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/terraforming_projects.csv", boggledTools.BoggledMods.tascModId);
            JSONArray industryOptions = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/industry_options.csv", boggledTools.BoggledMods.tascModId);

            // And finally mods
            JSONArray industryOptionOverrides = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/industry_options_mods.csv", boggledTools.BoggledMods.tascModId);
            JSONArray terraformingProjectOverrides = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/terraforming_projects_mods.csv", boggledTools.BoggledMods.tascModId);

            boggledTools.initialiseResourceProgressionsFromJSON(resourceProgressions);
            boggledTools.initialiseResourceLimitsFromJSON(resourceLimits);

            boggledTools.initialiseTerraformingRequirementFromJSON(terraformingRequirement);
            boggledTools.initialiseTerraformingRequirementsFromJSON(terraformingRequirements);

            boggledTools.initialiseTerraformingDurationModifiersFromJSON(terraformingDurationModifiers);

            boggledTools.initialiseTerraformingProjectEffectsFromJSON(terraformingProjectEffects);

            boggledTools.initialiseTerraformingProjectsFromJSON(terraformingProjects);
            boggledTools.initialiseIndustryOptionsFromJSON(industryOptions);

            boggledTools.initialiseTerraformingProjectOverrides(terraformingProjectOverrides);
            boggledTools.initialiseIndustryOptionOverrides(industryOptionOverrides);

            boggledTools.initialisePlanetTypesFromJSON(planetTypes);

        } catch (IOException | JSONException ex) {
            log.error(ex);
        }

        if (lastGameLoad == thisGameLoad) {
            thisGameLoad++;
        }
    }

    @Override
    public void onNewGame() {
        loadSettingsFromJSON();

        applyStationSettingsToAllStationsInSector();
    }

    @Override
    public void afterGameSave() {
        enablePlanetKiller();

        applyStationSettingsToAllStationsInSector();

        applyStationConstructionAbilitiesPerSettingsFile();

        applyTerraformingAbilitiesPerSettingsFile();

        applyDomainArchaeologySettings();

        replaceCryosanctums();

        addDomainTechBuildingsToVanillaColonies();
    }

    @Override
    public void beforeGameSave() {
        Global.getSector().getCharacterData().removeAbility("construct_astropolis_station");
        Global.getSector().getCharacterData().removeAbility("construct_mining_station");
        Global.getSector().getCharacterData().removeAbility("construct_siphon_station");

        Global.getSector().getCharacterData().removeAbility("boggled_construct_astropolis_station");
        Global.getSector().getCharacterData().removeAbility("boggled_construct_mining_station");
        Global.getSector().getCharacterData().removeAbility("boggled_construct_siphon_station");
        Global.getSector().getCharacterData().removeAbility("boggled_colonize_abandoned_station");

        Global.getSector().getCharacterData().removeAbility("boggled_deploy_planet_killer");

        Global.getSector().getCharacterData().removeAbility("boggled_open_terraforming_control_panel");

        Global.getSettings().getCommoditySpec(boggledTools.BoggledCommodities.domainArtifacts).getTags().clear();

        Global.getSector().getListenerManager().removeListenerOfClass(boggledPlanetKillerGroundRaidObjectiveListener.class);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        lastGameLoad = thisGameLoad;
//        loadSettingsFromJSON();

        enablePlanetKiller();

        applyStationSettingsToAllStationsInSector();

        applyStationConstructionAbilitiesPerSettingsFile();

        applyTerraformingAbilitiesPerSettingsFile();

        applyDomainArchaeologySettings();

        addDomainTechBuildingsToVanillaColonies();

        //debugActionsPleaseIgnore();
    }

    @Override
    public void onApplicationLoad()  {
        boggledTools.initialiseDefaultStationConstructionFactories();
        boggledTools.initialiseDefaultTerraformingRequirementFactories();
        boggledTools.initialiseDefaultTerraformingDurationModifierFactories();
        boggledTools.initialiseDefaultCommoditySupplyAndDemandFactories();
        boggledTools.initialiseDefaultTerraformingProjectEffectFactories();

//        loadSettingsFromJSON();
        if (Global.getSettings().getModManager().isModEnabled("aaacrew_replacer")){
            bogglesDefaultCargo.active = new booglesCrewReplacerCargo();
        }else{
            bogglesDefaultCargo.active = new bogglesDefaultCargo();
        }
    }
}