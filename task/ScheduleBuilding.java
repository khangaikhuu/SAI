package task;

import main.Bot;
import bwapi.Color;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class ScheduleBuilding implements Task
{
	Bot root;
	
	int needMinerals, needGas;
	UnitType buildingType;
	Unit onDutyWorkUnit;
	Position scheduledPosition;
	int lastTry;
	
	public ScheduleBuilding(Bot r, UnitType b) {
		root = r;
		buildingType = b;
		needMinerals = b.mineralPrice();
		needGas = b.gasPrice();
		onDutyWorkUnit = util.General.getNearestFreeWorker(root, root.gameInfo.myFirstBase.getPosition());
		root.gameInfo.unitOnDuty[onDutyWorkUnit.getID()] = true;
		lastTry = 0;
	}
	
	@Override
	public String getTaskName() {
		return "ScheduleBuilding " + buildingType;
	}

	@Override
	public int getNeedMinerals() {
		return needMinerals;
	}

	@Override
	public int getNeedGas() {
		return needGas;
	}
	
	@Override
	public void onFrame() {
		
		if(scheduledPosition != null)
			root.game.drawLineMap(onDutyWorkUnit.getPosition().getX(), onDutyWorkUnit.getPosition().getY(), scheduledPosition.getX(), scheduledPosition.getY(), new Color(255, 255, 0));
		
		
		if(root.frameInfo.frameNumber - lastTry > 10 && (onDutyWorkUnit.isIdle() || onDutyWorkUnit.isGatheringMinerals() || onDutyWorkUnit.isGatheringMinerals()))
		{
			// TODO: other race
			if(util.General.isGasBuilding(buildingType))
				scheduledPosition = util.General.buildNear(onDutyWorkUnit, root.gameInfo.myFirstBase.getTilePosition().getX(), root.gameInfo.myFirstBase.getTilePosition().getY(), buildingType);
			else
				scheduledPosition = util.General.buildNear(onDutyWorkUnit, root.gameInfo.myBase.get(0).buildingArea.getX() / 32, root.gameInfo.myBase.get(0).buildingArea.getY() / 32, buildingType);
			lastTry = root.frameInfo.frameNumber;
		}
		
		root.game.drawTextMap(onDutyWorkUnit.getPosition().getX(), onDutyWorkUnit.getPosition().getY(), "Task: " + "ScheduleBuilding (" +  (root.frameInfo.frameNumber - lastTry) + ") " + buildingType);
		
		
	}

	@Override
	public boolean isFinished() {
		
		if(scheduledPosition != null)
			for(Unit u : root.self.getUnits())
				if(u.getType() == buildingType)
				{
					if(u.getPosition().getX() > scheduledPosition.getX() && u.getPosition().getY() > scheduledPosition.getY() &&  u.getPosition().distanceTo(scheduledPosition.getX(), scheduledPosition.getY()) < 100)
					{
						root.gameInfo.unitOnDuty[onDutyWorkUnit.getID()] = false;
						onDutyWorkUnit.move(root.gameInfo.myFirstBase.getPosition());
						return true;
					}
				}
		
		return false;
	}
	
}