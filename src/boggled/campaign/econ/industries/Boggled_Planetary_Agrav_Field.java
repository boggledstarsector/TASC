package boggled.campaign.econ.industries;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

import boggled.campaign.econ.industries.interfaces.ShowBoggledTerraformingMenuOption;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import boggled.campaign.econ.StringHelper;
import boggled.campaign.econ.boggledTools;

public class Boggled_Planetary_Agrav_Field extends BaseIndustry implements ShowBoggledTerraformingMenuOption
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

        // Check research requirement
        if(!boggledTools.isBuildingResearchComplete(this.getId()))
        {
            return false;
        }

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.isBuildingResearchComplete(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledPlanetaryAgravFieldEnabled") || !boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        if(!this.market.hasIndustry("BOGGLED_DOMED_CITIES"))
        {
            return super.showWhenUnavailable();
        }

        if(!this.market.hasCondition("high_gravity") && !this.market.hasCondition("low_gravity"))
        {
            return super.showWhenUnavailable();
        }

        return super.showWhenUnavailable();
    }

    @Override
    public String getUnavailableReason()
    {
        if(!this.market.hasIndustry("BOGGLED_DOMED_CITIES"))
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "agrav_needs_domes");
        }

        if(!this.market.hasCondition("high_gravity") && !this.market.hasCondition("low_gravity"))
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "agrav_gravity_optimal", StringHelper.tokens("market", this.market.getName()));
        }

        return super.getUnavailableReason();
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
            tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "agrav_would_counter"), opad);
            int numCondsCountered = 0;
            for (String id : SUPPRESSED_CONDITIONS)
            {
                if(this.market.hasCondition(id))
                {
                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
                    String line = StringHelper.getString(StringHelper.INDUSTRIES, "agrav_condition", StringHelper.tokens("condition", condName));
                    StringHelper.applyHighlight(tooltip.addPara(line, 2f), Misc.getHighlightColor(), condName);
                    numCondsCountered++;
                }
            }

            if(numCondsCountered == 0)
            {
                String none = StringHelper.getString(StringHelper.INDUSTRIES, "agrav_none");
                StringHelper.applyHighlight(tooltip.addPara(none, 2f), Misc.getGrayColor(), "(none)");
            }
        }

        if(mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && isFunctional() && this.market.hasIndustry("BOGGLED_DOMED_CITIES"))
        {
            tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "agrav_countering"), opad);
            int numCondsCountered = 0;
            for (String id : SUPPRESSED_CONDITIONS)
            {
                if(this.market.hasCondition(id))
                {
                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
                    String line = StringHelper.getString(StringHelper.INDUSTRIES, "agrav_condition", StringHelper.tokens("condition", condName));
                    StringHelper.applyHighlight(tooltip.addPara(line, 2f), Misc.getHighlightColor(), condName);
                    numCondsCountered++;
                }
            }

            if(numCondsCountered == 0)
            {
                String none = StringHelper.getString(StringHelper.INDUSTRIES, "agrav_none");
                StringHelper.applyHighlight(tooltip.addPara(none, 2f), Misc.getGrayColor(), "(none)");
            }
        }
    }

    @Override
    public boolean canInstallAICores() {
        return false;
    }
}

