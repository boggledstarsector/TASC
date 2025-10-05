package boggled.terraforming;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConditionModificationRemoveExtremeWeather extends BoggledBaseTerraformingConditionModificationProject
{
    public ConditionModificationRemoveExtremeWeather(MarketAPI market)
    {
        super(market, "Stabilize weather patterns", new HashSet<>(), new HashSet<>(List.of(Conditions.EXTREME_WEATHER)));
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementWorldTypeAllowsTerraforming());
        projectRequirements.add(getRequirementMarketHasExtremeWeather());
        projectRequirements.add(getRequirementMarketHasAtmosphereProcessor());
        return projectRequirements;
    }

    public TerraformingRequirementObject getRequirementMarketHasExtremeWeather()
    {
        boolean requirementMet = this.market.hasCondition(Conditions.EXTREME_WEATHER);
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
                tooltipMakerAPI.addPara("Dummy text here - has extreme weather",10f);
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " has extreme weather", requirementMet, tooltip);
    }
}
