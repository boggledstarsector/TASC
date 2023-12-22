package data.campaign.econ.industries;

import java.awt.*;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import data.campaign.econ.boggledTools;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

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
    public static ArrayList<boggledTools.TerraformingRequirements> requirementsSuitable;
    public static ArrayList<String> conditionsAddedOnCompletion;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        Logger log = Global.getLogger(Boggled_Ouyang_Optimizer.class);
        requiredDaysToOptimize = data.getInt("duration");

        requirementsSuitable = new ArrayList<>();

        String[] requirementsSuitableStrings = data.getString("requirement_suitable").split(boggledTools.csvOptionSeparator);
        for (String requirementsSuitableString : requirementsSuitableStrings) {
            boggledTools.TerraformingRequirements requirementSuitable = boggledTools.getTerraformingRequirements().get(requirementsSuitableString);
            if (requirementSuitable == null) {
                log.error("Industry Ouyang Optimizer has invalid requirement " + requirementsSuitableString);
                continue;
            }
            requirementsSuitable.add(requirementSuitable);
        }

        conditionsAddedOnCompletion = new ArrayList<>(Arrays.asList(data.getString("conditions_added_on_completion").split(boggledTools.csvOptionSeparator)));
    }

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
        for (boggledTools.TerraformingRequirements terraformingRequirements : requirementsSuitable) {
            if (!terraformingRequirements.checkRequirement(market)) {
                return false;
            }
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
                    boggledTools.showProjectCompleteIntelMessage("Ouyang Optimization", getFocusMarket().getName(), market);

                    boggledTools.incrementVolatilesForOuyangOptimization(getFocusMarket());

                    for (String conditionAddedOnCompletion : conditionsAddedOnCompletion) {
                        boggledTools.addCondition(getFocusMarket(), conditionAddedOnCompletion);
                    }

                    boggledTools.surveyAll(getFocusMarket());
                    boggledTools.refreshSupplyAndDemand(getFocusMarket());
                    boggledTools.refreshAquacultureAndFarming(getFocusMarket());
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
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled) && boggledTools.getBooleanSetting("boggledOuyangOptimizerEnabled"))
        {
            if(this.marketSuitableForOptimizer())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.ouyangOptimizerEnabled) && boggledTools.marketIsStation(this.market);
    }

    @Override
    public String getUnavailableReason()
    {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.ouyangOptimizerEnabled))
        {
            // Station needs to obit a gas giant planet
            SectorEntityToken orbitFocus = getOrbitFocus();
            if (orbitFocus == null)
            {
                return this.market.getName() + " is not in orbit around a planet.";
            }

            if (orbitFocus.getMarket() == null || orbitFocus.getMarket().getPlanetEntity() == null || !boggledTools.getPlanetType(orbitFocus.getMarket().getPlanetEntity()).getPlanetId().equals(boggledTools.gasGiantPlanetId))
            {
                return "Only gas giants can undergo Ouyang optimization.";
            }

            // Can't already have extreme weather
            MarketAPI focusMarket = getFocusMarket();
            if (focusMarket.hasCondition(Conditions.EXTREME_WEATHER))
            {
                return getFocusMarket().getName() + " already has extreme weather - making it worse won't increase volatiles availability.";
            }

            // Can't already have maxed out resources
            if (focusMarket.hasCondition(Conditions.VOLATILES_PLENTIFUL))
            {
                return getFocusMarket().getName() + " is already extremely rich in volatiles. An Ouyang optimization would not yield any improvement.";
            }
        }
        return "Error in getUnavailableReason() in Ouyang Optimizer. Please tell Boggled about this on the forums.";
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
            int percentComplete = (int)((float)daysWithoutShortage / requiredDaysToOptimize) * 100;

            //Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
            percentComplete = Math.min(percentComplete, 99);

            tooltip.addPara("Ouyang optimization is approximately %s complete on " + getFocusMarket().getName() + ".", opad, highlight, percentComplete + "%");
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

