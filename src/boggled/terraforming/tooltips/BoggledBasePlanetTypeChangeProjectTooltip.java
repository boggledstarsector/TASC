package boggled.terraforming.tooltips;

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
        tooltipMakerAPI.addPara("%s worlds start with %s, %s and %s.", 10, Misc.getTextColor(), Misc.getHighlightColor(), new String[]{
                boggledTools.tascPlanetTypeDisplayStringMap.get(boggledTools.getTascPlanetType(this.terraformingProject.getPlanetIdToChangeInto())).one,
                (getSecondWord(this.terraformingProject.getBaseFarmlandId()) != null ? getSecondWord(this.terraformingProject.getBaseFarmlandId()) : "no") + " farmland",
                (getSecondWord(this.terraformingProject.getBaseOrganics()) != null ? getSecondWord(this.terraformingProject.getBaseOrganics()) : "no") + " organics",
                (getSecondWord(this.terraformingProject.getBaseVolatiles()) != null ? getSecondWord(this.terraformingProject.getBaseVolatiles()) : "no") + " volatiles"
        });

        int max_farmland_int = boggledTools.getMaxFarmlandLevelForTascPlanetType(boggledTools.getTascPlanetType(this.terraformingProject.getPlanetIdToChangeInto()));
        String max_farmland = boggledTools.getFarmlandConditionIdForInteger(max_farmland_int);
        int max_organics_int = boggledTools.getMaxOrganicsLevelForTascPlanetType(boggledTools.getTascPlanetType(this.terraformingProject.getPlanetIdToChangeInto()));
        String max_organics = boggledTools.getOrganicsConditionIdForInteger(max_organics_int);
        int max_volatiles_int = boggledTools.getMaxVolatilesLevelForTascPlanetType(boggledTools.getTascPlanetType(this.terraformingProject.getPlanetIdToChangeInto()));
        String max_volatiles = boggledTools.getVolatilesConditionIdForInteger(max_volatiles_int);
        tooltipMakerAPI.addPara("These amounts can be improved with further projects up to maximums of %s, %s and %s.", 10, Misc.getTextColor(), Misc.getHighlightColor(), new String[]{
                (getSecondWord(max_farmland) != null ? getSecondWord(max_farmland) : "no") + " farmland",
                (getSecondWord(max_organics) != null ? getSecondWord(max_organics) : "no") + " organics",
                (getSecondWord(max_volatiles) != null ? getSecondWord(max_volatiles) : "no") + " volatiles"
        });

        tooltipMakerAPI.addPara("Ore deposits are never modified by terraforming projects.", 10, Misc.getTextColor(), Misc.getHighlightColor(), new String[]{});
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
