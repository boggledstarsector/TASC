package data.campaign.econ.industries;

import java.util.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import org.json.JSONException;
import org.json.JSONObject;

public class Boggled_Magnetoshield extends BaseIndustry
{
    private static BoggledCommonIndustry commonindustry;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        commonindustry = new BoggledCommonIndustry(data, "Magnetoshield");
    }

    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    public static List<String> SUPPRESSED_CONDITIONS = new ArrayList<>();
    static
    {
        SUPPRESSED_CONDITIONS.add(Conditions.IRRADIATED);
    }

    @Override
    public void apply()
    {
        super.apply(true);

        if(isFunctional())
        {
            for (String cid : SUPPRESSED_CONDITIONS)
            {
                market.suppressCondition(cid);
            }
        }
    }

    @Override
    public void unapply()
    {
        for (String cid : SUPPRESSED_CONDITIONS)
        {
            market.unsuppressCondition(cid);
        }

        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild() { return commonindustry.isAvailableToBuild(getMarket()); }

    @Override
    public boolean showWhenUnavailable() { return commonindustry.showWhenUnavailable(getMarket()); }

    @Override
    public String getUnavailableReason() { return commonindustry.getUnavailableReason(getMarket()); }

    @Override
    public boolean canImprove()
    {
        return false;
    }

    @Override
    public boolean canInstallAICores() {
        return false;
    }
}
