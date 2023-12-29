package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import java.awt.*;

public class Boggled_Mesozoic_Park extends BaseIndustry {
    private final BoggledCommonIndustry thisIndustry;

    public Boggled_Mesozoic_Park() {
        super();
        thisIndustry = boggledTools.getIndustryProject("mesozoic_park");
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
        boggledTools.addCondition(this.market, "inimical_biosphere");
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

//    @Override
//    public void advance(float amount) {
//        super.advance(amount);
//        thisIndustry.advance(amount, this);
//    }

    //Need to update string in addImproveDesc if value changed
    private final float IMPROVE_BONUS = 1.20f;

    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        // This check exists to remove Mesozoic Park if the planet was terraformed to a type that is incompatible with Mesozoic Park
        String planetType = boggledTools.getPlanetType(this.market.getPlanetEntity()).getPlanetId();
        if(!(planetType.equals(boggledTools.terranPlanetId) || planetType.equals(boggledTools.waterPlanetId) || planetType.equals(boggledTools.junglePlanetId) || planetType.equals(boggledTools.desertPlanetId)))
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

            if (this.market.hasIndustry(boggledTools.BoggledIndustries.mesozoicParkIndustryId))
            {
                // Pass in null for mode when calling this from API code.
                this.market.removeIndustry(boggledTools.BoggledIndustries.mesozoicParkIndustryId, (MarketAPI.MarketInteractionMode)null, false);
            }

            if (this.market.isPlayerOwned())
            {
                MessageIntel intel = new MessageIntel("Mesozoic Park on " + this.market.getName(), Misc.getBasePlayerColor());
                intel.addLine("    - Closed");
                intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
            }
        }
    }

    @Override
    public void apply()
    {
        super.apply(true);
    }

    @Override
    public String getCurrentImage()
    {
        MarketAPI market = this.market;

        //Can't build on stations
        if(boggledTools.marketIsStation(market))
        {
            return this.getSpec().getImageName();
        }

        String planetType = boggledTools.getPlanetType(market.getPlanetEntity()).getPlanetId();

        //Can only build on terran, water, jungle or desert planets
        switch (planetType) {
            case boggledTools.terranPlanetId:
                return Global.getSettings().getSpriteName("boggled", "mesozoic_park_terran");
            case boggledTools.waterPlanetId:
                return Global.getSettings().getSpriteName("boggled", "mesozoic_park_water");
            case boggledTools.junglePlanetId:
                return Global.getSettings().getSpriteName("boggled", "mesozoic_park_jungle");
            case boggledTools.desertPlanetId:
                return Global.getSettings().getSpriteName("boggled", "mesozoic_park_desert");
        }

        return this.getSpec().getImageName();
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }


    @Override
    public void applyAICoreToIncomeAndUpkeep()
    {
        // Also handles the accessibility modifier
        // This will apply even if no AI core is installed

        float access_mult = (float)Math.round(market.getAccessibilityMod().computeEffective(0.0F) * 100.0F) / 100.0F;
        String access = "Accessibility";
        this.getIncome().modifyMult("ind_mesopark_access", access_mult, access);

        if(this.aiCoreId != null)
        {
            float alpha_mult = 1.20f;
            float beta_mult = 1.10f;
            float gamma_mult = 1.05f;

            String name = "AI Core assigned";
            switch (this.aiCoreId) {
                case Commodities.ALPHA_CORE:
                    name = "Alpha Core assigned";
                    this.getIncome().modifyMult("ind_core", alpha_mult, name);
                    break;
                case Commodities.BETA_CORE:
                    name = "Beta Core assigned";
                    this.getIncome().modifyMult("ind_core", beta_mult, name);
                    break;
                case Commodities.GAMMA_CORE:
                    name = "Gamma Core assigned";
                    this.getIncome().modifyMult("ind_core", gamma_mult, name);
                    break;
            }
        }
        else
        {
            this.getUpkeep().unmodifyMult("ind_core");
        }
    }

    private void addAICoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode, String coreType, String highlights) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = coreType + "-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = coreType + "-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases income by %s.", 0.0F, highlight, "20%");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases income by %s.", opad, highlight, "20%");
        }
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        addAICoreDescription(tooltip, mode, "Alpha", "20%");
    }

    @Override
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        addAICoreDescription(tooltip, mode, "Beta", "10%");
    }

    @Override
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        addAICoreDescription(tooltip, mode, "Gamma", "5%");
    }

    @Override
    public boolean canImprove() {
        return true;
    }

    @Override
    protected void applyImproveModifiers()
    {
        if (this.isImproved())
        {
            this.getIncome().modifyMult("ind_improved", IMPROVE_BONUS, "Improvements");

            if (!this.isFunctional())
            {
                this.unapply();
            }
        }
        else
        {
            this.getUpkeep().unmodifyMult("ind_improved");
        }
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String bonus = "20%";
        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            info.addPara("Income increased by %s.", 0.0F, highlight, bonus);
        }
        else
        {
            info.addPara("Increases income by %s.", 0.0F, highlight, bonus);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }

    @Override
    public float getPatherInterest()
    {
        return super.getPatherInterest() + 2f;
    }
}

