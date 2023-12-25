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

//    private String[] requiredOptions;
    private ArrayList<Pair<boggledTools.TerraformingRequirements, String>> requirementsSuitable;
    private ArrayList<Pair<boggledTools.TerraformingRequirements, String>> requirementsSuitableHidden;

    private boggledTools.TerraformingProject project;
    private String[][] conditionsAddedOnCompletion;
    private String[][] conditionsRemovedOnCompletion;

    private String[][] parseSubstrings(String[] data, String regex) {
        if (data.length == 1 && data[0].isEmpty()) {
            return new String[0][];
        }
        String[][] ret = new String[data.length][];
        for (int i = 0; i < data.length; ++i) {
            String[] subStrings = data[i].split(regex);
            ret[i] = new String[subStrings.length];
            System.arraycopy(subStrings, 0, ret[i], 0, subStrings.length);
        }
        return ret;
    }

    BoggledCommonIndustry(JSONObject data, String industry) throws JSONException {
        this.industry = industry;
//        this.requiredOptions = data.getString("required_options").split(boggledTools.csvOptionSeparator);

        this.requirementsSuitable = boggledTools.getRequirementsSuitable(data, "requirement_suitable", industry);
        this.requirementsSuitableHidden = boggledTools.getRequirementsSuitable(data, "requirement_suitable_hidden", industry);

//        String[] durationsWithConditions = data.getString("durations_with_conditions").split(boggledTools.csvOptionSeparator);
//        String[] durationModifiers = data.getString("dynamic_project_duration_modifiers").split(boggledTools.csvOptionSeparator);
//        this.durations = new DurationWithConditions(durationsWithConditions, durationModifiers);

//        String[] conditionsAddedOnCompletion = data.getString("conditions_added_on_completion").split(boggledTools.csvOptionSeparator);
//        String[] conditionsRemovedOnCompletion = data.getString("conditions_removed_on_completion").split(boggledTools.csvOptionSeparator);

//        this.conditionsAddedOnCompletion = parseSubstrings(conditionsAddedOnCompletion, boggledTools.csvSubOptionSeparator);
//        this.conditionsRemovedOnCompletion = parseSubstrings(conditionsRemovedOnCompletion, boggledTools.csvSubOptionSeparator);
    }

    public void overridesFromJSON(JSONObject data) throws JSONException {

    }

//    public int[] getDurations() {
//        return durations;
//    }

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
        if (!project.isEnabled()) {
            return false;
        }

        return marketSuitable(market, requirementsSuitable) && marketSuitable(market, requirementsSuitableHidden);
    }

    public boolean showWhenUnavailable(MarketAPI market) {
        if (!project.isEnabled()) {
            return false;
        }

        return marketSuitable(market, requirementsSuitableHidden);
    }

    public String getUnavailableReason(MarketAPI market, LinkedHashMap<String, String> tokenReplacements) {
        return boggledTools.getUnavailableReason(requirementsSuitable, requirementsSuitableHidden, industry, market, tokenReplacements, project.getEnableSettings());
    }
}
