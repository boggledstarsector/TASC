package boggled.scripts;

import boggled.campaign.econ.industries.BoggledCommonIndustry;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.combat.MutableStat;

import java.util.*;

public class BoggledTerraformingProject {
    public static class ProjectInstance {
        private BoggledTerraformingProject project;
        private int daysCompleted = 0;
        private int lastDayChecked = 0;

        public ProjectInstance(BoggledTerraformingProject project) {
            this.project = project;
            if (Global.getSector() == null) {
                // Happens when game first loads
                this.lastDayChecked = 0;
            } else {
                this.lastDayChecked = Global.getSector().getClock().getDay();
            }
        }

        public ProjectInstance(BoggledTerraformingProject project, int daysCompleted, int lastDayChecked) {
            this.project = project;
            this.daysCompleted = daysCompleted;
            this.lastDayChecked = lastDayChecked;
        }

        public Object readResolve() {
            BoggledTascPlugin.loadSettingsFromJSON();
            this.project = boggledTools.getProject(project.getId());
            return this;
        }

        public BoggledTerraformingProject getProject() { return project; }

        public int getPercentComplete(BoggledTerraformingRequirement.RequirementContext ctx) {
            return (int) Math.min(99, ((float)getDaysCompleted() / getProject().getModifiedProjectDuration(ctx)) * 100);
        }

        public int getDaysRemaining(BoggledTerraformingRequirement.RequirementContext ctx) {
            return project.getModifiedProjectDuration(ctx) - getDaysCompleted();
        }
        public int getDaysCompleted() { return daysCompleted; }
        public int getLastDayChecked() { return lastDayChecked; }

        public boolean advance(BoggledTerraformingRequirement.RequirementContext ctx) {
            CampaignClockAPI clock = Global.getSector().getClock();
            if (clock.getDay() == lastDayChecked) {
                return false;
            }
            lastDayChecked = clock.getDay();

            if (!project.requirementsMet(ctx)) {
                this.daysCompleted = 0;
                return false;
            }

            if (project.requirementsReset(ctx)) {
                this.daysCompleted = 0;
                return false;
            }

            if (project.requirementsStall(ctx)) {
                return false;
            }

            daysCompleted++;
            if (daysCompleted < project.getModifiedProjectDuration(ctx)) {
                return false;
            }

            project.finishProject(ctx, project.getProjectTooltip());

            return true;
        }
    }

    public static class RequirementsWithId {
        String id;
        BoggledProjectRequirementsAND requirements;

        public RequirementsWithId(String id, BoggledProjectRequirementsAND requirements) {
            this.id = id;
            this.requirements = requirements;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            RequirementsWithId that = (RequirementsWithId) object;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    public static class RequirementAddInfo {
        String containingId;
        List<BoggledProjectRequirementsAND.RequirementAdd> requirements;

        public RequirementAddInfo(String containingId, List<BoggledProjectRequirementsAND.RequirementAdd> requirements) {
            this.containingId = containingId;
            this.requirements = requirements;
        }
    }

    public static class RequirementRemoveInfo {
        String containingId;
        List<String> requirementIds;

        public RequirementRemoveInfo(String containingId, List<String> requirementIds) {
            this.containingId = containingId;
            this.requirementIds = requirementIds;
        }
    }

    private final String id;
    private final String[] enableSettings;
    private final String projectType;
    private final String projectTooltip;
    private final String intelCompleteMessage;

    private final String incompleteMessage;
    private final List<String> incompleteMessageHighlights;
    private final String disruptedMessage;
    private final List<String> disruptedMessageHighlights;
    // Multiple separate TerraformingRequirements form an AND'd collection
    // Each individual requirement inside the TerraformingRequirements forms an OR'd collection
    // ie If any of the conditions inside a TerraformingRequirements is fulfilled, that entire requirement is filled
    // But then all the TerraformingRequirements must be fulfilled for the project to be allowed
    // two is an optional description override

    private final BoggledProjectRequirementsAND requirements;
    private final BoggledProjectRequirementsAND requirementsHidden;

    private final List<RequirementsWithId> requirementsStall;
    private final List<RequirementsWithId> requirementsReset;

    private MutableStat baseProjectDuration;
    private final List<BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiers;

    private final List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectCompleteEffects;
    private final List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectOngoingEffects;

    public BoggledTerraformingProject(String id, String[] enableSettings, String projectType, String projectTooltip, String intelCompleteMessage, String incompleteMessage, List<String> incompleteMessageHighlights, String disruptedMessage, List<String> disruptedMessageHighlights, BoggledProjectRequirementsAND requirements, BoggledProjectRequirementsAND requirementsHidden, List<RequirementsWithId> requirementsStall, List<RequirementsWithId> requirementsReset, int baseProjectDuration, List<BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiers, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectCompleteEffects, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectOngoingEffects) {
        this.id = id;
        this.enableSettings = enableSettings;
        this.projectType = projectType;
        this.projectTooltip = projectTooltip;
        this.intelCompleteMessage = intelCompleteMessage;

        this.incompleteMessage = incompleteMessage;
        this.incompleteMessageHighlights = incompleteMessageHighlights;
        this.disruptedMessage = disruptedMessage;
        this.disruptedMessageHighlights = disruptedMessageHighlights;

        this.requirements = requirements;
        this.requirementsHidden = requirementsHidden;

        this.requirementsStall = requirementsStall;
        this.requirementsReset = requirementsReset;

        this.baseProjectDuration = new MutableStat(baseProjectDuration);
        this.durationModifiers = durationModifiers;

        this.projectCompleteEffects = projectCompleteEffects;
        this.projectOngoingEffects = projectOngoingEffects;
    }

    public String getId() { return id; }

    public String[] getEnableSettings() { return enableSettings; }

    public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

    public String getProjectType() { return projectType; }

    public String getProjectTooltip() {
        return projectTooltip;
    }

    public Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> getEffectTooltipInfo(BoggledTerraformingRequirement.RequirementContext ctx) {
        BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionMode descMode = BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionMode.TO_APPLY;
        ctx = new BoggledTerraformingRequirement.RequirementContext(ctx, this);
        Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> ret = new LinkedHashMap<>();
        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect effect : projectCompleteEffects) {
            effect.addEffectTooltipInfo(ctx, ret, "Terraforming", descMode, BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionSource.GENERIC);
        }
        return ret;
    }

    public Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> getOngoingEffectTooltipInfo(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource, BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionMode mode, BoggledTerraformingProjectEffect.TerraformingProjectEffect.DescriptionSource source) {
        ctx = new BoggledTerraformingRequirement.RequirementContext(ctx, this);
        Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> ret = new LinkedHashMap<>();
        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect effect : projectOngoingEffects) {
            effect.addEffectTooltipInfo(ctx, ret, effectSource, mode, source);
        }
        return ret;
    }

    public String getIntelCompleteMessage() { return intelCompleteMessage; }

    public String getIncompleteMessage() { return incompleteMessage; }

    public String[] getIncompleteMessageHighlights(Map<String, String> tokenReplacements) {
        List<String> replaced = new ArrayList<>(incompleteMessageHighlights.size());
        for (String highlight : incompleteMessageHighlights) {
            replaced.add(boggledTools.doTokenReplacement(highlight, tokenReplacements));
        }
        return replaced.toArray(new String[0]);
    }

    public String getDisruptedMessage() { return disruptedMessage; }

    public String[] getDisruptedMessageHighlights(Map<String, String> tokenReplacements) {
        List<String> replaced = new ArrayList<>(disruptedMessageHighlights.size());
        for (String highlight : disruptedMessageHighlights) {
            replaced.add(boggledTools.doTokenReplacement(highlight, tokenReplacements));
        }
        return replaced.toArray(new String[0]);
    }

    private String[] getStallResetMessages(BoggledTerraformingRequirement.RequirementContext ctx, List<RequirementsWithId> requirements) {
        List<String> ret = new ArrayList<>();
        Map<String, String> tokenReplacements = boggledTools.getTokenReplacements(ctx);
        for (RequirementsWithId requirement : requirements) {
            if (requirement.requirements.requirementsMet(ctx)) {
                List<BoggledCommonIndustry.TooltipData> tooltips = requirement.requirements.getTooltip(ctx, tokenReplacements, false, true);
                for (BoggledCommonIndustry.TooltipData tooltip : tooltips) {
                    ret.add(tooltip.text);
                }
            }
        }
        return ret.toArray(new String[0]);
    }

    public String[] getStallMessages(BoggledTerraformingRequirement.RequirementContext ctx) {
        return getStallResetMessages(ctx, requirementsStall);
    }

    public String[] getResetMessages(BoggledTerraformingRequirement.RequirementContext ctx) {
        return getStallResetMessages(ctx, requirementsReset);
    }

    public BoggledProjectRequirementsAND getRequirements() { return requirements; }

    public int getBaseProjectDuration() { return (int) baseProjectDuration.getBaseValue(); }
    public int getModifiedProjectDuration(BoggledTerraformingRequirement.RequirementContext ctx) {
        baseProjectDuration.unmodify();

        for (BoggledTerraformingDurationModifier.TerraformingDurationModifier durationModifier : durationModifiers) {
            baseProjectDuration.applyMods(durationModifier.getDurationModifier(ctx));
        }

        return Math.max(baseProjectDuration.getModifiedInt(), 0);
    }

    public boolean requirementsHiddenMet(BoggledTerraformingRequirement.RequirementContext ctx) {
        if (requirementsHidden == null) {
            Global.getLogger(this.getClass()).error("Terraforming hidden project requirements is null for project " + getId() + " and context " + ctx.getName());
            return false;
        }

        return requirementsHidden.requirementsMet(ctx);
    }

    public boolean requirementsMet(BoggledTerraformingRequirement.RequirementContext ctx) {
        if (requirements == null) {
            Global.getLogger(this.getClass()).error("Terraforming project requirements is null for project " + getId() + " and context " + ctx.getName());
            return false;
        }
        return requirementsHiddenMet(ctx) && requirements.requirementsMet(ctx);
    }

    public boolean requirementsStall(BoggledTerraformingRequirement.RequirementContext ctx) {
        for (RequirementsWithId requirementStall : requirementsStall) {
            if (requirementStall.requirements.requirementsMet(ctx)) {
                return true;
            }
        }
        return false;
    }

    public boolean requirementsReset(BoggledTerraformingRequirement.RequirementContext ctx) {
        for (RequirementsWithId requirementReset : requirementsReset) {
            if (requirementReset.requirements.requirementsMet(ctx)) {
                return true;
            }
        }
        return false;
    }

    public void startProject(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
        ctx = new BoggledTerraformingRequirement.RequirementContext(ctx, this);

        applyOngoingEffects(ctx, effectSource);
    }

    public void cancelProject(BoggledTerraformingRequirement.RequirementContext ctx) {
        ctx = new BoggledTerraformingRequirement.RequirementContext(ctx, this);

        unapplyOngoingEffects(ctx);
    }

    public void finishProject(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
        if (projectCompleteEffects.isEmpty()) {
            return;
        }

        ctx = new BoggledTerraformingRequirement.RequirementContext(ctx, this);

        unapplyOngoingEffects(ctx);

        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect effect : projectCompleteEffects) {
            effect.applyProjectEffect(ctx, effectSource);
        }

        String intelTooltip = getProjectTooltip();
        String intelCompletedMessage = getIntelCompleteMessage();

        boggledTools.surveyAll(ctx.getClosestMarket());

        boggledTools.showProjectCompleteIntelMessage(intelTooltip, intelCompletedMessage, ctx.getClosestMarket());
    }

    public void applyOngoingEffects(BoggledTerraformingRequirement.RequirementContext ctx, String effectSource) {
        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect effect : projectOngoingEffects) {
            effect.applyProjectEffect(ctx, effectSource);
        }
    }

    public void unapplyOngoingEffects(BoggledTerraformingRequirement.RequirementContext ctx) {
        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect effect : projectOngoingEffects) {

            effect.unapplyProjectEffect(ctx);
        }
    }

    /*
    From here on are mod helper functions
    */
    private void addProjectRequirements(BoggledProjectRequirementsAND reqToModify, List<BoggledProjectRequirementsAND.RequirementAdd> reqsToAdd) {
        for (BoggledProjectRequirementsAND.RequirementAdd reqToAdd : reqsToAdd) {
            reqToModify.addRequirement(reqToAdd);
        }
    }

    private void addProjectRequirements(List<RequirementsWithId> requirements, List<RequirementAddInfo> requirementsToAdd) {
        for (RequirementAddInfo reqAddInfo : requirementsToAdd) {
            BoggledProjectRequirementsAND req = null;
            for (RequirementsWithId reqWithId : requirements) {
                if (reqWithId.id.equals(reqAddInfo.containingId)) {
                    req = reqWithId.requirements;
                    break;
                }
            }
            if (req == null) {
                req = new BoggledProjectRequirementsAND();
                requirements.add(new RequirementsWithId(reqAddInfo.containingId, req));
            }
            addProjectRequirements(req, reqAddInfo.requirements);
        }
    }

    private void removeProjectRequirements(BoggledProjectRequirementsAND reqToModify, List<String> reqsToRemove) {
        for (String reqToRemove : reqsToRemove) {
            reqToModify.removeRequirement(reqToRemove);
        }
    }

    private void removeProjectRequirements(List<RequirementsWithId> requirements, List<RequirementRemoveInfo> requirementRemoveInfo) {
        for (RequirementRemoveInfo reqRemoveInfo : requirementRemoveInfo) {
            BoggledProjectRequirementsAND req = null;
            for (RequirementsWithId reqWithId : requirements) {
                if (reqWithId.id.equals(reqRemoveInfo.containingId)) {
                    req = reqWithId.requirements;
                }
            }
            if (req == null) {
                continue;
            }
            removeProjectRequirements(req, reqRemoveInfo.requirementIds);
        }
    }

    private void removeProjectEffects(List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectEffects, List<String> effectsToRemove) {
        for (String effectToRemove : effectsToRemove) {
            int idx;
            for (idx = 0; idx < projectEffects.size(); ++idx) {
                BoggledTerraformingProjectEffect.TerraformingProjectEffect projectEffect = projectEffects.get(idx);
                if (projectEffect.id.equals(effectToRemove)) {
                    break;
                }
            }
            if (idx == projectEffects.size()) {
                continue;
            }
            projectEffects.remove(idx);
        }
    }

    public void addRemoveProjectRequirements(List<BoggledProjectRequirementsAND.RequirementAdd> reqsAdded, List<String> reqsRemove, List<BoggledProjectRequirementsAND.RequirementAdd> reqsHiddenAdded, List<String> reqsHiddenRemove, List<RequirementAddInfo> reqsStallAdded, List<RequirementRemoveInfo> reqsStallRemove, List<RequirementAddInfo> reqsResetAdded, List<RequirementRemoveInfo> reqsResetRemove) {
        addProjectRequirements(requirements, reqsAdded);
        removeProjectRequirements(requirements, reqsRemove);

        addProjectRequirements(requirementsHidden, reqsHiddenAdded);
        removeProjectRequirements(requirementsHidden, reqsHiddenRemove);

        addProjectRequirements(requirementsStall, reqsStallAdded);
        removeProjectRequirements(requirementsStall, reqsStallRemove);

        addProjectRequirements(requirementsReset, reqsResetAdded);
        removeProjectRequirements(requirementsReset, reqsResetRemove);
    }

    public void addRemoveDurationModifiersAndDuration(Integer baseProjectDurationOverride, List<BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiersAdded, List<String> durationModifiersRemoved) {
        if (baseProjectDurationOverride != null) {
            baseProjectDuration = new MutableStat(baseProjectDurationOverride);
        }

        durationModifiers.addAll(durationModifiersAdded);
        for (String durationModifierRemoved : durationModifiersRemoved) {
            int idx;
            for (idx = 0; idx < durationModifiers.size(); ++idx) {
                BoggledTerraformingDurationModifier.TerraformingDurationModifier durationModifier = durationModifiers.get(idx);
                if (durationModifier.id.equals(durationModifierRemoved)) {
                    break;
                }
            }
            if (idx == durationModifiers.size()) {
                continue;
            }
            durationModifiers.remove(idx);
        }
    }

    public void addRemoveProjectEffects(List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectCompleteEffectsAdded, List<String> projectCompleteEffectsRemoved, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectOngoingEffectsAdded, List<String> projectOngoingEffectsRemoved) {
        projectCompleteEffects.addAll(projectCompleteEffectsAdded);
        removeProjectEffects(projectCompleteEffects, projectCompleteEffectsRemoved);

        projectOngoingEffects.addAll(projectOngoingEffectsAdded);
        removeProjectEffects(projectOngoingEffects, projectOngoingEffectsRemoved);
    }
}
