
package data.campaign.econ.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;

import java.util.Map;

import data.campaign.econ.boggledTools;

public class Terraforming_Controller extends BaseHazardCondition
{
    private static class BoggledSettings {
        public static String terraformingTime = "boggledTerraformingTime";
        public static String resourceImprovementTime = "boggledResourceImprovementTime";
        public static String conditionImprovementTime = resourceImprovementTime;
    }

    private static class BoggledTags {
        public static String terraformingControllerDaysCompleted = "boggledTerraformingControllerDaysCompleted_";
        public static String terraformingControllerLastDayChecked = "boggledTerraformingControllerLastDayChecked_";
        public static String terraformingControllerCurrentProject = "boggledTerraformingControllerCurrentProject_";
    }

    public Terraforming_Controller() { }

    public int daysRequiredForTypeChange = boggledTools.getIntSetting(BoggledSettings.terraformingTime);
    public int daysRequiredForResourceImprovement = boggledTools.getIntSetting(BoggledSettings.resourceImprovementTime);
    public int daysRequiredForConditionImprovement = boggledTools.getIntSetting(BoggledSettings.conditionImprovementTime);

    private int daysRequiredForCurrentProject;

    private int daysCompleted = 0;
    private int lastDayChecked = 0;

    private String currentProject = null;

    private void loadVariables()
    {
        daysRequiredForTypeChange = boggledTools.getIntSetting(BoggledSettings.terraformingTime);
        daysRequiredForResourceImprovement = boggledTools.getIntSetting(BoggledSettings.resourceImprovementTime);
        daysRequiredForConditionImprovement = boggledTools.getIntSetting(BoggledSettings.conditionImprovementTime);

        if (currentProject != null)
        {
            if (currentProject.contains(boggledTools.typeChangeProjectKey)) {
                daysRequiredForCurrentProject = daysRequiredForTypeChange;
            } else if (currentProject.contains(boggledTools.resourceImprovementKey)) {
                daysRequiredForCurrentProject = daysRequiredForResourceImprovement;
            } else if (currentProject.contains(boggledTools.conditionImprovementKey)) {
                daysRequiredForCurrentProject = daysRequiredForConditionImprovement;
            } else {
                daysRequiredForCurrentProject = 0;
            }
        }

        for (String tag : market.getTags()) {
            if (tag.contains(BoggledTags.terraformingControllerDaysCompleted)) {
                daysCompleted = Integer.parseInt(tag.replace(BoggledTags.terraformingControllerDaysCompleted, ""));
            } else if (tag.contains(BoggledTags.terraformingControllerLastDayChecked)) {
                lastDayChecked = Integer.parseInt(tag.replace(BoggledTags.terraformingControllerLastDayChecked, ""));
            } else if (tag.contains(BoggledTags.terraformingControllerCurrentProject)) {
                currentProject = tag.replace(BoggledTags.terraformingControllerCurrentProject, "");
            }
        }
    }

    private void storeVariables()
    {
        boggledTools.clearBoggledTerraformingControllerTags(market);

        market.addTag(BoggledTags.terraformingControllerDaysCompleted + daysCompleted);
        market.addTag(BoggledTags.terraformingControllerLastDayChecked + lastDayChecked);
        if(currentProject == null)
        {
            market.addTag(BoggledTags.terraformingControllerCurrentProject + "None");
        }
        else
        {
            market.addTag(BoggledTags.terraformingControllerCurrentProject + currentProject);
        }
    }

    public String getProject()
    {
        loadVariables();

        if(currentProject == null)
        {
            return boggledTools.noneProjectId;
        }
        else
        {
            return currentProject;
        }
    }

    public void setProject(String project)
    {
        daysCompleted = 0;
        lastDayChecked = Global.getSector().getClock().getDay();
        currentProject = project;

        if(market.isPlayerOwned() || market.getFaction().isPlayerFaction())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            if(project.equals(boggledTools.noneProjectId))
            {
                intel.addLine("    - Canceled");
            }
            else
            {
                intel.addLine("    - Started");
            }
            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }

        storeVariables();
    }

    public int getDaysCompleted()
    {
        loadVariables();

        return daysCompleted;
    }

    public int getDaysRequired()
    {
        loadVariables();

        return daysRequiredForCurrentProject;
    }

    public int getDaysRemaining()
    {
        return getDaysRequired() - getDaysCompleted();
    }

    public int getPercentComplete()
    {
        Double daysCompleted = (double) getDaysCompleted();
        Double daysRequired = (double) getDaysRequired();

        double percentCompete = (daysCompleted / daysRequired) * 100;
        int returnValue = (int) percentCompete;
        return Math.min(returnValue, 99);
    }

    public void advance(float amount)
    {
        loadVariables();

        super.advance(amount);

        if(!(market.isPlayerOwned() || market.getFaction().isPlayerFaction()) || boggledTools.marketIsStation(market))
        {
            boggledTools.removeCondition(market, boggledTools.BoggledConditions.terraformingControllerConditionId);
            return;
        }

        if(currentProject == null || currentProject.equals(boggledTools.noneProjectId))
        {
            daysCompleted = 0;
            lastDayChecked = 0;
        }
        else
        {
            CampaignClockAPI clock = Global.getSector().getClock();
            if(clock.getDay() != lastDayChecked)
            {
                if(boggledTools.projectRequirementsMet(market, currentProject))
                {
                    daysCompleted++;
                    lastDayChecked = clock.getDay();

                    if (daysCompleted >= daysRequiredForCurrentProject) {
                        boggledTools.TerraformingProject terraformingProject = boggledTools.getProject(currentProject);
                        if (terraformingProject != null) {
                            terraformingProject.finishProject(market);
                        } else {
                            Global.getLogger(Terraforming_Controller.class).error("Couldn't find TerraformingProject for project " + currentProject);
                        }

                        currentProject = null;
                        daysCompleted = 0;
                        lastDayChecked = 0;

                        if (market.isPlayerOwned() || market.getFaction().isPlayerFaction())
                        {
                            MessageIntel intel = new MessageIntel("Terraforming on " + market.getName(), Misc.getBasePlayerColor());
                            intel.addLine("    - Completed");
                            intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                            intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                        }
                    }
                }
                else
                {
                    lastDayChecked = clock.getDay();
                }
            }
        }

        storeVariables();
    }

    public void apply(String id) { super.apply(id); }

    public void unapply(String id) { super.unapply(id); }

    public Map<String, String> getTokenReplacements() { return super.getTokenReplacements(); }

    public boolean showIcon() { return false; }
}
