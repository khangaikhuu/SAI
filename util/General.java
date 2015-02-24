package util;

import task.ScheduleBuilding;
import task.Task;
import info.BaseInfo;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import main.Bot;

public class General {
    
	static public Bot root;
	
	static public int countBuildingIncludeScheduled(Bot root, UnitType type)
	{
		long prev = System.nanoTime();
		
		int ret = 0;
		for(Unit u : root.self.getUnits())
		{
			if(u.getType() == type)
				ret ++;
		}
		for(Task t : root.currentTasks)
		{
			if(t.getTaskName().equals("ScheduleBuilding " + type))
				ret ++;
		}
		
		root.debug.debugLong += System.nanoTime() - prev;
		
		return ret;
	}

	static public int countBuildingIncludeScheduledInRange(Bot root, UnitType type, Position p, int r)
	{
		long prev = System.nanoTime();
		

		int ret = 0;
		for(Unit u : root.self.getUnits())
		{
			if(u.getType() == type && u.getPosition().getDistance(p) <= r)
				ret ++;
		}
		for(Task t : root.currentTasks)
		{
			if(t.getTaskName().equals("ScheduleBuilding " + type) && ((ScheduleBuilding)t).base.base.getPosition().getDistance(p) <= r)
				ret ++;
		}
		root.debug.debugLong += System.nanoTime() - prev;
		return ret;
	}
	
	static public boolean isBase(Unit u)
    {
    	if(u.getType() == UnitType.Protoss_Nexus) return true;
    	if(u.getType() == UnitType.Terran_Command_Center) return true;
    	if(u.getType() == UnitType.Zerg_Hatchery) return true;
    	// TODO: Zerg 2nd / 3rd base?
    	return false;
    }
    
	static public boolean isWorker(Unit u)
    {
    	if(u.getType() == UnitType.Protoss_Probe) return true;
    	if(u.getType() == UnitType.Terran_SCV) return true;
    	if(u.getType() == UnitType.Zerg_Drone) return true;
    	return false;
    }
    
	static public boolean isAlive(Bot root, Unit u)
    {
    	return root.frameInfo.isAlive[u.getID()];
    }
    
	static public Unit getNearestFreeWorker(Bot root, Position p, int r)
    {
		long prev = System.nanoTime();
    	Unit ret = null;
    	for(Unit u : root.self.getUnits())
    		if(isWorker(u) && (u.isIdle() || u.isGatheringMinerals()) && root.gameInfo.unitOnDuty[u.getID()] == false && u.getPosition().distanceTo(p.getX(), p.getY()) < r)
    		{
    			if(u.isBeingConstructed())
    				continue;
    			
    			if(ret == null)
    				ret = u;
    			else {
    				if(u.getPosition().distanceTo(p.getX(), p.getY()) < ret.getPosition().distanceTo(p.getX(), p.getY()))
    					ret = u;
				}
    		}
    	if(ret != null)
    		root.gameInfo.unitOnDuty[ret.getID()] = true;
    	root.debug.debugLong += System.nanoTime() - prev;
    	return ret;
    }
    
	static public Position getNextBasePosition(Bot root)
	{
		long prev = System.nanoTime();
		double minDist = 10000000;
		Position ret = null;
		
		for(BaseLocation b : BWTA.getBaseLocations())
		{
			boolean alreadyMyBase = false;
			
			if(b.isMineralOnly())
				continue;
			
			for(Unit u : root.self.getUnits())
			{
				if(u.getType() == UnitType.Protoss_Nexus)
					if(u.getPosition().getDistance(b.getPosition()) < 500)
						alreadyMyBase = true;
			}
			
			
			if(alreadyMyBase)
				continue;
			if(b.getPosition().getDistance(root.gameInfo.myFirstBase().getPosition()) < minDist)
			{
				ret = new Position(b.getTilePosition().getX() * 32, b.getTilePosition().getY() * 32);
				minDist = b.getPosition().getDistance(root.gameInfo.myFirstBase().getPosition());
			}
			
		}
		root.debug.debugLong += System.nanoTime() - prev;
		return ret;
	}
	
	static public boolean isBattleUnit(Unit u)
    {
    	if(u.getType() == UnitType.Protoss_Zealot) return true;
    	if(u.getType() == UnitType.Protoss_Dragoon) return true;
    	if(u.getType() == UnitType.Protoss_Dark_Templar) return true;
    	if(u.getType() == UnitType.Protoss_Carrier) return true;
    	return false;
    }
    
	static public boolean isGasBuilding(UnitType buildingType)
    {
    	if(buildingType == UnitType.Protoss_Assimilator) return true;
    	if(buildingType == UnitType.Terran_Refinery) return true;
    	if(buildingType == UnitType.Zerg_Extractor) return true;
    	return false;
    }
	
	static public Position buildNear(Unit builder, Position ps, UnitType buildingType)
    {
		long prev = System.nanoTime();
		if(ps == null)
			return null;
		int x = ps.getX() / 32;
		int y = ps.getY() / 32;
		int[] dx = {0, 1, 0, -1};
		int[] dy = {1, 0, -1, 0};
		for(int d = 0; d <= 10 ; d +=1)
		{
			for(int f = 0; f < 4; f++)
			{
				int nf = (f + 1) % 4;
				for(int v = -d; v <= d; v++)
				{
					int nx = x + dx[f] * v + dx[nf] * d;
					int ny = y + dy[f] * v + dy[nf] * d;
					TilePosition p = new TilePosition(nx, ny);
					Position pos = new Position(p.getX() * 32, p.getY() * 32);
					//game.drawLineMap(pos.getX(), pos.getY(), builder.getPosition().getX(), builder.getPosition().getY(), new Color(255, 0, 0));
					if(builder.hasPath(pos) && builder.build(p, buildingType))
					{
						root.debug.debugLong += System.nanoTime() - prev;
						return pos;
					}
				}
			}
		}
		root.debug.debugLong += System.nanoTime() - prev;
		return null;
    }

	public static boolean isNotBeginning(Bot root) {
		if(root.self.supplyTotal() >= 30 * 2)
			return true;
		return false;
	}

	public static double distanceToMyBase(Bot root, Position position) {
		double r = 10000000;
		for(BaseInfo b : root.gameInfo.myBase)
		{
			r = Math.min(r, b.base.getPosition().getDistance(position));
		}
		return r;
	}

	public static int countBuildingInRange(Bot root, UnitType type, Position p, int r)
	{
		long prev = System.nanoTime();
		int ret = 0;
		for(Unit u : root.self.getUnits())
		{
			if(u.getType() == type && u.getPosition().getDistance(p) <= r)
				ret ++;
		}
		root.debug.debugLong += System.nanoTime() - prev;
		return ret;
	}

	static public int countBuilding(Bot root, UnitType type)
	{
		long prev = System.nanoTime();
		int ret = 0;
		for(Unit u : root.self.getUnits())
		{
			if(u.getType() == type)
				ret ++;
		}
		root.debug.debugLong += System.nanoTime() - prev;
		return ret;
	}

	public static boolean haveMyUnitAround(Bot root, Position p) {
		long prev = System.nanoTime();
		if(p == null)
			return false;
		for(Unit u : root.self.getUnits())
		{
			if(u.getPosition().getDistance(p) < 300)
			{
				root.debug.debugLong += System.nanoTime() - prev;
				return true;
			}
		}
		root.debug.debugLong += System.nanoTime() - prev;
		return false;
	}
}
