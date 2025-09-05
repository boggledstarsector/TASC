package boggled.campaign.econ.industries;

import java.lang.String;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import boggled.campaign.econ.boggledTools;

public class Boggled_Expand_Station extends BaseIndustry {

    @Override
    public void apply() { super.apply(true); }

    @Override
    public void unapply() {
        super.unapply();
    }

    @Override
    public void finishBuildingOrUpgrading() {
        super.finishBuildingOrUpgrading();
    }

    public float getBuildCost()
    {
        if(!boggledTools.getBooleanSetting("boggledStationProgressiveIncreaseInCostsToExpandStation"))
        {
            return this.getSpec().getCost();
        }
        else
        {
            double cost = (this.getSpec().getCost() * (Math.pow(2, boggledTools.getNumberOfStationExpansions(this.market))));
            return (float)cost;
        }
    }

    @Override
    protected void buildingFinished()
    {
        super.buildingFinished();

        boggledTools.stepTag(this.market, "boggled_station_construction_numExpansions_", 1);

        this.market.removeIndustry("BOGGLED_STATION_EXPANSION",null,false);
    }

    @Override
    public void startBuilding() {
        super.startBuilding();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(boggledTools.getBooleanSetting("boggledStationCrampedQuartersEnabled") && boggledTools.getBooleanSetting("boggledStationCrampedQuartersPlayerCanPayToIncreaseStationSize") && this.market.getPrimaryEntity().hasTag("station") && (11 > (boggledTools.getIntSetting("boggledStationCrampedQuartersSizeGrowthReductionStarts") + boggledTools.getNumberOfStationExpansions(this.market))))
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
        return false;
    }

    @Override
    public String getUnavailableReason()
    {
        return "This text should never be seen. Tell Boggled about this on the forums and mention 'getUnavailableReason() in Expand_station'";
    }

    public boolean canInstallAICores() {
        return false;
    }

    public boolean canImprove() {
        return false;
    }
}
