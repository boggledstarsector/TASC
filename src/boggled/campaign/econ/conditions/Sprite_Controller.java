
package boggled.campaign.econ.conditions;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import java.util.Map;
import boggled.campaign.econ.boggledTools;

public class Sprite_Controller extends BaseHazardCondition {
    public Sprite_Controller() { }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        MarketAPI market = this.market;
        SectorEntityToken entity = market.getPrimaryEntity();

        // Remove condition if it somehow ends up someplace other than one of the stations created by this mod.
        if(!entity.hasTag("boggled_astropolis") && !entity.hasTag("boggled_mining_station") && !entity.hasTag("boggled_siphon_station")) {
            market.removeCondition("sprite_controller");
            return;
        }

        if(entity.hasTag("boggled_astropolis")) {
            if(entity.getCustomEntityType().contains("boggled_astropolis_station_alpha")) {
                if(market.getFactionId().equals(Factions.NEUTRAL)) {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "alpha");
                }

                if(market.getSize() >= 5 && !entity.getCustomEntityType().equals("boggled_astropolis_station_alpha_medium") && !entity.getCustomEntityType().equals("boggled_astropolis_station_alpha_large")) {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "alpha");
                    boggledTools.swapStationSprite(entity, "astropolis", "alpha", 2);
                } else if(market.getSize() >= 6 && !entity.getCustomEntityType().equals("boggled_astropolis_station_alpha_large")) {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "alpha");
                    boggledTools.swapStationSprite(entity, "astropolis", "alpha", 3);
                }
            } else if(entity.getCustomEntityType().contains("boggled_astropolis_station_beta")) {
                if(market.getFactionId().equals(Factions.NEUTRAL)) {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "beta");
                }

                if(market.getSize() >= 5 && !entity.getCustomEntityType().equals("boggled_astropolis_station_beta_medium") && !entity.getCustomEntityType().equals("boggled_astropolis_station_beta_large")) {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "beta");
                    boggledTools.swapStationSprite(entity, "astropolis", "beta", 2);
                } else if(market.getSize() >= 6 && !entity.getCustomEntityType().equals("boggled_astropolis_station_beta_large")) {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "beta");
                    boggledTools.swapStationSprite(entity, "astropolis", "beta", 3);
                }
            }
            else if(entity.getCustomEntityType().contains("boggled_astropolis_station_gamma")) {
                if(market.getFactionId().equals(Factions.NEUTRAL)) {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "gamma");
                }

                if(market.getSize() >= 5 && !entity.getCustomEntityType().equals("boggled_astropolis_station_gamma_medium") && !entity.getCustomEntityType().equals("boggled_astropolis_station_gamma_large")) {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "gamma");
                    boggledTools.swapStationSprite(entity, "astropolis", "gamma", 2);
                } else if(market.getSize() >= 6 && !entity.getCustomEntityType().equals("boggled_astropolis_station_gamma_large")) {
                    boggledTools.deleteOldLightsOverlay(entity, "astropolis", "gamma");
                    boggledTools.swapStationSprite(entity, "astropolis", "gamma", 3);
                }
            }
        }
        else if(entity.hasTag("boggled_mining_station")) {
            if(market.getFactionId().equals(Factions.NEUTRAL)) {
                //Remember that we can't identify the correct mining station lights overlay because there could be
                //an unknown number of mining stations in the system in an unknown orbital configuration.
                //Deletes all overlays, then puts them all back.
                StarSystemAPI system = entity.getStarSystem();
                boggledTools.deleteOldLightsOverlay(entity, "mining", null);
                boggledTools.reapplyMiningStationLights(system);
            }

            if(market.getSize() >= 5 && !entity.getCustomEntityType().equals("boggled_mining_station_medium")) {
                StarSystemAPI system = entity.getStarSystem();
                boggledTools.deleteOldLightsOverlay(entity, "mining", null);
                boggledTools.swapStationSprite(entity, "mining", "null", 2);
                boggledTools.reapplyMiningStationLights(system);
            }
        }
        else if(entity.hasTag("boggled_siphon_station")) {
            if(market.getFactionId().equals(Factions.NEUTRAL)) {
                boggledTools.deleteOldLightsOverlay(entity, "siphon", null);
            }

            if(market.getSize() >= 5 && !entity.getCustomEntityType().equals("boggled_siphon_station_medium")) {
                boggledTools.deleteOldLightsOverlay(entity, "siphon", null);
                boggledTools.swapStationSprite(entity, "siphon", "null", 2);
            }
        }
    }

    @Override
    public void apply(String id) { super.apply(id); }

    @Override
    public void unapply(String id) { super.unapply(id); }

    @Override
    public Map<String, String> getTokenReplacements() { return super.getTokenReplacements(); }

    @Override
    public boolean showIcon() { return false; }
}
