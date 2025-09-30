package boggled.ui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class BoggledTerraformingCoreUI implements CustomUIPanelPlugin {
    CustomPanelAPI mainPanel;

    public static Color base = Global.getSector().getPlayerFaction().getBaseUIColor();
    public static Color bg = Global.getSector().getPlayerFaction().getDarkUIColor();

    public static float WIDTH = Global.getSettings().getScreenWidth();
    public static float HEIGHT = Global.getSettings().getScreenHeight() - 10.0F;

    private final float planetVisualWidth = 130;
    private final float planetVisualHeight = 130;

    private final float panePlanetVisualWidth = 400;
    private final float panePlanetVisualHeight = 400;

    private MarketAPI selectedMarket = null;

    private List<MarketAPI> markets = new ArrayList<>();

    private HashMap<ButtonAPI, MarketAPI> buttonToMarketMap = new HashMap<>();

    private CustomPanelAPI leftTerraformingPane = null;

    public BoggledTerraformingCoreUI() { }

    public void init(Object data)
    {
        this.mainPanel = Global.getSettings().createCustom(WIDTH, HEIGHT - 10f, this);

        CustomPanelAPI testingPanel = Global.getSettings().createCustom(planetVisualWidth,600,null);
        this.markets = Global.getSector().getEconomy().getMarkets(Global.getSector().getPlayerFleet().getStarSystem());
        markets.sort(Comparator.comparing(MarketAPI::getName));

        TooltipMakerAPI dummySectionHeaderTooltip = testingPanel.createUIElement(planetVisualWidth,0,false);
        dummySectionHeaderTooltip.addSectionHeading("Colonies", Alignment.MID, 0.0F);

        TooltipMakerAPI tooltip1 = testingPanel.createUIElement(planetVisualWidth,600,true);

        float yPos = 0;
        boolean setFirstMarket = false;
        for(MarketAPI market : markets)
        {
            if(market.getPlanetEntity() != null)
            {
                tooltip1.showPlanetInfo(market.getPlanetEntity(), planetVisualWidth, planetVisualHeight, true, 0);
                UIComponentAPI planetVisual = tooltip1.getPrev();

                ButtonAPI button = tooltip1.addAreaCheckbox("", market, Global.getSector().getPlayerFaction().getDarkUIColor(), new Color(0, 0, 0, 0), Global.getSector().getPlayerFaction().getDarkUIColor(), planetVisualWidth, planetVisualHeight, 0.0F);
                button.setEnabled(true);
                tooltip1.addComponent(button).inTL(0, yPos);
                tooltip1.sendToBottom(button);
                tooltip1.bringComponentToTop(planetVisual);
                button.unhighlight();
                button.setChecked(false);
                if(!setFirstMarket)
                {
                    button.setChecked(true);
                    setFirstMarket = true;
                }
                buttonToMarketMap.put(button, market);
                yPos += planetVisualHeight;
            }
        }

        testingPanel.addUIElement(dummySectionHeaderTooltip).inTL(0, 0);
        testingPanel.addUIElement(tooltip1).inTL(0,18);
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

    public void advance(float amount)
    {
        ButtonAPI checkedButton = null;
        for(ButtonAPI button : buttonToMarketMap.keySet())
        {
            if(button.isChecked())
            {
                checkedButton = button;

                for(ButtonAPI buttonToDeselect : buttonToMarketMap.keySet())
                {
                    if(buttonToDeselect.isChecked())
                    {
                        button.setChecked(false);
                    }

                    if(button.isHighlighted())
                    {
                        button.unhighlight();
                    }
                }

                break;
            }
        }

        if(checkedButton != null)
        {
            checkedButton.highlight();
            if(this.selectedMarket != buttonToMarketMap.get(checkedButton))
            {
                this.selectedMarket = buttonToMarketMap.get(checkedButton);
                this.mainPanel.removeComponent(this.leftTerraformingPane);
                this.leftTerraformingPane = showTerraformingLeftPane(this.selectedMarket);
            }
        }
    }

    private CustomPanelAPI showTerraformingLeftPane(MarketAPI market)
    {
        CustomPanelAPI leftPanel = this.mainPanel.createCustomPanel(panePlanetVisualWidth, 600, null);
        TooltipMakerAPI planetLargeViewLeft = leftPanel.createUIElement(panePlanetVisualWidth, panePlanetVisualHeight, false);
        planetLargeViewLeft.addSectionHeading(this.selectedMarket.getName() + " - Current Appearance", Alignment.MID, 0.0F);
        planetLargeViewLeft.showPlanetInfo(market.getPlanetEntity(), panePlanetVisualWidth, panePlanetVisualHeight, false, 0);

        TooltipMakerAPI conditionsViewHeader = leftPanel.createUIElement(panePlanetVisualWidth, 0, false);
        conditionsViewHeader.addSectionHeading(this.selectedMarket.getName() + " - Current Conditions", Alignment.MID, 0.0F);

        TooltipMakerAPI conditionsView = leftPanel.createUIElement(panePlanetVisualWidth, 40, false);
        float horizontalPosition = 0;
        ArrayList<MarketConditionAPI> conditions = (ArrayList<MarketConditionAPI>) market.getConditions();
        conditions.sort(Comparator.comparing(BoggledTerraformingCoreUI::getSortOrderForCondition));
        for(MarketConditionAPI condition : market.getConditions())
        {
            if(!condition.isPlanetary() || !condition.getPlugin().showIcon() || condition.getName().equals("Population"))
            {
                continue;
            }
            String pathToImage = Global.getSettings().getMarketConditionSpec(condition.getId()).getIcon();
            conditionsView.addImage(pathToImage,0);
            UIComponentAPI conditionImage = conditionsView.getPrev();
            conditionImage.getPosition().inTL(horizontalPosition, 0);
            horizontalPosition += 40;
        }

        TooltipMakerAPI projectsViewHeader = leftPanel.createUIElement(panePlanetVisualWidth, 0, false);
        projectsViewHeader.addSectionHeading("Terraforming Projects", Alignment.MID, 0.0F);

        TooltipMakerAPI projectsView = leftPanel.createUIElement(panePlanetVisualWidth, 600 - planetVisualHeight - 112, true);
        float projectHeight = 1;
        for(int i = 0; i < 40; i++)
        {
            ButtonAPI projectButton = projectsView.addButton("Test Button Name", (Object)null, Global.getSector().getPlayerFaction().getBaseUIColor(), Global.getSector().getPlayerFaction().getDarkUIColor(), Alignment.TL, CutStyle.ALL, panePlanetVisualWidth - 4, 18, 0.0F);
            projectsView.addComponent(projectButton).inTL(0, projectHeight);
            projectHeight += 18 + 1;
        }

        leftPanel.addUIElement(planetLargeViewLeft).inTL(0, 0);

        leftPanel.addUIElement(conditionsViewHeader).inTL(0, panePlanetVisualHeight + 18);
        leftPanel.addUIElement(conditionsView).inTL(0, panePlanetVisualHeight + 36);

        leftPanel.addUIElement(projectsViewHeader).inTL(0, panePlanetVisualHeight + 94);
        leftPanel.addUIElement(projectsView).inTL(0, panePlanetVisualHeight + 112);

        this.mainPanel.addComponent(leftPanel).inTL(planetVisualWidth, 0);
        return leftPanel;
    }

    private static float getSortOrderForCondition(MarketConditionAPI condition)
    {
        return -1 * Global.getSettings().getMarketConditionSpec(condition.getId()).getOrder();
    }

    public void processInput(List<InputEventAPI> events) {
    }

    public void buttonPressed(Object buttonId) {
    }
}
