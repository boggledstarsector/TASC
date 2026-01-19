package boggled.terraforming;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConditionModificationRemoveDenseAtmosphere extends BoggledBaseTerraformingConditionModificationProject
{
    public ConditionModificationRemoveDenseAtmosphere(MarketAPI market)
    {
        super(market, "Reduce atmospheric density", new HashSet<>(), new HashSet<>(List.of(Conditions.DENSE_ATMOSPHERE)));
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementWorldTypeAllowsTerraforming());
        projectRequirements.add(getRequirementMarketHasDenseAtmosphere());
        projectRequirements.add(getRequirementMarketHasAtmosphereProcessor());
        return projectRequirements;
    }

    public TerraformingRequirementObject getRequirementMarketHasDenseAtmosphere()
    {
        boolean requirementMet = this.market.hasCondition(Conditions.DENSE_ATMOSPHERE);
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
                tooltipMakerAPI.addPara("Dummy text here - has dense atmo",10f);
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " has a dense atmosphere", requirementMet, null);
    }
}
