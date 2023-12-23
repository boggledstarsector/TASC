package data.campaign.econ.industries;

import java.awt.*;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

public class Boggled_Ouyang_Optimizer extends BaseIndustry implements BoggledCommonIndustryInterface
{
    @Override
    public boolean canBeDisrupted()
    {
        return true;
    }

    private int daysWithoutShortage = 0;
    private int lastDayChecked = 0;
    public static int requiredDaysToOptimize = 200;

    private static BoggledCommonIndustry commonIndustry;

    public static ArrayList<String> conditionsAddedOnCompletion;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        commonIndustry = new BoggledCommonIndustry(data, "Ouyang Optimizer");

        requiredDaysToOptimize = data.getInt("duration");

        conditionsAddedOnCompletion = new ArrayList<>(Arrays.asList(data.getString("conditions_added_on_completion").split(boggledTools.csvOptionSeparator)));
    }

    @Override
    public LinkedHashMap<String, String> getTokenReplacements() {
        LinkedHashMap<String, String> tokenReplacements = new LinkedHashMap<>();
        tokenReplacements.put("$focusMarket", commonIndustry.getFocusMarketOrMarket(getMarket()).getName());
        return tokenReplacements;
    }

    @Override
    public boolean isAvailableToBuild() {
        return commonIndustry.isAvailableToBuild(getMarket());
    }

    @Override
    public boolean showWhenUnavailable() {
        return commonIndustry.showWhenUnavailable(getMarket());
    }

    @Override
    public String getUnavailableReason() {
        return commonIndustry.getUnavailableReason(getMarket(), getTokenReplacements());
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        if(commonIndustry.marketSuitableBoth(getMarket()) && this.isFunctional())
        {
            CampaignClockAPI clock = Global.getSector().getClock();

            if(clock.getDay() != lastDayChecked)
            {
                daysWithoutShortage++;
                lastDayChecked = clock.getDay();

                if(daysWithoutShortage >= requiredDaysToOptimize)
                {
                    boggledTools.showProjectCompleteIntelMessage("Ouyang Optimization", commonIndustry.getFocusMarketOrMarket(getMarket()).getName(), market);

                    boggledTools.incrementVolatilesForOuyangOptimization(commonIndustry.getFocusMarketOrMarket(getMarket()));

                    for (String conditionAddedOnCompletion : conditionsAddedOnCompletion) {
                        boggledTools.addCondition(commonIndustry.getFocusMarketOrMarket(getMarket()), conditionAddedOnCompletion);
                    }

                    boggledTools.surveyAll(commonIndustry.getFocusMarketOrMarket(getMarket()));
                    boggledTools.refreshSupplyAndDemand(commonIndustry.getFocusMarketOrMarket(getMarket()));
                    boggledTools.refreshAquacultureAndFarming(commonIndustry.getFocusMarketOrMarket(getMarket()));
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
        if(commonIndustry.marketSuitableBoth(getMarket()) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            int percentComplete = (int)((float)daysWithoutShortage / requiredDaysToOptimize) * 100;

            //Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
            percentComplete = Math.min(percentComplete, 99);

            tooltip.addPara("Ouyang optimization is approximately %s complete on " + commonIndustry.getFocusMarketOrMarket(getMarket()).getName() + ".", opad, highlight, percentComplete + "%");
        }

        // Tell the player they can remove it
        if(!commonIndustry.marketSuitableBoth(getMarket()) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Further Ouyang optimization would yield no improvements on " + commonIndustry.getFocusMarketOrMarket(getMarket()).getName() + ". The Ouyang Optimizer can now be deconstructed without any risk of regression.", opad);
        }

        if(this.isDisrupted() && commonIndustry.marketSuitableBoth(getMarket()) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
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

