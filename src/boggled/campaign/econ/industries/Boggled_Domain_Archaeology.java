package boggled.campaign.econ.industries;

import java.lang.String;

import boggled.campaign.econ.industries.interfaces.ShowBoggledTerraformingMenuOption;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import boggled.campaign.econ.boggledTools;

public class Boggled_Domain_Archaeology extends BaseIndustry implements ShowBoggledTerraformingMenuOption
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

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isResearched("tasc_domain_excavation"))
        {
            return false;
        }

        MarketAPI market = this.market;
        if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled") && (market.hasCondition(Conditions.RUINS_SCATTERED) || market.hasCondition(Conditions.RUINS_WIDESPREAD) || market.hasCondition(Conditions.RUINS_EXTENSIVE) || market.hasCondition(Conditions.RUINS_VAST)))
        {
            return true;
        }
        else
        {
            return false;
        }
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
        else
        {
            return true;
        }
    }

    @Override
    public String getUnavailableReason()
    {
        if(!(market.hasCondition(Conditions.RUINS_SCATTERED) || market.hasCondition(Conditions.RUINS_WIDESPREAD) || market.hasCondition(Conditions.RUINS_EXTENSIVE) || market.hasCondition(Conditions.RUINS_VAST)))
        {
            return ("Requires ruins");
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

