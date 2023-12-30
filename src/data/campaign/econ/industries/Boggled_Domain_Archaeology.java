package data.campaign.econ.industries;

import java.lang.String;

import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import data.campaign.econ.boggledTools;

public class Boggled_Domain_Archaeology extends BaseIndustry {
    private final BoggledCommonIndustry thisIndustry;

    public Boggled_Domain_Archaeology() {
        super();
        thisIndustry = boggledTools.getIndustryProject("domain_archaeology");
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
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void apply() {
        super.apply(true);
        thisIndustry.apply(this);

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

