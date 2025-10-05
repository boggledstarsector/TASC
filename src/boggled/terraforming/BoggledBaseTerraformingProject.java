package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.campaign.CampaignPlanet;

import java.util.ArrayList;
import java.util.HashSet;

public class BoggledBaseTerraformingProject extends BaseIntelPlugin {
    private boolean done = false;

    protected MarketAPI market;
    protected TerraformingProjectType projectType;
    private int daysCompleted = 0;
    private int lastDayChecked = 0;
    private final int requiredDaysToCompleteProject;

    public enum TerraformingProjectType {
        PLANET_TYPE_CHANGE, RESOURCE_IMPROVEMENT, CONDITION_IMPROVEMENT
    }

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
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        boggledTools.writeMessageToLog("Triggered advance in base terraforming project.");

        CampaignClockAPI clock = Global.getSector().getClock();
        if (clock.getDay() != this.lastDayChecked) {
            // Avoid calling requirementsMet every frame because it does a lot of calculations
            boolean requirementsMet = requirementsMet(getProjectRequirements());
            if (requirementsMet) {
                this.daysCompleted++;
                this.lastDayChecked = clock.getDay();

                if (this.daysCompleted >= this.requiredDaysToCompleteProject) {
                    completeThisProject();
                }
            } else {
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

    public void completeThisProject() {
        boggledTools.sendDebugIntelMessage("Project completed!");
        this.done = true;
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
                tooltipMakerAPI.addPara("Dummy text here - world type allows terraforming", 10f);
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
                tooltipMakerAPI.addPara("Dummy text here - world type allows human habitability", 10f);
            }
        };

        return new TerraformingRequirementObject("World type can be habitable for humans", worldTypeAllowsTerraforming, tooltip);
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
                tooltipMakerAPI.addPara("Dummy text here - market is habitable", 10f);
            }
        };

        return new TerraformingRequirementObject("Colony is habitable for humans", requirementMet, tooltip);
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
                tooltipMakerAPI.addPara("Dummy text here - atmo problem", 10f);
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " has standard atmospheric density", requirementMet, tooltip);
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

        return new TerraformingRequirementObject(this.market.getName() + " does not have a toxic or irradiated atmosphere", requirementMet, tooltip);
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

        return new TerraformingRequirementObject(this.market.getName() + " has an atmosphere processor", requirementMet, tooltip);
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

        return new TerraformingRequirementObject(this.market.getName() + " has a stellar reflector array", requirementMet, tooltip);
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

        return new TerraformingRequirementObject("Temperature is temperate or cold", requirementMet, tooltip);
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

        return new TerraformingRequirementObject("Temperature is temperate or hot", requirementMet, tooltip);
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

        return new TerraformingRequirementObject("Temperature is not very hot or very cold", requirementMet, tooltip);
    }

    public TerraformingRequirementObject getRequirementMarketHasModerateWater()
    {
        boggledTools.PlanetWaterLevel currentWaterLevel = boggledTools.getWaterLevelForMarket(this.market);
        boolean requirementMet = currentWaterLevel == boggledTools.PlanetWaterLevel.HIGH_WATER || currentWaterLevel == boggledTools.PlanetWaterLevel.MEDIUM_WATER;

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
                tooltipMakerAPI.addPara("Dummy text here - Moderate water",10f);
            }
        };

        return new TerraformingRequirementObject("Colony has at least a moderate amount of water", requirementMet, tooltip);
    }

    public TerraformingRequirementObject getRequirementMarketHasHighWater()
    {
        boggledTools.PlanetWaterLevel currentWaterLevel = boggledTools.getWaterLevelForMarket(this.market);
        boolean requirementMet = currentWaterLevel == boggledTools.PlanetWaterLevel.HIGH_WATER;

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
                tooltipMakerAPI.addPara("Dummy text here - High water",10f);
            }
        };

        return new TerraformingRequirementObject("Colony has a large amount of water", requirementMet, tooltip);
    }

    public static String getModId() {
        return boggledTools.BoggledMods.tascModId;
    }

    public static boolean isEnabledViaSettings() {
        return true;
    }

    public String getCurrentPlanetTypeDisplayString()
    {
        // Volcanic planet type id is actually lava in vanilla
        String tascPlanetType = boggledTools.getTascPlanetType(market.getPlanetEntity());
        if (tascPlanetType.equals(boggledTools.TascPlanetTypes.volcanicPlanetId)) {
            return "Volcanic";
        }
        else
        {
            return boggledTools.getPlanetSpec(tascPlanetType).getName();
        }
    }
}