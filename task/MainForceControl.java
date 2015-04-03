package task;

import java.util.ArrayList;
import java.util.List;

import bwapi.Color;
import bwapi.Position;
import bwapi.PositionOrUnit;
import bwapi.Unit;
import bwapi.UnitType;
import main.Bot;

public class MainForceControl extends Task {

	List <Unit> zealots;
	
	public MainForceControl(Bot r) {
		super(r);
		zealots = new ArrayList<Unit>();
		
	}
	
	@Override
	public String getName() {
		
		return "MainForceControl";
	}

	@Override
	public void onFrame() {
		root.guiManager.addDebugInfo("Attack!");
		root.game.drawCircleMap(root.blackboard.getAttackPosition(), (int)information.GlobalConstant.Squad_Radius, new Color(255, 0, 0));
		
		ArrayList<Unit> t = new ArrayList<Unit>();
		for(Unit u : zealots)
		{
			if(root.info.getUnitInfo(u).destroy)
				continue;
			t.add(u);
		}
		zealots = t;
		for(UnitType ut : root.goal.armyUnit)
		{
			if(root.info.myUnits.get(ut) != null)
				for(Unit u : root.info.myUnits.get(ut))
					if(root.info.getUnitInfo(u).currentTask == null)
					{
						root.info.getUnitInfo(u).currentTask = this;
						zealots.add(u);
					}
		}
		
		for(Unit u : zealots)
		{
			int x = root.guiManager.attackPosition.x;
			int y = root.guiManager.attackPosition.y;
			if(u.getDistance(x,  y) > information.GlobalConstant.Squad_Radius && root.blackboard.getIsAttacking() == false)
			{
				
				u.move(new Position(x, y));
			}
			else
			{
				if(root.game.getFrameCount() - root.info.getUnitInfo(u).lastCommandFrame > 50)
				{
					u.attack(new PositionOrUnit(new Position(x, y)));
					root.info.getUnitInfo(u).lastCommandFrame = root.game.getFrameCount();
				}
			}
		}
		
	}

	@Override
	public boolean checkPossible() {
		return true;
	}

	@Override
	public void init() {
		state = TaskState.ACTIVE;
	}

	@Override
	public void checkEnd() {
		
	}

	@Override
	public int needMinerials() {
		
		return 0;
	}

	@Override
	public int needGas() {
		
		return 0;
	}

	@Override
	public int needSupply() {
		
		return 0;
	}

	@Override
	public int needUnit(UnitType u) {
		
		return 0;
	}

	@Override
	public int provideUnit(UnitType u) {
		
		return 0;
	}

	@Override
	public int provideSupply() {
		
		return 0;
	}

	@Override
	public List<Unit> requestUnit(UnitType requestUnit, int numberNeeded) {
		List<Unit> ret = new ArrayList<Unit>();
		return ret;
	}

}
