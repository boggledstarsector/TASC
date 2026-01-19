package boggled.campaign.econ.industries.plugins;

import boggled.campaign.econ.boggledTools;
import boggled.campaign.econ.industries.interfaces.ShowBoggledTerraformingMenuOption;
import boggled.ui.BoggledCoreModifierEveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI;
import com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;
import java.util.List;

public class TerraformingMenuOptionProvider extends BaseIndustryOptionProvider
{
    public static final Object OPTION_OPEN_TERRAFORMING_MENU = new Object();

    public static void register()
    {
        ListenerManagerAPI listeners = Global.getSector().getListenerManager();
        if (!listeners.hasListenerOfClass(TerraformingMenuOptionProvider.class))
        {
            listeners.addListener(new TerraformingMenuOptionProvider(), true);
        }
    }

    @Override
    public List<IndustryOptionData> getIndustryOptions(Industry ind)
    {
        if (isUnsuitable(ind, false)) return null;

        List<IndustryOptionProvider.IndustryOptionData> result = new ArrayList<IndustryOptionData>();

        IndustryOptionData opt = new IndustryOptionProvider.IndustryOptionData("Open terraforming menu", OPTION_OPEN_TERRAFORMING_MENU, ind, this);
        //opt.color = new Color(150, 100, 255, 255);
        result.add(opt);

        return result;
    }

    @Override
    public boolean isUnsuitable(Industry ind, boolean allowUnderConstruction)
    {
        if(ind == null || ind.getMarket() == null)
        {
            return true;
        }

        // Temporarily disabled in conjunction with AotD until support can be added to Ashlib
        if(Global.getSettings().getModManager().isModEnabled("ashlib"))
        {
            return true;
        }

        if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled))
        {
            return false;
        }

        boolean isBoggledTerraformingIndustry = ind instanceof ShowBoggledTerraformingMenuOption;
        boolean isStation = boggledTools.marketIsStation(ind.getMarket());
        boolean playerOwned = ind.getMarket().isPlayerOwned();

        return super.isUnsuitable(ind, allowUnderConstruction) || !isBoggledTerraformingIndustry || isStation || !playerOwned;
    }

    @Override
    public void createTooltip(IndustryOptionData opt, TooltipMakerAPI tooltip, float width)
    {
        if(opt.id == OPTION_OPEN_TERRAFORMING_MENU)
        {
            tooltip.addPara("Opens the terraforming menu with this colony already selected.", 0f);
        }
    }

    @Override
    public void optionSelected(IndustryOptionData opt, DialogCreatorUI ui)
    {
        if(opt.id == OPTION_OPEN_TERRAFORMING_MENU)
        {
            MarketAPI targetMarket = opt.ind.getMarket();
            BoggledCoreModifierEveryFrameScript.setMarketToOpen(targetMarket);
            Global.getSector().getCampaignUI().showCoreUITab(CoreUITabId.OUTPOSTS, null);
        }
    }
}
