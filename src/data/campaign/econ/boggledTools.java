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
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
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

import java.awt.*;
import java.util.*;
import java.util.List;

import static java.util.Arrays.asList;

public class boggledTools
{
    public static class BoggledMods {
        public static final String lunalibModID = "lunalib";
        public static final String illustratedEntitiesModID = "illustrated_entities";
        public static final String tascModID = "Terraforming & Station Construction";
    }

    private static class BoggledSettings {
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

        public static final String domainArchaeologyEnabled = "boggledDomainArchaeologyEnabled";

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
        public static final String atmosphereProcessorIndustryID = "BOGGLED_ATMOSPHERE_PROCESSOR";
        public static final String genelabIndustryID = "BOGGLED_GENELAB";
        public static final String ismaraSlingIndustryID = "BOGGLED_ISMARA_SLING";
    }

    private static final String starPlanetID = "star";

    private static final String aridPlanetID = "arid";
    private static final String barrenPlanetID = "barren";
    private static final String desertPlanetID = "desert";
    private static final String frozenPlanetID = "frozen";
    private static final String gasGiantPlanetID = "gas_giant";
    private static final String junglePlanetID = "jungle";
    private static final String terranPlanetID = "terran";
    private static final String toxicPlanetID = "toxic";
    private static final String tundraPlanetID = "tundra";
    private static final String volcanicPlanetID = "volcanic";
    private static final String waterPlanetID = "water";

    private static final String unknownPlanetID = "unknown";

    public static final String terraformingControllerConditionID = "terraforming_controller";
    private static final String spriteControllerConditionID = "sprite_controller";
    private static final String crampedQuartersConditionID = "cramped_quarters";

    // A mistyped string compiles fine and leads to plenty of debugging. A mistyped constant gives an error.
    private static final String colonyNotJungleWorld = "Colony is not already a jungle world";
    private static final String colonyNotAridWorld = "Colony is not already an arid world";
    private static final String colonyNotTerranWorld = "Colony is not already a Terran world";
    private static final String colonyNotWaterWorld = "Colony is not already a water world";
    private static final String colonyNotTundraWorld = "Colony is not already a tundra world";
    private static final String colonyNotFrozenWorld = "Colony is not already a frozen world";

    private static final String colonyBarrenOrFrozen = "Colony is barren or frozen";
    private static final String colonyAtmosphericDensityNormal = "Colony has normal atmospheric density";
    private static final String colonyAtmosphereNotToxicOrIrradiated = "Colony atmosphere is not toxic or irradiated";
    private static final String colonyNotColdOrVeryCold = "Colony is not cold or very cold";
    private static final String colonyHotOrVeryHot = "Colony is hot or very hot";
    private static final String colonyNotVeryColdOrVeryHot = "Colony is not very cold or very hot";
    private static final String colonyNotHotOrVeryHot = "Colony is not hot or very hot";
    private static final String colonyVeryCold = "Colony is very cold";
    private static final String colonyTemperateOrHot = "Colony is temperate or hot";
    private static final String colonyTemperateOrCold = "Colony is temperate or cold";

    private static final String colonyHasStellarReflectors = "Colony has stellar reflectors";
    private static final String colonyHasAtmosphereProcessor = "Colony has atmosphere processor";
    private static final String colonyHasGenelab = "Colony has genelab";

    private static final String colonyHasModerateWaterPresent = "Colony has a moderate amount of water present";
    private static final String colonyHasLargeWaterPresent = "Colony has a large amount of water present";

    private static final String colonyHabitable = "Colony is habitable";
    private static final String colonyNotAlreadyHabitable = "Colony is not already habitable";

    private static final String colonyExtremeWeather = "Colony has extreme weather";
    private static final String colonyNormalClimate = "Colony has normal climate";

    private static final String colonyAtmosphereToxic = "Colony atmosphere is toxic";
    private static final String colonyIrradiated = "Colony is irradiated";

    private static final String colonyHasAtmosphere = "Colony has atmosphere";
    private static final String colonyAtmosphereSuboptimalDensity = "Colony atmosphere has suboptimal density";

    private static final String worldTypeSupportsFarmlandImprovement = "World type supports further farmland improvement";
    private static final String worldTypeSupportsOrganicsImprovement = "World type supports further organics improvement";
    private static final String worldTypeSupportsVolatilesImprovement = "World type supports further volatiles improvement";

    private static final String worldTypeAllowsTerraforming = "World type allows for terraforming";
    private static final String worldTypeAllowsMildClimate = "World type allows for mild climate";
    private static final String worldTypeAllowsHumanHabitability = "World type allows for human habitability";

    private static final String colonyHasAtLeast100kInhabitants = "Colony has at least 100,000 inhabitants";

    private static final String colonyHasOrbitalWorksWPristineNanoforge = "Colony has orbital works with a pristine nanoforge";

    public static final String noneProjectID = "None";

    public static final String aridTypeChangeProjectID = "aridTypeChange";
    public static final String frozenTypeChangeProjectID = "frozenTypeChange";
    public static final String jungleTypeChangeProjectID = "jungleTypeChange";
    public static final String terranTypeChangeProjectID = "terranTypeChange";
    public static final String tundraTypeChangeProjectID = "tundraTypeChange";
    public static final String waterTypeChangeProjectID = "waterTypeChange";

    public static final String farmlandResourceImprovementProjectID = "farmlandResourceImprovement";
    public static final String organicsResourceImprovementProjectID = "organicsResourceImprovement";
    public static final String volatilesResourceImprovementProjectID = "volatilesResourceImprovement";

    public static final String extremeWeatherConditionImprovementProjectID = "extremeWeatherConditionImprovement";
    public static final String mildClimateConditionImprovementProjectID = "mildClimateConditionImprovement";
    public static final String habitableConditionImprovementProjectID = "habitableConditionImprovement";
    public static final String atmosphereDensityConditionImprovementProjectID = "atmosphereDensityConditionImprovement";
    public static final String toxicAtmosphereConditionImprovementProjectID = "toxicAtmosphereConditionImprovement";
    public static final String irradiatedConditionImprovementProjectID = "irradiatedConditionImprovement";
    public static final String radiationConditionImprovementProjectID = "irradiatedConditionImprovement";
    public static final String removeAtmosphereConditionImprovementProjectID = "removeAtmosphereConditionImprovement";

    private static final String aotd_TypeChangeResearchRequirement = "Researched: Terraforming Templates";
    private static final String aotd_ConditionImprovementResearchRequirement = "Researched : Atmosphere Manipulation";
    private static final String aotd_ResourceImprovementResearchRequirement = "Researched : Advanced Terraforming Templates";

    private static final String aotd_TypeChangeResearchRequirementID = "tasc_terraforming_templates";
    private static final String aotd_ConditionImprovementResearchRequirementID = "tasc_atmosphere_manipulation";
    private static final String aotd_ResourceImprovementResearchRequirementID = "tasc_advacned_terraforming";

    private static HashMap<String, String[]> initialiseProjectRequirements() {
        HashMap<String, String[]> ret = new HashMap<>();

        boolean aotd_Enabled = Global.getSettings().getModManager().isModEnabled("aotd_vok");

        // Requires:
        //  - Not already arid
        //  - Normal atmosphere
        //  - Normal atmosphere
        //  - Atmosphere is not toxic or irradiated
        //  - Colony is temperate or hot
        //  - Stellar Reflectors
        //  - Water Level of 1
        ArrayList<String> aridTypeChangeProjectReq = new ArrayList<>(asList(
                worldTypeAllowsTerraforming,
                colonyNotAridWorld,
                colonyAtmosphericDensityNormal,
                colonyAtmosphereNotToxicOrIrradiated,
                colonyTemperateOrHot,
                colonyHasStellarReflectors,
                colonyHasModerateWaterPresent
        ));

        // Requires:
        //  - Not already jungle
        //  - Normal atmosphere
        //  - Atmosphere is not toxic or irradiated
        //  - Colony is temperate or hot
        //  - Stellar Reflectors
        //  - Water Level of 1
        ArrayList<String> jungleTypeChangeProjectReq = new ArrayList<>(asList(
                worldTypeAllowsTerraforming,
                colonyNotJungleWorld,
                colonyAtmosphericDensityNormal,
                colonyAtmosphereNotToxicOrIrradiated,
                colonyTemperateOrHot,
                colonyHasStellarReflectors,
                colonyHasModerateWaterPresent
        ));

        // Requires:
        //  - Not already Terran
        //  - Normal atmosphere
        //  - Atmosphere is not toxic or irradiated
        //  - Not very cold or very hot temperature
        //  - Stellar Reflectors
        //  - Water Level of 1
        ArrayList<String> terranTypeChangeProjectReq = new ArrayList<>(asList(
                worldTypeAllowsTerraforming,
                colonyNotTerranWorld,
                colonyAtmosphericDensityNormal,
                colonyAtmosphereNotToxicOrIrradiated,
                colonyNotVeryColdOrVeryHot,
                colonyHasStellarReflectors,
                colonyHasModerateWaterPresent
        ));

        // Requires:
        //  - Not already water
        //  - Normal atmosphere
        //  - Atmosphere is not toxic or irradiated
        //  - Not very cold or very hot temperature
        //  - Stellar Reflectors
        //  - Water Level of 2
        ArrayList<String> waterTypeChangeProjectReq = new ArrayList<>(asList(
                worldTypeAllowsTerraforming,
                colonyNotWaterWorld,
                colonyAtmosphericDensityNormal,
                colonyAtmosphereNotToxicOrIrradiated,
                colonyNotVeryColdOrVeryHot,
                colonyHasStellarReflectors,
                colonyHasLargeWaterPresent
        ));

        // Requires:
        //  - Not already tundra
        //  - Normal atmosphere
        //  - Atmosphere is not toxic or irradiated
        //  - Colony is temperate or cold
        //  - Stellar Reflectors
        //  - Water Level of 1
        ArrayList<String> tundraTypeChangeProjectReq = new ArrayList<>(asList(
                worldTypeAllowsTerraforming,
                colonyNotTundraWorld,
                colonyAtmosphericDensityNormal,
                colonyAtmosphereNotToxicOrIrradiated,
                colonyTemperateOrCold,
                colonyHasStellarReflectors,
                colonyHasModerateWaterPresent
        ));

        // Requires:
        //  - Not already frozen
        //  - Normal atmosphere
        //  - Very cold
        //  - Water Level of 2
        ArrayList<String> frozenTypeChangeProjectReq = new ArrayList<>(asList(
                worldTypeAllowsTerraforming,
                colonyNotFrozenWorld,
                colonyAtmosphericDensityNormal,
                colonyVeryCold,
                colonyHasLargeWaterPresent
        ));

        // Requires:
        //  - Planet type permits improvement in farmland
        //  - Normal atmosphere
        //  - Atmosphere is not toxic or irradiated
        //  - Water Level of 2
        ArrayList<String> farmlandResourceImprovementProjectReq = new ArrayList<>(asList(
                worldTypeSupportsFarmlandImprovement,
                colonyAtmosphericDensityNormal,
                colonyAtmosphereNotToxicOrIrradiated,
                colonyHasLargeWaterPresent
        ));

        // Requires:
        //  - Planet type permits improvement in organics
        ArrayList<String> organicsResourceImprovementProjectReq = new ArrayList<>(asList(
                worldTypeSupportsOrganicsImprovement
        ));

        // Requires:
        //  - Planet type permits improvement in volatiles
        ArrayList<String> volatilesResourceImprovementProjectReq = new ArrayList<>(asList(
                worldTypeSupportsVolatilesImprovement
        ));

        // Requires:
        //  - Market has Extreme Weather
        //  - Planet can be terraformed
        //  - Market has normal atmosphere
        //  - Market has operational Atmosphere Processor
        ArrayList<String> extremeWeatherConditionImprovementProjectReq = new ArrayList<>(asList(
                worldTypeAllowsTerraforming,
                colonyExtremeWeather,
                colonyAtmosphericDensityNormal,
                colonyHasAtmosphereProcessor
        ));

        // Requires:
        //  - Market lacks Extreme Weather and Mild Climate
        //  - Market is habitable
        //  - World is Earth-like type
        //  - Market has normal atmosphere
        //  - Market has operational Atmosphere Processor
        ArrayList<String> mildClimateConditionImprovementProjectReq = new ArrayList<>(asList(
                colonyNormalClimate,
                colonyHabitable,
                worldTypeAllowsMildClimate,
                colonyAtmosphericDensityNormal,
                colonyHasAtmosphereProcessor
        ));

        // Requires:
        //  - Market is not already habitable
        //  - World is Earth-like type
        //  - Market has normal atmosphere
        //  - Not very cold or very hot temperature
        //  - Atmosphere is not toxic or irradiated
        //  - Market has operational Atmosphere Processor
        ArrayList<String> habitableConditionImprovementProjectReq = new ArrayList<>(asList(
                colonyNotAlreadyHabitable,
                worldTypeAllowsHumanHabitability,
                colonyAtmosphericDensityNormal,
                colonyNotVeryColdOrVeryHot,
                colonyAtmosphereNotToxicOrIrradiated,
                colonyHasAtmosphereProcessor
        ));

        // Requires:
        //  - Market has atmosphere problem(s)
        //  - Planet can be terraformed
        //  - Market has operational Atmosphere Processor
        ArrayList<String> atmosphereDensityConditionImprovementProjectReq = new ArrayList<>(asList(
                colonyAtmosphereSuboptimalDensity,
                worldTypeAllowsTerraforming,
                colonyHasAtmosphereProcessor
        ));

        // Requires:
        //  - Market has atmosphere problem(s)
        //  - Planet can be terraformed
        //  - Market has operational Atmosphere Processor
        ArrayList<String> toxicAtmosphereConditionImprovementProjectReq = new ArrayList<>(asList(
                colonyAtmosphereToxic,
                worldTypeAllowsTerraforming,
                colonyHasAtmosphereProcessor
        ));

        // Requires:
        //  - Market is irradiated
        //  - Market has operational Genelab
        ArrayList<String> irradiatedConditionImprovementProjectReq = new ArrayList<>(asList(
                colonyIrradiated,
                colonyHasGenelab
        ));

        // Requires:
        //  - Market has an atmosphere
        //  - Planet can be terraformed
        //  - Market has operational Atmosphere Processor
        ArrayList<String> removeAtmosphereConditionImprovementProjectReq = new ArrayList<>(asList(
                colonyHasAtmosphere,
                colonyBarrenOrFrozen,
                colonyHasAtmosphereProcessor
        ));

        if(aotd_Enabled) {
            // AotD requirements go here, just .add the display string here
            // Type change -> "Researched: Terraforming Templates"
            // Condition improvement -> "Researched : Atmosphere Manipulation"
            // Resource improvement -> "Researched : Advanced Terraforming Templates"

            aridTypeChangeProjectReq.add(aotd_TypeChangeResearchRequirement);
            jungleTypeChangeProjectReq.add(aotd_TypeChangeResearchRequirement);
            terranTypeChangeProjectReq.add(aotd_TypeChangeResearchRequirement);
            waterTypeChangeProjectReq.add(aotd_TypeChangeResearchRequirement);
            tundraTypeChangeProjectReq.add(aotd_TypeChangeResearchRequirement);
            frozenTypeChangeProjectReq.add(aotd_TypeChangeResearchRequirement);

            farmlandResourceImprovementProjectReq.add(aotd_ResourceImprovementResearchRequirement);
            organicsResourceImprovementProjectReq.add(aotd_ResourceImprovementResearchRequirement);
            volatilesResourceImprovementProjectReq.add(aotd_ResourceImprovementResearchRequirement);

            extremeWeatherConditionImprovementProjectReq.add(aotd_ResourceImprovementResearchRequirement);
            mildClimateConditionImprovementProjectReq.add(aotd_ResourceImprovementResearchRequirement);
            habitableConditionImprovementProjectReq.add(aotd_ResourceImprovementResearchRequirement);
            atmosphereDensityConditionImprovementProjectReq.add(aotd_ResourceImprovementResearchRequirement);
            toxicAtmosphereConditionImprovementProjectReq.add(aotd_ResourceImprovementResearchRequirement);
            irradiatedConditionImprovementProjectReq.add(aotd_ResourceImprovementResearchRequirement);
            removeAtmosphereConditionImprovementProjectReq.add(aotd_ResourceImprovementResearchRequirement);
        }

        // Type changes
        ret.put(aridTypeChangeProjectID, aridTypeChangeProjectReq.toArray(new String[0]));
        ret.put(jungleTypeChangeProjectID, jungleTypeChangeProjectReq.toArray(new String[0]));
        ret.put(terranTypeChangeProjectID, terranTypeChangeProjectReq.toArray(new String[0]));
        ret.put(waterTypeChangeProjectID, waterTypeChangeProjectReq.toArray(new String[0]));
        ret.put(tundraTypeChangeProjectID, tundraTypeChangeProjectReq.toArray(new String[0]));
        ret.put(frozenTypeChangeProjectID, frozenTypeChangeProjectReq.toArray(new String[0]));

        // Resource improvements
        ret.put(farmlandResourceImprovementProjectID, farmlandResourceImprovementProjectReq.toArray(new String[0]));
        ret.put(organicsResourceImprovementProjectID, organicsResourceImprovementProjectReq.toArray(new String[0]));
        ret.put(volatilesResourceImprovementProjectID, volatilesResourceImprovementProjectReq.toArray(new String[0]));

        // Condition improvements
        ret.put(extremeWeatherConditionImprovementProjectID, extremeWeatherConditionImprovementProjectReq.toArray(new String[0]));
        ret.put(mildClimateConditionImprovementProjectID, mildClimateConditionImprovementProjectReq.toArray(new String[0]));
        ret.put(habitableConditionImprovementProjectID, habitableConditionImprovementProjectReq.toArray(new String[0]));
        ret.put(atmosphereDensityConditionImprovementProjectID, atmosphereDensityConditionImprovementProjectReq.toArray(new String[0]));
        ret.put(toxicAtmosphereConditionImprovementProjectID, toxicAtmosphereConditionImprovementProjectReq.toArray(new String[0]));
        ret.put(irradiatedConditionImprovementProjectID, irradiatedConditionImprovementProjectReq.toArray(new String[0]));
        ret.put(removeAtmosphereConditionImprovementProjectID, removeAtmosphereConditionImprovementProjectReq.toArray(new String[0]));

        return ret;
    }

    private static HashMap<String, String> initialiseProjectTooltips() {
        HashMap<String, String> ret = new HashMap<>();

        ret.put(jungleTypeChangeProjectID, "Jungle type change");
        ret.put(aridTypeChangeProjectID, "Arid type change");
        ret.put(terranTypeChangeProjectID, "Terran type change");
        ret.put(waterTypeChangeProjectID, "Water type change");
        ret.put(tundraTypeChangeProjectID, "Tundra type change");
        ret.put(frozenTypeChangeProjectID, "Frozen type change");

        ret.put(farmlandResourceImprovementProjectID, "Farmland resource improvement");
        ret.put(organicsResourceImprovementProjectID, "Organics resource improvement");
        ret.put(volatilesResourceImprovementProjectID, "Volatiles resource improvement");

        ret.put(extremeWeatherConditionImprovementProjectID, "Stabilize weather patterns");
        ret.put(mildClimateConditionImprovementProjectID, "Make climate mild");
        ret.put(habitableConditionImprovementProjectID, "Make atmosphere habitable");
        ret.put(atmosphereDensityConditionImprovementProjectID, "Normalize atmospheric density");
        ret.put(toxicAtmosphereConditionImprovementProjectID, "Reduce atmosphere toxicity");
        ret.put(radiationConditionImprovementProjectID, "Eliminate harmful radiation");
        ret.put(removeAtmosphereConditionImprovementProjectID, "Remove the atmosphere");

        return ret;
    }

    private static HashMap<String, Pair<ArrayList<String>, ArrayList<String>>> initialisePlanetTypeChangeConditions() {
        // one is conditions added, two is conditions removed
        HashMap<String, Pair<ArrayList<String>, ArrayList<String>>> ret = new HashMap<>();

        ArrayList<String> jungleConditionsAdded = new ArrayList<>(asList(
                Conditions.HABITABLE,
                Conditions.FARMLAND_ADEQUATE,
                Conditions.ORGANICS_COMMON
        ));
        ArrayList<String> jungleConditionsRemoved = new ArrayList<>(asList(
                Conditions.WATER_SURFACE,
                Conditions.VOLTURNIAN_LOBSTER_PENS,

                Conditions.FARMLAND_POOR,
                Conditions.FARMLAND_RICH,
                Conditions.FARMLAND_BOUNTIFUL,

                Conditions.ORGANICS_TRACE,
                Conditions.ORGANICS_ABUNDANT,
                Conditions.ORGANICS_PLENTIFUL,

                Conditions.VOLATILES_TRACE,
                Conditions.VOLATILES_DIFFUSE,
                Conditions.VOLATILES_ABUNDANT,
                Conditions.VOLATILES_PLENTIFUL
        ));

        ArrayList<String> aridConditionsAdded = new ArrayList<>(asList(
                Conditions.HABITABLE,
                Conditions.FARMLAND_ADEQUATE,
                Conditions.ORGANICS_COMMON
        ));
        ArrayList<String> aridConditionsRemoved = new ArrayList<>(asList(
                Conditions.WATER_SURFACE,
                Conditions.VOLTURNIAN_LOBSTER_PENS,

                Conditions.FARMLAND_POOR,
                Conditions.FARMLAND_RICH,
                Conditions.FARMLAND_BOUNTIFUL,

                Conditions.ORGANICS_TRACE,
                Conditions.ORGANICS_ABUNDANT,
                Conditions.ORGANICS_PLENTIFUL,

                Conditions.VOLATILES_TRACE,
                Conditions.VOLATILES_DIFFUSE,
                Conditions.VOLATILES_ABUNDANT,
                Conditions.VOLATILES_PLENTIFUL
        ));

        ArrayList<String> terranConditionsAdded = new ArrayList<>(asList(
                Conditions.HABITABLE,
                Conditions.FARMLAND_ADEQUATE,
                Conditions.ORGANICS_TRACE
        ));
        ArrayList<String> terranConditionsRemoved = new ArrayList<>(asList(
                Conditions.WATER_SURFACE,
                Conditions.VOLTURNIAN_LOBSTER_PENS,

                Conditions.FARMLAND_POOR,
                Conditions.FARMLAND_RICH,
                Conditions.FARMLAND_BOUNTIFUL,

                Conditions.ORGANICS_COMMON,
                Conditions.ORGANICS_ABUNDANT,
                Conditions.ORGANICS_PLENTIFUL,

                Conditions.VOLATILES_DIFFUSE,
                Conditions.VOLATILES_ABUNDANT,
                Conditions.VOLATILES_PLENTIFUL
        ));

        ArrayList<String> waterConditionsAdded = new ArrayList<>(asList(
                Conditions.HABITABLE,
                Conditions.WATER_SURFACE
        ));
        ArrayList<String> waterConditionsRemoved = new ArrayList<>(asList(
                Conditions.FARMLAND_POOR,
                Conditions.FARMLAND_ADEQUATE,
                Conditions.FARMLAND_RICH,
                Conditions.FARMLAND_BOUNTIFUL,

                Conditions.ORGANICS_TRACE,
                Conditions.ORGANICS_COMMON,
                Conditions.ORGANICS_ABUNDANT,
                Conditions.ORGANICS_PLENTIFUL,

                Conditions.VOLATILES_TRACE,
                Conditions.VOLATILES_DIFFUSE,
                Conditions.VOLATILES_ABUNDANT,
                Conditions.VOLATILES_PLENTIFUL
        ));

        ArrayList<String> tundraConditionsAdded = new ArrayList<>(asList(
                Conditions.HABITABLE,
                Conditions.FARMLAND_ADEQUATE,
                Conditions.ORGANICS_TRACE
        ));
        ArrayList<String> tundraConditionsRemoved = new ArrayList<>(asList(
                Conditions.WATER_SURFACE,
                Conditions.VOLTURNIAN_LOBSTER_PENS,

                Conditions.FARMLAND_POOR,
                Conditions.FARMLAND_RICH,
                Conditions.FARMLAND_BOUNTIFUL,

                Conditions.ORGANICS_COMMON,
                Conditions.ORGANICS_ABUNDANT,
                Conditions.ORGANICS_PLENTIFUL,

                Conditions.VOLATILES_DIFFUSE,
                Conditions.VOLATILES_ABUNDANT,
                Conditions.VOLATILES_PLENTIFUL
        ));

        ArrayList<String> frozenConditionsAdded = new ArrayList<>(asList(
                Conditions.VOLATILES_ABUNDANT
        ));
        ArrayList<String> frozenConditionsRemoved = new ArrayList<>(asList(
                Conditions.HABITABLE,
                Conditions.WATER_SURFACE,
                Conditions.VOLTURNIAN_LOBSTER_PENS,

                Conditions.FARMLAND_POOR,
                Conditions.FARMLAND_ADEQUATE,
                Conditions.FARMLAND_RICH,
                Conditions.FARMLAND_BOUNTIFUL,

                Conditions.ORGANICS_TRACE,
                Conditions.ORGANICS_COMMON,
                Conditions.ORGANICS_ABUNDANT,
                Conditions.ORGANICS_PLENTIFUL,

                Conditions.VOLATILES_TRACE,
                Conditions.VOLATILES_DIFFUSE,
                Conditions.VOLATILES_PLENTIFUL
        ));

        if (boggledTools.getBooleanSetting(BoggledSettings.terraformingTypeChangeAddVolatiles)) {
            terranConditionsAdded.add(Conditions.VOLATILES_TRACE);
            tundraConditionsAdded.add(Conditions.VOLATILES_TRACE);
        } else {
            terranConditionsRemoved.add(Conditions.VOLATILES_TRACE);
            tundraConditionsRemoved.add(Conditions.VOLATILES_TRACE);
        }

        // Modded conditions get added here, do the mod enabled check and throw them in the appropriate list

        ret.put(jungleTypeChangeProjectID, new Pair<>(jungleConditionsAdded, jungleConditionsRemoved));
        ret.put(aridTypeChangeProjectID, new Pair<>(aridConditionsAdded, aridConditionsRemoved));
        ret.put(terranTypeChangeProjectID, new Pair<>(terranConditionsAdded, terranConditionsRemoved));
        ret.put(waterTypeChangeProjectID, new Pair<>(waterConditionsAdded, waterConditionsRemoved));
        ret.put(tundraTypeChangeProjectID, new Pair<>(tundraConditionsAdded, tundraConditionsRemoved));
        ret.put(frozenTypeChangeProjectID, new Pair<>(frozenConditionsAdded, frozenConditionsRemoved));

        return ret;
    }

    private static final HashMap<String, String[]> projectRequirements = initialiseProjectRequirements();

    private static final HashMap<String, String> projectTooltip = initialiseProjectTooltips();

    // one is conditions added, two is conditions removed
    private static final HashMap<String, Pair<ArrayList<String>, ArrayList<String>>> planetTypeChangeConditions = initialisePlanetTypeChangeConditions();

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
            if (planet instanceof PlanetAPI && !getPlanetType(((PlanetAPI) planet)).equals(starPlanetID)) {
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
                if (entity instanceof PlanetAPI && !getPlanetType(((PlanetAPI) entity)).equals(starPlanetID)) {
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

    public static String getPlanetType(PlanetAPI planet) {
        // Sets the spec planet type, but not the actual planet type. Need the API fix from Alex to correct this.
        // All code should rely on this function to get the planet type so it should work without bugs.
        // String planetType = planet.getTypeId();

        if(planet == null || planet.getSpec() == null || planet.getSpec().getPlanetType() == null)
        {
            return unknownPlanetID;
        }

        // Added this to catch Unknown Skies planets or other modded planet types
        if(planet.getMarket() != null && planet.getMarket().hasCondition(Conditions.WATER_SURFACE))
        {
            return waterPlanetID;
        }

        String planetType = planet.getTypeId();

        switch (planetType) {
            case "nebula_center_old":
            case "nebula_center_average":
            case "nebula_center_young":
            case "star_neutron":
            case "black_hole":
            case "star_yellow":
            case "star_white":
            case "star_blue_giant":
            case "star_blue_supergiant":
            case "star_orange":
            case "star_orange_giant":
            case "star_red_supergiant":
            case "star_red_giant":
            case "star_red_dwarf":
            case "star_browndwarf":
            case "US_star_blue_giant":
            case "US_star_yellow":
            case "US_star_orange":
            case "US_star_red_giant":
            case "US_star_white":
            case "US_star_browndwarf":
            case "SCY_star":
            case "SCY_companionStar":
            case "SCY_wormholeUnder":
            case "SCY_wormholeA":
            case "SCY_wormholeB":
            case "SCY_wormholeC":
            case "istl_sigmaworld":
            case "istl_dysonshell":
            case "vayra_star_blue":
            case "vayra_star_brown":
            case "vayra_star_yellow_white":
                return starPlanetID;
            case "gas_giant":
            case "ice_giant":
            case "US_gas_giant":
            case "US_gas_giantB":
            case "fds_gas_giant":
            case "SCY_tartarus":
            case "galaxytigers_gas_giant":
                return gasGiantPlanetID;
            case "barren":
            case "barren_castiron":
            case "barren2":
            case "barren3":
            case "barren_venuslike":
            case "rocky_metallic":
            case "rocky_unstable":
            case "rocky_ice":
            case "irradiated":
            case "barren-bombarded":
            case "US_acid":
            case "US_acidRain":
            case "US_acidWind":
            case "US_barrenA":
            case "US_barrenB":
            case "US_barrenC":
            case "US_barrenD":
            case "US_barrenE":
            case "US_barrenF":
            case "US_azure":
            case "US_burnt":
            case "US_artificial":
            case "haunted":
            case "hmi_crystalline":
            case "SCY_miningColony":
            case "SCY_burntPlanet":
            case "SCY_moon":
            case "SCY_redRock":
            case "rad_planet":
            case "ecumenopolis":
            case "nskr_ice_desert":
                return barrenPlanetID;
            case "toxic":
            case "toxic_cold":
            case "US_green":
            case "SCY_acid":
                return toxicPlanetID;
            case "desert":
            case "desert1":
            case "arid":
            case "barren-desert":
            case "US_dust":
            case "US_desertA":
            case "US_desertB":
            case "US_desertC":
            case "US_red":
            case "US_redWind":
            case "US_lifelessArid":
            case "US_arid":
            case "US_crimson":
            case "US_storm":
            case "fds_desert":
            case "SCY_homePlanet":
            case "istl_aridbread":
            case "vayra_bread":
            case "US_auric":
            case "US_auricCloudy":
                return desertPlanetID;
            case "terran":
            case "terran-eccentric":
            case "US_lifeless":
            case "US_alkali":
            case "US_continent":
            case "US_magnetic":
            case "US_water":
            case "US_waterB":
            case "terran_adapted":
                return terranPlanetID;
            case "water":
                return waterPlanetID;
            case "tundra":
            case "US_purple":
            case "fds_tundra":
            case "galaxytigers_tundra":
                return tundraPlanetID;
            case "jungle":
            case "US_jungle":
            case "jungle_charkha":
                return junglePlanetID;
            case "frozen":
            case "frozen1":
            case "frozen2":
            case "frozen3":
            case "cryovolcanic":
            case "US_iceA":
            case "US_iceB":
            case "US_blue":
            case "fds_cryovolcanic":
            case "fds_frozen":
                return frozenPlanetID;
            case "lava":
            case "lava_minor":
            case "US_lava":
            case "US_volcanic":
            case "fds_lava":
                return volcanicPlanetID;
            default:
                return unknownPlanetID;
        }
    }

    public static ArrayList<MarketAPI> getNonStationMarketsPlayerControls()
    {
        ArrayList<MarketAPI> allPlayerMarkets = (ArrayList<MarketAPI>) Misc.getPlayerMarkets(true);
        ArrayList<MarketAPI> allNonStationPlayerMarkets = new ArrayList<>();
        for(MarketAPI market : allPlayerMarkets)
        {
            if(!boggledTools.marketIsStation(market))
            {
                if(!market.hasCondition(terraformingControllerConditionID))
                {
                    boggledTools.addCondition(market, terraformingControllerConditionID);
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

        String planetType = boggledTools.getPlanetType(market.getPlanetEntity());
        return !planetType.equals(starPlanetID) && !planetType.equals(gasGiantPlanetID) && !planetType.equals(volcanicPlanetID) && !planetType.equals(unknownPlanetID);
    }

    public static boolean getCreateMirrorsOrShades(MarketAPI market) {
        // Return true for mirrors, false for shades
        // Go by temperature first. If not triggered, will check planet type. Otherwise, just return true.

        if (market.hasCondition(Conditions.POOR_LIGHT) || market.hasCondition(Conditions.VERY_COLD) || market.hasCondition(Conditions.COLD)) {
            return true;
        } else if (market.hasCondition(Conditions.VERY_HOT) || market.hasCondition(Conditions.HOT)) {
            return false;
        }

        if (boggledTools.getPlanetType(market.getPlanetEntity()).equals(desertPlanetID) || boggledTools.getPlanetType(market.getPlanetEntity()).equals(junglePlanetID)) {
            return false;
        } else if (boggledTools.getPlanetType(market.getPlanetEntity()).equals(tundraPlanetID) || boggledTools.getPlanetType(market.getPlanetEntity()).equals(frozenPlanetID)) {
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
                if(terrainID.equals("asteroid_belt"))
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
            SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
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
            if (marketElement.getFactionId().equals(market.getFactionId()) && marketElement.hasIndustry(BoggledIndustries.ismaraSlingIndustryID) && marketElement.getIndustry(BoggledIndustries.ismaraSlingIndustryID).isFunctional()) {
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
        SectorEntityToken newStation = null;
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
        if(Global.getSettings().getModManager().isModEnabled(BoggledMods.illustratedEntitiesModID))
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

        switch (stationType) {
            case "astropolis": {
                SectorEntityToken targetTokenToDelete = null;

                switch (stationGreekLetter) {
                    case "alpha": {
                        for (SectorEntityToken entity : system.getAllEntities()) {
                            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && entity.getCircularOrbitAngle() == station.getCircularOrbitAngle() && (entity.hasTag(BoggledTags.lightsOverlayAstropolisAlphaSmall) || entity.hasTag(BoggledTags.lightsOverlayAstropolisAlphaMedium) || entity.hasTag(BoggledTags.lightsOverlayAstropolisAlphaLarge))) {
                                targetTokenToDelete = entity;
                                break;
                            }
                        }

                        if (targetTokenToDelete != null) {
                            system.removeEntity(targetTokenToDelete);
                            deleteOldLightsOverlay(station, stationType, stationGreekLetter);
                        }
                        break;
                    }
                    case "beta": {
                        for (SectorEntityToken entity : system.getAllEntities()) {
                            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && entity.getCircularOrbitAngle() == station.getCircularOrbitAngle() && (entity.hasTag(BoggledTags.lightsOverlayAstropolisBetaSmall) || entity.hasTag(BoggledTags.lightsOverlayAstropolisBetaMedium) || entity.hasTag(BoggledTags.lightsOverlayAstropolisBetaLarge))) {
                                targetTokenToDelete = entity;
                                break;
                            }
                        }

                        if (targetTokenToDelete != null) {
                            system.removeEntity(targetTokenToDelete);
                            deleteOldLightsOverlay(station, stationType, stationGreekLetter);
                        }
                        break;
                    }
                    case "gamma": {
                        for (SectorEntityToken entity : system.getAllEntities()) {
                            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && entity.getCircularOrbitAngle() == station.getCircularOrbitAngle() && (entity.hasTag(BoggledTags.lightsOverlayAstropolisGammaSmall) || entity.hasTag(BoggledTags.lightsOverlayAstropolisGammaMedium) || entity.hasTag(BoggledTags.lightsOverlayAstropolisGammaLarge))) {
                                targetTokenToDelete = entity;
                                break;
                            }
                        }

                        if (targetTokenToDelete != null) {
                            system.removeEntity(targetTokenToDelete);
                            deleteOldLightsOverlay(station, stationType, stationGreekLetter);
                        }
                        break;
                    }
                }
                break;
            }
            case "mining": {
                SectorEntityToken targetTokenToDelete = null;

                for (SectorEntityToken entity : system.getAllEntities()) {
                    if (entity.hasTag(BoggledTags.lightsOverlayMiningSmall) || entity.hasTag(BoggledTags.lightsOverlayMiningMedium)) {
                        targetTokenToDelete = entity;
                        break;
                    }
                }

                if (targetTokenToDelete != null) {
                    system.removeEntity(targetTokenToDelete);
                    deleteOldLightsOverlay(station, stationType, stationGreekLetter);
                }
                break;
            }
            case "siphon": {
                SectorEntityToken targetTokenToDelete = null;

                for (SectorEntityToken entity : system.getAllEntities()) {
                    if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && (entity.hasTag(BoggledTags.lightsOverlaySiphonSmall) || entity.hasTag(BoggledTags.lightsOverlaySiphonMedium))) {
                        targetTokenToDelete = entity;
                        break;
                    }
                }

                if (targetTokenToDelete != null) {
                    system.removeEntity(targetTokenToDelete);
                    deleteOldLightsOverlay(station, stationType, stationGreekLetter);
                }
                break;
            }
            default:
                //Do nothing because the station type is unrecognized
                return;
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
                if(!stationToApplyOverlayTo.getMarket().getFactionId().equals("neutral"))
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

    public static int getMaxFarmlandForMarket(MarketAPI market)
    {
        // Returns 0 if the planet can't have farmland
        // Returns 1 through 4 for the levels of farmland, with 1 being poor and 4 being bountiful

        PlanetAPI planet = market.getPlanetEntity();

        if(getPlanetType(planet).equals(starPlanetID) || getPlanetType(planet).equals(gasGiantPlanetID) || getPlanetType(planet).equals(barrenPlanetID) || getPlanetType(planet).equals(toxicPlanetID) || getPlanetType(planet).equals(volcanicPlanetID) || getPlanetType(planet).equals(frozenPlanetID) || getPlanetType(planet).equals(waterPlanetID) || getPlanetType(planet).equals(unknownPlanetID))
        {
            return 0;
        }
        else if(getPlanetType(planet).equals(junglePlanetID) || getPlanetType(planet).equals(desertPlanetID) || getPlanetType(planet).equals(terranPlanetID) || getPlanetType(planet).equals(tundraPlanetID))
        {
            return 4;
        }
        else
        {
            return 0;
        }
    }

    public static int getCurrentFarmlandForMarket(MarketAPI market)
    {
        // Returns 0 if the planet has no farmland
        // Returns 1 through 4 for the levels of farmland, with 1 being poor and 4 being bountiful

        if(market.hasCondition(Conditions.FARMLAND_POOR))
        {
            return 1;
        }
        else if(market.hasCondition(Conditions.FARMLAND_ADEQUATE))
        {
            return 2;
        }
        else if(market.hasCondition(Conditions.FARMLAND_RICH))
        {
            return 3;
        }
        else if(market.hasCondition(Conditions.FARMLAND_BOUNTIFUL))
        {
            return 4;
        }
        else
        {
            return 0;
        }
    }

    public static int getMaxOrganicsForMarket(MarketAPI market)
    {
        // Returns 0 if the planet can't have organics
        // Returns 1 through 4 for the levels of organics, with 1 being trace and 4 being plentiful

        PlanetAPI planet = market.getPlanetEntity();

        if(getPlanetType(planet).equals(starPlanetID) || getPlanetType(planet).equals(gasGiantPlanetID) || getPlanetType(planet).equals(barrenPlanetID) || getPlanetType(planet).equals(toxicPlanetID) || getPlanetType(planet).equals(volcanicPlanetID) || getPlanetType(planet).equals(frozenPlanetID) || getPlanetType(planet).equals(unknownPlanetID))
        {
            return 0;
        }
        else if(getPlanetType(planet).equals(waterPlanetID) || getPlanetType(planet).equals(junglePlanetID) || getPlanetType(planet).equals(terranPlanetID))
        {
            return 4;
        }
        else if(getPlanetType(planet).equals(desertPlanetID))
        {
            return 3;
        }
        else if(getPlanetType(planet).equals(tundraPlanetID))
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static int getCurrentOrganicsForMarket(MarketAPI market)
    {
        // Returns 0 if the planet has no organics
        // Returns 1 through 4 for the levels of organics, with 1 being trace and 4 being plentiful

        if(market.hasCondition(Conditions.ORGANICS_TRACE))
        {
            return 1;
        }
        else if(market.hasCondition(Conditions.ORGANICS_COMMON))
        {
            return 2;
        }
        else if(market.hasCondition(Conditions.ORGANICS_ABUNDANT))
        {
            return 3;
        }
        else if(market.hasCondition(Conditions.ORGANICS_PLENTIFUL))
        {
            return 4;
        }
        else
        {
            return 0;
        }
    }

    public static int getMaxVolatilesForMarket(MarketAPI market)
    {
        // Returns 0 if the planet can't have volatiles
        // Returns 1 through 4 for the levels of volatiles, with 1 being trace and 4 being plentiful

        PlanetAPI planet = market.getPlanetEntity();

        if(getPlanetType(planet).equals(starPlanetID) || getPlanetType(planet).equals(gasGiantPlanetID) || getPlanetType(planet).equals(barrenPlanetID) || getPlanetType(planet).equals(toxicPlanetID) || getPlanetType(planet).equals(volcanicPlanetID) || getPlanetType(planet).equals(junglePlanetID) || getPlanetType(planet).equals(unknownPlanetID))
        {
            return 0;
        }
        else if(getPlanetType(planet).equals(frozenPlanetID) || getPlanetType(planet).equals(tundraPlanetID) || getPlanetType(planet).equals(waterPlanetID))
        {
            return 4;
        }
        else if(getPlanetType(planet).equals(desertPlanetID) || getPlanetType(planet).equals(terranPlanetID))
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static int getCurrentVolatilesForMarket(MarketAPI market)
    {
        // Returns 0 if the planet has no volatiles
        // Returns 1 through 4 for the levels of volatiles, with 1 being trace and 4 being plentiful

        if(market.hasCondition(Conditions.VOLATILES_TRACE))
        {
            return 1;
        }
        else if(market.hasCondition(Conditions.VOLATILES_DIFFUSE))
        {
            return 2;
        }
        else if(market.hasCondition(Conditions.VOLATILES_ABUNDANT))
        {
            return 3;
        }
        else if(market.hasCondition(Conditions.VOLATILES_PLENTIFUL))
        {
            return 4;
        }
        else
        {
            return 0;
        }
    }

    public static void incrementFarmland(MarketAPI market)
    {
        if(market.hasCondition(Conditions.FARMLAND_POOR))
        {
            boggledTools.removeCondition(market, Conditions.FARMLAND_POOR);
            boggledTools.addCondition(market, Conditions.FARMLAND_ADEQUATE);
        }
        else if(market.hasCondition(Conditions.FARMLAND_ADEQUATE))
        {
            boggledTools.removeCondition(market, Conditions.FARMLAND_ADEQUATE);
            boggledTools.addCondition(market, Conditions.FARMLAND_RICH);
        }
        else if(market.hasCondition(Conditions.FARMLAND_RICH))
        {
            boggledTools.removeCondition(market, Conditions.FARMLAND_RICH);
            boggledTools.addCondition(market, Conditions.FARMLAND_BOUNTIFUL);
        }
        else if(market.hasCondition(Conditions.FARMLAND_BOUNTIFUL))
        {
            //Do nothing
        }
        else
        {
            boggledTools.addCondition(market, Conditions.FARMLAND_POOR);
        }

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);
        boggledTools.refreshAquacultureAndFarming(market);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Farmland improvement on " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static void incrementOrganics(MarketAPI market)
    {
        if(market.hasCondition(Conditions.ORGANICS_TRACE))
        {
            boggledTools.removeCondition(market, Conditions.ORGANICS_TRACE);
            boggledTools.addCondition(market, Conditions.ORGANICS_COMMON);
        }
        else if(market.hasCondition(Conditions.ORGANICS_COMMON))
        {
            boggledTools.removeCondition(market, Conditions.ORGANICS_COMMON);
            boggledTools.addCondition(market, Conditions.ORGANICS_ABUNDANT);
        }
        else if(market.hasCondition(Conditions.ORGANICS_ABUNDANT))
        {
            boggledTools.removeCondition(market, Conditions.ORGANICS_ABUNDANT);
            boggledTools.addCondition(market, Conditions.ORGANICS_PLENTIFUL);
        }
        else if(market.hasCondition(Conditions.ORGANICS_PLENTIFUL))
        {
            //Do nothing
        }
        else
        {
            boggledTools.addCondition(market, Conditions.ORGANICS_TRACE);
        }

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);
        boggledTools.refreshAquacultureAndFarming(market);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Organics improvement on " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static void incrementVolatiles(MarketAPI market)
    {
        if(market.hasCondition(Conditions.VOLATILES_TRACE))
        {
            boggledTools.removeCondition(market, Conditions.VOLATILES_TRACE);
            boggledTools.addCondition(market, Conditions.VOLATILES_DIFFUSE);
        }
        else if(market.hasCondition(Conditions.VOLATILES_DIFFUSE))
        {
            boggledTools.removeCondition(market, Conditions.VOLATILES_DIFFUSE);
            boggledTools.addCondition(market, Conditions.VOLATILES_ABUNDANT);
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
            boggledTools.addCondition(market, Conditions.VOLATILES_TRACE);
        }

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);
        boggledTools.refreshAquacultureAndFarming(market);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Volatiles improvement on " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
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
        String planetType = getPlanetType(market.getPlanetEntity());
        if(!planetType.equals(starPlanetID) && !planetType.equals(gasGiantPlanetID) && !planetType.equals(volcanicPlanetID) && !planetType.equals(unknownPlanetID))
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

        String planetType = getPlanetType(market.getPlanetEntity());
        if(!planetType.equals(gasGiantPlanetID) && !planetType.equals(unknownPlanetID))
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

    public static void terraformVariantToVariant(MarketAPI market, String newPlanetType)
    {
        // Not currently in use due to lack of Unknown Skies terraforming options
        // Highly likely to implement in the future
        switch (newPlanetType) {
            case "auric":
                newPlanetType = "US_auric";
                break;
            case "archipelago":
                newPlanetType = "US_water";
                break;
            case "continental":
                newPlanetType = "US_continent";
                break;
        }

        market.getPlanetEntity().changeType(newPlanetType, null);

        Pair<ArrayList<String>, ArrayList<String>> conditionsAddedRemoved = planetTypeChangeConditions.get(newPlanetType);
        if (conditionsAddedRemoved != null) {
            for (String addedCondition : conditionsAddedRemoved.one) {
                addCondition(market, addedCondition);
            }
            for (String removedCondition : conditionsAddedRemoved.two) {
                removeCondition(market, removedCondition);
            }
        }

        surveyAll(market);
        refreshSupplyAndDemand(market);
        refreshAquacultureAndFarming(market);

        if (market.isPlayerOwned())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            intel.addLine("    - Completed");
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public static String[] getProjectRequirementsStrings(String project)
    {
        String[] requirements = projectRequirements.get(project);
        if (requirements != null)
        {
            return requirements;
        }

        else if(project.contains("Crafting"))
        {
            // For now, all the special items require the same conditions to craft.

            // Requires:
            //  - Market is size 5 or larger
            //  - Market has operational Orbital Works with Pristine Nanoforge installed

            int artifactCost = boggledTools.getIntSetting(BoggledSettings.domainTechCraftingArtifactCost);
            int storyPointCost = boggledTools.getIntSetting(BoggledSettings.domainTechCraftingStoryPointCost);
            int adjustedArtifactCost = 0;
            if(project.contains("Hard"))
            {
                adjustedArtifactCost = artifactCost * 2;
            }
            else if(project.contains("Medium"))
            {
                adjustedArtifactCost = artifactCost;
            }
            else if(project.contains("Easy"))
            {
                adjustedArtifactCost = artifactCost / 2;
            }

            ArrayList<String> reqs = new ArrayList<>(asList(
                colonyHasAtLeast100kInhabitants,
                colonyHasOrbitalWorksWPristineNanoforge,
                "Fleet cargo contains at least " + adjustedArtifactCost + " Domain-era artifacts"
            ));

            if(storyPointCost > 0)
            {
                reqs.add(storyPointCost + " story points available to spend");
            }
            return reqs.toArray(new String[0]);
        }

        // Should never be reached unless bad project string passed in.
        return new String[]{"You should never see this text. If you do, tell Boggled about it on the forums."};
    }

    public static boolean requirementMet(MarketAPI market, String requirement)
    {
        PlanetAPI planet = null;
        String planetType = "";
        int planetWaterLevel = 0;

        if(!boggledTools.marketIsStation(market))
        {
            planet = market.getPlanetEntity();
            planetType = getPlanetType(planet);
            planetWaterLevel = getPlanetWaterLevel(market);
        }

        if(requirement.equals("You should never see this text. If you do, tell Boggled about it on the forums."))
        {
            return false;
        }
        else if(requirement.equals(colonyNotAridWorld))
        {
            return !planetType.equals(desertPlanetID);
        }
        else if(requirement.equals(colonyNotJungleWorld))
        {
            return !planetType.equals(junglePlanetID);
        }
        else if(requirement.equals(colonyNotTerranWorld))
        {
            return !planetType.equals(terranPlanetID);
        }
        else if(requirement.equals(colonyNotWaterWorld))
        {
            return !planetType.equals(waterPlanetID);
        }
        else if(requirement.equals(colonyNotTundraWorld))
        {
            return !planetType.equals(tundraPlanetID);
        }
        else if(requirement.equals(colonyNotFrozenWorld))
        {
            return !planetType.equals(frozenPlanetID);
        }
        else if(requirement.equals(colonyBarrenOrFrozen))
        {
            return planetType.equals(barrenPlanetID) || planetType.equals(frozenPlanetID);
        }
        else if(requirement.equals(colonyAtmosphericDensityNormal))
        {
            if(market.hasCondition(Conditions.NO_ATMOSPHERE) || market.hasCondition(Conditions.THIN_ATMOSPHERE) || market.hasCondition(Conditions.DENSE_ATMOSPHERE))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else if(requirement.equals(colonyAtmosphereNotToxicOrIrradiated))
        {
            if(market.hasCondition(Conditions.TOXIC_ATMOSPHERE) || market.hasCondition(Conditions.IRRADIATED))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else if(requirement.equals(colonyNotColdOrVeryCold))
        {
            return !market.hasCondition(Conditions.COLD) && !market.hasCondition(Conditions.VERY_COLD);
        }
        else if(requirement.equals(colonyHotOrVeryHot))
        {
            return market.hasCondition(Conditions.HOT) || market.hasCondition(Conditions.VERY_HOT);
        }
        else if(requirement.equals(colonyNotVeryColdOrVeryHot))
        {
            return !market.hasCondition(Conditions.VERY_HOT) && !market.hasCondition(Conditions.VERY_COLD);
        }
        else if(requirement.equals(colonyNotHotOrVeryHot))
        {
            return !market.hasCondition(Conditions.HOT) && !market.hasCondition(Conditions.VERY_HOT);
        }
        else if(requirement.equals(colonyVeryCold))
        {
            return market.hasCondition(Conditions.VERY_COLD);
        }
        else if(requirement.equals(colonyTemperateOrHot))
        {
            return !market.hasCondition(Conditions.VERY_COLD) && !market.hasCondition(Conditions.COLD) && !market.hasCondition(Conditions.VERY_HOT);
        }
        else if(requirement.equals(colonyTemperateOrCold))
        {
            return !market.hasCondition(Conditions.VERY_COLD) && !market.hasCondition(Conditions.HOT) && !market.hasCondition(Conditions.VERY_HOT);
        }
        else if(requirement.equals(colonyHasStellarReflectors))
        {
            return marketHasStellarReflectorArray(market);
        }
        else if(requirement.equals(colonyHasModerateWaterPresent))
        {
            return planetWaterLevel == 1 || planetWaterLevel == 2;
        }
        else if(requirement.equals(colonyHasLargeWaterPresent))
        {
            return planetWaterLevel == 2;
        }
        else if(requirement.equals(colonyHasAtmosphereProcessor))
        {
            return marketHasAtmosphereProcessor(market);
        }
        else if(requirement.equals(colonyHasGenelab))
        {
            return marketHasGenelab(market);
        }
        else if(requirement.equals(colonyHabitable))
        {
            return market.hasCondition(Conditions.HABITABLE);
        }
        else if(requirement.equals(colonyNotAlreadyHabitable))
        {
            return !market.hasCondition(Conditions.HABITABLE);
        }
        else if(requirement.equals(colonyExtremeWeather))
        {
            return market.hasCondition(Conditions.EXTREME_WEATHER) || market.hasCondition("US_storm");
        }
        else if(requirement.equals(colonyNormalClimate))
        {
            return !market.hasCondition(Conditions.MILD_CLIMATE) && !market.hasCondition(Conditions.EXTREME_WEATHER) && !market.hasCondition("US_storm");
        }
        else if(requirement.equals(colonyAtmosphereToxic))
        {
            return market.hasCondition(Conditions.TOXIC_ATMOSPHERE);
        }
        else if(requirement.equals(colonyIrradiated))
        {
            return market.hasCondition(Conditions.IRRADIATED);
        }
        else if(requirement.equals(colonyHasAtmosphere))
        {
            return !market.hasCondition(Conditions.NO_ATMOSPHERE);
        }
        else if(requirement.equals(colonyAtmosphereSuboptimalDensity))
        {
            return market.hasCondition(Conditions.NO_ATMOSPHERE) || market.hasCondition(Conditions.THIN_ATMOSPHERE) || market.hasCondition(Conditions.DENSE_ATMOSPHERE);
        }
        else if(requirement.equals(worldTypeSupportsFarmlandImprovement))
        {
            return getMaxFarmlandForMarket(market) > getCurrentFarmlandForMarket(market);
        }
        else if(requirement.equals(worldTypeSupportsOrganicsImprovement))
        {
            return getMaxOrganicsForMarket(market) > getCurrentOrganicsForMarket(market);
        }
        else if(requirement.equals(worldTypeSupportsVolatilesImprovement))
        {
            return getMaxVolatilesForMarket(market) > getCurrentVolatilesForMarket(market);
        }
        else if(requirement.equals(worldTypeAllowsTerraforming))
        {
            if(planetType.equals(starPlanetID) || planetType.equals(gasGiantPlanetID) || planetType.equals(volcanicPlanetID) || planetType.equals(unknownPlanetID))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else if(requirement.equals(worldTypeAllowsMildClimate) || requirement.equals(worldTypeAllowsHumanHabitability))
        {
            if(planetType.equals(junglePlanetID) || planetType.equals(desertPlanetID) || planetType.equals(terranPlanetID) || planetType.equals(waterPlanetID) || planetType.equals(tundraPlanetID))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else if(requirement.equals(colonyHasAtLeast100kInhabitants))
        {
            if(market.getSize() >= 5)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else if(requirement.equals(colonyHasOrbitalWorksWPristineNanoforge))
        {
            if(market.hasIndustry(Industries.ORBITALWORKS) && market.getIndustry(Industries.ORBITALWORKS).isFunctional())
            {
                for (SpecialItemData data : market.getIndustry(Industries.ORBITALWORKS).getVisibleInstalledItems()) {
                    if (data.getId().equals(Items.PRISTINE_NANOFORGE) || data.getId().equals("uaf_dimen_nanoforge")) {
                        return true;
                    }
                }
            }
            return false;
        }
        else if(requirement.contains("Fleet cargo contains at least"))
        {
            String[] words = requirement.split(" ");
            CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
            return playerCargo.getCommodityQuantity(BoggledCommodities.domainArtifacts) >= Integer.parseInt(words[5]);
        }
        else if(requirement.equals(boggledTools.getIntSetting(BoggledSettings.domainTechCraftingStoryPointCost) + " story points available to spend"))
        {
            MutableCharacterStatsAPI charStats = Global.getSector().getPlayerStats();
            return charStats.getStoryPoints() >= boggledTools.getIntSetting(BoggledSettings.domainTechCraftingStoryPointCost);
        }
//        else if(requirement.equals(aotd_TypeChangeResearchRequirement))
//        {
//            return AoTDMainResearchManager.getInstance().isResearchedForPlayer(aotd_TypeChangeResearchRequirementID);
//        }
//        else if (requirement.equals(aotd_ResourceImprovementResearchRequirement))
//        {
//            return AoTDMainResearchManager.getInstance().isResearchedForPlayer(aotd_ResourceImprovementResearchRequirementID);
//        }
//        else if (requirement.equals(aotd_ConditionImprovementResearchRequirement))
//        {
//            return AoTDMainResearchManager.getInstance().isResearchedForPlayer(aotd_ConditionImprovementResearchRequirementID);
//        }
        else
        {
            return false;
        }
    }

    public static boolean projectRequirementsMet(MarketAPI market, String project)
    {
        String[] requirements = getProjectRequirementsStrings(project);
        for (String requirement : requirements) {
            if (!requirementMet(market, requirement)) {
                return false;
            }
        }

        // Returns true if no requirement was failed above
        return true;
    }

    public static Boolean printProjectRequirementsReportIfStalled(MarketAPI market, String project, TextPanelAPI text)
    {
        Color highlight = Misc.getHighlightColor();
        Color good = Misc.getPositiveHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(project != null && !project.equals(noneProjectID))
        {
            // Print requirements, and if not met, print terraforming is stalled
            text.addPara("Project Requirements:");
            String[] requirements = boggledTools.getProjectRequirementsStrings(project);
            boolean foundUnmetRequirement = false;
            for (String requirement : requirements) {
                if (boggledTools.requirementMet(market, requirement)) {
                    text.addPara("      - %s", good, requirement);
                } else {
                    text.addPara("      - %s", bad, requirement);
                    foundUnmetRequirement = true;
                }
            }

            return foundUnmetRequirement;
        }

        return false;
    }

    public static void printProjectResults(MarketAPI market, String project, TextPanelAPI text)
    {
        Color highlight = Misc.getHighlightColor();
        Color good = Misc.getPositiveHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        switch (project) {
            case aridTypeChangeProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Arid world starting resources:");
                text.addPara("          - Adequate farmland, common organics, no volatiles");
                text.addPara("      - Arid world maximum resources:");
                text.addPara("          - Bountiful farmland, abundant organics, trace volatiles");
                text.addPara("      - Ore deposits are unaffected");
                break;
            case frozenTypeChangeProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Frozen world starting resources:");
                text.addPara("          - No farmland, no organics, abundant volatiles");
                text.addPara("      - Frozen world maximum resources:");
                text.addPara("          - No farmland, no organics, plentiful volatiles");
                text.addPara("      - Ore deposits are unaffected");
                break;
            case jungleTypeChangeProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Jungle world starting resources:");
                text.addPara("          - Adequate farmland, common organics, no volatiles");
                text.addPara("      - Jungle world maximum resources:");
                text.addPara("          - Bountiful farmland, plentiful organics, no volatiles");
                text.addPara("      - Ore deposits are unaffected");
                break;
            case terranTypeChangeProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Terran world starting resources:");
                if (boggledTools.getBooleanSetting("boggledTerraformingTypeChangeAddVolatiles")) {
                    text.addPara("          - Adequate farmland, trace organics, trace volatiles");
                } else {
                    text.addPara("          - Adequate farmland, trace organics, no volatiles");
                }
                text.addPara("      - Terran world maximum resources:");
                text.addPara("          - Bountiful farmland, plentiful organics, trace volatiles");
                text.addPara("      - Ore deposits are unaffected");
                break;
            case waterTypeChangeProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Water world starting resources:");
                text.addPara("          - No organics, no volatiles");
                text.addPara("      - Water world maximum resources:");
                text.addPara("          - Plentiful organics, plentiful volatiles");
                text.addPara("      - Ore deposits are unaffected");
                break;
            case tundraTypeChangeProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Tundra world starting resources:");
                if (boggledTools.getBooleanSetting("boggledTerraformingTypeChangeAddVolatiles")) {
                    text.addPara("          - Adequate farmland, trace organics, trace volatiles");
                } else {
                    text.addPara("          - Adequate farmland, trace organics, no volatiles");
                }
                text.addPara("      - Tundra world maximum resources:");
                text.addPara("          - Bountiful farmland, trace organics, plentiful volatiles");
                text.addPara("      - Ore deposits are unaffected");
                break;
            case farmlandResourceImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Farming yield improved by one");
                break;
            case organicsResourceImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Organics yield improved by one");
                break;
            case volatilesResourceImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Volatiles yield improved by one");
                break;
            case extremeWeatherConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Extreme weather patterns remediated");
                break;
            case mildClimateConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Climate made mild");
                break;
            case habitableConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Atmosphere made human-breathable");
                break;
            case atmosphereDensityConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Atmosphere with Earth-like density created");
                break;
            case toxicAtmosphereConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Atmospheric toxicity remediated");
                break;
            case irradiatedConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Radiation remediated");
                break;
            case removeAtmosphereConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, boggledTools.getTooltipProjectName(project));

                text.addPara("      - Atmosphere removed");
                break;
        }
    }

    public static int getPlanetWaterLevel(MarketAPI market)
    {
        // There are checks present elsewhere that will prevent passing in a station market.
        // If that happens anyway, it's best to just throw an exception.

        PlanetAPI planet = market.getPlanetEntity();
        String planetType = getPlanetType(planet);
        if(planetType.equals(waterPlanetID) || planetType.equals(frozenPlanetID) || planetType.equals("US_water") || planetType.equals("US_waterB") || hasIsmaraSling(market) || market.hasCondition(Conditions.WATER_SURFACE))
        {
            return 2;
        }
        else if(planetType.equals(desertPlanetID) || planetType.equals(terranPlanetID) || planetType.equals(tundraPlanetID) || planetType.equals(junglePlanetID) || (planetType.contains("US_") && market.hasCondition(Conditions.HABITABLE) && !market.hasCondition(Conditions.NO_ATMOSPHERE)))
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static boolean marketHasAtmosphereProcessor(MarketAPI market)
    {
        return market.getIndustry(BoggledIndustries.atmosphereProcessorIndustryID) != null && market.getIndustry(BoggledIndustries.atmosphereProcessorIndustryID).isFunctional() && market.hasIndustry(BoggledIndustries.atmosphereProcessorIndustryID);
    }

    public static boolean marketHasGenelab(MarketAPI market)
    {
        return market.getIndustry(BoggledIndustries.genelabIndustryID) != null && market.getIndustry(BoggledIndustries.genelabIndustryID).isFunctional() && market.hasIndustry(BoggledIndustries.genelabIndustryID);
    }

    public static boolean marketHasStellarReflectorArray(MarketAPI market)
    {
        // Should be functionally the same as the commented out code, unless the player disables autoplacement in the settings file.
        // Then this will allow terraforming on planets that start with reflectors without having to build the structure on them.
        return market.hasCondition(Conditions.SOLAR_ARRAY);

        /*
        if(market.getIndustry("BOGGLED_STELLAR_REFLECTOR_ARRAY") != null && market.getIndustry("BOGGLED_STELLAR_REFLECTOR_ARRAY").isFunctional() && market.hasIndustry("BOGGLED_STELLAR_REFLECTOR_ARRAY"))
        {
            return true;
        }
        else
        {
            return false;
        }
        */
    }

    public static boolean marketHasAtmoProblem(MarketAPI market)
    {
        if(!market.hasCondition(Conditions.MILD_CLIMATE) || !market.hasCondition(Conditions.HABITABLE) || market.hasCondition(Conditions.NO_ATMOSPHERE) || market.hasCondition(Conditions.THIN_ATMOSPHERE) || market.hasCondition(Conditions.DENSE_ATMOSPHERE) || market.hasCondition(Conditions.TOXIC_ATMOSPHERE) || market.hasCondition("US_storm"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static boolean marketIsIrradiated(MarketAPI market)
    {
        return market.hasCondition(Conditions.IRRADIATED);
    }

    public static int waterLevelNeededForProject(String project)
    {
        if(project.contains("TypeChange"))
        {
            if(project.equals(waterTypeChangeProjectID) || project.equals(frozenTypeChangeProjectID))
            {
                return 2;
            }
            else
            {
                // Need to adjust this if I end up allowing terraforming to US planet types or barren/toxic.
                // Assumes project type is Terran, jungle, arid or tundra.
                return 1;
            }
        }
        else if(project.contains("ResourceImprovement"))
        {
            if(project.equals(farmlandResourceImprovementProjectID))
            {
                return 2;
            }
            else if(project.equals(organicsResourceImprovementProjectID) || project.equals(volatilesResourceImprovementProjectID))
            {
                return 0;
            }
        }
        else if(project.contains("ConditionImprovement"))
        {
            if(project.equals(habitableConditionImprovementProjectID) || project.equals(mildClimateConditionImprovementProjectID) || project.equals(extremeWeatherConditionImprovementProjectID) || project.equals("noAtmosphereConditionImprovement") || project.equals("thinAtmosphereConditionImprovement"))
            {
                return 1;
            }
            else if(project.equals("denseAtmosphereConditionImprovement") || project.equals(toxicAtmosphereConditionImprovementProjectID))
            {
                return 0;
            }
        }

        // Should never be reached unless there's a bug present and/or bad value passed in
        return 0;
    }

    public static String getTooltipProjectName(String currentProject)
    {
        if(currentProject == null || currentProject.equals(noneProjectID))
        {
            return noneProjectID;
        }

        String tooltip = projectTooltip.get(currentProject);
        if(tooltip != null)
        {
            return tooltip;
        }
        else
        {
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

        market.addCondition(spriteControllerConditionID);
        market.addCondition(crampedQuartersConditionID);

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

        market.addCondition(spriteControllerConditionID);
        market.addCondition(crampedQuartersConditionID);

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

        market.addCondition(spriteControllerConditionID);
        market.addCondition(crampedQuartersConditionID);

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

    public static SectorEntityToken getPlanetTokenForQuest(String systemID, String entityID)
    {
        StarSystemAPI system = Global.getSector().getStarSystem(systemID);
        if(system != null)
        {
            SectorEntityToken possibleTarget = system.getEntityById(entityID);
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
        if(Global.getSettings().getModManager().isModEnabled(BoggledMods.lunalibModID))
        {
            return LunaSettings.getInt(BoggledMods.tascModID, key);
        }
        else
        {
            return Global.getSettings().getInt(key);
        }
    }

    public static boolean getBooleanSetting(String key)
    {
        if(Global.getSettings().getModManager().isModEnabled(BoggledMods.lunalibModID))
        {
            return LunaSettings.getBoolean(BoggledMods.tascModID, key);
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
        market.getPlanetEntity().changeType("water", null);
        sendDebugIntelMessage(market.getPlanetEntity().getTypeId());
        sendDebugIntelMessage(market.getPlanetEntity().getSpec().getPlanetType());
    }
}