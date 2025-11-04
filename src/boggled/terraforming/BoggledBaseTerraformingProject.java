package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import boggled.campaign.econ.conditions.Terraforming_Controller;
import boggled.terraforming.tooltips.BoggledBaseTerraformingProjectTooltip;
import boggled.ui.BoggledCoreModifierEveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CoreUITabId;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignPlanet;

import java.awt.*;
import java.util.*;

public class BoggledBaseTerraformingProject extends BaseIntelPlugin {

    protected MarketAPI market;
    protected TerraformingProjectType projectType;
    private int daysCompleted = 0;
    private int lastDayChecked = 0;
    private final int requiredDaysToCompleteProject;

    private static final String BUTTON_OPEN_TERRAFORMING_MENU = "Open terraforming menu";

    private boolean requirementsWereMetLastTick = true;

    public enum TerraformingProjectType {
        PLANET_TYPE_CHANGE, RESOURCE_IMPROVEMENT, CONDITION_IMPROVEMENT
    }

    public enum ProjectUpdateType {
        STARTED, STALLED, RESUMED, COMPLETED, CANCELLED
    }

    private final HashMap<ProjectUpdateType, String> updateMessageMap = new HashMap<>(){{
        put(ProjectUpdateType.STARTED, "Started");
        put(ProjectUpdateType.STALLED, "Stalled");
        put(ProjectUpdateType.RESUMED, "Resumed");
        put(ProjectUpdateType.COMPLETED, "Completed");
        put(ProjectUpdateType.CANCELLED, "Cancelled");
    }};

    public BoggledBaseTerraformingProject(MarketAPI market, TerraformingProjectType projectType) {
        this.market = market;
        this.projectType = projectType;
        this.requiredDaysToCompleteProject = switch (projectType) {
            case PLANET_TYPE_CHANGE -> boggledTools.getIntSetting("boggledTerraformingTime");
            case RESOURCE_IMPROVEMENT -> boggledTools.getIntSetting("boggledResourceImprovementTime");
            case CONDITION_IMPROVEMENT -> boggledTools.getIntSetting("boggledConditionImprovementTime");
        };
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        return this.market.getPlanetEntity();
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        if(this.isEnded())
        {
            return;
        }

        CampaignClockAPI clock = Global.getSector().getClock();
        if (clock.getDay() != this.lastDayChecked) {
            // Avoid calling requirementsMet() every frame because it does a lot of calculations
            boolean requirementsMet = requirementsMet(getProjectRequirements());
            if (requirementsMet) {
                if(!this.requirementsWereMetLastTick)
                {
                    this.resumeThisProject();
                    this.requirementsWereMetLastTick = true;
                }

                this.daysCompleted++;
                this.lastDayChecked = clock.getDay();

                if (this.daysCompleted >= this.requiredDaysToCompleteProject) {
                    completeThisProject();
                }
            } else {
                if(this.requirementsWereMetLastTick)
                {
                    this.stallThisProject();
                    this.requirementsWereMetLastTick = false;
                }
                this.lastDayChecked = clock.getDay();
            }
        }
    }

    public MarketAPI getMarket() {
        return this.market;
    }

    public int getDaysRemaining() {
        return this.requiredDaysToCompleteProject - this.daysCompleted;
    }

    public void startThisProject() {
        boggledTools.addCondition(market, boggledTools.BoggledConditions.terraformingControllerConditionId);
        Terraforming_Controller controller = boggledTools.getTerraformingControllerFromMarket(this.market);
        BoggledBaseTerraformingProject existingProject = controller.getCurrentProject();
        if(existingProject != null)
        {
            existingProject.cancelThisProject();
        }
        sendUpdateNotificationToPlayer(ProjectUpdateType.STARTED);
        controller.setCurrentProject(this);
        Global.getSector().getIntelManager().addIntel(this, true);
        Global.getSector().addTransientScript(this);
    }

    public void completeThisProject() {
        sendUpdateNotificationToPlayer(ProjectUpdateType.COMPLETED);
        Terraforming_Controller controller = boggledTools.getTerraformingControllerFromMarket(this.market);
        controller.setCurrentProject(null);
        this.endImmediately();
    }

    public void cancelThisProject() {
        sendUpdateNotificationToPlayer(ProjectUpdateType.CANCELLED);
        Terraforming_Controller controller = boggledTools.getTerraformingControllerFromMarket(this.market);
        controller.setCurrentProject(null);
        this.endImmediately();
    }

    public void stallThisProject() {
        sendUpdateNotificationToPlayer(ProjectUpdateType.STALLED);
    }

    public void resumeThisProject() {
        sendUpdateNotificationToPlayer(ProjectUpdateType.RESUMED);
    }

    public void sendUpdateNotificationToPlayer(ProjectUpdateType type)
    {
        MessageIntel intel = new MessageIntel(this.getProjectName() + " project on " + this.market.getName(), Misc.getBasePlayerColor());
        intel.addLine("    - " + updateMessageMap.get(type));
        intel.setIcon(this.getIcon());
        intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
        if(type == ProjectUpdateType.STARTED || type == ProjectUpdateType.STALLED || type == ProjectUpdateType.RESUMED)
        {
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.INTEL_TAB, this);
        }
        else
        {
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    public HashSet<String> constructConditionsListAfterProjectCompletion() {
        HashSet<String> constructedConditions = new HashSet<>();
        for (MarketConditionAPI marketCondition : this.market.getConditions()) {
            constructedConditions.add(marketCondition.getId());
        }

        for (String conditionToRemove : conditionsToRemoveUponCompletion()) {
            constructedConditions.remove(conditionToRemove);
        }

        constructedConditions.addAll(conditionsToAddUponCompletion());

        return constructedConditions;
    }

    public ArrayList<String> conditionsToAddUponCompletion() {
        return new ArrayList<>();
    }

    public ArrayList<String> conditionsToRemoveUponCompletion() {
        return new ArrayList<>();
    }

    public CampaignPlanet constructFakePlanetWithAppearanceAfterTerraforming() {
        return (CampaignPlanet) this.market.getPlanetEntity();
    }

    public String getProjectName() {
        return "Override this";
    }

    public static boolean isUnknownSkiesPlanetType(String str) {
        if (str == null || str.length() < 3) {
            return false;
        }

        // Use a case-insensitive check
        String prefix = str.substring(0, 3);
        return prefix.equalsIgnoreCase("us_");
    }

    public class TerraformingRequirementObject {
        public String tooltipDisplayText;
        public boolean requirementMet;
        public TooltipMakerAPI.TooltipCreator tooltip;

        public TerraformingRequirementObject(String tooltipDisplayText, Boolean requirementMet, TooltipMakerAPI.TooltipCreator tooltip) {
            this.tooltipDisplayText = tooltipDisplayText;
            this.requirementMet = requirementMet;
            this.tooltip = tooltip;
        }
    }

    public TerraformingRequirementObject getRequirementWorldTypeAllowsTerraforming() {
        String tascPlanetType = boggledTools.getTascPlanetType(market.getPlanetEntity());
        Boolean worldTypeAllowsTerraforming = boggledTools.tascPlanetTypeAllowsTerraforming(tascPlanetType);
        BoggledBaseTerraformingProjectTooltip tooltip = new BoggledBaseTerraformingProjectTooltip(this) {
            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                super.createTooltip(tooltipMakerAPI, b, o);
                tooltipMakerAPI.setTitleFont(Fonts.ORBITRON_12);
                tooltipMakerAPI.setTitleFontColor(worldTypeAllowsTerraforming ? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor());
                tooltipMakerAPI.addTitle("World type allows terraforming");
                tooltipMakerAPI.addPara("%s is considered a %s world. %s worlds %s be terraformed.", 10, Misc.getTextColor(), Misc.getHighlightColor(), new String[]{
                        market.getName(),
                        boggledTools.tascPlanetTypeDisplayStringMap.get(boggledTools.getTascPlanetType(market.getPlanetEntity())).two,
                        boggledTools.tascPlanetTypeDisplayStringMap.get(boggledTools.getTascPlanetType(market.getPlanetEntity())).one,
                        worldTypeAllowsTerraforming ? "can" : "cannot"
                });

                if(boggledTools.getTascPlanetType(market.getPlanetEntity()).equals(boggledTools.TascPlanetTypes.unknownPlanetId))
                {
                    tooltipMakerAPI.addPara("Please notify me (boggled) in the TASC forum thread that this planet type is unsupported. I will promptly issue a patch to add support. Be sure to provide this ID: " + market.getPlanetEntity().getTypeId(), 10);
                }
            }
        };

        return new TerraformingRequirementObject("World type allows terraforming", worldTypeAllowsTerraforming, tooltip);
    }

    public TerraformingRequirementObject getRequirementWorldTypeAllowsHumanHabitability() {
        String tascPlanetType = boggledTools.getTascPlanetType(market.getPlanetEntity());
        Boolean worldTypeAllowsTerraforming = boggledTools.tascPlanetTypeAllowsHumanHabitability(tascPlanetType);
        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
            }
        };

        return new TerraformingRequirementObject("World type can be habitable for humans", worldTypeAllowsTerraforming, null);
    }

    public TerraformingRequirementObject getRequirementMarketIsHabitable() {
        boolean requirementMet = this.market.hasCondition(Conditions.HABITABLE);
        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " is habitable for humans", requirementMet, null);
    }

    public TerraformingRequirementObject getRequirementAtmosphericDensityNormal() {
        Boolean requirementMet = !this.market.hasCondition(Conditions.NO_ATMOSPHERE) && !this.market.hasCondition(Conditions.THIN_ATMOSPHERE) && !this.market.hasCondition(Conditions.DENSE_ATMOSPHERE);
        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " has standard atmospheric density", requirementMet, null);
    }

    public TerraformingRequirementObject getRequirementAtmosphericNotToxicOrIrradiated() {
        Boolean requirementMet = !this.market.hasCondition(Conditions.IRRADIATED) && !this.market.hasCondition(Conditions.TOXIC_ATMOSPHERE);
        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                tooltipMakerAPI.addPara("Dummy text here - toxic or irrad", 10f);
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " does not have a toxic or irradiated atmosphere", requirementMet, null);
    }

    public TerraformingRequirementObject getRequirementMarketHasAtmosphereProcessor() {
        boolean requirementMet = this.market.hasIndustry(boggledTools.BoggledIndustries.atmosphereProcessorId);
        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                tooltipMakerAPI.addPara("Dummy text here - has atmo proc", 10f);
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " has an atmosphere processor", requirementMet, null);
    }

    public TerraformingRequirementObject getRequirementMarketHasStellarReflectorArray() {
        boolean requirementMet = this.market.hasIndustry(boggledTools.BoggledIndustries.stellarReflectorArrayIndustryId);
        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                tooltipMakerAPI.addPara("Dummy text here - has stellar reflector array", 10f);
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " has a stellar reflector array", requirementMet, null);
    }

    public ArrayList<TerraformingRequirementObject> getProjectRequirements() {
        ArrayList<TerraformingRequirementObject> projects = new ArrayList<>();
        if (Global.getSettings().getModManager().isModEnabled(boggledTools.BoggledMods.atodVokModId)) {
            projects.add(getRequirementProjectIsResearched());
        }

        return projects;
    }

    public boolean requirementsMet(ArrayList<TerraformingRequirementObject> projectRequirements) {
        for (TerraformingRequirementObject projectRequirementData : projectRequirements) {
            if (!projectRequirementData.requirementMet) {
                return false;
            }
        }

        return true;
    }

    public TerraformingRequirementObject getRequirementProjectIsResearched() {
        Boolean requirementMet = switch (projectType) {
            case PLANET_TYPE_CHANGE ->
                    boggledTools.isResearched(boggledTools.BoggledResearchProjects.planetTypeManipulation);
            case RESOURCE_IMPROVEMENT ->
                    boggledTools.isResearched(boggledTools.BoggledResearchProjects.resourceManipulation);
            case CONDITION_IMPROVEMENT ->
                    boggledTools.isResearched(boggledTools.BoggledResearchProjects.atmosphereManipulation);
        };

        String terraformingProjectType = switch (projectType) {
            case PLANET_TYPE_CHANGE -> "Planet Type Manipulation";
            case RESOURCE_IMPROVEMENT -> "Resource Manipulation";
            case CONDITION_IMPROVEMENT -> "Atmosphere Manipulation";
        };
        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                tooltipMakerAPI.addPara("Dummy text here - researched", 10f);
            }
        };

        return new TerraformingRequirementObject(terraformingProjectType + " research project has been completed", requirementMet, tooltip);
    }

    public TerraformingRequirementObject getRequirementMarketIsVeryCold() {
        Boolean requirementMet = this.market.hasCondition(Conditions.VERY_COLD);

        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                tooltipMakerAPI.addPara("Dummy text here - very cold", 10f);
            }
        };

        return new TerraformingRequirementObject("Temperature is very cold", requirementMet, tooltip);
    }

    public TerraformingRequirementObject getRequirementMarketIsTemperateOrCold() {
        Boolean requirementMet = !this.market.hasCondition(Conditions.VERY_COLD) && !this.market.hasCondition(Conditions.HOT) && !this.market.hasCondition(Conditions.VERY_HOT);

        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                tooltipMakerAPI.addPara("Dummy text here - temperate or cold", 10f);
            }
        };

        return new TerraformingRequirementObject("Temperature is temperate or cold", requirementMet, null);
    }

    public TerraformingRequirementObject getRequirementMarketIsTemperateOrHot() {
        Boolean requirementMet = !this.market.hasCondition(Conditions.VERY_COLD) && !this.market.hasCondition(Conditions.COLD) && !this.market.hasCondition(Conditions.VERY_HOT);

        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                tooltipMakerAPI.addPara("Dummy text here - temperate or hot", 10f);
            }
        };

        return new TerraformingRequirementObject("Temperature is temperate or hot", requirementMet, null);
    }

    public TerraformingRequirementObject getRequirementMarketIsNotVeryHotOrVeryCold() {
        Boolean requirementMet = !this.market.hasCondition(Conditions.VERY_COLD) && !this.market.hasCondition(Conditions.VERY_HOT);

        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                tooltipMakerAPI.addPara("Dummy text here - not very hot or very cold", 10f);
            }
        };

        return new TerraformingRequirementObject("Temperature is not very hot or very cold", requirementMet, null);
    }

    public TerraformingRequirementObject getRequirementMarketHasModerateWater()
    {
        boggledTools.PlanetWaterLevel currentWaterLevel = boggledTools.getWaterLevelForMarket(this.market);
        boolean requirementMet = currentWaterLevel == boggledTools.PlanetWaterLevel.HIGH_WATER || currentWaterLevel == boggledTools.PlanetWaterLevel.MEDIUM_WATER;

        BoggledBaseTerraformingProjectTooltip tooltip = new BoggledBaseTerraformingProjectTooltip(this) {
            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                super.createTooltip(tooltipMakerAPI, b, o);
                tooltipMakerAPI.setTitleFont(Fonts.ORBITRON_12);
                tooltipMakerAPI.setTitleFontColor(requirementMet ? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor());
                tooltipMakerAPI.addTitle(market.getName() + " has at least a moderate amount of water");
                String waterString = switch(boggledTools.getBaseWaterLevelForTascPlanetType(boggledTools.getTascPlanetType(market.getPlanetEntity())))
                {
                    case LOW_WATER -> "low";
                    case MEDIUM_WATER -> "moderate";
                    case HIGH_WATER -> "large";
                };
                tooltipMakerAPI.addPara("%s is considered a %s world. %s worlds always have a %s amount of water present.", 10, Misc.getTextColor(), Misc.getHighlightColor(), new String[]{
                        market.getName(),
                        boggledTools.tascPlanetTypeDisplayStringMap.get(boggledTools.getTascPlanetType(market.getPlanetEntity())).two,
                        boggledTools.tascPlanetTypeDisplayStringMap.get(boggledTools.getTascPlanetType(market.getPlanetEntity())).one,
                        waterString
                });
                tooltipMakerAPI.addPara("The amount of water present on a given planet for terraforming purposes can be increased to %s " +
                        "by constructing an Ismara's Sling or Asteroid Processing building at a colony in the same system. ",
                        10, Misc.getTextColor(), Misc.getHighlightColor(), new String[]{
                        "large"
                });

                ArrayList<Pair<MarketAPI, boggledTools.WaterIndustryStatus>> waterStatusList = boggledTools.getWaterIndustryStatusForSystem(market.getStarSystem());
                waterStatusList.sort(Comparator.comparing(BoggledBaseTerraformingProject::getSortOrderForWaterStatus));
                if(!waterStatusList.isEmpty())
                {
                    tooltipMakerAPI.addPara("Colonies you control in this system with one of those buildings: ", 10, Misc.getTextColor(), Misc.getTextColor(), new String[]{});
                    for(Pair<MarketAPI, boggledTools.WaterIndustryStatus> marketStatus : waterStatusList)
                    {
                        if(marketStatus.two == boggledTools.WaterIndustryStatus.OPERATIONAL)
                        {
                            tooltipMakerAPI.addPara("           " + marketStatus.one.getName() + " (operational)", 2, Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor(), new String[]{});
                        }
                        else if(marketStatus.two == boggledTools.WaterIndustryStatus.DISRUPTED)
                        {
                            tooltipMakerAPI.addPara("           " + marketStatus.one.getName() + " (disrupted)", 2, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), new String[]{});
                        }
                        else if(marketStatus.two == boggledTools.WaterIndustryStatus.SHORTAGE)
                        {
                            tooltipMakerAPI.addPara("           " + marketStatus.one.getName() + " (shortage)", 2, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), new String[]{});
                        }
                        else if(marketStatus.two == boggledTools.WaterIndustryStatus.UNDER_CONSTRUCTION)
                        {
                            tooltipMakerAPI.addPara("           " + marketStatus.one.getName() + " (under construction)", 2, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), new String[]{});
                        }
                    }
                }
                else
                {
                    tooltipMakerAPI.addPara("You don't control any colonies in this system with one of those buildings.", 10, Misc.getTextColor(), Misc.getTextColor(), new String[]{});
                }
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " has at least a moderate amount of water", requirementMet, tooltip);
    }



    public TerraformingRequirementObject getRequirementMarketHasHighWater()
    {
        boggledTools.PlanetWaterLevel currentWaterLevel = boggledTools.getWaterLevelForMarket(this.market);
        boolean requirementMet = currentWaterLevel == boggledTools.PlanetWaterLevel.HIGH_WATER;

        BoggledBaseTerraformingProjectTooltip tooltip = new BoggledBaseTerraformingProjectTooltip(this) {
            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                super.createTooltip(tooltipMakerAPI, b, o);
                tooltipMakerAPI.setTitleFont(Fonts.ORBITRON_12);
                tooltipMakerAPI.setTitleFontColor(requirementMet ? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor());
                tooltipMakerAPI.addTitle(market.getName() + " has a large amount of water");
                String waterString = switch(boggledTools.getBaseWaterLevelForTascPlanetType(boggledTools.getTascPlanetType(market.getPlanetEntity())))
                {
                    case LOW_WATER -> "low";
                    case MEDIUM_WATER -> "moderate";
                    case HIGH_WATER -> "large";
                };
                tooltipMakerAPI.addPara("%s is considered a %s world. %s worlds always have a %s amount of water present.", 10, Misc.getTextColor(), Misc.getHighlightColor(), new String[]{
                        market.getName(),
                        boggledTools.tascPlanetTypeDisplayStringMap.get(boggledTools.getTascPlanetType(market.getPlanetEntity())).two,
                        boggledTools.tascPlanetTypeDisplayStringMap.get(boggledTools.getTascPlanetType(market.getPlanetEntity())).one,
                        waterString
                });
                tooltipMakerAPI.addPara("The amount of water present on a given planet for terraforming purposes can be increased to %s " +
                                "by constructing an Ismara's Sling or Asteroid Processing building at a colony in the same system. ",
                        10, Misc.getTextColor(), Misc.getHighlightColor(), new String[]{
                                "large"
                        });

                ArrayList<Pair<MarketAPI, boggledTools.WaterIndustryStatus>> waterStatusList = boggledTools.getWaterIndustryStatusForSystem(market.getStarSystem());
                waterStatusList.sort(Comparator.comparing(BoggledBaseTerraformingProject::getSortOrderForWaterStatus));
                if(!waterStatusList.isEmpty())
                {
                    tooltipMakerAPI.addPara("Colonies you control in this system with one of those buildings: ", 10, Misc.getTextColor(), Misc.getTextColor(), new String[]{});
                    for(Pair<MarketAPI, boggledTools.WaterIndustryStatus> marketStatus : waterStatusList)
                    {
                        if(marketStatus.two == boggledTools.WaterIndustryStatus.OPERATIONAL)
                        {
                            tooltipMakerAPI.addPara("           " + marketStatus.one.getName() + " (operational)", 2, Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor(), new String[]{});
                        }
                        else if(marketStatus.two == boggledTools.WaterIndustryStatus.DISRUPTED)
                        {
                            tooltipMakerAPI.addPara("           " + marketStatus.one.getName() + " (disrupted)", 2, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), new String[]{});
                        }
                        else if(marketStatus.two == boggledTools.WaterIndustryStatus.SHORTAGE)
                        {
                            tooltipMakerAPI.addPara("           " + marketStatus.one.getName() + " (shortage)", 2, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), new String[]{});
                        }
                        else if(marketStatus.two == boggledTools.WaterIndustryStatus.UNDER_CONSTRUCTION)
                        {
                            tooltipMakerAPI.addPara("           " + marketStatus.one.getName() + " (under construction)", 2, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor(), new String[]{});
                        }
                    }
                }
                else
                {
                    tooltipMakerAPI.addPara("You don't control any colonies in this system with one of those buildings.", 10, Misc.getTextColor(), Misc.getTextColor(), new String[]{});
                }
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " has a large amount of water", requirementMet, tooltip);
    }

    private static String getSortOrderForWaterStatus(Pair<MarketAPI, boggledTools.WaterIndustryStatus> marketStatus)
    {
        return marketStatus.one.getName();
    }

    public String getModId() {
        return boggledTools.BoggledMods.tascModId;
    }

    @Override
    protected String getName()
    {
        return this.market.getName() + " Terraforming Project";
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("boggled", "terraforming_intel_icon");
    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
        Color highlight = Misc.getHighlightColor();
        Color gray = Misc.getGrayColor();
        Color playerColor = Misc.getBasePlayerColor();
        Color bad = Misc.getNegativeHighlightColor();
        float pad = 3.0F;
        float opad = 10.0F;
        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) {
            initPad = opad;
        }

        Color tc = this.getBulletColorForMode(mode);
        this.bullet(info);
        info.addPara("Project: %s", initPad, tc, playerColor, new String[]{this.getProjectName()});

        if(requirementsMet(getProjectRequirements()))
        {
            if(getDaysRemaining() == 1)
            {
                info.addPara("%s day remaining until completion", initPad, tc, highlight, new String[]{String.valueOf(getDaysRemaining())});
            }
            else
            {
                info.addPara("%s days remaining until completion", initPad, tc, highlight, new String[]{String.valueOf(getDaysRemaining())});
            }
        }
        else
        {
            info.addPara("Progress is stalled due to unmet requirements", initPad, bad, bad, new String[]{});
        }


        this.unindent(info);
    }

    @Override
    public boolean hasSmallDescription() {
        return true;
    }

    @Override
    public String getSmallDescriptionTitle()
    {
        return this.market.getName() + " Terraforming Project";
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color highlight = Misc.getHighlightColor();
        Color gray = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3.0F;
        float opad = 10.0F;

        // Player flag
        FactionAPI faction = this.market.getFaction();
        info.addImage(faction.getLogo(), width, 128.0F, 10);

        // Project status
        info.addPara("Project: %s", opad, tc, highlight, new String[]{this.getProjectName()});
        if(getDaysRemaining() == 1)
        {
            info.addPara("There is %s day remaining until this project is complete.", pad, tc, highlight, new String[]{String.valueOf(getDaysRemaining())});
        }
        else
        {
            info.addPara("There are %s days remaining until this project is complete.", pad, tc, highlight, new String[]{String.valueOf(getDaysRemaining())});
        }
        if(!requirementsMet(getProjectRequirements()))
        {
            info.addPara("Progress on this project is currently stalled due to unmet requirements.", pad, highlight, null, new String[]{});
        }

        // Print requirements
        info.addSectionHeading("Project Requirements", Alignment.MID, opad);

        // Get requirements for the selected project
        boolean firstRow = true;
        ArrayList<BoggledBaseTerraformingProject.TerraformingRequirementObject> projectRequirements = this.getProjectRequirements();
        for(BoggledBaseTerraformingProject.TerraformingRequirementObject requirement : projectRequirements)
        {
            Color textColor = requirement.requirementMet ? Misc.getPositiveHighlightColor() : Misc.getNegativeHighlightColor();
            info.addPara(requirement.tooltipDisplayText, textColor,firstRow ? opad : pad);
            firstRow = false;

            if(requirement.tooltip != null)
            {
                info.addTooltipToPrevious(requirement.tooltip, TooltipMakerAPI.TooltipLocation.ABOVE,false);
            }
        }

        info.addButton("Open terraforming menu", BUTTON_OPEN_TERRAFORMING_MENU, this.getFactionForUIColors().getBaseUIColor(), this.getFactionForUIColors().getDarkUIColor(), (float)((int)width), 20.0F, opad * 1.0F);
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if (buttonId == BUTTON_OPEN_TERRAFORMING_MENU)
        {
            MarketAPI targetMarket = this.market;
            BoggledCoreModifierEveryFrameScript.setMarketToOpen(targetMarket);
            Global.getSector().getCampaignUI().showCoreUITab(CoreUITabId.OUTPOSTS, null);
        }

        super.buttonPressConfirmed(buttonId, ui);
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add("Terraforming");
        return tags;
    }
}