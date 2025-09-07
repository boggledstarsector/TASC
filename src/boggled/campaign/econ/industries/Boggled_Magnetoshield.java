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
        if(!boggledTools.getBooleanSetting("boggledMagnetoshieldEnabled") || !boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        if(!this.market.hasCondition("irradiated"))
        {
            return false;
        }

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.getBooleanSetting("boggledMagnetoshieldEnabled") || !boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        if(!this.market.hasCondition("irradiated"))
        {
            return super.showWhenUnavailable();
        }

        return super.showWhenUnavailable();
    }

    @Override
    public String getUnavailableReason()
    {
        if(!this.market.hasCondition("irradiated"))
        {
            return this.market.getName() + " is not irradiated. There is no reason to build a magnetoshield.";
        }

        return super.getUnavailableReason();
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
