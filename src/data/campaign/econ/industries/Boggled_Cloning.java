package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import java.lang.String;

public class Boggled_Cloning extends BaseIndustry implements MarketImmigrationModifier
{
    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void apply()
    {
        super.apply(true);

        int size = this.market.getSize();

        if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled"))
        {
            this.demand("domain_artifacts", size - 2);
        }
        this.supply("organs", size - 2);

        if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled"))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"domain_artifacts"});
            this.applyDeficitToProduction(1, deficit, new String[]{"organs"});
        }

        if (!this.isFunctional())
        {
            this.supply.clear();
            this.unapply();
        }
    }

    public void modifyIncoming(MarketAPI market, PopulationComposition incoming)
    {
        incoming.getWeight().modifyFlat(getModId(), getImmigrationBonus(), Misc.ucFirst(this.getCurrentName().toLowerCase()));
    }

    protected float getImmigrationBonus() {
        return Math.max(0, market.getSize() - 1);
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        if(boggledTools.getBooleanSetting("boggledCloningEnabled"))
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
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        return false;
    }

    @Override
    public float getPatherInterest() {
        return 10.0F;
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        if (isFunctional())
        {
            tooltip.addPara("%s population growth (based on colony size)", 10f, Misc.getHighlightColor(), "+" + (int) getImmigrationBonus());
        }
    }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return true;
    }
}

