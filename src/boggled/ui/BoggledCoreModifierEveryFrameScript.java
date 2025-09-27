package boggled.ui;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.CutStyle;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import com.fs.starfarer.api.util.Pair;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BoggledCoreModifierEveryFrameScript implements EveryFrameScript {
    HashMap<ButtonAPI, Object> panelMap = null;
    ButtonAPI currentTab = null;

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final Class<?> methodClass;
    private static final MethodHandle invokeMethodHandle;

    static {
        try {
            methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
            invokeMethodHandle = lookup.findVirtual(methodClass, "invoke", MethodType.methodType(Object.class, Object.class, Object[].class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BoggledCoreModifierEveryFrameScript() { }

    public boolean isDone() {
        return false;
    }

    public static void setMemFlag(String value) {
        Global.getSector().getMemory().set("$aotd_outpost_state", value);
    }

    public boolean runWhilePaused() {
        return true;
    }

    public void advance(float amount)
    {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != null && Global.getSector().getCampaignUI().getCurrentCoreTab() == CoreUITabId.OUTPOSTS)
        {
            UIPanelAPI mainParent = getCurrentTab();
            if (mainParent != null)
            {

                if (tryToGetButtonProd("terraforming") == null)
                {
                    ButtonAPI aotdbutton = tryToGetButtonProd("research & production");
                    String buttonInsertNextTo = aotdbutton != null ? "research & production" : "custom production";
                    this.insertButton(tryToGetButtonProd(buttonInsertNextTo), mainParent, "terraforming", new TooltipMakerAPI.TooltipCreator() {
                        public boolean isTooltipExpandable(Object tooltipParam) {
                            return false;
                        }

                        public float getTooltipWidth(Object tooltipParam) {
                            return 500.0F;
                        }

                        public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                            tooltip.addSectionHeading("Terraforming & Station Construction", Alignment.MID, 0.0F);
                            tooltip.addPara("In this tab, you can find terraforming projects.", 5.0F);
                        }
                    }, tryToGetButtonProd("colonies"), 240.0F, 7, false);

                    //this.handleButtons();
                    //this.handleButtonsHighlight();
                }
            }
        }
    }

    public static UIPanelAPI getCoreUI() {
        CampaignUIAPI campaignUI = Global.getSector().getCampaignUI();
        InteractionDialogAPI dialog = campaignUI.getCurrentInteractionDialog();
        CoreUIAPI core;
        if (dialog == null) {
            core = (CoreUIAPI) invokeMethod("getCore", campaignUI, new Object[0]);
        } else {
            core = (CoreUIAPI) invokeMethod("getCoreUI", dialog, new Object[0]);
        }

        return core == null ? null : (UIPanelAPI)core;
    }

    public static UIPanelAPI getCurrentTab() {
        UIPanelAPI coreUltimate = getCoreUI();
        if (getCoreUI() == null) {
            return null;
        } else {
            return (UIPanelAPI) invokeMethod("getCurrentTab", coreUltimate, new Object[0]);
        }
    }

    public static Object invokeMethod(String methodName, Object instance, Object... arguments) {
        try {
            Object method = instance.getClass().getMethod(methodName);
            return invokeMethodHandle.invoke(method, instance, arguments);
        } catch (Throwable var4) {
            throw new RuntimeException(var4);
        }
    }

    public static ButtonAPI tryToGetButtonProd(String name) {
        try {
            Iterator var2 = getChildrenCopy( getCurrentTab()).iterator();

            while(var2.hasNext()) {
                UIComponentAPI componentAPI = (UIComponentAPI)var2.next();
                if (componentAPI instanceof ButtonAPI && ((ButtonAPI)componentAPI).getText().toLowerCase().contains(name)) {
                    return (ButtonAPI) componentAPI;
                }
            }

            return null;
        } catch (Exception var4) {
            return null;
        }
    }

    public static List<UIComponentAPI> getChildrenCopy(UIPanelAPI panel) {
        try {
            return (List)invokeMethod("getChildrenCopy", panel);
        } catch (Throwable var2) {
            return null;
        }
    }

    private void handleButtonsHighlight() {
        Iterator var1 = this.panelMap.keySet().iterator();

        while(var1.hasNext()) {
            ButtonAPI buttonAPI = (ButtonAPI)var1.next();
            if (!buttonAPI.equals(this.currentTab)) {
                buttonAPI.unhighlight();
            } else {
                buttonAPI.highlight();
            }
        }

    }

//    private void handleButtons() {
//        Iterator var1 = this.panelMap.keySet().iterator();
//
//        while(var1.hasNext()) {
//            ButtonAPI buttonAPI = (ButtonAPI)var1.next();
//            if (buttonAPI.isChecked()) {
//                buttonAPI.setChecked(false);
//                if (!this.currentTab.equals(buttonAPI)) {
//                    ProductionUtil.getCurrentTab().removeComponent((UIComponentAPI)this.panelMap.get(this.currentTab));
//                    if (buttonAPI.getText().toLowerCase().contains("terraforming")) {
//                        if (this.coreUiTech.getCurrentlyChosen() != null) {
//                            this.coreUiTech.playSound(this.coreUiTech.getCurrentlyChosen());
//                        }
//                    } else if (this.currentTab.getText().toLowerCase().contains("terraforming")) {
//                        this.coreUiTech.pauseSound();
//                    }
//
//                    this.currentTab = buttonAPI;
//                    setMemFlag(this.currentTab.getText().toLowerCase());
//                }
//            }
//        }
//
//    }

    private void insertButton(ButtonAPI button, UIPanelAPI mainParent, String name, TooltipMakerAPI.TooltipCreator creator, ButtonAPI button2, float size, int keyBind, boolean dissabled) {
        ButtonAPI newButton = (ButtonAPI)this.createPanelButton(name, size, button.getPosition().getHeight(), keyBind, dissabled, creator).two;
        mainParent.addComponent(newButton).inTL(button.getPosition().getX() + button.getPosition().getWidth() - button2.getPosition().getX() + 1.0F, 0.0F);
        mainParent.bringComponentToTop(newButton);
    }

    private Pair<CustomPanelAPI, ButtonAPI> createPanelButton(String buttonName, float width, float height, int bindingValue, boolean dissabled, TooltipMakerAPI.TooltipCreator onHoverTooltip) {
        CustomPanelAPI panel = Global.getSettings().createCustom(width, height, (CustomUIPanelPlugin)null);
        TooltipMakerAPI tooltipMakerAPI = panel.createUIElement(width, height, false);
        ButtonAPI button = tooltipMakerAPI.addButton(buttonName, (Object)null, Global.getSector().getPlayerFaction().getBaseUIColor(), Global.getSector().getPlayerFaction().getDarkUIColor(), Alignment.MID, CutStyle.TOP, width, height, 0.0F);
        button.setShortcut(bindingValue, false);
        button.setEnabled(!dissabled);
        if (onHoverTooltip != null) {
            tooltipMakerAPI.addTooltipToPrevious(onHoverTooltip, TooltipLocation.BELOW);
        }

        panel.addUIElement(tooltipMakerAPI).inTL(0.0F, 0.0F);
        return new Pair(panel, button);
    }
}
