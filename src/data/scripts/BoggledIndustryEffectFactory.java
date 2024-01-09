package data.scripts;

import data.campaign.econ.boggledTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

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
            ArrayList<String> commoditiesDeficited = new ArrayList<>(asList(data.split(boggledTools.csvOptionSeparator)));
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

    public static class ConditionToPatherInterestFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);
            List<BoggledIndustryEffect.ConditionToPatherInterest.Data> patherData = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                String conditionId = jsonData.getString("condition_id");
                float patherInterest = (float) jsonData.getDouble("interest");
                patherData.add(new BoggledIndustryEffect.ConditionToPatherInterest.Data(conditionId, patherInterest));
            }
            return new BoggledIndustryEffect.ConditionToPatherInterest(id, enableSettings, commoditiesDemanded, patherData);
        }
    }

    public static class IncrementTagFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String tag = jsonData.getString("tag");
            int step = jsonData.getInt("step");
            return new BoggledIndustryEffect.IncrementTag(id, enableSettings, commoditiesDemanded, tag, step);
        }
    }

    public static class RemoveIndustryFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            boggledTools.CheckIndustryExists(data);
            return new BoggledIndustryEffect.RemoveIndustry(id, enableSettings, commoditiesDemanded, data);
        }
    }

    public static class SuppressConditionsFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            List<String> conditionIds = new ArrayList<>(asList(data.split(boggledTools.csvOptionSeparator)));
            return new BoggledIndustryEffect.SuppressConditions(id, enableSettings, commoditiesDemanded, conditionIds);
        }
    }

    public static class ImproveGroundDefenseFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            float groundDefenseImproveBonus = Float.parseFloat(data);
            return new BoggledIndustryEffect.ImproveGroundDefense(id, enableSettings, commoditiesDemanded, groundDefenseImproveBonus);
        }
    }

    public static class IndustryEffectWithRequirementFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray reqsArray = jsonData.getJSONArray("requirement_ids");
            JSONArray industryEffectsArray = jsonData.getJSONArray("industry_effects");

            BoggledProjectRequirementsAND reqs = boggledTools.requirementsFromRequirementsArray(reqsArray, id, "requirements");
            List<BoggledIndustryEffect.IndustryEffect> industryEffects = new ArrayList<>();
            for (int i = 0; i < industryEffectsArray.length(); ++i) {
                String industryEffectString = industryEffectsArray.getString(i);
                BoggledIndustryEffect.IndustryEffect effect = boggledTools.getIndustryEffect(industryEffectString);
                if (effect != null) {
                    industryEffects.add(effect);
                }
            }
            return new BoggledIndustryEffect.IndustryEffectWithRequirement(id, enableSettings, commoditiesDemanded, reqs, industryEffects);
        }
    }
}
