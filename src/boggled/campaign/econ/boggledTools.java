package boggled.campaign.econ;

import boggled.campaign.econ.industries.BoggledCommonIndustry;
import boggled.scripts.*;
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
import com.fs.starfarer.campaign.*;
import com.fs.starfarer.loading.specs.PlanetSpec;
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;
import illustratedEntities.helper.ImageHandler;
import illustratedEntities.helper.Settings;
import illustratedEntities.helper.TextHandler;
import illustratedEntities.memory.ImageDataMemory;
import illustratedEntities.memory.TextDataEntry;
import illustratedEntities.memory.TextDataMemory;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class boggledTools {

    private static MarketAPI terraformingMenuTarget = null;

    public static void setTerraformingMenuTarget(MarketAPI market) {
        terraformingMenuTarget = market;
    }

    public static MarketAPI getTerraformingMenuTarget() {
        return terraformingMenuTarget;
    }

    public static void CheckSubmarketExists(String source, String submarketId) {
        for (SubmarketSpecAPI submarketSpec : Global.getSettings().getAllSubmarketSpecs()) {
            if (submarketSpec.getId().equals(submarketId)) {
                return;
            }
        }
        Global.getLogger(boggledTools.class).warn(source + ": Market condition ID '" + submarketId + "' doesn't exist");
    }

    public static void CheckMarketConditionExists(String source, String conditionId) {
        for (MarketConditionSpecAPI marketConditionSpec : Global.getSettings().getAllMarketConditionSpecs()) {
            if (marketConditionSpec.getId().equals(conditionId)) {
                return;
            }
        }
        Global.getLogger(boggledTools.class).warn(source + ": Condition ID '" + conditionId + "' doesn't exist");
    }

    public static void CheckCommodityExists(String source, String commodityId) {
        for (CommoditySpecAPI commoditySpec : Global.getSettings().getAllCommoditySpecs()) {
            if (commoditySpec.getId().equals(commodityId)) {
                return;
            }
        }
        Global.getLogger(boggledTools.class).warn(source + ": Commodity ID '" + commodityId + "' doesn't exist");
    }

    public static void CheckSpecialItemExists(String source, String specialItemId) {
        for (SpecialItemSpecAPI itemSpec : Global.getSettings().getAllSpecialItemSpecs()) {
            if (itemSpec.getId().equals(specialItemId)) {
                return;
            }
        }
        Global.getLogger(boggledTools.class).warn(source + ": Special Item ID '" + specialItemId + "' doesn't exist");
    }

    public static void CheckResourceExists(String source, String resourceId) {
        for (Map.Entry<String, List<String>> resourceProgression : getResourceProgressions().entrySet()) {
            if (resourceProgression.getKey().equals(resourceId)) {
                return;
            }
        }
        Global.getLogger(boggledTools.class).warn(source + ": Resource ID '" + resourceId + "' doesn't exist");
    }

    public static void CheckPlanetSpecExists(String source, String planet) {
        for (PlanetSpecAPI planetSpec : Global.getSettings().getAllPlanetSpecs()) {
            if (planetSpec.getPlanetType().equals(planet)) {
                return;
            }
        }
        Global.getLogger(boggledTools.class).warn(source + ": Planet type '" + planet + "' doesn't exist");
    }

    public static void CheckPlanetTypeExists(String source, String planet) {
        for (Map.Entry<String, PlanetType> planetType : planetTypesMap.entrySet()) {
            if (planetType.getValue().getPlanetId().equals(planet)) {
                return;
            }
        }
        Global.getLogger(boggledTools.class).warn(source + ": Planet type '" + planet + "' doesn't exist");
    }

    public static void CheckIndustryExists(String source, String industryId) {
        IndustrySpecAPI industrySpec = Global.getSettings().getIndustrySpec(industryId);
        if (industrySpec != null) {
            return;
        }
        Global.getLogger(boggledTools.class).warn(source + ": Industry ID '" + industryId + "' doesn't exist");
    }

    public static void CheckItemExists(String source, String itemId) {
        for (SpecialItemSpecAPI specialItemSpec : Global.getSettings().getAllSpecialItemSpecs()) {
            if (specialItemSpec.getId().equals(itemId)) {
                return;
            }
        }
        Global.getLogger(boggledTools.class).warn(source + ": Item ID '" + itemId + "' doesn't exist");
    }

    public static void CheckSkillExists(String source, String skillId) {
        for (String skillSpecId : Global.getSettings().getSkillIds()) {
            if (skillSpecId.equals(skillId)) {
                return;
            }
        }
        Global.getLogger(boggledTools.class).warn(source + ": Skill ID '" + skillId + "' doesn't exist");
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

        public static final String addDomainTechBuildingsToVanillaColonies = "boggledAddDomainTechBuildingsToVanillaColonies";
        public static final String cryosanctumReplaceEverywhere = "boggledCryosanctumReplaceEverywhere";

        // Building enables, checked in campaign.econ.industries.*
        // May move them to a CSV later

        public static final String boggledDomainArchaeologyEnabled = "boggledDomainArchaeologyEnabled";
        public static final String boggledDomainTechContentEnabled = "boggledDomainTechContentEnabled";
        public static final String enableAIMiningDronesStructure = "boggledEnableAIMiningDronesStructure";
        public static final String domainTechContentEnabled = "boggledDomainTechContentEnabled";
        public static final String domainTechCraftingEnabled = "boggledDomainTechCraftingEnabled";
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
        public static final String constructionRequiredDays = "boggled_construction_required_days_";
        public static final String constructionProgressDays = "boggled_construction_progress_days_";
        public static final String constructionProgressLastDayChecked = "boggled_construction_progress_lastDayChecked_";

        public static final String stationNamePrefix = "boggled_station_name_";

        public static final String terraformingController = "boggledTerraformingController";

        public static final String stationGreekLetterPrefix = "boggled_greek_";

        public static final String astropolisStation = "boggled_astropolis";
        public static final String miningStation = "boggled_mining";
        public static final String siphonStation = "boggled_siphon";

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

    public static class BoggledIndustries {
        public static final String cryosanctumIndustryId = "BOGGLED_CRYOSANCTUM";
        public static final String domainArchaeologyIndustryId = "BOGGLED_DOMAIN_ARCHAEOLOGY";
        public static final String genelabIndustryId = "BOGGLED_GENELAB";
        public static final String remnantStationIndustryId = "BOGGLED_REMNANT_STATION";
        public static final String stellarReflectorArrayIndustryId = "BOGGLED_STELLAR_REFLECTOR_ARRAY";
        public static final String domedCitiesIndustryId = "BOGGLED_DOMED_CITIES";
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
        public static final String spriteControllerConditionId = "sprite_controller";

        public static final String crampedQuartersConditionId = "cramped_quarters";
    }
    // A mistyped string compiles fine and leads to plenty of debugging. A mistyped constant gives an error.

    public static final String csvOptionSeparator = "\\s*\\|\\s*";
    public static final String noneProjectId = "None";

    public static final HashMap<String, BoggledTerraformingRequirementFactory.TerraformingRequirementFactory> terraformingRequirementFactories = new HashMap<>();
    public static final HashMap<String, BoggledTerraformingDurationModifierFactory.TerraformingDurationModifierFactory> terraformingDurationModifierFactories = new HashMap<>();
    public static final HashMap<String, BoggledTerraformingProjectEffectFactory.TerraformingProjectEffectFactory> terraformingProjectEffectFactories = new HashMap<>();
    public static Map<String, BoggledStationConstructionFactory.StationConstructionFactory> stationConstructionFactories = new HashMap<>();

    public static Set<String> aotdIgnoreSettings = new HashSet<>();

    public static void initialiseModIgnoreSettings() {
        aotdIgnoreSettings.add("boggledTerraformingContentEnabled");
        aotdIgnoreSettings.add("boggledTerraformingRemoveRadiationProjectEnabled");
        aotdIgnoreSettings.add("boggledTerraformingRemoveAtmosphereProjectEnabled");

        aotdIgnoreSettings.add("boggledStellarReflectorArrayEnabled");
        aotdIgnoreSettings.add("boggledGenelabEnabled");
        aotdIgnoreSettings.add("boggledMesozoicParkEnabled");
        aotdIgnoreSettings.add("boggledDomedCitiesEnabled");
        aotdIgnoreSettings.add("boggledStationConstructionContentEnabled");

        aotdIgnoreSettings.add("boggledAstropolisEnabled");
        aotdIgnoreSettings.add("boggledMiningStationEnabled");
        aotdIgnoreSettings.add("boggledSiphonStationEnabled");
        aotdIgnoreSettings.add("boggledStationColonizationEnabled");

        aotdIgnoreSettings.add("boggledDomainTechContentEnabled");
        aotdIgnoreSettings.add("boggledDomainArchaeologyEnabled");
        aotdIgnoreSettings.add("boggledKletkaSimulatorEnabled");
        aotdIgnoreSettings.add("boggledCHAMELEONEnabled");
        aotdIgnoreSettings.add("boggledLimelightNetworkPlayerBuildEnabled");
        aotdIgnoreSettings.add("boggledRemnantStationEnabled");
    }

    @Nullable
    public static BoggledTerraformingRequirement.TerraformingRequirement getTerraformingRequirement(String terraformingRequirementType, String id, String[] enableSettings, boolean invert, String data) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledTerraformingRequirementFactory.TerraformingRequirementFactory factory = terraformingRequirementFactories.get(terraformingRequirementType);
        if (factory == null) {
            log.error("Requirement " + id + " of type " + terraformingRequirementType + " has no assigned factory");
            return null;
        }
        BoggledTerraformingRequirement.TerraformingRequirement req = factory.constructFromJSON(id, enableSettings, invert, data);
        if (req == null) {
            log.error("Requirement " + id + " of type " + terraformingRequirementType + " was null when created with data " + data);
        }
        return req;
    }

    @Nullable
    public static BoggledProjectRequirementsOR getTerraformingRequirements(String terraformingRequirementId, String type, String id) {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledProjectRequirementsOR req = terraformingRequirements.get(terraformingRequirementId);
        if (req == null) {
            log.error(type + " " + id + " has invalid requirement " + terraformingRequirementId);
        }
        return req;
    }

    @Nullable
    public static BoggledTerraformingDurationModifier.TerraformingDurationModifier getDurationModifier(String durationModifierType, String id, String[] enableSettings, String data) {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledTerraformingDurationModifierFactory.TerraformingDurationModifierFactory factory = terraformingDurationModifierFactories.get(durationModifierType);
        if (factory == null) {
            log.error("Duration modifier " + id + " of type " + durationModifierType + " has no assigned factory");
            return null;
        }
        BoggledTerraformingDurationModifier.TerraformingDurationModifier mod = factory.constructFromJSON(id, enableSettings, data);
        if (mod == null) {
            log.error("Duration modifier " + id + " of type " + durationModifierType + " was null when created with data " + data);
            return null;
        }
        return mod;
    }

    @Nullable
    public static BoggledTerraformingProjectEffect.TerraformingProjectEffect getProjectEffect(String[] enableSettings, String projectEffectType, String id, String data) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        BoggledTerraformingProjectEffectFactory.TerraformingProjectEffectFactory factory = terraformingProjectEffectFactories.get(projectEffectType);
        if (factory == null) {
            log.error("Terraforming project effect " + id + " of type " + projectEffectType + " has no assigned factory");
            return null;
        }
        BoggledTerraformingProjectEffect.TerraformingProjectEffect effect = factory.constructFromJSON(id, enableSettings, data);
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

    public static String doTokenAndFormatReplacement(String replace, Map<String, String> tokenReplacements) {
        String ret = doTokenReplacement(replace, tokenReplacements);
        return ret.replace("%", "%%");
    }

    public static String doTokenReplacement(String replace, Map<String, String> tokenReplacements) {
        for (Map.Entry<String, String> replacement : tokenReplacements.entrySet()) {
            replace = replace.replaceAll("(?!\\b)" + Pattern.quote(replacement.getKey()) + "(?=\\b)", replacement.getValue());
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

    private static BoggledCommonIndustry.TooltipData buildUnavailableReason(@NotNull List<BoggledTerraformingProject.ProjectInstance> projects, BoggledTerraformingRequirement.RequirementContext ctx, Map<String, String> tokenReplacements) {
        List<BoggledCommonIndustry.TooltipData> tooltips = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        List<Color> highlightColors = new ArrayList<>();
        List<String> highlights = new ArrayList<>();
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            for (BoggledProjectRequirementsAND.RequirementAndThen req : project.getProject().getRequirements()) {
                tooltips.addAll(req.getTooltip(ctx, tokenReplacements, true, false));
            }
        }

        for (BoggledCommonIndustry.TooltipData tooltip : tooltips) {
            if (tooltip.text.isEmpty()) {
                continue;
            }
            if (builder.length() != 0) {
                builder.append("\n");
            }
            builder.append(tooltip.text);
            highlightColors.addAll(tooltip.highlightColors);
            highlights.addAll(tooltip.highlights);
        }

        return new BoggledCommonIndustry.TooltipData(builder.toString(), highlightColors, highlights);
    }

    @NotNull
    public static BoggledCommonIndustry.TooltipData getUnavailableReason(List<BoggledTerraformingProject.ProjectInstance> projects, String industry, BoggledTerraformingRequirement.RequirementContext ctx, Map<String, String> tokenReplacements) {
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            if (!boggledTools.optionsAllowThis(project.getProject().getEnableSettings())) {
                return new BoggledCommonIndustry.TooltipData("Error in getUnavailableReason() in " + industry + ". Please tell Boggled about this on the forums.");
            }
        }

        return buildUnavailableReason(projects, ctx, tokenReplacements);
    }

    public static void initialiseDefaultTerraformingRequirementFactories() {
        addTerraformingRequirementFactory("AlwaysTrue", new BoggledTerraformingRequirementFactory.AlwaysTrue());

        addTerraformingRequirementFactory("PlanetType", new BoggledTerraformingRequirementFactory.PlanetType());
        addTerraformingRequirementFactory("MarketHasCondition", new BoggledTerraformingRequirementFactory.MarketHasCondition());
        addTerraformingRequirementFactory("MarketConditionSuppressed", new BoggledTerraformingRequirementFactory.MarketConditionSuppressed());
        addTerraformingRequirementFactory("MarketHasIndustry", new BoggledTerraformingRequirementFactory.MarketHasIndustry());
        addTerraformingRequirementFactory("MarketHasIndustryWithItem", new BoggledTerraformingRequirementFactory.MarketHasIndustryWithItem());
        addTerraformingRequirementFactory("MarketHasIndustryWithAICore", new BoggledTerraformingRequirementFactory.MarketHasIndustryWithAICore());
        addTerraformingRequirementFactory("IndustryHasShortage", new BoggledTerraformingRequirementFactory.IndustryHasShortage());
        addTerraformingRequirementFactory("PlanetWaterLevel", new BoggledTerraformingRequirementFactory.PlanetWaterLevel());
        addTerraformingRequirementFactory("MarketHasWaterPresent", new BoggledTerraformingRequirementFactory.MarketHasWaterPresent());
        addTerraformingRequirementFactory("MarketIsAtLeastSize", new BoggledTerraformingRequirementFactory.MarketIsAtLeastSize());
        addTerraformingRequirementFactory("StationMarketIsExactlySize", new BoggledTerraformingRequirementFactory.StationMarketIsExactlySize());
        addTerraformingRequirementFactory("TerraformingPossibleOnMarket", new BoggledTerraformingRequirementFactory.TerraformingPossibleOnMarket());
        addTerraformingRequirementFactory("MarketHasTags", new BoggledTerraformingRequirementFactory.MarketHasTags());
        addTerraformingRequirementFactory("MarketStorageContainsAtLeast", new BoggledTerraformingRequirementFactory.MarketStorageContainsAtLeast());
        addTerraformingRequirementFactory("FleetStorageContainsAtLeast", new BoggledTerraformingRequirementFactory.FleetStorageContainsAtLeast());
        addTerraformingRequirementFactory("FleetTooCloseToJumpPoint", new BoggledTerraformingRequirementFactory.FleetTooCloseToJumpPoint());
        addTerraformingRequirementFactory("PlayerHasStoryPointsAtLeast", new BoggledTerraformingRequirementFactory.PlayerHasStoryPointsAtLeast());
        addTerraformingRequirementFactory("WorldTypeSupportsResourceImprovement", new BoggledTerraformingRequirementFactory.WorldTypeSupportsResourceImprovement());

        addTerraformingRequirementFactory("FocusPlanetType", new BoggledTerraformingRequirementFactory.FocusPlanetType());
        addTerraformingRequirementFactory("FocusObjectIsPlanet", new BoggledTerraformingRequirementFactory.FocusObjectIsPlanet());
        addTerraformingRequirementFactory("FocusMarketHasCondition", new BoggledTerraformingRequirementFactory.FocusMarketHasCondition());
        addTerraformingRequirementFactory("FocusMarketConditionSuppressed", new BoggledTerraformingRequirementFactory.FocusMarketConditionSuppressed());

        addTerraformingRequirementFactory("IntegerFromMarketTagSubstring", new BoggledTerraformingRequirementFactory.IntegerFromMarketTagSubstring());

        addTerraformingRequirementFactory("PlayerHasSkill", new BoggledTerraformingRequirementFactory.PlayerHasSkill());

        addTerraformingRequirementFactory("SystemStarHasTags", new BoggledTerraformingRequirementFactory.SystemStarHasTags());
        addTerraformingRequirementFactory("SystemStarType", new BoggledTerraformingRequirementFactory.SystemStarType());

        addTerraformingRequirementFactory("FleetInHyperspace", new BoggledTerraformingRequirementFactory.FleetInHyperspace());
        addTerraformingRequirementFactory("SystemHasJumpPoints", new BoggledTerraformingRequirementFactory.SystemHasJumpPoints());
        addTerraformingRequirementFactory("SystemHasPlanets", new BoggledTerraformingRequirementFactory.SystemHasPlanets());
        addTerraformingRequirementFactory("SystemHasStations", new BoggledTerraformingRequirementFactory.SystemHasStations());
        addTerraformingRequirementFactory("TargetPlanetOwnedBy", new BoggledTerraformingRequirementFactory.TargetPlanetOwnedBy());
        addTerraformingRequirementFactory("TargetStationOwnedBy", new BoggledTerraformingRequirementFactory.TargetStationOwnedBy());
        addTerraformingRequirementFactory("TargetPlanetGovernedByPlayer", new BoggledTerraformingRequirementFactory.TargetPlanetGovernedByPlayer());
        addTerraformingRequirementFactory("TargetPlanetWithinDistance", new BoggledTerraformingRequirementFactory.TargetPlanetWithinDistance());
        addTerraformingRequirementFactory("TargetStationWithinDistance", new BoggledTerraformingRequirementFactory.TargetStationWithinDistance());
        addTerraformingRequirementFactory("TargetStationColonizable", new BoggledTerraformingRequirementFactory.TargetStationColonizable());
        addTerraformingRequirementFactory("TargetPlanetIsAtLeastSize", new BoggledTerraformingRequirementFactory.TargetPlanetIsAtLeastSize());
        addTerraformingRequirementFactory("TargetPlanetOrbitFocusWithinDistance", new BoggledTerraformingRequirementFactory.TargetPlanetOrbitFocusWithinDistance());
        addTerraformingRequirementFactory("TargetPlanetStarWithinDistance", new BoggledTerraformingRequirementFactory.TargetPlanetStarWithinDistance());
        addTerraformingRequirementFactory("TargetPlanetOrbitersWithinDistance", new BoggledTerraformingRequirementFactory.TargetPlanetOrbitersWithinDistance());
        addTerraformingRequirementFactory("TargetPlanetMoonCountLessThan", new BoggledTerraformingRequirementFactory.TargetPlanetMoonCountLessThan());
        addTerraformingRequirementFactory("TargetPlanetOrbitersTooClose", new BoggledTerraformingRequirementFactory.TargetPlanetOrbitersTooClose());
        addTerraformingRequirementFactory("TargetPlanetStationCountLessThan", new BoggledTerraformingRequirementFactory.TargetPlanetStationCountLessThan());
        addTerraformingRequirementFactory("TargetSystemStationCountLessThan", new BoggledTerraformingRequirementFactory.TargetSystemStationCountLessThan());

        addTerraformingRequirementFactory("FleetInAsteroidBelt", new BoggledTerraformingRequirementFactory.FleetInAsteroidBelt());
        addTerraformingRequirementFactory("FleetInAsteroidField", new BoggledTerraformingRequirementFactory.FleetInAsteroidField());

        addTerraformingRequirementFactory("TargetPlanetStoryCritical", new BoggledTerraformingRequirementFactory.TargetPlanetStoryCritical());
        addTerraformingRequirementFactory("TargetStationStoryCritical", new BoggledTerraformingRequirementFactory.TargetStationStoryCritical());

        addTerraformingRequirementFactory("BooleanSettingIsTrue", new BoggledTerraformingRequirementFactory.BooleanSettingIsTrue());

        addTerraformingRequirementFactory("AOTDResearchRequirement", new BoggledTerraformingRequirementFactory.AOTDResearchRequirementFactory());
    }

    public static void addTerraformingRequirementFactory(String key, BoggledTerraformingRequirementFactory.TerraformingRequirementFactory value) {
        Global.getLogger(boggledTools.class).info("Adding terraforming requirement factory " + key);
        terraformingRequirementFactories.put(key, value);
    }

    public static void initialiseDefaultTerraformingDurationModifierFactories() {
        addTerraformingDurationModifierFactory("PlanetSize", new BoggledTerraformingDurationModifierFactory.PlanetSize());

        addTerraformingDurationModifierFactory("DurationSettingModifier", new BoggledTerraformingDurationModifierFactory.DurationSettingModifier());
    }

    public static void addTerraformingDurationModifierFactory(String key, BoggledTerraformingDurationModifierFactory.TerraformingDurationModifierFactory value) {
        Global.getLogger(boggledTools.class).info("Adding terraforming duration modifier factory " + key);
        terraformingDurationModifierFactories.put(key, value);
    }

    public static void initialiseDefaultTerraformingProjectEffectFactories() {
        addTerraformingProjectEffectFactory("PlanetTypeChange", new BoggledTerraformingProjectEffectFactory.PlanetTypeChange());
        addTerraformingProjectEffectFactory("IndustrySwap", new BoggledTerraformingProjectEffectFactory.IndustrySwap());
        addTerraformingProjectEffectFactory("MarketAddCondition", new BoggledTerraformingProjectEffectFactory.MarketAddCondition());
        addTerraformingProjectEffectFactory("MarketAddConditionNoRemove", new BoggledTerraformingProjectEffectFactory.MarketAddConditionNoRemove());
        addTerraformingProjectEffectFactory("MarketRemoveCondition", new BoggledTerraformingProjectEffectFactory.MarketRemoveCondition());
        addTerraformingProjectEffectFactory("MarketOptionalCondition", new BoggledTerraformingProjectEffectFactory.MarketOptionalCondition());
        addTerraformingProjectEffectFactory("MarketProgressResource", new BoggledTerraformingProjectEffectFactory.MarketProgressResource());

        addTerraformingProjectEffectFactory("FocusMarketAddCondition", new BoggledTerraformingProjectEffectFactory.FocusMarketAddCondition());
        addTerraformingProjectEffectFactory("FocusMarketRemoveCondition", new BoggledTerraformingProjectEffectFactory.FocusMarketRemoveCondition());
        addTerraformingProjectEffectFactory("FocusMarketProgressResource", new BoggledTerraformingProjectEffectFactory.FocusMarketProgressResource());
        addTerraformingProjectEffectFactory("FocusMarketAndSiphonStationProgressResource", new BoggledTerraformingProjectEffectFactory.FocusMarketAndSiphonStationProgressResource());

        addTerraformingProjectEffectFactory("MarketRemoveIndustry", new BoggledTerraformingProjectEffectFactory.MarketRemoveIndustry());

        addTerraformingProjectEffectFactory("RemoveItemFromSubmarket", new BoggledTerraformingProjectEffectFactory.RemoveItemFromSubmarket());
        addTerraformingProjectEffectFactory("RemoveItemFromFleetStorage", new BoggledTerraformingProjectEffectFactory.RemoveItemFromFleetStorage());
        addTerraformingProjectEffectFactory("RemoveStoryPointsFromPlayer", new BoggledTerraformingProjectEffectFactory.RemoveStoryPointsFromPlayer());
        addTerraformingProjectEffectFactory("AddItemToSubmarket", new BoggledTerraformingProjectEffectFactory.AddItemToSubmarket());

        addTerraformingProjectEffectFactory("AddStationToOrbit", new BoggledTerraformingProjectEffectFactory.AddStationToOrbit());
        addTerraformingProjectEffectFactory("AddStationToAsteroids", new BoggledTerraformingProjectEffectFactory.AddStationToAsteroids());

        addTerraformingProjectEffectFactory("ColonizeAbandonedStation", new BoggledTerraformingProjectEffectFactory.ColonizeAbandonedStation());

        addTerraformingProjectEffectFactory("EffectWithRequirement", new BoggledTerraformingProjectEffectFactory.EffectWithRequirement());

        addTerraformingProjectEffectFactory("AdjustRelationsWith", new BoggledTerraformingProjectEffectFactory.AdjustRelationsWith());
        addTerraformingProjectEffectFactory("AdjustRelationsWithAllExcept", new BoggledTerraformingProjectEffectFactory.AdjustRelationsWithAllExcept());
        addTerraformingProjectEffectFactory("TriggerMilitaryResponse", new BoggledTerraformingProjectEffectFactory.TriggerMilitaryResponse());
        addTerraformingProjectEffectFactory("DecivilizeMarket", new BoggledTerraformingProjectEffectFactory.DecivilizeMarket());

        addTerraformingProjectEffectFactory("ModifyPatherInterest", new BoggledTerraformingProjectEffectFactory.ModifyPatherInterest());
        addTerraformingProjectEffectFactory("ModifyColonyGrowthRate", new BoggledTerraformingProjectEffectFactory.ModifyColonyGrowthRate());
        addTerraformingProjectEffectFactory("ModifyColonyGroundDefense", new BoggledTerraformingProjectEffectFactory.ModifyColonyGroundDefense());
        addTerraformingProjectEffectFactory("ModifyColonyAccessibility", new BoggledTerraformingProjectEffectFactory.ModifyColonyAccessibility());
        addTerraformingProjectEffectFactory("ModifyColonyStability", new BoggledTerraformingProjectEffectFactory.ModifyColonyStability());
        addTerraformingProjectEffectFactory("ModifyIndustryUpkeep", new BoggledTerraformingProjectEffectFactory.ModifyIndustryUpkeep());
        addTerraformingProjectEffectFactory("ModifyIndustryIncome", new BoggledTerraformingProjectEffectFactory.ModifyIndustryIncome());
        addTerraformingProjectEffectFactory("ModifyIndustryIncomeByAccessibility", new BoggledTerraformingProjectEffectFactory.ModifyIndustryIncomeByAccessibility());
        addTerraformingProjectEffectFactory("ModifyIndustrySupplyWithDeficit", new BoggledTerraformingProjectEffectFactory.ModifyIndustrySupplyWithDeficit());
        addTerraformingProjectEffectFactory("ModifyIndustryDemand", new BoggledTerraformingProjectEffectFactory.ModifyIndustryDemand());

        addTerraformingProjectEffectFactory("EffectToIndustry", new BoggledTerraformingProjectEffectFactory.EffectToIndustry());

        addTerraformingProjectEffectFactory("SuppressConditions", new BoggledTerraformingProjectEffectFactory.SuppressConditions());

        addTerraformingProjectEffectFactory("IndustryMonthlyItemProduction", new BoggledTerraformingProjectEffectFactory.IndustryMonthlyItemProduction());
        addTerraformingProjectEffectFactory("IndustryMonthlyItemProductionChance", new BoggledTerraformingProjectEffectFactory.IndustryMonthlyItemProductionChance());
        addTerraformingProjectEffectFactory("IndustryMonthlyItemProductionChanceModifier", new BoggledTerraformingProjectEffectFactory.IndustryMonthlyItemProductionChanceModifier());

        addTerraformingProjectEffectFactory("StepTag", new BoggledTerraformingProjectEffectFactory.StepTag());
        addTerraformingProjectEffectFactory("IndustryRemove", new BoggledTerraformingProjectEffectFactory.IndustryRemove());
        addTerraformingProjectEffectFactory("TagSubstringPowerModifyBuildCost", new BoggledTerraformingProjectEffectFactory.TagSubstringPowerModifyBuildCost());
        addTerraformingProjectEffectFactory("EliminatePatherInterest", new BoggledTerraformingProjectEffectFactory.EliminatePatherInterest());

        addTerraformingProjectEffectFactory("CommodityDemandFlat", new BoggledTerraformingProjectEffectFactory.CommodityDemandFlat());
        addTerraformingProjectEffectFactory("CommodityDemandMarketSize", new BoggledTerraformingProjectEffectFactory.CommodityDemandMarketSize());
        addTerraformingProjectEffectFactory("CommodityDemandPlayerMarketSizeElseFlat", new BoggledTerraformingProjectEffectFactory.CommodityDemandPlayerMarketSizeElseFlat());

        addTerraformingProjectEffectFactory("CommoditySupplyFlat", new BoggledTerraformingProjectEffectFactory.CommoditySupplyFlat());
        addTerraformingProjectEffectFactory("CommoditySupplyMarketSize", new BoggledTerraformingProjectEffectFactory.CommoditySupplyMarketSize());

        addTerraformingProjectEffectFactory("CommodityDeficitToProduction", new BoggledTerraformingProjectEffectFactory.CommodityDeficitToProduction());
        addTerraformingProjectEffectFactory("CommodityDeficitModifierToUpkeep", new BoggledTerraformingProjectEffectFactory.CommodityDeficitModifierToUpkeep());

        addTerraformingProjectEffectFactory("AttachProjectToIndustry", new BoggledTerraformingProjectEffectFactory.AttachProjectToIndustry());
    }

    public static void addTerraformingProjectEffectFactory(String key, BoggledTerraformingProjectEffectFactory.TerraformingProjectEffectFactory value) {
        Global.getLogger(boggledTools.class).info("Adding terraforming project effect factory " + key);
        terraformingProjectEffectFactories.put(key, value);
    }

    public static void initialiseDefaultStationConstructionFactories() {
        addStationConstructionFactory(boggledTools.BoggledTags.astropolisStation, new BoggledStationConstructionFactory.AstropolisConstructionFactory());
        addStationConstructionFactory(boggledTools.BoggledTags.miningStation, new BoggledStationConstructionFactory.MiningStationConstructionFactory());
        addStationConstructionFactory(boggledTools.BoggledTags.siphonStation, new BoggledStationConstructionFactory.SiphonStationConstructionFactory());
    }

    public static void addStationConstructionFactory(String key, BoggledStationConstructionFactory.StationConstructionFactory value) {
        Global.getLogger(boggledTools.class).info("Adding station construction factory " + key);
        stationConstructionFactories.put(key, value);
    }

    @NotNull
    private static List<String> arrayListFromJSON(@NotNull JSONObject data, String key, String regex) throws JSONException {
        String toSplit = data.getString(key);
        if (toSplit.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(toSplit.split(regex)));
    }

    public static BoggledProjectRequirementsAND.RequirementAndThen requirementFromRequirementObject(JSONObject requirementObject, String sourceInfo, String id, String key) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);
        String requirementsString = requirementObject.getString("requirement_id");

        JSONArray andThenArray = requirementObject.optJSONArray("and_then");
        BoggledProjectRequirementsAND andThen = null;
        if (andThenArray != null) {
            andThen = requirementsFromRequirementsArray(andThenArray, sourceInfo, id, key);
        }

        BoggledProjectRequirementsOR req = terraformingRequirements.get(requirementsString);
        if (req == null) {
            log.info(sourceInfo + " " + id + " has invalid " + key + " " + requirementsString);
            return null;
        }
        return new BoggledProjectRequirementsAND.RequirementAndThen(req, andThen);
    }

    public static BoggledProjectRequirementsAND requirementsFromRequirementsArray(JSONArray requirementArray, String sourceInfo, String id, String key) throws JSONException {
        if (requirementArray == null) {
            return new BoggledProjectRequirementsAND();
        }

        List<BoggledProjectRequirementsAND.RequirementAndThen> reqs = new ArrayList<>();
        for (int i = 0; i < requirementArray.length(); ++i) {
            JSONObject requirementObject = requirementArray.getJSONObject(i);

            BoggledProjectRequirementsAND.RequirementAndThen req = requirementFromRequirementObject(requirementObject, sourceInfo, id, key);

            if (req != null) {
                reqs.add(req);
            }
        }

        return new BoggledProjectRequirementsAND(reqs);
    }

    public static BoggledProjectRequirementsAND requirementsFromJSON(JSONObject object, String sourceInfo, String id, String key) throws JSONException {
        String requirementsArrayString = object.optString(key);
        if (!requirementsArrayString.isEmpty()) {
            JSONArray reqsArray = new JSONArray(requirementsArrayString);
            return requirementsFromRequirementsArray(reqsArray, sourceInfo, id, key);
        }
        return null;
    }

    public static BoggledProjectRequirementsAND requirementsFromJSONNeverNull(JSONObject object, String sourceInfo, String id, String key) throws JSONException {
        BoggledProjectRequirementsAND ret = requirementsFromJSON(object, sourceInfo, id, key);
        if (ret == null) {
            return new BoggledProjectRequirementsAND();
        }
        return ret;
    }

    public static List<BoggledTerraformingProject.RequirementsWithId> requirementsWithIdFromJSON(JSONObject object, String sourceInfo, String id, String key) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        List<BoggledTerraformingProject.RequirementsWithId> ret = new ArrayList<>();
        String requirementsString = object.getString(key);
        if (requirementsString.isEmpty()) {
            return ret;
        }

        JSONArray requirementsWithIdArray = new JSONArray(requirementsString);
        for (int i = 0; i < requirementsWithIdArray.length(); ++i) {
            JSONObject requirementsObject = requirementsWithIdArray.getJSONObject(i);

            String requirementsId = requirementsObject.getString("requirements_id");
            BoggledProjectRequirementsAND req = requirementsFromJSON(requirementsObject, sourceInfo, requirementsId, "requirements");
            if (req != null) {
                ret.add(new BoggledTerraformingProject.RequirementsWithId(requirementsId, req));
            } else {
                log.warn(sourceInfo + " " + id + " has empty invalid requirement " + requirementsId);
            }
        }

        return ret;
    }

    private static Map<String, List<BoggledTerraformingProject>> aiCoreEffectsFromJSON(JSONObject object, String sourceInfo, String id, String key) throws JSONException {
        Map<String, List<BoggledTerraformingProject>> ret = new HashMap<>();
        String aiCoreEffectsString = object.optString(key);
        if (aiCoreEffectsString.isEmpty()) {
            return ret;
        }

        JSONArray aiCoreEffectsArray = new JSONArray(aiCoreEffectsString);
        for (int i = 0; i < aiCoreEffectsArray.length(); ++i) {
            JSONObject aiCoreObject = aiCoreEffectsArray.getJSONObject(i);
            String aiCoreId = aiCoreObject.getString("ai_core_id");
            List<BoggledTerraformingProject> projects = projectsFromJSON(aiCoreObject, sourceInfo, id, "projects");
            List<BoggledTerraformingProject> entry = ret.get(aiCoreId);
            if (entry == null) {
                ret.put(aiCoreId, projects);
            } else {
                entry.addAll(projects);
            }
        }

        return ret;
    }

    private static Map<String, List<String>> aiCoreEffectRemoveInfoFromJSON(JSONObject object, String sourceInfo, String id, String key) throws JSONException {
        Map<String, List<String>> ret = new HashMap<>();
        String aiCoreEffectsRemovedString = object.optString(key);
        if (aiCoreEffectsRemovedString.isEmpty()) {
            return ret;
        }

        JSONObject aiCoreEffectsRemoved = new JSONObject(aiCoreEffectsRemovedString);
        for (Iterator<String> it = aiCoreEffectsRemoved.keys(); it.hasNext(); ) {
            String aiCoreId = it.next();
            JSONArray aiCoreEffectsArray = aiCoreEffectsRemoved.getJSONArray(aiCoreId);
            List<String> aiCoreEffects = stringListFromJSON(aiCoreEffectsArray);

            ret.put(aiCoreId, aiCoreEffects);
        }

        return ret;
    }

    public static void initialisePlanetTypesFromJSON(@NotNull JSONArray planetTypesJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, PlanetType> planetTypesMap = new HashMap<>();

        planetTypesMap.put(unknownPlanetId, new PlanetType(unknownPlanetId, "unknown", false, 0, new ArrayList<Pair<BoggledProjectRequirementsAND, Integer>>()));

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

                List<Pair<BoggledProjectRequirementsAND, Integer>> conditionalWaterRequirements = new ArrayList<>();
                String conditionalWaterLevelsString = row.getString("conditional_water_requirements");
                if (!conditionalWaterLevelsString.isEmpty()) {
                    JSONArray conditionalWaterLevels = new JSONArray(conditionalWaterLevelsString);
                    for (int j = 0; j < conditionalWaterLevels.length(); ++j) {
                        JSONObject conditionalWaterLevel = conditionalWaterLevels.getJSONObject(j);
                        BoggledProjectRequirementsAND waterRequirement = requirementsFromJSONNeverNull(conditionalWaterLevel, "Conditional Water Level", id, "requirements");
                        int waterLevel = conditionalWaterLevel.getInt("water_level");

                        conditionalWaterRequirements.add(new Pair<>(waterRequirement, waterLevel));
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

    public static void initializeStellarReflectorArraySuppressedConditionsFromJSON(@NotNull JSONArray stellarReflectorArraySuppressedConditionsJSON) {
        HashSet<String> conditions = getConditionsListFromJson(stellarReflectorArraySuppressedConditionsJSON);

        boggledTools.stellarReflectorArraySuppressedConditions = new ArrayList<String>(conditions);
    }

    public static void initializeDomedCitiesSuppressedConditionsFromJSON(@NotNull JSONArray domedCitiesSuppressedConditionsJSON) {
        HashSet<String> conditions = getConditionsListFromJson(domedCitiesSuppressedConditionsJSON);

        boggledTools.domedCitiesSuppressedConditions = new ArrayList<String>(conditions);
    }

    public static HashSet<String> getConditionsListFromJson(JSONArray json) {
        HashSet<String> conditionsSet = new HashSet<>();

        for (int i = 0; i < json.length(); ++i) {
            try {
                JSONObject row = json.getJSONObject(i);

                String condition_id = row.getString("condition_id");
                if (condition_id != null && !condition_id.isEmpty()) {
                    conditionsSet.add(condition_id);
                }
            } catch (JSONException e) {
                // We can't swallow this exception because the game won't work correctly if the data isn't loaded
                throw new RuntimeException("Error in condition list JSON parsing: " + e);
            }
        }

        return conditionsSet;
    }

    public static List<String> getDomedCitiesSuppressedConditions() {
        return boggledTools.domedCitiesSuppressedConditions;
    }

    public static List<String> getStellarReflectorArraySuppressedConditions() {
        return boggledTools.stellarReflectorArraySuppressedConditions;
    }

    public static void initialiseResourceProgressionsFromJSON(@NotNull JSONArray resourceProgressionsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, List<String>> resourceProgressions = new HashMap<>();

        for (int i = 0; i < resourceProgressionsJSON.length(); ++i) {
            try {
                JSONObject row = resourceProgressionsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                List<String> resourceProgression = stringListFromJSON(row, "resource_progression");

                resourceProgressions.put(id, resourceProgression);
            } catch (JSONException e) {
                log.error("Error in resource progressions: " + e);
            }
        }

        boggledTools.resourceProgressions = resourceProgressions;
    }

    public static void initialiseResourceLimitsFromJSON(@NotNull JSONArray resourceLimitsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        Map<String, Map<String, String>> resourceLimits = new HashMap<>();

        for (int i = 0; i < resourceLimitsJSON.length(); ++i) {
            try {
                JSONObject row = resourceLimitsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }

                Map<String, String> planetResourceLimits = resourceLimits.get(id);
                if (planetResourceLimits == null) {
                    planetResourceLimits = new HashMap<>();
                    resourceLimits.put(id, planetResourceLimits);
                }

                String resourcesMaxString = row.getString("resources_max");
                if (!resourcesMaxString.isEmpty()) {
                    JSONArray resourcesMax = new JSONArray(resourcesMaxString);
                    for (int j = 0; j < resourcesMax.length(); ++j) {
                        JSONObject resource = resourcesMax.getJSONObject(j);

                        String resourceId = resource.getString("resource_id");
                        String resourceMax = resource.getString("resource_max");

                        CheckResourceExists("Planet Max Resource " + id, resourceId);

                        planetResourceLimits.put(resourceId, resourceMax);
                    }
                }
            } catch (JSONException e) {
                log.error("Error in resource limits: " + e);
            }
        }

        boggledTools.resourceLimits = resourceLimits;
    }

    private static List<BoggledCommonIndustry.ImageOverrideWithRequirement> imageOverridesFromJSON(JSONObject object, String key) throws JSONException {
        List<BoggledCommonIndustry.ImageOverrideWithRequirement> ret = new ArrayList<>();

        String imageOverridesString = object.getString(key);
        if (!imageOverridesString.isEmpty()) {
            JSONArray imageOverridesJson = new JSONArray(imageOverridesString);
            for (int j = 0; j < imageOverridesJson.length(); ++j) {
                JSONObject imageOverride = imageOverridesJson.getJSONObject(j);
                String id = imageOverride.getString("id");

                JSONArray requirementsArray = imageOverride.getJSONArray("requirements");
                BoggledProjectRequirementsAND imageReqs = requirementsFromRequirementsArray(requirementsArray, "Industry Options", id, "image_overrides");

                String category = imageOverride.getString("category");
                String imageId = imageOverride.getString("image_id");

                ret.add(new BoggledCommonIndustry.ImageOverrideWithRequirement(id, imageReqs, category, imageId));
            }
        }

        return ret;
    }

    public static void initialiseIndustryOptionsFromJSON(@NotNull JSONArray industryOptionsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledCommonIndustry> industryProjects = new HashMap<>();
        String idForErrors = "";
        String stage = "";
        for (int i = 0; i < industryOptionsJSON.length(); ++i) {
            try {
                JSONObject row = industryOptionsJSON.getJSONObject(i);

                stage = "id";
                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                stage = "tooltip";
                String industry = row.getString("tooltip");

                stage = "projects";
                String[] projectStrings = row.getString("projects").split(boggledTools.csvOptionSeparator);

                ArrayList<BoggledTerraformingProject> projects = new ArrayList<>();
                for (String projectString : projectStrings) {
                    BoggledTerraformingProject project = boggledTools.getProject(projectString);
                    if (project != null) {
                        projects.add(project);
                    }
                }

                stage = "building_finished_effects";
                List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> buildingFinishedEffects = projectEffectsFromJSON(row, "Industry Options", id, "building_finished_effects");

                stage = "improve_effects";
                List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> improveEffects = projectEffectsFromJSON(row, "Industry Options", id, "improve_effects");

                stage = "pre_build_effects";
                List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> preBuildEffects = projectEffectsFromJSON(row, "Industry Options", id, "pre_build_effects");

                stage = "ai_core_effects";
                Map<String, List<BoggledTerraformingProject>> aiCoreEffects = aiCoreEffectsFromJSON(row, "AI Core Effects", id, "ai_core_effects");

                List<BoggledProjectRequirementsAND> disruptRequirements = new ArrayList<>();

                stage = "base_pather_interest";
                float basePatherInterest = (float) row.getDouble("base_pather_interest");

                stage = "image_overrides";
                List<BoggledCommonIndustry.ImageOverrideWithRequirement> imageOverrides = imageOverridesFromJSON(row, "image_overrides");

                industryProjects.put(id, new BoggledCommonIndustry(id, industry, projects, buildingFinishedEffects, improveEffects, aiCoreEffects, disruptRequirements, basePatherInterest, imageOverrides, preBuildEffects));
            } catch (JSONException e) {
                log.error("Error in industry options " + idForErrors + " at stage " + stage + ": " + e);
            }
        }
        boggledTools.industryProjects = industryProjects;
    }

    public static void initialiseTerraformingRequirementFromJSON(@NotNull JSONArray terraformingRequirementJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        terraformingRequirement = new HashMap<>();
        String idForErrors = "";
        for (int i = 0; i < terraformingRequirementJSON.length(); ++i) {
            try {
                JSONObject row = terraformingRequirementJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                String[] enableSettings = row.getString("enable_settings").split(csvOptionSeparator);

                String requirementType = row.getString("requirement_type");
                boolean invert = row.getBoolean("invert");
                String data = row.getString("data");

                BoggledTerraformingRequirement.TerraformingRequirement req = getTerraformingRequirement(requirementType, id, enableSettings, invert, data);
                if (req != null) {
                    terraformingRequirement.put(id, req);
                }
            } catch (JSONException e) {
                log.error("Error in terraforming requirement " + idForErrors + ": " + e);
            }
        }
    }

    public static void initialiseTerraformingRequirementsFromJSON(@NotNull JSONArray terraformingRequirementsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        HashMap<String, BoggledProjectRequirementsOR> terraformingRequirements = new HashMap<>();
        String idForErrors = "";
        for (int i = 0; i < terraformingRequirementsJSON.length(); ++i) {
            try {
                JSONObject row = terraformingRequirementsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                String tooltipText = row.getString("tooltip");
                List<String> tooltipHighlightText = stringListFromJSON(row, "tooltip_highlights");
                List<Color> tooltipHighlight = new ArrayList<>(tooltipHighlightText.size());
                for (String tt : tooltipHighlightText) {
                    tooltipHighlight.add(Misc.getHighlightColor());
                }

                BoggledCommonIndustry.TooltipData tooltip = new BoggledCommonIndustry.TooltipData(tooltipText, tooltipHighlight, tooltipHighlightText);

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
                log.error("Error in terraforming requirements " + idForErrors + ": " + e);
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

                String[] enableSettings = row.getString("enable_settings").split(csvOptionSeparator);

                String durationModifierType = row.getString("duration_modifier_type");
                String data = row.getString("data");

                BoggledTerraformingDurationModifier.TerraformingDurationModifier mod = getDurationModifier(durationModifierType, id, enableSettings, data);
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

        terraformingProjectEffects = new HashMap<>();
        String idForErrors = "";
        for (int i = 0; i < projectEffectsJSON.length(); ++i) {
            try {
                JSONObject row = projectEffectsJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                String[] enableSettings = row.getString("enable_settings").split(csvOptionSeparator);
                String projectEffectType = row.getString("effect_type");
                String data = row.getString("data");

                BoggledTerraformingProjectEffect.TerraformingProjectEffect projectEffect = getProjectEffect(enableSettings, projectEffectType, id, data);
                if (projectEffect != null) {
                    terraformingProjectEffects.put(id, projectEffect);
                }
            } catch (JSONException e) {
                log.error("Error in project effect " + idForErrors + ": " + e);
            }
        }
    }

    public static void initialiseTerraformingProjectsFromJSON(@NotNull JSONArray terraformingProjectsJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        boggledTools.terraformingProjects = new LinkedHashMap<>();
        String idForErrors = "";
        String stage = "";
        for (int i = 0; i < terraformingProjectsJSON.length(); ++i) {
            try {
                JSONObject row = terraformingProjectsJSON.getJSONObject(i);

                stage = "id";
                String id = row.getString("id");
                if (id == null || id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                stage = "enable_settings";
                String[] enableSettings = row.getString("enable_settings").split(csvOptionSeparator);

                stage = "project_type";
                String projectType = row.getString("project_type");

                stage = "tooltip";
                String tooltip = row.getString("tooltip");

                stage = "tooltip_highlights";
                List<String> tooltipHighlights = stringListFromJSON(row, "tooltip_highlights");

                stage = "intel_complete_message";
                String intelCompleteMessage = row.getString("intel_complete_message");

                stage = "incomplete_message";
                String incompleteMessage = row.getString("incomplete_message");

                stage = "incomplete_message_highlights";
                List<String> incompleteMessageHighlights = stringListFromJSON(row, "incomplete_message_highlights");

                stage = "disrupted_message";
                String disruptedMessage = row.getString("disrupted_message");

                stage = "disrupted_message_highlights";
                List<String> disruptedMessageHighlights = stringListFromJSON(row, "disrupted_message_highlights");

                stage = "requirements";
                BoggledProjectRequirementsAND requirements = requirementsFromJSONNeverNull(row, "Terraforming Projects", id, "requirements");

                stage = "requirements_hidden";
                BoggledProjectRequirementsAND requirementsHidden = requirementsFromJSONNeverNull(row, "Terraforming Projects", id, "requirements_hidden");

                stage = "requirements_stall";
                List<BoggledTerraformingProject.RequirementsWithId> requirementsStall = requirementsWithIdFromJSON(row, "Terraforming Projects", id, "requirements_stall");

                stage = "requirements_reset";
                List<BoggledTerraformingProject.RequirementsWithId> requirementsReset = requirementsWithIdFromJSON(row, "Terraforming Projects", id, "requirements_reset");

                stage = "base_project_duration";
                int baseProjectDuration = row.optInt("base_project_duration", 0);

                stage = "dynamic_project_duration_modifiers";
                List<BoggledTerraformingDurationModifier.TerraformingDurationModifier> terraformingDurationModifiers = durationModifiersFromJSON(row, "Terraforming project", id, "dynamic_project_duration_modifiers");

                stage = "project_complete_effects";
                List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectCompleteEffects = projectEffectsFromJSON(row, "Terraforming Projects", id, "project_complete_effects");

                stage = "project_ongoing_effects";
                List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectOngoingEffects = projectEffectsFromJSON(row, "Terraforming Projects", id, "project_ongoing_effects");

                BoggledTerraformingProject terraformingProj = new BoggledTerraformingProject(id, enableSettings, projectType, tooltip, intelCompleteMessage, incompleteMessage, incompleteMessageHighlights, disruptedMessage, disruptedMessageHighlights, requirements, requirementsHidden, requirementsStall, requirementsReset, baseProjectDuration, terraformingDurationModifiers, projectCompleteEffects, projectOngoingEffects);
                boggledTools.terraformingProjects.put(id, terraformingProj);
            } catch (JSONException e) {
                log.error("Error in terraforming projects " + idForErrors + " at stage " + stage + ": " + e);
            }
        }
    }

    private static List<BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiersFromJSON(JSONObject object, String sourceInfo, String id, String key) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        String projectDurationModifiersString = object.optString(key);
        List<BoggledTerraformingDurationModifier.TerraformingDurationModifier> ret = new ArrayList<>();
        if (!projectDurationModifiersString.isEmpty()) {
            JSONArray projectDurationModifiersArray = new JSONArray(projectDurationModifiersString);
            for (int i = 0; i < projectDurationModifiersArray.length(); ++i) {
                String durationModifiersId = projectDurationModifiersArray.getString(i);
                BoggledTerraformingDurationModifier.TerraformingDurationModifier mod = durationModifiers.get(durationModifiersId);
                if (mod == null) {
                    log.info(sourceInfo + " " + id + " has invalid dynamic project duration modifier " + durationModifiersId);
                } else {
                    ret.add(mod);
                }
            }
        }

        return ret;
    }

    private static List<BoggledTerraformingProject> projectsFromJSON(JSONObject object, String sourceInfo, String id, String key) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        String projectsString = object.optString(key);
        List<BoggledTerraformingProject> ret = new ArrayList<>();
        if (projectsString.isEmpty()) {
            return ret;
        }

        JSONArray projectsArray = new JSONArray(projectsString);
        for (int i = 0; i < projectsArray.length(); ++i) {
            String projectId = projectsArray.getString(i);
            BoggledTerraformingProject project = boggledTools.terraformingProjects.get(projectId);
            if (project != null) {
                ret.add(project);
            } else {
                log.info(sourceInfo + " " + id + " has invalid project " + projectId);
            }
        }

        return ret;
    }

    private static List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectEffectsFromJSON(JSONObject object, String sourceInfo, String id, String key) throws JSONException {
        Logger log = Global.getLogger(boggledTools.class);

        String projectEffectsString = object.optString(key);
        List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> ret = new ArrayList<>();
        if (projectEffectsString.isEmpty()) {
            return ret;
        }

        JSONArray projectEffectsArray = new JSONArray(projectEffectsString);
        for (int i = 0; i < projectEffectsArray.length(); ++i) {
            String effectId = projectEffectsArray.getString(i);
            BoggledTerraformingProjectEffect.TerraformingProjectEffect effect = boggledTools.terraformingProjectEffects.get(effectId);
            if (effect != null) {
                ret.add(effect);
            } else {
                log.info(sourceInfo + " " + id + " has invalid project effect " + effectId);
            }
        }

        return ret;
    }

    private static List<BoggledProjectRequirementsAND.RequirementAdd> requirementAddFromJSON(JSONObject object, String sourceInfo, String id, String requirementType) throws JSONException {
        List<BoggledProjectRequirementsAND.RequirementAdd> ret = new ArrayList<>();
        String requirementAddString = object.optString(requirementType);
        if (requirementAddString.isEmpty()) {
            return ret;
        }

        JSONArray requirementsAddedArray = new JSONArray(requirementAddString);
        for (int i = 0; i < requirementsAddedArray.length(); ++i) {
            JSONObject requirementObject = requirementsAddedArray.getJSONObject(i);
            BoggledProjectRequirementsAND.RequirementAndThen req = requirementFromRequirementObject(requirementObject, sourceInfo, id, requirementType);
            String parentId = requirementObject.optString("parent_id");
            if (req != null) {
                ret.add(new BoggledProjectRequirementsAND.RequirementAdd(req, parentId));
            }
        }
        return ret;
    }

    private static List<BoggledTerraformingProject.RequirementAddInfo> keyedRequirementAddFromJSON(JSONObject object, String sourceInfo, String id, String baseKey, String key) throws JSONException {
        List<BoggledTerraformingProject.RequirementAddInfo> ret = new ArrayList<>();
        String requirementAddString = object.optString(baseKey);
        if (requirementAddString.isEmpty()) {
            return ret;
        }
        JSONArray requirementAddArray = new JSONArray(requirementAddString);
        for (int i = 0; i < requirementAddArray.length(); ++i) {
            JSONObject requirementAddObject = requirementAddArray.getJSONObject(i);
            String containingId = requirementAddObject.getString("containing_id");
            List<BoggledProjectRequirementsAND.RequirementAdd> requirementsAdded = requirementAddFromJSON(requirementAddObject, sourceInfo, id, key);
            ret.add(new BoggledTerraformingProject.RequirementAddInfo(containingId, requirementsAdded));
        }

        return ret;
    }

    public static List<String> stringListFromJSON(JSONArray object) throws JSONException {
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < object.length(); ++i) {
            ret.add(object.getString(i));
        }
        return ret;
    }

    private static List<String> stringListFromJSON(JSONObject object, String key) throws JSONException {
        List<String> ret = new ArrayList<>();
        String jsonString = object.optString(key);
        if (jsonString.isEmpty()) {
            return ret;
        }
        return stringListFromJSON(new JSONArray(jsonString));
    }

    private static List<BoggledTerraformingProject.RequirementRemoveInfo> keyedRequirementRemoveFromJSON(JSONObject object, String baseKey) throws JSONException {
        List<BoggledTerraformingProject.RequirementRemoveInfo> ret = new ArrayList<>();
        String requirementRemoveString = object.optString(baseKey);
        if (requirementRemoveString.isEmpty()) {
            return ret;
        }
        JSONArray requirementRemoveArray = new JSONArray(requirementRemoveString);
        for (int i = 0; i < requirementRemoveArray.length(); ++i) {
            JSONObject requirementRemoveObject = requirementRemoveArray.getJSONObject(i);
            String containingId = requirementRemoveObject.getString("containing_id");
            List<String> requirementsRemoved = stringListFromJSON(requirementRemoveObject, "requirements_removed");
            ret.add(new BoggledTerraformingProject.RequirementRemoveInfo(containingId, requirementsRemoved));
        }
        return ret;
    }

    public static void initialiseTerraformingProjectOverrides(@NotNull JSONArray terraformingProjectsOverrideJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        try {
            for (int i = 0; i < terraformingProjectsOverrideJSON.length(); ++i) {
                JSONObject row = terraformingProjectsOverrideJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id == null || id.isEmpty() || id.equals("your_mod_id_goes_here")) {
                    continue;
                }

                String projectId = row.getString("project_id");
                BoggledTerraformingProject proj = terraformingProjects.get(projectId);
                if (proj == null) {
                    log.error("Mod " + id + " terraforming project " + projectId + " not found, ignoring");
                    continue;
                }

                List<BoggledProjectRequirementsAND.RequirementAdd> reqsAdded = requirementAddFromJSON(row, "Terraforming Project Mods", id, "requirements_added");
                List<String> reqsRemoved = stringListFromJSON(row, "requirements_removed");

                List<BoggledProjectRequirementsAND.RequirementAdd> reqsHiddenAdded = requirementAddFromJSON(row, "Terraforming Project Mods", id, "requirements_hidden_added");
                List<String> reqsHiddenRemoved = stringListFromJSON(row, "requirements_hidden_added");

                List<BoggledTerraformingProject.RequirementAddInfo> reqsStallAdded = keyedRequirementAddFromJSON(row, "Terraforming Project Mods", id, "requirements_stall_added", "requirements_added");
                List<BoggledTerraformingProject.RequirementRemoveInfo> reqsStallRemoved = keyedRequirementRemoveFromJSON(row, "requirements_stall_removed");

                List<BoggledTerraformingProject.RequirementAddInfo> reqsResetAdded = keyedRequirementAddFromJSON(row, "Terraforming Project Mods", id, "requirements_reset_added", "requirements_added");
                List<BoggledTerraformingProject.RequirementRemoveInfo> reqsResetRemoved = keyedRequirementRemoveFromJSON(row, "requirements_reset_removed");

                Integer baseProjectDurationOverride = null;
                String baseProjectDurationOverrideString = row.optString("base_project_duration_override");
                if (!baseProjectDurationOverrideString.isEmpty()) {
                    baseProjectDurationOverride = row.getInt("base_project_duration_override");
                }
                List<BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiersAdded = durationModifiersFromJSON(row, "Terraforming Project Mods", id, "dynamic_project_duration_modifiers_added");
                List<String> durationModifiersRemoved = stringListFromJSON(row, "dynamic_project_duration_modifiers_removed");

                List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectCompleteEffectsAdded = projectEffectsFromJSON(row, "Terraforming Project Mods", id, "project_complete_effects_added");
                List<String> projectCompleteEffectsRemoved = stringListFromJSON(row, "project_complete_effects_removed");

                List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectOngoingEffectsAdded = projectEffectsFromJSON(row, "Terraforming Project Mods", id, "project_ongoing_effects_added");
                List<String> projectOngoingEffectsRemoved = stringListFromJSON(row, "project_ongoing_effects_removed");

                proj.addRemoveProjectRequirements(reqsAdded, reqsRemoved, reqsHiddenAdded, reqsHiddenRemoved, reqsStallAdded, reqsStallRemoved, reqsResetAdded, reqsResetRemoved);
                proj.addRemoveDurationModifiersAndDuration(baseProjectDurationOverride, durationModifiersAdded, durationModifiersRemoved);
                proj.addRemoveProjectEffects(projectCompleteEffectsAdded, projectCompleteEffectsRemoved, projectOngoingEffectsAdded, projectOngoingEffectsRemoved);
            }
        } catch (JSONException e) {
            log.error("Error in terraforming projects overrides: " + e);
        }
    }

    public static void initialiseIndustryOptionOverrides(@NotNull JSONArray industryOptionOverridesJSON) {
        Logger log = Global.getLogger(boggledTools.class);

        String idForErrors = "";
        for (int i = 0; i < industryOptionOverridesJSON.length(); ++i) {
            try {
                JSONObject row = industryOptionOverridesJSON.getJSONObject(i);

                String id = row.getString("id");
                if (id.isEmpty()) {
                    continue;
                }
                idForErrors = id;

                String industryId = row.getString("industry_id");
                BoggledCommonIndustry industry = industryProjects.get(industryId);
                if (industry == null) {
                    log.error("Industry option override " + idForErrors + " has invalid industry ID " + industryId);
                    continue;
                }

                List<BoggledTerraformingProject> projectsAdded = projectsFromJSON(row, "Industry Option Mods", id, "projects_added");
                List<String> projectsRemoved = stringListFromJSON(row, "projects_removed");

                List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> buildingFinishedEffectsAdded = projectEffectsFromJSON(row, "Industry Option Mods", id, "building_finished_effects_added");
                List<String> buildingFinishedEffectsRemoved = stringListFromJSON(row, "building_finished_effects_removed");

                List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> improveEffectsAdded = projectEffectsFromJSON(row, "Industry Option Mods", id, "improve_effects_added");
                List<String> improveEffectsRemoved = stringListFromJSON(row, "improve_effects_removed");

                Map<String, List<BoggledTerraformingProject>> aiCoreEffectsAdded = aiCoreEffectsFromJSON(row, "Industry Option Mods", id, "ai_core_effects_added");
                Map<String, List<String>> aiCoreEffectsRemoved = aiCoreEffectRemoveInfoFromJSON(row, "Industry Option Mods", id, "ai_core_effects_removed");

                String canBeDisruptedOverrideString = row.getString("can_be_disrupted_override");
                boolean canBeDisruptedOverride = industry.canBeDisrupted();
                if (!canBeDisruptedOverrideString.isEmpty()) {
                    canBeDisruptedOverride = row.getBoolean("can_be_disrupted_override");
                }
                String basePatherInterestOverrideString = row.getString("base_pather_interest_override");
                float basePatherInterestOverride = industry.getBasePatherInterest();
                if (!basePatherInterestOverrideString.isEmpty()) {
                    basePatherInterestOverride = (float) row.getDouble("base_pather_interest_override");
                }

                List<BoggledCommonIndustry.ImageOverrideWithRequirement> imageOverridesAdded = imageOverridesFromJSON(row, "image_overrides_added");
                List<String> imageOverridesRemoved = stringListFromJSON(row, "image_overrides_removed");

                List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> preBuildEffectsAdded = projectEffectsFromJSON(row, "Industry Option Mods", id, "pre_build_effects_added");
                List<String> preBuildEffectsRemoved = stringListFromJSON(row, "pre_build_effects_removed");

                industry.addRemoveProjects(projectsAdded, projectsRemoved);
                industry.addRemoveBuildingFinishImproveAiCorePrebuildEffects(buildingFinishedEffectsAdded, buildingFinishedEffectsRemoved, improveEffectsAdded, improveEffectsRemoved, aiCoreEffectsAdded, aiCoreEffectsRemoved, preBuildEffectsAdded, preBuildEffectsRemoved);
                industry.addRemoveImageOverrides(imageOverridesAdded, imageOverridesRemoved);
                industry.overrideCanBeDisruptedAndBasePatherInterest(canBeDisruptedOverride, basePatherInterestOverride);

            } catch (JSONException e) {
                log.error("Error in industry options overrides " + idForErrors + ": " + e);
            }
        }
    }

    public static BoggledTerraformingProject getProject(String projectId) {
        return terraformingProjects.get(projectId);
    }

    public static BoggledTerraformingProjectEffect.TerraformingProjectEffect getProjectEffect(String effectId) {
        return terraformingProjectEffects.get(effectId);
    }

    public static String getResourceLimit(PlanetAPI planet, String resourceId) {
        String planetTypeId = getPlanetType(planet).getPlanetId();
        Map<String, String> planetResourceLimits = resourceLimits.get(planetTypeId);
        if (planetResourceLimits == null) {
            return null;
        }
        return planetResourceLimits.get(resourceId);
    }

    public static Map<String, List<String>> getResourceProgressions() {
        return resourceProgressions;
    }

    public static Map<String, BoggledTerraformingRequirement.TerraformingRequirement> getTerraformingRequirements() {
        return terraformingRequirement;
    }

    private static Map<String, BoggledTerraformingRequirement.TerraformingRequirement> terraformingRequirement;
    private static Map<String, BoggledProjectRequirementsOR> terraformingRequirements;
    private static Map<String, BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiers;

    private static Map<String, BoggledTerraformingProjectEffect.TerraformingProjectEffect> terraformingProjectEffects;
    private static Map<String, BoggledCommonIndustry> industryProjects;
    private static Map<String, BoggledTerraformingProject> terraformingProjects;

    private static Map<String, List<String>> resourceProgressions;
    private static Map<String, Map<String, String>> resourceLimits;

    private static Map<String, PlanetType> planetTypesMap;

    private static List<String> domedCitiesSuppressedConditions;
    private static List<String> stellarReflectorArraySuppressedConditions;

    public static BoggledCommonIndustry getIndustryProject(String industry) {
        return industryProjects.get(industry);
    }

    public static BoggledTerraformingDurationModifier.TerraformingDurationModifier getDurationModifier(String modifier) {
        return durationModifiers.get(modifier);
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

    public static HashMap<String, LinkedHashMap<String, BoggledTerraformingProject>> getVisibleProjects(BoggledTerraformingRequirement.RequirementContext ctx) {
        HashMap<String, LinkedHashMap<String, BoggledTerraformingProject>> ret = new HashMap<>();
        for (Map.Entry<String, BoggledTerraformingProject> entry : terraformingProjects.entrySet()) {
            if (!entry.getValue().isEnabled()) {
                continue;
            }
            if (!entry.getValue().requirementsHiddenMet(ctx)) {
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

    public static Map<String, String> getTokenReplacements(BoggledTerraformingRequirement.RequirementContext ctx) {
        LinkedHashMap<String, String> ret = new LinkedHashMap<>();
        ret.put("$player", Global.getSector().getPlayerPerson().getNameString());
        MarketAPI market = ctx.getClosestMarket();
        if (market != null) {
            ret.put("$marketName", market.getName());
        } else {
            ret.put("$marketName", "market");
        }
        MarketAPI focusMarket = ctx.getFocusContext().getClosestMarket();
        if (focusMarket != null) {
            ret.put("$focusMarketName", focusMarket.getName());
        } else {
            ret.put("$focusMarketName", "orbited market");
        }
        PlanetAPI targetPlanet = ctx.getPlanet();
        if (targetPlanet != null) {
            ret.put("$planetTypeName", getPlanetType(targetPlanet).getPlanetTypeName());
            ret.put("$planetName", targetPlanet.getName());
        } else {
            ret.put("$planetName", "planet");
        }
        StarSystemAPI starSystem = ctx.getStarSystem();
        if (starSystem != null) {
            ret.put("$system", starSystem.getName());
        } else {
            ret.put("$system", "star system");
        }
        return ret;
    }

    public static void incrementOreForPlanetCracking(MarketAPI market)
    {
        if(market.hasCondition("ore_sparse"))
        {
            boggledTools.removeCondition(market, "ore_sparse");
            boggledTools.addCondition(market, "ore_moderate");
        }
        else if(market.hasCondition("ore_moderate"))
        {
            boggledTools.removeCondition(market, "ore_moderate");
            boggledTools.addCondition(market, "ore_abundant");
        }
        else if(market.hasCondition("ore_abundant"))
        {
            boggledTools.removeCondition(market, "ore_abundant");
            boggledTools.addCondition(market, "ore_rich");
        }
        else if(market.hasCondition("ore_rich"))
        {
            boggledTools.removeCondition(market, "ore_rich");
            boggledTools.addCondition(market, "ore_ultrarich");
        }
        else if(market.hasCondition("ore_ultrarich"))
        {
            //Do Nothing
        }
        else
        {
            boggledTools.addCondition(market, "ore_sparse");
        }

        if(market.hasCondition("rare_ore_sparse"))
        {
            boggledTools.removeCondition(market, "rare_ore_sparse");
            boggledTools.addCondition(market, "rare_ore_moderate");
        }
        else if(market.hasCondition("rare_ore_moderate"))
        {
            boggledTools.removeCondition(market, "rare_ore_moderate");
            boggledTools.addCondition(market, "rare_ore_abundant");
        }
        else if(market.hasCondition("rare_ore_abundant"))
        {
            boggledTools.removeCondition(market, "rare_ore_abundant");
            boggledTools.addCondition(market, "rare_ore_rich");
        }
        else if(market.hasCondition("rare_ore_rich"))
        {
            boggledTools.removeCondition(market, "rare_ore_rich");
            boggledTools.addCondition(market, "rare_ore_ultrarich");
        }
        else if(market.hasCondition("rare_ore_ultrarich"))
        {
            //Do Nothing
        }
        else
        {
            boggledTools.addCondition(market, "rare_ore_sparse");
        }
    }

    public static void incrementVolatilesForOuyangOptimization(MarketAPI market)
    {
        if(market.hasCondition("volatiles_trace"))
        {
            boggledTools.removeCondition(market, "volatiles_trace");
            boggledTools.addCondition(market, "volatiles_abundant");
        }
        else if(market.hasCondition("volatiles_diffuse"))
        {
            boggledTools.removeCondition(market, "volatiles_diffuse");
            boggledTools.addCondition(market, "volatiles_plentiful");
        }
        else if(market.hasCondition("volatiles_abundant"))
        {
            boggledTools.removeCondition(market, "volatiles_abundant");
            boggledTools.addCondition(market, "volatiles_plentiful");
        }
        else if(market.hasCondition("volatiles_plentiful"))
        {
            //Do nothing
        }
        else
        {
            boggledTools.addCondition(market, "volatiles_diffuse");
        }

        SectorEntityToken closestGasGiantToken = market.getPrimaryEntity();
        if(closestGasGiantToken != null)
        {
            Iterator allEntitiesInSystem = closestGasGiantToken.getStarSystem().getAllEntities().iterator();
            while(allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
                if(entity.hasTag("station") && entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(closestGasGiantToken) && (entity.getCustomEntitySpec().getDefaultName().equals("Side Station") || entity.getCustomEntitySpec().getDefaultName().equals("Siphon Station")) && !entity.getId().equals("beholder_station"))
                {
                    if(entity.getMarket() != null)
                    {
                        market = entity.getMarket();
                        if(market.hasCondition("volatiles_trace"))
                        {
                            boggledTools.removeCondition(market, "volatiles_trace");
                            boggledTools.addCondition(market, "volatiles_abundant");
                        }
                        else if(market.hasCondition("volatiles_diffuse"))
                        {
                            boggledTools.removeCondition(market, "volatiles_diffuse");
                            boggledTools.addCondition(market, "volatiles_plentiful");
                        }
                        else if(market.hasCondition("volatiles_abundant"))
                        {
                            boggledTools.removeCondition(market, "volatiles_abundant");
                            boggledTools.addCondition(market, "volatiles_plentiful");
                        }
                    }
                }
            }
        }
    }

    public static String getCommidityNameFromId(String commodityId) {
        return Global.getSettings().getCommoditySpec(commodityId).getName();
    }

    public static boolean marketHasAtmoProblem(MarketAPI market) {
        return !market.hasCondition(Conditions.MILD_CLIMATE) ||
                !market.hasCondition(Conditions.HABITABLE) ||
                market.hasCondition(Conditions.NO_ATMOSPHERE) ||
                market.hasCondition(Conditions.THIN_ATMOSPHERE) ||
                market.hasCondition(Conditions.DENSE_ATMOSPHERE) ||
                market.hasCondition(Conditions.TOXIC_ATMOSPHERE);
    }

    public static boolean terraformingPossibleOnMarket(MarketAPI market)
    {
        return !market.hasCondition(Conditions.IRRADIATED);
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

    public static void surveyAll(MarketAPI market)
    {
        if(market == null)
        {
            return;
        }

        for (MarketConditionAPI condition : market.getConditions())
        {
            condition.setSurveyed(true);
        }

        //Refreshes supply and demand for each industry on the market
        for (Industry industry : market.getIndustries())
        {
            industry.doPreSaveCleanup();
            industry.doPostSaveRestore();
        }

        refreshAquacultureAndFarming(market);
    }

    public static void refreshAquacultureAndFarming(MarketAPI market)
    {
        if(market == null || market.getPrimaryEntity() == null || market.getPlanetEntity() == null || market.hasTag("station") || market.getPrimaryEntity().hasTag("station"))
        {
            return;
        }
        else
        {
            if(market.hasIndustry("farming") && market.hasCondition("water_surface"))
            {
                market.getIndustry("farming").init("aquaculture", market);
            }
            else if(market.hasIndustry("aquaculture") && !market.hasCondition("water_surface"))
            {
                market.getIndustry("aquaculture").init("farming", market);
            }
        }
    }

    public static void stepTag(MarketAPI market, String tagToIncrement, int step) {
        if (market == null) {
            return;
        }

        for (String tag : market.getTags()) {
            if (tag.startsWith(tagToIncrement)) {
                int tagValueOld = Integer.parseInt(tag.substring(tagToIncrement.length()));
                market.removeTag(tag);
                market.addTag(tagToIncrement + (tagValueOld + step));
                return;
            }
        }
        market.addTag(tagToIncrement + step);
    }

    public static float getRandomOrbitalAngleFloat(float min, float max) {
        Random rand = new Random();
        return rand.nextFloat() * (max - min) + min;
    }

    public static int numStationsInOrbit(PlanetAPI targetPlanet, String... stationTags) {
        int numStations = 0;
        for (String stationTag : stationTags) {
            List<SectorEntityToken> entities = targetPlanet.getStarSystem().getEntitiesWithTag(stationTag);
            for (SectorEntityToken entity : entities) {
                if (entity.getOrbitFocus() == null) {
                    continue;
                }
                if (!entity.getOrbitFocus().equals(targetPlanet)) {
                    continue;
                }
                numStations++;
            }
        }
        return numStations;
    }

    public static int numStationsInSystem(StarSystemAPI starSystem, String... stationTags) {
        int numStations = 0;
        for (String stationTag : stationTags) {
            List<SectorEntityToken> entities = starSystem.getEntitiesWithTag(stationTag);
            for (SectorEntityToken entity : entities) {
                if (!entity.getFaction().getId().equals(Factions.NEUTRAL)) {
                    numStations++;
                }
            }
        }
        return numStations;
    }

    public static boolean gateInSystem(StarSystemAPI system) {
        return !system.getEntitiesWithTag(Tags.GATE).isEmpty();
    }

    public static boolean playerMarketInSystem(SectorEntityToken playerFleet) {
        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity.getMarket() != null && entity.getMarket().isPlayerOwned()) {
                return true;
            }
        }

        return false;
    }

    public static Integer getSizeOfLargestPlayerMarketInSystem(StarSystemAPI system) {
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

    public static Integer getPlayerMarketSizeRequirementToBuildGate() {
        return boggledTools.getIntSetting(BoggledSettings.marketSizeRequiredToBuildInactiveGate);
    }

    public static SectorEntityToken getClosestPlayerMarketToken(SectorEntityToken playerFleet) {
        if (!playerMarketInSystem(playerFleet)) {
            return null;
        }
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
            } else if (Misc.getDistance(entity, playerFleet) < Misc.getDistance(closestMarket, playerFleet)) {
                closestMarket = entity;
            }
        }

        return closestMarket;
    }

    public static SectorEntityToken getClosestGasGiantToken(SectorEntityToken playerFleet) {
        StarSystemAPI system = playerFleet.getStarSystem();
        if(system == null)
        {
            return null;
        }

        List<SectorEntityToken> allGasGiantsInSystem = new ArrayList<>();
        for(PlanetAPI planet : system.getPlanets())
        {
            // Make sure the gas giant has a valid market before considering it.
            // Should never matter unless another mod adds an invalid planet.
            if (planet.isGasGiant() && planet.getMarket() != null && planet.getMarket().getFaction() != null) {
                allGasGiantsInSystem.add(planet);
            }
        }

        SectorEntityToken closestGasGiant = null;
        for (SectorEntityToken entity : allGasGiantsInSystem)
        {
            if (closestGasGiant == null) {
                closestGasGiant = entity;
            } else if (Misc.getDistance(entity, playerFleet) < Misc.getDistance(closestGasGiant, playerFleet)) {
                closestGasGiant = entity;
            }
        }

        return closestGasGiant;
    }

    public static SectorEntityToken getClosestColonizableStationInSystem(SectorEntityToken playerFleet) {
        List<SectorEntityToken> allColonizableStationsInSystem = new ArrayList<>();
        for (SectorEntityToken entity : playerFleet.getStarSystem().getEntitiesWithTag(Tags.STATION)) {
            if (entity.getMarket() != null && entity.getMarket().hasCondition(Conditions.ABANDONED_STATION)) {
                allColonizableStationsInSystem.add(entity);
            }
        }

        SectorEntityToken closestStation = null;
        for (SectorEntityToken entity : allColonizableStationsInSystem) {
            if (closestStation == null) {
                closestStation = entity;
            } else if (Misc.getDistance(entity, playerFleet) < Misc.getDistance(closestStation, playerFleet)) {
                closestStation = entity;
            }
        }

        return closestStation;
    }

    public static SectorEntityToken getClosestStationInSystem(SectorEntityToken playerFleet) {
        StarSystemAPI starSystem = playerFleet.getStarSystem();
        if (starSystem == null) {
            return null;
        }
        List<SectorEntityToken> allStationsInSystem = playerFleet.getStarSystem().getEntitiesWithTag(Tags.STATION);

        SectorEntityToken closestStation = null;
        for (SectorEntityToken entity : allStationsInSystem) {
            if (closestStation == null) {
                closestStation = entity;
            } else if (Misc.getDistance(entity, playerFleet) < Misc.getDistance(closestStation, playerFleet)) {
                closestStation = entity;
            }
        }

        return closestStation;
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

    public static PlanetAPI getClosestPlanetToken(CampaignFleetAPI playerFleet) {
        StarSystemAPI system = playerFleet.getStarSystem();
        if(playerFleet.isInHyperspace() || playerFleet.isInHyperspaceTransition() || system == null)
        {
            return null;
        }

        Pair<PlanetAPI, Float> closestPlanet = null;
        for(PlanetAPI token : system.getPlanets())
        {
            // Black holes are stars
            if(token.isStar())
            {
                continue;
            }

            // There's been issues in the past with other mods adding invalid planets and markets which could cause a NPE in TASC
            if(token.getMarket() == null || token.getMarket().getFaction() == null)
            {
                continue;
            }

            if(closestPlanet == null)
            {
                closestPlanet = new Pair<>(token, Misc.getDistance(playerFleet, token));
            }
            else
            {
                float newDistance = Misc.getDistance(token, playerFleet);
                if(newDistance < closestPlanet.two)
                {
                    closestPlanet.one = token;
                    closestPlanet.two = newDistance;
                }
            }
        }

        return closestPlanet != null ? closestPlanet.one : null;
    }

    public static MarketAPI getClosestMarketToEntity(SectorEntityToken entity) {
        if(entity == null || entity.getStarSystem() == null || entity.isInHyperspace()) {
            return null;
        }

        List<MarketAPI> markets = Global.getSector().getEconomy().getMarkets(entity.getStarSystem());
        MarketAPI closestMarket = null;
        for(MarketAPI market : markets) {
            if(closestMarket == null || Misc.getDistance(entity, market.getPrimaryEntity()) < Misc.getDistance(entity, closestMarket.getPrimaryEntity())) {
                if(!market.getFactionId().equals(Factions.NEUTRAL)) {
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
        if(planet == null || planet.getSpec() == null || planet.getSpec().getPlanetType() == null) {
            return planetTypesMap.get(unknownPlanetId); // Guaranteed to be there
        }

        PlanetType planetType = planetTypesMap.get(planet.getTypeId());
        if (planetType != null) {
            return planetType;
        }
        return planetTypesMap.get(unknownPlanetId); // Guaranteed to be there
    }

    public static List<MarketAPI> getNonStationMarketsPlayerControls() {
        List<MarketAPI> allPlayerMarkets = Misc.getPlayerMarkets(true);
        List<MarketAPI> allNonStationPlayerMarkets = new ArrayList<>();
        for (MarketAPI market : allPlayerMarkets) {
            if (!boggledTools.marketIsStation(market)) {
                if (!market.hasCondition(BoggledConditions.terraformingControllerConditionId)) {
                    boggledTools.addCondition(market, BoggledConditions.terraformingControllerConditionId);
                }
                allNonStationPlayerMarkets.add(market);
            }
        }

        return allNonStationPlayerMarkets;
    }

    public static List<MarketAPI> getStationMarketsPlayerControls() {
        List<MarketAPI> allPlayerMarkets = Misc.getPlayerMarkets(true);
        List<MarketAPI> allStationPlayerMarkets = new ArrayList<>();
        for (MarketAPI market : allPlayerMarkets) {
            if (boggledTools.marketIsStation(market)) {
                if (!market.hasCondition(BoggledConditions.terraformingControllerConditionId)) {
                    boggledTools.addCondition(market, BoggledConditions.terraformingControllerConditionId);
                }
                allStationPlayerMarkets.add(market);
            }
        }

        return allStationPlayerMarkets;
    }

    public static boolean marketIsStation(MarketAPI market) {
        return market.getPrimaryEntity() == null || market.getPlanetEntity() == null || market.getPrimaryEntity().hasTag(Tags.STATION);
    }

    public static boolean getCreateMirrorsOrShades(MarketAPI market) {
        // If this gets called from a context where the player is not at a market (e.g. from VoK research tree) we need to null check
        if(market == null)
        {
            return true;
        }

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

        if(boggledTools.getBooleanSetting("boggledMiningStationLinkToResourceBelts"))
        {
            int numAsteroidBeltsInSystem = boggledTools.getNumAsteroidTerrainsInSystem(stationEntity);
            String resourceLevel = boggledTools.getMiningStationResourceString(numAsteroidBeltsInSystem);
            market.addCondition("ore_" + resourceLevel);
            market.addCondition("rare_ore_" + resourceLevel);
        }
        else
        {
            String resourceLevel = "moderate";
            int staticAmountPerSettings = boggledTools.getIntSetting("boggledMiningStationStaticAmount");
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

        market.addCondition("sprite_controller");
        market.addCondition("cramped_quarters");

        //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
        //market_conditions.csv overwrites the vanilla no_atmosphere condition
        //the only change made is to hide the icon on markets where primary entity has station tag
        //This is done so refining and fuel production can slot the special items
        //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
        market.addCondition("no_atmosphere");
        market.suppressCondition("no_atmosphere");

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);
        market.getConstructionQueue().addToEnd(Industries.MINING, 0);

        stationEntity.setMarket(market);

        Global.getSector().getEconomy().addMarket(market, true);

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        boggledTools.surveyAll(market);

        // If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
        // Need these EveryFrameScripts because we cannot replicate the vanilla behavior to show the player faction set up screen and the CoreUITab at the same time.
        // See post from Alex at https://fractalsoftworks.com/forum/index.php?topic=5061.0, page 794
        Global.getSector().addTransientScript(new BoggledPlayerFactionSetupScript());
        Global.getSector().addTransientScript(new BoggledNewStationMarketInteractionScript(market));

        Global.getSoundPlayer().playUISound("ui_boggled_station_constructed", 1.0F, 1.0F);
        return market;
    }

    public static MarketAPI createSiphonStationMarket(SectorEntityToken stationEntity, SectorEntityToken hostGasGiant)
    {
        // Assumes the station is being created in a valid spot.
        // isUsable() method of Construct_Siphon_Station handles validation.
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

        if(boggledTools.getBooleanSetting("boggledSiphonStationLinkToGasGiant"))
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
            int staticAmountPerSettings = boggledTools.getIntSetting("boggledSiphonStationStaticAmount");
            resourceLevel = switch (staticAmountPerSettings) {
                case 1 -> "trace";
                case 2 -> "diffuse";
                case 3 -> "abundant";
                case 4 -> "plentiful";
                default -> resourceLevel;
            };
            market.addCondition("volatiles_" + resourceLevel);
        }

        market.addCondition("sprite_controller");
        market.addCondition("cramped_quarters");

        //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
        //market_conditions.csv overwrites the vanilla no_atmosphere condition
        //the only change made is to hide the icon on markets where primary entity has station tag
        //This is done so refining and fuel production can slot the special items
        //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
        market.addCondition("no_atmosphere");
        market.suppressCondition("no_atmosphere");

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);
        market.getConstructionQueue().addToEnd(Industries.MINING, 0);

        stationEntity.setMarket(market);

        Global.getSector().getEconomy().addMarket(market, true);

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        boggledTools.surveyAll(market);

        // If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
        // Need these EveryFrameScripts because we cannot replicate the vanilla behavior to show the player faction set up screen and the CoreUITab at the same time.
        // See post from Alex at https://fractalsoftworks.com/forum/index.php?topic=5061.0, page 794
        Global.getSector().addTransientScript(new BoggledPlayerFactionSetupScript());
        Global.getSector().addTransientScript(new BoggledNewStationMarketInteractionScript(market));

        Global.getSoundPlayer().playUISound("ui_boggled_station_constructed", 1.0F, 1.0F);
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

        market.addCondition("sprite_controller");
        market.addCondition("cramped_quarters");

        //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
        //market_conditions.csv overwrites the vanilla no_atmosphere condition
        //the only change made is to hide the icon on markets where primary entity has station tag
        //This is done so refining and fuel production can slot the special items
        //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
        market.addCondition("no_atmosphere");
        market.suppressCondition("no_atmosphere");

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);

        stationEntity.setMarket(market);

        Global.getSector().getEconomy().addMarket(market, true);

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        boggledTools.surveyAll(market);

        // If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
        // Need these EveryFrameScripts because we cannot replicate the vanilla behavior to show the player faction set up screen and the CoreUITab at the same time.
        // See post from Alex at https://fractalsoftworks.com/forum/index.php?topic=5061.0, page 794
        Global.getSector().addTransientScript(new BoggledPlayerFactionSetupScript());
        Global.getSector().addTransientScript(new BoggledNewStationMarketInteractionScript(market));

        Global.getSoundPlayer().playUISound("ui_boggled_station_constructed", 1.0F, 1.0F);
        return market;
    }

    public static ArrayList<SectorEntityToken> getMoonsInOrbitAroundPlanet(SectorEntityToken planet)
    {
        ArrayList<SectorEntityToken> moons = new ArrayList<>();
        for(SectorEntityToken token : planet.getStarSystem().getPlanets())
        {
            if (token.getOrbitFocus() != null && !token.isStar() && token.getOrbitFocus().equals(planet) && token.getRadius() != 0)
            {
                moons.add(token);
            }
        }

        return moons;
    }

    public static int getNumMoonsInOrbitAroundPlanet(SectorEntityToken planet)
    {
        return getMoonsInOrbitAroundPlanet(planet).size();
    }

    public static int numAstroInOrbit(SectorEntityToken targetPlanet)
    {
        return getExistingAstropolisStations(targetPlanet).size();
    }

    public static ArrayList<SectorEntityToken> getExistingAstropolisStations(SectorEntityToken targetPlanet)
    {
        ArrayList<SectorEntityToken> existingAstropolisStations = new ArrayList<>();
        for(SectorEntityToken token : targetPlanet.getStarSystem().getAllEntities())
        {
            if (token.hasTag(boggledTools.BoggledTags.astropolisStation) && token.getOrbitFocus() != null && token.getOrbitFocus().equals(targetPlanet))
            {
                existingAstropolisStations.add(token);
            }
        }

        return existingAstropolisStations;
    }

    public static int getNumExistingAstroplisStations(SectorEntityToken targetPlanet)
    {
        return getExistingAstropolisStations(targetPlanet).size();
    }

    public static float generateNewAngleForAstropolisStation(List<Float> existingAngles) {
        // Normalize the angles to a 0-360 range and handle floating-point precision
        // for comparisons.
        final float ANGLE_STEP = 120.0f;
        final float MAX_ANGLE = 360.0f;
        // Epsilon for comparing floating-point numbers. A small value is
        // necessary as direct equality checks (==) can be unreliable.
        // Turns out Starsector angles can deviate by as much as ~.2 degrees
        final float EPSILON = 1.0f;

        // Case 1: The list is empty.
        // We now return a random angle instead of a fixed 0.0f.
        if (existingAngles.isEmpty()) {
            Random random = new Random();
            return random.nextFloat() * MAX_ANGLE;
        }

        // Case 2: The list has one element.
        // The new angle will be 120 degrees from the existing one.
        if (existingAngles.size() == 1) {
            float existing = existingAngles.get(0);
            float newAngle = (existing + ANGLE_STEP) % MAX_ANGLE;
            // Ensure the result is non-negative after the modulo operation
            if (newAngle < 0) {
                newAngle += MAX_ANGLE;
            }
            return newAngle;
        }

        // Case 3: The list has two elements.
        // The third angle will be 120 degrees from the second existing one
        // (which is 240 degrees from the first).
        if (existingAngles.size() == 2) {
            float a1 = existingAngles.get(0);
            float a2 = existingAngles.get(1);

            // A robust check to see which one is the "next" angle in the sequence.
            float candidateA = (a1 + ANGLE_STEP) % MAX_ANGLE;
            if (Math.abs(candidateA - a2) < EPSILON) {
                float newAngle = (a2 + ANGLE_STEP) % MAX_ANGLE;
                if (newAngle < 0) {
                    newAngle += MAX_ANGLE;
                }
                return newAngle;
            }

            // Re-evaluating the logic in the opposite direction.
            float candidateB = (a2 + ANGLE_STEP) % MAX_ANGLE;
            if (Math.abs(candidateB - a1) < EPSILON) {
                float newAngle = (a1 + ANGLE_STEP) % MAX_ANGLE;
                if (newAngle < 0) {
                    newAngle += MAX_ANGLE;
                }
                return newAngle;
            }

            // This block should technically not be reached given the problem constraints,
            // but is included for defensive programming.
            boggledTools.writeMessageToLog("Astropolis angles that caused the exception: " + a1 + " " + a2);
            throw new IllegalArgumentException("Existing angles are not 120 degrees apart.");
        }

        // Case 4: The list has three or more elements.
        // It's not possible to add a new angle while maintaining the pattern,
        // as a 120-degree separation allows for only three distinct angles.
        throw new IllegalArgumentException("Cannot add a new angle. The list already contains the maximum number of angles (3).");
    }

    public static String getGreekLetterForNextAstropolisCustomEntityId(int numAstroAlreadyPresent)
    {
        // Gets the greek letter to insert in the astropolis custom entity id used to
        // determine whether to create a station with the low, midline or high-tech sprite.
        int setting = boggledTools.getIntSetting("boggledAstropolisSpriteToUse");
        if(setting == 1)
        {
            return "alpha";
        }
        else if(setting == 2)
        {
            return "beta";
        }
        else if(setting == 3)
        {
            return "gamma";
        }
        else
        {
            // Handles 0 setting and if the value somehow is a bad value (ex. not 0, 1, 2 or 3)
            // TASC no longer supports more than three astropolis stations around a single planet.
            if(numAstroAlreadyPresent == 0)
            {
                return "alpha";
            }
            else if(numAstroAlreadyPresent == 1)
            {
                return "beta";
            }
            else
            {
                return "gamma";
            }
        }
    }

    public static String getAstropolisColonyNameStringGreekLetter(int numAstroAlreadyPresent)
    {
        // This is different from the above function - this is used purely for the name of the colony on the station.
        // TASC no longer supports more than three astropolis stations around a single planet.
        if(numAstroAlreadyPresent == 0)
        {
            return "Alpha";
        }
        else if(numAstroAlreadyPresent == 1)
        {
            return "Beta";
        }
        else
        {
            return "Gamma";
        }
    }

    public static SectorEntityToken getFocusOfAsteroidBelt(SectorEntityToken playerFleet) {
        for (Object object : playerFleet.getStarSystem().getEntities(CampaignTerrainAPI.class)) {
            CampaignTerrainAPI terrain = (CampaignTerrainAPI) object;
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

            if ((terrainPlugin instanceof AsteroidBeltTerrainPlugin && !(terrainPlugin instanceof AsteroidFieldTerrainPlugin)) && terrainPlugin.containsEntity(playerFleet)) {
                return terrain.getOrbitFocus();
            }
        }

        return null;
    }

    public static OrbitAPI getAsteroidFieldOrbit(SectorEntityToken playerFleet) {
        for (Object object : playerFleet.getStarSystem().getEntities(CampaignTerrainAPI.class)) {
            CampaignTerrainAPI terrain = (CampaignTerrainAPI) object;
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

            if (terrainPlugin instanceof AsteroidFieldTerrainPlugin && terrainPlugin.containsEntity(playerFleet)) {
                AsteroidFieldTerrainPlugin asteroidPlugin = (AsteroidFieldTerrainPlugin) terrain.getPlugin();
                return asteroidPlugin.getEntity().getOrbit();
            } else {
                return null;
            }
        }

        return null;
    }

    public static SectorEntityToken getAsteroidFieldEntity(SectorEntityToken playerFleet) {
        for (Object object : playerFleet.getStarSystem().getEntities(CampaignTerrainAPI.class)) {
            CampaignTerrainAPI terrain = (CampaignTerrainAPI) object;
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

            if (terrainPlugin instanceof AsteroidFieldTerrainPlugin && terrainPlugin.containsEntity(playerFleet)) {
                return terrain;
            }
        }

        // Should never return null because this method can't be called unless playerFleetInAsteroidField returned true
        return null;
    }

    public static boolean playerFleetInAsteroidBelt(SectorEntityToken playerFleet) {
        for (Object object : playerFleet.getStarSystem().getEntities(CampaignTerrainAPI.class)) {
            CampaignTerrainAPI terrain = (CampaignTerrainAPI) object;
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

            if ((terrainPlugin instanceof AsteroidBeltTerrainPlugin && !(terrainPlugin instanceof AsteroidFieldTerrainPlugin)) && terrainPlugin.containsEntity(playerFleet)) {
                return true;
            }
        }

        return false;
    }

    public static boolean playerFleetInAsteroidField(SectorEntityToken playerFleet)
    {
        for (Object object : playerFleet.getStarSystem().getEntities(CampaignTerrainAPI.class)) {
            CampaignTerrainAPI terrain = (CampaignTerrainAPI) object;
            CampaignTerrainPlugin terrainPlugin = terrain.getPlugin();

            if (terrainPlugin instanceof AsteroidFieldTerrainPlugin && terrainPlugin.containsEntity(playerFleet)) {
                return true;
            }
        }

        return false;
    }

    public static boolean playerFleetTooCloseToJumpPoint(SectorEntityToken playerFleet) {
        for (Object object : playerFleet.getStarSystem().getEntities(JumpPointAPI.class)) {
            JumpPointAPI entity = (JumpPointAPI) object;
            if (Misc.getDistance(playerFleet, entity) < 300f) {
                return true;
            }
        }

        return false;
    }

    public static int getNumAsteroidTerrainsInSystem(SectorEntityToken playerFleet) {
        int numRoids = 0;
        for (Object object : playerFleet.getStarSystem().getEntities(CampaignTerrainAPI.class)) {
            CampaignTerrainAPI terrain = (CampaignTerrainAPI) object;
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

        return numRoids;
    }

    public static String getMiningStationResourceString(Integer numAsteroidTerrains) {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.miningStationLinkToResourceBelts)) {
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

        int staticAmountPerSettings = boggledTools.getIntSetting(boggledTools.BoggledSettings.miningStationStaticAmount);
        switch (staticAmountPerSettings) {
            case 1:
                return "sparse";
            case 2:
                return "moderate";
            case 3:
                return "abundant";
            case 4:
                return "rich";
            case 5:
                return "ultrarich";
        }
        return "moderate";
    }

    public static int getNumberOfStationExpansions(MarketAPI market) {
        for (String tag : market.getTags()) {
            if (tag.contains(BoggledTags.stationConstructionNumExpansions)) {
                return Integer.parseInt(tag.substring(tag.length() - 1));
            }
        }

        return 0;
    }

    public static float randomOrbitalAngleFloat() {
        Random rand = new Random();
        return rand.nextFloat() * (360f);
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

    public static int numReflectorsInOrbit(MarketAPI market) {
        int numReflectors = 0;

        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains(Entities.STELLAR_MIRROR) || entity.getId().contains(Entities.STELLAR_SHADE) || entity.hasTag(Entities.STELLAR_MIRROR) || entity.hasTag(Entities.STELLAR_SHADE))) {
                numReflectors++;
            }
        }

        return numReflectors;
    }

    public static int numMirrorsInOrbit(MarketAPI market) {
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
        // Null check market because this can get called when the player is not docked and market is null (e.g. in VoK research tree)
        // I think VoK is creating a dummy market to put the industry on to generate the image and tooltip, but the dummy has no star system.
        if(market != null && market.getStarSystem() != null)
        {
            Iterator<SectorEntityToken> allEntitiesInSystem = market.getStarSystem().getAllEntities().iterator();
            while (allEntitiesInSystem.hasNext())
            {
                SectorEntityToken entity = allEntitiesInSystem.next();
                if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains(Entities.STELLAR_MIRROR) || entity.getId().contains(Entities.STELLAR_SHADE) || entity.hasTag(Entities.STELLAR_MIRROR) || entity.hasTag(Entities.STELLAR_SHADE)))
                {
                    allEntitiesInSystem.remove();
                    market.getStarSystem().removeEntity(entity);
                }
            }
        }
    }

    public static void swapStationSprite(SectorEntityToken station, String stationType, String stationGreekLetter, int targetSize) {
        MarketAPI market = station.getMarket();
        StarSystemAPI system = market.getStarSystem();
        OrbitAPI orbit = null;
        if(station.getOrbit() != null) {
            orbit = station.getOrbit();
        }
        CampaignClockAPI clock = Global.getSector().getClock();
        SectorEntityToken newStation;
        SectorEntityToken newStationLights = null;

        String size = null;
        if(targetSize == 1) {
            size = "small";
        } else if(targetSize == 2) {
            size = "medium";
        } else if(targetSize == 3) {
            size = "large";
        } else {
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

        if(newStation == null) {
            //Failed to create a new station likely because of erroneous passed values. Do nothing.
            return;
        }

        newStation.setContainingLocation(station.getContainingLocation());
        if(newStationLights != null) {
            newStationLights.setContainingLocation(station.getContainingLocation());
        }

        if(orbit != null) {
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

        // Handle Illustrated Entities custom images and/or description
        if(Global.getSettings().getModManager().isModEnabled(BoggledMods.illustratedEntitiesModId)) {
            boolean customImageHasBeenSet = ImageHandler.hasImage(station);
            if(customImageHasBeenSet) {
                int customImageId = ImageHandler.getImageId(station);
                ImageHandler.removeImageFrom(station);
                ImageHandler.setImage(newStation, ImageDataMemory.getInstance().get(customImageId), false);
            }

            TextDataEntry textDataEntry = TextHandler.getDataForEntity(station);
            if(textDataEntry != null) {
                boggledTools.setEntityIllustratedEntitiesCustomDescription(newStation, textDataEntry);
            }
        }

        //Deletes the old station. May cause limited issues related to ships orbiting the old location
        clearConnectedStations(market);
        system.removeEntity(station);

        newStation.setMarket(market);
        market.setPrimaryEntity(newStation);

        surveyAll(market);
    }

    public static void setEntityIllustratedEntitiesCustomDescription(SectorEntityToken sectorEntityToken, TextDataEntry textDataEntry)
    {
        // Have to avoid doing anything if Illustrated Entities is not enabled because it will cause a crash since the library isn't loaded
        if(Global.getSettings().getModManager().isModEnabled(BoggledMods.illustratedEntitiesModId))
        {
            // The passed SectorEntityToken will have the description lines from the passed TextDataEntry copied onto its own TextDataEntry.

            TextDataMemory dataMemory = TextDataMemory.getInstance();

            int i = dataMemory.getNexFreetNum();
            TextDataEntry newTextDataEntry = new TextDataEntry(i, sectorEntityToken.getId());

            for (int textNum = 1; textNum <= 2; textNum++)
            {
                for (int lineNum = 1; lineNum <= Settings.getInt(Settings.TEXT_LINE_NUM); lineNum++)
                {
                    String s = textDataEntry.getString(textNum, lineNum);
                    newTextDataEntry.setString(textNum, lineNum, s);
                }
            }

            newTextDataEntry.apply();
            dataMemory.set(newTextDataEntry.descriptionNum, newTextDataEntry);
        }
    }

    public static void deleteOldLightsOverlay(SectorEntityToken station, String stationType, String stationGreekLetter) {
        StarSystemAPI system = station.getStarSystem();

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

    public static void reapplyMiningStationLights(StarSystemAPI system) {
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

        if(stationToApplyOverlayTo != null) {
            if(stationsize == 1) {
                if(!stationToApplyOverlayTo.getMarket().getFactionId().equals(Factions.NEUTRAL))
                {
                    SectorEntityToken newMiningStationLights = system.addCustomEntity("boggled_miningStationLights", "Mining Station Lights Overlay", "boggled_mining_station_small_lights_overlay", stationToApplyOverlayTo.getFaction().getId());
                    newMiningStationLights.setOrbit(stationToApplyOverlayTo.getOrbit().makeCopy());
                }
                reapplyMiningStationLights(system);
            } else if(stationsize == 2) {
                if(!stationToApplyOverlayTo.getMarket().getFactionId().equals(Factions.NEUTRAL)) {
                    SectorEntityToken newMiningStationLights = system.addCustomEntity("boggled_miningStationLights", "Mining Station Lights Overlay", "boggled_mining_station_medium_lights_overlay", stationToApplyOverlayTo.getFaction().getId());
                    newMiningStationLights.setOrbit(stationToApplyOverlayTo.getOrbit().makeCopy());
                }
                reapplyMiningStationLights(system);
            }
        }
        else {
            for (SectorEntityToken entity : system.getAllEntities()) {
                if (entity.hasTag(BoggledTags.alreadyReappliedLightsOverlay)) {
                    entity.removeTag(BoggledTags.alreadyReappliedLightsOverlay);
                }
            }
        }
    }

    public static boolean marketHasOrbitalStation(MarketAPI market) {
        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && entity.hasTag(Tags.STATION)) {
                return true;
            }
        }

        return false;
    }

    public static void addCondition(MarketAPI market, String condition) {
        if(!market.hasCondition(condition)) {
            market.addCondition(condition);
            boggledTools.surveyAll(market);
        }
    }

    public static void removeCondition(MarketAPI market, String condition) {
        if(market != null && market.hasCondition(condition)) {
            market.removeCondition(condition);
            boggledTools.surveyAll(market);
        }
    }

    public static void changePlanetType(PlanetAPI planet, String newType) {
        PlanetSpecAPI planetSpec = planet.getSpec();
        for(PlanetSpecAPI targetSpec : Global.getSettings().getAllPlanetSpecs()) {
            if (targetSpec.getPlanetType().equals(newType)) {
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

    public static void applyPlanetKiller(MarketAPI market) {
        if(Misc.isStoryCritical(market) && !boggledTools.getBooleanSetting(BoggledSettings.planetKillerAllowDestructionOfColoniesMarkedAsEssentialForQuests)) {
            // Should never be reached because deployment will be disabled.
            return;
        }

        adjustRelationshipsDueToPlanetKillerUsage(market);
        triggerMilitaryResponseToPlanetKillerUsage(market);
        decivilizeMarketWithPlanetKiller(market);
        if(market.getPlanetEntity() != null && market.getPlanetEntity().getSpec() != null) {
            changePlanetTypeWithPlanetKiller(market);
            changePlanetConditionsWithPlanetKiller(market);
        }
    }

    public static void changePlanetTypeWithPlanetKiller(MarketAPI market) {
        String planetType = getPlanetType(market.getPlanetEntity()).getPlanetId();
        if(!planetType.equals(starPlanetId) && !planetType.equals(gasGiantPlanetId) && !planetType.equals(volcanicPlanetId) && !planetType.equals(unknownPlanetId))
        {
            changePlanetType(market.getPlanetEntity(), Conditions.IRRADIATED);
            market.addCondition(Conditions.IRRADIATED);
        }
    }

    public static void changePlanetConditionsWithPlanetKiller(MarketAPI market) {
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
        if(!planetType.equals(gasGiantPlanetId) && !planetType.equals(unknownPlanetId)) {
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

    public static List<FactionAPI> factionsToMakeHostileDueToPlanetKillerUsage(MarketAPI market) {
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

    public static void adjustRelationshipsDueToPlanetKillerUsage(MarketAPI market) {
        for(FactionAPI faction : factionsToMakeHostileDueToPlanetKillerUsage(market)) {
            faction.setRelationship(Factions.PLAYER, -100f);
        }
    }

    public static void decivilizeMarketWithPlanetKiller(MarketAPI market) {
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
        if (dialog != null && dialog.getPlugin() instanceof RuleBasedDialog) {
            if (dialog.getInteractionTarget() != null && dialog.getInteractionTarget().getMarket() != null) {
                Global.getSector().setPaused(false);
                dialog.getInteractionTarget().getMarket().getMemoryWithoutUpdate().advance(0.0001f);
                Global.getSector().setPaused(true);
            }

            ((RuleBasedDialog) dialog.getPlugin()).updateMemory();
        }

        if (dialog != null && dialog.getPlugin() instanceof RuleBasedDialog) {
            ((RuleBasedDialog) dialog.getPlugin()).updateMemory();
        }
    }

    public static void triggerMilitaryResponseToPlanetKillerUsage(MarketAPI market) {
        // Copied from MarketCMD addMilitaryResponse()
        if (market == null) return;

        if (!market.getFaction().getCustomBoolean(Factions.CUSTOM_NO_WAR_SIM)) {
            MilitaryResponseScript.MilitaryResponseParams params = new MilitaryResponseScript.MilitaryResponseParams(CampaignFleetAIAPI.ActionType.HOSTILE,
                    "player_ground_raid_" + market.getId(),
                    market.getFaction(),
                    market.getPrimaryEntity(),
                    0.75f,
                    30f);
            market.getContainingLocation().addScript(new MilitaryResponseScript(params));
        }

        List<CampaignFleetAPI> fleets = market.getContainingLocation().getFleets();
        for (CampaignFleetAPI other : fleets) {
            if (other.getFaction() == market.getFaction()) {
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
        private final List<Pair<BoggledProjectRequirementsAND, Integer>> conditionalWaterRequirements;

        public String getPlanetId() { return planetId; }
        public String getPlanetTypeName() { return planetTypeName; }
        public boolean getTerraformingPossible() { return terraformingPossible; }
        public int getWaterLevel(BoggledTerraformingRequirement.RequirementContext ctx) {
            if (conditionalWaterRequirements.isEmpty()) {
                return baseWaterLevel;
            }

            for (Pair<BoggledProjectRequirementsAND, Integer> conditionalWaterRequirement : conditionalWaterRequirements) {
                if (conditionalWaterRequirement.one.requirementsMet(ctx)) {
                    return conditionalWaterRequirement.two;
                }
            }
            return baseWaterLevel;
        }

        public PlanetType(String planetId, String planetTypeName, boolean terraformingPossible, int baseWaterLevel, List<Pair<BoggledProjectRequirementsAND, Integer>> conditionalWaterRequirements) {
            this.planetId = planetId;
            this.planetTypeName = planetTypeName;
            this.terraformingPossible = terraformingPossible;
            this.baseWaterLevel = baseWaterLevel;
            this.conditionalWaterRequirements = conditionalWaterRequirements;
            Collections.sort(this.conditionalWaterRequirements, new Comparator<Pair<BoggledProjectRequirementsAND, Integer>>() {
                @Override
                public int compare(Pair<BoggledProjectRequirementsAND, Integer> p1, Pair<BoggledProjectRequirementsAND, Integer> p2) {
                    return p1.two.compareTo(p2.two);
                }
            });
        }
    }

    public static String getTooltipProjectName(BoggledTerraformingRequirement.RequirementContext ctx, BoggledTerraformingProject currentProject) {
        if(currentProject == null) {
            return noneProjectId;
        }

        return currentProject.getProjectTooltip();
    }

    public static int getLastDayCheckedForConstruction(SectorEntityToken stationEntity) {
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.constructionProgressLastDayChecked)) {
                return Integer.parseInt(tag.replaceAll(BoggledTags.constructionProgressLastDayChecked, ""));
            }
        }

        return 0;
    }

    public static void clearClockCheckTagsForConstruction(SectorEntityToken stationEntity) {
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

    public static void clearBoggledTerraformingControllerTags(MarketAPI market) {
        String tagToDelete = null;
        for (String tag : market.getTags()) {
            if (tag.contains(BoggledTags.terraformingController)) {
                tagToDelete = tag;
                break;
            }
        }

        if(tagToDelete != null) {
            market.removeTag(tagToDelete);
            clearBoggledTerraformingControllerTags(market);
        }
    }

    public static String getStationTypeName(SectorEntityToken stationEntity) {
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.stationNamePrefix)) {
                String workingTag = tag.toLowerCase();
                if (workingTag.endsWith(" station")) {
                    return workingTag.substring(BoggledTags.stationNamePrefix.length(), workingTag.length() - " station".length());
                }
                return workingTag.substring(BoggledTags.stationNamePrefix.length());
            }
        }
        return "unknown";
    }

    public static int getConstructionProgressDays(SectorEntityToken stationEntity) {
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.constructionProgressDays)) {
                return Integer.parseInt(tag.substring(BoggledTags.constructionProgressDays.length()));
            }
        }
        return 0;
    }

    public static int getConstructionRequiredDays(SectorEntityToken stationEntity) {
        for (String tag : stationEntity.getTags()) {
            if (tag.contains(BoggledTags.constructionRequiredDays)) {
                return Integer.parseInt(tag.substring(BoggledTags.constructionRequiredDays.length()));
            }
        }
        return 0;
    }

    public static void clearProgressCheckTagsForConstruction(SectorEntityToken stationEntity) {
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

    public static void incrementConstructionProgressDays(SectorEntityToken stationEntity, int amount) {
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

    public static int[] getQuantitiesForStableLocationConstruction(String type) {
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

    public static SectorEntityToken getPlanetTokenForQuest(String systemId, String entityId) {
        StarSystemAPI system = Global.getSector().getStarSystem(systemId);
        if (system == null) {
            return null;
        }
        SectorEntityToken possibleTarget = system.getEntityById(entityId);
        if (possibleTarget == null) {
            return null;
        }
        if(!(possibleTarget instanceof PlanetAPI)) {
            return null;
        }
        return possibleTarget;
    }

    public static int getIntSetting(String key) {
        Integer val = LunaSettings.getInt(BoggledMods.tascModId, key);
        if (val != null) {
            return val;
        }
        return 0;
    }

    public static boolean getBooleanSetting(String key) {
        if (aotdIgnoreSettings.contains(key)) {
            return true;
        }

        Boolean val = LunaSettings.getBoolean(BoggledMods.tascModId, key);
        if (val != null) {
            return val;
        }
        return false;
    }

    public static float getFloatSetting(String key) {
        Float val = LunaSettings.getFloat(BoggledMods.tascModId, key);
        if (val != null) {
            return val;
        }
        return 0f;
    }

    public static boolean isResearched(String key)
    {
        // Pass this.getId() as key if this function is called from an industry
        if(Global.getSettings().getModManager().isModEnabled("aotd_vok"))
        {
            return AoTDMainResearchManager.getInstance().isResearchedForPlayer(key);
        }
        else
        {
            // TASC does not have built-in research functionality.
            // Always return true if the player is not using a mod that implements research.
            return true;
        }
    }

    public static String getIndustryUnavailableReasonResearchRequiredString()
    {
        return "This building must be researched before it can be constructed.";
    }

    public static boolean domainEraArtifactDemandEnabled()
    {
        if(getBooleanSetting(BoggledSettings.domainTechContentEnabled) && getBooleanSetting(BoggledSettings.domainArchaeologyEnabled))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void setDomainEraArtifactDemandIfEnabled(BaseIndustry baseIndustry, String commodityId, int amount)
    {
        if(domainEraArtifactDemandEnabled())
        {
            baseIndustry.demand(commodityId, amount);
        }
    }


    public static void writeMessageToLog(String message)
    {
        Global.getLogger(boggledTools.class).info(message);
    }

    //import boggled.campaign.econ.boggledTools; SectorEntityToken ent = null; for(SectorEntityToken entity : Global.getSector().getPlayerFleet().getStarSystem().getAllEntities()) {if(entity.getFullName().contains("Stellar ")) {ent = entity;}}; boggledTools.sendDebugIntelMessage(Float.toString(ent.getCircularOrbitPeriod()));
    public static void sendDebugIntelMessage(String message) {
        MessageIntel intel = new MessageIntel(message, Misc.getBasePlayerColor());
        intel.addLine(message);
        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, null);
    }


    public static void terraformDebug(MarketAPI market) {
        market.getPlanetEntity().changeType(boggledTools.waterPlanetId, null);
        sendDebugIntelMessage(market.getPlanetEntity().getTypeId());
        sendDebugIntelMessage(market.getPlanetEntity().getSpec().getPlanetType());
    }
}