package data.campaign.econ.industries;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
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
        this.demand("heavy_machinery", size - 2);
        this.supply("food", size);

        Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"heavy_machinery"});
        this.applyDeficitToProduction(1, deficit, new String[]{"food"});

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
    public boolean isAvailableToBuild()
    {
        if(boggledTools.getBooleanSetting("boggledHydroponicsEnabled"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return false;
    }

    @Override
    protected boolean canImproveToIncreaseProduction() {
        return true;
    }
}

