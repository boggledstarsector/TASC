package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceImprovementVolatiles extends BoggledBaseTerraformingProject
{
    public ResourceImprovementVolatiles(MarketAPI market)
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
        return "Improve volatiles";
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementWorldTypeSupportsVolatiles());
        projectRequirements.add(getRequirementVolatilesCanBeImproved());

        return projectRequirements;
    }

    @Override
    public ArrayList<String> conditionsToRemoveUponCompletion()
    {
        return new ArrayList<>(Arrays.asList("volatiles_trace", "volatiles_diffuse", "volatiles_abundant", "volatiles_plentiful"));
    }

    @Override
    public ArrayList<String> conditionsToAddUponCompletion()
    {
        String volatilesIdToAdd = boggledTools.getNextVolatilesConditionId(this.market);
        return new ArrayList<>(List.of(volatilesIdToAdd != null ? volatilesIdToAdd : "volatiles_plentiful"));
    }

    public TerraformingRequirementObject getRequirementWorldTypeSupportsVolatiles()
    {
        int maxVolatiles = boggledTools.getMaxVolatilesLevelForTascPlanetType(boggledTools.getTascPlanetType(this.market.getPlanetEntity()));
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
                tooltipMakerAPI.addPara("Dummy text here - World type supports volatiles",10f);
            }
        };

        return new TerraformingRequirementObject("World type supports volatiles deposits", maxVolatiles > 0, tooltip);
    }

    public TerraformingRequirementObject getRequirementVolatilesCanBeImproved()
    {
        int currentVolatiles = boggledTools.getCurrentVolatilesLevelForMarket(this.market);
        int maxVolatiles = boggledTools.getMaxVolatilesLevelForTascPlanetType(boggledTools.getTascPlanetType(this.market.getPlanetEntity()));
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
                tooltipMakerAPI.addPara("Dummy text here - Volatiles can be further improved",10f);
            }
        };

        return new TerraformingRequirementObject("Volatiles can be improved", currentVolatiles < maxVolatiles, tooltip);
    }
}
