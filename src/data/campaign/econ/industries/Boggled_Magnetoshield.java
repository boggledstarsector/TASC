package data.campaign.econ.industries;

import java.util.*;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import data.campaign.econ.boggledTools;

public class Boggled_Magnetoshield extends BaseIndustry
{
    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    public static List<String> SUPPRESSED_CONDITIONS = new ArrayList<String>();
    static
    {
        SUPPRESSED_CONDITIONS.add(Conditions.IRRADIATED);
    }

    @Override
    public void apply()
    {
        super.apply(true);

        if(isFunctional())
        {
            for (String cid : SUPPRESSED_CONDITIONS)
            {
                market.suppressCondition(cid);
            }
        }
    }

    @Override
    public void unapply()
    {
        for (String cid : SUPPRESSED_CONDITIONS)
        {
            market.unsuppressCondition(cid);
        }

        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        MarketAPI market = this.market;

        if(!boggledTools.getBooleanSetting("boggledMagnetoshieldEnabled") || !boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") || !this.market.hasCondition("irradiated"))
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledMagnetoshieldEnabled") || !boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public String getUnavailableReason()
    {
        if(!this.market.hasCondition("irradiated"))
        {
            return this.market.getName() + " is not irradiated. There is no reason to build a magnetoshield.";
        }
        else
        {
            return "Error in getUnavailableReason() in magnetoshield. Tell Boggled about this on the forums.";
        }
    }

    @Override
    public boolean canImprove()
    {
        return false;
    }

    @Override
    public boolean canInstallAICores() {
        return false;
    }
}
