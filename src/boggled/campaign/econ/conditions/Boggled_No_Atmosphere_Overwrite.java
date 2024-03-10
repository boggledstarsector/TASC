
package boggled.campaign.econ.conditions;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;

public class Boggled_No_Atmosphere_Overwrite extends BaseHazardCondition
{
    public Boggled_No_Atmosphere_Overwrite() { }

    @Override
    public boolean showIcon() {
        return !this.market.getPrimaryEntity().hasTag("station");
    }
}
