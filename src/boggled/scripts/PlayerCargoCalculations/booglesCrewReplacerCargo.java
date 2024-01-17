package boggled.scripts.PlayerCargoCalculations;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import data.scripts.crewReplacer_Job;
import data.scripts.crewReplacer_Main;

public class booglesCrewReplacerCargo extends bogglesDefaultCargo {
    private final String name = "boogles_TerraformingAndStationConstruction";
    public booglesCrewReplacerCargo(){
        String[] AbS_Requierments = {"metals","rare_metals","crew","heavy_machinery"};
        String[] AsS_Requierments = {"metals","rare_metals","crew","heavy_machinery"};
        String[] MiS_Requierments = {"metals","rare_metals","crew","heavy_machinery"};
        String[] SiS_Requierments = {"metals","rare_metals","crew","heavy_machinery"};
        createJobs(Abandoned_Station,AbS_Requierments);
        createJobs(Astropolis_Station,AsS_Requierments);
        createJobs(Mining_Station,MiS_Requierments);
        createJobs(Siphon_Station,SiS_Requierments);
    }
    private void createJobs(String stationType,String[] job_Materials){
        for (String a : job_Materials){
            String temp = this.CN(stationType,a);
            crewReplacer_Job job = crewReplacer_Main.getJob(temp);
            String crewSetName = name+"_"+a;
            crewReplacer_Main.getCrewSet(crewSetName).addCrewSet(a);//this crew set is created here, and adds the base commodity crew set this set. reason: this lets a player add a commodity to all stations (like adding a new station metal) at once. or just to one type of station by useing the job, and not the crew set. or to everything connected to crew replacer with the same commodityID. feel free to ask questions.
            job.addCrewSet(crewSetName);
            job.addNewCrew(a,1,10);//sets the defalt value of the defalt commodity. this is defalt defalt.
        }
    }
    @Override
    public float getCommodityAmount(String stationType, String commodity) {
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        return crewReplacer_Main.getJob(CN(stationType,commodity)).getAvailableCrewPower(cargo);
    }

    @Override
    public void removeCommodity(String stationType, String commodity, float amount) {
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        crewReplacer_Main.getJob(CN(stationType,commodity)).automaticlyGetAndApplyCrewLost(cargo,(int)amount,(int)amount);
    }
    private String CN(String stationType, String commodity){
        return name+"_"+stationType+"_"+commodity;
    }
}
