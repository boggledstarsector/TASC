package boggled.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

public class BoggledPlayerFactionSetupScript implements EveryFrameScript
{
    private boolean done = false;

    public BoggledPlayerFactionSetupScript()
    {
        if (Misc.isPlayerFactionSetUp())
        {
            done = true;
        }
    }

    @Override
    public boolean isDone()
    {
        return done;
    }

    @Override
    public boolean runWhilePaused()
    {
        return true;
    }

    @Override
    public void advance(float amount)
    {
        if(Misc.isPlayerFactionSetUp())
        {
            done = true;
            return;
        }

        if(!Misc.getPlayerMarkets(true).isEmpty())
        {
            Global.getSector().setPaused(true);
            if(Global.getSector().getCampaignUI().showPlayerFactionConfigDialog())
            {
                Global.getSector().getMemoryWithoutUpdate().set("$shownFactionConfigDialog", true);
            }

        }
    }
}
