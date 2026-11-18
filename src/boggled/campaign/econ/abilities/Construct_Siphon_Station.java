package boggled.campaign.econ.abilities;

import boggled.campaign.econ.StringHelper;
import boggled.campaign.econ.boggledTools;
import boggled.scripts.PlayerCargoCalculations.boggledDefaultCargo;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

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
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Mining_Station, "rare_metals", transplutonicsCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Mining_Station, "crew", crewCost);
        boggledDefaultCargo.active.removeCommodity(playerCargo, boggledDefaultCargo.Mining_Station, "heavy_machinery", heavyMachineryCost);

        String siphonName = StringHelper.getString(StringHelper.ABILITIES, "siphon_entity_name", StringHelper.tokens("planet", hostGasGiant.getName()));
        SectorEntityToken newSiphonStation = system.addCustomEntity("boggled_siphon_station", siphonName, "boggled_siphon_station_small", Global.getSector().getPlayerFleet().getFaction().getId());
        newSiphonStation.setCircularOrbitPointingDown(hostGasGiant, boggledTools.getAngleFromPlayerFleet(hostGasGiant), hostGasGiant.getRadius() + 50f, (hostGasGiant.getRadius() + 50f) / 10.0F);

        SectorEntityToken newSiphonStationLights = system.addCustomEntity("boggled_siphonStationLights", "Siphon Station Lights Overlay", "boggled_siphon_station_small_lights_overlay", Global.getSector().getPlayerFleet().getFaction().getId());
        newSiphonStationLights.setOrbit(newSiphonStation.getOrbit().makeCopy());

        boggledTools.createSiphonStationMarket(newSiphonStation, hostGasGiant);
    }

    @Override
    public boolean isUsable()
    {
        // Check if required research is completed (data-driven system)
        String requiredResearch = boggledTools.getRequiredResearchForAbility("boggled_construct_siphon_station");
        if (requiredResearch != null && !boggledTools.isResearched(requiredResearch))
        {
            return false;
        }

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

        if(system.hasTag(Tags.SYSTEM_ABYSSAL))
        {
            return false;
        }

        if(gasGiantAlreadyHasSiphonStationInOrbit(closestGasGiantToken))
        {
            return false;
        }

        // Can't build a siphon station if there's a moon orbiting very close to the gas giant.
        if(getMoonBlockingSiphonStation(closestGasGiantToken) != null)
        {
            return false;
        }

        // Prevent visual bug where if there are four or more moons in orbit the station will appear on top of them in the system view UI.
        if(boggledTools.getNumMoonsInOrbitAroundPlanet(closestGasGiantToken) >= 4)
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

        tooltip.addTitle(StringHelper.getString(StringHelper.ABILITIES, "siphon_title"));
        float pad = 10.0F;
        String credits = (int) creditCost + "";
        String crew = (int) crewCost + "";
        String heavyMachinery = (int) heavyMachineryCost + "";
        String metals = (int) metalCost + "";
        String transplutonics = (int) transplutonicsCost + "";
        String costSentence = StringHelper.getString(StringHelper.ABILITIES, "siphon_cost", StringHelper.tokens(
                "credits", credits, "crew", crew, "heavyMachinery", heavyMachinery, "metals", metals, "transplutonics", transplutonics));
        StringHelper.applyHighlight(tooltip.addPara(costSentence, pad), highlight, credits, crew, heavyMachinery, metals, transplutonics);

        // Check research requirement for tooltip
        String requiredResearch = boggledTools.getRequiredResearchForAbility("boggled_construct_siphon_station");
        if (requiredResearch != null && !boggledTools.isResearched(requiredResearch))
        {
            String researchName = boggledTools.getResearchDisplayName(requiredResearch);
            tooltip.addPara(StringHelper.getString(StringHelper.GENERAL, "requires_research", StringHelper.tokens("research", researchName)), bad, pad);
        }

        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        StarSystemAPI system = playerFleet.getStarSystem();

        if(playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition() || system == null)
        {
            tooltip.addPara(StringHelper.getString(StringHelper.ABILITIES, "siphon_hyperspace"), bad, pad);
        }
        else if(system.getJumpPoints().isEmpty())
        {
            tooltip.addPara(StringHelper.getString("cannot_construct_no_jump_points"), bad, pad);
        }
        else if(system.hasTag(Tags.SYSTEM_ABYSSAL))
        {
            tooltip.addPara(StringHelper.getString("cannot_construct_abyssal"), bad, pad);
        }

        if(!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && system != null)
        {
            // Inform the player whether they're close enough to a valid gas giant
            SectorEntityToken closestGasGiantToken = boggledTools.getClosestGasGiantToken(playerFleet);
            if(closestGasGiantToken == null)
            {
                tooltip.addPara(StringHelper.getString(StringHelper.ABILITIES, "siphon_no_gas_giants", StringHelper.tokens("system", system.getName())), bad, pad);
            }
            else if(!closestGasGiantToken.getMarket().getFactionId().equals(Factions.PLAYER) && !closestGasGiantToken.getMarket().getFactionId().equals(Factions.NEUTRAL))
            {
                tooltip.addPara(StringHelper.getString(StringHelper.ABILITIES, "siphon_other_faction", StringHelper.tokens(
                        "planet", closestGasGiantToken.getName(),
                        "faction", closestGasGiantToken.getMarket().getFaction().getDisplayName())), bad, pad);
            }
            else
            {
                if((Misc.getDistance(closestGasGiantToken, playerFleet) - closestGasGiantToken.getRadius()) > 250f)
                {
                    float distanceInSu = (Misc.getDistance(playerFleet, closestGasGiantToken) - closestGasGiantToken.getRadius()) / 2000f;
                    String distanceInSuString = String.format("%.2f", distanceInSu);
                    float requiredDistanceInSu = 250f / 2000f;
                    String requiredDistanceInSuString = String.format("%.2f", requiredDistanceInSu);
                    tooltip.addPara(StringHelper.getString(StringHelper.ABILITIES, "siphon_too_far", StringHelper.tokens(
                            "planet", closestGasGiantToken.getName(), "distance", distanceInSuString, "required", requiredDistanceInSuString)), bad, pad);
                }

                if(gasGiantAlreadyHasSiphonStationInOrbit(closestGasGiantToken))
                {
                    tooltip.addPara(StringHelper.getString(StringHelper.ABILITIES, "siphon_one_per_giant"), bad, pad);
                }

                PlanetAPI blockingMoon = getMoonBlockingSiphonStation(closestGasGiantToken);
                if(blockingMoon != null)
                {
                    tooltip.addPara(StringHelper.getString(StringHelper.ABILITIES, "siphon_moon_too_close", StringHelper.tokens(
                            "planet", closestGasGiantToken.getName(), "moon", blockingMoon.getName())), bad, pad);
                }

                // Prevent visual bug where if there are four or more moons in orbit the station will appear on top of them in the system view UI.
                if(boggledTools.getNumMoonsInOrbitAroundPlanet(closestGasGiantToken) >= 4)
                {
                    tooltip.addPara(StringHelper.getString(StringHelper.ABILITIES, "siphon_too_many_moons", StringHelper.tokens("planet", closestGasGiantToken.getName())), bad, pad);
                }
            }
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getCredits().get() < creditCost)
        {
            tooltip.addPara(StringHelper.getString("insufficient_credits"), bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Siphon_Station, "metals") < metalCost)
        {
            tooltip.addPara(StringHelper.getString("insufficient_metals"), bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Siphon_Station, "rare_metals") < transplutonicsCost)
        {
            tooltip.addPara(StringHelper.getString("insufficient_transplutonics"), bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Siphon_Station, "crew") < crewCost)
        {
            tooltip.addPara(StringHelper.getString("insufficient_crew"), bad, pad);
        }
        if(boggledDefaultCargo.active.getCommodityAmount(playerCargo, boggledDefaultCargo.Siphon_Station, "heavy_machinery") < heavyMachineryCost)
        {
            tooltip.addPara(StringHelper.getString("insufficient_heavy_machinery"), bad, pad);
        }

        if(this.isUsable())
        {
            String targetName = boggledTools.getClosestGasGiantToken(playerFleet).getName();
            String targetSentence = StringHelper.getString(StringHelper.ABILITIES, "siphon_target", StringHelper.tokens("planet", targetName));
            StringHelper.applyHighlight(tooltip.addPara(targetSentence, pad), highlight, targetName);
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