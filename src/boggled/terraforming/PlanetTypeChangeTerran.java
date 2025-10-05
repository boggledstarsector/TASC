package boggled.terraforming;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.ArrayList;

public class PlanetTypeChangeTerran extends BoggledBaseTerraformingPlanetTypeChangeProject
{
    public PlanetTypeChangeTerran(MarketAPI market)
    {
        super(market, "terran");
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementMarketIsNotVeryHotOrVeryCold());
        projectRequirements.add(getRequirementMarketHasStellarReflectorArray());
        projectRequirements.add(getRequirementMarketHasHighWater());

        return projectRequirements;
    }
}
