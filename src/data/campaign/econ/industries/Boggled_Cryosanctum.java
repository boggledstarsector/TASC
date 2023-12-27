package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.Cryosanctum;
import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

public class Boggled_Cryosanctum extends Cryosanctum
{
    private static BoggledCommonIndustry commonindustry;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        commonindustry = new BoggledCommonIndustry(data, "Cryosanctum");
    }

    @Override
    public void apply()
    {
        super.apply();

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            int size = this.market.getSize();
            this.demand(boggledTools.BoggledCommodities.domainArtifacts, size);
        }
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild() { return commonindustry.isAvailableToBuild(getMarket()); }

    @Override
    public boolean showWhenUnavailable() { return commonindustry.showWhenUnavailable(getMarket()); }

    @Override
    public String getUnavailableReason() {
        return commonindustry.getUnavailableReason(getMarket());
    }
}

