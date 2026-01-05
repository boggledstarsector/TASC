package boggled.terraforming.us;

import boggled.campaign.econ.boggledTools;
import boggled.terraforming.PlanetTypeChangeTerran;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class PlanetTypeChangeAlpine extends PlanetTypeChangeTerran
{
    public PlanetTypeChangeAlpine(MarketAPI market)
    {
        super(market, "US_alpine");
    }

    @Override
    public TerraformingRequirementObject getRequirementNotAlreadyTargetType()
    {
        Boolean requirementMet = !this.market.getPlanetEntity().getTypeId().equals("US_alpine");
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

        return new TerraformingRequirementObject(this.market.getName() + " is not already an alpine world", requirementMet, null);
    }

    @Override
    public String getModId() {
        return boggledTools.BoggledMods.unknownSkiesModId;
    }
}
