package data.campaign.econ.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidBeltTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import com.fs.starfarer.combat.entities.terrain.Planet;
import data.campaign.econ.boggledTools;
import data.scripts.BoggledUnderConstructionEveryFrameScript;

public class Construct_Siphon_Station extends BaseDurationAbility
{
    private float creditCost = boggledTools.getIntSetting("boggledSiphonStationBuildCreditCost");
    private float crewCost = boggledTools.getIntSetting("boggledSiphonStationBuildCrewCost");
    private float heavyMachineryCost = boggledTools.getIntSetting("boggledSiphonStationBuildHeavyMachineryCost");
    private float metalCost = boggledTools.getIntSetting("boggledSiphonStationBuildMetalCost");
    private float transplutonicsCost = boggledTools.getIntSetting("boggledSiphonStationBuildTransplutonicsCost");

    public Construct_Siphon_Station() { }

    @Override
    protected void activateImpl()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = playerFleet.getStarSystem();
        SectorEntityToken hostGasGiant = boggledTools.getClosestGasGiantToken(playerFleet);

        CargoAPI playerCargo = playerFleet.getCargo();
        playerCargo.getCredits().subtract(creditCost);
        playerCargo.removeCommodity("metals", metalCost);
        playerCargo.removeCommodity("rare_metals", transplutonicsCost);
        playerCargo.removeCommodity("crew", crewCost);
        playerCargo.removeCommodity("heavy_machinery", heavyMachineryCost);

        SectorEntityToken newSiphonStation = system.addCustomEntity("boggled_siphon_station", hostGasGiant.getName() + " Siphon Station", "boggled_siphon_station_small", Global.getSector().getPlayerFleet().getFaction().getId());
        newSiphonStation.setCircularOrbitPointingDown(hostGasGiant, boggledTools.getAngleFromPlayerFleet(hostGasGiant)+ 5f, hostGasGiant.getRadius() + 50f, (hostGasGiant.getRadius() + 50f) / 10.0F);

        SectorEntityToken newSiphonStationLights = system.addCustomEntity("boggled_siphonStationLights", "Siphon Station Lights Overlay", "boggled_siphon_station_small_lights_overlay", Global.getSector().getPlayerFleet().getFaction().getId());
        newSiphonStationLights.setOrbit(newSiphonStation.getOrbit().makeCopy());

        MarketAPI market = null;
        if(!boggledTools.getBooleanSetting("boggledStationConstructionDelayEnabled"))
        {
            market = boggledTools.createSiphonStationMarket(newSiphonStation, hostGasGiant);
        }
        else
        {
            newSiphonStation.addScript(new BoggledUnderConstructionEveryFrameScript(newSiphonStation));
            Global.getSoundPlayer().playUISound("ui_boggled_station_start_building", 1.0F, 1.0F);
        }
    }

    @Override
    public boolean isUsable()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            return false;
        }

        if(!(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition()))
        {
            SectorEntityToken closestGasGiantToken = null;
            closestGasGiantToken = boggledTools.getClosestGasGiantToken(playerFleet);

            if(closestGasGiantToken == null)
            {
                return false;
            }
            else if(!closestGasGiantToken.getMarket().getFactionId().equals("player") && !closestGasGiantToken.getMarket().getFactionId().equals("neutral"))
            {
                return false;
            }
            else if((boggledTools.getDistanceBetweenTokens(closestGasGiantToken, playerFleet) - closestGasGiantToken.getRadius()) > 250f)
            {
                return false;
            }
        }

        if(!boggledTools.systemHasJumpPoint(playerFleet.getStarSystem()))
        {
            return false;
        }

        if(!(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition()))
        {
            SectorEntityToken closestGasGiantToken = null;
            closestGasGiantToken = boggledTools.getClosestGasGiantToken(playerFleet);

            if(closestGasGiantToken != null)
            {
                Iterator allEntitiesInSystem = Global.getSector().getPlayerFleet().getStarSystem().getAllEntities().iterator();
                while(allEntitiesInSystem.hasNext())
                {
                    SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
                    if(entity.hasTag("station") && entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(closestGasGiantToken) && (entity.getCustomEntitySpec().getDefaultName().equals("Side Station") || entity.getCustomEntitySpec().getDefaultName().equals("Siphon Station")) && !entity.getId().equals("beholder_station"))
                    {
                        return false;
                    }
                }
            }
        }

        //check if the host gas giant has a moon that is too close to it
        if(!(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition()))
        {
            SectorEntityToken closestGasGiantToken = null;
            closestGasGiantToken = boggledTools.getClosestGasGiantToken(playerFleet);

            Iterator allPlanetsInSystem = playerFleet.getStarSystem().getPlanets().iterator();
            while(allPlanetsInSystem.hasNext())
            {
                PlanetAPI planet = (PlanetAPI) allPlanetsInSystem.next();
                if (planet.getOrbitFocus() != null && !planet.isStar() && planet.getOrbitFocus().equals(closestGasGiantToken) && planet.getCircularOrbitRadius() < (closestGasGiantToken.getRadius() + 250f))
                {
                    return false;
                }
            }
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            return false;
        }

        if(playerCargo.getCommodityQuantity("metals") < metalCost)
        {
            return false;
        }

        if(playerCargo.getCommodityQuantity("rare_metals") < transplutonicsCost)
        {
            return false;
        }

        if(playerCargo.getCommodityQuantity("crew") < crewCost)
        {
            return false;
        }

        if(playerCargo.getCommodityQuantity("heavy_machinery") < heavyMachineryCost)
        {
            return false;
        }

        if(this.isOnCooldown() || this.disableFrames > 0)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded)
    {
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        LabelAPI title = tooltip.addTitle("Construct Siphon Station");
        float pad = 10.0F;
        tooltip.addPara("Construct a siphon station in low orbit around a gas giant. Expends %s credits, %s crew, %s heavy machinery, %s metals and %s transplutonics for construction.", pad, highlight, new String[]{(int)creditCost + "",(int)crewCost + "",(int)heavyMachineryCost +"", (int)metalCost + "", (int)transplutonicsCost +""});

        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        if(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            tooltip.addPara("Siphon stations cannot be constructed in hyperspace.", bad, pad);
        }
        else if(!boggledTools.systemHasJumpPoint(playerFleet.getStarSystem()))
        {
            tooltip.addPara("You cannot construct a station in a system with no jump points.", bad, pad);
        }

        if(!(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition()))
        {
            SectorEntityToken closestGasGiantToken = null;
            closestGasGiantToken = boggledTools.getClosestGasGiantToken(playerFleet);

            if(closestGasGiantToken == null)
            {
                tooltip.addPara("There are no gas giants in this system.", bad, pad);
            }
            else if(!closestGasGiantToken.getMarket().getFactionId().equals("player") && !closestGasGiantToken.getMarket().getFactionId().equals("neutral"))
            {
                tooltip.addPara("The gas giant closest to your location is " + closestGasGiantToken.getName() + " which is controlled by " + closestGasGiantToken.getMarket().getFaction().getDisplayName() + ". You cannot construct a siphon station in orbit around a gas giant controlled by another faction.", bad, pad);
            }
            else if((boggledTools.getDistanceBetweenTokens(closestGasGiantToken, playerFleet) - closestGasGiantToken.getRadius()) > 250f)
            {
                float distanceInSu = (boggledTools.getDistanceBetweenTokens(playerFleet, closestGasGiantToken) - closestGasGiantToken.getRadius()) / 2000f;
                String distanceInSuString = String.format("%.2f", distanceInSu);
                float requiredDistanceInSu = 250f / 2000f;
                String requiredDistanceInSuString = String.format("%.2f", requiredDistanceInSu);
                tooltip.addPara("The gas giant closest to your location is " + closestGasGiantToken.getName() + ". Your fleet is " + distanceInSuString + " stellar units away. You must be within " + requiredDistanceInSuString + " stellar units to construct a siphon station.", bad, pad);
            }
        }

        if(!(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition()))
        {
            SectorEntityToken closestGasGiantToken = null;
            closestGasGiantToken = boggledTools.getClosestGasGiantToken(playerFleet);

            if(closestGasGiantToken != null)
            {
                Iterator allEntitiesInSystem = Global.getSector().getPlayerFleet().getStarSystem().getAllEntities().iterator();
                while(allEntitiesInSystem.hasNext())
                {
                    SectorEntityToken entity = (SectorEntityToken)allEntitiesInSystem.next();
                    if(entity.hasTag("station") && entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(closestGasGiantToken) && (entity.getCustomEntitySpec().getDefaultName().equals("Side Station") || entity.getCustomEntitySpec().getDefaultName().equals("Siphon Station")) && !entity.getId().equals("beholder_station") && !entity.getId().contains("armaa_"))
                    {
                        tooltip.addPara("Each gas giant can only support a single siphon station.", bad, pad);
                    }
                }
            }
        }

        //check if the host gas giant has a moon that is too close to it
        if(!(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition()))
        {
            SectorEntityToken closestGasGiantToken = null;
            closestGasGiantToken = boggledTools.getClosestGasGiantToken(playerFleet);

            Iterator allPlanetsInSystem = playerFleet.getStarSystem().getPlanets().iterator();
            while(allPlanetsInSystem.hasNext())
            {
                PlanetAPI planet = (PlanetAPI) allPlanetsInSystem.next();
                if (planet.getOrbitFocus() != null && !planet.isStar() && planet.getOrbitFocus().equals(closestGasGiantToken) && planet.getCircularOrbitRadius() < (closestGasGiantToken.getRadius() + 250f))
                {
                    tooltip.addPara("A siphon station would be unable to achieve a satisfactory orbit around " + closestGasGiantToken.getName() + " because " + planet.getName() + " is too close to the projected orbital path.", bad, pad);
                    break;
                }
            }
        }

        //Check if the host gas giant has four or more moons
        if(!(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition()))
        {
            int numMoons = 0;
            SectorEntityToken closestGasGiantToken = null;
            closestGasGiantToken = boggledTools.getClosestGasGiantToken(playerFleet);

            Iterator allPlanetsInSystem = playerFleet.getStarSystem().getPlanets().iterator();
            while(allPlanetsInSystem.hasNext())
            {
                PlanetAPI planet = (PlanetAPI) allPlanetsInSystem.next();
                if (planet.getOrbitFocus() != null && !planet.isStar() && planet.getOrbitFocus().equals(closestGasGiantToken) && planet.getRadius() != 0)
                {
                    numMoons++;
                }
            }

            if(numMoons >= 4)
            {
                tooltip.addPara("A siphon station would be unable to maintain a satisfactory orbit around " + closestGasGiantToken.getName() + " because there are four or more moons orbiting it. The siphon station would be unable to maintain a stable orbit due to the gravitational fluctuations caused by the moons.", bad, pad);
            }
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            tooltip.addPara("Insufficient credits.", bad, pad);
        }

        if(playerCargo.getCommodityQuantity("crew") < crewCost)
        {
            tooltip.addPara("Insufficient crew.", bad, pad);
        }

        if(playerCargo.getCommodityQuantity("heavy_machinery") < heavyMachineryCost)
        {
            tooltip.addPara("Insufficient heavy machinery.", bad, pad);
        }

        if(playerCargo.getCommodityQuantity("metals") < metalCost)
        {
            tooltip.addPara("Insufficient metals.", bad, pad);
        }

        if(playerCargo.getCommodityQuantity("rare_metals") < transplutonicsCost)
        {
            tooltip.addPara("Insufficient transplutonics.", bad, pad);
        }
    }

    @Override
    public boolean isTooltipExpandable() {
        return false;
    }

    @Override
    protected void applyEffect(float v, float v1) { }

    @Override
    protected void deactivateImpl() { }

    @Override
    protected void cleanupImpl() { }
}