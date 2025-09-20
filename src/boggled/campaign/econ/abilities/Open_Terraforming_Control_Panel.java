package boggled.campaign.econ.abilities;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
// import lunalib.lunaExtensions.DialogExtensionsKt;
// import evangel.tascui.CommandUIIntelK;

public class Open_Terraforming_Control_Panel extends BaseDurationAbility
{
    public Open_Terraforming_Control_Panel() { }

    @Override
    protected void activateImpl()
    {
        // Doesn't work apparently, will fix in next patch when I redo the terraforming control panel.
        // DialogExtensionsKt.openLunaCustomPanel(new CommandUIIntelK());
        // Global.getSector().openLunaCustomPanel(CommandUIIntelK())
    }

    @Override
    public boolean isUsable()
    {
        if(!boggledTools.isResearched("tasc_atmosphere_manipulation"))
        {
            return false;
        }

        if(!boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            return false;
        }

        return super.isUsable();
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded)
    {
        tooltip.addTitle("Open Terraforming Control Panel");
    }

    @Override
    public boolean isTooltipExpandable() {
        return false;
    }

    @Override
    protected void applyEffect(float v, float v1) { }

    @Override
    protected void deactivateImpl() { }

    @Override
    protected void cleanupImpl() { }
}