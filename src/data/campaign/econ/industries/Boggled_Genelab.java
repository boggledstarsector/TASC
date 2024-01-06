package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.*;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;

public class Boggled_Genelab extends BaseIndustry implements BoggledIndustryInterface {
    private final BoggledCommonIndustry thisIndustry;

    public Boggled_Genelab() {
        super();
        thisIndustry = boggledTools.getIndustryProject("genelab");
    }

    @Override
    public void startBuilding() {
        super.startBuilding();
        thisIndustry.startBuilding(this);
    }

    @Override
    public void startUpgrading() {
        super.startUpgrading();
        thisIndustry.startUpgrading(this);
    }

    @Override
    protected void buildingFinished() {
        super.buildingFinished();
        thisIndustry.buildingFinished(this);
    }

    @Override
    protected void upgradeFinished(Industry previous) {
        super.upgradeFinished(previous);
        thisIndustry.upgradeFinished(this, previous);
    }

    @Override
    public void finishBuildingOrUpgrading() {
        super.finishBuildingOrUpgrading();
        thisIndustry.finishBuildingOrUpgrading(this);
    }

    @Override
    public boolean isBuilding() { return thisIndustry.isBuilding(this); }

    @Override
    public boolean isFunctional() { return super.isFunctional() && thisIndustry.isFunctional(); }

    @Override
    public boolean isUpgrading() { return thisIndustry.isUpgrading(this); }

    @Override
    public float getBuildOrUpgradeProgress() { return thisIndustry.getBuildOrUpgradeProgress(this); }

    @Override
    public String getBuildOrUpgradeDaysText() {
        return thisIndustry.getBuildOrUpgradeDaysText(this);
    }

    @Override
    public String getBuildOrUpgradeProgressText() {
        return thisIndustry.getBuildOrUpgradeProgressText(this);
    }

    @Override
    public boolean isAvailableToBuild() { return thisIndustry.isAvailableToBuild(this); }

    @Override
    public boolean showWhenUnavailable() { return thisIndustry.showWhenUnavailable(this); }

    @Override
    public String getUnavailableReason() { return thisIndustry.getUnavailableReason(this); }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        thisIndustry.advance(amount, this);
    }

    @Override
    public void apply() {
        super.apply(true);
        thisIndustry.apply(this, this);
    }

    @Override
    public void unapply() {
        super.unapply();
        thisIndustry.unapply(this, this);
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        thisIndustry.addRightAfterDescriptionSection(this, tooltip, mode);
    }

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return true;
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        thisIndustry.addPostDemandSection(this, tooltip, hasDemand, mode);
    }

    @Override
    public void applyDeficitToProduction(String modId, Pair<String, Integer> deficit, String... commodities) {
        thisIndustry.applyDeficitToProduction(this, modId, deficit, commodities);
    }

    @Override
    public void setFunctional(boolean functional) {
        thisIndustry.setFunctional(functional);
    }

    private boolean pollutionIsOngoing()
    {
        if(this.market.hasCondition(Conditions.HABITABLE))
        {
            if(this.market.hasIndustry(Industries.HEAVYINDUSTRY) && this.market.getIndustry(Industries.HEAVYINDUSTRY).getSpecialItem() != null && (this.market.getIndustry(Industries.HEAVYINDUSTRY).getSpecialItem().getId().equals(Items.CORRUPTED_NANOFORGE) || this.market.getIndustry(Industries.HEAVYINDUSTRY).getSpecialItem().getId().equals(Items.PRISTINE_NANOFORGE)))
            {
                return true;
            }
            else if(this.market.hasIndustry(Industries.ORBITALWORKS) && this.market.getIndustry(Industries.ORBITALWORKS).getSpecialItem() != null && (this.market.getIndustry(Industries.ORBITALWORKS).getSpecialItem().getId().equals(Items.CORRUPTED_NANOFORGE) || this.market.getIndustry(Industries.ORBITALWORKS).getSpecialItem().getId().equals(Items.PRISTINE_NANOFORGE)))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade) {
        super.notifyBeingRemoved(mode, forUpgrade);
    }

    @Override
    public float getPatherInterest()
    {
        if(!this.market.isPlayerOwned())
        {
            return super.getPatherInterest();
        }
        else
        {
            return 10.0F;
        }
    }

//    @Override
//    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
//    {
//        float opad = 10.0F;
//        Color highlight = Misc.getHighlightColor();
//        Color bad = Misc.getNegativeHighlightColor();
//
//        //
//        // Inserts pollution cleanup status
//        //
//
//        if(this.market.hasCondition(Conditions.POLLUTION) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
//        {
//            //200 days to clean up; divide daysWithoutShortage by 2 to get the percent
//            int percentComplete = this.daysWithoutShortagePollution / 2;
//
//            //Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
//            if(percentComplete > 99)
//            {
//                percentComplete = 99;
//            }
//
//            if(pollutionIsOngoing())
//            {
//                tooltip.addPara("Heavy industrial activity is currently polluting " + this.market.getName() + ". Remediation can only begin once this ceases.", bad, opad);
//            }
//            else
//            {
//                tooltip.addPara("Approximately %s of the pollution on " + this.market.getName() + " has been remediated.", opad, highlight, percentComplete + "%");
//            }
//        }
//
//        if(this.isDisrupted() && this.market.hasCondition(Conditions.POLLUTION) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
//        {
//            tooltip.addPara("Pollution remediation progress is stalled while the Genelab is disrupted.", bad, opad);
//        }
//
//        //
//        // Inserts lobster seeding status
//        //
//
//        if(this.market.hasCondition(Conditions.WATER_SURFACE) && !this.market.hasCondition(Conditions.VOLTURNIAN_LOBSTER_PENS) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
//        {
//            //200 days to seed; divide daysWithoutShortage by 2 to get the percent
//            int percentComplete = this.daysWithoutShortageLobsters / 2;
//
//            //Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
//            if(percentComplete > 99)
//            {
//                percentComplete = 99;
//            }
//
//            tooltip.addPara("Lobster seeding is approximately %s complete.", opad, highlight, percentComplete + "%");
//
//        }
//
//        if(this.isDisrupted() && this.market.hasCondition(Conditions.WATER_SURFACE) && !this.market.hasCondition(Conditions.VOLTURNIAN_LOBSTER_PENS) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
//        {
//            tooltip.addPara("Lobster seeding progress is stalled while the Genelab is disrupted.", bad, opad);
//        }
//    }

//    @Override
//    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
//    {
//        boolean shortage = genelabHasShortage();
//        float opad = 10.0F;
//        Color bad = Misc.getNegativeHighlightColor();
//
//        if(shortage && this.market.hasCondition(Conditions.POLLUTION) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
//        {
//            tooltip.addPara("Pollution remediation progress is stalled due to a shortage of Domain-era artifacts.", bad, opad);
//        }
//
//        if(shortage && this.market.hasCondition(Conditions.WATER_SURFACE) && !this.market.hasCondition(Conditions.VOLTURNIAN_LOBSTER_PENS) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
//        {
//            tooltip.addPara("Lobster seeding progress is stalled due to a shortage of Domain-era artifacts.", bad, opad);
//        }
//    }

    @Override
    public boolean canImprove()
    {
        return false;
    }

    @Override
    public boolean canInstallAICores() {
        return false;
    }
}
