package boggled.terraforming;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.ArrayList;

public class PlanetTypeChangeJungle extends BoggledBaseTerraformingPlanetTypeChangeProject
{
    public PlanetTypeChangeJungle(MarketAPI market)
    {
        super(market, "jungle");
    }

    public PlanetTypeChangeJungle(MarketAPI market, String planetTypeId)
    {
        super(market, planetTypeId);
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementAtmosphericNotToxicOrIrradiated());
        projectRequirements.add(getRequirementMarketIsTemperateOrHot());
        projectRequirements.add(getRequirementMarketHasStellarReflectorArray());
        projectRequirements.add(getRequirementMarketHasHighWater());

        return projectRequirements;
    }
}
