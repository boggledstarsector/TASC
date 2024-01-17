package boggled.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import boggled.campaign.econ.boggledTools;

public class BoggledOrbitalStation extends OrbitalStation implements BoggledIndustryInterface {
    private BoggledCommonIndustry thisIndustry;

    public BoggledOrbitalStation(String industryId) {
        super();
        thisIndustry = boggledTools.getIndustryProject(industryId);
    }

    @Override
    public void init(String id, MarketAPI market) {
        super.init(id, market);
        thisIndustry = new BoggledCommonIndustry(boggledTools.getIndustryProject(id), this);
    }

    @Override
    public void startBuilding() {
        super.startBuilding();
        thisIndustry.startBuilding();
    }

    @Override
    public void startUpgrading() {
        super.startUpgrading();
        thisIndustry.startUpgrading();
    }

    @Override
    protected void buildingFinished() {
        super.buildingFinished();
        thisIndustry.buildingFinished();
    }

    @Override
    protected void upgradeFinished(Industry previous) {
        super.upgradeFinished(previous);
        thisIndustry.upgradeFinished(previous);
    }

    @Override
    public void finishBuildingOrUpgrading() {
        super.finishBuildingOrUpgrading();
        thisIndustry.finishBuildingOrUpgrading();
    }

    @Override
    public boolean isBuilding() { return thisIndustry.isBuilding(); }

    @Override
    public boolean isFunctional() { return super.isFunctional() && thisIndustry.isFunctional(); }

    @Override
    public boolean isUpgrading() { return thisIndustry.isUpgrading(); }

    @Override
    public float getBuildOrUpgradeProgress() { return thisIndustry.getBuildOrUpgradeProgress(); }

    @Override
    public String getBuildOrUpgradeDaysText() {
        return thisIndustry.getBuildOrUpgradeDaysText();
    }

    @Override
    public String getBuildOrUpgradeProgressText() {
        return thisIndustry.getBuildOrUpgradeProgressText();
    }

    @Override
    public boolean isAvailableToBuild() { return thisIndustry.isAvailableToBuild(); }

    @Override
    public boolean showWhenUnavailable() { return thisIndustry.showWhenUnavailable(); }

    @Override
    public String getUnavailableReason() { return thisIndustry.getUnavailableReason().text; }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        thisIndustry.advance(amount);
    }

    @Override
    public void apply() {
        super.apply(true);
        thisIndustry.apply();
    }

    @Override
    public void unapply() {
        super.unapply();
        thisIndustry.unapply();
    }

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, Industry.IndustryTooltipMode mode) {
        return thisIndustry.hasPostDemandSection(hasDemand, mode);
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        thisIndustry.addPostDemandSection(tooltip, hasDemand, mode);
    }

    @Override
    public void applyDeficitToProduction(String modId, Pair<String, Integer> deficit, String... commodities) {
        thisIndustry.applyDeficitToProduction(modId, deficit, commodities);
    }

    @Override
    public void setFunctional(boolean functional) {
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
    public boolean canBeDisrupted() {
        return thisIndustry.canBeDisrupted();
    }

    @Override
    public float getPatherInterest() { return 10.0F; }

    @Override
    public boolean canInstallAICores() {
        return thisIndustry.canInstallAICores();
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode) {
        thisIndustry.addAICoreDescription(tooltip, mode, "Alpha", "alpha_core");
    }

    @Override
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode) {
        thisIndustry.addAICoreDescription(tooltip, mode, "Beta", "beta_core");
    }

    @Override
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode) {
        thisIndustry.addAICoreDescription(tooltip, mode, "Gamma", "gamma_core");
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
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, Industry.IndustryTooltipMode mode) {
        thisIndustry.addRightAfterDescriptionSection(tooltip, mode);
    }

    @Override
    public boolean canImprove() {
        return thisIndustry.canImprove();
    }

    @Override
    protected void applyImproveModifiers() {
        thisIndustry.applyImproveModifiers();
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI tooltip, Industry.ImprovementDescriptionMode mode) {
        thisIndustry.addImproveDesc(tooltip, mode);
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
