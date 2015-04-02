package computation;

import task.Task;
import bwapi.*;
import main.Bot;

public class General {
	
	Bot root;
	
	public General(Bot r) {
		root = r;
	}
		
	public int countUnit(UnitType u, boolean includeTask, boolean includeBuilding, boolean includeFinish)
	{
		int ret = 0;
		if(includeTask)
			for(Task t : root.listOfTasks)
				ret += t.provideUnit(u);
		if(root.info.myUnits.get(u) != null)
		{
			if(includeBuilding && includeFinish)
				ret += root.info.myUnits.get(u).size();
			else 
			{
				for(Unit t : root.info.myUnits.get(u))
				{
					if(t.isBeingConstructed())
					{
						if(includeBuilding)
							ret ++;
					}
					else
					{
						if(includeFinish)
							ret ++;
					}
				}
			}
		}
		return ret;
	}
	
	public TilePosition getNearestTilePosition(Position p)
	{
		int w = root.game.mapWidth();
		int h = root.game.mapHeight();
		int x = (p.getX() + 16) / 32;
		int y = (p.getY() + 16) / 32;
		if(x < 0) x = 0;
		if(x >= w) x = w-1;
		if(y < 0) y = 0;
		if(y >= h) y = h-1;
		return new TilePosition(x, y);
	}
	
	public boolean isBase(UnitType t)
	{
		if(t == UnitType.Protoss_Nexus) return true;
		if(t == UnitType.Terran_Command_Center) return true;
		if(t == UnitType.Zerg_Hatchery) return true;
		if(t == UnitType.Zerg_Hive) return true;
		if(t == UnitType.Zerg_Lair) return true;
		return false;
	}
	
	public Position toPosition(TilePosition t)
	{
		return new Position(t.getX() * 32, t.getY() * 32);
	}
	
	public Position getMyFirstBasePosition()
	{
		return bwta.BWTA.getStartLocation(root.self).getPosition();
	}
	
	public boolean isGasBuilding(UnitType targetStruct) {
		if(targetStruct == UnitType.Protoss_Assimilator) return true;
		if(targetStruct == UnitType.Terran_Refinery) return true;
		if(targetStruct == UnitType.Zerg_Extractor) return true;
		return false;
	}

	public Position toBuildingCenter(TilePosition buildPosition, UnitType targetStruct) {
		if(root.util.isGasBuilding(targetStruct))
			return new Position(buildPosition.getX() * 32 - 16, buildPosition.getY() * 32 - 16);
		return new Position(buildPosition.getX() * 32 + targetStruct.tileWidth() * 16 ,
				            buildPosition.getY() * 32 + targetStruct.tileHeight() * 16);
	}
	
}
