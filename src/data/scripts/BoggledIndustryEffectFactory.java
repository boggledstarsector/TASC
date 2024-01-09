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

    public static class EffectToIndustryFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String industryId = jsonData.getString("industry_id");
            String effectId = jsonData.getString("effect_id");
            BoggledIndustryEffect.IndustryEffect effect = boggledTools.getIndustryEffect(effectId);
            return new BoggledIndustryEffect.EffectToIndustry(id, enableSettings, commoditiesDemanded, industryId, effect);
        }
    }

    public static class ModifyIncomeFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String typeString = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");

            return new BoggledIndustryEffect.ModifyIncome(id, enableSettings, commoditiesDemanded, typeString, value);
        }
    }

    public static class ModifyAccessibilityFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modType = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledIndustryEffect.ModifyAccessibility(id, enableSettings, commoditiesDemanded, modType, value);
        }
    }

    public static class ModifyStabilityFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modType = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledIndustryEffect.ModifyStability(id, enableSettings, commoditiesDemanded, modType, value);
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

    public static class ModifyAllDemandFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modType = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledIndustryEffect.ModifyAllDemand(id, enableSettings, commoditiesDemanded, modType, value);
        }
    }

    public static class ModifyUpkeepFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modType = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledIndustryEffect.ModifyUpkeep(id, enableSettings, commoditiesDemanded, modType, value);
        }
    }

    public static class EliminatePatherInterestFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) {
            return new BoggledIndustryEffect.EliminatePatherInterest(id, enableSettings, commoditiesDemanded);
        }
    }

    public static class ModifyPatherInterestFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            int patherInterestModifier = Integer.parseInt(data);
            return new BoggledIndustryEffect.ModifyPatherInterest(id, enableSettings, commoditiesDemanded, patherInterestModifier);
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

    public static class ModifyGroundDefenseFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modType = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledIndustryEffect.ModifyGroundDefense(id, enableSettings, commoditiesDemanded, modType, value);
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

    public static class AddConditionFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            boggledTools.CheckMarketConditionExists(data);
            return new BoggledIndustryEffect.AddCondition(id, enableSettings, commoditiesDemanded, data);
        }
    }

    public static class AddStellarReflectorsToMarketFactory implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, ArrayList<String> commoditiesDemanded, String data) throws JSONException {
            return new BoggledIndustryEffect.AddStellarReflectorsToMarket(id, enableSettings, commoditiesDemanded);
        }
    }
}
