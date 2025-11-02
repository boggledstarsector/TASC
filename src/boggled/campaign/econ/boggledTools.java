package boggled.campaign.econ;

import boggled.campaign.econ.conditions.Terraforming_Controller;
import boggled.campaign.econ.industries.Boggled_Ismara_Sling;
import boggled.scripts.*;
import boggled.terraforming.*;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.*;
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;
import illustratedEntities.helper.ImageHandler;
import illustratedEntities.helper.Settings;
import illustratedEntities.helper.TextHandler;
import illustratedEntities.memory.ImageDataMemory;
import illustratedEntities.memory.TextDataEntry;
import illustratedEntities.memory.TextDataMemory;
import lunalib.lunaSettings.LunaSettings;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.String;
import java.util.*;
import java.util.List;
import java.util.ArrayList;

import static java.util.Arrays.asList;

public class boggledTools {
    public static class BoggledMods {
        public static final String lunalibModId = "lunalib";
        public static final String illustratedEntitiesModId = "illustrated_entities";
        public static final String tascModId = "Terraforming & Station Construction";

        public static final String atodVokModId = "aotd_vok";
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

        public static final String perihelionProjectDaysToFinish = "boggledPerihelionProjectDaysToFinish";
        public static final String removeRadiationProjectEnabled = "boggledTerraformingRemoveRadiationProjectEnabled";
        public static final String removeAtmosphereProjectEnabled = "boggledTerraformingRemoveAtmosphereProjectEnabled";

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

        public static final String ismaraSlingAsteroidProcessingId = "BOGGLED_ISMARA_SLING";

        public static final String atmosphereProcessorId = "BOGGLED_ATMOSPHERE_PROCESSOR";
    }

    public static class BoggledResearchProjects {
        public static final String resourceManipulation = "tasc_resource_manipulation";
        public static final String atmosphereManipulation = "tasc_atmosphere_manipulation";
        public static final String planetTypeManipulation = "tasc_planet_type_manipulation";
    }

    public static class TascPlanetTypes {
        public static final String starPlanetId = "star";
        public static final String barrenPlanetId = "barren";
        public static final String desertPlanetId = "desert";
        public static final String frozenPlanetId = "frozen";
        public static final String gasGiantPlanetId = "gas_giant";
        public static final String junglePlanetId = "jungle";
        public static final String terranPlanetId = "terran";
        public static final String toxicPlanetId = "toxic";
        public static final String tundraPlanetId = "tundra";
        public static final String volcanicPlanetId = "volcanic";
        public static final String waterPlanetId = "water";
        public static final String unknownPlanetId = "unknown";
    }

    private static final Set<String> validPlanetTypes = new HashSet<>(Arrays.asList(
            TascPlanetTypes.starPlanetId,
            TascPlanetTypes.barrenPlanetId,
            TascPlanetTypes.desertPlanetId,
            TascPlanetTypes.frozenPlanetId,
            TascPlanetTypes.gasGiantPlanetId,
            TascPlanetTypes.junglePlanetId,
            TascPlanetTypes.terranPlanetId,
            TascPlanetTypes.toxicPlanetId,
            TascPlanetTypes.tundraPlanetId,
            TascPlanetTypes.volcanicPlanetId,
            TascPlanetTypes.waterPlanetId,
            TascPlanetTypes.unknownPlanetId
    ));

    public enum PlanetWaterLevel {
        LOW_WATER, MEDIUM_WATER, HIGH_WATER
    }

    public static PlanetWaterLevel getGreaterWaterLevel(PlanetWaterLevel level1, PlanetWaterLevel level2) {
        // Enums implement the Comparable interface.
        // compareTo returns:
        // - a negative number if level1 comes before level2
        // - 0 if they are the same
        // - a positive number if level1 comes after level2 (is "greater")
        if (level1.compareTo(level2) >= 0) {
            return level1;
        } else {
            return level2;
        }
    }

    private static final HashMap<String, String> planetTypeIdToTascPlanetTypeMapping = new HashMap<>();

    private static final HashMap<String, HashSet<String>> tascPlanetTypeToAllPlanetTypeIdsMapping = new HashMap<>();

    private static final HashMap<String, PlanetSpecAPI> planetTypeIdToPlanetSpecApiMapping = new HashMap<>();

    private static final HashMap<String, PlanetWaterLevel> tascPlanetTypeToBaseWaterLevelMapping = new HashMap<>(){{
        put(TascPlanetTypes.starPlanetId, PlanetWaterLevel.LOW_WATER);
        put(TascPlanetTypes.barrenPlanetId, PlanetWaterLevel.LOW_WATER);
        put(TascPlanetTypes.desertPlanetId, PlanetWaterLevel.MEDIUM_WATER);
        put(TascPlanetTypes.frozenPlanetId, PlanetWaterLevel.HIGH_WATER);
        put(TascPlanetTypes.gasGiantPlanetId, PlanetWaterLevel.LOW_WATER);
        put(TascPlanetTypes.junglePlanetId, PlanetWaterLevel.HIGH_WATER);
        put(TascPlanetTypes.terranPlanetId, PlanetWaterLevel.HIGH_WATER);
        put(TascPlanetTypes.toxicPlanetId, PlanetWaterLevel.LOW_WATER);
        put(TascPlanetTypes.tundraPlanetId, PlanetWaterLevel.MEDIUM_WATER);
        put(TascPlanetTypes.volcanicPlanetId, PlanetWaterLevel.LOW_WATER);
        put(TascPlanetTypes.waterPlanetId, PlanetWaterLevel.HIGH_WATER);
        put(TascPlanetTypes.unknownPlanetId, PlanetWaterLevel.LOW_WATER);
    }};

    // Stores the organics and volatiles base and maximum amounts for each TASC planet type.
    // Four elements in the ArrayList - 1st = organics base, 2nd = organics max, 3rd = volatiles base, 4th = volatiles max
    // Zero = None, One = lowest amount (-1), Four = highest amount (+2)
    // All habitable planet types can max out farmland.
    private static final HashMap<String, ArrayList<Integer>> tascPlanetTypeToResourceLevelMapping = new HashMap<>(){{
        put(TascPlanetTypes.starPlanetId, new ArrayList<>(Arrays.asList(0, 0, 0, 0)));
        put(TascPlanetTypes.barrenPlanetId, new ArrayList<>(Arrays.asList(0, 0, 0, 0)));
        put(TascPlanetTypes.desertPlanetId, new ArrayList<>(Arrays.asList(1, 2, 0, 1)));
        put(TascPlanetTypes.frozenPlanetId, new ArrayList<>(Arrays.asList(0, 0, 2, 4)));
        put(TascPlanetTypes.gasGiantPlanetId, new ArrayList<>(Arrays.asList(0, 0, 0, 0)));
        put(TascPlanetTypes.junglePlanetId, new ArrayList<>(Arrays.asList(2, 4, 0, 0)));
        put(TascPlanetTypes.terranPlanetId, new ArrayList<>(Arrays.asList(2, 4, 1, 4)));
        put(TascPlanetTypes.toxicPlanetId, new ArrayList<>(Arrays.asList(0, 0, 0, 0)));
        put(TascPlanetTypes.tundraPlanetId, new ArrayList<>(Arrays.asList(0, 1, 2, 4)));
        put(TascPlanetTypes.volcanicPlanetId, new ArrayList<>(Arrays.asList(0, 0, 0, 0)));
        put(TascPlanetTypes.waterPlanetId, new ArrayList<>(Arrays.asList(2, 4, 0, 3)));
        put(TascPlanetTypes.unknownPlanetId, new ArrayList<>(Arrays.asList(0, 0, 0, 0)));
    }};

    private static final HashMap<String, Boolean> tascPlanetTypeCanImproveFarmlandMapping = new HashMap<>(){{
        put(TascPlanetTypes.starPlanetId, false);
        put(TascPlanetTypes.barrenPlanetId, false);
        put(TascPlanetTypes.desertPlanetId, true);
        put(TascPlanetTypes.frozenPlanetId, false);
        put(TascPlanetTypes.gasGiantPlanetId, false);
        put(TascPlanetTypes.junglePlanetId, true);
        put(TascPlanetTypes.terranPlanetId, true);
        put(TascPlanetTypes.toxicPlanetId, false);
        put(TascPlanetTypes.tundraPlanetId, true);
        put(TascPlanetTypes.volcanicPlanetId, false);
        put(TascPlanetTypes.waterPlanetId, false);
        put(TascPlanetTypes.unknownPlanetId, false);
    }};

    private static final HashMap<String, Boolean> tascPlanetTypeAllowsForTerraforming = new HashMap<>(){{
        put(TascPlanetTypes.starPlanetId, false);
        put(TascPlanetTypes.barrenPlanetId, true);
        put(TascPlanetTypes.desertPlanetId, true);
        put(TascPlanetTypes.frozenPlanetId, true);
        put(TascPlanetTypes.gasGiantPlanetId, false);
        put(TascPlanetTypes.junglePlanetId, true);
        put(TascPlanetTypes.terranPlanetId, true);
        put(TascPlanetTypes.toxicPlanetId, true);
        put(TascPlanetTypes.tundraPlanetId, true);
        put(TascPlanetTypes.volcanicPlanetId, false);
        put(TascPlanetTypes.waterPlanetId, true);
        put(TascPlanetTypes.unknownPlanetId, false);
    }};

    public static final HashMap<String, Pair<String, String>> tascPlanetTypeDisplayStringMap = new HashMap<>(){{
        put(TascPlanetTypes.starPlanetId, new Pair<>("Star", "star"));
        put(TascPlanetTypes.barrenPlanetId, new Pair<>("Barren", "barren"));
        put(TascPlanetTypes.desertPlanetId, new Pair<>("Desert", "desert"));
        put(TascPlanetTypes.frozenPlanetId, new Pair<>("Frozen", "frozen"));
        put(TascPlanetTypes.gasGiantPlanetId, new Pair<>("Gas giant", "gas giant"));
        put(TascPlanetTypes.junglePlanetId, new Pair<>("Jungle", "jungle"));
        put(TascPlanetTypes.terranPlanetId, new Pair<>("Terran", "terran"));
        put(TascPlanetTypes.toxicPlanetId, new Pair<>("Toxic", "toxic"));
        put(TascPlanetTypes.tundraPlanetId, new Pair<>("Tundra", "tundra"));
        put(TascPlanetTypes.volcanicPlanetId, new Pair<>("Volcanic", "volcanic"));
        put(TascPlanetTypes.waterPlanetId, new Pair<>("Water", "water"));
        put(TascPlanetTypes.unknownPlanetId, new Pair<>("Unknown", "unknown"));
    }};

    private static final HashMap<String, Boolean> tascPlanetTypeAllowsForHumanHabitability = new HashMap<>(){{
        put(TascPlanetTypes.starPlanetId, false);
        put(TascPlanetTypes.barrenPlanetId, false);
        put(TascPlanetTypes.desertPlanetId, true);
        put(TascPlanetTypes.frozenPlanetId, false);
        put(TascPlanetTypes.gasGiantPlanetId, false);
        put(TascPlanetTypes.junglePlanetId, true);
        put(TascPlanetTypes.terranPlanetId, true);
        put(TascPlanetTypes.toxicPlanetId, false);
        put(TascPlanetTypes.tundraPlanetId, true);
        put(TascPlanetTypes.volcanicPlanetId, false);
        put(TascPlanetTypes.waterPlanetId, true);
        put(TascPlanetTypes.unknownPlanetId, false);
    }};

    private static final HashMap<Integer, String> intToOrganicsLevel = new HashMap<>(){{
        put(0, null);
        put(1, "organics_trace");
        put(2, "organics_common");
        put(3, "organics_abundant");
        put(4, "organics_plentiful");
    }};

    private static final HashMap<Integer, String> intToVolatilesLevel = new HashMap<>(){{
        put(0, null);
        put(1, "volatiles_trace");
        put(2, "volatiles_diffuse");
        put(3, "volatiles_abundant");
        put(4, "volatiles_plentiful");
    }};

    private static final HashMap<Integer, String> intToFarmlandLevel = new HashMap<>(){{
        put(0, null);
        put(1, "farmland_poor");
        put(2, "farmland_adequate");
        put(3, "farmland_rich");
        put(4, "farmland_bountiful");
    }};

    public static String getNextFarmlandConditionId(MarketAPI market)
    {
        if(market.hasCondition("farmland_poor"))
        {
            return "farmland_adequate";
        }
        else if(market.hasCondition("farmland_adequate"))
        {
            return "farmland_rich";
        }
        else if(market.hasCondition("farmland_rich"))
        {
            return "farmland_bountiful";
        }
        else if(market.hasCondition("farmland_bountiful"))
        {
            return null;
        }
        else
        {
            return "farmland_poor";
        }
    }
    public static String getCurrentFarmlandString(MarketAPI market)
    {
        if(market.hasCondition("farmland_poor"))
        {
            return getConditionFromString("farmland_poor").getName();
        }
        else if(market.hasCondition("farmland_adequate"))
        {
            return getConditionFromString("farmland_adequate").getName();
        }
        else if(market.hasCondition("farmland_rich"))
        {
            return getConditionFromString("farmland_rich").getName();
        }
        else if(market.hasCondition("farmland_bountiful"))
        {
            return getConditionFromString("farmland_bountiful").getName();
        }
        else
        {
            return null;
        }
    }

    public static String getNextFarmlandString(MarketAPI market)
    {
        Integer nextLevel = getNextFarmlandLevelInteger(market);
        if(nextLevel != null)
        {
            return getConditionFromString(intToFarmlandLevel.get(nextLevel)).getName();
        }
        else
        {
            return null;
        }
    }

    public static int getFarmlandLevelInteger(MarketAPI market)
    {
        if(market.hasCondition("farmland_poor"))
        {
            return 1;
        }
        else if(market.hasCondition("farmland_adequate"))
        {
            return 2;
        }
        else if(market.hasCondition("farmland_rich"))
        {
            return 3;
        }
        else if(market.hasCondition("farmland_bountiful"))
        {
            return 4;
        }
        else
        {
            return 0;
        }
    }

    public static Integer getNextFarmlandLevelInteger(MarketAPI market)
    {
        int currentLevel = getFarmlandLevelInteger(market);
        return getNextFarmlandLevelInteger(currentLevel);
    }

    public static Integer getNextFarmlandLevelInteger(int farmlandLevelInt)
    {
        if(farmlandLevelInt < 4)
        {
            return farmlandLevelInt + 1;
        }
        else
        {
            return null;
        }
    }

    public static MarketConditionSpecAPI getConditionFromString(String condition)
    {
        return Global.getSettings().getMarketConditionSpec(condition);
    }

    public static int getBaseOrganicsLevelForTascPlanetType(String tascPlanetType)
    {
        return tascPlanetTypeToResourceLevelMapping.get(tascPlanetType).get(0);
    }

    public static int getCurrentOrganicsLevelForMarket(MarketAPI market)
    {
        if(market.hasCondition("organics_trace"))
        {
            return 1;
        }
        else if(market.hasCondition("organics_common"))
        {
            return 2;
        }
        else if(market.hasCondition("organics_abundant"))
        {
            return 3;
        }
        else if(market.hasCondition("organics_plentiful"))
        {
            return 4;
        }
        else
        {
            return 0;
        }
    }

    public static int getCurrentVolatilesLevelForMarket(MarketAPI market)
    {
        if(market.hasCondition("volatiles_trace"))
        {
            return 1;
        }
        else if(market.hasCondition("volatiles_diffuse"))
        {
            return 2;
        }
        else if(market.hasCondition("volatiles_abundant"))
        {
            return 3;
        }
        else if(market.hasCondition("volatiles_plentiful"))
        {
            return 4;
        }
        else
        {
            return 0;
        }
    }

    public static String getConditionIdForBaseOrganicsLevelForTascPlanetType(String tascPlanetType)
    {
        return intToOrganicsLevel.get(tascPlanetTypeToResourceLevelMapping.get(tascPlanetType).get(0));
    }

    public static int getMaxOrganicsLevelForTascPlanetType(String tascPlanetType)
    {
        return tascPlanetTypeToResourceLevelMapping.get(tascPlanetType).get(1);
    }

    public static String getConditionIdForMaxOrganicsLevelForTascPlanetType(String tascPlanetType)
    {
        return intToOrganicsLevel.get(tascPlanetTypeToResourceLevelMapping.get(tascPlanetType).get(1));
    }

    public static String getNextOrganicsConditionId(int currentOrganicsLevel)
    {
        // Calculate the next level by incrementing the current level.
        int nextLevel = currentOrganicsLevel + 1;

        // Cap the level at the maximum allowed value, which is 4 (organics_plentiful).
        int finalLevel = Math.min(nextLevel, 4);

        // Look up the corresponding String ID in the map.
        // For levels 1, 2, 3, and 4, it returns the correct string.
        // Note: Level 0 maps to null, which would only happen if currentOrganicsLevel was -1.
        return intToOrganicsLevel.get(finalLevel);
    }

    public static String getNextOrganicsConditionId(MarketAPI market)
    {
        int currentLevel = 0;
        if(market.hasCondition("organics_trace"))
        {
            currentLevel = 1;
        }
        else if(market.hasCondition("organics_common"))
        {
            currentLevel = 2;
        }
        else if(market.hasCondition("organics_abundant"))
        {
            currentLevel = 3;
        }
        else if(market.hasCondition("organics_plentiful"))
        {
            currentLevel = 4;
        }

        return getNextOrganicsConditionId(currentLevel);
    }

    public static String getNextVolatilesConditionId(int currentVolatilesLevel)
    {
        // Calculate the next level by incrementing the current level.
        int nextLevel = currentVolatilesLevel + 1;

        // Cap the level at the maximum allowed value, which is 4 (organics_plentiful).
        int finalLevel = Math.min(nextLevel, 4);

        // Look up the corresponding String ID in the map.
        // For levels 1, 2, 3, and 4, it returns the correct string.
        // Note: Level 0 maps to null, which would only happen if currentOrganicsLevel was -1.
        return intToVolatilesLevel.get(finalLevel);
    }

    public static String getNextVolatilesConditionId(MarketAPI market)
    {
        int currentLevel = 0;
        if(market.hasCondition("volatiles_trace"))
        {
            currentLevel = 1;
        }
        else if(market.hasCondition("volatiles_diffuse"))
        {
            currentLevel = 2;
        }
        else if(market.hasCondition("volatiles_abundant"))
        {
            currentLevel = 3;
        }
        else if(market.hasCondition("volatiles_plentiful"))
        {
            currentLevel = 4;
        }

        return getNextVolatilesConditionId(currentLevel);
    }

    public static String getOrganicsConditionIdForInteger(int level)
    {
        return intToOrganicsLevel.get(level);
    }

    public static int getBaseVolatilesLevelForTascPlanetType(String tascPlanetType)
    {
        return tascPlanetTypeToResourceLevelMapping.get(tascPlanetType).get(2);
    }

    public static String getConditionIdForBaseVolatilesLevelForTascPlanetType(String tascPlanetType)
    {
        return intToVolatilesLevel.get(tascPlanetTypeToResourceLevelMapping.get(tascPlanetType).get(2));
    }

    public static int getMaxVolatilesLevelForTascPlanetType(String tascPlanetType)
    {
        return tascPlanetTypeToResourceLevelMapping.get(tascPlanetType).get(3);
    }

    public static String getConditionIdForMaxVolatilesLevelForTascPlanetType(String tascPlanetType)
    {
        return intToVolatilesLevel.get(tascPlanetTypeToResourceLevelMapping.get(tascPlanetType).get(3));
    }

    public static boolean tascPlanetTypeSupportsFarmland(String tascPlanetType)
    {
        return tascPlanetTypeCanImproveFarmlandMapping.get(tascPlanetType);
    }

    public static String getTascPlanetType(PlanetAPI planet)
    {
        if(planet == null || planet.getSpec() == null || planet.getSpec().getPlanetType() == null) {
            return TascPlanetTypes.unknownPlanetId;
        }

        String planetTypeId = planet.getSpec().getPlanetType();
        return planetTypeIdToTascPlanetTypeMapping.getOrDefault(planetTypeId, TascPlanetTypes.unknownPlanetId);
    }

    public static String getTascPlanetType(String planetTypeId)
    {
        return planetTypeIdToTascPlanetTypeMapping.getOrDefault(planetTypeId, TascPlanetTypes.unknownPlanetId);
    }

    public static boolean tascPlanetTypeAllowsTerraforming(String tascPlanetType)
    {
        return tascPlanetTypeAllowsForTerraforming.getOrDefault(tascPlanetType, false);
    }

    public static boolean tascPlanetTypeAllowsHumanHabitability(String tascPlanetType)
    {
        return tascPlanetTypeAllowsForHumanHabitability.getOrDefault(tascPlanetType, false);
    }

    public static HashSet<String> getAllPlanetTypeIdsForTascPlanetType(String tascPlanetType)
    {
        return tascPlanetTypeToAllPlanetTypeIdsMapping.getOrDefault(tascPlanetType, new HashSet<>());
    }

    public static PlanetWaterLevel getBaseWaterLevelForTascPlanetType(String tascPlanetType)
    {
        return tascPlanetTypeToBaseWaterLevelMapping.getOrDefault(tascPlanetType, PlanetWaterLevel.LOW_WATER);
    }

    public static PlanetWaterLevel getWaterLevelForMarket(MarketAPI market)
    {
        PlanetWaterLevel sourceInSystem = marketHasWaterSourceInSystem(market) ? PlanetWaterLevel.HIGH_WATER : PlanetWaterLevel.LOW_WATER;
        PlanetWaterLevel baseWaterLevelForPlanetType = getBaseWaterLevelForTascPlanetType(boggledTools.getTascPlanetType(market.getPlanetEntity()));
        return getGreaterWaterLevel(sourceInSystem, baseWaterLevelForPlanetType);
    }

    public static boolean marketHasWaterSourceInSystem(MarketAPI market)
    {
        if(market.getStarSystem() == null)
        {
            return false;
        }

        for(Pair<MarketAPI, WaterIndustryStatus> marketStatus : getWaterIndustryStatusForSystem(market.getStarSystem()))
        {
            if(marketStatus.two == WaterIndustryStatus.OPERATIONAL)
            {
                return true;
            }
        }

        return false;
    }

    public enum WaterIndustryStatus {
        OPERATIONAL,
        UNDER_CONSTRUCTION,
        DISRUPTED,
        SHORTAGE
    }

    public static ArrayList<Pair<MarketAPI, WaterIndustryStatus>> getWaterIndustryStatusForSystem(StarSystemAPI system)
    {
        ArrayList<Pair<MarketAPI, WaterIndustryStatus>> waterIndustries = new ArrayList<>();
        for(MarketAPI systemMarket : Global.getSector().getEconomy().getMarkets(system))
        {
            if(systemMarket.isPlayerOwned() && systemMarket.hasIndustry(BoggledIndustries.ismaraSlingAsteroidProcessingId))
            {
                Boggled_Ismara_Sling waterIndustry = (Boggled_Ismara_Sling) systemMarket.getIndustry(BoggledIndustries.ismaraSlingAsteroidProcessingId);
                if(waterIndustry.isDisrupted())
                {
                    waterIndustries.add(new Pair<>(systemMarket, WaterIndustryStatus.DISRUPTED));
                }
                else if(!waterIndustry.isFunctional())
                {
                    waterIndustries.add(new Pair<>(systemMarket, WaterIndustryStatus.UNDER_CONSTRUCTION));
                }
                else if(waterIndustry.ismaraSlingHasShortage())
                {
                    waterIndustries.add(new Pair<>(systemMarket, WaterIndustryStatus.SHORTAGE));
                }
                else
                {
                    waterIndustries.add(new Pair<>(systemMarket, WaterIndustryStatus.OPERATIONAL));
                }
            }
        }

        return waterIndustries;
    }

    public static class BoggledConditions {
        public static final String terraformingControllerConditionId = "terraforming_controller";
        public static final String spriteControllerConditionId = "sprite_controller";

        public static final String crampedQuartersConditionId = "cramped_quarters";
    }

    public static Set<String> aotdIgnoreSettings = new HashSet<>();

    public static void initialiseModIgnoreSettings() {
        aotdIgnoreSettings.add("boggledTerraformingContentEnabled");

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

    public static void initializeStellarReflectorArraySuppressedConditionsFromJSON(@NotNull JSONArray stellarReflectorArraySuppressedConditionsJSON) {
        HashSet<String> conditions = getStringListFromJson(stellarReflectorArraySuppressedConditionsJSON, "condition_id");

        boggledTools.stellarReflectorArraySuppressedConditions = new ArrayList<String>(conditions);
    }

    public static void initializeDomedCitiesSuppressedConditionsFromJSON(@NotNull JSONArray domedCitiesSuppressedConditionsJSON) {
        HashSet<String> conditions = getStringListFromJson(domedCitiesSuppressedConditionsJSON, "condition_id");

        boggledTools.domedCitiesSuppressedConditions = new ArrayList<String>(conditions);
    }

    public static void initializePlanetMappingsFromJSON(@NotNull JSONArray planetsJson)
    {
        for (int i = 0; i < planetsJson.length(); ++i) {
            try
            {
                JSONObject row = planetsJson.getJSONObject(i);
                String planetTypeId = row.getString("planet_type_id");
                String tascPlanetType = row.getString("tasc_planet_type");
                if(planetTypeId == null || planetTypeId.isBlank() || tascPlanetType == null || tascPlanetType.isBlank() || !validPlanetTypes.contains(tascPlanetType))
                {
                    throw new RuntimeException("You have a blank cell in the planet mapping CSV file, or you've specified an invalid TASC planet type. Delete the file and replace it with the original.");
                }

                planetTypeIdToTascPlanetTypeMapping.put(planetTypeId, tascPlanetType);
                if(tascPlanetTypeToAllPlanetTypeIdsMapping.containsKey(tascPlanetType))
                {
                    tascPlanetTypeToAllPlanetTypeIdsMapping.get(tascPlanetType).add(planetTypeId);
                }
                else
                {
                    tascPlanetTypeToAllPlanetTypeIdsMapping.put(tascPlanetType, new HashSet<>(){{
                        add(planetTypeId);
                    }});
                }

            }
            catch (JSONException e) {
                // We can't swallow this exception because the game won't work correctly if the data isn't loaded
                throw new RuntimeException("Error in planet mapping JSON parsing: " + e);
            }
        }

        List<PlanetSpecAPI> planetSpecList = Global.getSettings().getAllPlanetSpecs();
        for(PlanetSpecAPI planetSpec : planetSpecList)
        {
            planetTypeIdToPlanetSpecApiMapping.put(planetSpec.getPlanetType(), planetSpec);
        }
    }

    public static ArrayList<BoggledBaseTerraformingProject> getTerraformingProjects(MarketAPI market)
    {
        ArrayList<BoggledBaseTerraformingProject> projects = new ArrayList<>();
        projects.add(new PlanetTypeChangeTerran(market));
        projects.add(new PlanetTypeChangeWater(market));
        projects.add(new PlanetTypeChangeArid(market));
        projects.add(new PlanetTypeChangeJungle(market));
        projects.add(new PlanetTypeChangeTundra(market));
        projects.add(new PlanetTypeChangeFrozen(market));

        projects.add(new ConditionModificationAddHabitable(market));
        projects.add(new ConditionModificationAddMildClimate(market));
        projects.add(new ConditionModificationRemoveExtremeWeather(market));
        projects.add(new ConditionModificationRemoveNoAtmosphere(market));
        projects.add(new ConditionModificationRemoveThinAtmosphere(market));
        projects.add(new ConditionModificationRemoveDenseAtmosphere(market));
        projects.add(new ConditionModificationRemoveToxicAtmosphere(market));
        if(ConditionModificationRemoveAtmosphere.isEnabledViaSettings())
        {
            projects.add(new ConditionModificationRemoveAtmosphere(market));
        }
        if(ConditionModificationRemoveIrradiated.isEnabledViaSettings())
        {
            projects.add(new ConditionModificationRemoveIrradiated(market));
        }

        projects.add(new ResourceImprovementFarmland(market));
        projects.add(new ResourceImprovementOrganics(market));
        projects.add(new ResourceImprovementVolatiles(market));

        // Call methods in other mods here to get their custom terraforming projects

        return projects;
    }

    public static Terraforming_Controller getTerraformingControllerFromMarket(MarketAPI market)
    {
        return (Terraforming_Controller) market.getCondition(boggledTools.BoggledConditions.terraformingControllerConditionId).getPlugin();
    }

    public static PlanetSpecAPI getPlanetSpec(String planetTypeId)
    {
        return planetTypeIdToPlanetSpecApiMapping.getOrDefault(planetTypeId, null);
    }

    public static HashSet<String> getStringListFromJson(JSONArray json, String key) {
        HashSet<String> stringSet = new HashSet<>();

        for (int i = 0; i < json.length(); ++i) {
            try {
                JSONObject row = json.getJSONObject(i);

                String condition_id = row.getString(key);
                if (condition_id != null && !condition_id.isEmpty()) {
                    stringSet.add(condition_id);
                }
            } catch (JSONException e) {
                // We can't swallow this exception because the game won't work correctly if the data isn't loaded
                throw new RuntimeException("Error in string list JSON parsing: " + e);
            }
        }

        return stringSet;
    }

    public static List<String> getDomedCitiesSuppressedConditions() {
        return boggledTools.domedCitiesSuppressedConditions;
    }

    public static List<String> getStellarReflectorArraySuppressedConditions() {
        return boggledTools.stellarReflectorArraySuppressedConditions;
    }

    private static List<String> domedCitiesSuppressedConditions;
    private static List<String> stellarReflectorArraySuppressedConditions;

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

        if (boggledTools.getTascPlanetType(market.getPlanetEntity()).equals(TascPlanetTypes.desertPlanetId) || boggledTools.getTascPlanetType(market.getPlanetEntity()).equals(TascPlanetTypes.junglePlanetId)) {
            return false;
        } else if (boggledTools.getTascPlanetType(market.getPlanetEntity()).equals(TascPlanetTypes.tundraPlanetId) || boggledTools.getTascPlanetType(market.getPlanetEntity()).equals(TascPlanetTypes.frozenPlanetId)) {
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
        final float EPSILON = 0.0001f;

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

    public static MarketConditionAPI addCondition(MarketAPI market, String condition) {
        if(!market.hasCondition(condition))
        {
            market.addCondition(condition);
            boggledTools.surveyAll(market);
        }

        return market.getCondition(condition);
    }

    public static void removeCondition(MarketAPI market, String condition) {
        if(market != null && market.hasCondition(condition)) {
            market.removeCondition(condition);
            boggledTools.surveyAll(market);
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
}