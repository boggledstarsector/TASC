package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledCraftingPrintRequirements extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledCraftingPrintRequirements() {}

    public boggledCraftingPrintRequirements(SectorEntityToken entity) {
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
            return true;
        }
        else
        {
            Color highlight = Misc.getHighlightColor();
            Color good = Misc.getPositiveHighlightColor();
            Color bad = Misc.getNegativeHighlightColor();

            MarketAPI market = this.entity.getMarket();

            // Print crafting requirements
            String itemToCraftString = null;
            if(ruleId.contains("CorruptedNanoforge"))
            {
                itemToCraftString = "a corrupted nanoforge";
            }
            else if(ruleId.contains("PristineNanoforge"))
            {
                itemToCraftString = "a pristine nanoforge";
            }
            else if(ruleId.contains("SynchrotronCore"))
            {
                itemToCraftString = "a synchrotron core";
            }
            else if(ruleId.contains("HypershuntTap"))
            {
                itemToCraftString = "hypershunt tap";
            }
            else if(ruleId.contains("CryoarithmeticEngine"))
            {
                itemToCraftString = "a cryoarithmetic engine";
            }
            else if(ruleId.contains("PlanetKillerDevice"))
            {
                itemToCraftString = "a planet-killer device";
            }
            else if(ruleId.contains("FusionLamp"))
            {
                itemToCraftString = "a fusion lamp";
            }
            else if(ruleId.contains("FullereneSpool"))
            {
                itemToCraftString = "a fullerene spool";
            }
            else if(ruleId.contains("PlasmaDynamo"))
            {
                itemToCraftString = "a plasma dynamo";
            }
            else if(ruleId.contains("AutonomousMantleBore"))
            {
                itemToCraftString = "an autonomous mantle bore";
            }
            else if(ruleId.contains("SoilNanites"))
            {
                itemToCraftString = "soil nanites";
            }
            else if(ruleId.contains("CatalyticCore"))
            {
                itemToCraftString = "a catalytic core";
            }
            else if(ruleId.contains("CombatDroneReplicator"))
            {
                itemToCraftString = "a combat drone replicator";
            }
            else if(ruleId.contains("BiofactoryEmbryo"))
            {
                itemToCraftString = "a biofactory embryo";
            }
            else if(ruleId.contains("DealmakerHolosuite"))
            {
                itemToCraftString = "a dealmaker holosuite";
            }

            text.addPara("Requirements to craft " +  itemToCraftString + ":", highlight, new String[]{""});
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
                if(boggledTools.requirementMet(market, requirements[i]))
                {
                    text.addPara("      - %s", good, new String[]{requirements[i] + ""});
                }
                else
                {
                    text.addPara("      - %s", bad, new String[]{requirements[i] + ""});
                }
            }
        }

        return true;
    }
}