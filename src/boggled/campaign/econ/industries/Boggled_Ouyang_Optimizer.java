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
import boggled.campaign.econ.StringHelper;
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
    private final int requiredDaysToOptimize = boggledTools.getIntSetting(boggledTools.BoggledSettings.boggledOuyangOptimizerProjectTime);

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

        if(orbitFocus.getMarket() == null || orbitFocus.getMarket().getPlanetEntity() == null)
        {
            return false;
        }

        PlanetAPI planet = orbitFocus.getMarket().getPlanetEntity();
        String planetType = boggledTools.getTascPlanetType(planet);
        if(!planetType.equals(boggledTools.TascPlanetTypes.gasGiantPlanetId))
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
                        MessageIntel intel = new MessageIntel(StringHelper.getString(StringHelper.INDUSTRIES, "ouyang_intel_title", StringHelper.tokens("market", getFocusMarket().getName())), Misc.getBasePlayerColor());
                        intel.addLine(StringHelper.getString(StringHelper.INDUSTRIES, "ouyang_intel_completed"));
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
        super.apply(false);
        super.applyIncomeAndUpkeep(3);
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        // Exit immediately if we're in the AotD research menu
        if(boggledTools.isMarketAotdFakeForResearchMenu(this.market))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") || !boggledTools.getBooleanSetting("boggledOuyangOptimizerEnabled")) {
            return false;
        }

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

        if(orbitFocus.getMarket() == null || orbitFocus.getMarket().getPlanetEntity() == null)
        {
            return false;
        }

        PlanetAPI planet = orbitFocus.getMarket().getPlanetEntity();
        String planetType = boggledTools.getTascPlanetType(planet);
        if(!planetType.equals(boggledTools.TascPlanetTypes.gasGiantPlanetId))
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

        // Check research requirement
        if(!boggledTools.isBuildingResearchComplete(this.getId()))
        {
            return false;
        }

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.isBuildingResearchComplete(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") || !boggledTools.getBooleanSetting("boggledOuyangOptimizerEnabled")) {
            return false;
        }

        // Only buildable on stations
        if(!boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        // Station needs to obit a gas giant planet
        SectorEntityToken orbitFocus = getOrbitFocus();
        if(orbitFocus == null)
        {
            return super.showWhenUnavailable();
        }

        if(orbitFocus.getMarket() == null || orbitFocus.getMarket().getPlanetEntity() == null)
        {
            return super.showWhenUnavailable();
        }

        PlanetAPI planet = orbitFocus.getMarket().getPlanetEntity();
        String planetType = boggledTools.getTascPlanetType(planet);
        if(!planetType.equals(boggledTools.TascPlanetTypes.gasGiantPlanetId))
        {
            return super.showWhenUnavailable();
        }

        // Can't already have extreme weather
        MarketAPI focusMarket = getFocusMarket();
        if(focusMarket.hasCondition("extreme_weather"))
        {
            return super.showWhenUnavailable();
        }

        // Can't already have maxed out volatiles
        if(focusMarket.hasCondition("organics_plentiful"))
        {
            return super.showWhenUnavailable();
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
            return StringHelper.getString(StringHelper.INDUSTRIES, "ouyang_not_orbiting", StringHelper.tokens("market", this.market.getName()));
        }

        if(orbitFocus.getMarket() == null || orbitFocus.getMarket().getPlanetEntity() == null)
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "ouyang_not_orbiting", StringHelper.tokens("market", this.market.getName()));
        }

        PlanetAPI planet = orbitFocus.getMarket().getPlanetEntity();
        String planetType = boggledTools.getTascPlanetType(planet);
        if(!planetType.equals(boggledTools.TascPlanetTypes.gasGiantPlanetId))
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "ouyang_only_gas_giants");
        }

        // Can't already have extreme weather
        MarketAPI focusMarket = getFocusMarket();
        if(focusMarket.hasCondition("extreme_weather"))
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "ouyang_extreme_weather", StringHelper.tokens("market", getFocusMarket().getName()));
        }

        // Can't already have maxed out volatiles
        if(focusMarket.hasCondition("organics_plentiful"))
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "ouyang_volatiles_maxed", StringHelper.tokens("market", getFocusMarket().getName()));
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
        // Exit immediately if we're in the AotD research menu
        if(boggledTools.isMarketAotdFakeForResearchMenu(this.market))
        {
            return;
        }

        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();

        //Inserts optimization status after description
        if(this.marketSuitableForOptimizer() && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            int percentComplete = (int) (((float) this.daysWithoutShortage / (float) this.requiredDaysToOptimize) * 100F);

            //Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
            if(percentComplete > 99)
            {
                percentComplete = 99;
            }

            String percent = percentComplete + "%";
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "ouyang_progress", StringHelper.tokens("percent", percent, "market", getFocusMarket().getName()));
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), highlight, percent);
        }

        // Tell the player they can remove it
        if(!this.marketSuitableForOptimizer() && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "ouyang_finished", StringHelper.tokens("market", getFocusMarket().getName())), opad);
        }

        if(this.isDisrupted() && this.marketSuitableForOptimizer() && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            Color bad = Misc.getNegativeHighlightColor();
            tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "ouyang_stalled"), bad, opad);
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

