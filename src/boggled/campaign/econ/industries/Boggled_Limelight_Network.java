package boggled.campaign.econ.industries;

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
        if(boggledTools.domainEraArtifactDemandEnabled())
        {
            this.demand("domain_artifacts", size - 2);
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

            if(boggledTools.domainEraArtifactDemandEnabled())
            {
                Pair<String, Integer> deficit = getLimelightNetworkDeficit();
                if(deficit.two > 0)
                {
                    getIncome().modifyMult("deficit_income_malus", 0.0F, "Domain-era artifacts shortage");
                }
                else
                {
                    getIncome().unmodifyMult("deficit_income_malus");
                }

                // Reduce stability by amount of the shortage.
                if(deficit.two > 0)
                {
                    market.getStability().modifyFlat("deficit_stability_malus", -deficit.two, "Domain-era artifacts shortage");
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
                tooltip.addPara("Generating no income due to a shortage of %s.", opad, bad, boggledTools.getCommidityNameFromId(deficit.one));
            }

            // Reduce stability by amount of the shortage.
            if(deficit.two > 0 && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding()) {
                tooltip.addPara("Colony stability reduced by %s due to a shortage of %s.", opad, bad, deficit.two + "", boggledTools.getCommidityNameFromId(deficit.one));
            }
        }
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isResearched("tasc_limelight_network"))
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
        if(!boggledTools.isResearched("tasc_limelight_network"))
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
        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Increases income by %s.", 0.0F, highlight, new String[]{(int)((1.0F - UPKEEP_MULT) * 100.0F) + "%", "" + DEMAND_REDUCTION, bonus});
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Increases income by %s.", opad, highlight, new String[]{(int)((1.0F - UPKEEP_MULT) * 100.0F) + "%", "" + DEMAND_REDUCTION, bonus});
        }
    }

    @Override
    public void applyAICoreToIncomeAndUpkeep()
    {
        super.applyAICoreToIncomeAndUpkeep();

        if(this.aiCoreId != null && this.aiCoreId.equals("alpha_core"))
        {
            String name = "Alpha Core assigned";
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
            this.getIncome().modifyMult("ind_improved", IMPROVE_BONUS, "Improvements");
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
            return 0.0f;
        }
    }

    public Pair<String, Integer> getLimelightNetworkDeficit()
    {
        ArrayList<String> deficitCommodities = new ArrayList<>();
        if(boggledTools.domainEraArtifactDemandEnabled()) {
            deficitCommodities.add("domain_artifacts");
        }

        return this.getMaxDeficit(deficitCommodities.toArray(new String[0]));
    }

}

