package boggled.terraforming;

import boggled.campaign.econ.StringHelper;
import boggled.campaign.econ.boggledTools;
import boggled.campaign.econ.conditions.Terraforming_Controller;
import boggled.campaign.econ.industries.Boggled_Atmosphere_Processor;
import boggled.terraforming.tooltips.BoggledBaseTerraformingProjectTooltip;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
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
        put(ProjectUpdateType.STARTED, StringHelper.getString(StringHelper.INTEL, "update_started"));
        put(ProjectUpdateType.STALLED, StringHelper.getString(StringHelper.INTEL, "update_stalled"));
        put(ProjectUpdateType.RESUMED, StringHelper.getString(StringHelper.INTEL, "update_resumed"));
        put(ProjectUpdateType.COMPLETED, StringHelper.getString(StringHelper.INTEL, "update_completed"));
        put(ProjectUpdateType.CANCELLED, StringHelper.getString(StringHelper.INTEL, "update_cancelled"));
    }};

    private void refreshUpdateMessageMap() {
        updateMessageMap.put(ProjectUpdateType.STARTED, StringHelper.getString(StringHelper.INTEL, "update_started"));
        updateMessageMap.put(ProjectUpdateType.STALLED, StringHelper.getString(StringHelper.INTEL, "update_stalled"));
        updateMessageMap.put(ProjectUpdateType.RESUMED, StringHelper.getString(StringHelper.INTEL, "update_resumed"));
        updateMessageMap.put(ProjectUpdateType.COMPLETED, StringHelper.getString(StringHelper.INTEL, "update_completed"));
        updateMessageMap.put(ProjectUpdateType.CANCELLED, StringHelper.getString(StringHelper.INTEL, "update_cancelled"));
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
        Global.getSector().addScript(this);
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
        refreshUpdateMessageMap();
        String title = StringHelper.getString(StringHelper.INTEL, "message_title", StringHelper.tokens("project", this.getProjectName(), "market", this.market.getName()));
        MessageIntel intel = new MessageIntel(title, Misc.getBasePlayerColor());
        intel.addLine(updateMessageMap.get(type));
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

    protected String terraformingText(String id) {
        return StringHelper.getString(StringHelper.TERRAFORMING, id);
    }

    protected String marketRequirement(String id) {
        return StringHelper.getString(StringHelper.TERRAFORMING, id, StringHelper.tokens("market", this.market.getName()));
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
                tooltipMakerAPI.addTitle(terraformingText("req_world_type_allows"));
                String planetTypeName = boggledTools.getPlanetTypeDisplayName(boggledTools.getTascPlanetType(market.getPlanetEntity()));
                String worldTypeSentence = StringHelper.getString(StringHelper.TERRAFORMING,
                        worldTypeAllowsTerraforming ? "world_type_allows_can" : "world_type_allows_cannot",
                        StringHelper.tokens("market", market.getName(), "planetType", planetTypeName));
                StringHelper.applyHighlight(
                        tooltipMakerAPI.addPara(worldTypeSentence, Misc.getTextColor(), 10f),
                        Misc.getHighlightColor(),
                        market.getName(),
                        planetTypeName,
                        Misc.ucFirst(planetTypeName));

                if(boggledTools.getTascPlanetType(market.getPlanetEntity()).equals(boggledTools.TascPlanetTypes.unknownPlanetId))
                {
                    tooltipMakerAPI.addPara(StringHelper.getString(StringHelper.TERRAFORMING, "unsupported_planet_type", StringHelper.tokens("typeId", market.getPlanetEntity().getTypeId())), 10);
                }
            }
        };

        return new TerraformingRequirementObject(terraformingText("req_world_type_allows"), worldTypeAllowsTerraforming, tooltip);
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

        return new TerraformingRequirementObject(terraformingText("req_world_can_be_habitable"), worldTypeAllowsTerraforming, null);
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

        return new TerraformingRequirementObject(marketRequirement("req_is_habitable"), requirementMet, null);
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

        return new TerraformingRequirementObject(marketRequirement("req_standard_atmosphere"), requirementMet, null);
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

        return new TerraformingRequirementObject(marketRequirement("req_not_toxic_or_irradiated"), requirementMet, null);
    }

    public TerraformingRequirementObject getRequirementMarketHasAtmosphereProcessor() {
        Industry atmosphereProcessor = this.market.getIndustry(boggledTools.BoggledIndustries.atmosphereProcessorId);
        boolean requirementMet = atmosphereProcessor != null && atmosphereProcessor.isFunctional() && atmosphereProcessor instanceof Boggled_Atmosphere_Processor && (((Boggled_Atmosphere_Processor) atmosphereProcessor).getAtmosphereProcessorDeficit().two <= 0);
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

        return new TerraformingRequirementObject(marketRequirement("req_has_atmosphere_processor"), requirementMet, null);
    }

    protected boolean isAtmosphereProcessorBuildingEnabled() {
        return boggledTools.getBooleanSetting(boggledTools.BoggledSettings.atmosphereProcessorEnabled);
    }

    protected boolean isIsmaraSlingBuildingEnabled() {
        return boggledTools.getBooleanSetting(boggledTools.BoggledSettings.ismaraSlingEnabled);
    }

    protected boolean isStellarReflectorArrayBuildingEnabled() {
        return boggledTools.getBooleanSetting(boggledTools.BoggledSettings.stellarReflectorArrayEnabled);
    }

    public TerraformingRequirementObject getRequirementMarketHasStellarReflectorArray() {
        Industry stellarReflectorArray = this.market.getIndustry(boggledTools.BoggledIndustries.stellarReflectorArrayIndustryId);
        boolean requirementMet = stellarReflectorArray != null && stellarReflectorArray.isFunctional();
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

        return new TerraformingRequirementObject(marketRequirement("req_has_stellar_reflector"), requirementMet, null);
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

        String researchKey = switch (projectType) {
            case PLANET_TYPE_CHANGE -> "research_planet_type_manipulation";
            case RESOURCE_IMPROVEMENT -> "research_resource_manipulation";
            case CONDITION_IMPROVEMENT -> "research_atmosphere_manipulation";
        };
        String researchName = terraformingText(researchKey);
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

        return new TerraformingRequirementObject(StringHelper.getString(StringHelper.TERRAFORMING, "research_project_completed", StringHelper.tokens("research", researchName)), requirementMet, tooltip);
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

        return new TerraformingRequirementObject(terraformingText("req_temperature_very_cold"), requirementMet, tooltip);
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

        return new TerraformingRequirementObject(terraformingText("req_temperature_temperate_or_cold"), requirementMet, null);
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

        return new TerraformingRequirementObject(terraformingText("req_temperature_temperate_or_hot"), requirementMet, null);
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

        return new TerraformingRequirementObject(terraformingText("req_temperature_not_very_hot_or_very_cold"), requirementMet, null);
    }

    public TerraformingRequirementObject getRequirementMarketHasModerateWater()
    {
        boggledTools.PlanetWaterLevel currentWaterLevel = boggledTools.getWaterLevelForMarket(this.market);
        boolean requirementMet = currentWaterLevel == boggledTools.PlanetWaterLevel.HIGH_WATER || currentWaterLevel == boggledTools.PlanetWaterLevel.MEDIUM_WATER;
        String title = marketRequirement("req_moderate_water");

        BoggledBaseTerraformingProjectTooltip tooltip = new BoggledBaseTerraformingProjectTooltip(this) {
            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                super.createTooltip(tooltipMakerAPI, b, o);
                addWaterLevelTooltip(tooltipMakerAPI, requirementMet, title);
            }
        };

        return new TerraformingRequirementObject(title, requirementMet, tooltip);
    }

    public TerraformingRequirementObject getRequirementMarketHasHighWater()
    {
        boggledTools.PlanetWaterLevel currentWaterLevel = boggledTools.getWaterLevelForMarket(this.market);
        boolean requirementMet = currentWaterLevel == boggledTools.PlanetWaterLevel.HIGH_WATER;
        String title = marketRequirement("req_large_water");

        BoggledBaseTerraformingProjectTooltip tooltip = new BoggledBaseTerraformingProjectTooltip(this) {
            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
                super.createTooltip(tooltipMakerAPI, b, o);
                addWaterLevelTooltip(tooltipMakerAPI, requirementMet, title);
            }
        };

        return new TerraformingRequirementObject(title, requirementMet, tooltip);
    }

    private String baseWaterAmountWord()
    {
        return switch (boggledTools.getBaseWaterLevelForTascPlanetType(boggledTools.getTascPlanetType(market.getPlanetEntity())))
        {
            case LOW_WATER -> terraformingText("water_amount_low");
            case MEDIUM_WATER -> terraformingText("water_amount_moderate");
            case HIGH_WATER -> terraformingText("water_amount_large");
        };
    }

    private void addWaterLevelTooltip(TooltipMakerAPI tooltipMakerAPI, boolean requirementMet, String title)
    {
        tooltipMakerAPI.setTitleFont(Fonts.ORBITRON_12);
        tooltipMakerAPI.setTitleFontColor(requirementMet ? Misc.getPositiveHighlightColor(): Misc.getNegativeHighlightColor());
        tooltipMakerAPI.addTitle(title);
        String planetTypeName = boggledTools.getPlanetTypeDisplayName(boggledTools.getTascPlanetType(market.getPlanetEntity()));
        String waterString = baseWaterAmountWord();
        String considered = StringHelper.getString(StringHelper.TERRAFORMING, "water_base_present", StringHelper.tokens("market", market.getName(), "planetType", planetTypeName, "amount", waterString));
        StringHelper.applyHighlight(
                tooltipMakerAPI.addPara(considered, Misc.getTextColor(), 10f),
                Misc.getHighlightColor(),
                market.getName(),
                planetTypeName,
                Misc.ucFirst(planetTypeName),
                waterString);
        String large = terraformingText("water_amount_large");
        String increased = StringHelper.getString(StringHelper.TERRAFORMING, "water_can_be_increased", StringHelper.tokens("amount", large));
        StringHelper.applyHighlight(
                tooltipMakerAPI.addPara(increased, Misc.getTextColor(), 10f),
                Misc.getHighlightColor(),
                large);

        ArrayList<Pair<MarketAPI, boggledTools.WaterIndustryStatus>> waterStatusList = boggledTools.getWaterIndustryStatusForSystemPlayerMarketsOnly(market.getStarSystem());
        waterStatusList.sort(Comparator.comparing(BoggledBaseTerraformingProject::getSortOrderForWaterStatus));
        if(!waterStatusList.isEmpty())
        {
            tooltipMakerAPI.addPara(terraformingText("water_colonies_with_buildings"), Misc.getTextColor(), 10f);
            for(Pair<MarketAPI, boggledTools.WaterIndustryStatus> marketStatus : waterStatusList)
            {
                String statusKey = null;
                Color color = null;
                if(marketStatus.two == boggledTools.WaterIndustryStatus.OPERATIONAL)
                {
                    statusKey = "status_operational";
                    color = Misc.getPositiveHighlightColor();
                }
                else if(marketStatus.two == boggledTools.WaterIndustryStatus.DISRUPTED)
                {
                    statusKey = "status_disrupted";
                    color = Misc.getNegativeHighlightColor();
                }
                else if(marketStatus.two == boggledTools.WaterIndustryStatus.SHORTAGE)
                {
                    statusKey = "status_shortage";
                    color = Misc.getNegativeHighlightColor();
                }
                else if(marketStatus.two == boggledTools.WaterIndustryStatus.UNDER_CONSTRUCTION)
                {
                    statusKey = "status_under_construction";
                    color = Misc.getNegativeHighlightColor();
                }
                if(statusKey != null)
                {
                    String status = StringHelper.getString(StringHelper.GENERAL, statusKey);
                    String line = StringHelper.getString(StringHelper.TERRAFORMING, "water_colony_status", StringHelper.tokens("market", marketStatus.one.getName(), "status", status));
                    tooltipMakerAPI.addPara(line, color, 2f);
                }
            }
        }
        else
        {
            tooltipMakerAPI.addPara(terraformingText("water_no_colonies"), Misc.getTextColor(), 10f);
        }
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
        return StringHelper.getString(StringHelper.INTEL, "market_project_title", StringHelper.tokens("market", this.market.getName()));
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("boggled_intel_icons", "intel_icon_terraforming");
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
        String projectName = this.getProjectName();
        String projectLine = StringHelper.getString(StringHelper.INTEL, "bullet_project", StringHelper.tokens("project", projectName));
        StringHelper.applyHighlight(info.addPara(projectLine, tc, initPad), playerColor, projectName);

        if(requirementsMet(getProjectRequirements()))
        {
            String days = String.valueOf(getDaysRemaining());
            String dayKey = getDaysRemaining() == 1 ? "bullet_day_remaining" : "bullet_days_remaining";
            String dayLine = StringHelper.getString(StringHelper.INTEL, dayKey, StringHelper.tokens("days", days));
            StringHelper.applyHighlight(info.addPara(dayLine, tc, initPad), highlight, days);
        }
        else
        {
            info.addPara(StringHelper.getString(StringHelper.INTEL, "bullet_stalled"), bad, initPad);
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
        return StringHelper.getString(StringHelper.INTEL, "market_project_title", StringHelper.tokens("market", this.market.getName()));
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
        String projectName = this.getProjectName();
        String projectLine = StringHelper.getString(StringHelper.INTEL, "bullet_project", StringHelper.tokens("project", projectName));
        StringHelper.applyHighlight(info.addPara(projectLine, tc, opad), highlight, projectName);
        String days = String.valueOf(getDaysRemaining());
        String dayKey = getDaysRemaining() == 1 ? "desc_day_remaining" : "desc_days_remaining";
        String dayLine = StringHelper.getString(StringHelper.INTEL, dayKey, StringHelper.tokens("days", days));
        StringHelper.applyHighlight(info.addPara(dayLine, tc, pad), highlight, days);
        if(!requirementsMet(getProjectRequirements()))
        {
            info.addPara(StringHelper.getString(StringHelper.INTEL, "desc_stalled"), tc, pad);
        }

        // Print requirements
        info.addSectionHeading(StringHelper.getString(StringHelper.INTEL, "project_requirements"), Alignment.MID, opad);

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

        info.addButton(StringHelper.getString(StringHelper.INTEL, "open_terraforming_menu"), BUTTON_OPEN_TERRAFORMING_MENU, this.getFactionForUIColors().getBaseUIColor(), this.getFactionForUIColors().getDarkUIColor(), (float)((int)width), 20.0F, opad * 1.0F);
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if (buttonId == BUTTON_OPEN_TERRAFORMING_MENU)
        {
            MarketAPI targetMarket = this.market;
            boggledTools.setDefaultMarketToOpenForTerraformingTab(targetMarket);
            boggledTools.openCommandTabWithTerraformingSelected();
        }

        super.buttonPressConfirmed(buttonId, ui);
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(StringHelper.getString(StringHelper.INTEL, "intel_tag"));
        return tags;
    }
}