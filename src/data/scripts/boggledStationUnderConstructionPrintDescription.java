package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import boggled.campaign.econ.boggledTools;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledStationUnderConstructionPrintDescription extends BaseCommandPlugin {
    public boggledStationUnderConstructionPrintDescription() { }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if(dialog == null) {
            return false;
        }
        SectorEntityToken entity = dialog.getInteractionTarget();
        String stationType = boggledTools.getStationTypeName(entity);
        int progressDays = boggledTools.getConstructionProgressDays(entity);
        int requiredDays = boggledTools.getConstructionRequiredDays(entity);
        int daysRemaining = requiredDays - progressDays;

        String dayOrDays = daysRemaining == 1 ? "day" : "days";
        dialog.getTextPanel().addParagraph("This " + stationType + " station is currently under construction. It will be completed in approximately " + daysRemaining + " " + dayOrDays + ".");

        return true;
    }
}
