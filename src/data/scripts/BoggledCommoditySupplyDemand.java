package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import data.campaign.econ.industries.BoggledIndustryInterface;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import static java.util.Arrays.asList;

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

        protected void unapplyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {}

        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded, float pad) {}

        protected Pair<String, List<String>> addAICoreDescriptionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode, String prefix, String coreId) {
            return new Pair<String, List<String>>("", new ArrayList<String>());
        }

        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

        public void applyShortageEffect(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
            if (!isEnabled()) {
                return;
            }
            applyShortageEffectImpl(industry, industryInterface, commoditiesDemanded.toArray(new String[0]));
        }

        public void unapplyShortageEffect(BaseIndustry industry, BoggledIndustryInterface industryInterface) {
            if (!isEnabled()) {
                return;
            }
            unapplyShortageEffectImpl(industry, industryInterface, commoditiesDemanded.toArray(new String[0]));
        }

        public void addPostDemandSection(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            if (!isEnabled()) {
                return;
            }

            this.addPostDemandSectionImpl(industryTooltip, industry, tooltip, hasDemand, mode, commoditiesDemanded.toArray(new String[0]), 10.0f);
        }

        public Pair<String, List<String>> addAICoreDescription(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode, String coreType, String coreId) {
            if (!isEnabled()) {
                return new Pair<String, List<String>>("", new ArrayList<String>());
            }

            String prefix = coreType + "-level AI core currently assigned.";
            if (mode == Industry.AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == Industry.AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
                prefix = coreType + "-level AI core. ";
            }

            return this.addAICoreDescriptionImpl(industryTooltip, industry, tooltip, mode, prefix, coreId);
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
        public static class Data {
            String conditionId;
            float upkeepMultiplier;
            public Data(String conditionId, float upkeepMultiplier) {
                this.conditionId = conditionId;
                this.upkeepMultiplier = upkeepMultiplier;
            }
        }
        List<Data> data;

        public ConditionMultiplierToUpkeep(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, List<Data> data) {
            super(id, enableSettings, commoditiesDemanded);
            this.data = data;
        }

        @Override
        protected void applyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (Data datum : data) {
                if (industry.getMarket().hasCondition(datum.conditionId)) {
                    industry.getUpkeep().modifyMult(id, datum.upkeepMultiplier, Global.getSettings().getMarketConditionSpec(datum.conditionId).getName());
                    return;
                }
            }
            industry.getUpkeep().unmodifyMult(id);
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded, float pad) {
            for (Data datum : data) {
                if (!industry.getMarket().hasCondition(datum.conditionId)) {
                    continue;
                }

                String upkeepMultiplierString = "x" + datum.upkeepMultiplier;
                String description = Global.getSettings().getMarketConditionSpec(datum.conditionId).getName();
                tooltip.addPara(industryTooltip + " upkeep is increased by %s due to condition " + description + " present on " + industry.getMarket().getName() + ".", pad, Misc.getHighlightColor(), upkeepMultiplierString);
                return;
            }
        }
    }

    public static class TagMultiplierToUpkeep extends CommodityDemandShortageEffect {
        public static class Data {
            String tag;
            String description;
            float upkeepMultiplier;

            public Data(String tag, String description, float upkeepMultiplier) {
                this.tag = tag;
                this.description = description;
                this.upkeepMultiplier = upkeepMultiplier;
            }
        }
        List<Data> data;

        public TagMultiplierToUpkeep(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, List<Data> data) {
            super(id, enableSettings, commoditiesDemanded);
            this.data = data;
        }

        @Override
        protected void applyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (Data datum : data) {
                if (industry.getMarket().hasTag(datum.tag)) {
                    industry.getUpkeep().modifyMult(id, datum.upkeepMultiplier, datum.description);
                }
            }
            industry.getUpkeep().unmodifyMult(id);
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode, String[] commoditiesDemanded, float pad) {
            for (Data datum : data) {
                if (!industry.getMarket().hasTag(datum.tag)) {
                    continue;
                }

                String upkeepMultiplierString = Float.toString(datum.upkeepMultiplier);
                String description = datum.description;
                tooltip.addPara(industryTooltip + " upkeep is increased by %s due to " + industry.getMarket().getName() + " being " + description + ".", pad, Misc.getHighlightColor(), upkeepMultiplierString);
                return;
            }
        }
    }

    public static class IncomeBonusToIndustry extends CommodityDemandShortageEffect {
        public static class Data {
            String industryId;
            float incomeMultiplier;
            Data(String industryId, float incomeMultiplier) {
                this.industryId = industryId;
                this.incomeMultiplier = incomeMultiplier;
            }
        }
        List<Data> data;

        public IncomeBonusToIndustry(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, List<Data> data) {
            super(id, enableSettings, commoditiesDemanded);
            this.data = data;
        }

        @Override
        protected void applyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            if (!industry.isFunctional()) {
                unapplyShortageEffectImpl(industry, industryInterface, commoditiesDemanded);
                return;
            }
            for (Data datum : data) {
                Industry industryTo = industry.getMarket().getIndustry(datum.industryId);
                if (industryTo == null) {
                    return;
                }
                industryTo.getIncome().modifyMult(id, datum.incomeMultiplier, industry.getCurrentName());
            }
        }

        @Override
        protected void unapplyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (Data datum : data) {
                Industry industryTo = industry.getMarket().getIndustry(datum.industryId);
                if (industryTo == null) {
                    return;
                }
                industryTo.getIncome().unmodifyMult(id);
            }
        }
    }

    public static class SupplyBonusWithDeficitToIndustry extends CommodityDemandShortageEffect {
        public static class Data {
            String industryId;
            int flatBonus;
            Data(String industryId, int flatBonus) {
                this.industryId = industryId;
                this.flatBonus = flatBonus;
            }
        }
        List<Data> data;

        public static class AICoreData {
            String description;
            Map<String, Integer> bonus;
            AICoreData(String description, Map<String, Integer> bonus) {
                this.description = description;
                this.bonus = bonus;
            }
        }
        AICoreData aiCoreData;

        public SupplyBonusWithDeficitToIndustry(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, List<Data> data, AICoreData aiCoreData) {
            super(id, enableSettings, commoditiesDemanded);
            this.aiCoreData = aiCoreData;
            this.data = data;
        }

        @Override
        protected void applyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            if (!industry.isFunctional()) {
                unapplyShortageEffectImpl(industry, industryInterface, commoditiesDemanded);
                return;
            }

            int aiCoreBonus = 0;
            if (aiCoreData.bonus.containsKey(industry.getAICoreId())) {
                aiCoreBonus = aiCoreData.bonus.get(industry.getAICoreId());
            }
            aiCoreBonus -= Math.max(industry.getMaxDeficit(commoditiesDemanded).two, 0);
            aiCoreBonus = Math.max(aiCoreBonus, 0);

            for (Data datum : data) {
                Industry industryTo = industry.getMarket().getIndustry(datum.industryId);
                if (industryTo == null) {
                    continue;
                }

                int bonus = aiCoreBonus + datum.flatBonus;
                for (MutableCommodityQuantity c : industryTo.getAllSupply()) {
                    industryTo.getSupply(c.getCommodityId()).getQuantity().modifyFlat(id, bonus, "Description");
                }
            }
        }

        @Override
        protected void unapplyShortageEffectImpl(BaseIndustry industry, BoggledIndustryInterface industryInterface, String[] commoditiesDemanded) {
            for (Data datum : data) {
                Industry industryTo = industry.getMarket().getIndustry(datum.industryId);
                if (industryTo == null) {
                    continue;
                }

                for (MutableCommodityQuantity c : industryTo.getAllSupply()) {
                    industryTo.getSupply(c.getCommodityId()).getQuantity().unmodifyFlat(id);
                }
            }
        }

        @Override
        protected Pair<String, List<String>> addAICoreDescriptionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, Industry.AICoreDescriptionMode mode, String prefix, String coreId) {
            Map<String, String> tokenReplacements = getTokenReplacements(industry.getMarket(), coreId);
            String replaced = boggledTools.doTokenReplacement(aiCoreData.description, tokenReplacements);
            String highlights = boggledTools.doTokenReplacement("$aiCoreBonus", tokenReplacements);

            return new Pair<String, List<String>>(replaced, new ArrayList<>(asList(highlights)));
        }

        private Map<String, String> getTokenReplacements(MarketAPI market, String aiCoreId) {
            Map<String, String> ret = boggledTools.getTokenReplacements(market);
            Integer bonus = aiCoreData.bonus.get(aiCoreId);
            if (bonus != null) {
                ret.put("$aiCoreBonus", Integer.toString(bonus));
            }
            List<String> industries = new ArrayList<>();
            for (Data datum : data) {
                industries.add(Global.getSettings().getIndustrySpec(datum.industryId).getName().toLowerCase());
            }
            ret.put("$industryTo", Misc.getAndJoined(industries.toArray(new String[0])));

            return ret;
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
        List<Pair<String, Integer>> conditionAndQuantityOffsets;
        public ConditionModify(String id, String[] enableSettings, String commodity, String modId, List<Pair<String, Integer>> conditionAndQuantityOffsets) {
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
        public ConditionModifyDemand(String id, String[] enableSettings, String commodity, String modId, List<Pair<String, Integer>> conditionAndQuantityOffsets) {
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
        public ConditionModifySupply(String id, String[] enableSettings, String commodity, String modId, List<Pair<String, Integer>> conditionAndQuantityOffsets) {
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
