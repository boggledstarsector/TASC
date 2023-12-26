package data.campaign.econ.industries;

import java.lang.String;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

public class Boggled_Domain_Archaeology extends BaseIndustry
{
    private static BoggledCommonIndustry commonindustry;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        commonindustry = new BoggledCommonIndustry(data, "Domain Archaeology");
    }

    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void apply()
    {
        super.apply(true);

        MarketAPI market = this.market;
        int size = market.getSize();

        supply(boggledTools.BoggledCommodities.domainArtifacts, (size - 2));

        // Modify production based on ruins.
        // This is usually done by the condition itself, but it's done here for this industry because vanilla ruins don't impact production.
        if(market.hasCondition(Conditions.RUINS_SCATTERED))
        {
            this.supply("boggledRuinsMod", boggledTools.BoggledCommodities.domainArtifacts, -1, Misc.ucFirst("Scattered ruins"));
        }
        else if(market.hasCondition(Conditions.RUINS_WIDESPREAD))
        {
            //Do nothing - no impact on production
        }
        else if(market.hasCondition(Conditions.RUINS_EXTENSIVE))
        {
            this.supply("boggledRuinsMod", boggledTools.BoggledCommodities.domainArtifacts, 1, Misc.ucFirst("Extensive ruins"));
        }
        else if(market.hasCondition(Conditions.RUINS_VAST))
        {
            this.supply("boggledRuinsMod", boggledTools.BoggledCommodities.domainArtifacts, 2, Misc.ucFirst("Vast ruins"));
        }

        if (!this.isFunctional())
        {
            this.supply.clear();
        }
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        return commonindustry.isAvailableToBuild(getMarket());
//        if(!boggledTools.isResearched(this.getId()))
//        {
//            return false;
//        }

//        MarketAPI market = this.market;
//        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled) && (market.hasCondition(Conditions.RUINS_SCATTERED) || market.hasCondition(Conditions.RUINS_WIDESPREAD) || market.hasCondition(Conditions.RUINS_EXTENSIVE) || market.hasCondition(Conditions.RUINS_VAST)))
//        {
//            return true;
//        }
//        else
//        {
//            return false;
//        }
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return commonindustry.showWhenUnavailable(getMarket());
//        if(!boggledTools.isResearched(this.getId()))
//        {
//            return false;
//        }

//        if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) || !boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
//        {
//            return false;
//        }
//        else
//        {
//            return true;
//        }
    }

    @Override
    public String getUnavailableReason()
    {
        if(!(market.hasCondition(Conditions.RUINS_SCATTERED) || market.hasCondition(Conditions.RUINS_WIDESPREAD) || market.hasCondition(Conditions.RUINS_EXTENSIVE) || market.hasCondition(Conditions.RUINS_VAST)))
        {
            return "Requires ruins";
        }
        else
        {
            return "Error in getUnavailableReason() in the domain archaeology structure. Please tell Boggled about this on the forums.";
        }
    }

    @Override
    public float getPatherInterest()
    {
        float base = 1f;
        if (market.hasCondition(Conditions.RUINS_VAST))
        {
            base = 4;
        }
        else if (market.hasCondition(Conditions.RUINS_EXTENSIVE))
        {
            base = 3;
        }
        else if (market.hasCondition(Conditions.RUINS_WIDESPREAD))
        {
            base = 2;
        }
        else if (market.hasCondition(Conditions.RUINS_SCATTERED))
        {
            base = 1;
        }

        return base + super.getPatherInterest();
    }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return true;
    }
}

