package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import data.campaign.econ.boggledTools;

public class Boggled_Domain_Archaeology extends BoggledBaseIndustry {
    static {
        ResourceDepositsCondition.COMMODITY.put(Conditions.RUINS_SCATTERED, boggledTools.BoggledCommodities.domainArtifacts);
        ResourceDepositsCondition.COMMODITY.put(Conditions.RUINS_WIDESPREAD, boggledTools.BoggledCommodities.domainArtifacts);
        ResourceDepositsCondition.COMMODITY.put(Conditions.RUINS_EXTENSIVE, boggledTools.BoggledCommodities.domainArtifacts);
        ResourceDepositsCondition.COMMODITY.put(Conditions.RUINS_VAST, boggledTools.BoggledCommodities.domainArtifacts);

        ResourceDepositsCondition.MODIFIER.put(Conditions.RUINS_SCATTERED, -1);
        ResourceDepositsCondition.MODIFIER.put(Conditions.RUINS_WIDESPREAD, 0);
        ResourceDepositsCondition.MODIFIER.put(Conditions.RUINS_EXTENSIVE, 1);
        ResourceDepositsCondition.MODIFIER.put(Conditions.RUINS_VAST, 2);

        ResourceDepositsCondition.INDUSTRY.put(boggledTools.BoggledCommodities.domainArtifacts, boggledTools.BoggledIndustries.domainArchaeologyIndustryId);

        ResourceDepositsCondition.BASE_MODIFIER.put(boggledTools.BoggledCommodities.domainArtifacts, -2);
    }

    public Boggled_Domain_Archaeology() {
        super();
    }

//    @Override
//    public float getPatherInterest()
//    {
//        float base = 1f;
//        if (market.hasCondition(Conditions.RUINS_VAST))
//        {
//            base = 4;
//        }
//        else if (market.hasCondition(Conditions.RUINS_EXTENSIVE))
//        {
//            base = 3;
//        }
//        else if (market.hasCondition(Conditions.RUINS_WIDESPREAD))
//        {
//            base = 2;
//        }
//        else if (market.hasCondition(Conditions.RUINS_SCATTERED))
//        {
//            base = 1;
//        }
//
//        return base + super.getPatherInterest();
//    }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return true;
    }
}

