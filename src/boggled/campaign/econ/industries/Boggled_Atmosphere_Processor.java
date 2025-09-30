package boggled.campaign.econ.industries;

import java.awt.*;
import java.lang.String;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import boggled.campaign.econ.boggledTools;

public class Boggled_Atmosphere_Processor extends BaseIndustry
{
    @Override
    public boolean canBeDisrupted()
    {
        return true;
    }

    @Override
    public void apply()
    {
        super.apply(true);

        int size = this.market.getSize();
        this.demand("heavy_machinery", size);
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isResearched("tasc_atmosphere_manipulation"))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        if(!boggledTools.marketHasAtmoProblem(this.market))
        {
            return false;
        }

        if(!boggledTools.terraformingPossibleOnMarket(this.market))
        {
            return false;
        }

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.isResearched("tasc_atmosphere_manipulation"))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        if(!boggledTools.marketHasAtmoProblem(this.market))
        {
            return super.showWhenUnavailable();
        }

        if(!boggledTools.terraformingPossibleOnMarket(this.market))
        {
            return super.showWhenUnavailable();
        }

        return super.showWhenUnavailable();
    }

    @Override
    public String getUnavailableReason()
    {
        if(!boggledTools.marketHasAtmoProblem(this.market))
        {
            return "Atmospheric conditions on " + this.market.getName() + " are already optimal. There is no reason to build an atmosphere processor here.";
        }

        if(!boggledTools.terraformingPossibleOnMarket(this.market))
        {
            PlanetAPI planet = this.market.getPlanetEntity();
            if(boggledTools.getTascPlanetType(planet).equals(boggledTools.TascPlanetTypes.unknownPlanetId))
            {
                return "This planet type is unsupported by TASC. Please report this to boggled on the forums so he can add support. The planet type is: " + market.getPlanetEntity().getTypeId();
            }
            else
            {
                return "Stars, gas giants, volcanic worlds and irradiated worlds cannot be terraformed.";
            }
        }

       return super.getUnavailableReason();
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getPositiveHighlightColor();

        if(this.isDisrupted() && boggledTools.marketHasAtmoProblem(this.market) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Terraforming progress is stalled while the atmosphere processor is disrupted.", bad, opad);
        }
    }

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return true;
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        boolean shortage = false;
        Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"heavy_machinery"});
        if(deficit.two != 0)
        {
            shortage = true;
        }

        if(shortage && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            float opad = 10.0F;
            Color bad = Misc.getNegativeHighlightColor();

            if(deficit.two != 0)
            {
                tooltip.addPara("The atmosphere processor is inactive due to a shortage of heavy machinery.", bad, opad);
            }
        }
    }

    @Override
    public float getPatherInterest() { return super.getPatherInterest() + 2.0f; }

    @Override
    public boolean canImprove() { return false; }

    @Override
    public boolean canInstallAICores() {
        return false;
    }
}

