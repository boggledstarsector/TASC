package boggled.ui;

import boggled.campaign.econ.boggledTools;
import boggled.campaign.econ.conditions.Terraforming_Controller;
import boggled.terraforming.BoggledBaseTerraformingProject;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import java.awt.*;
import java.util.*;
import java.util.List;

public class BoggledTerraformingCoreUI implements CustomUIPanelPlugin {
    CustomPanelAPI mainPanel;
    private CustomPanelAPI leftTerraformingPane;
    private CustomPanelAPI rightTerraformingPane;
    private CustomPanelAPI planetSelectPane;

    private TooltipMakerAPI planetSelectView;

    public static float SCREEN_WIDTH = Math.min(Global.getSettings().getScreenWidth(), 1200);
    public static float SCREEN_HEIGHT = Math.min(Global.getSettings().getScreenHeight() - 125, 1000);

    private final float planetSelectPaneWidth = 155;
    private final float scrollPlanetWidth = 130;
    private final float scrollPlanetHeight = 130;

    private final float panePlanetWidth = 400;
    private final float panePlanetHeight = 400;

    private final float paneSeparator = 3;

    private MarketAPI market;
    private BoggledBaseTerraformingProject selectedProject;

    private final List<MarketAPI> playerMarkets = new ArrayList<>();

    private HashMap<UIComponentAPI, MarketAPI> planetVisualToMarketMap = new HashMap<>();

    private ArrayList<UIComponentAPI> planetVisualOrderedList = new ArrayList<>();

    private HashMap<ButtonAPI, MarketAPI> buttonToMarketMap = new HashMap<>();

    private HashMap<MarketAPI, ButtonAPI> marketToButtonMap = new HashMap<>();

    private HashMap<ButtonAPI, BoggledBaseTerraformingProject> buttonToProjectMap = new HashMap<>();

    private ButtonAPI startProjectButton;
    private ButtonAPI cancelProjectButton;

    private final Color transparent = new Color(0, 0, 0, 0);

    private final Color highlight = Global.getSector().getPlayerFaction().getBrightUIColor();

    public BoggledTerraformingCoreUI() { }

    private void populatePlayerMarkets()
    {
        List<MarketAPI> playerMarketsIncludingStations = Misc.getPlayerMarkets(true);
        for(MarketAPI market : playerMarketsIncludingStations)
        {
            if(!boggledTools.marketIsStation(market) && market.getPlanetEntity() != null && market.getFaction() != null)
            {
                this.playerMarkets.add(market);
            }
        }
        this.playerMarkets.sort(Comparator.comparing(MarketAPI::getName));
    }

    private void populatePlanetSelectViewWithPlanetVisuals(TooltipMakerAPI planetSelectView)
    {
        HashMap<UIComponentAPI, MarketAPI> newPlanetVisualToMarketMap = new HashMap<>();
        ArrayList<UIComponentAPI> newPlanetVisualOrderedList = new ArrayList<>();

        for(MarketAPI market : this.playerMarkets)
        {
            planetSelectView.showPlanetInfo(market.getPlanetEntity(), scrollPlanetWidth, scrollPlanetHeight, true, 0);
            UIComponentAPI planetVisual = planetSelectView.getPrev();
            newPlanetVisualToMarketMap.put(planetVisual, market);
            newPlanetVisualOrderedList.add(planetVisual);
        }

        this.planetVisualToMarketMap = newPlanetVisualToMarketMap;
        this.planetVisualOrderedList = newPlanetVisualOrderedList;
    }

    private void populatePlanetSelectViewWithButtons(TooltipMakerAPI planetSelectView, ButtonAPI buttonToKeep)
    {
        // Generate new mappings and replace the existing ones after method is finished
        HashMap<ButtonAPI, MarketAPI> newButtonToMarketMap = new HashMap<>();
        HashMap<MarketAPI, ButtonAPI> newMarketToButtonMap = new HashMap<>();

        // Figure out which market is mapped to the button to keep, if any.
        // Purpose of this is to clear all the other buttons which may be stuck in the highlighted state,
        // while leaving intact the one the player has selected.
        MarketAPI marketToKeep = buttonToKeep != null ? this.buttonToMarketMap.get(buttonToKeep) : null;

        // Remove all buttons except the one the player selected.
        for(ButtonAPI button : this.buttonToMarketMap.keySet())
        {
            if(button != buttonToKeep)
            {
                planetSelectView.removeComponent(button);
            }
        }

        float yPos = 0;
        for(UIComponentAPI planetVisual : this.planetVisualOrderedList)
        {
            if(marketToKeep == null || this.planetVisualToMarketMap.get(planetVisual) != marketToKeep)
            {
                MarketAPI currentMarket = this.planetVisualToMarketMap.get(planetVisual);
                ButtonAPI button = planetSelectView.addAreaCheckbox("", null, highlight, transparent, transparent, planetSelectPaneWidth, scrollPlanetHeight, 0.0F);
                button.setEnabled(true);
                planetSelectView.addComponent(button).inTL(0, yPos);
                button.unhighlight();
                button.setChecked(false);
                newButtonToMarketMap.put(button, currentMarket);
                newMarketToButtonMap.put(currentMarket, button);
            }
            else
            {
                newButtonToMarketMap.put(buttonToKeep, marketToKeep);
                newMarketToButtonMap.put(marketToKeep, buttonToKeep);
            }
            yPos += scrollPlanetHeight;
        }

        this.buttonToMarketMap = newButtonToMarketMap;
        this.marketToButtonMap = newMarketToButtonMap;
    }

    private void handlePlanetSelectPaneZAxis(TooltipMakerAPI planetSelectView)
    {
        // Put all the buttons underneath the animated planets

        for(ButtonAPI button : this.buttonToMarketMap.keySet())
        {
            planetSelectView.sendToBottom(button);
        }

        for(UIComponentAPI planetVisual : this.planetVisualToMarketMap.keySet())
        {
            planetSelectView.bringComponentToTop(planetVisual);
        }
    }

    private CustomPanelAPI createPlanetSelectPane(MarketAPI defaultSelectedMarket)
    {
        CustomPanelAPI planetSelectPane = Global.getSettings().createCustom(planetSelectPaneWidth, SCREEN_HEIGHT,null);

        // Add "Colonies" header
        TooltipMakerAPI planetSelectViewHeader = planetSelectPane.createUIElement(planetSelectPaneWidth,0,false);
        planetSelectViewHeader.addSectionHeading("Colonies", Alignment.MID, 0.0F);

        // Main scrolling panel with all the player colonies
        TooltipMakerAPI planetSelectView = planetSelectPane.createUIElement(planetSelectPaneWidth, SCREEN_HEIGHT,true);

        populatePlanetSelectViewWithPlanetVisuals(planetSelectView);
        populatePlanetSelectViewWithButtons(planetSelectView, null);
        handlePlanetSelectPaneZAxis(planetSelectView);

        // Check the default market button so advance will set up the left terraforming pane for it.
        ButtonAPI defaultMarketButton = this.marketToButtonMap.get(defaultSelectedMarket);
        defaultMarketButton.setChecked(true);

        planetSelectPane.addUIElement(planetSelectViewHeader).inTL(0, 0);
        planetSelectPane.addUIElement(planetSelectView).inTL(0,18);
        this.planetSelectView = planetSelectView;
        this.planetSelectPane = planetSelectPane;
        return planetSelectPane;
    }

    public void init(MarketAPI market)
    {
        // Load this.playerMarkets with all non-station player-controlled markets.
        // This includes Nexerelin purchased governor planets.
        populatePlayerMarkets();

        // this.mainPanel is the overall terraforming menu panel.
        // There will be three panels inserted into it -
        //      1. Planet Select Pane
        //      2. Left Terraforming Pane
        //      3. Right Terraforming Pane
        this.mainPanel = Global.getSettings().createCustom(SCREEN_WIDTH, SCREEN_HEIGHT, this);

        MarketAPI defaultMarket = market != null ? market : this.playerMarkets.get(0);
        mainPanel.addComponent(createPlanetSelectPane(defaultMarket)).inTL(0,0);
    }

    public CustomPanelAPI getMainPanel() {
        return this.mainPanel;
    }

    public void advance(float amount)
    {
        ButtonAPI clickedPlanetSelectButton = getClickedPlanetButton();
        if(clickedPlanetSelectButton != null)
        {
            handlePlanetSelectButtonClicked(clickedPlanetSelectButton);
        }

        ButtonAPI checkedProjectButton = null;
        for(ButtonAPI button : buttonToProjectMap.keySet())
        {
            if(button.isChecked())
            {
                checkedProjectButton = button;

                for(ButtonAPI buttonToDeselect : buttonToProjectMap.keySet())
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

        if(checkedProjectButton != null)
        {
            checkedProjectButton.highlight();
            if(this.selectedProject != buttonToProjectMap.get(checkedProjectButton))
            {
                this.selectedProject = buttonToProjectMap.get(checkedProjectButton);
                this.mainPanel.removeComponent(this.rightTerraformingPane);
                this.rightTerraformingPane = showTerraformingRightPane(this.market, this.selectedProject);
            }
        }

        if(this.startProjectButton != null && this.startProjectButton.isChecked())
        {
            this.startProjectButton.setChecked(false);
            startNewProject(this.market, this.selectedProject);
            return;
        }

        if(this.cancelProjectButton != null && this.cancelProjectButton.isChecked())
        {
            this.cancelProjectButton.setChecked(false);
            cancelProject(this.market);
        }
    }

    private ButtonAPI getClickedPlanetButton()
    {
        for(ButtonAPI button : buttonToMarketMap.keySet())
        {
            if(button.isChecked())
            {
                return button;
            }
        }

        return null;
    }

    private void handlePlanetSelectButtonClicked(ButtonAPI clickedButton)
    {
        clickedButton.highlight();
        clickedButton.setChecked(false);
        populatePlanetSelectViewWithButtons(this.planetSelectView, clickedButton);
        handlePlanetSelectPaneZAxis(this.planetSelectView);
        if(this.market != buttonToMarketMap.get(clickedButton))
        {
            this.market = buttonToMarketMap.get(clickedButton);
            this.selectedProject = null;
            this.buttonToProjectMap = new HashMap<>();
            this.mainPanel.removeComponent(this.leftTerraformingPane);
            this.mainPanel.removeComponent(this.rightTerraformingPane);
            this.leftTerraformingPane = showTerraformingLeftPane(this.market);
        }
    }

    private void handleTerraformingProjectButtonClicked(ButtonAPI button)
    {

    }

    private void handleStartProjectButtonClicked()
    {

    }

    private void handleCancelProjectButtonClicked()
    {

    }

    private CustomPanelAPI showTerraformingLeftPane(MarketAPI market)
    {
        CustomPanelAPI leftPanel = this.mainPanel.createCustomPanel(panePlanetWidth, 600, null);
        TooltipMakerAPI planetLargeViewLeft = leftPanel.createUIElement(panePlanetWidth, panePlanetHeight, false);
        planetLargeViewLeft.addSectionHeading(this.market.getName() + " - Current Appearance", Alignment.MID, 0.0F);
        planetLargeViewLeft.showPlanetInfo(market.getPlanetEntity(), panePlanetWidth, panePlanetHeight, false, 0);

        TooltipMakerAPI conditionsViewHeader = leftPanel.createUIElement(panePlanetWidth, 0, false);
        conditionsViewHeader.addSectionHeading(this.market.getName() + " - Current Conditions", Alignment.MID, 0.0F);

        TooltipMakerAPI conditionsView = leftPanel.createUIElement(panePlanetWidth, 40, false);
        float horizontalPosition = 0;
        ArrayList<MarketConditionAPI> conditions = (ArrayList<MarketConditionAPI>) market.getConditions();
        conditions.sort(Comparator.comparing(BoggledTerraformingCoreUI::getSortOrderForCondition));
        for(MarketConditionAPI condition : conditions)
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

        TooltipMakerAPI projectsViewHeader = leftPanel.createUIElement(panePlanetWidth, 0, false);
        projectsViewHeader.addSectionHeading("Terraforming Projects", Alignment.MID, 0.0F);

        TooltipMakerAPI projectsView = leftPanel.createUIElement(panePlanetWidth, 600 - panePlanetHeight - 112, true);
        float projectHeight = 1;
        ArrayList<BoggledBaseTerraformingProject> projects = boggledTools.getTerraformingProjects(market);

        for(int i = 0; i < projects.size(); i++)
        {
            ButtonAPI projectButton = projectsView.addButton(projects.get(i).getProjectName(), (Object)null, Global.getSector().getPlayerFaction().getBaseUIColor(), Global.getSector().getPlayerFaction().getDarkUIColor(), Alignment.TL, CutStyle.ALL, panePlanetWidth - 4, 18, 0.0F);
            projectsView.addComponent(projectButton).inTL(0, projectHeight);
            buttonToProjectMap.put(projectButton, projects.get(i));
            projectHeight += 18 + 1;
        }

        leftPanel.addUIElement(planetLargeViewLeft).inTL(0, 0);

        leftPanel.addUIElement(conditionsViewHeader).inTL(0, panePlanetHeight + 18);
        leftPanel.addUIElement(conditionsView).inTL(0, panePlanetHeight + 36);

        leftPanel.addUIElement(projectsViewHeader).inTL(0, panePlanetHeight + 94);
        leftPanel.addUIElement(projectsView).inTL(0, panePlanetHeight + 112);

        this.mainPanel.addComponent(leftPanel).inTL(planetSelectPaneWidth + 3, 0);
        return leftPanel;
    }

    private CustomPanelAPI showTerraformingRightPane(MarketAPI market, BoggledBaseTerraformingProject project)
    {
        CustomPanelAPI rightPanel = this.mainPanel.createCustomPanel(panePlanetWidth, 636, null);
        TooltipMakerAPI planetLargeViewRight = rightPanel.createUIElement(panePlanetWidth, panePlanetHeight, false);
        planetLargeViewRight.addSectionHeading(this.market.getName() + " - Appearance After Project Completed", Alignment.MID, 0.0F);
        planetLargeViewRight.showPlanetInfo(project.constructFakePlanetWithAppearanceAfterTerraforming(), panePlanetWidth, panePlanetHeight, false, 0);

        TooltipMakerAPI conditionsViewHeader = rightPanel.createUIElement(panePlanetWidth, 0, false);
        conditionsViewHeader.addSectionHeading(this.market.getName() + " - Conditions After Project Completed", Alignment.MID, 0.0F);

        TooltipMakerAPI conditionsView = rightPanel.createUIElement(panePlanetWidth, 40, false);
        float horizontalPosition = 0;
        ArrayList<MarketConditionSpecAPI> conditions = new ArrayList<>();
        HashSet<String> conditionStrings = project.constructConditionsListAfterProjectCompletion();
        for(String conditionId : conditionStrings)
        {
            conditions.add(Global.getSettings().getMarketConditionSpec(conditionId));
        }
        conditions.sort(Comparator.comparing(BoggledTerraformingCoreUI::getSortOrderForCondition));
        for(MarketConditionSpecAPI condition : conditions)
        {
            if(!condition.isPlanetary() || condition.getName().equals("Population"))
            {
                continue;
            }
            String pathToImage = condition.getIcon();
            conditionsView.addImage(pathToImage,0);
            UIComponentAPI conditionImage = conditionsView.getPrev();
            conditionImage.getPosition().inTL(horizontalPosition, 0);
            horizontalPosition += 40;
        }

        TooltipMakerAPI requirementsViewHeader = rightPanel.createUIElement(panePlanetWidth, 0, false);
        requirementsViewHeader.addSectionHeading("Project Requirements", Alignment.MID, 0.0F);

        TooltipMakerAPI requirementsView = rightPanel.createUIElement(panePlanetWidth, 600 - panePlanetHeight - 112, true);
        ArrayList<BoggledBaseTerraformingProject.TerraformingRequirementObject> projectRequirements = project.getProjectRequirements();
        float labelHeight = 1;
        for(int i = 0; i < projectRequirements.size(); i++)
        {
            BoggledBaseTerraformingProject.TerraformingRequirementObject projectRequirement = projectRequirements.get(i);
            Color textColor = projectRequirement.requirementMet ? Misc.getPositiveHighlightColor() : Misc.getNegativeHighlightColor();
            LabelAPI requirementLabel = requirementsView.addPara(projectRequirement.tooltipDisplayText, textColor,1f);
            requirementLabel.getPosition().inTL(0,labelHeight);
            labelHeight += 18 + 1;

            requirementsView.addTooltipToPrevious(projectRequirement.tooltip, TooltipMakerAPI.TooltipLocation.ABOVE,false);
        }

        TooltipMakerAPI projectTriggerButtonsPanel = rightPanel.createUIElement(panePlanetWidth, 54, false);
        LabelAPI triggerButtonsLabel = null;
        String ongoingProject = getOngoingProjectAtMarket(market);
        if(ongoingProject == null)
        {
            if(project.requirementsMet(projectRequirements))
            {
                triggerButtonsLabel = projectTriggerButtonsPanel.addPara("This project will take " + project.getDaysRemaining() + " days to complete.", Misc.getTextColor(), 1f);
            }
            else
            {
                triggerButtonsLabel = projectTriggerButtonsPanel.addPara("This project cannot be started because one or more requirements are not met.", Misc.getNegativeHighlightColor(), 1f);
            }
        }
        else
        {
            if(ongoingProject.equals(project.getProjectName()))
            {
                if(project.requirementsMet(projectRequirements))
                {
                    triggerButtonsLabel = projectTriggerButtonsPanel.addPara("There are " + project.getDaysRemaining() + " day(s) remaining until this project is complete.", Misc.getTextColor(), 1f);
                }
                else
                {
                    triggerButtonsLabel = projectTriggerButtonsPanel.addPara("There are " + project.getDaysRemaining() + " day(s) remaining until this project is complete. Progress is stalled because one or more requirements are not met.", Misc.getNegativeHighlightColor(), 1f);
                }
            }
            else
            {
                triggerButtonsLabel = projectTriggerButtonsPanel.addPara("There is already an ongoing project at " + this.market.getName() + ". If you start a new project, all progress on the existing project will be lost.", Misc.getNegativeHighlightColor(), 1f);
            }
        }
        triggerButtonsLabel.getPosition().inTL(0, 0);

        ButtonAPI startProjectButton = projectTriggerButtonsPanel.addButton("Start Project", (Object)null, Global.getSector().getPlayerFaction().getBaseUIColor(), Global.getSector().getPlayerFaction().getDarkUIColor(), Alignment.TL, CutStyle.ALL, 100, 36, 0.0F);
        if(!project.requirementsMet(projectRequirements))
        {
            startProjectButton.setEnabled(false);
        }
        projectTriggerButtonsPanel.addComponent(startProjectButton).inTL(0, 18);
        this.startProjectButton = startProjectButton;

        ButtonAPI cancelProjectButton = projectTriggerButtonsPanel.addButton("Cancel Project", (Object)null, Global.getSector().getPlayerFaction().getBaseUIColor(), Global.getSector().getPlayerFaction().getDarkUIColor(), Alignment.TL, CutStyle.ALL, 100, 36, 0.0F);
        String ongoingProjectName = getOngoingProjectAtMarket(market);
        if(ongoingProjectName == null || !ongoingProjectName.equals(project.getProjectName()))
        {
            cancelProjectButton.setEnabled(false);
        }
        projectTriggerButtonsPanel.addComponent(cancelProjectButton).inTL(105, 18);
        this.cancelProjectButton = cancelProjectButton;

        rightPanel.addUIElement(planetLargeViewRight).inTL(0, 0);

        rightPanel.addUIElement(conditionsViewHeader).inTL(0, panePlanetHeight + 18);
        rightPanel.addUIElement(conditionsView).inTL(0, panePlanetHeight + 36);

        rightPanel.addUIElement(requirementsViewHeader).inTL(0, panePlanetHeight + 94);
        rightPanel.addUIElement(requirementsView).inTL(0, panePlanetHeight + 112);

        rightPanel.addUIElement(projectTriggerButtonsPanel).inTL(0, 600);

        this.mainPanel.addComponent(rightPanel).inTL(scrollPlanetWidth + panePlanetWidth + 2, 0);
        return rightPanel;
    }

    private void startNewProject(MarketAPI market, BoggledBaseTerraformingProject project)
    {
        boggledTools.addCondition(market, boggledTools.BoggledConditions.terraformingControllerConditionId);
        Terraforming_Controller terraformingController = getTerraformingControllerFromMarket(market);
        ((Terraforming_Controller) terraformingController).setCurrentProject(project);
        boggledTools.sendDebugIntelMessage("Project started!");
        this.cancelProjectButton.setEnabled(true);
        Global.getSector().addTransientScript(project);
    }

    private void cancelProject(MarketAPI market)
    {
        boggledTools.addCondition(market, boggledTools.BoggledConditions.terraformingControllerConditionId);
        Terraforming_Controller terraformingController = getTerraformingControllerFromMarket(market);
        ((Terraforming_Controller) terraformingController).setCurrentProject(null);
        boggledTools.sendDebugIntelMessage("Project cancelled!");
        this.cancelProjectButton.setEnabled(false);
    }

    private String getOngoingProjectAtMarket(MarketAPI market)
    {
        boggledTools.addCondition(market, boggledTools.BoggledConditions.terraformingControllerConditionId);
        Terraforming_Controller terraformingController = getTerraformingControllerFromMarket(market);
        BoggledBaseTerraformingProject project = ((Terraforming_Controller) terraformingController).getCurrentProject();
        return project != null ? project.getProjectName() : null;
    }

    private Terraforming_Controller getTerraformingControllerFromMarket(MarketAPI market)
    {
        return (Terraforming_Controller) market.getCondition(boggledTools.BoggledConditions.terraformingControllerConditionId).getPlugin();
    }

    private static float getSortOrderForCondition(MarketConditionAPI condition)
    {
        return -1 * Global.getSettings().getMarketConditionSpec(condition.getId()).getOrder();
    }

    private static float getSortOrderForCondition(MarketConditionSpecAPI conditionSpec)
    {
        return -1 * conditionSpec.getOrder();
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
