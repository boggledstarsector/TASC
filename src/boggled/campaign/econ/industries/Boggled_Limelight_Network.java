package boggled.campaign.econ.industries;

import boggled.campaign.econ.StringHelper;
import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import java.awt.*;
import java.util.ArrayList;

public class Boggled_Limelight_Network extends BaseIndustry
{
    private final float IMPROVE_BONUS = 1.30f;

    private final float ALPHA_BONUS = 1.30f;
    private final int PRODUCTION_MALUS = -1;

    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void apply()
    {
        super.apply(true);

        int size = this.market.getSize();
        if(boggledTools.hyperlatticeAlloyDemandEnabled())
        {
            this.demand("hyperlattice_alloy", size - 2);
        }

        // Only has negative effects on player-owned markets
        if(this.market.isPlayerOwned())
        {
            // Reduce production by one at all industries
            for (Industry i : market.getIndustries()) {
                for (MutableCommodityQuantity c : i.getAllSupply()) {
                    i.getSupply(c.getCommodityId()).getQuantity().modifyFlat(id, PRODUCTION_MALUS, Misc.ucFirst(this.getCurrentName().toLowerCase()));
                }
            }

            if(boggledTools.hyperlatticeAlloyDemandEnabled())
            {
                Pair<String, Integer> deficit = getLimelightNetworkDeficit();
                if(deficit.two > 0)
                {
                    getIncome().modifyMult("deficit_income_malus", 0.0F, StringHelper.getString(StringHelper.INDUSTRIES, "limelight_shortage"));
                }
                else
                {
                    getIncome().unmodifyMult("deficit_income_malus");
                }

                // Reduce stability by amount of the shortage.
                if(deficit.two > 0)
                {
                    market.getStability().modifyFlat("deficit_stability_malus", -deficit.two, StringHelper.getString(StringHelper.INDUSTRIES, "limelight_shortage"));
                }
                else
                {
                    market.getStability().unmodifyFlat("deficit_stability_malus");
                }
            }
        }
    }

    @Override
    public void unapply()
    {
        // Undo reduce production by one at all industries
        for (Industry i : market.getIndustries()) {
            for (MutableCommodityQuantity c : i.getAllSupply()) {
                i.getSupply(c.getCommodityId()).getQuantity().unmodifyFlat(id);
            }
        }

        market.getStability().unmodifyFlat("deficit_stability_malus");

        super.unapply();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        // Only has negative effects on player-owned markets
        if(this.market.isPlayerOwned())
        {
            float opad = 10.0F;
            Color bad = Misc.getNegativeHighlightColor();

            Pair<String, Integer> deficit = getLimelightNetworkDeficit();
            if(deficit.two > 0 && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding()) {
                String commodity = boggledTools.getCommidityNameFromId(deficit.one);
                String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "limelight_no_income", StringHelper.tokens("commodity", commodity));
                StringHelper.applyHighlight(tooltip.addPara(sentence, opad), bad, commodity);
            }

            // Reduce stability by amount of the shortage.
            if(deficit.two > 0 && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding()) {
                String amount = deficit.two + "";
                String commodity = boggledTools.getCommidityNameFromId(deficit.one);
                String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "limelight_stability_reduced", StringHelper.tokens("amount", amount, "commodity", commodity));
                StringHelper.applyHighlight(tooltip.addPara(sentence, opad), bad, amount, commodity);
            }
        }
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isBuildingResearchComplete(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledLimelightNetworkPlayerBuildEnabled"))
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

        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledLimelightNetworkPlayerBuildEnabled"))
        {
            return false;
        }

        return super.showWhenUnavailable();
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String bonus = (int) Math.round((ALPHA_BONUS - 1.0f) * 100) + "%";
        String pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_alpha_assigned");
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_alpha_short");
        }
        String upkeep = (int)((1.0F - UPKEEP_MULT) * 100.0F) + "%";
        String demand = "" + DEMAND_REDUCTION;
        String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "limelight_alpha_core", StringHelper.tokens("prefix", pre, "upkeep", upkeep, "demand", demand, "income", bonus));

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            StringHelper.applyHighlight(text.addPara(sentence, 0.0F), highlight, upkeep, demand, bonus);
            tooltip.addImageWithText(opad);
        } else {
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), highlight, upkeep, demand, bonus);
        }
    }

    @Override
    public void applyAICoreToIncomeAndUpkeep()
    {
        super.applyAICoreToIncomeAndUpkeep();

        if(this.aiCoreId != null && this.aiCoreId.equals("alpha_core"))
        {
            String name = StringHelper.getString(StringHelper.INDUSTRIES, "limelight_alpha_assigned");
            this.getIncome().modifyMult("ind_lln_alpha_core", ALPHA_BONUS, name);
        }
        else
        {
            this.getUpkeep().unmodifyMult("ind_lln_alpha_core");
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
            this.getIncome().modifyMult("ind_improved", IMPROVE_BONUS, StringHelper.getString(StringHelper.INDUSTRIES, "limelight_improvements"));
        }
        else
        {
            this.getUpkeep().unmodifyMult("ind_improved");
        }
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String bonus = (int) Math.round((IMPROVE_BONUS - 1.0f) * 100) + "%";
        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "limelight_improve_increased", StringHelper.tokens("bonus", bonus));
            StringHelper.applyHighlight(info.addPara(sentence, 0.0F), highlight, bonus);
        }
        else
        {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "limelight_improve_increases", StringHelper.tokens("bonus", bonus));
            StringHelper.applyHighlight(info.addPara(sentence, 0.0F), highlight, bonus);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }

    @Override
    public float getPatherInterest()
    {
        if(this.market.isPlayerOwned())
        {
            return 10f;
        }
        else
        {
            return 0.0f;
        }
    }

    public Pair<String, Integer> getLimelightNetworkDeficit()
    {
        ArrayList<String> deficitCommodities = new ArrayList<>();
        if(boggledTools.hyperlatticeAlloyDemandEnabled()) {
            deficitCommodities.add("hyperlattice_alloy");
        }

        return this.getMaxDeficit(deficitCommodities.toArray(new String[0]));
    }

}

