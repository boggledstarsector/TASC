package boggled.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import data.kaysaar.aotd.vok.scripts.research.AoTDFactionResearchManager;
import data.kaysaar.aotd.vok.scripts.research.AoTDMainResearchManager;

import java.util.List;
import java.util.Map;

public class BoggledAotDEveryFrameScript implements EveryFrameScript {
    Map<List<String>, List<String>> researchAndAbilityIds;
    public BoggledAotDEveryFrameScript(Map<List<String>, List<String>> researchAndAbilityIds) {
        this.researchAndAbilityIds = researchAndAbilityIds;
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
        for (Map.Entry<List<String>, List<String>> entry : researchAndAbilityIds.entrySet()) {
            for (String researchId : entry.getKey()) {
                if (!manager.haveResearched(researchId)) {
                    continue;
                }

                for (String abilityId : entry.getValue()) {
                    if (!fleet.hasAbility(abilityId)) {
                        characterData.addAbility(abilityId);
                    }
                }
            }
        }
    }
}
