package data.scripts;

import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import org.json.JSONArray;
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
            JSONArray jsonArray = new JSONArray(data);

            List<BoggledCommoditySupplyDemand.ConditionMultiplierToUpkeep.Data> effectData = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                String conditionId = jsonData.getString("condition_id");
                float upkeepMultiplier = (float) jsonData.getDouble("upkeep_multiplier");

                effectData.add(new BoggledCommoditySupplyDemand.ConditionMultiplierToUpkeep.Data(conditionId, upkeepMultiplier));
            }

            return new BoggledCommoditySupplyDemand.ConditionMultiplierToUpkeep(id, enableSettings, commoditiesDemanded, effectData);
        }
    }

    public static class TagMultiplierToUpkeepFactory implements CommodityDemandShortageEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);

            List<BoggledCommoditySupplyDemand.TagMultiplierToUpkeep.Data> effectData = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonData = jsonArray.getJSONObject(i);

                String tag = jsonData.getString("tag");
                String description = jsonData.getString("description");
                float upkeepMultiplier = (float) jsonData.getDouble("upkeep_multiplier");

                effectData.add(new BoggledCommoditySupplyDemand.TagMultiplierToUpkeep.Data(tag, description, upkeepMultiplier));
            }

            return new BoggledCommoditySupplyDemand.TagMultiplierToUpkeep(id, enableSettings, commoditiesDemanded, effectData);
        }
    }

    public static class IncomeBonusToIndustryFactory implements CommodityDemandShortageEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);

            List<BoggledCommoditySupplyDemand.IncomeBonusToIndustry.Data> effectData = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonData = jsonArray.getJSONObject(i);

                String industryId = jsonData.getString("industry_id");
                float incomeMultiplier = (float) jsonData.getDouble("income_multiplier");

                effectData.add(new BoggledCommoditySupplyDemand.IncomeBonusToIndustry.Data(industryId, incomeMultiplier));
            }

            return new BoggledCommoditySupplyDemand.IncomeBonusToIndustry(id, enableSettings, commoditiesDemanded, effectData);
        }
    }

    public static class SupplyBonusWithDeficitToIndustryFactory implements CommodityDemandShortageEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.CommodityDemandShortageEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            JSONArray industryArray = jsonData.getJSONArray("industry_data");

            List<BoggledCommoditySupplyDemand.SupplyBonusWithDeficitToIndustry.Data> effectData = new ArrayList<>();
            for (int i = 0; i < industryArray.length(); ++i) {
                JSONObject industryData = industryArray.getJSONObject(i);

                String industryId = industryData.getString("industry_id");
                int bonus = industryData.getInt("bonus");

                effectData.add(new BoggledCommoditySupplyDemand.SupplyBonusWithDeficitToIndustry.Data(industryId, bonus));
            }

            JSONObject aiCoreObject = jsonData.getJSONObject("ai_core_data");

            String aiCoreDescription = aiCoreObject.getString("description");

            JSONArray aiCoreArray = aiCoreObject.getJSONArray("effects");

            Map<String, Integer> aiCoreEffectMap = new HashMap<>();
            for (int i = 0; i < aiCoreArray.length(); ++i) {
                JSONObject aiCoreData = aiCoreArray.getJSONObject(i);

                String aiCoreId = aiCoreData.getString("ai_core_id");
                int bonus = aiCoreData.getInt("bonus");

                aiCoreEffectMap.put(aiCoreId, bonus);
            }

            BoggledCommoditySupplyDemand.SupplyBonusWithDeficitToIndustry.AICoreData aiCoreData = new BoggledCommoditySupplyDemand.SupplyBonusWithDeficitToIndustry.AICoreData(aiCoreDescription, aiCoreEffectMap);

            return new BoggledCommoditySupplyDemand.SupplyBonusWithDeficitToIndustry(id, enableSettings, commoditiesDemanded, effectData, aiCoreData);
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
