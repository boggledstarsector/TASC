package boggled.campaign.econ.abilities;

import boggled.campaign.econ.boggledTools;
import boggled.scripts.PlayerCargoCalculations.boggledDefaultCargo;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public class Construct_Siphon_Station extends BaseDurationAbility
{
    private final float creditCost = boggledTools.getIntSetting("boggledSiphonStationBuildCreditCost");
    private final float crewCost = boggledTools.getIntSetting("boggledSiphonStationBuildCrewCost");
    private final float heavyMachineryCost = boggledTools.getIntSetting("boggledSiphonStationBuildHeavyMachineryCost");
    private final float metalCost = boggledTools.getIntSetting("boggledSiphonStationBuildMetalCost");
    private final float transplutonicsCost = boggledTools.getIntSetting("boggledSiphonStationBuildTransplutonicsCost");

    public Construct_Siphon_Station() { }

    @Override
    protected void activateImpl()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = playerFleet.getStarSystem();
        SectorEntityToken hostGasGiant = boggledTools.getClosestGasGiantToken(playerFleet);

        CargoAPI playerCargo = playerFleet.getCargo();
        playerCargo.getCredits().subtract(creditCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Mining_Station, "metals", metalCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Mining_Station, "rare_metals", metalCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Mining_Station, "crew", metalCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Mining_Station, "heavy_machinery", metalCost);

        SectorEntityToken newSiphonStation = system.addCustomEntity("boggled_siphon_station", hostGasGiant.getName() + " Siphon Station", "boggled_siphon_station_small", Global.getSector().getPlayerFleet().getFaction().getId());
        newSiphonStation.setCircularOrbitPointingDown(hostGasGiant, boggledTools.getAngleFromPlayerFleet(hostGasGiant), hostGasGiant.getRadius() + 50f, (hostGasGiant.getRadius() + 50f) / 10.0F);

        SectorEntityToken newSiphonStationLights = system.addCustomEntity("boggled_siphonStationLights", "Siphon Station Lights Overlay", "boggled_siphon_station_small_lights_overlay", Global.getSector().getPlayerFleet().getFaction().getId());
        newSiphonStationLights.setOrbit(newSiphonStation.getOrbit().makeCopy());

        boggledTools.createSiphonStationMarket(newSiphonStation, hostGasGiant);
    }

    @Override
    public boolean isUsable()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = playerFleet.getStarSystem();

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition() || system == null)
        {
            return false;
        }

        SectorEntityToken closestGasGiantToken = boggledTools.getClosestGasGiantToken(playerFleet);

        if(closestGasGiantToken == null)
        {
            return false;
        }
        // Can only build around neutral and player-owned gas giants. Checking the faction ID == player will exclude Nex player-governed planets.
        else if(!closestGasGiantToken.getMarket().getFactionId().equals(Factions.PLAYER) && !closestGasGiantToken.getMarket().getFactionId().equals(Factions.NEUTRAL))
        {
            return false;
        }
        else if((Misc.getDistance(closestGasGiantToken, playerFleet) - closestGasGiantToken.getRadius()) > 250f)
        {
            return false;
        }


        if(system.getJumpPoints().isEmpty())
        {
            return false;
        }

        for(SectorEntityToken token : system.getAllEntities())
        {
            if(token.hasTag(boggledTools.BoggledTags.siphonStation) && token.getOrbitFocus() != null && token.getOrbitFocus().equals(closestGasGiantToken))
            {
                return false;
            }
        }

        // Can't build a siphon station if there's a moon orbiting very close to the gas giant.
        if(getMoonBlockingSiphonStation(closestGasGiantToken) != null)
        {
            return false;
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Siphon_Station, "metals") < metalCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Siphon_Station, "rare_metals") < transplutonicsCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Siphon_Station, "crew") < crewCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Siphon_Station, "heavy_machinery") < heavyMachineryCost)
        {
            return false;
        }

        return super.isUsable();
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
        StarSystemAPI system = playerFleet.getStarSystem();

        if(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition() || system == null)
        {
            tooltip.addPara("Siphon stations cannot be constructed in hyperspace.", bad, pad);
        }
        else if(system.getJumpPoints().isEmpty())
        {
            tooltip.addPara("You cannot construct a station in a system with no jump points.", bad, pad);
        }

        if(!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && system != null)
        {
            // Inform the player whether they're close enough to a valid gas giant
            SectorEntityToken closestGasGiantToken = boggledTools.getClosestGasGiantToken(playerFleet);
            if(closestGasGiantToken == null)
            {
                tooltip.addPara("There are no gas giants in the " + system.getName(), bad, pad);
            }
            else if(!closestGasGiantToken.getMarket().getFactionId().equals(Factions.PLAYER) && !closestGasGiantToken.getMarket().getFactionId().equals(Factions.NEUTRAL))
            {
                tooltip.addPara("The gas giant closest to your fleet is " + closestGasGiantToken.getName() + " which is controlled by " + closestGasGiantToken.getMarket().getFaction().getDisplayName() + ". You cannot construct a siphon station in orbit around a gas giant controlled by another faction.", bad, pad);
            }
            else
            {
                if((Misc.getDistance(closestGasGiantToken, playerFleet) - closestGasGiantToken.getRadius()) > 250f)
                {
                    float distanceInSu = (Misc.getDistance(playerFleet, closestGasGiantToken) - closestGasGiantToken.getRadius()) / 2000f;
                    String distanceInSuString = String.format("%.2f", distanceInSu);
                    float requiredDistanceInSu = 250f / 2000f;
                    String requiredDistanceInSuString = String.format("%.2f", requiredDistanceInSu);
                    tooltip.addPara("The gas giant closest to your location is " + closestGasGiantToken.getName() + ". Your fleet is " + distanceInSuString + " stellar units away. You must be within " + requiredDistanceInSuString + " stellar units to construct a siphon station.", bad, pad);
                }

                if(gasGiantAlreadyHasSiphonStationInOrbit(closestGasGiantToken))
                {
                    tooltip.addPara("Each gas giant can only support a single siphon station.", bad, pad);
                }

                PlanetAPI blockingMoon = getMoonBlockingSiphonStation(closestGasGiantToken);
                if(blockingMoon != null)
                {
                    tooltip.addPara("A siphon station would be unable to achieve a satisfactory orbit around " + closestGasGiantToken.getName() + " because " + blockingMoon.getName() + " is too close to the projected orbital path.", bad, pad);
                }
            }
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            tooltip.addPara("Insufficient credits.", bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Siphon_Station, "metals") < metalCost)
        {
            tooltip.addPara("Insufficient metals.", bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Siphon_Station, "rare_metals") < transplutonicsCost)
        {
            tooltip.addPara("Insufficient transplutonics.", bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Siphon_Station, "crew") < crewCost)
        {
            tooltip.addPara("Insufficient crew.", bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Siphon_Station, "heavy_machinery") < heavyMachineryCost)
        {
            tooltip.addPara("Insufficient heavy machinery.", bad, pad);
        }
    }

    public PlanetAPI getMoonBlockingSiphonStation(SectorEntityToken closestGasGiantToken)
    {
        StarSystemAPI system = closestGasGiantToken.getStarSystem();

        // Can't build a siphon station if there's a moon orbiting very close to the gas giant.
        PlanetAPI returnPlanet = null;
        for(PlanetAPI planet : system.getPlanets())
        {
            if(planet.getOrbitFocus() != null && !planet.isStar() && planet.getOrbitFocus().equals(closestGasGiantToken) && planet.getCircularOrbitRadius() < (closestGasGiantToken.getRadius() + 250f))
            {
                if(returnPlanet == null || planet.getName().compareTo(returnPlanet.getName()) <= 0) { returnPlanet = planet;}
            }
        }

        return returnPlanet;
    }

    public boolean gasGiantAlreadyHasSiphonStationInOrbit(SectorEntityToken closestGasGiantToken)
    {
        for(SectorEntityToken token : closestGasGiantToken.getStarSystem().getAllEntities())
        {
            if(token.hasTag(boggledTools.BoggledTags.siphonStation) && token.getOrbitFocus() != null && token.getOrbitFocus().equals(closestGasGiantToken))
            {
                return true;
            }
        }

        return false;
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