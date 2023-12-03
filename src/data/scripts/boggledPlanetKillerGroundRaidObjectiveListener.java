package data.scripts;

import java.util.List;
import java.util.Map;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.GroundRaidObjectivesListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.util.Misc;
import data.campaign.econ.boggledTools;

public class boggledPlanetKillerGroundRaidObjectiveListener implements GroundRaidObjectivesListener
{
	@Override
	public void modifyRaidObjectives(MarketAPI market, SectorEntityToken entity, List<GroundRaidObjectivePlugin> objectives, MarketCMD.RaidType type, int marineTokens, int priority)
	{
		if (priority == 0 && market != null && type == MarketCMD.RaidType.VALUABLE)
		{
			if(!Misc.isStoryCritical(market) || boggledTools.getBooleanSetting("boggledPlanetKillerAllowDestructionOfColoniesMarkedAsEssentialForQuests"))
			{
				CargoAPI playerCargo = Global.getSector().getPlayerFleet().getCargo();
				if(playerCargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData("boggled_planetkiller", null)) > 0)
				{
					objectives.add(new boggledPlanetKillerGroundRaidObjectivePlugin(market));
				}
			}
		}
	}

	@Override
	public void reportRaidObjectivesAchieved(RaidResultData raidResultData, InteractionDialogAPI interactionDialogAPI, Map<String, MemoryAPI> map)
	{

	}
}









