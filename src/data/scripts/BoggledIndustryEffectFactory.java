package data.scripts;

import data.campaign.econ.boggledTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoggledIndustryEffectFactory {
    public interface IndustryEffectFactory {
        BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException;
    }

    public static class DeficitToInactiveFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            return new BoggledIndustryEffect.DeficitToInactive(id, enableSettings, commoditiesDemanded);
        }
    }

    public static class DeficitToCommodityFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            ArrayList<String> commoditiesDeficited = new ArrayList<>(Arrays.asList(data.split(boggledTools.csvOptionSeparator)));
            return new BoggledIndustryEffect.DeficitToCommodity(id, enableSettings, commoditiesDemanded, commoditiesDeficited);
        }
    }

    public static class DeficitMultiplierToUpkeepFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            float upkeepMultipler = Float.parseFloat(data);
            return new BoggledIndustryEffect.DeficitMultiplierToUpkeep(id, enableSettings, commoditiesDemanded, upkeepMultipler);
        }
    }

    public static class ConditionMultiplierToUpkeepFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);

            List<BoggledIndustryEffect.ConditionMultiplierToUpkeep.Data> effectData = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                String conditionId = jsonData.getString("condition_id");
                float upkeepMultiplier = (float) jsonData.getDouble("upkeep_multiplier");

                effectData.add(new BoggledIndustryEffect.ConditionMultiplierToUpkeep.Data(conditionId, upkeepMultiplier));
            }

            return new BoggledIndustryEffect.ConditionMultiplierToUpkeep(id, enableSettings, commoditiesDemanded, effectData);
        }
    }

    public static class TagMultiplierToUpkeepFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);

            List<BoggledIndustryEffect.TagMultiplierToUpkeep.Data> effectData = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonData = jsonArray.getJSONObject(i);

                String tag = jsonData.getString("tag");
                String description = jsonData.getString("description");
                float upkeepMultiplier = (float) jsonData.getDouble("upkeep_multiplier");

                effectData.add(new BoggledIndustryEffect.TagMultiplierToUpkeep.Data(tag, description, upkeepMultiplier));
            }

            return new BoggledIndustryEffect.TagMultiplierToUpkeep(id, enableSettings, commoditiesDemanded, effectData);
        }
    }

    public static class IncomeBonusToIndustryFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);

            List<BoggledIndustryEffect.IncomeBonusToIndustry.Data> effectData = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonData = jsonArray.getJSONObject(i);

                String industryId = jsonData.getString("industry_id");
                float incomeMultiplier = (float) jsonData.getDouble("income_multiplier");

                effectData.add(new BoggledIndustryEffect.IncomeBonusToIndustry.Data(industryId, incomeMultiplier));
            }

            return new BoggledIndustryEffect.IncomeBonusToIndustry(id, enableSettings, commoditiesDemanded, effectData);
        }
    }

    public static class BonusToAccessibilityFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            float accessibilityBonus = Float.parseFloat(data);
            return new BoggledIndustryEffect.BonusToAccessibility(id, enableSettings, commoditiesDemanded, accessibilityBonus);
        }
    }

    public static class BonusToStabilityFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            float stabilityBonus = Float.parseFloat(data);
            return new BoggledIndustryEffect.BonusToStability(id, enableSettings, commoditiesDemanded, stabilityBonus);
        }
    }

    public static class SupplyBonusToIndustryWithDeficitFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String industryId = jsonData.getString("industry_id");
            int supplyBonus = jsonData.getInt("bonus");
            return new BoggledIndustryEffect.SupplyBonusToIndustryWithDeficit(id, enableSettings, commoditiesDemanded, industryId, supplyBonus);
        }
    }

    public static class ReduceAllDemandFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            int demandReduction = Integer.parseInt(data);
            return new BoggledIndustryEffect.ReduceAllDemand(id, enableSettings, commoditiesDemanded, demandReduction);
        }
    }

    public static class ReduceUpkeepFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            float upkeepReduction = Float.parseFloat(data);
            return new BoggledIndustryEffect.ReduceUpkeep(id, enableSettings, commoditiesDemanded, upkeepReduction);
        }
    }

    public static class EliminatePatherInterestFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            return new BoggledIndustryEffect.EliminatePatherInterest(id, enableSettings, commoditiesDemanded);
        }
    }
}
