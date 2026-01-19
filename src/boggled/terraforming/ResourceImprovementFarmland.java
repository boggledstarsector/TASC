package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResourceImprovementFarmland extends BoggledBaseTerraformingProject
{
    public ResourceImprovementFarmland(MarketAPI market)
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
        return "Improve farmland";
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementWorldTypeSupportsFarmland());
        projectRequirements.add(getRequirementFarmlandCanBeImproved());
        projectRequirements.add(getRequirementAdequateWaterToImproveFarmland());
        projectRequirements.add(getRequirementAtmosphericDensityNormal());
        projectRequirements.add(getRequirementAtmosphericNotToxicOrIrradiated());

        return projectRequirements;
    }

    @Override
    public ArrayList<String> conditionsToRemoveUponCompletion()
    {
        return new ArrayList<>(Arrays.asList("farmland_poor", "farmland_adequate", "farmland_rich", "farmland_bountiful"));
    }

    @Override
    public ArrayList<String> conditionsToAddUponCompletion()
    {
        String farmlandIdToAdd = boggledTools.getNextFarmlandConditionId(this.market);
        return new ArrayList<>(List.of(farmlandIdToAdd != null ? farmlandIdToAdd : "farmland_bountiful"));
    }

    public TerraformingRequirementObject getRequirementWorldTypeSupportsFarmland()
    {
        String tascPlanetType = boggledTools.getTascPlanetType(market.getPlanetEntity());
        // String currentPlanetTypeDisplayString = getTascPlanetTypeDisplayString(tascPlanetType);
        Boolean worldTypeSupportsFarmland = boggledTools.tascPlanetTypeSupportsFarmland(tascPlanetType);
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
                tooltipMakerAPI.addPara("Dummy text here - world type supports farmland",10f);
            }
        };

        return new TerraformingRequirementObject("World type supports farmland", worldTypeSupportsFarmland, null);
    }

    public TerraformingRequirementObject getRequirementFarmlandCanBeImproved()
    {
        boolean requirementMet = boggledTools.getNextFarmlandString(market) != null;
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
                tooltipMakerAPI.addPara("Dummy text here - Farmland can be further improved",10f);
            }
        };

        return new TerraformingRequirementObject("Farmland can be improved", requirementMet, null);
    }

    public TerraformingRequirementObject getRequirementAdequateWaterToImproveFarmland()
    {
        boggledTools.PlanetWaterLevel currentWaterLevel = boggledTools.getWaterLevelForMarket(this.market);
        Integer nextFarmlandLevel = boggledTools.getNextFarmlandLevelInteger(this.market);
        boolean requirementMet = false;
        if(nextFarmlandLevel != null && nextFarmlandLevel < 3 && (currentWaterLevel == boggledTools.PlanetWaterLevel.MEDIUM_WATER || currentWaterLevel == boggledTools.PlanetWaterLevel.HIGH_WATER))
        {
            requirementMet = true;
        }
        else if(nextFarmlandLevel == null || nextFarmlandLevel > 2 && currentWaterLevel == boggledTools.PlanetWaterLevel.HIGH_WATER)
        {
            requirementMet = true;
        }

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
                tooltipMakerAPI.addPara("Dummy text here - Adequate water",10f);
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " has adequate water", requirementMet, null);
    }
}
