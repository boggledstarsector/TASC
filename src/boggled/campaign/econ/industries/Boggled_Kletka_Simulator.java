package boggled.campaign.econ.industries;

import java.awt.Color;
import java.util.*;
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
    private final int CONSTANT_DOMAIN_ERA_ARTIFACT_DEMAND_QUANTITY = 4;
    private final int ROLL_THRESHOLD_ALPHA = 100;
    private final int ROLL_THRESHOLD_BETA = 75;
    private final int ROLL_THRESHOLD_GAMMA = 50;
    private final int IMPROVEMENT_BONUS = 10;
    private final int ALPHA_CORE_BONUS = 30;
    private final int BETA_CORE_BONUS = 20;
    private final int GAMMA_CORE_BONUS = 10;
    @Override
    public boolean canBeDisrupted()
    {
        return true;
    }

    public Pair<String, Integer> getKletkaSimulatorDeficit()
    {
        ArrayList<String> deficitCommodities = new ArrayList<>();
        if(boggledTools.domainEraArtifactDemandEnabled()) {
            deficitCommodities.add("domain_artifacts");
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
        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "AI core training methodology improved by %s.", 0.0F, highlight, ALPHA_CORE_BONUS + "%");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Improves AI core training methodology by %s.", opad, highlight, ALPHA_CORE_BONUS + "%");
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
            text.addPara(pre + "AI core training methodology improved by %s.", opad, highlight, BETA_CORE_BONUS + "%");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Improves AI core training methodology by %s.", opad, highlight, BETA_CORE_BONUS + "%");
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
            text.addPara(pre + "AI core training methodology improved by %s.", opad, highlight, GAMMA_CORE_BONUS + "%");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Improves AI core training methodology by %s.", opad, highlight, GAMMA_CORE_BONUS + "%");
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
        if(boggledTools.domainEraArtifactDemandEnabled())
        {
            this.demand("domain_artifacts", CONSTANT_DOMAIN_ERA_ARTIFACT_DEMAND_QUANTITY);
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
                getUpkeep().modifyMult("temperature", 8.0f, "Station");
            }
            else if(market.hasCondition(Conditions.VERY_COLD))
            {
                if(suppCond.contains(Conditions.VERY_COLD) && suppressionRemovesTemperatureModifier)
                {
                    getUpkeep().modifyMult("temperature", 1.0f, "Extreme cold (suppressed)");
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 0.25f, "Extreme cold");
                }
            }
            else if(market.hasCondition(Conditions.COLD))
            {
                if(suppCond.contains(Conditions.COLD) && suppressionRemovesTemperatureModifier)
                {
                    getUpkeep().modifyMult("temperature", 1.0f, "Cold (suppressed)");
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 0.5f, "Cold");
                }
            }
            else if(market.hasCondition(Conditions.HOT))
            {
                if(suppCond.contains(Conditions.HOT) && suppressionRemovesTemperatureModifier)
                {
                    getUpkeep().modifyMult("temperature", 1.0f, "Hot (suppressed)");
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 2.0f, "Hot");
                }
            }
            else if(market.hasCondition(Conditions.VERY_HOT))
            {
                if(suppCond.contains(Conditions.VERY_HOT) && suppressionRemovesTemperatureModifier)
                {
                    getUpkeep().modifyMult("temperature", 1.0f, "Extreme heat (suppressed)");
                }
                else
                {
                    getUpkeep().modifyMult("temperature", 4.0f, "Extreme heat");
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
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledKletkaSimulatorEnabled"))
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

        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledKletkaSimulatorEnabled"))
        {
            return false;
        }

        return true;
    }

    @Override
    public String getUnavailableReason()
    {
        return "Error in getUnavailableReason() in the Kletka Simulator structure. Please tell boggled about this on the forums.";
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
            tooltip.addPara("Supercomputers will melt themselves without adequate cooling. Operating costs are lowest on very cold worlds and highest on very hot worlds and stations.", opad, highlight, new String[]{""});
        }

        // Don't show the core production changes until the Kletka Simulator is finished building
        if(mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.QUEUED || isBuilding())
        {
            return;
        }

        Pair<String, Integer> deficit = getKletkaSimulatorDeficit();
        tooltip.addPara("The Kletka Simulator attempts to produce a single AI core each month. The odds it will be of a given type are as follows:", opad, highlight, new String[]{});
        if(isDisrupted())
        {
            tooltip.addPara("%s", 2f, bad, "           None (disrupted)");
        }
        else if(deficit.two > 0)
        {
            tooltip.addPara("%s", 2f, bad, "           None (shortage)");
        }
        else
        {
            int rollAdjustment = getRollAdjustment();
            int alphaCorePercentage = Math.max(0, 100 + rollAdjustment - this.ROLL_THRESHOLD_ALPHA);
            int betaCorePercentage = Math.max(0, 100 + rollAdjustment - this.ROLL_THRESHOLD_BETA - alphaCorePercentage);
            int gammaCorePercentage = Math.max(0, 100 + rollAdjustment - this.ROLL_THRESHOLD_GAMMA - alphaCorePercentage - betaCorePercentage);
            int nothingPercentage = Math.max(0, 100 - alphaCorePercentage - betaCorePercentage - gammaCorePercentage);
            tooltip.addPara("           Alpha Core: %s", 2f, highlight, alphaCorePercentage + "%");
            tooltip.addPara("           Beta Core: %s", 2f, highlight, betaCorePercentage + "%");
            tooltip.addPara("           Gamma Core: %s", 2f, highlight, gammaCorePercentage + "%");
            tooltip.addPara("           Nothing (training failed): %s", 2f, highlight, nothingPercentage + "%");
        }
    }

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode)
    {
        return boggledTools.domainEraArtifactDemandEnabled();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        if(boggledTools.domainEraArtifactDemandEnabled())
        {
            float opad = 10.0F;
            Color highlight = Misc.getHighlightColor();

            tooltip.addPara("Kletka Simulators always demand %s Domain-era artifacts regardless of colony size.", opad, highlight, new String[]{"" + CONSTANT_DOMAIN_ERA_ARTIFACT_DEMAND_QUANTITY});
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

        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            info.addPara("AI core training methodology improved by %s.", 0f, highlight, IMPROVEMENT_BONUS + "%");
        }
        else
        {
            info.addPara("Improves AI core training methodology by %s.", 0f, highlight, IMPROVEMENT_BONUS + "%");
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }
}
