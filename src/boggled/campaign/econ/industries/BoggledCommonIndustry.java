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

    private List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> buildingFinishedEffects;
    private List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> improveEffects;

    private Map<String, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect>> aiCoreEffects;

    private List<BoggledProjectRequirementsAND> disruptRequirements;

    private float basePatherInterest;
    MutableStat modifiedPatherInterest;

    private List<ImageOverrideWithRequirement> imageReqs;
    private List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> preBuildEffects;

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

        public ProductionData(ProductionData that) {
            this.priority = that.priority;
            this.commodityId = that.commodityId;
            this.chance = new MutableStat(that.chance.getBaseValue());
            this.requirements = that.requirements;
        }
    }

    boolean monthlyProductionEnabled;
    Map<String, ProductionData> productionData;

    MutableStat buildCostModifier = new MutableStat(0f);
    MutableStat immigrationBonus = new MutableStat(0f);

    private List<Pair<String, Integer>> shortages = null;

    private boolean building = false;
    private boolean built = false;

    private void setFromThat(BoggledCommonIndustry that) {
        this.projects = that.projects;

        this.buildingFinishedEffects = that.buildingFinishedEffects;
        this.improveEffects = that.improveEffects;

        this.aiCoreEffects = that.aiCoreEffects;

        this.disruptRequirements = that.disruptRequirements;

        this.basePatherInterest = that.basePatherInterest;
        this.modifiedPatherInterest = that.modifiedPatherInterest;

        this.imageReqs = that.imageReqs;

        this.preBuildEffects = that.preBuildEffects;

        this.buildCostModifier = that.buildCostModifier;

        this.monthlyProductionEnabled = that.monthlyProductionEnabled;
        this.productionData = that.productionData;
    }

    public BoggledCommonIndustry() {
        this.ctx = null;
        this.industryId = "";
        this.industryTooltip = "";

        this.projects = new ArrayList<>();

        this.buildingFinishedEffects = new ArrayList<>();
        this.improveEffects = new ArrayList<>();
        this.aiCoreEffects = new HashMap<>();

        this.disruptRequirements = new ArrayList<>();

        this.basePatherInterest = 0f;
        this.modifiedPatherInterest = new MutableStat(0);

        this.imageReqs = new ArrayList<>();
        this.preBuildEffects = new ArrayList<>();

        this.monthlyProductionEnabled = false;
        this.productionData = new HashMap<>();
    }

    public BoggledCommonIndustry(String industryId, String industryTooltip, List<BoggledTerraformingProject> projects, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> buildingFinishedEffects, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> improveEffects, Map<String, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect>> aiCoreEffects, List<BoggledProjectRequirementsAND> disruptRequirements, float basePatherInterest, List<ImageOverrideWithRequirement> imageReqs, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> preBuildEffects) {
        this.ctx = null;
        this.industryId = industryId;
        this.industryTooltip = industryTooltip;

        this.projects = new ArrayList<>(projects.size());
        for (BoggledTerraformingProject project : projects) {
            this.projects.add(new BoggledTerraformingProject.ProjectInstance(project));
        }

        this.buildingFinishedEffects = buildingFinishedEffects;
        this.improveEffects = improveEffects;
        this.aiCoreEffects = aiCoreEffects;

        this.disruptRequirements = disruptRequirements;

        this.basePatherInterest = basePatherInterest;
        this.modifiedPatherInterest = new MutableStat(basePatherInterest);

        this.imageReqs = imageReqs;
        this.preBuildEffects = preBuildEffects;

        this.buildCostModifier = new MutableStat(Global.getSettings().getIndustrySpec(industryId).getCost());

        this.monthlyProductionEnabled = false;
        this.productionData = new HashMap<>();
    }

    public BoggledCommonIndustry(BoggledCommonIndustry that, BaseIndustry industry) {
        this.industryId = that.industryId;
        this.industryTooltip = that.industryTooltip;
        setFromThat(that);

        this.ctx = new BoggledTerraformingRequirement.RequirementContext(industry, null);
    }

    protected Object readResolve() {
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

        if (ctx.getSourceIndustry().isDisrupted()) {
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
             && !ctx.getSourceIndustry().isBuilding())) {
            return;
        }
        tooltip.addPara(format, pad, hl, highlights);
    }

    public void tooltipDisrupted(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode, String format, float pad, Color hl, String... highlights) {
        if (format.isEmpty()) {
            return;
        }
        if (!(   ctx.getSourceIndustry().isDisrupted()
              && marketSuitableBoth(ctx)
              && mode != Industry.IndustryTooltipMode.ADD_INDUSTRY
              && mode != Industry.IndustryTooltipMode.QUEUED
              && !ctx.getSourceIndustry().isBuilding())) {
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
        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect buildingFinishedEffect : buildingFinishedEffects) {
            buildingFinishedEffect.applyProjectEffect(ctx, ctx.getSourceIndustry().getNameForModifier());
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

    public void setShortages(List<Pair<String, Integer>> shortages) {
        this.shortages = shortages;
    }

    public List<Pair<String, Integer>> getShortages() {
        return shortages;
    }

    public boolean hasShortage() {
        return shortages != null && !shortages.isEmpty();
    }

    public boolean isFunctional() {
        return !building && built;
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
        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect buildingFinishedEffect : buildingFinishedEffects) {
            buildingFinishedEffect.unapplyProjectEffect(ctx);
        }
    }

    public float getBuildOrUpgradeProgress() {
        if (ctx.getSourceIndustry().isDisrupted()) {
            return 0.0f;
        } else if (building || !built) {
            return Math.min(1.0f, ctx.getSourceIndustry().getBuildProgress() / ctx.getSourceIndustry().getBuildTime());
        }

        float progress = 0f;
        for (int i = 0; i < projects.size(); ++i) {
            progress = Math.max(getPercentComplete(i) / 100f, progress);
        }
        return progress;
    }

    public String getBuildOrUpgradeDaysText() {
        int daysRemain;
        if (ctx.getSourceIndustry().isDisrupted()) {
            daysRemain = (int)(ctx.getSourceIndustry().getDisruptedDays());
        } else if (building || !built) {
            daysRemain = (int)(ctx.getSourceIndustry().getBuildTime() - ctx.getSourceIndustry().getBuildProgress());
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
        if (ctx.getSourceIndustry().isDisrupted()) {
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
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            project.getProject().applyOngoingEffects(ctx, ctx.getSourceIndustry().getNameForModifier());
        }

        List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> coreEffects = aiCoreEffects.get(ctx.getSourceIndustry().getAICoreId());
        if (coreEffects != null) {
            String effectSource = Global.getSettings().getCommoditySpec(ctx.getSourceIndustry().getAICoreId()).getName() + " assigned";
            for (BoggledTerraformingProjectEffect.TerraformingProjectEffect coreEffect : coreEffects) {
                coreEffect.applyProjectEffect(ctx, effectSource);
            }
        }

//        if (!ctx.getSourceIndustry().isFunctional()) {
//            ctx.getSourceIndustry().getAllSupply().clear();
//            ctx.getSourceIndustry().unapply();
//        }
    }

    public void unapply() {
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            project.getProject().unapplyOngoingEffects(ctx);
        }

        for (Map.Entry<String, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect>> coreEffects : aiCoreEffects.entrySet()) {
            for (BoggledTerraformingProjectEffect.TerraformingProjectEffect coreEffect : coreEffects.getValue()) {
                coreEffect.unapplyProjectEffect(ctx);
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

//        for (BoggledIndustryEffect.IndustryEffect effect : industryEffects) {
//            List<TooltipData> desc = effect.addRightAfterDescriptionSection(ctx, mode);
//            if (desc.isEmpty()) {
//                continue;
//            }
//            tooltip.addSpacer(pad);
//            for (TooltipData d : desc) {
//                tooltip.addPara(d.text, 0f, d.highlightColors.toArray(new Color[0]), d.highlights.toArray(new String[0]));
//            }
//        }

//        List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> coreEffects = aiCoreEffects.get(ctx.getSourceIndustry().getAICoreId());
//        if (coreEffects != null) {
//            for (BoggledTerraformingProjectEffect.TerraformingProjectEffect effect : coreEffects) {
//                List<TooltipData> desc = effect.addRightAfterDescriptionSection(ctx, mode);
//                if (desc.isEmpty()) {
//                    continue;
//                }
//                tooltip.addSpacer(pad);
//                for (TooltipData d : desc) {
//                    tooltip.addPara(d.text, pad, d.highlightColors.toArray(new Color[0]), d.highlights.toArray(new String[0]));
//                }
//            }
//        }
    }

    public boolean hasPostDemandSection(boolean hasDemand, Industry.IndustryTooltipMode mode) {
//        for (BoggledIndustryEffect.IndustryEffect effect : industryEffects) {
//            if (effect.isEnabled()) {
//                return true;
//            }
//        }
        return false;
    }

    public void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        float pad = 10f;
        BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionMode descMode;
        if (mode == Industry.IndustryTooltipMode.ADD_INDUSTRY) {
            descMode = BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionMode.TO_APPLY;
        } else {
            descMode = BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionMode.APPLIED;
        }

        for (BoggledTerraformingProject.ProjectInstance projectInstance : projects) {
            BoggledTerraformingProject project = projectInstance.getProject();
            Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> paras = project.getOngoingEffectTooltipInfo(ctx, ctx.getSourceIndustry().getCurrentName(), descMode, BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionSource.POST_DEMAND_SECTION);
            for (BoggledTerraformingProjectEffect.EffectTooltipPara para : paras.values()) {
                StringBuilder text = new StringBuilder(para.prefix);
                for (String infix : para.infix) {
                    text.append(infix);
                }
                text.append(para.suffix);
                tooltip.addPara(text.toString(), pad, para.highlightColors.toArray(new Color[0]), para.highlights.toArray(new String[0]));
            }
        }
    }

    public void applyDeficitToProduction(String modId, Pair<String, Integer> deficit, String... commodities) {
        for (String commodity : commodities) {
            if (ctx.getSourceIndustry().getSupply(commodity).getQuantity().isUnmodified()) {
                continue;
            }
            ctx.getSourceIndustry().supply(modId, commodity, -deficit.two, BaseIndustry.getDeficitText(deficit.one));
        }
    }

    public boolean canInstallAICores() {
        return !aiCoreEffects.isEmpty();
    }

    public void addAICoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode, String coreType, String coreId) {
        String prefix = coreType + "-level AI core currently assigned. ";
        BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionMode descMode = BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionMode.TO_APPLY;
        if (mode == Industry.AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == Industry.AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            prefix = coreType + "-level AI core. ";
        }

        StringBuilder builder = new StringBuilder(prefix);
        List<String> highlights = new ArrayList<>();
        List<Color> highlightColors = new ArrayList<>();
        List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> coreEffects = aiCoreEffects.get(coreId);
        if (coreEffects != null) {
            for (BoggledTerraformingProjectEffect.TerraformingProjectEffect effect : coreEffects) {
                String effectSource = Global.getSettings().getCommoditySpec(coreId).getName() + " assigned";
                Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> effectTypeToPara = new LinkedHashMap<>();
                effect.addEffectTooltipInfo(ctx, effectTypeToPara, effectSource, descMode, BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionSource.AI_CORE_DESCRIPTION);
                for (BoggledTerraformingProjectEffect.EffectTooltipPara effectTooltipPara : effectTypeToPara.values()) {
                    StringBuilder text = new StringBuilder(effectTooltipPara.prefix);
                    for (String infix : effectTooltipPara.infix) {
                        text.append(infix);
                    }
                    text.append(effectTooltipPara.suffix);
                    builder.append(text);
                    highlights.addAll(effectTooltipPara.highlights);
                    highlightColors.addAll(effectTooltipPara.highlightColors);
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
        if (!ctx.getSourceIndustry().isImproved() || !ctx.getSourceIndustry().isFunctional()) {
            unapplyImproveModifiers();
            return;
        }

        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect improveEffect : improveEffects) {
            improveEffect.applyProjectEffect(ctx, "Improvements (" + ctx.getSourceIndustry().getCurrentName() + ")");
        }
    }

    private void unapplyImproveModifiers() {
        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect improveEffect : improveEffects) {
            improveEffect.unapplyProjectEffect(ctx);
        }
    }

    public void addImproveDesc(TooltipMakerAPI tooltip, Industry.ImprovementDescriptionMode mode) {
        float pad = 10f;
        BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionMode descMode;
        if (mode == Industry.ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
            descMode = BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionMode.APPLIED;
        } else {
            descMode = BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionMode.TO_APPLY;
        }

        String effectSource = "Improvements (" + ctx.getSourceIndustry().getCurrentName() + ")";
        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect effect : improveEffects) {
            Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> effectTypeToPara = new LinkedHashMap<>();
            effect.addEffectTooltipInfo(ctx, effectTypeToPara, effectSource, descMode, BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionSource.IMPROVE_DESCRIPTION);
            for (BoggledTerraformingProjectEffect.EffectTooltipPara para : effectTypeToPara.values()) {
                StringBuilder text = new StringBuilder(para.prefix);
                for (String infix : para.infix) {
                    text.append(infix);
                }
                text.append(para.suffix);
                tooltip.addPara(text.toString(), 0f, para.highlightColors.toArray(new Color[0]), para.highlights.toArray(new String[0]));
                tooltip.addSpacer(pad);
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

        return ctx.getSourceIndustry().getSpec().getImageName();
    }

    public void modifyBuildCost(MutableStat modifier) {
        buildCostModifier.applyMods(modifier);
    }

    public void unmodifyBuildCost(String source) {
        buildCostModifier.unmodify(source);
    }

    public float getBuildCost() {
        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect preBuildEffect : preBuildEffects) {
            preBuildEffect.applyProjectEffect(ctx, industryTooltip);
        }
        return buildCostModifier.getModifiedValue();
    }

    public void setEnableMonthlyProduction(boolean enabled) {
        monthlyProductionEnabled = enabled;
    }

    public void addProductionData(ProductionData data) {
        productionData.put(data.commodityId, new ProductionData(data));
    }

    public void removeProductionData(ProductionData data) {
        productionData.remove(data.commodityId);
    }

    public void modifyProductionChance(String commodityId, String source, int value) {
        ProductionData pd = productionData.get(commodityId);
        if (pd == null) {
            return;
        }
        pd.chance.modifyFlat(source, value);
    }

    public void unmodifyProductionChance(String commodityId, String source) {
        ProductionData pd = productionData.get(commodityId);
        if (pd == null) {
            return;
        }
        pd.chance.unmodify(source);
    }

    public Pair<Integer, Integer> getProductionChance(String commodityId) {
        ProductionData pd = productionData.get(commodityId);
        if (pd == null) {
            return new Pair<>(0, 0);
        }
        int two = pd.chance.getModifiedInt();
        if (!building && !built) {

        } else if (!monthlyProductionEnabled) {
            two = 0;
        } else if (!ctx.getSourceIndustry().isFunctional()) {
            two = 0;
        } else if (!pd.requirements.requirementsMet(ctx)) {
            two = 0;
        }
        return new Pair<>((int) pd.chance.getBaseValue(), two);
    }

    public List<ProductionData> getProductionData() {
        List<ProductionData> ret = new ArrayList<>(productionData.values());
        Collections.sort(ret, new Comparator<ProductionData>() {
            @Override
            public int compare(ProductionData o1, ProductionData o2) {
                return Integer.compare(o1.priority, o2.priority);
            }
        });
        return ret;
    }

    public CargoAPI generateCargoForGatheringPoint(Random random) {
        if (!monthlyProductionEnabled) {
            return null;
        }
        if (!ctx.getSourceIndustry().isFunctional()) {
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
            int value = pd.chance.getModifiedInt();
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
