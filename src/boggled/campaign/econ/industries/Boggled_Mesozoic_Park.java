package boggled.campaign.econ.industries;

import boggled.campaign.econ.boggledTools;
import boggled.campaign.econ.industries.interfaces.ShowBoggledTerraformingMenuOption;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.*;

public class Boggled_Mesozoic_Park extends BaseIndustry implements ShowBoggledTerraformingMenuOption
{
    //Need to update string in addImproveDesc if value changed
    private final float IMPROVE_BONUS = 1.40f;

    private final float ALPHA_CORE_BONUS = 1.40f;
    private final float BETA_CORE_BONUS = 1.20f;
    private final float GAMMA_CORE_BONUS = 1.10f;

    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        // This check exists to remove Mesozoic Park if the planet was terraformed to a type that is incompatible with Mesozoic Park
        PlanetAPI planet = this.market.getPlanetEntity();
        String planetType = boggledTools.getTascPlanetType(planet);
        if(!(planetType.equals(boggledTools.TascPlanetTypes.terranPlanetId) || planetType.equals(boggledTools.TascPlanetTypes.desertPlanetId) || planetType.equals(boggledTools.TascPlanetTypes.waterPlanetId) || planetType.equals(boggledTools.TascPlanetTypes.junglePlanetId)))
        {
            // If an AI core is installed, put one in storage so the player doesn't "lose" an AI core
            if(this.aiCoreId != null)
            {
                CargoAPI cargo = this.market.getSubmarket("storage").getCargo();
                if (cargo != null)
                {
                    cargo.addCommodity(this.aiCoreId, 1.0F);
                }
            }

            if(this.market.isPlayerOwned())
            {
                MessageIntel intel = new MessageIntel("Mesozoic Park on " + this.market.getName(), Misc.getBasePlayerColor());
                intel.addLine("    - Closed");
                intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
            }

            if(this.market.hasIndustry("BOGGLED_MESOZOIC_PARK"))
            {
                // Pass in null for mode when calling this from API code.
                this.market.removeIndustry("BOGGLED_MESOZOIC_PARK", (MarketAPI.MarketInteractionMode)null, false);
            }
        }
    }

    @Override
    public void apply()
    {
        super.apply(true);
    }

    @Override
    protected void buildingFinished()
    {
        super.buildingFinished();

        boggledTools.addCondition(this.market, Conditions.INIMICAL_BIOSPHERE);
    }

    @Override
    public String getCurrentImage()
    {
        //Can't build on stations, return default image
        if(boggledTools.marketIsStation(this.market))
        {
            return this.getSpec().getImageName();
        }

        PlanetAPI planet = this.market.getPlanetEntity();
        String planetType = boggledTools.getTascPlanetType(planet);

        //Can only build on terran, water, jungle or desert planets
        if(planetType.equals(boggledTools.TascPlanetTypes.terranPlanetId))
        {
            return Global.getSettings().getSpriteName("boggled", "mesozoic_park_terran");
        }
        else if(planetType.equals(boggledTools.TascPlanetTypes.waterPlanetId))
        {
            return Global.getSettings().getSpriteName("boggled", "mesozoic_park_water");
        }
        else if(planetType.equals(boggledTools.TascPlanetTypes.junglePlanetId))
        {
            return Global.getSettings().getSpriteName("boggled", "mesozoic_park_jungle");
        }
        else if(planetType.equals(boggledTools.TascPlanetTypes.desertPlanetId))
        {
            return Global.getSettings().getSpriteName("boggled", "mesozoic_park_desert");
        }

        return this.getSpec().getImageName();
    }

    @Override
    public void unapply()
    {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isResearched("tasc_genetic_manipulation"))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") || !boggledTools.getBooleanSetting("boggledMesozoicParkEnabled"))
        {
            return false;
        }

        //Can't build on stations
        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        PlanetAPI planet = this.market.getPlanetEntity();
        String planetType = boggledTools.getTascPlanetType(planet);

        //Can't build on unknown planet types
        if(planetType.equals(boggledTools.TascPlanetTypes.unknownPlanetId))
        {
            return false;
        }

        //Can only build on terran, water, jungle or desert planets
        if(!planetType.equals(boggledTools.TascPlanetTypes.terranPlanetId) && !planetType.equals(boggledTools.TascPlanetTypes.waterPlanetId) && !planetType.equals(boggledTools.TascPlanetTypes.junglePlanetId) && !planetType.equals(boggledTools.TascPlanetTypes.desertPlanetId))
        {
            return false;
        }

        //Market must be habitable
        if(!this.market.hasCondition("habitable"))
        {
            return false;
        }

        //Certain market conditions preclude building
        if(this.market.hasCondition("no_atmosphere") || this.market.hasCondition("thin_atmosphere") || this.market.hasCondition("dense_atmosphere") || this.market.hasCondition("toxic_atmosphere") || this.market.hasCondition("irradiated"))
        {
            return false;
        }

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.isResearched("tasc_genetic_manipulation"))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") || !boggledTools.getBooleanSetting("boggledMesozoicParkEnabled"))
        {
            return false;
        }

        //Can't build on stations
        if(boggledTools.marketIsStation(this.market))
        {
            return false;
        }

        PlanetAPI planet = this.market.getPlanetEntity();
        String planetType = boggledTools.getTascPlanetType(planet);

        //Can't build on unknown planet types
        if(planetType.equals(boggledTools.TascPlanetTypes.unknownPlanetId))
        {
            return super.showWhenUnavailable();
        }

        //Can only build on terran, water, jungle or desert planets
        if(!planetType.equals(boggledTools.TascPlanetTypes.terranPlanetId) && !planetType.equals(boggledTools.TascPlanetTypes.waterPlanetId) && !planetType.equals(boggledTools.TascPlanetTypes.junglePlanetId) && !planetType.equals(boggledTools.TascPlanetTypes.desertPlanetId))
        {
            return super.showWhenUnavailable();
        }

        //Market must be habitable
        if(!this.market.hasCondition("habitable"))
        {
            return super.showWhenUnavailable();
        }

        //Certain market conditions preclude building
        if(this.market.hasCondition("no_atmosphere") || this.market.hasCondition("thin_atmosphere") || this.market.hasCondition("dense_atmosphere") || this.market.hasCondition("toxic_atmosphere") || this.market.hasCondition("irradiated"))
        {
            return super.showWhenUnavailable();
        }

        return super.showWhenUnavailable();
    }

    @Override
    public String getUnavailableReason()
    {
        PlanetAPI planet = this.market.getPlanetEntity();
        String planetType = boggledTools.getTascPlanetType(planet);

        //Can't build on unknown planet types
        if(planetType.equals(boggledTools.TascPlanetTypes.unknownPlanetId))
        {
            return "This planet type is unsupported by TASC. Please report this to boggled on the forums so he can add support. The planet type is: " + this.market.getPlanetEntity().getTypeId();
        }

        //Can only build on terran, water, jungle or desert planets
        if(!planetType.equals(boggledTools.TascPlanetTypes.terranPlanetId) && !planetType.equals(boggledTools.TascPlanetTypes.waterPlanetId) && !planetType.equals(boggledTools.TascPlanetTypes.junglePlanetId) && !planetType.equals(boggledTools.TascPlanetTypes.desertPlanetId))
        {
            return "Old Earth megafauna can only survive on world types that feature a relatively similar environment to Old Earth during the Mesozoic Era.";
        }

        //Market must be habitable
        if(!this.market.hasCondition("habitable"))
        {
            return "Old Earth megafauna can only survive on worlds with habitable surface conditions.";
        }

        //Certain market conditions preclude building
        if(this.market.hasCondition("no_atmosphere") || this.market.hasCondition("thin_atmosphere"))
        {
            return "Surface conditions on " + this.market.getName() + " are unsuitable for Old Earth megafauna due to insufficient atmospheric pressure.";
        }

        //Certain market conditions preclude building
        if(this.market.hasCondition("dense_atmosphere"))
        {
            return "Surface conditions on " + this.market.getName() + " are unsuitable for Old Earth megafauna due to excessive atmospheric pressure.";
        }

        //Certain market conditions preclude building
        if(this.market.hasCondition("toxic_atmosphere") || this.market.hasCondition("irradiated"))
        {
            return "Surface conditions on " + this.market.getName() + " are unsuitable for Old Earth megafauna due to atmospheric toxicity.";
        }

        return super.getUnavailableReason();
    }


    @Override
    public void applyAICoreToIncomeAndUpkeep()
    {
        // Also handles the accessibility and stability modifiers
        // This will apply even if no AI core is installed

        float access_mult = (float)Math.round(market.getAccessibilityMod().computeEffective(0.0F) * 100.0F) / 100.0F;
        String access = "Accessibility";
        this.getIncome().modifyMult("ind_mesopark_access", access_mult, access);

        float stability_mult = this.market.getStabilityValue() / 10.0F;
        String stability = "Stability";
        this.getIncome().modifyMult("ind_mesopark_stability", stability_mult, stability);

        if(this.aiCoreId != null)
        {
            String name = "AI Core assigned";
            if(this.aiCoreId.equals("alpha_core"))
            {
                name = "Alpha Core assigned";
                this.getIncome().modifyMult("ind_core", ALPHA_CORE_BONUS, name);
            }
            else if(this.aiCoreId.equals("beta_core"))
            {
                name = "Beta Core assigned";
                this.getIncome().modifyMult("ind_core", BETA_CORE_BONUS, name);
            }
            else if(this.aiCoreId.equals("gamma_core"))
            {
                name = "Gamma Core assigned";
                this.getIncome().modifyMult("ind_core", GAMMA_CORE_BONUS, name);
            }
        }
        else
        {
            this.getUpkeep().unmodifyMult("ind_core");
        }
    }

    @Override
    public void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String bonus = (int) Math.round((ALPHA_CORE_BONUS - 1.0f) * 100) + "%";
        String pre = "Alpha-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Alpha-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases income by %s.", 0.0F, highlight, bonus);
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases income by %s.", opad, highlight, bonus);
        }
    }

    @Override
    public void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String bonus = (int) Math.round((BETA_CORE_BONUS - 1.0f) * 100) + "%";
        String pre = "Beta-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Beta-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases income by %s.", opad, highlight, bonus);
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases income by %s.", opad, highlight, bonus);
        }
    }

    @Override
    public void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String bonus = (int) Math.round((GAMMA_CORE_BONUS - 1.0f) * 100) + "%";
        String pre = "Gamma-level AI core currently assigned. ";
        if (mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            pre = "Gamma-level AI core. ";
        }

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(this.aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + "Increases income by %s.", opad, highlight, bonus);
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + "Increases income by %s.", opad, highlight, bonus);
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

            if (!this.isFunctional())
            {
                this.unapply();
            }
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
        return super.getPatherInterest() + 2f;
    }
}

