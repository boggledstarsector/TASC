package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledTerraformingCheckEnabled extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledTerraformingCheckEnabled() {}

    public boggledTerraformingCheckEnabled(SectorEntityToken entity) {
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

        // Checks that -
        //  1. Terraforming content is enabled
        //  2. The entity the player is interacting with has a market
        //  3. The market is not a station market
        //  4. The market is controlled by the player
        if(boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") && this.entity.getMarket() != null && !boggledTools.marketIsStation(this.entity.getMarket()) && this.entity.getMarket().isPlayerOwned())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}