package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConditionModificationRemoveIrradiated extends BoggledBaseTerraformingConditionModificationProject
{
    public ConditionModificationRemoveIrradiated(MarketAPI market)
    {
        super(market, "Remove radiation", new HashSet<>(), new HashSet<>(List.of(Conditions.IRRADIATED)));
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementWorldTypeAllowsTerraforming());
        projectRequirements.add(getRequirementMarketHasIrradiated());
        projectRequirements.add(getRequirementMarketHasAtmosphereProcessor());
        return projectRequirements;
    }

    public TerraformingRequirementObject getRequirementMarketHasIrradiated()
    {
        boolean requirementMet = this.market.hasCondition(Conditions.IRRADIATED);
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
                tooltipMakerAPI.addPara("Dummy text here - has radiation",10f);
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " is irradiated", requirementMet, null);
    }

    public static boolean isEnabledViaSettings() {
        return boggledTools.getBooleanSetting(boggledTools.BoggledSettings.removeRadiationProjectEnabled);
    }
}
