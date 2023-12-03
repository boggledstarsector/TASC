package data.campaign.econ.industries;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;

public class Boggled_Genelab extends BaseIndustry
{
    public static float IMPROVE_BONUS = 1.50f;

    private int daysWithoutShortagePollution = 0;
    private int lastDayCheckedPollution = 0;
    private int requiredDaysToRemovePollution = 200;

    private int daysWithoutShortageLobsters = 0;
    private int lastDayCheckedLobsters = 0;
    private int requiredDaysToAddLobsters = 200;

    @Override
    public boolean canBeDisrupted() { return true; }

    public boolean genelabHasShortage()
    {
        boolean shortage = false;
        if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled"))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"domain_artifacts"});
            if(deficit.two != 0)
            {
                shortage = true;
            }
        }

        return shortage;
    }

    private boolean pollutionIsOngoing()
    {
        if(this.market.hasCondition("habitable"))
        {
            if(this.market.hasIndustry(Industries.HEAVYINDUSTRY) && this.market.getIndustry(Industries.HEAVYINDUSTRY).getSpecialItem() != null && (this.market.getIndustry(Industries.HEAVYINDUSTRY).getSpecialItem().getId().equals(Items.CORRUPTED_NANOFORGE) || this.market.getIndustry(Industries.HEAVYINDUSTRY).getSpecialItem().getId().equals(Items.PRISTINE_NANOFORGE)))
            {
                return true;
            }
            else if(this.market.hasIndustry(Industries.ORBITALWORKS) && this.market.getIndustry(Industries.ORBITALWORKS).getSpecialItem() != null && (this.market.getIndustry(Industries.ORBITALWORKS).getSpecialItem().getId().equals(Items.CORRUPTED_NANOFORGE) || this.market.getIndustry(Industries.ORBITALWORKS).getSpecialItem().getId().equals(Items.PRISTINE_NANOFORGE)))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        boolean shortage = genelabHasShortage();

        CampaignClockAPI clock = Global.getSector().getClock();

        //
        // Pollution removal
        //

        if(this.market.hasCondition("pollution") && this.isFunctional())
        {
            if(clock.getDay() != this.lastDayCheckedPollution && !shortage)
            {
                this.daysWithoutShortagePollution++;
                this.lastDayCheckedPollution = clock.getDay();

                if(pollutionIsOngoing())
                {
                    this.daysWithoutShortagePollution = 0;
                    this.lastDayCheckedPollution = clock.getDay();
                }

                if(daysWithoutShortagePollution >= requiredDaysToRemovePollution)
                {
                    if (this.market.isPlayerOwned())
                    {
                        MessageIntel intel = new MessageIntel("Pollution on " + this.market.getName(), Misc.getBasePlayerColor());
                        intel.addLine("    - Remediated");
                        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
                    }

                    this.daysWithoutShortagePollution = 0;
                    this.lastDayCheckedPollution = clock.getDay();

                    if(this.market.hasCondition("pollution"))
                    {
                        this.market.removeCondition("pollution");
                    }

                    boggledTools.surveyAll(this.market);
                    boggledTools.refreshSupplyAndDemand(this.market);
                    boggledTools.refreshAquacultureAndFarming(this.market);
                }
            }
        }

        //
        // Lobster seeding
        //

        if(this.market.hasCondition("water_surface") && !this.market.hasCondition("volturnian_lobster_pens") && this.isFunctional())
        {
            if(clock.getDay() != this.lastDayCheckedLobsters && !shortage)
            {
                this.daysWithoutShortageLobsters++;
                this.lastDayCheckedLobsters = clock.getDay();

                if(this.daysWithoutShortageLobsters >= this.requiredDaysToAddLobsters)
                {
                    if (this.market.isPlayerOwned())
                    {
                        MessageIntel intel = new MessageIntel("Lobster seeding on " + this.market.getName(), Misc.getBasePlayerColor());
                        intel.addLine("    - Completed");
                        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
                    }

                    this.daysWithoutShortageLobsters = 0;
                    this.lastDayCheckedLobsters = clock.getDay();

                    boggledTools.addCondition(this.market, "volturnian_lobster_pens");

                    boggledTools.surveyAll(this.market);
                    boggledTools.refreshSupplyAndDemand(this.market);
                    boggledTools.refreshAquacultureAndFarming(this.market);
                }
            }
        }
    }

    @Override
    public void apply()
    {
        super.apply(true);

        if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled"))
        {
            if(!this.market.isPlayerOwned())
            {
                this.demand("domain_artifacts", 4);
            }
            else
            {
                int size = this.market.getSize();
                this.demand("domain_artifacts", size);
            }
        }

        Boggled_Mesozoic_Park park = (Boggled_Mesozoic_Park) this.market.getIndustry("BOGGLED_MESOZOIC_PARK");
        if(park != null && this.isFunctional() && !this.genelabHasShortage())
        {
            park.getIncome().modifyMult("ind_genelab", IMPROVE_BONUS, "Genelab");
        }
    }

    @Override
    public void unapply()
    {
        if(this.market.hasIndustry("BOGGLED_MESOZOIC_PARK"))
        {
            Boggled_Mesozoic_Park park = (Boggled_Mesozoic_Park) this.market.getIndustry("BOGGLED_MESOZOIC_PARK");
            park.getIncome().unmodifyMult("ind_genelab");
        }

        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") || !boggledTools.getBooleanSetting("boggledGenelabEnabled"))
        {
            return false;
        }

        // Station markets can't build
        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        return false;
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        this.daysWithoutShortagePollution = 0;
        this.lastDayCheckedPollution = 0;

        this.daysWithoutShortageLobsters = 0;
        this.lastDayCheckedLobsters = 0;

        super.notifyBeingRemoved(mode, forUpgrade);
    }

    @Override
    public float getPatherInterest()
    {
        if(!this.market.isPlayerOwned())
        {
            return super.getPatherInterest();
        }
        else
        {
            return 10.0F;
        }
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        //
        // Inserts pollution cleanup status
        //

        if(this.market.hasCondition("pollution") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            //200 days to clean up; divide daysWithoutShortage by 2 to get the percent
            int percentComplete = this.daysWithoutShortagePollution / 2;

            //Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
            if(percentComplete > 99)
            {
                percentComplete = 99;
            }

            if(pollutionIsOngoing())
            {
                tooltip.addPara("Heavy industrial activity is currently polluting " + this.market.getName() + ". Remediation can only begin once this ceases.", bad, opad);
            }
            else
            {
                tooltip.addPara("Approximately %s of the pollution on " + this.market.getName() + " has been remediated.", opad, highlight, new String[]{percentComplete + "%"});
            }
        }

        if(this.isDisrupted() && this.market.hasCondition("pollution") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Pollution remediation progress is stalled while the Genelab is disrupted.", bad, opad);
        }

        //
        // Inserts lobster seeding status
        //

        if(this.market.hasCondition("water_surface") && !this.market.hasCondition("volturnian_lobster_pens") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            //200 days to seed; divide daysWithoutShortage by 2 to get the percent
            int percentComplete = this.daysWithoutShortageLobsters / 2;

            //Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
            if(percentComplete > 99)
            {
                percentComplete = 99;
            }

            tooltip.addPara("Lobster seeding is approximately %s complete.", opad, highlight, new String[]{percentComplete + "%"});

        }

        if(this.isDisrupted() && this.market.hasCondition("water_surface") && !this.market.hasCondition("volturnian_lobster_pens") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Lobster seeding progress is stalled while the Genelab is disrupted.", bad, opad);
        }
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        boolean shortage = genelabHasShortage();
        float opad = 10.0F;
        Color bad = Misc.getNegativeHighlightColor();

        if(shortage && this.market.hasCondition("pollution") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Pollution remediation progress is stalled due to a shortage of Domain-era artifacts.", bad, opad);
        }

        if(shortage && this.market.hasCondition("water_surface") && !this.market.hasCondition("volturnian_lobster_pens") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Lobster seeding progress is stalled due to a shortage of Domain-era artifacts.", bad, opad);
        }
    }

    @Override
    public boolean canImprove()
    {
        return false;
    }

    @Override
    public boolean canInstallAICores() {
        return false;
    }
}
