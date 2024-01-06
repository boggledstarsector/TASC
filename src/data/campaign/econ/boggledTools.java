package data.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.campaign.listeners.ListenerUtil;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.MilitaryResponseScript;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CircularFleetOrbit;
import com.fs.starfarer.campaign.CircularOrbit;
import com.fs.starfarer.campaign.CircularOrbitPointDown;
import com.fs.starfarer.campaign.CircularOrbitWithSpin;
import com.fs.starfarer.loading.specs.PlanetSpec;
import data.campaign.econ.industries.BoggledCommonIndustry;
import data.scripts.*;
import illustratedEntities.helper.ImageHandler;
import illustratedEntities.helper.Settings;
import illustratedEntities.helper.TextHandler;
import illustratedEntities.memory.ImageDataMemory;
import illustratedEntities.memory.TextDataEntry;
import illustratedEntities.memory.TextDataMemory;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.List;
import java.util.ArrayList;

import static java.util.Arrays.asList;

public class boggledTools {
    public static void CheckSubmarketExists(String submarketId) {
        for (SubmarketSpecAPI submarketSpec : Global.getSettings().getAllSubmarketSpecs()) {
            if (submarketSpec.getId().equals(submarketId)) {
                return;
            }
        }
        throw new IllegalArgumentException("Market condition ID '" + submarketId + "' doesn't exist");
    }

    public static void CheckMarketConditionExists(String conditionId) {
        for (MarketConditionSpecAPI marketConditionSpec : Global.getSettings().getAllMarketConditionSpecs()) {
            if (marketConditionSpec.getId().equals(conditionId)) {
                return;
            }
        }
        throw new IllegalArgumentException("Condition ID '" + conditionId + "' doesn't exist");
    }

    public static void CheckCommodityExists(String commodityId) {
        for (CommoditySpecAPI commoditySpec : Global.getSettings().getAllCommoditySpecs()) {
            if (commoditySpec.getId().equals(commodityId)) {
                return;
            }
        }
        throw new IllegalArgumentException("Commodity ID '" + commodityId + "' doesn't exist");
    }

    public static void CheckResourceExists(String resourceId) {
        for (Map.Entry<String, ArrayList<String>> resourceProgression : getResourceProgressions().entrySet()) {
            if (resourceProgression.getKey().equals(resourceId)) {
                return;
            }
        }
        throw new IllegalArgumentException("Resource ID '" + resourceId + "' doesn't exist");
    }

    public static void CheckPlanetTypeExists(String planetType) {
        for (PlanetSpecAPI planetSpec : Global.getSettings().getAllPlanetSpecs()) {
            if (planetSpec.getPlanetType().equals(planetType)) {
                return;
            }
        }
        throw new IllegalArgumentException("Planet type '" + planetType + "' doesn't exist");
    }

    public static void CheckIndustryExists(String industryId) {
        for (IndustrySpecAPI industrySpec : Global.getSettings().getAllIndustrySpecs()) {
            if (industrySpec.getId().equals(industryId)) {
                return;
            }
        }
        throw new IllegalArgumentException("Industry ID '" + industryId + "' doesn't exist");
    }

    public static void CheckItemExists(String itemId) {
        for (SpecialItemSpecAPI specialItemSpec : Global.getSettings().getAllSpecialItemSpecs()) {
            if (specialItemSpec.getId().equals(itemId)) {
                return;
            }
        }
        throw new IllegalArgumentException("Item ID '" + itemId + "' doesn't exist");
    }

    public static void CheckSkillExists(String skillId) {
        for (String skillSpecId : Global.getSettings().getSkillIds()) {
            if (skillSpecId.equals(skillId)) {
                return;
            }
        }
        throw new IllegalArgumentException("Skill ID '" + skillId + "' doesn't exist");
    }

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
    // A mistyped string compiles fine and leads to plenty of debugging. A mistyped constant gives an error.

    public static final String csvOptionSeparator = "\\s*\\|\\s*";
    public static final String noneProjectId = "None";

    public static final HashMap<String, BoggledTerraformingRequirementFactory.TerraformingRequirementFactory> terraformingRequirementFactories = new HashMap<>();
    public static final HashMap<String, BoggledTerraformingDurationModifierFactory.TerraformingDurationModifierFactory> terraformingDurationModifierFactories = new HashMap<>();
    public static final HashMap<String, BoggledTerraformingProjectEffectFactory.TerraformingProjectEffectFactory> terraformingProjectEffectFactories = new HashMap<>();

    public static final HashMap<String, BoggledCommoditySupplyDemandFactory.CommodityDemandFactory> commodityDemandFactories = new HashMap<>();
    public static final HashMap<String, BoggledCommoditySupplyDemandFactory.CommoditySupplyFactory> commoditySupplyFactories = new HashMap<>();

    public static final HashMap<String, BoggledCommoditySupplyDemandFactory.IndustryEffectFactory> industryEffectFactories = new HashMap<>();
    public static final HashMap<String, BoggledCommoditySupplyDemandFactory.AICoreEffectFactory> aiCoreEffectFactories = new HashMap<>();

    @Nullable
    public static BoggledTerraformingRequirement.TerraformingRequirement getTerraformingRequirement(String terraformingRequirementType, String id, boolean invert, String data) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledTerraformingRequirementFactory.TerraformingRequirementFactory factory = terraformingRequirementFactories.get(terraformingRequirementType);
        if (factory == null) {
            log.error("Requirement " + id + " of type " + terraformingRequirementType + " has no assigned factory");
            return null;
        }
        BoggledTerraformingRequirement.TerraformingRequirement req = factory.constructFromJSON(id, invert, data);
        if (req == null) {
            log.error("Requirement " + id + " of type " + terraformingRequirementType + " was null when created with data " + data);
        }
        return req;
    }

    public static BoggledProjectRequirementsOR getTerraformingRequirements(String terraformingRequirementId) {
        return terraformingRequirements.get(terraformingRequirementId);
    }

    @Nullable
    public static BoggledTerraformingDurationModifier.TerraformingDurationModifier getDurationModifier(String durationModifierType, String id, String data) {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledTerraformingDurationModifierFactory.TerraformingDurationModifierFactory factory = terraformingDurationModifierFactories.get(durationModifierType);
        if (factory == null) {
            log.error("Duration modifier " + id + " of type " + durationModifierType + " has no assigned factory");
            return null;
        }
        BoggledTerraformingDurationModifier.TerraformingDurationModifier mod = factory.constructFromJSON(data);
        if (mod == null) {
            log.error("Duration modifier " + id + " of type " + durationModifierType + " was null when created with data " + data);
            return null;
        }
        return mod;
    }

    @Nullable
    public static BoggledCommoditySupplyDemand.CommodityDemand getCommodityDemand(String commodityDemandType, String id, String[] enableSettings, String commodity, String data) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledCommoditySupplyDemandFactory.CommodityDemandFactory factory = commodityDemandFactories.get(commodityDemandType);
        if (factory == null) {
            log.error("Commodity demand " + id + " of type " + commodityDemandType + " has no assigned factory");
            return null;
        }
        BoggledCommoditySupplyDemand.CommodityDemand demand = factory.constructFromJSON(id, enableSettings, commodity, data);
        if (demand == null) {
            log.error("Commodity demand " + id + " of type " + commodityDemandType + " was null when created with data " + data);
            return null;
        }
        return demand;
    }

    @Nullable
    public static BoggledCommoditySupplyDemand.CommoditySupply getCommoditySupply(String commoditySupplyType, String id, String[] enableSettings, String commodity, String data) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledCommoditySupplyDemandFactory.CommoditySupplyFactory factory = commoditySupplyFactories.get(commoditySupplyType);
        if (factory == null) {
            log.error("Commodity demand " + id + " of type " + commoditySupplyType + " has no assigned factory");
            return null;
        }
        BoggledCommoditySupplyDemand.CommoditySupply supply = factory.constructFromJSON(id, enableSettings, commodity, data);
        if (supply == null) {
            log.error("Commodity supply " + id + " of type " + commoditySupplyType + " was null when created with data " + data);
            return null;
        }
        return supply;
    }

    @Nullable
    public static BoggledCommoditySupplyDemand.IndustryEffect getIndustryEffect(String industryEffectType, String id, String[] enableSettings, String[] commoditiesDemanded, String data) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledCommoditySupplyDemandFactory.IndustryEffectFactory factory = industryEffectFactories.get(industryEffectType);
        if (factory == null) {
            log.error("Industry effect " + id + " of type " + industryEffectType + " has no assigned factory");
            return null;
        }
        BoggledCommoditySupplyDemand.IndustryEffect effect = factory.constructFromJSON(id, enableSettings, new ArrayList<>(asList(commoditiesDemanded)), data);
        if (effect == null) {
            log.error("Industry effect " + id + " of type " + industryEffectType + " was null when created with data " + data);
            return null;
        }
        return effect;
    }

    @Nullable
    public static BoggledCommoditySupplyDemand.AICoreEffect getAICoreEffect(String aiCoreEffectType, String id, String[] enableSettings, String data) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledCommoditySupplyDemandFactory.AICoreEffectFactory factory = aiCoreEffectFactories.get(aiCoreEffectType);
        if (factory == null) {
            log.error("AI core effect " + id + " of type " + aiCoreEffectType + " has no assigned factory");
            return null;
        }
        BoggledCommoditySupplyDemand.AICoreEffect effect = factory.constructFromJSON(id, enableSettings, data);
        if (effect == null) {
            log.error("AI core effect " + id + " of type " + aiCoreEffectType + " was null when created with data " + data);
            return null;
        }
        return effect;
    }

    @Nullable
    public static BoggledTerraformingProjectEffect.TerraformingProjectEffect getProjectEffect(String projectEffectType, String id, String data) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledTerraformingProjectEffectFactory.TerraformingProjectEffectFactory factory = terraformingProjectEffectFactories.get(projectEffectType);
        if (factory == null) {
            log.error("Terraforming project effect " + id + " of type " + projectEffectType + " has no assigned factory");
            return null;
        }
        BoggledTerraformingProjectEffect.TerraformingProjectEffect effect = factory.constructFromJSON(data);
        if (effect == null) {
            log.error("Terraforming project effect " + id + " of type " + projectEffectType + " was null when created with data " + data);
        }
        return effect;
    }

    public static boolean optionsAllowThis(String... options) {
        for (String option : options) {
            if (option.isEmpty()) {
                continue;
            }
            if (!boggledTools.getBooleanSetting(option)) {
                return false;
            }
        }
        return true;
    }

    public static String doTokenReplacement(String replace, Map<String, String> tokenReplacements) {
        for (Map.Entry<String, String> replacement : tokenReplacements.entrySet()) {
            replace = replace.replace(replacement.getKey(), replacement.getValue());
        }
        return replace;
    }

    public static String buildCommodityList(BaseIndustry industry, String[] commoditiesDemanded) {
        if (commoditiesDemanded.length == 0) {
            return "";
        }

        List<Pair<String, Integer>> deficits = industry.getAllDeficit(commoditiesDemanded);
        String[] strings = new String[deficits.size()];
        for (int i = 0; i < deficits.size(); ++i) {
            strings[i] = Global.getSettings().getCommoditySpec(deficits.get(i).one).getLowerCaseName();
        }
        return Misc.getAndJoined(strings);
    }

    private static void buildUnavailableReason(StringBuilder builder, @NotNull List<BoggledTerraformingProject.ProjectInstance> projects, MarketAPI market, Map<String, String> tokenReplacements) {
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            if (builder.length() != 0) {
                builder.append("\n");
            }
            for (BoggledProjectRequirementsAND.RequirementWithTooltipOverride req : project.getProject().getProjectRequirements()) {
                if (!req.checkRequirement(market)) {
                    if (builder.length() != 0) {
                        builder.append("\n");
                    }
                    String replaced = req.getTooltip(tokenReplacements);
                    builder.append(replaced);
                }
            }
        }
    }
    @NotNull
    public static String getUnavailableReason(List<BoggledTerraformingProject.ProjectInstance> projects, String industry, MarketAPI market, Map<String, String> tokenReplacements) {
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            if (!boggledTools.optionsAllowThis(project.getProject().getEnableSettings())) {
                return "Error in getUnavailableReason() in " + industry + ". Please tell Boggled about this on the forums.";
            }
        }

        StringBuilder ret = new StringBuilder();

        buildUnavailableReason(ret, projects, market, tokenReplacements);

        return ret.toString();
    }

    public static void initialiseDefaultTerraformingRequirementFactories() {
        addTerraformingRequirementFactory("PlanetType", new BoggledTerraformingRequirementFactory.PlanetType());
        addTerraformingRequirementFactory("MarketHasCondition", new BoggledTerraformingRequirementFactory.MarketHasCondition());
        addTerraformingRequirementFactory("MarketHasIndustry", new BoggledTerraformingRequirementFactory.MarketHasIndustry());
        addTerraformingRequirementFactory("MarketHasIndustryWithItem", new BoggledTerraformingRequirementFactory.MarketHasIndustryWithItem());
        addTerraformingRequirementFactory("MarketHasWaterPresent", new BoggledTerraformingRequirementFactory.MarketHasWaterPresent());
        addTerraformingRequirementFactory("MarketIsAtLeastSize", new BoggledTerraformingRequirementFactory.MarketIsAtLeastSize());
        addTerraformingRequirementFactory("TerraformingPossibleOnMarket", new BoggledTerraformingRequirementFactory.TerraformingPossibleOnMarket());
        addTerraformingRequirementFactory("MarketHasTags", new BoggledTerraformingRequirementFactory.MarketHasTags());
        addTerraformingRequirementFactory("MarketStorageContainsAtLeast", new BoggledTerraformingRequirementFactory.MarketStorageContainsAtLeast());
        addTerraformingRequirementFactory("PlayerHasStoryPointsAtLeast", new BoggledTerraformingRequirementFactory.PlayerHasStoryPointsAtLeast());
        addTerraformingRequirementFactory("WorldTypeSupportsResourceImprovement", new BoggledTerraformingRequirementFactory.WorldTypeSupportsResourceImprovement());

        addTerraformingRequirementFactory("FocusPlanetType", new BoggledTerraformingRequirementFactory.FocusPlanetType());
        addTerraformingRequirementFactory("FocusMarketHasCondition", new BoggledTerraformingRequirementFactory.FocusMarketHasCondition());

        addTerraformingRequirementFactory("IntegerFromTagSubstring", new BoggledTerraformingRequirementFactory.IntegerFromTagSubstring());

        addTerraformingRequirementFactory("PlayerHasSkill", new BoggledTerraformingRequirementFactory.PlayerHasSkill());

        addTerraformingRequirementFactory("SystemStarHasTags", new BoggledTerraformingRequirementFactory.SystemStarHasTags());
        addTerraformingRequirementFactory("SystemStarType", new BoggledTerraformingRequirementFactory.SystemStarType());
    }

    public static void addTerraformingRequirementFactory(String key, BoggledTerraformingRequirementFactory.TerraformingRequirementFactory value) {
        Global.getLogger(boggledTools.class).info("Adding terraforming requirement factory " + key);
        terraformingRequirementFactories.put(key, value);
    }

    public static void initialiseDefaultTerraformingDurationModifierFactories() {
        addTerraformingDurationModifierFactory("PlanetSize", new BoggledTerraformingDurationModifierFactory.PlanetSize());
    }

    public static void addTerraformingDurationModifierFactory(String key, BoggledTerraformingDurationModifierFactory.TerraformingDurationModifierFactory value) {
        Global.getLogger(boggledTools.class).info("Adding terraforming duration modifier factory " + key);
        terraformingDurationModifierFactories.put(key, value);
    }

    public static void initialiseDefaultCommoditySupplyAndDemandFactories() {
        addCommoditySupplyFactory("FlatCommoditySupply", new BoggledCommoditySupplyDemandFactory.FlatSupplyFactory());
        addCommoditySupplyFactory("MarketSizeSupply", new BoggledCommoditySupplyDemandFactory.MarketSizeSupplyFactory());

        addCommodityDemandFactory("FlatCommodityDemand", new BoggledCommoditySupplyDemandFactory.FlatDemandFactory());
        addCommodityDemandFactory("MarketSizeDemand", new BoggledCommoditySupplyDemandFactory.MarketSizeDemandFactory());
        addCommodityDemandFactory("PlayerMarketSizeElseFlatDemand", new BoggledCommoditySupplyDemandFactory.PlayerMarketSizeElseFlatDemandFactory());
    }

    public static void addCommoditySupplyFactory(String key, BoggledCommoditySupplyDemandFactory.CommoditySupplyFactory value) {
        Global.getLogger(boggledTools.class).info("Adding commodity supply factory " + key);
        commoditySupplyFactories.put(key ,value);
    }

    public static void addCommodityDemandFactory(String key, BoggledCommoditySupplyDemandFactory.CommodityDemandFactory value) {
        Global.getLogger(boggledTools.class).info("Adding commodity demand factory " + key);
        commodityDemandFactories.put(key, value);
    }

    public static void initialiseDefaultIndustryEffectFactories() {
        addIndustryEffectFactory("DeficitToInactive", new BoggledCommoditySupplyDemandFactory.DeficitToInactiveFactory());
        addIndustryEffectFactory("DeficitToCommodity", new BoggledCommoditySupplyDemandFactory.DeficitToCommodityFactory());
        addIndustryEffectFactory("DeficitMultiplierToUpkeep", new BoggledCommoditySupplyDemandFactory.DeficitMultiplierToUpkeepFactory());

        addIndustryEffectFactory("ConditionMultiplierToUpkeep", new BoggledCommoditySupplyDemandFactory.ConditionMultiplierToUpkeepFactory());

        addIndustryEffectFactory("TagMultiplierToUpkeep", new BoggledCommoditySupplyDemandFactory.TagMultiplierToUpkeepFactory());

        addIndustryEffectFactory("IncomeBonusToIndustry", new BoggledCommoditySupplyDemandFactory.IncomeBonusToIndustryFactory());

        addIndustryEffectFactory("BonusToAccessibility", new BoggledCommoditySupplyDemandFactory.BonusToAccessibilityFactory());

        addAICoreEffectFactory("SupplyBonusWithDeficitToIndustry", new BoggledCommoditySupplyDemandFactory.SupplyBonusWithDeficitToIndustryFactory());

        addAICoreEffectFactory("ShitPost", new BoggledCommoditySupplyDemandFactory.ShitPostFactory());
    }

    public static void addIndustryEffectFactory(String key, BoggledCommoditySupplyDemandFactory.IndustryEffectFactory value) {
        Global.getLogger(boggledTools.class).info("Adding industry effect factory " + key);
        industryEffectFactories.put(key, value);
    }

    public static void addAICoreEffectFactory(String key, BoggledCommoditySupplyDemandFactory.AICoreEffectFactory value) {
        Global.getLogger(boggledTools.class).info("Adding AI core effect factory " + key);
        aiCoreEffectFactories.put(key, value);
    }

    public static void initialiseDefaultTerraformingProjectEffectFactories() {
        addTerraformingProjectEffectFactory("PlanetTypeChange", new BoggledTerraformingProjectEffectFactory.PlanetTypeChange());
        addTerraformingProjectEffectFactory("MarketAddCondition", new BoggledTerraformingProjectEffectFactory.MarketAddCondition());
        addTerraformingProjectEffectFactory("MarketRemoveCondition", new BoggledTerraformingProjectEffectFactory.MarketRemoveCondition());
        addTerraformingProjectEffectFactory("MarketOptionalCondition", new BoggledTerraformingProjectEffectFactory.MarketOptionalCondition());
        addTerraformingProjectEffectFactory("MarketProgressResource", new BoggledTerraformingProjectEffectFactory.MarketProgressResource());

        addTerraformingProjectEffectFactory("FocusMarketAddCondition", new BoggledTerraformingProjectEffectFactory.FocusMarketAddCondition());
        addTerraformingProjectEffectFactory("FocusMarketRemoveCondition", new BoggledTerraformingProjectEffectFactory.FocusMarketRemoveCondition());
        addTerraformingProjectEffectFactory("FocusMarketProgressResource", new BoggledTerraformingProjectEffectFactory.FocusMarketProgressResource());
        addTerraformingProjectEffectFactory("FocusMarketAndSiphonStationProgressResource", new BoggledTerraformingProjectEffectFactory.FocusMarketAndSiphonStationProgressResource());

        addTerraformingProjectEffectFactory("SystemAddCoronalTapFactory", new BoggledTerraformingProjectEffectFactory.SystemAddCoronalTapFactory());
        addTerraformingProjectEffectFactory("MarketAddStellarReflectors", new BoggledTerraformingProjectEffectFactory.MarketAddStellarReflectorsFactory());

        addTerraformingProjectEffectFactory("MarketRemoveIndustry", new BoggledTerraformingProjectEffectFactory.MarketRemoveIndustryFactory());

        addTerraformingProjectEffectFactory("RemoveCommodityFromSubmarket", new BoggledTerraformingProjectEffectFactory.RemoveCommodityFromSubmarketFactory());
        addTerraformingProjectEffectFactory("RemoveStoryPointsFromPlayer", new BoggledTerraformingProjectEffectFactory.RemoveStoryPointsFromPlayerFactory());
        addTerraformingProjectEffectFactory("AddItemToSubmarket", new BoggledTerraformingProjectEffectFactory.AddItemToSubmarketFactory());
    }

    public static void addTerraformingProjectEffectFactory(String key, BoggledTerraformingProjectEffectFactory.TerraformingProjectEffectFactory value) {
        Global.getLogger(boggledTools.class).info("Adding terraforming project effect factory " + key);
        terraformingProjectEffectFactories.put(key, value);
    }

    @NotNull
    @Contract("_, _, _ -> new")
    private static ArrayList<String> arrayListFromJSON(@NotNull JSONObject data, String key, String regex) throws JSONException {
        String toSplit = data.getString(key);
        if (toSplit.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(toSplit.split(regex)));
    }

    private static BoggledProjectRequirementsAND requirementsFromRequirementsArray(JSONArray requirementArray, String id, String requirementType) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        List<BoggledProjectRequirementsAND.RequirementWithTooltipOverride> reqs = new ArrayList<>();
        for (int i = 0; i < requirementArray.length(); ++i) {
            JSONObject requirementObject = requirementArray.getJSONObject(i);
            String requirementsString = requirementObject.getString("requirement_id");
            String descriptionOverride = requirementObject.optString("description", "");

            BoggledProjectRequirementsOR req = terraformingRequirements.get(requirementsString);
            if (req == null) {
                log.info("Project " + id + " has invalid " + requirementType + " " + requirementsString);
            } else {
                reqs.add(new BoggledProjectRequirementsAND.RequirementWithTooltipOverride(req, descriptionOverride));
            }
        }
        return new BoggledProjectRequirementsAND(reqs);
    }

    private static List<BoggledCommoditySupplyDemand.IndustryEffect> industryEffectsFromObject(JSONObject object, String key, String id) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        String[] effectStrings = object.getString(key).split(csvOptionSeparator);
        List<BoggledCommoditySupplyDemand.IndustryEffect> ret = new ArrayList<>();
        for (String effectString : effectStrings) {
            if (effectString.isEmpty()) {
                continue;
            }
            BoggledCommoditySupplyDemand.IndustryEffect effect = boggledTools.industryEffects.get(effectString);
            if (effect != null) {
                ret.add(effect);
            } else {
                log.info("Industry " + id + " has invalid " + key + " effect " + effectString);
            }
        }
        return ret;
    }

    public static void initialisePlanetTypesFromJSON(@NotNull JSONArray planetTypesJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, PlanetType> planetTypesMap = new HashMap<>();

        planetTypesMap.put(unknownPlanetId, new PlanetType(unknownPlanetId, "unknown", false, 0, new ArrayList<Pair<BoggledProjectRequirementsOR, Integer>>()));

        for (int i = 0; i < planetTypesJSON.length(); ++i) {
            try {
                JSONObject row = planetTypesJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String[] conditions = row.getString("conditions").split(boggledTools.csvOptionSeparator);
                String planetTypeName = row.getString("name");
                String planetTypeId = row.getString("terraforming_type_id");
                boolean terraformingPossible = row.getBoolean("terraforming_possible");

                int baseWaterLevel = row.getInt("base_water_level");

                List<Pair<BoggledProjectRequirementsOR, Integer>> conditionalWaterRequirements = new ArrayList<>();
                String conditionalWaterLevelsString = row.getString("conditional_water_requirements");
                if (!conditionalWaterLevelsString.isEmpty()) {
                    JSONArray conditionalWaterLevels = new JSONArray(conditionalWaterLevelsString);
                    for (int j = 0; j < conditionalWaterLevels.length(); ++j) {
                        JSONObject conditionalWaterLevel = conditionalWaterLevels.getJSONObject(j);
                        String requirement = conditionalWaterLevel.getString("requirement_id");
                        int waterLevel = conditionalWaterLevel.getInt("water_level");

                        BoggledProjectRequirementsOR waterRequirement = terraformingRequirements.get(requirement);
                        if (waterRequirement != null) {
                            conditionalWaterRequirements.add(new Pair<>(waterRequirement, waterLevel));
                        }
                    }
                }

                PlanetType planetType = new PlanetType(planetTypeId, planetTypeName, terraformingPossible, baseWaterLevel, conditionalWaterRequirements);

                planetTypesMap.put(id, planetType);

            } catch (JSONException e) {
                log.error("Error in planet types map: " + e);
            }
        }

        boggledTools.planetTypesMap = planetTypesMap;
    }

    public static void initialiseResourceProgressionsFromJSON(@NotNull JSONArray resourceProgressionsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, ArrayList<String>> resourceProgressions = new HashMap<>();

        for (int i = 0; i < resourceProgressionsJSON.length(); ++i) {
            try {
                JSONObject row = resourceProgressionsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id.isEmpty()) {
                    continue;
                }

                ArrayList<String> resource_progression = arrayListFromJSON(row, "resource_progression", boggledTools.csvOptionSeparator);

                resourceProgressions.put(id, resource_progression);
            } catch (JSONException e) {
                log.error("Error in resource progressions: " + e);
            }
        }

        boggledTools.resourceProgressions = resourceProgressions;
    }

    public static void initialiseResourceLimitsFromJSON(@NotNull JSONArray resourceLimitsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<Pair<String, String>, String> resourceLimits = new HashMap<>();

        for (int i = 0; i < resourceLimitsJSON.length(); ++i) {
            try {
                JSONObject row = resourceLimitsJSON.getJSONObject(i);

                String[] id = row.getString("id").split(boggledTools.csvOptionSeparator);
                if (id[0].isEmpty()) {
                    continue;
                }

                String resourceMax = row.getString("resource_max");

                assert(id.length == 2);
                Pair<String, String> key = new Pair<>(id[0], id[1]);

                resourceLimits.put(key, resourceMax);
            } catch (JSONException e) {
                log.error("Error in resource limits: " + e);
            }
        }

        boggledTools.resourceLimits = resourceLimits;
    }

    public static void initialiseIndustryOptionsFromJSON(@NotNull JSONArray industryOptionsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledCommonIndustry> industryProjects = new HashMap<>();
        for (int i = 0; i < industryOptionsJSON.length(); ++i) {
            try {
                JSONObject row = industryOptionsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String industry = row.getString("tooltip");

                String[] projectStrings = row.getString("projects").split(boggledTools.csvOptionSeparator);

                ArrayList<BoggledTerraformingProject> projects = new ArrayList<>();
                for (String projectString : projectStrings) {
                    BoggledTerraformingProject project = boggledTools.getProject(projectString);
                    if (project != null) {
                        projects.add(project);
                    }
                }
                
                String[] commoditySupplyStrings = row.getString("commodity_supply").split(boggledTools.csvOptionSeparator);
                List<BoggledCommoditySupplyDemand.CommoditySupply> commoditySupply = new ArrayList<>();
                for (String commoditySupplyString : commoditySupplyStrings) {
                    if (commoditySupplyString.isEmpty()) {
                        continue;
                    }
                    BoggledCommoditySupplyDemand.CommoditySupply supply = boggledTools.commoditySupply.get(commoditySupplyString);
                    if (supply != null) {
                        commoditySupply.add(supply);
                    } else {
                        log.info("Industry " + id + " has invalid commodity supply " + commoditySupplyString);
                    }
                }

                String[] commodityDemandStrings = row.getString("commodity_demand").split(boggledTools.csvOptionSeparator);
                List<BoggledCommoditySupplyDemand.CommodityDemand> commodityDemand = new ArrayList<>();
                for (String commodityDemandString : commodityDemandStrings) {
                    if (commodityDemandString.isEmpty()) {
                        continue;
                    }
                    BoggledCommoditySupplyDemand.CommodityDemand demand = boggledTools.commodityDemand.get(commodityDemandString);
                    if (demand != null) {
                        commodityDemand.add(demand);
                    } else {
                        log.info("Industry " + id + " has invalid commodity demand " + commodityDemandString);
                    }
                }

                List<BoggledCommoditySupplyDemand.IndustryEffect> industryEffects = industryEffectsFromObject(row, "industry_effects", id);
                List< BoggledCommoditySupplyDemand.IndustryEffect> improveEffects = industryEffectsFromObject(row, "improve_effects", id);

                String[] aiCoreEffectStrings = row.getString("ai_core_effects").split(csvOptionSeparator);
                ArrayList<BoggledCommoditySupplyDemand.AICoreEffect> aiCoreEffects = new ArrayList<>();
                for (String aiCoreEffectString : aiCoreEffectStrings) {
                    if (aiCoreEffectString.isEmpty()) {
                        continue;
                    }
                    BoggledCommoditySupplyDemand.AICoreEffect aiCoreEffect = boggledTools.aiCoreEffects.get(aiCoreEffectString);
                    if (aiCoreEffect != null) {
                        aiCoreEffects.add(aiCoreEffect);
                    } else {
                        log.info("Industry " + id + " has invalid AI core effect " + aiCoreEffectString);
                    }
                }

                industryProjects.put(id, new BoggledCommonIndustry(id, industry, projects, commoditySupply, commodityDemand, industryEffects, improveEffects, aiCoreEffects));
            } catch (JSONException e) {
                log.error("Error in industry options: " + e);
            }
        }
        boggledTools.industryProjects = industryProjects;
    }

    public static void initialiseCommoditySupplyAndDemandFromJSON(@NotNull JSONArray commodityDemandsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledCommoditySupplyDemand.CommodityDemand> commodityDemand = new HashMap<>();
        HashMap<String, BoggledCommoditySupplyDemand.CommoditySupply> commoditySupply = new HashMap<>();

        for (int i = 0; i < commodityDemandsJSON.length(); ++i) {
            try {
                JSONObject row = commodityDemandsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String supplyOrDemand = row.getString("supply_or_demand");

                String[] enableSettings = row.getString("enable_settings").split(csvOptionSeparator);

                String commodityDemandType = row.getString("commodity_quantity_type");
                String commodityId = row.getString("commodity_id");
                String data = row.getString("data");

                switch (supplyOrDemand) {
                    case "supply":
                        BoggledCommoditySupplyDemand.CommoditySupply supply = getCommoditySupply(commodityDemandType, id, enableSettings, commodityId, data);

                        commoditySupply.put(id, supply);
                        break;
                    case "demand":
                        BoggledCommoditySupplyDemand.CommodityDemand demand = getCommodityDemand(commodityDemandType, id, enableSettings, commodityId, data);

                        commodityDemand.put(id, demand);
                        break;
                }

            } catch (JSONException e) {
                log.error("Error in commodity supply and demand: " + e);
            }
        }
        boggledTools.commodityDemand = commodityDemand;
        boggledTools.commoditySupply = commoditySupply;
    }

    public static void initialiseIndustryEffectsFromJSON(@NotNull JSONArray industryEffectsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledCommoditySupplyDemand.IndustryEffect> industryEffects = new HashMap<>();

        String idForErrors = "";
        for (int i = 0; i < industryEffectsJSON.length(); ++i) {
            try {
                JSONObject row = industryEffectsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                String[] enableSettings = row.getString("enable_settings").split(csvOptionSeparator);

                String industryEffectType = row.getString("industry_effect_type");
                String[] commoditiesDemanded = row.getString("commodities_demanded").split(csvOptionSeparator);
                String data = row.getString("data");

                BoggledCommoditySupplyDemand.IndustryEffect effect = getIndustryEffect(industryEffectType, id, enableSettings, commoditiesDemanded, data);

                industryEffects.put(id, effect);
            } catch (JSONException e) {
                log.error("Error in industry effect '" + idForErrors + "': " + e);
            }
        }

        boggledTools.industryEffects = industryEffects;
    }

    public static void initialiseAICoreEffectsFromJSON(@NotNull JSONArray aiCoreEffectsJSON) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledCommoditySupplyDemand.AICoreEffect> aiCoreEffects = new HashMap<>();

        String idForErrors = "";
        for (int i = 0; i < aiCoreEffectsJSON.length(); ++i) {
            try {
                JSONObject row = aiCoreEffectsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                String[] enableSettings = row.getString("enable_settings").split(csvOptionSeparator);

                String aiCoreEffectType = row.getString("ai_core_effect_type");
                String data = row.getString("data");

                BoggledCommoditySupplyDemand.AICoreEffect effect = getAICoreEffect(aiCoreEffectType, id, enableSettings, data);

                aiCoreEffects.put(id, effect);
            } catch (JSONException e) {
                log.error("Error in AI core effect '" + idForErrors + "': " + e);
            }
        }

        boggledTools.aiCoreEffects = aiCoreEffects;
    }

    public static void initialiseTerraformingRequirementFromJSON(@NotNull JSONArray terraformingRequirementJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledTerraformingRequirement.TerraformingRequirement> terraformingRequirement = new HashMap<>();

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

                BoggledTerraformingRequirement.TerraformingRequirement req = getTerraformingRequirement(requirementType, id, invert, data);
                if (req != null) {
                    terraformingRequirement.put(id, req);
                }
            } catch (JSONException e) {
                log.error("Error in terraforming requirement: " + e);
            }
        }

        boggledTools.terraformingRequirement = terraformingRequirement;
    }

    public static void initialiseTerraformingRequirementsFromJSON(@NotNull JSONArray terraformingRequirementsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledProjectRequirementsOR> terraformingRequirements = new HashMap<>();
        for (int i = 0; i < terraformingRequirementsJSON.length(); ++i) {
            try {
                JSONObject row = terraformingRequirementsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id.isEmpty()) {
                    continue;
                }

                String tooltip = row.getString("tooltip");
                boolean invertAll = row.getBoolean("invert_all");
                String[] requirements = row.getString("requirements").split(boggledTools.csvOptionSeparator);

                ArrayList<BoggledTerraformingRequirement.TerraformingRequirement> reqs = new ArrayList<>();
                for (String requirement : requirements) {
                    BoggledTerraformingRequirement.TerraformingRequirement req = terraformingRequirement.get(requirement);
                    if (req != null) {
                        reqs.add(req);
                    } else {
                        log.error("Requirements " + id + " has invalid requirement " + requirement);
                    }
                }

                BoggledProjectRequirementsOR terraformingReqs = new BoggledProjectRequirementsOR(id, tooltip, invertAll, reqs);
                terraformingRequirements.put(id, terraformingReqs);

            } catch (JSONException e) {
                log.error("Error in terraforming requirements: " + e);
            }
        }
        boggledTools.terraformingRequirements = terraformingRequirements;
    }

    public static void initialiseTerraformingDurationModifiersFromJSON(@NotNull JSONArray durationModifiersJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiers = new HashMap<>();
        for (int i = 0; i < durationModifiersJSON.length(); ++i) {
            try {
                JSONObject row = durationModifiersJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String durationModifierType = row.getString("duration_modifier_type");
                String data = row.getString("data");

                BoggledTerraformingDurationModifier.TerraformingDurationModifier mod = getDurationModifier(durationModifierType, id, data);
                if (mod != null) {
                    durationModifiers.put(id, mod);
                }
            } catch (JSONException e) {
                log.error("Error in duration modifiers: " + e);
            }
        }

        boggledTools.durationModifiers = durationModifiers;
    }

    public static void initialiseTerraformingProjectEffectsFromJSON(@NotNull JSONArray projectEffectsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledTerraformingProjectEffect.TerraformingProjectEffect> terraformingProjectEffects = new HashMap<>();
        for (int i = 0; i < projectEffectsJSON.length(); ++i) {
            try {
                JSONObject row = projectEffectsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String projectEffectType = row.getString("effect_type");
                String data = row.getString("data");

                BoggledTerraformingProjectEffect.TerraformingProjectEffect projectEffect = getProjectEffect(projectEffectType, id, data);
                if (projectEffect != null) {
                    terraformingProjectEffects.put(id, projectEffect);
                }
            } catch (JSONException e) {
                log.error("Error in project effects: " + e);
            }
        }
        boggledTools.terraformingProjectEffects = terraformingProjectEffects;
    }

    public static void initialiseTerraformingProjectsFromJSON(@NotNull JSONArray terraformingProjectsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        LinkedHashMap<String, BoggledTerraformingProject> terraformingProjects = new LinkedHashMap<>();
        String idForErrors = "";
        for (int i = 0; i < terraformingProjectsJSON.length(); ++i) {
            try {
                JSONObject row = terraformingProjectsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                String[] enableSettings = row.getString("enable_settings").split(csvOptionSeparator);

                String projectType = row.getString("project_type");

                String tooltip = row.getString("tooltip");
                String intelCompleteMessage = row.getString("intel_complete_message");

                String incompleteMessage = row.getString("incomplete_message");
                ArrayList<String> incompleteHighlights = arrayListFromJSON(row, "incomplete_message_highlights", boggledTools.csvOptionSeparator);

                int baseProjectDuration = row.optInt("base_project_duration", 0);

                String[] projectDurationModifiers = row.getString("dynamic_project_duration_modifiers").split(boggledTools.csvOptionSeparator);
                ArrayList<BoggledTerraformingDurationModifier.TerraformingDurationModifier> terraformingDurationModifiers = new ArrayList<>();
                for (String projectDurationModifier : projectDurationModifiers) {
                    if (projectDurationModifier.isEmpty()) {
                        continue;
                    }
                    BoggledTerraformingDurationModifier.TerraformingDurationModifier mod = boggledTools.durationModifiers.get(projectDurationModifier);
                    if (mod != null) {
                        terraformingDurationModifiers.add(mod);
                    } else {
                        log.info("Project " + id + " has invalid duration modifier " + projectDurationModifier);
                    }
                }

                List<BoggledProjectRequirementsAND> reqsStall = new ArrayList<>();
                String requirementsStallArrayString = row.getString("requirements_stall");
                if (!requirementsStallArrayString.isEmpty()) {
                    JSONArray requirementsStallArray = new JSONArray(requirementsStallArrayString);
                    for (int j = 0; j < requirementsStallArray.length(); ++j) {
                        JSONArray reqArray = requirementsStallArray.getJSONArray(j);
                        BoggledProjectRequirementsAND reqsAnd = requirementsFromRequirementsArray(reqArray, id, "requirements_stall");
                        reqsStall.add(reqsAnd);
                    }
                }

                List<BoggledProjectRequirementsAND> reqsReset = new ArrayList<>();
                String requirementsResetArrayString = row.getString("requirements_reset");
                if (!requirementsResetArrayString.isEmpty()) {
                    JSONArray requirementsResetArray = new JSONArray(requirementsResetArrayString);
                    for (int j = 0; j < requirementsResetArray.length(); ++j) {
                        JSONArray reqArray = requirementsResetArray.getJSONArray(j);
                        BoggledProjectRequirementsAND reqsAnd = requirementsFromRequirementsArray(reqArray, id, "requirements_reset");
                        reqsStall.add(reqsAnd);
                    }
                }

                String[] projectEffects = row.getString("project_effects").split(boggledTools.csvOptionSeparator);
                ArrayList<BoggledTerraformingProjectEffect.TerraformingProjectEffect> terraformingProjectEffects = new ArrayList<>();
                for (String projectEffect : projectEffects) {
                    if (projectEffect.isEmpty()) {
                        continue;
                    }
                    BoggledTerraformingProjectEffect.TerraformingProjectEffect effect = boggledTools.terraformingProjectEffects.get(projectEffect);
                    if (effect != null) {
                        terraformingProjectEffects.add(effect);
                    } else {
                        log.info("Project " + id + " has invalid project effect " + projectEffect);
                    }
                }

                BoggledProjectRequirementsAND reqs;
                String requirementsArrayString = row.getString("requirements");
                if (!requirementsArrayString.isEmpty()) {
                    JSONArray reqsArray = new JSONArray(requirementsArrayString);
                    reqs = requirementsFromRequirementsArray(reqsArray, id, "requirements");
                } else {
                    reqs = new BoggledProjectRequirementsAND(new ArrayList<BoggledProjectRequirementsAND.RequirementWithTooltipOverride>());
                }

                BoggledProjectRequirementsAND reqsHidden;
                String requirementsHiddenArrayString = row.getString("requirements_hidden");
                if (!requirementsHiddenArrayString.isEmpty()) {
                    JSONArray reqsHiddenArray = new JSONArray(requirementsHiddenArrayString);
                    reqsHidden = requirementsFromRequirementsArray(reqsHiddenArray, id, "requirements_hidden");
                } else {
                    reqsHidden = new BoggledProjectRequirementsAND(new ArrayList<BoggledProjectRequirementsAND.RequirementWithTooltipOverride>());
                }

                BoggledTerraformingProject terraformingProj = new BoggledTerraformingProject(id, enableSettings, projectType, tooltip, intelCompleteMessage, incompleteMessage, incompleteHighlights, reqs, reqsHidden, baseProjectDuration, terraformingDurationModifiers, reqsStall, reqsReset, terraformingProjectEffects);
                terraformingProjects.put(id, terraformingProj);

            } catch (JSONException e) {
                log.error("Error in terraforming projects " + idForErrors + ": " + e);
            }
        }
        boggledTools.terraformingProjects = terraformingProjects;
    }

    public static void initialiseTerraformingRequirementsOverrides(@NotNull JSONArray terraformingRequirementsOverrideJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        try {
            for (int i = 0; i < terraformingRequirementsOverrideJSON.length(); ++i) {
                JSONObject row = terraformingRequirementsOverrideJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String requirementsId = row.getString("requirements_id");
                BoggledProjectRequirementsOR reqs = terraformingRequirements.get(requirementsId);
                if (reqs == null) {
                    log.error("Mod " + id + " terraforming requirements " + requirementsId + " not found, ignoring");
                    continue;
                }

                String[] requirementAddedStrings = row.getString("requirement_added").split(boggledTools.csvOptionSeparator);
                String[] requirementRemovedStrings = row.getString("requirement_removed").split(boggledTools.csvOptionSeparator);

                ArrayList<BoggledTerraformingRequirement.TerraformingRequirement> requirementAdded = new ArrayList<>();
                for (String requirementAddedString : requirementAddedStrings) {
                    BoggledTerraformingRequirement.TerraformingRequirement req = terraformingRequirement.get(requirementAddedString);
                    if (req != null) {
                        requirementAdded.add(req);
                    }
                }

                reqs.addRemoveProjectRequirement(requirementAdded, requirementRemovedStrings);

            }
        } catch (JSONException e) {
            log.error("Error in terraforming requirements overrides: " + e);
        }
    }

    public static void initialiseTerraformingProjectOverrides(@NotNull JSONArray terraformingProjectsOverrideJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        try {
            for (int i = 0; i < terraformingProjectsOverrideJSON.length(); ++i) {
                JSONObject row = terraformingProjectsOverrideJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                String projectId = row.getString("project_id");
                BoggledTerraformingProject proj = terraformingProjects.get(projectId);
                if (proj == null) {
                    log.error("Mod " + id + " terraforming project " + projectId + " not found, ignoring");
                    continue;
                }

                String tooltipOverride = row.getString("tooltip_override");
                String tooltipAddition = row.getString("tooltip_addition");

                String[] requirementsAddedStrings = row.getString("requirements_added").split(boggledTools.csvOptionSeparator);
                String[] requirementsRemovedStrings = row.getString("requirements_removed").split(boggledTools.csvOptionSeparator);
                String planetTypeChangeOverride = row.getString("planet_type_change_override");
                ArrayList<String> conditionsAdded = arrayListFromJSON(row, "conditions_added", boggledTools.csvOptionSeparator);
                ArrayList<String> conditionsRemoved = arrayListFromJSON(row, "conditions_removed", boggledTools.csvOptionSeparator);
                ArrayList<String> conditionsOption = arrayListFromJSON(row, "conditions_option", boggledTools.csvOptionSeparator);
                String optionName = row.getString("option_name");
                ArrayList<String> conditionProgressAdded = arrayListFromJSON(row, "condition_progress_added", boggledTools.csvOptionSeparator);
                ArrayList<String> conditionProgressRemoved = arrayListFromJSON(row, "condition_progress_removed", boggledTools.csvOptionSeparator);

                ArrayList<BoggledProjectRequirementsOR> requirementsAdded = new ArrayList<>();
                for (String requirementAddedString : requirementsAddedStrings) {
                    BoggledProjectRequirementsOR req = terraformingRequirements.get(requirementAddedString);
                    if (req != null) {
                        requirementsAdded.add(terraformingRequirements.get(requirementAddedString));
                    }
                }

//                proj.overrideAddTooltip(tooltipOverride, tooltipAddition);
//                proj.addRemoveProjectRequirements(requirementsAdded, requirementsRemovedStrings);
//                proj.overridePlanetTypeChange(planetTypeChangeOverride);
//                proj.addRemoveConditionsAddedRemoved(conditionsAdded, conditionsRemoved);
//                proj.addRemoveConditionProgress(conditionProgressAdded, conditionProgressRemoved);
            }
        } catch (JSONException e) {
            log.error("Error in terraforming projects overrides: " + e);
        }
    }

    public static BoggledTerraformingProject getProject(String projectId) {
        return terraformingProjects.get(projectId);
    }

    public static HashMap<Pair<String, String>, String> getResourceLimits() { return resourceLimits; }
    public static HashMap<String, ArrayList<String>> getResourceProgressions() { return resourceProgressions; }

    private static HashMap<String, BoggledTerraformingRequirement.TerraformingRequirement> terraformingRequirement;
    private static HashMap<String, BoggledProjectRequirementsOR> terraformingRequirements;
    private static HashMap<String, BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiers;

    private static HashMap<String, BoggledCommoditySupplyDemand.CommoditySupply> commoditySupply;
    private static HashMap<String, BoggledCommoditySupplyDemand.CommodityDemand> commodityDemand;

    private static HashMap<String, BoggledCommoditySupplyDemand.IndustryEffect> industryEffects;
    private static HashMap<String, BoggledCommoditySupplyDemand.AICoreEffect> aiCoreEffects;

    private static HashMap<String, BoggledTerraformingProjectEffect.TerraformingProjectEffect> terraformingProjectEffects;
    private static HashMap<String, BoggledCommonIndustry> industryProjects;
    private static LinkedHashMap<String, BoggledTerraformingProject> terraformingProjects;

    public static HashMap<String, BoggledProjectRequirementsOR> getTerraformingRequirements() {
        return terraformingRequirements;
    }

    private static HashMap<String, ArrayList<String>> resourceProgressions;
    private static HashMap<Pair<String, String>, String> resourceLimits;

    private static HashMap<String, PlanetType> planetTypesMap;

    public static BoggledCommonIndustry getIndustryProject(String industry) {
        return industryProjects.get(industry);
    }

    public static HashMap<String, Integer> getNumProjects() {
        HashMap<String, Integer> ret = new HashMap<>();
        for (Map.Entry<String, BoggledTerraformingProject> entry : terraformingProjects.entrySet()) {
            if (!entry.getValue().isEnabled()) {
                continue;
            }
            Integer val = ret.get(entry.getValue().getProjectType());
            if (val != null) {
                ret.put(entry.getValue().getProjectType(), ++val);
            } else {
                ret.put(entry.getValue().getProjectType(), 1);
            }
        }
        return ret;
    }

    public static HashMap<String, LinkedHashMap<String, BoggledTerraformingProject>> getVisibleProjects(MarketAPI market) {
        HashMap<String, LinkedHashMap<String, BoggledTerraformingProject>> ret = new HashMap<>();
        for (Map.Entry<String, BoggledTerraformingProject> entry : terraformingProjects.entrySet()) {
            if (!entry.getValue().isEnabled()) {
                continue;
            }
            if (!entry.getValue().requirementsHiddenMet(market)) {
                continue;
            }
            LinkedHashMap<String, BoggledTerraformingProject> val = ret.get(entry.getValue().getProjectType());
            if (val == null) {
                val = new LinkedHashMap<>();
                ret.put(entry.getValue().getProjectType(), val);
            }
            val.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    public static Map<String, String> getTokenReplacements(MarketAPI market) {
        LinkedHashMap<String, String> ret = new LinkedHashMap<>();
        ret.put("$player", Global.getSector().getPlayerPerson().getNameString());
        ret.put("$market", market.getName());
        ret.put("$focusMarket", BoggledCommonIndustry.getFocusMarketOrMarket(market).getName());
        ret.put("$planetTypeName", getPlanetType(market.getPlanetEntity()).getPlanetTypeName());
        ret.put("$system", market.getStarSystem().getName());
        return ret;
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

        PlanetType planetType = planetTypesMap.get(planet.getTypeId());
        if (planetType != null) {
            return planetType;
        }
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

    public static void showProjectCompleteIntelMessage(String project, String completedMessage, MarketAPI market) {
        if (completedMessage.isEmpty()) {
            return;
        }
        if (market.isPlayerOwned()) {
            MessageIntel intel = new MessageIntel(project + " on " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - " + completedMessage);
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static class PlanetType {
        private final String planetId;
        private final String planetTypeName;
        private final boolean terraformingPossible;
        private final int baseWaterLevel;
        private final List<Pair<BoggledProjectRequirementsOR, Integer>> conditionalWaterRequirements;

        public String getPlanetId() { return planetId; }
        public String getPlanetTypeName() { return planetTypeName; }
        public boolean getTerraformingPossible() { return terraformingPossible; }
        public int getWaterLevel(MarketAPI market) {
            if (conditionalWaterRequirements.isEmpty()) {
                return baseWaterLevel;
            }

            for (Pair<BoggledProjectRequirementsOR, Integer> conditionalWaterRequirement : conditionalWaterRequirements) {
                if (conditionalWaterRequirement.one.checkRequirement(market)) {
                    return conditionalWaterRequirement.two;
                }
            }
            return baseWaterLevel;
        }

        public PlanetType(String planetId, String planetTypeName, boolean terraformingPossible, int baseWaterLevel, List<Pair<BoggledProjectRequirementsOR, Integer>> conditionalWaterRequirements) {
            this.planetId = planetId;
            this.planetTypeName = planetTypeName;
            this.terraformingPossible = terraformingPossible;
            this.baseWaterLevel = baseWaterLevel;
            this.conditionalWaterRequirements = conditionalWaterRequirements;
            Collections.sort(this.conditionalWaterRequirements, new Comparator<Pair<BoggledProjectRequirementsOR, Integer>>() {
                @Override
                public int compare(Pair<BoggledProjectRequirementsOR, Integer> p1, Pair<BoggledProjectRequirementsOR, Integer> p2) {
                    return p1.two.compareTo(p2.two);
                }
            });
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
        BoggledProjectRequirementsOR reqs = terraformingRequirements.get("colony_has_atmo_problem");
        if (reqs == null) {
            // Abundance of caution
            return false;
        }
        return reqs.checkRequirement(market);
    }

    public static String getTooltipProjectName(MarketAPI market, BoggledTerraformingProject currentProject) {
        if(currentProject == null) {
            return noneProjectId;
        }

        return currentProject.getProjectTooltip(boggledTools.getTokenReplacements(market));
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