package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import data.scripts.BoggledCommoditySupplyDemand;
import data.scripts.BoggledTerraformingProject;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BoggledCommonIndustry {
    /*
    This class cannot be made into a base class of any of the Boggled industries because Remnant Station and Cryosanctum gets in the way, may be able to do something else though
     */
    private final String industryId;
    private final String industryTooltip;

    public List<BoggledTerraformingProject.ProjectInstance> projects;

    private List<BoggledCommoditySupplyDemand.CommoditySupply> commoditySupply;
    private List<BoggledCommoditySupplyDemand.CommodityDemand> commodityDemand;

    private List<BoggledCommoditySupplyDemand.IndustryEffect> industryEffects;
    private List<BoggledCommoditySupplyDemand.IndustryEffect> improveEffects;

    private List<BoggledCommoditySupplyDemand.AICoreEffect> aiCoreEffects;

    private boolean functional = true;

    private boolean building = false;
    private boolean built = false;

    public BoggledCommonIndustry(String industryId, String industryTooltip, List<BoggledTerraformingProject> projects, List<BoggledCommoditySupplyDemand.CommoditySupply> commoditySupply, List<BoggledCommoditySupplyDemand.CommodityDemand> commodityDemand, List<BoggledCommoditySupplyDemand.IndustryEffect> industryEffects, List<BoggledCommoditySupplyDemand.IndustryEffect> improveEffects, List<BoggledCommoditySupplyDemand.AICoreEffect> aiCoreEffects) {
        this.industryId = industryId;
        this.industryTooltip = industryTooltip;

        this.projects = new ArrayList<>(projects.size());
        for (BoggledTerraformingProject project : projects) {
            this.projects.add(new BoggledTerraformingProject.ProjectInstance(project));
        }

        this.commoditySupply = commoditySupply;
        this.commodityDemand = commodityDemand;

        this.industryEffects = industryEffects;
        this.improveEffects = improveEffects;
        this.aiCoreEffects = aiCoreEffects;
    }

    protected Object readResolve() {
        Global.getLogger(this.getClass()).info("Doing readResolve for " + industryId + " " + industryTooltip);
        BoggledCommonIndustry that = boggledTools.getIndustryProject(industryId);
        this.projects = that.projects;
        this.commoditySupply = that.commoditySupply;
        this.commodityDemand = that.commodityDemand;
        this.industryEffects = that.industryEffects;
        this.improveEffects = that.improveEffects;
        this.aiCoreEffects = that.aiCoreEffects;
        return this;
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

        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            project.advance(industry.getMarket());
        }
    }

    public int getPercentComplete(int projectIndex, MarketAPI market) {
        return (int) Math.min(99, ((float)projects.get(projectIndex).getDaysCompleted() / projects.get(projectIndex).getProject().getModifiedProjectDuration(getFocusMarketOrMarket(market))) * 100);
    }

    public int getDaysRemaining(int projectIndex, BaseIndustry industry) {
        BoggledTerraformingProject.ProjectInstance project = projects.get(projectIndex);
        return project.getProject().getModifiedProjectDuration(getFocusMarketOrMarket(industry.getMarket())) - project.getDaysCompleted();
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
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            anyProjectValid = anyProjectValid || project.getProject().requirementsMet(market);
        }
        return anyProjectValid;
    }

    private boolean marketSuitableHidden(MarketAPI market) {
        boolean anyProjectValid = false;
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            anyProjectValid = anyProjectValid || project.getProject().requirementsHiddenMet(market);
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
            if (projects.get(i).getProject().requirementsMet(industry.getMarket()) && getDaysRemaining(i, industry) > 0) {
                return true;
            }
        }
        return false;
    }

    public void setFunctional(boolean functional) {
        this.functional = functional;
    }

    public boolean isFunctional() {
        return functional;
    }

    public boolean isUpgrading(BaseIndustry industry) {
        if (!built) {
            return false;
        }
        for (int i = 0; i < projects.size(); ++i) {
            if (projects.get(i).getProject().requirementsMet(industry.getMarket()) && getDaysRemaining(i, industry) > 0) {
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
            boolean anyEnabled = false;
            for (BoggledTerraformingProject.ProjectInstance project : projects) {
                if (project.getProject().isEnabled()) {
                    anyEnabled = true;
                    break;
                }
            }
            if (!anyEnabled) {
                return false;
            }

            boolean noneMet = true;
            for (BoggledTerraformingProject.ProjectInstance project : projects) {
                if (project.getProject().requirementsMet(industry.getMarket())) {
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
            boolean anyEnabled = false;
            for (BoggledTerraformingProject.ProjectInstance project : projects) {
                if (project.getProject().isEnabled()) {
                    anyEnabled = true;
                    break;
                }
            }
            if (!anyEnabled) {
                return false;
            }

            boolean allHidden = true;
            for (BoggledTerraformingProject.ProjectInstance project : projects) {
                if (project.getProject().requirementsHiddenMet(industry.getMarket())) {
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

    public void apply(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
        for (BoggledCommoditySupplyDemand.CommoditySupply commoditySupply : commoditySupply) {
            commoditySupply.apply(industry);
        }
        for (BoggledCommoditySupplyDemand.CommodityDemand commodityDemand : commodityDemand) {
            commodityDemand.apply(industry);
        }

        for (BoggledCommoditySupplyDemand.IndustryEffect industryEffect : industryEffects) {
            industryEffect.applyEffect(industry, industryInterface);
        }

        for (BoggledCommoditySupplyDemand.AICoreEffect aiCoreEffect : aiCoreEffects) {
            aiCoreEffect.applyEffect(industry, industryInterface);
        }

        if (!industry.isFunctional()) {
            industry.getAllSupply().clear();
            industry.unapply();
        }
    }

    public void unapply(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
        for (BoggledCommoditySupplyDemand.IndustryEffect industryEffect : industryEffects) {
            industryEffect.unapplyEffect(industry, industryInterface);
        }

        for (BoggledCommoditySupplyDemand.AICoreEffect aiCoreEffect : aiCoreEffects) {
            aiCoreEffect.unapplyEffect(industry, industryInterface);
        }
    }

    public void addRightAfterDescriptionSection(BaseIndustry industry, TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode) {
        float pad = 10.0f;
        for (int i = 0; i < projects.size(); ++i) {
            BoggledTerraformingProject project = projects.get(i).getProject();
            if (project.requirementsMet(industry.getMarket())) {
                Map<String, String> tokenReplacements = getTokenReplacements(industry.getMarket(), i);
                String[] highlights = project.getIncompleteMessageHighlights(tokenReplacements);
                addFormatTokenReplacement(tokenReplacements);
                String incompleteMessage = boggledTools.doTokenReplacement(project.getIncompleteMessage(), tokenReplacements);
                tooltipIncomplete(industry, tooltip, mode, incompleteMessage, pad, Misc.getHighlightColor(), highlights);
                tooltipDisrupted(industry, tooltip, mode, "Here's a message", pad, Misc.getNegativeHighlightColor());
            }
        }

        for (BoggledCommoditySupplyDemand.IndustryEffect effect : industryEffects) {
            effect.addRightAfterDescriptionSection(industry.getCurrentName(), industry, tooltip, mode);
        }

        for (BoggledCommoditySupplyDemand.AICoreEffect effect : aiCoreEffects) {
            List<Pair<String, List<String>>> desc = effect.addRightAfterDescriptionSection(industry.getCurrentName(), industry, tooltip, mode);

            for (Pair<String, List<String>> d : desc) {
                tooltip.addPara(d.one, 10f, Misc.getHighlightColor(), d.two.toArray(new String[0]));
            }
        }
    }

    public boolean hasPostDemandSection(BaseIndustry industry, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        for (BoggledCommoditySupplyDemand.CommodityDemand demand : commodityDemand) {
            if (demand.isEnabled()) {
                return true;
            }
        }

        for (BoggledCommoditySupplyDemand.IndustryEffect effect : industryEffects) {
            if (effect.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public void addPostDemandSection(BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        Map<String, BoggledCommoditySupplyDemand.CommodityDemandPara> demandTypeToCommodity = new HashMap<>();
        for (BoggledCommoditySupplyDemand.CommodityDemand demand : commodityDemand) {
            demand.addPostDemandInfo(demandTypeToCommodity, industry);
        }

        for (Map.Entry<String, BoggledCommoditySupplyDemand.CommodityDemandPara> entry : demandTypeToCommodity.entrySet()) {
            String commoditiesAnd = Misc.getAndJoined(entry.getValue().commodities.toArray(new String[0]));

            tooltip.addPara(entry.getValue().prefix + commoditiesAnd + entry.getValue().suffix, 10f, Misc.getHighlightColor(), entry.getValue().highlights.toArray(new String[0]));
        }

        for (BoggledCommoditySupplyDemand.IndustryEffect effect : industryEffects) {
            effect.addPostDemandSection(industry.getCurrentName(), industry, tooltip, hasDemand, mode);
        }

        for (BoggledCommoditySupplyDemand.AICoreEffect effect : aiCoreEffects) {
            effect.addPostDemandSection(industry.getCurrentName(), industry, tooltip, hasDemand, mode);
        }
    }

    public void applyDeficitToProduction(BaseIndustry industry, String modId, Pair<String, Integer> deficit, String... commodities) {
        for (String commodity : commodities) {
            if (industry.getSupply(commodity).getQuantity().isUnmodified()) {
                continue;
            }
            industry.supply(modId, commodity, -deficit.two, BaseIndustry.getDeficitText(deficit.one));
        }
    }

    public boolean canInstallAICores() {
        return !aiCoreEffects.isEmpty();
    }

    public void addAICoreDescription(BaseIndustry industry, TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode, String coreType, String coreId) {
        String prefix = coreType + "-level AI core currently assigned. ";
        if (mode == Industry.AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == Industry.AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            prefix = coreType + "-level AI core. ";
        }

        StringBuilder builder = new StringBuilder(prefix);
        List<String> highlights = new ArrayList<>();
        for (BoggledCommoditySupplyDemand.AICoreEffect effect : aiCoreEffects) {
            List<Pair<String, List<String>>> aiCoreDescription = effect.addAICoreDescription(industryTooltip, industry, tooltip, mode, coreType, coreId);
            for (Pair<String, List<String>> desc : aiCoreDescription) {
                builder.append(" ");
                builder.append(desc.one);
                highlights.addAll(desc.two);
            }
        }

        TooltipMakerAPI writeTo = tooltip;
        float pad = 10f;
        float imagePad = 10f;
        if (mode == Industry.AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(coreId);
            writeTo = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0f);
            pad = 0f;
        }

        writeTo.addPara(builder.toString(), pad, Misc.getHighlightColor(), highlights.toArray(new String[0]));
        if (mode == Industry.AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            tooltip.addImageWithText(imagePad);
        }
    }

    public boolean canImprove(BaseIndustry industry) {
        return !improveEffects.isEmpty();
    }

    public void applyImproveModifiers(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
        if (!industry.isImproved() || !industry.isFunctional()) {
            unapplyImproveModifiers(industry, industryInterface);
            return;
        }

        for (BoggledCommoditySupplyDemand.IndustryEffect improveEffect : improveEffects) {
            improveEffect.applyEffect(industry, industryInterface);
        }
    }

    private void unapplyImproveModifiers(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
        for (BoggledCommoditySupplyDemand.IndustryEffect improveEffect : improveEffects) {
            improveEffect.unapplyEffect(industry, industryInterface);
        }
    }

    public void addImproveDesc(BaseIndustry industry, TooltipMakerAPI tooltip, Industry.ImprovementDescriptionMode mode) {
        for (BoggledCommoditySupplyDemand.IndustryEffect improveEffect : improveEffects) {
            improveEffect.addImproveDesc(industry.getCurrentName(), industry, tooltip, mode);
            tooltip.addSpacer(10.0f);
        }
    }

    public float getPatherInterest(BaseIndustry industry) {
        return 0f;
    }

    public void modifyIncoming(BaseIndustry industry, MarketAPI market, PopulationComposition incoming) {

    }

    private Map<String, String> getTokenReplacements(MarketAPI market, int projectIndex) {
        Map<String, String> ret = boggledTools.getTokenReplacements(market);
        ret.put("$percentComplete", Integer.toString(getPercentComplete(projectIndex, market)));
        return ret;
    }

    private void addFormatTokenReplacement(Map<String, String> tokenReplacements) {
        tokenReplacements.put("%", "%%");
    }
}
