package data.campaign.econ.industries;

import data.campaign.econ.boggledTools;

public class Boggled_Expand_Station extends BoggledBaseIndustry {
    public Boggled_Expand_Station() {
        super();
    }

    @Override
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
}
