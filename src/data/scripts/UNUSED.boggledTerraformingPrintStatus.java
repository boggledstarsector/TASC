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

public class boggledTerraformingPrintStatus extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledTerraformingPrintStatus() {}

    public boggledTerraformingPrintStatus(SectorEntityToken entity) {
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
            if(!market.hasCondition(boggledTools.BoggledConditions.terraformingControllerConditionID))
            {
                market.addCondition(boggledTools.BoggledConditions.terraformingControllerConditionID);
            }

            Terraforming_Controller terraformingController = (Terraforming_Controller) market.getCondition(boggledTools.BoggledConditions.terraformingControllerConditionID).getPlugin();
            String currentProject = terraformingController.getProject();

            text.addPara("Current terraforming project: %s", highlight, boggledTools.getTooltipProjectName(currentProject));

            if(currentProject != null && !currentProject.equals(boggledTools.noneProjectID))
            {
                text.addPara("%s days remaining (%s complete)", highlight, terraformingController.getDaysRemaining() + "", terraformingController.getPercentComplete() + "%");
            }

            // printProjectRequirementsReportIfStalled will return true if one or more requirements are not met. If so,
            // tell the player about same.
            if(boggledTools.printProjectRequirementsReportIfStalled(market, currentProject, text))
            {
                text.addPara("%s", bad, "Terraforming is stalled because one or more project requirements are not met!");
            }
        }

        return true;
    }
}