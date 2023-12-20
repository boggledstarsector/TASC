package data.campaign.econ.abilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.campaign.econ.boggledTools;
import data.scripts.boggledTerraformingDialogPlugin;

public class Open_Terraforming_Control_Panel extends BaseDurationAbility
{
    public Open_Terraforming_Control_Panel() { }

    @Override
    protected void activateImpl()
    {
        Global.getSector().getCampaignUI().showInteractionDialog(new boggledTerraformingDialogPlugin(),null);
    }

    @Override
    public boolean isUsable()
    {
        if(!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled))
        {
            return false;
        }

        if(this.isOnCooldown() || this.disableFrames > 0)
        {
            return false;
        }

        return true;
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