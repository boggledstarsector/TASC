package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import data.campaign.econ.conditions.Terraforming_Controller;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledTerraformingCheckProjectOngoing extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledTerraformingCheckProjectOngoing() {}

    public boggledTerraformingCheckProjectOngoing(SectorEntityToken entity) {
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

        if(this.entity.getMarket() == null || boggledTools.marketIsStation(this.entity.getMarket()))
        {
            return true;
        }
        else
        {
            Color highlight = Misc.getHighlightColor();
            Color good = Misc.getPositiveHighlightColor();
            Color bad = Misc.getNegativeHighlightColor();

            MarketAPI market = this.entity.getMarket();
            PlanetAPI planet = market.getPlanetEntity();
            Terraforming_Controller terraformingController = (Terraforming_Controller) market.getCondition("terraforming_controller").getPlugin();
            String currentProject = terraformingController.getProject();

            return currentProject.equals("None");
        }
    }
}