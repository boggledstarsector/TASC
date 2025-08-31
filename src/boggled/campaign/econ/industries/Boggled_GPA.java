package boggled.campaign.econ.industries;

import java.lang.String;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;

public class Boggled_GPA extends BaseIndustry
{
    @Override
    public boolean canBeDisrupted()
    {
        return false;
    }

    @Override
    public void apply()
    {
        super.apply(true);

        if(boggledTools.domainEraArtifactDemandEnabled())
        {
            this.demand("domain_artifacts", 3);
        }
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

