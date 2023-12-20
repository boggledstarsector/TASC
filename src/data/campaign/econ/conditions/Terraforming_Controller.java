
package data.campaign.econ.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;

import static java.util.Arrays.asList;

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

    private static HashMap<String, Pair<ArrayList<String>, ArrayList<String>>> initialiseProjectChangeConditions() {
        HashMap<String, Pair<ArrayList<String>, ArrayList<String>>> ret = new HashMap<>();
        // one is conditions added, two is conditions removed
        // Resource improvement and planet type change has to be handled separately for now, so they're not in here
        ArrayList<String> extremeWeatherConditionsAdded = new ArrayList<>();
        ArrayList<String> extremeWeatherConditionsRemoved = new ArrayList<>(asList(
                Conditions.EXTREME_WEATHER
        ));

        ArrayList<String> mildClimateConditionsAdded = new ArrayList<>(asList(
                Conditions.MILD_CLIMATE
        ));
        ArrayList<String> mildClimateConditionsRemoved = new ArrayList<>();

        ArrayList<String> habitableConditionsAdded = new ArrayList<>(asList(
                Conditions.HABITABLE
        ));
        ArrayList<String> habitableConditionsRemoved = new ArrayList<>();

        ArrayList<String> atmosphereDensityConditionsAdded = new ArrayList<>(asList(
                Conditions.NO_ATMOSPHERE,
                Conditions.THIN_ATMOSPHERE,
                Conditions.DENSE_ATMOSPHERE
        ));
        ArrayList<String> atmosphereDensityConditionsRemoved = new ArrayList<>();

        ArrayList<String> toxicAtmosphereConditionsAdded = new ArrayList<>();
        ArrayList<String> toxicAtmosphereConditionsRemoved = new ArrayList<>(asList(
                Conditions.TOXIC_ATMOSPHERE
        ));

        ArrayList<String> irradiatedConditionsAdded = new ArrayList<>();
        ArrayList<String> irradiatedConditionsRemoved = new ArrayList<>(asList(
                Conditions.IRRADIATED
        ));

        ArrayList<String> removeAtmosphereConditionsAdded = new ArrayList<>(asList(
                Conditions.NO_ATMOSPHERE
        ));
        ArrayList<String> removeAtmosphereConditionsRemoved = new ArrayList<>(asList(
                Conditions.THIN_ATMOSPHERE,
                Conditions.TOXIC_ATMOSPHERE,
                Conditions.DENSE_ATMOSPHERE,
                Conditions.POLLUTION,
                Conditions.INIMICAL_BIOSPHERE,
                Conditions.EXTREME_WEATHER,
                Conditions.MILD_CLIMATE,
                Conditions.HABITABLE
        ));

        ret.put(boggledTools.extremeWeatherConditionImprovementProjectID, new Pair<>(extremeWeatherConditionsAdded, extremeWeatherConditionsRemoved));
        ret.put(boggledTools.mildClimateConditionImprovementProjectID, new Pair<>(mildClimateConditionsAdded, mildClimateConditionsRemoved));
        ret.put(boggledTools.habitableConditionImprovementProjectID, new Pair<>(habitableConditionsAdded, habitableConditionsRemoved));
        ret.put(boggledTools.atmosphereDensityConditionImprovementProjectID, new Pair<>(atmosphereDensityConditionsAdded, atmosphereDensityConditionsRemoved));
        ret.put(boggledTools.toxicAtmosphereConditionImprovementProjectID, new Pair<>(toxicAtmosphereConditionsAdded, toxicAtmosphereConditionsRemoved));
        ret.put(boggledTools.irradiatedConditionImprovementProjectID, new Pair<>(irradiatedConditionsAdded, irradiatedConditionsRemoved));
        ret.put(boggledTools.removeAtmosphereConditionImprovementProjectID, new Pair<>(removeAtmosphereConditionsAdded, removeAtmosphereConditionsRemoved));

        // Modded conditions go here, check for mod and then add to relevant section

        return ret;
    }

    // one is conditions added, two is conditions removed
    private final HashMap<String, Pair<ArrayList<String>, ArrayList<String>>> projectChangeConditions = initialiseProjectChangeConditions();

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
            if (currentProject.contains("TypeChange")) {
                daysRequiredForCurrentProject = daysRequiredForTypeChange;
            } else if (currentProject.contains("ResourceImprovement")) {
                daysRequiredForCurrentProject = daysRequiredForResourceImprovement;
            } else if (currentProject.contains("ConditionImprovement")) {
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
            return "None";
        }
        else
        {
            return currentProject;
        }
    }

    public void setProject(String project)
    {
        daysCompleted = 0;
        lastDayChecked = 0;
        currentProject = project;

        if(market.isPlayerOwned() || market.getFaction().isPlayerFaction())
        {
            MessageIntel intel = new MessageIntel("Terraforming of " + market.getName(), Misc.getBasePlayerColor());
            if(project.equals(boggledTools.noneProjectID))
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
            boggledTools.removeCondition(market, boggledTools.terraformingControllerConditionID);
            return;
        }

        if(currentProject == null || currentProject.equals(boggledTools.noneProjectID))
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

                        Pair<ArrayList<String>, ArrayList<String>> conditionsAddedRemoved = projectChangeConditions.get(currentProject);
                        if (conditionsAddedRemoved != null) {
                            for (String conditionAdded : conditionsAddedRemoved.one) {
                                boggledTools.addCondition(market, conditionAdded);
                            }
                            for (String conditionRemoved : conditionsAddedRemoved.two) {
                                boggledTools.removeCondition(market, conditionRemoved);
                            }
                        } else {
                            switch (currentProject) {
                                case boggledTools.aridTypeChangeProjectID:
                                case boggledTools.frozenTypeChangeProjectID:
                                case boggledTools.jungleTypeChangeProjectID:
                                case boggledTools.terranTypeChangeProjectID:
                                case boggledTools.tundraTypeChangeProjectID:
                                case boggledTools.waterTypeChangeProjectID:
                                    boggledTools.terraformVariantToVariant(market, currentProject);
                                    break;
                                case boggledTools.farmlandResourceImprovementProjectID:
                                    boggledTools.incrementFarmland(market);
                                    break;
                                case boggledTools.organicsResourceImprovementProjectID:
                                    boggledTools.incrementOrganics(market);
                                    break;
                                case boggledTools.volatilesResourceImprovementProjectID:
                                    boggledTools.incrementVolatiles(market);
                                    break;
                            }
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
