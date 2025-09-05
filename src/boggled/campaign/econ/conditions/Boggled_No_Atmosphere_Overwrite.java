
package boggled.campaign.econ.conditions;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;

public class Boggled_No_Atmosphere_Overwrite extends BaseHazardCondition
{
    public Boggled_No_Atmosphere_Overwrite() { }

    @Override
    public boolean showIcon()
    {
        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
