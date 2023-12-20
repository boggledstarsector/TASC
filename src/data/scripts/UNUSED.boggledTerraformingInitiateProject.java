package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import data.campaign.econ.conditions.Terraforming_Controller;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledTerraformingInitiateProject extends BaseCommandPlugin
{
    private static HashMap<String, String> initialiseTriggerProjectToProjectId() {
        HashMap<String, String> ret = new HashMap<>();

        ret.put(boggledTerraformingDialogPlugin.triggerCancelCurrentProject, boggledTools.noneProjectID);

//        ret.put(boggledTerraformingDialogPlugin.triggerAridTypeChange, boggledTools.aridTypeChangeProjectID);
//        ret.put(boggledTerraformingDialogPlugin.triggerFrozenTypeChange, boggledTools.frozenTypeChangeProjectID);
//        ret.put(boggledTerraformingDialogPlugin.triggerJungleTypeChange, boggledTools.jungleTypeChangeProjectID);
//        ret.put(boggledTerraformingDialogPlugin.triggerTerranTypeChange, boggledTools.terranTypeChangeProjectID);
//        ret.put(boggledTerraformingDialogPlugin.triggerTundraTypeChange, boggledTools.tundraTypeChangeProjectID);
//        ret.put(boggledTerraformingDialogPlugin.triggerWaterTypeChange, boggledTools.waterTypeChangeProjectID);

//        ret.put(boggledTerraformingDialogPlugin.triggerFarmlandResourceImprovement, boggledTools.farmlandResourceImprovementProjectID);
//        ret.put(boggledTerraformingDialogPlugin.triggerOrganicsResourceImprovement, boggledTools.organicsResourceImprovementProjectID);
//        ret.put(boggledTerraformingDialogPlugin.triggerVolatilesResourceImprovement, boggledTools.volatilesResourceImprovementProjectID);

//        ret.put(boggledTerraformingDialogPlugin.triggerExtremeWeatherConditionImprovement, boggledTools.extremeWeatherConditionImprovementProjectID);
//        ret.put(boggledTerraformingDialogPlugin.triggerMildClimateConditionImprovement, boggledTools.mildClimateConditionImprovementProjectID);
//        ret.put(boggledTerraformingDialogPlugin.triggerHabitableConditionImprovement, boggledTools.habitableConditionImprovementProjectID);
//        ret.put(boggledTerraformingDialogPlugin.triggerAtmosphereDensityConditionImprovement, boggledTools.atmosphereDensityConditionImprovementProjectID);
//        ret.put(boggledTerraformingDialogPlugin.triggerToxicAtmosphereConditionImprovement, boggledTools.toxicAtmosphereConditionImprovementProjectID);
//        ret.put(boggledTerraformingDialogPlugin.triggerIrradiatedConditionImprovement, boggledTools.irradiatedConditionImprovementProjectID);
//        ret.put(boggledTerraformingDialogPlugin.triggerRemoveAtmosphereConditionImprovement, boggledTools.removeAtmosphereConditionImprovementProjectID);

        return ret;
    }
    private static final HashMap<String, String> triggerProjectToProjectId = initialiseTriggerProjectToProjectId();

    protected SectorEntityToken entity;

    public boggledTerraformingInitiateProject() {}

    public boggledTerraformingInitiateProject(SectorEntityToken entity) {
        this.init(entity);
    }

    protected void init(SectorEntityToken entity)
    {
        this.entity = entity;
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        if(dialog == null) return false;

        MarketAPI market = dialog.getInteractionTarget().getMarket();
        Terraforming_Controller terraformingController = (Terraforming_Controller) market.getCondition(boggledTools.BoggledConditions.terraformingControllerConditionID).getPlugin();

        String projectId = triggerProjectToProjectId.get(ruleId);
        if (projectId != null) {
            terraformingController.setProject(projectId);
        }

        return true;
    }
}