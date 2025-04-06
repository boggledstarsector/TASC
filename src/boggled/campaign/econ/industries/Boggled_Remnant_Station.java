package boggled.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.econ.impl.*;
import com.fs.starfarer.api.impl.campaign.fleets.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantOfficerGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import boggled.campaign.econ.boggledTools;

import java.util.*;
import java.awt.Color;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import org.lwjgl.util.vector.Vector2f;

public class Boggled_Remnant_Station extends OrbitalStation implements RouteManager.RouteFleetSpawner, FleetEventListener
{
    protected IntervalUtil tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f,
            Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);

    protected float returningPatrolValue = 0f;

    public static float BASE_DEFENSE_BONUS_BOGGLED_REMNANT_BATTLESTATION = 2.0F;
    public static int BASE_STABILITY_MOD_BOGGLED_REMNANT_BATTLESTATION = 3;

    // Use this to dynamically handle demand whether DEA is enabled or not.
    // Need to use some ordered data structure because we don't want the UI to display the commodities in a random order
    private ArrayList<Pair<String, Integer>> getCommodityDemand()
    {
        ArrayList<Pair<String, Integer>> demandArray = new ArrayList<>();
        demandArray.add(new Pair<>(Commodities.SUPPLIES, 7));

        if(boggledTools.domainEraArtifactDemandEnabled())
        {
            demandArray.add(new Pair<>(boggledTools.BoggledCommodities.domainArtifacts, 4));
        }

        return demandArray;
    }

    private String[] getCommodityStringArray()
    {
        ArrayList<Pair<String, Integer>> demandArray = getCommodityDemand();
        String[] commodityArray = new String[demandArray.size()];
        for(int i = 0; i < demandArray.size(); i++)
        {
            commodityArray[i] = demandArray.get(i).one;
        }

        return commodityArray;
    }

    @Override
    public void apply()
    {
        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);

        // Replaces the super.apply(false) call in OrbitalStation
        // We've basically duplicated what OrbitalStation.apply() does in this function but removed some stuff we don't want
        this.updateSupplyAndDemandModifiers();

        this.applyAICoreModifiers();
        this.applyImproveModifiers();

        // This does not modify immigration but this boilerplate code is here anyway in case another mod changes that.
        if (this instanceof MarketImmigrationModifier)
        {
            this.market.addTransientImmigrationModifier((MarketImmigrationModifier)this);
        }

        // There's no special item for orbital stations, but other mods might add one I suppose
        if (this.special != null)
        {
            InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(this.special.getId());
            if (effect != null)
            {
                List<String> unmet = effect.getUnmetRequirements(this);
                if (unmet != null && !unmet.isEmpty())
                {
                    effect.unapply(this);
                }
                else
                {
                    effect.apply(this);
                }
            }
        }

        this.modifyStabilityWithBaseMod();
        this.applyIncomeAndUpkeep(7);

        // Set demand based on values configured in getCommodityDemand(). Handles DEA being enabled or disabled.
        ArrayList<Pair<String, Integer>> demandArrayList = getCommodityDemand();
        for(Pair<String, Integer> demand : demandArrayList)
        {
            this.demand(demand.one, demand.two);
        }

        this.market.getStats().getDynamic().getMod("ground_defenses_mod").modifyMult(this.getModId(), getGroundDefenseMultiplierAfterCommodityShortage(), this.getNameForModifier());

        this.matchCommanderToAICore(this.aiCoreId);

        if (!this.isFunctional())
        {
            this.supply.clear();
            this.unapply();
        }
        else
        {
            this.applyCRToStation();
        }
    }

    @Override
    public void unapply()
    {
        super.unapply();
        this.unmodifyStabilityWithBaseMod();
        this.matchCommanderToAICore((String)null);
        this.market.getStats().getDynamic().getMod("ground_defenses_mod").unmodifyMult(this.getModId());

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        if (Global.getSector().getEconomy().isSimMode()) return;

        if (!isFunctional()) return;

        float days = Global.getSector().getClock().convertToDays(amount);

        float spawnRate = 1f;
        float rateMult = market.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).getModifiedValue();
        spawnRate *= rateMult;

        if (Global.getSector().isInNewGameAdvance()) {
            spawnRate *= 3f;
        }

        float extraTime = 0f;
        if (returningPatrolValue > 0) {
            // apply "returned patrols" to spawn rate, at a maximum rate of 1 interval per day
            float interval = tracker.getIntervalDuration();
            extraTime = interval * days;
            returningPatrolValue -= days;
            if (returningPatrolValue < 0) returningPatrolValue = 0;
        }
        tracker.advance(days * spawnRate + extraTime);

        //DebugFlags.FAST_PATROL_SPAWN = true;
        if (DebugFlags.FAST_PATROL_SPAWN) {
            tracker.advance(days * spawnRate * 100f);
        }

        if (tracker.intervalElapsed()) {
            String sid = getRouteSourceId();

            int light = getCount(FleetFactory.PatrolType.FAST);
            int medium = getCount(FleetFactory.PatrolType.COMBAT);
            int heavy = getCount(FleetFactory.PatrolType.HEAVY);

            // Need to hardcode this as the max patrols function does not work for this building
            int maxLight = 3;
            int maxMedium = 2;
            int maxHeavy = 1;

            WeightedRandomPicker<FleetFactory.PatrolType> picker = new WeightedRandomPicker<FleetFactory.PatrolType>();
            picker.add(FleetFactory.PatrolType.HEAVY, maxHeavy - heavy);
            picker.add(FleetFactory.PatrolType.COMBAT, maxMedium - medium);
            picker.add(FleetFactory.PatrolType.FAST, maxLight - light);

            if (picker.isEmpty()) return;

            FleetFactory.PatrolType type = picker.pick();
            MilitaryBase.PatrolFleetData custom = new MilitaryBase.PatrolFleetData(type);

            RouteManager.OptionalFleetData extra = new RouteManager.OptionalFleetData(market);
            extra.fleetType = type.getFleetType();

            RouteManager.RouteData route = RouteManager.getInstance().addRoute(sid, market, Misc.genRandomSeed(), extra, this, custom);
            extra.strength = (float) getPatrolCombatFP(type, route.getRandom());
            extra.strength = Misc.getAdjustedStrength(extra.strength, market);


            float patrolDays = 35f + (float) Math.random() * 10f;
            route.addSegment(new RouteManager.RouteSegment(patrolDays, market.getPrimaryEntity()));
        }
    }

    public void reportAboutToBeDespawnedByRouteManager(RouteManager.RouteData route) { }

    public boolean shouldRepeat(RouteManager.RouteData route) {
        return false;
    }

    public int getCount(FleetFactory.PatrolType... types) {
        int count = 0;
        for (RouteManager.RouteData data : RouteManager.getInstance().getRoutesForSource(getRouteSourceId())) {
            if (data.getCustom() instanceof MilitaryBase.PatrolFleetData) {
                MilitaryBase.PatrolFleetData custom = (MilitaryBase.PatrolFleetData) data.getCustom();
                for (FleetFactory.PatrolType type : types) {
                    if (type == custom.type) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    public static int getPatrolCombatFP(FleetFactory.PatrolType type, Random random) {
        float combat = 0;
        switch (type) {
            case FAST:
                combat = Math.round(3f + (float) random.nextFloat() * 2f) * 5f;
                break;
            case COMBAT:
                combat = Math.round(6f + (float) random.nextFloat() * 3f) * 5f;
                break;
            case HEAVY:
                combat = Math.round(10f + (float) random.nextFloat() * 5f) * 5f;
                break;
        }
        return (int) Math.round(combat);
    }

    public boolean shouldCancelRouteAfterDelayCheck(RouteManager.RouteData route) {
        return false;
    }

    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) { }

    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param)
    {
        if (fleet == this.stationFleet) {
            disrupt(this);
            if (this.stationFleet.getMembersWithFightersCopy().isEmpty()) {
                this.matchStationAndCommanderToCurrentIndustry();
            }

            this.stationFleet.setAbortDespawn(true);
        }

        if (!isFunctional()) return;

        if (reason == CampaignEventListener.FleetDespawnReason.REACHED_DESTINATION)
        {
            RouteManager.RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
            if (route.getCustom() instanceof MilitaryBase.PatrolFleetData)
            {
                MilitaryBase.PatrolFleetData custom = (MilitaryBase.PatrolFleetData) route.getCustom();
                if (custom.spawnFP > 0)
                {
                    float fraction = (float) fleet.getFleetPoints() / custom.spawnFP;
                    returningPatrolValue += fraction;
                }
            }
        }
    }

    public CampaignFleetAPI spawnFleet(RouteManager.RouteData route) {

        MilitaryBase.PatrolFleetData custom = (MilitaryBase.PatrolFleetData) route.getCustom();
        FleetFactory.PatrolType type = custom.type;

        Random random = route.getRandom();

        CampaignFleetAPI fleet = createPatrol(type, market.getFactionId(), route, market, null, random);

        if (fleet == null || fleet.isEmpty()) return null;

        fleet.addEventListener(this);

        market.getContainingLocation().addEntity(fleet);
        fleet.setFacing((float) Math.random() * 360f);
        // this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
        fleet.setLocation(market.getLocation().x, market.getLocation().y);

        fleet.addScript(new PatrolAssignmentAIV4(fleet, route));

        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true, 0.3f);

        if (custom.spawnFP <= 0) {
            custom.spawnFP = fleet.getFleetPoints();
        }

        return fleet;
    }

    public static CampaignFleetAPI createPatrol(FleetFactory.PatrolType type, String factionId, RouteManager.RouteData route, MarketAPI market, Vector2f locInHyper, Random random) {
        if (random == null) random = new Random();


        float combat = getPatrolCombatFP(type, random);
        float tanker = 0f;
        float freighter = 0f;
        String fleetType = type.getFleetType();
        switch (type) {
            case FAST:
                break;
            case COMBAT:
                tanker = Math.round((float) random.nextFloat() * 5f);
                break;
            case HEAVY:
                tanker = Math.round((float) random.nextFloat() * 10f);
                freighter = Math.round((float) random.nextFloat() * 10f);
                break;
        }

        FleetParamsV3 params = new FleetParamsV3(
                market,
                locInHyper,
                Factions.REMNANTS,
                route == null ? null : route.getQualityOverride(),
                fleetType,
                combat, // combatPts
                freighter, // freighterPts
                tanker, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f // qualityMod
        );
        if (route != null) {
            params.timestamp = route.getTimestamp();
        }
        params.random = random;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        if (fleet == null || fleet.isEmpty()) return null;
        fleet.setFaction(market.getFactionId(), true);
        fleet.setNoFactionInName(true);

        if (fleet == null || fleet.isEmpty()) return null;

        if (!fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PATROLS_HAVE_NO_PATROL_MEMORY_KEY)) {
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
            if (type == FleetFactory.PatrolType.FAST || type == FleetFactory.PatrolType.COMBAT) {
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR, true);
            }
        } else if (fleet.getFaction().getCustomBoolean(Factions.CUSTOM_PIRATE_BEHAVIOR)) {
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);

            // hidden pather and pirate bases
            // make them raid so there's some consequence to just having a colony in a system with one of those
            if (market != null && market.isHidden()) {
                fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_RAIDER, true);
            }
        }

        String postId = Ranks.POST_PATROL_COMMANDER;
        String rankId = Ranks.SPACE_COMMANDER;
        switch (type) {
            case FAST:
                rankId = Ranks.SPACE_LIEUTENANT;
                break;
            case COMBAT:
                rankId = Ranks.SPACE_COMMANDER;
                break;
            case HEAVY:
                rankId = Ranks.SPACE_CAPTAIN;
                break;
        }

        fleet.getCommander().setPostId(postId);
        fleet.getCommander().setRankId(rankId);

        // Necessary so rules.csv can differentiate the remnant patrols and display the correct dialogue
        fleet.addTag("boggledRemnantStationPatrol");
        return fleet;
    }

    public String getRouteSourceId()
    {
        return getMarket().getId() + "_" + boggledTools.BoggledIndustries.remnantStationIndustryId;
    }

    @Override
    public MarketCMD.RaidDangerLevel adjustCommodityDangerLevel(String commodityId, MarketCMD.RaidDangerLevel level)
    {
        return level.next();
    }

    @Override
    public MarketCMD.RaidDangerLevel adjustItemDangerLevel(String itemId, String data, MarketCMD.RaidDangerLevel level)
    {
        return level.next();
    }

    @Override
    protected float getCR()
    {
        // Deviates from vanilla logic since the Remnant station demands an unequal amount of supplies and DEA
        float q = Misc.getShipQuality(this.market);
        if (q < 0.0F)
        {
            q = 0.0F;
        }

        if (q > 1.0F)
        {
            q = 1.0F;
        }

        float d = getCommodityCrModifier();

        // 50% base CR + 50% * the lower of the ship quality modifier and shortage modifier
        return 0.5F + (0.5F * Math.min(d, q));
    }

    private float getCommodityCrModifier()
    {
        // Don't modify CR if there is no commodity demand
        if(this.demand.size() == 0)
        {
            return 1.0F;
        }

        // Get all the CR modifiers. Formula is (demand - deficit) / demand.
        float lowestModifier = 1.0F;
        for(String commodity : this.demand.keySet())
        {
            lowestModifier = Math.min(lowestModifier, computeCommodityDeficitCrModifierForSingleCommodity(commodity));
        }
        return lowestModifier;
    }

    private float computeCommodityDeficitCrModifierForSingleCommodity(String commodity)
    {
        float deficit = Math.max(0.0F, (float) this.getMaxDeficit(commodity).two);
        float demand = Math.max(0.0F, (float) this.getDemand(commodity).getQuantity().getModifiedInt());

        // If there's no demand, don't throw a divide by zero exception and instead return 1.0F
        if (demand < 1.0F)
        {
            demand = 1.0F;
            deficit = 0.0F;
        }

        // This should never happen unless there's bugged code in vanilla or another mod
        if(deficit > demand)
        {
            return 0.0F;
        }

        return (demand - deficit) / demand;
    }

    @Override
    protected Pair<String, Integer> getStabilityAffectingDeficit()
    {
        return this.getMaxDeficit(getCommodityStringArray());
    }

    @Override
    protected int getBaseStabilityMod() {
        return BASE_STABILITY_MOD_BOGGLED_REMNANT_BATTLESTATION;
    }

    @Override
    public String getNameForModifier()
    {
        return "Autonomous AI battlestation";
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        if (mode != IndustryTooltipMode.NORMAL || this.isFunctional())
        {
            Color h = Misc.getHighlightColor();
            float opad = 10.0F;
            float cr = this.getCR();
            tooltip.addPara("Station combat readiness: %s", opad, h, Math.round(cr * 100.0F) + "%");
            this.addStabilityPostDemandSectionBoggledRemnantStation(tooltip, hasDemand, mode);

            this.addGroundDefensesImpactSectionBoggledRemnantStation(tooltip, BASE_DEFENSE_BONUS_BOGGLED_REMNANT_BATTLESTATION, getCommodityStringArray());
        }
    }

    protected void addStabilityPostDemandSectionBoggledRemnantStation(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        Color h = Misc.getHighlightColor();
        float opad = 10.0F;
        MutableStat fake = new MutableStat(0.0F);
        int stabilityMod = BASE_STABILITY_MOD_BOGGLED_REMNANT_BATTLESTATION;
        int stabilityPenalty = this.getStabilityPenalty();
        if (stabilityPenalty > stabilityMod)
        {
            stabilityPenalty = stabilityMod;
        }

        String str = getDeficitText((String)this.getStabilityAffectingDeficit().one);
        fake.modifyFlat("1", (float)stabilityMod, this.getNameForModifier());
        if (stabilityPenalty != 0)
        {
            fake.modifyFlat("2", (float)(-stabilityPenalty), str);
        }

        int total = stabilityMod - stabilityPenalty;
        String totalStr = "+" + total;
        if (total < 0)
        {
            totalStr = "" + total;
            h = Misc.getNegativeHighlightColor();
        }

        float pad = 3.0F;
        if (total >= 0)
        {
            tooltip.addPara("Stability bonus: %s", opad, h, totalStr);
        } else
        {
            tooltip.addPara("Stability penalty: %s", opad, h, totalStr);
        }

        tooltip.addStatModGrid(400.0F, 35.0F, opad, pad, fake, new TooltipMakerAPI.StatModValueGetter()
        {
            public String getPercentValue(MutableStat.StatMod mod) {
                return null;
            }

            public String getMultValue(MutableStat.StatMod mod) {
                return null;
            }

            public Color getModColor(MutableStat.StatMod mod)
            {
                return mod.value < 0.0F ? Misc.getNegativeHighlightColor() : null;
            }

            public String getFlatValue(MutableStat.StatMod mod) {
                return null;
            }
        });
    }

    protected void addGroundDefensesImpactSectionBoggledRemnantStation(TooltipMakerAPI tooltip, float bonus, String... commodities) {
        Color h = Misc.getHighlightColor();
        float opad = 10.0F;
        MutableStat fake = new MutableStat(1.0F);
        fake.modifyFlat("1", bonus, this.getNameForModifier());
        float mult;
        String totalStr;
        if (commodities != null) {
            mult = getGroundDefenseMultiplierAfterCommodityShortage();
            if (mult < 1.0F + BASE_DEFENSE_BONUS_BOGGLED_REMNANT_BATTLESTATION) {
                totalStr = (String)this.getMaxDeficit(commodities).one;
                fake.modifyFlat("2", getGroundDefenseTooltipCommodityShortageSubtractionAmount(), getDeficitText(totalStr));
            }
        }

        mult = Misc.getRoundedValueFloat(fake.getModifiedValue());
        totalStr = "×" + mult;
        if (mult < 1.0F) {
            h = Misc.getNegativeHighlightColor();
        }

        float pad = 3.0F;
        tooltip.addPara("Ground defense strength: %s", opad, h, new String[]{totalStr});
        tooltip.addStatModGrid(400.0F, 35.0F, opad, pad, fake, new TooltipMakerAPI.StatModValueGetter() {
            public String getPercentValue(MutableStat.StatMod mod) {
                return null;
            }

            public String getMultValue(MutableStat.StatMod mod) {
                return null;
            }

            public Color getModColor(MutableStat.StatMod mod) {
                return mod.value < 0.0F ? Misc.getNegativeHighlightColor() : null;
            }

            public String getFlatValue(MutableStat.StatMod mod) {
                String r = Misc.getRoundedValue(mod.value);
                return mod.value >= 0.0F ? "+" + r : r;
            }
        });
    }

    // I don't understand how addGroundDefensesImpactSection really works,
    // but we need to get the smaller of the max deficit and the base ground defense bonus (2 for this building).
    private float getGroundDefenseTooltipCommodityShortageSubtractionAmount()
    {
        return -1 * Math.min(BASE_DEFENSE_BONUS_BOGGLED_REMNANT_BATTLESTATION, this.getMaxDeficit(getCommodityStringArray()).two);
    }

    // Vanilla as of 0.97a has a bug where the tooltip window says the ground defense multiplier is reduced due
    // to a shortage, but the actual defense calculation is not impacted and is still the base modifier.
    // This building actually supports reducing the ground defense multiplier due to a shortage.
    private float getGroundDefenseMultiplierAfterCommodityShortage()
    {
        // 1 + base bonus (2 here) - the max deficit.
        // Since this building requires 7 supplies and 4 DEA this could easily be negative.
        // The worst case should be a flat 1x multiplier, so use Math.max below to never return less than 1.0F.
        float groundDefenseMult = 1.0F + BASE_DEFENSE_BONUS_BOGGLED_REMNANT_BATTLESTATION - this.getMaxDeficit(getCommodityStringArray()).two;
        return Math.max(1.0F, groundDefenseMult);
    }

    @Override
    protected int getHumanCommanderLevel()
    {
        return Global.getSettings().getInt("tier3StationOfficerLevel");
    }

    @Override
    public float getPatherInterest() {
        return 10.0F;
    }

    @Override
    protected boolean isMiltiarized()
    {
        // OrbitalStation is militarized if it's a battlestation or starfortress.
        // Remnant stations are the equivalent of starfortresses, so always return true
        return true;
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.getBooleanSetting("boggledRemnantStationEnabled"))
        {
            return false;
        }

        if(!boggledTools.isResearched("tasc_remnant_station"))
        {
            return false;
        }

        if(!this.playerHasAutomatedShipsSkill())
        {
            return false;
        }

        if(!super.isAvailableToBuild())
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.getBooleanSetting("boggledRemnantStationEnabled"))
        {
            return false;
        }

        if(!boggledTools.isResearched("tasc_remnant_station"))
        {
            return false;
        }

        if(!this.playerHasAutomatedShipsSkill())
        {
            return true;
        }

        if(!super.isAvailableToBuild())
        {
            return true;
        }

        return true;
    }

    @Override
    public String getUnavailableReason()
    {
        if(!boggledTools.isResearched("tasc_remnant_station"))
        {
            return "Error in getUnavailableReason() in Boggled_Remnant_Station. Please report this to boggled on the forums.";
        }

        if(!this.playerHasAutomatedShipsSkill())
        {
            return "You lack the skill to command automated ships.";
        }

        if(!super.isAvailableToBuild())
        {
            return super.getUnavailableReason();
        }

        return "Error in getUnavailableReason() in Boggled_Remnant_Station. Please report this to boggled on the forums.";
    }

    private boolean playerHasAutomatedShipsSkill()
    {
        // Handles skill rework in Second in Command (https://fractalsoftworks.com/forum/index.php?topic=30407.0)
        // Both SC and vanilla add this tag when the player unlocks the automated ships skill
        return Misc.getAllowedRecoveryTags().contains(Tags.AUTOMATED_RECOVERABLE);
    }

    @Override
    protected void buildingFinished()
    {
        super.buildingFinished();

        tracker.forceIntervalElapsed();
    }

    @Override
    protected void matchCommanderToAICore(String aiCore)
    {
        // OrbitStation method only sets an AI core commander if you install an alpha core.
        // We need to customize it to always set an AI core commander, even if there's no core installed.
        if (this.stationFleet != null) {
            PersonAPI commander = null;
            if ("alpha_core".equals(aiCore))
            {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin("alpha_core");
                commander = plugin.createPerson("alpha_core", "remnant", (Random)null);
                if (this.stationFleet.getFlagship() != null)
                {
                    RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(this.stationFleet.getFlagship());
                }
            }
            else if ("beta_core".equals(aiCore))
            {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin("beta_core");
                commander = plugin.createPerson("beta_core", "remnant", (Random)null);
                if (this.stationFleet.getFlagship() != null)
                {
                    RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(this.stationFleet.getFlagship());
                }
            }
            //else if ("gamma_core".equals(aiCore))
            // Always set at least a gamma core, even if no core is installed.
            // I think vanilla stations always have a commander, so not setting one might cause null exceptions, bugs, etc.
            else
            {
                AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin("gamma_core");
                commander = plugin.createPerson("gamma_core", "remnant", (Random)null);
                if (this.stationFleet.getFlagship() != null)
                {
                    RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(this.stationFleet.getFlagship());
                }
            }

            if (commander != null && this.stationFleet.getFlagship() != null) {
                this.stationFleet.getFlagship().setCaptain(commander);
                this.stationFleet.getFlagship().setFlagship(false);
            }

        }
    }
}
