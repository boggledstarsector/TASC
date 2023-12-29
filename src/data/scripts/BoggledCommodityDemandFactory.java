package data.scripts;

public class BoggledCommodityDemandFactory {
    public interface CommodityDemandFactory {
        BoggledCommodityDemand.CommodityDemand constructFromJSON(String[] enableSettings, String commodity, String data);
    }

    public static class FlatFactory implements CommodityDemandFactory {
        @Override
        public BoggledCommodityDemand.CommodityDemand constructFromJSON(String[] enableSettings, String commodity, String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledCommodityDemand.Flat(enableSettings, commodity, quantity);
        }
    }

    public static class MarketSizeFactory implements CommodityDemandFactory {
        @Override
        public BoggledCommodityDemand.CommodityDemand constructFromJSON(String[] enableSettings, String commodity, String data) {
            int quantityOffset = Integer.parseInt(data);
            return new BoggledCommodityDemand.MarketSize(enableSettings, commodity, quantityOffset);
        }
    }
}
