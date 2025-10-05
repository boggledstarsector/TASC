package boggled.campaign.econ.industries;

import java.lang.String;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import boggled.campaign.econ.boggledTools;

public class Boggled_Domain_Archaeology extends BaseIndustry
{
    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void apply()
    {
        super.apply(true);

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

    public boolean marketHasRuins(MarketAPI market)
    {
        return market.hasCondition(Conditions.RUINS_SCATTERED) || market.hasCondition(Conditions.RUINS_WIDESPREAD) || market.hasCondition(Conditions.RUINS_EXTENSIVE) || market.hasCondition(Conditions.RUINS_VAST);
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isResearched("tasc_domain_excavation"))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled"))
        {
            return false;
        }

        if(!marketHasRuins(this.market))
        {
            return false;
        }

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.isResearched("tasc_domain_excavation"))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled"))
        {
            return false;
        }

        if(!marketHasRuins(this.market))
        {
            return super.showWhenUnavailable();
        }

        return super.showWhenUnavailable();
    }

    @Override
    public String getUnavailableReason()
    {
        if(!marketHasRuins(this.market))
        {
            return "Requires ruins";
        }

        return super.getUnavailableReason();
    }

    @Override
    public float getPatherInterest()
    {
        // Only increase pather interest on player-owned planets (i.e. not Agreus)
        float pather_interest_modifer = 0.0F;
        if(this.market.isPlayerOwned()) {
            if (market.hasCondition(Conditions.RUINS_VAST)) {
                pather_interest_modifer = 4.0F;
            } else if (market.hasCondition(Conditions.RUINS_EXTENSIVE)) {
                pather_interest_modifer = 3.0F;
            } else if (market.hasCondition(Conditions.RUINS_WIDESPREAD)) {
                pather_interest_modifer = 2.0F;
            } else if (market.hasCondition(Conditions.RUINS_SCATTERED)) {
                pather_interest_modifer = 1.0F;
            }
        }

        return super.getPatherInterest() + pather_interest_modifer;
    }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return true;
    }
}

