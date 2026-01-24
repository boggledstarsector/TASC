package boggled.terraforming;

import boggled.campaign.econ.boggledTools;
import boggled.terraforming.tooltips.BoggledBasePlanetTypeChangeProjectTooltip;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.campaign.CampaignEntity;
import com.fs.starfarer.campaign.CampaignPlanet;
import java.util.ArrayList;
import java.util.Arrays;

public class BoggledBaseTerraformingPlanetTypeChangeProject extends BoggledBaseTerraformingProject
{
    private final String planetIdToChangeInto;
    public BoggledBaseTerraformingPlanetTypeChangeProject(MarketAPI market, String planetIdToChangeInto)
    {
        super(market, TerraformingProjectType.PLANET_TYPE_CHANGE);
        this.planetIdToChangeInto = planetIdToChangeInto;
    }

    @Override
    public void completeThisProject()
    {
        // Remove conditions
        for(String conditionId : conditionsToRemoveUponCompletion())
        {
            boggledTools.removeCondition(this.market, conditionId);
        }

        // Add conditions
        for(String conditionId : conditionsToAddUponCompletion())
        {
            boggledTools.addCondition(this.market, conditionId);
        }

        // Change planet type (using vanilla method)
        this.market.getPlanetEntity().changeType(this.planetIdToChangeInto, null);

        super.completeThisProject();
    }

    @Override
    public ArrayList<String> conditionsToRemoveUponCompletion()
    {
        ArrayList<String> conditionsToRemove = new ArrayList<>(Arrays.asList(
                "habitable",
                "no_atmosphere",
                "thin_atmosphere",
                "toxic_atmosphere",
                "dense_atmosphere",
                "mild_climate",
                "extreme_weather",
                "inimical_biosphere",
                "water_surface",
                "volatiles_trace",
                "volatiles_diffuse",
                "volatiles_abundant",
                "volatiles_plentiful",
                "organics_trace",
                "organics_common",
                "organics_abundant",
                "organics_plentiful",
                "farmland_poor",
                "farmland_adequate",
                "farmland_rich",
                "farmland_bountiful",
                "US_crash",
                "US_storm"
        ));

        // Keep lobsters if planet goes from water -> water (only possible with Unknown Skies or other modded planet types added)
        if(!boggledTools.getTascPlanetType(this.planetIdToChangeInto).equals(boggledTools.TascPlanetTypes.waterPlanetId))
        {
            conditionsToRemove.add("volturnian_lobster_pens");
        }

        // Remove US conditions that seem to require habitable land to realistically remain present
        if(!boggledTools.tascPlanetTypeSupportsFarmland(boggledTools.getTascPlanetType(this.getPlanetIdToChangeInto())))
        {
            conditionsToRemove.add("US_mind");
            conditionsToRemove.add("US_shrooms");
        }

        // Remove US Sakura
        conditionsToRemove.add("US_sakura");

        // Remove US Hybrid (water planet production bonus)
        conditionsToRemove.add("US_hybrid");

        return conditionsToRemove;
    }

    @Override
    public ArrayList<String> conditionsToAddUponCompletion()
    {
        ArrayList<String> conditionsToAdd = new ArrayList<>();

        // Water planets must have water surface
        if(boggledTools.getTascPlanetType(this.planetIdToChangeInto).equals(boggledTools.TascPlanetTypes.waterPlanetId))
        {
            conditionsToAdd.add("water_surface");
        }

        // Frozen planets aren't habitable
        if(!boggledTools.getTascPlanetType(this.planetIdToChangeInto).equals(boggledTools.TascPlanetTypes.frozenPlanetId))
        {
            conditionsToAdd.add("habitable");
        }

        // Frozen planets and water planets don't have farmland
        if(!boggledTools.getTascPlanetType(this.planetIdToChangeInto).equals(boggledTools.TascPlanetTypes.frozenPlanetId) && !boggledTools.getTascPlanetType(this.planetIdToChangeInto).equals(boggledTools.TascPlanetTypes.waterPlanetId))
        {
            conditionsToAdd.add(getBaseFarmlandId());
        }

        // Handle Mesozoic Park inimical biosphere
        if(this.market.hasIndustry("BOGGLED_MESOZOIC_PARK"))
        {
            if(boggledTools.getTascPlanetType(this.planetIdToChangeInto).equals(boggledTools.TascPlanetTypes.terranPlanetId) || boggledTools.getTascPlanetType(this.planetIdToChangeInto).equals(boggledTools.TascPlanetTypes.waterPlanetId) || boggledTools.getTascPlanetType(this.planetIdToChangeInto).equals(boggledTools.TascPlanetTypes.junglePlanetId) || boggledTools.getTascPlanetType(this.planetIdToChangeInto).equals(boggledTools.TascPlanetTypes.desertPlanetId))
            {
                conditionsToAdd.add("inimical_biosphere");
            }
        }

        // Add organics
        String organicsId = getBaseOrganics();
        if(organicsId != null)
        {
            conditionsToAdd.add(organicsId);
        }

        // Add volatiles
        String volatilesId = getBaseVolatiles();
        if(volatilesId != null)
        {
            // Don't add volatiles to terran or tundra worlds if setting to do so has been toggled to false
            if(boggledTools.getBooleanSetting("boggledTerraformingTypeChangeAddVolatiles") || (!this.planetIdToChangeInto.equals(boggledTools.TascPlanetTypes.terranPlanetId) && !this.planetIdToChangeInto.equals(boggledTools.TascPlanetTypes.tundraPlanetId)))
            {
                conditionsToAdd.add(volatilesId);
            }
        }

        return conditionsToAdd;
    }

    public String getBaseFarmlandId()
    {
        return boggledTools.getConditionIdForBaseFarmlandLevelForTascPlanetType(boggledTools.getTascPlanetType(this.planetIdToChangeInto));
    }

    public String getBaseOrganics()
    {
        return boggledTools.getConditionIdForBaseOrganicsLevelForTascPlanetType(boggledTools.getTascPlanetType(this.planetIdToChangeInto));
    }

    public String getBaseVolatiles()
    {
        return boggledTools.getConditionIdForBaseVolatilesLevelForTascPlanetType(boggledTools.getTascPlanetType(this.planetIdToChangeInto));
    }

    @Override
    public CampaignPlanet constructFakePlanetWithAppearanceAfterTerraforming()
    {
        PlanetAPI marketPlanet = this.market.getPlanetEntity();

        // Apparently the light source can sometimes be an object that can't be cast to CampaignEntity.
        // I suspect this happens in binary/trinary systems but I'm not sure. It might be some mod interaction.
        // First check if the light source can be cast to CampaignEntity, if not, check if the system exists
        // and has a primary star that can be cast to CampaignEntity. Otherwise pass null.
        // Passing null results in the light source coming from a seemingly random angle instead of the same angle
        // as the light source hitting the "original" planet visual in the left pane.
        CampaignEntity campaignEntityLightSource = null;
        if (marketPlanet.getLightSource() instanceof CampaignEntity) {
            campaignEntityLightSource = (CampaignEntity) marketPlanet.getLightSource();
        }
        else if(marketPlanet.getStarSystem() != null && marketPlanet.getStarSystem().getStar() != null)
        {
            if(marketPlanet.getStarSystem().getStar() instanceof CampaignEntity) {
                campaignEntityLightSource = (CampaignEntity) marketPlanet.getStarSystem().getStar();
            }
        }

        return new CampaignPlanet(null, "constructedDummy", this.planetIdToChangeInto, marketPlanet.getRadius(), marketPlanet.getLocation().x, marketPlanet.getLocation().y, campaignEntityLightSource);
    }

    @Override
    public String getProjectName()
    {
        String projectName = boggledTools.getPlanetSpec(planetIdToChangeInto).getName();
        if(isUnknownSkiesPlanetType(planetIdToChangeInto))
        {
            projectName += " (Unknown Skies)";
        }
        return projectName + " Type Change";
    }

    public TerraformingRequirementObject getRequirementNotAlreadyTargetType()
    {
        String tascPlanetType = boggledTools.getTascPlanetType(this.market.getPlanetEntity());
        String tascPlanetTypeToChangeInto = boggledTools.getTascPlanetType(this.planetIdToChangeInto);
        String planetTypeDisplayString = boggledTools.tascPlanetTypeDisplayStringMap.get(tascPlanetTypeToChangeInto).two;
        Boolean requirementMet = !tascPlanetType.equals(tascPlanetTypeToChangeInto);
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
                tooltipMakerAPI.addPara("Dummy text here - not already target type",10f);
            }
        };

        return new TerraformingRequirementObject(this.market.getName() + " is not already a " + planetTypeDisplayString + " world", requirementMet, null);
    }

    @Override
    public ArrayList<TerraformingRequirementObject> getProjectRequirements()
    {
        ArrayList<TerraformingRequirementObject> projectRequirements = super.getProjectRequirements();
        projectRequirements.add(getRequirementWorldTypeAllowsTerraforming());
        projectRequirements.add(getRequirementNotAlreadyTargetType());
        projectRequirements.add(getRequirementAtmosphericDensityNormal());

        return projectRequirements;
    }

    public String getPlanetIdToChangeInto()
    {
        return this.planetIdToChangeInto;
    }

    public BoggledBasePlanetTypeChangeProjectTooltip getProjectTooltip()
    {
        return new BoggledBasePlanetTypeChangeProjectTooltip(this);
    }
}
