package boggled.terraforming;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConditionModificationAddHabitable extends BoggledBaseTerraformingConditionModificationProject
{
    public ConditionModificationAddHabitable(MarketAPI market)
    {
        super(market, "Make atmosphere habitable", new HashSet<>(List.of(Conditions.HABITABLE)), new HashSet<>());
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementWorldTypeAllowsHumanHabitability());
        projectRequirements.add(getRequirementMarketNotHabitable());
        projectRequirements.add(getRequirementMarketDoesNotHaveExtremeWeather());
        projectRequirements.add(getRequirementAtmosphericDensityNormal());
        projectRequirements.add(getRequirementAtmosphericNotToxicOrIrradiated());
        projectRequirements.add(getRequirementMarketHasAtmosphereProcessor());
        return projectRequirements;
    }

    public TerraformingRequirementObject getRequirementMarketNotHabitable()
    {
        boolean requirementMet = !this.market.hasCondition(Conditions.HABITABLE);
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
                tooltipMakerAPI.addPara("Dummy text here - not habitable",10f);
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " is not habitable for humans", requirementMet, null);
    }
}
