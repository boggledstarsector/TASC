package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledTerraformingProjectRequirementsMet extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledTerraformingProjectRequirementsMet() {}

    public boggledTerraformingProjectRequirementsMet(SectorEntityToken entity) {
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

        if(ruleId.contains("Crafting"))
        {
            return boggledTools.projectRequirementsMet(market, "Crafting");
        }
        else if(ruleId.equals("boggledAridTypeChangeYes") || ruleId.equals("boggledAridTypeChangeNo"))
        {
            return boggledTools.projectRequirementsMet(market, "arid_type_change");
        }
        else if(ruleId.equals("boggledFrozenTypeChangeYes") || ruleId.equals("boggledFrozenTypeChangeNo"))
        {
            return boggledTools.projectRequirementsMet(market, "frozen_type_change");
        }
        else if(ruleId.equals("boggledJungleTypeChangeYes") || ruleId.equals("boggledJungleTypeChangeNo"))
        {
            return boggledTools.projectRequirementsMet(market, "jungle_type_change");
        }
        else if(ruleId.equals("boggledTerranTypeChangeYes") || ruleId.equals("boggledTerranTypeChangeNo"))
        {
            return boggledTools.projectRequirementsMet(market, "terran_type_change");
        }
        else if(ruleId.equals("boggledTundraTypeChangeYes") || ruleId.equals("boggledTundraTypeChangeNo"))
        {
            return boggledTools.projectRequirementsMet(market, "tundra_type_change");
        }
        else if(ruleId.equals("boggledWaterTypeChangeYes") || ruleId.equals("boggledWaterTypeChangeNo"))
        {
            return boggledTools.projectRequirementsMet(market, "water_type_change");
        }
        else if(ruleId.equals("boggledFarmlandResourceImprovementYes") || ruleId.equals("boggledFarmlandResourceImprovementNo"))
        {
            return boggledTools.projectRequirementsMet(market, "farmland_resource_improvement");
        }
        else if(ruleId.equals("boggledOrganicsResourceImprovementYes") || ruleId.equals("boggledOrganicsResourceImprovementNo"))
        {
            return boggledTools.projectRequirementsMet(market, "organics_resource_improvement");
        }
        else if(ruleId.equals("boggledVolatilesResourceImprovementYes") || ruleId.equals("boggledVolatilesResourceImprovementNo"))
        {
            return boggledTools.projectRequirementsMet(market, "volatiles_resource_improvement");
        }
        else if(ruleId.equals("boggledExtremeWeatherConditionImprovementYes") || ruleId.equals("boggledExtremeWeatherConditionImprovementNo"))
        {
            return boggledTools.projectRequirementsMet(market, "extreme_weather_condition_improvement");
        }
        else if(ruleId.equals("boggledMildClimateConditionImprovementYes") || ruleId.equals("boggledMildClimateConditionImprovementNo"))
        {
            return boggledTools.projectRequirementsMet(market, "mild_climate_condition_improvement");
        }
        else if(ruleId.equals("boggledHabitableConditionImprovementYes") || ruleId.equals("boggledHabitableConditionImprovementNo"))
        {
            return boggledTools.projectRequirementsMet(market, "habitable_condition_improvement");
        }
        else if(ruleId.equals("boggledAtmosphereDensityConditionImprovementYes") || ruleId.equals("boggledAtmosphereDensityConditionImprovementNo"))
        {
            return boggledTools.projectRequirementsMet(market, "atmosphere_density_condition_improvement");
        }
        else if(ruleId.equals("boggledToxicAtmosphereConditionImprovementYes") || ruleId.equals("boggledToxicAtmosphereConditionImprovementNo"))
        {
            return boggledTools.projectRequirementsMet(market, "toxic_atmosphere_condition_improvement");
        }
        else if(ruleId.equals("boggledIrradiatedConditionImprovementYes") || ruleId.equals("boggledIrradiatedConditionImprovementNo"))
        {
            return boggledTools.projectRequirementsMet(market, "irradiated_condition_improvement");
        }
        else if(ruleId.equals("boggledRemoveAtmosphereConditionImprovementYes") || ruleId.equals("boggledRemoveAtmosphereConditionImprovementNo"))
        {
            return boggledTools.projectRequirementsMet(market, "remove_atmosphere_condition_improvement");
        }

        return true;
    }
}