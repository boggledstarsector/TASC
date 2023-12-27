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
    private static BoggledCommonIndustry commonIndustry;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        commonIndustry = new BoggledCommonIndustry(data, "Planet Cracker");
    }

    @Override
    public boolean canBeDisrupted()
    {
        return true;
    }

    @Override
    public boolean isAvailableToBuild() { return commonIndustry.isAvailableToBuild(getMarket()); }

    @Override
    public boolean showWhenUnavailable() { return commonIndustry.showWhenUnavailable(getMarket()); }

    @Override
    public String getUnavailableReason() { return commonIndustry.getUnavailableReason(getMarket()); }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        commonIndustry.advance(amount, this);
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

        commonIndustry.tooltipIncomplete(this, tooltip, mode, "Planet cracking is approximately %s complete on " + commonIndustry.getFocusMarketOrMarket(getMarket()).getName() + ".", opad, highlight, commonIndustry.getPercentComplete(0, this) + "%");

        commonIndustry.tooltipComplete(this, tooltip, mode, "Further planet cracking operations would serve no purpose on " + commonIndustry.getFocusMarketOrMarket(getMarket()).getName() + ". The Planet Cracker can now be deconstructed without any risk of regression.", opad, highlight);

        commonIndustry.tooltipDisrupted(this, tooltip, mode, "Progress is stalled while the planet cracker is disrupted.", opad, Misc.getNegativeHighlightColor());
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

