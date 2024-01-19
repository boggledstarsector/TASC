package boggled.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import boggled.campaign.econ.boggledTools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        public Object readResolve() {
            Global.getLogger(this.getClass()).info("Doing readResolve for ProjectInstance");
            this.project = boggledTools.getProject(project.getProjectId());
            return this;
        }

        public BoggledTerraformingProject getProject() { return project; }
        public int getDaysCompleted() { return daysCompleted; }
        public int getLastDayChecked() { return lastDayChecked; }

        public boolean advance(BoggledTerraformingRequirement.RequirementContext ctx) {
            CampaignClockAPI clock = Global.getSector().getClock();
            if (clock.getDay() == lastDayChecked) {
                return false;
            }
            lastDayChecked = clock.getDay();

            if (!project.requirementsMet(ctx)) {
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

            project.finishProject(ctx);
            return true;
        }
    }

    private final String projectId;
    private final String[] enableSettings;
    private final String projectType;
    private final String projectTooltip;
    private final String intelCompleteMessage;

    private final String incompleteMessage;
    private final List<String> incompleteMessageHighlights;
    // Multiple separate TerraformingRequirements form an AND'd collection
    // Each individual requirement inside the TerraformingRequirements forms an OR'd collection
    // ie If any of the conditions inside a TerraformingRequirements is fulfilled, that entire requirement is filled
    // But then all the TerraformingRequirements must be fulfilled for the project to be allowed
    // two is an optional description override

    private final BoggledProjectRequirementsAND projectRequirements;
    private final BoggledProjectRequirementsAND projectRequirementsHidden;

    private final int baseProjectDuration;
    private final List<BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiers;

    private final List<BoggledProjectRequirementsAND> requirementsStall;
    private final List<BoggledProjectRequirementsAND> requirementsReset;

    private final List<BoggledTerraformingProjectEffect.ProjectEffectWithRequirement> projectEffects;

    public BoggledTerraformingProject(String projectId, String[] enableSettings, String projectType, String projectTooltip, String intelCompleteMessage, String incompleteMessage, List<String> incompleteMessageHighlights, BoggledProjectRequirementsAND projectRequirements, BoggledProjectRequirementsAND projectRequirementsHidden, int baseProjectDuration, List<BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiers, List<BoggledProjectRequirementsAND> requirementsStall, List<BoggledProjectRequirementsAND> requirementsReset, List<BoggledTerraformingProjectEffect.ProjectEffectWithRequirement> projectEffects) {
        this.projectId = projectId;
        this.enableSettings = enableSettings;
        this.projectType = projectType;
        this.projectTooltip = projectTooltip;
        this.intelCompleteMessage = intelCompleteMessage;

        this.incompleteMessage = incompleteMessage;
        this.incompleteMessageHighlights = incompleteMessageHighlights;

        this.projectRequirements = projectRequirements;
        this.projectRequirementsHidden = projectRequirementsHidden;

        this.baseProjectDuration = baseProjectDuration;
        this.durationModifiers = durationModifiers;

        this.requirementsStall = requirementsStall;
        this.requirementsReset = requirementsReset;

        this.projectEffects = projectEffects;
    }

    public String getProjectId() { return projectId; }

    public String[] getEnableSettings() { return enableSettings; }

    public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

    public String getProjectType() { return projectType; }

    public String getProjectTooltip(Map<String, String> tokenReplacements) {
        for (BoggledTerraformingProjectEffect.ProjectEffectWithRequirement projectEffect : projectEffects) {
            projectEffect.effect.addTokenReplacements(tokenReplacements);
        }
        return boggledTools.doTokenReplacement(projectTooltip, tokenReplacements);
    }

    public Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> getEffectTooltipInfo(BoggledTerraformingRequirement.RequirementContext ctx) {
        Map<String, BoggledTerraformingProjectEffect.EffectTooltipPara> ret = new LinkedHashMap<>();
        for (BoggledTerraformingProjectEffect.ProjectEffectWithRequirement effect : projectEffects) {
            effect.effect.addEffectTooltipInfo(ctx, ret);
        }
        return ret;
    }

    public String getIntelCompleteMessage() { return intelCompleteMessage; }

    public String getIncompleteMessage() { return incompleteMessage; }

    public String[] getIncompleteMessageHighlights(Map<String, String> tokenReplacements) {
        ArrayList<String> replaced = new ArrayList<>(incompleteMessageHighlights.size());
        for (String highlight : incompleteMessageHighlights) {
            replaced.add(boggledTools.doTokenReplacement(highlight, tokenReplacements));
        }
        return replaced.toArray(new String[0]);
    }

    public BoggledProjectRequirementsAND getProjectRequirements() { return projectRequirements; }

    public int getModifiedProjectDuration(BoggledTerraformingRequirement.RequirementContext ctx) {
        float projectDuration = baseProjectDuration;
        for (BoggledTerraformingDurationModifier.TerraformingDurationModifier durationModifier : durationModifiers) {
            projectDuration += durationModifier.getDurationModifier(ctx, baseProjectDuration);
        }
        return Math.max((int) projectDuration, 0);
    }

    public boolean requirementsHiddenMet(BoggledTerraformingRequirement.RequirementContext ctx) {
        if (projectRequirementsHidden == null) {
            Global.getLogger(this.getClass()).error("Terraforming hidden project requirements is null for project " + getProjectId() + " and context " + ctx.getName());
            return false;
        }

        return projectRequirementsHidden.requirementsMet(ctx);
    }

    public boolean requirementsMet(BoggledTerraformingRequirement.RequirementContext ctx) {
        if (projectRequirements == null) {
            Global.getLogger(this.getClass()).error("Terraforming project requirements is null for project " + getProjectId() + " and context " + ctx.getName());
            return false;
        }
        return requirementsHiddenMet(ctx) && projectRequirements.requirementsMet(ctx);
    }

    public boolean requirementsStall(BoggledTerraformingRequirement.RequirementContext ctx) {
        for (BoggledProjectRequirementsAND requirementStall : requirementsStall) {
            if (requirementStall.requirementsMet(ctx)) {
                return true;
            }
        }
        return false;
    }

    public boolean requirementsReset(BoggledTerraformingRequirement.RequirementContext ctx) {
        for (BoggledProjectRequirementsAND requirementReset : requirementsReset) {
            if (requirementReset.requirementsMet(ctx)) {
                return true;
            }
        }
        return false;
    }

    public void finishProject(BoggledTerraformingRequirement.RequirementContext ctx) {
        for (BoggledTerraformingProjectEffect.ProjectEffectWithRequirement effect : projectEffects) {
            effect.effect.applyProjectEffectImpl(ctx);
        }

        String intelTooltip = getProjectTooltip(boggledTools.getTokenReplacements(ctx));
        String intelCompletedMessage = getIntelCompleteMessage();

        boggledTools.surveyAll(ctx.getMarket());
        boggledTools.refreshSupplyAndDemand(ctx.getMarket());
        boggledTools.refreshAquacultureAndFarming(ctx.getMarket());

        boggledTools.showProjectCompleteIntelMessage(intelTooltip, intelCompletedMessage, ctx.getMarket());
    }

//    public void overrideAddTooltip(String tooltipOverride, String tooltipAddition) {
//        if (!tooltipOverride.isEmpty()) {
//            projectTooltip = tooltipOverride;
//        }
//        projectTooltip += tooltipAddition;
//    }

//    public void addRemoveProjectRequirements(ArrayList<BoggledProjectRequirementsAND.RequirementWithTooltipOverride> add, String[] remove) {
//        Logger log = Global.getLogger(BoggledTerraformingProject.class);
//        for (String r : remove) {
//            for (int i = 0; i < projectRequirements.size(); ++i) {
//                BoggledProjectRequirementsOR projectReqs = projectRequirements.get(i).requirements;
//                if (r.equals(projectReqs.getRequirementId())) {
//                    log.info("Project " + projectId + " removing project requirement " + r);
//                    projectRequirements.remove(i);
//                    break;
//                }
//            }
//        }
//
//        projectRequirements.addAll(add);
//    }
}
