package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.campaign.CampaignEntity;
import com.fs.starfarer.campaign.CampaignPlanet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class BoggledBaseTerraformingProject extends BaseIntelPlugin
{
    protected MarketAPI market;
    protected TerraformingProjectType projectType;
    private int daysCompleted = 0;
    private int lastDayChecked = 0;
    private final int requiredDaysToCompleteProject;

    public enum TerraformingProjectType {
        PLANET_TYPE_CHANGE, RESOURCE_IMPROVEMENT, CONDITION_IMPROVEMENT
    }

    public BoggledBaseTerraformingProject(MarketAPI market, TerraformingProjectType projectType)
    {
        this.market = market;
        this.projectType = projectType;
        this.requiredDaysToCompleteProject = switch (projectType) {
            case PLANET_TYPE_CHANGE -> boggledTools.getIntSetting("boggledTerraformingTime");
            case RESOURCE_IMPROVEMENT -> boggledTools.getIntSetting("boggledResourceImprovementTime");
            case CONDITION_IMPROVEMENT -> boggledTools.getIntSetting("boggledConditionImprovementTime");
        };
    }

    @Override
    public void advance(float amount)
    {
        super.advance(amount);

        CampaignClockAPI clock = Global.getSector().getClock();
        if(clock.getDay() != this.lastDayChecked)
        {
            this.daysCompleted++;
            this.lastDayChecked = clock.getDay();

            if(this.daysCompleted >= this.requiredDaysToCompleteProject)
            {
                this.completeThisProject();
            }
        }
    }

    public MarketAPI getMarket()
    {
        return this.market;
    }

    public int getDaysRemaining()
    {
        return this.requiredDaysToCompleteProject - this.daysCompleted;
    }

    public void completeThisProject()
    {

    }

    public HashSet<String> constructConditionsListAfterProjectCompletion()
    {
        HashSet<String> constructedConditions = new HashSet<>();
        for(MarketConditionAPI marketCondition : this.market.getConditions())
        {
            constructedConditions.add(marketCondition.getId());
        }

        for(String conditionToRemove : conditionsToRemoveUponCompletion())
        {
            constructedConditions.remove(conditionToRemove);
        }

        constructedConditions.addAll(conditionsToAddUponCompletion());

        return constructedConditions;
    }

    public ArrayList<String> conditionsToAddUponCompletion()
    {
        return new ArrayList<>();
    }

    public ArrayList<String> conditionsToRemoveUponCompletion()
    {
        return new ArrayList<>();
    }

    public CampaignPlanet constructFakePlanetWithAppearanceAfterTerraforming()
    {
        PlanetAPI marketPlanet = this.market.getPlanetEntity();
        return new CampaignPlanet(null, "constructedDummy", marketPlanet.getTypeId(), marketPlanet.getRadius(), marketPlanet.getLocation().x, marketPlanet.getLocation().y, (CampaignEntity) marketPlanet.getLightSource());
    }

    public String getProjectName()
    {
        return "Override this";
    }

    public static boolean isUnknownSkiesPlanetType(String str)
    {
        if (str == null || str.length() < 3) {
            return false;
        }

        // Use a case-insensitive check
        String prefix = str.substring(0, 3);
        return prefix.equalsIgnoreCase("us_");
    }

    public Triple<String, Boolean, TooltipMakerAPI.TooltipCreator> getRequirementWorldTypeAllowsTerraforming()
    {
        String tascPlanetType = boggledTools.getTascPlanetType(market.getPlanetEntity());
        String currentPlanetTypeDisplayString = boggledTools.getPlanetSpec(tascPlanetType).getName();
        Boolean worldTypeAllowsTerraforming = boggledTools.tascPlanetTypeAllowsTerraforming(tascPlanetType);
        TooltipMakerAPI.TooltipCreator tooltip = new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object o) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object o) {
                return 0;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {

            }
        };

        return new ArrayList<>(Arrays.asList("World type allows terraforming", worldTypeAllowsTerraforming, tooltip));
    }
}
