package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import data.campaign.econ.boggledTools;
import org.apache.log4j.Logger;

import java.util.ArrayList;

public class BoggledTerraformingProject {
    private final String projectId;
    private final String[] enableSettings;
    private final String projectType;
    private String projectTooltip;
    // Multiple separate TerraformingRequirements form an AND'd collection
    // Each individual requirement inside the TerraformingRequirements forms an OR'd collection
    // ie If any of the conditions inside a TerraformingRequirements is fulfilled, that entire requirement is filled
    // But then all the TerraformingRequirements must be fulfilled for the project to be allowed
    // two is an optional description override

    private final BoggledProjectRequirementsAND projectRequirements;
    private final BoggledProjectRequirementsAND projectRequirementsHidden;

    private final ArrayList<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectEffects;

    private final int baseProjectDuration;
    private final ArrayList<BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiers;

    public String getProjectId() {
        return projectId;
    }

    public String[] getEnableSettings() {
        return enableSettings;
    }

    public boolean isEnabled() {
        return boggledTools.optionsAllowThis(enableSettings);
    }

    public String getProjectType() {
        return projectType;
    }

    public String getProjectTooltip() {
        return projectTooltip;
    }

    public BoggledProjectRequirementsAND getProjectRequirements() {
        return projectRequirements;
    }

    public int getModifiedProjectDuration(MarketAPI market) {
        float projectDuration = baseProjectDuration;
        for (BoggledTerraformingDurationModifier.TerraformingDurationModifier durationModifier : durationModifiers) {
            projectDuration += durationModifier.getDurationModifier(market, baseProjectDuration);
        }
        return Math.max((int) projectDuration, 0);
    }

    public boolean requirementsHiddenMet(MarketAPI market) {
        Logger log = Global.getLogger(boggledTools.class);
        log.info("Checking requirements hidden for project " + getProjectId() + " for market " + market.getName());

        if (projectRequirementsHidden == null) {
            log.error("Terraforming hidden project requirements is null for project " + getProjectId()
                    + " and market " + market.getName());
            return false;
        }

        return projectRequirementsHidden.requirementsMet(market);
    }

    public boolean requirementsMet(MarketAPI market) {
        Logger log = Global.getLogger(boggledTools.class);
        log.info("Checking project requirements for project " + getProjectId() + " for market " + market.getName());

        if (projectRequirements == null) {
            log.error("Terraforming project requirements is null for project " + getProjectId() + " and market " + market.getName());
            return false;
        }
        return requirementsHiddenMet(market) && projectRequirements.requirementsMet(market);
    }

    public void finishProject(MarketAPI market, String intelTooltip, String intelCompletedMessage) {
        for (BoggledTerraformingProjectEffect.TerraformingProjectEffect effect : projectEffects) {
            Global.getLogger(this.getClass()).info("Doing effect " + effect.getClass());
            effect.applyProjectEffect(market);
        }

        boggledTools.surveyAll(market);
        boggledTools.refreshSupplyAndDemand(market);
        boggledTools.refreshAquacultureAndFarming(market);

        boggledTools.showProjectCompleteIntelMessage(intelTooltip, intelCompletedMessage, market.getName(), market);
    }

    public void finishProject(MarketAPI market) {
        finishProject(market, getProjectTooltip(), "Completed");
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

    public BoggledTerraformingProject(String projectId, String[] enableSettings, String projectType, String projectTooltip, BoggledProjectRequirementsAND projectRequirements, BoggledProjectRequirementsAND projectRequirementsHidden, int baseProjectDuration, ArrayList<BoggledTerraformingDurationModifier.TerraformingDurationModifier> durationModifiers, ArrayList<BoggledTerraformingProjectEffect.TerraformingProjectEffect> projectEffects) {
        this.projectId = projectId;
        this.enableSettings = enableSettings;
        this.projectType = projectType;
        this.projectTooltip = projectTooltip;
        this.projectRequirements = projectRequirements;
        this.projectRequirementsHidden = projectRequirementsHidden;

        this.baseProjectDuration = baseProjectDuration;
        this.durationModifiers = durationModifiers;

        this.projectEffects = projectEffects;
    }
}
