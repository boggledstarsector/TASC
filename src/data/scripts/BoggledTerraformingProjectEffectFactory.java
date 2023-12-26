package data.scripts;

import data.campaign.econ.boggledTools;

public class BoggledTerraformingProjectEffectFactory {
    public interface TerraformingProjectEffectFactory {
        BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data);
    }

    public static class PlanetTypeChange implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            return new BoggledTerraformingProjectEffect.PlanetTypeChangeProjectEffect(data);
        }
    }

    public static class MarketAddCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            return new BoggledTerraformingProjectEffect.MarketAddConditionProjectEffect(data);
        }
    }

    public static class MarketRemoveCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            return new BoggledTerraformingProjectEffect.MarketRemoveConditionProjectEffect(data);
        }
    }

    public static class MarketOptionalCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            String[] optionAndData = data.split(boggledTools.csvSubOptionSeparator);
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
            return new BoggledTerraformingProjectEffect.MarketProgressResourceProjectEffect(resource, step);
        }
    }

    public static class FocusMarketAddCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
            return new BoggledTerraformingProjectEffect.FocusMarketAddConditionProjectEffect(data);
        }
    }

    public static class FocusMarketRemoveCondition implements TerraformingProjectEffectFactory {
        @Override
        public BoggledTerraformingProjectEffect.TerraformingProjectEffect constructFromJSON(String data) {
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
            return new BoggledTerraformingProjectEffect.FocusMarketAndSiphonStationProgressResourceProjectEffect(resource, step);
        }
    }
}
