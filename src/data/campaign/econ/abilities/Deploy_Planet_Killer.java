package data.campaign.econ.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import data.campaign.econ.boggledTools;

public class Deploy_Planet_Killer extends BaseDurationAbility
{
    public Deploy_Planet_Killer() { }

    @Override
    protected void activateImpl()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        CargoAPI playerCargo = playerFleet.getCargo();
        playerCargo.removeItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData("boggled_planetkiller", null), 1f);

        boggledTools.applyPlanetKiller(boggledTools.getClosestMarketToEntity(playerFleet));
    }

    @Override
    public boolean isUsable()
    {
        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition() || playerFleet.getStarSystem() == null)
        {
            return false;
        }

        MarketAPI closestMarket = boggledTools.getClosestMarketToEntity(playerFleet);

        if(closestMarket == null)
        {
            return false;
        }

        SectorEntityToken closestMarketEntity = closestMarket.getPrimaryEntity();

        if(closestMarketEntity == null)
        {
            return false;
        }
        else if(closestMarket.getFactionId().equals("neutral") || closestMarket.getFactionId().equals("player"))
        {
            return false;
        }
        else if(boggledTools.getDistanceBetweenTokens(closestMarketEntity, playerFleet) > 200.0f)
        {
            return false;
        }

        if(Misc.isStoryCritical(closestMarket) && !boggledTools.getBooleanSetting("boggledPlanetKillerAllowDestructionOfColoniesMarkedAsEssentialForQuests"))
        {
            return false;
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData("boggled_planetkiller", null)) <= 0.0f)
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

        LabelAPI title = tooltip.addTitle("Deploy Planet-Killer Device");
        float pad = 10.0F;
        tooltip.addPara("Use a planet-killer device to destroy a colony. This will severely damage relations with most major factions in the Sector.", pad, highlight, new String[]{});

        SectorEntityToken playerFleet = Global.getSector().getPlayerFleet();
        MarketAPI targetMarket = boggledTools.getClosestMarketToEntity(playerFleet);

        if(this.isUsable())
        {
            tooltip.addPara("Target colony: %s", pad, highlight, new String[]{targetMarket.getName()});
        }

        if (playerFleet.isInHyperspace() || Global.getSector().getPlayerFleet().isInHyperspaceTransition() || playerFleet.getStarSystem() == null)
        {
            tooltip.addPara("You cannot deploy a planet-killer device in hyperspace.", bad, pad);
        }

        if(!playerFleet.isInHyperspace() && !Global.getSector().getPlayerFleet().isInHyperspaceTransition() && playerFleet.getStarSystem() != null)
        {
            if(targetMarket == null || targetMarket.getPrimaryEntity() == null)
            {
                tooltip.addPara("There are no colonies in this system.", bad, pad);
            }
            else if(targetMarket.getFactionId().equals("player"))
            {
                tooltip.addPara("The colony closest to your location is " + targetMarket.getName() + ". You cannot destroy your own colony with a planet-killer device.", bad, pad);
            }
            else if(boggledTools.getDistanceBetweenTokens(targetMarket.getPrimaryEntity(), playerFleet) > 200f)
            {
                float distanceInSu = boggledTools.getDistanceBetweenTokens(playerFleet, targetMarket.getPrimaryEntity()) / 800f;
                String distanceInSuString = String.format("%.2f", distanceInSu);
                float requiredDistanceInSu = 200f / 800f;
                String requiredDistanceInSuString = String.format("%.2f", requiredDistanceInSu);
                tooltip.addPara("The colony closest to your location is " + targetMarket.getName() + ". Your fleet is " + distanceInSuString + " stellar units away. You must be within " + requiredDistanceInSuString + " stellar units to deploy a planet-killer device.", bad, pad);
            }

            if(targetMarket != null)
            {
                if(Misc.isStoryCritical(targetMarket) && !boggledTools.getBooleanSetting("boggledPlanetKillerAllowDestructionOfColoniesMarkedAsEssentialForQuests"))
                {
                    tooltip.addPara("The colony closest to your location is " + targetMarket.getName() + ". You cannot destroy this colony because it is critical to the fate of the Sector.", bad, pad);
                }
            }
        }

        CargoAPI playerCargo = playerFleet.getCargo();
        if(playerCargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData("boggled_planetkiller", null)) <= 0.0f)
        {
            tooltip.addPara("You do not possess a planet-killer device.", bad, pad);
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