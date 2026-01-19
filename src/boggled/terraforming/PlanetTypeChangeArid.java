package boggled.terraforming;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.ArrayList;

public class PlanetTypeChangeArid extends BoggledBaseTerraformingPlanetTypeChangeProject
{
    public PlanetTypeChangeArid(MarketAPI market)
    {
        super(market, "arid");
    }

    public PlanetTypeChangeArid(MarketAPI market, String planetTypeId)
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
        projectRequirements.add(getRequirementMarketHasModerateWater());

        return projectRequirements;
    }
}
