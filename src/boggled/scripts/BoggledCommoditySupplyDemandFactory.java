package boggled.scripts;

import org.json.JSONException;

public class BoggledCommoditySupplyDemandFactory {
    public interface CommoditySupplyFactory {
        BoggledCommoditySupplyDemand.CommoditySupply constructFromJSON(String id, String[] enableSettings, String commodity, String data) throws JSONException;
    }

    public static class FlatSupplyFactory implements CommoditySupplyFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupply constructFromJSON(String id, String[] enableSettings, String commodity, String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.FlatSupply(id, enableSettings, commodity, quantity);
        }
    }

    public static class MarketSizeSupplyFactory implements CommoditySupplyFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupply constructFromJSON(String id, String[] enableSettings, String commodity, String data) {
            int quantityOffset = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.MarketSizeSupply(id, enableSettings, commodity, quantityOffset);
        }
    }

    public interface CommodityDemandFactory {
        BoggledCommoditySupplyDemand.CommodityDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) throws JSONException;
    }

    public static class FlatDemandFactory implements CommodityDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.FlatDemand(id, enableSettings, commodity, quantity);
        }
    }

    public static class MarketSizeDemandFactory implements CommodityDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) {
            int quantityOffset = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.MarketSizeDemand(id, enableSettings, commodity, quantityOffset);
        }
    }

    public static class PlayerMarketSizeElseFlatDemandFactory implements CommodityDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.PlayerMarketSizeElseFlatDemand(id, enableSettings, commodity, quantity);
        }
    }
}
