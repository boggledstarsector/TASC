package boggled.scripts.PlayerCargoCalculations;

import com.fs.starfarer.api.Global;

public class bogglesDefaultCargo {
    public static bogglesDefaultCargo active;
    public static final String Abandoned_Station="Ab_S",Astropolis_Station="As_S", Mining_Station ="Mi_S",Siphon_Station="Si_S";
    public float getCommodityAmount(String stationType, String commodity){
        return Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(commodity);
    }
    public void removeCommodity(String stationType, String commodity, float amount){
        Global.getSector().getPlayerFleet().getCargo().removeCommodity(commodity,amount);
    }
}
