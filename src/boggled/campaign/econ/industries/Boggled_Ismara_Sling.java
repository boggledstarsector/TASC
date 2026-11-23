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
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;

public class Boggled_Ismara_Sling extends BaseIndustry implements ShowBoggledTerraformingMenuOption
{
    private final int STATIC_HEAVY_MACHINERY_DEMAND = 6;

    @Override
    public boolean canBeDisrupted()
    {
        return true;
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        // This check exists to remove Ismara's Sling if the planet was terraformed to a type that is incompatible with it.
        if(!boggledTools.marketIsStation(this.market) && (!boggledTools.getTascPlanetType(this.market.getPlanetEntity()).equals(boggledTools.TascPlanetTypes.waterPlanetId) && !boggledTools.getTascPlanetType(this.market.getPlanetEntity()).equals(boggledTools.TascPlanetTypes.frozenPlanetId)))
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
                MessageIntel intel = new MessageIntel(StringHelper.getString(StringHelper.INDUSTRIES, "ismara_intel_title", StringHelper.tokens("market", this.market.getName())), Misc.getBasePlayerColor());
                intel.addLine(StringHelper.getString(StringHelper.INDUSTRIES, "ismara_intel_deconstructed"));
                intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
            }

            if(this.market.hasIndustry("BOGGLED_ISMARA_SLING"))
            {
                // Pass in null for mode when calling this from API code.
                this.market.removeIndustry("BOGGLED_ISMARA_SLING", (MarketAPI.MarketInteractionMode)null, false);
            }
        }
    }
    public Pair<String, Integer> getIsmaraSlingDeficit()
    {
        ArrayList<String> deficitCommodities = new ArrayList<>();
        deficitCommodities.add("heavy_machinery");

        return this.getMaxDeficit(deficitCommodities.toArray(new String[0]));
    }

    public boolean ismaraSlingHasShortage()
    {
        return this.getIsmaraSlingDeficit().two > 0;
    }

    @Override
    public String getCurrentName()
    {
        if(boggledTools.marketIsStation(this.market))
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "ismara_name_asteroid_processing");
        }
        else
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "ismara_name");
        }
    }

    @Override
    public String getCurrentImage()
    {
        if(boggledTools.marketIsStation(this.market))
        {
            return Global.getSettings().getSpriteName("boggled", "asteroid_processing");
        }
        else
        {
            return this.getSpec().getImageName();
        }
    }

    @Override
    protected String getDescriptionOverride()
    {
        if(boggledTools.marketIsStation(this.market))
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "ismara_description_station");
        }
        else
        {
            return null;
        }
    }

    @Override
    public void apply()
    {
        super.apply(false);
        super.applyIncomeAndUpkeep(3);

        this.demand("heavy_machinery", STATIC_HEAVY_MACHINERY_DEMAND);
    }

    @Override
    public void unapply() {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isBuildingResearchComplete(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled) || !boggledTools.getBooleanSetting(boggledTools.BoggledSettings.ismaraSlingEnabled))
        {
            return false;
        }

        if(boggledTools.marketIsStation(this.market))
        {
            return super.isAvailableToBuild();
        }

        if(!boggledTools.getTascPlanetType(this.market.getPlanetEntity()).equals(boggledTools.TascPlanetTypes.waterPlanetId) && !boggledTools.getTascPlanetType(this.market.getPlanetEntity()).equals(boggledTools.TascPlanetTypes.frozenPlanetId))
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

        if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled) || !boggledTools.getBooleanSetting(boggledTools.BoggledSettings.ismaraSlingEnabled))
        {
            return false;
        }

        if(boggledTools.marketIsStation(this.market))
        {
            return super.showWhenUnavailable();
        }

        if(!boggledTools.getTascPlanetType(this.market.getPlanetEntity()).equals(boggledTools.TascPlanetTypes.waterPlanetId) && !boggledTools.getTascPlanetType(this.market.getPlanetEntity()).equals(boggledTools.TascPlanetTypes.frozenPlanetId))
        {
            return super.showWhenUnavailable();
        }

        return super.showWhenUnavailable();
    }

    @Override
    public String getUnavailableReason()
    {
        if(boggledTools.marketIsStation(this.market))
        {
            return super.getUnavailableReason();
        }

        if(!boggledTools.getTascPlanetType(this.market.getPlanetEntity()).equals(boggledTools.TascPlanetTypes.waterPlanetId) && !boggledTools.getTascPlanetType(this.market.getPlanetEntity()).equals(boggledTools.TascPlanetTypes.frozenPlanetId))
        {
            return StringHelper.getString(StringHelper.INDUSTRIES, "ismara_unavailable_planet");
        }

        return super.getUnavailableReason();
    }

    @Override
    public float getPatherInterest() { return 2.0F; }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();

        String demand = "" + STATIC_HEAVY_MACHINERY_DEMAND;
        String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "ismara_demand", StringHelper.tokens("name", this.getCurrentName(), "demand", demand));
        StringHelper.applyHighlight(tooltip.addPara(sentence, opad), highlight, demand);
    }

    @Override
    public boolean canInstallAICores() {
        return false;
    }

    @Override
    public boolean canImprove() { return false; }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color bad = Misc.getNegativeHighlightColor();

        Pair<String, Integer> deficit = getIsmaraSlingDeficit();
        if(deficit.two > 0)
        {
            String commodity = boggledTools.getCommidityNameFromId(deficit.one);
            String sentence = StringHelper.getString(StringHelper.INDUSTRIES, "ismara_shortage", StringHelper.tokens("name", this.getCurrentName(), "commodity", commodity));
            StringHelper.applyHighlight(tooltip.addPara(sentence, opad), bad, commodity);
        }
    }
}
