package boggled.ui;

import boggled.campaign.econ.StringHelper;
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
    private boolean isDone = false;
    private HashMap<ButtonAPI, UIComponentAPI> outpostsButtonToPanelMapping = null;

    private Float rootX = null;
    private Float rootY = null;

    private ButtonAPI currentButton = null;
    private ButtonAPI terraformingButton = null;

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
        return this.isDone;
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
        CustomPanelAPI dummyPanel = Global.getSettings().createCustom(170, 18, null);
        TooltipMakerAPI dummyTooltip = dummyPanel.createUIElement(170, 18, false);
        ButtonAPI terraformingButton = dummyTooltip.addButton(StringHelper.getString(StringHelper.UI, "terraforming_tab"), null, Global.getSector().getPlayerFaction().getBaseUIColor(), Global.getSector().getPlayerFaction().getDarkUIColor(), Alignment.MID, CutStyle.TOP, 170, 18, 0.0F);

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
                tooltip.addSectionHeading(StringHelper.getString(StringHelper.UI, "terraforming_tab_tooltip_heading"), Alignment.MID, 0.0F);
                tooltip.addPara(StringHelper.getString(StringHelper.UI, "terraforming_tab_tooltip"), 5.0F);
            }
        };
        dummyTooltip.addTooltipToPrevious(terraformingButtonTooltip, TooltipLocation.BELOW);
        dummyPanel.addUIElement(dummyTooltip).inTL(0.0F, 0.0F);
        return terraformingButton;
    }

    public static boolean insertTerraformingButtonIntoCorePanel(UIPanelAPI mainCorePanel, ButtonAPI terraformingButton)
    {
        List<UIComponentAPI> children = getChildrenCopy(mainCorePanel);
        if (children == null || terraformingButton == null || terraformingButton.getPosition() == null) {
            return false;
        }

        ButtonAPI leftmost = null;
        ButtonAPI rightmost = null;
        float minX = Float.POSITIVE_INFINITY;
        float maxRight = Float.NEGATIVE_INFINITY;
        for (UIComponentAPI component : children) {
            if (!(component instanceof ButtonAPI button) || button.getPosition() == null) {
                continue;
            }
            float x = button.getPosition().getX();
            float right = x + button.getPosition().getWidth();
            if (x < minX) {
                minX = x;
                leftmost = button;
            }
            if (right > maxRight) {
                maxRight = right;
                rightmost = button;
            }
        }
        if (leftmost == null || rightmost == null || leftmost.getPosition() == null || rightmost.getPosition() == null) {
            return false;
        }

        // Place the button at the end of the existing row. The leftmost button is the coordinate origin.
        float x = rightmost.getPosition().getX() + rightmost.getPosition().getWidth() - leftmost.getPosition().getX() + 1.0F;
        mainCorePanel.addComponent(terraformingButton).inTL(x, 0.0F);
        mainCorePanel.bringComponentToTop(terraformingButton);
        return true;
    }

    private HashMap<ButtonAPI, UIComponentAPI> getModifiedPanelMapWithTerraformingMenu(HashMap<ButtonAPI, UIComponentAPI> originalMap, UIPanelAPI outpostsParentPanel)
    {
        if (!isButtonOnPanel(outpostsParentPanel, this.terraformingButton)) {
            this.terraformingButton = getNewTerraformingButton();
            if (!insertTerraformingButtonIntoCorePanel(outpostsParentPanel, this.terraformingButton)) {
                this.terraformingButton = null;
                return originalMap;
            }
            BoggledTerraformingCoreUI terraformingUI = new BoggledTerraformingCoreUI();
            terraformingUI.init(marketToOpen);
            originalMap.put(this.terraformingButton, terraformingUI.getMainPanel());
        } else if (!originalMap.containsKey(this.terraformingButton)) {
            originalMap.put(this.terraformingButton, null);
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
        if(Global.getSettings().getModManager().isModEnabled("ashlib"))
        {
            boggledTools.writeMessageToLog("BoggledCoreModifierEveryFrameScript disabled because Ashlib is active.");
            this.isDone = true;
            return;
        }

        if(boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled) && Global.getSector().getCampaignUI().getCurrentCoreTab() != null && Global.getSector().getCampaignUI().getCurrentCoreTab() == CoreUITabId.OUTPOSTS)
        {
            UIPanelAPI mainParent = getMainCorePanel();
            if (mainParent != null)
            {
                // If we can't get the map using reflection without an exception being thrown,
                // that means we're not on the main menu with the tabs on top.
                // This happens if you click intel that tries to open the colony management screen.
                HashMap<ButtonAPI, UIComponentAPI> originalMap;
                try { originalMap = getOriginalPanelMap(mainParent); }
                catch(Exception e) { return; }

                // Root coordinates come from the leftmost command-row button's panel.
                if (rootX == null || rootY == null) {
                    ButtonAPI anchorButton = leftmostButton(originalMap.keySet());
                    UIComponentAPI anchorPanel = anchorButton == null ? null : originalMap.get(anchorButton);
                    if (anchorPanel == null || anchorPanel.getPosition() == null) {
                        return;
                    }
                    rootX = anchorPanel.getPosition().getX();
                    rootY = anchorPanel.getPosition().getY();
                }

                // If terraforming button isn't on the screen, create it and add it to the map.
                this.outpostsButtonToPanelMapping = getModifiedPanelMapWithTerraformingMenu(originalMap, mainParent);
                if (this.terraformingButton == null) {
                    return;
                }

                // If the user opened the CoreUI from the colony management industry tooltip, switch to the terraforming menu immediately
                // boggledTools.writeMessageToLog("Market to open is: " + (marketToOpen != null ? marketToOpen.getName() : "null"));
                MarketAPI marketTemp = marketToOpen;
                if(marketToOpen != null)
                {
                    uncheckButtons();
                    unhighlightButtons();
                    this.terraformingButton.setChecked(true);
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
                    if(highlightedButton == this.terraformingButton)
                    {
                        BoggledTerraformingCoreUI terraformingUI = new BoggledTerraformingCoreUI();
                        terraformingUI.init(marketTemp);
                        this.outpostsButtonToPanelMapping.put(highlightedButton, terraformingUI.getMainPanel());
                        UIComponentAPI componentToAdd = this.outpostsButtonToPanelMapping.get(highlightedButton);
                        mainParent.addComponent(componentToAdd).inTL(0f, 30f);
                    }
                    else
                    {
                        UIComponentAPI componentToAdd = this.outpostsButtonToPanelMapping.get(highlightedButton);
                        mainParent.addComponent(componentToAdd).setLocation(rootX, rootY);
                        // componentToAdd.getPosition().inTL(rootX, rootY);
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
                    // panelToRemove.getPosition().inTL(10000, 0);
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

    private static boolean isButtonOnPanel(UIPanelAPI panel, ButtonAPI button) {
        if (panel == null || button == null) {
            return false;
        }
        List<UIComponentAPI> children = getChildrenCopy(panel);
        return children != null && children.contains(button);
    }

    private static ButtonAPI leftmostButton(Iterable<ButtonAPI> buttons) {
        ButtonAPI best = null;
        float minX = Float.POSITIVE_INFINITY;
        for (ButtonAPI button : buttons) {
            if (button == null || button.getPosition() == null) {
                continue;
            }
            float x = button.getPosition().getX();
            if (x < minX) {
                minX = x;
                best = button;
            }
        }
        return best;
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
