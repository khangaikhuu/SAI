package util;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import main.Bot;

public class General {
    
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
    
	static public boolean isAlive(Unit u)
    {
    	if(Math.abs(u.getPosition().getX()) + Math.abs(u.getPosition().getY()) > 10000000)
    		return false;
    	return true;
    }
    
	static public Unit getNearestFreeWorker(Bot root, Position p)
    {
    	Unit ret = null;
    	for(Unit u : root.self.getUnits())
    		if(isWorker(u) && (u.isIdle() || u.isGatheringMinerals()) && root.gameInfo.unitOnDuty[u.getID()] == false)
    		{
    			//if(isAlive(u) == false) continue;
    			if(ret == null)
    				ret = u;
    			else {
    				if(u.getPosition().distanceTo(p.getX(), p.getY()) < ret.getPosition().distanceTo(p.getX(), p.getY()))
    					ret = u;
				}
    		}
    	
    	return ret;
    }
    
	static public boolean isBattleUnit(Unit u)
    {
    	if(u.getType() == UnitType.Protoss_Zealot) return true;
    	if(u.getType() == UnitType.Protoss_Dragoon) return true;
    	if(u.getType() == UnitType.Protoss_Dark_Templar) return true;
    	return false;
    }
    
	static public boolean isGasBuilding(UnitType buildingType)
    {
    	if(buildingType == UnitType.Protoss_Assimilator) return true;
    	if(buildingType == UnitType.Terran_Refinery) return true;
    	if(buildingType == UnitType.Zerg_Extractor) return true;
    	return false;
    }
	
	static public Position buildNear(Unit builder, int x, int y, UnitType buildingType)
    {
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
						return pos;
				}
			}
		}
		return null;
    }
}
