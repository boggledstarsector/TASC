package data.campaign.econ.industries;

import java.lang.String;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import data.campaign.econ.boggledTools;

public class Boggled_Expand_Station extends BaseIndustry {
    private final BoggledCommonIndustry thisIndustry;

    public Boggled_Expand_Station() {
        super();
        thisIndustry = boggledTools.getIndustryProject("expand_station");
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
        boggledTools.incrementNumberOfStationExpansions(this.market);

        this.market.removeIndustry(boggledTools.BoggledIndustries.stationExpansionIndustryId,null,false);
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
    public void apply() { super.apply(true); }

    @Override
    public void unapply() {
        super.unapply();
    }

    public float getBuildCost()
    {
        if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.stationProgressIncreaseInCostsToExpandStation))
        {
            return this.getSpec().getCost();
        }
        else
        {
            double cost = (this.getSpec().getCost() * (Math.pow(2, boggledTools.getNumberOfStationExpansions(this.market))));
            return (float)cost;
        }
    }



    public boolean canInstallAICores() {
        return false;
    }
}
