package boggled.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import boggled.campaign.econ.boggledTools;

// I removed this feature because of the bug where the market must be opened upon station building completion or else the population grows like crazy
// If the station builds immediately it makes sense for the player to interact with the market, if there's a delay we will have to open the market
// for player interaction remotely, which should not occur.
public class BoggledUnderConstructionEveryFrameScript implements EveryFrameScript
{
    private final SectorEntityToken stationEntity;
    private final BoggledStationConstructors.StationConstructionData stationConstructionData;
    private boolean isDone = false;
    private final int requiredDays;

    public BoggledUnderConstructionEveryFrameScript(BoggledTerraformingRequirement.RequirementContext ctx, SectorEntityToken station, BoggledStationConstructors.StationConstructionData stationConstructionData) {
        this.stationEntity = station;
        this.stationConstructionData = stationConstructionData;
        this.requiredDays = ctx.getProject().getModifiedProjectDuration(ctx);

        CampaignClockAPI clock = Global.getSector().getClock();
        stationEntity.addTag("boggled_construction_progress_lastDayChecked_" + clock.getDay());
        stationEntity.addTag("boggled_construction_progress_days_0");
        stationEntity.addTag("boggled_construction_required_days_" + requiredDays);
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float var1) {
        CampaignClockAPI clock = Global.getSector().getClock();

        if (!boggledTools.getBooleanSetting("boggledStationConstructionDelayEnabled")) {
            isDone = true;
            stationConstructionData.createMarket(stationEntity);
        }

        // Reload day check
        int lastDayChecked = boggledTools.getLastDayCheckedForConstruction(stationEntity);

        // Exit if a day has not passed
        if(clock.getDay() == lastDayChecked) {
            return;
        }
        // Add one day to the construction progress
        boggledTools.incrementConstructionProgressDays(stationEntity, 1);

        //Check if construction should be completed today
        int progress = boggledTools.getConstructionProgressDays(stationEntity);
        if(progress >= requiredDays) {
            isDone = true;
            stationConstructionData.createMarket(stationEntity);
        }

        //Update the lastDayChecked to today
        boggledTools.clearClockCheckTagsForConstruction(stationEntity);
        stationEntity.addTag("boggled_construction_progress_lastDayChecked_" + clock.getDay());
    }
}