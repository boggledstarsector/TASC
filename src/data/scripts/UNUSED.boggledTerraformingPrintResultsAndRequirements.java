package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import data.campaign.econ.conditions.Terraforming_Controller;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledTerraformingPrintResultsAndRequirements extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledTerraformingPrintResultsAndRequirements() {}

    public boggledTerraformingPrintResultsAndRequirements(SectorEntityToken entity) {
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

        if(this.entity.getMarket() == null || boggledTools.marketIsStation(this.entity.getMarket()))
        {
            return true;
        }
        else
        {
            Color highlight = Misc.getHighlightColor();
            Color good = Misc.getPositiveHighlightColor();
            Color bad = Misc.getNegativeHighlightColor();

            MarketAPI market = this.entity.getMarket();
            PlanetAPI planet = market.getPlanetEntity();
            Terraforming_Controller terraformingController = (Terraforming_Controller) market.getCondition(boggledTools.BoggledConditions.terraformingControllerConditionID).getPlugin();
            String currentProject = terraformingController.getProject();

            if(ruleId.equals("boggledCancelCurrentProjectMenu"))
            {
                text.addPara("%s", bad, new String[]{"You will lose all progress on your current project if you cancel it!"});
                return true;
            }
            else if(ruleId.equals("boggledAridTypeChangeYes") || ruleId.equals("boggledAridTypeChangeNo"))
            {
                boggledTools.printProjectResults(market, "arid_type_change", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "arid_type_change",text);
            }
            else if(ruleId.equals("boggledFrozenTypeChangeYes") || ruleId.equals("boggledFrozenTypeChangeNo"))
            {
                boggledTools.printProjectResults(market, "frozen_type_change", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "frozen_type_change",text);
            }
            else if(ruleId.equals("boggledJungleTypeChangeYes") || ruleId.equals("boggledJungleTypeChangeNo"))
            {
                boggledTools.printProjectResults(market, "jungle_type_change", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "jungle_type_change",text);
            }
            else if(ruleId.equals("boggledTerranTypeChangeYes") || ruleId.equals("boggledTerranTypeChangeNo"))
            {
                boggledTools.printProjectResults(market, "terran_type_change", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "terran_type_change",text);
            }
            else if(ruleId.equals("boggledTundraTypeChangeYes") || ruleId.equals("boggledTundraTypeChangeNo"))
            {
                boggledTools.printProjectResults(market, "tundra_type_change", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "tundra_type_change",text);
            }
            else if(ruleId.equals("boggledWaterTypeChangeYes") || ruleId.equals("boggledWaterTypeChangeNo"))
            {
                boggledTools.printProjectResults(market, "water_type_change", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "water_type_change",text);
            }
            else if(ruleId.equals("boggledFarmlandResourceImprovementYes") || ruleId.equals("boggledFarmlandResourceImprovementNo"))
            {
                boggledTools.printProjectResults(market, "farmland_resource_mprovement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "farmland_resource_improvement",text);
            }
            else if(ruleId.equals("boggledOrganicsResourceImprovementYes") || ruleId.equals("boggledOrganicsResourceImprovementNo"))
            {
                boggledTools.printProjectResults(market, "organics_resource_improvement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "organics_resource_improvement",text);
            }
            else if(ruleId.equals("boggledVolatilesResourceImprovementYes") || ruleId.equals("boggledVolatilesResourceImprovementNo"))
            {
                boggledTools.printProjectResults(market, "volatiles_resource_improvement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "volatiles_resource_improvement",text);
            }
            else if(ruleId.equals("boggledExtremeWeatherConditionImprovementYes") || ruleId.equals("boggledExtremeWeatherConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "extreme_weather_condition_improvement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "extreme_weather_condition_improvement",text);
            }
            else if(ruleId.equals("boggledMildClimateConditionImprovementYes") || ruleId.equals("boggledMildClimateConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "mild_climate_condition_improvement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "mild_climate_condition_improvement",text);
            }
            else if(ruleId.equals("boggledHabitableConditionImprovementYes") || ruleId.equals("boggledHabitableConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "habitable_condition_improvement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "habitable_condition_improvement",text);
            }
            else if(ruleId.equals("boggledAtmosphereDensityConditionImprovementYes") || ruleId.equals("boggledAtmosphereDensityConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "atmosphere_density_condition_improvement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "atmosphere_density_condition_improvement",text);
            }
            else if(ruleId.equals("boggledToxicAtmosphereConditionImprovementYes") || ruleId.equals("boggledToxicAtmosphereConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "toxic_atmosphere_condition_improvement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "toxic_atmosphere_condition_improvement",text);
            }
            else if(ruleId.equals("boggledIrradiatedConditionImprovementYes") || ruleId.equals("boggledIrradiatedConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "irradiated_condition_improvement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "irradiated_condition_improvement",text);
            }
            else if(ruleId.equals("boggledRemoveAtmosphereConditionImprovementYes") || ruleId.equals("boggledRemoveAtmosphereConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "remove_atmosphere_condition_improvement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "remove_atmosphere_condition_improvement",text);
            }

            if(!currentProject.equals("None"))
            {
                text.addPara("%s", bad, new String[]{"You will lose all progress on your current project if you start a new one!"});
            }
        }

        return true;
    }
}