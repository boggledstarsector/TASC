package data.scripts;

import data.campaign.econ.boggledTools;

public class BoggledTerraformingProjectEffectFactory {
    public interface TerraformingProjectEffectFactory {
        public abstract BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data);
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
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            String[] optionAndData = data.split(boggledTools.csvSubOptionSeparator);

            boggledTools.CheckMarketConditionExists(optionAndData[1]);

            if (boggledTools.getBooleanSetting(optionAndData[0])) {
                return new BoggledTerraformingProjectEffect.MarketAddConditionProjectEffect(optionAndData[1]);
            }
            return new BoggledTerraformingProjectEffect.MarketRemoveConditionProjectEffect(optionAndData[1]);
        }
    }

    public static class MarketProgressResource implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            String[] resourceAndStep = data.split(boggledTools.csvSubOptionSeparator);
            assert(resourceAndStep.length == 2);
            String resource = resourceAndStep[0];
            int step = Integer.parseInt(resourceAndStep[1]);

            boggledTools.CheckResourceExists(resource);

            return new BoggledTerraformingProjectEffect.MarketProgressResourceProjectEffect(resource, step);
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
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            String[] resourceAndStep = data.split(boggledTools.csvSubOptionSeparator);
            assert(resourceAndStep.length == 2);
            String resource = resourceAndStep[0];
            int step = Integer.parseInt(resourceAndStep[1]);

            boggledTools.CheckResourceExists(resource);

            return new BoggledTerraformingProjectEffect.FocusMarketProgressResourceProjectEffect(resource, step);
        }
    }

    public static class FocusMarketAndSiphonStationProgressResource implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            String[] resourceAndStep = data.split(boggledTools.csvSubOptionSeparator);
            assert(resourceAndStep.length == 2);
            String resource = resourceAndStep[0];
            int step = Integer.parseInt(resourceAndStep[1]);

            boggledTools.CheckResourceExists(resource);

            return new BoggledTerraformingProjectEffect.FocusMarketAndSiphonStationProgressResourceProjectEffect(resource, step);
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
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            String[] submarketIdAnditemIdAndQuantity = data.split(boggledTools.csvSubOptionSeparator);
            String submarketId = submarketIdAnditemIdAndQuantity[0];
            String commodityId = submarketIdAnditemIdAndQuantity[1];
            int quantity = Integer.parseInt(submarketIdAnditemIdAndQuantity[2]);

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
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            String[] submarketIdAnditemIdAndQuantity = data.split(boggledTools.csvSubOptionSeparator);
            String submarketId = submarketIdAnditemIdAndQuantity[0];
            String itemId = submarketIdAnditemIdAndQuantity[1];
            int quantity = Integer.parseInt(submarketIdAnditemIdAndQuantity[2]);

            boggledTools.CheckSubmarketExists(submarketId);
            boggledTools.CheckItemExists(itemId);

            return new BoggledTerraformingProjectEffect.AddItemToMarketStorage(submarketId, itemId, quantity);
        }
    }
}
