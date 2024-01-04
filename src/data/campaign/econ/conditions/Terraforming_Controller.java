
package data.campaign.econ.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;

import java.util.Map;

import data.campaign.econ.boggledTools;
import data.scripts.BoggledTerraformingProject;

public class Terraforming_Controller extends BaseHazardCondition {

    private BoggledTerraformingProject.ProjectInstance currentProject = null;

    @Override
    public boolean isTransient() {
        return false;
    }

    public BoggledTerraformingProject getProject() {
        if (currentProject == null) return null;
        return currentProject.getProject();
    }

    public void setProject(BoggledTerraformingProject project) {
        if (currentProject == null && project == null) {
            return;
        }
        BoggledTerraformingProject projectToCheck = project == null ? currentProject.getProject() : project;

        currentProject = project == null ? null : new BoggledTerraformingProject.ProjectInstance(project);

        if(market.isPlayerOwned() || market.getFaction().isPlayerFaction()) {
            String projectTooltip = projectToCheck.getProjectTooltip(boggledTools.getTokenReplacements(market));
            MessageIntel intel = new MessageIntel(projectTooltip + " on " + market.getName(), Misc.getBasePlayerColor());
            if (project == null) {
                intel.addLine("    - Canceled");
            } else {
                intel.addLine("    - Started");
            }
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public int getDaysCompleted() {
        if (currentProject == null) {
            return 0;
        }
        return currentProject.getDaysCompleted();
    }

    public int getDaysRequired() {
        if (currentProject == null) {
            return 0;
        }
        return currentProject.getProject().getModifiedProjectDuration(market);
    }

    public int getDaysRemaining() {
        return getDaysRequired() - getDaysCompleted();
    }

    public void advance(float amount) {
        super.advance(amount);

        if(!(market.isPlayerOwned() || market.getFaction().isPlayerFaction()) || boggledTools.marketIsStation(market)) {
            boggledTools.removeCondition(market, boggledTools.BoggledConditions.terraformingControllerConditionId);
            return;
        }

        if (currentProject != null) {
            if (currentProject.advance(market)) {
                currentProject = null;
            }
        }
    }

    public void apply(String id) { super.apply(id); }

    public void unapply(String id) { super.unapply(id); }

    public Map<String, String> getTokenReplacements() { return super.getTokenReplacements(); }

    public boolean showIcon() { return false; }
}
