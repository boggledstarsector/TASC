package boggled.scripts.PlayerCargoCalculations;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import data.scripts.crewReplacer_Job;
import data.scripts.crewReplacer_Main;

public class booglesCrewReplacerCargo extends bogglesDefaultCargo {
    private final String name = "boogles_TerraformingAndStationConstruction";
    public booglesCrewReplacerCargo(){
        String[] abandonedStationCommodities = { Commodities.METALS, Commodities.RARE_METALS, Commodities.CREW, Commodities.HEAVY_MACHINERY };
        String[] astropolisStationCommodities = { Commodities.METALS, Commodities.RARE_METALS, Commodities.CREW, Commodities.HEAVY_MACHINERY };
        String[] miningStationCommodities = { Commodities.METALS, Commodities.RARE_METALS, Commodities.CREW, Commodities.HEAVY_MACHINERY };
        String[] siphonStationCommodities = { Commodities.METALS, Commodities.RARE_METALS, Commodities.CREW, Commodities.HEAVY_MACHINERY };
        createJobs(Abandoned_Station, abandonedStationCommodities);
        createJobs(Astropolis_Station, astropolisStationCommodities);
        createJobs(Mining_Station, miningStationCommodities);
        createJobs(Siphon_Station, siphonStationCommodities);
    }

    private void createJobs(String stationType, String[] jobMaterials){
        for (String jobMaterial : jobMaterials) {
            String temp = this.CN(stationType, jobMaterial);
            crewReplacer_Job job = crewReplacer_Main.getJob(temp);
            String crewSetName = name + "_" + jobMaterial;
            crewReplacer_Main.getCrewSet(crewSetName).addCrewSet(jobMaterial); // this crew set is created here, and adds the base commodity crew set this set. reason: this lets a player add a commodity to all stations (like adding a new station metal) at once. or just to one type of station by useing the job, and not the crew set. or to everything connected to crew replacer with the same commodityID. feel free to ask questions.
            job.addCrewSet(crewSetName);
            job.addNewCrew(jobMaterial, 1, 10); // sets the defalt value of the defalt commodity. this is defalt defalt.
        }
    }

    @Override
    public float getCommodityAmount(CargoAPI cargo, String stationType, String commodity) {
        return crewReplacer_Main.getJob(CN(stationType,commodity)).getAvailableCrewPower(cargo);
    }

    @Override
    public void removeCommodity(CargoAPI cargo, String stationType, String commodity, float amount) {
        crewReplacer_Main.getJob(CN(stationType,commodity)).automaticlyGetAndApplyCrewLost(cargo,(int)amount, (int)amount);
    }
    private String CN(String stationType, String commodity){
        return name + "_" + stationType + "_" + commodity;
    }
}
