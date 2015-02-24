package component;

import main.Bot;
import info.BaseInfo;
import component.Component;
import bwapi.Color;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class BaseManager implements Component
{
	Bot root;
	
	int Radius = 300;
	
	public BaseManager(Bot r) {
		root = r;
	}
	
	void calculateBaseInfo() {
		root.gameInfo.myBase.clear();
		
		for(Unit u : root.self.getUnits())
		{
			if(util.General.isBase(u) && u.isBeingConstructed() == false)
			{
				boolean haveMineral = false;
                for (Unit neutralUnit : root.game.neutral().getUnits()) {
                    if (neutralUnit.getType().isMineralField() && neutralUnit.getPosition().getDistance(u.getPosition()) < Radius)
                    {
                    	haveMineral = true;
                    }
                }
				if(!haveMineral)
					continue;
				BaseInfo b = new BaseInfo();
				b.base = u;
				root.gameInfo.myBase.add(b);
			}
		}
		
		for(BaseInfo b : root.gameInfo.myBase)
		{
			for(Unit u : root.self.getUnits())
				if(u.distanceTo(b.base.getPosition().getX(), b.base.getPosition().getY()) <= Radius)
    			{
    				if(util.General.isWorker(u))
    				{
    					b.worker.add(u);
    				}
    				if(u.getType() == UnitType.Protoss_Assimilator && u.isBeingConstructed() == false)
    					b.gasStation.add(u);
    			}
			float nMineral = 0;
			float sumX = 0, sumY = 0;
			for (Unit neutralUnit : root.game.neutral().getUnits()) {
                if (neutralUnit.getType().isMineralField()) {
                	if(neutralUnit.getPosition().getDistance(b.base.getPosition()) < Radius)
                	{
                		nMineral += 1;
                		sumX += neutralUnit.getPosition().getX();
                		sumY += neutralUnit.getPosition().getY();
                		
                	}
                }
                if (neutralUnit.getType() == UnitType.Resource_Vespene_Geyser) {
                	if(neutralUnit.getPosition().getDistance(b.base.getPosition()) < Radius)
                	{
                		float factor = (float) 1.5;
                		nMineral += factor;
                		sumX += factor * neutralUnit.getPosition().getX();
                		sumY += factor * neutralUnit.getPosition().getY();
                		root.game.drawCircleMap(neutralUnit.getPosition().getX(), neutralUnit.getPosition().getY(), 10, new Color(255, 0, 0));
                		
                	}
                }
			}
			float cx = (sumX / nMineral), cy = (sumY / nMineral);
			float mx = b.base.getPosition().getX(), my = b.base.getPosition().getY();
			cx -= mx;
			cy -= my;
			float r = (float) Math.sqrt(cx * cx + cy * cy);
			cx *= - (7 * 32 / r);
			cy *= - (7 * 32 / r);
			b.buildingArea = new Position((int)(cx + mx), (int)(cy + my));
			root.game.drawCircleMap(b.buildingArea.getX(), b.buildingArea.getY(), 20, new Color(255, 0, 0));
		}
	}

	@Override
	public void onFrame() {
		
		calculateBaseInfo();
		
		int baseNumber = 0;
		
		
		for(BaseInfo b : root.gameInfo.myBase)
		{
			//TODO : do not use unitOnDuty, create a new one.
			if(root.gameInfo.unitOnDuty[b.base.getID()] == false && b.gasStation.size() == 0 && util.General.isNotBeginning(root) == true)
			{
				task.ScheduleBuilding t = new task.ScheduleBuilding(root, UnitType.Protoss_Assimilator, b);
				root.currentTasks.add(t);
				root.gameInfo.unitOnDuty[b.base.getID()] = true;
			}
			
			
			
			root.game.drawCircleMap(b.buildingArea.getX(), b.buildingArea.getY(), Radius, new Color(0, 0, 255));
			
			baseNumber += 1;
			root.game.drawCircleMap(b.base.getPosition().getX(), b.base.getPosition().getY(), Radius, new Color(0, 255, 0));
			int cntGatheringMinerals = 0, cntGatheringGas = 0;
			
			int countGasWorker = 0;
			for(Unit u : b.worker)
			{
				if(u.isGatheringGas() && u.isIdle() == false && util.General.isAlive(root, u))
					countGasWorker += 1;
			}
			
			for(Unit u : b.worker)
			{
				if(u.isGatheringMinerals())
					cntGatheringMinerals += 1;
				if(u.isGatheringGas())
					cntGatheringGas += 1;
				
				if(u.isIdle() && root.gameInfo.unitOnDuty[u.getID()] == false)
				{
	                Unit closestMineral = null;
	                //find the closest mineral
	                for (Unit neutralUnit : root.game.neutral().getUnits()) {
	                    if (neutralUnit.getType().isMineralField()) {
	                        if (closestMineral == null || u.getDistance(neutralUnit) < u.getDistance(closestMineral)) {
	                            closestMineral = neutralUnit;
	                        }
	                    }
	                }
	                //if a mineral patch was found, send the drone to gather it
	                if (closestMineral != null) {
	                    u.gather(closestMineral, false);
	                }
				}
			}
			
			while(b.gasStation.size() > 0 && countGasWorker < 3 && b.worker.size() > 5)
			{
				Unit u = util.General.getNearestFreeWorker(root, b.base.getPosition(), Radius);
				if(u == null)
					break;
				Unit station = b.gasStation.get(0);
				if(station == null)
					break;
				u.gather(station);
				countGasWorker += 1;
			}
			
			root.debug.addDebugInfo("	+Base #" + baseNumber + ": worker(Minerals/Gas/All) = " + cntGatheringMinerals + " / " + cntGatheringGas + " / " + b.worker.size());
			if(b.base.isTraining() == false && root.frameInfo.remainMinerals >= 50 && b.worker.size() < 25)
			{
				b.base.train(UnitType.Protoss_Probe);
			}
			
			if(baseNumber > 1)
			{
				if(util.General.isNotBeginning(root) && util.General.countBuildingIncludeScheduledInRange(root, UnitType.Protoss_Pylon, b.base.getPosition(), Radius) == 0)
				{
					task.ScheduleBuilding t = new task.ScheduleBuilding(root, UnitType.Protoss_Pylon, b);
					root.currentTasks.add(t);
				}
				else if(util.General.isNotBeginning(root) && util.General.countBuildingInRange(root, UnitType.Protoss_Pylon, b.base.getPosition(), Radius) > 0 && util.General.countBuildingIncludeScheduledInRange(root, UnitType.Protoss_Photon_Cannon, b.base.getPosition(), Radius) < 6)
				{
					task.ScheduleBuilding t = new task.ScheduleBuilding(root, UnitType.Protoss_Photon_Cannon, b);
					root.currentTasks.add(t);
				}
			}
		}
		
		for(Unit u : root.self.getUnits())
		{
			if(util.General.isWorker(u) && u.isIdle())
				u.move(root.gameInfo.myBase.get(root.gameInfo.myBase.size()-1).base.getPosition());
		}
		
		
		
	}

	@Override
	public String getComponentName() {
		return "BaseManager";
	}
}
