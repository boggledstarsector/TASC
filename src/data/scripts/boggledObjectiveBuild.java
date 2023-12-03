package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.CampaignObjective;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;
import org.json.JSONException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.lang.String;

public class boggledObjectiveBuild extends BaseCommandPlugin
{
    protected SectorEntityToken entity;
    protected TextPanelAPI text;

    public boggledObjectiveBuild() {}

    public boggledObjectiveBuild(SectorEntityToken entity) {
        this.init(entity);
    }

    protected void init(SectorEntityToken entity)
    {
        this.entity = entity;
        this.text = null;
    }

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap)
    {
        if(dialog == null) return false;

        String type = ((Misc.Token)params.get(0)).getString(memoryMap);
        this.entity = dialog.getInteractionTarget();

        this.text = dialog.getTextPanel();

        try
        {
            build(type, Global.getSector().getPlayerFleet().getFaction().getId());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return true;
    }

    public void build(String type, String factionId) throws IOException, JSONException {
        LocationAPI loc = this.entity.getContainingLocation();
        if(type.equals("inactive_gate"))
        {
            factionId = Global.getSector().getFaction("neutral").getId();
        }

        if(this.entity.getFaction() != Global.getSector().getPlayerFaction() && this.entity.getFaction() != Global.getSector().getFaction("neutral"))
        {
            factionId = this.entity.getFaction().getId();
            if(type.equals("inactive_gate"))
            {
                factionId = Global.getSector().getFaction("neutral").getId();
            }
            String blacklist = Global.getSettings().loadText("data/config/boggled/no_relations_boost_from_objective_upgrade.txt");
            String[] blacklistArray = blacklist.split(",");
            List<String> blacklistList = Arrays.asList(blacklistArray);

            if(!blacklistList.contains(factionId))
            {
                CoreReputationPlugin.CustomRepImpact impact = new CoreReputationPlugin.CustomRepImpact();
                impact.delta = Global.getSettings().getFloat("boggledDomainTechObjectivesUpgradeRelationshipImpact") / 100;
                Global.getSector().adjustPlayerReputation(new CoreReputationPlugin.RepActionEnvelope(CoreReputationPlugin.RepActions.CUSTOM, impact, (CommMessageAPI)null, (TextPanelAPI)null, false, true, "Change caused by upgrading " + this.entity.getCustomEntitySpec().getDefaultName().toLowerCase()), factionId);
            }
        }

        SectorEntityToken built = loc.addCustomEntity((String)null, (String)null, type, factionId);
        if (this.entity.getOrbit() != null)
        {
            built.setOrbit(this.entity.getOrbit().makeCopy());
        }

        built.setLocation(this.entity.getLocation().x, this.entity.getLocation().y);
        loc.removeEntity(this.entity);
        built.getMemoryWithoutUpdate().set("$originalStableLocation", this.entity);
        if (this.text != null)
        {
            this.removeBuildCosts(type);
            Global.getSoundPlayer().playUISound("ui_objective_constructed", 1.0F, 1.0F);
        }
    }

    public void removeBuildCosts(String type)
    {
        if (!DebugFlags.OBJECTIVES_DEBUG)
        {
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            String[] res = getResources();
            int[] quantities = boggledTools.getQuantitiesForStableLocationConstruction(type);

            for(int i = 0; i < res.length; ++i)
            {
                String commodityId = res[i];
                int quantity = quantities[i];
                cargo.removeCommodity(commodityId, (float)quantity);
            }
        }
    }

    public String[] getResources()
    {
        if(boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled"))
        {
            return new String[]{"heavy_machinery", "metals", "rare_metals", "domain_artifacts"};
        }
        else
        {
            return new String[]{"heavy_machinery", "metals", "rare_metals"};
        }
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
        Iterator var7 = spec.getTags().iterator();

        while(var7.hasNext())
        {
            String tag = (String)var7.next();
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
            o.printEffect(info, 0.0F);
            text.addTooltip();
            o.printNonFunctionalAndHackDescription(text);
        }

        Iterator var15 = this.entity.getContainingLocation().getEntitiesWithTag("objective").iterator();

        while(var15.hasNext())
        {
            SectorEntityToken curr = (SectorEntityToken)var15.next();
            if (curr.hasTag("objective") && curr.getFaction() != null && curr.getFaction().isPlayerFaction() && curr.getCustomEntitySpec() != null)
            {
                CustomCampaignEntityPlugin ccep = curr.getCustomPlugin();
                if (ccep instanceof CampaignObjective)
                {
                    CampaignObjective o = (CampaignObjective)ccep;
                    if (c == o.getClass())
                    {
                        if (this.entity == curr)
                        {
                            text.addPara("Another one in this star system would have no effect beyond providing redundancy in case this one is lost.");
                        }
                        else
                        {
                            text.addPara("There's already " + curr.getCustomEntitySpec().getAOrAn() + " " + curr.getCustomEntitySpec().getNameInText() + " under your control " + "in this star system. Another one would have no effect " + "beyond providing redundancy if one is lost.");
                        }
                        break;
                    }
                }
            }
        }
    }
}