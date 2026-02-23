package boggled.ui;

import boggled.campaign.econ.boggledTools;
import boggled.campaign.econ.conditions.Terraforming_Controller;
import boggled.terraforming.BoggledBaseTerraformingPlanetTypeChangeProject;
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

import static boggled.campaign.econ.boggledTools.getTerraformingControllerFromMarket;

public class BoggledTerraformingCoreUI implements CustomUIPanelPlugin {
    CustomPanelAPI mainPanel;
    private CustomPanelAPI leftTerraformingPane;
    private CustomPanelAPI rightTerraformingPane;
    private CustomPanelAPI planetSelectPane;

    private TooltipMakerAPI planetSelectView;

    private TooltipMakerAPI projectsView;

    private TooltipMakerAPI triggerButtonPanel;

    public static float SCREEN_WIDTH = Math.min(Global.getSettings().getScreenWidth(), 1200);
    public static float SCREEN_HEIGHT = Math.min(Global.getSettings().getScreenHeight() - 125, 1000);

    private final float planetSelectPaneWidth = 155;
    private final float scrollPlanetWidth = 130;
    private final float scrollPlanetHeight = 130;

    private final float panePlanetWidth = 400;
    private final float panePlanetHeight = 400;

    private final float paneSeparator = 3;

    private float triggerPanelVerticalPixelLocation;

    private MarketAPI market;
    private BoggledBaseTerraformingProject selectedProject;

    private final List<MarketAPI> playerMarkets = new ArrayList<>();

    private HashMap<UIComponentAPI, MarketAPI> planetVisualToMarketMap = new HashMap<>();

    private ArrayList<UIComponentAPI> planetVisualOrderedList = new ArrayList<>();

    private HashMap<ButtonAPI, MarketAPI> buttonToMarketMap = new HashMap<>();

    private HashMap<MarketAPI, ButtonAPI> marketToButtonMap = new HashMap<>();

    private HashMap<ButtonAPI, BoggledBaseTerraformingProject> buttonToProjectMap = new HashMap<>();

    private HashMap<BoggledBaseTerraformingProject, ButtonAPI> projectToButtonMap = new HashMap<>();

    private ArrayList<BoggledBaseTerraformingProject> terraformingProjectOrderedList = new ArrayList<>();

    private ButtonAPI startProjectButton;
    private ButtonAPI cancelProjectButton;

    private final Color transparent = new Color(0, 0, 0, 0);

    private final Color highlight = Global.getSector().getPlayerFaction().getBrightUIColor();

    private boolean automaticallySwitchToOngoingProject = false;

    public BoggledTerraformingCoreUI() { }

    private void populatePlayerMarkets()
    {
        List<MarketAPI> playerMarketsIncludingStations = Misc.getPlayerMarkets(true);
        for(MarketAPI market : playerMarketsIncludingStations)
        {
            if(
                    !boggledTools.marketIsStation(market)
                    && market.getPlanetEntity() != null
                    && market.getPlanetEntity().getTypeId() != null
                    && boggledTools.getPlanetSpec(market.getPlanetEntity().getTypeId()) != null
                    && market.getFaction() != null
            )
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

        if(!this.playerMarkets.isEmpty())
        {
            MarketAPI defaultMarket = market != null ? market : this.playerMarkets.get(0);
            this.mainPanel.addComponent(createPlanetSelectPane(defaultMarket)).inTL(0,0);
        }
        // Handle case where the player controls no planet-based colonies
        else
        {
            // Create panel the size of all the terraforming panels combined. Will sort of fill the entire screen.
            float noPlanetsPanelWidth = scrollPlanetWidth + (2 * panePlanetWidth);
            CustomPanelAPI noPlanetsPanel = this.mainPanel.createCustomPanel(noPlanetsPanelWidth, SCREEN_HEIGHT, null);

            // Put placeholder text in center of screen
            TooltipMakerAPI noPlanetsTooltip = noPlanetsPanel.createUIElement(noPlanetsPanelWidth, SCREEN_HEIGHT, true);
            Color textColor = Misc.getBasePlayerColor();
            LabelAPI requirementLabel = noPlanetsTooltip.addPara("Colonized planets you control will appear here", textColor,1f);
            requirementLabel.getPosition().inTL(noPlanetsPanelWidth / 2, SCREEN_HEIGHT / 2);

            noPlanetsPanel.addUIElement(noPlanetsTooltip).inTL(0, 0);
            this.mainPanel.addComponent(noPlanetsPanel).inTL(0, 0);
        }
    }

    public CustomPanelAPI getMainPanel() {
        return this.mainPanel;
    }

    public void advance(float amount)
    {
        // Handle clicks to planet select buttons
        ButtonAPI clickedPlanetSelectButton = getClickedPlanetButton();
        if(clickedPlanetSelectButton != null)
        {
            handlePlanetSelectButtonClicked(clickedPlanetSelectButton);
        }

        // Handle clicks to project buttons
        ButtonAPI clickedProjectButton = getClickedTerraformingProjectButton();
        if(clickedProjectButton != null)
        {
            handleTerraformingProjectButtonClicked(clickedProjectButton);
        }

        handleStartProjectButton();

        handleCancelProjectButton();
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

    private ButtonAPI getClickedTerraformingProjectButton()
    {
        for(ButtonAPI button : buttonToProjectMap.keySet())
        {
            if(button.isChecked())
            {
                return button;
            }
        }

        return null;
    }

    private void handleTerraformingProjectButtonClicked(ButtonAPI clickedButton)
    {
        if(this.buttonToProjectMap.get(clickedButton) != this.selectedProject)
        {
            this.selectedProject = buttonToProjectMap.get(clickedButton);
            this.mainPanel.removeComponent(this.rightTerraformingPane);
            this.rightTerraformingPane = showTerraformingRightPane(this.selectedProject);

            reloadProjectViewButtons();

            ButtonAPI selectedButton = this.projectToButtonMap.get(this.selectedProject);
            selectedButton.highlight();
        }
    }

    private void handleStartProjectButton()
    {
        if(this.startProjectButton != null && this.startProjectButton.isChecked())
        {
            startNewProject(this.market, this.selectedProject);
            this.rightTerraformingPane.removeComponent(this.triggerButtonPanel);
            TooltipMakerAPI newTriggerButtonPanel = createTriggerButtonPanel(this.rightTerraformingPane, this.selectedProject, this.selectedProject.getProjectRequirements());
            this.rightTerraformingPane.addUIElement(newTriggerButtonPanel).inTL(0, this.triggerPanelVerticalPixelLocation);
        }
    }

    private void handleCancelProjectButton()
    {
        if(this.cancelProjectButton != null && this.cancelProjectButton.isChecked())
        {
            cancelProject(this.market);
            this.rightTerraformingPane.removeComponent(this.triggerButtonPanel);
            TooltipMakerAPI newTriggerButtonPanel = createTriggerButtonPanel(this.rightTerraformingPane, this.selectedProject, this.selectedProject.getProjectRequirements());
            this.rightTerraformingPane.addUIElement(newTriggerButtonPanel).inTL(0, this.triggerPanelVerticalPixelLocation);
        }
    }

    private void reloadProjectViewButtons()
    {
        for(ButtonAPI button : this.buttonToProjectMap.keySet())
        {
            this.projectsView.removeComponent(button);
        }

        HashMap<ButtonAPI, BoggledBaseTerraformingProject> newButtonToProjectMap = new HashMap<>();
        HashMap<BoggledBaseTerraformingProject, ButtonAPI> newProjectToButtonMap = new HashMap<>();
        String ongoingProjectName = this.getOngoingProjectAtMarket(market);

        float projectHeightSpacer = 1;
        float projectHeight = projectHeightSpacer;
        for(BoggledBaseTerraformingProject project : this.terraformingProjectOrderedList)
        {
            ButtonAPI projectButton = projectsView.addButton(project.getProjectName(), (Object)null, Global.getSector().getPlayerFaction().getBaseUIColor(), Global.getSector().getPlayerFaction().getDarkUIColor(), Alignment.TL, CutStyle.ALL, panePlanetWidth - 4, 18, 0.0F);
            projectsView.addComponent(projectButton).inTL(0, projectHeight);
            newButtonToProjectMap.put(projectButton, project);
            newProjectToButtonMap.put(project, projectButton);
            projectHeight += 18 + projectHeightSpacer;

            // Only display tooltip if it's a planet type change project (for now, maybe other projects will have tooltips in the future)
            if(project instanceof BoggledBaseTerraformingPlanetTypeChangeProject)
            {
                projectsView.addTooltipToPrevious(((BoggledBaseTerraformingPlanetTypeChangeProject) project).getProjectTooltip(), TooltipMakerAPI.TooltipLocation.ABOVE,false);
            }

            // By default, select and display the right pane for the ongoing project, if there is one
            if(this.automaticallySwitchToOngoingProject && ongoingProjectName != null && project.getProjectName().equals(ongoingProjectName))
            {
                projectButton.setChecked(true);
                this.automaticallySwitchToOngoingProject = false;
            }
            else
            {
                projectButton.setChecked(false);
            }
        }

        this.buttonToProjectMap = newButtonToProjectMap;
        this.projectToButtonMap = newProjectToButtonMap;
    }

    private CustomPanelAPI createProjectsPanel(CustomPanelAPI leftTerraformingPane, float height)
    {
        CustomPanelAPI projectsPanel = leftTerraformingPane.createCustomPanel(panePlanetWidth, height, null);

        // Section header
        TooltipMakerAPI projectsViewHeader = projectsPanel.createUIElement(panePlanetWidth, 0, false);
        projectsViewHeader.addSectionHeading("Terraforming Projects", Alignment.MID, 0.0F);

        TooltipMakerAPI projectsView = projectsPanel.createUIElement(panePlanetWidth, height - 18, true);
        this.projectsView = projectsView;

        // Handle logic like hiding projects for mods that aren't enabled in this boggledTools method
        // Also handle sorting logic in there.
        this.terraformingProjectOrderedList = boggledTools.getTerraformingProjects(market);

        reloadProjectViewButtons();

        projectsPanel.addUIElement(projectsViewHeader).inTL(0, 0);
        projectsPanel.addUIElement(projectsView).inTL(0, 18);
        return projectsPanel;
    }

    private CustomPanelAPI showTerraformingLeftPane(MarketAPI market)
    {
        // Set the boolean to automatically check the ongoing project.
        // After switching this will get set back to false. Ensures we only switch to the ongoing project once per left pane load.
        // Without this check, everytime the player clicks a different project, it will switch back to the ongoing one.
        // Works because the left pane only gets loaded once when the player initially selects a planet.
        if(this.getOngoingProjectAtMarket(market) != null)
        {
            this.automaticallySwitchToOngoingProject = true;
        }

        ArrayList<MarketConditionAPI> conditions = (ArrayList<MarketConditionAPI>) market.getConditions();
        conditions.sort(Comparator.comparing(BoggledTerraformingCoreUI::getSortOrderForCondition));

        CustomPanelAPI leftPanel = this.mainPanel.createCustomPanel(panePlanetWidth, SCREEN_HEIGHT, null);

        // Create big left side current planet appearance view
        TooltipMakerAPI planetLargeViewLeft = leftPanel.createUIElement(panePlanetWidth, panePlanetHeight, false);
        planetLargeViewLeft.addSectionHeading(this.market.getName() + " - Current Appearance", Alignment.MID, 0.0F);
        planetLargeViewLeft.showPlanetInfo(market.getPlanetEntity(), panePlanetWidth, panePlanetHeight, false, 0);

        // Conditions view
        TooltipMakerAPI conditionsViewHeader = leftPanel.createUIElement(panePlanetWidth, 0, false);
        conditionsViewHeader.addSectionHeading(this.market.getName() + " - Current Conditions", Alignment.MID, 0.0F);

        ArrayList<MarketConditionAPI> conditionsTemp = new ArrayList<>();
        for(MarketConditionAPI condition : conditions)
        {
            //Hardcoded to always add
            if(condition.getId().equals("US_hybrid"))
            {
                conditionsTemp.add(condition);
            }

            if(condition.isPlanetary() && condition.getPlugin().showIcon() && !condition.getName().equals("Population"))
            {
                conditionsTemp.add(condition);
            }
        }
        conditions = conditionsTemp;

        // We can fit 10 conditions per line. Create a second line if we have more than 10.
        int numConditionsRows = (conditions.size() + 9) / 10;
        float conditionHeight = numConditionsRows * 40;
        TooltipMakerAPI conditionsView = leftPanel.createUIElement(panePlanetWidth, conditionHeight, false);
        float horizontalPosition = 0;
        float verticalPosition = 0;

        for(MarketConditionAPI condition : conditions)
        {
            String pathToImage = Global.getSettings().getMarketConditionSpec(condition.getId()).getIcon();
            conditionsView.addImage(pathToImage,0);
            UIComponentAPI conditionImage = conditionsView.getPrev();
            conditionImage.getPosition().inTL(horizontalPosition, verticalPosition);
            horizontalPosition += 40;
            if(horizontalPosition >= 400)
            {
                horizontalPosition = 0;
                verticalPosition += 40;
            }
        }

        // Get the custom panel for the list of projects
        CustomPanelAPI projectsPanel = createProjectsPanel(leftPanel, SCREEN_HEIGHT + 18 - (18 + panePlanetHeight + 18 + conditionHeight + 18));

        // Height is panePlanetHeight + 18 because of section header
        leftPanel.addUIElement(planetLargeViewLeft).inTL(0, 0);

        leftPanel.addUIElement(conditionsViewHeader).inTL(0, panePlanetHeight + 18);
        leftPanel.addUIElement(conditionsView).inTL(0, panePlanetHeight + 36);

        leftPanel.addComponent(projectsPanel).inTL(0, panePlanetHeight + conditionHeight + 54);

        this.mainPanel.addComponent(leftPanel).inTL(planetSelectPaneWidth + paneSeparator, 0);
        return leftPanel;
    }

    private CustomPanelAPI showTerraformingRightPane(BoggledBaseTerraformingProject project)
    {
        CustomPanelAPI rightPanel = this.mainPanel.createCustomPanel(panePlanetWidth, SCREEN_HEIGHT, null);

        // Get requirements for the selected project
        ArrayList<BoggledBaseTerraformingProject.TerraformingRequirementObject> projectRequirements = project.getProjectRequirements();

        // Create the trigger button panel
        TooltipMakerAPI projectTriggerButtonsPanel = createTriggerButtonPanel(rightPanel, project, projectRequirements);

        // Get sorted conditions list for what they would be after the project is completed
        ArrayList<MarketConditionSpecAPI> conditions = new ArrayList<>();
        HashSet<String> conditionStrings = project.constructConditionsListAfterProjectCompletion();
        for(String conditionId : conditionStrings)
        {
            conditions.add(Global.getSettings().getMarketConditionSpec(conditionId));
        }
        conditions.sort(Comparator.comparing(BoggledTerraformingCoreUI::getSortOrderForCondition));

        //Filter out invalid conditions
        ArrayList<MarketConditionSpecAPI> conditionsTemp = new ArrayList<>();
        for(MarketConditionSpecAPI condition : conditions)
        {
            //Hardcoded to always add
            if(condition.getId().equals("US_hybrid"))
            {
                conditionsTemp.add(condition);
            }

            if(condition.isPlanetary() && !condition.getName().equals("Population") && Global.getSettings().getMarketConditionSpec(condition.getId()).getIcon() != null)
            {
                conditionsTemp.add(condition);
            }
        }
        conditions = conditionsTemp;

        // We can fit 10 conditions per line. Create a second line if we have more than 10.
        int numConditionsRows = (conditions.size() + 9) / 10;
        float conditionHeight = numConditionsRows * 40;

        // Get the height of the requirements panel.
        float spaceRemainingForRequirementsPanel = SCREEN_HEIGHT - (panePlanetHeight + conditionHeight + 72 + 72);

        // Create planet view after project is complete.
        TooltipMakerAPI planetLargeViewRight = rightPanel.createUIElement(panePlanetWidth, panePlanetHeight, false);
        planetLargeViewRight.addSectionHeading(this.market.getName() + " - Appearance After Project Completed", Alignment.MID, 0.0F);
        planetLargeViewRight.showPlanetInfo(project.constructFakePlanetWithAppearanceAfterTerraforming(), panePlanetWidth, panePlanetHeight, false, 0);

        // Conditions view header
        TooltipMakerAPI conditionsViewHeader = rightPanel.createUIElement(panePlanetWidth, 0, false);
        conditionsViewHeader.addSectionHeading(this.market.getName() + " - Conditions After Project Completed", Alignment.MID, 0.0F);

        // Conditions view panel
        TooltipMakerAPI conditionsView = rightPanel.createUIElement(panePlanetWidth, conditionHeight, false);
        float horizontalPosition = 0;
        float verticalPosition = 0;
        for(MarketConditionSpecAPI condition : conditions)
        {
            String pathToImage = Global.getSettings().getMarketConditionSpec(condition.getId()).getIcon();
            conditionsView.addImage(pathToImage,0);
            UIComponentAPI conditionImage = conditionsView.getPrev();
            conditionImage.getPosition().inTL(horizontalPosition, verticalPosition);
            horizontalPosition += 40;
            if(horizontalPosition >= 400)
            {
                horizontalPosition = 0;
                verticalPosition += 40;
            }
        }

        // Projects requirements view
        TooltipMakerAPI requirementsViewHeader = rightPanel.createUIElement(panePlanetWidth, 0, false);
        requirementsViewHeader.addSectionHeading("Project Requirements", Alignment.MID, 0.0F);

        TooltipMakerAPI requirementsView = rightPanel.createUIElement(panePlanetWidth, spaceRemainingForRequirementsPanel, true);

        float labelSpacerHeight = 1;
        float labelHeight = labelSpacerHeight;
        for(BoggledBaseTerraformingProject.TerraformingRequirementObject requirement : projectRequirements)
        {
            Color textColor = requirement.requirementMet ? Misc.getPositiveHighlightColor() : Misc.getNegativeHighlightColor();
            LabelAPI requirementLabel = requirementsView.addPara(requirement.tooltipDisplayText, textColor,1f);
            requirementLabel.getPosition().inTL(0,labelHeight);
            labelHeight += 18 + labelSpacerHeight;

            if(requirement.tooltip != null)
            {
                requirementsView.addTooltipToPrevious(requirement.tooltip, TooltipMakerAPI.TooltipLocation.ABOVE,false);
            }
        }

        rightPanel.addUIElement(planetLargeViewRight).inTL(0, 0);

        rightPanel.addUIElement(conditionsViewHeader).inTL(0, panePlanetHeight + 18);
        rightPanel.addUIElement(conditionsView).inTL(0, panePlanetHeight + 36);

        rightPanel.addUIElement(requirementsViewHeader).inTL(0, panePlanetHeight + conditionHeight + 54);
        rightPanel.addUIElement(requirementsView).inTL(0, panePlanetHeight + conditionHeight + 72);

        this.triggerPanelVerticalPixelLocation = panePlanetHeight + conditionHeight + 72 + spaceRemainingForRequirementsPanel + 18;
        rightPanel.addUIElement(projectTriggerButtonsPanel).inTL(0, this.triggerPanelVerticalPixelLocation);

        this.mainPanel.addComponent(rightPanel).inTL(planetSelectPaneWidth + paneSeparator + panePlanetWidth + paneSeparator, 0);
        return rightPanel;
    }

    private TooltipMakerAPI createTriggerButtonPanel(CustomPanelAPI rightTerraformingPane, BoggledBaseTerraformingProject project, ArrayList<BoggledBaseTerraformingProject.TerraformingRequirementObject> projectRequirements)
    {
        // Start project and cancel project
        float triggerButtonLabelHeight = getTriggerButtonLabelHeight(project, projectRequirements);
        TooltipMakerAPI projectTriggerButtonsPanel = rightTerraformingPane.createUIElement(panePlanetWidth, 72, false);
        LabelAPI triggerButtonsLabel;
        BoggledBaseTerraformingProject ongoingProject = getOngoingBoggledBaseTerraformingProjectAtMarket(this.market);
        if(ongoingProject == null)
        {
            if(project.requirementsMet(projectRequirements))
            {
                triggerButtonsLabel = projectTriggerButtonsPanel.addPara("This project will take " + project.getDaysRemaining() + " days to complete.", Misc.getPositiveHighlightColor(), 1f);
            }
            else
            {
                triggerButtonsLabel = projectTriggerButtonsPanel.addPara("This project cannot be started because one or more requirements are not met.", Misc.getNegativeHighlightColor(), 1f);
            }
        }
        else
        {
            if(ongoingProject.getProjectName().equals(project.getProjectName()))
            {
                if(project.requirementsMet(projectRequirements))
                {
                    triggerButtonsLabel = projectTriggerButtonsPanel.addPara("There are " + ongoingProject.getDaysRemaining() + " day(s) remaining until this project is complete.", Misc.getPositiveHighlightColor(), 1f);
                }
                else
                {
                    triggerButtonsLabel = projectTriggerButtonsPanel.addPara("There are " + ongoingProject.getDaysRemaining() + " day(s) remaining until this project is complete. Progress is stalled because requirements are not met.", Misc.getNegativeHighlightColor(), 1f);
                }
            }
            else
            {
                triggerButtonsLabel = projectTriggerButtonsPanel.addPara("There is already an ongoing project here. If you start a new project, all progress on the existing project will be lost.", Misc.getNegativeHighlightColor(), 1f);
            }
        }
        triggerButtonsLabel.getPosition().inTL(0, triggerButtonLabelHeight == 36 ? 0 : 18);

        String ongoingProjectName = getOngoingProjectAtMarket(market);
        ButtonAPI startProjectButton = projectTriggerButtonsPanel.addButton("Start Project", null, Global.getSector().getPlayerFaction().getBaseUIColor(), Global.getSector().getPlayerFaction().getDarkUIColor(), Alignment.LMID, CutStyle.ALL, 150, 36, 0.0F);
        if((ongoingProjectName != null && ongoingProjectName.equals(project.getProjectName())) || !project.requirementsMet(projectRequirements))
        {
            startProjectButton.setEnabled(false);
        }
        projectTriggerButtonsPanel.addComponent(startProjectButton).inTL(0, 36);
        this.startProjectButton = startProjectButton;

        ButtonAPI cancelProjectButton = projectTriggerButtonsPanel.addButton("Cancel Project", null, Global.getSector().getPlayerFaction().getBaseUIColor(), Global.getSector().getPlayerFaction().getDarkUIColor(), Alignment.LMID, CutStyle.ALL, 150, 36, 0.0F);

        if(ongoingProjectName == null || !ongoingProjectName.equals(project.getProjectName()))
        {
            cancelProjectButton.setEnabled(false);
        }
        projectTriggerButtonsPanel.addComponent(cancelProjectButton).inTL(155, 36);
        this.cancelProjectButton = cancelProjectButton;
        this.triggerButtonPanel = projectTriggerButtonsPanel;
        return projectTriggerButtonsPanel;
    }

    private int getTriggerButtonLabelHeight(BoggledBaseTerraformingProject project, ArrayList<BoggledBaseTerraformingProject.TerraformingRequirementObject> projectRequirements)
    {
        String ongoingProject = getOngoingProjectAtMarket(market);
        if(ongoingProject == null)
        {
            if(project.requirementsMet(projectRequirements))
            {
                return 18;
            }
            else
            {
                return 36;
            }
        }
        else
        {
            if(ongoingProject.equals(project.getProjectName()))
            {
                if(project.requirementsMet(projectRequirements))
                {
                    return 18;
                }
                else
                {
                    return 36;
                }
            }
            else
            {
                return 36;
            }
        }
    }

    private void startNewProject(MarketAPI market, BoggledBaseTerraformingProject project)
    {
        project.startThisProject();
    }

    private void cancelProject(MarketAPI market)
    {
        Terraforming_Controller terraformingController = getTerraformingControllerFromMarket(market);
        BoggledBaseTerraformingProject currentProject = terraformingController.getCurrentProject();
        if(currentProject != null)
        {
            currentProject.cancelThisProject();
        }
    }

    private String getOngoingProjectAtMarket(MarketAPI market)
    {
        boggledTools.addCondition(market, boggledTools.BoggledConditions.terraformingControllerConditionId);
        Terraforming_Controller terraformingController = getTerraformingControllerFromMarket(market);
        BoggledBaseTerraformingProject project = ((Terraforming_Controller) terraformingController).getCurrentProject();
        return project != null ? project.getProjectName() : null;
    }

    private BoggledBaseTerraformingProject getOngoingBoggledBaseTerraformingProjectAtMarket(MarketAPI market)
    {
        boggledTools.addCondition(market, boggledTools.BoggledConditions.terraformingControllerConditionId);
        Terraforming_Controller terraformingController = getTerraformingControllerFromMarket(market);
        return terraformingController.getCurrentProject();
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
