package data.campaign.econ.industries;

import java.awt.*;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;

public class Boggled_Atmosphere_Processor extends BoggledBaseIndustry {
    public Boggled_Atmosphere_Processor() {
        super("atmosphere_processor");
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color bad = Misc.getNegativeHighlightColor();

        if(this.isDisrupted() && boggledTools.marketHasAtmoProblem(this.market) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding()) {
            tooltip.addPara("Terraforming progress is stalled while the atmosphere processor is disrupted.", bad, opad);
        }
    }

    @Override
    public float getPatherInterest() { return super.getPatherInterest() + 2.0f; }
}

