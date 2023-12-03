package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.IndustryOptionProvider;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import data.campaign.econ.boggledTools;
import java.util.Iterator;

public class BoggledTascPlugin extends BaseModPlugin
{
    public void applyStationSettingsToAllStationsInSector()
    {
        if(boggledTools.getBooleanSetting("boggledApplyStationSettingsToAllStationsInSector"))
        {
            Iterator allSystems = Global.getSector().getStarSystems().iterator();
            while(allSystems.hasNext())
            {
                StarSystemAPI system = (StarSystemAPI) allSystems.next();
                Iterator allMarketsInSystem = Global.getSector().getEconomy().getMarkets(system).iterator();
                while(allMarketsInSystem.hasNext())
                {
                    MarketAPI market = (MarketAPI) allMarketsInSystem.next();
                    SectorEntityToken primaryEntity = market.getPrimaryEntity();
                    if(primaryEntity != null && primaryEntity.hasTag("station"))
                    {
                        //Cramped Quarters also controls global hazard and accessibility modifications
                        //even if Cramped Quarters itself is disabled
                        if(!market.hasCondition("cramped_quarters"))
                        {
                            market.addCondition("cramped_quarters");
                        }

                        //Some special items require "no_atmosphere" condition on market to be installed
                        //Stations by default don't meet this condition because they don't have the "no_atmosphere" condition
                        //Combined with market_conditions.csv overwrite, this will give stations no_atmosphere while
                        //hiding all effects from the player and having no impact on the economy or hazard rating
                        if(!market.hasCondition("no_atmosphere"))
                        {
                            market.addCondition("no_atmosphere");
                            market.suppressCondition("no_atmosphere");
                        }
                    }
                }
            }
        }
    }

    public void applyTerraformingAbilitiesPerSettingsFile()
    {
        if(boggledTools.getBooleanSetting("boggledTerraformingContentEnabled"))
        {
            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_open_terraforming_control_panel"))
            {
                Global.getSector().getCharacterData().addAbility("boggled_open_terraforming_control_panel");
            }
        }
        else
        {
            Global.getSector().getCharacterData().removeAbility("boggled_open_terraforming_control_panel");
        }
    }

    public void applyStationConstructionAbilitiesPerSettingsFile()
    {
        if(boggledTools.getBooleanSetting("boggledStationConstructionContentEnabled"))
        {
            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_construct_astropolis_station"))
            {
                if(boggledTools.getBooleanSetting("boggledAstropolisEnabled"))
                {
                    Global.getSector().getCharacterData().addAbility("boggled_construct_astropolis_station");
                }
            }
            else
            {
                if(!boggledTools.getBooleanSetting("boggledAstropolisEnabled"))
                {
                    Global.getSector().getCharacterData().removeAbility("boggled_construct_astropolis_station");
                }
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_construct_mining_station"))
            {
                if(boggledTools.getBooleanSetting("boggledMiningStationEnabled"))
                {
                    Global.getSector().getCharacterData().addAbility("boggled_construct_mining_station");
                }
            }
            else
            {
                if(!boggledTools.getBooleanSetting("boggledMiningStationEnabled"))
                {
                    Global.getSector().getCharacterData().removeAbility("boggled_construct_mining_station");
                }
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_construct_siphon_station"))
            {
                if(boggledTools.getBooleanSetting("boggledSiphonStationEnabled"))
                {
                    Global.getSector().getCharacterData().addAbility("boggled_construct_siphon_station");
                }
            }
            else
            {
                if(!boggledTools.getBooleanSetting("boggledSiphonStationEnabled"))
                {
                    Global.getSector().getCharacterData().removeAbility("boggled_construct_siphon_station");
                }
            }

            if (!Global.getSector().getPlayerFleet().hasAbility("boggled_colonize_abandoned_station"))
            {
                if(boggledTools.getBooleanSetting("boggledStationColonizationEnabled"))
                {
                    Global.getSector().getCharacterData().addAbility("boggled_colonize_abandoned_station");
                }
            }
            else
            {
                if(!boggledTools.getBooleanSetting("boggledStationColonizationEnabled"))
                {
                    Global.getSector().getCharacterData().removeAbility("boggled_colonize_abandoned_station");
                }
            }
        }
        else
        {
            Global.getSector().getCharacterData().removeAbility("boggled_construct_astropolis_station");
            Global.getSector().getCharacterData().removeAbility("boggled_construct_mining_station");
            Global.getSector().getCharacterData().removeAbility("boggled_construct_siphon_station");
            Global.getSector().getCharacterData().removeAbility("boggled_colonize_abandoned_station");
        }
    }

    public void applyDomainArchaeologySettings()
    {
        //Enable/disable Domain-tech content
        if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled"))
        {
            if(Global.getSector().getFaction("luddic_church") != null && !Global.getSector().getFaction("luddic_church").isIllegal("domain_artifacts"))
            {
                Global.getSector().getFaction("luddic_church").makeCommodityIllegal("domain_artifacts");
            }

            if(Global.getSector().getFaction("luddic_path") != null && !Global.getSector().getFaction("luddic_path").isIllegal("domain_artifacts"))
            {
                Global.getSector().getFaction("luddic_path").makeCommodityIllegal("domain_artifacts");
            }

            Global.getSettings().getCommoditySpec("domain_artifacts").getTags().clear();

            if(boggledTools.getBooleanSetting("boggledReplaceAgreusTechMiningWithDomainArchaeology"))
            {
                SectorEntityToken agreusPlanet = boggledTools.getPlanetTokenForQuest("Arcadia", "agreus");
                if(agreusPlanet != null)
                {
                    MarketAPI agreusMarket = agreusPlanet.getMarket();
                    if(agreusMarket != null && agreusMarket.hasIndustry(Industries.TECHMINING) && !agreusMarket.hasIndustry("BOGGLED_DOMAIN_ARCHAEOLOGY") && !agreusMarket.isPlayerOwned())
                    {
                        // See boggledAgreusTechMiningEveryFrameScript for solution to Agreus Everybody loves KoC Techmining/Domain Archaeology issue
                        if(!Global.getSettings().getModManager().isModEnabled("Everybody loves KoC"))
                        {
                            agreusMarket.addIndustry("BOGGLED_DOMAIN_ARCHAEOLOGY");
                            agreusMarket.removeIndustry(Industries.TECHMINING, null, false);
                        }
                        else
                        {
                            Global.getSector().addTransientScript(new boggledAgreusTechMiningEveryFrameScript());
                        }
                    }
                }
            }
        }
        else
        {
            Global.getSettings().getCommoditySpec("domain_artifacts").getTags().add("nonecon");
        }
    }

    public void addDomainTechBuildingsToVanillaColonies()
    {
        // Check to avoid null pointer exception if player has modified/randomized sector
        if(Global.getSector() == null || Global.getSector().getStarSystem("Askonia") == null)
        {
            return;
        }

        if(!Global.getSector().getPlayerPerson().hasTag("boggledDomainTechBuildingPlacementFinished"))
        {
            // Add Genelab on Volturn
            if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled") && boggledTools.getBooleanSetting("boggledAddDomainTechBuildingsToVanillaColonies"))
            {
                SectorEntityToken volturnPlanet = boggledTools.getPlanetTokenForQuest("Askonia", "volturn");
                if(volturnPlanet != null)
                {
                    MarketAPI volturnMarket = volturnPlanet.getMarket();
                    if(volturnMarket != null && !volturnMarket.hasIndustry("BOGGLED_GENELAB"))
                    {
                        volturnMarket.addIndustry("BOGGLED_GENELAB");
                    }
                }
            }

            // Add LLN on Fikenhild
            if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled") && boggledTools.getBooleanSetting("boggledAddDomainTechBuildingsToVanillaColonies"))
            {
                SectorEntityToken fikenhildPlanet = boggledTools.getPlanetTokenForQuest("Westernesse", "fikenhild");
                if(fikenhildPlanet != null)
                {
                    MarketAPI fikenhildMarket = fikenhildPlanet.getMarket();
                    if(fikenhildMarket != null && !fikenhildMarket.hasIndustry("BOGGLED_LIMELIGHT_NETWORK"))
                    {
                        fikenhildMarket.addIndustry("BOGGLED_LIMELIGHT_NETWORK");
                    }
                }
            }

            // Add GPA on Ancyra
            if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled") && boggledTools.getBooleanSetting("boggledAddDomainTechBuildingsToVanillaColonies"))
            {
                SectorEntityToken ancyraPlanet = boggledTools.getPlanetTokenForQuest("Galatia", "ancyra");
                if(ancyraPlanet != null)
                {
                    MarketAPI ancyraMarket = ancyraPlanet.getMarket();
                    if(ancyraMarket != null && !ancyraMarket.hasIndustry("BOGGLED_GPA"))
                    {
                        ancyraMarket.addIndustry("BOGGLED_GPA");
                    }
                }
            }

            Global.getSector().getPlayerPerson().addTag("boggledDomainTechBuildingPlacementFinished");
        }
    }

    public void replaceCryosanctums()
    {
        // Replace all Cryosanctums
        if(!Global.getSector().getPlayerPerson().hasTag("boggledCryosanctumReplacementFinished") && boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledDomainArchaeologyEnabled") && boggledTools.getBooleanSetting("boggledCryosanctumReplaceEverywhere"))
        {
            for(StarSystemAPI system : Global.getSector().getStarSystems())
            {
                for(MarketAPI market : Global.getSector().getEconomy().getMarkets(system))
                {
                    if(market != null && market.hasIndustry(Industries.CRYOSANCTUM) && !market.hasIndustry("BOGGLED_CRYOSANCTUM"))
                    {
                        market.removeIndustry(Industries.CRYOSANCTUM, null, false);
                        market.addIndustry("BOGGLED_CRYOSANCTUM");
                    }
                }
            }

            Global.getSector().getPlayerPerson().addTag("boggledCryosanctumReplacementFinished");
        }
    }

    public void enablePlanetKiller()
    {
        if(boggledTools.getBooleanSetting("boggledDomainTechContentEnabled") && boggledTools.getBooleanSetting("boggledPlanetKillerEnabled"))
        {
            // PK weapons are deployed via ability, not a ground raid.
            // I left the mostly finished code for ground raid deployment in here in case I want to enable it in a future update.
            // Global.getSector().getListenerManager().addListener(new boggledPlanetKillerGroundRaidObjectiveListener());

            Global.getSector().getCharacterData().addAbility("boggled_deploy_planet_killer");
        }
    }

    @Override
    public void onNewGame()
    {
        applyStationSettingsToAllStationsInSector();
    }

    public void afterGameSave()
    {
        enablePlanetKiller();

        applyStationSettingsToAllStationsInSector();

        applyStationConstructionAbilitiesPerSettingsFile();

        applyTerraformingAbilitiesPerSettingsFile();

        applyDomainArchaeologySettings();

        replaceCryosanctums();

        addDomainTechBuildingsToVanillaColonies();
    }

    public void beforeGameSave()
    {
        Global.getSector().getCharacterData().removeAbility("boggled_construct_astropolis_station");
        Global.getSector().getCharacterData().removeAbility("boggled_construct_mining_station");
        Global.getSector().getCharacterData().removeAbility("boggled_construct_siphon_station");
        Global.getSector().getCharacterData().removeAbility("boggled_colonize_abandoned_station");

        Global.getSector().getCharacterData().removeAbility("boggled_deploy_planet_killer");

        Global.getSector().getCharacterData().removeAbility("boggled_open_terraforming_control_panel");

        Global.getSettings().getCommoditySpec("domain_artifacts").getTags().clear();

        Global.getSector().getListenerManager().removeListenerOfClass(boggledPlanetKillerGroundRaidObjectiveListener.class);
    }

    public void onGameLoad(boolean newGame)
    {
        enablePlanetKiller();

        applyStationSettingsToAllStationsInSector();

        applyStationConstructionAbilitiesPerSettingsFile();

        applyTerraformingAbilitiesPerSettingsFile();

        applyDomainArchaeologySettings();

        addDomainTechBuildingsToVanillaColonies();
    }
}