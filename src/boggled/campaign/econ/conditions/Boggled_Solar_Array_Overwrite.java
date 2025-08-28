package boggled.campaign.econ.conditions;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import boggled.campaign.econ.boggledTools;


public class Boggled_Solar_Array_Overwrite extends BaseMarketConditionPlugin
{
    public static int FARMING_BONUS = 2;

    public void advance(float amount)
    {
        super.advance(amount);

        if(!this.market.getFactionId().equals(Factions.NEUTRAL) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled) && boggledTools.getBooleanSetting("boggledStellarReflectorArrayMarketAutoPlacementEnabled") && !this.market.hasIndustry(boggledTools.BoggledIndustries.stellarReflectorArrayIndustryId))
        {
            this.market.addIndustry(boggledTools.BoggledIndustries.stellarReflectorArrayIndustryId);
        }
    }

    public void apply(String id)
    {
        for (String cid : boggledTools.getStellarReflectorArraySuppressedConditions())
        {
            this.market.suppressCondition(cid);
        }

        Industry industry = getIndustry();
        if (industry != null)
        {
            industry.getSupplyBonusFromOther().modifyFlat(id, FARMING_BONUS, Misc.ucFirst(condition.getName().toLowerCase()));
        }
    }

    public void unapply(String id)
    {
        for (String cid : boggledTools.getStellarReflectorArraySuppressedConditions())
        {
            this.market.unsuppressCondition(cid);
        }

        Industry industry = getIndustry();
        if (industry != null)
        {
            industry.getSupplyBonusFromOther().unmodifyFlat(id);
        }
    }

    protected Industry getIndustry()
    {
        Industry industry = market.getIndustry(Industries.FARMING);
        if (industry == null)
        {
            industry = market.getIndustry(Industries.AQUACULTURE);
        }

        return industry;
    }

    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded)
    {
        float opad = 10f;

        List<String> conds = new ArrayList<String>();
        for (String id : boggledTools.getStellarReflectorArraySuppressedConditions())
        {
            MarketConditionSpecAPI mc = Global.getSettings().getMarketConditionSpec(id);
            conds.add(mc.getName());
        }

        String farmAqua = "Farming";
        if(this.market.hasCondition(Conditions.WATER_SURFACE))
        {
            farmAqua = "Aquaculture";
        }

        tooltip.addPara("Counters the effects of " + Misc.getAndJoined(conds) + ".\n\nIncreases food production by %s (" + farmAqua + ").", opad, Misc.getHighlightColor(), "" + FARMING_BONUS);
    }

    public String getIconName()
    {
        if(!boggledTools.getCreateMirrorsOrShades(this.market))
        {
            return Global.getSettings().getSpriteName("boggled", "stellar_shade_condition");
        }
        else
        {
            return Global.getSettings().getSpriteName("boggled", "stellar_mirror_condition");
        }
    }
}