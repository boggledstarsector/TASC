package boggled.terraforming;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.ArrayList;

public class PlanetTypeChangeWater extends BoggledBaseTerraformingPlanetTypeChangeProject
{
    public PlanetTypeChangeWater(MarketAPI market)
    {
        super(market, "water");
    }

    public PlanetTypeChangeWater(MarketAPI market, String planetTypeId)
    {
        super(market, planetTypeId);
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementAtmosphericNotToxicOrIrradiated());
        projectRequirements.add(getRequirementMarketIsNotVeryHotOrVeryCold());
        projectRequirements.add(getRequirementMarketHasStellarReflectorArray());
        projectRequirements.add(getRequirementMarketHasHighWater());

        return projectRequirements;
    }
}
