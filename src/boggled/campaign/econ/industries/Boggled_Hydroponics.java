package boggled.campaign.econ.industries;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import java.lang.String;

public class Boggled_Hydroponics extends BaseIndustry
{
    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void apply()
    {
        super.apply(true);

        int size = this.market.getSize();
        if (!this.market.hasCondition("habitable")) {
            this.demand("organics", size - 2);
        }
        this.demand("heavy_machinery", size);
        this.supply("food", size);

        Pair<String, Integer> deficit = null;
        if (!this.market.hasCondition("habitable")) {
            deficit = this.getMaxDeficit(new String[]{"heavy_machinery", "organics"});
        }
        else
        {
            deficit = this.getMaxDeficit(new String[]{"heavy_machinery"});
        }

        this.applyDeficitToProduction(1, deficit, new String[]{"food"});

        if (!this.isFunctional())
        {
            this.supply.clear();
            this.unapply();
        }
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.getBooleanSetting("boggledHydroponicsEnabled"))
        {
            return false;
        }

        // Check research requirement
        if(!boggledTools.isBuildingResearchComplete(this.getId()))
        {
            return false;
        }

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.getBooleanSetting("boggledHydroponicsEnabled"))
        {
            return false;
        }

        return super.showWhenUnavailable();
    }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return true;
    }
}

