package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import data.campaign.econ.industries.BoggledIndustryInterface;
import kotlin.Triple;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class BoggledCommoditySupplyDemand {
    public static abstract class CommodityDemandShortageEffect {
        String id;
        String[] enableSettings;
        ArrayList<String> commoditiesDemanded;

        public CommodityDemandShortageEffect(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded) {
            this.id = id;
            this.enableSettings = enableSettings;
            this.commoditiesDemanded = commoditiesDemanded;
        }

        protected abstract void applyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded);

        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded, float pad) {}

        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

        public void applyShortageEffect(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
            if (!isEnabled()) {
                return;
            }
            applyShortageEffectImpl(industry, industryInterface, commoditiesDemanded.toArray(new String[0]));
        }

        public void addPostDemandSection(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            if (!isEnabled()) {
                return;
            }

            this.addPostDemandSectionImpl(industryTooltip, industry, tooltip, hasDemand, mode, commoditiesDemanded.toArray(new String[0]), 10.0f);
        }
    }

    public static class DeficitToInactive extends CommodityDemandShortageEffect {
        public DeficitToInactive(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded) {
            super(id, enableSettings, commoditiesDemanded);
        }

        @Override
        public void applyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            industryInterface.setFunctional(true);
            if (industry.getMaxDeficit(commoditiesDemanded).two > 0) {
                industryInterface.setFunctional(false);
            }
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded, float pad) {
            if (industry.getMaxDeficit(commoditiesDemanded).two <= 0) {
                return;
            }
            String shortage = boggledTools.buildCommodityList(industry, commoditiesDemanded);
            tooltip.addPara(industryTooltip + " is inactive due to a shortage of " + shortage, Misc.getNegativeHighlightColor(), pad);
        }
    }

    public static class DeficitToCommodity extends CommodityDemandShortageEffect {
        ArrayList<String> commoditiesDeficited;

        DeficitToCommodity(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, ArrayList<String> commoditiesDeficited) {
            super(id, enableSettings, commoditiesDemanded);
            this.commoditiesDeficited = commoditiesDeficited;
        }

        @Override
        public void applyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            Pair<String, Integer> deficit = industry.getMaxDeficit(commoditiesDemanded);
            industryInterface.applyDeficitToProduction(1, deficit, commoditiesDeficited.toArray(new String[0]));
        }
    }

    public static class DeficitMultiplierToUpkeep extends CommodityDemandShortageEffect {
        float upkeepMultiplier;

        public DeficitMultiplierToUpkeep(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, float upkeepMultiplier) {
            super(id, enableSettings, commoditiesDemanded);
            this.upkeepMultiplier = upkeepMultiplier;
        }

        @Override
        public void applyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            List<Pair<String, Integer>> deficits = industry.getAllDeficit(commoditiesDemanded);
            if (deficits.isEmpty()) {
                industry.getUpkeep().unmodifyMult(id + "_deficit");
            } else {
                industry.getUpkeep().modifyMult(id + "_deficit", upkeepMultiplier, "Commodity shortage");
            }
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded, float pad) {
            String shortage = boggledTools.buildCommodityList(industry, commoditiesDemanded);
            String upkeepMultiplierString = Float.toString(upkeepMultiplier);
            tooltip.addPara(industryTooltip + " upkeep is increased by %s due to a shortage of " + shortage + ".", pad, Misc.getHighlightColor(), upkeepMultiplierString);
        }
    }

    public static class ConditionMultiplierToUpkeep extends CommodityDemandShortageEffect {
        ArrayList<Pair<String, Float>> conditionUpkeepMultipliers;

        public ConditionMultiplierToUpkeep(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, ArrayList<Pair<String, Float>> conditionUpkeepMultipliers) {
            super(id, enableSettings, commoditiesDemanded);
            this.conditionUpkeepMultipliers = conditionUpkeepMultipliers;
        }

        @Override
        protected void applyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (Pair<String, Float> conditionUpkeepMultipler : conditionUpkeepMultipliers) {
                if (industry.getMarket().hasCondition(conditionUpkeepMultipler.one)) {
                    industry.getUpkeep().modifyMult(id, conditionUpkeepMultipler.two, Global.getSettings().getMarketConditionSpec(conditionUpkeepMultipler.one).getName());
                    return;
                }
            }
            industry.getUpkeep().unmodifyMult(id);
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded, float pad) {
            for (Pair<String, Float> conditionUpkeepMultipler : conditionUpkeepMultipliers) {
                if (!industry.getMarket().hasCondition(conditionUpkeepMultipler.one)) {
                    continue;
                }

                String upkeepMultiplierString = "x" + conditionUpkeepMultipler.two;
                String description = Global.getSettings().getMarketConditionSpec(conditionUpkeepMultipler.one).getName();
                tooltip.addPara(industryTooltip + " upkeep is increased by %s due to condition " + description + " present on " + industry.getMarket().getName() + ".", pad, Misc.getHighlightColor(), upkeepMultiplierString);
                return;
            }
        }
    }

    public static class TagMultiplierToUpkeep extends CommodityDemandShortageEffect {
        ArrayList<Triple<String, Float, String>> tagUpkeepMultiplierDescriptions;

        public TagMultiplierToUpkeep(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, ArrayList<Triple<String, Float, String>> tagUpkeepMultiplierDescriptions) {
            super(id, enableSettings, commoditiesDemanded);
            this.tagUpkeepMultiplierDescriptions = tagUpkeepMultiplierDescriptions;
        }

        @Override
        protected void applyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (Triple<String, Float, String> tagUpkeepMultiplerDescription : tagUpkeepMultiplierDescriptions) {
                if (industry.getMarket().hasTag(tagUpkeepMultiplerDescription.component1())) {
                    industry.getUpkeep().modifyMult(id, tagUpkeepMultiplerDescription.component2(), tagUpkeepMultiplerDescription.component3());
                }
            }
            industry.getUpkeep().unmodifyMult(id);
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded, float pad) {
            for (Triple<String, Float, String> tagUpkeepMultiplierDescription : tagUpkeepMultiplierDescriptions) {
                if (!industry.getMarket().hasCondition(tagUpkeepMultiplierDescription.component1())) {
                    continue;
                }

                String upkeepMultiplierString = Float.toString(tagUpkeepMultiplierDescription.component2());
                String description = tagUpkeepMultiplierDescription.component3();
                tooltip.addPara(industryTooltip + " upkeep is increased by %s due to " + industry.getMarket().getName() + " being " + description + ".", pad, Misc.getHighlightColor(), upkeepMultiplierString);
                return;
            }
        }
    }

    public static class IncomeBonusFromIndustry extends CommodityDemandShortageEffect {
        String industryId;
        float incomeMultiplier;

        public IncomeBonusFromIndustry(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String industryId, float incomeMultiplier) {
            super(id, enableSettings, commoditiesDemanded);
            this.industryId = industryId;
            this.incomeMultiplier = incomeMultiplier;
        }

        @Override
        protected void applyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            Industry industryFrom = industry.getMarket().getIndustry(industryId);
            if (industryFrom == null || !industryFrom.isFunctional()) {
                industry.getIncome().unmodifyMult(id);
                return;
            }
            industry.getIncome().modifyMult(id, incomeMultiplier, industryFrom.getCurrentName());
        }
    }

    public static abstract class CommoditySupplyAndDemand {
        protected final String id;
        protected final String[] enableSettings;
        protected String commodity;

        public CommoditySupplyAndDemand(String id, String[] enableSettings, String commodity) {
            this.id = id;
            this.enableSettings = enableSettings;
            this.commodity = commodity;
        }

        protected abstract void applySupplyDemandImpl(BaseIndustry industry);
        protected abstract void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, float pad);

        public String getCommodity() { return commodity; }
        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

        public void applySupplyDemand(BaseIndustry industry) {
            if (!isEnabled()) {
                return;
            }

            this.applySupplyDemandImpl(industry);
        }

        public void addPostDemandSection(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            if (!isEnabled()) {
                return;
            }

            this.addPostDemandSectionImpl(industryTooltip, industry, tooltip, hasDemand, mode, 10.0f);
        }

        public abstract boolean isDemand();
        public boolean isSupply() { return !isDemand(); }
    }

    public abstract static class Flat extends CommoditySupplyAndDemand {
        protected int quantity;
        public Flat(String id, String[] enableSettings, String commodity, int quantity) {
            super(id, enableSettings, commodity);
            this.quantity = quantity;
        }
    }
    public static class FlatDemand extends Flat {
        public FlatDemand(String id, String[] enableSettings, String commodity, int quantity) {
            super(id, enableSettings, commodity, quantity);
        }

        @Override
        protected void applySupplyDemandImpl(BaseIndustry industry) {
            industry.demand(id, commodity, quantity, BaseIndustry.BASE_VALUE_TEXT);
        }

        @Override
        public void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, float pad) {
            Color highlight = Misc.getHighlightColor();
            String commodityTooltip = Global.getSettings().getCommoditySpec(commodity).getName();
            tooltip.addPara(industryTooltip + " always demands %s " + commodityTooltip + " regardless of market size.", pad, highlight, Integer.toString(quantity));
        }

        @Override
        public boolean isDemand() { return true; }
    }

    public static class FlatSupply extends Flat {
        public FlatSupply(String id, String[] enableSettings, String commodity, int quantity) {
            super(id, enableSettings, commodity, quantity);
        }

        @Override
        protected void applySupplyDemandImpl(BaseIndustry industry) {
            industry.supply(id, commodity, quantity, BaseIndustry.BASE_VALUE_TEXT);
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, float pad) {
        }

        @Override
        public boolean isDemand() { return false; }
    }

    public abstract static class MarketSize extends CommoditySupplyAndDemand {
        protected int quantityOffset;
        public MarketSize(String id, String[] enableSettings, String commodity, int quantityOffset) {
            super(id, enableSettings, commodity);
            this.quantityOffset = quantityOffset;
        }

        protected abstract void applySupplyDemandMarketSize(BaseIndustry industry, int adjustedQuantity);

        @Override
        protected void applySupplyDemandImpl(BaseIndustry industry) {
            int adjustedQuantity = industry.getMarket().getSize() + quantityOffset;
            if (adjustedQuantity <= 0) {
                return;
            }
            applySupplyDemandMarketSize(industry, adjustedQuantity);
        }
    }

    public static class MarketSizeDemand extends MarketSize {
        public MarketSizeDemand(String id, String[] enableSettings, String commodity, int quantityOffset) {
            super(id, enableSettings, commodity, quantityOffset);
        }

        @Override
        protected void applySupplyDemandMarketSize(BaseIndustry industry, int adjustedQuantity) {
            industry.demand(id, commodity, adjustedQuantity, BaseIndustry.BASE_VALUE_TEXT);
        }

        @Override
        public void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, float pad) {
            String commodityTooltip = Global.getSettings().getCommoditySpec(commodity).getName();
            tooltip.addPara(industryTooltip + " demand for " + commodityTooltip + " increases with market size.", pad);
        }

        @Override
        public boolean isDemand() { return true; }
    }

    public static class MarketSizeSupply extends MarketSize {
        public MarketSizeSupply(String id, String[] enableSettings, String commodity, int quantityOffset) {
            super(id, enableSettings, commodity, quantityOffset);
        }

        @Override
        protected void applySupplyDemandMarketSize(BaseIndustry industry, int adjustedQuantity) {
            industry.supply(id, commodity, adjustedQuantity, BaseIndustry.BASE_VALUE_TEXT);
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, float pad) {

        }

        @Override
        public boolean isDemand() { return false; }
    }

    public abstract static class ConditionModify extends CommoditySupplyAndDemand {
        String modId;
        ArrayList<Pair<String, Integer>> conditionAndQuantityOffsets;
        public ConditionModify(String id, String[] enableSettings, String commodity, String modId, ArrayList<Pair<String, Integer>> conditionAndQuantityOffsets) {
            super(id, enableSettings, commodity);
            this.modId = modId;
            this.conditionAndQuantityOffsets = conditionAndQuantityOffsets;
        }

        public abstract void applySupplyDemandCondition(BaseIndustry industry, String conditionId, int quantityOffset);

        @Override
        public void applySupplyDemandImpl(BaseIndustry industry) {
            MarketAPI market = industry.getMarket();
            for (Pair<String, Integer> conditionAndQuantityOffset : conditionAndQuantityOffsets) {
                if (market.hasCondition(conditionAndQuantityOffset.one)) {
                    applySupplyDemandCondition(industry, conditionAndQuantityOffset.one, conditionAndQuantityOffset.two);
                }
            }
        }
    }

    public static class ConditionModifyDemand extends ConditionModify {
        public ConditionModifyDemand(String id, String[] enableSettings, String commodity, String modId, ArrayList<Pair<String, Integer>> conditionAndQuantityOffsets) {
            super(id, enableSettings, commodity, modId, conditionAndQuantityOffsets);
        }

        @Override
        public void applySupplyDemandCondition(BaseIndustry industry, String conditionId, int quantityOffset) {
            industry.demand(modId, commodity, quantityOffset, Misc.ucFirst(Global.getSettings().getMarketConditionSpec(conditionId).getName()));
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, float pad) {
        }

        @Override
        public boolean isDemand() { return true; }
    }

    public static class ConditionModifySupply extends ConditionModify {
        public ConditionModifySupply(String id, String[] enableSettings, String commodity, String modId, ArrayList<Pair<String, Integer>> conditionAndQuantityOffsets) {
            super(id, enableSettings, commodity, modId, conditionAndQuantityOffsets);
        }

        @Override
        public void applySupplyDemandCondition(BaseIndustry industry, String conditionId, int quantityOffset) {
            industry.supply(modId, commodity, quantityOffset, Misc.ucFirst(Global.getSettings().getMarketConditionSpec(conditionId).getName()));
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, float pad) {
        }

        @Override
        public boolean isDemand() { return false; }
    }

    public static class PlayerMarketSizeElseFlatDemand extends CommoditySupplyAndDemand {
        int quantity;
        public PlayerMarketSizeElseFlatDemand(String id, String[] enableSettings, String commodity, int quantity) {
            super(id, enableSettings, commodity);
            this.quantity = quantity;
        }

        @Override
        protected void applySupplyDemandImpl(BaseIndustry industry) {
            if (industry.getMarket().isPlayerOwned()) {
                industry.demand(id, commodity, industry.getMarket().getSize(), BaseIndustry.BASE_VALUE_TEXT);
            } else {
                industry.demand(id, commodity, quantity, BaseIndustry.BASE_VALUE_TEXT);
            }
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, float pad) {
            String commodityTooltip = Global.getSettings().getCommoditySpec(commodity).getName();
            if (industry.getMarket().isPlayerOwned()) {
                tooltip.addPara(industryTooltip + " demand for " + commodityTooltip + " increases with market size", pad);
            }
        }

        @Override
        public boolean isDemand() { return true; }
    }
}
