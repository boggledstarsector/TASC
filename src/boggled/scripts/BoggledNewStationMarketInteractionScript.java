package boggled.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.util.Misc;

public class BoggledNewStationMarketInteractionScript implements EveryFrameScript
{
    private boolean done = false;
    private final MarketAPI market;

    public BoggledNewStationMarketInteractionScript(MarketAPI market)
    {
        this.market = market;
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
        if(Misc.isPlayerFactionSetUp() && !Global.getSector().getCampaignUI().isShowingDialog() )
        {
            // Global.getSector().getCampaignUI().showCoreUITab(CoreUITabId.OUTPOSTS, market);
            Global.getSector().getCampaignUI().showInteractionDialog(market.getPrimaryEntity());
            done = true;
        }
    }
}
