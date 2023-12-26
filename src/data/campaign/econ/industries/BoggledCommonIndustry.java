package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import kotlin.Triple;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class BoggledCommonIndustry {

    private String industry;

    private ArrayList<Pair<boggledTools.TerraformingRequirements, String>> requirementsSuitable;
    private ArrayList<Pair<boggledTools.TerraformingRequirements, String>> requirementsSuitableHidden;

//    private ArrayList<Pair<String, Integer> commodityDemands;

    public ArrayList<Triple<boggledTools.TerraformingProject, String, String>> projects;

    public ArrayList<Integer> lastDayChecked;
    public ArrayList<Integer> daysWithoutShortage;

    BoggledCommonIndustry(JSONObject data, String industry) throws JSONException {
        this.industry = industry;

        String[] projects = data.getString("projects").split(boggledTools.csvOptionSeparator);

        this.projects = new ArrayList<>();
        for (String project : projects) {
            String[] projectWithTooltips = project.split(boggledTools.csvSubOptionSeparator);
            boggledTools.TerraformingProject p = boggledTools.getProject(projectWithTooltips[0]);
            if (p != null) {
                String intelTooltip = "";
                String intelCompleteMessage = "";
                if (projectWithTooltips.length > 1) {
                    intelTooltip = projectWithTooltips[1];
                }
                if (projectWithTooltips.length > 2) {
                    intelCompleteMessage = projectWithTooltips[2];
                }

                this.projects.add(new Triple<>(p, intelTooltip, intelCompleteMessage));
            }
        }
        this.lastDayChecked = new ArrayList<>(Collections.nCopies(this.projects.size(), 0));
        this.daysWithoutShortage = new ArrayList<>(Collections.nCopies(this.projects.size(), 0));

        this.requirementsSuitable = boggledTools.getRequirementsSuitable(data, "requirements", industry);
        this.requirementsSuitableHidden = boggledTools.getRequirementsSuitable(data, "requirements_hidden", industry);

//        this.commodityDemands = new ArrayList<>();
//        String[] commodityDemands = data.getString("commodity_demand").split(boggledTools.csvOptionSeparator);
//        for (String commodityDemand : commodityDemands) {
//            String[] commodityDemandAndCount = commodityDemand.split(boggledTools.csvSubOptionSeparator);
//            if (commodityDemandAndCount.length == 1) {
//                this.commodityDemands.add(new Pair<>(commodityDemandAndCount[0], -1));
//            } else {
//                this.commodityDemands.add(new Pair<>(commodityDemandAndCount[0], Integer.parseInt(commodityDemandAndCount[1])));
//            }
//        }
    }

    public void overridesFromJSON(JSONObject data) throws JSONException {

    }

    public void advance(float amount, BaseIndustry industry) {
        if (!(industry.isFunctional() && marketSuitableBoth(industry.getMarket()))) {
            return;
        }

        CampaignClockAPI clock = Global.getSector().getClock();
        for (int i = 0; i < projects.size(); ++i) {
            if (clock.getDay() == lastDayChecked.get(i)) {
                continue;
            }

            if (!(industry.isFunctional() && marketSuitableBoth(industry.getMarket()))) {
                continue;
            }

            boggledTools.TerraformingProject project = projects.get(i).component1();
            if (!project.requirementsMet(industry.getMarket())) {
                continue;
            }

            int daysWithoutShortage = this.daysWithoutShortage.get(i) + 1;
            this.daysWithoutShortage.set(i, daysWithoutShortage);
            this.lastDayChecked.set(i, clock.getDay());

            if (daysWithoutShortage < project.getModifiedProjectDuration(getFocusMarketOrMarket(industry.getMarket()))) {
                continue;
            }

            project.finishProject(getFocusMarketOrMarket(industry.getMarket()), projects.get(i).component2(), projects.get(i).component3());
        }
    }

    public int getPercentComplete(int projectIndex, BaseIndustry industry) {
        return (int) Math.min(99, ((float)daysWithoutShortage.get(projectIndex) / projects.get(projectIndex).component1().getModifiedProjectDuration(getFocusMarketOrMarket(industry.getMarket()))) * 100);
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

    public static MarketAPI getFocusMarketOrMarket(MarketAPI market) {
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
        if (!projects.isEmpty()) {
            for (Triple<boggledTools.TerraformingProject, String, String> project : projects) {
                if (!project.component1().isEnabled()) {
                    return false;
                }
            }

            boolean noneMet = true;
            for (Triple<boggledTools.TerraformingProject, String, String> project : projects) {
                if (project.component1().requirementsMet(market)) {
                    noneMet = false;
                    break;
                }
            }
            if (noneMet) {
                return false;
            }
        }

        return marketSuitable(market, requirementsSuitable) && marketSuitable(market, requirementsSuitableHidden);
    }

    public boolean showWhenUnavailable(MarketAPI market) {
        if (!projects.isEmpty()) {
            for (Triple<boggledTools.TerraformingProject, String, String> project : projects) {
                if (!project.component1().isEnabled()) {
                    return false;
                }
            }

            boolean allHidden = true;
            for (Triple<boggledTools.TerraformingProject, String, String> project : projects) {
                if (project.component1().requirementsHiddenMet(market)) {
                    allHidden = false;
                    break;
                }
            }
            if (allHidden) {
                return false;
            }
        }

        return marketSuitable(market, requirementsSuitableHidden);
    }

    public String getUnavailableReason(MarketAPI market, LinkedHashMap<String, String> tokenReplacements) {
        return boggledTools.getUnavailableReason(requirementsSuitable, requirementsSuitableHidden, industry, market, tokenReplacements, projects);
    }
}
