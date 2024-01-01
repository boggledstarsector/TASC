package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import data.scripts.BoggledCommoditySupplyDemand;
import data.scripts.BoggledTerraformingProject;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class BoggledCommonIndustry {
    /*
    This class cannot be made into a base class of any of the Boggled industries because Remnant Station gets in the way
     */
    private final String industryTooltip;

    public ArrayList<BoggledTerraformingProject> projects;
    private final ArrayList<BoggledCommoditySupplyDemand.CommoditySupplyAndDemand> commoditySupplyAndDemands;

    private boolean building = false;
    private boolean built = false;
    public ArrayList<Integer> lastDayChecked;
    public ArrayList<Integer> daysWithoutShortage;

    public BoggledCommonIndustry(String industryTooltip, ArrayList<BoggledTerraformingProject> projects, ArrayList<BoggledCommoditySupplyDemand.CommoditySupplyAndDemand> commoditySupplyAndDemands) {
        this.industryTooltip = industryTooltip;
        this.projects = projects;
        this.commoditySupplyAndDemands = commoditySupplyAndDemands;
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

            BoggledTerraformingProject project = projects.get(i);
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

            project.finishProject(getFocusMarketOrMarket(industry.getMarket()));
            this.daysWithoutShortage.set(i, 0);
        }
    }

    public int getPercentComplete(int projectIndex, MarketAPI market) {
        return (int) Math.min(99, ((float)daysWithoutShortage.get(projectIndex) / projects.get(projectIndex).getModifiedProjectDuration(getFocusMarketOrMarket(market))) * 100);
    }

    public int getDaysRemaining(int projectIndex, BaseIndustry industry) {
        return projects.get(projectIndex).getModifiedProjectDuration(getFocusMarketOrMarket(industry.getMarket())) - daysWithoutShortage.get(projectIndex);
    }

    public void tooltipIncomplete(BaseIndustry industry, TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode, String format, float pad, Color hl, String... highlights) {
        if (!(marketSuitableBoth(industry.getMarket()) && mode != Industry.IndustryTooltipMode.ADD_INDUSTRY && mode != Industry.IndustryTooltipMode.QUEUED)) {
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
        for (BoggledTerraformingProject project : projects) {
            anyProjectValid = anyProjectValid || project.requirementsMet(market);
        }
        return anyProjectValid;
    }

    private boolean marketSuitableHidden(MarketAPI market) {
        boolean anyProjectValid = false;
        for (BoggledTerraformingProject project : projects) {
            anyProjectValid = anyProjectValid || project.requirementsHiddenMet(market);
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
            if (projects.get(i).requirementsMet(industry.getMarket()) && getDaysRemaining(i, industry) > 0) {
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
            if (projects.get(i).requirementsMet(industry.getMarket()) && getDaysRemaining(i, industry) > 0) {
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
            progress = Math.max(getPercentComplete(i, industry.getMarket()) / 100f, progress);
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
            for (BoggledTerraformingProject project : projects) {
                if (!project.isEnabled()) {
                    return false;
                }
            }

            boolean noneMet = true;
            for (BoggledTerraformingProject project : projects) {
                if (project.requirementsMet(industry.getMarket())) {
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
            for (BoggledTerraformingProject project : projects) {
                if (!project.isEnabled()) {
                    return false;
                }
            }

            boolean allHidden = true;
            for (BoggledTerraformingProject project : projects) {
                if (project.requirementsHiddenMet(industry.getMarket())) {
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
        return boggledTools.getUnavailableReason(projects, industryTooltip, industry.getMarket(), boggledTools.getTokenReplacements(industry.getMarket()));
    }

    public void apply(BaseIndustry industry) {
        for (BoggledCommoditySupplyDemand.CommoditySupplyAndDemand commoditySupplyAndDemand : commoditySupplyAndDemands) {
            commoditySupplyAndDemand.applySupplyDemand(industry);
        }
    }

    public void addRightAfterDescriptionSection(BaseIndustry industry, TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode) {
        float pad = 10.0f;
//        for (BoggledTerraformingProject project : projects) {
        for (int i = 0; i < projects.size(); ++i) {
            BoggledTerraformingProject project = projects.get(i);
            if (project.requirementsMet(industry.getMarket())) {
                LinkedHashMap<String, String> tokenReplacements = getTokenReplacements(industry.getMarket(), i);
                String[] highlights = project.getIncompleteMessageHighlights(tokenReplacements);
                addFormatTokenReplacement(tokenReplacements);
                String incompleteMessage = boggledTools.doTokenReplacement(project.getIncompleteMessage(), tokenReplacements);
                tooltipIncomplete(industry, tooltip, mode, incompleteMessage, pad, Misc.getHighlightColor(), highlights);
                tooltipDisrupted(industry, tooltip, mode, "Here's a message", pad, Misc.getNegativeHighlightColor());
            }
        }
    }

    public boolean hasPostDemandSection(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        for (BoggledCommoditySupplyDemand.CommoditySupplyAndDemand commoditySupplyAndDemand : commoditySupplyAndDemands) {
            if (commoditySupplyAndDemand.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public void addPostDemandSection(BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        for (BoggledCommoditySupplyDemand.CommoditySupplyAndDemand commoditySupplyAndDemand : commoditySupplyAndDemands) {
            if (commoditySupplyAndDemand.isEnabled()) {
                commoditySupplyAndDemand.addPostDemandSection(industryTooltip, industry, tooltip, hasDemand, mode);
            }
        }
    }

    private LinkedHashMap<String, String> getTokenReplacements(MarketAPI market, int projectIndex) {
        LinkedHashMap<String, String> ret = boggledTools.getTokenReplacements(market);
        ret.put("$percentComplete", Integer.toString(getPercentComplete(projectIndex, market)));
        return ret;
    }

    private void addFormatTokenReplacement(LinkedHashMap<String, String> tokenReplacements) {
        tokenReplacements.put("%", "%%");
    }
}
