package data.campaign.econ.industries;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.impl.*;
import com.fs.starfarer.api.impl.campaign.fleets.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.campaign.econ.boggledTools;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class Boggled_Remnant_Station extends OrbitalStation implements RouteManager.RouteFleetSpawner, FleetEventListener
{
    protected IntervalUtil tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f, Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);

    protected float returningPatrolValue = 0f;

    // Added per SirHartley on page 148, comment #2211 of the TASC thread.
    // Adds compatibility for the Artillery Station feature from IndEvo.
    @Override
    protected void ensureStationEntityIsSetOrCreated()
    {
        if (stationEntity == null)
        {
            for (SectorEntityToken entity : market.getConnectedEntities())
            {
                if (entity.hasTag(Tags.STATION) && !entity.hasTag("no_orbital_station"))
                {
                    stationEntity = entity;
                    usingExistingStation = true;
                    break;
                }
            }
        }

        if (stationEntity == null)
        {
            stationEntity = market.getContainingLocation().addCustomEntity(null, market.getName() + " Station", Entities.STATION_BUILT_FROM_INDUSTRY, market.getFactionId());
            SectorEntityToken primary = market.getPrimaryEntity();
            float orbitRadius = primary.getRadius() + 150f;
            stationEntity.setCircularOrbitWithSpin(primary, (float) Math.random() * 360f, orbitRadius, orbitRadius / 10f, 5f, 5f);
            market.getConnectedEntities().add(stationEntity);
            stationEntity.setMarket(market);
        }
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

        float extraTime = 0f;
        if (returningPatrolValue > 0)
        {
            // apply "returned patrols" to spawn rate, at a maximum rate of 1 interval per day
            float interval = tracker.getIntervalDuration();
            extraTime = interval * days;
            returningPatrolValue -= days;
            if (returningPatrolValue < 0) returningPatrolValue = 0;
        }

        tracker.advance(days * spawnRate + extraTime);

        //tracker.advance(days * spawnRate * 100f);

        if (tracker.intervalElapsed())
        {
            String sid = getRouteSourceId();

            int light = getCount(FleetFactory.PatrolType.FAST);
            int medium = getCount(FleetFactory.PatrolType.COMBAT);
            int heavy = getCount(FleetFactory.PatrolType.HEAVY);

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
            float patrolDays = 35f + (float) Math.random() * 10f;

            route.addSegment(new RouteManager.RouteSegment(patrolDays, market.getPrimaryEntity()));
        }
    }

    public void reportAboutToBeDespawnedByRouteManager(RouteManager.RouteData route) { }

    public boolean shouldRepeat(RouteManager.RouteData route) {
        return false;
    }

    public int getCount(FleetFactory.PatrolType... types)
    {
        int count = 0;
        for (RouteManager.RouteData data : RouteManager.getInstance().getRoutesForSource(getRouteSourceId()))
        {
            if (data.getCustom() instanceof MilitaryBase.PatrolFleetData)
            {
                MilitaryBase.PatrolFleetData custom = (MilitaryBase.PatrolFleetData) data.getCustom();
                for (FleetFactory.PatrolType type : types)
                {
                    if (type == custom.type)
                    {
                        count++;
                        break;
                    }
                }
            }
        }

        return count;
    }

    public int getMaxPatrols(FleetFactory.PatrolType type)
    {
        if (type == FleetFactory.PatrolType.FAST)
        {
            return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).computeEffective(0);
        }
        if (type == FleetFactory.PatrolType.COMBAT)
        {
            return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).computeEffective(0);
        }
        if (type == FleetFactory.PatrolType.HEAVY)
        {
            return (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).computeEffective(0);
        }

        return 0;
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

        float combat = 0f;
        float tanker = 0f;
        float freighter = 0f;
        String fleetType = type.getFleetType();
        switch (type)
        {
            case FAST:
                combat = Math.round(3f + (float) random.nextFloat() * 2f) * 5f;
                break;
            case COMBAT:
                combat = Math.round(6f + (float) random.nextFloat() * 3f) * 5f;
                tanker = Math.round((float) random.nextFloat()) * 5f;
                break;
            case HEAVY:
                combat = Math.round(10f + (float) random.nextFloat() * 5f) * 5f;
                tanker = Math.round((float) random.nextFloat()) * 10f;
                freighter = Math.round((float) random.nextFloat()) * 10f;
                break;
        }

        FleetParamsV3 params = new FleetParamsV3(
                market,
                null, // loc in hyper; don't need if have market
                Factions.REMNANTS,
                route.getQualityOverride(), // quality override
                fleetType,
                combat, // combatPts
                freighter, // freighterPts
                tanker, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f // qualityMod - since the Lion's Guard is in a different-faction market, counter that penalty
        );

        params.timestamp = route.getTimestamp();
        params.random = random;
        params.modeOverride = Misc.getShipPickMode(market);
        params.modeOverride = FactionAPI.ShipPickMode.PRIORITY_THEN_ALL;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        if (fleet == null || fleet.isEmpty()) return null;

        fleet.setFaction(market.getFactionId(), true);
        fleet.setNoFactionInName(true);

        fleet.addEventListener(this);

//		PatrolAssignmentAIV2 ai = new PatrolAssignmentAIV2(fleet, custom);
//		fleet.addScript(ai);

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true, 0.3f);

        if (type == FleetFactory.PatrolType.FAST || type == FleetFactory.PatrolType.COMBAT)
        {
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR, true);
        }

        String postId = Ranks.POST_PATROL_COMMANDER;
        String rankId = Ranks.SPACE_COMMANDER;
        switch (type)
        {
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

        market.getContainingLocation().addEntity(fleet);
        fleet.setFacing((float) Math.random() * 360f);
        // this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
        fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

        fleet.addScript(new PatrolAssignmentAIV4(fleet, route));

        //market.getContainingLocation().addEntity(fleet);
        //fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

        if (custom.spawnFP <= 0)
        {
            custom.spawnFP = fleet.getFleetPoints();
        }

        fleet.addTag("boggledRemnantStationPatrol");
        return fleet;
    }

    public String getRouteSourceId() {
        return getMarket().getId() + "_" + boggledTools.BoggledIndustries.remnantStationIndustryID;
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
    protected void buildingFinished()
    {
        super.buildingFinished();

        tracker.forceIntervalElapsed();
    }

    @Override
    protected void upgradeFinished(Industry previous)
    {
        super.upgradeFinished(previous);

        tracker.forceIntervalElapsed();
    }

    @Override
    public boolean isAvailableToBuild()
    {
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.remnantStationEnabled) && super.isAvailableToBuild() && Global.getSector().getPlayerStats().getSkillLevel(Skills.AUTOMATED_SHIPS) != 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean showWhenUnavailable()
    {
        if(!boggledTools.isResearched(this.getId()))
        {
            return false;
        }

        return boggledTools.getBooleanSetting(boggledTools.BoggledSettings.remnantStationEnabled) && Global.getSector().getPlayerStats().getSkillLevel(Skills.AUTOMATED_SHIPS) == 0;
    }

    @Override
    public String getUnavailableReason()
    {
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.remnantStationEnabled) && Global.getSector().getPlayerStats().getSkillLevel(Skills.AUTOMATED_SHIPS) == 0)
        {
            return "You lack the Automated Ships skill.";
        }

        return "Error in getUnavailableReason() in Remnant Station. Please report this to boggled on the forums.";
    }

    @Override
    public void apply()
    {
        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);

        // Replaces the super.apply(false) call in OrbitalStation
        this.updateSupplyAndDemandModifiers();

        this.applyAICoreModifiers();
        this.applyImproveModifiers();
        if (this instanceof MarketImmigrationModifier)
        {
            this.market.addTransientImmigrationModifier((MarketImmigrationModifier)this);
        }

        if (this.special != null)
        {
            InstallableItemEffect effect = (InstallableItemEffect) ItemEffectsRepo.ITEM_EFFECTS.get(this.special.getId());
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

        int size = 7;
        this.market.getStability().modifyFlat(this.getModId(), 3.0f, "Autonomous AI battlestation");
        this.applyIncomeAndUpkeep((float)size);
        this.demand("supplies", size);

        this.market.getStats().getDynamic().getMod("ground_defenses_mod").modifyMult(this.getModId(), 3.0f, "Autonomous AI battlestation");
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
        this.unmodifyStabilityWithBaseMod();
        this.matchCommanderToAICore((String)null);
        this.market.getStats().getDynamic().getMod("ground_defenses_mod").unmodifyMult(this.getModId());

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
    }

    @Override
    protected int getBaseStabilityMod()
    {
        return 3;
    }

    @Override
    protected float getCR() {
        float deficit = (float)(Integer)this.getMaxDeficit("supplies").two;
        float demand = (float)Math.max(0, this.getDemand("supplies").getQuantity().getModifiedInt());
        if (deficit < 0.0F) {
            deficit = 0.0F;
        }

        if (demand < 1.0F) {
            demand = 1.0F;
            deficit = 0.0F;
        }

        float q = Misc.getShipQuality(this.market);
        if (q < 0.0F) {
            q = 0.0F;
        }

        if (q > 1.0F) {
            q = 1.0F;
        }

        float d = (demand - deficit) / demand;
        if (d < 0.0F) {
            d = 0.0F;
        }

        if (d > 1.0F) {
            d = 1.0F;
        }

        float cr = 0.5F + 0.5F * Math.min(d, q);
        if (cr > 1.0F) {
            cr = 1.0F;
        }

        return cr;
    }

    @Override
    protected Pair<String, Integer> getStabilityAffectingDeficit()
    {
        return this.getMaxDeficit("supplies");
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
            float bonus = 2.0f;

            this.addGroundDefensesImpactSectionBoggledRemnantStation(tooltip, bonus, "supplies");
        }
    }

    protected void addStabilityPostDemandSectionBoggledRemnantStation(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode)
    {
        Color h = Misc.getHighlightColor();
        float opad = 10.0F;
        MutableStat fake = new MutableStat(0.0F);
        int stabilityMod = this.getBaseStabilityMod();
        int stabilityPenalty = this.getStabilityPenalty();
        if (stabilityPenalty > stabilityMod) {
            stabilityPenalty = stabilityMod;
        }

        String str = getDeficitText((String)this.getStabilityAffectingDeficit().one);
        //fake.modifyFlat("1", (float)stabilityMod, this.getNameForModifier());
        fake.modifyFlat("1", (float)stabilityMod, "Autonomous AI Battlestation");
        if (stabilityPenalty != 0) {
            fake.modifyFlat("2", (float)(-stabilityPenalty), str);
        }

        int total = stabilityMod - stabilityPenalty;
        String totalStr = "+" + total;
        if (total < 0) {
            totalStr = "" + total;
            h = Misc.getNegativeHighlightColor();
        }

        float pad = 3.0F;
        if (total >= 0) {
            tooltip.addPara("Stability bonus: %s", opad, h, totalStr);
        } else {
            tooltip.addPara("Stability penalty: %s", opad, h, totalStr);
        }

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
                return null;
            }
        });
    }

    protected void addGroundDefensesImpactSectionBoggledRemnantStation(TooltipMakerAPI tooltip, float bonus, String... commodities)
    {
        Color h = Misc.getHighlightColor();
        float opad = 10.0F;
        MutableStat fake = new MutableStat(1.0F);
        //fake.modifyFlat("1", bonus, this.getNameForModifier());
        fake.modifyFlat("1", bonus, "Autonomous AI Battlestation");
        float mult;
        String totalStr;
        if (commodities != null) {
            mult = this.getDeficitMult(commodities);
            if (mult != 1.0F) {
                totalStr = (String)this.getMaxDeficit(commodities).one;
                fake.modifyFlat("2", -(1.0F - mult) * bonus, getDeficitText(totalStr));
            }
        }

        mult = Misc.getRoundedValueFloat(fake.getModifiedValue());
        totalStr = "×" + mult;
        if (mult < 1.0F) {
            h = Misc.getNegativeHighlightColor();
        }

        float pad = 3.0F;
        tooltip.addPara("Ground defense strength: %s", opad, h, totalStr);
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
        return true;
    }
}
