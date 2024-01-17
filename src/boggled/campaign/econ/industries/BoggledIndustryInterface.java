package boggled.campaign.econ.industries;

import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.util.Pair;

public interface BoggledIndustryInterface {
    void applyDeficitToProduction(String modId, Pair<String, Integer> deficit, String... commodities);

    void setFunctional(boolean functional);

    float getBasePatherInterest();
    void modifyPatherInterest(MutableStat modifier);
    void unmodifyPatherInterest(String source);

    void modifyImmigration(MutableStat modifier);
    void unmodifyImmigration(String source);

    void modifyBuildCost(MutableStat modifier);
    void unmodifyBuildCost(String source);

    void addProductionData(BoggledCommonIndustry.ProductionData data);
    void removeProductionData(BoggledCommonIndustry.ProductionData data);
    void modifyProductionChance(String commodityId, String source, int value);
    void unmodifyProductionChance(String commodityId, String source);
}
