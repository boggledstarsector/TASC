package boggled.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import boggled.campaign.econ.boggledTools;
import data.kaysaar.aotd.vok.scripts.research.AoTDFactionResearchManager;
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;

import java.util.Map;

public class BoggledAotDEveryFrameScript implements EveryFrameScript {

    public BoggledAotDEveryFrameScript() {
        // No constructor arguments needed - uses data-driven map from boggledTools
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        AoTDFactionResearchManager manager = AoTDMainResearchManager.getInstance().getManagerForPlayer();
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        CharacterDataAPI characterData = Global.getSector().getCharacterData();

        // Get the data-driven ability-to-research mapping
        Map<String, String> abilityToResearchMap = boggledTools.getAbilityResearchMap();

        for (Map.Entry<String, String> entry : abilityToResearchMap.entrySet()) {
            String abilityId = entry.getKey();
            String researchId = entry.getValue();

            // Check if player has completed the required research
            if (manager.haveResearched(researchId)) {
                // Add ability if player doesn't already have it
                if (!fleet.hasAbility(abilityId)) {
                    characterData.addAbility(abilityId);
                }
            }
        }
    }
}
