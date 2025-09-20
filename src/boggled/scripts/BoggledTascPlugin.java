package boggled.scripts;

import boggled.campaign.econ.industries.plugins.TerraformingMenuOptionProvider;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import boggled.campaign.econ.boggledTools;
import boggled.scripts.PlayerCargoCalculations.boggledDefaultCargo;
import boggled.scripts.PlayerCargoCalculations.boggledCrewReplacerCargo;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

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

    static boolean aotdEnabled = Global.getSettings().getModManager().isModEnabled("aotd_vok");

    public void applyStationSettingsToAllStationsInSector() {
        if(boggledTools.getBooleanSetting("boggledApplyStationSettingsToAllStationsInSector")) {
            for (StarSystemAPI system : Global.getSector().getStarSystems()) {
                for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
                    SectorEntityToken primaryEntity = market.getPrimaryEntity();
                    if (primaryEntity != null && boggledTools.marketIsStation(market)) {
                        //Cramped Quarters also controls global hazard and accessibility modifications
                        //even if Cramped Quarters itself is disabled
                        if (!market.hasCondition(boggledTools.BoggledConditions.crampedQuartersConditionId)) {
                            market.addCondition(boggledTools.BoggledConditions.crampedQuartersConditionId);
                        }
                    }
                }
            }
        }

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
                SectorEntityToken primaryEntity = market.getPrimaryEntity();
                if (primaryEntity == null) {
                    continue;
                }
                if (primaryEntity.hasTag("boggled_mining_station")) {
                    primaryEntity.removeTag("boggled_mining_station");
                    primaryEntity.addTag(boggledTools.BoggledTags.miningStation);
                }
                if (primaryEntity.hasTag("boggled_siphon_station")) {
                    primaryEntity.removeTag("boggled_siphon_station");
                    primaryEntity.addTag(boggledTools.BoggledTags.siphonStation);
                }

                if (primaryEntity.hasTag(boggledTools.BoggledTags.astropolisStation)
                    || primaryEntity.hasTag(boggledTools.BoggledTags.miningStation)
                    || primaryEntity.hasTag(boggledTools.BoggledTags.siphonStation)) {
                    if (!market.getMemoryWithoutUpdate().contains("$startingFactionId")) {
                        market.getMemoryWithoutUpdate().set("$startingFactionId", "player");
                    }
                }
            }
        }
    }

    public void applyTerraformingAbilitiesPerSettingsFile() {
        if((   boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled)
            || boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechCraftingEnabled))
           && !aotdEnabled) {
            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_open_terraforming_control_panel")) {
                Global.getSector().getCharacterData().addAbility("boggled_open_terraforming_control_panel");
            }
        } else {
            Global.getSector().getCharacterData().removeAbility("boggled_open_terraforming_control_panel");
        }
    }

    public void applyStationConstructionAbilitiesPerSettingsFile() {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.stationConstructionContentEnabled) && !aotdEnabled) {
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

    public void applyDomainEraArtifactSettings() {
        //Enable/disable Domain-tech content
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled)) {
            if(Global.getSector().getFaction(Factions.LUDDIC_CHURCH) != null && !Global.getSector().getFaction(Factions.LUDDIC_CHURCH).isIllegal(boggledTools.BoggledCommodities.domainArtifacts)) {
                Global.getSector().getFaction(Factions.LUDDIC_CHURCH).makeCommodityIllegal(boggledTools.BoggledCommodities.domainArtifacts);
            }

            if(Global.getSector().getFaction(Factions.LUDDIC_PATH) != null && !Global.getSector().getFaction(Factions.LUDDIC_PATH).isIllegal(boggledTools.BoggledCommodities.domainArtifacts)) {
                Global.getSector().getFaction(Factions.LUDDIC_PATH).makeCommodityIllegal(boggledTools.BoggledCommodities.domainArtifacts);
            }

            Global.getSettings().getCommoditySpec(boggledTools.BoggledCommodities.domainArtifacts).getTags().clear();
        } else {
            Global.getSettings().getCommoditySpec(boggledTools.BoggledCommodities.domainArtifacts).getTags().add("nonecon");
        }
    }

    public void addDomainTechBuildingsToVanillaColonies() {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.addDomainTechBuildingsToVanillaColonies))
        {
            // Do this before modified/randomized sector check since we can replace cryosanctums on any planet
            replaceCryosanctums();

            // Check to avoid null pointer exception if player has modified/randomized sector
            if(Global.getSector() == null || Global.getSector().getStarSystem("Askonia") == null) {
                return;
            }

            if(!Global.getSector().getPlayerPerson().hasTag("boggledDomainTechBuildingPlacementFinished")) {
                // Replace Tech-Mining with Domain Archaeology on Agreus
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

                // Add Genelab on Volturn
                SectorEntityToken volturnPlanet = boggledTools.getPlanetTokenForQuest("Askonia", "volturn");
                if(volturnPlanet != null) {
                    MarketAPI volturnMarket = volturnPlanet.getMarket();
                    if(volturnMarket != null && !volturnMarket.hasIndustry(boggledTools.BoggledIndustries.genelabIndustryId)) {
                        volturnMarket.addIndustry(boggledTools.BoggledIndustries.genelabIndustryId);
                    }
                }

                // Add LLN on Fikenhild
                SectorEntityToken fikenhildPlanet = boggledTools.getPlanetTokenForQuest("Westernesse", "fikenhild");
                if(fikenhildPlanet != null) {
                    MarketAPI fikenhildMarket = fikenhildPlanet.getMarket();
                    if(fikenhildMarket != null && !fikenhildMarket.hasIndustry("BOGGLED_LIMELIGHT_NETWORK")) {
                        fikenhildMarket.addIndustry("BOGGLED_LIMELIGHT_NETWORK");
                    }
                }

                // Add GPA on Ancyra
                SectorEntityToken ancyraPlanet = boggledTools.getPlanetTokenForQuest("Galatia", "ancyra");
                if(ancyraPlanet != null) {
                    MarketAPI ancyraMarket = ancyraPlanet.getMarket();
                    if(ancyraMarket != null && !ancyraMarket.hasIndustry("BOGGLED_GPA")) {
                        ancyraMarket.addIndustry("BOGGLED_GPA");
                    }
                }

                Global.getSector().getPlayerPerson().addTag("boggledDomainTechBuildingPlacementFinished");
            }
        }
    }

    public void replaceCryosanctums() {
        // Replace all Cryosanctums with the Boggled_Cryosanctum industry that can demand Domain-era artifacts
        if(!Global.getSector().getPlayerPerson().hasTag("boggledCryosanctumReplacementFinished")) {
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

    private static JSONArray concatJSONArray(JSONArray... arrs) throws JSONException {
        JSONArray ret = new JSONArray();
        for (JSONArray arr : arrs) {
            for (int i = 0; i < arr.length(); ++i) {
                ret.put(arr.get(i));
            }
        }
        return ret;
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

            // Domed Cities suppressed conditions
            JSONArray domedCitiesSuppressedConditions = settings.getMergedSpreadsheetDataForMod("condition_id", "data/campaign/terraforming/domed_cities_suppressed_conditions.csv", boggledTools.BoggledMods.tascModId);

            // Stellar Reflector Array suppressed conditions
            JSONArray stellarReflectorArraySuppressedConditions = settings.getMergedSpreadsheetDataForMod("condition_id", "data/campaign/terraforming/stellar_reflector_array_suppressed_conditions.csv", boggledTools.BoggledMods.tascModId);

            // And finally mods
            JSONArray terraformingProjectOverrides = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/terraforming_projects_mods.csv", boggledTools.BoggledMods.tascModId);

            if (aotdEnabled) {
                JSONArray aotdRequirement = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/aotd_integration/terraforming_requirement.csv", boggledTools.BoggledMods.tascModId);
                JSONArray aotdRequirements = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/aotd_integration/terraforming_requirements_OR.csv", boggledTools.BoggledMods.tascModId);

                terraformingRequirement = concatJSONArray(terraformingRequirement, aotdRequirement);
                terraformingRequirements = concatJSONArray(terraformingRequirements, aotdRequirements);
            }

            boggledTools.initialiseResourceProgressionsFromJSON(resourceProgressions);
            boggledTools.initialiseResourceLimitsFromJSON(resourceLimits);

            boggledTools.initialiseTerraformingRequirementFromJSON(terraformingRequirement);
            boggledTools.initialiseTerraformingRequirementsFromJSON(terraformingRequirements);

            boggledTools.initialisePlanetTypesFromJSON(planetTypes);

            boggledTools.initialiseTerraformingDurationModifiersFromJSON(terraformingDurationModifiers);

            boggledTools.initialiseTerraformingProjectEffectsFromJSON(terraformingProjectEffects);

            boggledTools.initialiseTerraformingProjectsFromJSON(terraformingProjects);

            boggledTools.initialiseTerraformingProjectOverrides(terraformingProjectOverrides);

            boggledTools.initializeDomedCitiesSuppressedConditionsFromJSON(domedCitiesSuppressedConditions);

            boggledTools.initializeStellarReflectorArraySuppressedConditionsFromJSON(stellarReflectorArraySuppressedConditions);

            if (aotdEnabled) {
                JSONArray aotdProjectOverrides = settings.getMergedSpreadsheetDataForMod("id", "data/campaign/terraforming/aotd_integration/terraforming_projects_mods.csv", boggledTools.BoggledMods.tascModId);
                boggledTools.initialiseTerraformingProjectOverrides(aotdProjectOverrides);
            }

        } catch (IOException | JSONException ex) {
            log.error(ex);
        }

        if (lastGameLoad == thisGameLoad) {
            thisGameLoad++;
        }
    }

    private void addAotDEveryFrameScript() {
        if (aotdEnabled) {
            Map<List<String>, List<String>> researchAndAbilityIds = new LinkedHashMap<>();
            researchAndAbilityIds.put(Collections.singletonList("tasc_station_restoration"), Collections.singletonList("boggled_colonize_abandoned_station"));
            researchAndAbilityIds.put(Collections.singletonList("tasc_astropolis_construction"), Collections.singletonList("boggled_construct_astropolis_station"));
            researchAndAbilityIds.put(Collections.singletonList("tasc_industrial_stations"), asList("boggled_construct_mining_station", "boggled_construct_siphon_station"));

            researchAndAbilityIds.put(asList("tasc_terraforming_templates", "tasc_atmosphere_manipulation", "tasc_genetic_manipulation"), Collections.singletonList("boggled_open_terraforming_control_panel"));

            Global.getSector().getPlayerFleet().addScript(new BoggledAotDEveryFrameScript(researchAndAbilityIds));
        }
    }

    private void registerListeners()
    {
        TerraformingMenuOptionProvider.register();
    }

    @Override
    public void onNewGame() { }

    @Override
    public void afterGameSave() {
        applyStationSettingsToAllStationsInSector();

        applyStationConstructionAbilitiesPerSettingsFile();

        applyTerraformingAbilitiesPerSettingsFile();

        applyDomainEraArtifactSettings();

        addDomainTechBuildingsToVanillaColonies();

        addAotDEveryFrameScript();
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

        Global.getSector().getPlayerFleet().removeScriptsOfClass(BoggledAotDEveryFrameScript.class);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        applyStationSettingsToAllStationsInSector();

        applyStationConstructionAbilitiesPerSettingsFile();

        applyTerraformingAbilitiesPerSettingsFile();

        applyDomainEraArtifactSettings();

        addDomainTechBuildingsToVanillaColonies();

        registerListeners();

        addAotDEveryFrameScript();

        lastGameLoad = thisGameLoad;
    }

    @Override
    public void onApplicationLoad()  {
        boggledTools.initialiseDefaultStationConstructionFactories();
        boggledTools.initialiseDefaultTerraformingRequirementFactories();
        boggledTools.initialiseDefaultTerraformingDurationModifierFactories();
        boggledTools.initialiseDefaultTerraformingProjectEffectFactories();

        loadSettingsFromJSON();

        if (Global.getSettings().getModManager().isModEnabled("aaacrew_replacer")) {
            boggledDefaultCargo.active = new boggledCrewReplacerCargo();
        } else {
            boggledDefaultCargo.active = new boggledDefaultCargo();
        }

        if (aotdEnabled) {
            boggledTools.initialiseModIgnoreSettings();
        }
    }
}