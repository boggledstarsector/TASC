package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import data.campaign.econ.boggledTools;

import java.util.ArrayList;
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

        public boolean advance(MarketAPI market) {
            CampaignClockAPI clock = Global.getSector().getClock();
            if (clock.getDay() == lastDayChecked) {
                return false;
            }
            lastDayChecked = clock.getDay();

            if (!project.requirementsMet(market)) {
                return false;
            }

            if (project.requirementsReset(market)) {
                this.daysCompleted = 0;
                return false;
            }

            if (project.requirementsStall(market)) {
                return false;
            }

            daysCompleted++;
            if (daysCompleted < project.getModifiedProjectDuration(market)) {
                return false;
            }

            project.finishProject(market);
            return true;
        }
    }

    private final String projectId;
    private final String[] enableSettings;
    private final String projectType;
    private String projectTooltip;
    private String intelCompleteMessage;

    private final String incompleteMessage;
    private final ArrayList<String> incompleteMessageHighlights;
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

    private final List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectEffects;

    public BoggledTerraformingProject(String projectId, String[] enableSettings, String projectType, String projectTooltip, String intelCompleteMessage, String incompleteMessage, ArrayList<String> incompleteMessageHighlights, BoggledProjectRequirementsAND projectRequirements, BoggledProjectRequirementsAND projectRequirementsHidden, int baseProjectDuration, List<BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiers, List<BoggledProjectRequirementsAND> requirementsStall, List<BoggledProjectRequirementsAND> requirementsReset, List<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectEffects) {
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
        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect projectEffect : projectEffects) {
            projectEffect.addTokenReplacements(tokenReplacements);
        }
        return boggledTools.doTokenReplacement(projectTooltip, tokenReplacements);
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

    public int getModifiedProjectDuration(MarketAPI market) {
        float projectDuration = baseProjectDuration;
        for (BoggledTerraformingDurationModifier.TerraformingDurationModifier durationModifier : durationModifiers) {
            projectDuration += durationModifier.getDurationModifier(market, baseProjectDuration);
        }
        return Math.max((int) projectDuration, 0);
    }

    public boolean requirementsHiddenMet(MarketAPI market) {
        if (projectRequirementsHidden == null) {
            Global.getLogger(this.getClass()).error("Terraforming hidden project requirements is null for project " + getProjectId()
                    + " and market " + market.getName());
            return false;
        }

        return projectRequirementsHidden.requirementsMet(market);
    }

    public boolean requirementsMet(MarketAPI market) {
        if (projectRequirements == null) {
            Global.getLogger(this.getClass()).error("Terraforming project requirements is null for project " + getProjectId() + " and market " + market.getName());
            return false;
        }
        return requirementsHiddenMet(market) && projectRequirements.requirementsMet(market);
    }

    public boolean requirementsStall(MarketAPI market) {
        for (BoggledProjectRequirementsAND requirementStall : requirementsStall) {
            if (requirementStall.requirementsMet(market)) {
                return true;
            }
        }
        return false;
    }

    public boolean requirementsReset(MarketAPI market) {
        for (BoggledProjectRequirementsAND requirementReset : requirementsReset) {
            if (requirementReset.requirementsMet(market)) {
                return true;
            }
        }
        return false;
    }

    public void finishProject(MarketAPI market) {
        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect effect : projectEffects) {
            effect.applyProjectEffect(market);
        }

        String intelTooltip = getProjectTooltip(boggledTools.getTokenReplacements(market));
        String intelCompletedMessage = getIntelCompleteMessage();

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);
        boggledTools.refreshAquacultureAndFarming(market);

        boggledTools.showProjectCompleteIntelMessage(intelTooltip, intelCompletedMessage, market);
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
