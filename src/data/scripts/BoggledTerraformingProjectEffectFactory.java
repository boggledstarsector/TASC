package data.scripts;

import data.campaign.econ.boggledTools;
import org.json.JSONException;
import org.json.JSONObject;

public class BoggledTerraformingProjectEffectFactory {
    public interface TerraformingProjectEffectFactory {
        public abstract BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) throws JSONException;
    }

    public static class PlanetTypeChange implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            boggledTools.CheckPlanetTypeExists(data);
            return new BoggledTerraformingProjectEffect.PlanetTypeChangeProjectEffect(data);
        }
    }

    public static class MarketAddCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            boggledTools.CheckMarketConditionExists(data);
            return new BoggledTerraformingProjectEffect.MarketAddConditionProjectEffect(data);
        }
    }

    public static class MarketRemoveCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            boggledTools.CheckMarketConditionExists(data);
            return new BoggledTerraformingProjectEffect.MarketRemoveConditionProjectEffect(data);
        }
    }

    public static class MarketOptionalCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String option = jsonData.getString("option");
            String conditionId = jsonData.getString("condition_id");

            boggledTools.CheckMarketConditionExists(conditionId);

            if (boggledTools.getBooleanSetting(option)) {
                return new BoggledTerraformingProjectEffect.MarketAddConditionProjectEffect(conditionId);
            }
            return new BoggledTerraformingProjectEffect.MarketRemoveConditionProjectEffect(conditionId);
        }
    }

    public static class MarketProgressResource implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String resourceId = jsonData.getString("resource_id");
            int step = jsonData.getInt("step");

            boggledTools.CheckResourceExists(resourceId);

            return new BoggledTerraformingProjectEffect.MarketProgressResourceProjectEffect(resourceId, step);
        }
    }

    public static class FocusMarketAddCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            boggledTools.CheckMarketConditionExists(data);
            return new BoggledTerraformingProjectEffect.FocusMarketAddConditionProjectEffect(data);
        }
    }

    public static class FocusMarketRemoveCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            boggledTools.CheckMarketConditionExists(data);
            return new BoggledTerraformingProjectEffect.FocusMarketRemoveConditionProjectEffect(data);
        }
    }

    public static class FocusMarketProgressResource implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String resourceId = jsonData.getString("resource_id");
            int step = jsonData.getInt("step");

            boggledTools.CheckResourceExists(resourceId);

            return new BoggledTerraformingProjectEffect.FocusMarketProgressResourceProjectEffect(resourceId, step);
        }
    }

    public static class FocusMarketAndSiphonStationProgressResource implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String resourceId = jsonData.getString("resource_id");
            int step = jsonData.getInt("step");

            boggledTools.CheckResourceExists(resourceId);

            return new BoggledTerraformingProjectEffect.FocusMarketAndSiphonStationProgressResourceProjectEffect(resourceId, step);
        }
    }

    public static class SystemAddCoronalTapFactory implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            return new BoggledTerraformingProjectEffect.SystemAddCoronalTap();
        }
    }

    public static class MarketAddStellarReflectorsFactory implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            return new BoggledTerraformingProjectEffect.MarketAddStellarReflectors();
        }
    }

    public static class MarketRemoveIndustryFactory implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            return new BoggledTerraformingProjectEffect.MarketRemoveIndustry(data);
        }
    }

    public static class RemoveCommodityFromSubmarketFactory implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String submarketId = jsonData.getString("submarket_id");
            String commodityId = jsonData.getString("commodity_id");
            int quantity = jsonData.getInt("quantity");

            boggledTools.CheckSubmarketExists(submarketId);
            boggledTools.CheckCommodityExists(commodityId);

            return new BoggledTerraformingProjectEffect.RemoveItemFromSubmarket(submarketId, commodityId, quantity);
        }
    }

    public static class RemoveStoryPointsFromPlayerFactory implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            int quantity = Integer.parseInt(data);
            return new BoggledTerraformingProjectEffect.RemoveStoryPointsFromPlayer(quantity);
        }
    }

    public static class AddItemToSubmarketFactory implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) throws JSONException {
            JSONObject jsonData = new JSONObject(data);
            String submarketId = jsonData.getString("submarket_id");
            String itemId = jsonData.getString("item_id");
            int quantity = jsonData.getInt("quantity");

            boggledTools.CheckSubmarketExists(submarketId);
            boggledTools.CheckItemExists(itemId);

            return new BoggledTerraformingProjectEffect.AddItemToMarketStorage(submarketId, itemId, quantity);
        }
    }
}
