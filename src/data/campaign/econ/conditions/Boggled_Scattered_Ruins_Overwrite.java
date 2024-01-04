
package data.campaign.econ.conditions;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;

public class Boggled_Scattered_Ruins_Overwrite extends BaseHazardCondition
{
    static {
        ResourceDepositsCondition.COMMODITY.put(Conditions.RUINS_SCATTERED, boggledTools.BoggledCommodities.domainArtifacts);
        ResourceDepositsCondition.COMMODITY.put(Conditions.RUINS_WIDESPREAD, boggledTools.BoggledCommodities.domainArtifacts);
        ResourceDepositsCondition.COMMODITY.put(Conditions.RUINS_EXTENSIVE, boggledTools.BoggledCommodities.domainArtifacts);
        ResourceDepositsCondition.COMMODITY.put(Conditions.RUINS_VAST, boggledTools.BoggledCommodities.domainArtifacts);

        ResourceDepositsCondition.MODIFIER.put(Conditions.RUINS_SCATTERED, -1);
        ResourceDepositsCondition.MODIFIER.put(Conditions.RUINS_WIDESPREAD, 0);
        ResourceDepositsCondition.MODIFIER.put(Conditions.RUINS_EXTENSIVE, 1);
        ResourceDepositsCondition.MODIFIER.put(Conditions.RUINS_VAST, 2);

        ResourceDepositsCondition.INDUSTRY.put(boggledTools.BoggledCommodities.domainArtifacts, boggledTools.BoggledIndustries.domainArchaeologyIndustryId);

        ResourceDepositsCondition.BASE_MODIFIER.put(boggledTools.BoggledCommodities.domainArtifacts, -2);
    }

    public Boggled_Scattered_Ruins_Overwrite() { }

    public void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded)
    {
        super.createTooltipAfterDescription(tooltip, expanded);

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            tooltip.addPara("%s to Domain-era artifact production (Domain Archaeology)", 10f, Misc.getHighlightColor(), "-1");
        }
    }
}
