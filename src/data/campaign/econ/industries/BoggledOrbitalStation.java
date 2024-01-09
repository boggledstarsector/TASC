package data.campaign.econ.industries;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;

public class BoggledOrbitalStation extends OrbitalStation implements BoggledIndustryInterface {
    private final BoggledCommonIndustry thisIndustry;

    public BoggledOrbitalStation(String industryId) {
        super();
        thisIndustry = boggledTools.getIndustryProject(industryId);
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
        thisIndustry.advance(amount, this, this);
    }

    @Override
    public void apply() {
        super.apply(true);
        thisIndustry.apply(this, this);
    }

    @Override
    public void unapply() {
        super.unapply();
        thisIndustry.unapply(this, this);
    }

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, Industry.IndustryTooltipMode mode) {
        return thisIndustry.hasPostDemandSection(this, hasDemand, mode);
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
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
        return thisIndustry.canBeDisrupted(this);
    }

    @Override
    public float getPatherInterest() { return 10.0F; }

    @Override
    public boolean canInstallAICores() {
        return thisIndustry.canInstallAICores();
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode) {
        thisIndustry.addAICoreDescription(this, tooltip, mode, "Alpha", "alpha_core");
    }

    @Override
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode) {
        thisIndustry.addAICoreDescription(this, tooltip, mode, "Beta", "beta_core");
    }

    @Override
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode) {
        thisIndustry.addAICoreDescription(this, tooltip, mode, "Gamma", "gamma_core");
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
        thisIndustry.addRightAfterDescriptionSection(this, tooltip, mode);
    }

    @Override
    public boolean canImprove() {
        return thisIndustry.canImprove(this);
    }

    @Override
    protected void applyImproveModifiers() {
        thisIndustry.applyImproveModifiers(this, this);
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI tooltip, Industry.ImprovementDescriptionMode mode) {
        thisIndustry.addImproveDesc(this, tooltip, mode);
        super.addImproveDesc(tooltip, mode);
    }
}
