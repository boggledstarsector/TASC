package boggled.campaign.econ.industries;

public class Boggled_Planet_Cracker extends BoggledBaseIndustry {
    public Boggled_Planet_Cracker() {
        super();
    }

//    @Override
//    public void startBuilding() {
//        super.startBuilding();
//        thisIndustry.startBuilding(this);
//    }
//
//    @Override
//    public void startUpgrading() {
//        super.startUpgrading();
//        thisIndustry.startUpgrading(this);
//    }
//
//    @Override
//    protected void buildingFinished() {
//        super.buildingFinished();
//        thisIndustry.buildingFinished(this, this);
//    }
//
//    @Override
//    protected void upgradeFinished(Industry previous) {
//        super.upgradeFinished(previous);
//        thisIndustry.upgradeFinished(this, previous);
//    }
//
//    @Override
//    public void finishBuildingOrUpgrading() {
//        super.finishBuildingOrUpgrading();
//        thisIndustry.finishBuildingOrUpgrading(this);
//    }
//
//    @Override
//    public boolean isBuilding() { return thisIndustry.isBuilding(this); }
//
//    @Override
//    public boolean isFunctional() { return super.isFunctional() && thisIndustry.isFunctional(); }
//
//    @Override
//    public boolean isUpgrading() { return thisIndustry.isUpgrading(this); }
//
//    @Override
//    public float getBuildOrUpgradeProgress() { return thisIndustry.getBuildOrUpgradeProgress(this); }
//
//    @Override
//    public String getBuildOrUpgradeDaysText() {
//        return thisIndustry.getBuildOrUpgradeDaysText(this);
//    }
//
//    @Override
//    public String getBuildOrUpgradeProgressText() {
//        return thisIndustry.getBuildOrUpgradeProgressText(this);
//    }
//
//    @Override
//    public boolean isAvailableToBuild() { return thisIndustry.isAvailableToBuild(this); }
//
//    @Override
//    public boolean showWhenUnavailable() { return thisIndustry.showWhenUnavailable(this); }
//
//    @Override
//    public String getUnavailableReason() { return thisIndustry.getUnavailableReason(this); }
//
//    @Override
//    public void advance(float amount) {
//        super.advance(amount);
//        thisIndustry.advance(amount, this);
//    }
//
//    @Override
//    public void apply() {
//        super.apply(true);
//        thisIndustry.apply(this, this);
//    }
//
//     @Override
//    public void unapply() {
//        super.unapply();
//        thisIndustry.unapply(this, this);
//    }
//
//    @Override
//    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
//        thisIndustry.addRightAfterDescriptionSection(this, tooltip, mode);
//
//        float opad = 10.0F;
//        Color highlight = Misc.getHighlightColor();
//
//        thisIndustry.tooltipIncomplete(this, tooltip, mode, "Planet cracking is approximately %s complete on " + thisIndustry.getFocusMarketOrMarket(getMarket()).getName() + ".", opad, highlight, thisIndustry.getPercentComplete(0, getMarket()) + "%");
//
//        thisIndustry.tooltipComplete(this, tooltip, mode, "Further planet cracking operations would serve no purpose on " + thisIndustry.getFocusMarketOrMarket(getMarket()).getName() + ". The Planet Cracker can now be deconstructed without any risk of regression.", opad, highlight);
//
//        thisIndustry.tooltipDisrupted(this, tooltip, mode, "Progress is stalled while the planet cracker is disrupted.", opad, Misc.getNegativeHighlightColor());
//    }
//
//    @Override
//    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
//        return true;
//    }
//
//    @Override
//    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
//        thisIndustry.addPostDemandSection(this, tooltip, hasDemand, mode);
//    }
//
//    @Override
//    public void applyDeficitToProduction(String modId, Pair<String, Integer> deficit, String... commodities) {
//        thisIndustry.applyDeficitToProduction(this, modId, deficit, commodities);
//    }
//
//    @Override
//    public void setFunctional(boolean functional) {
//        thisIndustry.setFunctional(functional);
//    }
//
//    @Override
//    public void modifyPatherInterest(String id, float patherInterest) {
//
//    }
//
//    @Override
//    public void unmodifyPatherInterest(String id) {
//
//    }
//
//    @Override
//    public float getBasePatherInterest() {
//        return 0;
//    }
//
//    @Override
//    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
//    {
//        super.notifyBeingRemoved(mode, forUpgrade);
//    }
//
//    @Override
//    public float getPatherInterest() { return super.getPatherInterest() + 2.0f; }
//
//    @Override
//    public boolean canImprove() { return false; }
//
//    @Override
//    public boolean canInstallAICores() {
//        return false;
//    }
}

