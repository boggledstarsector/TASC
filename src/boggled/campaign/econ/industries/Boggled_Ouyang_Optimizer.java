package boggled.campaign.econ.industries;

import java.awt.*;
import java.lang.String;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import boggled.campaign.econ.boggledTools;

public class Boggled_Ouyang_Optimizer extends BaseIndustry
{
    @Override
    public boolean canBeDisrupted()
    {
        return true;
    }

    private int daysWithoutShortage = 0;
    private int lastDayChecked = 0;
    public static int requiredDaysToOptimize = 200;

    private SectorEntityToken getOrbitFocus()
    {
        return this.market.getPrimaryEntity().getOrbitFocus();
    }

    private MarketAPI getFocusMarket()
    {
        return this.market.getPrimaryEntity().getOrbitFocus().getMarket();
    }

    private boolean marketSuitableForOptimizer()
    {
        // Only buildable on stations
        if(!boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        // Station needs to obit a gas giant planet
        SectorEntityToken orbitFocus = getOrbitFocus();
        if(orbitFocus == null)
        {
            return false;
        }

        if(orbitFocus.getMarket() == null || orbitFocus.getMarket().getPlanetEntity() == null || !boggledTools.getPlanetType(orbitFocus.getMarket().getPlanetEntity()).equals("gas_giant"))
        {
            return false;
        }

        // Can't already have extreme weather
        MarketAPI focusMarket = getFocusMarket();
        if(focusMarket.hasCondition("extreme_weather"))
        {
            return false;
        }

        // Can't already have maxed out volatiles
        if(focusMarket.hasCondition("organics_plentiful"))
        {
            return false;
        }

        return true;
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        if(this.marketSuitableForOptimizer() && this.isFunctional())
        {
            CampaignClockAPI clock = Global.getSector().getClock();

            if(clock.getDay() != lastDayChecked)
            {
                daysWithoutShortage++;
                lastDayChecked = clock.getDay();

                if(daysWithoutShortage >= requiredDaysToOptimize)
                {
                    if (this.market.isPlayerOwned())
                    {
                        MessageIntel intel = new MessageIntel("Ouyang Optimization on " + getFocusMarket().getName(), Misc.getBasePlayerColor());
                        intel.addLine("    - Completed");
                        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                    }

                    boggledTools.incrementVolatilesForOuyangOptimization(getFocusMarket());

                    boggledTools.addCondition(getFocusMarket(), "extreme_weather");
                }
            }
        }
    }

    @Override
    public void apply()
    {
        super.apply(true);
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") || !boggledTools.getBooleanSetting("boggledOuyangOptimizerEnabled")) {
            return false;
        }

        if(!this.marketSuitableForOptimizer())
        {
            return false;
        }

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") || !boggledTools.getBooleanSetting("boggledOuyangOptimizerEnabled")) {
            return false;
        }

        if(!this.marketSuitableForOptimizer())
        {
            return false;
        }

        return super.showWhenUnavailable();
    }

    @Override
    public String getUnavailableReason()
    {
        // Station needs to obit a gas giant planet
        SectorEntityToken orbitFocus = getOrbitFocus();
        if(orbitFocus == null)
        {
            return this.market.getName() + " is not in orbit around a planet.";
        }

        if(orbitFocus.getMarket() == null || orbitFocus.getMarket().getPlanetEntity() == null || !boggledTools.getPlanetType(orbitFocus.getMarket().getPlanetEntity()).equals("gas_giant"))
        {
            return "Only gas giants can undergo Ouyang optimization.";
        }

        // Can't already have extreme weather
        MarketAPI focusMarket = getFocusMarket();
        if(focusMarket.hasCondition("extreme_weather"))
        {
            return getFocusMarket().getName() + " already has extreme weather - making it worse won't increase volatiles availability.";
        }

        // Can't already have maxed out resources
        if(focusMarket.hasCondition("organics_plentiful"))
        {
            return getFocusMarket().getName() + " is already extremely rich in volatiles. An Ouyang optimization would not yield any improvement.";
        }

        return super.getUnavailableReason();
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        daysWithoutShortage = 0;
        lastDayChecked = 0;

        super.notifyBeingRemoved(mode, forUpgrade);
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();

        //Inserts optimization status after description
        if(this.marketSuitableForOptimizer() && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            //200 days; divide daysWithoutShortage by 2 to get the percent
            int percentComplete = daysWithoutShortage / 2;

            //Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
            if(percentComplete > 99)
            {
                percentComplete = 99;
            }

            tooltip.addPara("Ouyang optimization is approximately %s complete on " + getFocusMarket().getName() + ".", opad, highlight, new String[]{percentComplete + "%"});
        }

        // Tell the player they can remove it
        if(!this.marketSuitableForOptimizer() && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Further Ouyang optimization would yield no improvements on " + getFocusMarket().getName() + ". The Ouyang Optimizer can now be deconstructed without any risk of regression.", opad);
        }

        if(this.isDisrupted() && this.marketSuitableForOptimizer() && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            Color bad = Misc.getNegativeHighlightColor();
            tooltip.addPara("Progress is stalled while the Ouyang optimizer is disrupted.", bad, opad);
        }
    }

    @Override
    public float getPatherInterest() { return super.getPatherInterest() + 2.0f; }

    @Override
    public boolean canImprove() { return false; }

    @Override
    public boolean canInstallAICores() {
        return false;
    }
}

