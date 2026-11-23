package boggled.campaign.econ.industries;

import java.awt.Color;

import boggled.campaign.econ.StringHelper;
import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.util.Misc;

public class Boggled_AI_Mining_Drones extends BaseIndustry
{
    public static float IMPROVE_BONUS = .20F;

    @Override
    public boolean canBeDisrupted() {
        return false;
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);
    }

    public int getProductionBonusFromMiningDrones()
    {
        int ai_bonus = 0;
        if (this.aiCoreId == null)
        {
            ai_bonus = 0;
        }
        else if ("alpha_core".equals(this.aiCoreId))
        {
            ai_bonus = 3;
        }
        else if ("beta_core".equals(this.aiCoreId))
        {
            ai_bonus = 2;
        }
        else if ("gamma_core".equals(this.aiCoreId))
        {
            ai_bonus = 1;
        }
        else
        {
            ai_bonus = 0;
        }

        Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"fuel","supplies","ships"});
        if(deficit.two > 0)
        {
            ai_bonus = ai_bonus - deficit.two;
        }

        // Make sure we can't return a negative bonus if a large supply deficit exists
        if(ai_bonus < 0)
        {
            return 0;
        }
        else
        {
            return ai_bonus;
        }
    }

    @Override
    public void apply()
    {
        if(this.market.getPrimaryEntity() != null && this.market.getPrimaryEntity().hasTag("station") && this.isFunctional())
        {
            int size = this.market.getSize();
            this.demand("fuel", size);
            this.demand("supplies", size);
            this.demand("ships", size);

            //Increased production
            for(Industry i : market.getIndustries())
            {
                if(Industries.MINING.equals(i.getId()))
                {
                    for(MutableCommodityQuantity c : i.getAllSupply())
                    {
                        i.getSupply(c.getCommodityId()).getQuantity().modifyFlat(id, getProductionBonusFromMiningDrones(), StringHelper.getString(StringHelper.INDUSTRIES, "ai_mining_drones_modifier"));
                    }
                }
            }
        }

        super.apply(true);
    }

    @Override
    public void unapply()
    {
        for(Industry i : market.getIndustries())
        {
            for(MutableCommodityQuantity c : i.getAllSupply())
            {
                i.getSupply(c.getCommodityId()).getQuantity().unmodifyFlat(id);
            }
        }

        this.market.getAccessibilityMod().unmodifyFlat(this.getModId(5));

        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledStationConstructionContentEnabled") || !boggledTools.getBooleanSetting("boggledEnableAIMiningDronesStructure"))
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

        if(!boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledStationConstructionContentEnabled") || !boggledTools.getBooleanSetting("boggledEnableAIMiningDronesStructure"))
        {
            return false;
        }

        return super.showWhenUnavailable();
    }

    @Override
    public float getPatherInterest() { return 10.0F; }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_alpha_assigned");
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_alpha_short");
        }

        String bonus = "3";
        String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "ai_mining_drones_ai_increases_production_units", StringHelper.tokens("prefix", pre, "bonus", bonus));
        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            StringHelper.applyHighlight(text.addPara(sentence, 0.0F), highlight, bonus);
            tooltip.addImageWithText(opad);
        } else {
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), highlight, bonus);
        }
    }

    @Override
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_beta_assigned");
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_beta_short");
        }

        String bonus = "2";
        String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "ai_mining_drones_ai_increases_production_units", StringHelper.tokens("prefix", pre, "bonus", bonus));
        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            StringHelper.applyHighlight(text.addPara(sentence, opad), highlight, bonus);
            tooltip.addImageWithText(opad);
        } else {
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), highlight, bonus);
        }
    }

    @Override
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_gamma_assigned");
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_gamma_short");
        }

        String bonus = "1";
        String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "ai_mining_drones_ai_increases_production_unit", StringHelper.tokens("prefix", pre, "bonus", bonus));
        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            StringHelper.applyHighlight(text.addPara(sentence, opad), highlight, bonus);
            tooltip.addImageWithText(opad);
        } else {
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), highlight, bonus);
        }
    }

    @Override
    public void applyAICoreToIncomeAndUpkeep()
    {
        //This being blank prevents installed AI cores from altering monthly upkeep
    }

    @Override
    public void updateAICoreToSupplyAndDemandModifiers()
    {
        //This being blank prevents AI cores from reducing the demand
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(this.market.getPrimaryEntity() != null && this.market.getPrimaryEntity().hasTag("station"))
        {
            String bonus = getProductionBonusFromMiningDrones() + "";
            String bonusSentence = StringHelper.getString(StringHelper.INDUSTRIES, "ai_mining_drones_current_production_bonus", StringHelper.tokens("bonus", bonus));
            StringHelper.applyHighlight(tooltip.addPara(bonusSentence, opad), highlight, bonus);
            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"fuel","supplies","ships"});

            if(deficit.two > 0)
            {
                String reduced = StringHelper.getString(StringHelper.INDUSTRIES, "ai_mining_drones_production_reduced", StringHelper.tokens(
                        "amount", String.valueOf(deficit.two), "commodity", String.valueOf(deficit.one)));
                tooltip.addPara(reduced, bad, opad);
            }
        }
        else
        {
            tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "ai_mining_drones_station_only"), opad);
        }
    }

    @Override
    public boolean canImprove() {
        return true;
    }

    @Override
    protected void applyImproveModifiers()
    {
        if (this.isImproved())
        {
            market.getAccessibilityMod().modifyFlat(this.getModId(5), IMPROVE_BONUS, StringHelper.getString(StringHelper.INDUSTRIES, "ai_mining_drones_modifier"));

            if (!this.isFunctional())
            {
                this.unapply();
            }
        }
        else
        {
            this.market.getAccessibilityMod().unmodifyFlat(this.getModId(5));
        }
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        float a = IMPROVE_BONUS;
        String aStr = Math.round(a * 100.0F) + "%";
        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "ai_mining_drones_accessibility_increased", StringHelper.tokens("bonus", aStr));
            StringHelper.applyHighlight(info.addPara(sentence, 0.0F), highlight, aStr);
        }
        else
        {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "ai_mining_drones_increases_accessibility", StringHelper.tokens("bonus", aStr));
            StringHelper.applyHighlight(info.addPara(sentence, 0.0F), highlight, aStr);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }
}
