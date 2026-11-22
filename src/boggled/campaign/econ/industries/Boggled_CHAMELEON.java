package boggled.campaign.econ.industries;

import java.awt.*;
import java.lang.String;
import java.util.ArrayList;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import boggled.campaign.econ.StringHelper;
import boggled.campaign.econ.boggledTools;

public class Boggled_CHAMELEON extends BaseIndustry
{
    public static float IMPROVE_STABILITY_BONUS = 1f;

    private int daysWithoutShortageRogue = 0;
    private int lastDayCheckedRogue = 0;
    private final int requiredDaysToRemoveRogue = boggledTools.getIntSetting("boggledChameleonRogueCoreTerminationDaysToFinish");

    private static final String RAT_MOD_ID = "assortment_of_things";
    private static final String RAT_RAMPANT_MILITARY_CORE = "rat_rampant_military_core";

    private boolean marketHasRampantMilitaryCore()
    {
        return Global.getSettings().getModManager().isModEnabled(RAT_MOD_ID)
                && this.market.hasCondition(RAT_RAMPANT_MILITARY_CORE);
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        // The core will not allow a facility that could find and remove it to operate.
        if(this.marketHasRampantMilitaryCore())
        {
            return;
        }

        Pair<String, Integer> deficit = getChameleonDeficit();

        if(this.market.hasCondition("rogue_ai_core") && this.isFunctional())
        {
            CampaignClockAPI clock = Global.getSector().getClock();

            if(clock.getDay() != this.lastDayCheckedRogue && deficit.two <= 0)
            {
                this.daysWithoutShortageRogue++;
                this.lastDayCheckedRogue = clock.getDay();

                if(this.daysWithoutShortageRogue >= this.requiredDaysToRemoveRogue)
                {
                    if (this.market.isPlayerOwned())
                    {
                        MessageIntel intel = new MessageIntel(StringHelper.getString(StringHelper.INDUSTRIES, "chameleon_rogue_intel_title", StringHelper.tokens("market", market.getName())), Misc.getBasePlayerColor());
                        intel.addLine(StringHelper.getString(StringHelper.INDUSTRIES, "chameleon_rogue_intel_line"));
                        intel.setIcon(Global.getSettings().getSpriteName("boggled_intel_icons", "intel_icon_chameleon"));
                        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                    }

                    boggledTools.removeCondition(this.market, "rogue_ai_core");
                }
            }
        }
    }

    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void apply()
    {
        super.apply(true);

        if(boggledTools.domainEraArtifactDemandEnabled())
        {
            int size = this.market.getSize();
            this.demand("domain_artifacts", size);
        }
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    public Pair<String, Integer> getChameleonDeficit()
    {
        ArrayList<String> deficitCommodities = new ArrayList<>();
        if(boggledTools.domainEraArtifactDemandEnabled()) {
            deficitCommodities.add("domain_artifacts");
        }

        return this.getMaxDeficit(deficitCommodities.toArray(new String[0]));
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isBuildingResearchComplete(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledCHAMELEONEnabled"))
        {
            return false;
        }

        if(this.marketHasRampantMilitaryCore())
        {
            return false;
        }

        return super.isAvailableToBuild();
    }

    @Override
    public String getUnavailableReason()
    {
        if(this.marketHasRampantMilitaryCore())
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "chameleon_unavailable_rampant_core");
        }

        return super.getUnavailableReason();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.isBuildingResearchComplete(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledCHAMELEONEnabled"))
        {
            return false;
        }

        return super.showWhenUnavailable();
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        this.daysWithoutShortageRogue = 0;
        this.lastDayCheckedRogue = 0;

        super.notifyBeingRemoved(mode, forUpgrade);
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(this.marketHasRampantMilitaryCore() && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "chameleon_disabled_by_rampant_core"), bad, opad);
            return;
        }

        // Inserts rogue AI core removal status after description
        if(this.market.hasCondition("rogue_ai_core") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            int percentComplete = (int) (((float) this.daysWithoutShortageRogue / (float) this.requiredDaysToRemoveRogue) * 100F);

            // Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
            if(percentComplete > 99)
            {
                percentComplete = 99;
            }

            String percent = percentComplete + "%";
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "chameleon_rogue_investigation", StringHelper.tokens("market", this.market.getName(), "percent", percent));
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), highlight, percent);
        }

        if(this.isDisrupted() && this.market.hasCondition("rogue_ai_core") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "chameleon_rogue_stalled_disrupted"), bad, opad);
        }
    }

    @Override
    protected void applyAICoreToIncomeAndUpkeep()
    {
        if(this.marketHasRampantMilitaryCore())
        {
            getUpkeep().unmodifyMult("ind_core");
            return;
        }

        super.applyAICoreToIncomeAndUpkeep();
    }

    @Override
    protected void applyAlphaCoreSupplyAndDemandModifiers()
    {
        if(this.marketHasRampantMilitaryCore())
        {
            return;
        }

        super.applyAlphaCoreSupplyAndDemandModifiers();
    }

    @Override
    protected void applyBetaCoreSupplyAndDemandModifiers()
    {
        if(this.marketHasRampantMilitaryCore())
        {
            return;
        }

        super.applyBetaCoreSupplyAndDemandModifiers();
    }

    @Override
    protected void applyGammaCoreSupplyAndDemandModifiers()
    {
        if(this.marketHasRampantMilitaryCore())
        {
            return;
        }

        super.applyGammaCoreSupplyAndDemandModifiers();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        Pair<String, Integer> deficit = getChameleonDeficit();

        if(deficit.two > 0 && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            float opad = 10.0F;
            Color bad = Misc.getNegativeHighlightColor();
            String commodity = boggledTools.getCommidityNameFromId(deficit.one);
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "chameleon_inactive_shortage", StringHelper.tokens("commodity", commodity));
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), bad, commodity);
        }
    }

    @Override
    public float getPatherInterest()
    {
        Pair<String, Integer> deficit = getChameleonDeficit();

        if(isFunctional() && !isBuilding() && !this.marketHasRampantMilitaryCore() && this.aiCoreId != null && this.aiCoreId.equals("alpha_core") && deficit.two <= 0)
        {
            // Neutralizes Pather interest to zero.
            // Previously this just returned -1000, but with the 0.96a event system the large negative amount would be subtracted from the hostile event progress.
            int totalPatherInterest = 0;

            if (market.getAdmin().getAICoreId() != null)
            {
                totalPatherInterest += 10;
            }

            for(Industry industry : market.getIndustries())
            {
                if(!industry.isHidden() && !industry.getId().equals("BOGGLED_CHAMELEON"))
                {
                    float industryPatherInterest = industry.getPatherInterest();
                    if(industryPatherInterest >= 0.0)
                    {
                        totalPatherInterest += industryPatherInterest;
                    }
                }
            }

            return totalPatherInterest * -1;
        }
        else
        {
            return 10.0F;
        }
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        if(this.marketHasRampantMilitaryCore())
        {
            if(mode != AICoreDescriptionMode.INDUSTRY_TOOLTIP)
            {
                tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "chameleon_core_no_bonuses"), Misc.getNegativeHighlightColor(), 10.0F);
            }
            return;
        }

        float UPKEEP_MULT = 0.75F;
        int DEMAND_REDUCTION = 1;

        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_alpha_assigned");
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_alpha_short");
        }

        String percent = (int)((1.0F - UPKEEP_MULT) * 100.0F) + "%";
        String units = "" + DEMAND_REDUCTION;
        String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "chameleon_ai_alpha", StringHelper.tokens(
                "prefix", pre, "percent", percent, "units", units, "market", this.market.getName()));
        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            StringHelper.applyHighlight(text.addPara(sentence, 0.0F), highlight, percent, units);
            tooltip.addImageWithText(opad);
        } else {
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), highlight, percent, units);
        }
    }

    @Override
    public boolean canImprove() {
        return true;
    }

    @Override
    protected void applyImproveModifiers()
    {
        Pair<String, Integer> deficit = getChameleonDeficit();
        if(this.isImproved() && this.isFunctional() && !this.marketHasRampantMilitaryCore() && deficit.two <= 0)
        {
            market.getStability().modifyFlat("chameleon_improve", IMPROVE_STABILITY_BONUS, StringHelper.getString(StringHelper.INDUSTRIES, "chameleon_improve_modifier", StringHelper.tokens("improvements", getImprovementsDescForModifiers())));
        }
        else
        {
            market.getStability().unmodifyFlat("chameleon_improve");
        }
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
        float opad = 10f;
        Color highlight = Misc.getHighlightColor();

        String bonus = "" + (int) IMPROVE_STABILITY_BONUS;
        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "chameleon_stability_increased", StringHelper.tokens("bonus", bonus));
            StringHelper.applyHighlight(info.addPara(sentence, 0f), highlight, bonus);
        } else {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "chameleon_increases_stability", StringHelper.tokens("bonus", bonus));
            StringHelper.applyHighlight(info.addPara(sentence, 0f), highlight, bonus);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }
}

