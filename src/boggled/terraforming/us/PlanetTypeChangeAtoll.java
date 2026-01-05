package boggled.terraforming.us;

import boggled.campaign.econ.boggledTools;
import boggled.terraforming.PlanetTypeChangeWater;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.util.ArrayList;

public class PlanetTypeChangeAtoll extends PlanetTypeChangeWater
{
    public PlanetTypeChangeAtoll(MarketAPI market)
    {
        super(market, "US_waterAtoll");
    }

    @Override
    public ArrayList<String> conditionsToAddUponCompletion()
    {
        ArrayList<String> conditionsToAdd = super.conditionsToAddUponCompletion();
        conditionsToAdd.add("US_hybrid");
        return conditionsToAdd;
    }

    @Override
    public ArrayList<String> conditionsToRemoveUponCompletion()
    {
        ArrayList<String> conditionsToRemove = super.conditionsToRemoveUponCompletion();
        conditionsToRemove.remove("US_hybrid");
        return conditionsToRemove;
    }

    @Override
    public TerraformingRequirementObject getRequirementNotAlreadyTargetType()
    {
        Boolean requirementMet = !this.market.getPlanetEntity().getTypeId().equals("US_waterAtoll");
        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                tooltipMakerAPI.addPara("Dummy text here - not already target type",10f);
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " is not already an atoll world", requirementMet, null);
    }

    @Override
    public String getModId() {
        return boggledTools.BoggledMods.unknownSkiesModId;
    }
}
