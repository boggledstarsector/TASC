package boggled.campaign.econ.abilities;

import boggled.campaign.econ.StringHelper;
import boggled.campaign.econ.boggledTools;
import boggled.scripts.PlayerCargoCalculations.boggledDefaultCargo;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

public class Construct_Mining_Station extends BaseDurationAbility
{
    private final float creditCost = boggledTools.getIntSetting("boggledMiningStationBuildCreditCost");
    private final float crewCost = boggledTools.getIntSetting("boggledMiningStationBuildCrewCost");
    private final float heavyMachineryCost = boggledTools.getIntSetting("boggledMiningStationBuildHeavyMachineryCost");
    private final float metalCost = boggledTools.getIntSetting("boggledMiningStationBuildMetalCost");
    private final float transplutonicsCost = boggledTools.getIntSetting("boggledMiningStationBuildTransplutonicsCost");

    public Construct_Mining_Station() { }

    @Override
    protected void activateImpl()
    {
        // Assumes the player fleet is in a valid location to build a mining station
        // Null checks, etc. are done in the isUsable() function

        CampaignClockAPI clock = Global.getSector().getClock();
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        CargoAPI playerCargo = playerFleet.getCargo();
        playerCargo.getCredits().subtract(creditCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Mining_Station, "metals", metalCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Mining_Station, "rare_metals", transplutonicsCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Mining_Station, "crew", crewCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Mining_Station, "heavy_machinery", heavyMachineryCost);

        StarSystemAPI system = playerFleet.getStarSystem();
        String miningName = StringHelper.getString(StringHelper.ABILITIES, "mining_entity_name", StringHelper.tokens("system", system.getBaseName()));
        SectorEntityToken newMiningStation = system.addCustomEntity("boggled_mining_station" + clock.getCycle() + clock.getMonth() + clock.getDay(), miningName, "boggled_mining_station_small", playerFleet.getFaction().getId());

        //Set the mining station in an orbit that keeps it within the asteroid belt or asteroid field
        if(boggledTools.playerFleetInAsteroidBelt(playerFleet))
        {
            SectorEntityToken focus = boggledTools.getFocusOfAsteroidBelt(playerFleet);
            float orbitRadius = Misc.getDistance(focus, playerFleet);
            float orbitAngle = boggledTools.getAngleFromPlayerFleet(focus);

            newMiningStation.setCircularOrbitPointingDown(focus, orbitAngle, orbitRadius, orbitRadius / 10.0F);
        }
        else if(boggledTools.playerFleetInAsteroidField(playerFleet))
        {
            OrbitAPI asteroidOrbit = boggledTools.getAsteroidFieldOrbit(playerFleet);

            if (asteroidOrbit != null)
            {
                newMiningStation.setCircularOrbitWithSpin(asteroidOrbit.getFocus(), boggledTools.getAngleFromPlayerFleet(asteroidOrbit.getFocus()), Misc.getDistance(playerFleet, asteroidOrbit.getFocus()), asteroidOrbit.getOrbitalPeriod(), 5f, 10f);
            }
            else
            {
                SectorEntityToken centerOfAsteroidField = boggledTools.getAsteroidFieldEntity(playerFleet);
                newMiningStation.setCircularOrbitWithSpin(centerOfAsteroidField, boggledTools.getAngleFromPlayerFleet(centerOfAsteroidField), Misc.getDistance(playerFleet, centerOfAsteroidField), 40f, 5f, 10f);
            }
        }

        SectorEntityToken newMiningStationLights = system.addCustomEntity("boggled_miningStationLights", "Mining Station Lights Overlay", "boggled_mining_station_small_lights_overlay", playerFleet.getFaction().getId());
        newMiningStationLights.setOrbit(newMiningStation.getOrbit().makeCopy());

        MarketAPI market = boggledTools.createMiningStationMarket(newMiningStation);
        CargoAPI newMarketStorage = market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();

        //Delete abandoned mining stations and transfer their cargo to the newly created one
        ArrayList<SectorEntityToken> stationsToDelete = new ArrayList<>();

        // Lights overlay will have already been deleted by the Sprite_Controller condition
        for(SectorEntityToken entityInSystem : playerFleet.getStarSystem().getAllEntities())
        {
            if(entityInSystem.hasTag(boggledTools.BoggledTags.miningStation) && entityInSystem.getFaction().getId().equals(Factions.NEUTRAL))
            {
                stationsToDelete.add(entityInSystem);
            }
        }

        // Put the storage cargo and ships from the abandoned station into the new market so the player doesn't lose them
        for(SectorEntityToken station : stationsToDelete)
        {
            CargoAPI cargoFromDeletedStation = station.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();

            if(!cargoFromDeletedStation.isEmpty())
            {
                // Put the deleted station's cargo into the new station market if it was created
                newMarketStorage.addAll(cargoFromDeletedStation);
            }

            system.removeEntity(station);
        }
    }

    @Override
    public boolean isUsable()
    {
        // Check if required research is completed (data-driven system)
        String requiredResearch = boggledTools.getRequiredResearchForAbility("boggled_construct_mining_station");
        if (requiredResearch != null && !boggledTools.isResearched(requiredResearch))
        {
            return false;
        }

        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = playerFleet.getStarSystem();

        if(system == null || playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            return false;
        }

        int miningStationCap = boggledTools.getIntSetting("boggledMaxNumMiningStationsPerSystem");

        if(!boggledTools.playerFleetInAsteroidBelt(playerFleet) && !boggledTools.playerFleetInAsteroidField(playerFleet))
        {
            return false;
        }

        if(boggledTools.playerFleetTooCloseToJumpPoint(playerFleet))
        {
            return false;
        }

        if(system.getJumpPoints().isEmpty())
        {
            return false;
        }

        if(system.hasTag(Tags.SYSTEM_ABYSSAL))
        {
            return false;
        }

        if(miningStationCap == 0)
        {
            return false;
        }

        // Player can't build any more mining stations if they already constructed more than the limit.
        // They will need to allow the ones they already built to become decivilized before building more.
        int miningStationsInSystem = 0;
        for(SectorEntityToken token : system.getAllEntities())
        {
            // Neutral stations are decivilized
            if(token.hasTag("boggled_mining") && !token.getFaction().getId().equals(Factions.NEUTRAL))
            {
                miningStationsInSystem++;
            }
        }

        boolean miningStationCapReached = miningStationsInSystem > miningStationCap;

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Mining_Station, "metals") < metalCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Mining_Station, "rare_metals") < transplutonicsCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Mining_Station, "crew") < crewCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Mining_Station, "heavy_machinery") < heavyMachineryCost)
        {
            return false;
        }

        if(miningStationCapReached)
        {
            return false;
        }

        return super.isUsable();
    }

    @Override
    public boolean hasTooltip()
    {
        return true;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded)
    {
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        tooltip.addTitle(StringHelper.getString(StringHelper.ABILITIES, "mining_title"));
        float pad = 10.0F;
        String credits = (int) creditCost + "";
        String crew = (int) crewCost + "";
        String heavyMachinery = (int) heavyMachineryCost + "";
        String metals = (int) metalCost + "";
        String transplutonics = (int) transplutonicsCost + "";
        String costSentence = StringHelper.getString(StringHelper.ABILITIES, "mining_cost", StringHelper.tokens(
                "credits", credits, "crew", crew, "heavyMachinery", heavyMachinery, "metals", metals, "transplutonics", transplutonics));
        StringHelper.applyHighlight(tooltip.addPara(costSentence, pad), highlight, credits, crew, heavyMachinery, metals, transplutonics);

        // Check research requirement for tooltip
        String requiredResearch = boggledTools.getRequiredResearchForAbility("boggled_construct_mining_station");
        if (requiredResearch != null && !boggledTools.isResearched(requiredResearch))
        {
            String researchName = boggledTools.getResearchDisplayName(requiredResearch);
            tooltip.addPara(StringHelper.getString(StringHelper.GENERAL, "requires_research", StringHelper.tokens("research", researchName)), bad, pad);
        }

        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = playerFleet.getStarSystem();

        // Print the resources the mining station would have.
        if(boggledTools.getBooleanSetting("boggledMiningStationLinkToResourceBelts"))
        {
            // Dynamic resources based on number of asteroid terrains in the system.
            if (!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && system != null)
            {
                Integer numAsteroidBeltsInSystem = boggledTools.getNumAsteroidTerrainsInSystem(playerFleet);
                String count = numAsteroidBeltsInSystem + "";
                String resources = miningResourceDisplay(boggledTools.getMiningStationResourceString(numAsteroidBeltsInSystem));
                String beltSentence = StringHelper.getString(StringHelper.ABILITIES, "mining_belt_resources", StringHelper.tokens(
                        "count", count, "system", playerFleet.getStarSystem().getName(), "resources", resources));
                StringHelper.applyHighlight(tooltip.addPara(beltSentence, pad), highlight, count, resources);
            }
        }
        else
        {
            // Static resources. Defaults to moderate. The switch values are condition ids.
            String resourceLevel = "moderate";
            int staticAmountPerSettings = boggledTools.getIntSetting("boggledMiningStationStaticAmount");
            resourceLevel = switch (staticAmountPerSettings) {
                case 1 -> "sparse";
                case 2 -> "moderate";
                case 3 -> "abundant";
                case 4 -> "rich";
                case 5 -> "ultrarich";
                default -> resourceLevel;
            };
            String resourceDisplay = miningResourceDisplay(resourceLevel);
            String staticSentence = StringHelper.getString(StringHelper.ABILITIES, "mining_static_resources", StringHelper.tokens("level", resourceDisplay));
            StringHelper.applyHighlight(tooltip.addPara(staticSentence, pad), highlight, resourceDisplay);
        }

        boolean playerFleetInAsteroidBelt = false;
        if(!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && system != null)
        {
            if (boggledTools.playerFleetInAsteroidBelt(playerFleet) || boggledTools.playerFleetInAsteroidField(playerFleet))
            {
                playerFleetInAsteroidBelt = true;
            }
        }

        if(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition() || system == null)
        {
            tooltip.addPara(StringHelper.getString(StringHelper.ABILITIES, "mining_hyperspace"), bad, pad);
        }
        else if(system.hasTag(Tags.SYSTEM_ABYSSAL))
        {
            tooltip.addPara(StringHelper.getString("cannot_construct_abyssal"), bad, pad);
        }
        else if(boggledTools.playerFleetTooCloseToJumpPoint(playerFleet))
        {
            tooltip.addPara(StringHelper.getString(StringHelper.ABILITIES, "mining_too_close_to_jump_point"), bad, pad);
        }
        else if(system.getJumpPoints().isEmpty())
        {
            tooltip.addPara(StringHelper.getString("cannot_construct_no_jump_points"), bad, pad);
        }

        int miningStationCap = boggledTools.getIntSetting("boggledMaxNumMiningStationsPerSystem");
        int miningStationsInSystem = 0;
        if(!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && system != null)
        {
            // Player can't build any more mining stations if they already constructed more than the limit.
            // They will need to allow the ones they already built to become decivilized before building more.
            for(SectorEntityToken token : system.getAllEntities())
            {
                // Neutral stations are decivilized
                if(token.hasTag("boggled_mining") && !token.getFaction().getId().equals(Factions.NEUTRAL))
                {
                    miningStationsInSystem++;
                }
            }
        }

        boolean miningStationCapReached = miningStationsInSystem > miningStationCap;

        if(!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && system != null)
        {
            if(!playerFleetInAsteroidBelt)
            {
                tooltip.addPara(StringHelper.getString(StringHelper.ABILITIES, "mining_too_far"), bad, pad);
            }

            if(miningStationCap == 0)
            {
                tooltip.addPara(StringHelper.getString(StringHelper.ABILITIES, "mining_disabled"), bad, pad);
            }
            else if (miningStationCapReached && miningStationCap == 1)
            {
                tooltip.addPara(StringHelper.getString(StringHelper.ABILITIES, "mining_cap_one"), bad, pad);
            }
            else if(miningStationCapReached && miningStationCap > 1)
            {
                tooltip.addPara(StringHelper.getString(StringHelper.ABILITIES, "mining_cap_many", StringHelper.tokens("cap", miningStationCap + "")), bad, pad);
            }
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            tooltip.addPara(StringHelper.getString("insufficient_credits"), bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Mining_Station, "metals") < metalCost)
        {
            tooltip.addPara(StringHelper.getString("insufficient_metals"), bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Mining_Station, "rare_metals") < transplutonicsCost)
        {
            tooltip.addPara(StringHelper.getString("insufficient_transplutonics"), bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Mining_Station, "crew") < crewCost)
        {
            tooltip.addPara(StringHelper.getString("insufficient_crew"), bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Mining_Station, "heavy_machinery") < heavyMachineryCost)
        {
            tooltip.addPara(StringHelper.getString("insufficient_heavy_machinery"), bad, pad);
        }
    }

    private static String miningResourceDisplay(String resourceLevelId) {
        if (resourceLevelId == null) {
            return "";
        }
        return StringHelper.getString(StringHelper.ABILITIES, "mining_resource_" + resourceLevelId);
    }

    @Override
    public boolean isTooltipExpandable() { return false; }

    @Override
    protected void applyEffect(float v, float v1) { }

    @Override
    protected void deactivateImpl() { }

    @Override
    protected void cleanupImpl() { }
}