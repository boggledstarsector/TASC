package evangel.tascui;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.campaign.econ.MarketAPI
import lunalib.lunaExtensions.openLunaCustomPanel;

class CommandUIAbilityK : BaseDurationAbility() {
    @Override
    override fun isUsable() : Boolean {
        if (!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled)) {
            return false;
        }
        return !this.isOnCooldown && this.disableFrames <= 0;
    }

    @Override
    override fun hasTooltip() : Boolean { return true; }

    @Override
    override fun isTooltipExpandable() : Boolean { return false; }

    @Override
    override fun createTooltip(tooltip : TooltipMakerAPI, expanded : Boolean) {
        tooltip.addTitle("Open Terraforming Control Panel")
    }

    @Override
    override fun activateImpl() {
        Global.getSector().openLunaCustomPanel(CommandUIIntelK())
    }

    companion object
    {
        fun openTerraformingMenuForSpecificPlanet(market: MarketAPI)
        {
            boggledTools.setTerraformingMenuTarget(market)
            Global.getSector().openLunaCustomPanel(CommandUIIntelK())
        }
    }

    @Override
    override fun applyEffect(amount : Float, level : Float) {
    }

    @Override
    override fun deactivateImpl() {
    }

    @Override
    override fun cleanupImpl() {
    }
}
