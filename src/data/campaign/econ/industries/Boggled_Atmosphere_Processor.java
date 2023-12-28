package data.campaign.econ.industries;

import java.awt.*;
import java.lang.String;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

public class Boggled_Atmosphere_Processor extends BaseIndustry
{
    private static BoggledCommonIndustry sharedIndustry;
    private final BoggledCommonIndustry thisIndustry;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        sharedIndustry = new BoggledCommonIndustry(data);
    }

    public Boggled_Atmosphere_Processor() {
        super();
        thisIndustry = new BoggledCommonIndustry(sharedIndustry);
    }

    @Override
    public boolean canBeDisrupted()
    {
        return true;
    }

    @Override
    public boolean isAvailableToBuild() { return thisIndustry.isAvailableToBuild(getMarket()); }

    @Override
    public boolean showWhenUnavailable() { return thisIndustry.showWhenUnavailable(getMarket()); }

    @Override
    public String getUnavailableReason() { return thisIndustry.getUnavailableReason(getMarket()); }

    @Override
    public void apply()
    {
        super.apply(true);

        int size = this.market.getSize();
        this.demand(Commodities.HEAVY_MACHINERY, size);
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getPositiveHighlightColor();

        if(this.isDisrupted() && boggledTools.marketHasAtmoProblem(this.market) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Terraforming progress is stalled while the atmosphere processor is disrupted.", bad, opad);
        }
    }

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return true;
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        boolean shortage = false;
        Pair<String, Integer> deficit = this.getMaxDeficit(Commodities.HEAVY_MACHINERY);
        if(deficit.two != 0)
        {
            shortage = true;
        }

        if(shortage && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            float opad = 10.0F;
            Color bad = Misc.getNegativeHighlightColor();

            if(deficit.two != 0)
            {
                tooltip.addPara("The atmosphere processor is inactive due to a shortage of heavy machinery.", bad, opad);
            }
        }
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

