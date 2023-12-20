package data.campaign.econ.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import data.campaign.econ.boggledTools;
import data.scripts.BoggledUnderConstructionEveryFrameScript;
import data.scripts.PlayerCargoCalculations.bogglesDefaultCargo;

public class Construct_Mining_Station extends BaseDurationAbility
{
    private float creditCost = boggledTools.getIntSetting("boggledMiningStationBuildCreditCost");
    private float crewCost = boggledTools.getIntSetting("boggledMiningStationBuildCrewCost");
    private float heavyMachineryCost = boggledTools.getIntSetting("boggledMiningStationBuildHeavyMachineryCost");
    private float metalCost = boggledTools.getIntSetting("boggledMiningStationBuildMetalCost");
    private float transplutonicsCost = boggledTools.getIntSetting("boggledMiningStationBuildTransplutonicsCost");

    public Construct_Mining_Station() { }

    @Override
    protected void activateImpl()
    {
        CampaignClockAPI clock = Global.getSector().getClock();
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        CargoAPI playerCargo = playerFleet.getCargo();
        playerCargo.getCredits().subtract(creditCost);
        bogglesDefaultCargo.active.removeCommodity(bogglesDefaultCargo.Mining_Station,"metals", metalCost);
        bogglesDefaultCargo.active.removeCommodity(bogglesDefaultCargo.Mining_Station,"rare_metals", transplutonicsCost);
        bogglesDefaultCargo.active.removeCommodity(bogglesDefaultCargo.Mining_Station,"crew", crewCost);
        bogglesDefaultCargo.active.removeCommodity(bogglesDefaultCargo.Mining_Station,"heavy_machinery", heavyMachineryCost);

        StarSystemAPI system = playerFleet.getStarSystem();
        SectorEntityToken newMiningStation = system.addCustomEntity("boggled_mining_station" + clock.getCycle() + clock.getMonth() + clock.getDay(), system.getBaseName() + " Mining Station", "boggled_mining_station_small", playerFleet.getFaction().getId());

        //Set the mining station in an orbit that keeps it within the asteroid belt or asteroid field
        if(boggledTools.playerFleetInAsteroidBelt(playerFleet))
        {
            SectorEntityToken focus = boggledTools.getFocusOfAsteroidBelt(playerFleet);
            float orbitRadius = boggledTools.getDistanceBetweenTokens(focus, playerFleet);
            float orbitAngle = boggledTools.getAngleFromPlayerFleet(focus);

            newMiningStation.setCircularOrbitPointingDown(focus, orbitAngle + 1, orbitRadius, orbitRadius / 10.0F);
        }
        else if(boggledTools.playerFleetInAsteroidField(playerFleet))
        {
            OrbitAPI asteroidOrbit = boggledTools.getAsteroidFieldOrbit(playerFleet);

            if (asteroidOrbit != null)
            {
                newMiningStation.setCircularOrbitWithSpin(asteroidOrbit.getFocus(), boggledTools.getAngleFromPlayerFleet(asteroidOrbit.getFocus()), boggledTools.getDistanceBetweenTokens(playerFleet, asteroidOrbit.getFocus()), asteroidOrbit.getOrbitalPeriod(), 5f, 10f);
            }
            else
            {
                SectorEntityToken centerOfAsteroidField = boggledTools.getAsteroidFieldEntity(playerFleet);
                newMiningStation.setCircularOrbitWithSpin(centerOfAsteroidField, boggledTools.getAngleFromPlayerFleet(centerOfAsteroidField), boggledTools.getDistanceBetweenTokens(playerFleet, centerOfAsteroidField), 40f, 5f, 10f);
            }
        }

        SectorEntityToken newMiningStationLights = system.addCustomEntity("boggled_miningStationLights", "Mining Station Lights Overlay", "boggled_mining_station_small_lights_overlay", playerFleet.getFaction().getId());
        newMiningStationLights.setOrbit(newMiningStation.getOrbit().makeCopy());

        MarketAPI market = null;
        if(!boggledTools.getBooleanSetting("boggledStationConstructionDelayEnabled"))
        {
            market = boggledTools.createMiningStationMarket(newMiningStation);
        }
        else
        {
            newMiningStation.addScript(new BoggledUnderConstructionEveryFrameScript(newMiningStation));
            Global.getSoundPlayer().playUISound("ui_boggled_station_start_building", 1.0F, 1.0F);
        }

        //Delete abandoned mining stations and transfer their cargo to the newly created one
        CargoAPI cargo = null;
        ArrayList<SectorEntityToken> stationsToDelete = new ArrayList<SectorEntityToken>();

        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity.hasTag("boggled_mining_station") && entity.getFaction().getId().equals(Factions.NEUTRAL)) {
                stationsToDelete.add(entity);
            }
        }

        for (SectorEntityToken sectorEntityToken : stationsToDelete) {
            cargo = sectorEntityToken.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();
            if (!cargo.isEmpty()) {
                //Put the deleted stations' cargo into the new station market if it was created
                //Otherwise, if the station is still under construction, put it into the player cargo
                if (market != null) {
                    market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addAll(cargo);
                } else {
                    playerCargo.addAll(cargo);
                }
            }
            playerFleet.getStarSystem().removeEntity(sectorEntityToken);
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

        boolean playerHasResources = true;
        boolean miningStationCapReached = false;
        int miningStationsInSystem = 0;
        int miningStationCap = boggledTools.getIntSetting("boggledMaxNumMiningStationsPerSystem");

        if(!(boggledTools.playerFleetInAsteroidBelt(playerFleet) || boggledTools.playerFleetInAsteroidField(playerFleet)))
        {
            return false;
        }

        if(boggledTools.playerFleetTooCloseToJumpPoint(playerFleet))
        {
            return false;
        }

        if(!boggledTools.systemHasJumpPoint(playerFleet.getStarSystem()))
        {
            return false;
        }

        if(miningStationCap == 0)
        {
            return false;
        }

        for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
            if (entity.hasTag("boggled_mining_station") && !entity.getFaction().getId().equals(Factions.NEUTRAL)) {
                miningStationsInSystem++;
            }
        }

        if(miningStationsInSystem >= miningStationCap)
        {
            miningStationCapReached = true;
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            playerHasResources = false;
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Mining_Station,"metals") < metalCost)
        {
            playerHasResources = false;
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Mining_Station,"rare_metals") < transplutonicsCost)
        {
            playerHasResources = false;
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Mining_Station,"crew") < crewCost)
        {
            playerHasResources = false;
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Mining_Station,"heavy_machinery") < heavyMachineryCost)
        {
            playerHasResources = false;
        }

        return !this.isOnCooldown() && this.disableFrames <= 0 && !miningStationCapReached && playerHasResources;
    }

    @Override
    public boolean hasTooltip()
    {
        return true;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded)
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        LabelAPI title = tooltip.addTitle("Construct Mining Station");
        float pad = 10.0F;
        tooltip.addPara("Construct a mining station in an asteroid belt or asteroid field. Expends %s credits, %s crew, %s heavy machinery, %s metals and %s transplutonics for construction.", pad, highlight, new String[]{(int)creditCost + "",(int)crewCost + "",(int)heavyMachineryCost +"", (int)metalCost + "", (int)transplutonicsCost +""});

        boolean playerFleetInAsteroidBelt = false;

        if (!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            if (boggledTools.playerFleetInAsteroidBelt(playerFleet) || boggledTools.playerFleetInAsteroidField(playerFleet))
            {
                playerFleetInAsteroidBelt = true;
            }
        }

        if(boggledTools.getBooleanSetting("boggledMiningStationLinkToResourceBelts"))
        {
            if (!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition())
            {
                Integer numAsteroidBeltsInSystem = boggledTools.getNumAsteroidTerrainsInSystem(playerFleet);
                tooltip.addPara("There are %s asteroid belts and/or asteroid fields in the " + playerFleet.getStarSystem().getName() + ". A mining station constructed here would have %s resources.", pad, highlight, new String[]{numAsteroidBeltsInSystem +"", boggledTools.getMiningStationResourceString(numAsteroidBeltsInSystem)});
            }
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
            tooltip.addPara("Mining stations have %s ore and rare ore resources.", pad, highlight, new String[]{resourceLevel});
        }

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            tooltip.addPara("You cannot construct a mining station in hyperspace.", bad, pad);
        }
        else if(boggledTools.playerFleetTooCloseToJumpPoint(playerFleet))
        {
            tooltip.addPara("You cannot construct a mining station so close to a jump point.", bad, pad);
        }
        else if(!boggledTools.systemHasJumpPoint(playerFleet.getStarSystem()))
        {
            tooltip.addPara("You cannot construct a station in a system with no jump points.", bad, pad);
        }

        boolean miningStationCapReached = false;
        int miningStationsInSystem = 0;
        int miningStationCap = boggledTools.getIntSetting("boggledMaxNumMiningStationsPerSystem");

        if (!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
                if (entity.hasTag("boggled_mining_station") && !entity.getFaction().getId().equals(Factions.NEUTRAL)) {
                    miningStationsInSystem++;
                }
            }
        }

        if(miningStationsInSystem >= miningStationCap)
        {
            miningStationCapReached = true;
        }

        if (!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            for (SectorEntityToken entity : playerFleet.getStarSystem().getAllEntities()) {
                if (entity.hasTag("boggled_mining_station") && entity.getFaction().getId().equals(Factions.NEUTRAL)) {
                    tooltip.addPara("There is at least one abandoned player-built mining station in this system. If you construct a new mining station, any abandoned stations will be destroyed and any cargo stored on them will be transferred to the new station.", pad, highlight, new String[]{});
                }
            }
        }

        if(!playerFleetInAsteroidBelt)
        {
            tooltip.addPara("Your fleet is too far away from an asteroid belt or asteroid field to build a mining station.", bad, pad);
        }

        if(miningStationCapReached && miningStationCap == 0)
        {
            tooltip.addPara("Construction of player-built mining stations has been disabled in the settings.json file. To enable construction of mining stations, change the value boggledMaxNumMiningStationsPerSystem to something other than zero.", bad, pad);
        }
        else if (miningStationCapReached && miningStationCap == 1)
        {
            tooltip.addPara("Each system can only support one player-built mining station. The mining station that already exists must be abandoned before a new mining station can be built in this system.", bad, pad);
        }
        else if(miningStationCapReached && miningStationCap > 1)
        {
            tooltip.addPara("Each system can only support " + miningStationCap + " player-built mining stations. You must abandon one or more existing mining stations before a new mining station can be constructed in this system.", bad, pad);
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            tooltip.addPara("Insufficient credits.", bad, pad);
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Mining_Station,"crew") < crewCost)
        {
            tooltip.addPara("Insufficient crew.", bad, pad);
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Mining_Station,"heavy_machinery") < heavyMachineryCost)
        {
            tooltip.addPara("Insufficient heavy machinery.", bad, pad);
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Mining_Station,"metals") < metalCost)
        {
            tooltip.addPara("Insufficient metals.", bad, pad);
        }

        if(bogglesDefaultCargo.active.getCommodityAmount(bogglesDefaultCargo.Mining_Station,"rare_metals") < transplutonicsCost)
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