package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.String;

public class Boggled_Hydroponics extends BaseIndustry
{
    private static BoggledCommonIndustry sharedIndustry;
    private final BoggledCommonIndustry thisIndustry;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        sharedIndustry = new BoggledCommonIndustry(data);
    }

    public Boggled_Hydroponics() {
        super();
        thisIndustry = new BoggledCommonIndustry(sharedIndustry);
    }

    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void apply()
    {
        super.apply(true);

        int size = this.market.getSize();
        this.demand(Commodities.HEAVY_MACHINERY, size - 2);
        this.supply(Commodities.FOOD, size);

        Pair<String, Integer> deficit = this.getMaxDeficit(Commodities.HEAVY_MACHINERY);
        this.applyDeficitToProduction(1, deficit, Commodities.FOOD);

        if (!this.isFunctional())
        {
            this.supply.clear();
            this.unapply();
        }
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild() { return thisIndustry.isAvailableToBuild(getMarket()); }

    @Override
    public boolean showWhenUnavailable() { return thisIndustry.showWhenUnavailable(getMarket()); }

    @Override
    public String getUnavailableReason() { return thisIndustry.getUnavailableReason(getMarket()); }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return true;
    }
}

