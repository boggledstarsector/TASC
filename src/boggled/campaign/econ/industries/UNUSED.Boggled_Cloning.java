package boggled.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.*;

public class Boggled_Cloning extends BoggledBaseIndustry implements MarketImmigrationModifier {
    public Boggled_Cloning() {
        super();
    }

//    @Override
//    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
//        thisIndustry.addPostDemandSection(this, tooltip, hasDemand, mode);
//
//        if (isFunctional())
//        {
//            tooltip.addPara("%s population growth (based on colony size)", 10f, Misc.getHighlightColor(), "+" + (int) getImmigrationBonus());
//        }
//    }

//    @Override
//    public void modifyIncoming(MarketAPI market, PopulationComposition incoming)
//    {
//        incoming.getWeight().modifyFlat(getModId(), getImmigrationBonus(), Misc.ucFirst(this.getCurrentName().toLowerCase()));
//    }
//
//    protected float getImmigrationBonus() {
//        return Math.max(0, market.getSize() - 1);
//    }
//
//    @Override
//    protected boolean canImproveToIncreaseProduction() {
//        return true;
//    }
}

