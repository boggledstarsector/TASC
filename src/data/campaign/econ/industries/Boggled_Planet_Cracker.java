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

public class Boggled_Planet_Cracker extends BaseIndustry implements BoggledCommonIndustryInterface
{
    @Override
    public boolean canBeDisrupted()
    {
        return true;
    }

    private int daysWithoutShortage = 0;
    private int lastDayChecked = 0;

    private static BoggledCommonIndustry commonIndustry;

    public static ArrayList<String> conditionsAddedOnCompletion;

    public static int requiredDaysToCrack = 200;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        commonIndustry = new BoggledCommonIndustry(data, "Planet Cracker");

        requiredDaysToCrack = commonIndustry.getDurations()[0];

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

                if(daysWithoutShortage >= requiredDaysToCrack)
                {
                    boggledTools.showProjectCompleteIntelMessage("Planet cracking", "Completed", commonIndustry.getFocusMarketOrMarket(getMarket()).getName(), market);

                    boggledTools.incrementOreForPlanetCracking(commonIndustry.getFocusMarketOrMarket(getMarket()));

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

    public void apply()
    {
        super.apply(true);
    }

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

        commonIndustry.tooltipIncomplete(this, tooltip, mode, "Planet cracking is approximately %s complete on " + commonIndustry.getFocusMarketOrMarket(getMarket()).getName() + ".", opad, highlight, commonIndustry.getPercentComplete(daysWithoutShortage, requiredDaysToCrack) + "%");

        commonIndustry.tooltipComplete(this, tooltip, mode, "Further planet cracking operations would serve no purpose on " + commonIndustry.getFocusMarketOrMarket(getMarket()).getName() + ". The Planet Cracker can now be deconstructed without any risk of regression.", opad, highlight);

        commonIndustry.tooltipDisrupted(this, tooltip, mode, "Progress is stalled while the planet cracker is disrupted.", opad, Misc.getNegativeHighlightColor());
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

