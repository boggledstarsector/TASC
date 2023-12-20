
package data.campaign.econ.conditions;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;;
import data.campaign.econ.boggledTools;

public class Boggled_Widespread_Ruins_Overwrite extends BaseHazardCondition
{
    public Boggled_Widespread_Ruins_Overwrite() { }

    public void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded)
    {
        super.createTooltipAfterDescription(tooltip, expanded);

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            tooltip.addPara("No bonuses or penalties to Domain-era artifact production (Domain Archeology)", 10f, Misc.getHighlightColor(), "");
        }
    }
}
