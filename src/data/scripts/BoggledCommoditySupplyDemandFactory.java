package data.scripts;

import data.campaign.econ.boggledTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class BoggledCommoditySupplyDemandFactory {
    public interface IndustryEffectFactory {
        BoggledCommoditySupplyDemand.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException;
    }

    public static class DeficitToInactiveFactory implements IndustryEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            return new BoggledCommoditySupplyDemand.DeficitToInactive(id, enableSettings, commoditiesDemanded);
        }
    }

    public static class DeficitToCommodityFactory implements IndustryEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            ArrayList<String> commoditiesDeficited = new ArrayList<>(Arrays.asList(data.split(boggledTools.csvOptionSeparator)));
            return new BoggledCommoditySupplyDemand.DeficitToCommodity(id, enableSettings, commoditiesDemanded, commoditiesDeficited);
        }
    }

    public static class DeficitMultiplierToUpkeepFactory implements IndustryEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            float upkeepMultipler = Float.parseFloat(data);
            return new BoggledCommoditySupplyDemand.DeficitMultiplierToUpkeep(id, enableSettings, commoditiesDemanded, upkeepMultipler);
        }
    }

    public static class ConditionMultiplierToUpkeepFactory implements IndustryEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
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

    public static class TagMultiplierToUpkeepFactory implements IndustryEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
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

    public static class IncomeBonusToIndustryFactory implements IndustryEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
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

    public static class SupplyBonusWithDeficitToIndustryFactory implements IndustryEffectFactory {
        @Override
        public BoggledCommoditySupplyDemand.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);

            JSONArray industryArray = jsonData.getJSONArray("industry_data");

            List<BoggledCommoditySupplyDemand.SupplyBonusWithDeficitToIndustry.Data> effectData = new ArrayList<>();
            for (int i = 0; i < industryArray.length(); ++i) {
                JSONObject industryData = industryArray.getJSONObject(i);

                String industryId = industryData.getString("industry_id");
                String description = industryData.getString("description");
                int bonus = industryData.getInt("bonus");

                effectData.add(new BoggledCommoditySupplyDemand.SupplyBonusWithDeficitToIndustry.Data(industryId, description, bonus));
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
