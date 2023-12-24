package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class BoggledCommonIndustry {

    private String industry;

    private String[] requiredOptions;
    private ArrayList<Pair<boggledTools.TerraformingRequirements, String>> requirementsSuitable;
    private ArrayList<Pair<boggledTools.TerraformingRequirements, String>> requirementsSuitableHidden;

    private int[] durations;

    private String[] conditionsAddedOnCompletion;
    private String[] conditionsRemovedOnCompletion;

    BoggledCommonIndustry(JSONObject data, String industry) throws JSONException {
        this.industry = industry;
        this.requiredOptions = data.getString("required_options").split(boggledTools.csvOptionSeparator);

        this.requirementsSuitable = boggledTools.getRequirementsSuitable(data, "requirement_suitable", industry);
        this.requirementsSuitableHidden = boggledTools.getRequirementsSuitable(data, "requirement_suitable_hidden", industry);

        String[] durationStrings = data.getString("duration").split(boggledTools.csvOptionSeparator);
        this.durations = new int[durationStrings.length];
        for (int i = 0; i < durationStrings.length; ++i) {
            this.durations[i] = Integer.parseInt(durationStrings[i]);
        }

        this.conditionsAddedOnCompletion = data.getString("conditions_added_on_completion").split(boggledTools.csvOptionSeparator);
        this.conditionsRemovedOnCompletion = data.getString("conditions_removed_on_completion").split(boggledTools.csvOptionSeparator);
    }

    public void overridesFromJSON(JSONObject data) throws JSONException {

    }

    public int[] getDurations() {
        return durations;
    }

    public int getPercentComplete(int daysComplete, int daysRequired) {
        return (int) Math.min(99, ((float)daysComplete / daysRequired) * 100);
    }

    public void tooltipIncomplete(BaseIndustry industry, TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode, String format, float pad, Color hl, String... highlights) {
        if (!(marketSuitableBoth(industry.getMarket()) && mode != Industry.IndustryTooltipMode.ADD_INDUSTRY && mode != Industry.IndustryTooltipMode.QUEUED && !industry.isBuilding())) {
            return;
        }
        tooltip.addPara(format, pad, hl, highlights);
    }

    public void tooltipComplete(BaseIndustry industry, TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode, String format, float pad, Color hl, String... highlights) {
        if(!(!marketSuitableBoth(industry.getMarket()) && mode != Industry.IndustryTooltipMode.ADD_INDUSTRY && mode != Industry.IndustryTooltipMode.QUEUED && !industry.isBuilding())) {
            return;
        }
        tooltip.addPara(format, pad, hl, highlights);
    }

    public void tooltipDisrupted(BaseIndustry industry, TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode, String format, float pad, Color hl, String... highlights) {
        if (!(industry.isDisrupted() && marketSuitableBoth(industry.getMarket()) && mode != Industry.IndustryTooltipMode.ADD_INDUSTRY && mode != Industry.IndustryTooltipMode.QUEUED && !industry.isBuilding())) {
            return;
        }
        tooltip.addPara(format, pad, hl, highlights);
    }

    public boolean marketSuitable(MarketAPI market, ArrayList<Pair<boggledTools.TerraformingRequirements, String>> requirements) {
        for (Pair<boggledTools.TerraformingRequirements, String> terraformingRequirements : requirements) {
            if (!terraformingRequirements.one.checkRequirement(market)) {
                return false;
            }
        }
        return true;
    }

    public boolean marketSuitableBoth(MarketAPI market) {
        return marketSuitable(market, requirementsSuitable) && marketSuitable(market, requirementsSuitableHidden);
    }

    public MarketAPI getFocusMarketOrMarket(MarketAPI market) {
        MarketAPI ret = market.getPrimaryEntity().getOrbitFocus().getMarket();
        if (ret == null) {
            return market;
        }
        return ret;
    }

    /*
    These three are the main reason for this class
    Throw an instance of this on a type and just delegate to it for handling these three BaseIndustry functions
     */
    public boolean isAvailableToBuild(MarketAPI market) {
        if (!boggledTools.optionsAllowThis(requiredOptions)) {
            return false;
        }

        return marketSuitable(market, requirementsSuitable) && marketSuitable(market, requirementsSuitableHidden);
    }

    public boolean showWhenUnavailable(MarketAPI market) {
        if (!boggledTools.optionsAllowThis(requiredOptions)) {
            return false;
        }

        return marketSuitable(market, requirementsSuitableHidden);
    }

    public String getUnavailableReason(MarketAPI market, LinkedHashMap<String, String> tokenReplacements) {
        return boggledTools.getUnavailableReason(requirementsSuitable, requirementsSuitableHidden, industry, market, tokenReplacements, requiredOptions);
    }
}
