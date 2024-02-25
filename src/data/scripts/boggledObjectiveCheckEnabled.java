package data.scripts;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledObjectiveCheckEnabled extends BaseCommandPlugin
{
    protected SectorEntityToken entity;
    protected TextPanelAPI text;

    public boggledObjectiveCheckEnabled() {}

    public boggledObjectiveCheckEnabled(SectorEntityToken entity) {
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

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting("boggledDomainTechObjectivesEnabled"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}