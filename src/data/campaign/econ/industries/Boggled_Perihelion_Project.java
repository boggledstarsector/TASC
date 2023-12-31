package data.campaign.econ.industries;
import java.awt.*;
import java.lang.String;
import java.util.Iterator;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.FleetAdvanceScript;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.campaign.econ.boggledTools;

public class Boggled_Perihelion_Project extends BaseIndustry {

    protected Random random;

    private int daysWithoutShortageCoronalTap = 0;
    private int lastDayCheckedCoronalTap = 0;
    private int requiredDaysToBuildCoronalTap = boggledTools.getIntSetting("boggledPerihelionProjectDaysToFinish");

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        boolean shortage = perihelionProjectHasShortage();

        CampaignClockAPI clock = Global.getSector().getClock();

        //
        // Coronal tap construction
        //

        if(this.isFunctional())
        {
            if(clock.getDay() != this.lastDayCheckedCoronalTap && !shortage)
            {
                // Just in case the player changes the required days after building the structure. Without this, the required days will stay
                // at the original value regardless of subsequent changes in the settings file for Perihelion Project buildings already constructed.
                this.requiredDaysToBuildCoronalTap = boggledTools.getIntSetting("boggledPerihelionProjectDaysToFinish");

                this.daysWithoutShortageCoronalTap++;
                this.lastDayCheckedCoronalTap = clock.getDay();

                if(daysWithoutShortageCoronalTap >= requiredDaysToBuildCoronalTap)
                {
                    if (this.market.isPlayerOwned())
                    {
                        MessageIntel intel = new MessageIntel("Coronal hypershunt at " + this.market.getStarSystem().getName(), Misc.getBasePlayerColor());
                        intel.addLine("    - Constructed");
                        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
                    }

                    this.daysWithoutShortageCoronalTap = 0;
                    this.lastDayCheckedCoronalTap = clock.getDay();

                    if(!this.market.getStarSystem().hasTag(Tags.HAS_CORONAL_TAP))
                    {
                        this.createCoronalTapEntity(this.market.getStarSystem());
                    }

                    this.market.removeIndustry("BOGGLED_PERIHELION_PROJECT", null, false);
                }
            }
        }
    }

    @Override
    public void apply()
    {
        super.apply(false);
        super.applyIncomeAndUpkeep(3);

        if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled"))
        {
            this.demand("domain_artifacts", 7);
        }

        this.demand("metals", 11);
        this.demand("rare_metals", 9);
        this.demand("heavy_machinery", 7);
    }

    @Override
    public void unapply() { super.unapply(); }

    @Override
    public boolean canBeDisrupted() { return true; }

    public boolean perihelionProjectHasShortage()
    {
        boolean shortage = false;
        if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled"))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit(new String[]{"domain_artifacts"});
            if(deficit.two != 0)
            {
                shortage = true;
            }
        }

        Pair<String, Integer> deficitMetal = this.getMaxDeficit(new String[]{"metals"});
        if(deficitMetal.two != 0)
        {
            shortage = true;
        }

        Pair<String, Integer> deficitRareMetal = this.getMaxDeficit(new String[]{"rare_metals"});
        if(deficitRareMetal.two != 0)
        {
            shortage = true;
        }

        Pair<String, Integer> deficitHeavyMachinery = this.getMaxDeficit(new String[]{"heavy_machinery"});
        if(deficitHeavyMachinery.two != 0)
        {
            shortage = true;
        }

        return shortage;
    }

    public void createCoronalTapEntity(StarSystemAPI system)
    {
        SectorEntityToken tapToken = null;
        if (system.getType() == StarSystemGenerator.StarSystemType.TRINARY_2CLOSE)
        {
            tapToken = system.addCustomEntity("coronal_tap_" + this.market.getStarSystem().getName(), null, "coronal_tap", Global.getSector().getPlayerFaction().getId());
            float minDist = 3.4028235E38F;
            PlanetAPI closest = null;
            Iterator var5 = tapToken.getContainingLocation().getPlanets().iterator();
            while(var5.hasNext())
            {
                PlanetAPI star = (PlanetAPI)var5.next();
                if (star.isStar())
                {
                    float dist = Misc.getDistance(tapToken.getLocation(), star.getLocation());
                    if (dist < minDist)
                    {
                        minDist = dist;
                        closest = star;
                    }
                }
            }

            if (closest != null)
            {
                tapToken.setFacing(Misc.getAngleInDegrees(tapToken.getLocation(), closest.getLocation()) + 180.0F);
            }
        }
        else
        {
            WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>();
            WeightedRandomPicker<PlanetAPI> fallback = new WeightedRandomPicker<PlanetAPI>();
            for (PlanetAPI planet : system.getPlanets())
            {
                if (!planet.isNormalStar()) continue;
                if (planet.getTypeId().equals(StarTypes.BLUE_GIANT) || planet.getTypeId().equals(StarTypes.BLUE_SUPERGIANT))
                {
                    picker.add(planet);
                }
                else
                {
                    fallback.add(planet);
                }
            }
            if (picker.isEmpty())
            {
                picker.addAll(fallback);
            }

            PlanetAPI star = picker.pick();
            if (star != null)
            {
                CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec(Entities.CORONAL_TAP);
                float orbitRadius = star.getRadius() + spec.getDefaultRadius() + 100f;
                float orbitDays = orbitRadius / 20f;
                tapToken = system.addCustomEntity("coronal_tap_" + this.market.getStarSystem().getName(), null, "coronal_tap", Global.getSector().getPlayerFaction().getId());
                tapToken.setCircularOrbitPointingDown(star, boggledTools.getAngleFromEntity(market.getPrimaryEntity(), star), orbitRadius, orbitDays);
            }
        }

        if (tapToken != null)
        {
            system.addScript(new CoronalTapParticleScript(tapToken));
            tapToken.addTag("BOGGLED_BUILT_BY_PERIHELION_PROJECT");
            MemoryAPI memory = tapToken.getMemory();
            tapToken.getMemory().set("$usable", true);
            memory.unset("$hasDefenders");
            memory.unset("$defenderFleet");
            memory.set("$defenderFleetDefeated", true);
            tapToken.removeScriptsOfClass(FleetAdvanceScript.class);
            //system.addCorona(entity.entity, Terrain.CORONA_JET,
            //		500f, // radius outside planet
            //	    15f, // burn level of "wind"
            //		0f, // flare probability
            //		1f // CR loss mult while in it
            //	);

            //system.addTag(Tags.THEME_DERELICT);
            system.addTag(Tags.HAS_CORONAL_TAP);
        }
    }

    public boolean canBuildCoronalTapInSystem(StarSystemAPI system)
    {
        for (PlanetAPI planet : system.getPlanets())
        {
            if (!planet.isNormalStar()) continue;
            if (planet.getTypeId().equals(StarTypes.BLUE_GIANT) || planet.getTypeId().equals(StarTypes.BLUE_SUPERGIANT) || planet.getTypeId().equals("US_star_blue_giant"))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledPerihelionProjectEnabled"))
        {
            return false;
        }

        if(this.market.getStarSystem().hasTag(Tags.HAS_CORONAL_TAP))
        {
            return false;
        }

        if(!this.canBuildCoronalTapInSystem(this.market.getStarSystem()))
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

        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledPerihelionProjectEnabled"))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public String getUnavailableReason()
    {
        if(this.market.getStarSystem().hasTag(Tags.HAS_CORONAL_TAP))
        {
            return "The " + this.market.getStarSystem().getName() + " already has a coronal tap.";
        }

        return "Coronal hypershunts can only function if they orbit a blue giant star. There is no blue giant star in the " + this.market.getStarSystem().getName() + ".";
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        //
        // Inserts coronal tap construction status
        //

        if(mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            int percentComplete = (int) (((float) this.daysWithoutShortageCoronalTap / (float) this.requiredDaysToBuildCoronalTap) * 100F);

            //Makes sure the tooltip doesn't say "0% complete" on the first day due to rounding down
            if(percentComplete < 1)
            {
                percentComplete = 1;
            }

            //Makes sure the tooltip doesn't say "100% complete" on the last day due to rounding up
            if(percentComplete > 99)
            {
                percentComplete = 99;
            }

            tooltip.addPara("Construction of the coronal hypershunt in the " + this.market.getStarSystem().getName() + " is approximately %s complete.", opad, highlight, new String[]{percentComplete + "%"});
        }

        if(this.isDisrupted() && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Construction progress is stalled while the Perihelion Project is disrupted.", bad, opad);
        }
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        boolean shortage = perihelionProjectHasShortage();
        float opad = 10.0F;
        Color bad = Misc.getNegativeHighlightColor();

        if(shortage && mode != IndustryTooltipMode.ADD_INDUSTRY && mode != IndustryTooltipMode.QUEUED && !isBuilding())
        {
            tooltip.addPara("Construction of the coronal hypershunt is stalled due to a shortage of raw materials.", bad, opad);
        }
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade)
    {
        this.daysWithoutShortageCoronalTap = 0;
        this.lastDayCheckedCoronalTap = 0;

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
    public boolean canInstallAICores() {
        return false;
    }

    @Override
    public boolean canImprove()
    {
        return false;
    }
}
