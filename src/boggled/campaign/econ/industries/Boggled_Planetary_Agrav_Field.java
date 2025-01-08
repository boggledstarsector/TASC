package boggled.campaign.econ.industries;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import boggled.campaign.econ.boggledTools;

public class Boggled_Planetary_Agrav_Field extends BaseIndustry
{
    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    public static List<String> SUPPRESSED_CONDITIONS = new ArrayList<String>();
    static
    {
        SUPPRESSED_CONDITIONS.add(Conditions.HIGH_GRAVITY);
        SUPPRESSED_CONDITIONS.add(Conditions.LOW_GRAVITY);
    }

    @Override
    public void apply()
    {
        super.apply(true);

        if(isFunctional() && this.market.hasIndustry("BOGGLED_DOMED_CITIES"))
        {
            for (String cid : SUPPRESSED_CONDITIONS)
            {
                market.suppressCondition(cid);
            }
        }
    }

    @Override
    public void unapply()
    {
        for (String cid : SUPPRESSED_CONDITIONS)
        {
            market.unsuppressCondition(cid);
        }

        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.getBooleanSetting("boggledPlanetaryAgravFieldEnabled") || !boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        //Can't build on stations
        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        if(!this.market.hasIndustry("BOGGLED_DOMED_CITIES"))
        {
            return false;
        }

        if(!this.market.hasCondition("high_gravity") && !this.market.hasCondition("low_gravity"))
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.getBooleanSetting("boggledPlanetaryAgravFieldEnabled") || !boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        return true;
    }

    @Override
    public String getUnavailableReason()
    {
        MarketAPI market = this.market;

        //Can't build on stations
        if(boggledTools.marketIsStation(this.market))
        {
            return "Error in getUnavailableReason() in Planetary Agrav Field. Please report this to boggled on the forums.";
        }

        if(!this.market.hasCondition("high_gravity") && !this.market.hasCondition("low_gravity"))
        {
            return "Gravity on " + this.market.getName() + " is within the optimal range for humans. Building agrav generators here would serve little purpose.";
        }

        if(!this.market.hasIndustry("BOGGLED_DOMED_CITIES"))
        {
            return "It is not economically feasible to blanket an entire world with agrav generators. The population must be housed within a few centralized domed cities for a colony-wide agrav field to be practical.";
        }

        return "Error in getUnavailableReason() in Planetary Agrav Field. Please report this to boggled on the forums.";
    }

    @Override
    public void applyAICoreToIncomeAndUpkeep()
    {
        //Prevents AI cores from modifying upkeep
    }

    @Override
    protected void applyAlphaCoreSupplyAndDemandModifiers()
    {
        //Prevents AI cores from modifying supply and demand
    }

    @Override
    public boolean canImprove() {
        return false;
    }

    @Override
    public float getPatherInterest() { return super.getPatherInterest() + 2.0f; }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color bad = Misc.getNegativeHighlightColor();

        if(mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.QUEUED ||!isFunctional() || !this.market.hasIndustry("BOGGLED_DOMED_CITIES"))
        {
            tooltip.addPara("If operational, would counter the effects of:", opad, Misc.getHighlightColor(), "");
            int numCondsCountered = 0;
            for (String id : SUPPRESSED_CONDITIONS)
            {
                if(this.market.hasCondition(id))
                {
                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
                    tooltip.addPara("           %s", 2f, Misc.getHighlightColor(), condName);
                    numCondsCountered++;
                }
            }

            if(numCondsCountered == 0)
            {
                tooltip.addPara("           %s", 2f, Misc.getGrayColor(), "(none)");
            }
        }

        if(mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && isFunctional() && this.market.hasIndustry("BOGGLED_DOMED_CITIES"))
        {
            tooltip.addPara("Countering the effects of:", opad, Misc.getHighlightColor(), "");
            int numCondsCountered = 0;
            for (String id : SUPPRESSED_CONDITIONS)
            {
                if(this.market.hasCondition(id))
                {
                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
                    tooltip.addPara("           %s", 2f, Misc.getHighlightColor(), condName);
                    numCondsCountered++;
                }
            }

            if(numCondsCountered == 0)
            {
                tooltip.addPara("           %s", 2f, Misc.getGrayColor(), "(none)");
            }
        }
    }

    @Override
    public boolean canInstallAICores() {
        return false;
    }
}

