package boggled.ui;

import boggled.campaign.econ.boggledTools;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.CutStyle;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.List;

public class BoggledCoreModifierEveryFrameScript implements EveryFrameScript {
    private HashMap<ButtonAPI, UIComponentAPI> outpostsButtonToPanelMapping = null;

    private Float rootX = null;
    private Float rootY = null;

    private ButtonAPI currentButton = null;

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final Class<?> methodClass;
    private static final MethodHandle invokeMethodHandle;

    private static MarketAPI marketToOpen = null;

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

    public static void setMarketToOpen(MarketAPI market)
    {
        marketToOpen = market;
    }

    private HashMap<ButtonAPI, UIComponentAPI> getOriginalPanelMap(UIPanelAPI mainCorePanel)
    {
        return (HashMap) invokeMethod("getButtonToTab", mainCorePanel, new Object[0]);
    }

    public static ButtonAPI getNewTerraformingButton()
    {
        // Create a dummy CustomPanelAPI and TooltipMakerAPI, so we can make a button, which then gets inserted directly into the core panel
        CustomPanelAPI dummyPanel = Global.getSettings().createCustom(240, 18, null);
        TooltipMakerAPI dummyTooltip = dummyPanel.createUIElement(240, 18, false);
        ButtonAPI terraformingButton = dummyTooltip.addButton("Terraforming",null, Global.getSector().getPlayerFaction().getBaseUIColor(), Global.getSector().getPlayerFaction().getDarkUIColor(), Alignment.MID, CutStyle.TOP, 240, 18, 0.0F);

        // Setting the shortcut to 7 actually binds it to the 6 key...
        terraformingButton.setShortcut(7, false);
        terraformingButton.setEnabled(true);

        // Create on hover tooltip for terraforming button and add it to the button
        TooltipMakerAPI.TooltipCreator terraformingButtonTooltip = new TooltipMakerAPI.TooltipCreator() {
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

            public float getTooltipWidth(Object tooltipParam) {
                return 500.0F;
            }

            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addSectionHeading("Terraforming & Station Construction", Alignment.MID, 0.0F);
                tooltip.addPara("Use this tab to manage terraforming projects on your colonies.", 5.0F);
            }
        };
        dummyTooltip.addTooltipToPrevious(terraformingButtonTooltip, TooltipLocation.BELOW);
        dummyPanel.addUIElement(dummyTooltip).inTL(0.0F, 0.0F);
        return terraformingButton;
    }

    public static void insertTerraformingButtonIntoCorePanel(UIPanelAPI mainCorePanel, ButtonAPI terraformingButton)
    {
        ButtonAPI coloniesButton = tryToGetButton("colonies");
        ButtonAPI aotdbutton = tryToGetButton("research & production");
        String buttonInsertNextTo = aotdbutton != null ? "research & production" : "custom production";
        ButtonAPI insertNextToButton = tryToGetButton(buttonInsertNextTo);

        // X location of the terraforming button is:
        // the X position of the button immediately to the left (in the panel, not absolute),
        // + the width of the button immediately to the left
        // - the absolute (not in panel) X position of the colonies button
        // + 1 as a spacer
        mainCorePanel.addComponent(terraformingButton).inTL(insertNextToButton.getPosition().getX() + insertNextToButton.getPosition().getWidth() - coloniesButton.getPosition().getX() + 1.0F, 0.0F);
        mainCorePanel.bringComponentToTop(terraformingButton);
    }

    private HashMap<ButtonAPI, UIComponentAPI> getModifiedPanelMapWithTerraformingMenu(HashMap<ButtonAPI, UIComponentAPI> originalMap, UIPanelAPI outpostsParentPanel)
    {
        ButtonAPI existingTerraformingButton = tryToGetButton("terraforming");
        if(existingTerraformingButton == null)
        {
            ButtonAPI terraformingButton = getNewTerraformingButton();
            insertTerraformingButtonIntoCorePanel(outpostsParentPanel, terraformingButton);
            originalMap.put(terraformingButton, null);

            BoggledTerraformingCoreUI terraformingUI = new BoggledTerraformingCoreUI();
            terraformingUI.init(marketToOpen);
            originalMap.put(terraformingButton, terraformingUI.getMainPanel());
        }

        return originalMap;
    }

    public static UIPanelAPI getMainCorePanel()
    {
        UIPanelAPI mainCorePanel = getCoreUI();
        return (UIPanelAPI) invokeMethod("getCurrentTab", mainCorePanel, new Object[0]);
    }

    public void advance(float amount)
    {
        if (Global.getSector().getCampaignUI().getCurrentCoreTab() != null && Global.getSector().getCampaignUI().getCurrentCoreTab() == CoreUITabId.OUTPOSTS)
        {
            UIPanelAPI mainParent = getMainCorePanel();
            if (mainParent != null)
            {
                // Get the root coordinates of the Colonies tab in the top left.
                // Used later to know where to insert the panels.
                if(rootX == null || rootY == null)
                {
                    HashMap<ButtonAPI, UIComponentAPI> originalMap = getOriginalPanelMap(mainParent);
                    ButtonAPI coloniesButton = tryToGetButton("colonies");
                    UIComponentAPI coloniesPanel = originalMap.get(coloniesButton);
                    rootX = coloniesPanel.getPosition().getX();
                    rootY = coloniesPanel.getPosition().getY();
                }

                // If terraforming button isn't on the screen, create it and add it to the map.
                HashMap<ButtonAPI, UIComponentAPI> originalMap = getOriginalPanelMap(mainParent);
                this.outpostsButtonToPanelMapping = getModifiedPanelMapWithTerraformingMenu(originalMap, mainParent);
                ButtonAPI terraformingButton = tryToGetButton("terraforming");

                // If the user opened the CoreUI from the colony management industry tooltip, switch to the terraforming menu immediately
                boggledTools.writeMessageToLog("Market to open is: " + (marketToOpen != null ? marketToOpen.getName() : "null"));
                if(marketToOpen != null)
                {
                    uncheckButtons();
                    unhighlightButtons();
                    terraformingButton.setChecked(true);
                    marketToOpen = null;
                }

                ButtonAPI checkedButton = getCheckedButton();
                if(checkedButton != null)
                {
                    uncheckButtons();
                    unhighlightButtons();
                    checkedButton.highlight();
                }

                // If the highlighted button panel isn't already active, switch to it
                ButtonAPI highlightedButton = getHighlightedButton();
                if(highlightedButton != null && highlightedButton != this.currentButton)
                {
                    removeAllTabPanels(mainParent, null);
                    if(highlightedButton.getText().contains("Terraforming"))
                    {
                        mainParent.addComponent(this.outpostsButtonToPanelMapping.get(terraformingButton)).inTL(0f, 30f);
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

    private void uncheckButtons()
    {
        for(ButtonAPI button : this.outpostsButtonToPanelMapping.keySet())
        {
            button.setChecked(false);
        }
    }

    private void unhighlightButtons()
    {
        for(ButtonAPI button : this.outpostsButtonToPanelMapping.keySet())
        {
            button.unhighlight();
        }
    }

    private void removeAllTabPanels(UIPanelAPI mainParent, ButtonAPI keepButton)
    {
        for(ButtonAPI button : this.outpostsButtonToPanelMapping.keySet())
        {
            if(keepButton == null || button != keepButton)
            {
                UIComponentAPI panelToRemove = this.outpostsButtonToPanelMapping.get(button);
                if(panelToRemove != null)
                {
                    mainParent.removeComponent(panelToRemove);
                    // panelToRemove.getPosition().setLocation(10000, 0);
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

    public static Object invokeMethod(String methodName, Object instance, Object... arguments)
    {
        try
        {
            Object method = instance.getClass().getMethod(methodName);
            return invokeMethodHandle.invoke(method, instance, arguments);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public static ButtonAPI tryToGetButton(String name) {
        try
        {
            for(UIComponentAPI component : getChildrenCopy(getMainCorePanel()))
            {
                if (component instanceof ButtonAPI && ((ButtonAPI)component).getText().toLowerCase().contains(name))
                {
                    return (ButtonAPI) component;
                }
            }

            return null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static List<UIComponentAPI> getChildrenCopy(UIPanelAPI panel)
    {
        try
        {
            return (List)invokeMethod("getChildrenCopy", panel);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
