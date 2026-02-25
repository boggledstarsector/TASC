package boggled.ui;

import ashlib.data.plugins.coreui.CommandTabListener;
import ashlib.data.plugins.coreui.CommandUIPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import org.lwjgl.input.Keyboard;

public class TascAshlibCommandTabListener implements CommandTabListener {
    @Override
    public String getNameForTab() {
        return "Terraforming";
    }

    @Override
    public String getButtonToReplace() {
        return null;
    }

    @Override
    public String getButtonToBePlacedNear() {
        return "custom production";
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getTooltipCreatorForButton() {
        return new TooltipMakerAPI.TooltipCreator() {
            @Override
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 500;
            }

            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addSectionHeading("TASC", Alignment.MID,0f);
                tooltip.addPara("Use this tab to manage terraforming projects on your colonies.",5f);
            }
        };
    }


    @Override
    public CommandUIPlugin createPlugin() {
        return new BoggledTerraformingCoreUIAshlib();
    }

    @Override
    public float getWidthOfButton() {
        return 170;
    }

    @Override
    public int getKeyBind() {
        return  Keyboard.KEY_6;
    }

    @Override
    public void performRecalculations(UIComponentAPI uiPanelAPI) { }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public boolean shouldButtonBeEnabled() {
        return true;
    }

    @Override
    public void performRefresh(ButtonAPI buttonAPI) { }
}