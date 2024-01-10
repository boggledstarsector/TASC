package data.campaign.econ.industries;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.util.Pair;

public interface BoggledIndustryInterface {
    void applyDeficitToProduction(String modId, Pair<String, Integer> deficit, String... commodities);

    void setFunctional(boolean functional);

    void modifyPatherInterest(MutableStat modifier);
    void unmodifyPatherInterest(String source);

    void modifyImmigration(MutableStat modifier);
    void unmodifyImmigration(String source);

    void modifyBuildCost(MutableStat modifier);
    void unmodifyBuildCost(String source);
}
