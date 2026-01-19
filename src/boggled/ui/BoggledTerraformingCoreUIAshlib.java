package boggled.ui;

import ashlib.data.plugins.coreui.CommandUIPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class BoggledTerraformingCoreUIAshlib extends CommandUIPlugin {
    public static float SCREEN_WIDTH = BoggledTerraformingCoreUI.SCREEN_WIDTH;
    public static float SCREEN_HEIGHT = BoggledTerraformingCoreUI.SCREEN_HEIGHT;

    public BoggledTerraformingCoreUIAshlib() {
        super(SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    @Override
    public void init(String panelToShowcase, Object data) {
        init(null);
    }

    public void init(MarketAPI market)
    {
        BoggledTerraformingCoreUI boggledTerraformingCoreUI = new BoggledTerraformingCoreUI();
        boggledTerraformingCoreUI.init(market);
        CustomPanelAPI terraformingMainPanel = boggledTerraformingCoreUI.getMainPanel();
        this.mainPanel.addComponent(terraformingMainPanel).inTL(0,0);
    }

    public void advance(float amount)
    {

    }

    public void processInput(List<InputEventAPI> events) {
    }

    public void buttonPressed(Object buttonId) {
    }

    public void positionChanged(PositionAPI position) {
    }

    public void renderBelow(float alphaMult) {
    }

    public void render(float alphaMult) {
    }
}
