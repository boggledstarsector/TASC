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
    private HashMap<ButtonAPI, UIComponentAPI> outpostsButtonToPanelMapping = null;

    private Float rootX = null;
    private Float rootY = null;

    private ButtonAPI currentButton = null;

    private Float rootXForModdedPanels = null;
    private Float rootYForModdedPanels = null;
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

    public boolean runWhilePaused() {
        return true;
    }

    private HashMap<ButtonAPI, UIComponentAPI> getOriginalPanelMap(UIPanelAPI outpostsMainPanel)
    {
        return (HashMap) invokeMethod("getButtonToTab", outpostsMainPanel, new Object[0]);
    }

    private HashMap<ButtonAPI, UIComponentAPI> getModifiedPanelMapWithTerraformingMenu(HashMap<ButtonAPI, UIComponentAPI> originalMap, UIPanelAPI outpostsParentPanel)
    {
        ButtonAPI existingTerraformingButton = tryToGetButtonProd("terraforming");

        if(existingTerraformingButton == null)
        {
            ButtonAPI aotdbutton = tryToGetButtonProd("research & production");
            String buttonInsertNextTo = aotdbutton != null ? "research & production" : "custom production";
            ButtonAPI terraformingButton = this.insertButton(tryToGetButtonProd(buttonInsertNextTo), outpostsParentPanel, "Terraforming", new TooltipMakerAPI.TooltipCreator() {
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

            existingTerraformingButton = terraformingButton;
            BoggledTerraformingCoreUI terraformingUI = new BoggledTerraformingCoreUI();
            terraformingUI.init(null);
            originalMap.put(existingTerraformingButton, terraformingUI.getMainPanel());
        }

        return originalMap;
    }

    public static UIPanelAPI getCurrentTab() {
        UIPanelAPI coreUltimate = getCoreUI();
        if (getCoreUI() == null) {
            return null;
        } else {
            return (UIPanelAPI) invokeMethod("getCurrentTab", coreUltimate, new Object[0]);
        }
    }

    public void advance(float amount)
    {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != null && Global.getSector().getCampaignUI().getCurrentCoreTab() == CoreUITabId.OUTPOSTS)
        {
            UIPanelAPI mainParent = getCurrentTab();
            if (mainParent != null)
            {
                if(rootX == null || rootY == null)
                {
                    HashMap<ButtonAPI, UIComponentAPI> originalMap = getOriginalPanelMap(mainParent);
                    ButtonAPI coloniesButton = tryToGetButtonProd("colonies");
                    UIComponentAPI coloniesPanel = originalMap.get(coloniesButton);
                    rootX = coloniesPanel.getPosition().getX();
                    rootY = coloniesPanel.getPosition().getY();
                }

                HashMap<ButtonAPI, UIComponentAPI> originalMap = getOriginalPanelMap(mainParent);
                this.outpostsButtonToPanelMapping = getModifiedPanelMapWithTerraformingMenu(originalMap, mainParent);

                ButtonAPI checkedButton = getCheckedButton();
                if(checkedButton != null)
                {
                    for(ButtonAPI button : this.outpostsButtonToPanelMapping.keySet())
                    {
                        if(button.isChecked())
                        {
                            button.setChecked(false);
                        }

                        if(button.isHighlighted())
                        {
                            button.unhighlight();
                        }

                    }

                    checkedButton.highlight();
                }

                ButtonAPI highlightedButton = getHighlightedButton();
                removeAllTabPanels(mainParent, highlightedButton);
                if(highlightedButton != null && this.outpostsButtonToPanelMapping.containsKey(highlightedButton))
                {
                    if(this.currentButton != highlightedButton)
                    {
                        if(highlightedButton.getText().contains("Terraforming"))
                        {
                            BoggledTerraformingCoreUI terraformingUI = new BoggledTerraformingCoreUI();
                            terraformingUI.init(null);
                            this.outpostsButtonToPanelMapping.put(highlightedButton, terraformingUI.getMainPanel());
                            UIComponentAPI componentToAdd = this.outpostsButtonToPanelMapping.get(highlightedButton);
                            mainParent.addComponent(componentToAdd).inTL(0f, 35f);
                        }
                        else
                        {
                            UIComponentAPI componentToAdd = this.outpostsButtonToPanelMapping.get(highlightedButton);
                            mainParent.addComponent(componentToAdd).setLocation(rootX, rootY);
                        }
                        this.currentButton = highlightedButton;
                    }
                }
            }
        }
    }

    private ButtonAPI getCheckedButton()
    {
        for(ButtonAPI button : this.outpostsButtonToPanelMapping.keySet())
        {
            if(button.isChecked())
            {
                return button;
            }
        }

        return null;
    }

    private void removeAllTabPanels(UIPanelAPI mainParent, ButtonAPI keepButton)
    {
        for(ButtonAPI button : this.outpostsButtonToPanelMapping.keySet())
        {
            if(keepButton == null || button != keepButton)
            {
                mainParent.removeComponent(this.outpostsButtonToPanelMapping.get(button));
            }
        }
    }

    private ButtonAPI getHighlightedButton()
    {
        for(ButtonAPI button : this.outpostsButtonToPanelMapping.keySet())
        {
            if(button.isHighlighted())
            {
                return button;
            }
        }

        return null;
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

    private ButtonAPI insertButton(ButtonAPI button, UIPanelAPI mainParent, String name, TooltipMakerAPI.TooltipCreator creator, ButtonAPI button2, float size, int keyBind, boolean dissabled) {
        ButtonAPI newButton = (ButtonAPI)this.createPanelButton(name, size, button.getPosition().getHeight(), keyBind, dissabled, creator).two;
        mainParent.addComponent(newButton).inTL(button.getPosition().getX() + button.getPosition().getWidth() - button2.getPosition().getX() + 1.0F, 0.0F);
        mainParent.bringComponentToTop(newButton);
        return newButton;
    }

    private Pair<CustomPanelAPI, ButtonAPI> createPanelButton(String buttonName, float width, float height, int bindingValue, boolean dissabled, TooltipMakerAPI.TooltipCreator onHoverTooltip) {
        CustomPanelAPI panel = Global.getSettings().createCustom(width, height, (CustomUIPanelPlugin)null);
        TooltipMakerAPI tooltipMakerAPI = panel.createUIElement(width, height, false);
        ButtonAPI button = tooltipMakerAPI.addButton(buttonName, (Object)null, Global.getSector().getPlayerFaction().getBaseUIColor(), Global.getSector().getPlayerFaction().getDarkUIColor(), Alignment.MID, CutStyle.TOP, width, height, 0.0F);
        button.setShortcut(bindingValue, false);
        button.setEnabled(true);
        if (onHoverTooltip != null) {
            tooltipMakerAPI.addTooltipToPrevious(onHoverTooltip, TooltipLocation.BELOW);
        }

        panel.addUIElement(tooltipMakerAPI).inTL(0.0F, 0.0F);
        return new Pair(panel, button);
    }
}
