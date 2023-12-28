package data.campaign.econ.industries;

import java.util.*;
import java.lang.String;
import java.util.List;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

public class Boggled_Stellar_Reflector_Array extends BaseIndustry
{
    private static BoggledCommonIndustry sharedIndustry;
    private BoggledCommonIndustry thisIndustry;

    public static void settingsFromJSON(JSONObject data) throws JSONException {
        sharedIndustry = new BoggledCommonIndustry(data);
    }

    public Boggled_Stellar_Reflector_Array() {
        super();
        thisIndustry = new BoggledCommonIndustry(sharedIndustry);
    }

    // The "solar_array" condition handles suppressing hot, cold and poor light conditions.
    // This is here merely to allow the tooltip to say which conditions are/would be suppressed.
    public static List<String> SUPPRESSED_CONDITIONS = new ArrayList<String>();
    static
    {
        SUPPRESSED_CONDITIONS.add(Conditions.HOT);
        SUPPRESSED_CONDITIONS.add(Conditions.COLD);
        SUPPRESSED_CONDITIONS.add(Conditions.POOR_LIGHT);
    }

    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void apply()
    {
        super.apply(true);

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            this.demand(boggledTools.BoggledCommodities.domainArtifacts, 1);
        }

        boolean shortage = false;
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{boggledTools.BoggledCommodities.domainArtifacts});
            if(deficit.two != 0)
            {
                shortage = true;
            }
        }

        if(shortage)
        {
            getUpkeep().modifyMult("deficit", 5.0f, "Artifacts shortage");
        }
        else
        {
            getUpkeep().unmodifyMult("deficit");
        }
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        // The "solar_array" condition handles suppressing hot, cold and poor light conditions.

        // If this building gets disrupted, it removes the solar_array condition. I'm not sure why I originally
        // coded it this way instead of just checking if the building is disrupted, if yes, remove solar_array condition, if
        // not disrupted, add solar_array condition if not already present.
        // Seems to work fine as-is, so I'm not going to spend time changing it unless a bug is found.
        if(!this.market.hasCondition(Conditions.SOLAR_ARRAY) && this.isFunctional())
        {
            Random random = new Random();
            float minDays = 210f;
            float maxDays = 270f;

            float disruptionDur = minDays + random.nextFloat() * (maxDays - minDays);
            this.setDisrupted(disruptionDur, true);
        }
    }

    @Override
    protected void notifyDisrupted()
    {
        boggledTools.removeCondition(this.market, Conditions.SOLAR_ARRAY);
    }

    @Override
    protected void disruptionFinished()
    {
        boggledTools.addCondition(this.market, Conditions.SOLAR_ARRAY);
    }

    @Override
    protected void buildingFinished()
    {
        super.buildingFinished();

        MarketAPI market = this.market;
        boggledTools.addCondition(market, Conditions.SOLAR_ARRAY);

        if(boggledTools.numReflectorsInOrbit(market) >= 3)
        {
            return;
        }

        boolean mirrorsOrShades = boggledTools.getCreateMirrorsOrShades(market);
        boggledTools.clearReflectorsInOrbit(market);

        //True is mirrors, false is shades
        if(mirrorsOrShades)
        {
            SectorEntityToken orbitFocus = market.getPrimaryEntity().getOrbitFocus();
            float orbitRadius = market.getPrimaryEntity().getRadius() + 80.0F;
            if (orbitFocus != null && orbitFocus.isStar())
            {
                // Create the mirrors in a 90 degree arc opposite the star to suggest that light is being reflected onto the surface.
                // The orbital period of the mirrors is the same as the planet's orbital period around the star - this ensures the mirrors are always
                // in the same position on the far side of the planet relative to the star.

                StarSystemAPI system = market.getStarSystem();

                //First mirror
                SectorEntityToken mirrorAlpha = system.addCustomEntity("stellar_mirror_alpha", ("Stellar Mirror Alpha"), "stellar_mirror", market.getFactionId());
                mirrorAlpha.setCircularOrbitPointingDown(market.getPrimaryEntity(), market.getPrimaryEntity().getCircularOrbitAngle() - 30, orbitRadius, market.getPrimaryEntity().getCircularOrbitPeriod());
                mirrorAlpha.setCustomDescriptionId("stellar_mirror");

                //Second mirror
                SectorEntityToken mirrorBeta = system.addCustomEntity("stellar_mirror_beta", ("Stellar Mirror Beta"), "stellar_mirror", market.getFactionId());
                mirrorBeta.setCircularOrbitPointingDown(market.getPrimaryEntity(), market.getPrimaryEntity().getCircularOrbitAngle(), orbitRadius, market.getPrimaryEntity().getCircularOrbitPeriod());
                mirrorBeta.setCustomDescriptionId("stellar_mirror");

                //Third mirror
                SectorEntityToken mirrorGamma = system.addCustomEntity("stellar_mirror_gamma", ("Stellar Mirror Gamma"), "stellar_mirror", market.getFactionId());
                mirrorGamma.setCircularOrbitPointingDown(market.getPrimaryEntity(), market.getPrimaryEntity().getCircularOrbitAngle() + 30, orbitRadius, market.getPrimaryEntity().getCircularOrbitPeriod());
                mirrorGamma.setCustomDescriptionId("stellar_mirror");
            }
            else
            {
                // If the planet isn't orbiting a star, set up the mirrors at 120 degree intervals around the planet.

                StarSystemAPI system = market.getStarSystem();

                //First mirror
                SectorEntityToken mirrorAlpha = system.addCustomEntity("stellar_mirror_alpha", ("Stellar Mirror Alpha"), "stellar_mirror", market.getFactionId());
                mirrorAlpha.setCircularOrbitPointingDown(market.getPrimaryEntity(), 0, orbitRadius, orbitRadius / 10.0F);
                mirrorAlpha.setCustomDescriptionId("stellar_mirror");

                //Second mirror
                SectorEntityToken mirrorBeta = system.addCustomEntity("stellar_mirror_beta", ("Stellar Mirror Beta"), "stellar_mirror", market.getFactionId());
                mirrorBeta.setCircularOrbitPointingDown(market.getPrimaryEntity(), 120, orbitRadius, orbitRadius / 10.0F);
                mirrorBeta.setCustomDescriptionId("stellar_mirror");

                //Third mirror
                SectorEntityToken mirrorGamma = system.addCustomEntity("stellar_mirror_gamma", ("Stellar Mirror Gamma"), "stellar_mirror", market.getFactionId());
                mirrorGamma.setCircularOrbitPointingDown(market.getPrimaryEntity(), 240, orbitRadius, orbitRadius / 10.0F);
                mirrorGamma.setCustomDescriptionId("stellar_mirror");
            }
        }
        else
        {
            SectorEntityToken orbitFocus = market.getPrimaryEntity().getOrbitFocus();
            float orbitRadius = market.getPrimaryEntity().getRadius() + 80.0F;
            if (orbitFocus != null && orbitFocus.isStar())
            {
                // Same as above, except the shades are between the planet and the star to suggest that they're blocking some light. Uses
                // a slightly tighter arc than above, which I think I copied from the existing solar shade arrays in vanilla.

                StarSystemAPI system = market.getStarSystem();

                //First shade
                SectorEntityToken shadeAlpha = system.addCustomEntity("stellar_shade_alpha", ("Stellar Shade Alpha"), "stellar_shade", market.getFactionId());
                shadeAlpha.setCircularOrbitPointingDown(market.getPrimaryEntity(), market.getPrimaryEntity().getCircularOrbitAngle() + 154, orbitRadius, market.getPrimaryEntity().getCircularOrbitPeriod());
                shadeAlpha.setCustomDescriptionId("stellar_shade");

                //Second shade
                SectorEntityToken shadeBeta = system.addCustomEntity("stellar_shade_beta", ("Stellar Shade Beta"), "stellar_shade", market.getFactionId());
                shadeBeta.setCircularOrbitPointingDown(market.getPrimaryEntity(), market.getPrimaryEntity().getCircularOrbitAngle() + 180, orbitRadius, market.getPrimaryEntity().getCircularOrbitPeriod());
                shadeBeta.setCustomDescriptionId("stellar_shade");

                //Third shade
                SectorEntityToken shadeGamma = system.addCustomEntity("stellar_shade_gamma", ("Stellar Shade Gamma"), "stellar_shade", market.getFactionId());
                shadeGamma.setCircularOrbitPointingDown(market.getPrimaryEntity(), market.getPrimaryEntity().getCircularOrbitAngle() + 206, orbitRadius, market.getPrimaryEntity().getCircularOrbitPeriod());
                shadeGamma.setCustomDescriptionId("stellar_shade");
            }
            else
            {
                // Same as above for a planet not orbiting a star, except with shades instead of mirrors.

                StarSystemAPI system = market.getStarSystem();

                //First shade
                SectorEntityToken shadeAlpha = system.addCustomEntity("stellar_shade_alpha", ("Stellar Shade Alpha"), "stellar_shade", market.getFactionId());
                shadeAlpha.setCircularOrbitPointingDown(market.getPrimaryEntity(), 0, orbitRadius, orbitRadius / 10.0F);
                shadeAlpha.setCustomDescriptionId("stellar_shade");

                //Second shade
                SectorEntityToken shadeBeta = system.addCustomEntity("stellar_shade_beta", ("Stellar Shade Beta"), "stellar_shade", market.getFactionId());
                shadeBeta.setCircularOrbitPointingDown(market.getPrimaryEntity(), 120, orbitRadius, orbitRadius / 10.0F);
                shadeBeta.setCustomDescriptionId("stellar_shade");

                //Third shade
                SectorEntityToken shadeGamma = system.addCustomEntity("stellar_shade_gamma", ("Stellar Shade Gamma"), "stellar_shade", market.getFactionId());
                shadeGamma.setCircularOrbitPointingDown(market.getPrimaryEntity(), 240, orbitRadius, orbitRadius / 10.0F);
                shadeGamma.setCustomDescriptionId("stellar_shade");
            }
        }
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        super.notifyBeingRemoved(mode, forUpgrade);

        boggledTools.clearReflectorsInOrbit(this.market);
        boggledTools.removeCondition(market, Conditions.SOLAR_ARRAY);
    }

    @Override
    public String getCurrentImage()
    {
        if(!boggledTools.getCreateMirrorsOrShades(this.market))
        {
            return Global.getSettings().getSpriteName("boggled", "stellar_shade");
        }
        else
        {
            return Global.getSettings().getSpriteName("boggled", "stellar_mirror");
        }
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        float opad = 10.0F;

        if(mode == IndustryTooltipMode.ADD_INDUSTRY || mode == IndustryTooltipMode.QUEUED || !isFunctional())
        {
            tooltip.addPara("If operational, would counter the effects of:", opad, Misc.getHighlightColor(), "");
            int numCondsCountered = 0;
            for (String id : SUPPRESSED_CONDITIONS)
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
            tooltip.addPara("Countering the effects of:", opad);
            int numCondsCountered = 0;
            for (String id : SUPPRESSED_CONDITIONS)
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
    }

    @Override
    public boolean isAvailableToBuild() { return thisIndustry.isAvailableToBuild(getMarket()); }

    @Override
    public boolean showWhenUnavailable() { return thisIndustry.showWhenUnavailable(getMarket()); }

    @Override
    public String getUnavailableReason() { return thisIndustry.getUnavailableReason(getMarket()); }

    @Override
    public float getPatherInterest()
    {
        // Doesn't increase Pather interest on planets not owned by the player - I did this because by default Eochu Bres and
        // Eventide will have this industry at the start of the game. I don't want Pather cells to appear on those planets, since
        // they are not present in vanilla and this may cause unintended consequences (ex. planet killer detonated on one of those planets)
        if(this.market.isPlayerOwned())
        {
            return super.getPatherInterest() + 2f;
        }
        else
        {
            // Will return zero because NPC planets don't install AI cores without player intervention.
            return super.getPatherInterest();
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

