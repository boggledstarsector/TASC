package data.campaign.econ.industries;

import java.lang.String;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;

public class Boggled_GPA extends BaseIndustry
{
    @Override
    public boolean canBeDisrupted()
    {
        return true;
    }

    @Override
    public void apply()
    {
        super.apply(true);

        this.demand("domain_artifacts", 3);
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        return false;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return false;
    }

    @Override
    public String getUnavailableReason()
    {
        return "Error in getUnavailableReason() in Boggled GPA. Please tell Boggled about this on the forums.";
    }

    @Override
    public float getPatherInterest()
    {
        if(!this.market.isPlayerOwned())
        {
            return 0;
        }
        else
        {
            return super.getPatherInterest() + 2.0f;
        }
    }

    @Override
    public boolean canImprove() { return false; }

    @Override
    public boolean canInstallAICores() {
        return false;
    }
}

