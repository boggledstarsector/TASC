package boggled.ui;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.listeners.CoreUITabListener;

public class BoggledCoreModificationListener implements CoreUITabListener {
    public BoggledCoreModificationListener() { }

    public void reportAboutToOpenCoreTab(CoreUITabId tab, Object param) {
        boggledTools.writeMessageToLog("Triggered core listener. Tab: " + tab + ". Param class: " + param.getClass());
    }
}
