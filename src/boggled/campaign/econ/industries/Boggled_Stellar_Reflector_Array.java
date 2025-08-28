package boggled.campaign.econ.industries;

import java.util.*;
import java.lang.String;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.util.Pair;
import boggled.campaign.econ.boggledTools;

public class Boggled_Stellar_Reflector_Array extends BaseIndustry
{
    public static String aotdVokKey = "tasc_light_manipulation";

    @Override
    public boolean canBeDisrupted() {
        return true;
    }

    @Override
    public void apply()
    {
        super.apply(true);

        if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled"))
        {
            this.demand("domain_artifacts", 1);
        }

        boolean shortage = false;
        if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled"))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"domain_artifacts"});
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
        if(!this.isFunctional() && this.market.hasCondition("solar_array"))
        {
            boggledTools.removeCondition(this.market, "solar_array");
        }
        else if(this.isFunctional() && !this.market.hasCondition("solar_array"))
        {
            boggledTools.addCondition(this.market, "solar_array");
        }
    }

    private void createMirrorsOrShades(MarketAPI market)
    {
        if(boggledTools.numReflectorsInOrbit(market) >= 3)
        {
            return;
        }

        boggledTools.clearReflectorsInOrbit(market);

        //True is mirrors, false is shades
        boolean mirrorsOrShades = boggledTools.getCreateMirrorsOrShades(market);
        StarSystemAPI system = market.getStarSystem();

        ArrayList<Pair<String, String>> mirrorIdNamePairs = new ArrayList<>(Arrays.asList(
                new Pair<>("stellar_mirror_alpha", "Stellar Mirror Alpha"),
                new Pair<>("stellar_mirror_beta", "Stellar Mirror Beta"),
                new Pair<>("stellar_mirror_gamma", "Stellar Mirror Gamma")
        ));

        ArrayList<Pair<String, String>> shadeIdNamePairs = new ArrayList<>(Arrays.asList(
                new Pair<>("stellar_shade_alpha", "Stellar Shade Alpha"),
                new Pair<>("stellar_shade_beta", "Stellar Shade Beta"),
                new Pair<>("stellar_shade_gamma", "Stellar Shade Gamma")
        ));

        float baseAngle = market.getPrimaryEntity().getCircularOrbitAngle();
        ArrayList<Float> mirrorAnglesOrbitingStar = new ArrayList<>(Arrays.asList(
                baseAngle - 30,
                baseAngle,
                baseAngle + 30
        ));

        ArrayList<Float> shadeAnglesOrbitingStar = new ArrayList<>(Arrays.asList(
                baseAngle + 154,
                baseAngle + 180,
                baseAngle + 206
        ));

        ArrayList<Float> mirrorAndShadeAnglesOrbitingNotStar = new ArrayList<>(Arrays.asList(
                0f,
                120f,
                240f
        ));

        // Orbit Radius
        float orbitRadius = market.getPrimaryEntity().getRadius() + 80f;

        // Orbital period
        SectorEntityToken orbitFocus = market.getPrimaryEntity().getOrbitFocus();
        // Default period (cases where market is a moon orbiting a planet or market is in a nebula where it orbits nothing)
        // Based on the orbital period of the stellar mirrors around Eochu Bres in vanilla
        float orbitPeriod = 40f;
        // Match the period of the market to ensure the reflectors maintain their orientation relative to the star
        if(orbitFocus != null && orbitFocus.isStar())
        {
            orbitPeriod = market.getPrimaryEntity().getCircularOrbitPeriod();
        }

        // Reflector spawn angles
        // Default to star-relative angles
        ArrayList<Float> orbitAngles = mirrorsOrShades ? mirrorAnglesOrbitingStar : shadeAnglesOrbitingStar;
        // Switch to 120 degree intervals if the market is not orbiting a star
        if(orbitFocus == null || !orbitFocus.isStar())
        {
            orbitAngles = mirrorAndShadeAnglesOrbitingNotStar;
        }

        // Determine name and description for the reflectors
        ArrayList<Pair<String, String>> idNamePairs = mirrorsOrShades ? mirrorIdNamePairs : shadeIdNamePairs;
        String entityType = mirrorsOrShades ? "stellar_mirror" : "stellar_shade";
        String customDescriptionId = mirrorsOrShades ? "stellar_mirror" : "stellar_shade";

        for (int i = 0; i < 3; ++i) {
            SectorEntityToken reflector = system.addCustomEntity(idNamePairs.get(i).one, idNamePairs.get(i).two, entityType, market.getFactionId());
            reflector.setCircularOrbitPointingDown(market.getPrimaryEntity(), orbitAngles.get(i), orbitRadius, orbitPeriod);
            reflector.setCustomDescriptionId(customDescriptionId);
        }
    }

    @Override
    protected void buildingFinished()
    {
        super.buildingFinished();

        boggledTools.addCondition(this.market, "solar_array");

        createMirrorsOrShades(this.market);
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        super.notifyBeingRemoved(mode, forUpgrade);

        boggledTools.clearReflectorsInOrbit(this.market);
        boggledTools.removeCondition(market, "solar_array");

        // Removing the solar array condition fails to unsuppress conditions, I'm not sure why
        for (String cid : boggledTools.getStellarReflectorArraySuppressedConditions())
        {
            this.market.unsuppressCondition(cid);
        }
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
            for (String id : boggledTools.getStellarReflectorArraySuppressedConditions())
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
            for (String id : boggledTools.getStellarReflectorArraySuppressedConditions())
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
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isResearched(aotdVokKey))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") || !boggledTools.getBooleanSetting("boggledStellarReflectorArrayEnabled"))
        {
            return false;
        }

        //Can't be built by station markets
        if(boggledTools.marketIsStation(this.market)) { return false; }

        //Can't be built on dark planets. All planets in nebulas and orbiting black holes have dark condition.
        if(this.market.hasCondition("dark")) { return false; }

        return true;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.isResearched(aotdVokKey))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledTerraformingContentEnabled") || !boggledTools.getBooleanSetting("boggledStellarReflectorArrayEnabled"))
        {
            return false;
        }

        //Can't be built by station markets
        if(boggledTools.marketIsStation(this.market)) { return false; }

        return true;
    }

    @Override
    public String getUnavailableReason()
    {
        //Can't be built on dark planets. All planets in nebulas and orbiting black holes have dark condition.
        if(this.market.hasCondition("dark")) { return "Stellar reflectors won't have any effect on a world that receives no light."; }

        return "Error in getUnavailableReason() in the Stellar Reflector Array. Please tell Boggled about this on the forums.";
    }

    @Override
    public float getPatherInterest()
    {
        // Doesn't increase Pather interest on planets not owned by the player - I did this because by default Eochu Bres and
        // Eventide will have this industry at the start of the game. I don't want Pather cells to appear on those planets, since
        // they are not present in vanilla and this may cause unintended consequences (e.g. planet killer detonated on one of those planets)
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

