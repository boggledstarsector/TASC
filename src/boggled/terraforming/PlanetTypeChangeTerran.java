package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class PlanetTypeChangeTerran extends BoggledBaseTerraformingPlanetTypeChangeProject
{
    public PlanetTypeChangeTerran(MarketAPI market)
    {
        super(market, boggledTools.TascPlanetTypes.terranPlanetId);
    }
}
