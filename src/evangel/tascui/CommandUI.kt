package evangel.tascui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.*
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import com.fs.starfarer.api.util.Misc
import boggled.campaign.econ.boggledTools
import boggled.campaign.econ.conditions.Terraforming_Controller
import boggled.scripts.BoggledTerraformingProject
import boggled.scripts.BoggledTerraformingRequirement.RequirementContext
import lunalib.lunaUI.elements.LunaElement
import lunalib.lunaUI.elements.LunaSpriteElement
import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin
import org.apache.log4j.LogManager
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

class StaticTooltip(title : String?, condition : MarketConditionAPI?, vararg strings : String) : TooltipCreator {
    private val title : String?
    private val paras : ArrayList<String> = ArrayList()
    private val condition : MarketConditionAPI?

    init {
        this.title = title
        this.condition = condition
        for (string in strings) {
            paras.add(string)
        }
    }

    override fun isTooltipExpandable(tooltipParam : Any?) : Boolean {
        return false
    }

    override fun getTooltipWidth(tooltipParam : Any?) : Float {
        return 500f
    }

    override fun createTooltip(tooltip : TooltipMakerAPI, expanded : Boolean, tooltipParam : Any?) {
        var spacing = 0f
        if (title != null) {
            tooltip.addPara(title, Global.getSector().playerFaction.brightUIColor, 0f)
            spacing = 9f
        }

        for (para in paras) {
            var substitutedPara = para
            if (condition != null && condition.plugin != null) {
                for (sub in condition.plugin!!.tokenReplacements) {
                    substitutedPara = substitutedPara.replace(sub.key, sub.value)
                }
            }
            tooltip.addPara(substitutedPara, spacing)
            spacing = 8f
        }
        spacing = 8f

        if (condition != null && condition.genSpec != null) {
            val hazardRating = (condition.genSpec.hazard * 100).toInt()
            if (hazardRating != 0) {
                val hazardPrefix = if (hazardRating > 0) "+" else ""
                tooltip.addPara("$hazardPrefix$hazardRating%% hazard rating", spacing, Misc.getHighlightColor(), "$hazardPrefix$hazardRating%")
            }

            val commodity = ResourceDepositsCondition.COMMODITY[condition.id];
            if (commodity != null) {
                val commoditySpec = Global.getSettings().getCommoditySpec(commodity);
//                val industry = ResourceDepositsCondition.INDUSTRY[commodity]

                val mod = ResourceDepositsCondition.MODIFIER[condition.id]!!
                val plusOrBlank = if (mod > 0) "+" else ""
                if (mod == 0) {
                    tooltip.addPara("No bonuses or penalties to ${commoditySpec.name.lowercase(Locale.getDefault())} production.", spacing);
                } else {
                    tooltip.addPara("$plusOrBlank$mod to ${commoditySpec.name.lowercase(Locale.getDefault())} production.", spacing, Misc.getHighlightColor(), "$plusOrBlank$mod");
                }
            }
        }
    }
}

class ProjectRequirementsTooltip(width : Float) : TooltipCreator {
    var terraformingProject : BoggledTerraformingProject? = null
    private val width : Float
    var market : MarketAPI? = null

    init {
        this.width = width
    }

    fun setProject(terraformingProject : BoggledTerraformingProject) {
        this.terraformingProject = terraformingProject;
    }

    override fun isTooltipExpandable(tooltipParam : Any?): Boolean {
        return false
    }

    override fun getTooltipWidth(tooltipParam : Any?) : Float {
        return width
    }

    override fun createTooltip(tooltip : TooltipMakerAPI, expanded : Boolean, tooltipParam : Any?) {
        val ctx = RequirementContext(market!!)
        tooltip.addPara("Project duration: %s days", 0f, Misc.getHighlightColor(), terraformingProject!!.getModifiedProjectDuration(ctx).toString());

        tooltip.addSpacer(5f);

        val tokenReplacements = boggledTools.getTokenReplacements(ctx)
        for (projectRequirement in terraformingProject!!.projectRequirements) {
            val requirementMet = projectRequirement.checkRequirement(ctx)
            val color = if (requirementMet) Misc.getPositiveHighlightColor() else Misc.getNegativeHighlightColor()
            val tts = projectRequirement.getTooltip(ctx, tokenReplacements)
            for (tt in tts) {
                tooltip.addPara(tt.text, color, 0f)
            }
        }

        tooltip.addSpacer(5f);
    }
}

class PlanetCardTooltip(basePanel : CustomPanelAPI, market : MarketAPI) : TooltipCreator {
    private val basePanel : CustomPanelAPI
    private val market : MarketAPI

    init {
        this.basePanel = basePanel
        this.market = market
    }

    override fun isTooltipExpandable(tooltipParam : Any?): Boolean {
        return false;
    }

    override fun getTooltipWidth(tooltipParam : Any?): Float {
        return 450f
    }

    override fun createTooltip(tooltip : TooltipMakerAPI, expanded : Boolean, tooltipParam : Any?) {
        tooltip.addSectionHeading("Industries & structures", Alignment.MID, 0f)

//        val industriesPlaceholder = LunaUIPlaceholder(false, getTooltipWidth(null), 400f, "", "PlanetCardTooltip", basePanel, tooltip)
//        industriesPlaceholder.position!!.inTL(0f, 0f)
//
//        val industriesElement = industriesPlaceholder.lunaElement!!.createUIElement(getTooltipWidth(null), 400f, false)
//        industriesElement.position.inTL(0f, 0f)
//
//        industriesPlaceholder.lunaElement!!.addUIElement(industriesElement)

        var horizontalSpacing = 0f
        var verticalSpacing = 0f
        for (industry in market.industries) {
//            val industryHolder = LunaUIPlaceholder(false, getTooltipWidth(null) / 2, CommandUIIntelK.PLANET_CARD_HEIGHT / 2, "", "PlanetCardTooltipItem", industriesPlaceholder.lunaElement!!, industriesElement)
//            industryHolder.position!!.inTL(horizontalSpacing, verticalSpacing)
//
//            val industryElement = industryHolder.lunaElement!!.createUIElement(getTooltipWidth(null) / 2, CommandUIIntelK.PLANET_CARD_HEIGHT / 2, false)
//            industryElement.position.inTL(0f, 0f)
//
//            industryHolder.lunaElement!!.addUIElement(industryElement)
//
//            val sprite = LunaUISprite(industry.spec.imageName, 20f, 20f, 0f, 0f, 0f, 0f, "", "IndustrySprite", industryHolder.lunaElement!!, industryElement)

            horizontalSpacing += getTooltipWidth(null) / 2
            if (horizontalSpacing >= getTooltipWidth(null)) {
                horizontalSpacing = 0f
                verticalSpacing += 20f
            }
        }
    }
}

data class CommandUIButtonData(val planetCard : ButtonAPI, val market : MarketAPI, val uiIntelK : CommandUIIntelK, var projectLabel : LabelAPI? = null, var projectTimeRemaining : LabelAPI? = null) {
}

data class CommandUITerraformingButtonPanelData(val planetNameLabel : LabelAPI) {
}

class CommandUIIntelK : LunaBaseCustomPanelPlugin() {
    private var LOGGER = LogManager.getLogger("evangel.tascui.CommandUIIntelK")

    private var width = 0f
    private var height = 0f

    /*
    Holds buttons for each state regardless of if the planet meets the requirements
    Buttons are moved offscreen (inTL(100000f, 100000f)) when not being used
     */
    private var requirementsMetButtons : ArrayList<ButtonAPI> = ArrayList()
    private var requirementsNotMetButtons : ArrayList<ButtonAPI> = ArrayList()

    private var inactiveStartProjectButton : ButtonAPI? = null
    private var startProjectButton : ButtonAPI? = null
    private var requirementsNotMetButton : ButtonAPI? = null

    private var activeCancelProjectButton : ButtonAPI? = null
    private var inactiveCancelProjectButton : ButtonAPI? = null

    private var selectionButtonsPanel : CustomPanelAPI? = null

    private var selectedProject : ButtonAPI? = null
    private var selectedPlanet : CommandUIButtonData? = null

    private var terraformingPanelData : CommandUITerraformingButtonPanelData? = null

    companion object {
        private const val HEADER_HEIGHT = 20f

        private const val SORT_SPACING = 3f
        private const val PANEL_SPACING = 5f

        private const val SORT_MAGIC_X_PAD = -2f

        private const val NAME_WIDTH = 170f
        private const val NAME_PANEL_OFFSET = SORT_MAGIC_X_PAD
        private const val NAME_SORT_OFFSET = SORT_MAGIC_X_PAD

        private const val CONDITIONS_WIDTH = 200f
        private const val CONDITIONS_PANEL_OFFSET = NAME_PANEL_OFFSET + NAME_WIDTH + PANEL_SPACING
        private const val CONDITIONS_SORT_OFFSET = NAME_SORT_OFFSET + NAME_WIDTH + SORT_SPACING

        private const val HAZARD_WIDTH  = 150f
        private const val HAZARD_PANEL_OFFSET = CONDITIONS_PANEL_OFFSET + CONDITIONS_WIDTH + SORT_SPACING
        private const val HAZARD_SORT_OFFSET = CONDITIONS_SORT_OFFSET + CONDITIONS_WIDTH + SORT_SPACING

        private const val SORT_PANEL_WIDTH = HAZARD_SORT_OFFSET + HAZARD_WIDTH
        private const val SORT_PANEL_HEIGHT = HEADER_HEIGHT
        private const val SORT_ARROW_SPRITE_SIZE = 15f

        private const val PLANETS_PANEL_WIDTH = SORT_PANEL_WIDTH

        private const val PLANET_CARD_WIDTH = PLANETS_PANEL_WIDTH
        const val PLANET_CARD_HEIGHT = 80f

        private const val CONDITION_SPRITE_SIZE = 21f
        private const val CONDITION_SPRITE_SIZE_W_SPACING = CONDITION_SPRITE_SIZE + 3f

        private const val PLANET_CARD_HOLDER_MAGIC_X_PAD = 3f
        private const val PLANET_TYPE_LABEL_MAGIC_X_PAD = 4f

        private const val BUTTON_OFF_SCREEN_POSITION = 100000f

        private fun getTerraformingControllerFromMarket(market : MarketAPI) : Terraforming_Controller {
            return market.getCondition("terraforming_controller").plugin as Terraforming_Controller
        }

        private fun getTerraformingDaysRemaining(terraformingController: Terraforming_Controller) : Int {
            if (terraformingController.project == null) return 0
            return terraformingController.daysRemaining
        }

        private fun getTerraformingDaysRemainingComplete(terraformingController : Terraforming_Controller) : String {
            if (terraformingController.project == null) return ""
            val daysRemaining = terraformingController.daysRemaining
            val days = if (daysRemaining == 1) " day " else " days "
            return daysRemaining.toString() + days + "remaining"
        }
    }

    override fun init() {
        enableCloseButton = true
        width = panel.position.width
        height = panel.position.height

        val element = panel.createUIElement(width, HEADER_HEIGHT, false)
        element.addSectionHeading("Terraforming", Alignment.MID, 0f)
        element.position.inTL(0f, 0f)
        panel.addUIElement(element)

        val planetCardPanelWidth = SORT_PANEL_WIDTH
        val planetCardPanelHeight = height - HEADER_HEIGHT - 5f
        val planetCardPanelXPad = 0f
        val planetCardPanelYPad = HEADER_HEIGHT

        val terraformingPanelWidth = width - SORT_PANEL_WIDTH - 9f
        val terraformingPanelHeight = height - HEADER_HEIGHT - 5f
        val terraformingPanelXPad = SORT_PANEL_WIDTH + 9f
        val terraformingPanelYPad = HEADER_HEIGHT

        createPlanetList(panel, planetCardPanelWidth, planetCardPanelHeight, planetCardPanelXPad, planetCardPanelYPad)

        createTerraformingSelection(panel, terraformingPanelWidth, terraformingPanelHeight, terraformingPanelXPad, terraformingPanelYPad)
    }

    override fun processInput(events : MutableList<InputEventAPI>) {
        super.processInput(events)

        events.forEach { event ->
            if (event.isKeyDownEvent && event.eventValue == Keyboard.KEY_ESCAPE) {
                event.consume()
                close()
                return@forEach
            }
        }
    }

    private fun createTerraformingSelectionButtons(baseElement : TooltipMakerAPI, width : Float, height : Float, yPad : Float) : Float {
        val faction = Global.getSector().playerFaction

        val buttonsHolder = LunaElement(baseElement, width, height)
        buttonsHolder.renderBackground = false
        buttonsHolder.renderBorder = false
        buttonsHolder.position.inTL(0f, yPad)

//        buttonsHolder.innerElement.addAreaCheckbox(null, null, Global.getSector().playerFaction.baseUIColor, Color(122,122,122,255), Global.getSector().playerFaction.brightUIColor, width, height, 0f).position.inTL(0f, 0f)

        val projectCounts = boggledTools.getNumProjects()
        var projectCount = 0
        if (projectCounts["terraforming"] != null) {
            projectCount += projectCounts["terraforming"]!!
        }
        if (projectCounts["crafting"] != null) {
            projectCount += projectCounts["crafting"]!!
        }

        for (i in 0 until projectCount) {
            val projectRequirementsTooltip = ProjectRequirementsTooltip(width)

            val validButton = buttonsHolder.innerElement.addButton("", projectRequirementsTooltip, faction.baseUIColor, faction.darkUIColor, Alignment.LMID, CutStyle.ALL, width, HEADER_HEIGHT, 0f)
            val invalidButton = buttonsHolder.innerElement.addButton("", projectRequirementsTooltip, faction.baseUIColor.darker(), faction.darkUIColor.darker(), Alignment.LMID, CutStyle.ALL, width, HEADER_HEIGHT, 0f)

            buttonsHolder.innerElement.addTooltipTo(projectRequirementsTooltip, validButton, TooltipMakerAPI.TooltipLocation.RIGHT)
            buttonsHolder.innerElement.addTooltipTo(projectRequirementsTooltip, invalidButton, TooltipMakerAPI.TooltipLocation.RIGHT)

            validButton.position.inTL(BUTTON_OFF_SCREEN_POSITION, BUTTON_OFF_SCREEN_POSITION)
            invalidButton.position.inTL(BUTTON_OFF_SCREEN_POSITION, BUTTON_OFF_SCREEN_POSITION)

            requirementsMetButtons.add(validButton)
            requirementsNotMetButtons.add(invalidButton)
        }

        return (projectCount - 1) * (HEADER_HEIGHT + SORT_SPACING) + SORT_SPACING
    }

    private fun createTerraformingSelection(basePanel : CustomPanelAPI, width : Float, height : Float, xPad : Float, yPad : Float) {
        val verticalSpacing = HEADER_HEIGHT + 1f

        val terraformingPanel = basePanel.createCustomPanel(width, height, null)
        terraformingPanel.position.inTL(xPad, yPad)

        val terraformingElement = terraformingPanel.createUIElement(width, height, true)
        terraformingElement.position.inTL(0f, 0f)

        val sectionHeading = terraformingElement.addSectionHeading(selectedPlanet?.market?.name, Alignment.MID, 0f)
        sectionHeading.position.inTL(0f, 0f)

        val buttonsWidth = width * 0.5f - SORT_SPACING;
        val buttonsHeight = height - verticalSpacing - HEADER_HEIGHT

        selectionButtonsPanel = terraformingPanel.createCustomPanel(buttonsWidth, buttonsHeight, null)
        selectionButtonsPanel!!.position.inTL(BUTTON_OFF_SCREEN_POSITION, BUTTON_OFF_SCREEN_POSITION)

        val selectionButtonsElement = selectionButtonsPanel!!.createUIElement(buttonsWidth, buttonsHeight, true)
        selectionButtonsElement.position.inTL(0f, 0f)

        val selectionButtonsRequiredHeight = createTerraformingSelectionButtons(selectionButtonsElement, buttonsWidth - 5f, buttonsHeight, 0f)

        selectionButtonsElement.heightSoFar = selectionButtonsRequiredHeight
        selectionButtonsPanel!!.addUIElement(selectionButtonsElement)
        terraformingElement.addComponent(selectionButtonsPanel)

        val startCancelProjectButtonsWidth = width - SORT_SPACING
        val startCancelProjectButtonsHeight = HEADER_HEIGHT
        val startCancelProjectButtonsHolder = LunaElement(terraformingElement, startCancelProjectButtonsWidth, startCancelProjectButtonsHeight)
        startCancelProjectButtonsHolder.renderBackground = false
        startCancelProjectButtonsHolder.renderBorder = false
        startCancelProjectButtonsHolder.position.inTL(0f, height - startCancelProjectButtonsHeight)

//        startCancelProjectButtonsHolder.innerElement.addAreaCheckbox(null, null, Global.getSector().playerFaction.baseUIColor, Color(122,122,122,255), Global.getSector().playerFaction.brightUIColor, startCancelProjectButtonsWidth, startCancelProjectButtonsHeight, 0f).position.inTL(0f, 0f)

        val faction = Global.getSector().playerFaction

        inactiveStartProjectButton = startCancelProjectButtonsHolder.innerElement.addButton("Start project", null, faction.baseUIColor.darker(), faction.darkUIColor.darker(), Alignment.MID, CutStyle.ALL, buttonsWidth, HEADER_HEIGHT, 0f)
        startProjectButton = startCancelProjectButtonsHolder.innerElement.addButton("Start project", null, faction.baseUIColor, faction.darkUIColor, Alignment.MID, CutStyle.ALL, buttonsWidth, HEADER_HEIGHT, 0f)
        requirementsNotMetButton = startCancelProjectButtonsHolder.innerElement.addButton("Requirements not met", null, faction.baseUIColor.darker(), faction.darkUIColor.darker(), Alignment.MID, CutStyle.ALL, buttonsWidth, HEADER_HEIGHT, 0f)

        requirementsNotMetButton!!.setClickable(false)

        inactiveStartProjectButton!!.position.inTL(BUTTON_OFF_SCREEN_POSITION, BUTTON_OFF_SCREEN_POSITION)
        startProjectButton!!.position.inTL(BUTTON_OFF_SCREEN_POSITION, BUTTON_OFF_SCREEN_POSITION)
        requirementsNotMetButton!!.position.inTL(BUTTON_OFF_SCREEN_POSITION, BUTTON_OFF_SCREEN_POSITION)

        activeCancelProjectButton = startCancelProjectButtonsHolder.innerElement.addButton("Cancel current project", null, faction.baseUIColor, faction.darkUIColor, Alignment.MID, CutStyle.ALL, buttonsWidth, HEADER_HEIGHT, 0f)
        inactiveCancelProjectButton = startCancelProjectButtonsHolder.innerElement.addButton("Cancel current project", null, faction.baseUIColor.darker(), faction.darkUIColor.darker(), Alignment.MID, CutStyle.ALL, buttonsWidth, HEADER_HEIGHT, 0f)

        inactiveCancelProjectButton!!.setClickable(false)

        activeCancelProjectButton!!.position.inTL(BUTTON_OFF_SCREEN_POSITION, BUTTON_OFF_SCREEN_POSITION)
        inactiveCancelProjectButton!!.position.inTL(BUTTON_OFF_SCREEN_POSITION, BUTTON_OFF_SCREEN_POSITION)

        terraformingPanelData = CommandUITerraformingButtonPanelData(sectionHeading)

        terraformingPanel.addUIElement(terraformingElement)
        basePanel.addComponent(terraformingPanel)
    }

    private fun updateTerraformingSelection() {
        if (terraformingPanelData == null || selectedPlanet == null) {
            return;
        }

        terraformingPanelData!!.planetNameLabel.text = selectedPlanet!!.market.name

        selectedProject?.unhighlight()
        selectedProject = null

        for (button in requirementsMetButtons) {
            button.position!!.inTL(BUTTON_OFF_SCREEN_POSITION, BUTTON_OFF_SCREEN_POSITION)
        }
        for (button in requirementsNotMetButtons) {
            button.position!!.inTL(BUTTON_OFF_SCREEN_POSITION, BUTTON_OFF_SCREEN_POSITION)
        }

        startProjectButton!!.position.inTL(BUTTON_OFF_SCREEN_POSITION, BUTTON_OFF_SCREEN_POSITION)
        requirementsNotMetButton!!.position.inTL(BUTTON_OFF_SCREEN_POSITION, BUTTON_OFF_SCREEN_POSITION)

        val sortProjects = fun(projectOptions : LinkedHashMap<String, BoggledTerraformingProject>?, validOptions : ArrayList<BoggledTerraformingProject>, invalidOptions : ArrayList<BoggledTerraformingProject>) {
            if (projectOptions == null) {
                return;
            }
            for (projectOption in projectOptions) {
                if (projectOption.value.requirementsMet(RequirementContext(selectedPlanet!!.market))) {
                    validOptions.add(projectOption.value)
                } else {
                    invalidOptions.add(projectOption.value)
                }
            }
        }
        val visibleProjects = boggledTools.getVisibleProjects(RequirementContext(selectedPlanet!!.market))

        val validTerraformingOptions : ArrayList<BoggledTerraformingProject> = ArrayList()
        val invalidTerraformingOptions : ArrayList<BoggledTerraformingProject> = ArrayList()
        sortProjects(visibleProjects["terraforming"], validTerraformingOptions, invalidTerraformingOptions)

        val validCraftingOptions : ArrayList<BoggledTerraformingProject> = ArrayList()
        val invalidCraftingOptions : ArrayList<BoggledTerraformingProject> = ArrayList()
        sortProjects(visibleProjects["crafting"], validCraftingOptions, invalidCraftingOptions)

        var buttonVerticalSpacing = 0f
        val positionButtons = fun(ctx : RequirementContext, terraformingOptions : ArrayList<BoggledTerraformingProject>, buttons : ArrayList<ButtonAPI>, buttonsStart : Int) : Int {
            for (i in 0 until terraformingOptions.size) {
                val button = buttons[i + buttonsStart]

                val projectRequirementsTooltip = button.customData as ProjectRequirementsTooltip
                button.text = terraformingOptions[i].getProjectTooltip(boggledTools.getTokenReplacements(ctx))
                button.position.inTL(0f, buttonVerticalSpacing)
                projectRequirementsTooltip.market = selectedPlanet?.market
                projectRequirementsTooltip.setProject(terraformingOptions[i])

                buttonVerticalSpacing += HEADER_HEIGHT + SORT_SPACING
            }
            return terraformingOptions.size + buttonsStart
        }
        val ctx = RequirementContext(selectedPlanet!!.market)
        val validTerraformingEnd = positionButtons(ctx, validTerraformingOptions, requirementsMetButtons, 0)
        val invalidTerraformingEnd = positionButtons(ctx, invalidTerraformingOptions, requirementsNotMetButtons, validTerraformingEnd)

        buttonVerticalSpacing += SORT_SPACING

        val validCraftingEnd = positionButtons(ctx, validCraftingOptions, requirementsMetButtons, invalidTerraformingEnd)
        positionButtons(ctx, invalidCraftingOptions, requirementsNotMetButtons, validCraftingEnd)
    }

    private fun moveButtonsOffscreen(positionInPlacer : (x : Float, y : Float) -> PositionAPI, vararg inactiveButtons : ButtonAPI) {
        positionInPlacer(0f, 0f)
        for (inactiveButton in inactiveButtons) {
            inactiveButton.position.inTL(BUTTON_OFF_SCREEN_POSITION, BUTTON_OFF_SCREEN_POSITION)
        }
    }

    private fun handlePlanetCardPress(data : CommandUIButtonData) {
        if (selectedPlanet != null && selectedPlanet !== data) {
            selectedPlanet!!.planetCard.isChecked = false
            selectedPlanet!!.planetCard.unhighlight()
        }

        selectedPlanet = data
        selectedPlanet!!.planetCard.isChecked = !selectedPlanet!!.planetCard.isChecked
        if (selectedPlanet!!.planetCard.isChecked) selectedPlanet!!.planetCard.highlight()
        else selectedPlanet!!.planetCard.unhighlight()

        if (selectedPlanet != null) {
            updateTerraformingSelection()

            moveButtonsOffscreen(inactiveStartProjectButton!!.position::inTL, startProjectButton!!, requirementsNotMetButton!!)
            val terraformingController = getTerraformingControllerFromMarket(selectedPlanet!!.market)
            if (terraformingController.project == null) {
                moveButtonsOffscreen(inactiveCancelProjectButton!!.position::inTR, activeCancelProjectButton!!)
            } else {
                moveButtonsOffscreen(activeCancelProjectButton!!.position::inTR, inactiveCancelProjectButton!!)
            }

            selectionButtonsPanel!!.position.inTL(0f, HEADER_HEIGHT)
        }
    }

    private fun handleTerraformingOptionButtonPress() {
        val terraformingProject = (selectedProject?.customData as ProjectRequirementsTooltip).terraformingProject!!
        if (terraformingProject.requirementsMet(RequirementContext(selectedPlanet?.market))) {
            moveButtonsOffscreen(startProjectButton!!.position::inTL, requirementsNotMetButton!!, inactiveStartProjectButton!!)
        } else {
            moveButtonsOffscreen(requirementsNotMetButton!!.position::inTL, startProjectButton!!, inactiveStartProjectButton!!)
        }
    }

    private fun handleTerraformingStartProjectButtonPress() {
        val terraformingController = getTerraformingControllerFromMarket(selectedPlanet!!.market)

        val terraformingProject = (selectedProject?.customData as ProjectRequirementsTooltip).terraformingProject!!

        terraformingController.setProject(terraformingProject)

        selectedPlanet?.projectLabel?.text = terraformingProject.getProjectTooltip(boggledTools.getTokenReplacements(RequirementContext(selectedPlanet?.market)))
        selectedPlanet?.projectTimeRemaining?.text = getTerraformingDaysRemainingComplete(terraformingController)
        selectedPlanet?.projectTimeRemaining?.setHighlight("${getTerraformingDaysRemaining(terraformingController)}")

        moveButtonsOffscreen(activeCancelProjectButton!!.position::inTR, inactiveCancelProjectButton!!)
    }

    private fun handleTerraformingCancelProjectButtonPress() {
        val terraformingController = getTerraformingControllerFromMarket(selectedPlanet!!.market)
        terraformingController.setProject(null)

        selectedPlanet?.projectLabel?.text = "None"
        selectedPlanet?.projectTimeRemaining?.text = ""

        moveButtonsOffscreen(inactiveCancelProjectButton!!.position::inTR, activeCancelProjectButton!!)
    }

    private fun createSortButton(baseElement: TooltipMakerAPI, buttonText: String, data: Any?, width: Float, height: Float, xPad: Float, tooltip: StaticTooltip) {
        val faction = Global.getSector().playerFaction

        val sortHolder = LunaElement(baseElement, width, height)
        sortHolder.renderBackground = false
        sortHolder.renderBorder = false
        sortHolder.position.inTL(xPad, 0f)

        val sortButton = sortHolder.innerElement.addAreaCheckbox(buttonText, data, faction.baseUIColor.darker(), faction.darkUIColor, faction.brightUIColor, width, height, 0f)

        sortButton.setClickable(false)

        sortHolder.innerElement.addTooltipTo(tooltip, sortButton, TooltipMakerAPI.TooltipLocation.ABOVE)

        val arrowSpritePath = "graphics/ui/buttons/arrow_down2.png"
        val arrowSprite = LunaSpriteElement(arrowSpritePath, LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, sortHolder.innerElement, SORT_ARROW_SPRITE_SIZE, SORT_ARROW_SPRITE_SIZE)
        arrowSprite.position.inTR(0f, height / 2 - SORT_ARROW_SPRITE_SIZE / 2)
        arrowSprite.getSprite().color = faction.baseUIColor
    }

    private fun createSortButtons(basePanel : CustomPanelAPI, yPad : Float) {
        val sortPanel = basePanel.createCustomPanel(SORT_PANEL_WIDTH, SORT_PANEL_HEIGHT, null)
        sortPanel.position.inTL(0f, yPad)

        val sortPanelElement = sortPanel.createUIElement(SORT_PANEL_WIDTH, SORT_PANEL_HEIGHT, false)
        sortPanelElement.position.inTL(0f, 0f)

        sortPanel.addUIElement(sortPanelElement)

        basePanel.addComponent(sortPanel)

        createSortButton(sortPanelElement, "Name", null, NAME_WIDTH, SORT_PANEL_HEIGHT, NAME_SORT_OFFSET, StaticTooltip(null, null, "Colony name."))//, "Sorts colonies by date established."))
        createSortButton(sortPanelElement, "Conditions", null, CONDITIONS_WIDTH, SORT_PANEL_HEIGHT, CONDITIONS_SORT_OFFSET, StaticTooltip(null, null, "Planetary/local conditions. Does not include conditions added by human activity."))//, "Sorts by number of conditions."))
        createSortButton(sortPanelElement, "Hazard rating", null, HAZARD_WIDTH, SORT_PANEL_HEIGHT, HAZARD_SORT_OFFSET, StaticTooltip(null, null, "Planetary hazard rating."))//, "Sorts by hazard rating."))
    }

    private fun createPlanetsNamePanel(baseElement : TooltipMakerAPI, ctx : RequirementContext) {
        val nameHolder = LunaElement(baseElement, NAME_WIDTH, PLANET_CARD_HEIGHT)
        nameHolder.renderBackground = false
        nameHolder.renderBorder = false
        nameHolder.position.inTL(0f, 0f)

//        nameHolder.innerElement.addAreaCheckbox(null, null, Global.getSector().playerFaction.baseUIColor, Color(122,122,122,255), Global.getSector().playerFaction.brightUIColor, NAME_WIDTH, PLANET_CARD_HEIGHT, 0f).position.inTL(0f, 0f)

        nameHolder.innerElement.showPlanetInfo(ctx.planet, NAME_WIDTH, PLANET_CARD_HEIGHT, false, 0f)

        val nameLabel = nameHolder.innerElement.addPara(ctx.market.name, ctx.market.textColorForFactionOrPlanet, 0f)
        val nameLength = nameLabel.computeTextWidth(ctx.market.name)

        nameLabel.position.inTL(NAME_WIDTH / 2 - (nameLength * 0.9f) / 2, PLANET_CARD_HEIGHT - HEADER_HEIGHT)
    }

    private fun createPlanetsConditionPanel(baseElement : TooltipMakerAPI, ctx : RequirementContext) {
        /*
        stack SHOULD be: conditionHolder -> conditionElement -> conditionSprites
         */
        val conditionHolder = LunaElement(baseElement, CONDITIONS_WIDTH, PLANET_CARD_HEIGHT)
        conditionHolder.renderBackground = false
        conditionHolder.renderBorder = false
        conditionHolder.position.inTL(CONDITIONS_PANEL_OFFSET, 0f)

//        conditionHolder.innerElement.addAreaCheckbox(null, null, Global.getSector().playerFaction.baseUIColor, Color(122,122,122,255), Global.getSector().playerFaction.brightUIColor, CONDITIONS_WIDTH, PLANET_CARD_HEIGHT, 0f).position.inTL(0f, 0f)

        var conditionHorizontalSpacing = 2f
        var conditionVerticalSpacing = PLANET_CARD_HEIGHT / 2
        val planetaryConditionsCount = ctx.market.conditions.filter { it.isPlanetary }.size

        var conditionSpriteSize = CONDITION_SPRITE_SIZE
        val conditionSpriteCapacity = PLANET_CARD_HEIGHT / 2
        var conditionSpriteSpacing = 6f
        if ((planetaryConditionsCount * (conditionSpriteSize + conditionSpriteSpacing)) > CONDITIONS_WIDTH) {
            val horSpaceNeeded = planetaryConditionsCount * (conditionSpriteSize + conditionSpriteSpacing)
            val rowsNeeded = (horSpaceNeeded / CONDITIONS_WIDTH).toInt() + 1

            conditionSpriteSpacing /= rowsNeeded
            val verSpacePerRow = (conditionSpriteCapacity - conditionSpriteSpacing * (rowsNeeded - 1)) / (rowsNeeded)
            conditionSpriteSize = verSpacePerRow
        }

        val planetTypeLabelElement = conditionHolder.elementPanel.createUIElement(CONDITIONS_WIDTH, HEADER_HEIGHT, false)
        val planetTypeLabel = planetTypeLabelElement.addPara(ctx.planet.spec.name, ctx.planet.spec.iconColor, 0f)

        val planetTypeHeight = planetTypeLabel.computeTextHeight(ctx.planet.typeNameWithWorld)
        planetTypeLabelElement.position.inTL(PLANET_TYPE_LABEL_MAGIC_X_PAD, conditionVerticalSpacing - planetTypeHeight - 5f)
        planetTypeLabel.position.inTL(0f, 0f)
        conditionHolder.elementPanel.addUIElement(planetTypeLabelElement)

        for (condition in ctx.market.conditions) {
            if (!condition.isPlanetary) continue

            val conditionSprite = LunaSpriteElement(condition.spec.icon, LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, conditionHolder.innerElement, conditionSpriteSize, conditionSpriteSize)
            conditionSprite.position.inTL(conditionHorizontalSpacing, conditionVerticalSpacing)

            val ttName = condition.spec.name
            val ttDescription = condition.spec.desc
            for (replacement in condition.plugin.tokenReplacements) {
                ttDescription.replace(replacement.key, replacement.value)
            }

            conditionHolder.innerElement.addTooltipTo(StaticTooltip(ttName, condition, ttDescription), conditionSprite.innerElement, TooltipMakerAPI.TooltipLocation.BELOW)

            conditionHorizontalSpacing += conditionSpriteSize + conditionSpriteSpacing
            if ((conditionHorizontalSpacing + conditionSpriteSize) > CONDITIONS_WIDTH) {
                conditionHorizontalSpacing = 0f
                conditionVerticalSpacing += conditionSpriteSize + conditionSpriteSpacing
            }
        }
    }

    private fun createPlanetsHazardPanel(baseElement : TooltipMakerAPI, ctx : RequirementContext, faction : FactionAPI, buttonData : CommandUIButtonData) {
        /*
        stack SHOULD be: hazardHolder -> hazardText
         */
        val hazardHolder = LunaElement(baseElement, HAZARD_WIDTH, PLANET_CARD_HEIGHT)
        hazardHolder.renderBackground = false
        hazardHolder.renderBorder = false
        hazardHolder.position.inTL(HAZARD_PANEL_OFFSET, 0f)

        val hazardLabelRating = (ctx.market.hazard.modified * 100).toInt()
        val hazardLabel = hazardHolder.innerElement.addPara("Hazard rating: $hazardLabelRating%%", 0f, faction.baseUIColor, Misc.getHighlightColor(), "$hazardLabelRating%")
        hazardLabel.position.inTL(0f, PLANET_CARD_HEIGHT - HEADER_HEIGHT)

        val terraformingController = getTerraformingControllerFromMarket(ctx.market)
        val projectNameNicer = boggledTools.getTooltipProjectName(ctx, terraformingController.project)
        buttonData.projectLabel = hazardHolder.innerElement.addPara(projectNameNicer, faction.baseUIColor, 0f)
        buttonData.projectLabel!!.position.inTL(0f, SORT_SPACING)

        buttonData.projectTimeRemaining = hazardHolder.innerElement.addPara(getTerraformingDaysRemainingComplete(terraformingController), 0f, faction.baseUIColor, Misc.getHighlightColor(), "${getTerraformingDaysRemaining(terraformingController)}")
        buttonData.projectTimeRemaining!!.position.inTL(0f, PLANET_CARD_HEIGHT - 2 * HEADER_HEIGHT)
    }

    private fun createPlanetsPanel(basePanel : CustomPanelAPI, height : Float, yPad : Float) {
        val faction = Global.getSector().playerFaction
        val markets : ArrayList<MarketAPI> = boggledTools.getNonStationMarketsPlayerControls()

        val planetsPanelHeight = height
//        val planetsPanelHeight = markets.size * PLANET_CARD_HEIGHT + 3f

        val planetsPanel = basePanel.createCustomPanel(PLANETS_PANEL_WIDTH, planetsPanelHeight, null)
        planetsPanel.position.inTL(0f, yPad)

        val planetsElement = planetsPanel.createUIElement(PLANETS_PANEL_WIDTH, planetsPanelHeight, true)
        planetsElement.position.inTL(0f, 0f)

//        planetsElement.addAreaCheckbox(null, null, faction.baseUIColor, Color(128,128,128,255), faction.brightUIColor, PLANETS_PANEL_WIDTH, planetsPanelHeight, 0f).position.inTL(0f, 0f)

        if (markets.isEmpty()) {
            planetsElement.addAreaCheckbox("No planets", null, Color(0, 0, 0, 0), Color(0, 0, 0, 0), faction.brightUIColor, PLANETS_PANEL_WIDTH, planetsPanelHeight, 0f)
        }

        var verticalSpacing = 3f
        for (marketVar in markets) {
            val cardHolder = LunaElement(planetsElement, PLANET_CARD_WIDTH, PLANET_CARD_HEIGHT)
            cardHolder.position.inTL(PLANET_CARD_HOLDER_MAGIC_X_PAD, verticalSpacing)

            cardHolder.renderBackground = false
            cardHolder.renderBorder = false

            val button = cardHolder.innerElement.addAreaCheckbox(null, null, faction.darkUIColor, Color(0,0, 0,0), faction.brightUIColor, PLANET_CARD_WIDTH, PLANET_CARD_HEIGHT, 0f)
            button.position.inTL(0f, 0f)

            val buttonData = CommandUIButtonData(button, marketVar, this)
            val ctx = RequirementContext(marketVar)

            createPlanetsNamePanel(cardHolder.innerElement, ctx)

            createPlanetsConditionPanel(cardHolder.innerElement, ctx)

            createPlanetsHazardPanel(cardHolder.innerElement, ctx, faction, buttonData)

            val actualButton = LunaElement(cardHolder.innerElement, PLANET_CARD_WIDTH, PLANET_CARD_HEIGHT)
            actualButton.renderBackground = false
            actualButton.renderBorder = false
            actualButton.position.inTL(PLANET_CARD_HOLDER_MAGIC_X_PAD, 0f)

//            cardHolder.innerElement.addTooltipTo(PlanetCardTooltip(cardHolder.elementPanel, marketVar), button, TooltipMakerAPI.TooltipLocation.RIGHT)

            /*
            handlePlanetCardPress is where everything related to actually selecting a planetCard is handled
             */
            actualButton.onClick {
                handlePlanetCardPress(buttonData)
            }

            verticalSpacing += PLANET_CARD_HEIGHT
        }

        planetsPanel.addUIElement(planetsElement)
        basePanel.addComponent(planetsPanel)
    }

    private fun createPlanetList(basePanel : CustomPanelAPI, width : Float, height : Float, xPad : Float, yPad : Float) {

        val planetPanel = basePanel.createCustomPanel(width, height, null)
        planetPanel.position.inTL(xPad, yPad)

        var verticalSpacing = 0f
        createSortButtons(planetPanel, verticalSpacing)

        verticalSpacing += SORT_PANEL_HEIGHT

        val planetsPanelHeight = height - verticalSpacing - 5f
        createPlanetsPanel(planetPanel, planetsPanelHeight, verticalSpacing)

        basePanel.addComponent(planetPanel)
    }

    override fun advance(amount : Float) {
        val updateTerraformingButtons = { buttons : ArrayList<ButtonAPI> ->
            for (button in buttons) {
                if (button.isChecked) {
                    selectedProject?.unhighlight()
                    selectedProject = button
                    button.isChecked = false
                    button.highlight()

                    handleTerraformingOptionButtonPress()
                }
            }
        }
        updateTerraformingButtons(requirementsMetButtons)
        updateTerraformingButtons(requirementsNotMetButtons)

        if (startProjectButton?.isChecked == true) {
            startProjectButton!!.isChecked = false

            handleTerraformingStartProjectButtonPress()
        }

        if (activeCancelProjectButton?.isChecked == true) {
            activeCancelProjectButton!!.isChecked = false

            handleTerraformingCancelProjectButtonPress()
        }
    }
}