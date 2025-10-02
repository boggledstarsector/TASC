package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import java.util.ArrayList;
import java.util.HashSet;

public class BoggledBaseTerraformingConditionModificationProject extends BoggledBaseTerraformingProject
{
    private HashSet<String> conditionsToAdd = new HashSet<>();

    private HashSet<String> conditionsToRemove = new HashSet<>();

    private final String projectName;
    public BoggledBaseTerraformingConditionModificationProject(MarketAPI market, String projectName, HashSet<String> conditionsToAdd, HashSet<String> conditionsToRemove)
    {
        super(market, TerraformingProjectType.CONDITION_IMPROVEMENT);
        this.conditionsToAdd = conditionsToAdd;
        this.conditionsToRemove = conditionsToRemove;
        this.projectName = projectName;
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
        return this.projectName;
    }

    public TerraformingRequirementObject getRequirementConditionsWouldBeChanged()
    {
        boolean conditionsWouldChange = false;
        for(String condition : this.conditionsToAdd)
        {
            if(!this.market.hasCondition(condition))
            {
                conditionsWouldChange = true;
                break;
            }
        }

        if(!conditionsWouldChange)
        {
            for(String condition : this.conditionsToRemove)
            {
                if(this.market.hasCondition(condition))
                {
                    conditionsWouldChange = true;
                    break;
                }
            }
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
                tooltipMakerAPI.addPara("Dummy text here - conditions would be changed",10f);
            }
        };

        return new TerraformingRequirementObject(this.projectName, conditionsWouldChange, tooltip);
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementConditionsWouldBeChanged());

        return projectRequirements;
    }

    @Override
    public ArrayList<String> conditionsToRemoveUponCompletion()
    {
        return new ArrayList<>(this.conditionsToRemove);
    }

    @Override
    public ArrayList<String> conditionsToAddUponCompletion()
    {
        return new ArrayList<>(this.conditionsToAdd);
    }
}
