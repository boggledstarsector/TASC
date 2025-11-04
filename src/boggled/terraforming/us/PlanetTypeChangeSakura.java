package boggled.terraforming.us;

import boggled.campaign.econ.boggledTools;
import boggled.terraforming.PlanetTypeChangeTerran;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

import java.util.ArrayList;

public class PlanetTypeChangeSakura extends PlanetTypeChangeTerran
{
    public PlanetTypeChangeSakura(MarketAPI market)
    {
        super(market, "US_sakura");
    }

    @Override
    public ArrayList<String> conditionsToAddUponCompletion()
    {
        ArrayList<String> conditionsToAdd = super.conditionsToAddUponCompletion();
        conditionsToAdd.add("US_sakura");
        return conditionsToAdd;
    }

    @Override
    public String getModId() {
        return boggledTools.BoggledMods.unknownSkiesModId;
    }
}
