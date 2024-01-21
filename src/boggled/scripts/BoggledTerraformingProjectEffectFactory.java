package boggled.scripts;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;

public class BoggledTerraformingProjectEffectFactory {
    public interface TerraformingProjectEffectFactory {
        BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException;
    }

    public static class PlanetTypeChange implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            boggledTools.CheckPlanetTypeExists(data);
            return new BoggledTerraformingProjectEffect.PlanetTypeChangeProjectEffect(id, enableSettings, data);
        }
    }

    public static class MarketAddCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            boggledTools.CheckMarketConditionExists(data);
            return new BoggledTerraformingProjectEffect.MarketAddConditionProjectEffect(id, enableSettings, data);
        }
    }

    public static class MarketRemoveCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            boggledTools.CheckMarketConditionExists(data);
            return new BoggledTerraformingProjectEffect.MarketRemoveConditionProjectEffect(id, enableSettings, data);
        }
    }

    public static class MarketOptionalCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String option = jsonData.getString("option");
            String conditionId = jsonData.getString("condition_id");

            boggledTools.CheckMarketConditionExists(conditionId);

            if (boggledTools.getBooleanSetting(option)) {
                return new BoggledTerraformingProjectEffect.MarketAddConditionProjectEffect(id, enableSettings, conditionId);
            }
            return new BoggledTerraformingProjectEffect.MarketRemoveConditionProjectEffect(id, enableSettings, conditionId);
        }
    }

    public static class MarketProgressResource implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String resourceId = jsonData.getString("resource_id");
            int step = jsonData.getInt("step");

            boggledTools.CheckResourceExists(resourceId);

            return new BoggledTerraformingProjectEffect.MarketProgressResourceProjectEffect(id, enableSettings, resourceId, step);
        }
    }

    public static class FocusMarketAddCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            boggledTools.CheckMarketConditionExists(data);
            return new BoggledTerraformingProjectEffect.FocusMarketAddConditionProjectEffect(id, enableSettings, data);
        }
    }

    public static class FocusMarketRemoveCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            boggledTools.CheckMarketConditionExists(data);
            return new BoggledTerraformingProjectEffect.FocusMarketRemoveConditionProjectEffect(id, enableSettings, data);
        }
    }

    public static class FocusMarketProgressResource implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String resourceId = jsonData.getString("resource_id");
            int step = jsonData.getInt("step");

            boggledTools.CheckResourceExists(resourceId);

            return new BoggledTerraformingProjectEffect.FocusMarketProgressResourceProjectEffect(id, enableSettings, resourceId, step);
        }
    }

    public static class FocusMarketAndSiphonStationProgressResource implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String resourceId = jsonData.getString("resource_id");
            int step = jsonData.getInt("step");

            boggledTools.CheckResourceExists(resourceId);

            return new BoggledTerraformingProjectEffect.FocusMarketAndSiphonStationProgressResourceProjectEffect(id, enableSettings, resourceId, step);
        }
    }

    public static class SystemAddCoronalTap implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            return new BoggledTerraformingProjectEffect.SystemAddCoronalTap(id, enableSettings);
        }
    }

//    public static class MarketAddStellarReflectorsFactory implements TerraformingProjectEffectFactory {
//        @Override
//        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
//            return new BoggledTerraformingProjectEffect.MarketAddStellarReflectors();
//        }
//    }

    public static class MarketRemoveIndustry implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) {
            return new BoggledTerraformingProjectEffect.MarketRemoveIndustry(id, enableSettings, data);
        }
    }

    public static class RemoveCommodityFromSubmarket implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String submarketId = jsonData.getString("submarket_id");
            String commodityId = jsonData.getString("commodity_id");
            int quantity = jsonData.getInt("quantity");

            boggledTools.CheckSubmarketExists(submarketId);
            boggledTools.CheckCommodityExists(commodityId);

            return new BoggledTerraformingProjectEffect.RemoveCommodityFromSubmarket(id, enableSettings, submarketId, commodityId, quantity);
        }
    }

    public static class RemoveStoryPointsFromPlayer implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            int quantity = jsonData.getInt("quantity");
            return new BoggledTerraformingProjectEffect.RemoveStoryPointsFromPlayer(id, enableSettings, quantity);
        }
    }

    public static class RemoveCommodityFromFleetStorage implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String commodityId = jsonData.getString("commodity_id");
            int quantity = jsonData.getInt("quantity");

            boggledTools.CheckCommodityExists(commodityId);

            return new BoggledTerraformingProjectEffect.RemoveCommodityFromFleetStorage(id, enableSettings, commodityId, quantity);
        }
    }

    public static class RemoveCreditsFromFleet implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            int quantity = jsonData.getInt("quantity");
            return new BoggledTerraformingProjectEffect.RemoveCreditsFromFleet(id, enableSettings, quantity);
        }
    }

    public static class AddItemToSubmarket implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String submarketId = jsonData.getString("submarket_id");
            String itemId = jsonData.getString("item_id");
            int quantity = jsonData.getInt("quantity");

            boggledTools.CheckSubmarketExists(submarketId);
            boggledTools.CheckItemExists(itemId);

            return new BoggledTerraformingProjectEffect.AddItemToSubmarket(id, enableSettings, submarketId, itemId, quantity);
        }
    }

    public static class AddStationToOrbit implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String stationType = jsonData.getString("station_type");
            String stationName = jsonData.getString("station_name");
            JSONArray variantsArray = jsonData.optJSONArray("variants");
            List<String> variants = new ArrayList<>();
            if (variantsArray != null) {
                for (int i = 0; i < variantsArray.length(); ++i) {
                    variants.add(variantsArray.getString(i));
                }
            }
            int numStationsPerLayer = jsonData.getInt("num_stations_per_layer");
            float orbitRadius = (float) jsonData.getDouble("orbit_radius");

            BoggledStationConstructionFactory.StationConstructionFactory factory = boggledTools.stationConstructionFactories.get(stationType);
            BoggledStationConstructors.StationConstructionData stationConstructionData = factory.constructFromJSON(id, jsonData.getJSONObject("station_construction_data").toString());

            return new BoggledTerraformingProjectEffect.AddStationToOrbit(id, enableSettings, stationType, stationName, variants, numStationsPerLayer, orbitRadius, stationConstructionData);
        }
    }

    public static class AddStationToAsteroids implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String stationType = jsonData.getString("station_type");
            String stationName = jsonData.getString("station_name");
            JSONArray variantsArray = jsonData.optJSONArray("variants");
            List<String> variants = new ArrayList<>();
            if (variantsArray != null) {
                for (int i = 0; i < variantsArray.length(); ++i) {
                    variants.add(variantsArray.getString(i));
                }
            }
            int numStationsPerLayer = jsonData.getInt("num_stations_per_layer");
            float orbitRadius = (float) jsonData.getDouble("orbit_radius");

            BoggledStationConstructionFactory.StationConstructionFactory factory = boggledTools.stationConstructionFactories.get(stationType);
            BoggledStationConstructors.StationConstructionData stationConstructionData = factory.constructFromJSON(id, jsonData.getJSONObject("station_construction_data").toString());

            return new BoggledTerraformingProjectEffect.AddStationToEntity(id, enableSettings, stationType, stationName, variants, numStationsPerLayer, orbitRadius, stationConstructionData);
        }
    }

    public static class ColonizeAbandonedStation implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String id, String[] enableSettings, String data) throws JSONException {
            Logger log = Global.getLogger(this.getClass());
            JSONObject jsonData = new JSONObject(data);
            JSONObject stationColonizationDataObject = jsonData.optJSONObject("station_colonization_data");
            List<BoggledStationConstructors.StationConstructionData> stationConstructionData = new ArrayList<>();
            if (stationColonizationDataObject != null) {
                for (Iterator<String> it = stationColonizationDataObject.keys(); it.hasNext(); ) {
                    String key = it.next();
                    String stationColonizationDataString = stationColonizationDataObject.getJSONObject(key).toString();
                    BoggledStationConstructionFactory.StationConstructionFactory factory = boggledTools.stationConstructionFactories.get(key);
                    if (factory == null) {
                        log.error("ColonizeAbandonedStation " + id + " has invalid station construction factory " + key);
                        continue;
                    }
                    BoggledStationConstructors.StationConstructionData scd = factory.constructFromJSON(id, stationColonizationDataString);
                    if (scd != null) {
                        stationConstructionData.add(scd);
                    } else {
                        log.error("ColonizeAbandonedStation " + id + " has invalid station construction data " + key);
                    }
                }
            }
            BoggledStationConstructors.StationConstructionData defaultStationConstructionData = new BoggledStationConstructors.DefaultConstructionData("default_station", new ArrayList<String>());
            return new BoggledTerraformingProjectEffect.ColonizeAbandonedStation(id, enableSettings, defaultStationConstructionData, stationConstructionData);
        }
    }
}
