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
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
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
import java.util.ArrayList;

import static java.util.Arrays.asList;

public class boggledTools
{
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

    private static final String jungleTypeChangeProjectID = "jungleTypeChange";
    private static final String aridTypeChangeProjectID = "aridTypeChange";
    private static final String terranTypeChangeProjectID = "terranTypeChange";
    private static final String waterTypeChangeProjectID = "waterTypeChange";
    private static final String tundraTypeChangeProjectID = "tundraTypeChange";
    private static final String frozenTypeChangeProjectID = "frozenTypeChange";

    private static final String farmlandResourceImprovementProjectID = "farmlandResourceImprovement";
    private static final String organicsResourceImprovementProjectID = "organicsResourceImprovement";
    private static final String volatilesResourceImprovementProjectID = "volatilesResourceImprovement";

    private static final String extremeWeatherConditionImprovementProjectID = "extremeWeatherConditionImprovement";
    private static final String mildClimateConditionImprovementProjectID = "mildClimateConditionImprovement";
    private static final String habitableConditionImprovementProjectID = "habitableConditionImprovement";
    private static final String atmosphereDensityConditionImprovementProjectID = "atmosphereDensityConditionImprovement";
    private static final String toxicAtmosphereConditionImprovementProjectID = "toxicAtmosphereConditionImprovement";
    private static final String irradiatedConditionImprovementProjectID = "irradiatedConditionImprovement";
    private static final String radiationConditionImprovementProjectID = "radiationConditionImprovement";
    private static final String removeAtmosphereConditionImprovementProjectID = "removeAtmosphereConditionImprovement";

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

    private static final HashMap<String, String[]> projectRequirements = initialiseProjectRequirements();

    private static final HashMap<String, String> projectTooltip = initialiseProjectTooltips();

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
            if (entity.hasTag("gate")) {
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
        return boggledTools.getIntSetting("boggledMarketSizeRequiredToBuildInactiveGate");
    }

    public static SectorEntityToken getClosestPlayerMarketToken(SectorEntityToken playerFleet) {
        if (!playerMarketInSystem(playerFleet)) {
            return null;
        } else {
            ArrayList<SectorEntityToken> allPlayerMarketsInSystem = new ArrayList<SectorEntityToken>();

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
            ArrayList<SectorEntityToken> allGasGiantsInSystem = new ArrayList<SectorEntityToken>();

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
            if (entity.hasTag("station") && entity.getMarket() != null && entity.getMarket().hasCondition(Conditions.ABANDONED_STATION)) {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestColonizableStationInSystem(SectorEntityToken playerFleet) {
        if (!colonizableStationInSystem(playerFleet)) {
            return null;
        } else {
            ArrayList<SectorEntityToken> allColonizableStationsInSystem = new ArrayList<SectorEntityToken>();

            for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
                if (entity.hasTag("station") && entity.getMarket() != null && entity.getMarket().hasCondition(Conditions.ABANDONED_STATION)) {
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
            if (entity.hasTag("station")) {
                return true;
            }
        }

        return false;
    }

    public static SectorEntityToken getClosestStationInSystem(SectorEntityToken playerFleet) {
        if (!stationInSystem(playerFleet)) {
            return null;
        } else {
            ArrayList<SectorEntityToken> allStationsInSystem = new ArrayList<SectorEntityToken>();

            for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
                if (entity.hasTag("station")) {
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
        ArrayList<String> factionsWithMarketInSystem = new ArrayList<String>();

        for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
            if (!factionsWithMarketInSystem.contains(market.getFactionId())) {
                factionsWithMarketInSystem.add(market.getFactionId());
            }
        }

        return factionsWithMarketInSystem;
    }

    public static ArrayList<Integer> getCompanionListOfTotalMarketPopulation(StarSystemAPI system, ArrayList<String> factions) {
        ArrayList<Integer> totalFactionMarketSize = new ArrayList<Integer>();
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
            if (planet instanceof PlanetAPI && !getPlanetType(((PlanetAPI) planet)).equals("star")) {
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
            ArrayList<SectorEntityToken> allPlanetsInSystem = new ArrayList<SectorEntityToken>();

            for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
                if (entity instanceof PlanetAPI && !getPlanetType(((PlanetAPI) entity)).equals("star")) {
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
                if(!market.getFactionId().equals("neutral"))
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
            return "unknown";
        }

        // Added this to catch Unknown Skies planets or other modded planet types
        if(planet.getMarket() != null && planet.getMarket().hasCondition("water_surface"))
        {
            return "water";
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
                return "star";
            case "gas_giant":
            case "ice_giant":
            case "US_gas_giant":
            case "US_gas_giantB":
            case "fds_gas_giant":
            case "SCY_tartarus":
            case "galaxytigers_gas_giant":
                return "gas_giant";
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
                return "barren";
            case "toxic":
            case "toxic_cold":
            case "US_green":
            case "SCY_acid":
                return "toxic";
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
                return "desert";
            case "terran":
            case "terran-eccentric":
            case "US_lifeless":
            case "US_alkali":
            case "US_continent":
            case "US_magnetic":
            case "US_water":
            case "US_waterB":
            case "terran_adapted":
                return "terran";
            case "water":
                return "water";
            case "tundra":
            case "US_purple":
            case "fds_tundra":
            case "galaxytigers_tundra":
                return "tundra";
            case "jungle":
            case "US_jungle":
            case "jungle_charkha":
                return "jungle";
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
                return "frozen";
            case "lava":
            case "lava_minor":
            case "US_lava":
            case "US_volcanic":
            case "fds_lava":
                return "volcanic";
            default:
                return "unknown";
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
                if(!market.hasCondition("terraforming_controller"))
                {
                    boggledTools.addCondition(market, "terraforming_controller");
                }
                allNonStationPlayerMarkets.add(market);
            }
        }

        return allNonStationPlayerMarkets;
    }

    public static boolean marketIsStation(MarketAPI market) {
        return market.getPrimaryEntity() == null || market.getPlanetEntity() == null || market.getPrimaryEntity().hasTag("station");
    }

    public static boolean terraformingPossibleOnMarket(MarketAPI market) {
        if (marketIsStation(market)) {
            return false;
        }

        if (market.hasCondition("irradiated")) {
            return false;
        }

        String planetType = boggledTools.getPlanetType(market.getPlanetEntity());
        return !planetType.equals("star") && !planetType.equals("gas_giant") && !planetType.equals("volcanic") && !planetType.equals("unknown");
    }

    public static boolean getCreateMirrorsOrShades(MarketAPI market) {
        // Return true for mirrors, false for shades
        // Go by temperature first. If not triggered, will check planet type. Otherwise, just return true.

        if (market.hasCondition("poor_light") || market.hasCondition("very_cold") || market.hasCondition("cold")) {
            return true;
        } else if (market.hasCondition("very_hot") || market.hasCondition("hot")) {
            return false;
        }

        if (boggledTools.getPlanetType(market.getPlanetEntity()).equals("desert") || boggledTools.getPlanetType(market.getPlanetEntity()).equals("jungle")) {
            return false;
        } else if (boggledTools.getPlanetType(market.getPlanetEntity()).equals("tundra") || boggledTools.getPlanetType(market.getPlanetEntity()).equals("frozen")) {
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
        if (numAsteroidTerrains >= boggledTools.getIntSetting("boggledMiningStationUltrarichOre")) {
            return "ultrarich";
        }
        if (numAsteroidTerrains >= boggledTools.getIntSetting("boggledMiningStationRichOre")) {
            return "rich";
        }
        if (numAsteroidTerrains >= boggledTools.getIntSetting("boggledMiningStationAbundantOre")) {
            return "abundant";
        }
        if (numAsteroidTerrains >= boggledTools.getIntSetting("boggledMiningStationModerateOre")) {
            return "moderate";
        } else if (numAsteroidTerrains >= boggledTools.getIntSetting("boggledMiningStationSparseOre")) {
            return "sparse";
        } else {
            return "abundant";
        }
    }

    public static int getNumberOfStationExpansions(MarketAPI market) {
        for (String tag : market.getTags()) {
            if (tag.contains("boggled_station_construction_numExpansions_")) {
                return Integer.parseInt(tag.substring(tag.length() - 1));
            }
        }

        return 0;
    }

    public static void incrementNumberOfStationExpansions(MarketAPI market) {
        if (getNumberOfStationExpansions(market) == 0) {
            market.addTag("boggled_station_construction_numExpansions_1");
        } else {
            int numExpansionsOld = getNumberOfStationExpansions(market);
            market.removeTag("boggled_station_construction_numExpansions_" + numExpansionsOld);
            market.addTag("boggled_station_construction_numExpansions_" + (numExpansionsOld + 1));
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

    public static boolean playerTooClose(StarSystemAPI system)
    {
        return Global.getSector().getPlayerFleet().isInOrNearSystem(system);
    }

    public static void clearConnectedPlanets(MarketAPI market)
    {
        SectorEntityToken targetEntityToRemove = null;
        for (SectorEntityToken entity : market.getConnectedEntities()) {
            if (entity instanceof PlanetAPI && !entity.hasTag("station")) {
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
            if (entity.hasTag("station")) {
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
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains("stellar_mirror") || entity.getId().contains("stellar_shade") || entity.hasTag("stellar_mirror") || entity.hasTag("stellar_shade"))) {
                numReflectors++;
            }
        }

        return numReflectors;
    }

    public static int numMirrorsInOrbit(MarketAPI market)
    {
        int numMirrors = 0;

        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains("stellar_mirror") || entity.hasTag("stellar_mirror"))) {
                numMirrors++;
            }
        }

        return numMirrors;
    }

    public static int numShadesInOrbit(MarketAPI market)
    {
        int numShades = 0;

        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains("stellar_shade") || entity.hasTag("stellar_shade"))) {
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
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && (entity.getId().contains("stellar_mirror") || entity.getId().contains("stellar_shade") || entity.hasTag("stellar_mirror") || entity.hasTag("stellar_shade")))
            {
                allEntitiesInSystem.remove();
                market.getStarSystem().removeEntity(entity);
            }
        }
    }

    public static boolean hasIsmaraSling(MarketAPI market)
    {
        for (MarketAPI marketElement : Global.getSector().getEconomy().getMarkets(market.getStarSystem())) {
            if (marketElement.getFactionId().equals(market.getFactionId()) && marketElement.hasIndustry("BOGGLED_ISMARA_SLING") && marketElement.getIndustry("BOGGLED_ISMARA_SLING").isFunctional()) {
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
        if(Global.getSettings().getModManager().isModEnabled("illustrated_entities"))
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
                            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && entity.getCircularOrbitAngle() == station.getCircularOrbitAngle() && (entity.hasTag("boggled_lights_overlay_astropolis_alpha_small") || entity.hasTag("boggled_lights_overlay_astropolis_alpha_medium") || entity.hasTag("boggled_lights_overlay_astropolis_alpha_large"))) {
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
                            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && entity.getCircularOrbitAngle() == station.getCircularOrbitAngle() && (entity.hasTag("boggled_lights_overlay_astropolis_beta_small") || entity.hasTag("boggled_lights_overlay_astropolis_beta_medium") || entity.hasTag("boggled_lights_overlay_astropolis_beta_large"))) {
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
                            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && entity.getCircularOrbitAngle() == station.getCircularOrbitAngle() && (entity.hasTag("boggled_lights_overlay_astropolis_gamma_small") || entity.hasTag("boggled_lights_overlay_astropolis_gamma_medium") || entity.hasTag("boggled_lights_overlay_astropolis_gamma_large"))) {
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
                    if (entity.hasTag("boggled_lights_overlay_mining_small") || entity.hasTag("boggled_lights_overlay_mining_medium")) {
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
                    if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(station.getOrbitFocus()) && (entity.hasTag("boggled_lights_overlay_siphon_small") || entity.hasTag("boggled_lights_overlay_siphon_medium"))) {
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
            if (entity.hasTag("boggled_mining_station_small") && !entity.hasTag("boggled_already_reapplied_lights_overlay")) {
                stationToApplyOverlayTo = entity;
                stationsize = 1;
                entity.addTag("boggled_already_reapplied_lights_overlay");
                break;
            } else if (entity.hasTag("boggled_mining_station_medium") && !entity.hasTag("boggled_already_reapplied_lights_overlay")) {
                stationToApplyOverlayTo = entity;
                stationsize = 2;
                entity.addTag("boggled_already_reapplied_lights_overlay");
                break;
            }
        }

        if(stationToApplyOverlayTo != null)
        {
            if(stationsize == 1)
            {
                if(!stationToApplyOverlayTo.getMarket().getFactionId().equals("neutral"))
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
                if (entity.hasTag("boggled_already_reapplied_lights_overlay")) {
                    entity.removeTag("boggled_already_reapplied_lights_overlay");
                }
            }
        }
    }

    public static boolean marketHasOrbitalStation(MarketAPI market)
    {
        for (SectorEntityToken entity : market.getStarSystem().getAllEntities()) {
            if (entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(market.getPrimaryEntity()) && entity.hasTag("station")) {
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

        if(getPlanetType(planet).equals("star") || getPlanetType(planet).equals("gas_giant") || getPlanetType(planet).equals("barren") || getPlanetType(planet).equals("toxic") || getPlanetType(planet).equals("volcanic") || getPlanetType(planet).equals("frozen") || getPlanetType(planet).equals("water") || getPlanetType(planet).equals("unknown"))
        {
            return 0;
        }
        else if(getPlanetType(planet).equals("jungle") || getPlanetType(planet).equals("desert") || getPlanetType(planet).equals("terran") || getPlanetType(planet).equals("tundra"))
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

    public static int getMaxOrganicsForMarket(MarketAPI market)
    {
        // Returns 0 if the planet can't have organics
        // Returns 1 through 4 for the levels of organics, with 1 being trace and 4 being plentiful

        PlanetAPI planet = market.getPlanetEntity();

        if(getPlanetType(planet).equals("star") || getPlanetType(planet).equals("gas_giant") || getPlanetType(planet).equals("barren") || getPlanetType(planet).equals("toxic") || getPlanetType(planet).equals("volcanic") || getPlanetType(planet).equals("frozen") || getPlanetType(planet).equals("unknown"))
        {
            return 0;
        }
        else if(getPlanetType(planet).equals("water") || getPlanetType(planet).equals("jungle") || getPlanetType(planet).equals("terran"))
        {
            return 4;
        }
        else if(getPlanetType(planet).equals("desert"))
        {
            return 3;
        }
        else if(getPlanetType(planet).equals("tundra"))
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

    public static int getMaxVolatilesForMarket(MarketAPI market)
    {
        // Returns 0 if the planet can't have volatiles
        // Returns 1 through 4 for the levels of volatiles, with 1 being trace and 4 being plentiful

        PlanetAPI planet = market.getPlanetEntity();

        if(getPlanetType(planet).equals("star") || getPlanetType(planet).equals("gas_giant") || getPlanetType(planet).equals("barren") || getPlanetType(planet).equals("toxic") || getPlanetType(planet).equals("volcanic") || getPlanetType(planet).equals("jungle") || getPlanetType(planet).equals("unknown"))
        {
            return 0;
        }
        else if(getPlanetType(planet).equals("frozen") || getPlanetType(planet).equals("tundra") || getPlanetType(planet).equals("water"))
        {
            return 4;
        }
        else if(getPlanetType(planet).equals("desert") || getPlanetType(planet).equals("terran"))
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

    public static void incrementFarmland(MarketAPI market)
    {
        if(market.hasCondition("farmland_poor"))
        {
            boggledTools.removeCondition(market, "farmland_poor");
            boggledTools.addCondition(market, "farmland_adequate");
        }
        else if(market.hasCondition("farmland_adequate"))
        {
            boggledTools.removeCondition(market, "farmland_adequate");
            boggledTools.addCondition(market, "farmland_rich");
        }
        else if(market.hasCondition("farmland_rich"))
        {
            boggledTools.removeCondition(market, "farmland_rich");
            boggledTools.addCondition(market, "farmland_bountiful");
        }
        else if(market.hasCondition("farmland_bountiful"))
        {
            //Do nothing
        }
        else
        {
            boggledTools.addCondition(market, "farmland_poor");
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
        if(market.hasCondition("organics_trace"))
        {
            boggledTools.removeCondition(market, "organics_trace");
            boggledTools.addCondition(market, "organics_common");
        }
        else if(market.hasCondition("organics_common"))
        {
            boggledTools.removeCondition(market, "organics_common");
            boggledTools.addCondition(market, "organics_abundant");
        }
        else if(market.hasCondition("organics_abundant"))
        {
            boggledTools.removeCondition(market, "organics_abundant");
            boggledTools.addCondition(market, "organics_plentiful");
        }
        else if(market.hasCondition("organics_plentiful"))
        {
            //Do nothing
        }
        else
        {
            boggledTools.addCondition(market, "organics_trace");
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
        if(market.hasCondition("volatiles_trace"))
        {
            boggledTools.removeCondition(market, "volatiles_trace");
            boggledTools.addCondition(market, "volatiles_diffuse");
        }
        else if(market.hasCondition("volatiles_diffuse"))
        {
            boggledTools.removeCondition(market, "volatiles_diffuse");
            boggledTools.addCondition(market, "volatiles_abundant");
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
            boggledTools.addCondition(market, "volatiles_trace");
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

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);
        boggledTools.refreshAquacultureAndFarming(market);
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
            for (SectorEntityToken entity : closestGasGiantToken.getStarSystem().getAllEntities()) {
                if (entity.hasTag("station") && entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(closestGasGiantToken) && (entity.getCustomEntitySpec().getDefaultName().equals("Side Station") || entity.getCustomEntitySpec().getDefaultName().equals("Siphon Station")) && !entity.getId().equals("beholder_station")) {
                    if (entity.getMarket() != null) {
                        market = entity.getMarket();
                        if (market.hasCondition("volatiles_trace")) {
                            boggledTools.removeCondition(market, "volatiles_trace");
                            boggledTools.addCondition(market, "volatiles_abundant");
                        } else if (market.hasCondition("volatiles_diffuse")) {
                            boggledTools.removeCondition(market, "volatiles_diffuse");
                            boggledTools.addCondition(market, "volatiles_plentiful");
                        } else if (market.hasCondition("volatiles_abundant")) {
                            boggledTools.removeCondition(market, "volatiles_abundant");
                            boggledTools.addCondition(market, "volatiles_plentiful");
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
        if(Misc.isStoryCritical(market) && !boggledTools.getBooleanSetting("boggledPlanetKillerAllowDestructionOfColoniesMarkedAsEssentialForQuests"))
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
        if(!planetType.equals("star") && !planetType.equals("gas_giant") && !planetType.equals("volcanic") && !planetType.equals("unknown"))
        {
            changePlanetType(market.getPlanetEntity(), "irradiated");
            market.addCondition(Conditions.IRRADIATED);
        }
    }

    public static void changePlanetConditionsWithPlanetKiller(MarketAPI market)
    {
        // Modded conditions

        // Vanilla Conditions
        removeCondition(market, "habitable");
        removeCondition(market, "mild_climate");
        removeCondition(market, "water_surface");
        removeCondition(market, "volturnian_lobster_pens");

        removeCondition(market, "inimical_biosphere");

        removeCondition(market, "farmland_poor");
        removeCondition(market, "farmland_adequate");
        removeCondition(market, "farmland_rich");
        removeCondition(market, "farmland_bountiful");

        String planetType = getPlanetType(market.getPlanetEntity());
        if(!planetType.equals("gas_giant") && !planetType.equals("unknown"))
        {
            removeCondition(market, "organics_trace");
            removeCondition(market, "organics_common");
            removeCondition(market, "organics_abundant");
            removeCondition(market, "organics_plentiful");

            removeCondition(market, "volatiles_trace");
            removeCondition(market, "volatiles_diffuse");
            removeCondition(market, "volatiles_abundant");
            removeCondition(market, "volatiles_plentiful");
        }
    }

    public static List<FactionAPI> factionsToMakeHostileDueToPlanetKillerUsage(MarketAPI market)
    {
        List<FactionAPI> factionsToMakeHostile = new ArrayList<>();
        for(FactionAPI faction : Global.getSector().getAllFactions())
        {
            String factionId = faction.getId();
            if(factionId.equals("luddic_path") && market.getFactionId().equals("luddic_path"))
            {
                factionsToMakeHostile.add(faction);
            }

            if(!factionId.equals("player") && !factionId.equals("derelict") && !factionId.equals("luddic_path") && !factionId.equals("omega") && !factionId.equals("remnants") && !factionId.equals("sleepers"))
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
            faction.setRelationship("player", -100f);
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

        switch (newPlanetType) {
            case "jungle":
                // Modded conditions
                removeCondition(market, "US_storm");

                // Vanilla Conditions
                addCondition(market, "habitable");
                removeCondition(market, "water_surface");
                removeCondition(market, "volturnian_lobster_pens");

                removeCondition(market, "farmland_poor");
                addCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                removeCondition(market, "farmland_bountiful");

                removeCondition(market, "organics_trace");
                addCondition(market, "organics_common");
                removeCondition(market, "organics_abundant");
                removeCondition(market, "organics_plentiful");

                removeCondition(market, "volatiles_trace");
                removeCondition(market, "volatiles_diffuse");
                removeCondition(market, "volatiles_abundant");
                removeCondition(market, "volatiles_plentiful");
                break;
            case "arid":
                // Modded conditions

                // Vanilla Conditions
                addCondition(market, "habitable");
                removeCondition(market, "water_surface");
                removeCondition(market, "volturnian_lobster_pens");

                removeCondition(market, "farmland_poor");
                addCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                removeCondition(market, "farmland_bountiful");

                removeCondition(market, "organics_trace");
                addCondition(market, "organics_common");
                removeCondition(market, "organics_abundant");
                removeCondition(market, "organics_plentiful");

                removeCondition(market, "volatiles_trace");
                removeCondition(market, "volatiles_diffuse");
                removeCondition(market, "volatiles_abundant");
                removeCondition(market, "volatiles_plentiful");
                break;
            case "terran":
            case "US_auric":
            case "US_water":
            case "US_continent":
                // Modded conditions
                removeCondition(market, "US_storm");

                // Vanilla Conditions
                addCondition(market, "habitable");
                removeCondition(market, "water_surface");
                removeCondition(market, "volturnian_lobster_pens");

                removeCondition(market, "farmland_poor");
                addCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                removeCondition(market, "farmland_bountiful");

                addCondition(market, "organics_trace");
                removeCondition(market, "organics_common");
                removeCondition(market, "organics_abundant");
                removeCondition(market, "organics_plentiful");

                if (boggledTools.getBooleanSetting("boggledTerraformingTypeChangeAddVolatiles")) {
                    addCondition(market, "volatiles_trace");
                } else {
                    removeCondition(market, "volatiles_trace");
                }
                removeCondition(market, "volatiles_diffuse");
                removeCondition(market, "volatiles_abundant");
                removeCondition(market, "volatiles_plentiful");
                break;
            case "water":
                // Modded conditions
                removeCondition(market, "US_storm");

                // Vanilla Conditions
                addCondition(market, "habitable");
                addCondition(market, "water_surface");

                removeCondition(market, "farmland_poor");
                removeCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                removeCondition(market, "farmland_bountiful");

                removeCondition(market, "organics_trace");
                removeCondition(market, "organics_common");
                removeCondition(market, "organics_abundant");
                removeCondition(market, "organics_plentiful");

                removeCondition(market, "volatiles_trace");
                removeCondition(market, "volatiles_diffuse");
                removeCondition(market, "volatiles_abundant");
                removeCondition(market, "volatiles_plentiful");
                break;
            case "tundra":
                // Modded conditions
                removeCondition(market, "US_storm");

                // Vanilla Conditions
                addCondition(market, "habitable");
                removeCondition(market, "water_surface");
                removeCondition(market, "volturnian_lobster_pens");

                removeCondition(market, "farmland_poor");
                addCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                removeCondition(market, "farmland_bountiful");

                addCondition(market, "organics_trace");
                removeCondition(market, "organics_common");
                removeCondition(market, "organics_abundant");
                removeCondition(market, "organics_plentiful");

                if (boggledTools.getBooleanSetting("boggledTerraformingTypeChangeAddVolatiles")) {
                    addCondition(market, "volatiles_trace");
                } else {
                    removeCondition(market, "volatiles_trace");
                }
                removeCondition(market, "volatiles_diffuse");
                removeCondition(market, "volatiles_abundant");
                removeCondition(market, "volatiles_plentiful");
                break;
            case "frozen":
                // Modded conditions
                removeCondition(market, "US_storm");

                // Vanilla Conditions
                removeCondition(market, "habitable");
                removeCondition(market, "water_surface");
                removeCondition(market, "volturnian_lobster_pens");

                removeCondition(market, "farmland_poor");
                removeCondition(market, "farmland_adequate");
                removeCondition(market, "farmland_rich");
                removeCondition(market, "farmland_bountiful");

                removeCondition(market, "organics_trace");
                removeCondition(market, "organics_common");
                removeCondition(market, "organics_abundant");
                removeCondition(market, "organics_plentiful");

                removeCondition(market, "volatiles_trace");
                removeCondition(market, "volatiles_diffuse");
                addCondition(market, "volatiles_abundant");
                removeCondition(market, "volatiles_plentiful");
                break;
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

    public static String[] getAllTerraformingProjects()
    {
        return new String[]{
                "aridTypeChange",
                "jungleTypeChange",
                "terranTypeChange",
                "waterTypeChange",
                "tundraTypeChange",
                "frozenTypeChange",
                "farmlandResourceImprovement",
                "organicsResourceImprovement",
                "volatilesResourceImprovement",
                "extremeWeatherConditionImprovement",
                "mildClimateConditionImprovement",
                "habitableConditionImprovement",
                "atmosphereDensityConditionImprovement",
                "toxicAtmosphereConditionImprovement",
                "irradiatedConditionImprovement",
                "removeAtmosphereConditionImprovement"
        };
    }

    public static String[] getEnabledTerraformingProjects()
    {
        ArrayList<String> enabledProjects = new ArrayList<>();
        String[] allProjects = getAllTerraformingProjects();
        for(String project : allProjects)
        {
            // Only add radiation project if it's enabled in settings
            if(project.equals("irradiatedConditionImprovement"))
            {
                if(getBooleanSetting("boggledTerraformingRemoveRadiationProjectEnabled"))
                {
                    enabledProjects.add(project);
                }
            }
            // Only add atmosphere removal project if it's enabled in settings
            else if(project.equals("removeAtmosphereConditionImprovement"))
            {
                if(getBooleanSetting("boggledTerraformingRemoveAtmosphereProjectEnabled"))
                {
                    enabledProjects.add(project);
                }
            }
            else
            {
                enabledProjects.add(project);
            }
        }

        return enabledProjects.toArray(new String[0]);
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

            int artifactCost = boggledTools.getIntSetting("boggledDomainTechCraftingArtifactCost");
            int storyPointCost = boggledTools.getIntSetting("boggledDomainTechCraftingStoryPointCost");
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

    private static String[] appendLineTostringArray(String[] existingArray, String lineToAppend)
    {
        if(lineToAppend == null)
        {
            return existingArray;
        }
        else
        {
            String[] returnArray = new String[existingArray.length + 1];
            System.arraycopy(existingArray, 0, returnArray, 0, existingArray.length);
            returnArray[existingArray.length] = lineToAppend;
            return returnArray;
        }
    }

    public static boolean requirementMet(MarketAPI market, String requirement)
    {
        PlanetAPI planet = null;
        String planetType = "";
        Integer planetWaterLevel = 0;

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
            return !planetType.equals("desert");
        }
        else if(requirement.equals(colonyNotJungleWorld))
        {
            return !planetType.equals("jungle");
        }
        else if(requirement.equals(colonyNotTerranWorld))
        {
            return !planetType.equals("terran");
        }
        else if(requirement.equals(colonyNotWaterWorld))
        {
            return !planetType.equals("water");
        }
        else if(requirement.equals(colonyNotTundraWorld))
        {
            return !planetType.equals("tundra");
        }
        else if(requirement.equals(colonyNotFrozenWorld))
        {
            return !planetType.equals("frozen");
        }
        else if(requirement.equals(colonyBarrenOrFrozen))
        {
            return planetType.equals("barren") || planetType.equals("frozen");
        }
        else if(requirement.equals(colonyAtmosphericDensityNormal))
        {
            if(market.hasCondition("no_atmosphere") || market.hasCondition("thin_atmosphere") || market.hasCondition("dense_atmosphere"))
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
            if(market.hasCondition("toxic_atmosphere") || market.hasCondition("irradiated"))
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
            return !market.hasCondition("cold") && !market.hasCondition("very_cold");
        }
        else if(requirement.equals(colonyHotOrVeryHot))
        {
            return market.hasCondition("hot") || market.hasCondition("very_hot");
        }
        else if(requirement.equals(colonyNotVeryColdOrVeryHot))
        {
            return !market.hasCondition("very_hot") && !market.hasCondition("very_cold");
        }
        else if(requirement.equals(colonyNotHotOrVeryHot))
        {
            return !market.hasCondition("hot") && !market.hasCondition("very_hot");
        }
        else if(requirement.equals(colonyVeryCold))
        {
            return market.hasCondition("very_cold");
        }
        else if(requirement.equals(colonyTemperateOrHot))
        {
            return !market.hasCondition("very_cold") && !market.hasCondition("cold") && !market.hasCondition("very_hot");
        }
        else if(requirement.equals(colonyTemperateOrCold))
        {
            return !market.hasCondition("very_cold") && !market.hasCondition("hot") && !market.hasCondition("very_hot");
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
            return market.hasCondition("habitable");
        }
        else if(requirement.equals(colonyNotAlreadyHabitable))
        {
            return !market.hasCondition("habitable");
        }
        else if(requirement.equals(colonyExtremeWeather))
        {
            return market.hasCondition("extreme_weather") || market.hasCondition("US_storm");
        }
        else if(requirement.equals(colonyNormalClimate))
        {
            return !market.hasCondition("mild_climate") && !market.hasCondition("extreme_weather") && !market.hasCondition("US_storm");
        }
        else if(requirement.equals(colonyAtmosphereToxic))
        {
            return market.hasCondition("toxic_atmosphere");
        }
        else if(requirement.equals(colonyIrradiated))
        {
            return market.hasCondition("irradiated");
        }
        else if(requirement.equals(colonyHasAtmosphere))
        {
            return !market.hasCondition("no_atmosphere");
        }
        else if(requirement.equals(colonyAtmosphereSuboptimalDensity))
        {
            return market.hasCondition("no_atmosphere") || market.hasCondition("thin_atmosphere") || market.hasCondition("dense_atmosphere");
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
            if(planetType.equals("star") || planetType.equals("gas_giant") || planetType.equals("volcanic") || planetType.equals("unknown"))
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
            if(planetType.equals("jungle") || planetType.equals("desert") || planetType.equals("terran") || planetType.equals("water") || planetType.equals("tundra"))
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
                    if (data.getId().equals("pristine_nanoforge") || data.getId().equals("uaf_dimen_nanoforge")) {
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
            return playerCargo.getCommodityQuantity("domain_artifacts") >= Integer.parseInt(words[5]);
        }
        else if(requirement.equals(boggledTools.getIntSetting("boggledDomainTechCraftingStoryPointCost") + " story points available to spend"))
        {
            MutableCharacterStatsAPI charStats = Global.getSector().getPlayerStats();
            return charStats.getStoryPoints() >= boggledTools.getIntSetting("boggledDomainTechCraftingStoryPointCost");
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

        if(project != null && !project.equals("None"))
        {
            // Print requirements, and if not met, print terraforming is stalled
            text.addPara("Project Requirements:", highlight, new String[]{""});
            String[] requirements = boggledTools.getProjectRequirementsStrings(project);
            Boolean foundUnmetRequirement = false;
            for (String requirement : requirements) {
                if (boggledTools.requirementMet(market, requirement)) {
                    text.addPara("      - %s", good, new String[]{requirement + ""});
                } else {
                    text.addPara("      - %s", bad, new String[]{requirement + ""});
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
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Arid world starting resources:", highlight, new String[]{""});
                text.addPara("          - Adequate farmland, common organics, no volatiles", highlight, new String[]{""});
                text.addPara("      - Arid world maximum resources:", highlight, new String[]{""});
                text.addPara("          - Bountiful farmland, abundant organics, trace volatiles", highlight, new String[]{""});
                text.addPara("      - Ore deposits are unaffected", highlight, new String[]{""});
                break;
            case frozenTypeChangeProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Frozen world starting resources:", highlight, new String[]{""});
                text.addPara("          - No farmland, no organics, abundant volatiles", highlight, new String[]{""});
                text.addPara("      - Frozen world maximum resources:", highlight, new String[]{""});
                text.addPara("          - No farmland, no organics, plentiful volatiles", highlight, new String[]{""});
                text.addPara("      - Ore deposits are unaffected", highlight, new String[]{""});
                break;
            case jungleTypeChangeProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Jungle world starting resources:", highlight, new String[]{""});
                text.addPara("          - Adequate farmland, common organics, no volatiles", highlight, new String[]{""});
                text.addPara("      - Jungle world maximum resources:", highlight, new String[]{""});
                text.addPara("          - Bountiful farmland, plentiful organics, no volatiles", highlight, new String[]{""});
                text.addPara("      - Ore deposits are unaffected", highlight, new String[]{""});
                break;
            case terranTypeChangeProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Terran world starting resources:", highlight, new String[]{""});
                if (boggledTools.getBooleanSetting("boggledTerraformingTypeChangeAddVolatiles")) {
                    text.addPara("          - Adequate farmland, trace organics, trace volatiles", highlight, new String[]{""});
                } else {
                    text.addPara("          - Adequate farmland, trace organics, no volatiles", highlight, new String[]{""});
                }
                text.addPara("      - Terran world maximum resources:", highlight, new String[]{""});
                text.addPara("          - Bountiful farmland, plentiful organics, trace volatiles", highlight, new String[]{""});
                text.addPara("      - Ore deposits are unaffected", highlight, new String[]{""});
                break;
            case waterTypeChangeProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Water world starting resources:", highlight, new String[]{""});
                text.addPara("          - No organics, no volatiles", highlight, new String[]{""});
                text.addPara("      - Water world maximum resources:", highlight, new String[]{""});
                text.addPara("          - Plentiful organics, plentiful volatiles", highlight, new String[]{""});
                text.addPara("      - Ore deposits are unaffected", highlight, new String[]{""});
                break;
            case tundraTypeChangeProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Tundra world starting resources:", highlight, new String[]{""});
                if (boggledTools.getBooleanSetting("boggledTerraformingTypeChangeAddVolatiles")) {
                    text.addPara("          - Adequate farmland, trace organics, trace volatiles", highlight, new String[]{""});
                } else {
                    text.addPara("          - Adequate farmland, trace organics, no volatiles", highlight, new String[]{""});
                }
                text.addPara("      - Tundra world maximum resources:", highlight, new String[]{""});
                text.addPara("          - Bountiful farmland, trace organics, plentiful volatiles", highlight, new String[]{""});
                text.addPara("      - Ore deposits are unaffected", highlight, new String[]{""});
                break;
            case farmlandResourceImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Farming yield improved by one", highlight, new String[]{""});
                break;
            case organicsResourceImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Organics yield improved by one", highlight, new String[]{""});
                break;
            case volatilesResourceImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Volatiles yield improved by one", highlight, new String[]{""});
                break;
            case extremeWeatherConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Extreme weather patterns remediated", highlight, new String[]{""});
                break;
            case mildClimateConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Climate made mild", highlight, new String[]{""});
                break;
            case habitableConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Atmosphere made human-breathable", highlight, new String[]{""});
                break;
            case atmosphereDensityConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Atmosphere with Earth-like density created", highlight, new String[]{""});
                break;
            case toxicAtmosphereConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Atmospheric toxicity remediated", highlight, new String[]{""});
                break;
            case irradiatedConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Radiation remediated", highlight, new String[]{""});
                break;
            case removeAtmosphereConditionImprovementProjectID:
                text.addPara("Prospective project: %s", highlight, new String[]{boggledTools.getTooltipProjectName(project)});

                text.addPara("      - Atmosphere removed", highlight, new String[]{""});
                break;
        }
    }

    public static int getPlanetWaterLevel(MarketAPI market)
    {
        // There are checks present elsewhere that will prevent passing in a station market.
        // If that happens anyway, it's best to just throw an exception.

        PlanetAPI planet = market.getPlanetEntity();
        String planetType = getPlanetType(planet);
        if(planetType.equals("water") || planetType.equals("frozen") || planetType.equals("US_water") || planetType.equals("US_waterB") || hasIsmaraSling(market) || market.hasCondition("water_surface"))
        {
            return 2;
        }
        else if(planetType.equals("desert") || planetType.equals("terran") || planetType.equals("tundra") || planetType.equals("jungle") || (planetType.contains("US_") && market.hasCondition("habitable") && !market.hasCondition("no_atmosphere")))
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
        if(market.getIndustry("BOGGLED_ATMOSPHERE_PROCESSOR") != null && market.getIndustry("BOGGLED_ATMOSPHERE_PROCESSOR").isFunctional() && market.hasIndustry("BOGGLED_ATMOSPHERE_PROCESSOR"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static boolean marketHasGenelab(MarketAPI market)
    {
        if(market.getIndustry("BOGGLED_GENELAB") != null && market.getIndustry("BOGGLED_GENELAB").isFunctional() && market.hasIndustry("BOGGLED_GENELAB"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static boolean marketHasStellarReflectorArray(MarketAPI market)
    {
        // Should be functionally the same as the commented out code, unless the player disables autoplacement in the settings file.
        // Then this will allow terraforming on planets that start with reflectors without having to build the structure on them.
        return market.hasCondition("solar_array");

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
        if(!market.hasCondition("mild_climate") || !market.hasCondition("habitable") || market.hasCondition("no_atmosphere") || market.hasCondition("thin_atmosphere") || market.hasCondition("dense_atmosphere") || market.hasCondition("toxic_atmosphere") || market.hasCondition("US_storm"))
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
        if(market.hasCondition("irradiated"))
        {
            return true;
        }
        else
        {
            return false;
        }
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
        if(currentProject == null || currentProject.equals("None"))
        {
            return "None";
        }

        String tooltip = projectTooltip.get(currentProject);
        if(tooltip != null)
        {
            return tooltip;
        }
                return "Remove atmospheric radiation";
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


        // If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
        // Still bugged as of 0.95.1a
        Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);
        //Global.getSector().getCampaignUI().getCurrentInteractionDialog().dismiss();

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);

        Global.getSoundPlayer().playUISound("ui_boggled_station_constructed", 1.0F, 1.0F);

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

        //If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
        Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);
        //Global.getSector().getCampaignUI().getCurrentInteractionDialog().dismiss();

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);

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

        Global.getSector().getCampaignUI().showInteractionDialog(stationEntity);

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        Global.getSoundPlayer().playUISound("ui_boggled_station_constructed", 1.0F, 1.0F);
        return market;
    }

    public static int getLastDayCheckedForConstruction(SectorEntityToken stationEntity)
    {
        for (String tag : stationEntity.getTags()) {
            if (tag.contains("boggled_construction_progress_lastDayChecked_")) {
                return Integer.parseInt(tag.replaceAll("boggled_construction_progress_lastDayChecked_", ""));
            }
        }

        return 0;
    }

    public static void clearClockCheckTagsForConstruction(SectorEntityToken stationEntity)
    {
        String tagToDelete = null;
        for (String tag : stationEntity.getTags()) {
            if (tag.contains("boggled_construction_progress_lastDayChecked_")) {
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
            if (tag.contains("boggledTerraformingController")) {
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
            if (tag.contains("boggled_construction_progress_days_")) {
                return Integer.parseInt(tag.replaceAll("boggled_construction_progress_days_", ""));
            }
        }

        return 0;
    }

    public static void clearProgressCheckTagsForConstruction(SectorEntityToken stationEntity)
    {
        String tagToDelete = null;
        for (String tag : stationEntity.getTags()) {
            if (tag.contains("boggled_construction_progress_days_")) {
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

        stationEntity.addTag("boggled_construction_progress_days_" + strDays);
    }

    public static int[] getQuantitiesForStableLocationConstruction(String type)
    {
        if(boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled"))
        {
            if(type.equals("inactive_gate"))
            {
                return new int[]{
                        boggledTools.getIntSetting("boggledStableLocationGateCostHeavyMachinery"),
                        boggledTools.getIntSetting("boggledStableLocationGateCostMetals"),
                        boggledTools.getIntSetting("boggledStableLocationGateCostTransplutonics"),
                        boggledTools.getIntSetting("boggledStableLocationGateCostDomainEraArtifacts")};
            }
            else
            {
                return new int[]{
                        boggledTools.getIntSetting("boggledStableLocationDomainTechStructureCostHeavyMachinery"),
                        boggledTools.getIntSetting("boggledStableLocationDomainTechStructureCostMetals"),
                        boggledTools.getIntSetting("boggledStableLocationDomainTechStructureCostTransplutonics"),
                        boggledTools.getIntSetting("boggledStableLocationDomainTechStructureCostDomainEraArtifacts")};
            }
        }
        else
        {
            if(type.equals("inactive_gate"))
            {
                return new int[]{
                        boggledTools.getIntSetting("boggledStableLocationGateCostHeavyMachinery"),
                        boggledTools.getIntSetting("boggledStableLocationGateCostMetals"),
                        boggledTools.getIntSetting("boggledStableLocationGateCostTransplutonics")};
            }
            else
            {
                return new int[]{
                        boggledTools.getIntSetting("boggledStableLocationDomainTechStructureCostHeavyMachinery"),
                        boggledTools.getIntSetting("boggledStableLocationDomainTechStructureCostMetals"),
                        boggledTools.getIntSetting("boggledStableLocationDomainTechStructureCostTransplutonics")};
            }
        }
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
        if(Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            return LunaSettings.getInt("Terraforming & Station Construction", key);
        }
        else
        {
            return Global.getSettings().getInt(key);
        }
    }

    public static boolean getBooleanSetting(String key)
    {
        if(Global.getSettings().getModManager().isModEnabled("lunalib"))
        {
            return LunaSettings.getBoolean("Terraforming & Station Construction", key);
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