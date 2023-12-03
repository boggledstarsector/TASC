package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledCraftingRequirementsMet extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledCraftingRequirementsMet() {}

    public boggledCraftingRequirementsMet(SectorEntityToken entity) {
        this.init(entity);
    }

    protected void init(SectorEntityToken entity)
    {
        this.entity = entity;
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        if(dialog == null) return false;

        this.entity = dialog.getInteractionTarget();
        TextPanelAPI text = dialog.getTextPanel();

        if(this.entity.getMarket() == null)
        {
            return false;
        }
        else
        {
            MarketAPI market = this.entity.getMarket();

            String[] requirements = null;
            if(ruleId.contains("CorruptedNanoforge") || ruleId.contains("CombatDroneReplicator") || ruleId.contains("DealmakerHolosuite"))
            {
                requirements = boggledTools.getProjectRequirementsStrings("boggledCraftingEasy");
            }
            else if(ruleId.contains("PristineNanoforge") || ruleId.contains("HypershuntTap") || ruleId.contains("PlanetKillerDevice") || ruleId.contains("OrbitalFusionLamp"))
            {
                requirements = boggledTools.getProjectRequirementsStrings("boggledCraftingHard");
            }
            else
            {
                requirements = boggledTools.getProjectRequirementsStrings("boggledCraftingMedium");
            }

            int i;
            for (i = 0; i < requirements.length; i++)
            {
                if(!boggledTools.requirementMet(market, requirements[i]))
                {
                    return false;
                }
            }
        }

        return true;
    }
}