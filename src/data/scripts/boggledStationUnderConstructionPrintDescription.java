package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import boggled.campaign.econ.boggledTools;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledStationUnderConstructionPrintDescription extends BaseCommandPlugin
{
    private SectorEntityToken entity;

    public boggledStationUnderConstructionPrintDescription() { }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        if(dialog == null) return false;
        entity = dialog.getInteractionTarget();

        String entityType = entity.getCustomEntityType();
         if(entityType.contains("boggled_mining_station"))
         {
             printDescription("mining", dialog);
         }
         else if(entityType.contains("boggled_siphon_station"))
         {
             printDescription("siphon", dialog);
         }
         else if(entityType.contains("boggled_astropolis_station"))
         {
             printDescription("astropolis", dialog);
         }

        return true;
    }

    public void printDescription(String type, InteractionDialogAPI dialog)
    {
        int daysRemaining = boggledTools.getIntSetting("boggledStationConstructionDelayDays") - boggledTools.getConstructionProgressDays(entity);
        String dayOrDays = "days";
        if(daysRemaining == 1)
        {
            dayOrDays = "day";
        }

        switch (type) {
            case "mining":
                dialog.getTextPanel().addParagraph("This mining station is currently under construction. It will be completed in approximately " + daysRemaining + " " + dayOrDays + ".");
                break;
            case "siphon":
                dialog.getTextPanel().addParagraph("This siphon station is currently under construction. It will be completed in approximately " + daysRemaining + " " + dayOrDays + ".");
                break;
            case "astropolis":
                dialog.getTextPanel().addParagraph("This astropolis station is currently under construction. It will be completed in approximately " + daysRemaining + " " + dayOrDays + ".");
                break;
        }
    }
}