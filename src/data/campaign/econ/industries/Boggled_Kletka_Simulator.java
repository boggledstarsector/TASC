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

public class Boggled_Kletka_Simulator extends BaseIndustry implements BoggledIndustryInterface{
    private final BoggledCommonIndustry thisIndustry;

    public Boggled_Kletka_Simulator() {
        super();
        thisIndustry = boggledTools.getIndustryProject("BOGGLED_KLETKA_SIMULATOR");
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
        thisIndustry.buildingFinished(this, this);
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

    @Override
    public void advance(float amount) {
        super.advance(amount);
        thisIndustry.advance(amount, this);
    }

    @Override
    public void apply() {
        super.apply(false);
        super.applyIncomeAndUpkeep(3);
        thisIndustry.apply(this, this);
    }

     @Override
    public void unapply() {
        super.unapply();
        thisIndustry.unapply(this, this);
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
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
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return true;
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        thisIndustry.addPostDemandSection(this, tooltip, hasDemand, mode);
    }

    @Override
    public void applyDeficitToProduction(String modId, Pair<String, Integer> deficit, String... commodities) {
        thisIndustry.applyDeficitToProduction(this, modId, deficit, commodities);
    }

    @Override
    public void setFunctional(boolean functional) {
        thisIndustry.setFunctional(functional);
    }

    @Override
    public void modifyPatherInterest(String id, float patherInterest) {

    }

    @Override
    public void unmodifyPatherInterest(String id) {

    }

    @Override
    public float getBasePatherInterest() {
        return 0;
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

    private void addAICoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode, String coreType, String improves) {
        float opad = 10.0F;
        String pre = coreType + "-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = coreType + "-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + improves + " AI core training methodology.", opad);
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + improves + " AI core training methodology.", opad);
        }
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        addAICoreDescription(tooltip, mode, "Alpha", "Massively improves");
    }

    @Override
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        addAICoreDescription(tooltip, mode, "Beta", "Greatly improves");
    }

    @Override
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        addAICoreDescription(tooltip, mode, "Gamma", "Improves");
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
    public float getPatherInterest() { return 10.0F; }


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
