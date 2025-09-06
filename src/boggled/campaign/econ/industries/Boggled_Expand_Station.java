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
        if(!boggledTools.getBooleanSetting("boggledStationCrampedQuartersEnabled") || !boggledTools.getBooleanSetting("boggledStationCrampedQuartersPlayerCanPayToIncreaseStationSize"))
        {
            return false;
        }

        if(!boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        if(11 <= (boggledTools.getIntSetting("boggledStationCrampedQuartersSizeGrowthReductionStarts") + boggledTools.getNumberOfStationExpansions(this.market)))
        {
            return false;
        }

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.getBooleanSetting("boggledStationCrampedQuartersEnabled") || !boggledTools.getBooleanSetting("boggledStationCrampedQuartersPlayerCanPayToIncreaseStationSize"))
        {
            return false;
        }

        if(!boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        if(11 <= (boggledTools.getIntSetting("boggledStationCrampedQuartersSizeGrowthReductionStarts") + boggledTools.getNumberOfStationExpansions(this.market)))
        {
            return super.showWhenUnavailable();
        }

        return super.showWhenUnavailable();
    }

    @Override
    public String getUnavailableReason()
    {
        if(11 <= (boggledTools.getIntSetting("boggledStationCrampedQuartersSizeGrowthReductionStarts") + boggledTools.getNumberOfStationExpansions(this.market)))
        {
            return this.market.getName() + " has already been expanded to the maximum size.";
        }

        return super.getUnavailableReason();
    }

    public boolean canInstallAICores() {
        return false;
    }

    public boolean canImprove() {
        return false;
    }
}
