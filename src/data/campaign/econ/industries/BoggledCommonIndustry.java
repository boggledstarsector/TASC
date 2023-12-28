package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.campaign.econ.boggledTools;
import data.scripts.BoggledTerraformingProject;
import kotlin.Triple;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import static data.campaign.econ.boggledTools.getPlanetType;

public class BoggledCommonIndustry<T> {

    private static final String testString = "T";

    private String industry;
//    private ArrayList<Pair<String, Integer> commodityDemands;

    public ArrayList<Triple<BoggledTerraformingProject, String, String>> projects;

    private boolean building = false;
    private boolean built = false;
    public ArrayList<Integer> lastDayChecked;
    public ArrayList<Integer> daysWithoutShortage;

    BoggledCommonIndustry(JSONObject data) throws JSONException {
        this.industry = data.getString("tooltip");

        String[] projects = data.getString("projects").split(boggledTools.csvOptionSeparator);

        this.projects = new ArrayList<>();
        for (String project : projects) {
            String[] projectWithTooltips = project.split(boggledTools.csvSubOptionSeparator);
            BoggledTerraformingProject p = boggledTools.getProject(projectWithTooltips[0]);
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
    }

    BoggledCommonIndustry(BoggledCommonIndustry that) {
        this.industry = that.industry;
        this.projects = that.projects;
        lastDayChecked = new ArrayList<>(Collections.nCopies(projects.size(), 0));
        daysWithoutShortage = new ArrayList<>(Collections.nCopies(projects.size(), 0));
    }

    BoggledCommonIndustry() {

    }

    public void overridesFromJSON(JSONObject data) throws JSONException {

    }

    public void advance(float amount, BaseIndustry industry) {
        if (!built) {
            return;
        }

        if (industry.isDisrupted() || !marketSuitableBoth(industry.getMarket())) {
            return;
        }

        CampaignClockAPI clock = Global.getSector().getClock();
        for (int i = 0; i < projects.size(); ++i) {
            if (clock.getDay() == lastDayChecked.get(i)) {
                continue;
            }

            BoggledTerraformingProject project = projects.get(i).component1();
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

    public int getDaysRemaining(int projectIndex, BaseIndustry industry) {
        return projects.get(projectIndex).component1().getModifiedProjectDuration(getFocusMarketOrMarket(industry.getMarket())) - daysWithoutShortage.get(projectIndex);
    }

    public void startBuilding(BaseIndustry industry) {
        building = true;
        built = false;
    }

    public void finishBuildingOrUpgrading(BaseIndustry industry) {
        building = false;
        built = true;
    }

    public boolean isBuilding(BaseIndustry industry) {
        if (building) {
            return true;
        }
        if (!built) {
            // Stupid as hell but needs to be here for the industry to work same as vanilla structures
            return false;
        }
        for (int i = 0; i < projects.size(); ++i) {
            if (getDaysRemaining(i, industry) > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isUpgrading(BaseIndustry industry) {
        if (!built) {
            return false;
        }
        for (int i = 0; i < projects.size(); ++i) {
            if (getDaysRemaining(i, industry) > 0) {
                return true;
            }
        }
        return false;
    }

    public float getBuildOrUpgradeProgress(BaseIndustry industry) {
        if (industry.isDisrupted()) {
            return 0.0f;
        } else if (building || !built) {
            return Math.min(1.0f, industry.getBuildProgress() / industry.getBuildTime());
        }

        float progress = 0f;
        for (int i = 0; i < projects.size(); ++i) {
            progress = Math.max(getPercentComplete(i, industry) / 100f, progress);
        }
        return progress;
    }

    public String getBuildOrUpgradeDaysText(BaseIndustry industry) {
        int daysRemain;
        if (industry.isDisrupted()) {
            daysRemain = (int)(industry.getDisruptedDays());
        } else if (building || !built) {
            daysRemain = (int)(industry.getBuildTime() - industry.getBuildProgress());
        } else {
            daysRemain = Integer.MAX_VALUE;
            for (int i = 0; i < projects.size(); ++i) {
                daysRemain = Math.min(getDaysRemaining(i, industry), daysRemain);
            }
        }
        String dayOrDays = daysRemain == 1 ? "day" : "days";
        return daysRemain + " " + dayOrDays;
    }

    public String getBuildOrUpgradeProgressText(BaseIndustry industry) {
        String prefix;
        if (industry.isDisrupted()) {
            prefix = "Disrupted";
        } else if (building || !built) {
            prefix = "Building";
        } else {
            prefix = this.industry;
        }
        return prefix + ": " + getBuildOrUpgradeDaysText(industry) + " left";
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

    private boolean marketSuitableVisible(MarketAPI market) {
        boolean anyProjectValid = false;
        for (Triple<BoggledTerraformingProject, String, String> project : projects) {
            anyProjectValid = anyProjectValid || project.component1().requirementsMet(market);
        }
        return anyProjectValid;
    }

    private boolean marketSuitableHidden(MarketAPI market) {
        boolean anyProjectValid = false;
        for (Triple<BoggledTerraformingProject, String, String> project : projects) {
            anyProjectValid = anyProjectValid || project.component1().requirementsHiddenMet(market);
        }
        return anyProjectValid;
    }

    public boolean marketSuitableBoth(MarketAPI market) {
        return marketSuitableHidden(market) && marketSuitableVisible(market);
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
            for (Triple<BoggledTerraformingProject, String, String> project : projects) {
                if (!project.component1().isEnabled()) {
                    return false;
                }
            }

            boolean noneMet = true;
            for (Triple<BoggledTerraformingProject, String, String> project : projects) {
                if (project.component1().requirementsMet(market)) {
                    noneMet = false;
                    break;
                }
            }
            if (noneMet) {
                return false;
            }
        }

        return marketSuitableVisible(market) && marketSuitableHidden(market);
    }

    public boolean showWhenUnavailable(MarketAPI market) {
        if (!projects.isEmpty()) {
            for (Triple<BoggledTerraformingProject, String, String> project : projects) {
                if (!project.component1().isEnabled()) {
                    return false;
                }
            }

            boolean allHidden = true;
            for (Triple<BoggledTerraformingProject, String, String> project : projects) {
                if (project.component1().requirementsHiddenMet(market)) {
                    allHidden = false;
                    break;
                }
            }
            if (allHidden) {
                return false;
            }
        }

        return marketSuitableHidden(market);
    }

    private LinkedHashMap<String, String> getTokenReplacements(MarketAPI market) {
        LinkedHashMap<String, String> ret = new LinkedHashMap<>();
        ret.put("$market", market.getName());
        ret.put("$focusMarket", getFocusMarketOrMarket(market).getName());
        ret.put("$planetTypeName", getPlanetType(market.getPlanetEntity()).getPlanetTypeName());
        ret.put("$system", market.getStarSystem().getName());
        return ret;
    }

    public String getUnavailableReason(MarketAPI market) {
        return boggledTools.getUnavailableReason(projects, industry, market, getTokenReplacements(market));
    }
}
