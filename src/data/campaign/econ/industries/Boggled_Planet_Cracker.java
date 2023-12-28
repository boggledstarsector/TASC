package data.campaign.econ.industries;

import java.awt.*;
import java.lang.String;

import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONException;
import org.json.JSONObject;

public class Boggled_Planet_Cracker extends BaseIndustry
{
    private static BoggledCommonIndustry sharedIndustry;
    private BoggledCommonIndustry thisIndustry;

    private BoggledCommonIndustry<Integer> testCI = new BoggledCommonIndustry<>();
    private BoggledCommonIndustry<Float> testCI2 = new BoggledCommonIndustry<>();

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        sharedIndustry = new BoggledCommonIndustry(data);
    }

    public Boggled_Planet_Cracker() {
        super();
        thisIndustry = new BoggledCommonIndustry(sharedIndustry);
    }

    @Override
    public void startBuilding() {
        super.startBuilding();
        thisIndustry.startBuilding(this);
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
    public boolean isAvailableToBuild() { return thisIndustry.isAvailableToBuild(getMarket()); }

    @Override
    public boolean showWhenUnavailable() { return thisIndustry.showWhenUnavailable(getMarket()); }

    @Override
    public String getUnavailableReason() { return thisIndustry.getUnavailableReason(getMarket()); }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        thisIndustry.advance(amount, this);
    }

    @Override
    public void apply()
    {
        super.apply(true);
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        super.notifyBeingRemoved(mode, forUpgrade);
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();

        thisIndustry.tooltipIncomplete(this, tooltip, mode, "Planet cracking is approximately %s complete on " + thisIndustry.getFocusMarketOrMarket(getMarket()).getName() + ".", opad, highlight, thisIndustry.getPercentComplete(0, this) + "%");

        thisIndustry.tooltipComplete(this, tooltip, mode, "Further planet cracking operations would serve no purpose on " + thisIndustry.getFocusMarketOrMarket(getMarket()).getName() + ". The Planet Cracker can now be deconstructed without any risk of regression.", opad, highlight);

        thisIndustry.tooltipDisrupted(this, tooltip, mode, "Progress is stalled while the planet cracker is disrupted.", opad, Misc.getNegativeHighlightColor());
    }

    @Override
    public float getPatherInterest() { return super.getPatherInterest() + 2.0f; }

    @Override
    public boolean canImprove() { return false; }

    @Override
    public boolean canInstallAICores() {
        return false;
    }
}

