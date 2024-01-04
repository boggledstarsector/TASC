package data.campaign.econ.industries;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;

public class Boggled_Ismara_Sling extends BaseIndustry implements BoggledIndustryInterface {
    private final BoggledCommonIndustry thisIndustry;

    public Boggled_Ismara_Sling() {
        super();
        thisIndustry = boggledTools.getIndustryProject("ismara_sling");
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

//    @Override
//    public void advance(float amount) {
//        super.advance(amount);
//        thisIndustry.advance(amount, this);
//    }

    @Override
    public boolean canBeDisrupted()
    {
        return true;
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        // This check exists to remove Ismara's Sling if the planet was terraformed to a type that is incompatible with it.
        // If market is not station and market's water level is below 2 (high water supply level)
        if (!boggledTools.marketIsStation(getMarket()) && boggledTools.getPlanetType(getMarket().getPlanetEntity()).getWaterLevel(getMarket()) < 2)
        {
            // If an AI core is installed, put one in storage so the player doesn't "lose" an AI core
            if (this.aiCoreId != null)
            {
                CargoAPI cargo = this.market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();
                if (cargo != null)
                {
                    cargo.addCommodity(this.aiCoreId, 1.0F);
                }
            }

            if (this.market.hasIndustry(boggledTools.BoggledIndustries.ismaraSlingIndustryId))
            {
                // Pass in null for mode when calling this from API code.
                this.market.removeIndustry(boggledTools.BoggledIndustries.ismaraSlingIndustryId, null, false);
            }

            if (this.market.isPlayerOwned())
            {
                MessageIntel intel = new MessageIntel("Ismara's Sling on " + this.market.getName(), Misc.getBasePlayerColor());
                intel.addLine("    - Deconstructed");
                intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
            }
        }
    }

    public boolean slingHasShortage()
    {
        boolean shortage = false;
        Pair<String, Integer> deficit = this.getMaxDeficit(Commodities.HEAVY_MACHINERY);
        if(deficit.two != 0)
        {
            shortage = true;
        }

        return shortage;
    }

    @Override
    public String getCurrentName()
    {
        if(boggledTools.marketIsStation(this.market))
        {
            return "Asteroid Processing";
        }
        else
        {
            return "Ismara's Sling";
        }
    }

    @Override
    public String getCurrentImage()
    {
        if(boggledTools.marketIsStation(this.market))
        {
            return Global.getSettings().getSpriteName("boggled", "asteroid_processing");
        }
        else
        {
            return this.getSpec().getImageName();
        }
    }

    @Override
    protected String getDescriptionOverride()
    {
        if(boggledTools.marketIsStation(this.market))
        {
            return "Crashing asteroids rich in water-ice into planets is an effective means of terraforming - except when the asteroid is so large that the impact would be cataclysmic. In this case, the asteroid can be towed to a space station, where the water-ice is safely extracted and shipped to the destination planet. Can only help terraform worlds in the same system.";
        }
        return null;
    }

    @Override
    public void apply() {
        super.apply(true);
        thisIndustry.apply(this, this);

        super.apply(false);
        super.applyIncomeAndUpkeep(3);
    }

    @Override
    public void unapply() {
        super.unapply();
    }

    @Override
    public float getPatherInterest() { return 10.0F; }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        thisIndustry.addPostDemandSection(this, tooltip, hasDemand, mode);
    }

    @Override
    public boolean canInstallAICores() {
        return false;
    }

    @Override
    public boolean canImprove() { return false; }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color bad = Misc.getNegativeHighlightColor();

        if(slingHasShortage())
        {
            tooltip.addPara(this.getCurrentName() + " is experiencing a shortage of heavy machinery. No water-ice can be supplied for terraforming projects until the shortage is resolved.", bad, opad);
        }
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
