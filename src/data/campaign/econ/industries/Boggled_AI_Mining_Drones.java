package data.campaign.econ.industries;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.*;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONException;
import org.json.JSONObject;

public class Boggled_AI_Mining_Drones extends BaseIndustry
{
    public static float IMPROVE_BONUS = .20F;

    private static BoggledCommonIndustry commonIndustry;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        commonIndustry = new BoggledCommonIndustry(data, "AI Mining Drones");
    }

    @Override
    public boolean isAvailableToBuild() { return commonIndustry.isAvailableToBuild(getMarket()); }

    @Override
    public boolean showWhenUnavailable() { return commonIndustry.showWhenUnavailable(getMarket()); }

    @Override
    public String getUnavailableReason() { return commonIndustry.getUnavailableReason(getMarket()); }

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
        if (Commodities.ALPHA_CORE.equals(this.aiCoreId))
        {
            ai_bonus = 3;
        }
        else if (Commodities.BETA_CORE.equals(this.aiCoreId))
        {
            ai_bonus = 2;
        }
        else if (Commodities.GAMMA_CORE.equals(this.aiCoreId))
        {
            ai_bonus = 1;
        }

        Pair<String, Integer> deficit = this.getMaxDeficit(Commodities.FUEL, Commodities.SUPPLIES, Commodities.SHIPS);
        if(deficit.two > 0)
        {
            ai_bonus = ai_bonus - deficit.two;
        }

        // Make sure we can't return a negative bonus if a large supply deficit exists
        return Math.max(ai_bonus, 0);
    }

    @Override
    public void apply()
    {
        if(this.market.getPrimaryEntity() != null && this.market.getPrimaryEntity().hasTag(Tags.STATION) && this.isFunctional())
        {
            int size = this.market.getSize();
            this.demand(Commodities.FUEL, size);
            this.demand(Commodities.SUPPLIES, size);
            this.demand(Commodities.SHIPS, size);

            //Increased production
            Industry i = market.getIndustry(Industries.MINING);
            if (i != null) {
                for (MutableCommodityQuantity c : i.getAllSupply()) {
                    i.getSupply(c.getCommodityId()).getQuantity().modifyFlat(id, getProductionBonusFromMiningDrones(), "AI Mining Drones");
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
    public float getPatherInterest() { return 10.0F; }

    private void addAICoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode, String coreType, String highlights) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = coreType + "-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = coreType + "-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases mining production by %s units.", 0.0F, highlight, highlights);
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases mining production by %s units.", opad, highlight, highlights);
        }
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        addAICoreDescription(tooltip, mode, "Alpha", "3");
    }

    @Override
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        addAICoreDescription(tooltip, mode, "Beta", "2");
    }

    @Override
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        addAICoreDescription(tooltip, mode, "Gamma", "1");
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

        if(this.market.getPrimaryEntity() != null && this.market.getPrimaryEntity().hasTag(Tags.STATION))
        {
            tooltip.addPara("Current production bonus: %s", opad, highlight, getProductionBonusFromMiningDrones() + "");
            Pair<String, Integer> deficit = this.getMaxDeficit(Commodities.FUEL, Commodities.SUPPLIES, Commodities.SHIPS);

            if(deficit.two > 0)
            {
                tooltip.addPara("The production bonus is being reduced by " + deficit.two + " due to a " + deficit.one + " shortage.", bad, opad);
            }
        }
        else
        {
            tooltip.addPara("AI Mining Drones are only useful on station-based markets.", opad);
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
            info.addPara("Colony accessibility increased by %s.", 0.0F, highlight, aStr);
        }
        else
        {
            info.addPara("Increases colony accessibility by %s.", 0.0F, highlight, aStr);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }
}
