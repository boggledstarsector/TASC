package boggled.campaign.econ.industries;

import boggled.scripts.*;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import boggled.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BoggledCommonIndustry {
    /*
    This class cannot be made into a base class of any of the Boggled industries because Remnant Station and Cryosanctum gets in the way, may be able to do something else though
     */
    private final BoggledTerraformingRequirement.RequirementContext ctx;
    private final String industryId;
    private final String industryTooltip;

    public List<BoggledTerraformingProject.ProjectInstance> projects;

    private List<BoggledCommoditySupplyDemand.CommoditySupply> commoditySupply;
    private List<BoggledCommoditySupplyDemand.CommodityDemand> commodityDemand;

    private List<BoggledIndustryEffect.IndustryEffect> buildingFinishedEffects;
    private List<BoggledIndustryEffect.IndustryEffect> industryEffects;
    private List<BoggledIndustryEffect.IndustryEffect> improveEffects;

//    private List<BoggledIndustryEffect.AICoreEffect> aiCoreEffects;
    private Map<String, List<BoggledIndustryEffect.IndustryEffect>> aiCoreEffects;

    private List<BoggledProjectRequirementsAND> disruptRequirements;

    private float basePatherInterest;
    MutableStat modifiedPatherInterest;

    private List<ImageOverrideWithRequirement> imageReqs;
    private List<BoggledIndustryEffect.IndustryEffect> preBuildEffects;

    public static class ProductionData {
        int priority;
        public String commodityId;
        public MutableStat chance;
        public BoggledProjectRequirementsAND requirements;
        public ProductionData(int priority, String commodityId, int chance, BoggledProjectRequirementsAND requirements) {
            this.priority = priority;
            this.commodityId = commodityId;
            this.chance = new MutableStat(chance);
            this.requirements = requirements;
        }
    }

    Map<String, ProductionData> productionData;
    Map<String, MutableStat> productionDataModifiers;

    MutableStat buildCostModifier = new MutableStat(0f);
    MutableStat immigrationBonus = new MutableStat(0f);

    private boolean functional = true;

    private boolean building = false;
    private boolean built = false;

    private void setFromThat(BoggledCommonIndustry that) {
        this.projects = that.projects;

        this.commoditySupply = that.commoditySupply;
        this.commodityDemand = that.commodityDemand;

        this.buildingFinishedEffects = that.buildingFinishedEffects;
        this.industryEffects = that.industryEffects;
        this.improveEffects = that.improveEffects;

        this.aiCoreEffects = that.aiCoreEffects;

        this.disruptRequirements = that.disruptRequirements;

        this.basePatherInterest = that.basePatherInterest;
        this.modifiedPatherInterest = that.modifiedPatherInterest;

        this.imageReqs = that.imageReqs;

        this.preBuildEffects = that.preBuildEffects;

        this.buildCostModifier = that.buildCostModifier;

        this.productionData = that.productionData;
        this.productionDataModifiers = that.productionDataModifiers;
    }

    public BoggledCommonIndustry() {
        this.ctx = null;
        this.industryId = "";
        this.industryTooltip = "";

        this.projects = new ArrayList<>();

        this.commodityDemand = new ArrayList<>();
        this.commoditySupply = new ArrayList<>();

        this.buildingFinishedEffects = new ArrayList<>();
        this.industryEffects = new ArrayList<>();
        this.improveEffects = new ArrayList<>();
        this.aiCoreEffects = new HashMap<>();

        this.disruptRequirements = new ArrayList<>();

        this.basePatherInterest = 0f;
        this.modifiedPatherInterest = new MutableStat(0);

        this.imageReqs = new ArrayList<>();
        this.preBuildEffects = new ArrayList<>();

        this.productionData = new HashMap<>();
        this.productionDataModifiers = new HashMap<>();
    }

    public BoggledCommonIndustry(String industryId, String industryTooltip, List<BoggledTerraformingProject> projects, List<BoggledCommoditySupplyDemand.CommoditySupply> commoditySupply, List<BoggledCommoditySupplyDemand.CommodityDemand> commodityDemand, List<BoggledIndustryEffect.IndustryEffect> buildingFinishedEffects, List<BoggledIndustryEffect.IndustryEffect> industryEffects, List<BoggledIndustryEffect.IndustryEffect> improveEffects, Map<String, List<BoggledIndustryEffect.IndustryEffect>> aiCoreEffects, List<BoggledProjectRequirementsAND> disruptRequirements, float basePatherInterest, List<ImageOverrideWithRequirement> imageReqs, List<BoggledIndustryEffect.IndustryEffect> preBuildEffects) {
        this.ctx = null;
        this.industryId = industryId;
        this.industryTooltip = industryTooltip;

        this.projects = new ArrayList<>(projects.size());
        for (BoggledTerraformingProject project : projects) {
            this.projects.add(new BoggledTerraformingProject.ProjectInstance(project));
        }

        this.commoditySupply = commoditySupply;
        this.commodityDemand = commodityDemand;

        this.buildingFinishedEffects = buildingFinishedEffects;
        this.industryEffects = industryEffects;
        this.improveEffects = improveEffects;
        this.aiCoreEffects = aiCoreEffects;

        this.disruptRequirements = disruptRequirements;

        this.basePatherInterest = basePatherInterest;
        this.modifiedPatherInterest = new MutableStat(basePatherInterest);

        this.imageReqs = imageReqs;
        this.preBuildEffects = preBuildEffects;

        this.buildCostModifier = new MutableStat(Global.getSettings().getIndustrySpec(industryId).getCost());

        this.productionData = new HashMap<>();
        this.productionDataModifiers = new HashMap<>();
    }

    public BoggledCommonIndustry(BoggledCommonIndustry that, BaseIndustry industry) {
        this.industryId = that.industryId;
        this.industryTooltip = that.industryTooltip;
        setFromThat(that);

        this.ctx = new BoggledTerraformingRequirement.RequirementContext(industry);
    }

    protected Object readResolve() {
        Global.getLogger(this.getClass()).info("Doing readResolve for " + industryId + " " + industryTooltip);
        BoggledCommonIndustry that = boggledTools.getIndustryProject(industryId);
        setFromThat(that);

        return this;
    }

    public void overridesFromJSON(JSONObject data) throws JSONException {

    }

    public void advance(float amount) {
        if (!built) {
            return;
        }

        if (ctx.getIndustry().isDisrupted() || !marketSuitableBoth(ctx)) {
            return;
        }

        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            project.advance(ctx);
        }
    }

    public int getPercentComplete(int projectIndex) {
        return (int) Math.min(99, ((float)projects.get(projectIndex).getDaysCompleted() / projects.get(projectIndex).getProject().getModifiedProjectDuration(ctx.getFocusContext())) * 100);
    }

    public int getDaysRemaining(int projectIndex) {
        BoggledTerraformingProject.ProjectInstance project = projects.get(projectIndex);
        return project.getProject().getModifiedProjectDuration(ctx.getFocusContext()) - project.getDaysCompleted();
    }

    public void tooltipIncomplete(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode, String format, float pad, Color hl, String... highlights) {
        if (format.isEmpty()) {
            return;
        }
        if (!(   marketSuitableBoth(ctx)
              && mode != Industry.IndustryTooltipMode.ADD_INDUSTRY
              && mode != Industry.IndustryTooltipMode.QUEUED)) {
            return;
        }
        tooltip.addPara(format, pad, hl, highlights);
    }

    public void tooltipComplete(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode, String format, float pad, Color hl, String... highlights) {
        if (format.isEmpty()) {
            return;
        }
        if(!(   !marketSuitableBoth(ctx)
             && mode != Industry.IndustryTooltipMode.ADD_INDUSTRY
             && mode != Industry.IndustryTooltipMode.QUEUED
             && !ctx.getIndustry().isBuilding())) {
            return;
        }
        tooltip.addPara(format, pad, hl, highlights);
    }

    public void tooltipDisrupted(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode, String format, float pad, Color hl, String... highlights) {
        if (format.isEmpty()) {
            return;
        }
        if (!(   ctx.getIndustry().isDisrupted()
              && marketSuitableBoth(ctx)
              && mode != Industry.IndustryTooltipMode.ADD_INDUSTRY
              && mode != Industry.IndustryTooltipMode.QUEUED
              && !ctx.getIndustry().isBuilding())) {
            return;
        }
        tooltip.addPara(format, pad, hl, highlights);
    }

    private boolean marketSuitableVisible(BoggledTerraformingRequirement.RequirementContext ctx) {
        boolean anyProjectValid = false;
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            anyProjectValid = anyProjectValid || project.getProject().requirementsMet(ctx);
        }
        return anyProjectValid;
    }

    private boolean marketSuitableHidden(BoggledTerraformingRequirement.RequirementContext ctx) {
        boolean anyProjectValid = false;
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            anyProjectValid = anyProjectValid || project.getProject().requirementsHiddenMet(ctx);
        }
        return anyProjectValid;
    }

    public boolean marketSuitableBoth(BoggledTerraformingRequirement.RequirementContext ctx) {
        return marketSuitableHidden(ctx) && marketSuitableVisible(ctx);
    }

    public static MarketAPI getFocusMarketOrMarket(MarketAPI market) {
        if (market == null) {
            return null;
        }
        SectorEntityToken focus = market.getPrimaryEntity().getOrbitFocus();
        if (focus == null) {
            return null;
        }
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
    public void startBuilding() {
        building = true;
        built = false;
    }

    public void startUpgrading() {
    }

    public void buildingFinished() {
        building = false;
        built = true;
        for (BoggledIndustryEffect.IndustryEffect buildingFinishedEffect : buildingFinishedEffects) {
            buildingFinishedEffect.applyEffect(ctx, ctx.getIndustry().getNameForModifier());
        }
    }

    public void upgradeFinished(Industry previous) {
    }

    public void finishBuildingOrUpgrading() {
    }

    public boolean isBuilding() {
        if (building) {
            return true;
        }
        if (!built) {
            // Stupid as hell but needs to be here for the industry to work same as vanilla structures
            return false;
        }
        for (int i = 0; i < projects.size(); ++i) {
            if (projects.get(i).getProject().requirementsMet(ctx) && getDaysRemaining(i) > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean canBeDisrupted() {
        for (BoggledProjectRequirementsAND disruptRequirement : disruptRequirements) {
            if (disruptRequirement.requirementsMet(ctx)) {
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

    public boolean isUpgrading() {
        if (!built) {
            return false;
        }
        for (int i = 0; i < projects.size(); ++i) {
            if (projects.get(i).getProject().requirementsMet(ctx) && getDaysRemaining(i) > 0) {
                return true;
            }
        }
        return false;
    }

    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade) {
        for (BoggledIndustryEffect.IndustryEffect buildingFinishedEffect : buildingFinishedEffects) {
            buildingFinishedEffect.unapplyEffect(ctx);
        }
    }

    public float getBuildOrUpgradeProgress() {
        if (ctx.getIndustry().isDisrupted()) {
            return 0.0f;
        } else if (building || !built) {
            return Math.min(1.0f, ctx.getIndustry().getBuildProgress() / ctx.getIndustry().getBuildTime());
        }

        float progress = 0f;
        for (int i = 0; i < projects.size(); ++i) {
            progress = Math.max(getPercentComplete(i) / 100f, progress);
        }
        return progress;
    }

    public String getBuildOrUpgradeDaysText() {
        int daysRemain;
        if (ctx.getIndustry().isDisrupted()) {
            daysRemain = (int)(ctx.getIndustry().getDisruptedDays());
        } else if (building || !built) {
            daysRemain = (int)(ctx.getIndustry().getBuildTime() - ctx.getIndustry().getBuildProgress());
        } else {
            daysRemain = Integer.MAX_VALUE;
            for (int i = 0; i < projects.size(); ++i) {
                daysRemain = Math.min(getDaysRemaining(i), daysRemain);
            }
        }
        String dayOrDays = daysRemain == 1 ? "day" : "days";
        return daysRemain + " " + dayOrDays;
    }

    public String getBuildOrUpgradeProgressText() {
        String prefix;
        if (ctx.getIndustry().isDisrupted()) {
            prefix = "Disrupted";
        } else if (building || !built) {
            prefix = "Building";
        } else {
            prefix = this.industryTooltip;
        }
        return prefix + ": " + getBuildOrUpgradeDaysText() + " left";
    }

    public boolean isAvailableToBuild() {
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
                if (project.getProject().requirementsMet(ctx)) {
                    noneMet = false;
                    break;
                }
            }
            if (noneMet) {
                return false;
            }
        }

        return marketSuitableVisible(ctx) && marketSuitableHidden(ctx);
    }

    public boolean showWhenUnavailable() {
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
                if (project.getProject().requirementsHiddenMet(ctx)) {
                    allHidden = false;
                    break;
                }
            }
            if (allHidden) {
                return false;
            }
        }

        return marketSuitableHidden(ctx);
    }

    public TooltipData getUnavailableReason() {
        return boggledTools.getUnavailableReason(projects, industryTooltip, ctx, boggledTools.getTokenReplacements(ctx));
    }

    public void apply() {
        for (BoggledCommoditySupplyDemand.CommoditySupply commoditySupply : commoditySupply) {
            commoditySupply.apply(ctx.getIndustry());
        }
        for (BoggledCommoditySupplyDemand.CommodityDemand commodityDemand : commodityDemand) {
            commodityDemand.apply(ctx.getIndustry());
        }

        for (BoggledIndustryEffect.IndustryEffect industryEffect : industryEffects) {
            industryEffect.applyEffect(ctx, ctx.getIndustry().getNameForModifier());
        }

        List<BoggledIndustryEffect.IndustryEffect> coreEffects = aiCoreEffects.get(ctx.getIndustry().getAICoreId());
        if (coreEffects != null) {
            String effectSource = Global.getSettings().getCommoditySpec(ctx.getIndustry().getAICoreId()).getName() + " assigned";
            for (BoggledIndustryEffect.IndustryEffect coreEffect : coreEffects) {
                coreEffect.applyEffect(ctx, effectSource);
            }
        }

        if (!ctx.getIndustry().isFunctional()) {
            ctx.getIndustry().getAllSupply().clear();
            ctx.getIndustry().unapply();
        }
    }

    public void unapply() {
        for (BoggledIndustryEffect.IndustryEffect industryEffect : industryEffects) {
            industryEffect.unapplyEffect(ctx);
        }

        for (Map.Entry<String, List<BoggledIndustryEffect.IndustryEffect>> coreEffects : aiCoreEffects.entrySet()) {
            for (BoggledIndustryEffect.IndustryEffect coreEffect : coreEffects.getValue()) {
                coreEffect.unapplyEffect(ctx);
            }
        }
    }

    public void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode) {
        float pad = 10.0f;
        for (int i = 0; i < projects.size(); ++i) {
            BoggledTerraformingProject project = projects.get(i).getProject();
            if (project.requirementsMet(ctx)) {
                Map<String, String> tokenReplacements = getTokenReplacements(ctx, i);
                String[] highlights = project.getIncompleteMessageHighlights(tokenReplacements);
                addFormatTokenReplacement(tokenReplacements);
                String incompleteMessage = boggledTools.doTokenReplacement(project.getIncompleteMessage(), tokenReplacements);
                tooltipIncomplete(tooltip, mode, incompleteMessage, pad, Misc.getHighlightColor(), highlights);
                tooltipDisrupted(tooltip, mode, "Here's a message", pad, Misc.getNegativeHighlightColor());
            }
        }

        for (BoggledIndustryEffect.IndustryEffect effect : industryEffects) {
            List<TooltipData> desc = effect.addRightAfterDescriptionSection(ctx, mode);
            if (desc.isEmpty()) {
                continue;
            }
            tooltip.addSpacer(pad);
            for (TooltipData d : desc) {
                tooltip.addPara(d.text, 0f, d.highlightColors.toArray(new Color[0]), d.highlights.toArray(new String[0]));
            }
        }

        List<BoggledIndustryEffect.IndustryEffect> coreEffects = aiCoreEffects.get(ctx.getIndustry().getAICoreId());
        if (coreEffects != null) {
            for (BoggledIndustryEffect.IndustryEffect effect : coreEffects) {
                List<TooltipData> desc = effect.addRightAfterDescriptionSection(ctx, mode);
                if (desc.isEmpty()) {
                    continue;
                }
                tooltip.addSpacer(pad);
                for (TooltipData d : desc) {
                    tooltip.addPara(d.text, pad, d.highlightColors.toArray(new Color[0]), d.highlights.toArray(new String[0]));
                }
            }
        }
    }

    public boolean hasPostDemandSection(boolean hasDemand, Industry.IndustryTooltipMode mode) {
        for (BoggledCommoditySupplyDemand.CommodityDemand demand : commodityDemand) {
            if (demand.isEnabled()) {
                return true;
            }
        }

        for (BoggledIndustryEffect.IndustryEffect effect : industryEffects) {
            if (effect.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        Map<String, BoggledCommoditySupplyDemand.CommodityDemandPara> demandTypeToCommodity = new HashMap<>();
        for (BoggledCommoditySupplyDemand.CommodityDemand demand : commodityDemand) {
            demand.addPostDemandInfo(demandTypeToCommodity, ctx.getIndustry());
        }

        for (Map.Entry<String, BoggledCommoditySupplyDemand.CommodityDemandPara> entry : demandTypeToCommodity.entrySet()) {
            String commoditiesAnd = Misc.getAndJoined(entry.getValue().commodities.toArray(new String[0]));

            tooltip.addPara(entry.getValue().prefix + commoditiesAnd + entry.getValue().suffix, 10f, Misc.getHighlightColor(), entry.getValue().highlights.toArray(new String[0]));
        }

        List<TooltipData> tooltipData = new ArrayList<>();
        for (BoggledIndustryEffect.IndustryEffect effect : industryEffects) {
            List<TooltipData> data = effect.addPostDemandSection(ctx, hasDemand, mode);
            if (data != null && !data.isEmpty()) {
                tooltipData.addAll(data);
            }
        }

        List<BoggledIndustryEffect.IndustryEffect> coreEffects = aiCoreEffects.get(ctx.getIndustry().getAICoreId());
        if (coreEffects != null) {
            for (BoggledIndustryEffect.IndustryEffect effect : coreEffects) {
                List<TooltipData> data = effect.addPostDemandSection(ctx, hasDemand, mode);
                if (data != null && !data.isEmpty()) {
                    tooltipData.addAll(data);
                }
            }
        }

        for (TooltipData data : tooltipData) {
            tooltip.addPara(data.text, 10.f, data.highlightColors.toArray(new Color[0]), data.highlights.toArray(new String[0]));
        }
    }

    public void applyDeficitToProduction(String modId, Pair<String, Integer> deficit, String... commodities) {
        for (String commodity : commodities) {
            if (ctx.getIndustry().getSupply(commodity).getQuantity().isUnmodified()) {
                continue;
            }
            ctx.getIndustry().supply(modId, commodity, -deficit.two, BaseIndustry.getDeficitText(deficit.one));
        }
    }

    public boolean canInstallAICores() {
        return !aiCoreEffects.isEmpty();
    }

    public void addAICoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode, String coreType, String coreId) {
        String prefix = coreType + "-level AI core currently assigned. ";
        BoggledIndustryEffect.IndustryEffect.DescriptionMode descMode = BoggledIndustryEffect.IndustryEffect.DescriptionMode.TO_APPLY;
        if (mode == Industry.AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == Industry.AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            prefix = coreType + "-level AI core. ";
        }

        StringBuilder builder = new StringBuilder(prefix);
        List<String> highlights = new ArrayList<>();
        List<Color> highlightColors = new ArrayList<>();
        List<BoggledIndustryEffect.IndustryEffect> coreEffects = aiCoreEffects.get(coreId);
        if (coreEffects != null) {
            for (BoggledIndustryEffect.IndustryEffect effect : coreEffects) {
                List<TooltipData> aiCoreDescription = effect.getApplyOrAppliedDesc(ctx, descMode);
                for (TooltipData desc : aiCoreDescription) {
                    builder.append(" ");
                    builder.append(desc.text);
                    highlights.addAll(desc.highlights);
                    highlightColors.addAll(desc.highlightColors);
                }
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

        writeTo.addPara(builder.toString(), pad, highlightColors.toArray(new Color[0]), highlights.toArray(new String[0]));
        if (mode == Industry.AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            tooltip.addImageWithText(imagePad);
        }
    }

    public boolean canImprove() {
        return !improveEffects.isEmpty();
    }

    public void applyImproveModifiers() {
        if (!ctx.getIndustry().isImproved() || !ctx.getIndustry().isFunctional()) {
            unapplyImproveModifiers();
            return;
        }

        for (BoggledIndustryEffect.IndustryEffect improveEffect : improveEffects) {
            improveEffect.applyEffect(ctx, "Improvement (" + ctx.getIndustry().getCurrentName() + ")");
        }
    }

    private void unapplyImproveModifiers() {
        for (BoggledIndustryEffect.IndustryEffect improveEffect : improveEffects) {
            improveEffect.unapplyEffect(ctx);
        }
    }

    public void addImproveDesc(TooltipMakerAPI tooltip, Industry.ImprovementDescriptionMode mode) {
        BoggledIndustryEffect.IndustryEffect.DescriptionMode descMode;
        if (mode == Industry.ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
            descMode = BoggledIndustryEffect.IndustryEffect.DescriptionMode.APPLIED;
        } else {
            descMode = BoggledIndustryEffect.IndustryEffect.DescriptionMode.TO_APPLY;
        }

        for (BoggledIndustryEffect.IndustryEffect improveEffect : improveEffects) {
            List<TooltipData> improveDescription = improveEffect.getApplyOrAppliedDesc(ctx, descMode);
            for (TooltipData desc : improveDescription) {
                tooltip.addPara(desc.text, 0f, desc.highlightColors.toArray(new Color[0]), desc.highlights.toArray(new String[0]));
                tooltip.addSpacer(10.0f);
            }
        }
    }

    public void modifyPatherInterest(MutableStat modifier) {
        modifiedPatherInterest.applyMods(modifier);
    }

    public void unmodifyPatherInterest(String source) {
        modifiedPatherInterest.unmodify(source);
    }

    public float getPatherInterest() {
        return modifiedPatherInterest.getModifiedValue();
    }

    public float getBasePatherInterest() {
        return basePatherInterest;
    }

    public void modifyImmigration(MutableStat modifier) {
        immigrationBonus.applyMods(modifier);
    }

    public void unmodifyImmigration(String source) {
        immigrationBonus.unmodify(source);
    }

    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.getWeight().applyMods(immigrationBonus);
    }

    private Map<String, String> getTokenReplacements(BoggledTerraformingRequirement.RequirementContext ctx, int projectIndex) {
        Map<String, String> ret = boggledTools.getTokenReplacements(ctx);
        ret.put("$percentComplete", Integer.toString(getPercentComplete(projectIndex)));
        return ret;
    }

    private void addFormatTokenReplacement(Map<String, String> tokenReplacements) {
        tokenReplacements.put("%", "%%");
    }

    public static class TooltipData {
        public String text;
        public List<Color> highlightColors;
        public List<String> highlights;

        public TooltipData(String text, List<Color> highlightColors, List<String> highlights) {
            this.text = text;
            this.highlightColors = highlightColors;
            this.highlights = highlights;
        }

        public TooltipData(String text, Color highlightColor, String highlight) {
            this.text = text;
            this.highlightColors = new ArrayList<>(Collections.singletonList(highlightColor));
            this.highlights = new ArrayList<>(Collections.singletonList(highlight));
        }

        public TooltipData(String text) {
            this.text = text;
            this.highlightColors = new ArrayList<>();
            this.highlights = new ArrayList<>();
        }
    }

    public static class ImageOverrideWithRequirement {
        BoggledProjectRequirementsAND requirements;
        String category;
        String id;

        public ImageOverrideWithRequirement(BoggledProjectRequirementsAND requirements, String category, String id) {
            this.requirements = requirements;
            this.category = category;
            this.id = id;
        }
    }

    public String getCurrentImage() {
        for (ImageOverrideWithRequirement req : imageReqs) {
            if (req.requirements.requirementsMet(ctx)) {
                return Global.getSettings().getSpriteName(req.category, req.id);
            }
        }

        return ctx.getIndustry().getSpec().getImageName();
    }

    public void modifyBuildCost(MutableStat modifier) {
        buildCostModifier.applyMods(modifier);
    }

    public void unmodifyBuildCost(String source) {
        buildCostModifier.unmodify(source);
    }

    public float getBuildCost() {
        for (BoggledIndustryEffect.IndustryEffect preBuildEffect : preBuildEffects) {
            preBuildEffect.applyEffect(ctx, "Prebuild lol");
        }
        return buildCostModifier.getModifiedValue();
    }

    public void addProductionData(ProductionData data) {
        productionData.put(data.commodityId, data);
        if (!productionDataModifiers.containsKey(data.commodityId)) {
            productionDataModifiers.put(data.commodityId, new MutableStat(0));
        }
    }

    public void removeProductionData(ProductionData data) {
        productionData.remove(data.commodityId);
    }

    public void modifyProductionChance(String commodityId, String source, int value) {
        MutableStat modifier = productionDataModifiers.get(commodityId);
        if (modifier == null) {
            productionDataModifiers.put(commodityId, new MutableStat(0));
            modifier = productionDataModifiers.get(commodityId);
        }
        modifier.modifyFlat(source, value);
    }

    public void unmodifyProductionChance(String commodityId, String source) {
        MutableStat modifier = productionDataModifiers.get(commodityId);
        if (modifier == null) {
            productionDataModifiers.put(commodityId, new MutableStat(0));
            modifier = productionDataModifiers.get(commodityId);
        }
        modifier.unmodify(source);
    }

    public CargoAPI generateCargoForGatheringPoint(Random random) {
        if (!ctx.getIndustry().isFunctional()) {
            return null;
        }
        // As each item is checked, offset is incremented by the chance of the item
        // If the roll is less than chance + offset, give the item and return
        // Goes from smaller priority value items to bigger priority value items
        // The smaller the priority, the higher the priority, with zero being the highest priority
        // If multiple items share the same priority, order is unspecified
        // Number in range (0, 100], ie possible values are from 1 to 100 inclusive both ends
        int roll = random.nextInt(100) + 1;
        int offset = 0;
        CargoAPI ret = Global.getFactory().createCargo(true);

        List<ProductionData> workingData = new ArrayList<>(productionData.values());
        Collections.sort(workingData, new Comparator<ProductionData>() {
            @Override
            public int compare(ProductionData o1, ProductionData o2) {
                return Integer.compare(o1.priority, o2.priority);
            }
        });

        for (ProductionData pd : workingData) {
            if (!pd.requirements.requirementsMet(ctx)) {
                offset += pd.chance.getModifiedInt();
                continue;
            }
            MutableStat modifier = productionDataModifiers.get(pd.commodityId);
            int value = pd.chance.getModifiedInt() + modifier.getModifiedInt();
            if (value == 0) {
                continue;
            }
            if (roll < (offset + value)) {
                ret.addCommodity(pd.commodityId, 1);
                return ret;
            }
            offset += value;
        }
        return null;
    }
}
