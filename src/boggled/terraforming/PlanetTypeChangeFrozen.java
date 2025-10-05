package boggled.terraforming;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.ArrayList;

public class PlanetTypeChangeFrozen extends BoggledBaseTerraformingPlanetTypeChangeProject
{
    public PlanetTypeChangeFrozen(MarketAPI market)
    {
        super(market, "frozen");
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementMarketIsVeryCold());
        projectRequirements.add(getRequirementMarketHasHighWater());

        return projectRequirements;
    }
}
