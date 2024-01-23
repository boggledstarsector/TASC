package boggled.campaign.econ.abilities;

import boggled.scripts.PlayerCargoCalculations.bogglesDefaultCargo;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import boggled.campaign.econ.boggledTools;
import org.jetbrains.annotations.NotNull;

public class Construct_Astropolis_Station extends BaseDurationAbility
{
    private float creditCost = boggledTools.getIntSetting("boggledAstropolisStationBuildCreditCost");
    private float crewCost = boggledTools.getIntSetting("boggledAstropolisStationBuildCrewCost");
    private float heavyMachineryCost = boggledTools.getIntSetting("boggledAstropolisStationBuildHeavyMachineryCost");
    private float metalCost = boggledTools.getIntSetting("boggledAstropolisStationBuildMetalCost");
    private float transplutonicsCost = boggledTools.getIntSetting("boggledAstropolisStationBuildTransplutonicsCost");

    public Construct_Astropolis_Station() { }

    private int numAstroInOrbit(SectorEntityToken targetPlanet)
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        if(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            return 0;
        }

        List<SectorEntityToken> allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities();

        int numAstropoli = 0;
        for(SectorEntityToken entity : allEntitiesInSystem)
        {
            if (entity.hasTag("boggled_astropolis") && entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(targetPlanet))
            {
                numAstropoli++;
            }
        }

        return numAstropoli;
    }

    private String getGreekLetter(int numAstroAlreadyPresent)
    {
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
        numAstroAlreadyPresent = Math.abs(numAstroAlreadyPresent);
        return getGreekAlphabetList().get(numAstroAlreadyPresent % 3).toLowerCase();
    }

    public String getColonyNameString(int numAstroAlreadyPresent)
    {
        numAstroAlreadyPresent = Math.abs(numAstroAlreadyPresent);
        List<String> greekAlphabetList = getGreekAlphabetList();
        int letterNum = numAstroAlreadyPresent % greekAlphabetList.size();
        int suffixNum = numAstroAlreadyPresent / greekAlphabetList.size();
        String ret = greekAlphabetList.get(letterNum);
        if (suffixNum != 0) {
            ret = ret + "-" + suffixNum;
        }
        return ret;
    }

    @NotNull
    private static List<String> getGreekAlphabetList() {
        List<String> greekAlphabetList = new ArrayList<>();
        greekAlphabetList.add("Alpha");
        greekAlphabetList.add("Beta");
        greekAlphabetList.add("Gamma");
        greekAlphabetList.add("Delta");
        greekAlphabetList.add("Epsilon");
        greekAlphabetList.add("Zeta");
        greekAlphabetList.add("Eta");
        greekAlphabetList.add("Theta");
        greekAlphabetList.add("Kappa");
        greekAlphabetList.add("Lambda");
        greekAlphabetList.add("Mu");
        greekAlphabetList.add("Nu");
        greekAlphabetList.add("Xi");
        greekAlphabetList.add("Omicron");
        greekAlphabetList.add("Pi");
        greekAlphabetList.add("Rho");
        greekAlphabetList.add("Sigma");
        greekAlphabetList.add("Tau");
        greekAlphabetList.add("Upsilon");
        greekAlphabetList.add("Phi");
        greekAlphabetList.add("Chi");
        greekAlphabetList.add("Psi");
        greekAlphabetList.add("Omega");
        return greekAlphabetList;
    }

    @Override
    protected void activateImpl()
    {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        CargoAPI playerCargo = playerFleet.getCargo();
        playerCargo.getCredits().subtract(creditCost);
        bogglesDefaultCargo.active.removeCommodity(bogglesDefaultCargo.Astropolis_Station,"metals", metalCost);
        bogglesDefaultCargo.active.removeCommodity(bogglesDefaultCargo.Astropolis_Station,"rare_metals", transplutonicsCost);
        bogglesDefaultCargo.active.removeCommodity(bogglesDefaultCargo.Astropolis_Station,"crew", crewCost);
        bogglesDefaultCargo.active.removeCommodity(bogglesDefaultCargo.Astropolis_Station,"heavy_machinery", heavyMachineryCost);

        SectorEntityToken targetPlanet = boggledTools.getClosestPlanetToken(playerFleet);
        int numAstro = numAstroInOrbit(targetPlanet);
        StarSystemAPI system = playerFleet.getStarSystem();
        float orbitRadius = targetPlanet.getRadius() + 375.0F;

        FactionAPI playerFaction = Global.getSector().getPlayerFaction();
        String playerFactionId = playerFaction.getId();

        SectorEntityToken newAstropolis = system.addCustomEntity("boggled_astropolis" + numAstro, targetPlanet.getName() + " Astropolis " + getColonyNameString(numAstro), "boggled_astropolis_station_" + getGreekLetter(numAstro) + "_small", playerFactionId);
        SectorEntityToken newAstropolisLights = system.addCustomEntity("boggled_astropolisLights", targetPlanet.getName() + " Astropolis " + getColonyNameString(numAstro) + " Lights Overlay", "boggled_astropolis_station_" + getGreekLetter(numAstro) + "_small_lights_overlay", playerFactionId);

        boggledTools.writeMessageToLog("ASTROPOLIS STATION ACTIVATEIMPL: Entering build logic.");
        if(numAstro == 0)
        {
            boggledTools.writeMessageToLog("ASTROPOLIS STATION ACTIVATEIMPL: No astropoli found. Creating one with random orbital angle.");
            newAstropolis.setCircularOrbitPointingDown(targetPlanet, boggledTools.randomOrbitalAngleFloat(), orbitRadius, orbitRadius / 10.0F);
            newAstropolisLights.setOrbit(newAstropolis.getOrbit().makeCopy());
        }
        else if(numAstro >= 1)
        {
            List<SectorEntityToken> allEntitiesInSystem = playerFleet.getStarSystem().getAllEntities();
            SectorEntityToken firstAstroToken = null;
            SectorEntityToken secondAstroToken = null;

            for(SectorEntityToken entity : allEntitiesInSystem)
            {
                if (entity.hasTag("boggled_astropolis") && entity.getOrbitFocus() != null && entity.getOrbitFocus().equals(targetPlanet) && entity.getCustomEntityType().contains("boggled_astropolis_station"))
                {
                    if(firstAstroToken == null)
                    {
                        firstAstroToken = entity;
                    }
                    else
                    {
                        secondAstroToken = entity;
                        break;
                    }
                }
            }

            if(secondAstroToken == null)
            {
                newAstropolis.setCircularOrbitPointingDown(targetPlanet, firstAstroToken.getCircularOrbitAngle() + 120f, orbitRadius, orbitRadius / 10.0F);
                boggledTools.writeMessageToLog("ASTROPOLIS STATION ACTIVATEIMPL: Created second astropolis station with orbital angle " + (firstAstroToken.getCircularOrbitAngle() + 120f) + ". The first station has angle " + firstAstroToken.getCircularOrbitAngle() + ".");
            }
            else
            {
                boggledTools.writeMessageToLog("ASTROPOLIS STATION ACTIVATEIMPL: Creating third astropolis station.");
                if(Math.abs(((firstAstroToken.getCircularOrbitAngle() + 120f) % 360f) - secondAstroToken.getCircularOrbitAngle()) < 1f)
                {
                    boggledTools.writeMessageToLog("ASTROPOLIS STATION ACTIVATEIMPL: Created third astropolis station with orbital angle " + (firstAstroToken.getCircularOrbitAngle() - 120f) + ". The first station has angle " + firstAstroToken.getCircularOrbitAngle() + " and the second station has angle " + secondAstroToken.getCircularOrbitAngle() + ".");
                    newAstropolis.setCircularOrbitPointingDown(targetPlanet, firstAstroToken.getCircularOrbitAngle() - 120f, orbitRadius, orbitRadius / 10.0F);
                }
                else
                {
                    boggledTools.writeMessageToLog("ASTROPOLIS STATION ACTIVATEIMPL: Created third astropolis station with orbital angle " + (firstAstroToken.getCircularOrbitAngle() + 120f) + ". The first station has angle " + firstAstroToken.getCircularOrbitAngle() + " and the second station has angle " + secondAstroToken.getCircularOrbitAngle() + ".");
                    newAstropolis.setCircularOrbitPointingDown(targetPlanet, firstAstroToken.getCircularOrbitAngle() + 120f, orbitRadius, orbitRadius / 10.0F);
                }
            }

            newAstropolisLights.setOrbit(newAstropolis.getOrbit().makeCopy());
        }

        MarketAPI market = null;
        if(!boggledTools.getBooleanSetting("boggledStationConstructionDelayEnabled"))
        {
//            market = boggledTools.createAstropolisStationMarket(newAstropolis, targetPlanet);
        }
        else
        {
//            newAstropolis.addScript(new BoggledUnderConstructionEveryFrameScript(newAstropolis));
            Global.getSoundPlayer().playUISound("ui_boggled_station_start_building", 1.0F, 1.0F);
        }
    }

    final class astropolisOrbitBlocker
    {
        public SectorEntityToken blocker;
        public String reason;

        public astropolisOrbitBlocker(SectorEntityToken blocker, String reason)
        {
            this.blocker = blocker;
            this.reason = reason;
        }
    }

    private astropolisOrbitBlocker astropolisOrbitBlocked(SectorEntityToken targetPlanet)
    {
        //check if the host market radius is too small
        if(targetPlanet.getRadius() < 125f)
        {
            return new astropolisOrbitBlocker(null, "radius_too_small");
        }

        //check if the host market is too close to its orbital focus
        if(targetPlanet.getOrbitFocus() != null && targetPlanet.getCircularOrbitRadius() < (targetPlanet.getOrbitFocus().getRadius() + 900f))
        {
            return new astropolisOrbitBlocker(targetPlanet.getOrbitFocus(), "too_close_to_focus");
        }

        //check if the host market is too close to a star
        if(targetPlanet.getOrbitFocus() != null && targetPlanet.getOrbitFocus().isStar() && targetPlanet.getCircularOrbitRadius() < (targetPlanet.getOrbitFocus().getRadius() + 1400f))
        {
            return new astropolisOrbitBlocker(targetPlanet.getOrbitFocus(), "too_close_to_star");
        }

        //check if the host market has a moon that is too close to it
        for (PlanetAPI planet : targetPlanet.getStarSystem().getPlanets()) {
            if (planet.getOrbitFocus() != null && !planet.isStar() && planet.getOrbitFocus().equals(targetPlanet) && planet.getCircularOrbitRadius() < (targetPlanet.getRadius() + 500f) && planet.getRadius() != 0)
            {
                return new astropolisOrbitBlocker(planet, "moon_too_close");
            }
        }

        //Check if the host market has four moons - need to block building here because it creates a visual bug where the astropolis
        //appears on top of one of the other four moons in the system view
        int numMoons = 0;
        for (PlanetAPI planet : targetPlanet.getStarSystem().getPlanets()) {
            if (planet.getOrbitFocus() != null && !planet.isStar() && planet.getOrbitFocus().equals(targetPlanet) && planet.getRadius() != 0)
            {
                numMoons++;
            }
        }

        if(numMoons >= 4)
        {
            return new astropolisOrbitBlocker(null, "too_many_moons");
        }

        //check if the host market and other planets are orbiting the same focus are too close to each other
        for (PlanetAPI planet : targetPlanet.getStarSystem().getPlanets()) {
            if (planet.getOrbitFocus() != null && !planet.isStar() && planet.getOrbitFocus().equals(targetPlanet.getOrbitFocus()))
            {
                if(Math.abs(planet.getCircularOrbitRadius() - targetPlanet.getCircularOrbitRadius()) < 400f && Math.abs(planet.getCircularOrbitRadius() - targetPlanet.getCircularOrbitRadius()) != 0)
                {
                    return new astropolisOrbitBlocker(planet, "same_focus_too_close");
                }
            }
        }

        return null;
    }

    @Override
    public boolean isUsable()
    {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            return false;
        }

        if(playerFleet.getStarSystem().getJumpPoints().isEmpty())
        {
            return false;
        }

        SectorEntityToken targetPlanet = boggledTools.getClosestPlanetToken(playerFleet);
        if(targetPlanet == null || targetPlanet.getMarket() == null ||
           (   !targetPlanet.getMarket().isPlayerOwned()
            && targetPlanet.getMarket().getFaction() != Global.getSector().getFaction(Factions.NEUTRAL)
            && targetPlanet.getMarket().getFaction() != Global.getSector().getFaction(Factions.PLAYER)))
        {
            return false;
        }

        // Checks for governorship by player. Governed planets have isPlayerOwned() equals true but
        // the controlling FactionAPI is still major faction, not player
        if(targetPlanet.getMarket().isPlayerOwned() && targetPlanet.getMarket().getFaction() != playerFleet.getFaction() && !boggledTools.getBooleanSetting("boggledCanBuildAstropolisOnPurchasedGovernorshipPlanets"))
        {
            return false;
        }

        if((Misc.getDistance(targetPlanet, playerFleet) - targetPlanet.getRadius()) > 500f)
        {
            return false;
        }

        astropolisOrbitBlocker block = astropolisOrbitBlocked(targetPlanet);
        if(block != null && !boggledTools.getBooleanSetting("boggledAstropolisIgnoreOrbitalRequirements"))
        {
            return false;
        }

        int astroLimit = boggledTools.getIntSetting("boggledMaxNumAstropoliPerPlanet");

        if(astroLimit == 0 || astroLimit > 3)
        {
            return false;
        }

        int astroInOrbit = numAstroInOrbit(targetPlanet);
        if(astroInOrbit >= astroLimit)
        {
            return false;
        }

        boolean playerHasResources = true;
        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            playerHasResources = false;
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Astropolis_Station,"metals") < metalCost)
        {
            playerHasResources = false;
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Astropolis_Station,"rare_metals") < transplutonicsCost)
        {
            playerHasResources = false;
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Astropolis_Station,"crew") < crewCost)
        {
            playerHasResources = false;
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Astropolis_Station,"heavy_machinery") < heavyMachineryCost)
        {
            playerHasResources = false;
        }

        return !this.isOnCooldown() && this.disableFrames <= 0 && playerHasResources;
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
        SectorEntityToken targetPlanet = boggledTools.getClosestPlanetToken(playerFleet);
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        LabelAPI title = tooltip.addTitle("Construct Astropolis Station");
        float pad = 10.0F;
        tooltip.addPara("Construct an astropolis station in orbit around a planet or moon. Expends %s credits, %s crew, %s heavy machinery, %s metals and %s transplutonics for construction.", pad, highlight, new String[]{(int)creditCost + "",(int)crewCost + "",(int)heavyMachineryCost +"", (int)metalCost + "", (int)transplutonicsCost +""});

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            tooltip.addPara("You cannot construct an astropolis station in hyperspace.", bad, pad);
        }
        else if(playerFleet.getStarSystem().getJumpPoints().isEmpty())
        {
            tooltip.addPara("You cannot construct a station in a system with no jump points.", bad, pad);
        }

        if (!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && targetPlanet != null)
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
                tooltip.addPara("Target host world: %s", pad, highlight, targetPlanet.getName());
            }

            if(targetPlanet.getMarket() != null && (!targetPlanet.getMarket().isPlayerOwned() && targetPlanet.getMarket().getFaction() != Global.getSector().getFaction(Factions.NEUTRAL) && targetPlanet.getMarket().getFaction() != Global.getSector().getFaction(Factions.PLAYER)))
            {
                tooltip.addPara("You cannot construct an astropolis station in orbit around a world already controlled by another faction.", bad, pad);
            }

            //Checks for governorship by player. Governed planets have isPlayerOwned() equals true but
            //the controlling FactionAPI is still major faction, not player
            if(targetPlanet.getMarket() != null && targetPlanet.getMarket().isPlayerOwned() && targetPlanet.getMarket().getFaction() != playerFleet.getFaction() && !boggledTools.getBooleanSetting("boggledCanBuildAstropolisOnPurchasedGovernorshipPlanets"))
            {
                tooltip.addPara("You cannot construct an astropolis station in orbit around a world owned by another faction.", bad, pad);
            }

            if(!boggledTools.getBooleanSetting("boggledAstropolisIgnoreOrbitalRequirements"))
            {
                astropolisOrbitBlocker astroblocker = astropolisOrbitBlocked(targetPlanet);
                if(astroblocker != null && astroblocker.reason != null && astroblocker.reason.equals("radius_too_small"))
                {
                    tooltip.addPara(targetPlanet.getName() + " is too small to host an astropolis.", bad, pad);
                }
                else if(astroblocker != null && astroblocker.reason != null && astroblocker.reason.equals("too_close_to_focus"))
                {
                    tooltip.addPara("An astropolis would be unable to achieve a satisfactory orbit around " + targetPlanet.getName() + " because it is orbiting too close to " + astroblocker.blocker.getName() + ".", bad, pad);
                }
                else if(astroblocker != null && astroblocker.reason != null && astroblocker.reason.equals("too_close_to_star"))
                {
                    tooltip.addPara(targetPlanet.getName() + " is too close to " + astroblocker.blocker.getName() + " to host an astropolis.", bad, pad);
                }
                else if(astroblocker != null && astroblocker.reason != null && astroblocker.reason.equals("moon_too_close"))
                {
                    tooltip.addPara("An astropolis would be unable to achieve a satisfactory orbit around " + targetPlanet.getName() + " because " + astroblocker.blocker.getName() + " is orbiting too close to it.", bad, pad);
                }
                else if(astroblocker != null && astroblocker.reason != null && astroblocker.reason.equals("same_focus_too_close"))
                {
                    tooltip.addPara("An astropolis would be unable to maintain a satisfactory orbit around " + targetPlanet.getName() + " because " + astroblocker.blocker.getName() + " periodically approaches very near to this world.", bad, pad);
                }
                else if(astroblocker != null && astroblocker.reason != null && astroblocker.reason.equals("too_many_moons"))
                {
                    tooltip.addPara("An astropolis would be unable to maintain a satisfactory orbit around " + targetPlanet.getName() + " because there are four or more moons orbiting it.", bad, pad);
                }
            }

            int maxAstropoliPerPlanet = boggledTools.getIntSetting("boggledMaxNumAstropoliPerPlanet");
            int astroInOrbit = numAstroInOrbit(targetPlanet);
            if(maxAstropoliPerPlanet > 3)
            {
                tooltip.addPara("Permissible values for boggledMaxNumAstropoliPerPlanet are 0, 1, 2 or 3. Please use the settings file for this mod to enter a permissible value. The reason for this limitation is that too many colonies in close proximity will cause performance issues.", bad, pad);
            }
            else if(maxAstropoliPerPlanet == 0)
            {
                tooltip.addPara("Astropolis construction has been disabled because boggledMaxNumAstropoliPerPlanet is set to zero in the settings file. To enable construction, set boggledMaxNumAstropoliPerPlanet to 1, 2 or 3.", bad, pad);
            }
            else if(astroInOrbit >= maxAstropoliPerPlanet && maxAstropoliPerPlanet == 1)
            {
                tooltip.addPara("Each world can only support a single astropolis. " + targetPlanet.getName() + " already has an astropolis in orbit.", bad, pad);
            }
            else if(astroInOrbit >= maxAstropoliPerPlanet && maxAstropoliPerPlanet > 1)
            {
                tooltip.addPara("Each world can support a maximum of " + maxAstropoliPerPlanet + " astropoli. " + targetPlanet.getName() + " has reached or exceeded that limit.", bad, pad);
            }
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            tooltip.addPara("Insufficient credits.", bad, pad);
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Astropolis_Station,"crew") < crewCost)
        {
            tooltip.addPara("Insufficient crew.", bad, pad);
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Astropolis_Station,"heavy_machinery") < heavyMachineryCost)
        {
            tooltip.addPara("Insufficient heavy machinery.", bad, pad);
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Astropolis_Station,"metals") < metalCost)
        {
            tooltip.addPara("Insufficient metals.", bad, pad);
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Astropolis_Station,"rare_metals") < transplutonicsCost)
        {
            tooltip.addPara("Insufficient transplutonics.", bad, pad);
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