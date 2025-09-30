package boggled.campaign.econ.industries;
import java.awt.*;
import java.lang.String;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CoronalTapParticleScript;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.HypershuntIntel;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.FleetAdvanceScript;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import boggled.campaign.econ.boggledTools;
import org.jetbrains.annotations.NotNull;

public class Boggled_Perihelion_Project extends BaseIndustry {

    protected Random random = new Random();

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
                        MessageIntel intel = new MessageIntel("Coronal tap at " + this.market.getStarSystem().getName(), Misc.getBasePlayerColor());
                        intel.addLine("    - Constructed");
                        intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                        Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, this.market);
                    }

                    this.daysWithoutShortageCoronalTap = 0;
                    this.lastDayCheckedCoronalTap = clock.getDay();

                    if(!this.market.getStarSystem().hasTag(Tags.HAS_CORONAL_TAP))
                    {
                        this.createCoronalTapEntity(this.market.getStarSystem(), this.market.getFaction());
                    }

                    this.market.removeIndustry(this.getId(), null, false);
                }
            }
        }
    }

    @Override
    public void apply()
    {
        super.apply(false);
        super.applyIncomeAndUpkeep(3);

        // Only demand commodities when AotD is not installed.
        // AotD megastructure activation functionality requires huge amounts so the player will pay there.
        if(!Global.getSettings().getModManager().isModEnabled("aotd_vok"))
        {
            if(boggledTools.domainEraArtifactDemandEnabled())
            {
                this.demand("domain_artifacts", 7);
            }

            this.demand("metals", 11);
            this.demand("rare_metals", 9);
            this.demand("heavy_machinery", 7);
        }
    }

    @Override
    public void unapply() { super.unapply(); }

    @Override
    public boolean canBeDisrupted() { return true; }

    public boolean perihelionProjectHasShortage()
    {
        boolean shortage = false;
        if(boggledTools.domainEraArtifactDemandEnabled())
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

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledPerihelionProjectEnabled"))
        {
            return false;
        }

        StarSystemAPI system = this.market.getStarSystem();
        if(system == null)
        {
            return false;
        }

        if(system.hasTag(Tags.HAS_CORONAL_TAP))
        {
            return false;
        }

        if(systemHasMarketWithPerihelionProject(system, this.market))
        {
            return false;
        }

        if(!systemValidForCoronalHypershuntPlacement(system))
        {
            return false;
        }

        return super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") || !boggledTools.getBooleanSetting("boggledPerihelionProjectEnabled"))
        {
            return false;
        }

        StarSystemAPI system = this.market.getStarSystem();
        if(system == null)
        {
            return false;
        }

        if(system.hasTag(Tags.HAS_CORONAL_TAP))
        {
            return super.showWhenUnavailable();
        }

        if(systemHasMarketWithPerihelionProject(system, this.market))
        {
            return super.showWhenUnavailable();
        }

        if(!systemValidForCoronalHypershuntPlacement(system))
        {
            return super.showWhenUnavailable();
        }

        return super.showWhenUnavailable();
    }

    @Override
    public String getUnavailableReason()
    {
        StarSystemAPI system = this.market.getStarSystem();
        if(system.hasTag(Tags.HAS_CORONAL_TAP))
        {
            return "The " + this.market.getStarSystem().getName() + " already has a coronal tap.";
        }

        if(systemHasMarketWithPerihelionProject(system, this.market))
        {
            return "There is already a colony in the " + this.market.getStarSystem().getName() + " constructing a coronal tap.";
        }

        if(!systemValidForCoronalHypershuntPlacement(system))
        {
            return "Coronal taps can only be constructed in systems with at least one blue star and/or trinary systems where all three stars are not the same color.";
        }

        return super.getUnavailableReason();
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode)
    {
        // Avoid showing this tooltip in the AotD research menu.
        if(this.market == null || this.market.getStarSystem() == null)
        {
            return;
        }

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

            tooltip.addPara("Construction of the coronal tap in the " + this.market.getStarSystem().getName() + " is approximately %s complete.", opad, highlight, new String[]{percentComplete + "%"});
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
            tooltip.addPara("Construction of the coronal tap is stalled due to a shortage of raw materials.", bad, opad);
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
        return 10.0F;
    }

    @Override
    public boolean canInstallAICores() {
        return false;
    }

    @Override
    protected void buildingFinished() {
        super.buildingFinished();

        // Immediately create the tap if AotD is enabled
        // The player will have to use the megastructure tab to activate the tap.
        if(Global.getSettings().getModManager().isModEnabled("aotd_vok"))
        {
            if(!this.market.getStarSystem().hasTag(Tags.HAS_CORONAL_TAP))
            {
                this.createCoronalTapEntity(this.market.getStarSystem(), this.market.getFaction());
            }

            this.market.removeIndustry(this.getId(), null, false);
        }
    }

    @Override
    protected String getDescriptionOverride()
    {
        // When finished, the AotD version still needs to be activated in the megastructure tab.
        // The dialog says the tap is in disrepair and needs work - have to adjust the tooltip
        // to explain to the player that they will need to do additional steps to activate it.
        if(Global.getSettings().getModManager().isModEnabled("aotd_vok"))
        {
            return this.getSpec().getDesc() + "\n\nOnce the structure itself is complete, additional on-site work will be required to bring the internal components into an operational state.";
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean canImprove()
    {
        return false;
    }

    // Vanilla code to create coronal taps in procedurally generated systems.
    // C:\Program Files (x86)\Fractal Softworks\Starsector\starsector-core\com\fs\starfarer\api\impl\campaign\procgen\themes\MiscellaneousThemeGenerator.class
    public SectorEntityToken createCoronalTapEntity(@NotNull StarSystemAPI system, @NotNull FactionAPI faction)
    {
        boggledTools.writeMessageToLog("TASC: Adding coronal tap to " + system.getNameWithLowercaseType() + ".");

        String factionId = faction.getId();
        SectorEntityToken tapToken = null;
        if(system.getType() == StarSystemGenerator.StarSystemType.TRINARY_2CLOSE)
        {
            tapToken = system.addCustomEntity("coronal_tap_" + system.getNameWithLowercaseType(), null, "coronal_tap", factionId);
            system.addScript(new MiscellaneousThemeGenerator.MakeCoronalTapFaceNearestStar(tapToken));
        }
        else
        {
            // Literally copy/pasted from Alex's code, except changed to making a SectorEntityToken instead of using the BaseThemeGenerator class
            WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker();
            WeightedRandomPicker<PlanetAPI> fallback = new WeightedRandomPicker();
            Iterator var7 = system.getPlanets().iterator();

            label51:
            while(true) {
                while(true) {
                    PlanetAPI planet;
                    do {
                        if (!var7.hasNext()) {
                            if (picker.isEmpty()) {
                                picker.addAll(fallback);
                            }

                            planet = (PlanetAPI)picker.pick();
                            if (planet != null) {
                                CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec("coronal_tap");
                                float orbitRadius = planet.getRadius() + spec.getDefaultRadius() + 100.0F;
                                float orbitDays = orbitRadius / 20.0F;
                                OrbitAPI orbit = Global.getFactory().createCircularOrbitPointingDown(planet, random.nextFloat() * 360.0F, orbitRadius, orbitDays);
                                tapToken = system.addCustomEntity("coronal_tap_" + system.getNameWithLowercaseType(), null, "coronal_tap", factionId);
                                tapToken.setOrbit(orbit);
                            }
                            break label51;
                        }

                        planet = (PlanetAPI)var7.next();
                    } while(!planet.isNormalStar());

                    if (!planetIsBlueStar(planet)) {
                        fallback.add(planet);
                    } else {
                        picker.add(planet);
                    }
                }
            }
        }

        if (tapToken != null)
        {
            system.addScript(new CoronalTapParticleScript(tapToken));
            system.addTag(Tags.HAS_CORONAL_TAP);

            // Added to make the coronal hypershunt usable immediately
            // instead of the player having to fight a redacted fleet first.
            tapToken.addTag("boggled_tasc_built_with_perihelion_project");

            MemoryAPI memory = tapToken.getMemory();
            memory.unset("$hasDefenders");
            memory.unset("$defenderFleet");
            memory.set("$defenderFleetDefeated", true);
            tapToken.removeScriptsOfClass(FleetAdvanceScript.class);

            // AotD has special requirements to activate the coronal tap.
            // Leave it in a non-usable state so AotD can take over with the megastructure tab.
            if(!Global.getSettings().getModManager().isModEnabled("aotd_vok"))
            {
                tapToken.getMemory().set("$usable", true);

                // Without AotD, vanilla uses this intel to find coronal taps nearby.
                HypershuntIntel newIntel = new HypershuntIntel(tapToken, null);
            }
        }

        return tapToken;
    }

    public boolean planetIsBlueStar(@NotNull PlanetAPI planet)
    {
        // Vanilla does not appear to consider modded star types as blue stars so we need to implement it here.
        boggledTools.writeMessageToLog("Coronal tap logic debug: Planet " + planet.getName() + " has planet type ID: " + planet.getTypeId());

        // There are multiple IDs for blue stars (e.g. blue star, blue supergiant, blue giant) so use contains
        return planet.isStar() && planet.getTypeId().contains("blue");
    }

    public ArrayList<PlanetAPI> getBlueStarsInSystem(@NotNull StarSystemAPI system)
    {
        ArrayList<PlanetAPI> blueStarsList = new ArrayList<>();
        for(PlanetAPI planet : system.getPlanets())
        {
            if(planetIsBlueStar(planet))
            {
                blueStarsList.add(planet);
            }
        }

        return blueStarsList;
    }

    public boolean systemHasBlueStar(@NotNull StarSystemAPI system)
    {
        ArrayList<PlanetAPI> blueStarList = getBlueStarsInSystem(system);
        return !blueStarList.isEmpty();
    }

    // https://starsector.fandom.com/wiki/Coronal_Hypershunt
    // Coronal hypershunts can be placed in trinary systems or systems with a blue star
    // I'm not sure if it's guaranteed that trinary_2close systems have different colored stars, but vanilla uses that logic.
    public boolean systemValidForCoronalHypershuntPlacement(@NotNull StarSystemAPI system)
    {
        return system.getType() == StarSystemGenerator.StarSystemType.TRINARY_2CLOSE || systemHasBlueStar(system);
    }

    public boolean systemAlreadyHasCoronalHypershuntOrMarketWithPerihelionProject(@NotNull StarSystemAPI system)
    {
        return system.hasTag(Tags.HAS_CORONAL_TAP) || systemHasMarketWithPerihelionProject(system, this.market);
    }

    // Starsector puts a "fake" copy of the industry on the market the player is interacting with, so we need to exclude it from this check.
    public boolean systemHasMarketWithPerihelionProject(@NotNull StarSystemAPI system, MarketAPI exclude)
    {
        for(MarketAPI market : Global.getSector().getEconomy().getMarkets(system))
        {
            if(market.hasIndustry(this.getId()) && !market.getId().equals(exclude.getId()))
            {
                boggledTools.writeMessageToLog("TASC: Market " + market.getName() + " already has Perihelion Project industry.");
                return true;
            }
        }

        return false;
    }
}
