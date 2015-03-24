package task;

import info.BaseInfo;
import main.Bot;
import bwapi.Color;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class ScheduleBuilding implements Task
{
	Bot root;
	int Radius = 300;
	int needMinerals, needGas;
	UnitType buildingType;
	Unit onDutyWorkUnit;
	Position scheduledPosition;
	int lastTry;
	public BaseInfo base;
	boolean arriveThatPosition;
	
	public ScheduleBuilding(Bot r, UnitType b, BaseInfo baseinfo) {
		base = baseinfo;
		root = r;
		buildingType = b;
		needMinerals = b.mineralPrice();
		needGas = b.gasPrice();
		onDutyWorkUnit = util.General.getNearestFreeWorker(root, base.base.getPosition(), Radius);
		if(onDutyWorkUnit != null)
			root.gameInfo.unitOnDuty[onDutyWorkUnit.getID()] = true;
		lastTry = 0;
		arriveThatPosition = false;
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
		
		if(onDutyWorkUnit == null || util.General.isAlive(root, onDutyWorkUnit) == false)
		{
			onDutyWorkUnit = util.General.getNearestFreeWorker(root, base.base.getPosition(), Radius);
			if(onDutyWorkUnit != null)
				root.gameInfo.unitOnDuty[onDutyWorkUnit.getID()] = true;
		}
		
		if(onDutyWorkUnit == null || util.General.isAlive(root, onDutyWorkUnit) == false)
			return;
		
		if(scheduledPosition != null)
			root.game.drawLineMap(onDutyWorkUnit.getPosition().getX(), onDutyWorkUnit.getPosition().getY(), scheduledPosition.getX(), scheduledPosition.getY(), new Color(255, 255, 0));
		
		if(buildingType == UnitType.Protoss_Nexus && arriveThatPosition == false)
		{
			if(onDutyWorkUnit.isMoving() == false)
				onDutyWorkUnit.move(util.General.getNextBasePosition(root));
			if(onDutyWorkUnit.getPoint().getDistance(util.General.getNextBasePosition(root)) < 20)
				arriveThatPosition = true;
			
			return;
		}
		
		if(root.frameInfo.frameNumber - lastTry > 400 || (root.frameInfo.frameNumber - lastTry > 10 && (onDutyWorkUnit.isIdle() || onDutyWorkUnit.isGatheringMinerals() || onDutyWorkUnit.isGatheringGas())))
		{
			if(buildingType == UnitType.Protoss_Nexus)
				scheduledPosition = util.General.buildNear(onDutyWorkUnit, util.General.getNextBasePosition(root), buildingType);
			else if(util.General.isGasBuilding(buildingType) || buildingType == UnitType.Protoss_Photon_Cannon)
				scheduledPosition = util.General.buildNear(onDutyWorkUnit, base.base.getPosition(), buildingType);
			else
				scheduledPosition = util.General.buildNear(onDutyWorkUnit, base.buildingArea, buildingType);
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
						if(onDutyWorkUnit == null || util.General.isAlive(root, onDutyWorkUnit) == false)
							return true;
						root.gameInfo.unitOnDuty[onDutyWorkUnit.getID()] = false;
						onDutyWorkUnit.move(base.base.getPosition());
						return true;
					}
				}
		
		return false;
	}
	
}