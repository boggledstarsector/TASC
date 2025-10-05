package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ConditionModificationRemoveAtmosphere extends BoggledBaseTerraformingConditionModificationProject
{
    public ConditionModificationRemoveAtmosphere(MarketAPI market)
    {
        super(market, "Remove atmosphere", new HashSet<>(List.of(Conditions.NO_ATMOSPHERE)), new HashSet<>(Arrays.asList(
                Conditions.THIN_ATMOSPHERE,
                Conditions.TOXIC_ATMOSPHERE,
                Conditions.DENSE_ATMOSPHERE,
                Conditions.HABITABLE,
                Conditions.MILD_CLIMATE,
                Conditions.INIMICAL_BIOSPHERE,
                Conditions.FARMLAND_POOR,
                Conditions.FARMLAND_ADEQUATE,
                Conditions.FARMLAND_RICH,
                Conditions.FARMLAND_BOUNTIFUL,
                Conditions.POLLUTION
        )));
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementWorldTypeAllowsTerraforming());
        projectRequirements.add(getRequirementMarketIsNotWaterWorld());
        projectRequirements.add(getRequirementMarketHasAtmosphereProcessor());
        return projectRequirements;
    }

    public TerraformingRequirementObject getRequirementMarketIsNotWaterWorld()
    {
        boolean requirementMet = this.market.hasCondition(Conditions.WATER_SURFACE);
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
                tooltipMakerAPI.addPara("Dummy text here - not water",10f);
            }
        };

        return new TerraformingRequirementObject("Colony is not a Water world", requirementMet, tooltip);
    }

    public static boolean isEnabledViaSettings() {
        return boggledTools.getBooleanSetting(boggledTools.BoggledSettings.removeAtmosphereProjectEnabled);
    }
}
