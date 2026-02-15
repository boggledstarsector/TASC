package boggled.campaign.econ.industries;

import java.awt.Color;
import java.util.ArrayList;
import boggled.campaign.econ.boggledTools;
import boggled.campaign.econ.industries.interfaces.ShowBoggledTerraformingMenuOption;
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

public class Boggled_Genelab extends BaseIndustry implements ShowBoggledTerraformingMenuOption
{
    public final int BASE_MESOZOIC_PARK_INCOME_BONUS = 100;
    public final int MESOZOIC_PARK_INCOME_BONUS_IMPROVEMENT = 50;
    public final int MESOZOIC_PARK_INCOME_BONUS_GAMMA_CORE = 15;
    public final int MESOZOIC_PARK_INCOME_BONUS_BETA_CORE = 30;
    public final int MESOZOIC_PARK_INCOME_BONUS_ALPHA_CORE = 60;

    private int daysWithoutShortagePollution = 0;
    private int lastDayCheckedPollution = 0;
    private final int requiredDaysToRemovePollution = boggledTools.getIntSetting("boggledGenelabPollutionRemediationDaysToFinish");

    private int daysWithoutShortageLobsters = 0;
    private int lastDayCheckedLobsters = 0;
    private final int requiredDaysToAddLobsters = boggledTools.getIntSetting("boggledGenelabLobsterSeedingDaysToFinish");

    public enum GenelabMode {
        LOBSTER,
        TREX
    }

    @Override
    public boolean canBeDisrupted() { return true; }

    private boolean pollutionIsOngoing()
    {
        // Heavy Industry and Orbital Works create pollution if they have a nanoforge installed.
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

        Pair<String, Integer> deficit = getGenelabDeficit();
        boolean shortage = deficit.two > 0;
        CampaignClockAPI clock = Global.getSector().getClock();

        //
        // Pollution removal
        //

        boolean pollutionIsOngoing = pollutionIsOngoing();
        if(pollutionIsOngoing)
        {
            this.daysWithoutShortagePollution = 0;
            this.lastDayCheckedPollution = clock.getDay();
        }

        if(this.market.hasCondition("pollution") && this.isFunctional() && !pollutionIsOngoing)
        {
            if(clock.getDay() != this.lastDayCheckedPollution && !shortage)
            {
                this.daysWithoutShortagePollution++;
                this.lastDayCheckedPollution = clock.getDay();

                if(daysWithoutShortagePollution >= requiredDaysToRemovePollution)
                {
                    if(this.market.isPlayerOwned())
                    {
                        MessageIntel intel = new MessageIntel("Pollution on " + this.market.getName(), Misc.getBasePlayerColor());
                        intel.addLine("    - Remediated");
                        intel.setIcon(getCurrentIntelIcon());
                        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
                    }

                    this.daysWithoutShortagePollution = 0;
                    this.lastDayCheckedPollution = clock.getDay();

                    boggledTools.removeCondition(this.market, "pollution");
                }
            }
        }

        //
        // Lobster seeding
        //

        if(boggledTools.marketHasWaterSurface(this.market) && !this.market.hasCondition("volturnian_lobster_pens") && this.isFunctional())
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
                        intel.setIcon(getCurrentIntelIcon());
                        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
                    }

                    this.daysWithoutShortageLobsters = 0;
                    this.lastDayCheckedLobsters = clock.getDay();

                    boggledTools.addCondition(this.market, "volturnian_lobster_pens");
                }
            }
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
            int percentComplete = (int) (((float) this.daysWithoutShortagePollution / (float) this.requiredDaysToRemovePollution) * 100F);

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

        if(boggledTools.marketHasWaterSurface(this.market) && !this.market.hasCondition("volturnian_lobster_pens") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            int percentComplete = (int) (((float) this.daysWithoutShortageLobsters / (float) this.requiredDaysToAddLobsters) * 100F);

            //Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up 99.5 to 100
            if(percentComplete > 99)
            {
                percentComplete = 99;
            }

            tooltip.addPara("Lobster seeding is approximately %s complete.", opad, highlight, new String[]{percentComplete + "%"});

        }

        if(this.isDisrupted() && boggledTools.marketHasWaterSurface(this.market) && !this.market.hasCondition("volturnian_lobster_pens") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Lobster seeding progress is stalled while the Genelab is disrupted.", bad, opad);
        }
    }

    @Override
    public void apply()
    {
        super.apply(true);
        int size = this.market.getSize();

        if(boggledTools.domainEraArtifactDemandEnabled())
        {
            if(!this.market.isPlayerOwned())
            {
                this.demand("domain_artifacts", 4);
            }
            else
            {
                this.demand("domain_artifacts", size - 2);
            }
        }
        if (!this.market.hasCondition("habitable")) {
            this.demand("organics", size - 2);
        }

        // All modifications to Mesozoic Park income bonus are handled in getGenelabMesozoicParkIncomeBonus().
        // We deal with reductions due to shortages in that function instead of here.
        Boggled_Mesozoic_Park park = (Boggled_Mesozoic_Park) this.market.getIndustry("BOGGLED_MESOZOIC_PARK");
        if(park != null && this.isFunctional())
        {
            park.getIncome().modifyMult("ind_genelab", ((float) (getGenelabMesozoicParkIncomeBonus() + 100)) / 100.0f, "Genelab");
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
        if(!boggledTools.isBuildingResearchComplete(this.getId()))
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

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.isBuildingResearchComplete(this.getId()))
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

        return super.showWhenUnavailable();
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
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        Pair<String, Integer> deficit = getGenelabDeficit();
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") && boggledTools.getBooleanSetting("boggledMesozoicParkEnabled"))
        {
            if(mode != IndustryTooltipMode.QUEUED && !isBuilding())
            {
                int currentIncomeBonus = getGenelabMesozoicParkIncomeBonus();
                if(this.isFunctional())
                {
                    tooltip.addPara("Mesozoic Park income bonus: %s", opad, currentIncomeBonus > 0 ? highlight : bad, currentIncomeBonus + "%");
                    if(deficit.two > 0)
                    {
                        tooltip.addPara("Mesozoic Park income bonus reduced by %s due to a shortage of %s.", opad, bad, (deficit.two * 50) + "%", boggledTools.getCommidityNameFromId(deficit.one));
                    }
                }
            }
        }

        if(deficit.two > 0 && this.market.hasCondition("pollution") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Pollution remediation progress is stalled due to a shortage of %s.", opad, bad, boggledTools.getCommidityNameFromId(deficit.one));
        }

        if(deficit.two > 0 && boggledTools.marketHasWaterSurface(this.market) && !this.market.hasCondition("volturnian_lobster_pens") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Lobster seeding progress is stalled due to a shortage of %s.", opad, bad, boggledTools.getCommidityNameFromId(deficit.one));
        }
    }

    public int getGenelabMesozoicParkIncomeBonus()
    {
        int improveBonus = BASE_MESOZOIC_PARK_INCOME_BONUS;
        if(!this.isFunctional())
        {
            return 0;
        }

        if(this.isImproved())
        {
            improveBonus = improveBonus + MESOZOIC_PARK_INCOME_BONUS_IMPROVEMENT;
        }

        if(this.aiCoreId != null)
        {
            improveBonus = switch (this.aiCoreId) {
                case "gamma_core" -> improveBonus + MESOZOIC_PARK_INCOME_BONUS_GAMMA_CORE;
                case "beta_core" -> improveBonus + MESOZOIC_PARK_INCOME_BONUS_BETA_CORE;
                case "alpha_core" -> improveBonus + MESOZOIC_PARK_INCOME_BONUS_ALPHA_CORE;
                default -> improveBonus;
            };
        }

        // Reduce bonus by 50% for each unit of commodity deficit
        Pair<String, Integer> deficit = getGenelabDeficit();
        improveBonus = improveBonus - (50 * deficit.two);

        // At worst, leave the Mesozoic Park income unchanged
        return Math.max(improveBonus, 0);
    }

    public Pair<String, Integer> getGenelabDeficit()
    {
        ArrayList<String> deficitCommodities = new ArrayList<>();
        if (!this.market.hasCondition("habitable")) {
            deficitCommodities.add("organics");
        }
        if(boggledTools.domainEraArtifactDemandEnabled()) {
            deficitCommodities.add("domain_artifacts");
        }

        return this.getMaxDeficit(deficitCommodities.toArray(new String[0]));
    }

    @Override
    public boolean canInstallAICores() {
        return boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") && boggledTools.getBooleanSetting("boggledMesozoicParkEnabled");
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
            text.addPara(pre + "Increases Mesozoic Park income by %s.", 0.0F, highlight, MESOZOIC_PARK_INCOME_BONUS_ALPHA_CORE + "%");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases Mesozoic Park income by %s.", opad, highlight, MESOZOIC_PARK_INCOME_BONUS_ALPHA_CORE + "%");
        }
    }

    @Override
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Beta-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Beta-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases Mesozoic Park income by %s.", opad, highlight, MESOZOIC_PARK_INCOME_BONUS_BETA_CORE + "%");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases Mesozoic Park income by %s.", opad, highlight, MESOZOIC_PARK_INCOME_BONUS_BETA_CORE  + "%");
        }
    }

    @Override
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = "Gamma-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Gamma-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases Mesozoic Park income by %s.", opad, highlight, MESOZOIC_PARK_INCOME_BONUS_GAMMA_CORE + "%");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases Mesozoic Park income by %s.", opad, highlight, MESOZOIC_PARK_INCOME_BONUS_GAMMA_CORE + "%");
        }
    }

    @Override
    public void applyAICoreToIncomeAndUpkeep() { }

    @Override
    protected void updateAICoreToSupplyAndDemandModifiers() { }

    @Override
    protected void applyAlphaCoreSupplyAndDemandModifiers() { }

    @Override
    protected void applyBetaCoreSupplyAndDemandModifiers() { }

    @Override
    protected void applyGammaCoreSupplyAndDemandModifiers() { }

    @Override
    protected void applyAlphaCoreModifiers() { }

    @Override
    protected void applyBetaCoreModifiers() { }

    @Override
    protected void applyGammaCoreModifiers() { }

    @Override
    protected void applyNoAICoreModifiers() { }

    @Override
    public boolean canImprove()
    {
        return boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") && boggledTools.getBooleanSetting("boggledMesozoicParkEnabled");
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode)
    {
        float opad = 10f;
        Color highlight = Misc.getHighlightColor();

        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            info.addPara("Mesozoic Park income increased by %s.", 0f, highlight, MESOZOIC_PARK_INCOME_BONUS_IMPROVEMENT + "%");
        }
        else
        {
            info.addPara("Increases Mesozoic Park income by %s.", 0f, highlight, MESOZOIC_PARK_INCOME_BONUS_IMPROVEMENT + "%");
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }

    @Override
    public String getCurrentImage()
    {
        if(getGenelabMode() == GenelabMode.TREX)
        {
            return Global.getSettings().getSpriteName("boggled", "genelab_trex");
        }
        else
        {
            return this.getSpec().getImageName();
        }
    }

    public String getCurrentIntelIcon()
    {
        return getGenelabMode() == GenelabMode.LOBSTER ?
                Global.getSettings().getSpriteName("boggled_intel_icons", "intel_icon_genelab_lobster") :
                Global.getSettings().getSpriteName("boggled_intel_icons", "intel_icon_genelab_trex");
    }

    public GenelabMode getGenelabMode()
    {
        if(boggledTools.marketHasWaterSurface(this.market))
        {
            return GenelabMode.LOBSTER;
        }
        else
        {
            return GenelabMode.TREX;
        }
    }
}
