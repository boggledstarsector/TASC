package boggled.scripts;

import boggled.campaign.econ.industries.BoggledCommonIndustry;
import com.fs.starfarer.api.util.Pair;
import boggled.campaign.econ.boggledTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class BoggledIndustryEffectFactory {
    public interface IndustryEffectFactory {
        BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException;
    }

    public static class DeficitToInactive implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray jsonCommoditiesDemanded = jsonData.getJSONArray("commodities_demanded");
            List<String> commoditiesDemanded = new ArrayList<>();
            for (int i = 0; i < jsonCommoditiesDemanded.length(); ++i) {
                commoditiesDemanded.add(jsonCommoditiesDemanded.getString(i));
            }
            return new BoggledIndustryEffect.DeficitToInactive(id, enableSettings, commoditiesDemanded);
        }
    }

    public static class DeficitToCommodity implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray jsonCommoditiesDemanded = jsonData.getJSONArray("commodities_demanded");
            List<String> commoditiesDemanded = new ArrayList<>();
            for (int i = 0; i < jsonCommoditiesDemanded.length(); ++i) {
                commoditiesDemanded.add(jsonCommoditiesDemanded.getString(i));
            }

            JSONArray jsonCommoditiesDeficited = jsonData.getJSONArray("commodities_deficited");
            List<String> commoditiesDeficited = new ArrayList<>();
            for (int i = 0; i < jsonCommoditiesDeficited.length(); ++i) {
                commoditiesDeficited.add(jsonCommoditiesDeficited.getString(i));
            }

            return new BoggledIndustryEffect.DeficitToCommodity(id, enableSettings, commoditiesDemanded, commoditiesDeficited);
        }
    }

    public static class DeficitMultiplierToUpkeep implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray jsonCommoditiesDemanded = jsonData.getJSONArray("commodities_demanded");
            List<String> commoditiesDemanded = new ArrayList<>();
            for (int i = 0; i < jsonCommoditiesDemanded.length(); ++i) {
                commoditiesDemanded.add(jsonCommoditiesDemanded.getString(i));
            }

            float upkeepMultiplier = (float) jsonData.getDouble("upkeep_multiplier");
            return new BoggledIndustryEffect.DeficitMultiplierToUpkeep(id, enableSettings, commoditiesDemanded, upkeepMultiplier);
        }
    }

    public static class EffectToIndustry implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String industryId = jsonData.getString("industry_id");
            String effectId = jsonData.getString("effect_id");
            BoggledIndustryEffect.IndustryEffect effect = boggledTools.getIndustryEffect(effectId);
            return new BoggledIndustryEffect.EffectToIndustry(id, enableSettings, industryId, effect);
        }
    }

    public static class ModifyIncome implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String typeString = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");

            return new BoggledIndustryEffect.ModifyIncome(id, enableSettings, typeString, value);
        }
    }

    public static class ModifyAccessibility implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modType = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledIndustryEffect.ModifyAccessibility(id, enableSettings, modType, value);
        }
    }

    public static class ModifyStability implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modType = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledIndustryEffect.ModifyStability(id, enableSettings, modType, value);
        }
    }

    public static class SupplyBonusToIndustryWithDeficit implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            JSONArray jsonCommoditiesDemanded = jsonData.getJSONArray("commodities_demanded");
            List<String> commoditiesDemanded = new ArrayList<>();
            for (int i = 0; i < jsonCommoditiesDemanded.length(); ++i) {
                commoditiesDemanded.add(jsonCommoditiesDemanded.getString(i));
            }
            String industryId = jsonData.getString("industry_id");
            int supplyBonus = jsonData.getInt("bonus");
            return new BoggledIndustryEffect.SupplyBonusToIndustryWithDeficit(id, enableSettings, commoditiesDemanded, industryId, "flat", supplyBonus);
        }
    }

    public static class ModifyAllDemand implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modType = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledIndustryEffect.ModifyAllDemand(id, enableSettings, modType, value);
        }
    }

    public static class ModifyUpkeep implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modType = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledIndustryEffect.ModifyUpkeep(id, enableSettings, modType, value);
        }
    }

    public static class EliminatePatherInterest implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) {
            return new BoggledIndustryEffect.EliminatePatherInterest(id, enableSettings);
        }
    }

    public static class ModifyPatherInterest implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modType = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledIndustryEffect.ModifyPatherInterest(id, enableSettings, modType, value);
        }
    }

    public static class ModifyColonyGrowthRate implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modType = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledIndustryEffect.ModifyColonyGrowthRate(id, enableSettings, modType, value);
        }
    }

    public static class IncrementTag implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String tag = jsonData.getString("tag");
            int step = jsonData.getInt("step");
            return new BoggledIndustryEffect.IncrementTag(id, enableSettings, tag, step);
        }
    }

    public static class RemoveIndustry implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            boggledTools.CheckIndustryExists(id, data);
            return new BoggledIndustryEffect.RemoveIndustry(id, enableSettings, data);
        }
    }

    public static class SuppressConditions implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            List<String> conditionIds = new ArrayList<>(asList(data.split(boggledTools.csvOptionSeparator)));
            return new BoggledIndustryEffect.SuppressConditions(id, enableSettings, conditionIds);
        }
    }

    public static class ModifyGroundDefense implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String modType = jsonData.getString("type");
            float value = (float) jsonData.getDouble("value");
            return new BoggledIndustryEffect.ModifyGroundDefense(id, enableSettings, modType, value);
        }
    }

    public static class IndustryEffectWithRequirement implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
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
            return new BoggledIndustryEffect.IndustryEffectWithRequirement(id, enableSettings, reqs, industryEffects);
        }
    }

    public static class AddCondition implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            boggledTools.CheckMarketConditionExists(id, data);
            return new BoggledIndustryEffect.AddCondition(id, enableSettings, data);
        }
    }

    public static class AddStellarReflectorsToMarket implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            return new BoggledIndustryEffect.AddStellarReflectorsToMarket(id, enableSettings);
        }
    }

    public static class TagSubstringPowerModifyBuildCost implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String tagSubstring = jsonData.getString("tag");
            int defaultValue = jsonData.getInt("default");
            return new BoggledIndustryEffect.TagSubstringPowerModifyBuildCost(id, enableSettings, tagSubstring, defaultValue);
        }
    }

    public static class MonthlyItemProductionChance implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);
            List<BoggledCommonIndustry.ProductionData> productionData = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int priority = jsonObject.getInt("commodity_priority");
                String commodityId = jsonObject.getString("commodity_id");
                int chance = jsonObject.getInt("chance");
                JSONArray requirementsArray = jsonObject.optJSONArray("requirement_ids");
                BoggledProjectRequirementsAND reqs = boggledTools.requirementsFromRequirementsArray(requirementsArray, id, "MonthlyItemProductionChance");

                productionData.add(new BoggledCommonIndustry.ProductionData(priority, commodityId, chance, reqs));
            }
            return new BoggledIndustryEffect.MonthlyItemProductionChance(id, enableSettings, productionData);
        }
    }

    public static class MonthlyItemProductionChanceModifier implements IndustryEffectFactory {
        @Override
        public BoggledIndustryEffect.IndustryEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONArray jsonArray = new JSONArray(data);
            List<Pair<String, Integer>> productionData = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String commodityId = jsonObject.getString("commodity_id");
                int chanceModifier = jsonObject.getInt("chance_modifier");

                productionData.add(new Pair<>(commodityId, chanceModifier));
            }

            return new BoggledIndustryEffect.MonthlyItemProductionChanceModifier(id, enableSettings, productionData);
        }
    }
}
