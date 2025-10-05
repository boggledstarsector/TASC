package boggled.terraforming;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.ArrayList;

public class PlanetTypeChangeArid extends BoggledBaseTerraformingPlanetTypeChangeProject
{
    public PlanetTypeChangeArid(MarketAPI market)
    {
        super(market, "arid");
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementMarketIsTemperateOrHot());
        projectRequirements.add(getRequirementMarketHasStellarReflectorArray());
        projectRequirements.add(getRequirementMarketHasModerateWater());

        return projectRequirements;
    }
}
