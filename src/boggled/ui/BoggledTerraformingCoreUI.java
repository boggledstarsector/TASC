package boggled.ui;

import boggled.campaign.econ.boggledTools;
import boggled.campaign.econ.conditions.Terraforming_Controller;
import boggled.terraforming.BoggledBaseTerraformingProject;
import boggled.terraforming.PlanetTypeChangeFrozen;
import boggled.terraforming.PlanetTypeChangeTerran;
import boggled.terraforming.PlanetTypeChangeWater;
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

    public static Color base = Global.getSector().getPlayerFaction().getBaseUIColor();
    public static Color bg = Global.getSector().getPlayerFaction().getDarkUIColor();

    public static float WIDTH = Global.getSettings().getScreenWidth();
    public static float HEIGHT = Global.getSettings().getScreenHeight() - 10.0F;

    private final float planetVisualWidth = 130;
    private final float planetVisualHeight = 130;

    private final float panePlanetVisualWidth = 400;
    private final float panePlanetVisualHeight = 400;

    private MarketAPI selectedMarket = null;
    private BoggledBaseTerraformingProject selectedProject = null;

    private List<MarketAPI> markets = new ArrayList<>();

    private HashMap<ButtonAPI, MarketAPI> buttonToMarketMap = new HashMap<>();

    private HashMap<ButtonAPI, BoggledBaseTerraformingProject> buttonToProjectMap = new HashMap<>();

    private CustomPanelAPI leftTerraformingPane = null;
    private CustomPanelAPI rightTerraformingPane = null;

    private ButtonAPI startProjectButton = null;
    private ButtonAPI cancelProjectButton = null;

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
                this.selectedProject = null;
                this.buttonToProjectMap = new HashMap<>();
                this.mainPanel.removeComponent(this.leftTerraformingPane);
                this.mainPanel.removeComponent(this.rightTerraformingPane);
                this.leftTerraformingPane = showTerraformingLeftPane(this.selectedMarket);
                return;
            }
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
                this.rightTerraformingPane = showTerraformingRightPane(this.selectedMarket, this.selectedProject);
            }
        }

        if(this.startProjectButton != null && this.startProjectButton.isChecked())
        {
            this.startProjectButton.setChecked(false);
            startNewProject(this.selectedMarket, this.selectedProject);
            return;
        }

        if(this.cancelProjectButton != null && this.cancelProjectButton.isChecked())
        {
            this.cancelProjectButton.setChecked(false);
            cancelProject(this.selectedMarket);
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

        TooltipMakerAPI projectsViewHeader = leftPanel.createUIElement(panePlanetVisualWidth, 0, false);
        projectsViewHeader.addSectionHeading("Terraforming Projects", Alignment.MID, 0.0F);

        TooltipMakerAPI projectsView = leftPanel.createUIElement(panePlanetVisualWidth, 600 - planetVisualHeight - 112, true);
        float projectHeight = 1;
        ArrayList<BoggledBaseTerraformingProject> projects = new ArrayList<>();
        projects.add(new PlanetTypeChangeWater(market));
        projects.add(new PlanetTypeChangeTerran(market));
        projects.add(new PlanetTypeChangeFrozen(market));

        for(int i = 0; i < projects.size(); i++)
        {
            ButtonAPI projectButton = projectsView.addButton(projects.get(i).getProjectName(), (Object)null, Global.getSector().getPlayerFaction().getBaseUIColor(), Global.getSector().getPlayerFaction().getDarkUIColor(), Alignment.TL, CutStyle.ALL, panePlanetVisualWidth - 4, 18, 0.0F);
            projectsView.addComponent(projectButton).inTL(0, projectHeight);
            buttonToProjectMap.put(projectButton, projects.get(i));
            projectHeight += 18 + 1;
        }

        leftPanel.addUIElement(planetLargeViewLeft).inTL(0, 0);

        leftPanel.addUIElement(conditionsViewHeader).inTL(0, panePlanetVisualHeight + 18);
        leftPanel.addUIElement(conditionsView).inTL(0, panePlanetVisualHeight + 36);

        leftPanel.addUIElement(projectsViewHeader).inTL(0, panePlanetVisualHeight + 94);
        leftPanel.addUIElement(projectsView).inTL(0, panePlanetVisualHeight + 112);

        this.mainPanel.addComponent(leftPanel).inTL(planetVisualWidth + 1, 0);
        return leftPanel;
    }

    private CustomPanelAPI showTerraformingRightPane(MarketAPI market, BoggledBaseTerraformingProject project)
    {
        CustomPanelAPI rightPanel = this.mainPanel.createCustomPanel(panePlanetVisualWidth, 636, null);
        TooltipMakerAPI planetLargeViewRight = rightPanel.createUIElement(panePlanetVisualWidth, panePlanetVisualHeight, false);
        planetLargeViewRight.addSectionHeading(this.selectedMarket.getName() + " - Appearance After Project Completed", Alignment.MID, 0.0F);
        planetLargeViewRight.showPlanetInfo(project.constructFakePlanetWithAppearanceAfterTerraforming(), panePlanetVisualWidth, panePlanetVisualHeight, false, 0);

        TooltipMakerAPI conditionsViewHeader = rightPanel.createUIElement(panePlanetVisualWidth, 0, false);
        conditionsViewHeader.addSectionHeading(this.selectedMarket.getName() + " - Conditions After Project Completed", Alignment.MID, 0.0F);

        TooltipMakerAPI conditionsView = rightPanel.createUIElement(panePlanetVisualWidth, 40, false);
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

        TooltipMakerAPI requirementsViewHeader = rightPanel.createUIElement(panePlanetVisualWidth, 0, false);
        requirementsViewHeader.addSectionHeading("Project Requirements", Alignment.MID, 0.0F);

        TooltipMakerAPI requirementsView = rightPanel.createUIElement(panePlanetVisualWidth, 600 - planetVisualHeight - 112, true);
        ArrayList<BoggledBaseTerraformingProject.TerraformingRequirementTooltipData> projectRequirements = project.getProjectRequirements();
        float labelHeight = 1;
        for(int i = 0; i < projectRequirements.size(); i++)
        {
            BoggledBaseTerraformingProject.TerraformingRequirementTooltipData projectRequirement = projectRequirements.get(i);
            Color textColor = projectRequirement.requirementMet ? Misc.getPositiveHighlightColor() : Misc.getNegativeHighlightColor();
            LabelAPI requirementLabel = requirementsView.addPara(projectRequirement.tooltipDisplayText, textColor,1f);
            requirementLabel.getPosition().inTL(0,labelHeight);
            labelHeight += 18 + 1;

            requirementsView.addTooltipToPrevious(projectRequirement.tooltip, TooltipMakerAPI.TooltipLocation.ABOVE,false);
        }

        TooltipMakerAPI projectTriggerButtonsPanel = rightPanel.createUIElement(panePlanetVisualWidth, 54, false);
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
                triggerButtonsLabel = projectTriggerButtonsPanel.addPara("There is already an ongoing project at " + this.selectedMarket.getName() + ". If you start a new project, all progress on the existing project will be lost.", Misc.getNegativeHighlightColor(), 1f);
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

        rightPanel.addUIElement(conditionsViewHeader).inTL(0, panePlanetVisualHeight + 18);
        rightPanel.addUIElement(conditionsView).inTL(0, panePlanetVisualHeight + 36);

        rightPanel.addUIElement(requirementsViewHeader).inTL(0, panePlanetVisualHeight + 94);
        rightPanel.addUIElement(requirementsView).inTL(0, panePlanetVisualHeight + 112);

        rightPanel.addUIElement(projectTriggerButtonsPanel).inTL(0, 600);

        this.mainPanel.addComponent(rightPanel).inTL(planetVisualWidth + panePlanetVisualWidth + 2, 0);
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
}
