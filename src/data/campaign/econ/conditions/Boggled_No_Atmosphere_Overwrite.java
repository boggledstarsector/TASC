
package data.campaign.econ.conditions;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;

public class Boggled_No_Atmosphere_Overwrite extends BaseHazardCondition
{
    public Boggled_No_Atmosphere_Overwrite() { }

    public boolean showIcon()
    {
        if(this.market.getPrimaryEntity().hasTag("station"))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
