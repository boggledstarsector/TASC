package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.impl.Cryosanctum;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;

public class Boggled_Cryosanctum extends Cryosanctum implements BoggledIndustryInterface {
    private final BoggledCommonIndustry thisIndustry;

    public Boggled_Cryosanctum() {
        super();
        thisIndustry = boggledTools.getIndustryProject("cryosanctum");
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
    public void unapply()
    {
        super.unapply();
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
    public void applyDeficitToProduction(int index, Pair<String, Integer> deficit, String... commodities) {
        super.applyDeficitToProduction(index, deficit, commodities);
    }

    @Override
    public void setFunctional(boolean functional) {
        thisIndustry.setFunctional(functional);
    }
}

