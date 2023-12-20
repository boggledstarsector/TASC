package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledCraftingCheckEnabled extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledCraftingCheckEnabled() {}

    public boggledCraftingCheckEnabled(SectorEntityToken entity) {
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
        //  1. The entity the player is interacting with has a market
        //  2. The market is controlled by the player
        //  3. Domain-tech content is enabled
        //  4. Domain-tech crafting is enabled
        //  5. Domain Archeology is enabled
        if(this.entity.getMarket() != null && this.entity.getMarket().isPlayerOwned() && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting("boggledDomainTechCraftingEnabled") && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}