package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import data.campaign.econ.boggledTools;

import java.util.*;
import java.util.List;

public class BoggledCommoditySupplyDemand {

    public static abstract class CommoditySupply {
        protected final String id;
        protected final String[] enableSettings;
        protected String commodity;

        public CommoditySupply(String id, String[] enableSettings, String commodity) {
            this.id = id;
            this.enableSettings = enableSettings;
            this.commodity = commodity;
        }

        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

        public abstract void applyImpl(BaseIndustry industry);

        public void apply(BaseIndustry industry) {
            if (!isEnabled()) {
                return;
            }

            applyImpl(industry);
        }
    }

    public static class FlatSupply extends CommoditySupply {
        int quantity;

        public FlatSupply(String id, String[] enableSettings, String commodity, int quantity) {
            super(id, enableSettings, commodity);
            this.quantity = quantity;
        }

        @Override
        public void applyImpl(BaseIndustry industry) {
            industry.supply(id, commodity, quantity, industry.getCurrentName());
        }
    }

    public static class MarketSizeSupply extends CommoditySupply {
        int quantityOffset;

        public MarketSizeSupply(String id, String[] enableSettings, String commodity, int quantityOffset) {
            super(id, enableSettings, commodity);
            this.quantityOffset = quantityOffset;
        }

        @Override
        public void applyImpl(BaseIndustry industry) {
            int adjustedQuantity = Math.max(0, industry.getMarket().getSize() + quantityOffset);
            industry.supply(id, commodity, adjustedQuantity, industry.getCurrentName());
        }
    }

    public static class CommodityDemandPara {
        public String prefix;
        public String suffix;
        public Set<String> commodities = new LinkedHashSet<>();
        public List<String> highlights = new ArrayList<>();

        CommodityDemandPara(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        void addCommodity(String commodityString) {
            commodities.add(commodityString);
        }

        void addHighlight(String highlight) {
            highlights.add(highlight);
        }
    }

    public static abstract class CommodityDemand {
        protected final String id;
        protected final String[] enableSettings;
        protected String commodity;

        public CommodityDemand(String id, String[] enableSettings, String commodity) {
            this.id = id;
            this.enableSettings = enableSettings;
            this.commodity = commodity;
        }

        public boolean isEnabled() { return boggledTools.optionsAllowThis(enableSettings); }

        protected abstract void applyImpl(BaseIndustry industry);
        protected abstract void addPostDemandInfoImpl(Map<String, BoggledCommoditySupplyDemand.CommodityDemandPara> demandTypeToCommodity, BaseIndustry industry, String industryTooltip);

        public void apply(BaseIndustry industry) {
            if (!isEnabled()) {
                return;
            }

            applyImpl(industry);
        }

        public void addPostDemandInfo(Map<String, BoggledCommoditySupplyDemand.CommodityDemandPara> demandTypeToCommodity, BaseIndustry industry) {
            if (!isEnabled()) {
                return;
            }

            addPostDemandInfoImpl(demandTypeToCommodity, industry, industry.getCurrentName());
        }
    }

    public static class FlatDemand extends CommodityDemand {
        int quantity;

        public FlatDemand(String id, String[] enableSettings, String commodity, int quantity) {
            super(id, enableSettings, commodity);
            this.quantity = quantity;
        }

        @Override
        protected void applyImpl(BaseIndustry industry) {
            industry.demand(id, commodity, quantity, industry.getCurrentName());
        }

        @Override
        protected void addPostDemandInfoImpl(Map<String, BoggledCommoditySupplyDemand.CommodityDemandPara> demandTypeToCommodity, BaseIndustry industry, String industryTooltip) {
            if (!demandTypeToCommodity.containsKey("FlatDemand")) {
                demandTypeToCommodity.put("FlatDemand", new CommodityDemandPara(industryTooltip + " always demands ", " regardless of market size."));
            }
            CommodityDemandPara para = demandTypeToCommodity.get("FlatDemand");
            para.addCommodity(quantity + " " + Global.getSettings().getCommoditySpec(commodity).getLowerCaseName());
            para.addHighlight(Integer.toString(quantity));
        }
    }

    public static class MarketSizeDemand extends CommodityDemand {
        int quantityOffset;

        public MarketSizeDemand(String id, String[] enableSettings, String commodity, int quantityOffset) {
            super(id, enableSettings, commodity);
            this.quantityOffset = quantityOffset;
        }

        @Override
        protected void applyImpl(BaseIndustry industry) {
            int adjustedQuantity = Math.max(0, industry.getMarket().getSize() + quantityOffset);
            industry.demand(id, commodity, adjustedQuantity, industry.getCurrentName());
        }

        @Override
        protected void addPostDemandInfoImpl(Map<String, BoggledCommoditySupplyDemand.CommodityDemandPara> demandTypeToCommodity, BaseIndustry industry, String industryTooltip) {
            if (!demandTypeToCommodity.containsKey("MarketSizeDemand")) {
                demandTypeToCommodity.put("MarketSizeDemand", new CommodityDemandPara(industryTooltip + " demand for ", " increases with market size"));
            }
            CommodityDemandPara para = demandTypeToCommodity.get("MarketSizeDemand");
            para.addCommodity(Global.getSettings().getCommoditySpec(commodity).getLowerCaseName());
        }
    }

    public static class PlayerMarketSizeElseFlatDemand extends CommodityDemand {
        int quantity;
        public PlayerMarketSizeElseFlatDemand(String id, String[] enableSettings, String commodity, int quantity) {
            super(id, enableSettings, commodity);
            this.quantity = quantity;
        }

        @Override
        public void applyImpl(BaseIndustry industry) {
            if (industry.getMarket().isPlayerOwned()) {
                industry.demand(id, commodity, industry.getMarket().getSize(), industry.getCurrentName());
            } else {
                industry.demand(id, commodity, quantity, industry.getCurrentName());
            }
        }

        @Override
        protected void addPostDemandInfoImpl(Map<String, BoggledCommoditySupplyDemand.CommodityDemandPara> demandTypeToCommodity, BaseIndustry industry, String industryTooltip) {
            if (industry.getMarket().isPlayerOwned()) {
                if (!demandTypeToCommodity.containsKey("MarketSizeDemand")) {
                    demandTypeToCommodity.put("MarketSizeDemand", new CommodityDemandPara(industryTooltip + " demand for ", " increases with market size"));
                }
                CommodityDemandPara para = demandTypeToCommodity.get("MarketSizeDemand");
                para.addCommodity(Global.getSettings().getCommoditySpec(commodity).getLowerCaseName());
            } else {
                if (!demandTypeToCommodity.containsKey("FlatDemand")) {
                    demandTypeToCommodity.put("FlatDemand", new CommodityDemandPara(industryTooltip + " always demands ", " regardless of market size."));
                }
                CommodityDemandPara para = demandTypeToCommodity.get("FlatDemand");
                para.addCommodity(quantity + " " + Global.getSettings().getCommoditySpec(commodity).getLowerCaseName());
                para.addHighlight(Integer.toString(quantity));
            }
        }
    }
}
