package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import data.campaign.econ.boggledTools;
import data.campaign.econ.conditions.Terraforming_Controller;

import java.awt.*;
import java.util.*;

import static java.util.Arrays.asList;

public class boggledTerraformingDialogPlugin implements InteractionDialogPlugin
{
    protected InteractionDialogAPI dialog;

    private int pageNumber;

    private static final String aridTypeChangeYes = "boggledAridTypeChangeYes";
    private static final String triggerAridTypeChange = "boggledTriggerAridTypeChange";

    private static final String frozenTypeChangeYes = "boggledFrozenTypeChangeYes";
    private static final String triggerFrozenTypeChange = "boggledTriggerFrozenTypeChange";

    private static final String jungleTypeChangeYes = "boggledJungleTypeChangeYes";
    private static final String triggerJungleTypeChange = "boggledTriggerJungleTypeChange";

    private static final String terranTypeChangeYes = "boggledTerranTypeChangeYes";
    private static final String triggerTerranTypeChange = "boggledTriggerTerranTypeChange";

    private static final String tundraTypeChangeYes = "boggledTundraTypeChangeYes";
    private static final String triggerTundraTypeChange = "boggledTriggerTundraTypeChange";

    private static final String waterTypeChangeYes = "boggledWaterTypeChangeYes";
    private static final String triggerWaterTypeChange = "boggledTriggerWaterTypeChange";

    private static final String farmlandResourceImprovementYes = "boggledFarmlandResourceImprovementYes";
    private static final String triggerFarmlandResourceImprovement = "boggledTriggerFarmlandResourceImprovement";

    private static final String organicsResourceImprovementYes = "boggledOrganicsResourceImprovementYes";
    private static final String triggerOrganicsResourceImprovement = "boggledTriggerOrganicsResourceImprovement";

    private static final String volatilesResourceImprovementYes = "boggledVolatilesResourceImprovementYes";
    private static final String triggerVolatilesResourceImprovement = "boggledTriggerVolatilesResourceImprovement";

    private static final String extremeWeatherConditionImprovementYes = "boggledExtremeWeatherConditionImprovementYes";
    private static final String triggerExtremeWeatherConditionImprovement = "boggledTriggerExtremeWeatherConditionImprovement";

    private static final String mildClimateConditionImprovementYes = "boggledMildClimateConditionImprovementYes";
    private static final String triggerMildClimateConditionImprovement = "boggledTriggerMildClimateConditionImprovement";

    private static final String habitableConditionImprovementYes = "boggledHabitableConditionImprovementYes";
    private static final String triggerHabitableConditionImprovement = "boggledTriggerHabitableConditionImprovement";

    private static final String atmosphereDensityConditionImprovementYes = "boggledAtmosphereDensityConditionImprovementYes";
    private static final String triggerAtmosphereDensityConditionImprovement = "boggledTriggerAtmosphereDensityConditionImprovement";

    private static final String toxicAtmosphereConditionImprovementYes = "boggledToxicAtmosphereConditionImprovementYes";
    private static final String triggerToxicAtmosphereConditionImprovement = "boggledTriggerToxicAtmosphereConditionImprovement";

    private static final String irradiatedConditionImprovementYes = "boggledIrradiatedConditionImprovementYes";
    private static final String triggerIrradiatedConditionImprovement = "boggledTriggerIrradiatedConditionImprovement";

    private static final String removeAtmosphereConditionImprovementYes = "boggledRemoveAtmosphereConditionImprovementYes";
    private static final String triggerRemoveAtmosphereConditionImprovement = "boggledTriggerRemoveAtmosphereConditionImprovement";

    private static final String triggerCancelCurrentProject = "boggledTriggerCancelCurrentProject";

    private HashMap<OptionId, String> initialiseOptionIdToProjectYesDialogue() {
        HashMap<OptionId, String> ret = new HashMap<>();

        ret.put(OptionId.ARID_TYPE_CHANGE, aridTypeChangeYes);
        ret.put(OptionId.FROZEN_TYPE_CHANGE, frozenTypeChangeYes);
        ret.put(OptionId.JUNGLE_TYPE_CHANGE, jungleTypeChangeYes);
        ret.put(OptionId.TERRAN_TYPE_CHANGE, terranTypeChangeYes);
        ret.put(OptionId.TUNDRA_TYPE_CHANGE, tundraTypeChangeYes);
        ret.put(OptionId.WATER_TYPE_CHANGE, waterTypeChangeYes);

        ret.put(OptionId.FARMLAND_IMPROVEMENT, farmlandResourceImprovementYes);
        ret.put(OptionId.ORGANICS_IMPROVEMENT, organicsResourceImprovementYes);
        ret.put(OptionId.VOLATILES_IMPROVEMENT, volatilesResourceImprovementYes);

        ret.put(OptionId.EXTREME_WEATHER_IMPROVEMENT, extremeWeatherConditionImprovementYes);
        ret.put(OptionId.MILD_CLIMATE_IMPROVEMENT, mildClimateConditionImprovementYes);
        ret.put(OptionId.HABITABLE_IMPROVEMENT, habitableConditionImprovementYes);
        ret.put(OptionId.ATMOSPHERE_DENSITY_IMPROVEMENT, atmosphereDensityConditionImprovementYes);
        ret.put(OptionId.TOXIC_ATMOSPHERE_IMPROVEMENT, toxicAtmosphereConditionImprovementYes);
        ret.put(OptionId.IRRADIATED_IMPROVEMENT, irradiatedConditionImprovementYes);
        ret.put(OptionId.REMOVE_ATMOSPHERE_IMPROVEMENT, removeAtmosphereConditionImprovementYes);

        return ret;
    }

    private HashMap<OptionId, String> initialiseOptionIdToStartProjectDialogue() {
        HashMap<OptionId, String> ret = new HashMap<>();

        ret.put(OptionId.START_ARID_TYPE_CHANGE_PROJECT, triggerAridTypeChange);
        ret.put(OptionId.START_FROZEN_TYPE_CHANGE_PROJECT, triggerFrozenTypeChange);
        ret.put(OptionId.START_JUNGLE_TYPE_CHANGE_PROJECT, triggerJungleTypeChange);
        ret.put(OptionId.START_TERRAN_TYPE_CHANGE_PROJECT, triggerTerranTypeChange);
        ret.put(OptionId.START_TUNDRA_TYPE_CHANGE_PROJECT, triggerTundraTypeChange);
        ret.put(OptionId.START_WATER_TYPE_CHANGE_PROJECT, triggerWaterTypeChange);

        ret.put(OptionId.START_FARMLAND_IMPROVEMENT, triggerFarmlandResourceImprovement);
        ret.put(OptionId.START_ORGANICS_IMPROVEMENT, triggerOrganicsResourceImprovement);
        ret.put(OptionId.START_VOLATILES_IMPROVEMENT, triggerVolatilesResourceImprovement);

        ret.put(OptionId.START_EXTREME_WEATHER_IMPROVEMENT, triggerExtremeWeatherConditionImprovement);
        ret.put(OptionId.START_MILD_CLIMATE_IMPROVEMENT, triggerMildClimateConditionImprovement);
        ret.put(OptionId.START_HABITABLE_IMPROVEMENT, triggerHabitableConditionImprovement);
        ret.put(OptionId.START_ATMOSPHERE_DENSITY_IMPROVEMENT, triggerAtmosphereDensityConditionImprovement);
        ret.put(OptionId.START_TOXIC_ATMOSPHERE_IMPROVEMENT, triggerToxicAtmosphereConditionImprovement);
        ret.put(OptionId.START_IRRADIATED_IMPROVEMENT, triggerIrradiatedConditionImprovement);
        ret.put(OptionId.START_REMOVE_ATMOSPHERE, triggerRemoveAtmosphereConditionImprovement);

        ret.put(OptionId.START_CANCEL_PROJECT, triggerCancelCurrentProject);

        return ret;
    }

    private HashMap<OptionId, Pair<OptionId, OptionId>> initialiseProjectOptionIdToStartProjectOptionId() {
        HashMap<OptionId, Pair<OptionId, OptionId>> ret = new HashMap<>();

        ret.put(OptionId.ARID_TYPE_CHANGE, new Pair<>(OptionId.START_ARID_TYPE_CHANGE_PROJECT, OptionId.TYPE_CHANGE_OPTIONS));
        ret.put(OptionId.FROZEN_TYPE_CHANGE, new Pair<>(OptionId.START_FROZEN_TYPE_CHANGE_PROJECT, OptionId.TYPE_CHANGE_OPTIONS));
        ret.put(OptionId.JUNGLE_TYPE_CHANGE, new Pair<>(OptionId.START_JUNGLE_TYPE_CHANGE_PROJECT, OptionId.TYPE_CHANGE_OPTIONS));
        ret.put(OptionId.TERRAN_TYPE_CHANGE, new Pair<>(OptionId.START_TERRAN_TYPE_CHANGE_PROJECT, OptionId.TYPE_CHANGE_OPTIONS));
        ret.put(OptionId.TUNDRA_TYPE_CHANGE, new Pair<>(OptionId.START_TUNDRA_TYPE_CHANGE_PROJECT, OptionId.TYPE_CHANGE_OPTIONS));
        ret.put(OptionId.WATER_TYPE_CHANGE, new Pair<>(OptionId.START_WATER_TYPE_CHANGE_PROJECT, OptionId.TYPE_CHANGE_OPTIONS));

        ret.put(OptionId.FARMLAND_IMPROVEMENT, new Pair<>(OptionId.START_FARMLAND_IMPROVEMENT, OptionId.RESOURCE_IMPROVEMENTS));
        ret.put(OptionId.ORGANICS_IMPROVEMENT, new Pair<>(OptionId.START_ORGANICS_IMPROVEMENT, OptionId.RESOURCE_IMPROVEMENTS));
        ret.put(OptionId.VOLATILES_IMPROVEMENT, new Pair<>(OptionId.START_VOLATILES_IMPROVEMENT, OptionId.RESOURCE_IMPROVEMENTS));

        ret.put(OptionId.EXTREME_WEATHER_IMPROVEMENT, new Pair<>(OptionId.START_EXTREME_WEATHER_IMPROVEMENT, OptionId.CONDITION_IMPROVEMENTS));
        ret.put(OptionId.MILD_CLIMATE_IMPROVEMENT, new Pair<>(OptionId.START_MILD_CLIMATE_IMPROVEMENT, OptionId.CONDITION_IMPROVEMENTS));
        ret.put(OptionId.HABITABLE_IMPROVEMENT, new Pair<>(OptionId.START_HABITABLE_IMPROVEMENT, OptionId.CONDITION_IMPROVEMENTS));
        ret.put(OptionId.ATMOSPHERE_DENSITY_IMPROVEMENT, new Pair<>(OptionId.START_ATMOSPHERE_DENSITY_IMPROVEMENT, OptionId.CONDITION_IMPROVEMENTS));
        ret.put(OptionId.TOXIC_ATMOSPHERE_IMPROVEMENT, new Pair<>(OptionId.START_TOXIC_ATMOSPHERE_IMPROVEMENT, OptionId.CONDITION_IMPROVEMENTS));
        ret.put(OptionId.IRRADIATED_IMPROVEMENT, new Pair<>(OptionId.START_IRRADIATED_IMPROVEMENT, OptionId.CONDITION_IMPROVEMENTS));
        ret.put(OptionId.REMOVE_ATMOSPHERE_IMPROVEMENT, new Pair<>(OptionId.START_REMOVE_ATMOSPHERE, OptionId.CONDITION_IMPROVEMENTS));

        return ret;
    }

    private HashMap<OptionId, ArrayList<Pair<String, OptionId>>> initialiseTerraformingOptions() {
        HashMap<OptionId, ArrayList<Pair<String, OptionId>>> ret = new HashMap<>();

        ArrayList<Pair<String, OptionId>> typeChangeOptions = new ArrayList<>(asList(
                new Pair<>("Consider terraforming $planet into an arid world", OptionId.ARID_TYPE_CHANGE),
                new Pair<>("Consider terraforming $planet into a frozen world", OptionId.FROZEN_TYPE_CHANGE),
                new Pair<>("Consider terraforming $planet into a jungle world", OptionId.JUNGLE_TYPE_CHANGE),
                new Pair<>("Consider terraforming $planet into a Terran world", OptionId.TERRAN_TYPE_CHANGE),
                new Pair<>("consider terraforming $planet into a tundra world", OptionId.TUNDRA_TYPE_CHANGE),
                new Pair<>("Consider terraforming $planet into a water world", OptionId.WATER_TYPE_CHANGE)
        ));

        ArrayList<Pair<String, OptionId>> resourceImprovementOptions = new ArrayList<>(asList(
                new Pair<>("Consider improving the farmland on $planet", OptionId.FARMLAND_IMPROVEMENT),
                new Pair<>("Consider improving the organics on $planet", OptionId.ORGANICS_IMPROVEMENT),
                new Pair<>("Consider improving the volatiles on $planet", OptionId.VOLATILES_IMPROVEMENT)
        ));

        ArrayList<Pair<String, OptionId>> conditionImprovementOptions = new ArrayList<>(asList(
                new Pair<>("Consider making the weather patterns on $planet less extreme", OptionId.EXTREME_WEATHER_IMPROVEMENT),
                new Pair<>("Consider making the weather patterns on $planet mild", OptionId.MILD_CLIMATE_IMPROVEMENT),
                new Pair<>("Consider making the atmosphere on $planet human-breathable", OptionId.HABITABLE_IMPROVEMENT),
                new Pair<>("Consider normalizing atmospheric density on $planet", OptionId.ATMOSPHERE_DENSITY_IMPROVEMENT),
                new Pair<>("Consider remediating atmospheric toxicity on $planet", OptionId.TOXIC_ATMOSPHERE_IMPROVEMENT)
        ));
        if (boggledTools.getBooleanSetting("boggledTerraformingRemoveRadiationProjectEnabled"))
        {
            conditionImprovementOptions.add(new Pair<>("Consider remediating radiation on $planet", OptionId.IRRADIATED_IMPROVEMENT));
        }
        if (boggledTools.getBooleanSetting("boggledTerraformingRemoveAtmosphereProjectEnabled"))
        {
            conditionImprovementOptions.add(new Pair<>("Consider removing the atmosphere on $planet", OptionId.REMOVE_ATMOSPHERE_IMPROVEMENT));
        }

        ret.put(OptionId.TYPE_CHANGE_OPTIONS, typeChangeOptions);
        ret.put(OptionId.RESOURCE_IMPROVEMENTS, resourceImprovementOptions);
        ret.put(OptionId.CONDITION_IMPROVEMENTS, conditionImprovementOptions);

        return ret;
    }

    private final HashMap<OptionId, String> projectOptionIdToProjectYesDialogue = initialiseOptionIdToProjectYesDialogue();
    private final HashMap<OptionId, String> startProjectOptionIdToStartProjectDialogue = initialiseOptionIdToStartProjectDialogue();
    private final HashMap<OptionId, Pair<OptionId, OptionId>> projectOptionIdToStartProjectOptionId = initialiseProjectOptionIdToStartProjectOptionId();

    private final HashMap<OptionId, ArrayList<Pair<String, OptionId>>> terraformingOptions = initialiseTerraformingOptions();

    @Override
    public void init(InteractionDialogAPI dialog)
    {
        this.dialog = dialog;
        this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
    }

    private OptionId getOptionIdForInt(int i, boolean all)
    {
        if(all)
        {
            if(i == 1)
            {
                return OptionId.COLONY_1_ALL;
            }
            else if(i == 2)
            {
                return OptionId.COLONY_2_ALL;
            }
            else if(i == 3)
            {
                return OptionId.COLONY_3_ALL;
            }
            else if(i == 4)
            {
                return OptionId.COLONY_4_ALL;
            }
            else if(i == 5)
            {
                return OptionId.COLONY_5_ALL;
            }
            else if(i == 6)
            {
                return OptionId.COLONY_6_ALL;
            }
            else if(i == 7)
            {
                return OptionId.COLONY_7_ALL;
            }
            else if(i == 8)
            {
                return OptionId.COLONY_8_ALL;
            }
            else
            {
                return null;
            }
        }
        else
        {
            if(i == 1)
            {
                return OptionId.COLONY_1;
            }
            else if(i == 2)
            {
                return OptionId.COLONY_2;
            }
            else if(i == 3)
            {
                return OptionId.COLONY_3;
            }
            else if(i == 4)
            {
                return OptionId.COLONY_4;
            }
            else if(i == 5)
            {
                return OptionId.COLONY_5;
            }
            else if(i == 6)
            {
                return OptionId.COLONY_6;
            }
            else if(i == 7)
            {
                return OptionId.COLONY_7;
            }
            else if(i == 8)
            {
                return OptionId.COLONY_8;
            }
            else
            {
                return null;
            }
        }
    }

    private MarketAPI getMarketBasedOnNameString(String marketName)
    {
        ArrayList<MarketAPI> allNonStationPlayerMarkets = boggledTools.getNonStationMarketsPlayerControls();
        for(MarketAPI market : allNonStationPlayerMarkets)
        {
            if(market.getName().equals(marketName))
            {
                return market;
            }
        }

        return null;
    }

    private void showColonySelectedOptionsAndLoadVisual(String optionText, boolean all, boolean printStatus)
    {
        // Set interaction target
        MarketAPI market = getMarketBasedOnNameString(optionText);
        if(market == null)
        {
            if(dialog == null)
            {
                boggledTools.writeMessageToLog("Option text: " + optionText + " - dialog was null");
            }
            else if(dialog.getInteractionTarget() == null)
            {
                boggledTools.writeMessageToLog("Option text: " + optionText + " - interaction target was null");
            }
            else if(dialog.getInteractionTarget().getMarket() == null)
            {
                boggledTools.writeMessageToLog("Option text: " + optionText + " - get market was null");
            }
            market = dialog.getInteractionTarget().getMarket();
        }
        dialog.setInteractionTarget(market.getPlanetEntity());
        Terraforming_Controller controller = (Terraforming_Controller) market.getCondition("terraforming_controller").getPlugin();

        if(printStatus)
        {
            // Print planet name to dialog window
            Color playerColor = Misc.getBasePlayerColor();
            dialog.getTextPanel().addPara("Colony: %s", playerColor, new String[]{market.getName()});

            // Print status of current project to dialog window
            boggledTerraformingPrintStatus printStatusCmd = new boggledTerraformingPrintStatus();
            printStatusCmd.execute(null, this.dialog, null, null);
        }

        // Display an image of the planet
        dialog.getVisualPanel().showLargePlanet(market.getPlanetEntity());

        dialog.getOptionPanel().addOption("Planet type change options", OptionId.TYPE_CHANGE_OPTIONS);
        /*
        // Waiting on US 0.96a update
        if(Global.getSettings().getModManager().isModEnabled("US"))
        {
            dialog.getOptionPanel().addOption("Unknown Skies planet type change options", OptionId.US_TYPE_CHANGE_OPTIONS);
        }
         */
        dialog.getOptionPanel().addOption("Resource deposit improvement options", OptionId.RESOURCE_IMPROVEMENTS);
        dialog.getOptionPanel().addOption("Atmospheric conditions improvement options", OptionId.CONDITION_IMPROVEMENTS);

        if(all)
        {
            dialog.getOptionPanel().addOption("List current colony conditions", OptionId.VIEW_COLONY_ALL);
            dialog.getOptionPanel().addOption("Cancel current project", OptionId.CANCEL_PROJECT);
            dialog.getOptionPanel().addOption("Back", OptionId.ALL_COLONIES);
        }
        else
        {
            dialog.getOptionPanel().addOption("List current colony conditions", OptionId.VIEW_COLONY);
            dialog.getOptionPanel().addOption("Cancel current project", OptionId.CANCEL_PROJECT);
            dialog.getOptionPanel().addOption("Back", OptionId.COLONIES_WITH_NO_ONGOING_PROJECT);
        }

        if(controller.getProject() == null || controller.getProject().equals("None"))
        {
            dialog.getOptionPanel().setEnabled(OptionId.CANCEL_PROJECT, false);
        }
        dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
    }

    private void printColonyConditions()
    {
        Color highlight = Misc.getHighlightColor();
        TextPanelAPI text = this.dialog.getTextPanel();

        MarketAPI market = this.dialog.getInteractionTarget().getMarket();
        text.addPara("Current resources and conditions on %s:", highlight, new String[]{market.getName()});

        ArrayList<String> conditionsStrings = new ArrayList<>();
        for(MarketConditionAPI condition : market.getConditions())
        {
            if(condition.getPlugin().showIcon())
            {
                if(!conditionsStrings.contains(condition.getName()) && !condition.getName().equals("Population"))
                {
                    conditionsStrings.add(condition.getName());
                }
            }
        }

        Collections.sort(conditionsStrings);
        for(String name : conditionsStrings)
        {
            text.addPara("      - %s", highlight, new String[]{name});
        }
    }

    static class MarketComparator implements Comparator<MarketAPI>
    {
        public int compare(MarketAPI market1, MarketAPI market2)
        {
            int compVal = market1.getName().compareTo(market2.getName());
            if (compVal == 0)
                return 0;
            else if (compVal > 0)
                return 1;
            else
                return -1;
        }
    }

    private void typeChangeDialogueOption(boggledTerraformingPrintResultsAndRequirements printResultsAndRequirements, boggledTerraformingProjectRequirementsMet requirementsMet, OptionId projectOptionId)
    {
        String ruleId = projectOptionIdToProjectYesDialogue.get(projectOptionId);
        Pair<OptionId, OptionId> optionId = projectOptionIdToStartProjectOptionId.get(projectOptionId);

        // Print results and requirements for prospective project
        printResultsAndRequirements.execute(ruleId, dialog, null, null);

        dialog.getOptionPanel().addOption("Start project",optionId.one);

        if (!requirementsMet.execute(ruleId, dialog, null, null))
        {
            dialog.getOptionPanel().setEnabled(optionId.one, false);
        }

        dialog.getOptionPanel().addOption("Back", optionId.two);
        dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
    }

    @Override
    public void optionSelected(String optionText, Object optionData)
    {
        if (optionData instanceof OptionId)
        {
            // Clear shown options before we show new ones
            dialog.getOptionPanel().clearOptions();

            // Create objects for printing text to the panel
            boggledTerraformingInitiateProject initiateProject = new boggledTerraformingInitiateProject();
            boggledTerraformingPrintResultsAndRequirements printResultsAndRequirements = new boggledTerraformingPrintResultsAndRequirements();
            boggledTerraformingProjectRequirementsMet requirementsMet = new boggledTerraformingProjectRequirementsMet();

            // Get MarketAPI lists
            ArrayList<MarketAPI> allNonStationPlayerMarkets = boggledTools.getNonStationMarketsPlayerControls();
            Collections.sort(allNonStationPlayerMarkets, new MarketComparator());
            int numMarkets = allNonStationPlayerMarkets.size();

            ArrayList<MarketAPI> marketsWithNoOngoingProject = new ArrayList<MarketAPI>();
            for(MarketAPI market : allNonStationPlayerMarkets)
            {
                if(!market.hasCondition("terraforming_controller") || ((Terraforming_Controller) market.getCondition("terraforming_controller").getPlugin()).getProject().equals("None"))
                {
                    marketsWithNoOngoingProject.add(market);
                }
            }
            Collections.sort(marketsWithNoOngoingProject, new MarketComparator());
            int numMarketsNoOngoing = marketsWithNoOngoingProject.size();

            // Handle all possible options the player can choose
            switch ((OptionId) optionData)
            {
                // The invisible "init" option was selected by the init method.
                case DUMMY:
                    dialog.setInteractionTarget(null);
                    break;
                case INIT:
                    // Reset pagination variables
                    pageNumber = 0;

                    dialog.getTextPanel().addPara("Please select a colony to start a terraforming project.");

                    dialog.getOptionPanel().addOption("List colonies with no ongoing terraforming project", OptionId.COLONIES_WITH_NO_ONGOING_PROJECT);
                    if(numMarketsNoOngoing == 0)
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.COLONIES_WITH_NO_ONGOING_PROJECT, false);
                    }
                    dialog.getOptionPanel().addOption("List all colonies", OptionId.ALL_COLONIES);
                    if(numMarkets == 0)
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.ALL_COLONIES, false);
                    }
                    dialog.getOptionPanel().addOption("Exit", OptionId.EXIT_DIALOG);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case EXIT_DIALOG:
                    dialog.setInteractionTarget(null);
                    dialog.dismiss();
                    break;
                case COLONIES_WITH_NO_ONGOING_PROJECT:
                    if(optionText.equals("Next page"))
                    {
                        pageNumber += 1;
                    }

                    if(numMarketsNoOngoing <= 8)
                    {
                        for(int i = 0; i < numMarketsNoOngoing; i++)
                        {
                            dialog.getOptionPanel().addOption(marketsWithNoOngoingProject.get(i).getName(), getOptionIdForInt(i + 1, false));
                        }
                    }
                    else
                    {
                        for(int i = (pageNumber * 7); i < (pageNumber * 7) + 7 && i < numMarketsNoOngoing; i++)
                        {
                            dialog.getOptionPanel().addOption(marketsWithNoOngoingProject.get(i).getName(), getOptionIdForInt((i - (pageNumber * 7)) + 1, false));
                        }

                        dialog.getOptionPanel().addOption("Next page", OptionId.COLONIES_WITH_NO_ONGOING_PROJECT);
                        dialog.getOptionPanel().setEnabled(OptionId.COLONIES_WITH_NO_ONGOING_PROJECT, false);
                        if( numMarketsNoOngoing - ((pageNumber + 1) * 7) > 0)
                        {
                            dialog.getOptionPanel().setEnabled(OptionId.COLONIES_WITH_NO_ONGOING_PROJECT, true);
                        }
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.INIT);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case ALL_COLONIES:
                    if(optionText.equals("Next page"))
                    {
                        pageNumber += 1;
                    }

                    if(numMarkets <= 8)
                    {
                        for(int i = 0; i < numMarkets; i++)
                        {
                            dialog.getOptionPanel().addOption(allNonStationPlayerMarkets.get(i).getName(), getOptionIdForInt(i + 1, true));
                        }
                    }
                    else
                    {
                        for(int i = (pageNumber * 7); i < (pageNumber * 7) + 7 && i < numMarkets; i++)
                        {
                            dialog.getOptionPanel().addOption(allNonStationPlayerMarkets.get(i).getName(), getOptionIdForInt((i - (pageNumber * 7)) + 1, true));
                        }

                        dialog.getOptionPanel().addOption("Next page", OptionId.ALL_COLONIES);
                        dialog.getOptionPanel().setEnabled(OptionId.ALL_COLONIES, false);
                        if( numMarkets - ((pageNumber + 1) * 7) > 0)
                        {
                            dialog.getOptionPanel().setEnabled(OptionId.ALL_COLONIES, true);
                        }
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.INIT);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case COLONY_1:
                case COLONY_2:
                case COLONY_3:
                case COLONY_4:
                case COLONY_5:
                case COLONY_6:
                case COLONY_7:
                case COLONY_8:
                    showColonySelectedOptionsAndLoadVisual(optionText, false, true);
                    break;
                case COLONY_1_ALL:
                case COLONY_2_ALL:
                case COLONY_3_ALL:
                case COLONY_4_ALL:
                case COLONY_5_ALL:
                case COLONY_6_ALL:
                case COLONY_7_ALL:
                case COLONY_8_ALL:
                    showColonySelectedOptionsAndLoadVisual(optionText, true, true);
                    break;
                case VIEW_COLONY:
                case VIEW_COLONY_ALL:
                    printColonyConditions();
                    showColonySelectedOptionsAndLoadVisual(optionText, optionData.equals(OptionId.VIEW_COLONY_ALL), false);
                    break;
                case CANCEL_PROJECT:
                    Color bad = Misc.getNegativeHighlightColor();
                    TextPanelAPI text = this.dialog.getTextPanel();
                    text.addPara("%s", bad, "Canceling the current project will result in all progress being lost!");

                    dialog.getOptionPanel().addOption("Cancel project", OptionId.START_CANCEL_PROJECT);
                    dialog.getOptionPanel().addOption("Back", OptionId.INIT);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;


                case TYPE_CHANGE_OPTIONS:
                case RESOURCE_IMPROVEMENTS:
                case CONDITION_IMPROVEMENTS:
                    ArrayList<Pair<String, OptionId>> terraformingOption = terraformingOptions.get(optionData);
                    for (Pair<String, OptionId> terraform : terraformingOption)
                    {
                        dialog.getOptionPanel().addOption(terraform.one.replace("$planet", dialog.getInteractionTarget().getMarket().getName()), terraform.two);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.COLONY_1);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;

                /*
                Type changes from here
                */
                case ARID_TYPE_CHANGE:
                case FROZEN_TYPE_CHANGE:
                case JUNGLE_TYPE_CHANGE:
                case TERRAN_TYPE_CHANGE:
                case TUNDRA_TYPE_CHANGE:
                case WATER_TYPE_CHANGE:

                case FARMLAND_IMPROVEMENT:
                case ORGANICS_IMPROVEMENT:
                case VOLATILES_IMPROVEMENT:

                case EXTREME_WEATHER_IMPROVEMENT:
                case MILD_CLIMATE_IMPROVEMENT:
                case HABITABLE_IMPROVEMENT:
                case ATMOSPHERE_DENSITY_IMPROVEMENT:
                case TOXIC_ATMOSPHERE_IMPROVEMENT:
                case IRRADIATED_IMPROVEMENT:
                case REMOVE_ATMOSPHERE_IMPROVEMENT:
                    typeChangeDialogueOption(printResultsAndRequirements, requirementsMet, (OptionId)optionData);
                    break;

                case START_ARID_TYPE_CHANGE_PROJECT:
                case START_FROZEN_TYPE_CHANGE_PROJECT:
                case START_JUNGLE_TYPE_CHANGE_PROJECT:
                case START_TERRAN_TYPE_CHANGE_PROJECT:
                case START_TUNDRA_TYPE_CHANGE_PROJECT:
                case START_WATER_TYPE_CHANGE_PROJECT:

                case START_FARMLAND_IMPROVEMENT:
                case START_ORGANICS_IMPROVEMENT:
                case START_VOLATILES_IMPROVEMENT:

                case START_EXTREME_WEATHER_IMPROVEMENT:
                case START_MILD_CLIMATE_IMPROVEMENT:
                case START_HABITABLE_IMPROVEMENT:
                case START_ATMOSPHERE_DENSITY_IMPROVEMENT:
                case START_TOXIC_ATMOSPHERE_IMPROVEMENT:
                case START_IRRADIATED_IMPROVEMENT:
                case START_REMOVE_ATMOSPHERE:

                case START_CANCEL_PROJECT:
                    String ruleId = startProjectOptionIdToStartProjectDialogue.get(optionData);
                    initiateProject.execute(ruleId, dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
            }
        }
    }

    enum OptionId
    {
        DUMMY,
        INIT,
        EXIT_DIALOG,
        COLONIES_WITH_NO_ONGOING_PROJECT,
        ALL_COLONIES,
        COLONY_1,
        COLONY_2,
        COLONY_3,
        COLONY_4,
        COLONY_5,
        COLONY_6,
        COLONY_7,
        COLONY_8,
        COLONY_1_ALL,
        COLONY_2_ALL,
        COLONY_3_ALL,
        COLONY_4_ALL,
        COLONY_5_ALL,
        COLONY_6_ALL,
        COLONY_7_ALL,
        COLONY_8_ALL,
        VIEW_COLONY,
        VIEW_COLONY_ALL,
        TYPE_CHANGE_OPTIONS,
        CANCEL_PROJECT,
        START_CANCEL_PROJECT,
        ARID_TYPE_CHANGE,
        START_ARID_TYPE_CHANGE_PROJECT,
        FROZEN_TYPE_CHANGE,
        START_FROZEN_TYPE_CHANGE_PROJECT,
        JUNGLE_TYPE_CHANGE,
        START_JUNGLE_TYPE_CHANGE_PROJECT,
        TERRAN_TYPE_CHANGE,
        START_TERRAN_TYPE_CHANGE_PROJECT,
        TUNDRA_TYPE_CHANGE,
        START_TUNDRA_TYPE_CHANGE_PROJECT,
        WATER_TYPE_CHANGE,
        START_WATER_TYPE_CHANGE_PROJECT,
        US_TYPE_CHANGE_OPTIONS,
        RESOURCE_IMPROVEMENTS,
        FARMLAND_IMPROVEMENT,
        START_FARMLAND_IMPROVEMENT,
        ORGANICS_IMPROVEMENT,
        START_ORGANICS_IMPROVEMENT,
        VOLATILES_IMPROVEMENT,
        START_VOLATILES_IMPROVEMENT,
        CONDITION_IMPROVEMENTS,
        EXTREME_WEATHER_IMPROVEMENT,
        START_EXTREME_WEATHER_IMPROVEMENT,
        MILD_CLIMATE_IMPROVEMENT,
        START_MILD_CLIMATE_IMPROVEMENT,
        HABITABLE_IMPROVEMENT,
        START_HABITABLE_IMPROVEMENT,
        ATMOSPHERE_DENSITY_IMPROVEMENT,
        START_ATMOSPHERE_DENSITY_IMPROVEMENT,
        TOXIC_ATMOSPHERE_IMPROVEMENT,
        START_TOXIC_ATMOSPHERE_IMPROVEMENT,
        IRRADIATED_IMPROVEMENT,
        START_IRRADIATED_IMPROVEMENT,
        REMOVE_ATMOSPHERE_IMPROVEMENT,
        START_REMOVE_ATMOSPHERE,
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) { }

    @Override
    public void advance(float amount) { }

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) { }

    @Override
    public Object getContext()
    {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap()
    {
        return null;
    }
}