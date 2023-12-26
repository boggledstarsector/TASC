package data.campaign.econ.industries;

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
import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

public class Boggled_Planetary_Agrav_Field extends BaseIndustry
{
    private static BoggledCommonIndustry commonindustry;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        commonindustry = new BoggledCommonIndustry(data, "Planetary Agrav Field");
    }

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

        if(isFunctional() && (this.market.hasIndustry(boggledTools.BoggledIndustries.domedCitiesIndustryId) || boggledTools.getPlanetType(this.market.getPlanetEntity()).equals(boggledTools.gasGiantPlanetId)))
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
        if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.planetaryAgravFieldEnabled) || !boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled))
        {
            return false;
        }

        //Can't build on stations
        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        if(!this.market.hasIndustry(boggledTools.BoggledIndustries.domedCitiesIndustryId) && !boggledTools.getPlanetType(this.market.getPlanetEntity()).equals(boggledTools.gasGiantPlanetId))
        {
            return false;
        }

        if(!this.market.hasCondition(Conditions.HIGH_GRAVITY) && !this.market.hasCondition(Conditions.LOW_GRAVITY))
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.planetaryAgravFieldEnabled) || !boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled))
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

        if(!this.market.hasCondition(Conditions.HIGH_GRAVITY) && !this.market.hasCondition(Conditions.LOW_GRAVITY))
        {
            return "Gravity on " + this.market.getName() + " is within the optimal range for humans. Building agrav generators here would serve little purpose.";
        }

        if(!this.market.hasIndustry(boggledTools.BoggledIndustries.domedCitiesIndustryId))
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

        if(mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.QUEUED ||!isFunctional() || !this.market.hasIndustry(boggledTools.BoggledIndustries.domedCitiesIndustryId))
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

        if(mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && isFunctional() && this.market.hasIndustry(boggledTools.BoggledIndustries.domedCitiesIndustryId))
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

