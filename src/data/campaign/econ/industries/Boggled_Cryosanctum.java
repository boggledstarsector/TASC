package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.Cryosanctum;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;

public class Boggled_Cryosanctum extends Cryosanctum implements BoggledIndustryInterface {
    private BoggledCommonIndustry thisIndustry;

    public Boggled_Cryosanctum() {
        super();
        thisIndustry = new BoggledCommonIndustry();
    }

    @Override
    public void init(String id, MarketAPI market) {
        super.init(id, market);
        thisIndustry = boggledTools.getIndustryProject(id);
    }

    @Override
    public final void startBuilding() {
        super.startBuilding();
        thisIndustry.startBuilding(this);
    }

    @Override
    public final void startUpgrading() {
        super.startUpgrading();
        thisIndustry.startUpgrading(this);
    }

    @Override
    protected final void buildingFinished() {
        super.buildingFinished();
        thisIndustry.buildingFinished(this, this);
    }

    @Override
    protected final void upgradeFinished(Industry previous) {
        super.upgradeFinished(previous);
        thisIndustry.upgradeFinished(this, previous);
    }

    @Override
    public final void finishBuildingOrUpgrading() {
        super.finishBuildingOrUpgrading();
        thisIndustry.finishBuildingOrUpgrading(this);
    }

    @Override
    public final boolean isBuilding() { return thisIndustry.isBuilding(this); }

    @Override
    public final boolean isFunctional() { return super.isFunctional() && thisIndustry.isFunctional(); }

    @Override
    public final boolean isUpgrading() { return thisIndustry.isUpgrading(this); }

    @Override
    public final void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade) {
        super.notifyBeingRemoved(mode, forUpgrade);
        thisIndustry.notifyBeingRemoved(this, this, mode, forUpgrade);
    }

    @Override
    public final float getBuildOrUpgradeProgress() { return thisIndustry.getBuildOrUpgradeProgress(this); }

    @Override
    public final String getBuildOrUpgradeDaysText() {
        return thisIndustry.getBuildOrUpgradeDaysText(this);
    }

    @Override
    public final String getBuildOrUpgradeProgressText() {
        return thisIndustry.getBuildOrUpgradeProgressText(this);
    }

    @Override
    public final boolean isAvailableToBuild() { return thisIndustry.isAvailableToBuild(this); }

    @Override
    public final boolean showWhenUnavailable() { return thisIndustry.showWhenUnavailable(this); }

    @Override
    public final String getUnavailableReason() { return thisIndustry.getUnavailableReason(this); }

    @Override
    public final void advance(float amount) {
        super.advance(amount);
        thisIndustry.advance(amount, this, this);
    }

    @Override
    public final void apply() {
        super.apply(true);
        thisIndustry.apply(this, this);
    }

    @Override
    public final void unapply() {
        super.unapply();
        thisIndustry.unapply(this, this);
    }

    @Override
    protected final boolean hasPostDemandSection(boolean hasDemand, Industry.IndustryTooltipMode mode) {
        return thisIndustry.hasPostDemandSection(this, hasDemand, mode);
    }

    @Override
    protected final void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        thisIndustry.addPostDemandSection(this, tooltip, hasDemand, mode);
    }

    @Override
    public final void applyDeficitToProduction(String modId, Pair<String, Integer> deficit, String... commodities) {
        thisIndustry.applyDeficitToProduction(this, modId, deficit, commodities);
    }

    @Override
    public final void setFunctional(boolean functional) {
        thisIndustry.setFunctional(functional);
    }

    @Override
    public float getBasePatherInterest() {
        return super.getPatherInterest() + thisIndustry.getBasePatherInterest();
    }

    @Override
    public void modifyPatherInterest(MutableStat modifier) {
        thisIndustry.modifyPatherInterest(modifier);
    }

    @Override
    public void unmodifyPatherInterest(String source) {
        thisIndustry.unmodifyPatherInterest(source);
    }

    @Override
    public void modifyImmigration(MutableStat modifier) {
        thisIndustry.modifyImmigration(modifier);
    }

    @Override
    public void unmodifyImmigration(String source) {
        thisIndustry.unmodifyImmigration(source);
    }

    @Override
    public final boolean canBeDisrupted() {
        return thisIndustry.canBeDisrupted(this);
    }

    @Override
    public final float getPatherInterest() {
        return super.getPatherInterest() + thisIndustry.getPatherInterest(this);
    }

    @Override
    public final boolean canInstallAICores() {
        return thisIndustry.canInstallAICores();
    }

    @Override
    public final void addAlphaCoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode) {
        thisIndustry.addAICoreDescription(this, tooltip, mode, "Alpha", "alpha_core");
    }

    @Override
    public final void addBetaCoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode) {
        thisIndustry.addAICoreDescription(this, tooltip, mode, "Beta", "beta_core");
    }

    @Override
    public final void addGammaCoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode) {
        thisIndustry.addAICoreDescription(this, tooltip, mode, "Gamma", "gamma_core");
    }

    @Override
    public final void applyAICoreToIncomeAndUpkeep()
    {
        // This being blank prevents installed AI cores from altering monthly upkeep
        // AI cores affect income and upkeep from the regular apply() function
    }

    @Override
    public final void updateAICoreToSupplyAndDemandModifiers()
    {
        // This being blank prevents AI cores from reducing the demand
        // AI cores affect supply and demand from the regular apply() function
    }

    @Override
    protected final void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode) {
        thisIndustry.addRightAfterDescriptionSection(this, tooltip, mode);
    }

    @Override
    public final boolean canImprove() {
        return thisIndustry.canImprove(this);
    }

    @Override
    protected final void applyImproveModifiers() {
        thisIndustry.applyImproveModifiers(this, this);
    }

    @Override
    public final void addImproveDesc(TooltipMakerAPI tooltip, Industry.ImprovementDescriptionMode mode) {
        thisIndustry.addImproveDesc(this, tooltip, mode);
        super.addImproveDesc(tooltip, mode);
    }

    @Override
    public void modifyBuildCost(MutableStat modifier) {
        thisIndustry.modifyBuildCost(modifier);
    }

    @Override
    public void unmodifyBuildCost(String source) {
        thisIndustry.unmodifyBuildCost(source);
    }

    @Override
    public void addProductionData(BoggledCommonIndustry.ProductionData data) {
        thisIndustry.addProductionData(data);
    }

    @Override
    public void removeProductionData(BoggledCommonIndustry.ProductionData data) {
        thisIndustry.removeProductionData(data);
    }

    @Override
    public void modifyProductionChance(String commodityId, String source, int value) {
        thisIndustry.modifyProductionChance(commodityId, source, value);
    }

    @Override
    public void unmodifyProductionChance(String commodityId, String source) {
        thisIndustry.unmodifyProductionChance(commodityId, source);
    }
}

