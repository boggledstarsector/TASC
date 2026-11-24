package boggled.campaign.econ.industries;

import java.awt.Color;
import java.util.*;
import boggled.campaign.econ.StringHelper;
import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;

public class Boggled_Kletka_Simulator extends BaseIndustry
{
    private final int CONSTANT_HYPERLATTICE_ALLOY_DEMAND_QUANTITY = 4;
    private final int ROLL_THRESHOLD_ALPHA = 100;
    private final int ROLL_THRESHOLD_BETA = 75;
    private final int ROLL_THRESHOLD_GAMMA = 50;
    private final int IMPROVEMENT_BONUS = 10;
    private final int ALPHA_CORE_BONUS = 15;
    private final int BETA_CORE_BONUS = 10;
    private final int GAMMA_CORE_BONUS = 5;
    @Override
    public boolean canBeDisrupted()
    {
        return true;
    }

    public Pair<String, Integer> getKletkaSimulatorDeficit()
    {
        ArrayList<String> deficitCommodities = new ArrayList<>();
        if(boggledTools.hyperlatticeAlloyDemandEnabled()) {
            deficitCommodities.add("hyperlattice_alloy");
        }

        return this.getMaxDeficit(deficitCommodities.toArray(new String[0]));
    }

    public int getRollAdjustment()
    {
        int adjustment = 0;
        if (this.isImproved()) {
            adjustment += IMPROVEMENT_BONUS;
        }

        if (this.aiCoreId != null) {
            if (this.aiCoreId.equals("alpha_core")) {
                adjustment += ALPHA_CORE_BONUS;
            } else if (this.aiCoreId.equals("beta_core")) {
                adjustment += BETA_CORE_BONUS;
            } else if (this.aiCoreId.equals("gamma_core")) {
                adjustment += GAMMA_CORE_BONUS;
            }
        }

        return adjustment;
    }

    @Override
    public CargoAPI generateCargoForGatheringPoint(Random random) {
        Pair<String, Integer> deficit = getKletkaSimulatorDeficit();

        CargoAPI cargo = Global.getFactory().createCargo(true);
        cargo.clear();
        if (!this.isFunctional() || deficit.two > 0) {
            // Return empty cargo if there's a deficit or disruption
            return cargo;
        } else {
            // How this works:
            // Generate a random int between 1 and 100 inclusive.
            // Add 10 to the roll for improvements. Add AI core bonuses to the roll (10, 20 or 30). Max adjustment is 40.
            // After adjusting the roll based on improvements and AI cores, check if it crosses the threshold for any of the AI core types.
            // Award the highest quality AI core where the threshold was crossed.
            // e.g. alpha core threshold is 100, it would be impossible to acquire an alpha core without
            // boosting your roll with improvements or AI cores since the maximum roll is 100, and it must exceed the threshold.
            int originalRoll = random.nextInt(100) + 1;
            int adjustedRoll = originalRoll + getRollAdjustment();

            if(adjustedRoll > this.ROLL_THRESHOLD_ALPHA)
            {
                cargo.addCommodity("alpha_core", 1f);
            }
            else if(adjustedRoll > this.ROLL_THRESHOLD_BETA)
            {
                cargo.addCommodity("beta_core", 1f);
            }
            else if(adjustedRoll > this.ROLL_THRESHOLD_GAMMA)
            {
                cargo.addCommodity("gamma_core", 1f);
            }

            boggledTools.writeMessageToLog("Kletka Simulator on " + this.market.getName() + ": Original roll was " + originalRoll + ". Adjusted roll after accounting for AI cores and improvements was " + adjustedRoll + ". Threshold for alpha core: " + ROLL_THRESHOLD_ALPHA + ". Threshold for beta core: " + ROLL_THRESHOLD_BETA + ". Threshold for gamma core: " + ROLL_THRESHOLD_GAMMA + ".");
            return cargo;
        }
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_alpha_assigned");
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_alpha_short");
        }
        String bonus = ALPHA_CORE_BONUS + "%";

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "kletka_core_training_improved", StringHelper.tokens("prefix", pre, "bonus", bonus));
            StringHelper.applyHighlight(text.addPara(sentence, 0.0F), highlight, bonus);
            tooltip.addImageWithText(opad);
        } else {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "kletka_core_training_improves", StringHelper.tokens("prefix", pre, "bonus", bonus));
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
        String bonus = BETA_CORE_BONUS + "%";

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "kletka_core_training_improved", StringHelper.tokens("prefix", pre, "bonus", bonus));
            StringHelper.applyHighlight(text.addPara(sentence, opad), highlight, bonus);
            tooltip.addImageWithText(opad);
        } else {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "kletka_core_training_improves", StringHelper.tokens("prefix", pre, "bonus", bonus));
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
        String bonus = GAMMA_CORE_BONUS + "%";

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "kletka_core_training_improved", StringHelper.tokens("prefix", pre, "bonus", bonus));
            StringHelper.applyHighlight(text.addPara(sentence, opad), highlight, bonus);
            tooltip.addImageWithText(opad);
        } else {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "kletka_core_training_improves", StringHelper.tokens("prefix", pre, "bonus", bonus));
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), highlight, bonus);
        }
    }

    @Override
    public void applyAICoreToIncomeAndUpkeep()
    {
        //Prevent AI cores from modifying income and upkeep
    }

    @Override
    public void updateAICoreToSupplyAndDemandModifiers()
    {
        //Prevent AI cores from modifying supply and demand
    }

    @Override
    public void apply()
    {
        if(boggledTools.hyperlatticeAlloyDemandEnabled())
        {
            this.demand("hyperlattice_alloy", CONSTANT_HYPERLATTICE_ALLOY_DEMAND_QUANTITY);
        }

        super.apply(false);
        super.applyIncomeAndUpkeep(3);

        if(boggledTools.getBooleanSetting("boggledKletkaSimulatorTemperatureBasedUpkeep"))
        {
            MarketAPI market = this.market;
            LinkedHashSet<String> suppCond = market.getSuppressedConditions();
            boolean suppressionRemovesTemperatureModifier = boggledTools.getBooleanSetting("boggledKletkaSimulatorSuppressTemperatureBasedUpkeep");
            if(boggledTools.marketIsStation(market))
            {
                getUpkeep().modifyMult("temperature", 8.0f, StringHelper.getString(StringHelper.INDUSTRIES, "kletka_upkeep_station"));
            }
            else if(market.hasCondition(Conditions.VERY_COLD))
            {
                if(suppCond.contains(Conditions.VERY_COLD) && suppressionRemovesTemperatureModifier)
                {
                    getUpkeep().modifyMult("temperature", 1.0f, StringHelper.getString(StringHelper.INDUSTRIES, "kletka_upkeep_very_cold_suppressed"));
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 0.25f, StringHelper.getString(StringHelper.INDUSTRIES, "kletka_upkeep_very_cold"));
                }
            }
            else if(market.hasCondition(Conditions.COLD))
            {
                if(suppCond.contains(Conditions.COLD) && suppressionRemovesTemperatureModifier)
                {
                    getUpkeep().modifyMult("temperature", 1.0f, StringHelper.getString(StringHelper.INDUSTRIES, "kletka_upkeep_cold_suppressed"));
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 0.5f, StringHelper.getString(StringHelper.INDUSTRIES, "kletka_upkeep_cold"));
                }
            }
            else if(market.hasCondition(Conditions.HOT))
            {
                if(suppCond.contains(Conditions.HOT) && suppressionRemovesTemperatureModifier)
                {
                    getUpkeep().modifyMult("temperature", 1.0f, StringHelper.getString(StringHelper.INDUSTRIES, "kletka_upkeep_hot_suppressed"));
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 2.0f, StringHelper.getString(StringHelper.INDUSTRIES, "kletka_upkeep_hot"));
                }
            }
            else if(market.hasCondition(Conditions.VERY_HOT))
            {
                if(suppCond.contains(Conditions.VERY_HOT) && suppressionRemovesTemperatureModifier)
                {
                    getUpkeep().modifyMult("temperature", 1.0f, StringHelper.getString(StringHelper.INDUSTRIES, "kletka_upkeep_very_hot_suppressed"));
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 4.0f, StringHelper.getString(StringHelper.INDUSTRIES, "kletka_upkeep_very_hot"));
                }
            }
            else
            {
                getUpkeep().unmodifyMult("temperature");
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

        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledKletkaSimulatorEnabled"))
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

        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledKletkaSimulatorEnabled"))
        {
            return false;
        }

        return super.showWhenUnavailable();
    }

    @Override
    public float getPatherInterest() { return 10.0F; }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        if(boggledTools.getBooleanSetting("boggledKletkaSimulatorTemperatureBasedUpkeep"))
        {
            tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "kletka_cooling"), opad);
        }

        // Don't show the core production changes until the Kletka Simulator is finished building
        if(mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.QUEUED || isBuilding())
        {
            return;
        }

        Pair<String, Integer> deficit = getKletkaSimulatorDeficit();
        tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "kletka_odds_intro"), opad);
        if(isDisrupted())
        {
            String none = StringHelper.getString(StringHelper.INDUSTRIES, "kletka_none_disrupted");
            StringHelper.applyHighlight(tooltip.addPara(none, 2f), bad, none);
        }
        else if(deficit.two > 0)
        {
            String none = StringHelper.getString(StringHelper.INDUSTRIES, "kletka_none_shortage");
            StringHelper.applyHighlight(tooltip.addPara(none, 2f), bad, none);
        }
        else
        {
            int rollAdjustment = getRollAdjustment();
            int alphaCorePercentage = Math.max(0, 100 + rollAdjustment - this.ROLL_THRESHOLD_ALPHA);
            int betaCorePercentage = Math.max(0, 100 + rollAdjustment - this.ROLL_THRESHOLD_BETA - alphaCorePercentage);
            int gammaCorePercentage = Math.max(0, 100 + rollAdjustment - this.ROLL_THRESHOLD_GAMMA - alphaCorePercentage - betaCorePercentage);
            int nothingPercentage = Math.max(0, 100 - alphaCorePercentage - betaCorePercentage - gammaCorePercentage);
            String alphaPercent = alphaCorePercentage + "%";
            String betaPercent = betaCorePercentage + "%";
            String gammaPercent = gammaCorePercentage + "%";
            String nothingPercent = nothingPercentage + "%";
            StringHelper.applyHighlight(tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "kletka_odds_alpha", StringHelper.tokens("percent", alphaPercent)), 2f), highlight, alphaPercent);
            StringHelper.applyHighlight(tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "kletka_odds_beta", StringHelper.tokens("percent", betaPercent)), 2f), highlight, betaPercent);
            StringHelper.applyHighlight(tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "kletka_odds_gamma", StringHelper.tokens("percent", gammaPercent)), 2f), highlight, gammaPercent);
            StringHelper.applyHighlight(tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "kletka_odds_nothing", StringHelper.tokens("percent", nothingPercent)), 2f), highlight, nothingPercent);
        }
    }

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode)
    {
        return boggledTools.hyperlatticeAlloyDemandEnabled();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        if(boggledTools.hyperlatticeAlloyDemandEnabled())
        {
            float opad = 10.0F;
            Color highlight = Misc.getHighlightColor();

            String demand = "" + CONSTANT_HYPERLATTICE_ALLOY_DEMAND_QUANTITY;
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "kletka_demand_artifacts", StringHelper.tokens("demand", demand));
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), highlight, demand);
        }
    }

    @Override
    public boolean canImprove() {
        return true;
    }

    @Override
    protected void applyImproveModifiers()
    {
        //Handled above in the cargo function
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
        float opad = 10f;
        Color highlight = Misc.getHighlightColor();

        String bonus = IMPROVEMENT_BONUS + "%";
        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "kletka_improve_training_improved", StringHelper.tokens("bonus", bonus));
            StringHelper.applyHighlight(info.addPara(sentence, 0f), highlight, bonus);
        }
        else
        {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "kletka_improve_training_improves", StringHelper.tokens("bonus", bonus));
            StringHelper.applyHighlight(info.addPara(sentence, 0f), highlight, bonus);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }
}
