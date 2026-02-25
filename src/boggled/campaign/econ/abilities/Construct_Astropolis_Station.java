package boggled.campaign.econ.abilities;

import boggled.campaign.econ.boggledTools;
import boggled.scripts.PlayerCargoCalculations.boggledDefaultCargo;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;

public class Construct_Astropolis_Station extends BaseDurationAbility
{
    private final float creditCost = boggledTools.getIntSetting("boggledAstropolisStationBuildCreditCost");
    private final float crewCost = boggledTools.getIntSetting("boggledAstropolisStationBuildCrewCost");
    private final float heavyMachineryCost = boggledTools.getIntSetting("boggledAstropolisStationBuildHeavyMachineryCost");
    private final float metalCost = boggledTools.getIntSetting("boggledAstropolisStationBuildMetalCost");
    private final float transplutonicsCost = boggledTools.getIntSetting("boggledAstropolisStationBuildTransplutonicsCost");

    public Construct_Astropolis_Station() { }

    @Override
    protected void activateImpl()
    {
        // Validations are handled in isUsable(). This function assumes we're in a valid situation to create an astropolis.
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = playerFleet.getStarSystem();
        FactionAPI playerFaction = Global.getSector().getPlayerFaction();
        String playerFactionID = playerFaction.getId();

        CargoAPI playerCargo = playerFleet.getCargo();
        playerCargo.getCredits().subtract(creditCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Astropolis_Station, "metals", metalCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Astropolis_Station, "rare_metals", transplutonicsCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Astropolis_Station, "crew", crewCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Astropolis_Station, "heavy_machinery", heavyMachineryCost);

        SectorEntityToken targetPlanet = boggledTools.getClosestPlanetToken(playerFleet);
        int numAstro = boggledTools.numAstroInOrbit(targetPlanet);
        float orbitRadius = targetPlanet.getRadius() + 375.0F;

        SectorEntityToken newAstropolis = system.addCustomEntity("boggled_astropolis_" + numAstro, targetPlanet.getName() + " Astropolis " + boggledTools.getAstropolisColonyNameStringGreekLetter(numAstro), "boggled_astropolis_station_" + boggledTools.getGreekLetterForNextAstropolisCustomEntityId(numAstro) + "_small", playerFactionID);
        SectorEntityToken newAstropolisLights = system.addCustomEntity("boggled_astropolisLights", targetPlanet.getName() + " Astropolis " + boggledTools.getAstropolisColonyNameStringGreekLetter(numAstro) + " Lights Overlay", "boggled_astropolis_station_" + boggledTools.getGreekLetterForNextAstropolisCustomEntityId(numAstro) + "_small_lights_overlay", playerFactionID);

        // Figure out the orbital angle for the new astropolis.
        ArrayList<SectorEntityToken> existingAstropolisStations = boggledTools.getExistingAstropolisStations(targetPlanet);
        ArrayList<Float> existingOrbitAngles = new ArrayList<>();
        for(SectorEntityToken token : existingAstropolisStations)
        {
            existingOrbitAngles.add(token.getCircularOrbitAngle());
        }
        float newAngle = boggledTools.generateNewAngleForAstropolisStation(existingOrbitAngles);

        // Set the orbit using the angle generated above and create the lights overlay.
        newAstropolis.setCircularOrbitPointingDown(targetPlanet, newAngle, orbitRadius, orbitRadius / 10.0F);
        newAstropolisLights.setOrbit(newAstropolis.getOrbit().makeCopy());

        boggledTools.createAstropolisStationMarket(newAstropolis, targetPlanet);
    }

    static final class AstropolisOrbitBlocker
    {
        public SectorEntityToken blocker;
        public String reason;

        public AstropolisOrbitBlocker(SectorEntityToken blocker, String reason)
        {
            this.blocker = blocker;
            this.reason = reason;
        }
    }

    private AstropolisOrbitBlocker astropolisOrbitBlocked(SectorEntityToken targetPlanet)
    {
        //check if the host market radius is too small
        if(targetPlanet.getRadius() < 125f)
        {
            return new AstropolisOrbitBlocker(null, "radius_too_small");
        }

        //check if the host market is too close to its orbital focus
        if(targetPlanet.getOrbitFocus() != null && targetPlanet.getCircularOrbitRadius() < (targetPlanet.getOrbitFocus().getRadius() + 900f))
        {
            return new AstropolisOrbitBlocker(targetPlanet.getOrbitFocus(), "too_close_to_focus");
        }

        //check if the host market is too close to the star it's orbiting
        if(targetPlanet.getOrbitFocus() != null && targetPlanet.getOrbitFocus().isStar() && targetPlanet.getCircularOrbitRadius() < (targetPlanet.getOrbitFocus().getRadius() + 1400f))
        {
            return new AstropolisOrbitBlocker(targetPlanet.getOrbitFocus(), "too_close_to_star");
        }

        //check if the host market has a moon that is too close to it
        // Sort by distance in case there's more than one moon so the tooltip is consistent between executions of this code.
        ArrayList<SectorEntityToken> moonsTooClose = new ArrayList<>();
        for(SectorEntityToken token : targetPlanet.getStarSystem().getPlanets())
        {
            if (token.getOrbitFocus() != null && !token.isStar() && token.getOrbitFocus().equals(targetPlanet) && token.getCircularOrbitRadius() < (targetPlanet.getRadius() + 500f) && token.getRadius() != 0)
            {
                moonsTooClose.add(token);
            }
        }
        if(!moonsTooClose.isEmpty())
        {
            moonsTooClose.sort((m1, m2) -> Float.compare(m1.getCircularOrbitRadius(), m2.getCircularOrbitRadius()));
            return new AstropolisOrbitBlocker(moonsTooClose.get(0), "moon_too_close");
        }

        //Check if the host market has four moons - need to block building here because it creates a visual bug where the astropolis
        //appears on top of one of the other four moons in the system view
        if(boggledTools.getNumMoonsInOrbitAroundPlanet(targetPlanet) >= 4)
        {
            return new AstropolisOrbitBlocker(null, "too_many_moons");
        }

        return null;
    }

    @Override
    public boolean isUsable()
    {
        // Check if required research is completed (data-driven system)
        String requiredResearch = boggledTools.getRequiredResearchForAbility("boggled_construct_astropolis_station");
        if (requiredResearch != null && !boggledTools.isResearched(requiredResearch))
        {
            return false;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = playerFleet.getStarSystem();
        SectorEntityToken targetPlanet = boggledTools.getClosestPlanetToken(playerFleet);

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition() || system == null)
        {
            return false;
        }

        if(system.getJumpPoints().isEmpty())
        {
            return false;
        }

        if(targetPlanet == null)
        {
            return false;
        }

        // Checks for governorship by player. Governed planets have isPlayerOwned() equals true but
        // the controlling FactionAPI is still major faction, not player
        if(!(targetPlanet.getMarket().isPlayerOwned() && boggledTools.getBooleanSetting("boggledCanBuildAstropolisOnPurchasedGovernorshipPlanets")) && !targetPlanet.getMarket().getFactionId().equals(Factions.PLAYER))
        {
            return false;
        }

        if((Misc.getDistance(targetPlanet, playerFleet) - targetPlanet.getRadius()) > 500f)
        {
            return false;
        }

        AstropolisOrbitBlocker block = astropolisOrbitBlocked(targetPlanet);
        if(block != null && !boggledTools.getBooleanSetting("boggledAstropolisIgnoreOrbitalRequirements"))
        {
            return false;
        }

        int numAstroInOrbit = boggledTools.getNumExistingAstroplisStations(targetPlanet);
        if(numAstroInOrbit >= 3)
        {
            return false;
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Astropolis_Station, "metals") < metalCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Astropolis_Station, "rare_metals") < transplutonicsCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Astropolis_Station, "crew") < crewCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Astropolis_Station, "heavy_machinery") < heavyMachineryCost)
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
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = playerFleet.getStarSystem();
        SectorEntityToken targetPlanet = boggledTools.getClosestPlanetToken(playerFleet);
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        tooltip.addTitle("Construct Astropolis Station");
        float pad = 10.0F;
        tooltip.addPara("Construct an astropolis station in orbit around a planet. Expends %s credits, %s crew, %s heavy machinery, %s metals and %s transplutonics for construction.", pad, highlight, new String[]{(int)creditCost + "",(int)crewCost + "",(int)heavyMachineryCost +"", (int)metalCost + "", (int)transplutonicsCost +""});

        // Check research requirement for tooltip
        String requiredResearch = boggledTools.getRequiredResearchForAbility("boggled_construct_astropolis_station");
        if (requiredResearch != null && !boggledTools.isResearched(requiredResearch))
        {
            String researchName = boggledTools.getResearchDisplayName(requiredResearch);
            tooltip.addPara("Requires the " + researchName + " research to be completed.", bad, pad);
        }

        if(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition() || system == null)
        {
            tooltip.addPara("You cannot construct an astropolis station in hyperspace.", bad, pad);
        }
        else if(system.getJumpPoints().isEmpty())
        {
            tooltip.addPara("You cannot construct a station in a system with no jump points.", bad, pad);
        }
        else if(targetPlanet == null)
        {
            tooltip.addPara("There are no planets in the " + system.getName() + ".", bad, pad);
        }

        if(!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && system != null && targetPlanet != null)
        {
            if((Misc.getDistance(targetPlanet, playerFleet) - targetPlanet.getRadius()) > 500f)
            {
                float distanceInSu = (Misc.getDistance(targetPlanet, playerFleet) - targetPlanet.getRadius()) / 2000f;
                String distanceInSuString = String.format("%.2f", distanceInSu);
                float requiredDistanceInSu = 500f / 2000f;
                String requiredDistanceInSuString = String.format("%.2f", requiredDistanceInSu);
                tooltip.addPara("The world closest to your location is " + targetPlanet.getName() + ". Your fleet is " + distanceInSuString + " stellar units away. You must be within " + requiredDistanceInSuString + " stellar units to construct an astropolis station.", bad, pad);
            }
            else
            {
                tooltip.addPara("Target host world: %s", pad, highlight, new String[]{targetPlanet.getName()});
            }

            // Checks for governorship by player. Governed planets have isPlayerOwned() equals true but
            // the controlling FactionAPI is still major faction, not player
            if(!(targetPlanet.getMarket().isPlayerOwned() && boggledTools.getBooleanSetting("boggledCanBuildAstropolisOnPurchasedGovernorshipPlanets")) && !targetPlanet.getMarket().getFactionId().equals(Factions.PLAYER))
            {
                if(targetPlanet.getMarket().getFactionId().equals(Factions.NEUTRAL))
                {
                    tooltip.addPara("You can only construct astropolis stations in orbit around worlds you control.", bad, pad);
                }
                else
                {
                    tooltip.addPara("You cannot construct an astropolis station in orbit around a world colonized by another faction.", bad, pad);
                }
            }

            if(!boggledTools.getBooleanSetting("boggledAstropolisIgnoreOrbitalRequirements"))
            {
                AstropolisOrbitBlocker astroBlocker = astropolisOrbitBlocked(targetPlanet);
                if(astroBlocker != null && astroBlocker.reason != null && astroBlocker.reason.equals("radius_too_small"))
                {
                    tooltip.addPara(targetPlanet.getName() + " is too small to host an astropolis.", bad, pad);
                }
                else if(astroBlocker != null && astroBlocker.reason != null && astroBlocker.reason.equals("too_close_to_focus"))
                {
                    tooltip.addPara("An astropolis would be unable to achieve a satisfactory orbit around " + targetPlanet.getName() + " because it is orbiting too close to " + astroBlocker.blocker.getName() + ".", bad, pad);
                }
                else if(astroBlocker != null && astroBlocker.reason != null && astroBlocker.reason.equals("too_close_to_star"))
                {
                    tooltip.addPara(targetPlanet.getName() + " is too close to " + astroBlocker.blocker.getName() + " to host an astropolis.", bad, pad);
                }
                else if(astroBlocker != null && astroBlocker.reason != null && astroBlocker.reason.equals("moon_too_close"))
                {
                    tooltip.addPara("An astropolis would be unable to achieve a satisfactory orbit around " + targetPlanet.getName() + " because " + astroBlocker.blocker.getName() + " is orbiting too close to it.", bad, pad);
                }
                else if(astroBlocker != null && astroBlocker.reason != null && astroBlocker.reason.equals("too_many_moons"))
                {
                    tooltip.addPara("An astropolis would be unable to maintain a satisfactory orbit around " + targetPlanet.getName() + " because there are four or more moons orbiting it.", bad, pad);
                }
            }

            int numAstroInOrbit = boggledTools.getNumExistingAstroplisStations(targetPlanet);
            if(numAstroInOrbit >= 3)
            {
                tooltip.addPara("Each world can support a maximum of three astropolis stations in orbit.", bad, pad);
            }
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            tooltip.addPara("Insufficient credits.", bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Astropolis_Station, "metals") < metalCost)
        {
            tooltip.addPara("Insufficient metals.", bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Astropolis_Station, "rare_metals") < transplutonicsCost)
        {
            tooltip.addPara("Insufficient transplutonics.", bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Astropolis_Station, "crew") < crewCost)
        {
            tooltip.addPara("Insufficient crew.", bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Astropolis_Station, "heavy_machinery") < heavyMachineryCost)
        {
            tooltip.addPara("Insufficient heavy machinery.", bad, pad);
        }
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