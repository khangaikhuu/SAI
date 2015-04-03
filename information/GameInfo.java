package information;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BaseLocation;
import task.Task;
import main.Bot;

public class GameInfo {
	
	public Bot root;
	
	public HashMap<UnitType, List<Unit>> myUnits;
	
	public class UnitInfo
	{
		public Task currentTask;
		public boolean destroy;
		public int lastCommandFrame;
		
		public UnitInfo() {
			destroy = false;
		}
	}
	
	UnitInfo[] unitInfo;
	
	public BaseInfo[] bases;
	
	private boolean betterThan(BaseInfo a, BaseInfo b) {
		
		/* Compare method:
		 * 1. should be walkable from my start point
		 * 2. should have gas
		 * 3. should be close to my start point
		 */
		
		Position myStartLocation = root.util.getMyFirstBasePosition();
		TilePosition me = root.util.getNearestTilePosition(myStartLocation);
		TilePosition pa = root.util.getNearestTilePosition(a.position);
		TilePosition pb = root.util.getNearestTilePosition(b.position);
		
		boolean connectA = bwta.BWTA.isConnected(me, pa);
		boolean connectB = bwta.BWTA.isConnected(me, pb);
		
		if(connectA == true && connectB == false) return true;
		if(connectA == false && connectB == true) return false;
		
		if(a.baseLocation.isMineralOnly() == false && b.baseLocation.isMineralOnly() == true) return true;
		if(a.baseLocation.isMineralOnly() == true && b.baseLocation.isMineralOnly() == false) return false;
		
		double distA = bwta.BWTA.getGroundDistance(me, pa);
		double distB = bwta.BWTA.getGroundDistance(me, pb);
		
		return (distA < distB);
	}
	
	public void updateBasesFirstFrame()
	{
		int n = bwta.BWTA.getBaseLocations().size();
		Position myStartLocation = root.util.getMyFirstBasePosition();
		TilePosition me = root.util.getNearestTilePosition(myStartLocation);
		bases = new BaseInfo[n];
		for(int i = 0; i < n; i++)
		{
			bases[i] = new BaseInfo(root);
			BaseLocation b = bwta.BWTA.getBaseLocations().get(i);
			bases[i].baseLocation = b;
			bases[i].position = b.getPosition();
			if(bwta.BWTA.isConnected(me, root.util.getNearestTilePosition(b.getPosition())) == false)
				bases[i].canWalkTo = false;
		}
		
		for(int iteration = 0; iteration < n; iteration ++)
			for(int i = 0; i < n-1; i++)
				if(betterThan(bases[i+1], bases[i]))
				{
					BaseInfo t = bases[i];
					bases[i] = bases[i+1];
					bases[i+1] = t;
				}
		for(int i = 0; i < n; i++)
			bases[i].baseID = i;
		
		
		for(Unit u : root.game.getAllUnits())
		{
			if(u.getType().isMineralField())
			{
				int which = 0;
				for(int i = 0; i < n; i++)
					if(u.getDistance(bases[i].position) < u.getDistance(bases[which].position))
						which = i;
				bases[which].minerals.add(u);
			}
			if(u.getType() == UnitType.Resource_Vespene_Geyser)
			{
				int which = 0;
				for(int i = 0; i < n; i++)
					if(u.getDistance(bases[i].position) < u.getDistance(bases[which].position))
						which = i;
				bases[which].gas.add(u);
			}
			if(root.util.isBase(u.getType()))
			{
				bases[0].myBase = u;
			}
		}
		
		for(int i = 0; i < n; i++)
			bases[i].onFirstFrame();
		
		for(int i = 0; i < Math.min(n, 4); i++)
		{
			if(i == 0)
				bases[i].getBuildingArea(GlobalConstant.Building_Area_X_MainBase, GlobalConstant.Building_Area_Y_MainBase);
			else
				bases[i].getBuildingArea(GlobalConstant.Building_Area_X_OtherBase, GlobalConstant.Building_Area_Y_OtherBase);
		}
		
	}
	
	public int getReadyBases()
	{
		int ret = 0;
		for(int i = 0; i < bases.length; i++)
			if(bases[i].gatherResourceTask != null && bases[i].avaliableMinerals > 4)
				ret ++;
		return ret;
	}
	
	public UnitInfo getUnitInfo(Unit u)
	{
		if(unitInfo[u.getID()] == null)
			unitInfo[u.getID()] = new UnitInfo();
		return unitInfo[u.getID()];
	}
	
	public boolean canStartNewTask(Unit u)
	{
		if(getUnitInfo(u).currentTask != null) return false;
		if(u.isBeingConstructed()) return false;
		if(u.isTraining()) return false;
		return true;
	}
	
	public Task getTask(Unit u)
	{
		return getUnitInfo(u).currentTask;
	}
	
	public void setTask(Unit u, Task t)
	{
		getUnitInfo(u).currentTask= t;
	}
	
	public GameInfo(Bot r) {
		root = r;
		unitInfo = new UnitInfo[GlobalConstant.MAX_UNIT];
		myUnits = new HashMap<UnitType, List<Unit>>();
	}
	
	public void onFrameStart()
	{
		for(UnitType ut: myUnits.keySet())
		{
			List<Unit> nextList = new ArrayList<Unit>();
			for(Unit u : myUnits.get(ut))
			{
				if(getUnitInfo(u).destroy)
					continue;
				nextList.add(u);
			}
			myUnits.put(ut, nextList);
			//root.guiManager.addDebugInfo(ut + " : " + myUnits.get(ut).size());
		}
		
		for(int i = 0; i < bases.length; i++)
			bases[i].onFrame();
		
		
		/*
		for(int i = 0; i < bases.length; i++)
		{
			//System.out.println(i + " : " + bases[i].minerals.size());
			root.game.drawCircleMap(bases[i].position.getX(), bases[i].position.getY(), GlobalConstant.Base_Range, new Color(255, 0, 0));
			for(int j = 0; j < bases[i].minerals.size(); j++)
			{
				Position p = bases[i].minerals.get(j).getPosition();
				int x = p.getX();
				int y = p.getY();
				root.game.drawTextMap(x, y, "Mineral #" + j);
			}
		}
		*/
		
		/*
		for(int i = 0; i < bases.length - 1; i++)
		{
			root.game.drawLineMap(bases[i].position.getX(), bases[i].position.getY(), bases[i+1].position.getX(), bases[i+1].position.getY(), new Color(255, 0, 0));
		}
		*/
		
		
		
	}
	
	public List<Unit> getMyFinishedCombatUnits()
	{
		List<Unit> ret = new ArrayList<Unit>();
		for(UnitType ut : myUnits.keySet())
			if(root.util.isCombatUnit(ut))
				for(Unit u : myUnits.get(ut))
				{
					if(u.isBeingConstructed())
						continue;
					ret.add(u);
				}
		
		return ret;		
	}
	
	public List<Unit> getMyUnitsByType(UnitType t)
	{
		List<Unit> ret = new ArrayList<Unit>();
		if(myUnits.get(t) == null)
			return ret;
		return myUnits.get(t);
	}

	public void onUnitDestroy(Unit u) {
		if(u.getPlayer().getID() == root.self.getID() || u.getPlayer().isNeutral())
			getUnitInfo(u).destroy = true;
	}

	public void onUnitCreate(Unit u) {
		
		if(u.getPlayer().getID() == root.self.getID() || u.getPlayer().isNeutral())
		{
			List<Unit> lis = myUnits.get(u.getType());
			if(lis == null)
			{
				myUnits.put(u.getType(), new ArrayList<Unit>());
				lis = myUnits.get(u.getType());
			}
			lis.add(u);
		}
	}
	
}
