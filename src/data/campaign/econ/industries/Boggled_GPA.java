package data.campaign.econ.industries;

import java.lang.String;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

public class Boggled_GPA extends BaseIndustry
{
    private static BoggledCommonIndustry commonindustry;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        commonindustry = new BoggledCommonIndustry(data);
    }

    @Override
    public boolean canBeDisrupted()
    {
        return true;
    }

    @Override
    public void apply()
    {
        super.apply(true);

        this.demand(boggledTools.BoggledCommodities.domainArtifacts, 3);
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        return false;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return false;
    }

    @Override
    public String getUnavailableReason()
    {
        return "Error in getUnavailableReason() in Boggled GPA. Please tell Boggled about this on the forums.";
    }

    @Override
    public float getPatherInterest()
    {
        if(!this.market.isPlayerOwned())
        {
            return 0;
        }
        else
        {
            return super.getPatherInterest() + 2.0f;
        }
    }

    @Override
    public boolean canImprove() { return false; }

    @Override
    public boolean canInstallAICores() {
        return false;
    }
}

