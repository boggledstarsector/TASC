package data.campaign.econ.industries;

import java.awt.Color;
import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

public class Boggled_Kletka_Simulator extends BaseIndustry {
    private final BoggledCommonIndustry thisIndustry;

    public Boggled_Kletka_Simulator() {
        super();
        thisIndustry = boggledTools.getIndustryProject("kletka_simulator");
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
    public boolean canBeDisrupted()
    {
        return true;
    }

    @Override
    public CargoAPI generateCargoForGatheringPoint(Random random)
    {
        boolean shortage = false;
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit(boggledTools.BoggledCommodities.domainArtifacts);
            if(deficit.two != 0)
            {
                shortage = true;
            }
        }

        if (!this.isFunctional() || shortage)
        {
            return null;
        }
        else
        {
            CargoAPI result = Global.getFactory().createCargo(true);
            result.clear();
            float roll = random.nextFloat() * 100f;

            if(this.isImproved())
            {
                if(this.aiCoreId == null)
                {
                    if(roll > 70f)
                    {
                        result.addCommodity(Commodities.BETA_CORE, 1f);
                    }
                    else if(roll > 45f)
                    {
                        result.addCommodity(Commodities.GAMMA_CORE, 1f);
                    }
                }
                else if(this.aiCoreId.equals(Commodities.ALPHA_CORE))
                {
                    if(roll > 65f)
                    {
                        result.addCommodity(Commodities.ALPHA_CORE, 1f);
                    }
                    else if(roll > 40f)
                    {
                        result.addCommodity(Commodities.BETA_CORE, 1f);
                    }
                    else if(roll > 15f)
                    {
                        result.addCommodity(Commodities.GAMMA_CORE, 1f);
                    }
                }
                else if(this.aiCoreId.equals(Commodities.BETA_CORE))
                {
                    if(roll > 75f)
                    {
                        result.addCommodity(Commodities.ALPHA_CORE, 1f);
                    }
                    else if(roll > 50f)
                    {
                        result.addCommodity(Commodities.BETA_CORE, 1f);
                    }
                    else if(roll > 25f)
                    {
                        result.addCommodity(Commodities.GAMMA_CORE, 1f);
                    }
                }
                else if(this.aiCoreId.equals(Commodities.GAMMA_CORE))
                {
                    if(roll > 85f)
                    {
                        result.addCommodity(Commodities.ALPHA_CORE, 1f);
                    }
                    else if(roll > 60f)
                    {
                        result.addCommodity(Commodities.BETA_CORE, 1f);
                    }
                    else if(roll > 35f)
                    {
                        result.addCommodity(Commodities.GAMMA_CORE, 1f);
                    }
                }
            }
            else
            {
                if(this.aiCoreId == null)
                {
                    if(roll > 80f)
                    {
                        result.addCommodity(Commodities.BETA_CORE, 1f);
                    }
                    else if(roll > 55f)
                    {
                        result.addCommodity(Commodities.GAMMA_CORE, 1f);
                    }
                }
                else if(this.aiCoreId.equals(Commodities.ALPHA_CORE))
                {
                    if(roll > 75f)
                    {
                        result.addCommodity(Commodities.ALPHA_CORE, 1f);
                    }
                    else if(roll > 50f)
                    {
                        result.addCommodity(Commodities.BETA_CORE, 1f);
                    }
                    else if(roll > 25f)
                    {
                        result.addCommodity(Commodities.GAMMA_CORE, 1f);
                    }
                }
                else if(this.aiCoreId.equals(Commodities.BETA_CORE))
                {
                    if(roll > 85f)
                    {
                        result.addCommodity(Commodities.ALPHA_CORE, 1f);
                    }
                    else if(roll > 60f)
                    {
                        result.addCommodity(Commodities.BETA_CORE, 1f);
                    }
                    else if(roll > 35f)
                    {
                        result.addCommodity(Commodities.GAMMA_CORE, 1f);
                    }
                }
                else if(this.aiCoreId.equals(Commodities.GAMMA_CORE))
                {
                    if(roll > 95f)
                    {
                        result.addCommodity(Commodities.ALPHA_CORE, 1f);
                    }
                    else if(roll > 70f)
                    {
                        result.addCommodity(Commodities.BETA_CORE, 1f);
                    }
                    else if(roll > 45f)
                    {
                        result.addCommodity(Commodities.GAMMA_CORE, 1f);
                    }
                }
            }

            return result;
        }
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Massively improves AI core training methodology.", 0.0F, highlight, "");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Massively improves AI core training methodology.", opad, highlight, "");
        }
    }

    @Override
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Beta-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Beta-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Greatly improves AI core training methodology.", opad, highlight, "");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Greatly improves AI core training methodology.", opad, highlight, "");
        }
    }

    @Override
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Gamma-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Gamma-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Improves AI core training methodology.", opad, highlight, "");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Improves AI core training methodology.", opad, highlight, "");
        }
    }

    @Override
    public void applyAICoreToIncomeAndUpkeep()
    {
        //Prevent AI cores from modifying income and upkeep
    }

    @Override
    public void updateAICoreToSupplyAndDemandModifiers()
    {
        //Prevent AI cores from modifying supply and demand
    }

    @Override
    public void apply() {
        super.apply(false);
        super.applyIncomeAndUpkeep(3);
        thisIndustry.apply(this);

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.kletkaSimulatorTemperatureBasedUpkeep))
        {
            MarketAPI market = this.market;
            if(boggledTools.marketIsStation(market))
            {
                getUpkeep().modifyMult("temperature", 8.0f, "Station");
            }
            else if(market.hasCondition("very_cold"))
            {
                getUpkeep().modifyMult("temperature", 0.25f, "Extreme cold");
            }
            else if(market.hasCondition("cold"))
            {
                getUpkeep().modifyMult("temperature", 0.5f, "Cold");
            }
            else if(market.hasCondition("hot"))
            {
                getUpkeep().modifyMult("temperature", 2.0f, "Hot");
            }
            else if(market.hasCondition("very_hot"))
            {
                getUpkeep().modifyMult("temperature", 4.0f, "Extreme heat");
            }
            else
            {
                getUpkeep().unmodifyMult("temperature");
            }
        }

        // This version will cause suppression of temperature conditions to remove the temperature-based
        // upkeep bonus.
        /*
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.kletkaSimulatorTemperatureBasedUpkeep))
        {
            MarketAPI market = this.market;
            LinkedHashSet<String> suppCond = market.getSuppressedConditions();
            if(market.hasCondition("very_cold"))
            {
                if(!suppCond.contains(Conditions.VERY_COLD))
                {
                    getUpkeep().modifyMult("temperature", 0.25f, "Extreme cold");
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 1.0f, "Extreme cold (suppressed)");
                }
            }
            else if(market.hasCondition("cold"))
            {
                if(!suppCond.contains(Conditions.COLD))
                {
                    getUpkeep().modifyMult("temperature", 0.5f, "Cold");
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 1.0f, "Cold (suppressed)");
                }
            }
            else if(market.hasCondition("hot"))
            {
                if(!suppCond.contains(Conditions.HOT))
                {
                    getUpkeep().modifyMult("temperature", 2.0f, "Hot");
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 1.0f, "Hot (suppressed)");
                }
            }
            else if(market.hasCondition("very_hot"))
            {
                if(!suppCond.contains(Conditions.VERY_HOT))
                {
                    getUpkeep().modifyMult("temperature", 4.0f, "Extreme heat");
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 1.0f, "Extreme heat (suppressed)");
                }
            }
            else if(market.getPrimaryEntity().hasTag("station"))
            {
                getUpkeep().modifyMult("temperature", 8.0f, "Station");
            }
            else
            {
                getUpkeep().unmodifyMult("temperature");
            }
        }
         */
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public float getPatherInterest() { return 10.0F; }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.kletkaSimulatorTemperatureBasedUpkeep))
        {
            tooltip.addPara("Supercomputers will melt themselves without adequate cooling. Operating costs are lowest on very cold worlds and highest on stations.", opad);
        }

        boolean shortage = false;
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit(boggledTools.BoggledCommodities.domainArtifacts);
            if(deficit.two != 0)
            {
                shortage = true;
            }
        }

        if(mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.QUEUED || isBuilding())
        {
            return;
        }
        else if(isDisrupted())
        {
            tooltip.addPara("Current chances to produce an AI core at the end of the month: %s", opad, bad, "           None (disrupted)");
            return;
        }
        else if(shortage)
        {
            tooltip.addPara("Current chances to produce an AI core at the end of the month: %s", opad, bad, "           None (shortage of Domain-era artifacts)");
            return;
        }

        if(isImproved())
        {
            if((this.aiCoreId == null))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "30%","25%","45%");

                tooltip.addPara("Install an AI core to improve production chances.", opad);
            }
            else if(this.aiCoreId.equals(Commodities.GAMMA_CORE))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "15%","25%","25%","35%");
            }
            else if(this.aiCoreId.equals(Commodities.BETA_CORE))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "25%","25%","25%","25%");
            }
            else if(this.aiCoreId.equals(Commodities.ALPHA_CORE))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "35%","25%","25%","15%");
            }
        }
        else
        {
            if((this.aiCoreId == null))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "20%","25%","55%");

                tooltip.addPara("Install an AI core to improve production chances.", opad);
            }
            else if(this.aiCoreId.equals(Commodities.GAMMA_CORE))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "5%","25%","25%","45%");
            }
            else if(this.aiCoreId.equals(Commodities.BETA_CORE))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "15%","25%","25%","35%");
            }
            else if(this.aiCoreId.equals(Commodities.ALPHA_CORE))
            {
                tooltip.addPara("Current chances to produce an AI core at the end of the month:\n" + "Alpha Core: %s\n" + "Beta Core: %s\n" + "Gamma Core: %s\n"  + "Nothing: %s", opad, highlight, "25%","25%","25%","25%");
            }
        }
    }

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode)
    {
        return boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled);
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            float opad = 10.0F;
            Color highlight = Misc.getHighlightColor();

            tooltip.addPara("Kletka Simulators always demand %s Domain-era artifacts regardless of market size.", opad, highlight, "4");
        }
    }

    @Override
    public boolean canImprove() {
        return true;
    }

    @Override
    protected void applyImproveModifiers()
    {
        //Handled above in the cargo function
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
        float opad = 10f;
        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            info.addPara("AI core training methodology improved.", 0f);
        }
        else
        {
            info.addPara("Improves AI core training methodology.", 0f);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }
}
