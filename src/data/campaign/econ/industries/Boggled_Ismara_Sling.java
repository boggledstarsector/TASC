package data.campaign.econ.industries;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

public class Boggled_Ismara_Sling extends BaseIndustry
{
    private static BoggledCommonIndustry commonindustry;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        commonindustry = new BoggledCommonIndustry(data, "Ismara Sling");
    }

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
        if(!boggledTools.marketIsStation(this.market) && (!boggledTools.getPlanetType(this.market.getPlanetEntity()).equals(boggledTools.waterPlanetId) && !boggledTools.getPlanetType(this.market.getPlanetEntity()).equals(boggledTools.frozenPlanetId)))
        {
            // If an AI core is installed, put one in storage so the player doesn't "lose" an AI core
            if (this.aiCoreId != null)
            {
                CargoAPI cargo = this.market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();
                if (cargo != null)
                {
                    cargo.addCommodity(this.aiCoreId, 1.0F);
                }
            }

            if (this.market.hasIndustry(boggledTools.BoggledIndustries.ismaraSlingIndustryId))
            {
                // Pass in null for mode when calling this from API code.
                this.market.removeIndustry(boggledTools.BoggledIndustries.ismaraSlingIndustryId, null, false);
            }

            if (this.market.isPlayerOwned())
            {
                MessageIntel intel = new MessageIntel("Ismara's Sling on " + this.market.getName(), Misc.getBasePlayerColor());
                intel.addLine("    - Deconstructed");
                intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
            }
        }
    }

    public boolean slingHasShortage()
    {
        boolean shortage = false;
        Pair<String, Integer> deficit = this.getMaxDeficit(Commodities.HEAVY_MACHINERY);
        if(deficit.two != 0)
        {
            shortage = true;
        }

        return shortage;
    }

    @Override
    public String getCurrentName()
    {
        if(boggledTools.marketIsStation(this.market))
        {
            return "Asteroid Processing";
        }
        else
        {
            return "Ismara's Sling";
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
            return "Crashing asteroids rich in water-ice into planets is an effective means of terraforming - except when the asteroid is so large that the impact would be cataclysmic. In this case, the asteroid can be towed to a space station, where the water-ice is safely extracted and shipped to the destination planet. Can only help terraform worlds in the same system.";
        }
        else
        {
            return null;
        }
    }

    @Override
    public void apply()
    {
        super.apply(true);

        this.demand(Commodities.HEAVY_MACHINERY, 6);

        super.apply(false);
        super.applyIncomeAndUpkeep(3);
    }

    @Override
    public void unapply() {
        super.unapply();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        return commonindustry.isAvailableToBuild(getMarket());
//        if(!boggledTools.isResearched(this.getId()))
//        {
//            return false;
//        }
//
//        if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled))
//        {
//            return false;
//        }
//
//        if(boggledTools.marketIsStation(this.market))
//        {
//            return true;
//        }
//
//        return boggledTools.getPlanetType(this.market.getPlanetEntity()).equals(boggledTools.waterPlanetId) || boggledTools.getPlanetType(this.market.getPlanetEntity()).equals(boggledTools.frozenPlanetId);
    }

    @Override
    public boolean showWhenUnavailable()
    {
        return commonindustry.showWhenUnavailable(getMarket());
//        if(!boggledTools.isResearched(this.getId()))
//        {
//            return false;
//        }

//        return boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled);
    }

    @Override
    public String getUnavailableReason()
    {
        if(!boggledTools.getPlanetType(this.market.getPlanetEntity()).equals(boggledTools.waterPlanetId) && !boggledTools.getPlanetType(this.market.getPlanetEntity()).equals(boggledTools.frozenPlanetId))
        {
            return "Ismara's Sling can only be built on cryovolcanic, frozen and water-covered worlds.";
        }
        else
        {
            return "Error in getUnavailableReason() in the Ismara's Sling structure. Please tell Boggled about this on the forums.";
        }
    }

    @Override
    public float getPatherInterest() { return 10.0F; }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();

        if(boggledTools.marketIsStation(this.market))
        {
            tooltip.addPara("Asteroid Processing always demands %s heavy machinery regardless of market size.", opad, highlight, "6");
        }
        else
        {
            tooltip.addPara("Ismara's Sling always demands %s heavy machinery regardless of market size.", opad, highlight, "6");
        }
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

        if(slingHasShortage())
        {
            tooltip.addPara(this.getCurrentName() + " is experiencing a shortage of heavy machinery. No water-ice can be supplied for terraforming projects until the shortage is resolved.", bad, opad);
        }
    }
}
