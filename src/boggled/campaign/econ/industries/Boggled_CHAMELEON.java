package boggled.campaign.econ.industries;

import java.awt.*;
import java.lang.String;
import java.util.ArrayList;

import boggled.campaign.econ.industries.interfaces.ShowBoggledTerraformingMenuOption;
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
import boggled.campaign.econ.boggledTools;

public class Boggled_CHAMELEON extends BaseIndustry implements ShowBoggledTerraformingMenuOption
{
    public static float IMPROVE_STABILITY_BONUS = 1f;

    private int daysWithoutShortageDeciv = 0;
    private int lastDayCheckedDeciv = 0;
    private final int requiredDaysToRemoveDeciv = boggledTools.getIntSetting("boggledChameleonDecivilizedSubpopEradicationDaysToFinish");

    private int daysWithoutShortageRogue = 0;
    private int lastDayCheckedRogue = 0;
    private final int requiredDaysToRemoveRogue = boggledTools.getIntSetting("boggledChameleonRogueCoreTerminationDaysToFinish");

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        Pair<String, Integer> deficit = getChameleonDeficit();

        if((this.market.hasCondition("decivilized_subpop") || this.market.hasCondition("decivilized")) && this.isFunctional())
        {
            CampaignClockAPI clock = Global.getSector().getClock();
            if(clock.getDay() != this.lastDayCheckedDeciv && deficit.two <= 0)
            {
                this.daysWithoutShortageDeciv++;
                this.lastDayCheckedDeciv = clock.getDay();

                if(this.daysWithoutShortageDeciv >= this.requiredDaysToRemoveDeciv)
                {
                    if(this.market.isPlayerOwned())
                    {
                        MessageIntel intel = new MessageIntel("Decivilized subpopulation on " + market.getName(), Misc.getBasePlayerColor());
                        intel.addLine("    - Eradicated");
                        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
                    }

                    boggledTools.removeCondition(this.market, "decivilized_subpop");
                    boggledTools.removeCondition(this.market, "decivilized");
                }
            }
        }

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
                        MessageIntel intel = new MessageIntel("Rogue AI core on " + market.getName(), Misc.getBasePlayerColor());
                        intel.addLine("    - Terminated");
                        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
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
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledCHAMELEONEnabled"))
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return false;
    }

    @Override
    public String getUnavailableReason()
    {
        return "Error in getUnavailableReason() in the CHAMELEON structure. Please tell Boggled about this on the forums.";
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        this.daysWithoutShortageDeciv = 0;
        this.lastDayCheckedDeciv = 0;
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

        // Inserts pacification status after description
        if((this.market.hasCondition("decivilized_subpop") || this.market.hasCondition("decivilized")) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            int percentComplete = (int) (((float) this.daysWithoutShortageDeciv / (float) this.requiredDaysToRemoveDeciv) * 100F);

            // Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
            if(percentComplete > 99)
            {
                percentComplete = 99;
            }

            tooltip.addPara("Approximately %s of the decivilized subpopulation on " + this.market.getName() + " has been eradicated.", opad, highlight, new String[]{percentComplete + "%"});
        }

        if(this.isDisrupted() && (this.market.hasCondition("decivilized_subpop") || this.market.hasCondition("decivilized")) && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Decivilized subpopulation eradication is stalled while the CHAMELEON is disrupted.", bad, opad);
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

            tooltip.addPara("An investigation into the whereabouts of the rogue AI core on " + this.market.getName() + " is approximately %s complete.", opad, highlight, new String[]{percentComplete + "%"});
        }

        if(this.isDisrupted() && this.market.hasCondition("rogue_ai_core") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Rogue AI core termination is stalled while the CHAMELEON is disrupted.", bad, opad);
        }
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        Pair<String, Integer> deficit = getChameleonDeficit();

        if(deficit.two > 0 && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            float opad = 10.0F;
            Color bad = Misc.getNegativeHighlightColor();
            tooltip.addPara("CHAMELEON is inactive due to a shortage of %s.", opad, bad, boggledTools.getCommidityNameFromId(deficit.one));
        }
    }

    @Override
    public float getPatherInterest()
    {
        Pair<String, Integer> deficit = getChameleonDeficit();

        if(isFunctional() && !isBuilding() && this.aiCoreId != null && this.aiCoreId.equals("alpha_core") && deficit.two <= 0)
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
        float UPKEEP_MULT = 0.75F;
        int DEMAND_REDUCTION = 1;

        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Pather cells on " + this.market.getName() + " are eliminated.", 0.0F, highlight, new String[]{(int)((1.0F - UPKEEP_MULT) * 100.0F) + "%", "" + DEMAND_REDUCTION});
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Reduces upkeep cost by %s. Reduces demand by %s unit. " + "Pather cells on " + this.market.getName() + " are eliminated.", opad, highlight, new String[]{(int)((1.0F - UPKEEP_MULT) * 100.0F) + "%", "" + DEMAND_REDUCTION});
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
        if(this.isImproved() && this.isFunctional() && deficit.two <= 0)
        {
            market.getStability().modifyFlat("chameleon_improve", IMPROVE_STABILITY_BONUS, getImprovementsDescForModifiers() + " (CHAMELEON)");
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

        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP) {
            info.addPara("Stability increased by %s.", 0f, highlight, "" + (int) IMPROVE_STABILITY_BONUS);
        } else {
            info.addPara("Increases stability by %s.", 0f, highlight, "" + (int) IMPROVE_STABILITY_BONUS);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }
}

