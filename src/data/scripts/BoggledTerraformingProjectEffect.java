package data.scripts;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import data.campaign.econ.boggledTools;
import data.campaign.econ.industries.BoggledCommonIndustry;

import java.util.ArrayList;

public class BoggledTerraformingProjectEffect {
    public interface TerraformingProjectEffect {
        void applyProjectEffect(MarketAPI market);
    }

    public static class PlanetTypeChangeProjectEffect implements TerraformingProjectEffect {
        private final String newPlanetType;

        public PlanetTypeChangeProjectEffect(String newPlanetType) {
            this.newPlanetType = newPlanetType;
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            market.getPlanetEntity().changeType(newPlanetType, null);
        }
    }

    public static class MarketAddConditionProjectEffect implements TerraformingProjectEffect {
        private final String condition;

        public MarketAddConditionProjectEffect(String condition) {
            this.condition = condition;
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            boggledTools.addCondition(market, condition);
        }
    }

    public static class MarketRemoveConditionProjectEffect implements TerraformingProjectEffect {
        String condition;

        public MarketRemoveConditionProjectEffect(String condition) {
            this.condition = condition;
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            boggledTools.removeCondition(market, condition);
        }
    }

    public static class MarketProgressResourceProjectEffect implements TerraformingProjectEffect {
        private final String resource;
        private final int step;

        public MarketProgressResourceProjectEffect(String resource, int step) {
            this.resource = resource;
            this.step = step;
        }

        private void incrementResourceWithDefault(MarketAPI market, ArrayList<String> resourceProgression) {
            // Step because OuyangOptimization goes volatiles_trace (0) to volatiles_abundant (2), etc
            String defaultResource = resourceProgression.get(Math.max(0, step - 1));
            boolean resourceFound = false;
            for (int i = 0; i < resourceProgression.size() - 1; ++i) {
                if (market.hasCondition(resourceProgression.get(i))) {
                    boggledTools.removeCondition(market, resourceProgression.get(i));
                    boggledTools.addCondition(market, resourceProgression.get(Math.min(i + step, resourceProgression.size() - 1)));
                    resourceFound = true;
                    break;
                }
            }

            if (!resourceFound && defaultResource != null && !defaultResource.isEmpty()) {
                boggledTools.addCondition(market, defaultResource);
            }
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            ArrayList<String> resourcesProgression = boggledTools.getResourceProgressions().get(resource);
            if (resourcesProgression == null || resourcesProgression.isEmpty()) {
                return;
            }

            incrementResourceWithDefault(market, boggledTools.getResourceProgressions().get(resource));
        }
    }

    public static class FocusMarketAddConditionProjectEffect extends MarketAddConditionProjectEffect {
        public FocusMarketAddConditionProjectEffect(String condition) {
            super(condition);
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            super.applyProjectEffect(BoggledCommonIndustry.getFocusMarketOrMarket(market));
        }
    }

    public static class FocusMarketRemoveConditionProjectEffect extends MarketRemoveConditionProjectEffect {
        public FocusMarketRemoveConditionProjectEffect(String condition) {
            super(condition);
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            super.applyProjectEffect(BoggledCommonIndustry.getFocusMarketOrMarket(market));
        }
    }

    public static class FocusMarketProgressResourceProjectEffect extends MarketProgressResourceProjectEffect {
        public FocusMarketProgressResourceProjectEffect(String resource, int step) {
            super(resource, step);
        }
        @Override
        public void applyProjectEffect(MarketAPI market) {
            super.applyProjectEffect(BoggledCommonIndustry.getFocusMarketOrMarket(market));
        }
    }

    public static class FocusMarketAndSiphonStationProgressResourceProjectEffect extends MarketProgressResourceProjectEffect {
        public FocusMarketAndSiphonStationProgressResourceProjectEffect(String resource, int step) {
            super(resource, step);
        }

        @Override
        public void applyProjectEffect(MarketAPI market) {
            super.applyProjectEffect(BoggledCommonIndustry.getFocusMarketOrMarket(market));

            SectorEntityToken closestGasGiantToken = market.getPrimaryEntity();
            if (closestGasGiantToken == null) {
                return;
            }
            for (SectorEntityToken entity : closestGasGiantToken.getStarSystem().getAllEntities()) {
                /*
                Search through all entities in the system
                Just to find any siphon stations attached to the gas giant this station is orbiting
                Because gas giants can have both acropolis stations and siphon stations
                Should make this more flexible in the future, but for now, eh
                 */
                if (entity.hasTag(Tags.STATION)
                    && entity.getOrbitFocus() != null
                    && entity.getOrbitFocus().equals(closestGasGiantToken)
                    && entity.getMarket() != null
                    && (entity.getCustomEntitySpec().getDefaultName().equals("Side Station")
                        || entity.getCustomEntitySpec().getDefaultName().equals("Siphon Station"))
                    && !entity.getId().equals("beholder_station"))
                {
                    super.applyProjectEffect(entity.getMarket());
                }
            }
        }
    }
}
