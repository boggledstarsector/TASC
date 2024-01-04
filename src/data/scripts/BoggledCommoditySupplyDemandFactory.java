package data.scripts;

import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import kotlin.Triple;

import java.util.ArrayList;
import java.util.Arrays;

public class BoggledCommoditySupplyDemandFactory {
    public interface CommodityDemandShortageEffectFactory {
        BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data);
    }

    public static class DeficitToInactiveFactory implements CommodityDemandShortageEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            return new BoggledCommoditySupplyDemand.DeficitToInactive(id, enableSettings, commoditiesDemanded);
        }
    }

    public static class DeficitToCommodityFactory implements CommodityDemandShortageEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            ArrayList<String> commoditiesDeficited = new ArrayList<>(Arrays.asList(data.split(boggledTools.csvSubOptionSeparator)));
            return new BoggledCommoditySupplyDemand.DeficitToCommodity(id, enableSettings, commoditiesDemanded, commoditiesDeficited);
        }
    }

    public static class DeficitMultiplierToUpkeepFactory implements CommodityDemandShortageEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            float upkeepMultipler = Float.parseFloat(data);
            return new BoggledCommoditySupplyDemand.DeficitMultiplierToUpkeep(id, enableSettings, commoditiesDemanded, upkeepMultipler);
        }
    }

    public static class ConditionMultiplierToUpkeepFactory implements CommodityDemandShortageEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            String[] conditionUpkeepMultiplierStrings = data.split(boggledTools.csvOptionSeparator);
            ArrayList<Pair<String, Float>> conditionUpkeepMultipliers = new ArrayList<>();
            for (String conditionUpkeepMultiplierString : conditionUpkeepMultiplierStrings) {
                String[] conditionUpkeepMultiplierPair = conditionUpkeepMultiplierString.split(boggledTools.csvSubOptionSeparator);
                String condition = conditionUpkeepMultiplierPair[0];
                float upkeepMultiplier = Float.parseFloat(conditionUpkeepMultiplierPair[1]);
                conditionUpkeepMultipliers.add(new Pair<>(condition, upkeepMultiplier));
            }

            return new BoggledCommoditySupplyDemand.ConditionMultiplierToUpkeep(id, enableSettings, commoditiesDemanded, conditionUpkeepMultipliers);
        }
    }

    public static class TagMultiplierToUpkeepFactory implements CommodityDemandShortageEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            String[] tagUpkeepMultiplierDescriptionStrings = data.split(boggledTools.csvOptionSeparator);
            ArrayList<Triple<String, Float, String>> tagUpkeepMultiplierDescriptions = new ArrayList<>();
            for (String tagUpkeepMultiplierDescriptionString : tagUpkeepMultiplierDescriptionStrings) {
                String[] tagUpkeepMultiplerDescriptionTriple = tagUpkeepMultiplierDescriptionString.split(boggledTools.csvSubOptionSeparator);
                String tag = tagUpkeepMultiplerDescriptionTriple[0];
                float upkeepMultiplier = Float.parseFloat(tagUpkeepMultiplerDescriptionTriple[1]);
                String description = "";
                if (tagUpkeepMultiplerDescriptionTriple.length == 3) {
                    description = tagUpkeepMultiplerDescriptionTriple[2];
                }
                tagUpkeepMultiplierDescriptions.add(new Triple<>(tag, upkeepMultiplier, description));
            }

            return new BoggledCommoditySupplyDemand.TagMultiplierToUpkeep(id, enableSettings, commoditiesDemanded, tagUpkeepMultiplierDescriptions);
        }
    }

    public static class IncomeBonusFromIndustryFactory implements CommodityDemandShortageEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            String[] industryIdAndIncomeMultiplerStrings = data.split(boggledTools.csvSubOptionSeparator);
            String industryId = industryIdAndIncomeMultiplerStrings[0];
            float incomeMultiplier = Float.parseFloat(industryIdAndIncomeMultiplerStrings[1]);
            return new BoggledCommoditySupplyDemand.IncomeBonusFromIndustry(id, enableSettings, commoditiesDemanded, industryId, incomeMultiplier);
        }
    }

    public interface CommoditySupplyDemandFactory {
        BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data);
    }

    public static class FlatDemandFactory implements CommoditySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.FlatDemand(id, enableSettings, commodity, quantity);
        }
    }

    public static class FlatSupplyFactory implements CommoditySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.FlatSupply(id, enableSettings, commodity, quantity);
        }
    }

    public static class MarketSizeDemandFactory implements CommoditySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) {
            int quantityOffset = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.MarketSizeDemand(id, enableSettings, commodity, quantityOffset);
        }
    }

    public static class MarketSizeSupplyFactory implements CommoditySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) {
            int quantityOffset = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.MarketSizeSupply(id, enableSettings, commodity, quantityOffset);
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
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) {
            Pair<String, ArrayList<Pair<String, Integer>>> modIdAndConditionAndQuantityOffsets = parseData(data);
            return new BoggledCommoditySupplyDemand.ConditionModifySupply(id, enableSettings, commodity, modIdAndConditionAndQuantityOffsets.one, modIdAndConditionAndQuantityOffsets.two);
        }
    }

    public static class ConditionModifyDemandFactory extends ConditionModifySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) {
            Pair<String, ArrayList<Pair<String, Integer>>> modIdAndConditionAndQuantityOffsets = parseData(data);
            return new BoggledCommoditySupplyDemand.ConditionModifyDemand(id, enableSettings, commodity, modIdAndConditionAndQuantityOffsets.one, modIdAndConditionAndQuantityOffsets.two);
        }
    }

    public static class PlayerMarketSizeElseFlatDemandFactory implements CommoditySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledCommoditySupplyDemand.PlayerMarketSizeElseFlatDemand(id, enableSettings, commodity, quantity);
        }
    }
}
