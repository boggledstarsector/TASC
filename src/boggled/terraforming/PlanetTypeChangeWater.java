package boggled.terraforming;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class PlanetTypeChangeWater extends BoggledBaseTerraformingPlanetTypeChangeProject
{
    public PlanetTypeChangeWater(MarketAPI market)
    {
        super(market, "water");
    }
}
