
package boggled.campaign.econ.conditions;

import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.Map;
import boggled.campaign.econ.boggledTools;

public class Cramped_Quarters extends BaseHazardCondition implements MarketImmigrationModifier {
    public Cramped_Quarters() { }

    public void advance(float amount)
    {
        super.advance(amount);
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.getWeight().modifyFlat(this.getModId(), this.getImmigrationBonus(), Misc.ucFirst(this.condition.getName().toLowerCase()));
    }

    private int getStationCrampedThreshold(MarketAPI market) {
        return boggledTools.getIntSetting(boggledTools.BoggledSettings.stationCrampedQuartersSizeGrowthReductionStarts) + boggledTools.getNumberOfStationExpansions(market);
    }

    protected float getImmigrationBonus() {
        MarketAPI market = this.market;
        int stationCrampedThreshold = getStationCrampedThreshold(market);
        if (market.getSize() < stationCrampedThreshold) {
            return 0F;
        }

        float baseCrampedQuartersPenalty = (float) boggledTools.getIntSetting("boggledStationCrampedQuartersBaseGrowthPenalty");

        // Needs to return a negative value to reduce growth, so it's multiplied by -1.0F.
        // At the cramped threshold, reduces growth by 1 x the penalty.
        // The growth penalty doubles each time the market size increases by one.
        return (float) Math.pow(2.0F, market.getSize() - stationCrampedThreshold) * -1.0F * baseCrampedQuartersPenalty;
    }

    @Override
    public void apply(String id) {
        super.apply(id);

        if(this.market == null)
        {
            return;
        }

        if(!this.market.hasCondition(Conditions.NO_ATMOSPHERE)) {
            // Adds the no atmosphere condition, then suppresses it so it won't increase hazard
            // market_conditions.csv overwrites the vanilla no_atmosphere condition
            // The only change made is to hide the icon on markets where primary entity has station tag
            // This is done so refining and fuel production can slot the special items
            // Hopefully Alex will fix the no_atmosphere detection in the future so this hack can be removed
            market.addCondition(Conditions.NO_ATMOSPHERE);
        }

        if(this.market.hasCondition(Conditions.NO_ATMOSPHERE))
        {
            this.market.suppressCondition(Conditions.NO_ATMOSPHERE);
        }

        if(this.market == null || this.market.getTariff() == null || this.market.getFactionId() == null || this.market.getHazard() == null || this.market.getAccessibilityMod() == null) {
            // boggledTools.removeCondition(this.market, "cramped_quarters");
            // Don't try to remove this condition here because it will throw an exception under some unusual circumstances.
            return;
        }

        if(this.market.getTariff().getBaseValue() == 0.0f && this.market.getTariff().getModifiedValue() == 0.0f) {
            this.market.getTariff().modifyFlat("base_tariff_for_station", 0.30f);
        }

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.stationCrampedQuartersEnabled)) {
            // Market growth modifier
            this.market.addTransientImmigrationModifier(this);
        }

        if(boggledTools.getIntSetting("boggledStationHazardRatingModifier") != 0) {
            // Market hazard modifier
            float hazard = (float)boggledTools.getIntSetting("boggledStationHazardRatingModifier") / 100.0F;
            this.market.getHazard().modifyFlat(id, hazard, "Base station hazard");
        }

        if(boggledTools.getIntSetting("boggledStationAccessibilityBoost") != 0) {
            // Accessibility boost
            float access = (float)boggledTools.getIntSetting("boggledStationAccessibilityBoost") / 100.0F;
            this.market.getAccessibilityMod().modifyFlat(this.getModId(), access, "Space station");
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);

        if(this.market == null || this.market.getTariff() == null || this.market.getFactionId() == null || this.market.getHazard() == null || this.market.getAccessibilityMod() == null) {
            // boggledTools.removeCondition(this.market, "cramped_quarters");
            // Don't try to remove this condition here because it will throw an exception under some unusual circumstances.
            return;
        }

        this.market.getTariff().unmodifyFlat("base_tariff_for_station");

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.stationCrampedQuartersEnabled)) {
            this.market.removeTransientImmigrationModifier(this);
        }

        if(boggledTools.getIntSetting("boggledStationHazardRatingModifier") != 0) {
            this.market.getHazard().unmodifyFlat(id);
        }

        if(boggledTools.getIntSetting("boggledStationAccessibilityBoost") != 0) {
            this.market.getAccessibilityMod().unmodifyFlat("Space station");
        }
    }

    @Override
    public Map<String, String> getTokenReplacements() { return super.getTokenReplacements(); }

    @Override
    public boolean showIcon() {
        return boggledTools.getBooleanSetting(boggledTools.BoggledSettings.stationCrampedQuartersEnabled);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara("Population growth reduction at this station begins at market size %s and gets worse if the station continues to grow further beyond that limit.", 10.0F, Misc.getHighlightColor(), new String[]{getStationCrampedThreshold(this.market) + ""});

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.stationCrampedQuartersPlayerCanPayToIncreaseStationSize))
        {
            tooltip.addPara("Stations can be expanded to increase the maximum number of residents. Number of times this station has been expanded: %s", 10.0F, Misc.getHighlightColor(), boggledTools.getNumberOfStationExpansions(this.market) + "");
        }

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.stationCrampedQuartersPlayerCanPayToIncreaseStationSize) && boggledTools.getBooleanSetting(boggledTools.BoggledSettings.stationProgressIncreaseInCostsToExpandStation))
        {
            tooltip.addPara("Station expansions become more progressively more expensive as the size of the station grows. Each new expansion is twice the cost of the previous one.", 10.0F, Misc.getHighlightColor(), boggledTools.getNumberOfStationExpansions(this.market) + "");
        }
    }
}
