package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import boggled.campaign.econ.conditions.Terraforming_Controller;
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
import com.fs.starfarer.campaign.CampaignPlanet;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BoggledBaseTerraformingProject extends BaseIntelPlugin {

    protected MarketAPI market;
    protected TerraformingProjectType projectType;
    private int daysCompleted = 0;
    private int lastDayChecked = 0;
    private final int requiredDaysToCompleteProject;

    private static String BUTTON_OPEN_TERRAFORMING_MENU = "Open terraforming menu";

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
        sendUpdateNotificationToPlayer(ProjectUpdateType.STARTED);
        boggledTools.addCondition(market, boggledTools.BoggledConditions.terraformingControllerConditionId);
        Terraforming_Controller controller = boggledTools.getTerraformingControllerFromMarket(this.market);
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

        return new TerraformingRequirementObject(this.market.getName() + " is habitable for humans", requirementMet, tooltip);
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

        return new TerraformingRequirementObject(this.market.getName() + " has at least a moderate amount of water", requirementMet, tooltip);
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

        return new TerraformingRequirementObject(this.market.getName() + " has a large amount of water", requirementMet, tooltip);
    }

    public static String getModId() {
        return boggledTools.BoggledMods.tascModId;
    }

    public static boolean isEnabledViaSettings() {
        return true;
    }

    public String getTascPlanetTypeDisplayString(String tascPlanetType)
    {
        // Volcanic planet type id is actually lava in vanilla
        if(tascPlanetType.equals(boggledTools.TascPlanetTypes.volcanicPlanetId)) {
            return "Volcanic";
        }
        // arid is the planet ID we're switching into for desert TASC type
        else if(tascPlanetType.equals(boggledTools.TascPlanetTypes.desertPlanetId)) {
            return "Desert";
        }
        else
        {
            return boggledTools.getPlanetSpec(tascPlanetType).getName();
        }
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
            info.addTooltipToPrevious(requirement.tooltip, TooltipMakerAPI.TooltipLocation.ABOVE,false);
            firstRow = false;
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