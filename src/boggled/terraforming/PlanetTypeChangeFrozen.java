package boggled.terraforming;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class PlanetTypeChangeFrozen extends BoggledBaseTerraformingPlanetTypeChangeProject
{
    public PlanetTypeChangeFrozen(MarketAPI market)
    {
        super(market, "frozen");
    }
}
