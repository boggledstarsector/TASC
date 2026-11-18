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
import boggled.campaign.econ.StringHelper;
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

    // Mode is told apart with skyCitiesName(), seafloorCitiesName(), and domedCitiesName().
    @Override
    public String getCurrentName() {
        if(boggledTools.getTascPlanetType(market.getPlanetEntity()).equals(boggledTools.TascPlanetTypes.gasGiantPlanetId))
        {
            return skyCitiesName();
        }
        else if(boggledTools.getTascPlanetType(market.getPlanetEntity()).equals(boggledTools.TascPlanetTypes.waterPlanetId))
        {
            return seafloorCitiesName();
        }
        else
        {
            return domedCitiesName();
        }
    }

    private String skyCitiesName() {
        return StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_name_sky");
    }

    private String seafloorCitiesName() {
        return StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_name_seafloor");
    }

    private String domedCitiesName() {
        return StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_name_domed");
    }

    // Only the base Domed Cities mode can be disrupted
    @Override
    public boolean canBeDisrupted()
    {
        return this.getCurrentName().equals(domedCitiesName());
    }

    @Override
    public void apply()
    {
        super.apply(true);

        // Sky Cities upkeep malus
        if(this.getCurrentName().equals(skyCitiesName()))
        {
            this.getUpkeep().modifyMultAlways("boggled_sky_cities", SKY_CITIES_UPKEEP_MULTIPLIER, StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_upkeep_multiplier"));
        }

        // Reduces ground defense in Domed Cities and Sky Cities modes, increases it in Seafloor Cities mode.
        // Always applies the bonus/malus, even if the building is disrupted
        if(this.getCurrentName().equals(seafloorCitiesName()))
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
                    this.market.getHazard().modifyFlat(this.getModId(), hazard, StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_modifier_seafloor"));
                }
                else
                {
                    market.suppressCondition(cid);
                }
            }

            // Reduces accessibility by 10% if in Seafloor Cities mode, boosts it by 10% if in Sky Cities mode
            if(this.getCurrentName().equals(seafloorCitiesName()))
            {
                this.market.getAccessibilityMod().modifyFlat(this.getModId(), ACCESSIBILITY_MALUS, StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_modifier_seafloor"));
            }
            else if(this.getCurrentName().equals(skyCitiesName()))
            {
                this.market.getAccessibilityMod().modifyFlat(this.getModId(), ACCESSIBILITY_BONUS, StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_modifier_sky"));
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
        if(this.getCurrentName().equals(skyCitiesName()))
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
        if(this.getCurrentName().equals(skyCitiesName()))
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
        if(!boggledTools.isBuildingResearchComplete(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledDomedCitiesEnabled") || !boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        if(!boggledTools.isBuildingResearchComplete(this.getId()))
        {
            return false;
        }

        //Can't build on stations
        if(boggledTools.marketIsStation(market))
        {
            return false;
        }

        // Meteor impacts preclude building except for Seafloor Cities
        if(!this.getCurrentName().equals(seafloorCitiesName()) && market.hasCondition("meteor_impacts"))
        {
            return false;
        }

        // Tectonic activity precludes building unless Harmonic Damper is built and functional or it's Sky Cities mode
        // There's no check to automatically remove Domed Cities if Harmonic Damper is deconstructed or disrupted.
        if(!this.getCurrentName().equals(skyCitiesName()) && market.hasCondition("extreme_tectonic_activity") && (market.getIndustry("BOGGLED_HARMONIC_DAMPER") == null || !market.getIndustry("BOGGLED_HARMONIC_DAMPER").isFunctional()))
        {
            return false;
        }

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        MarketAPI market = this.market;
        if(!boggledTools.isBuildingResearchComplete(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledDomedCitiesEnabled") || !boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        if(!boggledTools.isBuildingResearchComplete(this.getId()))
        {
            return false;
        }

        //Can't build on stations
        if(boggledTools.marketIsStation(market))
        {
            return false;
        }

        // Meteor impacts preclude building except for Seafloor Cities
        if(!this.getCurrentName().equals(seafloorCitiesName()) && market.hasCondition("meteor_impacts"))
        {
            return super.showWhenUnavailable();
        }

        // Tectonic activity precludes building unless Harmonic Damper is built and functional or it's Sky Cities mode
        // There's no check to automatically remove Domed Cities if Harmonic Damper is deconstructed or disrupted.
        if(!this.getCurrentName().equals(skyCitiesName()) && market.hasCondition("extreme_tectonic_activity") && (market.getIndustry("BOGGLED_HARMONIC_DAMPER") == null || !market.getIndustry("BOGGLED_HARMONIC_DAMPER").isFunctional()))
        {
            return super.showWhenUnavailable();
        }

        return super.showWhenUnavailable();
    }

    @Override
    public String getUnavailableReason()
    {
        MarketAPI market = this.market;

        // Meteor impacts preclude building except for Seafloor Cities
        if(!this.getCurrentName().equals(seafloorCitiesName()) && market.hasCondition("meteor_impacts"))
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_meteor_impacts", StringHelper.tokens("market", market.getName()));
        }

        // Tectonic activity precludes building unless Harmonic Damper is built and functional or it's Sky Cities mode
        // There's no check to automatically remove Domed Cities if Harmonic Damper is deconstructed or disrupted.
        if(!this.getCurrentName().equals(skyCitiesName()) && market.hasCondition("extreme_tectonic_activity") && (market.getIndustry("BOGGLED_HARMONIC_DAMPER") == null || !market.getIndustry("BOGGLED_HARMONIC_DAMPER").isFunctional()))
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_seismic_events", StringHelper.tokens("market", market.getName()));
        }

        return super.getUnavailableReason();
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
        String pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_alpha_assigned");
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = StringHelper.getString(StringHelper.INDUSTRIES, "ai_core_alpha_short");
        }

        String bonus = "3";
        String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_ai_increases_stability", StringHelper.tokens("prefix", pre, "bonus", bonus));
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

        String bonus = "2";
        String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_ai_increases_stability", StringHelper.tokens("prefix", pre, "bonus", bonus));
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

        String bonus = "1";
        String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_ai_increases_stability", StringHelper.tokens("prefix", pre, "bonus", bonus));
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
    public String getCurrentImage()
    {
        if(this.getCurrentName().equals(seafloorCitiesName()))
        {
            return Global.getSettings().getSpriteName("boggled", "seafloor_cities");
        }
        else if(this.getCurrentName().equals(skyCitiesName()))
        {
            return Global.getSettings().getSpriteName("boggled", "sky_cities");
        }
        else
        {
            String currentPlanetType = boggledTools.getTascPlanetType(this.market.getPlanetEntity());
            return switch (currentPlanetType) {
                // Terran is the default (will be shown in AotD VoK research menu)
                case boggledTools.TascPlanetTypes.terranPlanetId ->
                        Global.getSettings().getSpriteName("boggled", "domed_cities_terran");
                case boggledTools.TascPlanetTypes.barrenPlanetId ->
                        Global.getSettings().getSpriteName("boggled", "domed_cities_barren");
                case boggledTools.TascPlanetTypes.volcanicPlanetId ->
                        Global.getSettings().getSpriteName("boggled", "domed_cities_volcanic");
                case boggledTools.TascPlanetTypes.toxicPlanetId ->
                        Global.getSettings().getSpriteName("boggled", "domed_cities_toxic");
                case boggledTools.TascPlanetTypes.tundraPlanetId ->
                        Global.getSettings().getSpriteName("boggled", "domed_cities_tundra");
                case boggledTools.TascPlanetTypes.frozenPlanetId ->
                        Global.getSettings().getSpriteName("boggled", "domed_cities_frozen");
                case boggledTools.TascPlanetTypes.junglePlanetId ->
                        Global.getSettings().getSpriteName("boggled", "domed_cities_jungle");
                case boggledTools.TascPlanetTypes.desertPlanetId ->
                        Global.getSettings().getSpriteName("boggled", "domed_cities_desert");
                default -> this.getSpec().getImageName();
            };
        }
    }

    @Override
    protected String getDescriptionOverride()
    {
        if(this.getCurrentName().equals(seafloorCitiesName()))
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_desc_seafloor");
        }
        else if(this.getCurrentName().equals(skyCitiesName()))
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_desc_sky");
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
            if(this.getCurrentName().equals(seafloorCitiesName()))
            {
                market.getStability().modifyFlat("DOME_improve", IMPROVE_STABILITY_BONUS, StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_improve_seafloor", StringHelper.tokens("improvements", getImprovementsDescForModifiers())));
            }
            else if(this.getCurrentName().equals(skyCitiesName()))
            {
                market.getStability().modifyFlat("DOME_improve", IMPROVE_STABILITY_BONUS, StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_improve_sky", StringHelper.tokens("improvements", getImprovementsDescForModifiers())));
            }
            else
            {
                market.getStability().modifyFlat("DOME_improve", IMPROVE_STABILITY_BONUS, StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_improve_domed", StringHelper.tokens("improvements", getImprovementsDescForModifiers())));
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

        String bonus = "" + (int) IMPROVE_STABILITY_BONUS;
        if (mode == ImprovementDescriptionMode.INDUSTRY_TOOLTIP)
        {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_stability_increased", StringHelper.tokens("bonus", bonus));
            StringHelper.applyHighlight(info.addPara(sentence, 0f), highlight, bonus);
        }
        else
        {
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_increases_stability", StringHelper.tokens("bonus", bonus));
            StringHelper.applyHighlight(info.addPara(sentence, 0f), highlight, bonus);
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
        if(this.isFunctional())
        {
            return Math.max(0, market.getSize() - 1);
        }
        else
        {
            return 0.0f;
        }

    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        float opad = 10.0F;

        if(mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.QUEUED || !isFunctional())
        {
            tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_would_counter"), opad);
            int numCondsCountered = 0;
            for (String id : boggledTools.getDomedCitiesSuppressedConditions())
            {
                if(this.market.hasCondition(id))
                {
                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
                    String line = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_condition_line", StringHelper.tokens("condition", condName));
                    StringHelper.applyHighlight(tooltip.addPara(line, 2f), Misc.getHighlightColor(), condName);
                    numCondsCountered++;
                }
            }

            if(numCondsCountered == 0)
            {
                String none = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_none");
                String line = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_condition_line", StringHelper.tokens("condition", none));
                StringHelper.applyHighlight(tooltip.addPara(line, 2f), Misc.getGrayColor(), none);
            }
        }

        if(mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && isFunctional())
        {
            tooltip.addPara(StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_countering"), opad);
            int numCondsCountered = 0;
            for (String id : boggledTools.getDomedCitiesSuppressedConditions())
            {
                if(this.market.hasCondition(id))
                {
                    String condName = Global.getSettings().getMarketConditionSpec(id).getName();
                    String line = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_condition_line", StringHelper.tokens("condition", condName));
                    StringHelper.applyHighlight(tooltip.addPara(line, 2f), Misc.getHighlightColor(), condName);
                    numCondsCountered++;
                }
            }

            if(numCondsCountered == 0)
            {
                String none = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_none");
                String line = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_condition_line", StringHelper.tokens("condition", none));
                StringHelper.applyHighlight(tooltip.addPara(line, 2f), Misc.getGrayColor(), none);
            }
        }

        if(isFunctional())
        {
            String growth = "+" + (int) getImmigrationBonus();
            String growthSentence = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_population_growth", StringHelper.tokens("bonus", growth));
            StringHelper.applyHighlight(tooltip.addPara(growthSentence, 10f), Misc.getHighlightColor(), growth);
        }

        if(this.getCurrentName().equals(seafloorCitiesName()))
        {
            String defense = "x" + DEFENSE_BONUS;
            String defenseSentence = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_ground_defense", StringHelper.tokens("bonus", defense));
            StringHelper.applyHighlight(tooltip.addPara(defenseSentence, opad), Misc.getHighlightColor(), defense);
        }
        else
        {
            if(boggledTools.getBooleanSetting("boggledDomedCitiesDefensePenaltyEnabled"))
            {
                String defense = "x" + DEFENSE_MALUS;
                String defenseSentence = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_ground_defense", StringHelper.tokens("bonus", defense));
                StringHelper.applyHighlight(tooltip.addPara(defenseSentence, opad), Misc.getNegativeHighlightColor(), defense);
            }
        }

        if(this.getCurrentName().equals(seafloorCitiesName()))
        {
            String penalty = "-10%";
            String penaltySentence = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_accessibility_penalty", StringHelper.tokens("penalty", penalty));
            StringHelper.applyHighlight(tooltip.addPara(penaltySentence, opad), Misc.getNegativeHighlightColor(), penalty);
        }
        else if(this.getCurrentName().equals(skyCitiesName()))
        {
            String access = "10%";
            String accessSentence = StringHelper.getString(StringHelper.INDUSTRIES, "domed_cities_accessibility_bonus", StringHelper.tokens("bonus", access));
            StringHelper.applyHighlight(tooltip.addPara(accessSentence, opad), Misc.getHighlightColor(), access);
        }
    }
}

