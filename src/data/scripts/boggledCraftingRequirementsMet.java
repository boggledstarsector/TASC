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

        if(this.entity.getMarket() == null)
        {
            return false;
        }
        else
        {
            MarketAPI market = this.entity.getMarket();

            String craftingProjectId = null;
            if(ruleId.contains("CorruptedNanoforge"))
            {
                craftingProjectId = boggledTools.craftCorruptedNanoforgeProjectID;
            }
            else if(ruleId.contains("PristineNanoforge"))
            {
                craftingProjectId = boggledTools.craftPristineNanoforgeProjectID;
            }
            else if(ruleId.contains("SynchrotronCore"))
            {
                craftingProjectId = boggledTools.craftSynchrotronProjectID;
            }
            else if(ruleId.contains("HypershuntTap"))
            {
                craftingProjectId = boggledTools.craftHypershuntTapProjectID;
            }
            else if(ruleId.contains("CryoarithmeticEngine"))
            {
                craftingProjectId = boggledTools.craftCryoarithmeticEngineProjectID;
            }
            else if(ruleId.contains("PlanetKillerDevice"))
            {
                craftingProjectId = boggledTools.craftPlanetKillerDeviceProjectID;
            }
            else if(ruleId.contains("FusionLamp"))
            {
                craftingProjectId = boggledTools.craftFusionLampProjectID;
            }
            else if(ruleId.contains("FullereneSpool"))
            {
                craftingProjectId = boggledTools.craftFullereneSpoolProjectID;
            }
            else if(ruleId.contains("PlasmaDynamo"))
            {
                craftingProjectId = boggledTools.craftPlasmaDynamoProjectID;
            }
            else if(ruleId.contains("AutonomousMantleBore"))
            {
                craftingProjectId = boggledTools.craftAutonomousMantleBoreProjectID;
            }
            else if(ruleId.contains("SoilNanites"))
            {
                craftingProjectId = boggledTools.craftSoilNanitesProjectID;
            }
            else if(ruleId.contains("CatalyticCore"))
            {
                craftingProjectId = boggledTools.craftCatalyticCoreProjectID;
            }
            else if(ruleId.contains("CombatDroneReplicator"))
            {
                craftingProjectId = boggledTools.craftCombatDroneReplicatorProjectID;
            }
            else if(ruleId.contains("BiofactoryEmbryo"))
            {
                craftingProjectId = boggledTools.craftBiofactoryEmbryoProjectID;
            }
            else if(ruleId.contains("DealmakerHolosuite"))
            {
                craftingProjectId = boggledTools.craftDealmakerHolosuiteProjectID;
            }

            boggledTools.TerraformingProject craftingProject = boggledTools.getCraftingProject(craftingProjectId);
            if (craftingProject != null) {
                for (boggledTools.TerraformingRequirements craftingRequirements : craftingProject.getProjectRequirements()) {
                    if (!craftingRequirements.checkRequirement(market)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}