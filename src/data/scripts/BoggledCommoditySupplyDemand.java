package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;

import java.awt.*;
import java.util.ArrayList;

public class BoggledCommoditySupplyDemand {
    public static abstract class CommoditySupplyAndDemand {
        protected final String[] enableSettings;
        protected String commodity;

        public CommoditySupplyAndDemand(String[] enableSettings, String commodity) {
            this.enableSettings = enableSettings;
            this.commodity = commodity;
        }

        protected abstract void applySupplyDemandImpl(BaseIndustry industry);
        protected abstract void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode);

        public boolean isEnabled() {
            return boggledTools.optionsAllowThis(enableSettings);
        }

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

            this.addPostDemandSectionImpl(industryTooltip, industry, tooltip, hasDemand, mode);
        }
    }

    public abstract static class Flat extends CommoditySupplyAndDemand {
        protected int quantity;
        public Flat(String[] enableSettings, String commodity, int quantity) {
            super(enableSettings, commodity);
            this.quantity = quantity;
        }
    }
    public static class FlatDemand extends Flat {
        public FlatDemand(String[] enableSettings, String commodity, int quantity) {
            super(enableSettings, commodity, quantity);
        }

        @Override
        protected void applySupplyDemandImpl(BaseIndustry industry) {
            industry.demand(commodity, quantity);
        }

        @Override
        public void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            float pad = 10.0f;
            Color highlight = Misc.getHighlightColor();
            String commodityTooltip = Global.getSettings().getCommoditySpec(commodity).getName();
            tooltip.addPara(industryTooltip + " always demand %s " + commodityTooltip + " regardless of market size.", pad, highlight, Integer.toString(quantity));
        }
    }

    public static class FlatSupply extends Flat {
        public FlatSupply(String[] enableSettings, String commodity, int quantity) {
            super(enableSettings, commodity, quantity);
        }

        @Override
        protected void applySupplyDemandImpl(BaseIndustry industry) { industry.supply(commodity, quantity); }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        }
    }

    public abstract static class MarketSize extends CommoditySupplyAndDemand {
        protected int quantityOffset;
        public MarketSize(String[] enableSettings, String commodity, int quantityOffset) {
            super(enableSettings, commodity);
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
        public MarketSizeDemand(String[] enableSettings, String commodity, int quantityOffset) {
            super(enableSettings, commodity, quantityOffset);
        }

        @Override
        protected void applySupplyDemandMarketSize(BaseIndustry industry, int adjustedQuantity) {
            industry.demand(commodity, adjustedQuantity);
        }

        @Override
        public void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
            float pad = 10.0f;
            String commodityTooltip = Global.getSettings().getCommoditySpec(commodity).getName();
            tooltip.addPara(industryTooltip + " demand for " + commodityTooltip + " increases with market size", pad);
        }
    }

    public static class MarketSizeSupply extends MarketSize {
        public MarketSizeSupply(String[] enableSettings, String commodity, int quantityOffset) {
            super(enableSettings, commodity, quantityOffset);
        }

        @Override
        protected void applySupplyDemandMarketSize(BaseIndustry industry, int adjustedQuantity) {
            industry.supply(commodity, adjustedQuantity);
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {

        }
    }

    public abstract static class ConditionModify extends CommoditySupplyAndDemand {
        String modId;
        ArrayList<Pair<String, Integer>> conditionAndQuantityOffsets;
        public ConditionModify(String[] enableSettings, String commodity, String modId, ArrayList<Pair<String, Integer>> conditionAndQuantityOffsets) {
            super(enableSettings, commodity);
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
        public ConditionModifyDemand(String[] enableSettings, String commodity, String modId, ArrayList<Pair<String, Integer>> conditionAndQuantityOffsets) {
            super(enableSettings, commodity, modId, conditionAndQuantityOffsets);
        }

        @Override
        public void applySupplyDemandCondition(BaseIndustry industry, String conditionId, int quantityOffset) {
            industry.demand(modId, commodity, quantityOffset, Misc.ucFirst(Global.getSettings().getMarketConditionSpec(conditionId).getName()));
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        }
    }

    public static class ConditionModifySupply extends ConditionModify {
        public ConditionModifySupply(String[] enableSettings, String commodity, String modId, ArrayList<Pair<String, Integer>> conditionAndQuantityOffsets) {
            super(enableSettings, commodity, modId, conditionAndQuantityOffsets);
        }

        @Override
        public void applySupplyDemandCondition(BaseIndustry industry, String conditionId, int quantityOffset) {
            industry.supply(modId, commodity, quantityOffset, Misc.ucFirst(Global.getSettings().getMarketConditionSpec(conditionId).getName()));
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        }
    }

    public static class PlayerMarketSizeElseFlatDemand extends CommoditySupplyAndDemand {
        int quantity;
        public PlayerMarketSizeElseFlatDemand(String[] enableSettings, String commodity, int quantity) {
            super(enableSettings, commodity);
            this.quantity = quantity;
        }

        @Override
        protected void applySupplyDemandImpl(BaseIndustry industry) {
            if (industry.getMarket().isPlayerOwned()) {
                industry.demand(commodity, industry.getMarket().getSize());
            } else {
                industry.demand(commodity, quantity);
            }
        }

        @Override
        protected void addPostDemandSectionImpl(String industryTooltip, BaseIndustry industry, TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {

        }
    }
}
