package data.scripts;

import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;

import java.util.ArrayList;

public class BoggledCommoditySupplyDemandFactory {
    public interface CommoditySupplyDemandFactory {
        BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String[] enableSettings, String commodity, String data);
    }

    public static class FlatDemandFactory implements CommoditySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String[] enableSettings, String commodity, String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.FlatDemand(enableSettings, commodity, quantity);
        }
    }

    public static class FlatSupplyFactory implements CommoditySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String[] enableSettings, String commodity, String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.FlatSupply(enableSettings, commodity, quantity);
        }
    }

    public static class MarketSizeDemandFactory implements CommoditySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String[] enableSettings, String commodity, String data) {
            int quantityOffset = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.MarketSizeDemand(enableSettings, commodity, quantityOffset);
        }
    }

    public static class MarketSizeSupplyFactory implements CommoditySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String[] enableSettings, String commodity, String data) {
            int quantityOffset = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.MarketSizeSupply(enableSettings, commodity, quantityOffset);
        }
    }

    public abstract static class ConditionModifySupplyDemandFactory implements CommoditySupplyDemandFactory {
        protected Pair<String, ArrayList<Pair<String, Integer>>> parseData(String data) {
            String[] modIdAndConditionAndQuantityOffsets = data.split(boggledTools.csvOptionSeparator);
            String modId = modIdAndConditionAndQuantityOffsets[0];
            ArrayList<Pair<String, Integer>> conditionAndQuantityOffsets = new ArrayList<>();
            for (int i = 1; i < modIdAndConditionAndQuantityOffsets.length; ++i) {
                String[] conditionAndQuantityOffsetsString = modIdAndConditionAndQuantityOffsets[i].split(boggledTools.csvSubOptionSeparator);
                conditionAndQuantityOffsets.add(new Pair<>(conditionAndQuantityOffsetsString[0], Integer.parseInt(conditionAndQuantityOffsetsString[1])));
            }
            return new Pair<>(modId, conditionAndQuantityOffsets);
        }
    }

    public static class ConditionModifySupplyFactory extends ConditionModifySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String[] enableSettings, String commodity, String data) {
            Pair<String, ArrayList<Pair<String, Integer>>> modIdAndConditionAndQuantityOffsets = parseData(data);
            return new BoggledCommoditySupplyDemand.ConditionModifySupply(enableSettings, commodity, modIdAndConditionAndQuantityOffsets.one, modIdAndConditionAndQuantityOffsets.two);
        }
    }

    public static class ConditionModifyDemandFactory extends ConditionModifySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String[] enableSettings, String commodity, String data) {
            Pair<String, ArrayList<Pair<String, Integer>>> modIdAndConditionAndQuantityOffsets = parseData(data);
            return new BoggledCommoditySupplyDemand.ConditionModifyDemand(enableSettings, commodity, modIdAndConditionAndQuantityOffsets.one, modIdAndConditionAndQuantityOffsets.two);
        }
    }

    public static class PlayerMarketSizeElseFlatDemandFactory implements CommoditySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String[] enableSettings, String commodity, String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.PlayerMarketSizeElseFlatDemand(enableSettings, commodity, quantity);
        }
    }
}
