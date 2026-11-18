package boggled.terraforming.tooltips;

import boggled.campaign.econ.StringHelper;
import boggled.campaign.econ.boggledTools;
import boggled.terraforming.BoggledBaseTerraformingPlanetTypeChangeProject;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BoggledBasePlanetTypeChangeProjectTooltip extends BoggledBaseTerraformingProjectTooltip {

    protected BoggledBaseTerraformingPlanetTypeChangeProject terraformingProject;
    public BoggledBasePlanetTypeChangeProjectTooltip(BoggledBaseTerraformingPlanetTypeChangeProject project)
    {
        super(project);
        this.terraformingProject = project;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
        tooltipMakerAPI.setTitleFont(Fonts.ORBITRON_12);
        tooltipMakerAPI.setTitleFontColor(Misc.getPositiveHighlightColor());
        tooltipMakerAPI.addTitle(this.terraformingProject.getProjectName());
        String planetType = Misc.ucFirst(boggledTools.getPlanetTypeDisplayName(boggledTools.getTascPlanetType(this.terraformingProject.getPlanetIdToChangeInto())));
        String farmland = depositPhrase(this.terraformingProject.getBaseFarmlandId(), "farmland");
        String organics = depositPhrase(this.terraformingProject.getBaseOrganics(), "organics");
        String volatiles = depositPhrase(this.terraformingProject.getBaseVolatiles(), "volatiles");
        String starts = StringHelper.getString(StringHelper.TERRAFORMING, "type_change_starts_with", StringHelper.tokens("planetType", planetType, "farmland", farmland, "organics", organics, "volatiles", volatiles));
        StringHelper.applyHighlight(
                tooltipMakerAPI.addPara(starts, Misc.getTextColor(), 10f),
                Misc.getHighlightColor(),
                planetType,
                farmland,
                organics,
                volatiles);

        int max_farmland_int = boggledTools.getMaxFarmlandLevelForTascPlanetType(boggledTools.getTascPlanetType(this.terraformingProject.getPlanetIdToChangeInto()));
        String max_farmland = boggledTools.getFarmlandConditionIdForInteger(max_farmland_int);
        int max_organics_int = boggledTools.getMaxOrganicsLevelForTascPlanetType(boggledTools.getTascPlanetType(this.terraformingProject.getPlanetIdToChangeInto()));
        String max_organics = boggledTools.getOrganicsConditionIdForInteger(max_organics_int);
        int max_volatiles_int = boggledTools.getMaxVolatilesLevelForTascPlanetType(boggledTools.getTascPlanetType(this.terraformingProject.getPlanetIdToChangeInto()));
        String max_volatiles = boggledTools.getVolatilesConditionIdForInteger(max_volatiles_int);
        String maxFarmlandPhrase = depositPhrase(max_farmland, "farmland");
        String maxOrganicsPhrase = depositPhrase(max_organics, "organics");
        String maxVolatilesPhrase = depositPhrase(max_volatiles, "volatiles");
        String maximums = StringHelper.getString(StringHelper.TERRAFORMING, "type_change_maximums", StringHelper.tokens("farmland", maxFarmlandPhrase, "organics", maxOrganicsPhrase, "volatiles", maxVolatilesPhrase));
        StringHelper.applyHighlight(
                tooltipMakerAPI.addPara(maximums, Misc.getTextColor(), 10f),
                Misc.getHighlightColor(),
                maxFarmlandPhrase,
                maxOrganicsPhrase,
                maxVolatilesPhrase);

        tooltipMakerAPI.addPara(StringHelper.getString(StringHelper.TERRAFORMING, "ore_deposits_unchanged"), Misc.getTextColor(), 10f);
    }

    private static String depositPhrase(String conditionId, String resource) {
        String level = getSecondWord(conditionId);
        if (level == null) {
            level = "no";
        }
        return StringHelper.getString(StringHelper.TERRAFORMING, "deposit_" + resource + "_" + level);
    }

    public static String getSecondWord(String input) {
        if(input == null)
        {
            return null;
        }

        // Split the string by the underscore
        String[] parts = input.split("_");

        // Return the second element
        return parts[1];
    }
}
