package data.scripts;

import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class BoggledCommoditySupplyDemandFactory {
    public interface CommodityDemandShortageEffectFactory {
        BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException;
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
        public BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            List<BoggledCommoditySupplyDemand.ConditionMultiplierToUpkeep.Data> conditionIdsAndMultipliers = new ArrayList<>();
            for (Iterator<String> it = jsonData.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject conditionMultiplierData = jsonData.getJSONObject(key);
                float value = (float) conditionMultiplierData.getDouble("multiplier");
                conditionIdsAndMultipliers.add(new BoggledCommoditySupplyDemand.ConditionMultiplierToUpkeep.Data(key, value));
            }

            return new BoggledCommoditySupplyDemand.ConditionMultiplierToUpkeep(id, enableSettings, commoditiesDemanded, conditionIdsAndMultipliers);
        }
    }

    public static class TagMultiplierToUpkeepFactory implements CommodityDemandShortageEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            List<BoggledCommoditySupplyDemand.TagMultiplierToUpkeep.Data> tagsAndMultipliers = new ArrayList<>();
            for (Iterator<String> it = jsonData.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject conditionMultiplierData = jsonData.getJSONObject(key);
                float value = (float) conditionMultiplierData.getDouble("multiplier");
                String description = conditionMultiplierData.getString("description");
                tagsAndMultipliers.add(new BoggledCommoditySupplyDemand.TagMultiplierToUpkeep.Data(key, value, description));
            }

            return new BoggledCommoditySupplyDemand.TagMultiplierToUpkeep(id, enableSettings, commoditiesDemanded, tagsAndMultipliers);
        }
    }

    public static class IncomeBonusToIndustryFactory implements CommodityDemandShortageEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            List<BoggledCommoditySupplyDemand.IncomeBonusToIndustry.Data> industryIdIncomeMultipliers = new ArrayList<>();
            for (Iterator<String> it = jsonData.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject industryData = jsonData.getJSONObject(key);
                float multiplier = (float) industryData.getDouble("multiplier");
                industryIdIncomeMultipliers.add(new BoggledCommoditySupplyDemand.IncomeBonusToIndustry.Data(key, multiplier));
            }

            return new BoggledCommoditySupplyDemand.IncomeBonusToIndustry(id, enableSettings, commoditiesDemanded, industryIdIncomeMultipliers);
        }
    }

    public static class SupplyBonusWithDeficitToIndustryFactory implements CommodityDemandShortageEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONObject industries = jsonData.getJSONObject("industries");
            JSONObject aiCoreBonuses = jsonData.getJSONObject("ai_core_bonuses");

            List<BoggledCommoditySupplyDemand.SupplyBonusWithDeficitToIndustry.Data> effectData = new ArrayList<>();
            for (Iterator<String> it = industries.keys(); it.hasNext(); ) {
                String key = it.next();
                int value = industries.getInt(key);
                effectData.add(new BoggledCommoditySupplyDemand.SupplyBonusWithDeficitToIndustry.Data(key, value));
            }

            Map<String, Integer> aiCoreIdAndBonusesMap = new HashMap<>();
            for (Iterator<String> it = aiCoreBonuses.keys(); it.hasNext(); ) {
                String key = it.next();
                int value = aiCoreBonuses.getInt(key);
                aiCoreBonuses.put(key, value);
            }

            return new BoggledCommoditySupplyDemand.SupplyBonusWithDeficitToIndustry(id, enableSettings, commoditiesDemanded, aiCoreIdAndBonusesMap, effectData);
        }
    }

    public interface CommoditySupplyDemandFactory {
        BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) throws JSONException;
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
        protected Pair<String, List<Pair<String, Integer>>> parseData(String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            String modId = jsonData.getString("mod_id");
            JSONObject conditionsData = jsonData.getJSONObject("conditions");
            List<Pair<String, Integer>> conditions = new ArrayList<>();
            for (Iterator<String> it = conditionsData.keys(); it.hasNext(); ) {
                String key = it.next();
                int value = conditionsData.getInt(key);
                conditions.add(new Pair<>(key, value));
            }
            return new Pair<>(modId, conditions);
        }
    }

    public static class ConditionModifySupplyFactory extends ConditionModifySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) throws JSONException {
            Pair<String, List<Pair<String, Integer>>> modIdAndConditionAndQuantityOffsets = parseData(data);
            return new BoggledCommoditySupplyDemand.ConditionModifySupply(id, enableSettings, commodity, modIdAndConditionAndQuantityOffsets.one, modIdAndConditionAndQuantityOffsets.two);
        }
    }

    public static class ConditionModifyDemandFactory extends ConditionModifySupplyDemandFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommoditySupplyAndDemand constructFromJSON(String id, String[] enableSettings, String commodity, String data) throws JSONException {
            Pair<String, List<Pair<String, Integer>>> modIdAndConditionAndQuantityOffsets = parseData(data);
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
