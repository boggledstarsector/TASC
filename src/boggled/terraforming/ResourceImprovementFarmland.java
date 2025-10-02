package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import java.util.ArrayList;
import java.util.HashSet;

public class ResourceImprovementFarmland extends BoggledBaseTerraformingProject
{
    public ResourceImprovementFarmland(MarketAPI market)
    {
        super(market, TerraformingProjectType.RESOURCE_IMPROVEMENT);
    }

    @Override
    public void completeThisProject()
    {
        // Remove conditions
        for(String conditionId : this.conditionsToRemove)
        {
            boggledTools.removeCondition(this.market, conditionId);
        }

        // Add conditions
        for(String conditionId : this.conditionsToAdd)
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
        // projectRequirements.add(getRequirementConditionsWouldBeChanged());

        return projectRequirements;
    }

    @Override
    public ArrayList<String> conditionsToRemoveUponCompletion()
    {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<String> conditionsToAddUponCompletion()
    {
        return new ArrayList<>(this.conditionsToAdd);
    }

    public TerraformingRequirementObject getRequirementWorldTypeSupportsFarmland()
    {
        String tascPlanetType = boggledTools.getTascPlanetType(market.getPlanetEntity());
        String currentPlanetTypeDisplayString = boggledTools.getPlanetSpec(tascPlanetType).getName();
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

        return new TerraformingRequirementObject("World type supports farmland", worldTypeSupportsFarmland, tooltip);
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

        return new TerraformingRequirementObject("Farmland can be improved", requirementMet, tooltip);
    }
}
