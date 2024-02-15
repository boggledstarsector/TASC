package boggled.scripts.PlayerCargoCalculations;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;

public class bogglesDefaultCargo {
    public static bogglesDefaultCargo active;
    public static final String Abandoned_Station = "Ab_S";
    public static final String Astropolis_Station = "As_S";
    public static final String Mining_Station = "Mi_S";
    public static final String Siphon_Station = "Si_S";
    public float getCommodityAmount(CargoAPI cargo, String stationType, String commodity){
        return Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(commodity);
    }
    public void removeCommodity(CargoAPI cargo, String stationType, String commodity, float amount){
        Global.getSector().getPlayerFleet().getCargo().removeCommodity(commodity,amount);
    }
}
