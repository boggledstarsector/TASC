package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledCraftingBuildItem extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledCraftingBuildItem() {}

    public boggledCraftingBuildItem(SectorEntityToken entity) {
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

        if(ruleId.equals("boggledTriggerCraftingCorruptedNanoforge"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(1);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("corrupted_nanoforge", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingPristineNanoforge"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(3);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("pristine_nanoforge", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingSynchrotronCore"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(2);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("synchrotron", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingHypershuntTap"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(3);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("coronal_portal", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingCryoarithmeticEngine"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(2);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("cryoarithmetic_engine", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingPlanetKillerDevice"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(3);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("boggled_planetkiller", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingOrbitalFusionLamp"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(3);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("orbital_fusion_lamp", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingFullereneSpool"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(2);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("fullerene_spool", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingPlasmaDynamo"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(2);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("plasma_dynamo", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingAutonomousMantleBore"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(2);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("mantle_bore", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingSoilNanites"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(2);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("soil_nanites", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingCatalyticCore"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(2);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("catalytic_core", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingCombatDroneReplicator"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(1);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("drone_replicator", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingBiofactoryEmbryo"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(2);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("biofactory_embryo", null), 1);
        }
        else if(ruleId.equals("boggledTriggerCraftingDealmakerHolosuite"))
        {
            this.subtractStoryPointsFromPlayer();
            this.removeDomainEraArtifactsFromCargo(1);
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData("dealmaker_holosuite", null), 1);
        }

        return true;
    }

    private void removeDomainEraArtifactsFromCargo(int difficulty)
    {
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        int artifactCost = boggledTools.getIntSetting("boggledDomainTechCraftingArtifactCost");
        int adjustedArtifactCost = 0;
        if(difficulty == 3)
        {
            adjustedArtifactCost = artifactCost * 2;
        }
        else if(difficulty == 2)
        {
            adjustedArtifactCost = artifactCost;
        }
        else if(difficulty == 1)
        {
            adjustedArtifactCost = artifactCost / 2;
        }
        cargo.removeCommodity(boggledTools.BoggledCommodities.domainArtifacts, adjustedArtifactCost);
    }

    private void subtractStoryPointsFromPlayer()
    {
        Integer storyPointCost = boggledTools.getIntSetting("boggledDomainTechCraftingStoryPointCost");
        if(storyPointCost > 0)
        {
            MutableCharacterStatsAPI charStats = Global.getSector().getPlayerStats();
            charStats.spendStoryPoints(storyPointCost, false, null, false, null);
        }
    }
}