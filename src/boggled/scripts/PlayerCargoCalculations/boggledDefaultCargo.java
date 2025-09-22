package boggled.scripts.PlayerCargoCalculations;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;

// Only used for subtracting commodities for the four station construction abilities
public class boggledDefaultCargo {

    // Loaded with either the default cargo or the Crew Replacer mod cargo on application load.
    // Static variable so can be accessed anywhere with boggledDefaultCargo.active
    public static boggledDefaultCargo active;
    public static final String Abandoned_Station = "Ab_S";
    public static final String Astropolis_Station = "As_S";
    public static final String Mining_Station = "Mi_S";
    public static final String Siphon_Station = "Si_S";
    public float getCommodityAmount(CargoAPI cargo, String stationType, String commodity)
    {
        return Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity(commodity);
    }
    public void removeCommodity(CargoAPI cargo, String stationType, String commodity, float amount)
    {
        Global.getSector().getPlayerFleet().getCargo().removeCommodity(commodity,amount);
    }
}
