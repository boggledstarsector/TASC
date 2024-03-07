package boggled.campaign.econ.industries;

import boggled.scripts.*;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import boggled.campaign.econ.boggledTools;

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
    public List<BoggledTerraformingProject.ProjectInstance> attachedProjects;

    private List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> buildingFinishedEffects;
    private List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> improveEffects;

    private Map<String, List<BoggledTerraformingProject.ProjectInstance>> aiCoreEffects;

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
    private boolean built = true;

    private BoggledTerraformingProject.ProjectInstance findCorrespondingProjectInstance(BoggledTerraformingProject.ProjectInstance that) {
        for (BoggledTerraformingProject.ProjectInstance project : this.projects) {
            if (!project.getProject().getId().equals(that.getProject().getId())) {
                continue;
            }
            return project;
        }
        return null;
    }

    private List<BoggledTerraformingProject.ProjectInstance> getUpdatedProjectInstanceList(List<BoggledTerraformingProject.ProjectInstance> that) {
        List<BoggledTerraformingProject.ProjectInstance> projects = new ArrayList<>(that.size());
        for (BoggledTerraformingProject.ProjectInstance project : that) {
            BoggledTerraformingProject.ProjectInstance thisProject = findCorrespondingProjectInstance(project);
            BoggledTerraformingProject.ProjectInstance replacedProject = project;
            if (thisProject != null) {
                replacedProject = thisProject;
            }
            projects.add(new BoggledTerraformingProject.ProjectInstance(replacedProject.getProject(), replacedProject.getDaysCompleted(), replacedProject.getLastDayChecked()));
        }
        return projects;
    }

    private void setFromThat(BoggledCommonIndustry that) {
        this.projects = getUpdatedProjectInstanceList(that.projects);
        this.attachedProjects = getUpdatedProjectInstanceList(that.attachedProjects);

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
        this.attachedProjects = new ArrayList<>();

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

    public BoggledCommonIndustry(String industryId, String industryTooltip, List<BoggledTerraformingProject> projects, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> buildingFinishedEffects, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> improveEffects, Map<String, List<BoggledTerraformingProject>> aiCoreEffects, List<BoggledProjectRequirementsAND> disruptRequirements, float basePatherInterest, List<ImageOverrideWithRequirement> imageReqs, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> preBuildEffects) {
        this.ctx = null;
        this.industryId = industryId;
        this.industryTooltip = industryTooltip;

        this.projects = new ArrayList<>(projects.size());
        for (BoggledTerraformingProject project : projects) {
            this.projects.add(new BoggledTerraformingProject.ProjectInstance(project));
        }
        this.attachedProjects = new ArrayList<>();

        this.buildingFinishedEffects = buildingFinishedEffects;
        this.improveEffects = improveEffects;
        this.aiCoreEffects = new HashMap<>();
        for (Map.Entry<String, List<BoggledTerraformingProject>> aiCoreEffect : aiCoreEffects.entrySet()) {
            List<BoggledTerraformingProject.ProjectInstance> aiCoreProjectInstances = new ArrayList<>();
            for (BoggledTerraformingProject project : aiCoreEffect.getValue()) {
                aiCoreProjectInstances.add(new BoggledTerraformingProject.ProjectInstance(project));
            }
            this.aiCoreEffects.put(aiCoreEffect.getKey(), aiCoreProjectInstances);
        }

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

    public void advance(float amount) {
        if (!built) {
            return;
        }

        if (ctx.getSourceIndustry().isDisrupted()) {
            return;
        }

        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
            project.advance(instanceCtx);
        }
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
        boolean allProjectValid = true;
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            allProjectValid = allProjectValid && project.getProject().requirementsHiddenMet(ctx);
        }
        return allProjectValid;
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
        building = false;
        built = true;
        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect buildingFinishedEffect : buildingFinishedEffects) {
            buildingFinishedEffect.applyProjectEffect(ctx, ctx.getSourceIndustry().getNameForModifier());
        }
    }

    public boolean isBuilding() {
        if (building) {
            return true;
        }
        if (!built) {
            // Stupid as hell but needs to be here for the industry to work same as vanilla structures
            return false;
        }

        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
            if (project.getProject().requirementsMet(instanceCtx) && project.getDaysRemaining(instanceCtx) > 0) {
                return true;
            }
        }

        for (BoggledTerraformingProject.ProjectInstance project : attachedProjects) {
            BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
            if (project.getDaysRemaining(instanceCtx) > 0) {
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
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
            if (project.getProject().requirementsMet(instanceCtx) && project.getDaysRemaining(instanceCtx) > 0) {
                return true;
            }
        }

        for (BoggledTerraformingProject.ProjectInstance project : attachedProjects) {
            BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
            if (project.getDaysRemaining(instanceCtx) > 0) {
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
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
            if (project.getProject().requirementsMet(instanceCtx) && project.getDaysRemaining(instanceCtx) > 0) {
                progress = Math.max(project.getPercentComplete(ctx) / 100f, progress);
            }
        }

        for (BoggledTerraformingProject.ProjectInstance project : attachedProjects) {
            BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
            if (project.getDaysRemaining(instanceCtx) > 0) {
                progress = Math.max(project.getPercentComplete(instanceCtx) / 100f, progress);
            }
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
            for (BoggledTerraformingProject.ProjectInstance project : projects) {
                BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
                int daysRemaining = project.getDaysRemaining(instanceCtx);
                if (project.getProject().requirementsMet(instanceCtx) && daysRemaining > 0) {
                    daysRemain = Math.min(daysRemaining, daysRemain);
                }
            }

            for (BoggledTerraformingProject.ProjectInstance project : attachedProjects) {
                BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
                int daysRemaining = project.getDaysRemaining(instanceCtx);
                if (daysRemaining > 0) {
                    daysRemain = Math.min(daysRemaining, daysRemain);
                }
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
        } else if (!attachedProjects.isEmpty()) {
            prefix = "Terraforming";
            for (BoggledTerraformingProject.ProjectInstance project : attachedProjects) {
                BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
                int daysRemaining = project.getDaysRemaining(instanceCtx);
                if (daysRemaining > 0) {
                    prefix = project.getProject().getProjectType();
                    break;
                }
            }
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
                BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
                if (project.getProject().requirementsMet(instanceCtx)) {
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
                BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
                if (project.getProject().requirementsHiddenMet(instanceCtx)) {
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
        if (!ctx.getSourceIndustry().isFunctional()) {
            return;
        }

        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
            project.getProject().applyOngoingEffects(instanceCtx, instanceCtx.getSourceIndustry().getNameForModifier());
        }

        List<BoggledTerraformingProject.ProjectInstance> coreEffect = aiCoreEffects.get(ctx.getSourceIndustry().getAICoreId());
        if (coreEffect != null) {
            String effectSource = Global.getSettings().getCommoditySpec(ctx.getSourceIndustry().getAICoreId()).getName();
            for (BoggledTerraformingProject.ProjectInstance projectInstance : coreEffect) {
                BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, projectInstance);
                BoggledTerraformingProject project = projectInstance.getProject();
                if (project.requirementsStall(instanceCtx)) {
                    project.unapplyOngoingEffects(instanceCtx);
                } else {
                    project.applyOngoingEffects(instanceCtx, effectSource);
                }
            }
        } else {
            for (List<BoggledTerraformingProject.ProjectInstance> removedCoreEffect : aiCoreEffects.values()) {
                for (BoggledTerraformingProject.ProjectInstance projectInstance : removedCoreEffect) {
                    BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, projectInstance);
                    BoggledTerraformingProject project = projectInstance.getProject();
                    project.unapplyOngoingEffects(instanceCtx);
                }
            }
        }
    }

    public void unapply() {
        for (BoggledTerraformingProject.ProjectInstance project : projects) {
            BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
            project.getProject().unapplyOngoingEffects(instanceCtx);
        }

        for (List<BoggledTerraformingProject.ProjectInstance> coreEffect : aiCoreEffects.values()) {
            for (BoggledTerraformingProject.ProjectInstance projectInstance : coreEffect) {
                projectInstance.getProject().unapplyOngoingEffects(ctx);
            }
        }
    }

    private void addRightAfterDescriptionSectionProject(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode, BoggledTerraformingProject.ProjectInstance projectInstance) {
        float pad = 10.0f;
        BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, projectInstance);
        BoggledTerraformingProject project = projectInstance.getProject();
        if (!project.requirementsMet(instanceCtx)) {
            return;
        }

        Map<String, String> tokenReplacements = getTokenReplacements(projectInstance);

        if (projectInstance.getDaysRemaining(instanceCtx) > 0) {
            String[] highlights = project.getIncompleteMessageHighlights(tokenReplacements);
            String incompleteMessage = boggledTools.doTokenAndFormatReplacement(project.getIncompleteMessage(), tokenReplacements);
            tooltipIncomplete(tooltip, mode, incompleteMessage, pad, Misc.getHighlightColor(), highlights);
        }

        if (instanceCtx.getSourceIndustry().isDisrupted()) {
            String[] highlights = project.getDisruptedMessageHighlights(tokenReplacements);
            String disruptedMessage = boggledTools.doTokenReplacement(project.getDisruptedMessage(), tokenReplacements);
            tooltipDisrupted(tooltip, mode, disruptedMessage, pad, Misc.getHighlightColor(), highlights);
        }

        String[] stallMessages = project.getStallMessages(instanceCtx);
        for (String stallMessage : stallMessages) {
            if (stallMessage.isEmpty()) {
                continue;
            }
            tooltip.addPara(stallMessage, Misc.getNegativeHighlightColor(), pad);
        }

        String[] resetMessages = project.getResetMessages(instanceCtx);
        for (String resetMessage : resetMessages) {
            if (resetMessage.isEmpty()) {
                continue;
            }
            tooltip.addPara(resetMessage, Misc.getNegativeHighlightColor(), pad);
        }
    }

    public void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode) {
        List<BoggledTerraformingProject.ProjectInstance> aiCoreEffect = aiCoreEffects.get(ctx.getSourceIndustry().getAICoreId());
        if (aiCoreEffect != null) {
            for (BoggledTerraformingProject.ProjectInstance projectInstance : aiCoreEffect) {
                addRightAfterDescriptionSectionProject(tooltip, mode, projectInstance);
            }
        }

        for (BoggledTerraformingProject.ProjectInstance projectInstance : projects) {
            addRightAfterDescriptionSectionProject(tooltip, mode, projectInstance);
        }

        for (BoggledTerraformingProject.ProjectInstance projectInstance : attachedProjects) {
            addRightAfterDescriptionSectionProject(tooltip, mode, projectInstance);
        }
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
                if (para.prefix.isEmpty() && para.suffix.isEmpty()) {
                    if (para.infix.isEmpty()) {
                        continue;
                    }
                    boolean allEmpty = true;
                    for (String infix : para.infix) {
                        if (!infix.isEmpty()) {
                            allEmpty = false;
                            break;
                        }
                    }
                    if (allEmpty) {
                        continue;
                    }
                }
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
        String prefix = coreType + "-level AI core currently assigned.";
        BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionMode descMode = BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionMode.TO_APPLY;
        if (mode == Industry.AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == Industry.AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            prefix = coreType + "-level AI core.";
        }

        StringBuilder builder = new StringBuilder(prefix);
        List<String> highlights = new ArrayList<>();
        List<Color> highlightColors = new ArrayList<>();
        List<BoggledTerraformingProject.ProjectInstance> coreEffect = aiCoreEffects.get(coreId);
        if (coreEffect != null) {
            for (BoggledTerraformingProject.ProjectInstance projectInstance : coreEffect) {
                String effectSource = Global.getSettings().getCommoditySpec(coreId).getName() + " assigned";
                Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> effectTypeToPara = projectInstance.getProject().getOngoingEffectTooltipInfo(ctx, effectSource, descMode, BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionSource.AI_CORE_DESCRIPTION);

                for (BoggledTerraformingProjectEffect.EffectTooltipPara effectTooltipPara : effectTypeToPara.values()) {
                    if (builder.length() != 0) {
                        builder.append(" ");
                    }
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
            improveEffect.applyProjectEffect(ctx, "Improvements");
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

    private Map<String, String> getTokenReplacements(BoggledTerraformingProject.ProjectInstance project) {
        BoggledTerraformingRequirement.RequirementContext instanceCtx = new BoggledTerraformingRequirement.RequirementContext(ctx, project);
        Map<String, String> ret = boggledTools.getTokenReplacements(instanceCtx);
        ret.put("$percentComplete", Integer.toString(project.getPercentComplete(instanceCtx)));
        return ret;
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
        String id;
        BoggledProjectRequirementsAND requirements;
        String category;
        String imageId;

        public ImageOverrideWithRequirement(String id, BoggledProjectRequirementsAND requirements, String category, String imageId) {
            this.id = id;
            this.requirements = requirements;
            this.category = category;
            this.imageId = imageId;
        }
    }

    public String getCurrentImage() {
        for (ImageOverrideWithRequirement req : imageReqs) {
            if (req.requirements.requirementsMet(ctx)) {
                return Global.getSettings().getSpriteName(req.category, req.imageId);
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

    public int getLastProductionRoll() {
        return lastProductionRoll;
    }

    int lastProductionRoll = 0;

    public CargoAPI generateCargoForGatheringPoint(Random random) {
        if (!monthlyProductionEnabled) {
            return null;
        }
        if (!ctx.getSourceIndustry().isFunctional()) {
            return null;
        }
        // If the roll is less than chance, give the item and return
        // Goes from smaller priority value items to bigger priority value items
        // The smaller the priority, the higher the priority, with zero being the highest priority
        // If multiple items share the same priority, order is unspecified
        // Number in range (0, 100], ie possible values are from 1 to 100 inclusive both ends
        int roll = random.nextInt(100) + 1;
        lastProductionRoll = roll;
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
                continue;
            }
            int chance = pd.chance.getModifiedInt();
            if (chance == 0) {
                continue;
            }
            if (roll < chance) {
                ret.addCommodity(pd.commodityId, 1);
                return ret;
            }
        }
        return null;
    }

    void attachProject(BoggledTerraformingProject.ProjectInstance projectInstance) {
        int idx = attachedProjects.indexOf(projectInstance);
        if (idx != -1) {
            return;
        }
        attachedProjects.add(projectInstance);
    }

    void detachProject(BoggledTerraformingProject.ProjectInstance projectInstance) {
        int idx = attachedProjects.indexOf(projectInstance);
        if (idx == -1) {
            return;
        }
        attachedProjects.remove(idx);
    }

    /*
    From here on are mod helper functions
     */
    private void addEffects(List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> effects, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> effectsToAdd) {
        effects.addAll(effectsToAdd);
    }

    private void removeEffects(List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> effects, List<String> effectsToRemove) {
        for (String effectToRemove : effectsToRemove) {
            int idx;
            for (idx = 0; idx < effects.size(); ++idx) {
                BoggledTerraformingProjectEffect.TerraformingProjectEffect effect = effects.get(idx);
                if (effect.getId().equals(effectToRemove)) {
                    break;
                }
            }
            if (idx == effects.size()) {
                continue;
            }
            effects.remove(idx);
        }
    }

    public void addRemoveProjects(List<BoggledTerraformingProject> projectsAdded, List<String> projectsRemoved) {
        for (BoggledTerraformingProject projectAdded : projectsAdded) {
            projects.add(new BoggledTerraformingProject.ProjectInstance(projectAdded));
        }

        for (String projectRemoved : projectsRemoved) {
            int idx;
            for (idx = 0; idx < projects.size(); ++idx) {
                BoggledTerraformingProject.ProjectInstance project = projects.get(idx);
                if (project.getProject().getId().equals(projectRemoved)) {
                    break;
                }
            }
            if (idx == projects.size()) {
                continue;
            }
            projects.remove(idx);
        }
    }

    public void addRemoveBuildingFinishImproveAiCorePrebuildEffects(List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> buildingFinishedEffectsAdded, List<String> buildingFinishedEffectsRemoved, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> improveEffectsAdded, List<String> improveEffectsRemoved, Map<String, List<BoggledTerraformingProject>> aiCoreEffectsAdded, Map<String, List<String>> aiCoreEffectsRemoved, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> preBuildEffectsAdded, List<String> preBuildEffectsRemoved) {
        addEffects(buildingFinishedEffects, buildingFinishedEffectsAdded);
        removeEffects(buildingFinishedEffects, buildingFinishedEffectsRemoved);

        addEffects(improveEffects, improveEffectsAdded);
        removeEffects(improveEffects, improveEffectsRemoved);

        for (Map.Entry<String, List<BoggledTerraformingProject>> aiCoreEffectAdded : aiCoreEffectsAdded.entrySet()) {
            List<BoggledTerraformingProject.ProjectInstance> aiCoreEffect = aiCoreEffects.get(aiCoreEffectAdded.getKey());
            if (aiCoreEffect != null) {
                for (BoggledTerraformingProject project : aiCoreEffectAdded.getValue()) {
                    aiCoreEffect.add(new BoggledTerraformingProject.ProjectInstance(project));
                }
            }
        }

        for (Map.Entry<String, List<String>> aiCoreEffectRemoved : aiCoreEffectsRemoved.entrySet()) {
            List<BoggledTerraformingProject.ProjectInstance> aiCoreEffect = aiCoreEffects.get(aiCoreEffectRemoved.getKey());
            if (aiCoreEffect != null) {
                for (String effectToRemove : aiCoreEffectRemoved.getValue()) {
                    int idx;
                    for (idx = 0; idx < aiCoreEffect.size(); ++idx) {
                        BoggledTerraformingProject.ProjectInstance effect = aiCoreEffect.get(idx);
                        if (effect.getProject().getId().equals(effectToRemove)) {
                            break;
                        }
                    }
                    if (idx == aiCoreEffect.size()) {
                        continue;
                    }
                    aiCoreEffect.remove(idx);
                }
            }
        }

        addEffects(preBuildEffects, preBuildEffectsAdded);
        removeEffects(preBuildEffects, preBuildEffectsRemoved);
    }

    public void addRemoveImageOverrides(List<ImageOverrideWithRequirement> imageOverridesAdded, List<String> imageOverridesRemoved) {
        imageReqs.addAll(imageOverridesAdded);
        for (String imageOverrideRemoved : imageOverridesRemoved) {
            int idx;
            for (idx = 0; idx < imageReqs.size(); ++idx) {
                ImageOverrideWithRequirement imageReq = imageReqs.get(idx);
                if (imageReq.id.equals(imageOverrideRemoved)) {
                    break;
                }
            }
            if (idx == imageReqs.size()) {
                continue;
            }
            imageReqs.remove(idx);
        }
    }

    public void overrideCanBeDisruptedAndBasePatherInterest(boolean canBeDisruptedOverride, float basePatherInterestOverride) {
        basePatherInterest = basePatherInterestOverride;
    }
}
