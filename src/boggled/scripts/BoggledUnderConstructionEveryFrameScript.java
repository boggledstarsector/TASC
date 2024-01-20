package boggled.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import boggled.campaign.econ.boggledTools;

public class BoggledUnderConstructionEveryFrameScript implements EveryFrameScript
{
    private SectorEntityToken stationEntity;
    private boolean isDone = false;
    private int requiredDays = boggledTools.getIntSetting("boggledStationConstructionDelayDays");

    public BoggledUnderConstructionEveryFrameScript(SectorEntityToken station)
    {
        this.stationEntity = station;

        CampaignClockAPI clock = Global.getSector().getClock();
        stationEntity.addTag("boggled_construction_progress_lastDayChecked_" + clock.getDay());
        stationEntity.addTag("boggled_construction_progress_days_0");
    }

    public boolean isDone() {
        return isDone;
    }

    public boolean runWhilePaused()
    {
        return false;
    }

    public void advance(float var1)
    {
        CampaignClockAPI clock = Global.getSector().getClock();

        // Reload day check
        int lastDayChecked = boggledTools.getLastDayCheckedForConstruction(stationEntity);

        // Exit if a day has not passed
        if(clock.getDay() == lastDayChecked)
        {
            return;
        }
        // Add one day to the construction progress
        boggledTools.incrementConstructionProgressDays(stationEntity, 1);

        //Check if construction should be completed today
        int progress = boggledTools.getConstructionProgressDays(stationEntity);
        if(progress >= requiredDays)
        {
            isDone = true;
            String entityType = stationEntity.getCustomEntityType();
            if(entityType.contains("boggled_mining_station"))
            {
                boggledTools.createMiningStationMarket(stationEntity);
            }
            else if(entityType.contains("boggled_siphon_station"))
            {
                boggledTools.createSiphonStationMarket(stationEntity, stationEntity.getOrbitFocus());
            }
            else if(entityType.contains("boggled_astropolis_station"))
            {
                boggledTools.createAstropolisStationMarket(stationEntity, stationEntity.getOrbitFocus());
            }
        }

        //Update the lastDayChecked to today
        boggledTools.clearClockCheckTagsForConstruction(stationEntity);
        stationEntity.addTag("boggled_construction_progress_lastDayChecked_" + clock.getDay());
    }
}