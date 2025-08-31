package boggled.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.impl.Cryosanctum;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.util.Pair;
import boggled.campaign.econ.boggledTools;
import java.util.ArrayList;
import java.util.List;

public class Boggled_Cryosanctum extends Cryosanctum
{
    @Override
    public void apply()
    {
        super.apply(false);
        int size = 6;
        this.applyIncomeAndUpkeep((float)size);
        this.demand("supplies", size - 3);
        if (!this.market.hasCondition("habitable")) {
            this.demand("organics", size - 3);
        }
        if(boggledTools.domainEraArtifactDemandEnabled()) {
            this.demand("domain_artifacts", size - 3);
        }
        this.supply("organs", size);


        ArrayList<String> deficitCommodities = new ArrayList<>(List.of("supplies"));
        if (!this.market.hasCondition("habitable")) {
            deficitCommodities.add("organics");
        }
        if(boggledTools.domainEraArtifactDemandEnabled()) {
            deficitCommodities.add("domain_artifacts");
        }

        Pair<String, Integer> deficit = this.getMaxDeficit(deficitCommodities.toArray(new String[0]));
        if ((Integer)deficit.two > 0) {
            deficit.two = -1;
        }

        this.applyDeficitToProduction(1, deficit, new String[]{"organs"});
        if (!this.isFunctional()) {
            this.supply.clear();
        }
    }

    public boolean isAvailableToBuild() {
        return boggledTools.getBooleanSetting("boggledCryosanctumPlayerBuildEnabled");
    }

    public boolean showWhenUnavailable() {
        return false;
    }

    public MarketCMD.RaidDangerLevel adjustCommodityDangerLevel(String commodityId, MarketCMD.RaidDangerLevel level) {
        return level.next();
    }

    public MarketCMD.RaidDangerLevel adjustItemDangerLevel(String itemId, String data, MarketCMD.RaidDangerLevel level) {
        return level.next();
    }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return true;
    }

    @Override
    protected String getDescriptionOverride() {
        // Override the TASC industries.csv description with the vanilla one.
        // Will automatically update the description if vanilla updates it.
        return Global.getSettings().getIndustrySpec(Industries.CRYOSANCTUM).getDesc();
    }
}

