package boggled.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import boggled.campaign.econ.boggledTools;

public class boggledAgreusTechMiningEveryFrameScript implements EveryFrameScript
{
    private boolean hasSetDomainEraArtifactSupply = false;
    private float waitTime = .75f;

    public boggledAgreusTechMiningEveryFrameScript() { }

    private void setDomainEraArtifactSupply()
    {
        if(boggledTools.getBooleanSetting("boggledReplaceAgreusTechMiningWithDomainArchaeology"))
        {
            SectorEntityToken agreusPlanet = boggledTools.getPlanetTokenForQuest("Arcadia", "agreus");
            if(agreusPlanet != null)
            {
                MarketAPI agreusMarket = agreusPlanet.getMarket();
                if(agreusMarket != null && agreusMarket.hasIndustry(Industries.TECHMINING) && !agreusMarket.hasIndustry(boggledTools.BoggledIndustries.domainArchaeologyIndustryId) && !agreusMarket.isPlayerOwned())
                {
                    // Don't swap these industries if Everybody loves KoC mod is enabled because this causes compatibility issues
                    // per post in TASC thread on page 142, post #2128 by bodeshmoun.
                    // UPDATE 5/15/23: Not placing the Domain Archaeology building is problematic because buildings that require DEA have no supply.
                    // Adds DEA artifact production to the Techmining industry on Agreus. I can't just add the DA building itself because Agreus
                    // already has four industries due to KoC and adding a fifth reduces stability by 5.

                    if(Global.getSettings().getModManager().isModEnabled("Everybody loves KoC"))
                    {
                        ((BaseIndustry) agreusMarket.getIndustry("techmining")).supply(boggledTools.BoggledCommodities.domainArtifacts, agreusMarket.getSize() - 1);
                    }
                }
            }
        }
    }

    @Override
    public boolean isDone()
    {
        if(hasSetDomainEraArtifactSupply)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean runWhilePaused()
    {
        return true;
    }

    @Override
    public void advance(float amount)
    {
        // Don't do anything while in a menu/dialog
        CampaignUIAPI ui = Global.getSector().getCampaignUI();
        if (Global.getSector().isInNewGameAdvance() || ui.isShowingDialog() || ui.isShowingMenu())
        {
            return;
        }

        if (!hasSetDomainEraArtifactSupply && waitTime <= 0f)
        {
            setDomainEraArtifactSupply();
            hasSetDomainEraArtifactSupply = true;
        }
        else
        {
            waitTime = waitTime - amount;
        }
    }
}