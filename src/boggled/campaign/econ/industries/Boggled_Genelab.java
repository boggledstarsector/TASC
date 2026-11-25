package boggled.campaign.econ.industries;

import java.awt.Color;
import java.util.ArrayList;
import boggled.campaign.econ.StringHelper;
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

    static final String HYDROPONICS_FOOD_BONUS_MOD = "genelab_hydroponics_food";
    private static final int HYDROPONICS_FOOD_BONUS_OPERATIONAL = 2;
    private static final int HYDROPONICS_FOOD_BONUS_ALPHA_CORE = 2;
    private static final int HYDROPONICS_FOOD_BONUS_IMPROVED = 2;
    private static final int HYDROPONICS_FOOD_BONUS_PER_SHORTAGE = 2;

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
                        MessageIntel intel = new MessageIntel(StringHelper.getString(StringHelper.INDUSTRIES, "genelab_pollution_intel_title", StringHelper.tokens("market", this.market.getName())), Misc.getBasePlayerColor());
                        intel.addLine(StringHelper.getString(StringHelper.INDUSTRIES, "genelab_pollution_intel_line"));
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
                        MessageIntel intel = new MessageIntel(StringHelper.getString(StringHelper.INDUSTRIES, "genelab_lobster_intel_title", StringHelper.tokens("market", this.market.getName())), Misc.getBasePlayerColor());
                        intel.addLine(StringHelper.getString(StringHelper.INDUSTRIES, "genelab_lobster_intel_line"));
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
                tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "genelab_pollution_ongoing", StringHelper.tokens("market", this.market.getName())), bad, opad);
            }
            else
            {
                String percent = percentComplete + "%";
                String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "genelab_pollution_remediated", StringHelper.tokens("percent", percent, "market", this.market.getName()));
                StringHelper.applyHighlight(tooltip.addPara(sentence, opad), highlight, percent);
            }
        }

        if(this.isDisrupted() && this.market.hasCondition("pollution") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "genelab_pollution_stalled_disrupted"), bad, opad);
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

            String percent = percentComplete + "%";
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "genelab_lobster_progress", StringHelper.tokens("percent", percent));
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), highlight, percent);

        }

        if(this.isDisrupted() && boggledTools.marketHasWaterSurface(this.market) && !this.market.hasCondition("volturnian_lobster_pens") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "genelab_lobster_stalled_disrupted"), bad, opad);
        }
    }

    @Override
    public void apply()
    {
        super.apply(true);
        int size = this.market.getSize();

        if(boggledTools.hyperlatticeAlloysDemandEnabled())
        {
            if(!this.market.isPlayerOwned())
            {
                this.demand("hyperlattice_alloys", 4);
            }
            else
            {
                this.demand("hyperlattice_alloys", size - 2);
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
            park.getIncome().modifyMult("ind_genelab", ((float) (getGenelabMesozoicParkIncomeBonus() + 100)) / 100.0f, StringHelper.getString(StringHelper.INDUSTRIES, "genelab_income_modifier"));
        }

        Industry hydroponics = this.market.getIndustry("BOGGLED_HYDROPONICS");
        if(hydroponics instanceof Boggled_Hydroponics)
        {
            updateHydroponicsFoodBonus((Boggled_Hydroponics) hydroponics);
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

        removeHydroponicsFoodBonus();

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
                    String bonus = currentIncomeBonus + "%";
                    String bonusSentence = StringHelper.getString(StringHelper.INDUSTRIES, "genelab_mesozoic_income_bonus", StringHelper.tokens("bonus", bonus));
                    StringHelper.applyHighlight(tooltip.addPara(bonusSentence, opad), currentIncomeBonus > 0 ? highlight : bad, bonus);
                    if(deficit.two > 0)
                    {
                        String percent = (deficit.two * 50) + "%";
                        String commodity = boggledTools.getCommidityNameFromId(deficit.one);
                        String reduced = StringHelper.getString(StringHelper.INDUSTRIES, "genelab_mesozoic_income_reduced", StringHelper.tokens("percent", percent, "commodity", commodity));
                        StringHelper.applyHighlight(tooltip.addPara(reduced, opad), bad, percent, commodity);
                    }
                }
            }
        }

        if(boggledTools.getBooleanSetting("boggledHydroponicsEnabled") && mode != IndustryTooltipMode.QUEUED && !isBuilding() && this.isFunctional())
        {
            int foodBonus = getHydroponicsFoodProductionBonus();
            String bonus = Integer.toString(foodBonus);
            String bonusSentence = StringHelper.getString(StringHelper.INDUSTRIES, "genelab_hydroponics_food_bonus", StringHelper.tokens("bonus", bonus));
            StringHelper.applyHighlight(tooltip.addPara(bonusSentence, opad), foodBonus > 0 ? highlight : bad, "+" + bonus);
        }

        if(deficit.two > 0 && this.market.hasCondition("pollution") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            String commodity = boggledTools.getCommidityNameFromId(deficit.one);
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "genelab_pollution_stalled_shortage", StringHelper.tokens("commodity", commodity));
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), bad, commodity);
        }

        if(deficit.two > 0 && boggledTools.marketHasWaterSurface(this.market) && !this.market.hasCondition("volturnian_lobster_pens") && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            String commodity = boggledTools.getCommidityNameFromId(deficit.one);
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "genelab_lobster_stalled_shortage", StringHelper.tokens("commodity", commodity));
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), bad, commodity);
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

    public int getHydroponicsFoodProductionBonus()
    {
        if(!this.isFunctional())
        {
            return 0;
        }

        int bonus = HYDROPONICS_FOOD_BONUS_OPERATIONAL;
        if(Commodities.ALPHA_CORE.equals(this.aiCoreId))
        {
            bonus += HYDROPONICS_FOOD_BONUS_ALPHA_CORE;
        }
        if(this.isImproved())
        {
            bonus += HYDROPONICS_FOOD_BONUS_IMPROVED;
        }

        Pair<String, Integer> deficit = getGenelabDeficit();
        bonus -= HYDROPONICS_FOOD_BONUS_PER_SHORTAGE * deficit.two;

        return Math.max(bonus, 0);
    }

    static void updateHydroponicsFoodBonus(Boggled_Hydroponics hydroponics)
    {
        int bonus = 0;
        Industry genelab = hydroponics.getMarket().getIndustry(boggledTools.BoggledIndustries.genelabIndustryId);
        if(genelab instanceof Boggled_Genelab)
        {
            bonus = ((Boggled_Genelab) genelab).getHydroponicsFoodProductionBonus();
        }

        MutableCommodityQuantity food = hydroponics.getSupply().get(Commodities.FOOD);
        if(!hydroponics.isFunctional())
        {
            if(food != null)
            {
                food.getQuantity().unmodifyFlat(HYDROPONICS_FOOD_BONUS_MOD);
            }
            return;
        }

        if(bonus <= 0)
        {
            if(food != null)
            {
                food.getQuantity().unmodifyFlat(HYDROPONICS_FOOD_BONUS_MOD);
            }
            return;
        }

        if(food == null)
        {
            food = hydroponics.getSupply(Commodities.FOOD);
        }
        food.getQuantity().modifyFlat(HYDROPONICS_FOOD_BONUS_MOD, bonus, StringHelper.getString(StringHelper.INDUSTRIES, "genelab_income_modifier"));
    }

    private void removeHydroponicsFoodBonus()
    {
        Industry industry = this.market.getIndustry("BOGGLED_HYDROPONICS");
        if(!(industry instanceof Boggled_Hydroponics))
        {
            return;
        }

        MutableCommodityQuantity food = ((Boggled_Hydroponics) industry).getSupply().get(Commodities.FOOD);
        if(food != null)
        {
            food.getQuantity().unmodifyFlat(HYDROPONICS_FOOD_BONUS_MOD);
        }
    }

    public Pair<String, Integer> getGenelabDeficit()
    {
        ArrayList<String> deficitCommodities = new ArrayList<>();
        if (!this.market.hasCondition("habitable")) {
            deficitCommodities.add("organics");
        }
        if(boggledTools.hyperlatticeAlloysDemandEnabled()) {
            deficitCommodities.add("hyperlattice_alloys");
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
        String pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_alpha_assigned");
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_alpha_short");
        }

        String bonus = MESOZOIC_PARK_INCOME_BONUS_ALPHA_CORE + "%";
        String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "genelab_ai_increases_mesozoic_income", StringHelper.tokens("prefix", pre, "bonus", bonus));
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
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_beta_assigned");
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_beta_short");
        }

        String bonus = MESOZOIC_PARK_INCOME_BONUS_BETA_CORE + "%";
        String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "genelab_ai_increases_mesozoic_income", StringHelper.tokens("prefix", pre, "bonus", bonus));
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
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_gamma_assigned");
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_gamma_short");
        }

        String bonus = MESOZOIC_PARK_INCOME_BONUS_GAMMA_CORE + "%";
        String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "genelab_ai_increases_mesozoic_income", StringHelper.tokens("prefix", pre, "bonus", bonus));
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

        String bonus = MESOZOIC_PARK_INCOME_BONUS_IMPROVEMENT + "%";
        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "genelab_mesozoic_income_increased", StringHelper.tokens("bonus", bonus));
            StringHelper.applyHighlight(info.addPara(sentence, 0f), highlight, bonus);
        }
        else
        {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "genelab_increases_mesozoic_income", StringHelper.tokens("bonus", bonus));
            StringHelper.applyHighlight(info.addPara(sentence, 0f), highlight, bonus);
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
