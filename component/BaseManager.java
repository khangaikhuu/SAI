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
			if(util.General.isBase(u))
			{
				BaseInfo b = new BaseInfo();
				b.base = u;
				root.gameInfo.myBase.add(b);
				if(root.gameInfo.myFirstBase == null)
					root.gameInfo.myFirstBase = u;
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
			cx *= - (10 * 32 / r);
			cy *= - (10 * 32 / r);
			
			root.game.drawCircleMap((int)(cx + mx), (int)(cy + my), 20, new Color(255, 0, 0));
			b.buildingArea = new Position((int)(cx + mx), (int)(cy + my));
			
		}
	}

	@Override
	public void onFrame() {
		
		calculateBaseInfo();
		
		int baseNumber = 0;
		for(BaseInfo b : root.gameInfo.myBase)
		{
			
			root.game.drawCircleMap(b.buildingArea.getX(), b.buildingArea.getY(), Radius, new Color(0, 0, 255));
			
			baseNumber += 1;
			root.game.drawCircleMap(b.base.getPosition().getX(), b.base.getPosition().getY(), Radius, new Color(0, 255, 0));
			int cntGatheringMinerals = 0, cntGatheringGas = 0;
			
			int countGasWorker = 0;
			for(Unit u : b.worker)
			{
				if(u.isGatheringGas())
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
				
				//TODO: what if some gas-worker died.
				if(root.gameInfo.unitOnDuty[u.getID()] == false && b.gasStation.size() > 0 && countGasWorker < 3)
				{
					u.gather(b.gasStation.get(0));
					countGasWorker += 1;
				}
				
			}
			root.debug.addDebugInfo("	+Base #" + baseNumber + ": worker(Minerals/Gas/All) = " + cntGatheringMinerals + " / " + cntGatheringGas + " / " + b.worker.size());
			if(b.base.isTraining() == false && root.frameInfo.remainMinerals >= 50 && b.worker.size() < 25)
			{
				b.base.train(UnitType.Protoss_Probe);
			}
		}
	}

	@Override
	public String getComponentName() {
		return "BaseManager";
	}
}
