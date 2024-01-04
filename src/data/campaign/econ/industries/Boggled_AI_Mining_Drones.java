package data.campaign.econ.industries;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.*;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;

public class Boggled_AI_Mining_Drones extends BaseIndustry implements BoggledIndustryInterface {
    private final BoggledCommonIndustry thisIndustry;

    public Boggled_AI_Mining_Drones() {
        super();
        thisIndustry = boggledTools.getIndustryProject("ai_mining_drones");
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

    @Override
    public void advance(float amount) {
        super.advance(amount);
        thisIndustry.advance(amount, this);
    }

    @Override
    public void apply() {
        thisIndustry.apply(this, this);

        if(this.market.getPrimaryEntity() != null && this.market.getPrimaryEntity().hasTag(Tags.STATION) && this.isFunctional()) {
            //Increased production
            Industry i = market.getIndustry(Industries.MINING);
            if (i != null) {
                for (MutableCommodityQuantity c : i.getAllSupply()) {
                    i.getSupply(c.getCommodityId()).getQuantity().modifyFlat(id, getProductionBonusFromMiningDrones(), "AI Mining Drones");
                }
            }
        }

        super.apply(true);
        thisIndustry.apply(this, this);
    }

    @Override
    public void unapply()
    {
        for(Industry i : market.getIndustries())
        {
            for(MutableCommodityQuantity c : i.getAllSupply())
            {
                i.getSupply(c.getCommodityId()).getQuantity().unmodifyFlat(id);
            }
        }

        this.market.getAccessibilityMod().unmodifyFlat(this.getModId(5));

        super.unapply();
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
    public void applyDeficitToProduction(int index, Pair<String, Integer> deficit, String... commodities) {
        super.applyDeficitToProduction(index, deficit, commodities);
    }

    @Override
    public void setFunctional(boolean functional) {
        thisIndustry.setFunctional(functional);
    }

    @Override
    public boolean canBeDisrupted() {
        return false;
    }

    public static float IMPROVE_BONUS = .20F;

    public int getProductionBonusFromMiningDrones()
    {
        int ai_bonus = 0;
        if (Commodities.ALPHA_CORE.equals(this.aiCoreId))
        {
            ai_bonus = 3;
        }
        else if (Commodities.BETA_CORE.equals(this.aiCoreId))
        {
            ai_bonus = 2;
        }
        else if (Commodities.GAMMA_CORE.equals(this.aiCoreId))
        {
            ai_bonus = 1;
        }

        Pair<String, Integer> deficit = this.getMaxDeficit(Commodities.FUEL, Commodities.SUPPLIES, Commodities.SHIPS);
        if(deficit.two > 0)
        {
            ai_bonus = ai_bonus - deficit.two;
        }

        // Make sure we can't return a negative bonus if a large supply deficit exists
        return Math.max(ai_bonus, 0);
    }

    @Override
    public float getPatherInterest() { return 10.0F; }

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
            text.addPara(pre + "Increases mining production by %s units.", 0.0F, highlight, highlights);
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases mining production by %s units.", opad, highlight, highlights);
        }
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        addAICoreDescription(tooltip, mode, "Alpha", "3");
    }

    @Override
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        addAICoreDescription(tooltip, mode, "Beta", "2");
    }

    @Override
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        addAICoreDescription(tooltip, mode, "Gamma", "1");
    }

    @Override
    public void applyAICoreToIncomeAndUpkeep()
    {
        //This being blank prevents installed AI cores from altering monthly upkeep
    }

    @Override
    public void updateAICoreToSupplyAndDemandModifiers()
    {
        //This being blank prevents AI cores from reducing the demand
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(this.market.getPrimaryEntity() != null && this.market.getPrimaryEntity().hasTag(Tags.STATION))
        {
            tooltip.addPara("Current production bonus: %s", opad, highlight, getProductionBonusFromMiningDrones() + "");
            Pair<String, Integer> deficit = this.getMaxDeficit(Commodities.FUEL, Commodities.SUPPLIES, Commodities.SHIPS);

            if(deficit.two > 0)
            {
                tooltip.addPara("The production bonus is being reduced by " + deficit.two + " due to a " + deficit.one + " shortage.", bad, opad);
            }
        }
        else
        {
            tooltip.addPara("AI Mining Drones are only useful on station-based markets.", opad);
        }
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
            market.getAccessibilityMod().modifyFlat(this.getModId(5), IMPROVE_BONUS, "AI Mining Drones");

            if (!this.isFunctional())
            {
                this.unapply();
            }
        }
        else
        {
            this.market.getAccessibilityMod().unmodifyFlat(this.getModId(5));
        }
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        float a = IMPROVE_BONUS;
        String aStr = Math.round(a * 100.0F) + "%";
        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            info.addPara("Colony accessibility increased by %s.", 0.0F, highlight, aStr);
        }
        else
        {
            info.addPara("Increases colony accessibility by %s.", 0.0F, highlight, aStr);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }
}
