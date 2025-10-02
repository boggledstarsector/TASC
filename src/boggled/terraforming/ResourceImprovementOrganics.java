package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
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
        // Remove conditions
        for(String conditionId : conditionsToRemoveUponCompletion())
        {
            boggledTools.removeCondition(this.market, conditionId);
        }

        // Add conditions
        for(String conditionId : conditionsToAddUponCompletion())
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
}
