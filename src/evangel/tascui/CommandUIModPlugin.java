package evangel.tascui;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import data.campaign.econ.boggledTools;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public final class CommandUIModPlugin extends BaseModPlugin {
    private static final Logger LOGGER = LogManager.getLogger("evangel.tascui.CommandUI_ModPlugin");
    @Override
    public void onApplicationLoad() throws Exception {
        super.onApplicationLoad();
    }

    @Override
    public void onNewGame() {
        super.onNewGame();
    }

    public void applyTerraformingAbilitiesPerSettingsFile() {
        if (boggledTools.getBooleanSetting("boggledTerraformingContentEnabled")) {
            if (!Global.getSector().getPlayerFleet().hasAbility("evangel_open_terraforming_control_panel")) {
                Global.getSector().getCharacterData().addAbility("evangel_open_terraforming_control_panel");
            }
            if (Global.getSector().getPlayerFleet().hasAbility("boggled_open_terraforming_control_panel")) {
                Global.getSector().getCharacterData().removeAbility("boggled_open_terraforming_control_panel");
            }
        } else {
            Global.getSector().getCharacterData().removeAbility("evangel_open_terraforming_control_panel");
        }
    }

    @Override
    public void beforeGameSave() {
        super.beforeGameSave();
        Global.getSector().getCharacterData().removeAbility("evangel_open_terraforming_control_panel");
    }

    @Override
    public void afterGameSave() {
        super.afterGameSave();
        this.applyTerraformingAbilitiesPerSettingsFile();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);
        this.applyTerraformingAbilitiesPerSettingsFile();
    }
}
