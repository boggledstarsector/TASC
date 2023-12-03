package data.scripts;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.graid.AbstractGoalGroundRaidObjectivePluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.campaign.econ.boggledTools;

public class boggledPlanetKillerGroundRaidObjectivePlugin extends AbstractGoalGroundRaidObjectivePluginImpl {

	public static int XP_GAIN = 10000;

	public boggledPlanetKillerGroundRaidObjectivePlugin(MarketAPI market)
	{
		super(market, RaidDangerLevel.EXTREME);
	}
	
	public String getName() {
		return "Deploy Planet-Killer Device";
	}

	@Override
	public String getIconName()
	{
		return Global.getSettings().getSpecialItemSpec("boggled_planetkiller").getIconName();
	}

	public int performRaid(CargoAPI loot, Random random, float lootMult, TextPanelAPI text)
	{
		if (marinesAssigned <= 0) return 0;

		CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
		playerCargo.removeItems(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData("boggled_planetkiller", null), 1f);

		boggledTools.applyPlanetKiller(this.market);

		// TODO - other than the problems below, this code should work fine.
		// Need to correct tooltip - it says stability reduced, reputation damaged like a normal raid.

		return XP_GAIN;
	}
	
	@Override
	public boolean hasTooltip() {
		return true;
	}

	@Override
	public void createTooltip(TooltipMakerAPI t, boolean expanded)
	{
		t.addPara("Detonate a planet-killer device on " + market.getName() + ". This will result in almost universal condemnation by the inhabitants of the Sector.", 0f);
	}
}









