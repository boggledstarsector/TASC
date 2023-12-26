package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.String;

public class Boggled_Cloning extends BaseIndustry implements MarketImmigrationModifier
{
    private static BoggledCommonIndustry commonindustry;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        commonindustry = new BoggledCommonIndustry(data, "Cloning");
    }

    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void apply()
    {
        super.apply(true);

        int size = this.market.getSize();

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            this.demand(boggledTools.BoggledCommodities.domainArtifacts, size - 2);
        }
        this.supply(Commodities.ORGANS, size - 2);

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit(boggledTools.BoggledCommodities.domainArtifacts);
            this.applyDeficitToProduction(1, deficit, Commodities.ORGANS);
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
        return commonindustry.isAvailableToBuild(getMarket());
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return commonindustry.showWhenUnavailable(getMarket());
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

