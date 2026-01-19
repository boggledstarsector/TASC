package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceImprovementOrganics extends BoggledBaseTerraformingProject
{
    public ResourceImprovementOrganics(MarketAPI market)
    {
        super(market, TerraformingProjectType.RESOURCE_IMPROVEMENT);
    }

    @Override
    public void completeThisProject()
    {
        ArrayList<String> conditionsToRemove = conditionsToRemoveUponCompletion();
        ArrayList<String> conditionsToAdd = conditionsToAddUponCompletion();

        // Remove conditions
        for(String conditionId : conditionsToRemove)
        {
            boggledTools.removeCondition(this.market, conditionId);
        }

        // Add conditions
        for(String conditionId : conditionsToAdd)
        {
            boggledTools.addCondition(this.market, conditionId);
        }

        super.completeThisProject();
    }

    @Override
    public String getProjectName()
    {
        return "Improve organics";
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementWorldTypeSupportsOrganics());
        projectRequirements.add(getRequirementOrganicsCanBeImproved());

        return projectRequirements;
    }

    @Override
    public ArrayList<String> conditionsToRemoveUponCompletion()
    {
        return new ArrayList<>(Arrays.asList("organics_trace", "organics_common", "organics_abundant", "organics_plentiful"));
    }

    @Override
    public ArrayList<String> conditionsToAddUponCompletion()
    {
        String organicsIdToAdd = boggledTools.getNextOrganicsConditionId(this.market);
        return new ArrayList<>(List.of(organicsIdToAdd != null ? organicsIdToAdd : "organics_plentiful"));
    }

    public TerraformingRequirementObject getRequirementWorldTypeSupportsOrganics()
    {
        int maxOrganics = boggledTools.getMaxOrganicsLevelForTascPlanetType(boggledTools.getTascPlanetType(this.market.getPlanetEntity()));
        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                tooltipMakerAPI.addPara("Dummy text here - World type supports organics",10f);
            }
        };

        return new TerraformingRequirementObject("World type supports organics deposits", maxOrganics > 0, null);
    }

    public TerraformingRequirementObject getRequirementOrganicsCanBeImproved()
    {
        int currentOrganics = boggledTools.getCurrentOrganicsLevelForMarket(this.market);
        int maxOrganics = boggledTools.getMaxOrganicsLevelForTascPlanetType(boggledTools.getTascPlanetType(this.market.getPlanetEntity()));
        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                tooltipMakerAPI.addPara("Dummy text here - Organics can be further improved",10f);
            }
        };

        return new TerraformingRequirementObject("Organics can be improved", currentOrganics < maxOrganics, null);
    }
}
