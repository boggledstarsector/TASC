package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledCheckUnknownSkiesEnabled extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledCheckUnknownSkiesEnabled() {}

    public boggledCheckUnknownSkiesEnabled(SectorEntityToken entity) {
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

        if(Global.getSettings().getModManager().isModEnabled("US"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}