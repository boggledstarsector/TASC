package boggled.campaign.econ.industries;

import java.awt.*;
import java.lang.String;

import boggled.campaign.econ.industries.interfaces.ShowBoggledTerraformingMenuOption;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import boggled.campaign.econ.boggledTools;

public class Boggled_Domed_Cities extends BaseIndustry implements MarketImmigrationModifier, ShowBoggledTerraformingMenuOption
{
    public static float IMPROVE_STABILITY_BONUS = 1f;

    public static float DEFENSE_BONUS = 6f;
    public static float DEFENSE_MALUS = 0.05f;

    public static float ACCESSIBILITY_BONUS = .10f;
    public static float ACCESSIBILITY_MALUS = -.10f;

    public static float SKY_CITIES_UPKEEP_MULTIPLIER = 6f;
    public static float SKY_CITIES_BUILD_COST_MULTIPLIER = 3f;

    // Use this function to determine which mode the building is in
    // e.g. this.getCurrentName().equals("Domed Cities")
    @Override
    public String getCurrentName() {
        if(boggledTools.getPlanetType(market.getPlanetEntity()).getPlanetId().equals("gas_giant"))
        {
            return "Sky Cities";
        }
        else if(boggledTools.getPlanetType(market.getPlanetEntity()).getPlanetId().equals("water"))
        {
            return "Seafloor Cities";
        }
        else
        {
            return "Domed Cities";
        }
    }

    // Only the base Domed Cities mode can be disrupted
    @Override
    public boolean canBeDisrupted()
    {
        return this.getCurrentName().equals("Domed Cities");
    }

    @Override
    public void apply()
    {
        super.apply(true);

        // Sky Cities upkeep malus
        if(this.getCurrentName().equals("Sky Cities"))
        {
            this.getUpkeep().modifyMultAlways("boggled_sky_cities", SKY_CITIES_UPKEEP_MULTIPLIER, "Sky cities upkeep multiplier");
        }

        // Reduces ground defense in Domed Cities and Sky Cities modes, increases it in Seafloor Cities mode.
        // Always applies the bonus/malus, even if the building is disrupted
        if(this.getCurrentName().equals("Seafloor Cities"))
        {
            this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(getModId(), DEFENSE_BONUS, getNameForModifier());
        }
        else
        {
            if(boggledTools.getBooleanSetting("boggledDomedCitiesDefensePenaltyEnabled"))
            {
                this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(getModId(), DEFENSE_MALUS, getNameForModifier());
            }
        }

        //Only suppresses conditions, improves stability and affects accessibility if it's not disrupted
        if(isFunctional())
        {
            // Suppress conditions
            for (String cid : boggledTools.getDomedCitiesSuppressedConditions())
            {
                if(cid.equals("water_surface") && this.market.hasCondition("water_surface"))
                {
                    // Temporary hack to "suppress" water surface without actually suppressing it -
                    // actually suppressing it causes aquaculture to produce no food.
                    // Alex has to fix this in vanilla as far as I know.
                    float hazard = -0.25f;
                    this.market.getHazard().modifyFlat(this.getModId(), hazard, "Seafloor cities");
                }
                else
                {
                    market.suppressCondition(cid);
                }
            }

            // Reduces accessibility by 10% if in Seafloor Cities mode, boosts it by 10% if in Sky Cities mode
            if(this.getCurrentName().equals("Seafloor Cities"))
            {
                this.market.getAccessibilityMod().modifyFlat(this.getModId(), ACCESSIBILITY_MALUS, "Seafloor cities");
            }
            else if(this.getCurrentName().equals("Sky Cities"))
            {
                this.market.getAccessibilityMod().modifyFlat(this.getModId(), ACCESSIBILITY_BONUS, "Sky cities");
            }

            //Stability bonus
            if (this.aiCoreId == null)
            {
                this.market.getStability().unmodifyFlat(this.getModId());
            }
            else if (this.aiCoreId.equals("gamma_core"))
            {
                this.market.getStability().modifyFlat(this.getModId(), (float)1, this.getNameForModifier());
            }
            else if (this.aiCoreId.equals("beta_core"))
            {
                this.market.getStability().modifyFlat(this.getModId(), (float)2, this.getNameForModifier());
            }
            else if (this.aiCoreId.equals("alpha_core"))
            {
                this.market.getStability().modifyFlat(this.getModId(), (float)3, this.getNameForModifier());
            }
        }
    }

    @Override
    public void unapply()
    {
        for (String cid : boggledTools.getDomedCitiesSuppressedConditions())
        {
            market.unsuppressCondition(cid);
        }

        this.getUpkeep().unmodifyMult("boggled_sky_cities");

        this.market.getStability().unmodifyFlat(this.getModId());
        this.market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyMult(getModId());

        // Seafloor cities modifiers
        this.market.getAccessibilityMod().unmodifyFlat(this.getModId());
        this.market.getHazard().unmodifyFlat(this.getModId());
        super.unapply();
    }

    // Sky Cities mode has increased upkeep
    @Override
    public float getBaseUpkeep()
    {
        if(this.getCurrentName().equals("Sky Cities"))
        {
            return super.getBaseUpkeep() * SKY_CITIES_UPKEEP_MULTIPLIER;
        }
        else
        {
            return super.getBaseUpkeep();
        }
    }

    // Sky Cities has increased build cost
    @Override
    public float getBuildCost()
    {
        if(this.getCurrentName().equals("Sky Cities"))
        {
            return super.getBuildCost() * SKY_CITIES_BUILD_COST_MULTIPLIER;
        }
        else
        {
            return super.getBuildCost();
        }
    }

    @Override
    public boolean isAvailableToBuild()
    {
        MarketAPI market = this.market;

        if(!boggledTools.getBooleanSetting("boggledDomedCitiesEnabled") || !boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        //Can't build on stations
        if(boggledTools.marketIsStation(market))
        {
            return false;
        }

        // Meteor impacts preclude building except for Seafloor Cities
        if(!this.getCurrentName().equals("Seafloor Cities") && market.hasCondition("meteor_impacts"))
        {
            return false;
        }

        // Tectonic activity precludes building unless Harmonic Damper is built and functional or it's Sky Cities mode
        // There's no check to automatically remove Domed Cities if Harmonic Damper is deconstructed or disrupted.
        if(!this.getCurrentName().equals("Sky Cities") && market.hasCondition("extreme_tectonic_activity") && (market.getIndustry("BOGGLED_HARMONIC_DAMPER") == null || !market.getIndustry("BOGGLED_HARMONIC_DAMPER").isFunctional()))
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        MarketAPI market = this.market;

        if(!boggledTools.getBooleanSetting("boggledDomedCitiesEnabled") || !boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        if(boggledTools.marketIsStation(market))
        {
            return false;
        }

        return true;
    }

    @Override
    public String getUnavailableReason()
    {
        MarketAPI market = this.market;

        // Should never be seen because showWhenAvailable() will be false if either condition is true.
        if(!boggledTools.getBooleanSetting("boggledDomedCitiesEnabled") || !boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return "Error in getUnavailableReason() in Domed Cities. Please report this to boggled on the forums.";
        }

        // Should never be seen because showWhenAvailable() will be false if the market is a station.
        if(boggledTools.marketIsStation(market))
        {
            return "Error in getUnavailableReason() in Domed Cities. Please report this to boggled on the forums.";
        }

        // Meteor impacts preclude building except for Seafloor Cities
        if(!this.getCurrentName().equals("Seafloor Cities") && market.hasCondition("meteor_impacts"))
        {
            return market.getName() + " experiences frequent meteor impacts that could destroy megastructures. It would be too dangerous to construct one here.";
        }

        // Tectonic activity precludes building unless Harmonic Damper is built and functional or it's Sky Cities mode
        // There's no check to automatically remove Domed Cities if Harmonic Damper is deconstructed or disrupted.
        if(!this.getCurrentName().equals("Sky Cities") && market.hasCondition("extreme_tectonic_activity") && (market.getIndustry("BOGGLED_HARMONIC_DAMPER") == null || !market.getIndustry("BOGGLED_HARMONIC_DAMPER").isFunctional()))
        {
            return market.getName() + " experiences frequent seismic events that could destroy megastructures. It would be too dangerous to construct one here.";
        }

        return "Error in getUnavailableReason() in Domed Cities. Please report this to boggled on the forums.";
    }


    @Override
    public void applyAICoreToIncomeAndUpkeep()
    {
        //Prevents AI cores from modifying upkeep
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
            text.addPara(pre + "Increases stability by %s.", 0.0F, highlight, "3");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases stability by %s.", opad, highlight, "3");
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
            text.addPara(pre + "Increases stability by %s.", opad, highlight, "2");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases stability by %s.", opad, highlight, "2");
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
            text.addPara(pre + "Increases stability by %s.", opad, highlight, "1");
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases stability by %s.", opad, highlight, "1");
        }
    }

    @Override
    public String getCurrentImage()
    {
        if(this.getCurrentName().equals("Seafloor Cities"))
        {
            return Global.getSettings().getSpriteName("boggled", "seafloor_cities");
        }
        else if(this.getCurrentName().equals("Sky Cities"))
        {
            return Global.getSettings().getSpriteName("boggled", "sky_cities");
        }
        else
        {
            return this.getSpec().getImageName();
        }
    }

    @Override
    protected String getDescriptionOverride()
    {
        if(this.getCurrentName().equals("Seafloor Cities"))
        {
            return "It's not impossible to build a city at the bottom of the sea - in fact, it's advantageous when defending against raids and bombardment. Traders are hampered by the underwater conditions, making the colony less accessible.";

        }
        else if(this.getCurrentName().equals("Sky Cities"))
        {
            return "Placeholder sky cities description.";
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean canImprove()
    {
        return true;
    }

    @Override
    protected void applyImproveModifiers()
    {
        if (isImproved())
        {
            if(this.getCurrentName().equals("Seafloor Cities"))
            {
                market.getStability().modifyFlat("DOME_improve", IMPROVE_STABILITY_BONUS, getImprovementsDescForModifiers() + " (Seafloor cities)");
            }
            else if(this.getCurrentName().equals("Sky Cities"))
            {
                market.getStability().modifyFlat("DOME_improve", IMPROVE_STABILITY_BONUS, getImprovementsDescForModifiers() + " (Sky cities)");
            }
            else
            {
                market.getStability().modifyFlat("DOME_improve", IMPROVE_STABILITY_BONUS, getImprovementsDescForModifiers() + " (Domed cities)");
            }
        }
        else
        {
            market.getStability().unmodifyFlat("DOME_improve");
        }
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode)
    {
        float opad = 10f;
        Color highlight = Misc.getHighlightColor();

        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            info.addPara("Stability increased by %s.", 0f, highlight, "" + (int) IMPROVE_STABILITY_BONUS);
        }
        else
        {
            info.addPara("Increases stability by %s.", 0f, highlight, "" + (int) IMPROVE_STABILITY_BONUS);
        }

        info.addSpacer(opad);
        super.addImproveDesc(info, mode);
    }

    @Override
    public float getPatherInterest() { return super.getPatherInterest() + 2.0f; }

    public void modifyIncoming(MarketAPI market, PopulationComposition incoming)
    {
        incoming.getWeight().modifyFlat(getModId(), getImmigrationBonus(), Misc.ucFirst(this.getCurrentName().toLowerCase()));
    }

    protected float getImmigrationBonus()
    {
        return Math.max(0, market.getSize() - 1);
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        float opad = 10.0F;

        if(mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.QUEUED || !isFunctional())
        {
            tooltip.addPara("If operational, would counter the effects of:", opad, Misc.getHighlightColor(), "");
            int numCondsCountered = 0;
            for (String id : boggledTools.getDomedCitiesSuppressedConditions())
            {
                if(this.market.hasCondition(id))
                {
                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
                    tooltip.addPara("           %s", 2f, Misc.getHighlightColor(), condName);
                    numCondsCountered++;
                }
            }

            if(numCondsCountered == 0)
            {
                tooltip.addPara("           %s", 2f, Misc.getGrayColor(), "(none)");
            }
        }

        if(mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && isFunctional())
        {
            tooltip.addPara("Countering the effects of:", opad, Misc.getHighlightColor(), "");
            int numCondsCountered = 0;
            for (String id : boggledTools.getDomedCitiesSuppressedConditions())
            {
                if(this.market.hasCondition(id))
                {
                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
                    tooltip.addPara("           %s", 2f, Misc.getHighlightColor(), condName);
                    numCondsCountered++;
                }
            }

            if(numCondsCountered == 0)
            {
                tooltip.addPara("           %s", 2f, Misc.getGrayColor(), "(none)");
            }
        }

        if(isFunctional())
        {
            tooltip.addPara("%s population growth (based on colony size)", 10f, Misc.getHighlightColor(), "+" + (int) getImmigrationBonus());
        }

        if(this.getCurrentName().equals("Seafloor Cities"))
        {
            tooltip.addPara("Ground defense strength: %s", opad, Misc.getHighlightColor(), new String[]{"x" + DEFENSE_BONUS});
        }
        else
        {
            if(boggledTools.getBooleanSetting("boggledDomedCitiesDefensePenaltyEnabled"))
            {
                tooltip.addPara("Ground defense strength: %s", opad, Misc.getNegativeHighlightColor(), new String[]{"x" + DEFENSE_MALUS});
            }
        }

        if(this.getCurrentName().equals("Seafloor Cities"))
        {
            tooltip.addPara("Accessibility penalty: %s", opad, Misc.getNegativeHighlightColor(), "-10%");
        }
        else
        {
            tooltip.addPara("Accessibility bonus: %s", opad, Misc.getHighlightColor(), "10%");
        }
    }
}

