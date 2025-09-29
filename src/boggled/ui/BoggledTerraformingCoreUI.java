package boggled.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;

import java.awt.Color;
import java.util.List;

public class BoggledTerraformingCoreUI implements CustomUIPanelPlugin {
    CustomPanelAPI mainPanel;

    public static Color base = Global.getSector().getPlayerFaction().getBaseUIColor();
    public static Color bg = Global.getSector().getPlayerFaction().getDarkUIColor();

    public static float WIDTH = Global.getSettings().getScreenWidth();
    public static float HEIGHT = Global.getSettings().getScreenHeight() - 10.0F;

    public BoggledTerraformingCoreUI() { }

    public void init(Object data)
    {
        this.mainPanel = Global.getSettings().createCustom(WIDTH, HEIGHT - 10f, this);

        CustomPanelAPI testingPanel = Global.getSettings().createCustom(110,600,null);
        List<MarketAPI> markets = Global.getSector().getEconomy().getMarkets(Global.getSector().getPlayerFleet().getStarSystem());

        TooltipMakerAPI tooltip1 = testingPanel.createUIElement(110,600,true);
        float yPos = 0;
        for(MarketAPI market : markets)
        {
            if(market.getPlanetEntity() != null)
            {
                tooltip1.showPlanetInfo(market.getPlanetEntity(), 110, 110, true, 0);
                UIComponentAPI planetVisual = tooltip1.getPrev();

                ButtonAPI button = tooltip1.addAreaCheckbox("", market, Global.getSector().getPlayerFaction().getDarkUIColor(), new Color(0, 0, 0, 0), Global.getSector().getPlayerFaction().getDarkUIColor(), 110, 110, 0.0F);
                button.setOpacity(100);
                button.setEnabled(true);
                tooltip1.addComponent(button).inTL(0, yPos);
                tooltip1.sendToBottom(button);
                tooltip1.bringComponentToTop(planetVisual);
                button.unhighlight();
                button.setChecked(false);
                yPos += 110;
            }
        }
        testingPanel.addUIElement(tooltip1).inTL(0,0);
        mainPanel.addComponent(testingPanel).inTL(0,0);
    }

    public CustomPanelAPI getMainPanel() {
        return this.mainPanel;
    }
    public void positionChanged(PositionAPI position) {
    }

    public void renderBelow(float alphaMult) {
    }

    public void render(float alphaMult) {
    }

    public void advance(float amount) {
    }

    public void processInput(List<InputEventAPI> events) {
    }

    public void buttonPressed(Object buttonId) {
    }
}
