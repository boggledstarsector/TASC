package data.campaign.econ.industries;

public class Boggled_Domain_Archaeology extends BoggledBaseIndustry {


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

