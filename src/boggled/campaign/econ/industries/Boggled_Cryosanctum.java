package boggled.campaign.econ.industries;

import boggled.scripts.BoggledTerraformingProject;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.Cryosanctum;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import boggled.campaign.econ.boggledTools;

import java.util.List;

public class Boggled_Cryosanctum extends Cryosanctum implements BoggledIndustryInterface {
    private BoggledCommonIndustry thisIndustry;

    public Boggled_Cryosanctum() {
        super();
        thisIndustry = new BoggledCommonIndustry();
    }

    @Override
    public void init(String id, MarketAPI market) {
        super.init(id, market);
        thisIndustry = new BoggledCommonIndustry(boggledTools.getIndustryProject(id), this);
    }

    @Override
    public final void startBuilding() {
        super.startBuilding();
        thisIndustry.startBuilding();
    }

    @Override
    public final void startUpgrading() {
        super.startUpgrading();
        thisIndustry.startUpgrading();
    }

    @Override
    protected final void buildingFinished() {
        super.buildingFinished();
        thisIndustry.buildingFinished();
    }

    @Override
    protected final void upgradeFinished(Industry previous) {
        super.upgradeFinished(previous);
        thisIndustry.upgradeFinished(previous);
    }

    @Override
    public final void finishBuildingOrUpgrading() {
        super.finishBuildingOrUpgrading();
        thisIndustry.finishBuildingOrUpgrading();
    }

    @Override
    public final boolean isBuilding() { return super.isBuilding() || thisIndustry.isBuilding(); }

    @Override
    public final boolean isFunctional() { return super.isFunctional() && thisIndustry.isFunctional(); }

    @Override
    public final boolean isUpgrading() { return super.isUpgrading() || thisIndustry.isUpgrading(); }

    @Override
    public final void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade) {
        super.notifyBeingRemoved(mode, forUpgrade);
        thisIndustry.notifyBeingRemoved(mode, forUpgrade);
    }

    @Override
    public final float getBuildOrUpgradeProgress() { return thisIndustry.getBuildOrUpgradeProgress(); }

    @Override
    public final String getBuildOrUpgradeDaysText() {
        return thisIndustry.getBuildOrUpgradeDaysText();
    }

    @Override
    public final String getBuildOrUpgradeProgressText() {
        return thisIndustry.getBuildOrUpgradeProgressText();
    }

    @Override
    public final boolean isAvailableToBuild() { return thisIndustry.isAvailableToBuild(); }

    @Override
    public final boolean showWhenUnavailable() { return thisIndustry.showWhenUnavailable(); }

    @Override
    public final String getUnavailableReason() { return thisIndustry.getUnavailableReason().text; }

    @Override
    public final void advance(float amount) {
        super.advance(amount);
        thisIndustry.advance(amount);
    }

    @Override
    public final void apply() {
        thisIndustry.apply();
        super.apply(true);
    }

    @Override
    public final void unapply() {
        thisIndustry.unapply();
        super.unapply();
    }

    @Override
    protected final boolean hasPostDemandSection(boolean hasDemand, Industry.IndustryTooltipMode mode) {
        return thisIndustry.hasPostDemandSection(hasDemand, mode);
    }

    @Override
    protected final void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        thisIndustry.addPostDemandSection(tooltip, hasDemand, mode);
    }

    @Override
    public final void applyDeficitToProduction(String modId, Pair<String, Integer> deficit, String... commodities) {
        thisIndustry.applyDeficitToProduction(modId, deficit, commodities);
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
        return thisIndustry.canBeDisrupted();
    }

    @Override
    public final float getPatherInterest() {
        return super.getPatherInterest() + thisIndustry.getPatherInterest();
    }

    @Override
    public final boolean canInstallAICores() {
        return thisIndustry.canInstallAICores();
    }

    @Override
    public final void addAlphaCoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode) {
        thisIndustry.addAICoreDescription(tooltip, mode, "Alpha", "alpha_core");
    }

    @Override
    public final void addBetaCoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode) {
        thisIndustry.addAICoreDescription(tooltip, mode, "Beta", "beta_core");
    }

    @Override
    public final void addGammaCoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode) {
        thisIndustry.addAICoreDescription(tooltip, mode, "Gamma", "gamma_core");
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
        thisIndustry.addRightAfterDescriptionSection(tooltip, mode);
    }

    @Override
    public final boolean canImprove() {
        return thisIndustry.canImprove();
    }

    @Override
    protected final void applyImproveModifiers() {
        thisIndustry.applyImproveModifiers();
    }

    @Override
    public final void addImproveDesc(TooltipMakerAPI tooltip, Industry.ImprovementDescriptionMode mode) {
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
    public void setEnableMonthlyProduction(boolean enabled) {
        thisIndustry.setEnableMonthlyProduction(enabled);
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
    public List<BoggledCommonIndustry.ProductionData> getProductionData() {
        return thisIndustry.getProductionData();
    }

    @Override
    public void modifyProductionChance(String commodityId, String source, int value) {
        thisIndustry.modifyProductionChance(commodityId, source, value);
    }

    @Override
    public void unmodifyProductionChance(String commodityId, String source) {
        thisIndustry.unmodifyProductionChance(commodityId, source);
    }

    @Override
    public Pair<Integer, Integer> getProductionChance(String commodityId) {
        return thisIndustry.getProductionChance(commodityId);
    }

    @Override
    public int getLastProductionRoll() {
        return thisIndustry.getLastProductionRoll();
    }

    @Override
    public void attachProject(BoggledTerraformingProject.ProjectInstance projectInstance) {
        thisIndustry.attachProject(projectInstance);
    }

    @Override
    public void detachProject(BoggledTerraformingProject.ProjectInstance projectInstance) {
        thisIndustry.detachProject(projectInstance);
    }
}

