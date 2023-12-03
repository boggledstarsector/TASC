package data.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import data.campaign.econ.conditions.Terraforming_Controller;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class boggledTerraformingDialogPlugin implements InteractionDialogPlugin
{
    protected InteractionDialogAPI dialog;

    private int pageNumber;

    @Override
    public void init(InteractionDialogAPI dialog)
    {
        this.dialog = dialog;
        this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
    }

    public OptionId getOptionIdForInt(int i, boolean all)
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

    class MarketComparator implements Comparator<MarketAPI>
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
                    printColonyConditions();
                    showColonySelectedOptionsAndLoadVisual(optionText, false, false);
                    break;
                case VIEW_COLONY_ALL:
                    printColonyConditions();
                    showColonySelectedOptionsAndLoadVisual(optionText, true, false);
                    break;
                case CANCEL_PROJECT:
                    Color bad = Misc.getNegativeHighlightColor();
                    TextPanelAPI text = this.dialog.getTextPanel();
                    text.addPara("%s", bad, new String[]{"Canceling the current project will result in all progress being lost!"});

                    dialog.getOptionPanel().addOption("Cancel project", OptionId.START_CANCEL_PROJECT);
                    dialog.getOptionPanel().addOption("Back", OptionId.INIT);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_CANCEL_PROJECT:
                    initiateProject.execute("boggledTriggerCancelCurrentProject", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case TYPE_CHANGE_OPTIONS:
                    dialog.getOptionPanel().addOption("Consider terraforming " + dialog.getInteractionTarget().getMarket().getName() + " into an arid world", OptionId.ARID_TYPE_CHANGE);
                    dialog.getOptionPanel().addOption("Consider terraforming " + dialog.getInteractionTarget().getMarket().getName() + " into a frozen world", OptionId.FROZEN_TYPE_CHANGE);
                    dialog.getOptionPanel().addOption("Consider terraforming " + dialog.getInteractionTarget().getMarket().getName() + " into a jungle world", OptionId.JUNGLE_TYPE_CHANGE);
                    dialog.getOptionPanel().addOption("Consider terraforming " + dialog.getInteractionTarget().getMarket().getName() + " into a Terran world", OptionId.TERRAN_TYPE_CHANGE);
                    dialog.getOptionPanel().addOption("Consider terraforming " + dialog.getInteractionTarget().getMarket().getName() + " into a tundra world", OptionId.TUNDRA_TYPE_CHANGE);
                    dialog.getOptionPanel().addOption("Consider terraforming " + dialog.getInteractionTarget().getMarket().getName() + " into a water world", OptionId.WATER_TYPE_CHANGE);

                    dialog.getOptionPanel().addOption("Back", OptionId.COLONY_1);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case ARID_TYPE_CHANGE:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledAridTypeChangeYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_ARID_TYPE_CHANGE_PROJECT);
                    if(!requirementsMet.execute("boggledAridTypeChangeYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_ARID_TYPE_CHANGE_PROJECT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.TYPE_CHANGE_OPTIONS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_ARID_TYPE_CHANGE_PROJECT:
                    initiateProject.execute("boggledTriggerAridTypeChange", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case FROZEN_TYPE_CHANGE:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledFrozenTypeChangeYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_FROZEN_TYPE_CHANGE_PROJECT);
                    if(!requirementsMet.execute("boggledFrozenTypeChangeYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_FROZEN_TYPE_CHANGE_PROJECT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.TYPE_CHANGE_OPTIONS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_FROZEN_TYPE_CHANGE_PROJECT:
                    initiateProject.execute("boggledTriggerFrozenTypeChange", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case JUNGLE_TYPE_CHANGE:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledJungleTypeChangeYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_JUNGLE_TYPE_CHANGE_PROJECT);
                    if(!requirementsMet.execute("boggledJungleTypeChangeYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_JUNGLE_TYPE_CHANGE_PROJECT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.TYPE_CHANGE_OPTIONS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_JUNGLE_TYPE_CHANGE_PROJECT:
                    initiateProject.execute("boggledTriggerJungleTypeChange", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case TERRAN_TYPE_CHANGE:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledTerranTypeChangeYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_TERRAN_TYPE_CHANGE_PROJECT);
                    if(!requirementsMet.execute("boggledTerranTypeChangeYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_TERRAN_TYPE_CHANGE_PROJECT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.TYPE_CHANGE_OPTIONS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_TERRAN_TYPE_CHANGE_PROJECT:
                    initiateProject.execute("boggledTriggerTerranTypeChange", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case TUNDRA_TYPE_CHANGE:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledTundraTypeChangeYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_TUNDRA_TYPE_CHANGE_PROJECT);
                    if(!requirementsMet.execute("boggledTundraTypeChangeYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_TUNDRA_TYPE_CHANGE_PROJECT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.TYPE_CHANGE_OPTIONS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_TUNDRA_TYPE_CHANGE_PROJECT:
                    initiateProject.execute("boggledTriggerTundraTypeChange", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case WATER_TYPE_CHANGE:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledWaterTypeChangeYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_WATER_TYPE_CHANGE_PROJECT);
                    if(!requirementsMet.execute("boggledWaterTypeChangeYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_WATER_TYPE_CHANGE_PROJECT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.TYPE_CHANGE_OPTIONS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_WATER_TYPE_CHANGE_PROJECT:
                    initiateProject.execute("boggledTriggerWaterTypeChange", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case RESOURCE_IMPROVEMENTS:
                    dialog.getOptionPanel().addOption("Consider improving the farmland on " + dialog.getInteractionTarget().getMarket().getName(), OptionId.FARMLAND_IMPROVEMENT);
                    dialog.getOptionPanel().addOption("Consider improving the organics on " + dialog.getInteractionTarget().getMarket().getName(), OptionId.ORGANICS_IMPROVEMENT);
                    dialog.getOptionPanel().addOption("Consider improving the volatiles on " + dialog.getInteractionTarget().getMarket().getName(), OptionId.VOLATILES_IMPROVEMENT);

                    dialog.getOptionPanel().addOption("Back", OptionId.COLONY_1);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case FARMLAND_IMPROVEMENT:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledFarmlandResourceImprovementYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_FARMLAND_IMPROVEMENT);
                    if(!requirementsMet.execute("boggledFarmlandResourceImprovementYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_FARMLAND_IMPROVEMENT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.RESOURCE_IMPROVEMENTS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_FARMLAND_IMPROVEMENT:
                    initiateProject.execute("boggledTriggerFarmlandResourceImprovement", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case ORGANICS_IMPROVEMENT:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledOrganicsResourceImprovementYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_ORGANICS_IMPROVEMENT);
                    if(!requirementsMet.execute("boggledOrganicsResourceImprovementYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_ORGANICS_IMPROVEMENT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.RESOURCE_IMPROVEMENTS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_ORGANICS_IMPROVEMENT:
                    initiateProject.execute("boggledTriggerOrganicsResourceImprovement", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case VOLATILES_IMPROVEMENT:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledVolatilesResourceImprovementYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_VOLATILES_IMPROVEMENT);
                    if(!requirementsMet.execute("boggledVolatilesResourceImprovementYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_VOLATILES_IMPROVEMENT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.RESOURCE_IMPROVEMENTS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_VOLATILES_IMPROVEMENT:
                    initiateProject.execute("boggledTriggerVolatilesResourceImprovement", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case CONDITION_IMPROVEMENTS:
                    dialog.getOptionPanel().addOption("Consider making the weather patterns on " + dialog.getInteractionTarget().getMarket().getName() + " less extreme", OptionId.EXTREME_WEATHER_IMPROVEMENT);
                    dialog.getOptionPanel().addOption("Consider making the weather patterns on " + dialog.getInteractionTarget().getMarket().getName() + " mild", OptionId.MILD_CLIMATE_IMPROVEMENT);
                    dialog.getOptionPanel().addOption("Consider making the atmosphere on " + dialog.getInteractionTarget().getMarket().getName() + " human-breathable", OptionId.HABITABLE_IMPROVEMENT);
                    dialog.getOptionPanel().addOption("Consider normalizing atmospheric density on " + dialog.getInteractionTarget().getMarket().getName(), OptionId.ATMOSPHERE_DENSITY_IMPROVEMENT);
                    dialog.getOptionPanel().addOption("Consider remediating atmospheric toxicity on " + dialog.getInteractionTarget().getMarket().getName(), OptionId.TOXIC_ATMOSPHERE_IMPROVEMENT);
                    if(boggledTools.getBooleanSetting("boggledTerraformingRemoveRadiationProjectEnabled"))
                    {
                        dialog.getOptionPanel().addOption("Consider remediating radiation on " + dialog.getInteractionTarget().getMarket().getName(), OptionId.IRRADIATED_IMPROVEMENT);
                    }
                    if(boggledTools.getBooleanSetting("boggledTerraformingRemoveAtmosphereProjectEnabled"))
                    {
                        dialog.getOptionPanel().addOption("Consider removing the atmosphere on " + dialog.getInteractionTarget().getMarket().getName(), OptionId.REMOVE_ATMOSPHERE_IMPROVEMENT);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.COLONY_1);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case EXTREME_WEATHER_IMPROVEMENT:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledExtremeWeatherConditionImprovementYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_EXTREME_WEATHER_IMPROVEMENT);
                    if(!requirementsMet.execute("boggledExtremeWeatherConditionImprovementYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_EXTREME_WEATHER_IMPROVEMENT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.CONDITION_IMPROVEMENTS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_EXTREME_WEATHER_IMPROVEMENT:
                    initiateProject.execute("boggledTriggerExtremeWeatherConditionImprovement", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case MILD_CLIMATE_IMPROVEMENT:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledMildClimateConditionImprovementYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_MILD_CLIMATE_IMPROVEMENT);
                    if(!requirementsMet.execute("boggledMildClimateConditionImprovementYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_MILD_CLIMATE_IMPROVEMENT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.CONDITION_IMPROVEMENTS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_MILD_CLIMATE_IMPROVEMENT:
                    initiateProject.execute("boggledTriggerMildClimateConditionImprovement", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case HABITABLE_IMPROVEMENT:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledHabitableConditionImprovementYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_HABITABLE_IMPROVEMENT);
                    if(!requirementsMet.execute("boggledHabitableConditionImprovementYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_HABITABLE_IMPROVEMENT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.CONDITION_IMPROVEMENTS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_HABITABLE_IMPROVEMENT:
                    initiateProject.execute("boggledTriggerHabitableConditionImprovement", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case ATMOSPHERE_DENSITY_IMPROVEMENT:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledAtmosphereDensityConditionImprovementYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_ATMOSPHERE_DENSITY_IMPROVEMENT);
                    if(!requirementsMet.execute("boggledAtmosphereDensityConditionImprovementYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_ATMOSPHERE_DENSITY_IMPROVEMENT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.CONDITION_IMPROVEMENTS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_ATMOSPHERE_DENSITY_IMPROVEMENT:
                    initiateProject.execute("boggledTriggerAtmosphereDensityConditionImprovement", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case TOXIC_ATMOSPHERE_IMPROVEMENT:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledToxicAtmosphereConditionImprovementYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_TOXIC_ATMOSPHERE_IMPROVEMENT);
                    if(!requirementsMet.execute("boggledToxicAtmosphereConditionImprovementYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_TOXIC_ATMOSPHERE_IMPROVEMENT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.CONDITION_IMPROVEMENTS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_TOXIC_ATMOSPHERE_IMPROVEMENT:
                    initiateProject.execute("boggledTriggerToxicAtmosphereConditionImprovement", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case IRRADIATED_IMPROVEMENT:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledIrradiatedConditionImprovementYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_IRRADIATED_IMPROVEMENT);
                    if(!requirementsMet.execute("boggledIrradiatedConditionImprovementYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_IRRADIATED_IMPROVEMENT, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.CONDITION_IMPROVEMENTS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_IRRADIATED_IMPROVEMENT:
                    initiateProject.execute("boggledTriggerIrradiatedConditionImprovement", dialog, null, null);
                    this.optionSelected(null, boggledTerraformingDialogPlugin.OptionId.INIT);
                    break;
                case REMOVE_ATMOSPHERE_IMPROVEMENT:
                    // Print results and requirements for prospective project
                    printResultsAndRequirements.execute("boggledRemoveAtmosphereConditionImprovementYes", dialog, null, null);

                    // Add start project option and disable it if the requirements are not met
                    dialog.getOptionPanel().addOption("Start project", OptionId.START_REMOVE_ATMOSPHERE);
                    if(!requirementsMet.execute("boggledRemoveAtmosphereConditionImprovementYes", dialog, null, null))
                    {
                        dialog.getOptionPanel().setEnabled(OptionId.START_REMOVE_ATMOSPHERE, false);
                    }

                    dialog.getOptionPanel().addOption("Back", OptionId.CONDITION_IMPROVEMENTS);
                    dialog.setOptionOnEscape("Exit", OptionId.DUMMY);
                    break;
                case START_REMOVE_ATMOSPHERE:
                    initiateProject.execute("boggledTriggerRemoveAtmosphereConditionImprovement", dialog, null, null);
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