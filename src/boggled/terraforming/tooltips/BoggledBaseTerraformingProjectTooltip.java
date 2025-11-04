package boggled.terraforming.tooltips;

import boggled.terraforming.BoggledBaseTerraformingProject;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class BoggledBaseTerraformingProjectTooltip implements TooltipMakerAPI.TooltipCreator {
    private static final float tooltipWidth = 500;

    protected BoggledBaseTerraformingProject project;

    public BoggledBaseTerraformingProjectTooltip(BoggledBaseTerraformingProject project)
    {
        this.project = project;
    }
    @Override
    public boolean isTooltipExpandable(Object o) {
        return false;
    }

    @Override
    public float getTooltipWidth(Object o) {
        return tooltipWidth;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltipMakerAPI, boolean b, Object o) {
    }
}
