package boggled.ui;

import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.listeners.CoreUITabListener;

public class BoggledCoreModificationListener implements CoreUITabListener {
    public BoggledCoreModificationListener() { }

    public void reportAboutToOpenCoreTab(CoreUITabId tab, Object param)
    {

    }

    @Override
    public void reportOpenedCoreTab(CoreUITabId tab, Object param)
    {
        if (tab == CoreUITabId.OUTPOSTS)
        {
            BoggledCoreModifierEveryFrameScript.onOutpostsTabOpened();
        }
    }
}
