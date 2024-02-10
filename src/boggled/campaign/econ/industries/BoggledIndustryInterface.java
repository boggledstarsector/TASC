package boggled.campaign.econ.industries;

import boggled.scripts.BoggledTerraformingProject;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.util.Pair;

import java.util.List;

public interface BoggledIndustryInterface {
    void applyDeficitToProduction(String modId, Pair<String, Integer> deficit, String... commodities);

    float getBasePatherInterest();
    void modifyPatherInterest(MutableStat modifier);
    void unmodifyPatherInterest(String source);

    void modifyImmigration(MutableStat modifier);
    void unmodifyImmigration(String source);

    void modifyBuildCost(MutableStat modifier);
    void unmodifyBuildCost(String source);

    void setEnableMonthlyProduction(boolean enabled);
    void addProductionData(BoggledCommonIndustry.ProductionData data);
    void removeProductionData(BoggledCommonIndustry.ProductionData data);
    List<BoggledCommonIndustry.ProductionData> getProductionData();
    void modifyProductionChance(String commodityId, String source, int value);
    void unmodifyProductionChance(String commodityId, String source);

    Pair<Integer, Integer> getProductionChance(String commodityId);

    void attachProject(BoggledTerraformingProject.ProjectInstance projectInstance);
    void detachProject(BoggledTerraformingProject.ProjectInstance projectInstance);
}
