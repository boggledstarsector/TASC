package boggled.campaign.econ.industries;

import java.awt.Color;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
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
                if(i.getCurrentName().equals("Mining"))
                {
                    for(MutableCommodityQuantity c : i.getAllSupply())
                    {
                        i.getSupply(c.getCommodityId()).getQuantity().modifyFlat(id, getProductionBonusFromMiningDrones(), "AI Mining Drones");
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

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
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
        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases mining production by %s units.", 0.0F, highlight, "3");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases mining production by %s units.", opad, highlight, "3");
        }
    }

    @Override
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Beta-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Beta-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases mining production by %s units.", opad, highlight, "2");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases mining production by %s units.", opad, highlight, "2");
        }
    }

    @Override
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Gamma-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Gamma-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases mining production by %s unit.", opad, highlight, "1");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases mining production by %s unit.", opad, highlight, "1");
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
            tooltip.addPara("Current production bonus: %s", opad, highlight, new String[]{getProductionBonusFromMiningDrones() + ""});
            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"fuel","supplies","ships"});

            if(deficit.two > 0)
            {
                tooltip.addPara("The production bonus is being reduced by " + deficit.two + " due to a " + deficit.one + " shortage.", bad, opad);
            }
        }
        else
        {
            tooltip.addPara("AI Mining Drones are only useful on station-based markets.", opad, highlight, new String[]{""});
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
            market.getAccessibilityMod().modifyFlat(this.getModId(5), IMPROVE_BONUS, "AI Mining Drones");

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
            info.addPara("Colony accessibility increased by %s.", 0.0F, highlight, new String[]{aStr});
        }
        else
        {
            info.addPara("Increases colony accessibility by %s.", 0.0F, highlight, new String[]{aStr});
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }
}
