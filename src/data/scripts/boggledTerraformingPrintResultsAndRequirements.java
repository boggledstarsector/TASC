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
            Terraforming_Controller terraformingController = (Terraforming_Controller) market.getCondition("terraforming_controller").getPlugin();
            String currentProject = terraformingController.getProject();

            if(ruleId.equals("boggledCancelCurrentProjectMenu"))
            {
                text.addPara("%s", bad, new String[]{"You will lose all progress on your current project if you cancel it!"});
                return true;
            }
            else if(ruleId.equals("boggledAridTypeChangeYes") || ruleId.equals("boggledAridTypeChangeNo"))
            {
                boggledTools.printProjectResults(market, "aridTypeChange", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "aridTypeChange",text);
            }
            else if(ruleId.equals("boggledFrozenTypeChangeYes") || ruleId.equals("boggledFrozenTypeChangeNo"))
            {
                boggledTools.printProjectResults(market, "frozenTypeChange", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "frozenTypeChange",text);
            }
            else if(ruleId.equals("boggledJungleTypeChangeYes") || ruleId.equals("boggledJungleTypeChangeNo"))
            {
                boggledTools.printProjectResults(market, "jungleTypeChange", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "jungleTypeChange",text);
            }
            else if(ruleId.equals("boggledTerranTypeChangeYes") || ruleId.equals("boggledTerranTypeChangeNo"))
            {
                boggledTools.printProjectResults(market, "terranTypeChange", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "terranTypeChange",text);
            }
            else if(ruleId.equals("boggledTundraTypeChangeYes") || ruleId.equals("boggledTundraTypeChangeNo"))
            {
                boggledTools.printProjectResults(market, "tundraTypeChange", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "tundraTypeChange",text);
            }
            else if(ruleId.equals("boggledWaterTypeChangeYes") || ruleId.equals("boggledWaterTypeChangeNo"))
            {
                boggledTools.printProjectResults(market, "waterTypeChange", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "waterTypeChange",text);
            }
            else if(ruleId.equals("boggledFarmlandResourceImprovementYes") || ruleId.equals("boggledFarmlandResourceImprovementNo"))
            {
                boggledTools.printProjectResults(market, "farmlandResourceImprovement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "farmlandResourceImprovement",text);
            }
            else if(ruleId.equals("boggledOrganicsResourceImprovementYes") || ruleId.equals("boggledOrganicsResourceImprovementNo"))
            {
                boggledTools.printProjectResults(market, "organicsResourceImprovement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "organicsResourceImprovement",text);
            }
            else if(ruleId.equals("boggledVolatilesResourceImprovementYes") || ruleId.equals("boggledVolatilesResourceImprovementNo"))
            {
                boggledTools.printProjectResults(market, "volatilesResourceImprovement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "volatilesResourceImprovement",text);
            }
            else if(ruleId.equals("boggledExtremeWeatherConditionImprovementYes") || ruleId.equals("boggledExtremeWeatherConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "extremeWeatherConditionImprovement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "extremeWeatherConditionImprovement",text);
            }
            else if(ruleId.equals("boggledMildClimateConditionImprovementYes") || ruleId.equals("boggledMildClimateConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "mildClimateConditionImprovement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "mildClimateConditionImprovement",text);
            }
            else if(ruleId.equals("boggledHabitableConditionImprovementYes") || ruleId.equals("boggledHabitableConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "habitableConditionImprovement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "habitableConditionImprovement",text);
            }
            else if(ruleId.equals("boggledAtmosphereDensityConditionImprovementYes") || ruleId.equals("boggledAtmosphereDensityConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "atmosphereDensityConditionImprovement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "atmosphereDensityConditionImprovement",text);
            }
            else if(ruleId.equals("boggledToxicAtmosphereConditionImprovementYes") || ruleId.equals("boggledToxicAtmosphereConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "toxicAtmosphereConditionImprovement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "toxicAtmosphereConditionImprovement",text);
            }
            else if(ruleId.equals("boggledIrradiatedConditionImprovementYes") || ruleId.equals("boggledIrradiatedConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "irradiatedConditionImprovement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "irradiatedConditionImprovement",text);
            }
            else if(ruleId.equals("boggledRemoveAtmosphereConditionImprovementYes") || ruleId.equals("boggledRemoveAtmosphereConditionImprovementNo"))
            {
                boggledTools.printProjectResults(market, "removeAtmosphereConditionImprovement", text);
                boggledTools.printProjectRequirementsReportIfStalled(market, "removeAtmosphereConditionImprovement",text);
            }

            if(!currentProject.equals("None"))
            {
                text.addPara("%s", bad, new String[]{"You will lose all progress on your current project if you start a new one!"});
            }
        }

        return true;
    }
}