package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import java.awt.*;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import org.json.JSONException;
import org.json.JSONObject;

public class Boggled_Limelight_Network extends BaseIndustry
{
    private static BoggledCommonIndustry commonindustry;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        commonindustry = new BoggledCommonIndustry(data, "Limelight Network");
    }

    //Need to update string in addImproveDesc if value changed
    private final float IMPROVE_BONUS = 1.20f;

    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);
    }

    @Override
    public void apply()
    {
        super.apply(true);

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            int size = this.market.getSize();
            this.demand(boggledTools.BoggledCommodities.domainArtifacts, size - 2);
        }

        if(hasShortage())
        {
            getUpkeep().modifyMult("deficit", 5.0f, "Artifacts shortage");
        }
        else
        {
            getUpkeep().unmodifyMult("deficit");
        }
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    protected void buildingFinished()
    {
        super.buildingFinished();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        return boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.limelightNetworkPlayerBuildEnabled);
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        return boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.limelightNetworkPlayerBuildEnabled);
    }

    @Override
    public String getUnavailableReason()
    {
        return super.getUnavailableReason();
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Increases income by %s.", 0.0F, highlight, (int)((1.0F - UPKEEP_MULT) * 100.0F) + "%", "" + DEMAND_REDUCTION, "50%");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Increases income by %s.", opad, highlight, (int)((1.0F - UPKEEP_MULT) * 100.0F) + "%", "" + DEMAND_REDUCTION, "50%");
        }
    }

    @Override
    public void applyAICoreToIncomeAndUpkeep()
    {
        super.applyAICoreToIncomeAndUpkeep();

        float alpha_mult = 1.50f;

        if(this.aiCoreId != null && this.aiCoreId.equals(Commodities.ALPHA_CORE))
        {
            String name = "Alpha Core assigned";
            this.getIncome().modifyMult("ind_lln_alpha_core", alpha_mult, name);
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
            this.getIncome().modifyMult("ind_improved", IMPROVE_BONUS, "Improvements");

            if (!this.isFunctional())
            {
                this.unapply();
            }
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
        String bonus = "20%";
        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            info.addPara("Income increased by %s.", 0.0F, highlight, new String[]{bonus});
        }
        else
        {
            info.addPara("Increases income by %s.", 0.0F, highlight, new String[]{bonus});
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
            return super.getPatherInterest();
        }
    }

    private boolean hasShortage()
    {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{boggledTools.BoggledCommodities.domainArtifacts});
            return deficit.two != 0;
        }

        return false;
    }

}

