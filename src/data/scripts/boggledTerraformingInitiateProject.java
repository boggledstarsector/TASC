package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.conditions.Terraforming_Controller;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledTerraformingInitiateProject extends BaseCommandPlugin
{
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

        this.entity = dialog.getInteractionTarget();
        TextPanelAPI text = dialog.getTextPanel();

        MarketAPI market = this.entity.getMarket();
        PlanetAPI planet = market.getPlanetEntity();
        Terraforming_Controller terraformingController = (Terraforming_Controller) market.getCondition("terraforming_controller").getPlugin();
        String currentProject = terraformingController.getProject();

        if(ruleId.equals("boggledTriggerCancelCurrentProject"))
        {
            terraformingController.setProject("None");
        }
        else if(ruleId.equals("boggledTriggerAridTypeChange"))
        {
            terraformingController.setProject("aridTypeChange");
        }
        else if(ruleId.equals("boggledTriggerFrozenTypeChange"))
        {
            terraformingController.setProject("frozenTypeChange");
        }
        else if(ruleId.equals("boggledTriggerJungleTypeChange"))
        {
            terraformingController.setProject("jungleTypeChange");
        }
        else if(ruleId.equals("boggledTriggerTerranTypeChange"))
        {
            terraformingController.setProject("terranTypeChange");
        }
        else if(ruleId.equals("boggledTriggerTundraTypeChange"))
        {
            terraformingController.setProject("tundraTypeChange");
        }
        else if(ruleId.equals("boggledTriggerWaterTypeChange"))
        {
            terraformingController.setProject("waterTypeChange");
        }
        else if(ruleId.equals("boggledTriggerFarmlandResourceImprovement"))
        {
            terraformingController.setProject("farmlandResourceImprovement");
        }
        else if(ruleId.equals("boggledTriggerOrganicsResourceImprovement"))
        {
            terraformingController.setProject("organicsResourceImprovement");
        }
        else if(ruleId.equals("boggledTriggerVolatilesResourceImprovement"))
        {
            terraformingController.setProject("volatilesResourceImprovement");
        }
        else if(ruleId.equals("boggledTriggerExtremeWeatherConditionImprovement"))
        {
            terraformingController.setProject("extremeWeatherConditionImprovement");
        }
        else if(ruleId.equals("boggledTriggerMildClimateConditionImprovement"))
        {
            terraformingController.setProject("mildClimateConditionImprovement");
        }
        else if(ruleId.equals("boggledTriggerHabitableConditionImprovement"))
        {
            terraformingController.setProject("habitableConditionImprovement");
        }
        else if(ruleId.equals("boggledTriggerAtmosphereDensityConditionImprovement"))
        {
            terraformingController.setProject("atmosphereDensityConditionImprovement");
        }
        else if(ruleId.equals("boggledTriggerToxicAtmosphereConditionImprovement"))
        {
            terraformingController.setProject("toxicAtmosphereConditionImprovement");
        }
        else if(ruleId.equals("boggledTriggerIrradiatedConditionImprovement"))
        {
            terraformingController.setProject("irradiatedConditionImprovement");
        }
        else if(ruleId.equals("boggledTriggerRemoveAtmosphereConditionImprovement"))
        {
            terraformingController.setProject("removeAtmosphereConditionImprovement");
        }

        return true;
    }
}