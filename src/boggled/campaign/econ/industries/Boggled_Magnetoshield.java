package boggled.campaign.econ.industries;


import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;

public class Boggled_Magnetoshield extends BaseIndustry
{
    private boolean thisBuildingRemovedIrradiatedUponConstruction = false;
    @Override
    public boolean canBeDisrupted() {
        return false;
    }

    @Override
    public void apply() {
        super.apply(true);
    }

    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade) {
        super.notifyBeingRemoved(mode, forUpgrade);
        if(thisBuildingRemovedIrradiatedUponConstruction)
        {
            boggledTools.addCondition(this.market, Conditions.IRRADIATED);
        }
    }

    protected void buildingFinished() {
        super.buildingFinished();
        if(this.market.hasCondition(Conditions.IRRADIATED))
        {
            boggledTools.removeCondition(this.market, Conditions.IRRADIATED);
            thisBuildingRemovedIrradiatedUponConstruction = true;
        }
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledMagnetoshieldEnabled") || !boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        if(!this.market.hasCondition("irradiated"))
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

        if(!this.market.hasCondition("irradiated"))
        {
            return true;
        }

        return true;
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
            return "Error in getUnavailableReason() in magnetoshield. Tell boggled about this on the forums.";
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
