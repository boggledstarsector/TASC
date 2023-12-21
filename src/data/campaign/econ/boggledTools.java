package data.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CircularFleetOrbit;
import com.fs.starfarer.campaign.CircularOrbit;
import com.fs.starfarer.campaign.CircularOrbitPointDown;
import com.fs.starfarer.campaign.CircularOrbitWithSpin;
import com.fs.starfarer.loading.specs.PlanetSpec;
import illustratedEntities.helper.ImageHandler;
import illustratedEntities.helper.Settings;
import illustratedEntities.helper.TextHandler;
import illustratedEntities.memory.ImageDataMemory;
import illustratedEntities.memory.TextDataEntry;
import illustratedEntities.memory.TextDataMemory;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;

import static java.util.Arrays.asList;

public class boggledTools
{
    public static class BoggledMods {
        public static final String lunalibModId = "lunalib";
        public static final String illustratedEntitiesModId = "illustrated_entities";
        public static final String tascModId = "Terraforming & Station Construction";
    }

    public static class BoggledSettings {
        // Overriding enable check, used in the ability checks. Don't bother putting it in terraforming_projects.csv
        public static final String terraformingContentEnabled = "boggledTerraformingContentEnabled";
        public static final String stationConstructionContentEnabled = "boggledStationConstructionContentEnabled";
        public static final String astropolisEnabled = "boggledAstropolisEnabled";
        public static final String miningStationEnabled = "boggledMiningStationEnabled";
        public static final String siphonStationEnabled = "boggledSiphonStationEnabled";
        public static final String stationColonizationEnabled = "boggledStationColonizationEnabled";

        public static final String perihelionProjectEnabled = "boggledPerihelionProjectEnabled";
        public static final String planetCrackerEnabled = "boggledPlanetCrackerEnabled";

        public static final String planetKillerEnabled = "boggledPlanetKillerEnabled";

        public static final String replaceAgreusTechMiningWithDomainArchaeology = "boggledReplaceAgreusTechMiningWithDomainArchaeology";

        public static final String addDomainTechBuildingsToVanillaColonies = "boggledAddDomainTechBuildingsToVanillaColonies";
        public static final String cryosanctumReplaceEverywhere = "boggledCryosanctumReplaceEverywhere";

        // Building enables, checked in campaign.econ.industries.*
        // May move them to a CSV later
        public static final String enableAIMiningDronesStructure = "boggledEnableAIMiningDronesStructure";
        public static final String domainTechContentEnabled = "boggledDomainTechContentEnabled";
        public static final String domainArchaeologyEnabled = "boggledDomainArchaeologyEnabled";
        public static final String CHAMELEONEnabled = "boggledCHAMELEONEnabled";
        public static final String cloningEnabled = "boggledCloningEnabled";
        public static final String cryosanctumPlayerBuildEnabled = "boggledCryosanctumPlayerBuildEnabled";
        public static final String domedCitiesEnabled = "boggledDomedCitiesEnabled";
        public static final String genelabEnabled = "boggledGenelabEnabled";
        public static final String harmonicDamperEnabled = "boggledHarmonicDamperEnabled";
        public static final String hydroponicsEnabled = "boggledHydroponicsEnabled";
        public static final String kletkaSimulatorEnabled = "boggledKletkaSimulatorEnabled";
        public static final String limelightNetworkPlayerBuildEnabled = "boggledLimelightNetworkPlayerBuildEnabled";
        public static final String magnetoshieldEnabled = "boggledMagnetoshieldEnabled";
        public static final String mesozoicParkEnabled = "boggledMesozoicParkEnabled";
        public static final String ouyangOptimizerEnabled = "boggledOuyangOptimizerEnabled";
        public static final String planetaryAgravFieldEnabled = "boggledPlanetaryAgravFieldEnabled";
        public static final String remnantStationEnabled = "boggledRemnantStationEnabled";
        public static final String stellarReflectorArrayEnabled = "boggledStellarReflectorArrayEnabled";

        public static final String domedCitiesDefensePenaltyEnabled = "boggledDomedCitiesDefensePenaltyEnabled";
        public static final String stationCrampedQuartersEnabled = "boggledStationCrampedQuartersEnabled";
        public static final String stationCrampedQuartersPlayerCanPayToIncreaseStationSize = "boggledStationCrampedQuartersPlayerCanPayToIncreaseStationSize";
        public static final String stationCrampedQuartersSizeGrowthReductionStarts = "boggledStationCrampedQuartersSizeGrowthReductionStarts";
        public static final String stationProgressIncreaseInCostsToExpandStation = "boggledStationProgressiveIncreaseInCostsToExpandStation";

        public static final String kletkaSimulatorTemperatureBasedUpkeep = "boggledKletkaSimulatorTemperatureBasedUpkeep";

        public static final String miningStationUltrarichOre = "boggledMiningStationUltrarichOre";
        public static final String miningStationRichOre = "boggledMiningStationRichOre";
        public static final String miningStationAbundantOre = "boggledMiningStationAbundantOre";
        public static final String miningStationModerateOre = "boggledMiningStationModerateOre";
        public static final String miningStationSparseOre = "boggledMiningStationSparseOre";

        public static final String domainTechCraftingArtifactCost = "boggledDomainTechCraftingArtifactCost";
        public static final String domainTechCraftingStoryPointCost = "boggledDomainTechCraftingStoryPointCost";

        public static final String miningStationLinkToResourceBelts = "boggledMiningStationLinkToResourceBelts";
        public static final String miningStationStaticAmount = "boggledMiningStationStaticAmount";

        public static final String siphonStationLinkToGasGiant = "boggledSiphonStationLinkToGasGiant";
        public static final String siphonStationStaticAmount = "boggledSiphonStationStaticAmount";

        public static final String terraformingTypeChangeAddVolatiles = "boggledTerraformingTypeChangeAddVolatiles";

        public static final String stableLocationGateCostHeavyMachinery = "boggledStableLocationGateCostHeavyMachinery";
        public static final String stableLocationGateCostMetals = "boggledStableLocationGateCostMetals";
        public static final String stableLocationGateCostTransplutonics = "boggledStableLocationGateCostTransplutonics";
        public static final String stableLocationGateCostDomainEraArtifacts = "boggledStableLocationGateCostDomainEraArtifacts";

        public static final String stableLocationDomainTechStructureCostHeavyMachinery = "boggledStableLocationDomainTechStructureCostHeavyMachinery";
        public static final String stableLocationDomainTechStructureCostMetals = "boggledStableLocationDomainTechStructureCostMetals";
        public static final String stableLocationDomainTechStructureCostTransplutonics = "boggledStableLocationDomainTechStructureCostTransplutonics";
        public static final String stableLocationDomainTechStructureCostDomainEraArtifacts = "boggledStableLocationDomainTechStructureCostDomainEraArtifacts";

        public static final String marketSizeRequiredToBuildInactiveGate = "boggledMarketSizeRequiredToBuildInactiveGate";

        public static final String planetKillerAllowDestructionOfColoniesMarkedAsEssentialForQuests = "boggledPlanetKillerAllowDestructionOfColoniesMarkedAsEssentialForQuests";

        public static final String perihelionProjectDaysToFinish = "boggledPerihelionProjectDaysToFinish";
    }

    public static class BoggledTags {
        public static final String constructionProgressDays = "boggled_construction_progress_days_";
        public static final String constructionProgressLastDayChecked = "boggled_construction_progress_lastDayChecked_";

        public static final String terraformingController = "boggledTerraformingController";

        public static final String lightsOverlayAstropolisAlphaSmall = "boggled_lights_overlay_astropolis_alpha_small";
        public static final String lightsOverlayAstropolisAlphaMedium = "boggled_lights_overlay_astropolis_alpha_medium";
        public static final String lightsOverlayAstropolisAlphaLarge = "boggled_lights_overlay_astropolis_alpha_large";

        public static final String lightsOverlayAstropolisBetaSmall = "boggled_lights_overlay_astropolis_beta_small";
        public static final String lightsOverlayAstropolisBetaMedium = "boggled_lights_overlay_astropolis_beta_medium";
        public static final String lightsOverlayAstropolisBetaLarge = "boggled_lights_overlay_astropolis_beta_large";

        public static final String lightsOverlayAstropolisGammaSmall = "boggled_lights_overlay_astropolis_gamma_small";
        public static final String lightsOverlayAstropolisGammaMedium = "boggled_lights_overlay_astropolis_gamma_medium";
        public static final String lightsOverlayAstropolisGammaLarge = "boggled_lights_overlay_astropolis_gamma_large";

        public static final String lightsOverlayMiningSmall = "boggled_lights_overlay_mining_small";
        public static final String lightsOverlayMiningMedium = "boggled_lights_overlay_mining_medium";
        public static final String lightsOverlaySiphonSmall = "boggled_lights_overlay_siphon_small";
        public static final String lightsOverlaySiphonMedium = "boggled_lights_overlay_siphon_medium";
        public static final String alreadyReappliedLightsOverlay = "boggled_already_reapplied_lights_overlay";

        public static final String miningStationSmall = "boggled_mining_station_small";
        public static final String miningStationMedium = "boggled_mining_station_medium";

        public static final String stationConstructionNumExpansionsOne = "boggled_station_construction_numExpansions_1";
        public static final String stationConstructionNumExpansions = "boggled_station_construction_numExpansions_";
    }

    public static class BoggledSounds {
        public static final String stationConstructed = "ui_boggled_station_constructed";
    }

    public static class BoggledCommodities {
        public static final String domainArtifacts = "domain_artifacts";
    }

    public static class BoggledEntities {

    }

    public static class BoggledIndustries {
        public static final String AIMiningDronesIndustryId = "BOGGLED_AI_MINING_DRONES";
        public static final String atmosphereProcessorIndustryId = "BOGGLED_ATMOSPHERE_PROCESSOR";
        public static final String CHAMELEONIndustryId = "BOGGLED_CHAMELEON";
        public static final String cloningIndustryId = "BOGGLED_CLONING";
        public static final String cryosanctumIndustryId = "BOGGLED_CRYOSANCTUM";
        public static final String domainArchaeologyIndustryId = "BOGGLED_DOMAIN_ARCHAEOLOGY";
        public static final String domedCitiesIndustryId = "BOGGLED_DOMED_CITIES";
        public static final String genelabIndustryId = "BOGGLED_GENELAB";
        public static final String harmonicDamperIndustryId = "BOGGLED_HARMONIC_DAMPER";
        public static final String hydroponicsIndustryId = "BOGGLED_HYDROPONICS";
        public static final String ismaraSlingIndustryId = "BOGGLED_ISMARA_SLING";
        public static final String kletkaSimulatorIndustryId = "BOGGLED_KLETKA_SIMULATOR";
        public static final String magnetoShieldIndustryId = "BOGGLED_MAGNETOSHIELD";
        public static final String mesozoicParkIndustryId = "BOGGLED_MESOZOIC_PARK";
        public static final String perihelionProjectIndustryId = "BOGGLED_PERIHELION_PROJECT";
        public static final String planetaryAgravFieldIndustryId = "BOGGLED_PLANETARY_AGRAV_FIELD";
        public static final String remnantStationIndustryId = "BOGGLED_REMNANT_STATION";
        public static final String stationExpansionIndustryId = "BOGGLED_STATION_EXPANSION";
        public static final String stellarReflectorArrayIndustryId = "BOGGLED_STELLAR_REFLECTOR_ARRAY";
    }

    private static final String starPlanetId = "star";

    private static final String barrenPlanetId = "barren";
    public static final String desertPlanetId = "desert";
    public static final String frozenPlanetId = "frozen";
    public static final String gasGiantPlanetId = "gas_giant";
    public static final String junglePlanetId = "jungle";
    public static final String terranPlanetId = "terran";
    private static final String toxicPlanetId = "toxic";
    private static final String tundraPlanetId = "tundra";
    private static final String volcanicPlanetId = "volcanic";
    public static final String waterPlanetId = "water";

    public static final String unknownPlanetId = "unknown";

    public static class BoggledConditions {
        public static final String terraformingControllerConditionId = "terraforming_controller";
        private static final String spriteControllerConditionId = "sprite_controller";

        public static final String crampedQuartersConditionId = "cramped_quarters";
    }

    private static class BoggledProjectRequirements {
        public static final String colonyHasAtLeast100kInhabitantsRequirementId = "colony_has_at_least_100k_inhabitants";

        public static final String colonyHasOrbitalWorksWPristineNanoforgeRequirementId = "colony_has_orbital_works_w_pristine_nanoforge";

        public static final String fleetCargoContainsAtLeastEasyDomainArtifactsRequirementId = "fleet_cargo_contains_at_least_easy_domain_artifacts";
        public static final String fleetCargoContainsAtLeastMediumDomainArtifactsRequirementId = "fleet_cargo_contains_at_least_medium_domain_artifacts";
        public static final String fleetCargoContainsAtLeastHardDomainArtifactsRequirementId = "fleet_cargo_contains_at_least_hard_domain_artifacts";

        public static final String playerHasStoryPointsRequirementId = "player_has_at_least_story_points";

        public static final String colonyHasAtLeast100kInhabitants = "Colony has at least 100,000 inhabitants";

        public static final String colonyHasOrbitalWorksWPristineNanoforge = "Colony has orbital works with a pristine nanoforge";
    }
    // A mistyped string compiles fine and leads to plenty of debugging. A mistyped constant gives an error.

//    public static final String typeChangeProjectKey = "type_change";
//    public static final String resourceImprovementKey = "resource_improvement";
//    public static final String conditionImprovementKey = "condition_improvement";

    public static final String noneProjectId = "None";

//    public static final String farmlandResourceImprovementProjectId = "farmland_resource_improvement";
//    public static final String organicsResourceImprovementProjectId = "organics_resource_improvement";
//    public static final String volatilesResourceImprovementProjectId = "volatiles_resource_improvement";

    public static final String craftCorruptedNanoforgeProjectId = "craftCorruptedNanoforge";
    public static final String craftPristineNanoforgeProjectId = "craftPristineNanoforge";
    public static final String craftSynchrotronProjectId = "craftSynchrotron";
    public static final String craftHypershuntTapProjectId = "craftHypershuntTap";
    public static final String craftCryoarithmeticEngineProjectId = "craftCryoarithmeticEngine";
    public static final String craftPlanetKillerDeviceProjectId = "craftPlanetKillerDevice";
    public static final String craftFusionLampProjectId = "craftFusionLamp";
    public static final String craftFullereneSpoolProjectId = "craftFullereneSpool";
    public static final String craftPlasmaDynamoProjectId = "craftPlasmaDynamo";
    public static final String craftAutonomousMantleBoreProjectId = "craftAutonomousMantleBore";
    public static final String craftSoilNanitesProjectId = "craftSoilNanites";
    public static final String craftCatalyticCoreProjectId = "craftCatalyticCore";
    public static final String craftCombatDroneReplicatorProjectId = "craftCombatDroneReplicator";
    public static final String craftBiofactoryEmbryoProjectId = "crafyBiofactoryEmbryo";
    public static final String craftDealmakerHolosuiteProjectId = "craftDealmakerHolosuite";

    public static final String craftCorruptedNanoforgeProjectTooltip = "Craft Corrupted Nanoforge";
    public static final String craftPristineNanoforgeProjectTooltip = "Craft Pristine Nanoforge";
    public static final String craftSynchrotronProjectTooltip = "Craft Synchrotron";
    public static final String craftHypershuntTapProjectTooltip = "Craft Hypershunt Tap";
    public static final String craftCryoarithmeticEngineProjectTooltip = "Craft Cryoarithmetic Engine";
    public static final String craftPlanetKillerDeviceProjectTooltip = "Craft Planet Killer Device";
    public static final String craftFusionLampProjectTooltip = "Craft Fusion Lamp";
    public static final String craftFullereneSpoolProjectTooltip = "Craft Fullerene Spool";
    public static final String craftPlasmaDynamoProjectTooltip = "Craft Plasma Dynamo";
    public static final String craftAutonomousMantleBoreProjectTooltip = "Craft Autonomous Mantle Bore";
    public static final String craftSoilNanitesProjectTooltip = "Craft Soil Nanites";
    public static final String craftCatalyticCoreProjectTooltip = "Craft Catalytic Core";
    public static final String craftCombatDroneReplicatorProjectTooltip = "Craft Combat Drone Replicator";
    public static final String craftBiofactoryEmbryoProjectTooltip = "Craft Biofactory Embryo";
    public static final String craftDealmakerHolosuiteProjectTooltip = "Craft Dealmaker Holosuite";

    public static final HashMap<String, TerraformingRequirementFactory> terraformingRequirementFactories = new HashMap<>();
    public static final HashMap<String, TerraformingDurationModifierFactory> terraformingDurationModifierFactories = new HashMap<>();

    public static void initialiseDefaultTerraformingRequirementFactories() {
        addTerraformingRequirementFactory("PlanetTypeRequirement", new PlanetTypeRequirementFactory());
        addTerraformingRequirementFactory("MarketHasCondition", new MarketHasConditionFactory());
        addTerraformingRequirementFactory("MarketHasIndustry", new MarketHasIndustryFactory());
        addTerraformingRequirementFactory("MarketHasIndustryWithItem", new MarketHasIndustryWithItemFactory());
        addTerraformingRequirementFactory("MarketHasWaterPresent", new MarketHasWaterPresentFactory());
        addTerraformingRequirementFactory("MarketIsAtLeastSize", new MarketIsAtLeastSizeFactory());
        addTerraformingRequirementFactory("FleetCargoContainsAtLeast", new FleetCargoContainsAtLeastFactory());
        addTerraformingRequirementFactory("PlayerHasStoryPointsAtLeast", new PlayerHasStoryPointsAtLeastFactory());
        addTerraformingRequirementFactory("WorldTypeSupportsResourceImprovement", new WorldTypeSupportsResourceImprovementFactory());
    }

    public static void addTerraformingRequirementFactory(String key, TerraformingRequirementFactory value) {
        Global.getLogger(boggledTools.class).info("Adding terraforming requirement factory " + key);
        terraformingRequirementFactories.put(key, value);
    }

    public static void initialiseDefaultTerraformingDurationModifierFactories() {
        addTerraformingDurationModifierFactory("PlanetSize", new PlanetSizeDurationModifierFactory());
    }

    public static void addTerraformingDurationModifierFactory(String key, TerraformingDurationModifierFactory value) {
        Global.getLogger(boggledTools.class).info("Adding terraforming duration modifier factory " + key);
        terraformingDurationModifierFactories.put(key, value);
    }

    private static ArrayList<String> arrayListFromJSON(JSONObject data, String key, String regex) throws JSONException {
        String toSplit = data.getString(key);
        if (toSplit.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(toSplit.split(regex)));
    }

    public static void initialisePlanetTypesFromJSON(JSONArray planetTypesJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, PlanetType> planetTypesMap = new HashMap<>();
        LinkedHashMap<String, String> planetConditionsMap = new LinkedHashMap<>();

        planetTypesMap.put(unknownPlanetId, new PlanetType(unknownPlanetId, false, 0, new ArrayList<TerraformingRequirements>(), 0));

        for (int i = 0; i < planetTypesJSON.length(); ++i) {
            try {
                JSONObject row = planetTypesJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String[] conditions = row.getString("conditions").split("\\|");
                String planetTypeId = row.getString("terraforming_type_id");
                boolean terraformingPossible = row.getBoolean("terraforming_possible");

                int baseWaterLevel = row.getInt("base_water_level");
                String[] conditionalWaterRequirementsString = row.getString("conditional_water_requirements").split("\\|");
                int conditionalWaterLevel = row.optInt("conditional_water_level", 0);

                ArrayList<TerraformingRequirements> conditionalWaterRequirements = new ArrayList<>();
                for (String conditionalWaterRequirement : conditionalWaterRequirementsString) {
                    if (conditionalWaterRequirement.isEmpty()) {
                        continue;
                    }

                    TerraformingRequirements waterReq = terraformingRequirements.get(conditionalWaterRequirement);
                    if (waterReq != null) {
                        conditionalWaterRequirements.add(waterReq);
                    } else {
                        log.error("Conditional water requirement " + id + " failed to get conditional water requirement " + conditionalWaterRequirement);
                    }
                }

                PlanetType planetType = new PlanetType(planetTypeId, terraformingPossible, baseWaterLevel, conditionalWaterRequirements, conditionalWaterLevel);

                planetTypesMap.put(id, planetType);
                for (String condition : conditions) {
                    planetConditionsMap.put(condition, planetTypeId);
                }

            } catch (JSONException e) {
                log.error(e);
            }
        }

        boggledTools.planetTypesMap = planetTypesMap;
        boggledTools.planetConditionsMap = planetConditionsMap;
    }

    public static void initialiseResourceProgressionsFromJSON(JSONArray resourceProgressionsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, ArrayList<String>> resourceProgressionsMap = new HashMap<>();

        for (int i = 0; i < resourceProgressionsJSON.length(); ++i) {
            try {
                JSONObject row = resourceProgressionsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id.isEmpty()) {
                    continue;
                }

                ArrayList<String> resource_progression = arrayListFromJSON(row, "resource_progression", "\\|");

                resourceProgressionsMap.put(id, resource_progression);
            } catch (JSONException e) {
                log.error(e);
            }
        }

        boggledTools.resourceProgressions = resourceProgressionsMap;
    }

    public static void initialiseResourceLimitsFromJSON(JSONArray resourceLimitsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<Pair<String, String>, String> resourceLimits = new HashMap<>();

        for (int i = 0; i < resourceLimitsJSON.length(); ++i) {
            try {
                JSONObject row = resourceLimitsJSON.getJSONObject(i);

                String[] id = row.getString("id").split("\\|");
                if (id[0].isEmpty()) {
                    continue;
                }

                String resourceMax = row.getString("resource_max");

                assert(id.length == 2);
                Pair<String, String> key = new Pair<>(id[0], id[1]);

                resourceLimits.put(key, resourceMax);

            } catch (JSONException e) {
                log.error(e);
            }
        }

        boggledTools.resourceLimits = resourceLimits;
    }

    public static void initialiseTerraformingRequirementFromJSON(JSONArray terraformingRequirementJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, TerraformingRequirement> terraformingReqs = new HashMap<>();

        for (int i = 0; i < terraformingRequirementJSON.length(); ++i) {
            try {
                JSONObject row = terraformingRequirementJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String requirementType = row.getString("requirement_type");
                boolean invert = row.getBoolean("invert");
                String data = row.getString("data");

                TerraformingRequirementFactory factory = terraformingRequirementFactories.get(requirementType);
                if (factory != null) {
                    try {
                        TerraformingRequirement req = factory.constructFromJSON(id, invert, data);
                        if (req != null) {
                            terraformingReqs.put(id, req);
                        } else {
                            log.error("Requirement " + id + " of type " + requirementType + " was null when created with data " + data);
                        }
                    } catch (AbstractMethodError e) {
                        log.error("Requirement " + id + " of type " + requirementType + " has incorrect constructFromJSON function signature");
                    }
                } else {
                    log.info("No factory for requirement type " + requirementType);
                }

            } catch (JSONException e) {
                log.error(e);
            }
        }

        boggledTools.terraformingRequirement = terraformingReqs;
    }

    public static void initialiseTerraformingRequirementsFromJSON(JSONArray terraformingRequirementsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, TerraformingRequirements> terraformingReqss = new HashMap<>();
        for (int i = 0; i < terraformingRequirementsJSON.length(); ++i) {
            try {
                JSONObject row = terraformingRequirementsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id.isEmpty()) {
                    continue;
                }

                String tooltip = row.getString("tooltip");
                boolean invertAll = row.getBoolean("invert_all");
                String[] requirements = row.getString("requirements").split("\\|");

                ArrayList<TerraformingRequirement> reqs = new ArrayList<>();
                for (String requirement : requirements) {
                    TerraformingRequirement req = terraformingRequirement.get(requirement);
                    if (req != null) {
                        reqs.add(req);
                    } else {
                        log.error("Requirements " + id + " has invalid requirement " + requirement);
                    }
                }

                TerraformingRequirements terraformingReqs = new TerraformingRequirements(id, tooltip, invertAll, reqs);
                terraformingReqss.put(id, terraformingReqs);

            } catch (JSONException e) {
                log.error(e);
            }
        }
        boggledTools.terraformingRequirements = terraformingReqss;
    }

    public static void initialiseTerraformingProjectsFromJSON(JSONArray terraformingProjectsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        LinkedHashMap<String, TerraformingProject> terraformingProjects = new LinkedHashMap<>();
        for (int i = 0; i < terraformingProjectsJSON.length(); ++i) {
            try {
                JSONObject row = terraformingProjectsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String enableSetting = row.getString("enable_setting");
                if (!enableSetting.isEmpty() && !getBooleanSetting(enableSetting)) {
                    log.info("Setting " + enableSetting + " is false, disabling project " + id);
                    continue;
                } else if (!enableSetting.isEmpty()) {
                    log.info("Setting " + enableSetting + " is true, enabling project " + id);
                }

                String tooltip = row.getString("tooltip");
                String[] requirementsStrings = row.getString("requirements").split("\\|");
                String planetTypeChange = row.getString("planet_type_change");
                ArrayList<String> conditionsAdded = arrayListFromJSON(row, "conditions_added", "\\|");
                ArrayList<String> conditionsRemoved = arrayListFromJSON(row, "conditions_removed", "\\|");

                ArrayList<String> optionalConditions = arrayListFromJSON(row, "optional_conditions", "\\|");
                assert(optionalConditions.size() % 2 == 0);
                for (int j = 0; j < optionalConditions.size(); j += 2) {
                    Pair<String, String> optionalCondition = new Pair<>(optionalConditions.get(j), optionalConditions.get(j + 1));
                    if (getBooleanSetting(optionalCondition.one)) {
                        log.info("Setting " + optionalCondition.one + " is true, adding option " + optionalCondition.two + " to " + id);
                        conditionsAdded.add(optionalCondition.two);
                    } else {
                        log.info("Setting " + optionalCondition.one + " is false, remove option " + optionalCondition.two + " to " + id);
                        conditionsRemoved.add(optionalCondition.two);
                    }
                }

                ArrayList<String> conditionsProgress = arrayListFromJSON(row, "conditions_to_progress", "\\|");

                int baseProjectDuration = row.getInt("base_project_duration");

                String[] projectDurationModifiers = row.getString("dynamic_project_duration_modifiers").split("\\|");
                ArrayList<TerraformingDurationModifier> terraformingDurationModifiers = new ArrayList<>();
                for (String projectDurationModifier : projectDurationModifiers) {
                    TerraformingDurationModifierFactory factory = terraformingDurationModifierFactories.get(projectDurationModifier);
                    if (factory != null) {
                        String data = "";
                        TerraformingDurationModifier durationModifier = factory.constructFromJSON(data);
                        if (durationModifier != null) {
                            terraformingDurationModifiers.add(durationModifier);
                        } else {
                            log.error("Project " + id + " duration modifier " + projectDurationModifier + " was null when created with data " + data);
                        }
                    }
                }

                ArrayList<TerraformingRequirements> reqs = new ArrayList<>();
                for (String requirementsString : requirementsStrings) {
                    TerraformingRequirements req = terraformingRequirements.get(requirementsString);
                    if (req != null) {
                        reqs.add(req);
                    }
                }

                TerraformingProject terraformingProj = new TerraformingProject(id, tooltip, reqs, planetTypeChange, conditionsAdded, conditionsRemoved, conditionsProgress, baseProjectDuration, terraformingDurationModifiers);
                terraformingProjects.put(id, terraformingProj);

            } catch (JSONException e) {
                log.error(e);
            }
        }
        boggledTools.terraformingProjects = terraformingProjects;
    }

    public static void initialiseTerraformingRequirementsOverrides(JSONArray terraformingRequirementsOverrideJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        try {
            for (int i = 0; i < terraformingRequirementsOverrideJSON.length(); ++i) {
                JSONObject row = terraformingRequirementsOverrideJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String requirementsId = row.getString("requirements_id");
                TerraformingRequirements reqs = terraformingRequirements.get(requirementsId);
                if (reqs == null) {
                    log.error("Mod " + id + " terraforming requirements " + requirementsId + " not found, ignoring");
                    continue;
                }

                String[] requirementAddedStrings = row.getString("requirement_added").split("\\|");
                String[] requirementRemovedStrings = row.getString("requirement_removed").split("\\|");

                ArrayList<TerraformingRequirement> requirementAdded = new ArrayList<>();
                for (String requirementAddedString : requirementAddedStrings) {
                    TerraformingRequirement req = terraformingRequirement.get(requirementAddedString);
                    if (req != null) {
                        requirementAdded.add(req);
                    }
                }

                reqs.addRemoveProjectRequirement(requirementAdded, requirementRemovedStrings);

            }
        } catch (JSONException e) {
            log.error(e);
        }
    }

    public static void initialiseTerraformingProjectOverrides(JSONArray terraformingProjectsOverrideJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        try {
            for (int i = 0; i < terraformingProjectsOverrideJSON.length(); ++i) {
                JSONObject row = terraformingProjectsOverrideJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String projectId = row.getString("project_id");
                TerraformingProject proj = terraformingProjects.get(projectId);
                if (proj == null) {
                    log.error("Mod " + id + " terraforming project " + projectId + " not found, ignoring");
                    continue;
                }

                String tooltipOverride = row.getString("tooltip_override");
                String tooltipAddition = row.getString("tooltip_addition");

                String[] requirementsAddedStrings = row.getString("requirements_added").split("\\|");
                String[] requirementsRemovedStrings = row.getString("requirements_removed").split("\\|");
                String planetTypeChangeOverride = row.getString("planet_type_change_override");
                ArrayList<String> conditionsAdded = arrayListFromJSON(row, "conditions_added", "\\|");
                ArrayList<String> conditionsRemoved = arrayListFromJSON(row, "conditions_removed", "\\|");
                ArrayList<String> conditionsOption = arrayListFromJSON(row, "conditions_option", "\\|");
                String optionName = row.getString("option_name");
                ArrayList<String> conditionProgressAdded = arrayListFromJSON(row, "condition_progress_added", "\\|");
                ArrayList<String> conditionProgressRemoved = arrayListFromJSON(row, "condition_progress_removed", "\\|");

                ArrayList<TerraformingRequirements> requirementsAdded = new ArrayList<>();
                for (String requirementAddedString : requirementsAddedStrings) {
                    TerraformingRequirements req = terraformingRequirements.get(requirementAddedString);
                    if (req != null) {
                        requirementsAdded.add(terraformingRequirements.get(requirementAddedString));
                    }
                }

                proj.overrideAddTooltip(tooltipOverride, tooltipAddition);
                proj.addRemoveProjectRequirements(requirementsAdded, requirementsRemovedStrings);
                proj.overridePlanetTypeChange(planetTypeChangeOverride);
                proj.addRemoveConditionsAddedRemoved(conditionsAdded, conditionsRemoved);
//                proj.addRemoveConditionProgress(conditionProgressAdded, conditionProgressRemoved);
            }
        } catch (JSONException e) {
            log.error(e);
        }
    }

    private static ArrayList<TerraformingProject> initialiseCraftingProjects() {
        ArrayList<TerraformingProject> ret = new ArrayList<>();

        TerraformingRequirements colonyHasAtLeast100kInhabitants = new TerraformingRequirements(BoggledProjectRequirements.colonyHasAtLeast100kInhabitantsRequirementId, BoggledProjectRequirements.colonyHasAtLeast100kInhabitants, false, new ArrayList<TerraformingRequirement>(asList(
                new MarketIsAtLeastSize("market_is_at_least_size_5", false, 5)
        )));

        TerraformingRequirements colonyHasOrbitalWorksWPristineNanoforge = new TerraformingRequirements(BoggledProjectRequirements.colonyHasOrbitalWorksWPristineNanoforgeRequirementId, BoggledProjectRequirements.colonyHasOrbitalWorksWPristineNanoforge, false, new ArrayList<TerraformingRequirement>(asList(
                new MarketHasIndustryWithItem("market_has_orbital_works_with_pristine_nanoforge",false, Industries.ORBITALWORKS, Items.PRISTINE_NANOFORGE)
        )));

        int domainArtifactCostMedium = boggledTools.getIntSetting(BoggledSettings.domainTechCraftingArtifactCost);
        int domainArtifactCostHard = domainArtifactCostMedium * 2;
        int domainArtifactCostEasy = domainArtifactCostMedium / 2;
        TerraformingRequirements fleetCargoContainsAtLeastDomainArtifactsEasy = new TerraformingRequirements(BoggledProjectRequirements.fleetCargoContainsAtLeastEasyDomainArtifactsRequirementId, "Fleet cargo contains at least " + domainArtifactCostEasy + " Domain-era artifacts", false, new ArrayList<TerraformingRequirement>(asList(
                new FleetCargoContainsAtLeast("fleet_cargo_contains_at_least_easy_domain_artifacts",false, BoggledCommodities.domainArtifacts, domainArtifactCostEasy)
        )));

        TerraformingRequirements fleetCargoContainsAtLeastDomainArtifactsMedium = new TerraformingRequirements(BoggledProjectRequirements.fleetCargoContainsAtLeastMediumDomainArtifactsRequirementId, "Fleet cargo contains at least " + domainArtifactCostMedium + " Domain-era artifacts", false, new ArrayList<TerraformingRequirement>(asList(
                new FleetCargoContainsAtLeast("fleet_cargo_contains_at_least_medium_domain_artifacts", false, BoggledCommodities.domainArtifacts, domainArtifactCostMedium)
        )));

        TerraformingRequirements fleetCargoContainsAtLeastDomainArtifactsHard = new TerraformingRequirements(BoggledProjectRequirements.fleetCargoContainsAtLeastHardDomainArtifactsRequirementId, "Fleet cargo contains at least " + domainArtifactCostHard + " Domain-era artifacts", false, new ArrayList<TerraformingRequirement>(asList(
                new FleetCargoContainsAtLeast("fleet_cargo_contains_at_least_hard_domain_artifacts", false, BoggledCommodities.domainArtifacts, domainArtifactCostHard)
        )));

        int storyPointCost = boggledTools.getIntSetting(BoggledSettings.domainTechCraftingStoryPointCost);
        TerraformingRequirements playerHasStoryPointsAtLeast = new TerraformingRequirements(BoggledProjectRequirements.playerHasStoryPointsRequirementId, storyPointCost + " story points available to spend", false, new ArrayList<TerraformingRequirement>(asList(
                new PlayerHasStoryPointsAtLeast("player_has_at_least_cost_story_points", false, storyPointCost)
        )));

        ArrayList<TerraformingRequirements> craftingProjectReqsEasy = new ArrayList<>(asList(
                colonyHasAtLeast100kInhabitants,
                colonyHasOrbitalWorksWPristineNanoforge,
                fleetCargoContainsAtLeastDomainArtifactsEasy
        ));

        ArrayList<TerraformingRequirements> craftingProjectReqsMedium = new ArrayList<>(asList(
                colonyHasAtLeast100kInhabitants,
                colonyHasOrbitalWorksWPristineNanoforge,
                fleetCargoContainsAtLeastDomainArtifactsMedium
        ));

        ArrayList<TerraformingRequirements> craftingProjectReqsHard = new ArrayList<>(asList(
                colonyHasAtLeast100kInhabitants,
                colonyHasOrbitalWorksWPristineNanoforge,
                fleetCargoContainsAtLeastDomainArtifactsHard
        ));

        if (storyPointCost > 0) {
            craftingProjectReqsEasy.add(playerHasStoryPointsAtLeast);
            craftingProjectReqsMedium.add(playerHasStoryPointsAtLeast);
            craftingProjectReqsHard.add(playerHasStoryPointsAtLeast);
        }

        ArrayList<String> emptyList = new ArrayList<>();
        ArrayList<TerraformingDurationModifier> emptyList2 = new ArrayList<>();

        ret.add(new TerraformingProject(craftCorruptedNanoforgeProjectId, craftCorruptedNanoforgeProjectTooltip, craftingProjectReqsEasy, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftPristineNanoforgeProjectId, craftPristineNanoforgeProjectTooltip, craftingProjectReqsHard, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftSynchrotronProjectId, craftSynchrotronProjectTooltip, craftingProjectReqsMedium, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftHypershuntTapProjectId, craftHypershuntTapProjectTooltip, craftingProjectReqsHard, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftCryoarithmeticEngineProjectId, craftCryoarithmeticEngineProjectTooltip, craftingProjectReqsMedium, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftPlanetKillerDeviceProjectId, craftPlanetKillerDeviceProjectTooltip, craftingProjectReqsHard, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftFusionLampProjectId, craftFusionLampProjectTooltip, craftingProjectReqsHard, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftFullereneSpoolProjectId, craftFullereneSpoolProjectTooltip, craftingProjectReqsMedium, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftPlasmaDynamoProjectId, craftPlasmaDynamoProjectTooltip, craftingProjectReqsMedium, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftAutonomousMantleBoreProjectId, craftAutonomousMantleBoreProjectTooltip, craftingProjectReqsMedium, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftSoilNanitesProjectId, craftSoilNanitesProjectTooltip, craftingProjectReqsMedium, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftCatalyticCoreProjectId, craftCatalyticCoreProjectTooltip, craftingProjectReqsMedium, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftCombatDroneReplicatorProjectId, craftCombatDroneReplicatorProjectTooltip, craftingProjectReqsEasy, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftBiofactoryEmbryoProjectId, craftBiofactoryEmbryoProjectTooltip, craftingProjectReqsMedium, "", emptyList, emptyList, emptyList, 0, emptyList2));

        ret.add(new TerraformingProject(craftDealmakerHolosuiteProjectId, craftDealmakerHolosuiteProjectTooltip, craftingProjectReqsEasy, "", emptyList, emptyList, emptyList, 0, emptyList2));

        return ret;
    }

    public static TerraformingProject getProject(String projectId) {
        return terraformingProjects.get(projectId);
    }

    public static TerraformingProject getCraftingProject(String projectId) {
        for (TerraformingProject project : craftingProjects) {
            if (project.projectId.equals(projectId)) {
                return project;
            }
        }
        return null;
    }

    private static HashMap<String, TerraformingRequirement> terraformingRequirement;
    private static HashMap<String, TerraformingRequirements> terraformingRequirements;
    private static HashMap<String, TerraformingProject> terraformingProjects;

    private static HashMap<String, ArrayList<String>> resourceProgressions;
    private static HashMap<Pair<String, String>, String> resourceLimits;

    private static HashMap<String, PlanetType> planetTypesMap;
    private static LinkedHashMap<String, String> planetConditionsMap;

    private static ArrayList<TerraformingProject> craftingProjects = initialiseCraftingProjects();

    public static HashMap<String, TerraformingProject> getTerraformingProjects() { return terraformingProjects; }

    private static void reinitialiseInfo() {
        craftingProjects = initialiseCraftingProjects();
    }

    public static float getDistanceBetweenPoints(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    public static float getDistanceBetweenTokens(SectorEntityToken tokenA, SectorEntityToken tokenB) {
        return getDistanceBetweenPoints(tokenA.getLocation().x, tokenA.getLocation().y, tokenB.getLocation().x, tokenB.getLocation().y);
    }

    public static float getAngle(float focusX, float focusY, float playerX, float playerY) {
        float angle = (float) Math.toDegrees(Math.atan2(focusY - playerY, focusX - playerX));

        //Not entirely sure what math is going on behind the scenes but this works to get the station to spawn next to the player
        angle = angle + 180f;

        return angle;
    }

    public static float getAngleFromPlayerFleet(SectorEntityToken target) {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        return getAngle(target.getLocation().x, target.getLocation().y, playerFleet.getLocation().x, playerFleet.getLocation().y);
    }

    public static float getAngleFromEntity(SectorEntityToken entity, SectorEntityToken target) {
        return getAngle(target.getLocation().x, target.getLocation().y, entity.getLocation().x, entity.getLocation().y);
    }

    public static void surveyAll(MarketAPI market) {
        for (MarketConditionAPI condition : market.getConditions()) {
            condition.setSurveyed(true);
        }
    }

    public static void refreshSupplyAndDemand(MarketAPI market) {
        //Refreshes supply and demand for each industry on the market
        List<Industry> industries = market.getIndustries();
        for (Industry industry : industries) {
            industry.doPreSaveCleanup();
            industry.doPostSaveRestore();
        }
    }

    public static float getRandomOrbitalAngleFloat(float min, float max) {
        Random rand = new Random();
        return rand.nextFloat() * (max - min) + min;
    }

    public static boolean gateInSystem(StarSystemAPI system)
    {
        for (SectorEntityToken entity : system.getAllEntities()) {
            if (entity.hasTag(Tags.GATE)) {
                return true;
            }
        }

        return false;
    }

    public static boolean playerMarketInSystem(SectorEntityToken playerFleet) {
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity.getMarket() != null && entity.getMarket().isPlayerOwned()) {
                return true;
            }
        }

        return false;
    }

    public static Integer getSizeOfLargestPlayerMarketInSystem(StarSystemAPI system)
    {
        // Returns zero if there are no player markets in the system.
        // Counts markets where the player purchased governorship.

        int largestMarketSize = 0;
        for (MarketAPI market : Misc.getPlayerMarkets(true)) {
            if (market.getStarSystem().equals(system) && market.getSize() > largestMarketSize) {
                largestMarketSize = market.getSize();
            }
        }

        return largestMarketSize;
    }

    public static Integer getPlayerMarketSizeRequirementToBuildGate()
    {
        return boggledTools.getIntSetting(BoggledSettings.marketSizeRequiredToBuildInactiveGate);
    }

    public static SectorEntityToken getClosestPlayerMarketToken(SectorEntityToken playerFleet) {
        if (!playerMarketInSystem(playerFleet)) {
            return null;
        } else {
            ArrayList<SectorEntityToken> allPlayerMarketsInSystem = new ArrayList<>();

            for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
                if (entity.getMarket() != null && entity.getMarket().isPlayerOwned()) {
                    allPlayerMarketsInSystem.add(entity);
                }
            }

            SectorEntityToken closestMarket = null;
            for (SectorEntityToken entity : allPlayerMarketsInSystem) {
                if (closestMarket == null) {
                    closestMarket = entity;
                } else if (getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestMarket, playerFleet)) {
                    closestMarket = entity;
                }
            }

            return closestMarket;
        }
    }

    public static boolean gasGiantInSystem(SectorEntityToken playerFleet) {
        for (SectorEntityToken planet : playerFleet.getStarSystem().getAllEntities()) {
            if (planet instanceof PlanetAPI && ((PlanetAPI) planet).isGasGiant()) {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestGasGiantToken(SectorEntityToken playerFleet) {
        if (!gasGiantInSystem(playerFleet)) {
            return null;
        } else {
            ArrayList<SectorEntityToken> allGasGiantsInSystem = new ArrayList<>();

            for (SectorEntityToken planet : playerFleet.getStarSystem().getAllEntities()) {
                if (planet instanceof PlanetAPI && ((PlanetAPI) planet).isGasGiant()) {
                    allGasGiantsInSystem.add(planet);
                }
            }

            SectorEntityToken closestGasGiant = null;
            for (SectorEntityToken entity : allGasGiantsInSystem) {
                if (closestGasGiant == null) {
                    closestGasGiant = entity;
                } else if (getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestGasGiant, playerFleet)) {
                    closestGasGiant = entity;
                }
            }

            return closestGasGiant;
        }
    }

    public static boolean colonizableStationInSystem(SectorEntityToken playerFleet) {
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity.hasTag(Tags.STATION) && entity.getMarket() != null && entity.getMarket().hasCondition(Conditions.ABANDONED_STATION)) {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestColonizableStationInSystem(SectorEntityToken playerFleet) {
        if (!colonizableStationInSystem(playerFleet)) {
            return null;
        } else {
            ArrayList<SectorEntityToken> allColonizableStationsInSystem = new ArrayList<>();

            for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
                if (entity.hasTag(Tags.STATION) && entity.getMarket() != null && entity.getMarket().hasCondition(Conditions.ABANDONED_STATION)) {
                    allColonizableStationsInSystem.add(entity);
                }
            }

            SectorEntityToken closestStation = null;
            for (SectorEntityToken entity : allColonizableStationsInSystem) {
                if (closestStation == null) {
                    closestStation = entity;
                } else if (getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestStation, playerFleet)) {
                    closestStation = entity;
                }
            }

            return closestStation;
        }
    }

    public static boolean stationInSystem(SectorEntityToken playerFleet) {
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity.hasTag(Tags.STATION)) {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestStationInSystem(SectorEntityToken playerFleet) {
        if (!stationInSystem(playerFleet)) {
            return null;
        } else {
            ArrayList<SectorEntityToken> allStationsInSystem = new ArrayList<>();

            for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
                if (entity.hasTag(Tags.STATION)) {
                    allStationsInSystem.add(entity);
                }
            }

            SectorEntityToken closestStation = null;
            for (SectorEntityToken entity : allStationsInSystem) {
                if (closestStation == null) {
                    closestStation = entity;
                } else if (getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestStation, playerFleet)) {
                    closestStation = entity;
                }
            }

            return closestStation;
        }
    }

    public static ArrayList<String> getListOfFactionsWithMarketInSystem(StarSystemAPI system) {
        ArrayList<String> factionsWithMarketInSystem = new ArrayList<>();

        for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
            if (!factionsWithMarketInSystem.contains(market.getFactionId())) {
                factionsWithMarketInSystem.add(market.getFactionId());
            }
        }

        return factionsWithMarketInSystem;
    }

    public static ArrayList<Integer> getCompanionListOfTotalMarketPopulation(StarSystemAPI system, ArrayList<String> factions) {
        ArrayList<Integer> totalFactionMarketSize = new ArrayList<>();
        int buffer = 0;

        for (String faction : factions) {
            for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
                if (market.getFactionId().equals(faction)) {
                    buffer = buffer + market.getSize();
                }
            }

            totalFactionMarketSize.add(buffer);
            buffer = 0;
        }

        return totalFactionMarketSize;
    }

    public static boolean planetInSystem(SectorEntityToken playerFleet) {
        for (SectorEntityToken planet : playerFleet.getStarSystem().getAllEntities()) {
            if (planet instanceof PlanetAPI && !getPlanetType(((PlanetAPI) planet)).equals(starPlanetId)) {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestPlanetToken(SectorEntityToken playerFleet) {
        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition()) {
            return null;
        }

        if (!planetInSystem(playerFleet)) {
            return null;
        } else {
            ArrayList<SectorEntityToken> allPlanetsInSystem = new ArrayList<>();

            for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
                if (entity instanceof PlanetAPI && !getPlanetType(((PlanetAPI) entity)).equals(starPlanetId)) {
                    allPlanetsInSystem.add(entity);
                }
            }

            SectorEntityToken closestPlanet = null;
            for (SectorEntityToken entity : allPlanetsInSystem) {
                if (closestPlanet == null) {
                    closestPlanet = entity;
                } else if (getDistanceBetweenTokens(entity, playerFleet) < getDistanceBetweenTokens(closestPlanet, playerFleet)) {
                    closestPlanet = entity;
                }
            }

            return closestPlanet;
        }
    }

    public static MarketAPI getClosestMarketToEntity(SectorEntityToken entity)
    {
        if(entity == null || entity.getStarSystem() == null || entity.isInHyperspace())
        {
            return null;
        }

        List<MarketAPI> markets = Global.getSector().getEconomy().getMarkets(entity.getStarSystem());
        MarketAPI closestMarket = null;
        for(MarketAPI market : markets)
        {
            if(closestMarket == null || getDistanceBetweenTokens(entity, market.getPrimaryEntity()) < getDistanceBetweenTokens(entity, closestMarket.getPrimaryEntity()))
            {
                if(!market.getFactionId().equals(Factions.NEUTRAL))
                {
                    closestMarket = market;
                }
            }
        }

        return closestMarket;
    }

    public static PlanetType getPlanetType(PlanetAPI planet) {
        // Sets the spec planet type, but not the actual planet type. Need the API fix from Alex to correct this.
        // All code should rely on this function to get the planet type so it should work without bugs.
        // String planetType = planet.getTypeId();

        if(planet == null || planet.getSpec() == null || planet.getSpec().getPlanetType() == null)
        {
            return planetTypesMap.get(unknownPlanetId); // Guaranteed to be there
        }

//        if (planet.getMarket() != null) {
//            MarketAPI market = planet.getMarket();
//            for (String condition : planetConditionsMap.keySet()) {
//                if (market.hasCondition(condition)) {
//                    return planetConditionsMap.get(condition);
//                }
//            }
//        }

        PlanetType planetType = planetTypesMap.get(planet.getTypeId());
        if (planetType != null) {
            return planetType;
        }
//        Global.getLogger(boggledTools.class).info("Planet " + planet.getName() + " typeID " + planet.getTypeId() + " has unknown planet type");
        return planetTypesMap.get(unknownPlanetId); // Guaranteed to be there
    }

    public static ArrayList<MarketAPI> getNonStationMarketsPlayerControls()
    {
        ArrayList<MarketAPI> allPlayerMarkets = (ArrayList<MarketAPI>) Misc.getPlayerMarkets(true);
        ArrayList<MarketAPI> allNonStationPlayerMarkets = new ArrayList<>();
        for(MarketAPI market : allPlayerMarkets)
        {
            if(!boggledTools.marketIsStation(market))
            {
                if(!market.hasCondition(BoggledConditions.terraformingControllerConditionId))
                {
                    boggledTools.addCondition(market, BoggledConditions.terraformingControllerConditionId);
                }
                allNonStationPlayerMarkets.add(market);
            }
        }

        return allNonStationPlayerMarkets;
    }

    public static boolean marketIsStation(MarketAPI market) {
        return market.getPrimaryEntity() == null || market.getPlanetEntity() == null || market.getPrimaryEntity().hasTag(Tags.STATION);
    }

    public static boolean terraformingPossibleOnMarket(MarketAPI market) {
        if (marketIsStation(market)) {
            return false;
        }

        if (market.hasCondition(Conditions.IRRADIATED)) {
            return false;
        }

        return boggledTools.getPlanetType(market.getPlanetEntity()).getTerraformingPossible();
    }

    public static boolean getCreateMirrorsOrShades(MarketAPI market) {
        // Return true for mirrors, false for shades
        // Go by temperature first. If not triggered, will check planet type. Otherwise, just return true.
        if (market.hasCondition(Conditions.POOR_LIGHT) || market.hasCondition(Conditions.VERY_COLD) || market.hasCondition(Conditions.COLD)) {
            return true;
        } else if (market.hasCondition(Conditions.VERY_HOT) || market.hasCondition(Conditions.HOT)) {
            return false;
        }

        if (boggledTools.getPlanetType(market.getPlanetEntity()).getPlanetId().equals(desertPlanetId) || boggledTools.getPlanetType(market.getPlanetEntity()).getPlanetId().equals(junglePlanetId)) {
            return false;
        } else if (boggledTools.getPlanetType(market.getPlanetEntity()).getPlanetId().equals(tundraPlanetId) || boggledTools.getPlanetType(market.getPlanetEntity()).getPlanetId().equals(frozenPlanetId)) {
            return true;
        }

        return true;
    }

    public static SectorEntityToken getFocusOfAsteroidBelt(SectorEntityToken playerFleet)
    {
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity instanceof CampaignTerrainAPI) {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI) entity;
                CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

                if ((terrainPlugin instanceof AsteroidBeltTerrainPlugin && !(terrainPlugin instanceof AsteroidFieldTerrainPlugin)) && terrainPlugin.containsEntity(playerFleet)) {
                    return entity.getOrbitFocus();
                }
            }
        }

        return null;
    }

    public static OrbitAPI getAsteroidFieldOrbit(SectorEntityToken playerFleet)
    {
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity instanceof CampaignTerrainAPI) {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI) entity;
                CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

                if (terrainPlugin instanceof AsteroidFieldTerrainPlugin && terrainPlugin.containsEntity(playerFleet)) {
                    AsteroidFieldTerrainPlugin asteroidPlugin = (AsteroidFieldTerrainPlugin) terrain.getPlugin();
                    return asteroidPlugin.getEntity().getOrbit();
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    public static SectorEntityToken getAsteroidFieldEntity(SectorEntityToken playerFleet)
    {
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity instanceof CampaignTerrainAPI) {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI) entity;
                CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

                if (terrainPlugin instanceof AsteroidFieldTerrainPlugin && terrainPlugin.containsEntity(playerFleet)) {
                    return terrain;
                }
            }
        }

        // Should never return null because this method can't be called unless playerFleetInAsteroidField returned true
        return null;
    }

    public static boolean playerFleetInAsteroidBelt(SectorEntityToken playerFleet)
    {
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity instanceof CampaignTerrainAPI) {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI) entity;
                CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

                if ((terrainPlugin instanceof AsteroidBeltTerrainPlugin && !(terrainPlugin instanceof AsteroidFieldTerrainPlugin)) && terrainPlugin.containsEntity(playerFleet)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean playerFleetInAsteroidField(SectorEntityToken playerFleet)
    {
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity instanceof CampaignTerrainAPI) {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI) entity;
                CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

                if (terrainPlugin instanceof AsteroidFieldTerrainPlugin && terrainPlugin.containsEntity(playerFleet)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean playerFleetTooCloseToJumpPoint(SectorEntityToken playerFleet) {
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity instanceof JumpPointAPI && getDistanceBetweenTokens(playerFleet, entity) < 300f) {
                return true;
            }
        }

        return false;
    }

    public static Integer getNumAsteroidTerrainsInSystem(SectorEntityToken playerFleet)
    {
        Integer numRoids = 0;
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity instanceof CampaignTerrainAPI) {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI) entity;
                CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

                if (terrainPlugin instanceof AsteroidBeltTerrainPlugin) {
                    numRoids++;
                }

                /*
                // For testing purposes only
                if(terrainId.equals("asteroid_belt"))
                {
                    AsteroidBeltTerrainPlugin belt = (AsteroidBeltTerrainPlugin) terrain.getPlugin();
                    CampaignUIAPI ui = Global.getSector().getCampaignUI();
                    ui.addMessage("Radius: " + belt.getRingParams().middleRadius, Color.YELLOW);
                }
                */
            }
        }

        return numRoids;
    }

    public static Integer getNumAsteroidBeltsInSystem(SectorEntityToken playerFleet)
    {
        Integer numBelts = 0;
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity instanceof CampaignTerrainAPI) {
                CampaignTerrainAPI terrain = (CampaignTerrainAPI) entity;
                CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

                if (terrainPlugin instanceof AsteroidBeltTerrainPlugin && !(terrainPlugin instanceof AsteroidFieldTerrainPlugin)) {
                    numBelts++;
                }
            }
        }

        return numBelts;
    }

    public static String getMiningStationResourceString(Integer numAsteroidTerrains) {
        if (numAsteroidTerrains >= boggledTools.getIntSetting(BoggledSettings.miningStationUltrarichOre)) {
            return "ultrarich";
        }
        if (numAsteroidTerrains >= boggledTools.getIntSetting(BoggledSettings.miningStationRichOre)) {
            return "rich";
        }
        if (numAsteroidTerrains >= boggledTools.getIntSetting(BoggledSettings.miningStationAbundantOre)) {
            return "abundant";
        }
        if (numAsteroidTerrains >= boggledTools.getIntSetting(BoggledSettings.miningStationModerateOre)) {
            return "moderate";
        } else if (numAsteroidTerrains >= boggledTools.getIntSetting(BoggledSettings.miningStationSparseOre)) {
            return "sparse";
        } else {
            return "abundant";
        }
    }

    public static int getNumberOfStationExpansions(MarketAPI market) {
        for (String tag : market.getTags()) {
            if (tag.contains(BoggledTags.stationConstructionNumExpansions)) {
                return Integer.parseInt(tag.substring(tag.length() - 1));
            }
        }

        return 0;
    }

    public static void incrementNumberOfStationExpansions(MarketAPI market) {
        if (getNumberOfStationExpansions(market) == 0) {
            market.addTag(BoggledTags.stationConstructionNumExpansionsOne);
        } else {
            int numExpansionsOld = getNumberOfStationExpansions(market);
            market.removeTag(BoggledTags.stationConstructionNumExpansions + numExpansionsOld);
            market.addTag(BoggledTags.stationConstructionNumExpansions + (numExpansionsOld + 1));
        }
    }

    public static boolean systemHasJumpPoint(StarSystemAPI system)
    {
        return !system.getJumpPoints().isEmpty();
    }

    public static float randomOrbitalAngleFloat()
    {
        Random rand = new Random();
        return rand.nextFloat() * (360f);
    }

    public static void refreshAquacultureAndFarming(MarketAPI market)
    {
        if(market == null || market.getPrimaryEntity() == null || market.getPlanetEntity() == null || market.hasTag(Tags.STATION) || market.getPrimaryEntity().hasTag(Tags.STATION))
        {
            return;
        }
        else
        {
            if(market.hasIndustry(Industries.FARMING) && market.hasCondition(Conditions.WATER_SURFACE))
            {
                market.getIndustry(Industries.FARMING).init(Industries.AQUACULTURE, market);
            }
            else if(market.hasIndustry(Industries.AQUACULTURE) && !market.hasCondition(Conditions.WATER_SURFACE))
            {
                market.getIndustry(Industries.AQUACULTURE).init(Industries.FARMING, market);
            }
        }
    }

    public static boolean playerTooClose(StarSystemAPI system)
    {
        return Global.getSector().getPlayerFleet().isInOrNearSystem(system);
    }

    public static void clearConnectedPlanets(MarketAPI market)
    {
        SectorEntityToken targetEntityToRemove = null;
        for (SectorEntityToken entity : market.getConnectedEntities()) {
            if (entity instanceof PlanetAPI && !entity.hasTag(Tags.STATION)) {
                targetEntityToRemove = entity;
            }
        }

        if(targetEntityToRemove != null)
        {
            market.getConnectedEntities().remove(targetEntityToRemove);
            clearConnectedPlanets(market);
        }
    }

    public static void clearConnectedStations(MarketAPI market)
    {
        SectorEntityToken targetEntityToRemove = null;
        for (SectorEntityToken entity : market.getConnectedEntities()) {
            if (entity.hasTag(Tags.STATION)) {
                targetEntityToRemove = entity;
            }
        }

        if(targetEntityToRemove != null)
        {
            market.getConnectedEntities().remove(targetEntityToRemove);
            clearConnectedStations(market);
        }
    }

    public static int numReflectorsInOrbit(MarketAPI market)
    {
        int numReflectors = 0;

        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains(Entities.STELLAR_MIRROR) || entity.getId().contains(Entities.STELLAR_SHADE) || entity.hasTag(Entities.STELLAR_MIRROR) || entity.hasTag(Entities.STELLAR_SHADE))) {
                numReflectors++;
            }
        }

        return numReflectors;
    }

    public static int numMirrorsInOrbit(MarketAPI market)
    {
        int numMirrors = 0;

        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains(Entities.STELLAR_MIRROR) || entity.hasTag(Entities.STELLAR_MIRROR))) {
                numMirrors++;
            }
        }

        return numMirrors;
    }

    public static int numShadesInOrbit(MarketAPI market)
    {
        int numShades = 0;

        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains(Entities.STELLAR_SHADE) || entity.hasTag(Entities.STELLAR_SHADE))) {
                numShades++;
            }
        }

        return numShades;
    }

    public static void clearReflectorsInOrbit(MarketAPI market)
    {
        Iterator<SectorEntityToken> allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();
        while(allEntitiesInSystem.hasNext())
        {
            SectorEntityToken entity = allEntitiesInSystem.next();
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains(Entities.STELLAR_MIRROR) || entity.getId().contains(Entities.STELLAR_SHADE) || entity.hasTag(Entities.STELLAR_MIRROR) || entity.hasTag(Entities.STELLAR_SHADE)))
            {
                allEntitiesInSystem.remove();
                market.getStarSystem().removeEntity(entity);
            }
        }
    }

    public static boolean hasIsmaraSling(MarketAPI market)
    {
        for (MarketAPI marketElement : Global.getSector().getEconomy().getMarkets(market.getStarSystem())) {
            if (marketElement.getFactionId().equals(market.getFactionId()) && marketElement.hasIndustry(BoggledIndustries.ismaraSlingIndustryId) && marketElement.getIndustry(BoggledIndustries.ismaraSlingIndustryId).isFunctional()) {
                return true;
            }
        }

        return false;
    }

    public static void swapStationSprite(SectorEntityToken station, String stationType, String stationGreekLetter, int targetSize)
    {
        MarketAPI market = station.getMarket();
        StarSystemAPI system = market.getStarSystem();
        OrbitAPI orbit = null;
        if(station.getOrbit() != null)
        {
            orbit = station.getOrbit();
        }
        CampaignClockAPI clock = Global.getSector().getClock();
        SectorEntityToken newStation;
        SectorEntityToken newStationLights = null;

        String size = "null";
        if(targetSize == 1)
        {
            size = "small";
        }
        else if(targetSize == 2)
        {
            size = "medium";
        }
        else if(targetSize == 3)
        {
            size = "large";
        }

        if(size.equals("null"))
        {
            //Do nothing if an erroneous size value was passed.
            return;
        }

        switch (stationType) {
            case "astropolis":
                newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + stationGreekLetter + "_" + size, market.getFactionId());
                newStationLights = system.addCustomEntity("boggled_station_lights_overlay_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName() + " Lights Overlay", "boggled_" + stationType + "_station_" + stationGreekLetter + "_" + size + "_lights_overlay", market.getFactionId());
                break;
            case "mining":
                newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + size, market.getFactionId());
                //We can't tell which lights overlay to delete earlier because there could be multiple mining stations in a single system.
                //Therefore we delete them all earlier, then recreate them all later.
                break;
            case "siphon":
                newStation = system.addCustomEntity("boggled_station_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName(), "boggled_" + stationType + "_station_" + size, market.getFactionId());
                newStationLights = system.addCustomEntity("boggled_station_lights_overlay_swapped_" + clock.getCycle() + "_" + clock.getMonth() + "_" + clock.getDay(), station.getName() + " Lights Overlay", "boggled_" + stationType + "_station_" + size + "_lights_overlay", market.getFactionId());
                break;
            default:
                //Do nothing because the station type is unrecognized
                return;
        }

        if(newStation == null)
        {
            //Failed to create a new station likely because of erroneous passed values. Do nothing.
            return;
        }

        newStation.setContainingLocation(station.getContainingLocation());
        if(newStationLights != null)
        {
            newStationLights.setContainingLocation(station.getContainingLocation());
        }

        if(orbit != null)
        {
            newStation.setOrbit(orbit);
            if(newStationLights != null)
            {
                newStationLights.setOrbit(newStation.getOrbit().makeCopy());
            }
        }
        newStation.setMemory(station.getMemory());
        newStation.setFaction(market.getFactionId());
        station.setCircularOrbit(newStation, 0, 0, 1);

        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station)) {
                if (entity.getOrbit().getClass().equals(CircularFleetOrbit.class)) {
                    ((CircularFleetOrbit) entity.getOrbit()).setFocus(newStation);
                }

                if (entity.getOrbit().getClass().equals(CircularOrbit.class)) {
                    ((CircularOrbit) entity.getOrbit()).setFocus(newStation);
                }

                if (entity.getOrbit().getClass().equals(CircularOrbitPointDown.class)) {
                    ((CircularOrbitPointDown) entity.getOrbit()).setFocus(newStation);
                }

                if (entity.getOrbit().getClass().equals(CircularOrbitWithSpin.class)) {
                    ((CircularOrbitWithSpin) entity.getOrbit()).setFocus(newStation);
                }
            }
        }

        // Handle Illustrated Entities custom images and/or description.
        // TASC uses classes from the Illustrated Entities to do this - the Illustrated Entities JAR is imported as a library into TASC.
        if(Global.getSettings().getModManager().isModEnabled(BoggledMods.illustratedEntitiesModId))
        {
            boolean customImageHasBeenSet = ImageHandler.hasImage(station);
            if(customImageHasBeenSet)
            {
                int customImageId = ImageHandler.getImageId(station);
                ImageHandler.removeImageFrom(station);
                ImageHandler.setImage(newStation, ImageDataMemory.getInstance().get(customImageId), false);
            }

            TextDataEntry textDataEntry = TextHandler.getDataForEntity(station);
            if(textDataEntry != null)
            {
                boggledTools.setEntityIllustratedEntitiesCustomDescription(newStation, textDataEntry);
            }
        }

        //Deletes the old station. May cause limited issues related to ships orbiting the old location
        clearConnectedStations(market);
        system.removeEntity(station);

        newStation.setMarket(market);
        market.setPrimaryEntity(newStation);

        surveyAll(market);
        refreshSupplyAndDemand(market);
    }

    public static void setEntityIllustratedEntitiesCustomDescription(SectorEntityToken sectorEntityToken, TextDataEntry textDataEntry)
    {
        // The passed SectorEntityToken will have the description lines from the passed TextDataEntry copied onto its own TextDataEntry.

        TextDataMemory dataMemory = TextDataMemory.getInstance();

        int i = dataMemory.getNexFreetNum();
        TextDataEntry newTextDataEntry = new TextDataEntry(i, sectorEntityToken.getId());

        for (int textNum = 1; textNum <= 2; textNum++)
        {
            for (int lineNum = 1; lineNum <= Settings.LINE_AMT; lineNum++)
            {
                String s = textDataEntry.getString(textNum, lineNum);
                newTextDataEntry.setString(textNum, lineNum, s);
            }
        }

        newTextDataEntry.apply();
        dataMemory.set(newTextDataEntry.descriptionNum, newTextDataEntry);
    }

    public static void deleteOldLightsOverlay(SectorEntityToken station, String stationType, String stationGreekLetter)
    {
        StarSystemAPI system = station.getStarSystem();
        OrbitAPI orbit = null;
        if(station.getOrbit() != null)
        {
            orbit = station.getOrbit();
        }

        SectorEntityToken targetTokenToDelete = null;
        switch (stationType) {
            case "astropolis": {
                String smallTag = null;
                String mediumTag = null;
                String largeTag = null;
                switch (stationGreekLetter) {
                    case "alpha": {
                        smallTag = BoggledTags.lightsOverlayAstropolisAlphaSmall;
                        mediumTag = BoggledTags.lightsOverlayAstropolisAlphaMedium;
                        largeTag = BoggledTags.lightsOverlayAstropolisAlphaLarge;

                        break;
                    }
                    case "beta": {
                        smallTag = BoggledTags.lightsOverlayAstropolisBetaSmall;
                        mediumTag = BoggledTags.lightsOverlayAstropolisBetaMedium;
                        largeTag = BoggledTags.lightsOverlayAstropolisBetaLarge;
                        break;
                    }
                    case "gamma": {
                        smallTag = BoggledTags.lightsOverlayAstropolisGammaSmall;
                        mediumTag = BoggledTags.lightsOverlayAstropolisGammaMedium;
                        largeTag = BoggledTags.lightsOverlayAstropolisGammaLarge;
                        break;
                    }
                }

                if (smallTag != null) {
                    for (SectorEntityToken entity : system.getAllEntities()) {
                        if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && entity.getCircularOrbitAngle() == station.getCircularOrbitAngle() && (entity.hasTag(smallTag) || entity.hasTag(mediumTag) || entity.hasTag(largeTag))) {
                            targetTokenToDelete = entity;
                            break;
                        }
                    }

                }
                break;
            }
            case "mining": {
                for (SectorEntityToken entity : system.getAllEntities()) {
                    if (entity.hasTag(BoggledTags.lightsOverlayMiningSmall) || entity.hasTag(BoggledTags.lightsOverlayMiningMedium)) {
                        targetTokenToDelete = entity;
                        break;
                    }
                }
                break;
            }
            case "siphon": {
                for (SectorEntityToken entity : system.getAllEntities()) {
                    if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && (entity.hasTag(BoggledTags.lightsOverlaySiphonSmall) || entity.hasTag(BoggledTags.lightsOverlaySiphonMedium))) {
                        targetTokenToDelete = entity;
                        break;
                    }
                }
                break;
            }
            default:
                //Do nothing because the station type is unrecognized
                return;
        }

        if (targetTokenToDelete != null) {
            system.removeEntity(targetTokenToDelete);
            deleteOldLightsOverlay(station, stationType, stationGreekLetter);
        }
    }

    public static void reapplyMiningStationLights(StarSystemAPI system)
    {
        SectorEntityToken stationToApplyOverlayTo = null;
        int stationsize = 0;

        for (SectorEntityToken entity : system.getAllEntities()) {
            if (entity.hasTag(BoggledTags.miningStationSmall) && !entity.hasTag(BoggledTags.alreadyReappliedLightsOverlay)) {
                stationToApplyOverlayTo = entity;
                stationsize = 1;
                entity.addTag(BoggledTags.alreadyReappliedLightsOverlay);
                break;
            } else if (entity.hasTag(BoggledTags.miningStationMedium) && !entity.hasTag(BoggledTags.alreadyReappliedLightsOverlay)) {
                stationToApplyOverlayTo = entity;
                stationsize = 2;
                entity.addTag(BoggledTags.alreadyReappliedLightsOverlay);
                break;
            }
        }

        if(stationToApplyOverlayTo != null)
        {
            if(stationsize == 1)
            {
                if(!stationToApplyOverlayTo.getMarket().getFactionId().equals(Factions.NEUTRAL))
                {
                    SectorEntityToken newMiningStationLights = system.addCustomEntity("boggled_miningStationLights", "Mining Station Lights Overlay", "boggled_mining_station_small_lights_overlay", stationToApplyOverlayTo.getFaction().getId());
                    newMiningStationLights.setOrbit(stationToApplyOverlayTo.getOrbit().makeCopy());
                }
                reapplyMiningStationLights(system);
            }
            else if(stationsize == 2)
            {
                if(!stationToApplyOverlayTo.getMarket().getFactionId().equals(Factions.NEUTRAL))
                {
                    SectorEntityToken newMiningStationLights = system.addCustomEntity("boggled_miningStationLights", "Mining Station Lights Overlay", "boggled_mining_station_medium_lights_overlay", stationToApplyOverlayTo.getFaction().getId());
                    newMiningStationLights.setOrbit(stationToApplyOverlayTo.getOrbit().makeCopy());
                }
                reapplyMiningStationLights(system);
            }
        }
        else
        {
            for (SectorEntityToken entity : system.getAllEntities()) {
                if (entity.hasTag(BoggledTags.alreadyReappliedLightsOverlay)) {
                    entity.removeTag(BoggledTags.alreadyReappliedLightsOverlay);
                }
            }
        }
    }

    public static boolean marketHasOrbitalStation(MarketAPI market)
    {
        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && entity.hasTag(Tags.STATION)) {
                return true;
            }
        }

        return false;
    }

    public static void incrementOreForPlanetCracking(MarketAPI market)
    {
        if(market.hasCondition(Conditions.ORE_SPARSE))
        {
            boggledTools.removeCondition(market, Conditions.ORE_SPARSE);
            boggledTools.addCondition(market, Conditions.ORE_MODERATE);
        }
        else if(market.hasCondition(Conditions.ORE_MODERATE))
        {
            boggledTools.removeCondition(market, Conditions.ORE_MODERATE);
            boggledTools.addCondition(market, Conditions.ORE_ABUNDANT);
        }
        else if(market.hasCondition(Conditions.ORE_ABUNDANT))
        {
            boggledTools.removeCondition(market, Conditions.ORE_ABUNDANT);
            boggledTools.addCondition(market, Conditions.ORE_RICH);
        }
        else if(market.hasCondition(Conditions.ORE_RICH))
        {
            boggledTools.removeCondition(market, Conditions.ORE_RICH);
            boggledTools.addCondition(market, Conditions.ORE_ULTRARICH);
        }
        else if(market.hasCondition(Conditions.ORE_ULTRARICH))
        {
            //Do Nothing
        }
        else
        {
            boggledTools.addCondition(market, Conditions.ORE_SPARSE);
        }

        if(market.hasCondition(Conditions.RARE_ORE_SPARSE))
        {
            boggledTools.removeCondition(market, Conditions.RARE_ORE_SPARSE);
            boggledTools.addCondition(market, Conditions.RARE_ORE_MODERATE);
        }
        else if(market.hasCondition(Conditions.RARE_ORE_MODERATE))
        {
            boggledTools.removeCondition(market, Conditions.RARE_ORE_MODERATE);
            boggledTools.addCondition(market, Conditions.RARE_ORE_ABUNDANT);
        }
        else if(market.hasCondition(Conditions.RARE_ORE_ABUNDANT))
        {
            boggledTools.removeCondition(market, Conditions.RARE_ORE_ABUNDANT);
            boggledTools.addCondition(market, Conditions.RARE_ORE_RICH);
        }
        else if(market.hasCondition(Conditions.RARE_ORE_RICH))
        {
            boggledTools.removeCondition(market, Conditions.RARE_ORE_RICH);
            boggledTools.addCondition(market, Conditions.RARE_ORE_ULTRARICH);
        }
        else if(market.hasCondition(Conditions.RARE_ORE_ULTRARICH))
        {
            //Do Nothing
        }
        else
        {
            boggledTools.addCondition(market, Conditions.RARE_ORE_SPARSE);
        }

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);
        boggledTools.refreshAquacultureAndFarming(market);
    }

    public static void incrementVolatilesForOuyangOptimization(MarketAPI market)
    {
        if(market.hasCondition(Conditions.VOLATILES_TRACE))
        {
            boggledTools.removeCondition(market, Conditions.VOLATILES_TRACE);
            boggledTools.addCondition(market, Conditions.VOLATILES_ABUNDANT);
        }
        else if(market.hasCondition(Conditions.VOLATILES_DIFFUSE))
        {
            boggledTools.removeCondition(market, Conditions.VOLATILES_DIFFUSE);
            boggledTools.addCondition(market, Conditions.VOLATILES_PLENTIFUL);
        }
        else if(market.hasCondition(Conditions.VOLATILES_ABUNDANT))
        {
            boggledTools.removeCondition(market, Conditions.VOLATILES_ABUNDANT);
            boggledTools.addCondition(market, Conditions.VOLATILES_PLENTIFUL);
        }
        else if(market.hasCondition(Conditions.VOLATILES_PLENTIFUL))
        {
            //Do nothing
        }
        else
        {
            boggledTools.addCondition(market, Conditions.VOLATILES_DIFFUSE);
        }

        SectorEntityToken closestGasGiantToken = market.getPrimaryEntity();
        if(closestGasGiantToken != null)
        {
            for (SectorEntityToken entity : closestGasGiantToken.getStarSystem().getAllEntities()) {
                if (entity.hasTag(Tags.STATION) && entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(closestGasGiantToken) && (entity.getCustomEntitySpec().getDefaultName().equals("Side Station") || entity.getCustomEntitySpec().getDefaultName().equals("Siphon Station")) && !entity.getId().equals("beholder_station")) {
                    if (entity.getMarket() != null) {
                        market = entity.getMarket();
                        if (market.hasCondition(Conditions.VOLATILES_TRACE)) {
                            boggledTools.removeCondition(market, Conditions.VOLATILES_TRACE);
                            boggledTools.addCondition(market, Conditions.VOLATILES_ABUNDANT);
                        } else if (market.hasCondition(Conditions.VOLATILES_DIFFUSE)) {
                            boggledTools.removeCondition(market, Conditions.VOLATILES_DIFFUSE);
                            boggledTools.addCondition(market, Conditions.VOLATILES_PLENTIFUL);
                        } else if (market.hasCondition(Conditions.VOLATILES_ABUNDANT)) {
                            boggledTools.removeCondition(market, Conditions.VOLATILES_ABUNDANT);
                            boggledTools.addCondition(market, Conditions.VOLATILES_PLENTIFUL);
                        }

                        boggledTools.surveyAll(market);
                        boggledTools.refreshSupplyAndDemand(market);
                        boggledTools.refreshAquacultureAndFarming(market);
                    }
                }
            }
        }
    }

    public static void addCondition(MarketAPI market, String condition)
    {
        if(!market.hasCondition(condition))
        {
            market.addCondition(condition);
            boggledTools.surveyAll(market);
            boggledTools.refreshSupplyAndDemand(market);
            boggledTools.refreshAquacultureAndFarming(market);
        }
    }

    public static void removeCondition(MarketAPI market, String condition)
    {
        if(market != null && market.hasCondition(condition))
        {
            market.removeCondition(condition);
            boggledTools.surveyAll(market);
            boggledTools.refreshSupplyAndDemand(market);
            boggledTools.refreshAquacultureAndFarming(market);
        }
    }

    public static void changePlanetType(PlanetAPI planet, String newType)
    {
        PlanetSpecAPI planetSpec = planet.getSpec();
        for(PlanetSpecAPI targetSpec : Global.getSettings().getAllPlanetSpecs())
        {
            if (targetSpec.getPlanetType().equals(newType))
            {
                planetSpec.setAtmosphereColor(targetSpec.getAtmosphereColor());
                planetSpec.setAtmosphereThickness(targetSpec.getAtmosphereThickness());
                planetSpec.setAtmosphereThicknessMin(targetSpec.getAtmosphereThicknessMin());
                planetSpec.setCloudColor(targetSpec.getCloudColor());
                planetSpec.setCloudRotation(targetSpec.getCloudRotation());
                planetSpec.setCloudTexture(targetSpec.getCloudTexture());
                planetSpec.setGlowColor(targetSpec.getGlowColor());
                planetSpec.setGlowTexture(targetSpec.getGlowTexture());

                planetSpec.setIconColor(targetSpec.getIconColor());
                planetSpec.setPlanetColor(targetSpec.getPlanetColor());
                planetSpec.setStarscapeIcon(targetSpec.getStarscapeIcon());
                planetSpec.setTexture(targetSpec.getTexture());
                planetSpec.setUseReverseLightForGlow(targetSpec.isUseReverseLightForGlow());
                ((PlanetSpec) planetSpec).planetType = newType;
                ((PlanetSpec) planetSpec).name = targetSpec.getName();
                ((PlanetSpec) planetSpec).descriptionId = ((PlanetSpec) targetSpec).descriptionId;
                break;
            }
        }

        planet.applySpecChanges();
    }

    public static void applyPlanetKiller(MarketAPI market)
    {
        if(Misc.isStoryCritical(market) && !boggledTools.getBooleanSetting(BoggledSettings.planetKillerAllowDestructionOfColoniesMarkedAsEssentialForQuests))
        {
            // Should never be reached because deployment will be disabled.
            return;
        }
        else if(marketIsStation(market))
        {
            adjustRelationshipsDueToPlanetKillerUsage(market);
            triggerMilitaryResponseToPlanetKillerUsage(market);
            decivilizeMarketWithPlanetKiller(market);
        }
        else if(market.getPlanetEntity() != null && market.getPlanetEntity().getSpec() != null)
        {
            changePlanetTypeWithPlanetKiller(market);
            changePlanetConditionsWithPlanetKiller(market);

            adjustRelationshipsDueToPlanetKillerUsage(market);
            triggerMilitaryResponseToPlanetKillerUsage(market);
            decivilizeMarketWithPlanetKiller(market);
        }
    }

    public static void changePlanetTypeWithPlanetKiller(MarketAPI market)
    {
        String planetType = getPlanetType(market.getPlanetEntity()).getPlanetId();
        if(!planetType.equals(starPlanetId) && !planetType.equals(gasGiantPlanetId) && !planetType.equals(volcanicPlanetId) && !planetType.equals(unknownPlanetId))
        {
            changePlanetType(market.getPlanetEntity(), Conditions.IRRADIATED);
            market.addCondition(Conditions.IRRADIATED);
        }
    }

    public static void changePlanetConditionsWithPlanetKiller(MarketAPI market)
    {
        // Modded conditions

        // Vanilla Conditions
        removeCondition(market, Conditions.HABITABLE);
        removeCondition(market, Conditions.MILD_CLIMATE);
        removeCondition(market, Conditions.WATER_SURFACE);
        removeCondition(market, Conditions.VOLTURNIAN_LOBSTER_PENS);

        removeCondition(market, Conditions.INIMICAL_BIOSPHERE);

        removeCondition(market, Conditions.FARMLAND_POOR);
        removeCondition(market, Conditions.FARMLAND_ADEQUATE);
        removeCondition(market, Conditions.FARMLAND_RICH);
        removeCondition(market, Conditions.FARMLAND_BOUNTIFUL);

        String planetType = getPlanetType(market.getPlanetEntity()).getPlanetId();
        if(!planetType.equals(gasGiantPlanetId) && !planetType.equals(unknownPlanetId))
        {
            removeCondition(market, Conditions.ORGANICS_TRACE);
            removeCondition(market, Conditions.ORGANICS_COMMON);
            removeCondition(market, Conditions.ORGANICS_ABUNDANT);
            removeCondition(market, Conditions.ORGANICS_PLENTIFUL);

            removeCondition(market, Conditions.VOLATILES_TRACE);
            removeCondition(market, Conditions.VOLATILES_DIFFUSE);
            removeCondition(market, Conditions.VOLATILES_ABUNDANT);
            removeCondition(market, Conditions.VOLATILES_PLENTIFUL);
        }
    }

    public static List<FactionAPI> factionsToMakeHostileDueToPlanetKillerUsage(MarketAPI market)
    {
        List<FactionAPI> factionsToMakeHostile = new ArrayList<>();
        for(FactionAPI faction : Global.getSector().getAllFactions())
        {
            String factionId = faction.getId();
            if(factionId.equals(Factions.LUDDIC_PATH) && market.getFactionId().equals(Factions.LUDDIC_PATH))
            {
                factionsToMakeHostile.add(faction);
            }

            if(!factionId.equals(Factions.PLAYER) && !factionId.equals(Factions.DERELICT) && !factionId.equals(Factions.LUDDIC_PATH) && !factionId.equals(Factions.OMEGA) && !factionId.equals(Factions.REMNANTS) && !factionId.equals(Factions.SLEEPER))
            {
                factionsToMakeHostile.add(faction);
            }
        }

        return factionsToMakeHostile;
    }

    public static void adjustRelationshipsDueToPlanetKillerUsage(MarketAPI market)
    {
        for(FactionAPI faction : factionsToMakeHostileDueToPlanetKillerUsage(market))
        {
            faction.setRelationship(Factions.PLAYER, -100f);
        }
    }

    public static void decivilizeMarketWithPlanetKiller(MarketAPI market)
    {
        int atrocities = (int) Global.getSector().getCharacterData().getMemoryWithoutUpdate().getFloat(MemFlags.PLAYER_ATROCITIES);
        atrocities++;
        Global.getSector().getCharacterData().getMemoryWithoutUpdate().set(MemFlags.PLAYER_ATROCITIES, atrocities);

        // Added per Histidine's comments in the forum - see Page 148, comment #2210 in the TASC thread.
        // If you're reading this because it's not working properly for what you're trying to do, let me know!
        //ListenerUtil.reportSaturationBombardmentFinished(null, market, null);
        MarketCMD.TempData actionData = new MarketCMD.TempData();
        actionData.bombardType = MarketCMD.BombardType.SATURATION;	/* probably not needed but just in case someone forgot which listener method they were using */
        actionData.willBecomeHostile = factionsToMakeHostileDueToPlanetKillerUsage(market);	/* Fill this with FactionAPI that will get mad */
        ListenerUtil.reportSaturationBombardmentFinished(null, market, actionData);
        
        DecivTracker.decivilize(market, true);
        MarketCMD.addBombardVisual(market.getPrimaryEntity());
        MarketCMD.addBombardVisual(market.getPrimaryEntity());
        MarketCMD.addBombardVisual(market.getPrimaryEntity());

        // Copied from MarketCMD saturation bombing code.
        InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        if (dialog != null && dialog.getPlugin() instanceof RuleBasedDialog)
        {
            if (dialog.getInteractionTarget() != null && dialog.getInteractionTarget().getMarket() != null)
            {
                Global.getSector().setPaused(false);
                dialog.getInteractionTarget().getMarket().getMemoryWithoutUpdate().advance(0.0001f);
                Global.getSector().setPaused(true);
            }

            ((RuleBasedDialog) dialog.getPlugin()).updateMemory();
        }

        if (dialog != null && dialog.getPlugin() instanceof RuleBasedDialog)
        {
                ((RuleBasedDialog) dialog.getPlugin()).updateMemory();
        }
    }

    public static void triggerMilitaryResponseToPlanetKillerUsage(MarketAPI market)
    {
        // Copied from MarketCMD addMilitaryResponse()

        if (market == null) return;

        if (!market.getFaction().getCustomBoolean(Factions.CUSTOM_NO_WAR_SIM))
        {
            MilitaryResponseScript.MilitaryResponseParams params = new MilitaryResponseScript.MilitaryResponseParams(CampaignFleetAIAPI.ActionType.HOSTILE,
                    "player_ground_raid_" + market.getId(),
                    market.getFaction(),
                    market.getPrimaryEntity(),
                    0.75f,
                    30f);
            market.getContainingLocation().addScript(new MilitaryResponseScript(params));
        }

        List<CampaignFleetAPI> fleets = market.getContainingLocation().getFleets();
        for (CampaignFleetAPI other : fleets)
        {
            if (other.getFaction() == market.getFaction())
            {
                MemoryAPI mem = other.getMemoryWithoutUpdate();
                Misc.setFlagWithReason(mem, MemFlags.MEMORY_KEY_MAKE_HOSTILE_WHILE_TOFF, "raidAlarm", true, 1f);
            }
        }
    }

    public static class TerraformingProject {
        private final String projectId;
        private String projectTooltip;
        // Multiple separate TerraformingRequirements form an AND'd collection
        // Each individual requirement inside the TerraformingRequirements forms an OR'd collection
        // ie If any of the conditions inside a TerraformingRequirements is fulfilled, that entire requirement is filled
        // But then all the TerraformingRequirements must be fulfilled for the project to be allowed
        private final ArrayList<TerraformingRequirements> projectRequirements;

        private String planetTypeChange;

        private final ArrayList<String> conditionsAdded;
        private final ArrayList<String> conditionsRemoved;
        private final ArrayList<String> resourcesToProgress;

        private final int baseProjectDuration;
        private final ArrayList<TerraformingDurationModifier> durationModifiers;

        public String getProjectId() { return projectId; }
        public String getProjectTooltip() { return projectTooltip; }
        public ArrayList<TerraformingRequirements> getProjectRequirements() { return projectRequirements; }
        public int getModifiedProjectDuration(MarketAPI market) {
            float projectDuration = baseProjectDuration;
            for (TerraformingDurationModifier durationModifier : durationModifiers) {
                projectDuration += durationModifier.getDurationModifier(market, baseProjectDuration);
            }
            return Math.max((int) projectDuration, 0);
        }
        public String getPlanetTypeChange() { return planetTypeChange; }
        public ArrayList<String> getConditionsAdded() { return conditionsAdded; }

        public boolean requirementsMet(MarketAPI market) {
            Logger log = Global.getLogger(boggledTools.class);
            log.info("Checking project requirements for project " + getProjectId() + " for market " + market.getName());

            if (projectRequirements == null) {
                log.error("Terraforming project requirements is null for project " + getProjectId() + " and market " + market.getName());
                return false;
            }

            for (TerraformingRequirements requirements : projectRequirements) {
                if (!requirements.checkRequirement(market)) {
                    return false;
                }
            }
            return true;
        }

        public void finishProject(MarketAPI market) {
            addRemoveConditions(market);
            incrementResource(market);
            terraformPlanetType(market);

            surveyAll(market);
            refreshSupplyAndDemand(market);
            refreshAquacultureAndFarming(market);

            if (market.isPlayerOwned())
            {
                MessageIntel intel = new MessageIntel(projectTooltip + " on " + market.getName(), Misc.getBasePlayerColor());
                intel.addLine("    - Completed");
                intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
            }
        }

        private void addRemoveConditions(MarketAPI market) {
            for (String conditionRemoved : conditionsRemoved) {
                Global.getLogger(TerraformingProject.class).info("Removing condition " + conditionRemoved + " from planet " + market.getName());
                boggledTools.removeCondition(market, conditionRemoved);
            }
            for (String conditionAdded : conditionsAdded) {
                Global.getLogger(TerraformingProject.class).info("Adding condition " + conditionAdded + " from planet " + market.getName());
                boggledTools.addCondition(market, conditionAdded);
            }
        }

        private void terraformPlanetType(MarketAPI market) {
            if (planetTypeChange.isEmpty()) {
                return;
            }
            Global.getLogger(TerraformingProject.class).info("Terraforming planet type to " + planetTypeChange);

            market.getPlanetEntity().changeType(planetTypeChange, null);
        }

        private void incrementResource(MarketAPI market) {
            if (resourcesToProgress.isEmpty()) {
                return;
            }
            Logger log = Global.getLogger(TerraformingProject.class);

            for (String resourceType : resourcesToProgress) {
                ArrayList<String> resourcesProgression = boggledTools.resourceProgressions.get(resourceType);
                if (resourcesProgression == null || resourcesProgression.isEmpty()) {
                    log.error("Project " + projectId + " has invalid condition to progress " + resourceType);
                    continue;
                }

                boolean marketHadCondition = false;
                for (int i = 0; i < resourcesProgression.size() - 1; ++i) {
                    String condition = resourcesProgression.get(i);
                    if (!market.hasCondition(condition)) {
                        continue;
                    }
                    boggledTools.removeCondition(market, condition);
                    boggledTools.addCondition(market, resourcesProgression.get(i + 1));
                    marketHadCondition = true;
                    break;
                }

                if (!marketHadCondition) {
                    boggledTools.addCondition(market, resourcesProgression.get(0));
                }
            }
        }

        public void overrideAddTooltip(String tooltipOverride, String tooltipAddition) {
            if (!tooltipOverride.isEmpty()) {
                projectTooltip = tooltipOverride;
            }
            projectTooltip += tooltipAddition;
        }
        public void addRemoveProjectRequirements(ArrayList<TerraformingRequirements> add, String[] remove) {
            Logger log = Global.getLogger(TerraformingProject.class);
            for (String r : remove) {
                for (int i = 0; i < projectRequirements.size(); ++i) {
                    TerraformingRequirements projectReqs = projectRequirements.get(i);
                    if (r.equals(projectReqs.requirementId)) {
                        log.info("Project " + projectId + " removing project requirement " + r);
                        projectRequirements.remove(i);
                        break;
                    }
                }
            }

            projectRequirements.addAll(add);
        }

        public void overridePlanetTypeChange(String planetTypeChange) {
            this.planetTypeChange = planetTypeChange;
        }

        public void addRemoveConditionsAddedRemoved(ArrayList<String> conditionsAdded, ArrayList<String> conditionsRemoved) {

        }

        TerraformingProject(String projectId, String projectTooltip, ArrayList<TerraformingRequirements> projectRequirements, String planetTypeChange, ArrayList<String> conditionsAdded, ArrayList<String> conditionsRemoved, ArrayList<String> conditionsToProgress, int baseProjectDuration, ArrayList<TerraformingDurationModifier> durationModifiers) {
            this.projectId = projectId;
            this.projectTooltip = projectTooltip;
            this.projectRequirements = projectRequirements;

            this.planetTypeChange = planetTypeChange;
            this.conditionsAdded = conditionsAdded;
            this.conditionsRemoved = conditionsRemoved;
            this.resourcesToProgress = conditionsToProgress;

            this.baseProjectDuration = baseProjectDuration;
            this.durationModifiers = durationModifiers;
        }
    }

    public static class TerraformingRequirements {
        private final String requirementId;
        private final String requirementTooltip;
        private final boolean invertAll;
        private final ArrayList<TerraformingRequirement> terraformingRequirements;

        public void addRemoveProjectRequirement(ArrayList<TerraformingRequirement> add, String[] remove) {
            Logger log = Global.getLogger(TerraformingRequirements.class);
            for (String r : remove) {
                for (int i = 0; i < terraformingRequirements.size(); ++i) {
                    TerraformingRequirement terraformingReq = terraformingRequirements.get(i);
                    if (r.equals(terraformingReq.requirementId)) {
                        log.info("Terraforming requirements " + requirementId + " removing requirement " + r);
                        terraformingRequirements.remove(i);
                        break;
                    }
                }
            }

            terraformingRequirements.addAll(add);
        }

        TerraformingRequirements(String requirementId, String requirementTooltip, boolean invertAll, ArrayList<TerraformingRequirement> terraformingRequirements) {
            this.requirementId = requirementId;
            this.requirementTooltip = requirementTooltip;
            this.invertAll = invertAll;
            this.terraformingRequirements = terraformingRequirements;
        }

        public final String getTooltip() { return requirementTooltip; }

        public final boolean checkRequirement(MarketAPI market) {
            boolean requirementsMet = false;
            for (TerraformingRequirement terraformingRequirement : terraformingRequirements) {
                requirementsMet = requirementsMet || terraformingRequirement.checkRequirement(market);
            }
            if (invertAll) {
                requirementsMet = !requirementsMet;
            }
            return requirementsMet;
        }
    }

    public interface TerraformingDurationModifierFactory {
        TerraformingDurationModifier constructFromJSON(String data);
    }

    public static class PlanetSizeDurationModifierFactory implements TerraformingDurationModifierFactory {
        @Override
        public TerraformingDurationModifier constructFromJSON(String data) {
            return new PlanetSizeDurationModifier();
        }
    }

    public interface TerraformingDurationModifier {
        float getDurationModifier(MarketAPI market, int baseDuration);
    }

    public static class PlanetSizeDurationModifier implements TerraformingDurationModifier {
        @Override
        public float getDurationModifier(MarketAPI market, int baseDuration) {
            return market.getPlanetEntity().getRadius();
        }
    }

    public interface TerraformingRequirementFactory {
        TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data);
    }

    public static class PlanetTypeRequirementFactory implements TerraformingRequirementFactory {
        @Override
        public TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            return new PlanetTypeRequirement(requirementId, invert, data);
        }
    }

    public static class MarketHasConditionFactory implements TerraformingRequirementFactory {
        @Override
        public TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            return new MarketHasCondition(requirementId, invert, data);
        }
    }

    public static class MarketHasIndustryFactory implements TerraformingRequirementFactory {
        @Override
        public TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            return new MarketHasIndustry(requirementId, invert, data);
        }
    }

    public static class MarketHasIndustryWithItemFactory implements TerraformingRequirementFactory {
        @Override
        public TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            String[] industryAndItem = data.split("\\|");
            assert(industryAndItem.length == 2);
            return new MarketHasIndustryWithItem(requirementId, invert, industryAndItem[0], industryAndItem[1]);
        }
    }

    public static class MarketHasWaterPresentFactory implements TerraformingRequirementFactory {
        @Override
        public TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            String[] waterLevelStrings = data.split("\\|");
            assert(waterLevelStrings.length == 2);
            int[] waterLevels = new int[waterLevelStrings.length];
            for (int i = 0; i < waterLevels.length; ++i) {
                waterLevels[i] = Integer.parseInt(waterLevelStrings[i]);
            }
            return new MarketHasWaterPresent(requirementId, invert, waterLevels[0], waterLevels[1]);
        }
    }

    public static class MarketIsAtLeastSizeFactory implements TerraformingRequirementFactory {
        @Override
        public TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            int colonySize = Integer.parseInt(data);
            return new MarketIsAtLeastSize(requirementId, invert, colonySize);
        }
    }

    public static class FleetCargoContainsAtLeastFactory implements TerraformingRequirementFactory {
        @Override
        public TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            String[] cargoIdAndQuantityStrings = data.split("\\|");
            assert(cargoIdAndQuantityStrings.length == 2);
            int quantity = Integer.parseInt(cargoIdAndQuantityStrings[1]);
            return new FleetCargoContainsAtLeast(requirementId, invert, cargoIdAndQuantityStrings[0], quantity);
        }
    }

    public static class PlayerHasStoryPointsAtLeastFactory implements TerraformingRequirementFactory {
        @Override
        public TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            int quantity = Integer.parseInt(data);
            return new PlayerHasStoryPointsAtLeast(requirementId, invert, quantity);
        }
    }

    public static class WorldTypeSupportsResourceImprovementFactory implements TerraformingRequirementFactory {
        @Override
        public TerraformingRequirement constructFromJSON(String requirementId, boolean invert, String data) {
            return new WorldTypeSupportsResourceImprovement(requirementId, invert, data);
        }
    }

    public abstract static class TerraformingRequirement {
        private final String requirementId;
        private final boolean invert;

        protected TerraformingRequirement(String requirementId, boolean invert) {
            this.requirementId = requirementId;
            this.invert = invert;
        }

        protected abstract boolean checkRequirementImpl(MarketAPI market);

        public final boolean checkRequirement(MarketAPI market) {
            boolean ret = checkRequirementImpl(market);
            if (invert) {
                ret = !ret;
            }
            return ret;
        }
    }

    public static class PlanetTypeRequirement extends TerraformingRequirement {
        String planetTypeId;

        PlanetTypeRequirement(String requirementId, boolean invert, String planetTypeId) {
            super(requirementId, invert);
            this.planetTypeId = planetTypeId;
        }

        @Override
        protected final boolean checkRequirementImpl(MarketAPI market) {
            return planetTypeId.equals(getPlanetType(market.getPlanetEntity()).planetId);
        }
    }

    public static class MarketHasCondition extends TerraformingRequirement {
        String conditionId;
        MarketHasCondition(String requirementId, boolean invert, String conditionId) {
            super(requirementId, invert);
            this.conditionId = conditionId;
        }

        @Override
        protected final boolean checkRequirementImpl(MarketAPI market) {
            return market.hasCondition(conditionId);
        }
    }

    public static class MarketHasIndustry extends TerraformingRequirement {
        String industryId;
        MarketHasIndustry(String requirementId, boolean invert, String industryId) {
            super(requirementId, invert);
            this.industryId = industryId;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            Industry industry = market.getIndustry(industryId);
            return industry != null && industry.isFunctional() && market.hasIndustry(industryId);
        }
    }

    public static class MarketHasIndustryWithItem extends TerraformingRequirement {
        String industryId;
        String itemId;
        MarketHasIndustryWithItem(String requirementId, boolean invert, String industryId, String itemId) {
            super(requirementId, invert);
            this.industryId = industryId;
            this.itemId = itemId;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            Industry industry = market.getIndustry(industryId);
            if (industry == null) {
                return false;
            }
            for (SpecialItemData specialItemData : industry.getVisibleInstalledItems()) {
                if (itemId.equals(specialItemData.getId())) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class MarketHasWaterPresent extends TerraformingRequirement {
        int minWaterLevel;
        int maxWaterLevel;
        MarketHasWaterPresent(String requirementId, boolean invert, int minWaterLevel, int maxWaterLevel) {
            super(requirementId, invert);
            this.minWaterLevel = minWaterLevel;
            this.maxWaterLevel = maxWaterLevel;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            int waterLevel = getPlanetWaterLevel(market);
            return minWaterLevel <= waterLevel && waterLevel <= maxWaterLevel;
        }
    }


    public static class MarketIsAtLeastSize extends TerraformingRequirement {
        int colonySize;
        MarketIsAtLeastSize(String requirementId, boolean invert, int colonySize) {
            super(requirementId, invert);
            this.colonySize = colonySize;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            return market.getSize() >= colonySize;
        }
    }

    public static class FleetCargoContainsAtLeast extends TerraformingRequirement {
        String cargoId;
        int quantity;
        FleetCargoContainsAtLeast(String requirementId, boolean invert, String cargoId, int quantity) {
            super(requirementId, invert);
            this.cargoId = cargoId;
            this.quantity = quantity;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            return Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(cargoId) >= quantity;
        }
    }

    public static class PlayerHasStoryPointsAtLeast extends TerraformingRequirement {
        int quantity;
        PlayerHasStoryPointsAtLeast(String requirementId, boolean invert, int quantity) {
            super(requirementId, invert);
            this.quantity = quantity;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            return Global.getSector().getPlayerStats().getStoryPoints() >= quantity;
        }
    }

    public static class WorldTypeSupportsResourceImprovement extends TerraformingRequirement {
        String resourceId;
        WorldTypeSupportsResourceImprovement(String requirementId, boolean invert, String resourceId) {
            super(requirementId, invert);
            this.resourceId = resourceId;
        }

        @Override
        protected boolean checkRequirementImpl(MarketAPI market) {
            Pair<String, String> key = new Pair<>(getPlanetType(market.getPlanetEntity()).getPlanetId(), resourceId);
            String maxResource = resourceLimits.get(key);

            if (maxResource == null || maxResource.isEmpty()) {
                return false;
            }

            if (market.hasCondition(maxResource)) {
                return false;
            }

            ArrayList<String> resourceProgression = resourceProgressions.get(resourceId);

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

    public static Boolean printProjectRequirementsReportIfStalled(MarketAPI market, String project, TextPanelAPI text)
    {
        Color good = Misc.getPositiveHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(project != null && !project.equals(noneProjectId))
        {
            // Print requirements, and if not met, print terraforming is stalled
            text.addPara("Project Requirements:");

            TerraformingProject terraformingProject = getProject(project);
            if (terraformingProject != null) {
                boolean foundUnmetRequirement = false;
                for (TerraformingRequirements terraformingRequirements : terraformingProject.projectRequirements) {
                    if (terraformingRequirements.checkRequirement(market)) {
                        text.addPara("      - %s", good, terraformingRequirements.requirementTooltip);
                    }
                    else {
                        text.addPara("      - %s", bad, terraformingRequirements.requirementTooltip);
                        foundUnmetRequirement = true;
                    }
                }
                return foundUnmetRequirement;
            }
        }

        return false;
    }

    public static void printProjectResults(MarketAPI market, String project, TextPanelAPI text)
    {
        Color highlight = Misc.getHighlightColor();

        TerraformingProject terraformingProject = getProject(project);

        if (terraformingProject != null) {
            text.addPara("Prospective project: %s", highlight, terraformingProject.getProjectTooltip());
//            for (String result : terraformingProject.projectResults) {
//                text.addPara(result);
//            }
        }
    }

    public static class PlanetType {
        private final String planetId;
        private final boolean terraformingPossible;
        private final int baseWaterLevel;
        private final ArrayList<TerraformingRequirements> conditionalWaterRequirements;
        private final int conditionalWaterLevel;

        public String getPlanetId() { return planetId; }
        public boolean getTerraformingPossible() { return terraformingPossible; }
        public int getWaterLevel(MarketAPI market) {
            if (conditionalWaterRequirements.isEmpty()) {
                return baseWaterLevel;
            }

            for (TerraformingRequirements conditionalWaterRequirement : conditionalWaterRequirements) {
                if (!conditionalWaterRequirement.checkRequirement(market)) {
                    return baseWaterLevel;
                }
            }
            return conditionalWaterLevel;
        }

        public PlanetType(String planetId, boolean terraformingPossible, int baseWaterLevel, ArrayList<TerraformingRequirements> conditionalWaterRequirements, int conditionalWaterLevel) {
            this.planetId = planetId;
            this.terraformingPossible = terraformingPossible;
            this.baseWaterLevel = baseWaterLevel;
            this.conditionalWaterRequirements = conditionalWaterRequirements;
            this.conditionalWaterLevel = conditionalWaterLevel;
        }
    }

    public static int getPlanetWaterLevel(MarketAPI market)
    {
        // There are checks present elsewhere that will prevent passing in a station market.
        // If that happens anyway, it's best to just throw an exception.
        if (hasIsmaraSling(market)) {
            return 2;
        }

        PlanetAPI planet = market.getPlanetEntity();
        PlanetType planetType = getPlanetType(planet);
        return planetType.getWaterLevel(market);
    }

    public static boolean marketHasAtmoProblem(MarketAPI market)
    {
        TerraformingRequirements reqs = terraformingRequirements.get("market_has_atmo_problem");
        if (reqs == null) {
            // Abundance of caution
            return false;
        }
        return reqs.checkRequirement(market);
    }

    public static String getTooltipProjectName(String currentProject)
    {
        if(currentProject == null || currentProject.equals(noneProjectId))
        {
            return noneProjectId;
        }

        reinitialiseInfo();
        TerraformingProject terraformingProject = getProject(currentProject);
        if (terraformingProject != null) {
            return terraformingProject.projectTooltip;
        } else {
            return "ERROR";
        }
    }

    public static MarketAPI createMiningStationMarket(SectorEntityToken stationEntity)
    {
        CampaignClockAPI clock = Global.getSector().getClock();
        StarSystemAPI system = stationEntity.getStarSystem();
        String systemName = system.getName();

        //Create the mining station market
        MarketAPI market = Global.getFactory().createMarket(systemName + clock.getCycle() + clock.getMonth() + clock.getDay() + "MiningStationMarket", stationEntity.getName(), 3);
        market.setSize(3);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.setPrimaryEntity(stationEntity);

        market.setFactionId(Global.getSector().getPlayerFleet().getFaction().getId());
        market.setPlayerOwned(true);

        market.addCondition(Conditions.POPULATION_3);

        if(boggledTools.getBooleanSetting(BoggledSettings.miningStationLinkToResourceBelts))
        {
            int numAsteroidBeltsInSystem = boggledTools.getNumAsteroidTerrainsInSystem(stationEntity);
            String resourceLevel = boggledTools.getMiningStationResourceString(numAsteroidBeltsInSystem);
            market.addCondition("ore_" + resourceLevel);
            market.addCondition("rare_ore_" + resourceLevel);
        }
        else
        {
            String resourceLevel = "moderate";
            int staticAmountPerSettings = boggledTools.getIntSetting(BoggledSettings.miningStationStaticAmount);
            switch(staticAmountPerSettings)
            {
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

        market.addCondition(BoggledConditions.spriteControllerConditionId);
        market.addCondition(BoggledConditions.crampedQuartersConditionId);

        //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
        //market_conditions.csv overwrites the vanilla no_atmosphere condition
        //the only change made is to hide the icon on markets where primary entity has station tag
        //This is done so refining and fuel production can slot the special items
        //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
        market.addCondition(Conditions.NO_ATMOSPHERE);
        market.suppressCondition(Conditions.NO_ATMOSPHERE);

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);
        market.getConstructionQueue().addToEnd(Industries.MINING, 0);

        stationEntity.setMarket(market);

        Global.getSector().getEconomy().addMarket(market, true);


        // If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
        // Still bugged as of 0.95.1a
        Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);
        //Global.getSector().getCampaignUI().getCurrentInteractionDialog().dismiss();

        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        StoragePlugin storage = (StoragePlugin)market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket(Submarkets.LOCAL_RESOURCES);

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);

        Global.getSoundPlayer().playUISound(BoggledSounds.stationConstructed, 1.0F, 1.0F);

        return market;
    }

    public static MarketAPI createSiphonStationMarket(SectorEntityToken stationEntity, SectorEntityToken hostGasGiant)
    {
        CampaignClockAPI clock = Global.getSector().getClock();
        StarSystemAPI system = stationEntity.getStarSystem();
        String systemName = system.getName();

        //Create the siphon station market
        MarketAPI market = Global.getFactory().createMarket(systemName + ":" + hostGasGiant.getName() + "SiphonStationMarket", stationEntity.getName(), 3);
        market.setSize(3);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.setPrimaryEntity(stationEntity);

        market.setFactionId(Global.getSector().getPlayerFleet().getFaction().getId());
        market.setPlayerOwned(true);

        market.addCondition(Conditions.POPULATION_3);

        if(boggledTools.getBooleanSetting(BoggledSettings.siphonStationLinkToGasGiant))
        {
            if(hostGasGiant.getMarket().hasCondition(Conditions.VOLATILES_TRACE))
            {
                market.addCondition(Conditions.VOLATILES_TRACE);
            }
            else if(hostGasGiant.getMarket().hasCondition(Conditions.VOLATILES_DIFFUSE))
            {
                market.addCondition(Conditions.VOLATILES_DIFFUSE);
            }
            else if(hostGasGiant.getMarket().hasCondition(Conditions.VOLATILES_ABUNDANT))
            {
                market.addCondition(Conditions.VOLATILES_ABUNDANT);
            }
            else if(hostGasGiant.getMarket().hasCondition(Conditions.VOLATILES_PLENTIFUL))
            {
                market.addCondition(Conditions.VOLATILES_PLENTIFUL);
            }
            else //Can a gas giant not have any volatiles at all?
            {
                market.addCondition(Conditions.VOLATILES_TRACE);
            }
        }
        else
        {
            String resourceLevel = "diffuse";
            int staticAmountPerSettings = boggledTools.getIntSetting(BoggledSettings.siphonStationStaticAmount);
            switch(staticAmountPerSettings)
            {
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

        market.addCondition(BoggledConditions.spriteControllerConditionId);
        market.addCondition(BoggledConditions.crampedQuartersConditionId);

        //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
        //market_conditions.csv overwrites the vanilla no_atmosphere condition
        //the only change made is to hide the icon on markets where primary entity has station tag
        //This is done so refining and fuel production can slot the special items
        //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
        market.addCondition(Conditions.NO_ATMOSPHERE);
        market.suppressCondition(Conditions.NO_ATMOSPHERE);

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);
        market.getConstructionQueue().addToEnd(Industries.MINING, 0);

        stationEntity.setMarket(market);

        Global.getSector().getEconomy().addMarket(market, true);

        //If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
        Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);
        //Global.getSector().getCampaignUI().getCurrentInteractionDialog().dismiss();

        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        StoragePlugin storage = (StoragePlugin)market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket(Submarkets.LOCAL_RESOURCES);

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);

        Global.getSoundPlayer().playUISound(BoggledSounds.stationConstructed, 1.0F, 1.0F);
        return market;
    }

    public static MarketAPI createAstropolisStationMarket(SectorEntityToken stationEntity, SectorEntityToken hostPlanet)
    {
        CampaignClockAPI clock = Global.getSector().getClock();

        //Create the astropolis market
        MarketAPI market = Global.getFactory().createMarket(hostPlanet.getName() + "astropolisMarket" + clock.getCycle() + clock.getMonth() + clock.getDay(), stationEntity.getName(), 3);
        market.setSize(3);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.setPrimaryEntity(stationEntity);

        market.setFactionId(Global.getSector().getPlayerFaction().getId());
        market.setPlayerOwned(true);

        market.addCondition(Conditions.POPULATION_3);

        market.addCondition(BoggledConditions.spriteControllerConditionId);
        market.addCondition(BoggledConditions.crampedQuartersConditionId);

        //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
        //market_conditions.csv overwrites the vanilla no_atmosphere condition
        //the only change made is to hide the icon on markets where primary entity has station tag
        //This is done so refining and fuel production can slot the special items
        //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
        market.addCondition(Conditions.NO_ATMOSPHERE);
        market.suppressCondition(Conditions.NO_ATMOSPHERE);

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);

        stationEntity.setMarket(market);

        Global.getSector().getEconomy().addMarket(market, true);

        Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);

        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        StoragePlugin storage = (StoragePlugin)market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket(Submarkets.LOCAL_RESOURCES);

        Global.getSoundPlayer().playUISound(BoggledSounds.stationConstructed, 1.0F, 1.0F);
        return market;
    }

    public static int getLastDayCheckedForConstruction(SectorEntityToken stationEntity)
    {
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.constructionProgressLastDayChecked)) {
                return Integer.parseInt(tag.replaceAll(BoggledTags.constructionProgressLastDayChecked, ""));
            }
        }

        return 0;
    }

    public static void clearClockCheckTagsForConstruction(SectorEntityToken stationEntity)
    {
        String tagToDelete = null;
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.constructionProgressLastDayChecked)) {
                tagToDelete = tag;
                break;
            }
        }

        if(tagToDelete != null)
        {
            stationEntity.removeTag(tagToDelete);
            clearClockCheckTagsForConstruction(stationEntity);
        }
    }

    public static void clearBoggledTerraformingControllerTags(MarketAPI market)
    {
        String tagToDelete = null;
        for (String tag : market.getTags()) {
            if (tag.contains(BoggledTags.terraformingController)) {
                tagToDelete = tag;
                break;
            }
        }

        if(tagToDelete != null)
        {
            market.removeTag(tagToDelete);
            clearBoggledTerraformingControllerTags(market);
        }
    }

    public static int getConstructionProgressDays(SectorEntityToken stationEntity)
    {
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.constructionProgressDays)) {
                return Integer.parseInt(tag.replaceAll(BoggledTags.constructionProgressDays, ""));
            }
        }

        return 0;
    }

    public static void clearProgressCheckTagsForConstruction(SectorEntityToken stationEntity)
    {
        String tagToDelete = null;
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.constructionProgressDays)) {
                tagToDelete = tag;
                break;
            }
        }

        if(tagToDelete != null)
        {
            stationEntity.removeTag(tagToDelete);
            clearProgressCheckTagsForConstruction(stationEntity);
        }
    }

    public static void incrementConstructionProgressDays(SectorEntityToken stationEntity, int amount)
    {
        int currentDays = getConstructionProgressDays(stationEntity);

        clearProgressCheckTagsForConstruction(stationEntity);

        currentDays = currentDays + amount;

        String strDays = currentDays + "";

        while(strDays.length() < 6)
        {
            strDays = "0" + strDays;
        }

        stationEntity.addTag(BoggledTags.constructionProgressDays + strDays);
    }

    public static int[] getQuantitiesForStableLocationConstruction(String type)
    {
        ArrayList<Integer> ret = new ArrayList<>();

        if (type.equals(Entities.INACTIVE_GATE)) {
            ret.addAll(asList(
                    boggledTools.getIntSetting(BoggledSettings.stableLocationGateCostHeavyMachinery),
                    boggledTools.getIntSetting(BoggledSettings.stableLocationGateCostMetals),
                    boggledTools.getIntSetting(BoggledSettings.stableLocationGateCostTransplutonics)
            ));
            if (boggledTools.getBooleanSetting(BoggledSettings.domainArchaeologyEnabled)) {
                ret.add(boggledTools.getIntSetting(BoggledSettings.stableLocationGateCostDomainEraArtifacts));
            }
        } else {
            ret.addAll(asList(
                    boggledTools.getIntSetting(BoggledSettings.stableLocationDomainTechStructureCostHeavyMachinery),
                    boggledTools.getIntSetting(BoggledSettings.stableLocationDomainTechStructureCostMetals),
                    boggledTools.getIntSetting(BoggledSettings.stableLocationDomainTechStructureCostTransplutonics)
            ));
            if (boggledTools.getBooleanSetting(BoggledSettings.domainArchaeologyEnabled)) {
                ret.add(boggledTools.getIntSetting(BoggledSettings.stableLocationDomainTechStructureCostDomainEraArtifacts));
            }
        }
        int[] ret2 = new int[ret.size()];
        Iterator<Integer> it = ret.iterator();
        for (int i = 0; i < ret.size(); i++) ret2[i] = it.next();
        return ret2;
    }

    public static SectorEntityToken getPlanetTokenForQuest(String systemId, String entityId)
    {
        StarSystemAPI system = Global.getSector().getStarSystem(systemId);
        if(system != null)
        {
            SectorEntityToken possibleTarget = system.getEntityById(entityId);
            if(possibleTarget != null)
            {
                if(possibleTarget instanceof PlanetAPI)
                {
                    return possibleTarget;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    public static int getIntSetting(String key)
    {
        if(Global.getSettings().getModManager().isModEnabled(BoggledMods.lunalibModId))
        {
            return LunaSettings.getInt(BoggledMods.tascModId, key);
        }
        else
        {
            return Global.getSettings().getInt(key);
        }
    }

    public static boolean getBooleanSetting(String key)
    {
        if(Global.getSettings().getModManager().isModEnabled(BoggledMods.lunalibModId))
        {
            return LunaSettings.getBoolean(BoggledMods.tascModId, key);
        }
        else
        {
            return Global.getSettings().getBoolean(key);
        }
    }

    public static boolean isResearched(String key)
    {
        // Pass this.getId() as key if this function is called from an industry

        if(Global.getSettings().getModManager().isModEnabled("aod_core"))
        {
            Map<String,Boolean> researchSaved = (HashMap<String, Boolean>) Global.getSector().getPersistentData().get("researchsaved");
            return researchSaved != null ?  researchSaved.get(key) : false;
        }
        else
        {
            // TASC does not have built-in research functionality.
            // Always return true if the player is not using a mod that implements research.
            return true;
        }
    }

    public static void writeMessageToLog(String message)
    {
        Global.getLogger(boggledTools.class).info(message);
    }

    public static void sendDebugIntelMessage(String message)
    {
        MessageIntel intel = new MessageIntel(message, Misc.getBasePlayerColor());
        intel.addLine(message);
        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, null);
    }

    public static void terraformDebug(MarketAPI market)
    {
        market.getPlanetEntity().changeType(boggledTools.waterPlanetId, null);
        sendDebugIntelMessage(market.getPlanetEntity().getTypeId());
        sendDebugIntelMessage(market.getPlanetEntity().getSpec().getPlanetType());
    }
}