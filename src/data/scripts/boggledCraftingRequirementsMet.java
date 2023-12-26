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
                craftingProjectId = boggledTools.craftCorruptedNanoforgeProjectId;
            }
            else if(ruleId.contains("PristineNanoforge"))
            {
                craftingProjectId = boggledTools.craftPristineNanoforgeProjectId;
            }
            else if(ruleId.contains("SynchrotronCore"))
            {
                craftingProjectId = boggledTools.craftSynchrotronProjectId;
            }
            else if(ruleId.contains("HypershuntTap"))
            {
                craftingProjectId = boggledTools.craftHypershuntTapProjectId;
            }
            else if(ruleId.contains("CryoarithmeticEngine"))
            {
                craftingProjectId = boggledTools.craftCryoarithmeticEngineProjectId;
            }
            else if(ruleId.contains("PlanetKillerDevice"))
            {
                craftingProjectId = boggledTools.craftPlanetKillerDeviceProjectId;
            }
            else if(ruleId.contains("FusionLamp"))
            {
                craftingProjectId = boggledTools.craftFusionLampProjectId;
            }
            else if(ruleId.contains("FullereneSpool"))
            {
                craftingProjectId = boggledTools.craftFullereneSpoolProjectId;
            }
            else if(ruleId.contains("PlasmaDynamo"))
            {
                craftingProjectId = boggledTools.craftPlasmaDynamoProjectId;
            }
            else if(ruleId.contains("AutonomousMantleBore"))
            {
                craftingProjectId = boggledTools.craftAutonomousMantleBoreProjectId;
            }
            else if(ruleId.contains("SoilNanites"))
            {
                craftingProjectId = boggledTools.craftSoilNanitesProjectId;
            }
            else if(ruleId.contains("CatalyticCore"))
            {
                craftingProjectId = boggledTools.craftCatalyticCoreProjectId;
            }
            else if(ruleId.contains("CombatDroneReplicator"))
            {
                craftingProjectId = boggledTools.craftCombatDroneReplicatorProjectId;
            }
            else if(ruleId.contains("BiofactoryEmbryo"))
            {
                craftingProjectId = boggledTools.craftBiofactoryEmbryoProjectId;
            }
            else if(ruleId.contains("DealmakerHolosuite"))
            {
                craftingProjectId = boggledTools.craftDealmakerHolosuiteProjectId;
            }

            BoggledTerraformingProject craftingProject = boggledTools.getCraftingProject(craftingProjectId);
            if (craftingProject != null) {
                for (BoggledTerraformingRequirements craftingRequirements : craftingProject.getProjectRequirements()) {
                    if (!craftingRequirements.checkRequirement(market)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}