package boggled.campaign.econ.industries;

public class Boggled_Ismara_Sling extends BoggledBaseIndustry {
//    private final BoggledCommonIndustry thisIndustry;

    public Boggled_Ismara_Sling() {
        super();
    }

//    @Override
//    public void advance(float amount) {
//        super.advance(amount);
//
//        // This check exists to remove Ismara's Sling if the planet was terraformed to a type that is incompatible with it.
//        // If market is not station and market's water level is below 2 (high water supply level)
//        if (!boggledTools.marketIsStation(getMarket()) && boggledTools.getPlanetType(getMarket().getPlanetEntity()).getWaterLevel(getMarket()) < 2)
//        {
//            // If an AI core is installed, put one in storage so the player doesn't "lose" an AI core
//            if (this.aiCoreId != null)
//            {
//                CargoAPI cargo = this.market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();
//                if (cargo != null)
//                {
//                    cargo.addCommodity(this.aiCoreId, 1.0F);
//                }
//            }
//
//            if (this.market.hasIndustry(boggledTools.BoggledIndustries.ismaraSlingIndustryId))
//            {
//                // Pass in null for mode when calling this from API code.
//                this.market.removeIndustry(boggledTools.BoggledIndustries.ismaraSlingIndustryId, null, false);
//            }
//
//            if (this.market.isPlayerOwned())
//            {
//                MessageIntel intel = new MessageIntel("Ismara's Sling on " + this.market.getName(), Misc.getBasePlayerColor());
//                intel.addLine("    - Deconstructed");
//                intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
//                intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
//                Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
//            }
//        }
//    }
//
//    public boolean slingHasShortage()
//    {
//        boolean shortage = false;
//        Pair<String, Integer> deficit = this.getMaxDeficit(Commodities.HEAVY_MACHINERY);
//        if(deficit.two != 0)
//        {
//            shortage = true;
//        }
//
//        return shortage;
//    }

//    @Override
//    public String getCurrentName()
//    {
//        if(boggledTools.marketIsStation(this.market))
//        {
//            return "Asteroid Processing";
//        }
//        else
//        {
//            return "Ismara's Sling";
//        }
//    }
//
//    @Override
//    public String getCurrentImage()
//    {
//        if(boggledTools.marketIsStation(this.market))
//        {
//            return Global.getSettings().getSpriteName("boggled", "asteroid_processing");
//        }
//        else
//        {
//            return this.getSpec().getImageName();
//        }
//    }

//    @Override
//    protected String getDescriptionOverride()
//    {
//        if(boggledTools.marketIsStation(this.market))
//        {
//            return "Crashing asteroids rich in water-ice into planets is an effective means of terraforming - except when the asteroid is so large that the impact would be cataclysmic. In this case, the asteroid can be towed to a space station, where the water-ice is safely extracted and shipped to the destination planet. Can only help terraform worlds in the same system.";
//        }
//        return null;
//    }

//    @Override
//    public void apply() {
//        super.apply(true);
//        thisIndustry.apply(this, this);
//
//        super.apply(false);
//        super.applyIncomeAndUpkeep(3);
//    }
//
//    @Override
//    public void unapply() {
//        super.unapply();
//        thisIndustry.unapply(this, this);
//    }

//    @Override
//    public float getPatherInterest() { return 10.0F; }
//
//    @Override
//    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
//        thisIndustry.addPostDemandSection(this, tooltip, hasDemand, mode);
//    }
//
//    @Override
//    public boolean canInstallAICores() {
//        return false;
//    }
//
//    @Override
//    public boolean canImprove() { return false; }
//
//    @Override
//    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
//    {
//        float opad = 10.0F;
//        Color bad = Misc.getNegativeHighlightColor();
//
//        if(slingHasShortage())
//        {
//            tooltip.addPara(this.getCurrentName() + " is experiencing a shortage of heavy machinery. No water-ice can be supplied for terraforming projects until the shortage is resolved.", bad, opad);
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
}
