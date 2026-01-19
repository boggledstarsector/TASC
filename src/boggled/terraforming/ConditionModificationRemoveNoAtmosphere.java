package boggled.terraforming;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConditionModificationRemoveNoAtmosphere extends BoggledBaseTerraformingConditionModificationProject
{
    public ConditionModificationRemoveNoAtmosphere(MarketAPI market)
    {
        super(market, "Add atmosphere", new HashSet<>(List.of(Conditions.THIN_ATMOSPHERE)), new HashSet<>(List.of(Conditions.NO_ATMOSPHERE)));
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementWorldTypeAllowsTerraforming());
        projectRequirements.add(getRequirementMarketHasNoAtmosphere());
        projectRequirements.add(getRequirementMarketHasAtmosphereProcessor());
        return projectRequirements;
    }

    public TerraformingRequirementObject getRequirementMarketHasNoAtmosphere()
    {
        boolean requirementMet = this.market.hasCondition(Conditions.NO_ATMOSPHERE);
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
                tooltipMakerAPI.addPara("Dummy text here - no atmo",10f);
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " has no atmosphere", requirementMet, null);
    }
}
