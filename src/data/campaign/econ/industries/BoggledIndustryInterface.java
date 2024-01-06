package data.campaign.econ.industries;

import com.fs.starfarer.api.util.Pair;

public interface BoggledIndustryInterface {
    void applyDeficitToProduction(String modId, Pair<String, Integer> deficit, String... commodities);

    void setFunctional(boolean functional);
}
