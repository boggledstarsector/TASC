package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledIsPlanetGasGiant extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledIsPlanetGasGiant() {}

    public boggledIsPlanetGasGiant(SectorEntityToken entity) {
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

        // Make sure it's not a station
        if(this.entity.getMarket() == null || this.entity.getMarket().getPlanetEntity() == null)
        {
            return false;
        }

        String type = boggledTools.getPlanetType(this.entity.getMarket().getPlanetEntity()).getPlanetId();

        if(type.equals(boggledTools.gasGiantPlanetId))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}