package data.campaign.econ.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import data.campaign.econ.boggledTools;

public class Colonize_Abandoned_Station extends BaseDurationAbility
{
    private float creditCost = boggledTools.getIntSetting("boggledStationRecolonizeCreditCost");
    private float crewCost = boggledTools.getIntSetting("boggledStationRecolonizeCrewCost");
    private float heavyMachineryCost = boggledTools.getIntSetting("boggledStationRecolonizeHeavyMachineryCost");
    private float metalCost = boggledTools.getIntSetting("boggledStationRecolonizeMetalCost");
    private float transplutonicsCost = boggledTools.getIntSetting("boggledStationRecolonizeTransplutonicsCost");

    public Colonize_Abandoned_Station() { }

    @Override
    protected void activateImpl()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        SectorEntityToken targetEntityForMarket = boggledTools.getClosestColonizableStationInSystem(playerFleet);
        StarSystemAPI system = targetEntityForMarket.getStarSystem();

        CargoAPI playerCargo = playerFleet.getCargo();
        playerCargo.getCredits().subtract(creditCost);
        playerCargo.removeCommodity("metals", metalCost);
        playerCargo.removeCommodity("rare_metals", transplutonicsCost);
        playerCargo.removeCommodity("crew", crewCost);
        playerCargo.removeCommodity("heavy_machinery", heavyMachineryCost);

        targetEntityForMarket.setFaction("player");
        CargoAPI cargo = targetEntityForMarket.getMarket().getSubmarket("storage").getCargo();

        //Create the new station market
        CampaignClockAPI clock = Global.getSector().getClock();
        MarketAPI market = Global.getFactory().createMarket(targetEntityForMarket.getId() + targetEntityForMarket.getName() + clock.getCycle() + clock.getMonth() + clock.getDay() + "NewMarketForStation", targetEntityForMarket.getName(), 3);
        market.setSize(3);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.setPrimaryEntity(targetEntityForMarket);

        market.setFactionId("player");
        market.setPlayerOwned(true);

        market.addCondition(Conditions.POPULATION_3);

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);

        if(targetEntityForMarket.hasTag("boggled_astropolis"))
        {
            SectorEntityToken newLightsOnColonize = system.addCustomEntity("boggled_newLightsOnColonize", "New Lights Overlay From Colonizing Abandoned Station", targetEntityForMarket.getCustomEntityType() + "_lights_overlay", playerFleet.getFaction().getId());
            newLightsOnColonize.setOrbit(targetEntityForMarket.getOrbit().makeCopy());
        }
        else if(targetEntityForMarket.hasTag("boggled_gatekeeper_station"))
        {
            market.addIndustry("ASTRAL_GATE");

            targetEntityForMarket.setCustomDescriptionId("gatekeeper_station");

            SectorEntityToken newLightsOnColonize = system.addCustomEntity("boggled_newLightsOnColonize", "New Lights Overlay From Colonizing Abandoned Station", targetEntityForMarket.getCustomEntityType() + "_lights_overlay", playerFleet.getFaction().getId());
            newLightsOnColonize.setOrbit(targetEntityForMarket.getOrbit().makeCopy());
        }
        if(targetEntityForMarket.hasTag("boggled_mining_station"))
        {
            market.addCondition(Conditions.ORE_MODERATE);
            market.addCondition(Conditions.RARE_ORE_MODERATE);
            market.getConstructionQueue().addToEnd(Industries.MINING, 0);

            targetEntityForMarket.setCustomDescriptionId("boggled_mining_station");

            SectorEntityToken newLightsOnColonize = system.addCustomEntity("boggled_newLightsOnColonize", "New Lights Overlay From Colonizing Abandoned Station", targetEntityForMarket.getCustomEntityType() + "_lights_overlay", playerFleet.getFaction().getId());
            newLightsOnColonize.setOrbit(targetEntityForMarket.getOrbit().makeCopy());
        }
        else if(targetEntityForMarket.hasTag("boggled_siphon_station") || targetEntityForMarket.getFullName().contains("Abandoned Siphon Station"))
        {
            SectorEntityToken hostGasGiant = null;
            if(targetEntityForMarket.getOrbitFocus() != null && targetEntityForMarket.getOrbitFocus() instanceof PlanetAPI && targetEntityForMarket.getOrbitFocus().getMarket() != null && boggledTools.getPlanetType((PlanetAPI)targetEntityForMarket.getOrbitFocus()).equals("gas_giant"))
            {
                hostGasGiant = targetEntityForMarket.getOrbitFocus();
            }

            if(hostGasGiant != null && !market.hasTag("boggled_astropolis") && !market.hasTag("boggled_mining_station"))
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

                market.getConstructionQueue().addToEnd(Industries.MINING, 0);

                if(targetEntityForMarket.getFullName().contains("Abandoned Siphon Station"))
                {
                    targetEntityForMarket.setName(hostGasGiant.getName() + " Siphon Station");
                    market.setName(hostGasGiant.getName() + " Siphon Station");
                }
            }

            targetEntityForMarket.setCustomDescriptionId("boggled_siphon_station");

            if(targetEntityForMarket.hasTag("boggled_siphon_station"))
            {
                SectorEntityToken newLightsOnColonize = system.addCustomEntity("boggled_newLightsOnColonize", "New Lights Overlay From Colonizing Abandoned Station", targetEntityForMarket.getCustomEntityType() + "_lights_overlay", playerFleet.getFaction().getId());
                newLightsOnColonize.setOrbit(targetEntityForMarket.getOrbit().makeCopy());
            }
        }
        else if(targetEntityForMarket.getId().contains("new_maxios"))
        {
            market.addCondition(Conditions.ORE_MODERATE);
            market.getConstructionQueue().addToEnd(Industries.MINING, 0);
        }
        else if(targetEntityForMarket.getId().contains("laicaille_habitat"))
        {
            market.addCondition(Conditions.ORE_ABUNDANT);
            market.getConstructionQueue().addToEnd(Industries.MINING, 0);
        }
        else if(targetEntityForMarket.getId().contains("thule_pirate_station"))
        {
            market.addCondition(Conditions.VOLATILES_DIFFUSE);
            market.addCondition(Conditions.COLD);
            market.getConstructionQueue().addToEnd(Industries.MINING, 0);
        }
        else if(targetEntityForMarket.getId().contains("port_tse"))
        {
            market.addCondition(Conditions.ORE_ABUNDANT);
            market.addCondition(Conditions.RARE_ORE_RICH);
            market.getConstructionQueue().addToEnd(Industries.MINING, 0);
        }
        else if(targetEntityForMarket.getId().contains("arcadia_station"))
        {
            market.addCondition(Conditions.VOLATILES_ABUNDANT);
            market.getConstructionQueue().addToEnd(Industries.MINING, 0);
        }
        else if(targetEntityForMarket.getId().contains("tigra_city"))
        {
            market.addCondition(Conditions.ORE_MODERATE);
            market.getConstructionQueue().addToEnd(Industries.MINING, 0);
        }

        //Adds the no atmosphere condition, then suppresses it so it won't increase hazard
        //market_conditions.csv overwrites the vanilla no_atmosphere condition
        //the only change made is to hide the icon on markets where primary entity has station tag
        //This is done so refining and fuel production can slot the special items
        //Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
        market.addCondition("no_atmosphere");
        market.suppressCondition("no_atmosphere");

        targetEntityForMarket.setMarket(market);

        Global.getSector().getEconomy().addMarket(market, true);

        targetEntityForMarket.setInteractionImage("illustrations", "orbital_construction");
        targetEntityForMarket.getMemoryWithoutUpdate().set("$abandonedStation", false);

        //If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
        Global.getSector().getCampaignUI().showInteractionDialog(targetEntityForMarket);

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        if(!cargo.isEmpty())
        {
            market.getSubmarket("storage").getCargo().addAll(cargo);
        }

        market.addCondition("sprite_controller");
        market.addCondition("cramped_quarters");

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);

        Global.getSoundPlayer().playUISound("ui_boggled_station_constructed", 1.0F, 1.0F);
    }

    @Override
    public boolean isUsable()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            return false;
        }

        if(!boggledTools.systemHasJumpPoint(playerFleet.getStarSystem()))
        {
            return false;
        }

        SectorEntityToken closestColonizableStation = boggledTools.getClosestColonizableStationInSystem(playerFleet);

        if(closestColonizableStation == null)
        {
            return false;
        }
        else if(closestColonizableStation.getMarket() != null && !closestColonizableStation.getMarket().getFactionId().equals("neutral"))
        {
            return false;
        }
        else if(boggledTools.getDistanceBetweenTokens(closestColonizableStation, playerFleet) > 400f)
        {
            return false;
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

        LabelAPI title = tooltip.addTitle("Colonize Abandoned Station");
        float pad = 10.0F;
        tooltip.addPara("Colonize an abandoned station. Expends %s credits, %s crew, %s heavy machinery, %s metals and %s transplutonics for construction.", pad, highlight, new String[]{(int)creditCost + "",(int)crewCost + "",(int)heavyMachineryCost +"", (int)metalCost + "", (int)transplutonicsCost +""});

        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        if(this.isUsable())
        {
            tooltip.addPara("Colonization target: %s", pad, highlight, new String[]{boggledTools.getClosestColonizableStationInSystem(playerFleet).getName()});
        }

        if(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition())
        {
            tooltip.addPara("You cannot colonize stations located in hyperspace.", bad, pad);
        }
        else if(!boggledTools.systemHasJumpPoint(playerFleet.getStarSystem()))
        {
            tooltip.addPara("You cannot construct a station in a system with no jump points.", bad, pad);
        }

        if(!(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition()))
        {
            if(!boggledTools.systemHasJumpPoint(playerFleet.getStarSystem()))
            {
                tooltip.addPara("You cannot construct a station in a system with no jump points.", bad, pad);
            }

            SectorEntityToken closestStation = boggledTools.getClosestStationInSystem(playerFleet);
            SectorEntityToken closestColonizableStation = boggledTools.getClosestColonizableStationInSystem(playerFleet);

            if(closestStation == null)
            {
                tooltip.addPara("There are no stations in this system.", bad, pad);
            }
            else if(closestStation.getMarket() != null && !closestStation.getMarket().getFactionId().equals("neutral"))
            {
                tooltip.addPara("The station closest to your location is " + closestStation.getName() + " and it is controlled by " + closestStation.getMarket().getFaction().getDisplayNameWithArticle() + ". You cannot colonize a station that is already under the control of a major faction.", bad, pad);
            }
            else if(!closestStation.equals(closestColonizableStation))
            {
                tooltip.addPara("The station closest to your location is " + closestStation.getName() + ". It is in a state of extreme disrepair and is not a viable target for colonization.", bad, pad);
            }
            else if(boggledTools.getDistanceBetweenTokens(closestColonizableStation, playerFleet) > 400f)
            {
                float distanceInSu = boggledTools.getDistanceBetweenTokens(playerFleet, closestColonizableStation) / 2000f;
                String distanceInSuString = String.format("%.2f", distanceInSu);
                float requiredDistanceInSu = 400f / 2000f;
                String requiredDistanceInSuString = String.format("%.2f", requiredDistanceInSu);
                tooltip.addPara("The station closest to your location is " + closestStation.getName() + ". Your fleet is " + distanceInSuString + " stellar units away. You must be within " + requiredDistanceInSuString + " stellar units to colonize the station.", bad, pad);
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