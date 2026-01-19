package boggled.campaign.econ.industries;

import boggled.campaign.econ.boggledTools;
import boggled.campaign.econ.industries.interfaces.ShowBoggledTerraformingMenuOption;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import java.lang.String;
import java.util.ArrayList;

public class Boggled_Cloning extends BaseIndustry implements MarketImmigrationModifier, ShowBoggledTerraformingMenuOption
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

        // Demand and supply
        if(boggledTools.domainEraArtifactDemandEnabled())
        {
            this.demand("domain_artifacts", size);
        }

        if (!this.market.hasCondition("habitable")) {
            this.demand("organics", size - 2);
        }

        this.supply("organs", size - 2);

        // Deficit impact on supply
        ArrayList<String> deficitCommodities = new ArrayList<>();
        if(boggledTools.domainEraArtifactDemandEnabled())
        {
            deficitCommodities.add("domain_artifacts");
        }

        if (!this.market.hasCondition("habitable")) {
            deficitCommodities.add("organics");
        }

        Pair<String, Integer> deficit = this.getMaxDeficit(deficitCommodities.toArray(new String[0]));
        this.applyDeficitToProduction(1, deficit, new String[]{"organs"});

        if (!this.isFunctional())
        {
            this.supply.clear();
            this.unapply();
        }
    }

    public void modifyIncoming(MarketAPI market, PopulationComposition incoming)
    {
        if (this.isFunctional())
        {
            incoming.getWeight().modifyFlat(getModId(), getImmigrationBonus(), Misc.ucFirst(this.getCurrentName().toLowerCase()));
        }
    }

    protected float getImmigrationBonus() {
        return Math.max(0, market.getSize() - 1);
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.getBooleanSetting("boggledCloningEnabled"))
        {
            return false;
        }

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.getBooleanSetting("boggledCloningEnabled"))
        {
            return false;
        }

        return super.showWhenUnavailable();
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

