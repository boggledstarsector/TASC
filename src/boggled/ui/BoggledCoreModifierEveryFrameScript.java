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
import org.lwjgl.input.Keyboard;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class BoggledCoreModifierEveryFrameScript implements EveryFrameScript {
    private boolean isDone = false;
    private HashMap<ButtonAPI, UIComponentAPI> outpostsButtonToPanelMapping = null;

    private Float rootX = null;
    private Float rootY = null;

    private ButtonAPI currentButton = null;
    private ButtonAPI terraformingButton = null;
    private int terraformingTabIndex = -1;

    private static BoggledCoreModifierEveryFrameScript active;

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

    public BoggledCoreModifierEveryFrameScript() {
        active = this;
    }

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

    public static void onOutpostsTabOpened()
    {
        if (active == null || active.isDone)
        {
            return;
        }
        if (Global.getSettings().getModManager().isModEnabled("ashlib"))
        {
            return;
        }
        if (!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled))
        {
            return;
        }
        active.ensureTerraformingButton();
    }

    private HashMap<ButtonAPI, UIComponentAPI> getOriginalPanelMap(UIPanelAPI mainCorePanel)
    {
        return (HashMap) invokeMethod("getButtonToTab", mainCorePanel, new Object[0]);
    }

    public static int shortcutForNewCommandButton(UIPanelAPI panel)
    {
        if (panel == null)
        {
            return Keyboard.KEY_NONE;
        }
        return shortcutForIndex(commandButtons(panel).size());
    }

    public static int shortcutForIndex(int index)
    {
        if (index < 0 || index > 9)
        {
            return Keyboard.KEY_NONE;
        }
        // KEY_1 through KEY_0 are contiguous. Index 0 is the Colonies slot.
        return Keyboard.KEY_1 + index;
    }

    public static ButtonAPI getNewTerraformingButton(int tabIndex)
    {
        // Create a dummy CustomPanelAPI and TooltipMakerAPI, so we can make a button, which then gets inserted directly into the core panel
        CustomPanelAPI dummyPanel = Global.getSettings().createCustom(170, 18, null);
        TooltipMakerAPI dummyTooltip = dummyPanel.createUIElement(170, 18, false);
        ButtonAPI terraformingButton = dummyTooltip.addButton(StringHelper.getString(StringHelper.UI, "terraforming_tab"), null, Global.getSector().getPlayerFaction().getBaseUIColor(), Global.getSector().getPlayerFaction().getDarkUIColor(), Alignment.MID, CutStyle.TOP, 170, 18, 0.0F);

        terraformingButton.setShortcut(shortcutForIndex(tabIndex), false);
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
        List<ButtonAPI> buttons = commandButtons(mainCorePanel);
        if (buttons.isEmpty() || terraformingButton == null || terraformingButton.getPosition() == null) {
            return false;
        }

        ButtonAPI rightmost = null;
        float maxRight = Float.NEGATIVE_INFINITY;
        for (ButtonAPI button : buttons) {
            float right = button.getPosition().getX() + button.getPosition().getWidth();
            if (right > maxRight) {
                maxRight = right;
                rightmost = button;
            }
        }
        if (rightmost == null) {
            return false;
        }

        mainCorePanel.addComponent(terraformingButton).rightOfTop(rightmost, 1.0F);
        mainCorePanel.bringComponentToTop(terraformingButton);
        return true;
    }

    private HashMap<ButtonAPI, UIComponentAPI> getModifiedPanelMapWithTerraformingMenu(HashMap<ButtonAPI, UIComponentAPI> originalMap, UIPanelAPI outpostsParentPanel)
    {
        if (!isButtonOnPanel(outpostsParentPanel, this.terraformingButton)) {
            int tabIndex = commandButtons(outpostsParentPanel).size();
            this.terraformingButton = getNewTerraformingButton(tabIndex);
            this.terraformingTabIndex = tabIndex;
            if (!insertTerraformingButtonIntoCorePanel(outpostsParentPanel, this.terraformingButton)) {
                this.terraformingButton = null;
                this.terraformingTabIndex = -1;
                return originalMap;
            }
            BoggledTerraformingCoreUI terraformingUI = new BoggledTerraformingCoreUI();
            terraformingUI.init(marketToOpen);
            originalMap.put(this.terraformingButton, terraformingUI.getMainPanel());
        } else if (!originalMap.containsKey(this.terraformingButton)) {
            originalMap.put(this.terraformingButton, null);
            if (this.terraformingTabIndex < 0) {
                this.terraformingTabIndex = indexOfButton(outpostsParentPanel, this.terraformingButton);
            }
        }

        return originalMap;
    }

    public static UIPanelAPI getMainCorePanel()
    {
        UIPanelAPI mainCorePanel = getCoreUI();
        if (mainCorePanel == null)
        {
            return null;
        }
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

        if (!boggledTools.getBooleanSetting(boggledTools.BoggledSettings.terraformingContentEnabled))
        {
            return;
        }

        CoreUITabId coreTab = Global.getSector().getCampaignUI().getCurrentCoreTab();
        if (coreTab != CoreUITabId.OUTPOSTS)
        {
            return;
        }

        if (!ensureTerraformingButton())
        {
            return;
        }

        UIPanelAPI mainParent = getMainCorePanel();
        if (mainParent == null)
        {
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
        if (checkedButton != null)
        {
            int checkedIndex = indexOfButton(mainParent, checkedButton);
            if (checkedIndex < 0 && checkedButton == this.terraformingButton)
            {
                checkedIndex = this.terraformingTabIndex;
            }
            if (checkedIndex >= 0)
            {
                setSelectedTabIndex(checkedIndex);
            }
        }

        int selectedIndex = getSelectedTabIndex();
        if (checkedButton == null && selectedIndex == this.terraformingTabIndex && this.currentButton != this.terraformingButton)
        {
            ButtonAPI highlighted = getHighlightedButton();
            if (highlighted != null && highlighted != this.terraformingButton)
            {
                int highlightedIndex = indexOfButton(mainParent, highlighted);
                if (highlightedIndex >= 0)
                {
                    setSelectedTabIndex(highlightedIndex);
                    selectedIndex = highlightedIndex;
                }
            }
        }
        if (this.terraformingTabIndex >= 0 && selectedIndex >= 0 && selectedIndex != this.terraformingTabIndex)
        {
            hideTerraformingPanel(mainParent);
            if (!mappedButtonHasIndex(mainParent, selectedIndex))
            {
                return;
            }
        }

        checkedButton = getCheckedButton();
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

    private boolean ensureTerraformingButton()
    {
        UIPanelAPI mainParent = getMainCorePanel();
        if (mainParent == null)
        {
            return false;
        }

        // If we can't get the map using reflection without an exception being thrown,
        // that means we're not on the main menu with the tabs on top.
        // This happens if you click intel that tries to open the colony management screen.
        HashMap<ButtonAPI, UIComponentAPI> originalMap;
        try { originalMap = getOriginalPanelMap(mainParent); }
        catch(Exception e) { return false; }

        // Root coordinates come from the leftmost command-row button's panel.
        if (rootX == null || rootY == null) {
            ButtonAPI anchorButton = leftmostButton(originalMap.keySet());
            UIComponentAPI anchorPanel = anchorButton == null ? null : originalMap.get(anchorButton);
            if (anchorPanel == null || anchorPanel.getPosition() == null) {
                return false;
            }
            rootX = anchorPanel.getPosition().getX();
            rootY = anchorPanel.getPosition().getY();
        }

        // If terraforming button isn't on the screen, create it and add it to the map.
        this.outpostsButtonToPanelMapping = getModifiedPanelMapWithTerraformingMenu(originalMap, mainParent);
        return this.terraformingButton != null && this.outpostsButtonToPanelMapping != null;
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

    private void hideTerraformingPanel(UIPanelAPI mainParent)
    {
        if (this.terraformingButton == null || this.outpostsButtonToPanelMapping == null)
        {
            return;
        }
        UIComponentAPI panel = this.outpostsButtonToPanelMapping.get(this.terraformingButton);
        if (panel != null && isComponentOnPanel(mainParent, panel))
        {
            mainParent.removeComponent(panel);
        }
        this.terraformingButton.setChecked(false);
        this.terraformingButton.unhighlight();
        if (this.currentButton == this.terraformingButton)
        {
            this.currentButton = null;
        }
    }

    private boolean mappedButtonHasIndex(UIPanelAPI panel, int index)
    {
        if (this.outpostsButtonToPanelMapping == null)
        {
            return false;
        }
        for (ButtonAPI button : this.outpostsButtonToPanelMapping.keySet())
        {
            if (indexOfButton(panel, button) == index)
            {
                return true;
            }
        }
        return false;
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

    private static Object commandTabData()
    {
        try
        {
            Object uiData = Global.getSector().getUIData();
            if (uiData == null)
            {
                return null;
            }
            return invokeMethod("getCommandData", uiData);
        }
        catch (RuntimeException e)
        {
            return null;
        }
    }

    private static int getSelectedTabIndex()
    {
        Object data = commandTabData();
        if (data == null)
        {
            return -1;
        }
        Object value = invokeMethod("getSelectedTabIndex", data);
        return value instanceof Integer integer ? integer : -1;
    }

    private static void setSelectedTabIndex(int index)
    {
        Object data = commandTabData();
        if (data == null)
        {
            return;
        }
        try
        {
            Object method = data.getClass().getMethod("setSelectedTabIndex", int.class);
            invokeMethodHandle.invoke(method, data, new Object[] { index });
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    private static boolean isButtonOnPanel(UIPanelAPI panel, ButtonAPI button) {
        return isComponentOnPanel(panel, button);
    }

    private static boolean isComponentOnPanel(UIPanelAPI panel, UIComponentAPI component) {
        if (panel == null || component == null) {
            return false;
        }
        List<UIComponentAPI> children = getChildrenCopy(panel);
        if (children == null) {
            return false;
        }
        for (UIComponentAPI child : children) {
            if (child == component || unwrap(child) == component) {
                return true;
            }
        }
        return false;
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

    private static List<ButtonAPI> commandButtons(UIPanelAPI panel)
    {
        List<ButtonAPI> buttons = new ArrayList<>();
        List<UIComponentAPI> children = getChildrenCopy(panel);
        if (children == null)
        {
            return buttons;
        }
        for (UIComponentAPI child : children)
        {
            UIComponentAPI unwrapped = unwrap(child);
            if (unwrapped instanceof ButtonAPI button && button.getPosition() != null && !buttons.contains(button))
            {
                buttons.add(button);
            }
        }
        return buttons;
    }

    private static int indexOfButton(UIPanelAPI panel, ButtonAPI button)
    {
        if (button == null)
        {
            return -1;
        }
        List<ButtonAPI> buttons = commandButtons(panel);
        buttons.sort(Comparator.comparingDouble(candidate -> candidate.getPosition().getX()));
        for (int i = 0; i < buttons.size(); i++)
        {
            if (buttons.get(i) == button)
            {
                return i;
            }
        }
        return -1;
    }

    private static UIComponentAPI unwrap(UIComponentAPI component)
    {
        if (component instanceof ButtonAPI || component == null)
        {
            return component;
        }
        for (Method method : component.getClass().getMethods())
        {
            String name = method.getName();
            if ((!name.equals("getWrapped") && !name.equals("wrapped")) || method.getParameterCount() != 0)
            {
                continue;
            }
            if (!UIComponentAPI.class.isAssignableFrom(method.getReturnType()))
            {
                continue;
            }
            try
            {
                Object wrapped = invokeMethodHandle.invoke(method, component, new Object[0]);
                if (wrapped instanceof UIComponentAPI ui && ui != component)
                {
                    return unwrap(ui);
                }
            }
            catch (Throwable ignored)
            {
                return component;
            }
        }
        return component;
    }

    public static List<UIComponentAPI> getChildrenCopy(UIPanelAPI panel)
    {
        if (panel == null)
        {
            return null;
        }
        try
        {
            return panel.getChildrenCopy2();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
