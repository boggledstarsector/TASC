package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.*;
import com.fs.starfarer.api.impl.campaign.econ.CommRelayCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import boggled.campaign.econ.StringHelper;
import boggled.campaign.econ.boggledTools;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledObjectivePrintDescription extends BaseCommandPlugin
{
    protected SectorEntityToken entity;

    public boggledObjectivePrintDescription() {}

    public boggledObjectivePrintDescription(SectorEntityToken entity) {
        this.init(entity);
    }

    protected void init(SectorEntityToken entity)
    {
        this.entity = entity;
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        if(dialog == null) return false;

        String type = ((Misc.Token)params.get(0)).getString(memoryMap);
        this.entity = dialog.getInteractionTarget();

        TextPanelAPI text = dialog.getTextPanel();

        printDescription(type);
        //Misc.showCost(text, (Color)null, (Color)null, getResources(), getQuantities(type));
        Misc.showCost(text, StringHelper.getString(StringHelper.INTEL, "objective_resources_cost_label"), true, 120.0F, (Color)null, (Color)null, getResources(), boggledTools.getQuantitiesForStableLocationConstruction(type), (boolean[])null);
        StarSystemAPI system = Global.getSector().getPlayerFleet().getStarSystem();
        if(!canBuild(type))
        {
            text.addPara(StringHelper.getString(StringHelper.INTEL, "objective_no_resources"));

            if(type.equals("inactive_gate"))
            {
                if(boggledTools.gateInSystem(system))
                {
                    text.addPara(StringHelper.getString(StringHelper.INTEL, "objective_gate_exists"));
                }

                if(boggledTools.getBooleanSetting("boggledPlayerMustHaveMarketInSystemToBuildInactiveGate") && boggledTools.getSizeOfLargestPlayerMarketInSystem(system) < boggledTools.getPlayerMarketSizeRequirementToBuildGate())
                {
                    text.addPara(StringHelper.getString(StringHelper.INTEL, "objective_gate_market_size",
                            StringHelper.tokens("size", String.valueOf(boggledTools.getPlayerMarketSizeRequirementToBuildGate()))));
                }
            }
        }
        else
        {
            if(type.equals("inactive_gate"))
            {
                if(boggledTools.gateInSystem(system))
                {
                    text.addPara(StringHelper.getString(StringHelper.INTEL, "objective_gate_exists"));
                }

                if(boggledTools.getBooleanSetting("boggledPlayerMustHaveMarketInSystemToBuildInactiveGate") && boggledTools.getSizeOfLargestPlayerMarketInSystem(system) < boggledTools.getPlayerMarketSizeRequirementToBuildGate())
                {
                    text.addPara(StringHelper.getString(StringHelper.INTEL, "objective_gate_market_size",
                            StringHelper.tokens("size", String.valueOf(boggledTools.getPlayerMarketSizeRequirementToBuildGate()))));
                }
            }
            else
            {
                text.addPara(StringHelper.getString(StringHelper.INTEL, "objective_proceed"));
            }
        }

        return true;
    }

    public String[] getResources()
    {
        return new String[]{Commodities.HEAVY_MACHINERY, Commodities.METALS, Commodities.RARE_METALS, Commodities.PARTS};
    }

    public boolean canBuild(String type)
    {
        if(DebugFlags.OBJECTIVES_DEBUG)
        {
            return true;
        }
        else
        {
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            String[] res = getResources();
            int[] quantities = boggledTools.getQuantitiesForStableLocationConstruction(type);

            for(int i = 0; i < res.length; ++i)
            {
                String commodityId = res[i];
                int quantity = quantities[i];
                if ((float)quantity > cargo.getQuantity(CargoAPI.CargoItemType.RESOURCES, commodityId))
                {
                    return false;
                }
            }

            return true;
        }
    }

    public void printDescription(String type)
    {
        InteractionDialogAPI dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        TextPanelAPI text = dialog.getTextPanel();
        SectorEntityToken entity = Global.getSector().getPlayerFleet().getInteractionTarget();

        Description desc = Global.getSettings().getDescription(type, Description.Type.CUSTOM);
        if (desc != null)
        {
            text.addParagraph(desc.getText1());
        }

        CustomEntitySpecAPI spec = Global.getSettings().getCustomEntitySpec(type);
        CustomCampaignEntityPlugin plugin = spec.getPlugin();
        SectorEntityToken temp = this.entity.getContainingLocation().createToken(0.0F, 0.0F);

        for (String tag : spec.getTags()) {
            temp.addTag(tag);
        }

        plugin.init(temp, null);
        boolean objective = this.entity.hasTag("objective");
        if (objective)
        {
            plugin = this.entity.getCustomPlugin();
        }

        Class c = null;
        if (plugin instanceof CampaignObjective)
        {
            CampaignObjective o = (CampaignObjective)plugin;
            c = o.getClass();
            TooltipMakerAPI info = text.beginTooltip();

            //Below hack makes sure to always print the bonus from a Domain-tech objective
            //Without the hack, the text will print the bonus from the existing Sector-tech objective
            //when the player clicks the upgrade option
            //o.printEffect(info, 0.0F);
            if(plugin instanceof CommRelayEntityPlugin)
            {
                int bonus = Math.abs(Math.round(CommRelayCondition.COMM_RELAY_BONUS));
                String sentence = StringHelper.getString(StringHelper.INTEL, "objective_effect_stability", StringHelper.tokens("bonus", "+" + bonus));
                StringHelper.applyHighlight(info.addPara(sentence, 0.0F), Misc.getHighlightColor(), "+" + bonus);
            }
            else if(plugin instanceof SensorArrayEntityPlugin)
            {
                float SENSOR_BONUS = 700.0F;
                int bonus = (int)SENSOR_BONUS;
                String sentence = StringHelper.getString(StringHelper.INTEL, "objective_effect_sensor_range", StringHelper.tokens("bonus", "+" + bonus));
                StringHelper.applyHighlight(info.addPara(sentence, 0.0F), Misc.getHighlightColor(), "+" + bonus);
            }
            else if(plugin instanceof NavBuoyEntityPlugin)
            {
                float NAV_BONUS = 3.0F;
                int bonus = (int)NAV_BONUS;
                String sentence = StringHelper.getString(StringHelper.INTEL, "objective_effect_burn", StringHelper.tokens("bonus", "+" + bonus));
                StringHelper.applyHighlight(info.addPara(sentence, 0.0F), Misc.getHighlightColor(), "+" + bonus);
            }

            text.addTooltip();
            o.printNonFunctionalAndHackDescription(text);
        }

        for (SectorEntityToken curr : this.entity.getContainingLocation().getEntitiesWithTag("objective")) {
            if (curr.hasTag("objective") && curr.getFaction() != null && curr.getFaction().isPlayerFaction() && curr.getCustomEntitySpec() != null) {
                CustomCampaignEntityPlugin ccep = curr.getCustomPlugin();
                if (ccep instanceof CampaignObjective) {
                    CampaignObjective o = (CampaignObjective) ccep;
                    if (c == o.getClass()) {
                        if (this.entity == curr) {
                            text.addPara(StringHelper.getString(StringHelper.INTEL, "objective_redundancy_this"));
                        } else {
                            text.addPara(StringHelper.getString(StringHelper.INTEL, "objective_redundancy_other",
                                    StringHelper.tokens(
                                            "article", curr.getCustomEntitySpec().getAOrAn(),
                                            "name", curr.getCustomEntitySpec().getNameInText())));
                        }
                        break;
                    }
                }
            }
        }
    }
}