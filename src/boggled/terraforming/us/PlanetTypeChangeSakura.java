package boggled.terraforming.us;

import boggled.campaign.econ.boggledTools;
import boggled.terraforming.PlanetTypeChangeTerran;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

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
    public ArrayList<String> conditionsToRemoveUponCompletion()
    {
        ArrayList<String> conditionsToRemove = super.conditionsToRemoveUponCompletion();
        conditionsToRemove.remove("US_sakura");
        return conditionsToRemove;
    }

    @Override
    public TerraformingRequirementObject getRequirementNotAlreadyTargetType()
    {
        Boolean requirementMet = !this.market.getPlanetEntity().getTypeId().equals("US_sakura");
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

        return new TerraformingRequirementObject(this.market.getName() + " is not already a sakura world", requirementMet, null);
    }

    @Override
    public String getModId() {
        return boggledTools.BoggledMods.unknownSkiesModId;
    }
}
