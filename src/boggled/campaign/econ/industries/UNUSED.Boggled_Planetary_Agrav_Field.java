package boggled.campaign.econ.industries;

public class Boggled_Planetary_Agrav_Field extends BoggledBaseIndustry {
    public Boggled_Planetary_Agrav_Field() {
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
//
//        if(isFunctional() && (this.market.hasIndustry(boggledTools.BoggledIndustries.domedCitiesIndustryId) || boggledTools.getPlanetType(this.market.getPlanetEntity()).getPlanetId().equals(boggledTools.gasGiantPlanetId)))
//        {
//            for (String cid : SUPPRESSED_CONDITIONS)
//            {
//                market.suppressCondition(cid);
//            }
//        }
//    }
//
//    @Override
//    public void unapply() {
//        super.unapply();
//        thisIndustry.unapply(this, this);
//
//        for (String cid : SUPPRESSED_CONDITIONS)
//        {
//            market.unsuppressCondition(cid);
//        }
//    }
//
//    @Override
//    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
//        thisIndustry.addRightAfterDescriptionSection(this, tooltip, mode);
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
//
//        float opad = 10.0F;
//        Color bad = Misc.getNegativeHighlightColor();
//
//        if(mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.QUEUED ||!isFunctional() || !this.market.hasIndustry(boggledTools.BoggledIndustries.domedCitiesIndustryId))
//        {
//            tooltip.addPara("If operational, would counter the effects of:", opad, Misc.getHighlightColor(), "");
//            int numCondsCountered = 0;
//            for (String id : SUPPRESSED_CONDITIONS)
//            {
//                if(this.market.hasCondition(id))
//                {
//                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
//                    tooltip.addPara("           %s", 2f, Misc.getHighlightColor(), condName);
//                    numCondsCountered++;
//                }
//            }
//
//            if(numCondsCountered == 0)
//            {
//                tooltip.addPara("           %s", 2f, Misc.getGrayColor(), "(none)");
//            }
//        }
//
//        if(mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && isFunctional() && this.market.hasIndustry(boggledTools.BoggledIndustries.domedCitiesIndustryId))
//        {
//            tooltip.addPara("Countering the effects of:", opad, Misc.getHighlightColor(), "");
//            int numCondsCountered = 0;
//            for (String id : SUPPRESSED_CONDITIONS)
//            {
//                if(this.market.hasCondition(id))
//                {
//                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
//                    tooltip.addPara("           %s", 2f, Misc.getHighlightColor(), condName);
//                    numCondsCountered++;
//                }
//            }
//
//            if(numCondsCountered == 0)
//            {
//                tooltip.addPara("           %s", 2f, Misc.getGrayColor(), "(none)");
//            }
//        }
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
//    public static List<String> SUPPRESSED_CONDITIONS = new ArrayList<String>();
//    static
//    {
//        SUPPRESSED_CONDITIONS.add(Conditions.HIGH_GRAVITY);
//        SUPPRESSED_CONDITIONS.add(Conditions.LOW_GRAVITY);
//    }
//
//    @Override
//    public void applyAICoreToIncomeAndUpkeep()
//    {
//        //Prevents AI cores from modifying upkeep
//    }
//
//    @Override
//    protected void applyAlphaCoreSupplyAndDemandModifiers()
//    {
//        //Prevents AI cores from modifying supply and demand
//    }
//
//    @Override
//    public boolean canImprove() {
//        return false;
//    }
//
//    @Override
//    public float getPatherInterest() { return super.getPatherInterest() + 2.0f; }
//
//    @Override
//    public boolean canInstallAICores() {
//        return false;
//    }
}

