package boggled.terraforming;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConditionModificationAddMildClimate extends BoggledBaseTerraformingConditionModificationProject
{
    public ConditionModificationAddMildClimate(MarketAPI market)
    {
        super(market, "Make climate mild", new HashSet<>(List.of(Conditions.MILD_CLIMATE)), new HashSet<>());
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementWorldTypeAllowsHumanHabitability());
        projectRequirements.add(getRequirementMarketIsHabitable());
        projectRequirements.add(getRequirementMarketNotMildClimate());
        projectRequirements.add(getRequirementAtmosphericDensityNormal());
        projectRequirements.add(getRequirementAtmosphericNotToxicOrIrradiated());
        projectRequirements.add(getRequirementMarketHasAtmosphereProcessor());
        return projectRequirements;
    }

    public TerraformingRequirementObject getRequirementMarketNotMildClimate()
    {
        boolean requirementMet = !this.market.hasCondition(Conditions.MILD_CLIMATE);
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
                tooltipMakerAPI.addPara("Dummy text here - not mild climate",10f);
            }
        };

        return new TerraformingRequirementObject("Colony does not have a mild climate", requirementMet, tooltip);
    }
}
