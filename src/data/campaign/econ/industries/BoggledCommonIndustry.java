package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.campaign.econ.boggledTools;
import data.scripts.BoggledCommodityDemand;
import data.scripts.BoggledTerraformingProject;
import kotlin.Triple;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import static data.campaign.econ.boggledTools.getPlanetType;

public class BoggledCommonIndustry {
    /*
    This class cannot be made into a base class of any of the Boggled industries because Remnant Station gets in the way
     */
    private String industryTooltip;

    public ArrayList<Triple<BoggledTerraformingProject, String, String>> projects;
    private ArrayList<BoggledCommodityDemand.CommodityDemand> commodityDemands;

    private boolean building = false;
    private boolean built = false;
    public ArrayList<Integer> lastDayChecked;
    public ArrayList<Integer> daysWithoutShortage;

    public BoggledCommonIndustry(String industryTooltip, ArrayList<Triple<BoggledTerraformingProject, String, String>> projects, ArrayList<BoggledCommodityDemand.CommodityDemand> commodityDemands) {
        this.industryTooltip = industryTooltip;
        this.projects = projects;
        this.commodityDemands = commodityDemands;
        this.lastDayChecked = new ArrayList<>(Collections.nCopies(projects.size(), 0));
        this.daysWithoutShortage = new ArrayList<>(Collections.nCopies(projects.size(), 0));
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
            int step = 1;
            if (!project.requirementsMet(industry.getMarket())) {
                step = -1;
            }

            int daysWithoutShortage = this.daysWithoutShortage.get(i) + step;
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
    These are the main reason for this class
    Throw an instance of this on a type and just delegate to it for handling these BaseIndustry functions
     */
    public void startBuilding(BaseIndustry industry) {
        building = true;
        built = false;
    }

    public void startUpgrading(BaseIndustry industry) {

    }

    public void buildingFinished(BaseIndustry industry) {
        building = false;
        built = true;
    }

    public void upgradeFinished(BaseIndustry industry, Industry previous) {

    }

    public void finishBuildingOrUpgrading(BaseIndustry industry) {
//        building = false;
//        built = true;
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
            prefix = this.industryTooltip;
        }
        return prefix + ": " + getBuildOrUpgradeDaysText(industry) + " left";
    }

    public boolean isAvailableToBuild(BaseIndustry industry) {
        if (!projects.isEmpty()) {
            for (Triple<BoggledTerraformingProject, String, String> project : projects) {
                if (!project.component1().isEnabled()) {
                    return false;
                }
            }

            boolean noneMet = true;
            for (Triple<BoggledTerraformingProject, String, String> project : projects) {
                if (project.component1().requirementsMet(industry.getMarket())) {
                    noneMet = false;
                    break;
                }
            }
            if (noneMet) {
                return false;
            }
        }

        return marketSuitableVisible(industry.getMarket()) && marketSuitableHidden(industry.getMarket());
    }

    public boolean showWhenUnavailable(BaseIndustry industry) {
        if (!projects.isEmpty()) {
            for (Triple<BoggledTerraformingProject, String, String> project : projects) {
                if (!project.component1().isEnabled()) {
                    return false;
                }
            }

            boolean allHidden = true;
            for (Triple<BoggledTerraformingProject, String, String> project : projects) {
                if (project.component1().requirementsHiddenMet(industry.getMarket())) {
                    allHidden = false;
                    break;
                }
            }
            if (allHidden) {
                return false;
            }
        }

        return marketSuitableHidden(industry.getMarket());
    }

    public String getUnavailableReason(BaseIndustry industry) {
        return boggledTools.getUnavailableReason(projects, industryTooltip, industry.getMarket(), getTokenReplacements(industry.getMarket()));
    }

    public void apply(BaseIndustry industry) {
        for (BoggledCommodityDemand.CommodityDemand commodityDemand : commodityDemands) {
            commodityDemand.applyDemand(industry);
        }
    }

    public boolean hasPostDemandSection(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        for (BoggledCommodityDemand.CommodityDemand commodityDemand : commodityDemands) {
            if (commodityDemand.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public void addPostDemandSection(BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        for (BoggledCommodityDemand.CommodityDemand commodityDemand : commodityDemands) {
            if (commodityDemand.isEnabled()) {
                commodityDemand.addPostDemandSection(industryTooltip, industry, tooltip, hasDemand, mode);
            }
        }
    }

    private LinkedHashMap<String, String> getTokenReplacements(MarketAPI market) {
        LinkedHashMap<String, String> ret = new LinkedHashMap<>();
        ret.put("$market", market.getName());
        ret.put("$focusMarket", getFocusMarketOrMarket(market).getName());
        ret.put("$planetTypeName", getPlanetType(market.getPlanetEntity()).getPlanetTypeName());
        ret.put("$system", market.getStarSystem().getName());
        return ret;
    }
}
