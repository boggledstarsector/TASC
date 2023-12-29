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

public class Boggled_Stellar_Reflector_Array extends BaseIndustry {
    private final BoggledCommonIndustry thisIndustry;

    public Boggled_Stellar_Reflector_Array() {
        super();
        thisIndustry = boggledTools.getIndustryProject("stellar_reflector_array");
    }

    @Override
    public void startBuilding() {
        super.startBuilding();
        thisIndustry.startBuilding(this);
    }

    @Override
    public void startUpgrading() {
        super.startUpgrading();
        thisIndustry.startUpgrading(this);
    }

//    @Override
//    protected void buildingFinished() {
//        super.buildingFinished();
//        thisIndustry.buildingFinished(this);
//    }

    @Override
    protected void upgradeFinished(Industry previous) {
        super.upgradeFinished(previous);
        thisIndustry.upgradeFinished(this, previous);
    }

    @Override
    public void finishBuildingOrUpgrading() {
        super.finishBuildingOrUpgrading();
        thisIndustry.finishBuildingOrUpgrading(this);
    }

    @Override
    public boolean isBuilding() { return thisIndustry.isBuilding(this); }

    @Override
    public boolean isUpgrading() { return thisIndustry.isUpgrading(this); }

    @Override
    public float getBuildOrUpgradeProgress() { return thisIndustry.getBuildOrUpgradeProgress(this); }

    @Override
    public String getBuildOrUpgradeDaysText() { return thisIndustry.getBuildOrUpgradeDaysText(this); }

    @Override
    public String getBuildOrUpgradeProgressText() { return thisIndustry.getBuildOrUpgradeProgressText(this); }

    @Override
    public boolean isAvailableToBuild() { return thisIndustry.isAvailableToBuild(this); }

    @Override
    public boolean showWhenUnavailable() { return thisIndustry.showWhenUnavailable(this); }

    @Override
    public String getUnavailableReason() { return thisIndustry.getUnavailableReason(this); }

//    @Override
//    public void advance(float amount) {
//        super.advance(amount);
//        thisIndustry.advance(amount, this);
//    }

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
    public void apply() {
        super.apply(true);
        thisIndustry.apply(this);

        boolean shortage = false;
        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainTechContentEnabled) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.domainArchaeologyEnabled))
        {
            Pair<String, Integer> deficit = this.getMaxDeficit(boggledTools.BoggledCommodities.domainArtifacts);
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

    void createMirrorsOrShades(MarketAPI market) {
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

        float orbitRadius = market.getPrimaryEntity().getRadius() + 80f;
        float orbitDays = market.getPrimaryEntity().getCircularOrbitPeriod();
        float orbitDaysNotStar = market.getPrimaryEntity().getCircularOrbitPeriod() / 10;

        SectorEntityToken orbitFocus = market.getPrimaryEntity().getOrbitFocus();

        ArrayList<Pair<String, String>> idNamePairs = mirrorsOrShades ? mirrorIdNamePairs : shadeIdNamePairs;
        String entityType = mirrorsOrShades ? "stellar_mirror" : "stellar_shade";
        String customDescriptionId = mirrorsOrShades ? "stellar_mirror" : "stellar_shade";
        ArrayList<Float> orbitAngles = mirrorsOrShades ? mirrorAnglesOrbitingStar : shadeAnglesOrbitingStar;
        float orbitPeriod = orbitDays;
        if (!(orbitFocus != null && orbitFocus.isStar())) {
            orbitAngles = mirrorAndShadeAnglesOrbitingNotStar;
            orbitPeriod = orbitDaysNotStar;
        }

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
        thisIndustry.buildingFinished(this);

        MarketAPI market = this.market;
        boggledTools.addCondition(market, Conditions.SOLAR_ARRAY);

        createMirrorsOrShades(market);
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
        thisIndustry.addPostDemandSection(this, tooltip, hasDemand, mode);
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

