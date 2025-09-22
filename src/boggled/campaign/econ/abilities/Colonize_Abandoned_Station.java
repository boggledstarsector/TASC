package boggled.campaign.econ.abilities;

import boggled.campaign.econ.boggledTools;
import boggled.scripts.BoggledNewStationMarketInteractionScript;
import boggled.scripts.BoggledPlayerFactionSetupScript;
import boggled.scripts.PlayerCargoCalculations.boggledDefaultCargo;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public class Colonize_Abandoned_Station extends BaseDurationAbility
{
    private final float creditCost = boggledTools.getIntSetting("boggledStationRecolonizeCreditCost");
    private final float crewCost = boggledTools.getIntSetting("boggledStationRecolonizeCrewCost");
    private final float heavyMachineryCost = boggledTools.getIntSetting("boggledStationRecolonizeHeavyMachineryCost");
    private final float metalCost = boggledTools.getIntSetting("boggledStationRecolonizeMetalCost");
    private final float transplutonicsCost = boggledTools.getIntSetting("boggledStationRecolonizeTransplutonicsCost");

    public Colonize_Abandoned_Station() { }

    @Override
    protected void activateImpl()
    {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        SectorEntityToken targetEntityForMarket = boggledTools.getClosestColonizableStationInSystem(playerFleet);
        StarSystemAPI system = targetEntityForMarket.getStarSystem();

        CargoAPI playerCargo = playerFleet.getCargo();
        playerCargo.getCredits().subtract(creditCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Abandoned_Station, "metals", metalCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Abandoned_Station, "rare_metals", transplutonicsCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Abandoned_Station, "crew", crewCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Abandoned_Station, "heavy_machinery", heavyMachineryCost);

        targetEntityForMarket.setFaction(Factions.PLAYER);
        CargoAPI cargo = targetEntityForMarket.getMarket().getSubmarket("storage").getCargo();

        //Create the new station market
        CampaignClockAPI clock = Global.getSector().getClock();
        MarketAPI market = Global.getFactory().createMarket(targetEntityForMarket.getId() + targetEntityForMarket.getName() + clock.getCycle() + clock.getMonth() + clock.getDay() + "NewMarketForStation", targetEntityForMarket.getName(), 3);
        market.setSize(3);

        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.setPrimaryEntity(targetEntityForMarket);

        market.setFactionId(Factions.PLAYER);
        market.setPlayerOwned(true);

        market.addCondition(Conditions.POPULATION_3);

        market.addIndustry(Industries.POPULATION);
        market.getConstructionQueue().addToEnd(Industries.SPACEPORT, 0);

        if(targetEntityForMarket.hasTag(boggledTools.BoggledTags.astropolisStation))
        {
            SectorEntityToken newLightsOnColonize = system.addCustomEntity("boggled_newLightsOnColonize", "New Lights Overlay From Colonizing Abandoned Station", targetEntityForMarket.getCustomEntityType() + "_lights_overlay", playerFleet.getFaction().getId());
            newLightsOnColonize.setOrbit(targetEntityForMarket.getOrbit().makeCopy());
        }
        else if(targetEntityForMarket.hasTag(boggledTools.BoggledTags.miningStation))
        {
            if(boggledTools.getBooleanSetting("boggledMiningStationLinkToResourceBelts"))
            {
                int numAsteroidBeltsInSystem = boggledTools.getNumAsteroidTerrainsInSystem(targetEntityForMarket);
                String resourceLevel = boggledTools.getMiningStationResourceString(numAsteroidBeltsInSystem);
                market.addCondition("ore_" + resourceLevel);
                market.addCondition("rare_ore_" + resourceLevel);
            }
            else
            {
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
                market.addCondition("ore_" + resourceLevel);
                market.addCondition("rare_ore_" + resourceLevel);
            }
            market.getConstructionQueue().addToEnd(Industries.MINING, 0);

            SectorEntityToken newLightsOnColonize = system.addCustomEntity("boggled_newLightsOnColonize", "New Lights Overlay From Colonizing Abandoned Station", targetEntityForMarket.getCustomEntityType() + "_lights_overlay", playerFleet.getFaction().getId());
            newLightsOnColonize.setOrbit(targetEntityForMarket.getOrbit().makeCopy());
        }
        else if(targetEntityForMarket.hasTag(boggledTools.BoggledTags.siphonStation) || targetEntityForMarket.getFullName().contains("Abandoned Siphon Station"))
        {
            SectorEntityToken hostGasGiant = null;
            if(targetEntityForMarket.getOrbitFocus() != null && targetEntityForMarket.getOrbitFocus() instanceof PlanetAPI && targetEntityForMarket.getOrbitFocus().getMarket() != null && ((PlanetAPI) targetEntityForMarket.getOrbitFocus()).isGasGiant())
            {
                hostGasGiant = targetEntityForMarket.getOrbitFocus();
            }

            if(hostGasGiant != null && !market.hasTag(boggledTools.BoggledTags.astropolisStation) && !market.hasTag(boggledTools.BoggledTags.miningStation))
            {
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

                market.getConstructionQueue().addToEnd(Industries.MINING, 0);

                if(targetEntityForMarket.getFullName().contains("Abandoned Siphon Station"))
                {
                    targetEntityForMarket.setName(hostGasGiant.getName() + " Siphon Station");
                    market.setName(hostGasGiant.getName() + " Siphon Station");
                }
            }

            // targetEntityForMarket.setCustomDescriptionId("boggled_siphon_station");

            if(targetEntityForMarket.hasTag(boggledTools.BoggledTags.siphonStation))
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

        market.addSubmarket("storage");
        StoragePlugin storage = (StoragePlugin)market.getSubmarket("storage").getPlugin();
        storage.setPlayerPaidToUnlock(true);
        market.addSubmarket("local_resources");

        if(!cargo.isEmpty())
        {
            playerFleet.getCargo().addAll(cargo);
            FleetDataAPI mothballedShips = cargo.getMothballedShips();
            for(FleetMemberAPI ship : mothballedShips.getMembersInPriorityOrder())
            {
                Global.getSector().getPlayerFleet().getFleetData().addFleetMember(ship);
            }
        }

        market.addCondition("sprite_controller");
        market.addCondition("cramped_quarters");

        boggledTools.surveyAll(market);

        // If the player doesn't view the colony management screen within a few days of market creation, then there can be a bug related to population growth
        // Need these EveryFrameScripts because we cannot replicate the vanilla behavior to show the player faction set up screen and the CoreUITab at the same time.
        // See post from Alex at https://fractalsoftworks.com/forum/index.php?topic=5061.0, page 794
        Global.getSector().addTransientScript(new BoggledPlayerFactionSetupScript());
        Global.getSector().addTransientScript(new BoggledNewStationMarketInteractionScript(market));

        Global.getSoundPlayer().playUISound("ui_boggled_station_constructed", 1.0F, 1.0F);
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

        if(system.getJumpPoints().isEmpty())
        {
            return false;
        }

        SectorEntityToken closestColonizableStation = boggledTools.getClosestColonizableStationInSystem(playerFleet);

        if(closestColonizableStation == null)
        {
            return false;
        }
        else if(closestColonizableStation.getMarket() != null && !closestColonizableStation.getMarket().getFactionId().equals(Factions.NEUTRAL))
        {
            return false;
        }
        else if(Misc.getDistance(closestColonizableStation, playerFleet) > 400f)
        {
            return false;
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Abandoned_Station, "metals") < metalCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Abandoned_Station, "rare_metals") < transplutonicsCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Abandoned_Station, "crew") < crewCost)
        {
            return false;
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Abandoned_Station, "heavy_machinery") < heavyMachineryCost)
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

        LabelAPI title = tooltip.addTitle("Colonize Abandoned Station");
        float pad = 10.0F;
        tooltip.addPara("Colonize an abandoned station. Expends %s credits, %s crew, %s heavy machinery, %s metals and %s transplutonics for construction.", pad, highlight, new String[]{(int)creditCost + "",(int)crewCost + "",(int)heavyMachineryCost +"", (int)metalCost + "", (int)transplutonicsCost +""});

        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = playerFleet.getStarSystem();

        if(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition() || system == null)
        {
            tooltip.addPara("You cannot colonize stations in hyperspace.", bad, pad);
        }
        else if(system.getJumpPoints().isEmpty())
        {
            tooltip.addPara("You cannot construct a station in a system with no jump points.", bad, pad);
        }
        else
        {
            SectorEntityToken closestStation = boggledTools.getClosestStationInSystem(playerFleet);
            SectorEntityToken closestColonizableStation = boggledTools.getClosestColonizableStationInSystem(playerFleet);

            if(closestStation == null)
            {
                tooltip.addPara("There are no stations in the " + system.getName() + ".", bad, pad);
            }
            else if(closestStation.getMarket() != null && !closestStation.getMarket().getFactionId().equals(Factions.NEUTRAL))
            {
                tooltip.addPara("The station closest to your location is " + closestStation.getName() + " and it is controlled by " + closestStation.getMarket().getFaction().getDisplayNameWithArticle() + ". You cannot colonize a station that is already under the control of a major faction.", bad, pad);
            }
            else if(!closestStation.equals(closestColonizableStation))
            {
                tooltip.addPara("The station closest to your location is " + closestStation.getName() + ". It is in a state of extreme disrepair and is not a viable target for colonization.", bad, pad);
            }
            else if(Misc.getDistance(closestColonizableStation, playerFleet) > 400f)
            {
                float distanceInSu = Misc.getDistance(playerFleet, closestColonizableStation) / 2000f;
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
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Abandoned_Station, "metals") < metalCost)
        {
            tooltip.addPara("Insufficient metals.", bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Abandoned_Station, "rare_metals") < transplutonicsCost)
        {
            tooltip.addPara("Insufficient transplutonics.", bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Abandoned_Station, "crew") < crewCost)
        {
            tooltip.addPara("Insufficient crew.", bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Abandoned_Station, "heavy_machinery") < heavyMachineryCost)
        {
            tooltip.addPara("Insufficient heavy machinery.", bad, pad);
        }

        if(this.isUsable())
        {
            tooltip.addPara("Colonization target: %s", pad, highlight, new String[]{boggledTools.getClosestColonizableStationInSystem(playerFleet).getName()});
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