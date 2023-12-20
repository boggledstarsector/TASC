package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.GateEntityPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledInactiveGateCheckEnabled extends BaseCommandPlugin
{
    protected SectorEntityToken entity;
    protected TextPanelAPI text;

    public boggledInactiveGateCheckEnabled() {}

    public boggledInactiveGateCheckEnabled(SectorEntityToken entity) {
        this.init(entity);
    }

    protected void init(SectorEntityToken entity)
    {
        this.entity = entity;
        this.text = null;
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        if(dialog == null) return false;

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting("boggledDomainTechInactiveGateConstructionEnabled"))
        {
            if(boggledTools.getBooleanSetting("boggledDomainTechInactiveGateConstructionMainQuestCompletionRequired"))
            {
                Boolean gatesActive = (Boolean) Global.getSector().getMemoryWithoutUpdate().get(GateEntityPlugin.PLAYER_CAN_USE_GATES);
                if(gatesActive)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }
}